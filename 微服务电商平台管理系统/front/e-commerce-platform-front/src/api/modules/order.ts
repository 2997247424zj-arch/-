import request from '../request'

export const createOrderAPI = (data: any) => {
  return request.post('/order/create', data)
}

export const getOrderListAPI = (params?: any) => {
  return request.get('/order/my-list', { params })
}

export const getOrderDetailAPI = (orderNo: string) => {
  return request.get(`/order/detail/${orderNo}`)
}

export const cancelOrderAPI = (orderNo: string) => {
  return request.post('/order/cancel', { orderNo })
}

export const confirmReceiveAPI = (orderNo: string) => {
  return request.post('/order/confirm-receive', { orderNo })
}
