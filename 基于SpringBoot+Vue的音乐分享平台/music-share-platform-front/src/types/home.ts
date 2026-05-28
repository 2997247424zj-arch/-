export interface PlatformOverview {
  totalTracks: number
  totalPlaylists: number
  activeCreators: number
  dailyShares: number
}

export interface GenreSummary {
  id: string
  name: string
  description: string
  accentColor: string
  trackCount: number
}

export interface TrackSummary {
  id: string
  title: string
  artist: string
  genre: string
  moodTag: string
  durationText: string
  description: string
  highlightColor: string
  likes: number
  streamUrl: string | null
  downloadUrl: string | null
  streamAvailable: boolean
  audioSourceType: string
}

export interface HomePlayableTrack {
  id: string
  title: string
  artist: string
  genre: string
  moodTag: string
  durationText: string
  description: string
  highlightColor: string
  streamUrl: string | null
  downloadUrl: string | null
  streamAvailable: boolean
  audioSourceType: string
}

export interface PlaylistSummary {
  id: string
  title: string
  curator: string
  description: string
  trackCount: number
  followers: number
}

export interface ActivitySummary {
  id: string
  userName: string
  action: string
  targetName: string
  relativeTime: string
}

export interface HomePageResponse {
  overview: PlatformOverview
  genres: GenreSummary[]
  trendingTracks: TrackSummary[]
  recommendedSleepTracks: HomePlayableTrack[]
  featuredPlaylists: PlaylistSummary[]
  latestActivities: ActivitySummary[]
  deliveryRoadmap: string[]
}
