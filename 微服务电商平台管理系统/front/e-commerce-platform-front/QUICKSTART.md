# 快速启动指南

## 🚀 一键启动

### 1. 启动后端服务

**Windows 用户：**
```bash
# 双击运行
start-backend.bat
```

**Linux/Mac 用户：**
```bash
./start-backend.sh
```

### 2. 启动前端服务

```bash
npm install
npm run dev
```

### 3. 访问应用

- 前端地址：http://localhost:5173
- 后端地址：http://localhost:8080
- 连接测试：http://localhost:5173/debug/backend

## 🔧 模式切换

### Mock 模式（默认）
```typescript
// src/api/useMock.ts
export const USE_MOCK = true
```
- 使用虚拟数据
- 无需启动后端
- 适合前端开发

### 后端模式
```typescript
// src/api/useMock.ts
export const USE_MOCK = false
```
- 连接真实后端
- 需要启动后端服务
- 适合前后端联调

## 📋 功能清单

### ✅ 已完成功能

- **用户系统**：注册、登录、个人信息管理
- **商品系统**：浏览、搜索、筛选、详情
- **购物车**：添加、修改、删除、结算
- **订单系统**：创建、查看、取消、支付
- **地址管理**：增删改查、默认地址
- **数据看板**：销售统计、图表展示

### 🎨 技术特性

- **TypeScript**：完整类型支持
- **响应式设计**：适配各种屏幕
- **组件化**：可复用组件库
- **状态管理**：Pinia 全局状态
- **Mock 系统**：完整虚拟数据
- **错误处理**：友好错误提示

## 🛠️ 开发工具

```bash
npm run dev          # 启动开发服务器
npm run build        # 构建生产版本
npm run type-check   # TypeScript 检查
npm run lint         # 代码检查
```

## 📚 文档链接

- [项目说明](README.md)
- [开发文档](DEVELOPMENT.md)
- [后端连接](BACKEND_CONNECTION.md)
- [API 文档](e-commerce-back/API_TEST.md)

## ❓ 常见问题

### 后端启动失败
1. 检查 Java 版本（需要 17+）
2. 检查 Maven 安装
3. 检查端口 8080 是否被占用

### 前端启动失败
1. 检查 Node.js 版本（需要 18+）
2. 删除 node_modules 重新安装
3. 检查端口 5173 是否被占用

### 连接失败
1. 确保后端服务已启动
2. 检查 Mock 模式设置
3. 使用调试页面测试连接

## 🎯 下一步

1. 启动后端服务
2. 关闭 Mock 模式
3. 测试前后端连接
4. 开始业务开发

---

**提示**：建议先使用 Mock 模式熟悉功能，再切换到后端模式进行联调。