import type { ActivitySummary } from '@/types/home'

export interface CommentSummary {
  id: string
  userName: string
  targetType: string
  targetName: string
  content: string
  likeCount: number
  relativeTime: string
}

export interface CommunityResponse {
  totalComments: number
  totalPlaylists: number
  totalCreators: number
  latestActivities: ActivitySummary[]
  latestComments: CommentSummary[]
}
