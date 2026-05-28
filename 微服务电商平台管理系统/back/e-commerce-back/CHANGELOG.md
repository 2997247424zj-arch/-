# 更新日志

## [1.1.0] - 2026-04-23

### 🎉 重大更新

#### 认证方式变更
- **移除**: JWT Token认证
- **新增**: Session表单验证
- **影响**: 所有需要认证的接口
- **优势**: 更简单、更易用、前端无需手动管理token

#### CORS配置增强
- **修复**: CORS跨域访问问题
- **新增**: 支持更多HTTP方法（OPTIONS, HEAD, PATCH）
- **新增**: exposedHeaders配置
- **新增**: `/api/log` 日志接口

#### 商品数据自动初始化
- **新增**: `ProductInitService` 自动初始化服务
- **新增**: 20个示例商品数据
- **新增**: 使用Unsplash高质量图片
- **特性**: 应用启动时自动检测并初始化

### ✨ 新增功能

#### 用户模块
- 新增退出登录接口 `POST /api/user/logout`
- 登录接口现在返回完整用户信息（不再返回token）
- 用户信息接口不再需要传递token

#### 日志模块
- 新增前端日志接口 `POST /api/log`
- 支持不同日志级别（info, warn, error）
- 自动记录到后端日志系统

#### 商品模块
- 自动初始化6个分类的商品
- 每个商品包含真实的描述和图片
- 智能检测，避免重复初始化

### 🔧 修改内容

#### 配置文件
- `WebConfig.java`
  - 增强CORS配置
  - 移除JWT拦截器
  - 新增Session认证拦截器
  - 配置拦截器排除路径

#### 服务层
- `UserService.java`
  - `login()` 方法返回类型从 `Result<String>` 改为 `Result<User>`
  - 移除JWT token生成逻辑
  - 登录成功返回用户信息（密码字段置空）

#### 控制器层
- `UserController.java`
  - 所有方法使用 `HttpSession` 替代 `@RequestHeader("Authorization")`
  - 新增 `logout()` 方法
  - 简化用户信息获取逻辑

- `CartController.java`
  - 所有方法使用 `HttpSession` 替代token
  - 统一错误处理

- `OrderController.java`
  - 所有方法使用 `HttpSession` 替代token
  - 统一错误处理

### 📝 新增文件

#### 源代码
- `log/controller/LogController.java` - 日志控制器
- `product/service/ProductInitService.java` - 商品初始化服务

#### 文档
- `README.md` - 项目说明文档
- `CHANGELOG.md` - 更新日志（本文件）
- `快速启动指南.md` - 快速启动说明
- `前端集成指南.md` - 前端开发者指南
- `更新说明.md` - 详细更新说明
- `问题解决总结.md` - 技术实现细节

#### 测试脚本
- `test-api.sh` - Linux/Mac测试脚本
- `test-api.ps1` - Windows PowerShell测试脚本

### 🗑️ 移除内容

- 移除所有Controller中的JWT token验证代码
- 移除 `@RequestHeader("Authorization")` 注解
- 移除 `JwtUtil.getUserIdFromToken()` 调用

### 🔄 API变化

#### 登录接口

**之前:**
```json
POST /api/user/login
Request: { "username": "test", "password": "123456" }
Response: {
  "code": 200,
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "成功"
}
```

**现在:**
```json
POST /api/user/login
Request: { "username": "test", "password": "123456" }
Response: {
  "code": 200,
  "data": {
    "id": 1,
    "username": "test",
    "email": "test@example.com",
    "phone": "13800138000"
  },
  "message": "成功"
}
```

#### 其他接口

**之前:** 需要在Header中传递 `Authorization: Bearer <token>`

**现在:** 自动使用Session，无需手动传递认证信息

### 📦 依赖变化

无依赖变化，所有依赖保持不变。

### ⚠️ 破坏性变更

#### 前端需要修改

1. **登录逻辑**
   ```javascript
   // 之前
   const { data } = await api.post('/user/login', { username, password });
   localStorage.setItem('token', data.data);
   
   // 现在
   const { data } = await api.post('/user/login', { username, password });
   localStorage.setItem('user', JSON.stringify(data.data));
   ```

2. **请求配置**
   ```javascript
   // 之前
   api.get('/cart/list', {
     headers: { Authorization: `Bearer ${token}` }
   });
   
   // 现在
   api.get('/cart/list'); // Session自动携带
   ```

3. **全局配置**
   ```javascript
   // 必须添加
   axios.create({
     withCredentials: true  // 关键配置
   });
   
   // 或
   fetch(url, {
     credentials: 'include'  // 关键配置
   });
   ```

### 🐛 Bug修复

- 修复CORS跨域访问问题
- 修复前端日志接口404错误
- 修复Session认证拦截器配置问题

### 📈 性能优化

- 移除JWT token验证的性能开销
- 使用Spring内置Session管理，性能更优

### 🔒 安全性

- 密码加密使用BCrypt（保持不变）
- Session自动管理，减少token泄露风险
- CORS配置更加完善

### 📚 文档改进

- 新增完整的API文档
- 新增前端集成指南
- 新增快速启动指南
- 新增测试脚本和说明

### 🧪 测试

- ✅ 所有接口编译通过
- ✅ 提供自动化测试脚本
- ✅ 覆盖主要业务流程

### 🚀 部署建议

#### 开发环境
直接启动即可，无需额外配置。

#### 生产环境
1. 修改CORS配置，指定具体域名
2. 配置Session持久化（推荐使用Redis）
3. 启用HTTPS
4. 配置Session超时时间

```yaml
server:
  servlet:
    session:
      timeout: 30m
      cookie:
        secure: true
        http-only: true
```

### 📞 迁移指南

详细的迁移指南请参考：
- [前端集成指南.md](./前端集成指南.md)
- [更新说明.md](./更新说明.md)

### 🙏 致谢

感谢所有贡献者和使用者的支持！

---

## [1.0.0] - 2026-04-20

### 初始版本

- 用户管理模块
- 商品管理模块
- 购物车模块
- 订单模块
- 支付模块
- JWT认证
