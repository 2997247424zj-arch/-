import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

function scrollBehaviorMode(): ScrollBehavior {
  if (typeof window === 'undefined') {
    return 'auto'
  }

  return window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
}

function safeRedirect(value: unknown) {
  if (typeof value !== 'string') {
    return ''
  }

  return value.startsWith('/') && !value.startsWith('/login') ? value : ''
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', meta: { depth: 0 }, component: () => import('../views/HomeView.vue') },
    { path: '/discover', name: 'discover', meta: { depth: 1 }, component: () => import('../views/DiscoverView.vue') },
    { path: '/library', name: 'library', meta: { depth: 2 }, component: () => import('../views/LibraryView.vue') },
    { path: '/community', name: 'community', meta: { depth: 3 }, component: () => import('../views/CommunityView.vue') },
    { path: '/login', name: 'login', meta: { depth: 4 }, component: () => import('../views/LoginView.vue') },
    { path: '/songs/:id', name: 'song-detail', meta: { depth: 2.5 }, component: () => import('../views/SongDetailView.vue') },
    {
      path: '/playlists/:id',
      name: 'playlist-detail',
      meta: { depth: 2.5 },
      component: () => import('../views/PlaylistDetailView.vue'),
    },
    { path: '/me', name: 'me', meta: { depth: 5, requiresAuth: true }, component: () => import('../views/MyView.vue') },
    {
      path: '/creator',
      name: 'creator',
      meta: { depth: 6, requiresAuth: true, requiresCreator: true },
      component: () => import('../views/CreatorStudioView.vue'),
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    const behavior = scrollBehaviorMode()
    if (savedPosition) {
      return { ...savedPosition, behavior }
    }
    if (to.hash) {
      return { el: to.hash, behavior }
    }
    return { top: 0, behavior }
  },
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (!authStore.isAuthenticated) {
    authStore.restoreSession()
  }

  if (to.name === 'login' && authStore.isAuthenticated) {
    return safeRedirect(to.query.redirect) || '/library'
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.requiresCreator) {
    const role = authStore.profile?.role
    if (role !== 'CREATOR' && role !== 'ADMIN') {
      return '/library'
    }
  }
})

export default router
