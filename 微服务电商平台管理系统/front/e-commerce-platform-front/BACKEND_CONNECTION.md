# 后端连接指南

## 🚀 快速解决"后台不能直接进入"问题

### 方法1：使用快速访问页面（推荐）

访问：http://localhost:5173/debug/access

这个页面提供：
- 一键设置测试登录状态
- 直接访问各个页面
- 查看当前数据模式
- 快速切换登录状态

### 方法2：在首页使用开发者按钮

1. 访问首页：http://localhost:5173
2. 点击右下角的蓝色工具按钮 🔧
3. 进入快速访问页面

### 方法3：浏览器控制台设置

1. 按 F12 打开开发者工具
2. 在控制台输入：`setTestToken()`
3. 刷新页面即可访问需要登录的页面

### 方法4：正常登录

1. 访问：http://localhost:5173/login
2. 在 Mock 模式下，任意用户名和密码都可以登录

## 🔧 当前配置状态

- **Mock 模式**：已启用（`USE_MOCK = true`）
- **Dashboard 认证**：已移除（可直接访问）
- **快速访问**：已添加多个入口

## 📋 可直接访问的页面

| 页面 | 地址 | 说明 |
|------|------|------|
| 首页 | `/home` | 无需登录 |
| 商品列表 | `/product/list` | 无需登录 |
| 数据看板 | `/dashboard` | 已移除登录限制 |
| 快速访问 | `/debug/access` | 开发者工具页面 |
| 后端测试 | `/debug/backend` | 连接测试页面 |

## 🛠️ 需要登录的页面

以下页面需要先设置登录状态：

- 购物车：`/cart`
- 订单相关：`/order/*`
- 用户中心：`/user`
- 地址管理：`/user/address`

## 后端 API 接口说明

### 响应格式

所有 API 返回统一格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

### 认证方式

- 登录后获取 JWT Token
- 请求头携带：`Authorization: Bearer <token>`

### 接口列表

#### 用户模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | `/api/user/register` | 用户注册 |
| 用户登录 | POST | `/api/user/login` | 用户登录，返回 token |
| 获取用户信息 | GET | `/api/user/info` | 需要认证 |
| 地址列表 | GET | `/api/user/address/list` | 需要认证 |
| 添加地址 | POST | `/api/user/address/add` | 需要认证 |

#### 商品模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 商品列表 | GET | `/api/product/list` | 获取所有商品 |
| 商品详情 | GET | `/api/product/detail/{id}` | 根据ID获取商品详情 |
| 商品库存 | GET | `/api/product/stock/{id}` | 获取商品库存 |
| 分类商品 | GET | `/api/product/category/{category}` | 根据分类获取商品 |

#### 购物车模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 添加购物车 | POST | `/api/cart/add` | 需要认证 |
| 购物车列表 | GET | `/api/cart/list` | 需要认证 |
| 更新购物车 | POST | `/api/cart/update` | 需要认证 |
| 删除购物车 | DELETE | `/api/cart/remove/{id}` | 需要认证 |

#### 订单模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建订单 | POST | `/api/order/create` | 需要认证 |
| 订单列表 | GET | `/api/order/list` | 需要认证 |
| 订单详情 | GET | `/api/order/{id}` | 需要认证 |
| 取消订单 | POST | `/api/order/cancel/{id}` | 需要认证 |

#### 支付模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建支付 | POST | `/api/pay/create` | 创建支付记录 |
| 支付通知 | POST | `/api/pay/notify` | 支付回调通知 |

## 前后端数据对接

### 1. 登录流程

**前端发送：**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**后端返回：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 2. 商品列表

**前端请求：**
```
GET /api/product/list
```

**后端返回：**
```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "iPhone 14 Pro",
      "price": 7999.00,
      "stock": 100,
      "category": "电子产品",
      "description": "最新款iPhone"
    }
  ]
}
```

### 3. 购物车操作

**添加商品到购物车：**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**更新购物车：**
```json
{
  "id": 1,
  "quantity": 3,
  "isSelected": 1
}
```

## 常见问题

### 1. 跨域问题

前端已配置代理，确保后端允许跨域访问：

```java
@CrossOrigin(origins = "http://localhost:5173")
```

### 2. Token 认证失败

检查：
- Token 是否正确存储在 localStorage
- 请求头是否正确携带 Authorization
- Token 是否过期

### 3. 接口 404

检查：
- 后端服务是否启动
- 接口路径是否正确
- 代理配置是否正确

### 4. 数据格式不匹配

检查：
- 前端发送的数据格式是否符合后端期望
- 后端返回的数据结构是否符合前端解析

## 调试工具

### 1. 后端连接测试页面

访问 `/debug/backend` 页面进行连接测试

### 2. 浏览器开发者工具

- Network 标签查看请求响应
- Console 查看错误信息
- Application 查看 localStorage

### 3. 后端日志

查看后端控制台输出的请求日志

## 部署注意事项

### 开发环境

- 前端：http://localhost:5173
- 后端：http://localhost:8080
- 代理：前端代理 `/api` 到后端

### 生产环境

1. 修改 `src/config/backend.ts` 中的生产环境地址
2. 构建前端：`npm run build`
3. 部署到 Web 服务器
4. 确保后端 API 可访问

## 故障排除

### 连接失败

1. 检查后端服务是否启动
2. 检查端口是否被占用
3. 检查防火墙设置
4. 检查网络连接

### 认证失败

1. 检查 Token 格式
2. 检查 Token 有效期
3. 检查请求头设置
4. 重新登录获取新 Token

### 数据异常

1. 检查数据库连接
2. 检查数据表结构
3. 检查数据初始化
4. 查看后端错误日志

## 联系支持

如果遇到问题，请：

1. 查看浏览器控制台错误
2. 查看后端日志输出
3. 使用调试页面测试连接
4. 检查网络请求详情

---

**提示：** 建议先使用 Mock 模式完成前端开发，再切换到后端模式进行联调测试。