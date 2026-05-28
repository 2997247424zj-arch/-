export interface SongSummary {
  id: string
  title: string
  artist: string
  genre: string
  moodTag: string
  durationText: string
  playCount: number
  likeCount: number
  highlightColor: string
  description: string
  streamUrl: string | null
  downloadUrl: string | null
  streamAvailable: boolean
  audioSourceType: string
}

export interface SongListResponse {
  availableGenres: string[]
  songs: SongSummary[]
}

export interface SongDetailResponse {
  id: string
  title: string
  artist: string
  genre: string
  moodTag: string
  durationText: string
  playCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  highlightColor: string
  description: string
  audioUrl: string
  streamUrl: string | null
  downloadUrl: string | null
  streamAvailable: boolean
  audioSourceType: string
  coverUrl: string
  uploaderName: string
  uploaderBio: string
  tags: string[]
}

export interface PlaylistSummary {
  id: string
  title: string
  curator: string
  sceneTag: string
  trackCount: number
  followers: number
  coverColor: string
  description: string
}

export interface PlaylistListResponse {
  playlists: PlaylistSummary[]
}

export interface PlaylistTrackSummary {
  id: string
  title: string
  artist: string
  genre: string
  durationText: string
  likeCount: number
  streamUrl: string | null
  downloadUrl: string | null
  streamAvailable: boolean
  audioSourceType: string
}

export interface PlaylistDetailResponse {
  id: string
  title: string
  curator: string
  curatorUserId: string
  curatorBio: string
  sceneTag: string
  trackCount: number
  followers: number
  commentCount: number
  coverColor: string
  description: string
  songs: PlaylistTrackSummary[]
}

export interface FavoriteResponse {
  targetType: string
  targetId: string
  totalFavorites: number
}

export interface LikeResponse {
  targetType: string
  targetId: string
  totalLikes: number
}

export interface CreateSongResponse {
  songId: string
  title: string
  audioUrl: string
  streamUrl: string
  downloadUrl: string
  audioSourceType: string
}

export interface UploadAudioResponse {
  audioUrl: string
  streamUrl: string
  downloadUrl: string
  fileName: string
  sourceType: string
}
