export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  displayName: string
  role: 'USER' | 'CREATOR'
  favoriteGenre: string
  bio: string
}

export interface UserProfile {
  id: string
  username: string
  displayName: string
  role: string
  favoriteGenre: string
  bio: string
}

export interface LoginResponse {
  accessToken: string
  profile: UserProfile
  capabilities: string[]
}
