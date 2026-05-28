把可在线播放、可免费下载的音频文件放到这个目录。

当前数据库里的 `songs.audio_url` 例子是：

- `/media/audio/midnight-run.mp3`
- `/media/audio/paper-sun.mp3`
- `/media/audio/noise-map.mp3`

后端会把这些文件名映射到：

- 播放地址：`/api/media/audio/{fileName}`
- 下载地址：`/api/media/audio/{fileName}/download`

因此你只需要把文件名对上的音频放进这里，例如：

- `midnight-run.mp3`
- `paper-sun.mp3`
- `noise-map.mp3`

支持的常见格式：

- `.mp3`
- `.wav`
- `.ogg`
- `.m4a`
- `.flac`
