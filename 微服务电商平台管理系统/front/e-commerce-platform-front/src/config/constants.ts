// 全局常量配置

// 订单状态
export const ORDER_STATUS = {
  PENDING: 0, // 待支付
  PAID: 1, // 已支付
  CANCELLED: 2, // 已取消
  COMPLETED: 3, // 已完成
} as const

// 订单状态文本映射
export const ORDER_STATUS_TEXT: Record<number, string> = {
  [ORDER_STATUS.PENDING]: '待支付',
  [ORDER_STATUS.PAID]: '已支付',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.COMPLETED]: '已完成',
}

// 订单状态颜色映射
export const ORDER_STATUS_COLOR: Record<number, string> = {
  [ORDER_STATUS.PENDING]: 'warning',
  [ORDER_STATUS.PAID]: 'primary',
  [ORDER_STATUS.CANCELLED]: 'info',
  [ORDER_STATUS.COMPLETED]: 'success',
}

// 支付方式
export const PAY_TYPE = {
  ALIPAY: 1, // 支付宝
  WECHAT: 2, // 微信
  BANK_CARD: 3, // 银行卡
} as const

// 支付方式文本映射
export const PAY_TYPE_TEXT: Record<number, string> = {
  [PAY_TYPE.ALIPAY]: '支付宝',
  [PAY_TYPE.WECHAT]: '微信支付',
  [PAY_TYPE.BANK_CARD]: '银行卡',
}

// 分页配置
export const PAGINATION = {
  PAGE_SIZE: 12,
  PAGE_SIZES: [12, 24, 36, 48],
} as const

// 图片上传配置
export const UPLOAD = {
  MAX_SIZE: 2 * 1024 * 1024, // 2MB
  ACCEPT_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
  ACCEPT_EXTENSIONS: ['.jpg', '.jpeg', '.png', '.gif', '.webp'],
} as const

// 表单验证规则
export const VALIDATION = {
  PHONE_PATTERN: /^1[3-9]\d{9}$/,
  EMAIL_PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PASSWORD_MIN_LENGTH: 6,
  PASSWORD_MAX_LENGTH: 20,
  USERNAME_MIN_LENGTH: 2,
  USERNAME_MAX_LENGTH: 20,
} as const

// 本地存储键名
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
  CART_COUNT: 'cartCount',
  THEME: 'theme',
  LANGUAGE: 'language',
} as const

// API 响应码
export const API_CODE = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500,
} as const

// 商品排序方式
export const PRODUCT_SORT = {
  DEFAULT: '',
  PRICE_ASC: 'price_asc',
  PRICE_DESC: 'price_desc',
  SALES: 'sales',
  NEW: 'new',
} as const

// 商品排序文本映射
export const PRODUCT_SORT_TEXT: Record<string, string> = {
  [PRODUCT_SORT.DEFAULT]: '默认排序',
  [PRODUCT_SORT.PRICE_ASC]: '价格升序',
  [PRODUCT_SORT.PRICE_DESC]: '价格降序',
  [PRODUCT_SORT.SALES]: '销量优先',
  [PRODUCT_SORT.NEW]: '最新上架',
}

// 默认头像
export const DEFAULT_AVATAR =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 默认商品图片
export const DEFAULT_PRODUCT_IMAGE = 'https://via.placeholder.com/400x400?text=No+Image'

// 时间格式
export const DATE_FORMAT = {
  FULL: 'YYYY-MM-DD HH:mm:ss',
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm:ss',
  MONTH: 'YYYY-MM',
  YEAR: 'YYYY',
} as const

// 响应式断点
export const BREAKPOINTS = {
  XS: 480,
  SM: 768,
  MD: 992,
  LG: 1200,
  XL: 1920,
} as const
