import request from '../request'

export const getCategoryListAPI = () => {
  return request.get('/product/category/list')
}

export const getProductListAPI = (params: any) => {
  return request.get('/product/list', { params })
}

export const getProductDetailAPI = (id: string | number) => {
  return request.get(`/product/detail/${id}`)
}

export const getProductStockAPI = (id: string | number) => {
  return request.get(`/product/stock/${id}`)
}

export const searchProductAPI = (keyword: string, params?: any) => {
  return request.get('/product/search', { params: { keyword, ...params } })
}

export const getProductsByCategoryAPI = (category: string) => {
  return request.get(`/product/category/${category}`)
}
