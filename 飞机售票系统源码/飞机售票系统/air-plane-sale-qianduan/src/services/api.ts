// API服务配置和接口定义

// 基础配置
// 注意：Spring Boot默认端口是8080，如果使用代理或不同端口，请通过环境变量VITE_API_BASE_URL配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const REQUEST_TIMEOUT = 10000

const roleHomeMap: Record<string, string> = {
  admin: '/dashboard',
  operator: '/portal/operations',
  passenger: '/portal/passengers'
}

const normalizeUserPayload = (user?: any) => {
  const raw = user || {}
  const role = raw.role || 'passenger'
  const homePath = raw.homePath || roleHomeMap[role] || roleHomeMap.passenger

  // 字段规范化：兼容不同后端字段命名
  const phone = raw.phone ?? raw.mobile ?? raw.tel ?? raw.phoneNumber ?? raw.phone_number ?? ''
  const idCard = raw.idCard ?? raw.id_card ?? raw.idNo ?? raw.id_no ?? ''
  const email = raw.email ?? raw.mail ?? raw.emailAddress ?? raw.email_address ?? ''
  const realName = raw.realName ?? raw.real_name ?? raw.name ?? ''
  const username = raw.username ?? raw.user_name ?? raw.account ?? ''
  const registeredAt = raw.registeredAt ?? raw.register_time ?? raw.registerTime ?? raw.createdAt ?? raw.created_at ?? raw.gmtCreate ?? ''

  return {
    ...raw,
    phone,
    idCard,
    email,
    realName,
    username,
    registeredAt,
    role,
    homePath
  }
}

const mockAccounts = [
  {
    username: 'demo',
    password: 'password123',
    user: {
      id: '1',
      username: 'demo',
      realName: '系统管理员',
      email: 'demo@example.com',
      phone: '13800000000',
      role: 'admin',
      homePath: roleHomeMap.admin
    }
  },
  {
    username: 'occ_admin',
    password: 'occ123',
    user: {
      id: '2',
      username: 'occ_admin',
      realName: '运行指挥官',
      email: 'occ@example.com',
      phone: '13900000000',
      role: 'operator',
      homePath: roleHomeMap.operator
    }
  },
  {
    username: 'vip_pax',
    password: 'pax123',
    user: {
      id: '3',
      username: 'vip_pax',
      realName: '乘客',
      email: 'pax@example.com',
      phone: '13700000000',
      role: 'passenger',
      homePath: roleHomeMap.passenger
    }
  }
]

// 管理后台 - 模拟的航班数据存储（用于本地开发/后端未启动时的回退）
const mockAdminFlights: any[] = [
  {
    id: 'mock-1001',
    departureDate: '2025-12-25',
    flightNumber: 'MOCK1001',
    airline: '示例航空',
    aircraftModel: '波音737-800',
    departure: '北京首都国际机场',
    destination: '上海浦东国际机场',
    departureTime: '2025-12-25T08:30:00',
    duration: '2小时10分',
    quantity: 200,
    price: 680,
    companyAccount: 'mock_admin'
  },
  {
    id: 'mock-1002',
    departureDate: '2025-12-26',
    flightNumber: 'MOCK1002',
    airline: '示例航空',
    aircraftModel: '空客A320',
    departure: '广州白云机场',
    destination: '深圳宝安机场',
    departureTime: '2025-12-26T10:00:00',
    duration: '1小时10分',
    quantity: 150,
    price: 420,
    companyAccount: 'mock_admin'
  }
]
let mockAdminNextId = 1100

// 请求拦截器
const requestInterceptor = (config: any) => {
  // 添加内容类型（文件上传时不设置，让浏览器自动设置）
  if (!(config.body instanceof FormData)) {
    config.headers = config.headers || {}
    config.headers['Content-Type'] = 'application/json'
  }
  // 携带凭证（Cookie）以支持会话登录与受保护接口
  // 注意：后端需开启 CORS 且允许凭证：Access-Control-Allow-Credentials: true
  // 且 Access-Control-Allow-Origin 不能为 "*"，需为具体源
  config.credentials = config.credentials || 'include'

  // 附带token（若后端使用JWT/Token鉴权）
  try {
    const token = sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token')
    if (token) {
      config.headers = config.headers || {}
      if (!config.headers['Authorization']) {
        config.headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
      }
    }
  } catch {}
  
  return config
}

// 响应拦截器
const responseInterceptor = (response: any) => {
  if (response.status >= 200 && response.status < 300) {
    // 后端返回统一格式：{success: boolean, message: string, data: any}
    const data = response.data
    // 如果后端返回错误，抛出异常
    if (data && typeof data === 'object' && 'success' in data) {
      if (data.success === false) {
        throw new Error(data.message || '请求失败')
      }
      // 返回完整的响应对象，让各个 API 方法自己决定如何处理
      // 登录接口需要完整的 {success, message, data} 格式
    }
    return data
  } else {
    throw new Error(response.statusText || '请求失败')
  }
}

// 错误处理
const errorHandler = (error: any) => {
  // 如果是AbortError（超时），直接抛出
  if (error.name === 'AbortError') {
    throw new Error('请求超时，请稍后重试')
  }
  
  // 如果是网络错误
  if (error.message && error.message.includes('fetch')) {
    throw new Error('网络连接失败，请检查网络设置')
  }
  
  // 其他错误直接抛出
  throw error
}

// 基础请求函数
const request = async (url: string, options: RequestInit = {}) => {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT)
  
  try {
    // 处理请求体：如果是对象且不是FormData，则转换为JSON
    let body = options.body
    if (body && typeof body === 'object' && !(body instanceof FormData)) {
      body = JSON.stringify(body)
    }
    
    const config = requestInterceptor({
      ...options,
      body,
      signal: controller.signal
    })
    
    const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`
    
    let response: Response
    try {
      response = await fetch(fullUrl, config)
    } catch (fetchError: any) {
      clearTimeout(timeoutId)
      // 捕获网络错误（如连接失败、CORS错误等）
      if (fetchError.name === 'AbortError') {
        throw new Error('请求超时，请检查网络连接或稍后重试')
      }
      if (fetchError.message && fetchError.message.includes('Failed to fetch')) {
        throw new Error(`无法连接到服务器，请确保后端服务已启动 (${API_BASE_URL})`)
      }
      throw new Error(`网络错误: ${fetchError.message || '无法连接到服务器'}`)
    }
    
    clearTimeout(timeoutId)
    
    // 检查响应内容类型
    const contentType = response.headers.get('content-type')
    let data
    try {
      if (contentType && contentType.includes('application/json')) {
        data = await response.json()
      } else {
        // 非JSON响应（如文件下载）
        data = await response.blob()
      }
    } catch (parseError) {
      throw new Error('服务器返回的数据格式错误')
    }
    
    // 如果HTTP状态码不是2xx，检查是否是后端返回的错误格式
    if (!response.ok) {
      // 后端返回统一错误格式：{success: false, message: "错误信息", data: null}
      if (data && typeof data === 'object' && 'success' in data && data.success === false) {
        throw new Error(data.message || '请求失败')
      }
      // 否则抛出HTTP错误
      throw new Error(data?.message || `请求失败 (${response.status})`)
    }
    
    return responseInterceptor({
      ...response,
      data,
      status: response.status,
      statusText: response.statusText
    })
  } catch (error: any) {
    clearTimeout(timeoutId)
    // 如果已经是Error对象，直接抛出
    if (error instanceof Error) {
      throw error
    }
    return errorHandler(error)
  }
}

// 认证相关API
export const authApi = {
  // 登录
  async login(credentials: { username: string; password: string; role?: string }) {
    try {
      // 调用后端登录接口（仅发送用户名和密码，不传递role）
      // 后端会根据用户名自动识别用户的实际role，并在响应中返回
      const payload = { username: credentials.username, password: credentials.password }
      const result = await request('/auth/login', {
        method: 'POST',
        body: payload
      })
      
      // 后端统一返回：{ success: true, message: "登录成功", data: { id, username, realName, role } }
      // 兼容两种格式：data.user 或 data 直接为用户对象
      if (result && result.success && result.data) {
        const user = (result.data && result.data.user) ? result.data.user : result.data
        let normalizedUser = normalizeUserPayload(user)

        // 若后端未返回某些基础字段，尝试用本地已存在的信息进行补全
        try {
          const existingStr = sessionStorage.getItem('user_info')
          if (existingStr) {
            const existing = JSON.parse(existingStr)
            normalizedUser = {
              ...existing,
              ...normalizedUser,
              // 仅当新数据缺失时使用旧值进行填充
              phone: normalizedUser.phone || existing.phone || '',
              email: normalizedUser.email || existing.email || '',
              realName: normalizedUser.realName || existing.realName || '',
              username: normalizedUser.username || existing.username || '',
              idCard: normalizedUser.idCard || existing.idCard || '',
              registeredAt: (normalizedUser as any).registeredAt || (existing as any).registeredAt || ''
            }
          }
        } catch {}

        // 若仍缺少手机号，尝试调用个人资料接口补齐（忽略错误，最大程度兼容后端实现）
        if (!normalizedUser.phone) {
          try {
            const profileResp = await request('/passenger/profile')
            const profile = profileResp?.data || profileResp
            if (profile && profile.phone) {
              normalizedUser.phone = profile.phone
            }
          } catch {}
        }
        
        // 保存用户信息到sessionStorage
        sessionStorage.setItem('user_info', JSON.stringify(normalizedUser))
        
        return {
          success: true,
          message: result.message || '登录成功',
          user: normalizedUser
        }
      }
      
      throw new Error(result?.message || '登录失败')
    } catch (error: any) {
      // 如果后端不可用，使用模拟数据（开发环境）
      // 为了安全，默认不启用本地模拟登录。仅当显式配置 VITE_ENABLE_MOCK_LOGIN=true 时才允许。
      if (import.meta.env.VITE_ENABLE_MOCK_LOGIN === 'true') {
        console.warn('登录API不可用，使用模拟登录 (已通过环境变量显式开启)')
        const matchedAccount = mockAccounts.find(
          account => account.username === credentials.username
        )
        if (matchedAccount && matchedAccount.password === credentials.password) {
          const mockUser = normalizeUserPayload(matchedAccount.user)
          sessionStorage.setItem('user_info', JSON.stringify(mockUser))
          return {
            success: true,
            message: '登录成功（模拟）',
            user: mockUser
          }
        }
      }
      
      // 如果是网络错误，直接抛出
      if (error.message && (error.message.includes('无法连接到服务器') || error.message.includes('网络错误'))) {
        throw error
      }
      throw new Error(error.message || '用户名或密码错误')
    }
  },
  
  // 注册
  async register(userData: {
    username: string
    idCard: string
    phone: string
    realName: string
    password: string
    role?: string
    smsCode?: string
  }) {
    try {
      // 调用后端注册接口
      const result = await request('/auth/register', {
        method: 'POST',
        body: JSON.stringify(userData)
      })
      
      // 后端返回格式：{success: true, message: "注册成功", data: {id: "...", username: "...", message: "注册成功"}}
      if (result && result.success) {
        return {
          success: true,
          message: result.message || '注册成功',
          data: result.data
        }
      }
      
      // 如果后端返回失败，抛出错误
      throw new Error(result?.message || '注册失败')
    } catch (error: any) {
      // 如果是网络错误，直接抛出（保留原始错误信息）
      if (error.message && (error.message.includes('无法连接到服务器') || 
                            error.message.includes('网络错误') ||
                            error.message.includes('请求超时'))) {
        throw error
      }
      
      // 如果是后端返回的错误信息，直接使用
      if (error.message) {
        throw error
      }
      
      // 其他未知错误，包装错误信息
      throw new Error(error.message || '注册失败，请稍后重试')
    }
  },

  // 发送短信验证码
  async sendSms(phone: string) {
    try {
      const result = await request('/auth/sms/send', {
        method: 'POST',
        body: JSON.stringify({ phone })
      })
      if (result && result.success) {
        return { success: true, message: result.message || '发送成功' }
      }
      throw new Error(result?.message || '发送验证码失败')
    } catch (error: any) {
      throw new Error(error.message || '发送验证码失败')
    }
  },

  // 发送邮箱验证码
  async sendEmailCode(email: string) {
    try {
      const result = await request('/auth/send-email-code', {
        method: 'POST',
        body: JSON.stringify({ email })
      })
      if (result && result.success) {
        return { success: true, message: result.message || '发送成功' }
      }
      throw new Error(result?.message || '发送验证码失败')
    } catch (error: any) {
      throw new Error(error.message || '发送验证码失败')
    }
  },

  // 校验短信验证码（可选）
  async verifySms(phone: string, code: string) {
    try {
      const result = await request('/auth/sms/verify', {
        method: 'POST',
        body: JSON.stringify({ phone, code })
      })
      if (result && result.success) {
        return { success: true, message: result.message || '校验通过' }
      }
      throw new Error(result?.message || '验证码校验失败')
    } catch (error: any) {
      throw new Error(error.message || '验证码校验失败')
    }
  },
  // 手机号注册（手机号 + 密码 + 验证码）
  async registerByPhone(phone: string, smsCode: string, password: string) {
    try {
      const result = await request('/auth/register/phone', {
        method: 'POST',
        body: JSON.stringify({ phone, smsCode, password })
      })
      if (result && result.success) {
        return { success: true, message: result.message || '注册成功', data: result.data }
      }
      throw new Error(result?.message || '注册失败')
    } catch (error: any) {
      throw new Error(error.message || '注册失败')
    }
  },

  // 邮箱注册
  async registerByEmail(emailData: {
    email: string;
    emailCode: string;
    username: string;
    password: string;
    realName?: string;
    idCard?: string;
    phone?: string;
  }) {
    try {
      const result = await request('/auth/register/email', {
        method: 'POST',
        body: JSON.stringify(emailData)
      })
      if (result && result.success) {
        return { success: true, message: result.message || '注册成功', data: result.data }
      }
      throw new Error(result?.message || '注册失败')
    } catch (error: any) {
      throw new Error(error.message || '注册失败')
    }
  },
  
  // 检查用户名是否已存在
  async checkUsername(username: string) {
    try {
      // 调用后端检查用户名接口
      const result = await request(`/auth/check-username?username=${encodeURIComponent(username)}`)
      
      // 后端返回格式：{success: true, message: "操作成功", data: {exists: true/false}}
      if (result && result.success && result.data) {
        return result.data.exists === true
      }
      
      return false
    } catch (error) {
      // 如果API不可用，返回false（允许继续注册，由后端验证）
      if (import.meta.env.DEV) {
        console.warn('检查用户名API不可用')
      }
      return false
    }
  },
  
  // 检查邮箱是否已存在
  async checkEmail(email: string) {
    try {
      // 调用后端检查邮箱接口
      const result = await request(`/auth/check-email?email=${encodeURIComponent(email)}`)
      
      // 后端返回格式：{success: true, message: "操作成功", data: {exists: true/false}}
      if (result && result.success && result.data) {
        return result.data.exists === true
      }
      
      return false
    } catch (error) {
      // 如果API不可用，返回false（允许继续注册，由后端验证）
      if (import.meta.env.DEV) {
        console.warn('检查邮箱API不可用')
      }
      return false
    }
  },
  
  // 退出登录
  async logout() {
    try {
      // 后端没有logout接口，直接清除本地存储
      // 如果需要，可以调用后端接口
      // await request('/auth/logout', { method: 'POST' })
    } catch (error) {
      console.warn('退出登录API调用失败:', error)
    } finally {
      // 清除会话存储
      sessionStorage.removeItem('user_info')
    }
  },
  
  // 获取当前用户信息
  async getCurrentUser() {
    try {
      // 后端没有getCurrentUser接口，从本地存储获取
      // 如果需要，可以调用后端接口
      // const result = await request('/auth/me')
      // return result
      
      const userInfo = sessionStorage.getItem('user_info')
      if (userInfo) {
        return JSON.parse(userInfo)
      }
      throw new Error('未登录')
    } catch (error) {
      // 如果API不可用，从本地存储获取
      const userInfo = sessionStorage.getItem('user_info')
      if (userInfo) {
        return JSON.parse(userInfo)
      }
      throw error
    }
  }
}

// 航班相关API
export const flightApi = {
  // 搜索航班
  async searchFlights(params: {
    departure: string
    destination: string
    date: string
    passengers?: number
    class?: string
  }) {
    try {
      const query = new URLSearchParams(params as any).toString()
      const result = await request(`/flights/search?${query}`)
      
      // 后端返回格式：{success: true, message: "搜索成功", data: {flights: [...], total: ...}}
      // 提取 data 字段中的内容
      let flightsData = result
      if (result && typeof result === 'object' && 'data' in result && result.data) {
        flightsData = result.data
      }
      // 兼容直接返回 flights 的情况
      if (flightsData && flightsData.flights) {
        // 确保每个航班对象都包含 airline 字段
        if (Array.isArray(flightsData.flights)) {
          // 如果指定了舱位等级，为每个航班查询对应舱位的余票数
          if (params.class) {
            for (const flight of flightsData.flights) {
              try {
                const seatCountResult = await seatApi.getAvailableSeatCount(flight.id, params.class)
                if (seatCountResult && seatCountResult.data) {
                  flight.availableSeats = seatCountResult.data.count || 0
                }
              } catch (e) {
                // 如果查询失败，使用默认值
                flight.availableSeats = flight.seats || 45
              }
            }
          }
          
          flightsData.flights = flightsData.flights.map((flight: any) => {
            // 如果 airline 字段缺失，根据航班号自动识别
            if (!flight.airline && flight.flightNumber) {
              const prefix = flight.flightNumber.substring(0, 2).toUpperCase()
              const airlineMap: Record<string, string> = {
                'CA': '中国国际航空',
                'MU': '中国东方航空',
                'CZ': '中国南方航空',
                'HU': '海南航空',
                'ZH': '深圳航空',
                'MF': '厦门航空',
                '3U': '四川航空',
                '9C': '春秋航空',
                'JD': '首都航空',
                'HO': '吉祥航空',
                'FM': '上海航空',
                'KN': '中国联合航空',
                'PN': '西部航空',
                'G5': '华夏航空',
                'KY': '昆明航空',
                '8L': '祥鹏航空',
                'GS': '天津航空',
                'EU': '成都航空',
                'NS': '河北航空',
                'GJ': '长龙航空',
                'Y8': '扬子江快运'
              }
              flight.airline = airlineMap[prefix] || '未知航空公司'
            }
            return flight
          })
        }
        return flightsData
      }
      return flightsData
    } catch (error) {
      // 只返回后端错误，不使用本地模拟数据
      throw error
    }
  },
  
  // 获取航班详情
  async getFlightDetail(flightId: string) {
    try {
      const result = await request(`/flights/${flightId}`)
      return result
    } catch (error) {
      // 模拟数据
      console.warn('获取航班详情API不可用，使用模拟数据')
      return {
        id: flightId,
        flightNumber: 'CA1234',
        airline: '中国国际航空',
        departure: '北京',
        destination: '上海',
        departureTime: '08:00',
        arrivalTime: '10:30',
        duration: '2小时30分钟',
        aircraft: '波音737-800',
        price: 680,
        availableSeats: 45,
        seatMap: {
          economy: { price: 680, available: 35 },
          business: { price: 1280, available: 10 }
        }
      }
    }
  },
  
  // 预订航班
  async bookFlight(bookingData: {
    flightId: string | number
    passengers: Array<{
      name: string
      idCard: string
      seatClass?: string
      seatNumber?: string
      phone?: string
      seatFee?: number
    }>
    contactInfo?: {
      name: string
      phone: string
      email: string
    }
    totalAmount?: number
    usedPoints?: number
    appliedCoupons?: Array<{
      id: string | number
      name: string
      type: 'cash' | 'percentage'
      appliedAmount: number
    }>
  }) {
    try {
      // 从sessionStorage获取当前用户ID
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      // 确保用户ID是数字类型
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }

      // 构建请求体，包含passengerId
      const requestBody: any = {
        passengerId: passengerId,
        flightId: typeof bookingData.flightId === 'string' ? parseInt(bookingData.flightId, 10) : bookingData.flightId,
        passengers: bookingData.passengers.map(p => ({
          name: p.name,
          idCard: p.idCard,
          phone: p.phone || '',
          seatNumber: p.seatNumber || null,
          seatClass: p.seatClass || 'economy',
          seatFee: p.seatFee || 0
        }))
      }

      // 添加总金额（如果提供）
      if (bookingData.totalAmount !== undefined) {
        requestBody.totalAmount = bookingData.totalAmount
      }

      // 添加使用的积分（如果提供）
      if (bookingData.usedPoints !== undefined && bookingData.usedPoints > 0) {
        requestBody.usedPoints = bookingData.usedPoints
      }

      // 添加优惠券信息（如果提供）
      if (bookingData.appliedCoupons && bookingData.appliedCoupons.length > 0) {
        requestBody.appliedCoupons = bookingData.appliedCoupons.map(coupon => ({
          id: typeof coupon.id === 'string' ? parseInt(coupon.id, 10) : coupon.id,
          name: coupon.name,
          type: coupon.type,
          appliedAmount: coupon.appliedAmount
        }))
      }

      const result = await request('/flights/book', {
        method: 'POST',
        body: JSON.stringify(requestBody)
      })
      
      // 处理响应数据格式
      if (result && result.success && result.data) {
        return result.data
      }
      // 如果result直接包含orderId和orderNo，也返回
      if (result && (result.orderId || result.orderNo)) {
        return result
      }
      return result
    } catch (error) {
      throw error
    }
  }
}

// 天气相关API
export const weatherApi = {
  // 查询天气
  async queryWeather(city: string) {
    try {
      const result = await request(`/weather/query?city=${encodeURIComponent(city)}`)
      // 后端返回格式：{success: true, message: "查询成功", data: {reason: "...", result: {...}, error_code: 0}}
      // 我们需要返回 result.data.result (实际的天气数据)
      if (result && result.success && result.data && result.data.result) {
        return result.data.result
      }
      return result?.data
    } catch (error) {
      throw error
    }
  }
}

// (支付宝接口集成已被撤回 - 如需恢复可重新实现)

// 座位相关API
export const seatApi = {
  // 根据航班ID获取座位列表
  async getSeatsByFlightId(flightId: string | number) {
    try {
      const result = await request(`/seats/flight/${flightId}`)
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      throw error
    }
  },

  // 根据航班ID获取座位布局（包含机型布局信息和实际座位状态）
  async getSeatLayoutByFlightId(flightId: string | number) {
    try {
      const result = await request(`/seats/flight/${flightId}/layout`)
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      throw error
    }
  },

  // 根据航班ID和舱位等级获取可用座位数
  async getAvailableSeatCount(flightId: string | number, cabinClass?: string) {
    try {
      const query = cabinClass ? `?cabinClass=${encodeURIComponent(cabinClass)}` : ''
      const result = await request(`/seats/flight/${flightId}/available-count${query}`)
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      throw error
    }
  },

  // 为航班创建座位（根据机型布局）
  async createSeatsForFlight(flightId: string | number) {
    try {
      const result = await request(`/seats/flight/${flightId}/create`, {
        method: 'POST'
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量更新座位状态（例如：已占用）
  async updateSeatStatus(flightId: string | number, seatIds: Array<string | number>, status: string) {
    try {
      const payload = {
        seatIds: seatIds.map(id => typeof id === 'string' ? parseInt(id, 10) : id),
        // 前端可传 'available' / 'occupied' / '可用' / '已占用'，后端会统一转换为中文
        status
      }
      const result = await request(`/seats/flight/${flightId}/status`, {
        method: 'POST',
        body: JSON.stringify(payload)
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 运营（OCC）相关API - 仅供 @operationsStaff 使用
const normalizeOpFlightPlan = (plan: any) => {
  const flightNo = plan.flightNo ?? plan.flight_no ?? plan.flightNumber ?? plan.flight_number ?? ''
  const originAirport = plan.originAirport ?? plan.origin_airport ?? plan.origin ?? ''
  const destAirport = plan.destAirport ?? plan.dest_airport ?? plan.destination ?? ''
  const schedDepTime = plan.schedDepTime ?? plan.sched_dep_time ?? plan.departureTime ?? plan.departure_time ?? ''
  const schedArrTime = plan.schedArrTime ?? plan.sched_arr_time ?? plan.arrivalTime ?? plan.arrival_time ?? ''

  return {
    id: plan.id ?? plan.planId ?? plan.plan_id ?? plan.flightId ?? plan.flight_id ?? flightNo,
    flightNo,
    originAirport,
    destAirport,
    schedDepTime,
    schedArrTime,
    planDate: plan.planDate ?? plan.plan_date ?? '',
    gate: plan.gate ?? '',
    runway: plan.runway ?? '',
    crewInfo: plan.crewInfo ?? plan.crew_info ?? null,
    status: plan.status ?? plan.planStatus ?? plan.plan_status ?? ''
  }
}

export const operationsApi = {
  // 今日航班状态分布
  async getTodayStatusDistribution(params?: { planDate?: string }) {
    const query = params?.planDate ? `?planDate=${encodeURIComponent(params.planDate)}` : ''
    const result = await request(`/op/operations/flights/status-distribution${query}`)
    const data = (result && result.data) ? result.data : result
    const counts = data?.statusCounts ?? data?.counts ?? data ?? {}

    return {
      scheduled: counts.scheduled ?? 0,
      delayed: counts.delayed ?? 0,
      cancelled: counts.cancelled ?? counts.canceled ?? 0,
      boarding: counts.boarding ?? 0,
      departed: counts.departed ?? 0,
      arrived: counts.arrived ?? 0
    }
  },
  // 上报运营仪表盘的状态快照（持久化到后端，非阻塞）
  async reportStatusSnapshot(counts: { scheduled?: number; delayed?: number; cancelled?: number; boarding?: number; departed?: number; arrived?: number }) {
    try {
      await request('/admin/statistics/operations-snapshot', {
        method: 'POST',
        body: counts
      })
      return { success: true }
    } catch (error) {
      // 上报失败不应影响前端显示，静默处理
      console.warn('上报运营快照失败', error)
      return { success: false, error }
    }
  },

  // 今日航班计划列表
  async getTodayFlightPlans(params?: { planDate?: string }) {
    const queryParts: string[] = []
    if (params?.planDate) {
      queryParts.push(`planDate=${encodeURIComponent(params.planDate)}`)
    }
    const query = queryParts.length ? `?${queryParts.join('&')}` : ''
    const result = await request(`/op/operations/flights/plans${query}`)
    const list = (result && result.data && result.data.plans) ? result.data.plans
      : (result && result.data) ? result.data
      : result

    if (!Array.isArray(list)) return []
    return list.map(item => normalizeOpFlightPlan(item))
  }
,
  // 获取运行指标
  async getMetrics() {
    try {
      const result = await request('/operations/metrics')
      const data = (result && result.data) ? result.data : result
      return data
    } catch (error) {
      console.warn('获取运行指标API不可用', error)
      throw error
    }
  }
  ,
  // 航班网络分析
  async getNetworkOverview() {
    try {
      const result = await request('/operations/network/overview')
      const data = (result && result.data) ? result.data : result
      return data
    } catch (error) {
      console.warn('获取航班网络分析API不可用', error)
      throw error
    }
  },
  async getTopRoutes(page: number = 0, size: number = 5, sort: string = 'revenue') {
    try {
      const result = await request(`/operations/network/top-routes?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`)
      const data = (result && result.data) ? result.data : result
      return data
    } catch (error) {
      console.warn('获取热门航线API不可用', error)
      throw error
    }
  }
  ,
  // 获取运营侧异常/告警（复用管理员接口）
  async getAlerts(limit: number = 20) {
    try {
      const result = await request(`/admin/alerts?limit=${limit}`)
      return result
    } catch (error) {
      console.warn('获取运营告警API不可用', error)
      throw error
    }
  },
  // 获取航班异常事件（来自 flight_exceptions 表），支持分页返回 {exceptions, total, page, size}
  async getFlightExceptions(page: number = 0, size: number = 20) {
    try {
      const result = await request(`/op/operations/exceptions?page=${page}&size=${size}`)
      const data = (result && result.data) ? result.data : result
      // Normalize: { exceptions: [...], total, page, size }
      const exceptions = data?.exceptions ?? data ?? []
      const total = Number(data?.total ?? 0)
      const resp = { exceptions, total, page: Number(data?.page ?? page), size: Number(data?.size ?? size) }
      return resp
    } catch (error) {
      console.warn('获取航班异常API不可用', error)
      throw error
    }
  },
  // 运营处理异常事件（更新状态和备注）
  async processException(id: number | string, status: string, operatorNote?: string) {
    try {
      const body: any = { status }
      if (operatorNote) body.operatorNote = operatorNote
      const result = await request(`/op/operations/exceptions/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      console.warn('处理航班异常API不可用', error)
      throw error
    }
  },
  // 处理/更新告警状态（运营）
  async processAlert(id: number, status: string, operatorNote?: string) {
    try {
      const body: any = { status }
      if (operatorNote) body.operatorNote = operatorNote
      const result = await request(`/admin/alerts/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      console.warn('运营处理告警API不可用', error)
      throw error
    }
  }
}

// 订单相关API
export const orderApi = {
  // 获取用户订单列表
  async getOrders(params?: { 
    page?: number
    size?: number
    orderNumber?: string
    customer?: string
    status?: string
    startDate?: string
    endDate?: string
  }) {
    try {
      // 从sessionStorage获取当前用户ID
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      // 确保用户ID是数字类型（后端需要Integer类型）
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      console.log('获取订单列表，用户ID:', passengerId, '参数:', params)
      
      // 构建查询参数，包含passengerId和所有筛选参数
      const queryParams: any = {
        passengerId: passengerId,
        ...(params || {})
      }
      
      // 移除undefined值和空字符串
      Object.keys(queryParams).forEach(key => {
        if (queryParams[key] === undefined || queryParams[key] === '') {
          delete queryParams[key]
        }
      })
      
      const query = new URLSearchParams(queryParams as any).toString()
      const result = await request(`/orders?${query}`)
      
      console.log('订单API原始响应:', result)
      
      // 处理响应数据格式
      // 后端返回格式：{success: true, message: "查询成功", data: {orders: [...], total: ...}}
      if (result && result.success && result.data) {
        const data = result.data
        console.log('解析后的订单数据:', data)
        console.log('orders数组类型:', Array.isArray(data.orders), 'orders长度:', data.orders?.length)
        
        // 确保返回的数据包含orders数组
        if (data.orders && Array.isArray(data.orders)) {
          // 如果orders是空数组但total>0，可能是分页问题
          if (data.orders.length === 0 && data.total > 0) {
            console.warn('警告：orders数组为空但total>0，可能是分页参数问题')
            console.warn('当前查询参数:', params)
            console.warn('返回的data:', JSON.stringify(data, null, 2))
          }
          
          return {
            orders: data.orders,
            total: data.total || data.orders.length,
            page: data.page || 0,
            size: data.size || 10,
            totalPages: data.totalPages || 1
          }
        }
        // 如果data本身就是数组
        if (Array.isArray(data)) {
          return {
            orders: data,
            total: data.length,
            page: 0,
            size: data.length,
            totalPages: 1
          }
        }
      }
      
      // 如果result直接包含orders字段（兼容其他格式）
      if (result && result.orders && Array.isArray(result.orders)) {
        return {
          orders: result.orders,
          total: result.total || result.orders.length,
          page: result.page || 0,
          size: result.size || 10,
          totalPages: result.totalPages || 1
        }
      }
      
      // 如果result本身就是数组
      if (Array.isArray(result)) {
        return {
          orders: result,
          total: result.length,
          page: 0,
          size: result.length,
          totalPages: 1
        }
      }
      
      console.warn('无法解析订单数据格式:', result)
      return {
        orders: [],
        total: 0,
        page: 0,
        size: 10,
        totalPages: 0
      }
    } catch (error: any) {
      console.error('获取订单API失败:', error)
      // 不再返回模拟数据，直接抛出错误，让调用方处理
      throw error
    }
  },
  
  // 获取订单详情（根据订单号）
  async getOrderDetail(orderNo: string) {
    try {
      // 从sessionStorage获取当前用户ID
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      // 确保用户ID是数字类型
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      console.log('获取订单详情，订单号:', orderNo, '用户ID:', passengerId)
      
      // 使用订单号查询订单详情
      const result = await request(`/orders/by-order-no/${encodeURIComponent(orderNo)}?passengerId=${passengerId}`)
      
      console.log('订单详情API响应:', result)
      
      // 处理响应数据格式
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      console.error('获取订单详情失败:', error)
      throw error
    }
  },
  
  // 取消订单
  async cancelOrder(orderId: string) {
    try {
      const result = await request(`/orders/${orderId}/cancel`, {
        method: 'POST'
      })
      return result
    } catch (error) {
      throw error
    }
  },
  
  // 更新订单金额（覆盖 totalAmount 字段）
  async updateOrderAmount(orderId: string, amount: number) {
    try {
      const payload = { totalAmount: amount }
      const result = await request(`/orders/${orderId}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 乘客提交改签申请（机票改签）
  async requestReschedule(payload: {
    orderNo: string
    route: string
    changeFee: number
    priceDiff: number
    reason: string
    newFlight: any
  }) {
    // 从当前登录用户获取乘客ID和姓名
    const currentUser = apiUtils.getCurrentUser()
    if (!currentUser || !currentUser.id) {
      throw new Error('用户未登录')
    }

    const passengerId =
      typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
    if (isNaN(passengerId)) {
      throw new Error('用户ID格式错误')
    }

    const applicantName = currentUser.realName || currentUser.username || ''

    // 解析旧航线
    const oldRoute = payload.route || ''

    // 旧/新航班号
    // 原航班号：优先使用 payload.oldFlightNo，其次才看 newFlight 里是否有冗余字段
    const oldFlightNo =
      payload.oldFlightNo ||
      payload.orderFlightNo ||
      payload.newFlight?.oldFlightNo ||
      payload.newFlight?.oldFlightNumber ||
      ''
    const newFlightNo =
      payload.newFlight?.flightNumber ||
      payload.newFlight?.flightNo ||
      payload.newFlight?.newFlightNo ||
      ''

    // 旧/新航线（例如：北京 → 上海）
    const newRoute =
      `${payload.newFlight?.departure || payload.newFlight?.origin || ''} → ` +
      `${payload.newFlight?.destination || payload.newFlight?.dest || ''}`

    // 处理时间字段，转换为后端 LocalDateTime 可解析的字符串（yyyy-MM-dd HH:mm:ss）
    const normalizeDateTime = (value: any, dateHint?: string | null): string | null => {
      if (!value && !dateHint) return null

      // 如果只有日期提示，没有时间，补 00:00:00
      if (!value && dateHint) {
        return `${dateHint}T00:00:00`
      }

      if (typeof value === 'string') {
        let s = value.trim()

        // 只有时间（08:00 或 08:00:00），需要配合日期
        if (/^\d{2}:\d{2}(:\d{2})?$/.test(s)) {
          if (!dateHint) {
            return null
          }
          if (s.length === 5) {
            s = `${s}:00`
          }
          return `${dateHint}T${s}`
        }

        // 只有日期
        if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
          return `${s}T00:00:00`
        }

        // 已经是完整的日期时间
        return s
      }
      return null
    }

    // 原航班起飞时间优先使用订单里的完整时间
    const oldDepartureTime = normalizeDateTime(
      payload.oldDepartureTime ||
        payload.orderDepartureTime ||
        payload.newFlight?.oldDepartureTime ||
        payload.newFlight?.oldDepTime ||
        payload.newFlight?.departureTime,
      payload.oldDepartureDate
    )

    // 新航班起飞时间：用航班搜索返回的 date + departureTime
    const newDepartureTime = normalizeDateTime(
      payload.newFlight?.schedDepTime ||
        payload.newFlight?.departureTime ||
        payload.newFlight?.newDepartureTime,
      payload.newFlight?.date || payload.newDepartureDate
    )

    const body = {
      orderno: payload.orderNo, // 与后端实体字段对齐
      passengerId,
      applicantName,
      ticketNo: null,
      oldFlightNo,
      oldRoute,
      oldDepartureTime,
      newFlightNo,
      newRoute,
      newDepartureTime,
      changeFee: payload.changeFee,
      fareDiff: payload.priceDiff,
      status: '', // 后端默认设置为“待处理”
      reason: payload.reason
    }
    // 可选：如果前端传入了改签涉及的乘客与座位信息，一并传给后端
    if (payload.passengers) {
      // 传递乘客数组（{ passengerId, passengerName, seatId?, seatNumber? }）
      body.passengers = payload.passengers
    }
    if (payload.seatAssignments) {
      // 传递座位分配明细（数组或对象），后端可按需解析
      body.seatAssignments = payload.seatAssignments
    }

    const result = await request('/ticket-change-requests', {
      method: 'POST',
      body
    })
    return result
  },
  /**
   * 发起改签支付（返回支付宝页面 HTML 字符串）
   * 注意：该方法直接使用 fetch 调用后端支付接口以获取支付宝页面（后端返回的 form HTML）
   */
  async payReschedule(payload: {
    orderNo: string
    changeFee: number
    priceDiff: number
    reason: string
    newFlight: any
  }) {
    try {
      const url = `${API_BASE_URL.replace(/\/api$/, '')}/api/alipay/payReschedule`
      const response = await fetch(url, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })
      if (!response.ok) {
        const txt = await response.text()
        throw new Error(txt || `支付接口错误 (${response.status})`)
      }
      const html = await response.text()
      return html
    } catch (error: any) {
      throw new Error(error?.message || '发起改签支付失败')
    }
  },
  /**
   * 发起已有订单的支付（返回支付宝页面 HTML 字符串）
   * 后端接口：/api/alipay/payOrder
   */
  async payOrder(payload: { orderNo: string, amount: number }) {
    try {
      const url = `${API_BASE_URL.replace(/\/api$/, '')}/api/alipay/payOrder`
      const response = await fetch(url, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })
      if (!response.ok) {
        const txt = await response.text()
        throw new Error(txt || `支付接口错误 (${response.status})`)
      }
      const html = await response.text()
      return html
    } catch (error: any) {
      throw new Error(error?.message || '发起订单支付失败')
    }
  },
  /**
   * 发起取消申请支付（返回支付宝页面 HTML 字符串）
   * 后端接口：/api/alipay/payCancel
   */
  async payCancel(payload: {
    orderNo: string
    cancelFee: number
    refundFare?: number
    reason?: string
  }) {
    // 取消支付接口已从后端移除；前端不应再调用该方法。
    throw new Error('取消支付接口已移除：乘客端取消不再走支付宝支付流程')
  },

  // 乘客提交取消申请（机票取消）
  async requestCancel(payload: {
    orderNo: string
    route: string
    flightNo?: string
    departureTime?: string
    cancelFee: number
    ticketPrice: number
    ticketNo?: string
    reason: string
  }) {
    const currentUser = apiUtils.getCurrentUser()
    if (!currentUser || !currentUser.id) {
      throw new Error('用户未登录')
    }

    const passengerId =
      typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
    if (isNaN(passengerId)) {
      throw new Error('用户ID格式错误')
    }

    const applicantName = currentUser.realName || currentUser.username || ''

    const refundFareRaw = (payload.ticketPrice || 0) - (payload.cancelFee || 0)
    const refundFare = refundFareRaw > 0 ? refundFareRaw : 0

    const body: any = {
      orderno: payload.orderNo,
      passengerId,
      applicantName,
      ticketNo: payload.ticketNo || null,
      flightNo: payload.flightNo || '',
      route: payload.route,
      departureTime: payload.departureTime || null,
      cancelFee: payload.cancelFee,
      refundFare,
      status: '', // 交由后端默认设置为“待处理”
      reason: payload.reason
    }

    const result = await request('/ticket-cancel-requests', {
      method: 'POST',
      body
    })
    return result
  },
  
  // 删除订单
  async deleteOrder(orderId: string | number) {
    try {
      // 从sessionStorage获取当前用户ID
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      // 确保用户ID是数字类型
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      console.log('删除订单，订单ID:', orderId, '用户ID:', passengerId)
      
      // 调用后端API删除订单
      const result = await request(`/orders/${orderId}?passengerId=${passengerId}`, {
        method: 'DELETE'
      })
      
      console.log('删除订单API响应:', result)
      
      // 处理响应数据格式
      if (result && result.success) {
        return result
      }
      return result
    } catch (error) {
      console.error('删除订单失败:', error)
      throw error
    }
  }
}

// 航班管理API（管理员）
export const flightManagementApi = {
  // 获取航班列表（带筛选和分页）
  async getFlightList(params: {
    page?: number
    size?: number
    departureDateStart?: string
    departureDateEnd?: string
    airline?: string
    departure?: string
    destination?: string
  }) {
    try {
      const query = new URLSearchParams(params as any).toString()
      const result = await request(`/admin/flights?${query}`)
      return result
    } catch (error) {
      // 回退到本地模拟数据，支持分页与简单筛选
      console.warn('获取航班列表API不可用，使用本地模拟数据', error)
      const page = Number(params?.page ?? 0)
      const size = Number(params?.size ?? 10)
      const start = page * size
      // 简单筛选：departure / destination / airline / date 范围
      let list = mockAdminFlights.slice()
      if (params?.departure) {
        list = list.filter(f => String(f.departure || '').includes(String(params.departure)))
      }
      if (params?.destination) {
        list = list.filter(f => String(f.destination || '').includes(String(params.destination)))
      }
      if (params?.airline) {
        list = list.filter(f => String(f.airline || '').includes(String(params.airline)))
      }
      if (params?.departureDateStart || params?.departureDateEnd) {
        const startDate = params?.departureDateStart ? new Date(params.departureDateStart) : null
        const endDate = params?.departureDateEnd ? new Date(params.departureDateEnd) : null
        list = list.filter(f => {
          if (!f.departureTime) return true
          const d = new Date(f.departureTime)
          if (startDate && d < startDate) return false
          if (endDate && d > endDate) return false
          return true
        })
      }
      const pageList = list.slice(start, start + size)
      return { flights: pageList, total: list.length }
    }
  },

  // 获取航班详情
  async getFlightById(flightId: string) {
    try {
      const result = await request(`/admin/flights/${flightId}`)
      return result
    } catch (error) {
      // 回退到本地模拟数据
      console.warn('getFlightById API 不可用，使用本地模拟数据', error)
      const found = mockAdminFlights.find(f => String(f.id) === String(flightId))
      if (found) return found
      throw error
    }
  },

  // 创建航班
  async createFlight(flightData: any) {
    try {
      const result = await request('/admin/flights', {
        method: 'POST',
        body: JSON.stringify(flightData)
      })
      return result
    } catch (error) {
      // 后端不可用时使用本地模拟存储创建航班
      console.warn('createFlight API 不可用，使用本地模拟数据创建航班', error)
      const id = `mock-${mockAdminNextId++}`
      const created: any = {
        id,
        flightNumber: flightData.flightNumber ?? flightData.flightNo ?? `AUTO-${id}`,
        airline: flightData.airline ?? '示例航空',
        aircraftModel: flightData.aircraftModel ?? '未知机型',
        originAirport: flightData.originAirport ?? flightData.departure ?? '',
        destAirport: flightData.destAirport ?? flightData.destination ?? '',
        departureTime: flightData.schedDepTime ?? flightData.departureTime ?? '',
        arrivalTime: flightData.schedArrTime ?? flightData.arrivalTime ?? '',
        departureDate: flightData.schedDepTime ? String(flightData.schedDepTime).slice(0,10) : (flightData.departureDate ?? ''),
        duration: flightData.duration ?? '',
        quantity: Number(flightData.quantity ?? flightData.seats ?? 0),
        price: Number(flightData.price ?? 0),
        status: flightData.status ?? 'scheduled',
        routeInfo: flightData.routeInfo ?? ''
      }
      // 保持兼容字段名
      created.departure = created.originAirport
      created.destination = created.destAirport
      // 检查本地模拟数据是否已存在相同的航班号+出发时间组合，避免重复
      const duplicate = mockAdminFlights.some(f =>
        String(f.flightNumber) === String(created.flightNumber) &&
        String(f.departureTime) === String(created.departureTime)
      )
      if (duplicate) {
        return { success: false, message: '航班号与计划出发时间的组合已存在（本地模拟）' }
      }
      mockAdminFlights.unshift(created)
      return { success: true, data: created }
    }
  },

  // 更新航班
  async updateFlight(flightId: string, flightData: any) {
    try {
      const result = await request(`/admin/flights/${flightId}`, {
        method: 'PUT',
        body: JSON.stringify(flightData)
      })
      return result
    } catch (error) {
      console.warn('updateFlight API 不可用，使用本地模拟数据更新航班', error)
      const idx = mockAdminFlights.findIndex(f => String(f.id) === String(flightId))
      if (idx >= 0) {
        const existing = mockAdminFlights[idx]
        const updated = { ...existing, ...flightData }
        // 兼容字段映射
        if (flightData.flightNo && !flightData.flightNumber) updated.flightNumber = flightData.flightNo
        if (flightData.originAirport) updated.departure = flightData.originAirport
        if (flightData.destAirport) updated.destination = flightData.destAirport
        mockAdminFlights[idx] = updated
        return { success: true, data: updated }
      }
      throw error
    }
  },

  // 删除航班
  async deleteFlight(flightId: string) {
    try {
      const result = await request(`/admin/flights/${flightId}`, {
        method: 'DELETE'
      })
      return result
    } catch (error) {
      console.warn('deleteFlight API 不可用，使用本地模拟数据删除航班', error)
      const idx = mockAdminFlights.findIndex(f => String(f.id) === String(flightId))
      if (idx >= 0) {
        mockAdminFlights.splice(idx, 1)
        return { success: true }
      }
      throw error
    }
  },

  // 批量删除航班
  async deleteFlights(flightIds: string[]) {
    try {
      const result = await request('/admin/flights/batch', {
        method: 'DELETE',
        body: JSON.stringify({ ids: flightIds })
      })
      return result
    } catch (error) {
      console.warn('deleteFlights API 不可用，使用本地模拟数据批量删除', error)
      let removedCount = 0
      flightIds.forEach(id => {
        const idx = mockAdminFlights.findIndex(f => String(f.id) === String(id))
        if (idx >= 0) {
          mockAdminFlights.splice(idx, 1)
          removedCount++
        }
      })
      return { success: true, removed: removedCount }
    }
  }
}

// 机型管理API（管理员）
export const aircraftTypeApi = {
  // 获取机型列表（支持分页）
  async getAircraftTypes(status?: 'active' | 'all' | 'retired', page?: number, size?: number) {
    try {
      const params: string[] = []
      if (status) params.push(`status=${status}`)
      if (page !== undefined) params.push(`page=${page}`)
      if (size !== undefined) params.push(`size=${size}`)
      const query = params.length > 0 ? `?${params.join('&')}` : ''
      const result = await request(`/admin/aircraft-types${query}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 根据ID获取机型详情
  async getAircraftTypeById(id: number) {
    try {
      const result = await request(`/admin/aircraft-types/${id}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 创建机型
  async createAircraftType(data: {
    typeCode: string
    manufacturer?: string
    model?: string
    seatLayout?: string
    status?: 'active' | 'retired'
  }) {
    try {
      const result = await request('/admin/aircraft-types', {
        method: 'POST',
        body: JSON.stringify(data)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量创建机型
  async createAircraftTypes(dataList: Array<{
    typeCode: string
    manufacturer?: string
    model?: string
    seatLayout?: string
    status?: 'active' | 'retired'
  }>) {
    try {
      const result = await request('/admin/aircraft-types/batch', {
        method: 'POST',
        body: JSON.stringify(dataList)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 更新机型
  async updateAircraftType(id: number, data: {
    typeCode?: string
    manufacturer?: string
    model?: string
    seatLayout?: string
    status?: 'active' | 'retired'
  }) {
    try {
      const result = await request(`/admin/aircraft-types/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 删除机型
  async deleteAircraftType(id: number) {
    try {
      const result = await request(`/admin/aircraft-types/${id}`, {
        method: 'DELETE'
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量删除机型
  async deleteAircraftTypes(ids: number[]) {
    try {
      const result = await request('/admin/aircraft-types/batch', {
        method: 'DELETE',
        body: JSON.stringify({ ids })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 多字段搜索机型（支持分页）
  async searchAircraftTypes(params: {
    typeCode?: string
    manufacturer?: string
    model?: string
    status?: 'active' | 'retired' | 'all'
    page?: number
    size?: number
  }) {
    try {
      const queryParams: string[] = []
      if (params.typeCode) queryParams.push(`typeCode=${encodeURIComponent(params.typeCode)}`)
      if (params.manufacturer) queryParams.push(`manufacturer=${encodeURIComponent(params.manufacturer)}`)
      if (params.model) queryParams.push(`model=${encodeURIComponent(params.model)}`)
      if (params.status && params.status !== 'all') queryParams.push(`status=${params.status}`)
      if (params.page !== undefined) queryParams.push(`page=${params.page}`)
      if (params.size !== undefined) queryParams.push(`size=${params.size}`)
      
      const query = queryParams.length > 0 ? `?${queryParams.join('&')}` : ''
      const result = await request(`/admin/aircraft-types/search${query}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取所有制造商列表
  async getManufacturers() {
    try {
      const result = await request('/admin/aircraft-types/manufacturers')
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取所有型号列表
  async getModels() {
    try {
      const result = await request('/admin/aircraft-types/models')
      return result
    } catch (error) {
      throw error
    }
  }
}

 

// 订单管理API（管理员）
export const orderManagementApi = {
  // 获取订单列表（带筛选和分页）
  async getOrderList(params: {
    page?: number
    size?: number
    orderNumber?: string  // 后端使用的字段名是 orderNumber
    customer?: string
    status?: string
    startDate?: string
    endDate?: string
  }) {
    try {
      console.log('========== orderManagementApi.getOrderList ==========')
      console.log('接收到的参数:', params)
      
      // 过滤掉 undefined 和空字符串的参数
      const cleanParams: any = {}
      Object.keys(params).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      
      // 构建查询字符串，确保参数正确编码
      const queryParts: string[] = []
      Object.keys(cleanParams).forEach(key => {
        const value = cleanParams[key]
        if (value !== undefined && value !== null && value !== '') {
          const encodedKey = encodeURIComponent(key)
          const encodedValue = encodeURIComponent(String(value))
          queryParts.push(`${encodedKey}=${encodedValue}`)
        }
      })
      const query = queryParts.join('&')
      const url = query ? `/admin/orders?${query}` : '/admin/orders'
      
      const result = await request(url)
      return result
    } catch (error) {
      console.warn('获取订单列表API不可用')
      throw error
    }
  },

  // 获取订单详情
  async getOrderById(orderId: string | number) {
    try {
      const result = await request(`/admin/orders/${orderId}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 删除订单（管理员）
  async deleteOrder(orderId: string | number) {
    try {
      const result = await request(`/admin/orders/${orderId}`, {
        method: 'DELETE'
      })
      return result
    } catch (error) {
      throw error
    }
  },
  // 恢复订单（管理员）
  async restoreOrder(orderId: string | number) {
    try {
      const result = await request(`/admin/orders/${orderId}/restore`, {
        method: 'POST'
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量标记订单
  async markOrders(orderIds: string[], mark: string) {
    try {
      const result = await request('/admin/orders/mark', {
        method: 'POST',
        body: JSON.stringify({ orderIds, mark })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 导出订单CSV
  async exportOrdersCSV(params: any) {
    try {
      const query = new URLSearchParams(params as any).toString()
      const response = await fetch(`${API_BASE_URL}/admin/orders/export?${query}`)
      const blob = await response.blob()
      return blob
    } catch (error) {
      throw error
    }
  },

  // 获取订单沟通记录
  async getOrderChatHistory(orderId: string) {
    try {
      const result = await request(`/admin/orders/${orderId}/chat`)
      return result
    } catch (error) {
      throw error
    }
  }
}

// 改签管理API
export const ticketManagementApi = {
  // 获取订票审核列表
  async getBookingReviewList(params?: {
    page?: number
    size?: number
    status?: string
  }) {
    try {
      const query = params ? new URLSearchParams(params as any).toString() : ''
      const result = await request(`/admin/tickets/booking-review?${query}`)
      return result
    } catch (error) {
      console.warn('获取订票审核列表API不可用，使用模拟数据')
      throw error
    }
  },

  // 获取退票审核列表
  async getRefundReviewList(params?: {
    page?: number
    size?: number
    status?: string
  }) {
    try {
      const query = params ? new URLSearchParams(params as any).toString() : ''
      const result = await request(`/admin/tickets/refund-review?${query}`)
      return result
    } catch (error) {
      console.warn('获取退票审核列表API不可用，使用模拟数据')
      throw error
    }
  },

  // 批准订票申请
  async approveBooking(applicationId: string) {
    try {
      const result = await request(`/admin/tickets/booking-review/${applicationId}/approve`, {
        method: 'POST'
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 驳回订票申请
  async rejectBooking(applicationId: string, reason: string) {
    try {
      const result = await request(`/admin/tickets/booking-review/${applicationId}/reject`, {
        method: 'POST',
        body: JSON.stringify({ reason })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批准退票申请
  async approveRefund(applicationId: string, processedBy?: number, remark?: string) {
    try {
      const body: any = {}
      if (processedBy !== undefined) body.processedBy = processedBy
      if (remark !== undefined) body.remark = remark
      
      const result = await request(`/admin/tickets/refund-review/${applicationId}/approve`, {
        method: 'POST',
        body: Object.keys(body).length > 0 ? JSON.stringify(body) : undefined
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 拒绝退票申请
  async rejectRefund(applicationId: string, reason: string, processedBy?: number) {
    try {
      const result = await request(`/admin/tickets/refund-review/${applicationId}/reject`, {
        method: 'POST',
        body: JSON.stringify({ reason, processedBy })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 请求补充材料
  async requestMaterial(applicationId: string, message: string) {
    try {
      const result = await request(`/admin/tickets/refund-review/${applicationId}/request-material`, {
        method: 'POST',
        body: JSON.stringify({ message })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取风险策略规则
  async getRiskRules() {
    try {
      const result = await request('/admin/tickets/risk-rules')
      return result
    } catch (error) {
      throw error
    }
  },

  // 更新风险策略规则顺序
  async updateRiskRulesOrder(rules: Array<{ id: string; order: number }>) {
    try {
      const result = await request('/admin/tickets/risk-rules/order', {
        method: 'PUT',
        body: JSON.stringify({ rules })
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 管理员改签审核API（审核页面专用）
export const adminTicketChangeReviewApi = {
  // 获取改签审核列表
  async getChangeReviewList(params: {
    page?: number
    size?: number
    status?: string
    changeNo?: string
    applicantName?: string
    orderNo?: string
  }) {
    try {
      const cleanParams: any = {}
      Object.keys(params).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      const query = new URLSearchParams(cleanParams as any).toString()
      const url = query ? `/admin/ticket-change-review?${query}` : '/admin/ticket-change-review'
      const result = await request(url)
      return result
    } catch (error) {
      console.warn('获取改签审核列表API不可用')
      throw error
    }
  },

  // 批准改签申请
  async approveChangeRequest(id: string | number, processedBy?: number, remark?: string) {
    try {
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) {
        throw new Error('无效的申请ID')
      }
      
      const body: any = {}
      if (processedBy !== undefined) body.processedBy = processedBy
      if (remark !== undefined) body.remark = remark
      const result = await request(`/admin/ticket-change-review/${numericId}/approve`, {
        method: 'POST',
        body: Object.keys(body).length > 0 ? JSON.stringify(body) : undefined
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 拒绝改签申请
  async rejectChangeRequest(id: string | number, reason: string, processedBy?: number) {
    try {
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) {
        throw new Error('无效的申请ID')
      }
      
      const body: any = { remark: reason }
      if (processedBy !== undefined) body.processedBy = processedBy
      const result = await request(`/admin/ticket-change-review/${numericId}/reject`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取统计数据
  async getStatistics(status?: string) {
    try {
      const params: any = {}
      if (status) params.status = status
      const query = new URLSearchParams(params).toString()
      const url = query ? `/admin/ticket-change-review/statistics?${query}` : '/admin/ticket-change-review/statistics'
      const result = await request(url)
      return result
    } catch (error) {
      throw error
    }
  }
}

// 运营人员改签审核API（运营页面专用）
export const operationsTicketChangeReviewApi = {
  async approveChangeRequest(id: string | number, processedBy?: number, remark?: string, newOrderNo?: string) {
    try {
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) throw new Error('无效的申请ID')
      const body: any = {}
      if (processedBy !== undefined) body.processedBy = processedBy
      if (remark !== undefined) body.remark = remark
      if (newOrderNo !== undefined && newOrderNo != null && String(newOrderNo).trim() !== '') body.newOrderNo = String(newOrderNo).trim()
      const result = await request(`/operations/ticket-change-review/${numericId}/approve`, {
        method: 'POST',
        body: Object.keys(body).length > 0 ? JSON.stringify(body) : undefined
      })
      return result
    } catch (error) {
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        console.warn('模拟：运营改签批准成功（后端不可用）', error)
        return { success: true, message: '模拟：已批准', data: {} }
      }
      throw error
    }
  },
  async rejectChangeRequest(id: string | number, reason: string, processedBy?: number) {
    try {
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) throw new Error('无效的申请ID')
      const body: any = { remark: reason }
      if (processedBy !== undefined) body.processedBy = processedBy
      const result = await request(`/operations/ticket-change-review/${numericId}/reject`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        console.warn('模拟：运营改签拒绝成功（后端不可用）', error)
        return { success: true, message: '模拟：已拒绝', data: {} }
      }
      throw error
    }
  }
}

// 改签申请管理API（管理员）
export const changeRequestManagementApi = {
  // 获取改签申请列表
  async getChangeRequestList(params: {
    page?: number
    size?: number
    status?: string
    orderNo?: string
    applicantName?: string
    startDate?: string
    endDate?: string
  }) {
    try {
      // 过滤掉 undefined 和空字符串的参数
      const cleanParams: any = {}
      Object.keys(params).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      const query = new URLSearchParams(cleanParams as any).toString()
      const url = query ? `/admin/ticket-change-requests?${query}` : '/admin/ticket-change-requests'
      const result = await request(url)
      return result
    } catch (error) {
      console.warn('获取改签申请列表API不可用，使用模拟数据（如已启用）', error)
      // 当后端不可用时，根据环境变量返回模拟数据以保证前端页面可用
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        const mock = {
          success: true,
          data: {
            list: [
              {
                id: 1,
                changeNo: 'TCR202512260018072137',
                orderno: 'ORD20251225192259863',
                orderNo: 'ORD20251225192259863',
                applicantName: '普通乘客',
                requestTime: new Date().toISOString(),
                status: '待处理',
                oldFlightNo: 'CA1201',
                oldRoute: '北京首都国际机场 -> 上海虹桥国际机场',
                oldDepartureTime: '2025-12-09T08:00:00',
                newFlightNo: 'CA1201',
                newRoute: '北京首都国际机场 -> 上海虹桥国际机场',
                newDepartureTime: '2025-12-09T08:00:00',
                changeFee: 7.55,
                fareDiff: 0,
                reason: '测试改签',
                remark: ''
              }
            ],
            total: 1,
            page: 0
          }
        }
        return mock
      }
      throw error
    }
  },

  // 获取改签申请详情
  async getChangeRequestById(id: string | number) {
    try {
      const result = await request(`/admin/ticket-change-requests/${id}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 批准改签申请
  async approveChangeRequest(id: string | number, processedBy?: number, remark?: string, newOrderNo?: string) {
    try {
      // 确保ID是数字类型
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) {
        throw new Error('无效的申请ID')
      }
      
      const body: any = {}
      if (processedBy !== undefined) body.processedBy = processedBy
      if (remark !== undefined) body.remark = remark
      // 可选：审批时填写生成的新订单号，后端应在收到后把 new_order_no 写入 ticket_change_requests
      if (newOrderNo !== undefined && newOrderNo !== null && String(newOrderNo).trim() !== '') {
        body.newOrderNo = String(newOrderNo).trim()
      }
      const result = await request(`/admin/ticket-change-requests/${numericId}/approve`, {
        method: 'POST',
        body: Object.keys(body).length > 0 ? JSON.stringify(body) : undefined
      })
      return result
    } catch (error) {
      // 当后端不可用时提供模拟成功结果，避免前端出现未处理异常（仅在开发或显式启用模拟时）
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        console.warn('模拟：改签批准成功（后端不可用）', error)
        return { success: true, message: '模拟：已批准', data: {} }
      }
      throw error
    }
  },

  // 拒绝改签申请
  async rejectChangeRequest(id: string | number, reason: string, processedBy?: number) {
    try {
      // 确保ID是数字类型
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) {
        throw new Error('无效的申请ID')
      }
      
      const body: any = { remark: reason }
      if (processedBy !== undefined) body.processedBy = processedBy
      const result = await request(`/admin/ticket-change-requests/${numericId}/reject`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        console.warn('模拟：改签拒绝成功（后端不可用）', error)
        return { success: true, message: '模拟：已拒绝', data: {} }
      }
      throw error
    }
  },

  // 批量批准
  async batchApprove(ids: (string | number)[], processedBy?: number, remark?: string) {
    try {
      // 将ID转换为数字（后端需要Integer类型）
      const numericIds = ids.map(id => {
        if (typeof id === 'string') {
          const num = parseInt(id, 10)
          return isNaN(num) ? 0 : num
        }
        return id
      }).filter(id => id > 0) // 过滤掉无效ID
      
      const body: any = { ids: numericIds }
      if (processedBy !== undefined) body.processedBy = processedBy
      if (remark !== undefined) body.remark = remark
      const result = await request('/admin/ticket-change-requests/batch-approve', {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量拒绝
  async batchReject(ids: (string | number)[], reason: string, processedBy?: number) {
    try {
      // 将ID转换为数字（后端需要Integer类型）
      const numericIds = ids.map(id => {
        if (typeof id === 'string') {
          const num = parseInt(id, 10)
          return isNaN(num) ? 0 : num
        }
        return id
      }).filter(id => id > 0) // 过滤掉无效ID
      
      const body: any = {
        ids: numericIds,
        remark: reason
      }
      if (processedBy !== undefined) body.processedBy = processedBy
      const result = await request('/admin/ticket-change-requests/batch-reject', {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取统计数据
  async getStatistics() {
    try {
      const result = await request('/admin/ticket-change-requests/statistics')
      return result
    } catch (error) {
      throw error
    }
  }
}

// 退票申请管理API（管理员/航空运营）
export const cancelRequestManagementApi = {
  // 获取退票申请列表
  async getCancelRequestList(params: {
    page?: number
    size?: number
    status?: string
    orderNo?: string
    applicantName?: string
    startDate?: string
    endDate?: string
  }) {
    try {
      // 过滤掉 undefined 和空字符串的参数
      const cleanParams: any = {}
      Object.keys(params).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      const query = new URLSearchParams(cleanParams as any).toString()
      // 约定后台复用 /admin/ticket-cancel-requests 作为 ticket_cancel_requests 表的管理入口
      const url = query
        ? `/admin/ticket-cancel-requests?${query}`
        : '/admin/ticket-cancel-requests'
      const result = await request(url)
      return result
    } catch (error) {
      console.warn('获取退票申请列表API不可用，使用模拟数据（如已启用）', error)
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        const mock = {
          success: true,
          data: {
            list: [
              {
                id: 101,
                cancelNo: 'CLR202512260001',
                cancel_no: 'CLR202512260001',
                orderno: 'ORD20251225192259863',
                orderNo: 'ORD20251225192259863',
                applicantName: '普通乘客',
                requestTime: new Date().toISOString(),
                status: '待处理',
                flightNo: 'CA1201',
                route: '北京首都国际机场 -> 上海虹桥国际机场',
                departureTime: '2025-12-09T08:00:00',
                cancelFee: 10.0,
                refundFare: 100.0,
                reason: '测试退票',
                remark: ''
              }
            ],
            total: 1,
            page: 0
          }
        }
        return mock
      }
      throw error
    }
  },

  // 获取退票申请详情
  async getCancelRequestById(id: string | number) {
    try {
      const result = await request(`/admin/ticket-cancel-requests/${id}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 批准退票申请
  async approveCancelRequest(id: string | number, processedBy?: number, remark?: string) {
    try {
      // 确保ID是数字类型
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) {
        throw new Error('无效的申请ID')
      }

      const body: any = {}
      if (processedBy !== undefined) body.processedBy = processedBy
      if (remark !== undefined) body.remark = remark
      const result = await request(
        `/admin/ticket-cancel-requests/${numericId}/approve`,
        {
          method: 'POST',
          body: Object.keys(body).length > 0 ? JSON.stringify(body) : undefined
        }
      )
      return result
    } catch (error) {
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        console.warn('模拟：退票批准成功（后端不可用）', error)
        return { success: true, message: '模拟：已批准', data: {} }
      }
      throw error
    }
  },

  // 拒绝退票申请
  async rejectCancelRequest(id: string | number, reason: string, processedBy?: number) {
    try {
      const numericId = typeof id === 'string' ? parseInt(id, 10) : id
      if (isNaN(numericId)) {
        throw new Error('无效的申请ID')
      }

      const body: any = { remark: reason }
      if (processedBy !== undefined) body.processedBy = processedBy
      const result = await request(
        `/admin/ticket-cancel-requests/${numericId}/reject`,
        {
          method: 'POST',
          body: JSON.stringify(body)
        }
      )
      return result
    } catch (error) {
      if (import.meta.env.VITE_ENABLE_MOCK_APIS === 'true' || import.meta.env.DEV) {
        console.warn('模拟：退票拒绝成功（后端不可用）', error)
        return { success: true, message: '模拟：已拒绝', data: {} }
      }
      throw error
    }
  },

  // 批量批准
  async batchApprove(
    ids: (string | number)[],
    processedBy?: number,
    remark?: string
  ) {
    try {
      const numericIds = ids
        .map(id => {
          if (typeof id === 'string') {
            const num = parseInt(id, 10)
            return isNaN(num) ? 0 : num
          }
          return id
        })
        .filter(id => id > 0)

      const body: any = { ids: numericIds }
      if (processedBy !== undefined) body.processedBy = processedBy
      if (remark !== undefined) body.remark = remark
      const result = await request('/admin/ticket-cancel-requests/batch-approve', {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量拒绝
  async batchReject(
    ids: (string | number)[],
    reason: string,
    processedBy?: number
  ) {
    try {
      const numericIds = ids
        .map(id => {
          if (typeof id === 'string') {
            const num = parseInt(id, 10)
            return isNaN(num) ? 0 : num
          }
          return id
        })
        .filter(id => id > 0)

      const body: any = {
        ids: numericIds,
        remark: reason
      }
      if (processedBy !== undefined) body.processedBy = processedBy
      const result = await request('/admin/ticket-cancel-requests/batch-reject', {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取统计数据
  async getStatistics() {
    try {
      const result = await request('/admin/ticket-cancel-requests/statistics')
      return result
    } catch (error) {
      throw error
    }
  }
}

// 退订管理API
export const refundManagementApi = {
  // 获取退订申请列表
  async getRefundList(params: {
    page?: number
    size?: number
    orderNumber?: string
    passengerName?: string
    startDate?: string
    endDate?: string
    status?: string
  }) {
    try {
      const query = new URLSearchParams(params as any).toString()
      const result = await request(`/admin/refunds?${query}`)
      return result
    } catch (error) {
      console.warn('获取退订列表API不可用，使用模拟数据')
      throw error
    }
  },

  // 批准退订申请
  async approveRefund(refundId: string) {
    try {
      const result = await request(`/admin/refunds/${refundId}/approve`, {
        method: 'POST'
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 拒绝退订申请
  async rejectRefund(refundId: string, reason: string) {
    try {
      const result = await request(`/admin/refunds/${refundId}/reject`, {
        method: 'POST',
        body: JSON.stringify({ reason })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量批准
  async batchApprove(refundIds: string[]) {
    try {
      const result = await request('/admin/refunds/batch-approve', {
        method: 'POST',
        body: JSON.stringify({ ids: refundIds })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量拒绝
  async batchReject(refundIds: string[], reason: string) {
    try {
      const result = await request('/admin/refunds/batch-reject', {
        method: 'POST',
        body: JSON.stringify({ ids: refundIds, reason })
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 系统设置API
export const systemSettingsApi = {
  // 获取系统设置
  async getSettings() {
    try {
      const result = await request('/admin/settings')
      return result
    } catch (error) {
      console.warn('获取系统设置API不可用，使用模拟数据')
      throw error
    }
  },

  // 保存系统设置
  async saveSettings(settings: any) {
    try {
      const result = await request('/admin/settings', {
        method: 'PUT',
        body: JSON.stringify(settings)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 上传系统Logo
  async uploadLogo(file: File) {
    try {
      const formData = new FormData()
      formData.append('logo', file)
      const result = await request('/admin/settings/logo', {
        method: 'POST',
        body: formData,
        headers: {} // 让浏览器自动设置Content-Type
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 系统日志API
export const systemLogsApi = {
  // 获取操作日志列表
  async getLogs(params: {
    page?: number
    size?: number
    actionType?: string
    operator?: string
    module?: string
    startDate?: string
    endDate?: string
  }) {
    try {
      const query = new URLSearchParams(params as any).toString()
      const result = await request(`/admin/logs?${query}`)
      return result
    } catch (error) {
      console.warn('获取操作日志API不可用，使用模拟数据')
      throw error
    }
  },

  // 导出日志
  async exportLogs(params: any) {
    try {
      const query = new URLSearchParams(params as any).toString()
      const response = await fetch(`${API_BASE_URL}/admin/logs/export?${query}`)
      const blob = await response.blob()
      return blob
    } catch (error) {
      throw error
    }
  },

  // 清空日志
  async clearLogs() {
    try {
      const result = await request('/admin/logs', {
        method: 'DELETE'
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 用户管理API
export const userManagementApi = {
  // 获取用户列表
  async getUserList(params: {
    page?: number
    size?: number
    keyword?: string
    role?: string
    department?: string
    status?: string
  }) {
    try {
      // 过滤掉 undefined 和空字符串的参数
      const cleanParams: any = {}
      Object.keys(params).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      const query = new URLSearchParams(cleanParams).toString()
      const url = query ? `/admin/users?${query}` : '/admin/users'
      console.log('请求用户列表URL:', url)
      const result = await request(url)
      return result
    } catch (error) {
      console.warn('获取用户列表API不可用，使用模拟数据')
      throw error
    }
  },

  // 创建用户
  async createUser(userData: {
    name: string
    username: string
    idCard: string
    phone: string
    role: string
    password: string
  }) {
    try {
      const result = await request('/admin/users', {
        method: 'POST',
        body: JSON.stringify(userData)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 更新用户
  async updateUser(userId: string, userData: any) {
    try {
      const result = await request(`/admin/users/${userId}`, {
        method: 'PUT',
        body: JSON.stringify(userData)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 切换用户状态
  async toggleUserStatus(userId: string) {
    try {
      const result = await request(`/admin/users/${userId}/toggle-status`, {
        method: 'POST'
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取用户权限
  async getUserPermissions(userId: string) {
    try {
      const result = await request(`/admin/users/${userId}/permissions`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 更新用户权限
  async updateUserPermissions(userId: string, permissions: Array<{ id: string; checked: boolean }>) {
    try {
      const result = await request(`/admin/users/${userId}/permissions`, {
        method: 'PUT',
        body: JSON.stringify({ permissions })
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 批量导入用户
  async batchImportUsers(file: File) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      const result = await request('/admin/users/batch-import', {
        method: 'POST',
        body: formData,
        headers: {} // 让浏览器自动设置Content-Type
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 重置用户密码（复用更新用户接口，只更新 password 字段）
  async resetPassword(userId: string, data: { newPassword: string }) {
    try {
      const payload = {
        password: data.newPassword
      }
      const result = await request(`/admin/users/${userId}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 乘客相关API
export const passengerApi = {
  // 获取乘客个人信息
  async getProfile() {
    try {
      const result = await request('/passenger/profile')
      return result
    } catch (error) {
      console.warn('获取乘客信息API不可用，使用模拟数据')
      // 从本地存储获取
      const userInfo = sessionStorage.getItem('user_info')
      if (userInfo) {
        return JSON.parse(userInfo)
      }
      throw error
    }
  },

  // 更新乘客个人信息
  async updateProfile(profileData: {
    realName?: string
    email?: string
    phone?: string
    avatar?: string
  }) {
    try {
      const result = await request('/passenger/profile', {
        method: 'PUT',
        body: JSON.stringify(profileData)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取乘客统计数据
  async getStatistics() {
    try {
      // 从sessionStorage获取当前用户ID
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const result = await request(`/passenger/statistics?passengerId=${currentUser.id}`)
      
      // 处理响应数据格式
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      console.warn('获取统计数据API不可用，使用模拟数据', error)
      return {
        totalOrders: 12,
        upcomingFlights: 2,
        completedFlights: 10,
        totalSpent: 28560,
        averageSpent: 2380,
        orderGrowth: 15
      }
    }
  },

  // 获取消费趋势数据（近6个月）
  async getSpendingTrend() {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const result = await request(`/passenger/statistics/spending-trend?passengerId=${currentUser.id}`)
      
      if (result && result.success && result.data) {
        return result.data.trend || []
      }
      return []
    } catch (error) {
      console.warn('获取消费趋势API不可用，使用模拟数据', error)
      return [
        { month: '6月', value: 3200 },
        { month: '7月', value: 4500 },
        { month: '8月', value: 3800 },
        { month: '9月', value: 5200 },
        { month: '10月', value: 4800 },
        { month: '11月', value: 5500 }
      ]
    }
  },

  // 获取月度对比数据（订单数 vs 消费额）
  async getMonthlyComparison() {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const result = await request(`/passenger/statistics/monthly-comparison?passengerId=${currentUser.id}`)
      
      if (result && result.success && result.data) {
        return result.data.comparison || []
      }
      return []
    } catch (error) {
      console.warn('获取月度对比API不可用，使用模拟数据', error)
      return [
        { month: '6月', orders: 2, spending: 3200 },
        { month: '7月', orders: 3, spending: 4500 },
        { month: '8月', orders: 2, spending: 3800 },
        { month: '9月', orders: 4, spending: 5200 },
        { month: '10月', orders: 3, spending: 4800 },
        { month: '11月', orders: 4, spending: 5500 }
      ]
    }
  },

  // 获取订单状态分布
  async getOrderStatusDistribution() {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const result = await request(`/passenger/statistics/status-distribution?passengerId=${currentUser.id}`)
      
      if (result && result.success && result.data) {
        return result.data.distribution || []
      }
      return []
    } catch (error) {
      console.warn('获取状态分布API失败:', error)
      return []
    }
  },

  // 获取热门航线
  async getPopularRoutes(limit: number = 5) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const result = await request(`/passenger/statistics/popular-routes?passengerId=${currentUser.id}&limit=${limit}`)
      
      if (result && result.success && result.data) {
        return result.data.routes || []
      }
      return []
    } catch (error) {
      console.warn('获取热门航线API失败:', error)
      return []
    }
  },

  // 获取所有统计数据（一次性获取）
  async getAllStatistics() {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const result = await request(`/passenger/statistics/all?passengerId=${currentUser.id}`)
      
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      console.warn('获取所有统计数据API不可用', error)
      throw error
    }
  },

  // 获取待出行航班列表
  async getUpcomingFlights() {
    try {
      const result = await request('/passenger/flights/upcoming')
      return result
    } catch (error) {
      console.warn('获取待出行航班API不可用，使用模拟数据')
      return {
        flights: [
          {
            id: 1,
            date: '2023-12-20',
            departureTime: '09:00',
            arrivalTime: '11:30',
            origin: '北京',
            destination: '上海',
            flightNumber: 'CA1234',
            status: '准点'
          },
          {
            id: 2,
            date: '2023-12-25',
            departureTime: '14:30',
            arrivalTime: '17:00',
            origin: '上海',
            destination: '广州',
            flightNumber: 'MU5678',
            status: '准点'
          }
        ]
      }
    }
  },

  // 获取最近订单
  async getRecentOrders(limit: number = 5) {
    try {
      // 从sessionStorage获取当前用户ID
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const result = await request(`/passenger/orders/recent?passengerId=${currentUser.id}&limit=${limit}`)
      
      // 处理响应数据格式
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      console.warn('获取最近订单API不可用，使用模拟数据', error)
      return {
        orders: []
      }
    }
  },

  // 申请改签
  async requestChange(changeData: {
    orderId: string
    newFlightId: string
    reason?: string
  }) {
    try {
      const result = await request('/passenger/orders/change', {
        method: 'POST',
        body: JSON.stringify(changeData)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 申请退票
  async requestRefund(refundData: {
    orderId: string
    reason?: string
  }) {
    try {
      const result = await request('/passenger/orders/refund', {
        method: 'POST',
        body: JSON.stringify(refundData)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 在线值机
  async checkIn(checkInData: {
    orderId: string
    seatNumber?: string
    baggage?: number
  }) {
    try {
      const result = await request('/passenger/check-in', {
        method: 'POST',
        body: JSON.stringify(checkInData)
      })
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取行程单
  async getItinerary(orderId: string) {
    try {
      const response = await fetch(`${API_BASE_URL}/passenger/orders/${orderId}/itinerary`)
      const blob = await response.blob()
      return blob
    } catch (error) {
      throw error
    }
  },

  // 获取推荐航班
  async getRecommendedFlights() {
    try {
      const result = await request('/passenger/flights/recommended')
      return result
    } catch (error) {
      console.warn('获取推荐航班API不可用，使用模拟数据')
      return {
        flights: [
          { id: 'r1', route: '深圳 ⇀ 成都', price: '￥890 起' },
          { id: 'r2', route: '杭州 ⇀ 哈尔滨', price: '￥680 起' },
          { id: 'r3', route: '上海 ⇀ 香港', price: '￥760 起' }
        ]
      }
    }
  },

  // 获取行程通知
  async getTravelNotices() {
    try {
      const result = await request('/passenger/notices')
      return result
    } catch (error) {
      console.warn('获取行程通知API不可用，使用模拟数据')
      return {
        notices: [
          {
            id: 'n1',
            message: 'MU567 航班将于出发前 3 小时开放在线值机，请提前准备证件。',
            type: 'info',
            time: new Date().toISOString()
          }
        ]
      }
    }
  },

  // 联系客服
  async contactSupport(supportData: {
    orderId?: string
    subject: string
    message: string
    type: 'email' | 'phone' | 'chat'
  }) {
    try {
      const result = await request('/passenger/support', {
        method: 'POST',
        body: JSON.stringify(supportData)
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 机票相关API（乘客）
export const ticketApi = {
  // 获取当前用户的机票列表（分页）
  async getMyTickets(params?: { page?: number; size?: number; ticketNo?: string; ticketStatus?: string }) {
    try {
      const queryParams: any = {
        page: params?.page ?? 0,
        size: params?.size ?? 10
      }
      // 若前端持有当前用户信息，则同时以 query param 形式传 passengerId 作为后端回退兼容
      try {
        const currentUser = apiUtils.getCurrentUser()
        if (currentUser && currentUser.id) {
          queryParams.passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
        }
      } catch {}
      // 支持按机票号搜索
      if (params && (params as any).ticketNo) {
        queryParams.ticketNo = (params as any).ticketNo
      }
      // 支持按机票状态过滤（例如 '已出票'）
      if (params && (params as any).ticketStatus) {
        queryParams.ticketStatus = (params as any).ticketStatus
      }
      const query = new URLSearchParams(queryParams as any).toString()
      const result = await request(`/passenger/tickets?${query}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取机票详情（不返回敏感字段：id, passenger_id, created_at, updated_at）
  async getTicketDetail(ticketId: string | number) {
    try {
      // 后端从服务端会话中获取当前登录用户ID，前端无需传 passengerId
      // 同时尝试把当前用户id作为回退参数传入，便于服务端在没有session时兼容
      let url = `/passenger/tickets/${encodeURIComponent(String(ticketId))}`
      try {
        const currentUser = apiUtils.getCurrentUser()
        if (currentUser && currentUser.id) {
          url += `?passengerId=${encodeURIComponent(String(currentUser.id))}`
        }
      } catch {}
      const result = await request(url)
      return result
    } catch (error) {
      throw error
    }
  },

  // 打印机票（后端返回PDF Blob或下载URL）
  async printTicket(ticketId: string | number) {
    try {
      // 后端从服务端会话中获取当前登录用户ID
      let fullUrl = `${API_BASE_URL}/passenger/tickets/${encodeURIComponent(String(ticketId))}/print`
      try {
        const currentUser = apiUtils.getCurrentUser()
        if (currentUser && currentUser.id) {
          fullUrl += `?passengerId=${encodeURIComponent(String(currentUser.id))}`
        }
      } catch {}
      const response = await fetch(fullUrl, {
        method: 'GET',
        credentials: 'include',
        headers: {
          // 允许后端控制返回类型
          Accept: 'application/pdf, application/octet-stream, application/json'
        }
      })
      if (!response.ok) {
        // 尝试解析错误信息
        let errText = response.statusText
        try {
          const txt = await response.text()
          errText = txt || errText
        } catch {}
        throw new Error(errText || `打印失败 (${response.status})`)
      }
      const contentType = response.headers.get('content-type') || ''
      if (contentType.includes('application/json')) {
        // 返回JSON（可能是{success, data: { url } }）
        const json = await response.json()
        return json
      }
      // 返回文件流
      const blob = await response.blob()
      return blob
    } catch (error) {
      throw error
    }
  },

  // 软删除机票（标记为已删除，但不从数据库物理删除）
  async softDeleteTicket(ticketId: number, passengerId: number) {
    return request(`/passenger/tickets/${ticketId}/soft-delete?passengerId=${passengerId}`, {
      method: 'DELETE'
    })
  },

  async softDeleteTicketByTicketNo(ticketNo: string, passengerId: number) {
    return request(`/passenger/tickets/soft-delete-by-ticket-no?ticketNo=${encodeURIComponent(ticketNo)}&passengerId=${passengerId}`, {
      method: 'DELETE'
    })
  }
}

// 数据统计API
export const statisticsApi = {
  // 获取仪表板统计数据
  async getDashboardStats() {
    try {
      const result = await request('/admin/statistics/dashboard')
      return result
    } catch (error) {
      console.warn('获取统计数据API不可用，使用模拟数据')
      throw error
    }
  },

  // 获取订单趋势
  async getOrderTrend(params: { period: number }) {
    try {
      const query = new URLSearchParams(params as any).toString()
      const result = await request(`/admin/statistics/order-trend?${query}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取热门航线
  async getTopRoutes(limit: number = 5) {
    try {
      const result = await request(`/admin/statistics/top-routes?limit=${limit}`)
      return result
    } catch (error) {
      throw error
    }
  },

  // 获取运营待处理事项数量
  async getPendingItems() {
    try {
      const result = await request('/admin/statistics/pending-items')
      return result
    } catch (error) {
      console.warn('获取待处理事项API不可用，使用模拟数据')
      // 返回模拟数据
      return {
        flightAdjustments: 3,
        maintenanceAlerts: 2,
        operationalReports: 5
      }
    }
  },

  // 记录待处理事项点击行为
  async recordPendingItemClick(itemType: string) {
    try {
      const result = await request('/admin/statistics/record-pending-click', {
        method: 'POST',
        body: JSON.stringify({ itemType })
      })
      return result
    } catch (error) {
      // 静默失败，不影响用户体验
      console.warn('记录点击行为失败:', error)
      return { success: false }
    }
  }
}

// 补充：获取订单类型分布
statisticsApi.getOrderTypeDistribution = async function() {
  try {
    const result = await request('/admin/statistics/order-type-distribution')
    return result
  } catch (error) {
    throw error
  }
}

// 管理员异常告警 API
export const adminAlertsApi = {
  // 获取最新告警（支持分页：page, size）
  async getAlerts(page: number = 0, size: number = 20, status?: string) {
    try {
      const qs = [`page=${page}`, `size=${size}`]
      if (status) qs.push(`status=${encodeURIComponent(status)}`)
      const result = await request(`/admin/alerts?${qs.join('&')}`)
      // 返回后端统一格式或数组，交给调用方处理
      return result
    } catch (error) {
      console.warn('获取管理员告警API不可用', error)
      throw error
    }
  }
  ,
  // 创建新的告警/异常事件
  async createAlert(payload: any) {
    try {
      const result = await request('/admin/alerts', {
        method: 'POST',
        body: JSON.stringify(payload)
      })
      return result
    } catch (error) {
      console.warn('创建告警API不可用', error)
      throw error
    }
  },
  // 处理/更新告警状态
  async processAlert(id: number, status: string, operatorNote?: string) {
    try {
      const body: any = { status }
      if (operatorNote) body.operatorNote = operatorNote
      const result = await request(`/admin/alerts/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      console.warn('处理告警API不可用', error)
      throw error
    }
  }
}

// 常用乘客API
export const frequentPassengerApi = {
  // 获取用户的所有常用乘客
  async getFrequentPassengers(userId: number) {
    try {
      const result = await request(`/frequent-passengers?userId=${userId}`)
      // 后端返回格式：{success: true, message: "查询成功", data: {list: [...], total: ...}}
      if (result && result.success && result.data) {
        return result.data.list || []
      }
      return []
    } catch (error) {
      console.warn('获取常用乘客API不可用', error)
      return []
    }
  },

  // 添加常用乘客
  async addFrequentPassenger(userId: number, passengerData: {
    name: string
    idCard: string
    relationship?: string
    phone?: string
    remarks?: string
    isDefault?: boolean
  }) {
    try {
      const result = await request(`/frequent-passengers?userId=${userId}`, {
        method: 'POST',
        body: JSON.stringify(passengerData)
      })
      // 后端返回格式：{success: true, message: "添加常用乘客成功", data: {...}}
      if (result && result.success && result.data) {
        return result.data
      }
      throw new Error(result?.message || '添加失败')
    } catch (error: any) {
      throw new Error(error.message || '添加常用乘客失败')
    }
  },

  // 更新常用乘客
  async updateFrequentPassenger(userId: number, id: number, passengerData: {
    name?: string
    idCard?: string
    relationship?: string
    phone?: string
    remarks?: string
    isDefault?: boolean
  }) {
    try {
      const result = await request(`/frequent-passengers/${id}?userId=${userId}`, {
        method: 'PUT',
        body: JSON.stringify(passengerData)
      })
      // 后端返回格式：{success: true, message: "更新常用乘客成功", data: {...}}
      if (result && result.success && result.data) {
        return result.data
      }
      throw new Error(result?.message || '更新失败')
    } catch (error: any) {
      throw new Error(error.message || '更新常用乘客失败')
    }
  },

  // 删除常用乘客
  async deleteFrequentPassenger(userId: number, id: number) {
    try {
      const result = await request(`/frequent-passengers/${id}?userId=${userId}`, {
        method: 'DELETE'
      })
      // 后端返回格式：{success: true, message: "删除常用乘客成功", data: null}
      if (result && result.success) {
        return true
      }
      throw new Error(result?.message || '删除失败')
    } catch (error: any) {
      throw new Error(error.message || '删除常用乘客失败')
    }
  },

  // 设置默认常用乘客
  async setDefaultFrequentPassenger(userId: number, id: number) {
    try {
      const result = await request(`/frequent-passengers/${id}/set-default?userId=${userId}`, {
        method: 'PUT'
      })
      if (result && result.success) {
        return true
      }
      throw new Error(result?.message || '设置失败')
    } catch (error: any) {
      throw new Error(error.message || '设置默认常用乘客失败')
    }
  },

  // 获取默认常用乘客
  async getDefaultFrequentPassenger(userId: number) {
    try {
      const result = await request(`/frequent-passengers/default?userId=${userId}`)
      if (result && result.success && result.data) {
        return result.data
      }
      return null
    } catch (error) {
      console.warn('获取默认常用乘客API不可用', error)
      return null
    }
  }
}

// 积分相关API
export const pointsApi = {
  // 获取用户积分信息
  async getPointsInfo() {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const result = await request(`/passenger/points?passengerId=${passengerId}`)
      
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      console.warn('获取积分信息API不可用', error)
      throw error
    }
  },

  // 获取积分记录
  async getPointsHistory(params?: { page?: number; size?: number }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const queryParams: any = { passengerId, ...(params || {}) }
      const query = new URLSearchParams(queryParams as any).toString()
      const result = await request(`/passenger/points/history?${query}`)
      
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      console.warn('获取积分记录API不可用', error)
      throw error
    }
  },

  // 积分兑换
  async exchangePoints(exchangeData: {
    exchangeType: string
    requiredPoints: number
    itemName: string
  }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const result = await request('/passenger/points/exchange', {
        method: 'POST',
        body: JSON.stringify({
          passengerId,
          ...exchangeData
        })
      })
      
      if (result && result.success) {
        return result
      }
      throw new Error(result?.message || '兑换失败')
    } catch (error: any) {
      throw new Error(error.message || '积分兑换失败')
    }
  }
}

// 优惠券相关API
export const couponApi = {
  // 获取优惠券列表
  async getCoupons(params?: { status?: string; filter?: string }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const queryParams: any = { passengerId, ...(params || {}) }
      const query = new URLSearchParams(queryParams as any).toString()
      const result = await request(`/passenger/coupons?${query}`)
      
      if (result && result.success && result.data) {
        return result.data
      }
      return result
    } catch (error) {
      console.warn('获取优惠券列表API不可用', error)
      throw error
    }
  },

  // 领取优惠券
  async receiveCoupon(couponId: number) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const result = await request(`/passenger/coupons/${couponId}/receive?passengerId=${passengerId}`, {
        method: 'POST'
      })
      
      if (result && result.success) {
        return result
      }
      throw new Error(result?.message || '领取失败')
    } catch (error: any) {
      throw new Error(error.message || '领取优惠券失败')
    }
  },

  // 使用优惠券
  async useCoupon(couponId: number) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const result = await request(`/passenger/coupons/${couponId}/use?passengerId=${passengerId}`, {
        method: 'POST'
      })
      
      if (result && result.success) {
        return result
      }
      throw new Error(result?.message || '使用失败')
    } catch (error: any) {
      throw new Error(error.message || '使用优惠券失败')
    }
  }
}

// 重点旅客预约API
export const specialServiceRequestApi = {
  // 创建重点旅客预约
  async createRequest(requestData: {
    orderNo: string
    phone: string
    passengerType: string
    departureAirport: string
    arrivalAirport: string
    entryServices?: Record<string, any>
    exitServices?: Record<string, any>
    description?: string
  }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      // 将服务需求对象转换为JSON字符串
      const entryServicesJson = requestData.entryServices ? JSON.stringify(requestData.entryServices) : null
      const exitServicesJson = requestData.exitServices ? JSON.stringify(requestData.exitServices) : null
      
      const payload = {
        passengerId,
        orderNo: requestData.orderNo,
        phone: requestData.phone,
        passengerType: requestData.passengerType,
        departureAirport: requestData.departureAirport,
        arrivalAirport: requestData.arrivalAirport,
        entryServices: entryServicesJson,
        exitServices: exitServicesJson,
        description: requestData.description || null,
        status: 'pending'
      }
      
      const result = await request('/special-service-requests', {
        method: 'POST',
        body: JSON.stringify(payload)
      })
      
      if (result && result.success) {
        return result.data
      }
      throw new Error(result?.message || '提交失败')
    } catch (error: any) {
      throw new Error(error.message || '提交重点旅客预约失败')
    }
  },
  
  // 获取用户的预约列表
  async getRequests(params?: { page?: number; size?: number; status?: string }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const queryParams: any = {
        passengerId,
        page: params?.page || 0,
        size: params?.size || 10
      }
      
      if (params?.status) {
        queryParams.status = params.status
      }
      
      const query = new URLSearchParams(queryParams as any).toString()
      const result = await request(`/special-service-requests?${query}`)
      
      if (result && result.success && result.data) {
        // 解析JSON字段
        const list = result.data.list || []
        list.forEach((item: any) => {
          if (item.entryServices) {
            try {
              item.entryServices = typeof item.entryServices === 'string' 
                ? JSON.parse(item.entryServices) 
                : item.entryServices
            } catch (e) {
              item.entryServices = {}
            }
          }
          if (item.exitServices) {
            try {
              item.exitServices = typeof item.exitServices === 'string' 
                ? JSON.parse(item.exitServices) 
                : item.exitServices
            } catch (e) {
              item.exitServices = {}
            }
          }
        })
        
        return {
          list,
          total: result.data.total || 0,
          page: result.data.page || 0,
          size: result.data.size || 10,
          totalPages: result.data.totalPages || 0
        }
      }
      
      return {
        list: [],
        total: 0,
        page: 0,
        size: 10,
        totalPages: 0
      }
    } catch (error: any) {
      console.warn('获取预约列表API不可用', error)
      throw error
    }
  },
  
  // 根据ID获取预约详情
  async getRequestById(id: number) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const result = await request(`/special-service-requests/${id}?passengerId=${passengerId}`)
      
      if (result && result.success && result.data) {
        const item = result.data
        // 解析JSON字段
        if (item.entryServices) {
          try {
            item.entryServices = typeof item.entryServices === 'string' 
              ? JSON.parse(item.entryServices) 
              : item.entryServices
          } catch (e) {
            item.entryServices = {}
          }
        }
        if (item.exitServices) {
          try {
            item.exitServices = typeof item.exitServices === 'string' 
              ? JSON.parse(item.exitServices) 
              : item.exitServices
          } catch (e) {
            item.exitServices = {}
          }
        }
        return item
      }
      throw new Error(result?.message || '查询失败')
    } catch (error: any) {
      throw new Error(error.message || '获取预约详情失败')
    }
  },
  
  // 更新预约状态
  async updateStatus(id: number, status: string) {
    try {
      const result = await request(`/special-service-requests/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status })
      })
      
      if (result && result.success) {
        return result.data
      }
      throw new Error(result?.message || '更新失败')
    } catch (error: any) {
      throw new Error(error.message || '更新预约状态失败')
    }
  },
  
  // 更新预约信息
  async updateRequest(id: number, requestData: {
    phone?: string
    passengerType?: string
    departureAirport?: string
    arrivalAirport?: string
    entryServices?: Record<string, any>
    exitServices?: Record<string, any>
    description?: string
  }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      // 将服务需求对象转换为JSON字符串
      const entryServicesJson = requestData.entryServices ? JSON.stringify(requestData.entryServices) : undefined
      const exitServicesJson = requestData.exitServices ? JSON.stringify(requestData.exitServices) : undefined
      
      const payload: any = {}
      if (requestData.phone !== undefined) payload.phone = requestData.phone
      if (requestData.passengerType !== undefined) payload.passengerType = requestData.passengerType
      if (requestData.departureAirport !== undefined) payload.departureAirport = requestData.departureAirport
      if (requestData.arrivalAirport !== undefined) payload.arrivalAirport = requestData.arrivalAirport
      if (entryServicesJson !== undefined) payload.entryServices = entryServicesJson
      if (exitServicesJson !== undefined) payload.exitServices = exitServicesJson
      if (requestData.description !== undefined) payload.description = requestData.description
      
      const result = await request(`/special-service-requests/${id}?passengerId=${passengerId}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      })
      
      if (result && result.success) {
        return result.data
      }
      throw new Error(result?.message || '更新失败')
    } catch (error: any) {
      throw new Error(error.message || '更新预约失败')
    }
  },
  
  // 删除预约
  async deleteRequest(id: number) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const result = await request(`/special-service-requests/${id}?passengerId=${passengerId}`, {
        method: 'DELETE'
      })
      
      if (result && result.success) {
        return true
      }
      throw new Error(result?.message || '删除失败')
    } catch (error: any) {
      throw new Error(error.message || '删除预约失败')
    }
  },
  
  // 获取预约统计
  async getCount(status?: string) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const queryParams: any = { passengerId }
      if (status) {
        queryParams.status = status
      }
      
      const query = new URLSearchParams(queryParams as any).toString()
      const result = await request(`/special-service-requests/count?${query}`)
      
      if (result && result.success && result.data) {
        return result.data.count || 0
      }
      return 0
    } catch (error) {
      console.warn('获取预约统计API不可用', error)
      return 0
    }
  }
}

// 管理员/运营专用的重点旅客预约管理API
export const adminSpecialServiceRequestApi = {
  // 获取预约列表（管理员视角，按状态过滤）
  async getList(params: { page?: number; size?: number; status?: string }) {
    try {
      const cleanParams: any = {}
      Object.keys(params || {}).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      const query = new URLSearchParams(cleanParams as any).toString()
      const url = query ? `/admin/special-service-requests?${query}` : '/admin/special-service-requests'
      const result = await request(url)
      return result
    } catch (error) {
      throw error
    }
  },
  // 获取分配给某个运营的预约（operatorId）
  async getAssigned(params: { operatorId: number; page?: number; size?: number }) {
    try {
      const cleanParams: any = {}
      Object.keys(params || {}).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      const query = new URLSearchParams(cleanParams as any).toString()
      const url = query ? `/admin/special-service-requests/assigned?${query}` : '/admin/special-service-requests/assigned'
      const result = await request(url)
      return result
    } catch (error) {
      throw error
    }
  },
  // 批准
  async approve(id: number) {
    try {
      const result = await request(`/admin/special-service-requests/${id}/approve`, {
        method: 'POST'
      })
      return result
    } catch (error) {
      throw error
    }
  },
  // 拒绝
  async reject(id: number, reason?: string) {
    try {
      const body: any = {}
      if (reason) body.reason = reason
      const result = await request(`/admin/special-service-requests/${id}/reject`, {
        method: 'POST',
        body: JSON.stringify(body)
      })
      return result
    } catch (error) {
      throw error
    }
  },
  // 指派运营
  async assign(id: number, operatorId: number) {
    try {
      const result = await request(`/admin/special-service-requests/${id}/assign`, {
        method: 'POST',
        body: JSON.stringify({ operatorId })
      })
      return result
    } catch (error) {
      throw error
    }
  },
  // 更新状态（通用）
  async updateStatus(id: number, status: string) {
    try {
      const result = await request(`/admin/special-service-requests/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status })
      })
      return result
    } catch (error) {
      throw error
    }
  }
}

// 行李管理API
export const baggageApi = {
  // 乘客端：创建行李登记
  async createBaggage(baggageData: {
    orderno: string
    baggageType: string
    baggageCount: number
    totalWeight?: number | null
    weightLimit?: number | null
    dimensions?: string
    baggageFee?: number | null
    excessFee?: number | null
    description?: string
    remark?: string
    arrivalTimeFlight?: string | null
    status?: string
  }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      // 构建请求体，确保字段名正确映射
      const requestBody: any = {
        passengerId,
        status: baggageData.status || 'registered', // 默认状态为"registered"（已登记）
        orderno: baggageData.orderno,
        baggageType: baggageData.baggageType,
        baggageCount: baggageData.baggageCount
      }
      
      // 添加可选字段
      if (baggageData.totalWeight !== null && baggageData.totalWeight !== undefined) {
        requestBody.totalWeight = baggageData.totalWeight
      }
      if (baggageData.weightLimit !== null && baggageData.weightLimit !== undefined) {
        requestBody.weightLimit = baggageData.weightLimit
      }
      if (baggageData.dimensions) {
        requestBody.dimensions = baggageData.dimensions
      }
      if (baggageData.baggageFee !== null && baggageData.baggageFee !== undefined) {
        requestBody.baggageFee = baggageData.baggageFee
      }
      if (baggageData.excessFee !== null && baggageData.excessFee !== undefined) {
        requestBody.excessFee = baggageData.excessFee
      }
      if (baggageData.description) {
        requestBody.description = baggageData.description
      }
      if (baggageData.remark) {
        requestBody.remark = baggageData.remark
      }
      // 到达时间字段名使用数据库字段名 arrival_time_flight
      if (baggageData.arrivalTimeFlight) {
        requestBody.arrival_time_flight = baggageData.arrivalTimeFlight
      }
      
      const result = await request('/baggage', {
        method: 'POST',
        body: JSON.stringify(requestBody)
      })
      
      if (result && result.success) {
        return result.data
      }
      throw new Error(result?.message || '登记失败')
    } catch (error: any) {
      throw new Error(error.message || '登记行李失败')
    }
  },

  // 乘客端：获取行李列表
  async getBaggageList(params?: { status?: string; page?: number; size?: number }) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const queryParams: any = { passengerId, ...(params || {}) }
      const query = new URLSearchParams(queryParams as any).toString()
      const result = await request(`/baggage?${query}`)
      
      if (result && result.success && result.data) {
        return result.data
      }
      return { list: [], total: 0, page: 0, size: 10, totalPages: 0 }
    } catch (error: any) {
      throw new Error(error.message || '获取行李列表失败')
    }
  },

  // 乘客端：获取行李详情
  async getBaggageDetail(id: number) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(passengerId)) {
        throw new Error('用户ID格式错误')
      }
      
      const result = await request(`/baggage/${id}?passengerId=${passengerId}`)
      
      if (result && result.success && result.data) {
        return result.data
      }
      throw new Error(result?.message || '查询失败')
    } catch (error: any) {
      throw new Error(error.message || '获取行李详情失败')
    }
  },
  // 乘客端：提取行李
  async pickupBaggage(id: number) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      const passengerId = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      const result = await request(`/baggage/${id}/pickup`, {
        method: 'POST',
        body: JSON.stringify({ passengerId })
      })
      if (result && result.success && result.data) return result.data
      throw new Error(result?.message || '提取失败')
    } catch (error: any) {
      throw new Error(error.message || '提取行李失败')
    }
  },

  // 运营端：获取所有行李列表（带筛选和分页）
  async getOperatorBaggageList(params?: {
    page?: number
    size?: number
    status?: string
    baggageNo?: string
    orderno?: string
    flightNo?: string
    passengerName?: string
  }) {
    try {
      const cleanParams: any = {}
      Object.keys(params || {}).forEach(key => {
        const value = (params as any)[key]
        if (value !== undefined && value !== null && value !== '') {
          cleanParams[key] = value
        }
      })
      
      const query = new URLSearchParams(cleanParams as any).toString()
      const url = query ? `/op/baggage?${query}` : '/op/baggage'
      const result = await request(url)
      
      if (result && result.success && result.data) {
        return result.data
      }
      return { list: [], total: 0, page: 0, size: 10, totalPages: 0 }
    } catch (error: any) {
      throw new Error(error.message || '获取行李列表失败')
    }
  },

  // 运营端：更新行李状态
  async updateBaggageStatus(id: number, status: string, operatorRemark?: string) {
    try {
      const currentUser = apiUtils.getCurrentUser()
      if (!currentUser || !currentUser.id) {
        throw new Error('用户未登录')
      }
      
      const processedBy = typeof currentUser.id === 'string' ? parseInt(currentUser.id, 10) : currentUser.id
      if (isNaN(processedBy)) {
        throw new Error('用户ID格式错误')
      }
      
      const body: any = { status, processedBy }
      if (operatorRemark) {
        body.operatorRemark = operatorRemark
      }
      
      const result = await request(`/op/baggage/${id}/status`, {
        method: 'PUT',
        body: JSON.stringify(body)
      })
      
      if (result && result.success) {
        return result.data
      }
      throw new Error(result?.message || '更新失败')
    } catch (error: any) {
      throw new Error(error.message || '更新行李状态失败')
    }
  },

  // 运营端：获取行李详情
  async getOperatorBaggageDetail(id: number) {
    try {
      const result = await request(`/op/baggage/${id}`)
      
      if (result && result.success && result.data) {
        return result.data
      }
      throw new Error(result?.message || '查询失败')
    } catch (error: any) {
      throw new Error(error.message || '获取行李详情失败')
    }
  }
}

// 工具函数
export const apiUtils = {
  // 检查是否已登录
  isLoggedIn(): boolean {
    return !!sessionStorage.getItem('user_info')
  },
  
  // 获取当前用户信息
  getCurrentUser() {
    const userInfo = sessionStorage.getItem('user_info')
    return userInfo ? JSON.parse(userInfo) : null
  },
  
  // 设置用户信息
  setUserInfo(userInfo: any) {
    sessionStorage.setItem('user_info', JSON.stringify(userInfo))
  },
  
  // 清除用户信息
  clearUserInfo() {
    sessionStorage.removeItem('user_info')
  }
}