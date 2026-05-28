// 用户相关类型
export interface UserInfo {
  id: number
  username: string
  phone: string
  email?: string
  avatar?: string
  role?: string
  createTime?: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  phone: string
  password: string
  confirmPassword: string
}

// 商品相关类型
export interface Category {
  id: number
  name: string
  icon?: string
  parentId?: number
  sort?: number
}

export interface Product {
  id: number
  name: string
  description?: string
  price: number
  coverImg?: string
  images?: string[]
  categoryId?: number
  categoryName?: string
  stock?: number
  sales?: number
  status?: number
  createTime?: string
}

export interface ProductListParams {
  page?: number
  size?: number
  categoryId?: string | number
  keyword?: string
  minPrice?: number
  maxPrice?: number
  sortBy?: string
}

// 购物车相关类型
export interface CartItem {
  id: number
  productId: number
  productName: string
  productCoverImg?: string
  price: number
  quantity: number
  isSelected: number
  stock?: number
}

export interface AddCartParams {
  productId: number
  quantity: number
}

export interface UpdateCartParams {
  id: number
  quantity: number
  isSelected?: number
}

// 地址相关类型
export interface Address {
  id: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: number
}

export interface AddressForm {
  id?: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: number
}

// 订单相关类型
export interface Order {
  id: number
  orderNo: string
  totalAmount: number
  payAmount?: number
  status: OrderStatus
  paymentType?: number
  paymentTime?: string
  createTime: string
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  orderItems?: OrderItem[]
}

export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  productImage?: string
  price: number
  quantity: number
}

export interface CreateOrderParams {
  addressId: number
  totalAmount: number
  remark?: string
}

export interface OrderListParams {
  page?: number
  size?: number
  status?: OrderStatus
}

export enum OrderStatus {
  PENDING = 0, // 待支付
  PAID = 1, // 已支付
  CANCELLED = 2, // 已取消（前端展示枚举保持不变）
  COMPLETED = 3, // 已完成
}

// 支付相关类型
export interface CreatePayParams {
  orderNo: string
  payType: PayType
}

export enum PayType {
  ALIPAY = 1, // 支付宝
  WECHAT = 2, // 微信
}

// API 响应类型
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}
