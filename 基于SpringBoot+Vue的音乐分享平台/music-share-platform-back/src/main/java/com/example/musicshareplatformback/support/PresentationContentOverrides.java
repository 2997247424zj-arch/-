package com.example.musicshareplatformback.support;

import java.util.Map;

public final class PresentationContentOverrides {

    private static final Map<String, String> USER_BIOS = Map.of(
            "admin-1", "负责审核推荐内容与维护首页运营位。",
            "creator-1", "上传独立流行和夜行感编曲 demo。",
            "user-1", "习惯收藏学习向歌单和深夜电台内容。",
            "user-2", "会定期整理工作日晚间歌单。",
            "user-3", "偏爱节奏感明确的夜跑歌单。",
            "user-4", "喜欢低保真和学习循环播放内容。",
            "user-5", "偏爱现场回放和编曲实验。",
            "user-6", "擅长整理通勤和晚归歌单。",
            "user-7", "持续收录 live session 和小型现场片段。"
    );

    private static final Map<String, SongText> SONG_TEXTS = Map.of(
            "song-1", new SongText("夜行感", "适合夜跑、车窗反光和加速感画面的电子单曲。"),
            "song-2", new SongText("清晨通透", "带一点木吉他和轻拍节奏的独立流行作品。"),
            "song-3", new SongText("专注循环", "适合学习和长时间专注的低保真循环节拍。"),
            "song-4", new SongText("海风复古", "复古合成器与轻人声结合的 city pop demo。"),
            "song-5", new SongText("空间扩散", "更偏氛围电子的扩散型编排，适合耳机收听。"),
            "song-6", new SongText("书桌夜灯", "偏温暖的 lo-fi 鼓组和磁带噪点底色。"),
            "song-7", new SongText("晚风通勤", "更偏流行向的 city pop 单曲，适合晚归通勤。"),
            "song-8", new SongText("房间广播", "吉他和轻人声编织出的室内广播感。"),
            "song-9", new SongText("雨后晚归", "适合傍晚回家路上的放松型 city pop。")
    );

    private static final Map<String, PlaylistText> PLAYLIST_TEXTS = Map.of(
            "playlist-1", new PlaylistText("深夜赶工不掉线", "学习专注", "适合写代码、剪片和整理灵感的稳定节奏歌单。"),
            "playlist-2", new PlaylistText("雨后开车回家", "通勤驾驶", "偏晚归路程感的 city pop、轻电子和氛围人声。"),
            "playlist-3", new PlaylistText("小型现场精选", "现场回放", "汇总独立音乐现场片段、彩排录音和氛围版改编。"),
            "playlist-4", new PlaylistText("周末房间广播", "房间电台", "适合独处、收纳和阅读时段的温和播放队列。")
    );

    private static final Map<String, String> GENRE_DESCRIPTIONS = Map.of(
            "City Pop", "适合夜跑和通勤的复古合成器声音。",
            "Electronic", "适合舞曲、氛围电子和节奏实验。",
            "Indie", "适合挖掘独立音乐人和新鲜 live session。",
            "Lo-fi", "低保真节拍与学习场景高度匹配。"
    );

    private static final Map<String, String> GENRE_COLORS = Map.of(
            "City Pop", "#ff8a5b",
            "Electronic", "#9b51e0",
            "Indie", "#4f7cff",
            "Lo-fi", "#6fcf97"
    );

    private static final Map<String, String> COMMENT_TEXTS = Map.of(
            "comment-1", "这个鼓点特别适合夜跑，副歌提速很明显。",
            "comment-2", "前奏一出来就有清晨透气感，适合通勤。",
            "comment-3", "循环学习的时候很稳，不会打断注意力。",
            "comment-4", "这个歌单的节奏分布很好，适合一整晚工作。",
            "comment-5", "这个 demo 版本保留了很好的海风感混响。",
            "comment-6", "我也喜欢它在第二段鼓组的推进。"
    );

    private static final Map<String, String> ACTIVITY_ACTIONS = Map.of(
            "activity-1", "分享了歌单",
            "activity-2", "发布了评论",
            "activity-3", "收藏了歌曲",
            "activity-4", "上传了新 demo"
    );

    private static final Map<String, String> ACTIVITY_TARGETS = Map.of(
            "activity-1", "深夜赶工不掉线",
            "activity-2", "Midnight Run",
            "activity-3", "Paper Sun",
            "activity-4", "Ocean Drive Demo"
    );

    private PresentationContentOverrides() {
    }

    public static String userBio(String userId, String fallback) {
        return USER_BIOS.getOrDefault(userId, fallback);
    }

    public static SongText song(String songId, String fallbackMoodTag, String fallbackDescription) {
        return SONG_TEXTS.getOrDefault(songId, new SongText(fallbackMoodTag, fallbackDescription));
    }

    public static PlaylistText playlist(String playlistId, String fallbackTitle, String fallbackSceneTag, String fallbackDescription) {
        return PLAYLIST_TEXTS.getOrDefault(playlistId, new PlaylistText(fallbackTitle, fallbackSceneTag, fallbackDescription));
    }

    public static String genreDescription(String genreName) {
        return GENRE_DESCRIPTIONS.getOrDefault(genreName, "适合继续扩展标签、推荐和场景分类。");
    }

    public static String genreColor(String genreName, String fallbackColor) {
        return GENRE_COLORS.getOrDefault(genreName, fallbackColor == null || fallbackColor.isBlank() ? "#3858ff" : fallbackColor);
    }

    public static String commentContent(String commentId, String fallback) {
        return COMMENT_TEXTS.getOrDefault(commentId, fallback);
    }

    public static String activityAction(String activityId, String fallback) {
        return ACTIVITY_ACTIONS.getOrDefault(activityId, fallback);
    }

    public static String activityTarget(String activityId, String fallback) {
        return ACTIVITY_TARGETS.getOrDefault(activityId, fallback);
    }

    public record SongText(
            String moodTag,
            String description
    ) {
    }

    public record PlaylistText(
            String title,
            String sceneTag,
            String description
    ) {
    }
}
