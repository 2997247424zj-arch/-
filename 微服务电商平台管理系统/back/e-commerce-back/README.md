# 电商后端系统

基于Spring Boot 3.2.0的电商后端系统，提供用户管理、商品管理、购物车、订单和支付等功能。

## 🎯 最新更新 (2026-04-23)

### ✅ 已解决的问题

1. **CORS跨域问题** - 完善了CORS配置，添加了日志接口
2. **认证方式简化** - 从JWT改为Session验证，更简单易用
3. **商品数据初始化** - 应用启动时自动初始化20个示例商品（含真实图片）

详细信息请查看 [问题解决总结.md](./问题解决总结.md)

## 📚 文档导航

- **[快速启动指南](./快速启动指南.md)** - 5分钟快速启动应用
- **[前端集成指南](./前端集成指南.md)** - 前端开发者必读
- **[更新说明](./更新说明.md)** - 详细的更新内容和API变化
- **[问题解决总结](./问题解决总结.md)** - 技术实现细节
- **[API测试文档](./API_TEST.md)** - API接口测试说明

## 🚀 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 配置数据库

```sql
CREATE DATABASE `e-commerce-platform` 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml` 中的数据库密码。

### 3. 启动应用

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 4. 验证启动

访问 http://localhost:8080/api/product/list 查看商品列表。

### 5. 运行测试

```bash
# Windows
.\test-api.ps1

# Linux/Mac
./test-api.sh
```

## 🏗️ 技术栈

- **框架**: Spring Boot 3.2.0
- **ORM**: MyBatis Plus 3.5.6
- **数据库**: MySQL 8.0
- **安全**: Spring Security (密码加密)
- **认证**: Session (简单表单验证)
- **构建工具**: Maven

## 📦 主要功能

### 用户模块
- ✅ 用户注册
- ✅ 用户登录/退出
- ✅ 用户信息管理
- ✅ 收货地址管理

### 商品模块
- ✅ 商品列表查询
- ✅ 商品分类查询
- ✅ 商品详情查询
- ✅ 库存查询
- ✅ 自动初始化示例数据

### 购物车模块
- ✅ 添加商品到购物车
- ✅ 查看购物车
- ✅ 更新购物车（数量、选中状态）
- ✅ 删除购物车项

### 订单模块
- ✅ 创建订单
- ✅ 订单列表查询
- ✅ 订单详情查询
- ✅ 取消订单

### 支付模块
- ✅ 创建支付单
- ✅ 支付回调处理

## 🔌 API接口

### 公开接口（无需登录）
```
GET  /api/product/list                    # 获取所有商品
GET  /api/product/list/category/{id}      # 按分类获取商品
GET  /api/product/detail/{id}             # 获取商品详情
POST /api/user/register                   # 用户注册
POST /api/user/login                      # 用户登录
POST /api/log                             # 前端日志
```

### 需要登录的接口
```
POST /api/user/logout                     # 退出登录
GET  /api/user/info                       # 获取用户信息
GET  /api/user/address/list               # 获取地址列表
POST /api/user/address/add                # 添加地址

POST /api/cart/add                        # 添加到购物车
GET  /api/cart/list                       # 获取购物车
POST /api/cart/update                     # 更新购物车
POST /api/cart/delete                     # 删除购物车项

POST /api/order/create                    # 创建订单
GET  /api/order/list                      # 获取订单列表
GET  /api/order/detail/{orderNo}          # 获取订单详情
POST /api/order/cancel                    # 取消订单

POST /api/pay/create                      # 创建支付
POST /api/pay/notify                      # 支付回调
```

## 🔐 认证方式

系统使用 **Session** 进行身份验证：

1. 用户登录成功后，用户信息存入Session
2. 后续请求自动携带Session Cookie
3. 拦截器验证Session中的用户信息

**前端配置要点：**
```javascript
// Fetch API
fetch('http://localhost:8080/api/xxx', {
  credentials: 'include'  // 必须配置
});

// Axios
axios.create({
  withCredentials: true  // 必须配置
});
```

## 📊 数据库表结构

- `user` - 用户表
- `address` - 收货地址表
- `product` - 商品表
- `cart_item` - 购物车表
- `order` - 订单表
- `order_item` - 订单明细表
- `pay_record` - 支付记录表

## 🎨 示例商品数据

系统启动时自动初始化20个商品，包含：

- 📱 电子产品：iPhone、MacBook、iPad等
- 👕 服装：T恤、牛仔裤、运动鞋等
- 🍫 食品：咖啡豆、巧克力、坚果等
- 📚 图书：技术书籍、人文社科等
- 🏠 家居：台灯、抱枕、加湿器等
- 🏃 运动：瑜伽垫、水杯、背包等

所有商品图片来自 Unsplash，高质量免费图片。

## 🧪 测试

### 自动化测试脚本

提供了完整的API测试脚本：

- `test-api.sh` - Linux/Mac版本
- `test-api.ps1` - Windows版本

测试覆盖：
- ✅ 商品查询
- ✅ 用户注册
- ✅ 用户登录
- ✅ 购物车操作
- ✅ 地址管理
- ✅ 订单创建
- ✅ 退出登录

### 手动测试

使用Postman、Insomnia或curl进行测试。

**注意：** 需要登录的接口必须先调用登录接口，并保存Cookie。

## 🔧 配置说明

### application.yml

```yaml
server:
  port: 8080                              # 服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/e-commerce-platform
    username: root
    password: 你的密码                    # 修改这里
    
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true   # 驼峰命名转换
```

### CORS配置

默认允许所有域名访问（开发环境）。

**生产环境建议：**
```java
registry.addMapping("/**")
        .allowedOrigins("https://yourdomain.com")  // 指定具体域名
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowCredentials(true);
```

## 📝 开发建议

1. **IDE推荐**: IntelliJ IDEA Ultimate
2. **数据库工具**: MySQL Workbench / DBeaver
3. **API测试**: Postman / Insomnia
4. **版本控制**: Git

## 🐛 常见问题

### 端口被占用
修改 `application.yml` 中的 `server.port`

### 数据库连接失败
检查MySQL服务是否启动，用户名密码是否正确

### 商品数据未初始化
清空product表后重启应用

### CORS错误
确保前端请求配置了 `credentials: 'include'`

更多问题请查看 [快速启动指南](./快速启动指南.md)

## 📄 许可证

MIT License

## 👥 贡献

欢迎提交Issue和Pull Request！

## 📞 联系方式

如有问题，请查看文档或提交Issue。

---

**祝开发愉快！** 🎉
