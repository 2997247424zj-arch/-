// 配置变更日志记录工具

interface ConfigChangeLog {
  id: string
  timestamp: number
  userId: string
  userName: string
  configType: string
  configKey: string
  oldValue: any
  newValue: any
  changeReason?: string
}

// 配置变更日志存储
const configLogs: ConfigChangeLog[] = []

// 从localStorage加载日志
const loadLogs = () => {
  try {
    const savedLogs = localStorage.getItem('config_change_logs')
    if (savedLogs) {
      const logs = JSON.parse(savedLogs)
      configLogs.length = 0
      configLogs.push(...logs)
    }
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn('加载配置日志失败:', error)
    }
  }
}

// 保存日志到localStorage
const saveLogs = () => {
  try {
    // 只保留最近1000条日志
    const logsToSave = configLogs.slice(-1000)
    localStorage.setItem('config_change_logs', JSON.stringify(logsToSave))
  } catch (error) {
    if (import.meta.env.DEV) {
      console.warn('保存配置日志失败:', error)
    }
  }
}

// 初始化时加载日志
loadLogs()

// 记录配置变更
export const logConfigChange = (
  configType: string,
  configKey: string,
  oldValue: any,
  newValue: any,
  changeReason?: string
) => {
  try {
    // 获取当前用户信息
    const userInfoStr = sessionStorage.getItem('user_info')
    const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null
    
    const log: ConfigChangeLog = {
      id: `log_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      timestamp: Date.now(),
      userId: userInfo?.id || 'anonymous',
      userName: userInfo?.username || userInfo?.realName || '匿名用户',
      configType,
      configKey,
      oldValue,
      newValue,
      changeReason
    }
    
    configLogs.push(log)
    saveLogs()
    
    // 触发日志变更事件
    window.dispatchEvent(new CustomEvent('config-log-changed', { detail: log }))
    
    return log
  } catch (error) {
    if (import.meta.env.DEV) {
      console.error('记录配置变更日志失败:', error)
    }
    return null
  }
}

// 获取配置变更日志
export const getConfigLogs = (params?: {
  configType?: string
  configKey?: string
  startTime?: number
  endTime?: number
  limit?: number
}): ConfigChangeLog[] => {
  let filteredLogs = [...configLogs]
  
  if (params?.configType) {
    filteredLogs = filteredLogs.filter(log => log.configType === params.configType)
  }
  
  if (params?.configKey) {
    filteredLogs = filteredLogs.filter(log => log.configKey === params.configKey)
  }
  
  if (params?.startTime) {
    filteredLogs = filteredLogs.filter(log => log.timestamp >= params.startTime!)
  }
  
  if (params?.endTime) {
    filteredLogs = filteredLogs.filter(log => log.timestamp <= params.endTime!)
  }
  
  // 按时间倒序排列
  filteredLogs.sort((a, b) => b.timestamp - a.timestamp)
  
  // 限制返回数量
  if (params?.limit) {
    filteredLogs = filteredLogs.slice(0, params.limit)
  }
  
  return filteredLogs
}

// 清除日志
export const clearConfigLogs = (beforeTimestamp?: number) => {
  if (beforeTimestamp) {
    const index = configLogs.findIndex(log => log.timestamp < beforeTimestamp)
    if (index > -1) {
      configLogs.splice(0, index + 1)
    }
  } else {
    configLogs.length = 0
  }
  saveLogs()
}

// 导出日志为JSON
export const exportConfigLogs = (): string => {
  return JSON.stringify(configLogs, null, 2)
}

