import request from '../request'

export const createPayAPI = (data: { orderId?: number; orderNo?: string; payMethod: string }) => {
  const payType = data.payMethod === 'wechat' ? 2 : 1
  return request.post('/pay/create', {
    ...data,
    payType,
  })
}

export const payNotifyAPI = (data: { payNo: string; status: string; transactionId?: string }) => {
  return request.post('/pay/notify', data)
}
