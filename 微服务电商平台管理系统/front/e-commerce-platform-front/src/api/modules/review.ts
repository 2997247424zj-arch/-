import request from '../request'

/**
 * 添加评价
 */
export const addReviewAPI = (data: {
  productId: number
  orderId?: number
  rating: number
  content: string
  images?: string
}) => {
  return request({
    url: '/review/add',
    method: 'POST',
    data,
  })
}

/**
 * 获取商品评价列表
 */
export const getProductReviewsAPI = (productId: number) => {
  return request({
    url: `/review/product/${productId}`,
    method: 'GET',
  })
}

/**
 * 点赞/取消点赞
 */
export const toggleReviewLikeAPI = (reviewId: number) => {
  return request({
    url: `/review/like/${reviewId}`,
    method: 'POST',
  })
}

/**
 * 删除评价
 */
export const deleteReviewAPI = (reviewId: number) => {
  return request({
    url: `/review/delete/${reviewId}`,
    method: 'DELETE',
  })
}
