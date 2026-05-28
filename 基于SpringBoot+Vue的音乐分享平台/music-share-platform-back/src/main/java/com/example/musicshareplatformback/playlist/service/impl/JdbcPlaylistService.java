package com.example.musicshareplatformback.playlist.service.impl;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.media.AudioAccessInfo;
import com.example.musicshareplatformback.media.AudioAccessResolver;
import com.example.musicshareplatformback.playlist.dto.PlaylistDetailResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistListResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistSummary;
import com.example.musicshareplatformback.playlist.dto.PlaylistTrackSummary;
import com.example.musicshareplatformback.playlist.service.PlaylistService;
import com.example.musicshareplatformback.support.PresentationContentOverrides;
import com.example.musicshareplatformback.support.RelativeTimeFormatter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JdbcPlaylistService implements PlaylistService {

    private final JdbcTemplate jdbcTemplate;
    private final AudioAccessResolver audioAccessResolver;

    public JdbcPlaylistService(JdbcTemplate jdbcTemplate, AudioAccessResolver audioAccessResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.audioAccessResolver = audioAccessResolver;
    }

    @Override
    public PlaylistListResponse getPlaylists() {
        List<PlaylistSummary> playlists = jdbcTemplate.query("""
                        select playlist_id, title, curator_name, scene_tag, track_count,
                               follower_count, cover_color, description
                        from playlists
                        where is_public = 1
                        order by follower_count desc, created_at desc
                        """,
                (rs, rowNum) -> {
                    PresentationContentOverrides.PlaylistText text = PresentationContentOverrides.playlist(
                            rs.getString("playlist_id"),
                            rs.getString("title"),
                            rs.getString("scene_tag"),
                            rs.getString("description")
                    );

                    return new PlaylistSummary(
                            rs.getString("playlist_id"),
                            text.title(),
                            rs.getString("curator_name"),
                            text.sceneTag(),
                            rs.getInt("track_count"),
                            rs.getInt("follower_count"),
                            rs.getString("cover_color"),
                            text.description()
                    );
                }
        );

        return new PlaylistListResponse(playlists);
    }

    @Override
    public PlaylistDetailResponse getPlaylistDetail(String playlistId) {
        try {
            PlaylistDetailResponse detail = jdbcTemplate.queryForObject("""
                            select p.playlist_id, p.title, p.curator_name, p.curator_user_id, p.scene_tag, p.track_count,
                                   p.follower_count, p.cover_color, p.description, u.bio as curator_bio,
                                   (select count(*) from comments c where c.playlist_id = p.playlist_id) as comment_count
                            from playlists p
                            left join users u on u.user_id = p.curator_user_id
                            where p.playlist_id = ? and p.is_public = 1
                            """,
                    (rs, rowNum) -> {
                        PresentationContentOverrides.PlaylistText text = PresentationContentOverrides.playlist(
                                rs.getString("playlist_id"),
                                rs.getString("title"),
                                rs.getString("scene_tag"),
                                rs.getString("description")
                        );

                        return new PlaylistDetailResponse(
                                rs.getString("playlist_id"),
                                text.title(),
                                rs.getString("curator_name"),
                                rs.getString("curator_user_id"),
                                PresentationContentOverrides.userBio(rs.getString("curator_user_id"), rs.getString("curator_bio")),
                                text.sceneTag(),
                                rs.getInt("track_count"),
                                rs.getInt("follower_count"),
                                rs.getInt("comment_count"),
                                rs.getString("cover_color"),
                                text.description(),
                                loadPlaylistSongs(playlistId)
                        );
                    },
                    playlistId
            );

            if (detail == null) {
                throw new IllegalArgumentException("目标歌单不存在。");
            }

            return detail;
        } catch (EmptyResultDataAccessException exception) {
            throw new IllegalArgumentException("目标歌单不存在。");
        }
    }

    @Override
    public List<CommentSummary> getPlaylistComments(String playlistId) {
        return jdbcTemplate.query("""
                        select c.comment_id, u.display_name, c.content, c.like_count, c.created_at, p.title
                        from comments c
                        join users u on u.user_id = c.user_id
                        join playlists p on p.playlist_id = c.playlist_id
                        where c.playlist_id = ?
                        order by c.created_at desc
                        """,
                (rs, rowNum) -> new CommentSummary(
                        rs.getString("comment_id"),
                        rs.getString("display_name"),
                        "PLAYLIST",
                        PresentationContentOverrides.playlist(playlistId, rs.getString("title"), "", "").title(),
                        PresentationContentOverrides.commentContent(rs.getString("comment_id"), rs.getString("content")),
                        rs.getInt("like_count"),
                        RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                ),
                playlistId
        );
    }

    private List<PlaylistTrackSummary> loadPlaylistSongs(String playlistId) {
        return jdbcTemplate.query("""
                        select s.song_id, s.title, s.artist_name, s.genre_name, s.duration_text, s.like_count, s.audio_url
                        from playlist_songs ps
                        join songs s on s.song_id = ps.song_id
                        where ps.playlist_id = ?
                        order by ps.sort_order asc
                        """,
                (rs, rowNum) -> {
                    AudioAccessInfo accessInfo = audioAccessResolver.resolve(rs.getString("audio_url"));
                    return new PlaylistTrackSummary(
                            rs.getString("song_id"),
                            rs.getString("title"),
                            rs.getString("artist_name"),
                            rs.getString("genre_name"),
                            rs.getString("duration_text"),
                            rs.getInt("like_count"),
                            accessInfo.streamUrl(),
                            accessInfo.downloadUrl(),
                            accessInfo.available(),
                            accessInfo.sourceType()
                    );
                },
                playlistId
        );
    }
}
