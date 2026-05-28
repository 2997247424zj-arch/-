/**
 * 时间格式化工具函数
 * 直接显示后端返回的北京时间，不进行任何时区转换
 * 后端已配置为返回北京时间格式（yyyy-MM-dd HH:mm:ss），前端直接使用
 */

/**
 * 格式化日期时间字符串 - 直接使用后端返回的北京时间，不进行时区转换
 * 输入格式支持：YYYY-MM-DD HH:mm:ss, YYYY-MM-DDTHH:mm:ss
 * 输出格式：YYYY-MM-DD HH:mm（北京时间）
 */
export function formatDateTime(dateTime: string | Date | null | undefined): string {
  if (!dateTime) return ''
  
  // 如果是字符串，直接从字符串中提取日期和时间，不进行时区转换
  if (typeof dateTime === 'string') {
    let dateStr = dateTime.trim()
    
    // 移除时区信息（Z, +08:00, -05:00等），只保留日期和时间部分
    // 移除末尾的Z
    if (dateStr.endsWith('Z')) {
      dateStr = dateStr.slice(0, -1)
    }
    // 移除时区偏移（+08:00, -05:00等）
    const timezoneMatch = dateStr.match(/([+-]\d{2}:\d{2})$/);
    if (timezoneMatch) {
      dateStr = dateStr.slice(0, -timezoneMatch[0].length)
    }
    
    // 将T替换为空格，统一格式
    dateStr = dateStr.replace('T', ' ')
    
    // 提取日期和时间部分（格式：YYYY-MM-DD HH:mm:ss 或 YYYY-MM-DD HH:mm）
    const match = dateStr.match(/^(\d{4}-\d{2}-\d{2})\s+(\d{2}:\d{2})(?::\d{2})?/)
    if (match) {
      const [, datePart, timePart] = match
      // 只返回日期和时间，不包含秒
      return `${datePart} ${timePart}`
    }
    
    // 如果格式不匹配，尝试直接返回格式化后的字符串
    return dateStr
  } else if (dateTime instanceof Date) {
    // 如果是Date对象，直接格式化（这种情况较少，因为后端通常返回字符串）
    // 但即使是这样，我们也只做简单的格式化，不进行时区转换
    const year = dateTime.getFullYear()
    const month = String(dateTime.getMonth() + 1).padStart(2, '0')
    const day = String(dateTime.getDate()).padStart(2, '0')
    const hour = String(dateTime.getHours()).padStart(2, '0')
    const minute = String(dateTime.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
  }
  return ''
}

/**
 * 格式化时间部分 - 直接使用后端返回的北京时间，不进行时区转换
 * 输入格式支持：YYYY-MM-DD HH:mm:ss, YYYY-MM-DDTHH:mm:ss, HH:mm
 * 输出格式：HH:mm（北京时间）
 */
export function formatTime(timeStr: string | null | undefined): string {
  if (!timeStr) return '--:--'
  
  const str = String(timeStr).trim()
  
  // 如果已经是 HH:mm 格式，直接返回
  if (str.match(/^\d{2}:\d{2}$/)) {
    return str
  }
  
  // 从日期时间字符串中提取时间部分，不进行时区转换
  let timePart: string | undefined = ''
  if (str.includes(' ')) {
    const parts = str.split(' ')
    if (parts.length >= 2 && parts[1]) {
      timePart = parts[1].substring(0, 5) // 提取 HH:mm 部分
    }
  } else if (str.includes('T')) {
    const parts = str.split('T')
    if (parts.length >= 2 && parts[1]) {
      // 移除时区信息
      let timeStr = parts[1]
      if (timeStr.includes('Z')) {
        timeStr = timeStr.replace('Z', '')
      }
      const timezoneMatch = timeStr.match(/([+-]\d{2}:\d{2})$/);
      if (timezoneMatch) {
        timeStr = timeStr.slice(0, -timezoneMatch[0].length)
      }
      timePart = timeStr.substring(0, 5) // 提取 HH:mm 部分
    }
  }
  
  if (timePart && timePart.match(/^\d{2}:\d{2}$/)) {
    return timePart
  }
  
  // 如果无法解析，返回原值的前5个字符（可能是时间格式）
  return str.length >= 5 ? str.substring(0, 5) : str
}

/**
 * 格式化日期部分 - 只提取日期，不进行时区转换
 * 输入格式支持：YYYY-MM-DD HH:mm:ss, YYYY-MM-DDTHH:mm:ss
 * 输出格式：YYYY-MM-DD
 */
export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return ''
  
  const str = String(dateStr).trim()
  
  // 如果已经是 YYYY-MM-DD 格式
  if (str.match(/^\d{4}-\d{2}-\d{2}$/)) {
    return str
  }
  
  // 从日期时间字符串中提取日期部分
  if (str.includes(' ')) {
    const parts = str.split(' ')
    return parts[0] || ''
  } else if (str.includes('T')) {
    const parts = str.split('T')
    return parts[0] || ''
  }
  
  // 如果无法解析，返回原值的前10个字符（可能是日期格式）
  return str.length >= 10 ? str.substring(0, 10) : str
}

