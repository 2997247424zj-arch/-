#!/usr/bin/env node
import fs from 'node:fs/promises'
import vm from 'node:vm'

const DEFAULT_BASE_URL = 'https://music.cpp-prog.com/'
const DEFAULT_JSON_OUT = 'cpp_prog_music_catalog.json'
const DEFAULT_SQL_OUT = 'cpp_prog_music_import.sql'

const args = parseArgs(process.argv.slice(2))
const baseUrl = ensureTrailingSlash(args.base ?? DEFAULT_BASE_URL)
const jsonOut = args.out ?? DEFAULT_JSON_OUT
const sqlOut = args.sql ?? DEFAULT_SQL_OUT
const resolveTemp = Boolean(args['resolve-temp'])
const maxTracks = Number(args['max-tracks'] ?? 0)
const concurrency = Math.max(1, Number(args.concurrency ?? 6))
const timeoutMs = Math.max(1000, Number(args.timeout ?? 15000))

const fetchedAt = new Date().toISOString()

main().catch((error) => {
  console.error(error instanceof Error ? error.message : error)
  process.exitCode = 1
})

async function main() {
  const musicListUrl = new URL('js/musicList.js', baseUrl).href
  const musicListSource = await fetchText(musicListUrl)
  const musicList = evaluateMusicList(musicListSource)

  const playlistSeeds = musicList
    .map((item, index) => ({ ...item, listIndex: index }))
    .filter((item) => item.id || Array.isArray(item.item))

  const playlistResults = []
  for (const seed of playlistSeeds) {
    if (seed.id) {
      playlistResults.push(await loadRemotePlaylist(seed.id))
    } else if (Array.isArray(seed.item) && seed.item.length > 0) {
      playlistResults.push(normalizeManualPlaylist(seed))
    }
  }

  const trackMap = new Map()
  const playlistTracks = []
  for (const playlist of playlistResults) {
    for (const [index, track] of playlist.tracks.entries()) {
      if (!trackMap.has(track.id)) {
        trackMap.set(track.id, track)
      }
      playlistTracks.push({
        playlistId: playlist.id,
        songId: track.id,
        sortOrder: index + 1,
      })
    }
  }

  let tracks = [...trackMap.values()]
  if (maxTracks > 0) {
    tracks = tracks.slice(0, maxTracks)
  }

  if (resolveTemp) {
    tracks = await mapLimit(tracks, concurrency, async (track) => {
      const resolvedUrl = await resolveTemporaryUrl(track)
      return {
        ...track,
        resolvedUrl,
        resolvedAt: new Date().toISOString(),
      }
    })
  }

  const limitedTrackIds = new Set(tracks.map((track) => track.id))
  const output = {
    sourcePage: baseUrl,
    musicListUrl,
    fetchedAt,
    resolveTemp,
    playlistCount: playlistResults.length,
    trackCount: tracks.length,
    playlists: playlistResults.map((playlist) => ({
      ...playlist,
      tracks: playlist.tracks.filter((track) => limitedTrackIds.has(track.id)).map((track) => track.id),
    })),
    tracks,
    playlistTracks: playlistTracks.filter((item) => limitedTrackIds.has(item.songId)),
  }

  await fs.writeFile(jsonOut, `${JSON.stringify(output, null, 2)}\n`, 'utf8')
  await fs.writeFile(sqlOut, buildSql(output), 'utf8')

  console.log(`Fetched playlists: ${output.playlistCount}`)
  console.log(`Fetched tracks: ${output.trackCount}`)
  console.log(`JSON written: ${jsonOut}`)
  console.log(`SQL written: ${sqlOut}`)
}

async function loadRemotePlaylist(playlistId) {
  const apiUrl = new URL('api.php', baseUrl)
  apiUrl.searchParams.set('types', 'playlist')
  apiUrl.searchParams.set('id', playlistId)

  const data = await fetchJson(apiUrl.href)
  const playlist = data.playlist ?? {}
  const tracks = Array.isArray(playlist.tracks) ? playlist.tracks : []

  return {
    id: `cpp-prog-${playlist.id ?? playlistId}`,
    sourcePlaylistId: String(playlist.id ?? playlistId),
    title: safeText(playlist.name, `cpp-prog playlist ${playlistId}`),
    coverUrl: safeText(playlist.coverImgUrl, ''),
    curatorName: safeText(playlist.creator?.nickname, 'music.cpp-prog'),
    curatorAvatar: safeText(playlist.creator?.avatarUrl, ''),
    sourceUrl: apiUrl.href,
    tracks: tracks.map(normalizeNeteaseTrack),
  }
}

function normalizeManualPlaylist(seed) {
  return {
    id: `cpp-prog-manual-${seed.listIndex}`,
    sourcePlaylistId: String(seed.listIndex),
    title: safeText(seed.name, `manual playlist ${seed.listIndex}`),
    coverUrl: safeText(seed.cover, ''),
    curatorName: safeText(seed.creatorName, 'music.cpp-prog'),
    curatorAvatar: safeText(seed.creatorAvatar, ''),
    sourceUrl: baseUrl,
    tracks: seed.item.map(normalizeManualTrack),
  }
}

function normalizeNeteaseTrack(track) {
  const neteaseId = String(track.id)
  const durationSeconds = Math.max(0, Math.floor(Number(track.dt ?? 0) / 1000))
  const artist = Array.isArray(track.ar)
    ? track.ar.map((item) => item?.name).filter(Boolean).join(' / ')
    : ''

  return {
    id: `netease-${neteaseId}`,
    source: 'netease',
    sourceSongId: neteaseId,
    title: safeText(track.name, `NetEase ${neteaseId}`),
    artist: safeText(artist, 'Unknown Artist'),
    album: safeText(track.al?.name, ''),
    durationSeconds,
    durationText: formatDuration(durationSeconds),
    coverUrl: safeText(track.al?.picUrl, ''),
    outerUrl: neteaseOuterUrl(neteaseId),
    resolvedUrl: null,
  }
}

function normalizeManualTrack(track) {
  const source = safeText(track.source, 'netease')
  const sourceSongId = String(track.id ?? track.url_id ?? '')
  return {
    id: `${source}-${sourceSongId}`,
    source,
    sourceSongId,
    title: safeText(track.name, `Manual ${sourceSongId}`),
    artist: safeText(track.artist, 'Unknown Artist'),
    album: safeText(track.album, ''),
    durationSeconds: 0,
    durationText: '00:00',
    coverUrl: safeText(track.pic, ''),
    outerUrl: safeText(track.url, '') || (source === 'netease' ? neteaseOuterUrl(sourceSongId) : ''),
    resolvedUrl: null,
  }
}

async function resolveTemporaryUrl(track) {
  if (!track.sourceSongId || !track.source) {
    return track.outerUrl
  }

  const apiUrl = new URL('api.php', baseUrl)
  apiUrl.searchParams.set('types', 'url')
  apiUrl.searchParams.set('id', track.sourceSongId)
  apiUrl.searchParams.set('source', track.source)

  try {
    const data = await fetchJson(apiUrl.href)
    return safeText(data.url, '') || track.outerUrl
  } catch {
    return track.outerUrl
  }
}

function evaluateMusicList(source) {
  const context = {}
  vm.createContext(context)
  vm.runInContext(`${source}\n;musicList;`, context, {
    timeout: 1000,
    filename: 'musicList.js',
  })

  if (!Array.isArray(context.musicList)) {
    throw new Error('musicList.js did not expose a musicList array.')
  }

  return context.musicList
}

async function fetchJson(url) {
  const text = await fetchText(url)
  return JSON.parse(stripJsonp(text))
}

async function fetchText(url, attempt = 1) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)

  try {
    const response = await fetch(url, {
      signal: controller.signal,
      headers: {
        accept: 'application/json,text/javascript,text/plain,*/*',
        'user-agent': 'MusicSharePlatformCrawler/1.0',
      },
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status} for ${url}`)
    }

    return await response.text()
  } catch (error) {
    if (attempt < 3) {
      await sleep(500 * attempt)
      return fetchText(url, attempt + 1)
    }
    throw error
  } finally {
    clearTimeout(timer)
  }
}

async function mapLimit(items, limit, worker) {
  const results = new Array(items.length)
  let nextIndex = 0

  async function run() {
    while (nextIndex < items.length) {
      const currentIndex = nextIndex
      nextIndex += 1
      results[currentIndex] = await worker(items[currentIndex], currentIndex)
    }
  }

  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, run))
  return results
}

function buildSql(output) {
  const uniquePlaylistRows = output.playlists.map((playlist) => {
    const trackCount = output.playlistTracks.filter((item) => item.playlistId === playlist.id).length
    return [
      playlist.id,
      clip(playlist.title, 150),
      'creator-1',
      clip(playlist.curatorName, 100),
      'music.cpp-prog',
      clip(`Fetched from ${output.sourcePage}`, 255),
      trackCount,
      0,
      '#8aa1ff',
      1,
    ]
  })

  const songRows = output.tracks.map((track, index) => [
    track.id,
    clip(track.title, 150),
    clip(track.artist, 100),
    'Imported',
    'music.cpp-prog',
    track.durationSeconds,
    track.durationText,
    Math.max(0, 10000 - index * 8),
    Math.max(0, 800 - index),
    colorForIndex(index),
    clip(`Fetched from ${output.sourcePage}; source=${track.source}; id=${track.sourceSongId}`, 255),
    clip(track.outerUrl || track.resolvedUrl, 255),
    clip(track.coverUrl ? `${track.coverUrl}${track.coverUrl.includes('?') ? '' : '?param=300y300'}` : '', 255),
    'PUBLISHED',
    'creator-1',
  ])

  const playlistSongRows = output.playlistTracks.map((item) => [
    item.playlistId,
    item.songId,
    item.sortOrder,
  ])

  const tagRows = output.tracks.flatMap((track) => [
    [track.id, 'music.cpp-prog'],
    [track.id, track.source],
  ])

  return [
    '-- Generated by crawl_cpp_prog_music.mjs',
    `-- Source: ${output.sourcePage}`,
    `-- Fetched at: ${output.fetchedAt}`,
    '',
    'INSERT INTO `songs` (`song_id`, `title`, `artist_name`, `genre_name`, `mood_tag`, `duration_seconds`, `duration_text`, `play_count`, `like_count`, `highlight_color`, `description`, `audio_url`, `cover_url`, `release_status`, `uploaded_by_user_id`, `created_at`) VALUES',
    `${songRows.map(sqlTupleWithNow).join(',\n')}\nON DUPLICATE KEY UPDATE`,
    '  `title` = VALUES(`title`), `artist_name` = VALUES(`artist_name`), `genre_name` = VALUES(`genre_name`), `mood_tag` = VALUES(`mood_tag`),',
    '  `duration_seconds` = VALUES(`duration_seconds`), `duration_text` = VALUES(`duration_text`), `play_count` = VALUES(`play_count`),',
    '  `like_count` = VALUES(`like_count`), `highlight_color` = VALUES(`highlight_color`), `description` = VALUES(`description`),',
    '  `audio_url` = VALUES(`audio_url`), `cover_url` = VALUES(`cover_url`), `release_status` = VALUES(`release_status`),',
    '  `uploaded_by_user_id` = VALUES(`uploaded_by_user_id`);',
    '',
    'INSERT INTO `playlists` (`playlist_id`, `title`, `curator_user_id`, `curator_name`, `scene_tag`, `description`, `track_count`, `follower_count`, `cover_color`, `is_public`, `created_at`) VALUES',
    `${uniquePlaylistRows.map(sqlTupleWithNow).join(',\n')}\nON DUPLICATE KEY UPDATE`,
    '  `title` = VALUES(`title`), `curator_user_id` = VALUES(`curator_user_id`), `curator_name` = VALUES(`curator_name`),',
    '  `scene_tag` = VALUES(`scene_tag`), `description` = VALUES(`description`), `track_count` = VALUES(`track_count`),',
    '  `follower_count` = VALUES(`follower_count`), `cover_color` = VALUES(`cover_color`), `is_public` = VALUES(`is_public`);',
    '',
    'INSERT INTO `playlist_songs` (`playlist_id`, `song_id`, `sort_order`, `added_at`) VALUES',
    `${playlistSongRows.map(sqlTupleWithNow).join(',\n')}\nON DUPLICATE KEY UPDATE`,
    '  `sort_order` = VALUES(`sort_order`), `added_at` = VALUES(`added_at`);',
    '',
    'INSERT IGNORE INTO `song_tags` (`song_id`, `tag_name`) VALUES',
    `${tagRows.map(sqlTuple).join(',\n')};`,
    '',
  ].join('\n')
}

function sqlTuple(values) {
  return `  (${values.map(sqlValue).join(', ')})`
}

function sqlTupleWithNow(values) {
  return `  (${values.map(sqlValue).join(', ')}, NOW())`
}

function sqlValue(value) {
  if (value === null || value === undefined || value === '') {
    return 'NULL'
  }
  if (typeof value === 'number') {
    return String(value)
  }
  return `'${String(value).replaceAll('\\', '\\\\').replaceAll("'", "''")}'`
}

function stripJsonp(text) {
  const trimmed = text.trim()
  const match = trimmed.match(/^[\w$.]+\(([\s\S]*)\);?$/)
  return match ? match[1] : trimmed
}

function parseArgs(rawArgs) {
  const parsed = {}
  for (let index = 0; index < rawArgs.length; index += 1) {
    const arg = rawArgs[index]
    if (!arg.startsWith('--')) {
      continue
    }

    const [key, inlineValue] = arg.slice(2).split('=', 2)
    if (inlineValue !== undefined) {
      parsed[key] = inlineValue
    } else if (rawArgs[index + 1] && !rawArgs[index + 1].startsWith('--')) {
      parsed[key] = rawArgs[index + 1]
      index += 1
    } else {
      parsed[key] = true
    }
  }
  return parsed
}

function ensureTrailingSlash(value) {
  return value.endsWith('/') ? value : `${value}/`
}

function safeText(value, fallback) {
  if (value === null || value === undefined) {
    return fallback
  }
  const text = String(value).trim()
  return text || fallback
}

function formatDuration(totalSeconds) {
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

function neteaseOuterUrl(id) {
  return `https://music.163.com/song/media/outer/url?id=${id}.mp3`
}

function colorForIndex(index) {
  const colors = ['#8aa1ff', '#6fcf97', '#56ccf2', '#bb6bd9', '#f4b942', '#ff8a5b']
  return colors[index % colors.length]
}

function clip(value, maxLength) {
  const text = safeText(value, '')
  return text.length > maxLength ? text.slice(0, maxLength) : text
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}
