import request from '../request'

// ==================== 用户管理 ====================

/**
 * 获取用户列表（分页）
 */
export const getUserListAPI = (params: {
  page: number
  size: number
  keyword?: string
}) => {
  return request({
    url: '/admin/user/list',
    method: 'GET',
    params,
  })
}

/**
 * 更新用户状态
 */
export const updateUserStatusAPI = (userId: number, status: number) => {
  return request({
    url: `/admin/user/status/${userId}`,
    method: 'PUT',
    params: { status },
  })
}

/**
 * 删除用户
 */
export const deleteUserAPI = (userId: number) => {
  return request({
    url: `/admin/user/delete/${userId}`,
    method: 'DELETE',
  })
}

/**
 * 获取用户详情
 */
export const getUserDetailAPI = (userId: number) => {
  return request({
    url: `/admin/user/detail/${userId}`,
    method: 'GET',
  })
}

// ==================== 商品管理 ====================

/**
 * 获取商品列表（分页）
 */
export const getProductListAPI = (params: {
  page: number
  size: number
  keyword?: string
  categoryId?: number
}) => {
  return request({
    url: '/admin/product/list',
    method: 'GET',
    params,
  })
}

/**
 * 添加商品
 */
export const addProductAPI = (data: any) => {
  return request({
    url: '/admin/product/add',
    method: 'POST',
    data,
  })
}

/**
 * 更新商品
 */
export const updateProductAPI = (data: any) => {
  return request({
    url: '/admin/product/update',
    method: 'PUT',
    data,
  })
}

/**
 * 删除商品
 */
export const deleteProductAPI = (id: number) => {
  return request({
    url: `/admin/product/delete/${id}`,
    method: 'DELETE',
  })
}

/**
 * 更新商品状态
 */
export const updateProductStatusAPI = (id: number, status: number) => {
  return request({
    url: `/admin/product/status/${id}`,
    method: 'PUT',
    params: { status },
  })
}

/**
 * 更新商品库存
 */
export const updateProductStockAPI = (id: number, stock: number) => {
  return request({
    url: `/admin/product/stock/${id}`,
    method: 'PUT',
    params: { stock },
  })
}

// ==================== 订单管理 ====================

/**
 * 获取所有订单（分页）
 */
export const getAllOrdersAPI = (params: {
  page: number
  size: number
  status?: number
  keyword?: string
}) => {
  return request({
    url: '/admin/order/list',
    method: 'GET',
    params,
  })
}

/**
 * 更新订单状态
 */
export const updateOrderStatusAPI = (orderNo: string, status: number) => {
  return request({
    url: `/admin/order/status/${orderNo}`,
    method: 'PUT',
    params: { status },
  })
}

/**
 * 获取订单详情
 */
export const getOrderDetailAPI = (orderNo: string) => {
  return request({
    url: `/admin/order/detail/${orderNo}`,
    method: 'GET',
  })
}

/**
 * 删除订单
 */
export const deleteOrderAPI = (orderNo: string) => {
  return request({
    url: `/admin/order/delete/${orderNo}`,
    method: 'DELETE',
  })
}

// ==================== 统计数据 ====================

/**
 * 获取统计数据
 */
export const getStatsAPI = () => {
  return request({
    url: '/admin/stats',
    method: 'GET',
  })
}

/**
 * 获取活动记录
 */
export const getActivitiesAPI = () => {
  return request({
    url: '/admin/activities',
    method: 'GET',
  })
}
