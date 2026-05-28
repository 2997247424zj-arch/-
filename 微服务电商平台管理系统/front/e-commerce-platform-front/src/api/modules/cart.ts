import request from '../request'

export const addCartAPI = (data: { productId: number; quantity: number }) => {
  return request.post('/cart/add', data)
}

export const addToCartAPI = addCartAPI

export const getCartListAPI = () => {
  return request.get('/cart/list')
}

export const updateCartAPI = (data: { id: number; quantity: number; isSelected?: number }) => {
  return request.post('/cart/update', data)
}

export const deleteCartAPI = (id: number) => {
  return request.post('/cart/delete', { ids: [id] })
}

export const deleteCartBatchAPI = (ids: number[]) => {
  return request.post('/cart/delete', { ids })
}
