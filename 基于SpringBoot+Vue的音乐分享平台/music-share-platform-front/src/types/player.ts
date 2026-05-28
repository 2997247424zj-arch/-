export interface PlayerTrack {
  id: string
  title: string
  artist: string
  genre?: string
  moodTag?: string
  description?: string
  streamUrl: string | null
  downloadUrl?: string | null
  audioSourceType?: string
  highlightColor?: string
}
