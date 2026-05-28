// 虚拟数据用于前端测试
import type { Product, Category, CartItem, Order, Address, UserInfo } from '@/types'
import { OrderStatus } from '@/types'

// 用户信息
export const mockUserInfo: UserInfo = {
  id: 1,
  username: '张三',
  phone: '13800138000',
  email: 'zhangsan@example.com',
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  createTime: '2024-01-15 10:30:00',
}

// 商品分类
export const mockCategories: Category[] = [
  { id: 1, name: '手机数码', icon: 'Iphone' },
  { id: 2, name: '电脑办公', icon: 'Platform' },
  { id: 3, name: '智能穿戴', icon: 'Watch' },
  { id: 4, name: '影音娱乐', icon: 'Headset' },
  { id: 5, name: '摄影摄像', icon: 'Camera' },
  { id: 6, name: '家用电器', icon: 'Monitor' },
]

// 商品列表
export const mockProducts: Product[] = [
  {
    id: 1,
    name: 'iPhone 15 Pro Max',
    description: '钛金属设计，A17 Pro 芯片，强大的专业级相机系统',
    price: 9999,
    coverImg: 'https://images.unsplash.com/photo-1592286927505-2fd0d113e4e7?w=400',
    categoryId: 1,
    categoryName: '手机数码',
    stock: 50,
    sales: 1280,
    status: 1,
    createTime: '2024-01-10 09:00:00',
  },
  {
    id: 2,
    name: 'MacBook Pro 16英寸',
    description: 'M3 Max 芯片，128GB 内存，专业级性能',
    price: 25999,
    coverImg: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400',
    categoryId: 2,
    categoryName: '电脑办公',
    stock: 30,
    sales: 856,
    status: 1,
    createTime: '2024-01-12 10:00:00',
  },
  {
    id: 3,
    name: 'Apple Watch Ultra 2',
    description: '钛金属表壳，双频 GPS，100米防水',
    price: 6499,
    coverImg: 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400',
    categoryId: 3,
    categoryName: '智能穿戴',
    stock: 80,
    sales: 2340,
    status: 1,
    createTime: '2024-01-08 11:00:00',
  },
  {
    id: 4,
    name: 'AirPods Pro 2',
    description: '主动降噪，空间音频，USB-C 充电',
    price: 1899,
    coverImg: 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=400',
    categoryId: 4,
    categoryName: '影音娱乐',
    stock: 120,
    sales: 3560,
    status: 1,
    createTime: '2024-01-05 14:00:00',
  },
  {
    id: 5,
    name: 'Sony A7M4 微单相机',
    description: '3300万像素全画幅，5轴防抖，4K 60P视频',
    price: 15999,
    coverImg: 'https://images.unsplash.com/photo-1606980707986-1b0e1c407b0d?w=400',
    categoryId: 5,
    categoryName: '摄影摄像',
    stock: 25,
    sales: 456,
    status: 1,
    createTime: '2024-01-20 15:00:00',
  },
  {
    id: 6,
    name: 'Dyson V15 吸尘器',
    description: '激光探测，智能感应，强劲吸力',
    price: 4999,
    coverImg: 'https://images.unsplash.com/photo-1558317374-067fb5f30001?w=400',
    categoryId: 6,
    categoryName: '家用电器',
    stock: 60,
    sales: 1890,
    status: 1,
    createTime: '2024-01-18 16:00:00',
  },
  {
    id: 7,
    name: 'iPad Pro 12.9英寸',
    description: 'M2 芯片，Liquid 视网膜 XDR 显示屏',
    price: 8999,
    coverImg: 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400',
    categoryId: 2,
    categoryName: '电脑办公',
    stock: 45,
    sales: 1120,
    status: 1,
    createTime: '2024-01-22 09:30:00',
  },
  {
    id: 8,
    name: 'Samsung Galaxy S24 Ultra',
    description: '200MP 主摄，骁龙 8 Gen 3，5000mAh 电池',
    price: 8999,
    coverImg: 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=400',
    categoryId: 1,
    categoryName: '手机数码',
    stock: 70,
    sales: 980,
    status: 1,
    createTime: '2024-01-25 10:30:00',
  },
  {
    id: 9,
    name: 'Bose QuietComfort 45',
    description: '主动降噪耳机，24小时续航',
    price: 2299,
    coverImg: 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=400',
    categoryId: 4,
    categoryName: '影音娱乐',
    stock: 90,
    sales: 2670,
    status: 1,
    createTime: '2024-01-28 11:30:00',
  },
  {
    id: 10,
    name: 'DJI Mini 4 Pro 无人机',
    description: '4K/60fps 视频，全向避障，34分钟续航',
    price: 4799,
    coverImg: 'https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=400',
    categoryId: 5,
    categoryName: '摄影摄像',
    stock: 35,
    sales: 678,
    status: 1,
    createTime: '2024-02-01 12:00:00',
  },
  {
    id: 11,
    name: 'Xiaomi 小米手环 8',
    description: '1.62英寸 AMOLED 屏幕，16天续航',
    price: 299,
    coverImg: 'https://images.unsplash.com/photo-1557438159-51eec7a6c9e8?w=400',
    categoryId: 3,
    categoryName: '智能穿戴',
    stock: 200,
    sales: 5670,
    status: 1,
    createTime: '2024-02-03 13:00:00',
  },
  {
    id: 12,
    name: 'LG OLED 65英寸电视',
    description: '4K 120Hz，杜比视界，HDMI 2.1',
    price: 12999,
    coverImg: 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=400',
    categoryId: 6,
    categoryName: '家用电器',
    stock: 20,
    sales: 234,
    status: 1,
    createTime: '2024-02-05 14:00:00',
  },
]

// 购物车数据
export const mockCartItems: CartItem[] = [
  {
    id: 1,
    productId: 1,
    productName: 'iPhone 15 Pro Max',
    productCoverImg: 'https://images.unsplash.com/photo-1592286927505-2fd0d113e4e7?w=400',
    price: 9999,
    quantity: 1,
    isSelected: 1,
    stock: 50,
  },
  {
    id: 2,
    productId: 4,
    productName: 'AirPods Pro 2',
    productCoverImg: 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=400',
    price: 1899,
    quantity: 2,
    isSelected: 1,
    stock: 120,
  },
  {
    id: 3,
    productId: 3,
    productName: 'Apple Watch Ultra 2',
    productCoverImg: 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400',
    price: 6499,
    quantity: 1,
    isSelected: 0,
    stock: 80,
  },
]

// 地址数据
export const mockAddresses: Address[] = [
  {
    id: 1,
    receiver: '张三',
    phone: '13800138000',
    province: '广东省',
    city: '深圳市',
    district: '南山区',
    detailAddress: '科技园南区深南大道10000号',
    isDefault: 1,
  },
  {
    id: 2,
    receiver: '李四',
    phone: '13900139000',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detailAddress: '建国路88号SOHO现代城',
    isDefault: 0,
  },
  {
    id: 3,
    receiver: '王五',
    phone: '13700137000',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detailAddress: '陆家嘴环路1000号恒生银行大厦',
    isDefault: 0,
  },
]

// 订单数据
export const mockOrders: Order[] = [
  {
    id: 1,
    orderNo: 'ORD202402080001',
    totalAmount: 13797,
    payAmount: 13797,
    status: OrderStatus.COMPLETED,
    paymentType: 1,
    paymentTime: '2024-02-08 10:35:20',
    createTime: '2024-02-08 10:30:15',
    receiverName: '张三',
    receiverPhone: '13800138000',
    receiverAddress: '广东省深圳市南山区科技园南区深南大道10000号',
    orderItems: [
      {
        id: 1,
        orderId: 1,
        productId: 1,
        productName: 'iPhone 15 Pro Max',
        productImage: 'https://images.unsplash.com/photo-1592286927505-2fd0d113e4e7?w=400',
        price: 9999,
        quantity: 1,
      },
      {
        id: 2,
        orderId: 1,
        productId: 4,
        productName: 'AirPods Pro 2',
        productImage: 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=400',
        price: 1899,
        quantity: 2,
      },
    ],
  },
  {
    id: 2,
    orderNo: 'ORD202402070001',
    totalAmount: 25999,
    payAmount: 25999,
    status: OrderStatus.PAID,
    paymentType: 2,
    paymentTime: '2024-02-07 15:20:30',
    createTime: '2024-02-07 15:18:45',
    receiverName: '张三',
    receiverPhone: '13800138000',
    receiverAddress: '广东省深圳市南山区科技园南区深南大道10000号',
    orderItems: [
      {
        id: 3,
        orderId: 2,
        productId: 2,
        productName: 'MacBook Pro 16英寸',
        productImage: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400',
        price: 25999,
        quantity: 1,
      },
    ],
  },
  {
    id: 3,
    orderNo: 'ORD202402060001',
    totalAmount: 6499,
    payAmount: 6499,
    status: OrderStatus.PENDING,
    createTime: '2024-02-06 09:15:20',
    receiverName: '李四',
    receiverPhone: '13900139000',
    receiverAddress: '北京市北京市朝阳区建国路88号SOHO现代城',
    orderItems: [
      {
        id: 4,
        orderId: 3,
        productId: 3,
        productName: 'Apple Watch Ultra 2',
        productImage: 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=400',
        price: 6499,
        quantity: 1,
      },
    ],
  },
  {
    id: 4,
    orderNo: 'ORD202402050001',
    totalAmount: 15999,
    payAmount: 15999,
    status: OrderStatus.CANCELLED,
    createTime: '2024-02-05 14:30:00',
    receiverName: '张三',
    receiverPhone: '13800138000',
    receiverAddress: '广东省深圳市南山区科技园南区深南大道10000号',
    orderItems: [
      {
        id: 5,
        orderId: 4,
        productId: 5,
        productName: 'Sony A7M4 微单相机',
        productImage: 'https://images.unsplash.com/photo-1606980707986-1b0e1c407b0d?w=400',
        price: 15999,
        quantity: 1,
      },
    ],
  },
  {
    id: 5,
    orderNo: 'ORD202402040001',
    totalAmount: 4999,
    payAmount: 4999,
    status: OrderStatus.COMPLETED,
    paymentType: 1,
    paymentTime: '2024-02-04 11:25:10',
    createTime: '2024-02-04 11:20:30',
    receiverName: '王五',
    receiverPhone: '13700137000',
    receiverAddress: '上海市上海市浦东新区陆家嘴环路1000号恒生银行大厦',
    orderItems: [
      {
        id: 6,
        orderId: 5,
        productId: 6,
        productName: 'Dyson V15 吸尘器',
        productImage: 'https://images.unsplash.com/photo-1558317374-067fb5f30001?w=400',
        price: 4999,
        quantity: 1,
      },
    ],
  },
]

// 根据 ID 获取商品详情
export const getProductById = (id: number): Product | undefined => {
  return mockProducts.find((p) => p.id === id)
}

// 根据订单号获取订单详情
export const getOrderByNo = (orderNo: string): Order | undefined => {
  return mockOrders.find((o) => o.orderNo === orderNo)
}

// 根据分类 ID 筛选商品
export const getProductsByCategory = (categoryId?: number): Product[] => {
  if (!categoryId) return mockProducts
  return mockProducts.filter((p) => p.categoryId === categoryId)
}

// 搜索商品
export const searchProducts = (keyword: string): Product[] => {
  const lowerKeyword = keyword.toLowerCase()
  return mockProducts.filter(
    (p) =>
      p.name.toLowerCase().includes(lowerKeyword) ||
      p.description?.toLowerCase().includes(lowerKeyword),
  )
}
