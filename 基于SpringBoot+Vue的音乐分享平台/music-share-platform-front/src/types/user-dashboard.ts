export interface RecentActionSummary {
  id: string
  type: string
  title: string
  subtitle: string
  relativeTime: string
}

export interface UserDashboardResponse {
  userId: string
  displayName: string
  role: string
  favoriteGenre: string
  bio: string
  totalFavorites: number
  totalLikes: number
  totalComments: number
  recentFavorites: RecentActionSummary[]
  recentLikes: RecentActionSummary[]
  recentComments: RecentActionSummary[]
}
