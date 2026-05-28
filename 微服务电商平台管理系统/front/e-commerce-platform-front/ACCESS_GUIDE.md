# 🚀 访问指南

## ⚠️ 重要：正确的访问地址

前端服务当前运行在：**http://localhost:5173**

## 📱 可访问的页面

### 🔥 推荐测试页面

| 页面名称 | 访问地址 | 说明 |
|---------|---------|------|
| **超级简单测试** | http://localhost:5173/debug/easy | 一键测试所有功能 |
| **快速访问面板** | http://localhost:5173/debug/access | 所有功能入口 |
| **首页** | http://localhost:5173/home | 主页面 |
| **登录页面** | http://localhost:5173/login | 登录界面（Mock 模式任意密码可登录） |

### 📋 所有可用页面

```
首页：          http://localhost:5173/home
数据看板：      http://localhost:5173/dashboard
商品列表：      http://localhost:5173/product/list
购物车：        http://localhost:5173/cart
订单列表：      http://localhost:5173/order/list
个人中心：      http://localhost:5173/user
地址管理：      http://localhost:5173/user/address
登录页面：      http://localhost:5173/login
```

### 🔧 调试页面

```
超级简单测试：  http://localhost:5173/debug/easy
快速访问：      http://localhost:5173/debug/access
后端测试：      http://localhost:5173/debug/backend
登录测试：      http://localhost:5173/debug/simple-login
```

## 🎯 快速开始

### 方式一：使用超级简单测试页面（最推荐）

1. 访问：http://localhost:5173/debug/easy
2. 点击"自动登录"按钮
3. 点击任意页面按钮进行测试

### 方式二：直接访问首页

1. 访问：http://localhost:5173/home
2. 点击右下角的蓝色工具按钮（开发模式下显示）
3. 进入快速访问面板

### 方式三：使用登录页面

1. 访问：http://localhost:5173/login
2. 输入任意用户名和密码（Mock 模式下都会成功）
3. 登录后可以访问所有页面

## ✅ 当前配置状态

- ✅ **Mock 模式已启用** - 使用虚拟数据
- ✅ **路由守卫已禁用** - 可以直接访问所有页面
- ✅ **认证要求已移除** - 不需要登录即可测试
- ✅ **前端服务运行中** - 端口 5173

## 🔍 如果页面无法访问

### 检查清单

1. **确认端口号**
   - 正确：http://localhost:5173
   - 错误：http://localhost:5175（端口号错误）

2. **检查服务状态**
   ```bash
   # 查看是否有 node 进程运行
   Get-Process | Where-Object {$_.ProcessName -like "*node*"}
   ```

3. **重启前端服务**
   ```bash
   # 停止当前服务（Ctrl+C）
   # 重新启动
   npm run dev
   ```

4. **清除浏览器缓存**
   - 按 Ctrl+Shift+Delete
   - 清除缓存和 Cookie
   - 刷新页面

## 💡 测试建议

### 第一次访问

1. 先访问首页：http://localhost:5173/home
2. 确认页面正常显示
3. 然后访问测试页面：http://localhost:5173/debug/easy

### 如果遇到 404

1. 检查 URL 是否正确
2. 确认端口号是 5173
3. 尝试访问首页确认服务正常

### 如果遇到白屏

1. 打开浏览器开发者工具（F12）
2. 查看 Console 标签的错误信息
3. 查看 Network 标签的网络请求

## 🎉 开始测试

**立即访问：http://localhost:5173/debug/easy**

这是最简单的测试入口，提供一键登录和所有页面导航！

---

**提示**：
- 确保使用正确的端口号 5173
- 所有页面都可以直接访问
- Mock 模式下任意用户名密码都可以登录
- 专注于功能测试，不用担心权限问题