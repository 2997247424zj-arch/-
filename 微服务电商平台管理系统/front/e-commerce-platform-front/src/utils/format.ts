// 格式化工具函数

/**
 * 格式化价格
 * @param price 价格
 * @param decimals 小数位数
 * @returns 格式化后的价格字符串
 */
export const formatPrice = (price: number | string, decimals: number = 2): string => {
  const num = typeof price === 'string' ? parseFloat(price) : price
  return num.toFixed(decimals)
}

/**
 * 格式化数字（千分位）
 * @param num 数字
 * @returns 格式化后的字符串
 */
export const formatNumber = (num: number | string): string => {
  const n = typeof num === 'string' ? parseFloat(num) : num
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化日期时间
 * @param date 日期
 * @param format 格式
 * @returns 格式化后的日期字符串
 */
export const formatDate = (
  date: Date | string | number,
  format: string = 'YYYY-MM-DD HH:mm:ss',
): string => {
  const d = typeof date === 'string' || typeof date === 'number' ? new Date(date) : date

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化相对时间
 * @param date 日期
 * @returns 相对时间字符串
 */
export const formatRelativeTime = (date: Date | string | number): string => {
  const d = typeof date === 'string' || typeof date === 'number' ? new Date(date) : date
  const now = new Date()
  const diff = now.getTime() - d.getTime()

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  if (months < 12) return `${months}个月前`
  return `${years}年前`
}

/**
 * 隐藏手机号中间4位
 * @param phone 手机号
 * @returns 隐藏后的手机号
 */
export const hidePhone = (phone: string): string => {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 隐藏邮箱部分字符
 * @param email 邮箱
 * @returns 隐藏后的邮箱
 */
export const hideEmail = (email: string): string => {
  if (!email || !email.includes('@')) return email
  const parts = email.split('@')
  const name = parts[0]
  const domain = parts[1]
  if (!name || !domain) return email
  const visibleLength = Math.min(3, name.length)
  const hiddenName = name.substring(0, visibleLength) + '***'
  return `${hiddenName}@${domain}`
}

/**
 * 文件大小格式化
 * @param bytes 字节数
 * @returns 格式化后的文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 截断文本
 * @param text 文本
 * @param length 长度
 * @param suffix 后缀
 * @returns 截断后的文本
 */
export const truncate = (text: string, length: number, suffix: string = '...'): string => {
  if (text.length <= length) return text
  return text.substring(0, length) + suffix
}

/**
 * 生成随机字符串
 * @param length 长度
 * @returns 随机字符串
 */
export const randomString = (length: number = 8): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 防抖函数
 * @param fn 函数
 * @param delay 延迟时间
 * @returns 防抖后的函数
 */
export const debounce = <T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300,
): ((...args: Parameters<T>) => void) => {
  let timer: ReturnType<typeof setTimeout> | null = null
  return function (this: any, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param fn 函数
 * @param delay 延迟时间
 * @returns 节流后的函数
 */
export const throttle = <T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300,
): ((...args: Parameters<T>) => void) => {
  let lastTime = 0
  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      fn.apply(this, args)
      lastTime = now
    }
  }
}
