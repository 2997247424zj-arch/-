const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: BodyInit | unknown
  token?: string
  isFormData?: boolean
}

// --- GET 缓存与请求去重 ---
const CACHE_TTL = 30_000

const cache = new Map<string, { data: unknown; expiresAt: number }>()
const inFlight = new Map<string, Promise<unknown>>()

async function requestJson<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const response = await fetch(`${API_BASE_URL}${normalizedPath}`, {
    method: options.method ?? 'GET',
    headers: {
      Accept: 'application/json',
      ...(options.body && !options.isFormData ? { 'Content-Type': 'application/json' } : {}),
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
    },
    body: options.body
      ? options.isFormData
        ? (options.body as BodyInit)
        : JSON.stringify(options.body)
      : undefined,
  })

  const data = (await response.json()) as { message?: string }
  if (!response.ok) {
    throw new Error(data.message ?? `Request failed with status ${response.status}`)
  }

  return data as T
}

export function getJson<T>(path: string, token?: string): Promise<T> {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (token) {
    return requestJson<T>(normalizedPath, { token })
  }

  // 1. 命中未过期缓存，直接返回
  const cached = cache.get(normalizedPath)
  if (cached && Date.now() < cached.expiresAt) {
    return Promise.resolve(cached.data as T)
  }

  // 2. 相同路径已有进行中请求，复用
  const existing = inFlight.get(normalizedPath)
  if (existing) {
    return existing as Promise<T>
  }

  // 3. 发起请求，缓存结果，清理 inFlight
  const promise = requestJson<T>(normalizedPath).then((data) => {
    cache.set(normalizedPath, { data, expiresAt: Date.now() + CACHE_TTL })
    inFlight.delete(normalizedPath)
    return data
  }).catch((err: unknown) => {
    inFlight.delete(normalizedPath)
    throw err
  })

  inFlight.set(normalizedPath, promise as Promise<unknown>)
  return promise
}

export function postJson<T>(path: string, body: unknown, token?: string): Promise<T> {
  return requestJson<T>(path, { method: 'POST', body, token })
}

export function postForm<T>(path: string, body: FormData, token?: string): Promise<T> {
  return requestJson<T>(path, { method: 'POST', body, token, isFormData: true })
}
