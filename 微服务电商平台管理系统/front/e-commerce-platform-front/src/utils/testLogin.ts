// 测试登录工具
export const setTestToken = () => {
  const testToken = 'test-token-' + Date.now()
  localStorage.setItem('token', testToken)
  console.log('已设置测试 Token:', testToken)
}

export const clearTestToken = () => {
  localStorage.removeItem('token')
  console.log('已清除 Token')
}

// 在浏览器控制台中可以使用：
// window.setTestToken() 设置测试 token
// window.clearTestToken() 清除 token
if (typeof window !== 'undefined') {
  ;(window as any).setTestToken = setTestToken
  ;(window as any).clearTestToken = clearTestToken
}
