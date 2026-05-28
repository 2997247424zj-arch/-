// Mock API 开关和拦截器
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import {
  mockUserInfo,
  mockCategories,
  mockProducts,
  mockCartItems,
  mockAddresses,
  mockOrders,
  getProductById,
  getOrderByNo,
  searchProducts,
  getProductsByCategory,
} from '@/utils/mockData'

// 是否启用 Mock 数据
export const USE_MOCK = false

// 模拟延迟
const delay = (ms: number = 300) => new Promise((resolve) => setTimeout(resolve, ms))

// Mock 响应包装
const mockResponse = async <T>(data: T, delayMs: number = 300): Promise<AxiosResponse> => {
  await delay(delayMs)
  return {
    data: {
      code: 200,
      msg: 'success',
      data,
    },
    status: 200,
    statusText: 'OK',
    headers: {},
    config: {} as InternalAxiosRequestConfig,
  }
}

// Mock 请求拦截器
export const mockRequestInterceptor = async (
  config: InternalAxiosRequestConfig,
): Promise<AxiosResponse | InternalAxiosRequestConfig> => {
  if (!USE_MOCK) return config

  const { url, method, params, data } = config

  // 用户相关
  if (url === '/api/user/login' && method === 'post') {
    return mockResponse({ token: 'mock-token-' + Date.now(), userInfo: mockUserInfo })
  }

  if (url === '/api/user/register' && method === 'post') {
    return mockResponse({ message: '注册成功' })
  }

  if (url === '/api/user/info' && method === 'get') {
    return mockResponse(mockUserInfo)
  }

  if (url === '/api/user/update' && method === 'put') {
    return mockResponse({ message: '修改成功' })
  }

  if (url === '/api/user/change-password' && method === 'post') {
    return mockResponse({ message: '密码修改成功' })
  }

  // 地址相关
  if (url === '/api/user/address/list' && method === 'get') {
    return mockResponse(mockAddresses)
  }

  if (url === '/api/user/address/add' && method === 'post') {
    return mockResponse({ message: '地址添加成功' })
  }

  if (url === '/api/user/address/update' && method === 'put') {
    return mockResponse({ message: '地址修改成功' })
  }

  if (url?.startsWith('/api/user/address/delete/') && method === 'delete') {
    return mockResponse({ message: '地址删除成功' })
  }

  if (url?.startsWith('/api/user/address/default/') && method === 'put') {
    return mockResponse({ message: '设置成功' })
  }

  // 商品分类
  if (url === '/api/product/category/list' && method === 'get') {
    return mockResponse(mockCategories)
  }

  // 商品列表
  if (url === '/api/product/list' && method === 'get') {
    const categoryId = params?.categoryId
    const page = params?.page || 1
    const size = params?.size || 12

    let filteredProducts = categoryId
      ? getProductsByCategory(Number(categoryId))
      : mockProducts

    // 价格筛选
    if (params?.minPrice) {
      filteredProducts = filteredProducts.filter((p) => p.price >= params.minPrice)
    }
    if (params?.maxPrice) {
      filteredProducts = filteredProducts.filter((p) => p.price <= params.maxPrice)
    }

    // 排序
    if (params?.sortBy === 'price_asc') {
      filteredProducts.sort((a, b) => a.price - b.price)
    } else if (params?.sortBy === 'price_desc') {
      filteredProducts.sort((a, b) => b.price - a.price)
    } else if (params?.sortBy === 'sales') {
      filteredProducts.sort((a, b) => (b.sales || 0) - (a.sales || 0))
    }

    const start = (page - 1) * size
    const end = start + size
    const list = filteredProducts.slice(start, end)

    return mockResponse({
      list,
      total: filteredProducts.length,
      page,
      size,
    })
  }

  // 商品搜索
  if (url === '/api/product/search' && method === 'get') {
    const keyword = params?.keyword || ''
    const filteredProducts = searchProducts(keyword)
    const page = params?.page || 1
    const size = params?.size || 12
    const start = (page - 1) * size
    const end = start + size
    const list = filteredProducts.slice(start, end)

    return mockResponse({
      list,
      total: filteredProducts.length,
      page,
      size,
    })
  }

  // 商品详情
  if (url?.startsWith('/api/product/detail/') && method === 'get') {
    const id = Number(url.split('/').pop())
    const product = getProductById(id)
    return mockResponse(product || null)
  }

  // 商品库存
  if (url?.startsWith('/api/product/stock/') && method === 'get') {
    const id = Number(url.split('/').pop())
    const product = getProductById(id)
    return mockResponse({ stockNum: product?.stock || 0 })
  }

  // 购物车相关
  if (url === '/api/cart/list' && method === 'get') {
    return mockResponse(mockCartItems)
  }

  if (url === '/api/cart/add' && method === 'post') {
    return mockResponse({ message: '添加成功' })
  }

  if (url === '/api/cart/update' && method === 'post') {
    return mockResponse({ message: '更新成功' })
  }

  if (url === '/api/cart/delete' && method === 'post') {
    return mockResponse({ message: '删除成功' })
  }

  // 订单相关
  if (url === '/api/order/list' && method === 'get') {
    let filteredOrders = mockOrders
    if (params?.status !== undefined) {
      filteredOrders = mockOrders.filter((o) => o.status === params.status)
    }
    return mockResponse({
      list: filteredOrders,
      total: filteredOrders.length,
    })
  }

  if (url === '/api/order/create' && method === 'post') {
    const orderNo = 'ORD' + Date.now()
    return mockResponse({ orderNo, message: '订单创建成功' })
  }

  if (url?.startsWith('/api/order/detail/') && method === 'get') {
    const orderNo = url.split('/').pop() as string
    const order = getOrderByNo(orderNo)
    return mockResponse(order || null)
  }

  if (url === '/api/order/cancel' && method === 'post') {
    return mockResponse({ message: '订单已取消' })
  }

  if (url === '/api/order/confirm-receive' && method === 'post') {
    return mockResponse({ message: '确认收货成功' })
  }

  // 支付相关
  if (url === '/api/pay/create' && method === 'post') {
    return mockResponse({ message: '支付成功' })
  }

  // 如果没有匹配的 mock，返回原始配置继续请求
  return config
}
