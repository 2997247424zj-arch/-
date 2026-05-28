import request from '../request'

export const loginAPI = (data: any) => {
  return request.post('/user/login', data)
}

export const registerAPI = (data: any) => {
  return request.post('/user/register', data)
}

export const getUserInfoAPI = () => {
  return request.get('/user/info')
}

export const updateUserInfoAPI = (data: any) => {
  return request.put('/user/update', data)
}

export const updatePasswordAPI = (data: { oldPassword: string; newPassword: string }) => {
  return request.post('/user/change-password', data)
}

export const uploadAvatarAPI = (data: FormData) => {
  return request.post('/user/avatar', data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export const getAddressListAPI = () => {
  return request.get('/user/address/list')
}

export const addAddressAPI = (data: any) => {
  return request.post('/user/address/add', data)
}

export const updateAddressAPI = (data: any) => {
  return request.put('/user/address/update', data)
}

export const deleteAddressAPI = (id: number) => {
  return request.delete(`/user/address/delete/${id}`)
}

export const setDefaultAddressAPI = (id: number) => {
  return request.put(`/user/address/default/${id}`)
}
