# Session 401 问题调试指南

## 问题描述
登录后立即调用 `/api/user/info` 返回 401 错误，Session 未能正确持久化。

## 已完成的修复

### 1. 后端配置修改

#### application.yml - 添加Session配置
```yaml
server:
  servlet:
    session:
      timeout: 30m
      cookie:
        name: JSESSIONID
        http-only: true
        secure: false
        same-site: lax
        path: /
        max-age: 1800
```

#### UserController.java - 添加调试日志
- 登录时打印 Session ID、用户信息
- 获取用户信息时打印 Session ID、是否新Session、userId

#### CorsConfig.java - 添加请求日志
- 打印每个请求的 URI、Method、Origin、Cookie

### 2. 前端配置修改

#### user.ts - 增强登录流程
- 添加详细的 console.log 日志
- 登录成功后等待 100ms 再调用 fetchUserInfo
- fetchUserInfo 失败时抛出错误而不是静默处理

## 调试步骤

### 步骤 1: 重启后端服务
1. 在 IDEA 中停止 Spring Boot 应用
2. 重新启动应用
3. 确保看到 "Started ECommerceBackApplication" 日志

### 步骤 2: 清理前端缓存
```bash
cd front/e-commerce-platform-front
rm -rf node_modules/.vite
npm run dev
```

### 步骤 3: 清理浏览器缓存
1. 打开浏览器开发者工具 (F12)
2. 进入 Application/应用程序 标签
3. 清除所有 Cookies
4. 清除 LocalStorage
5. 刷新页面 (Ctrl+Shift+R 强制刷新)

### 步骤 4: 测试登录流程
1. 打开 http://localhost:5173/login
2. 打开浏览器开发者工具的 Network 标签
3. 输入测试账号: `admin` / `123456`
4. 点击登录按钮

### 步骤 5: 检查网络请求

#### 检查登录请求 (POST /api/user/login)
**Request Headers 应该包含:**
```
Origin: http://localhost:5173
Content-Type: application/json
```

**Response Headers 应该包含:**
```
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
Set-Cookie: JSESSIONID=xxxxx; Path=/; HttpOnly; SameSite=Lax
```

**Response Body:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "admin",
    "role": "admin",
    ...
  }
}
```

#### 检查用户信息请求 (GET /api/user/info)
**Request Headers 应该包含:**
```
Origin: http://localhost:5173
Cookie: JSESSIONID=xxxxx
```

**如果返回 401:**
- 检查 Cookie 是否被发送
- 检查 JSESSIONID 值是否与登录时相同

### 步骤 6: 检查后端日志

**登录成功时应该看到:**
```
=== CORS Filter ===
Request URI: /api/user/login
Request Method: POST
Origin: http://localhost:5173
Cookie Header: null

=== 登录成功 ===
Session ID: 1234567890ABCDEF
User ID: 1
Username: admin
Role: admin
Session MaxInactiveInterval: 1800
```

**获取用户信息时应该看到:**
```
=== CORS Filter ===
Request URI: /api/user/info
Request Method: GET
Origin: http://localhost:5173
Cookie Header: JSESSIONID=1234567890ABCDEF

=== 获取用户信息 ===
Session ID: 1234567890ABCDEF
Session isNew: false
User ID from session: 1
```

### 步骤 7: 检查浏览器控制台

**应该看到以下日志:**
```
=== 开始登录 ===
登录响应: {code: 200, message: "登录成功", data: {...}}
登录成功，用户信息: {id: 1, username: "admin", role: "admin", ...}
=== 获取用户信息 ===
用户信息响应: {code: 200, message: "成功", data: {...}}
用户信息获取成功: {id: 1, username: "admin", role: "admin", ...}
```

## 常见问题排查

### 问题 1: Set-Cookie 响应头不存在
**原因:** CORS 配置问题
**解决:** 检查 CorsConfig.java 是否正确设置了 `Access-Control-Allow-Credentials: true`

### 问题 2: Cookie 没有被发送
**原因:** 前端 axios 配置问题
**解决:** 确认 request.ts 中 `withCredentials: true` 已设置

### 问题 3: Session ID 不一致
**原因:** 每次请求创建新 Session
**解决:** 
- 检查 Cookie 的 SameSite 属性
- 确认前后端在同一域名下（localhost）
- 检查 Cookie 的 Path 是否为 `/`

### 问题 4: Session 立即过期
**原因:** Session 超时配置太短
**解决:** 检查 application.yml 中 `timeout: 30m` 配置

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | admin | 管理员账号，登录后跳转到 /admin |
| test | 123456 | user | 普通用户，登录后跳转到 /home |
| user1 | 123456 | user | 普通用户，登录后跳转到 /home |

## 下一步行动

如果以上步骤都正常，但仍然出现 401 错误：

1. **检查 Spring Boot 版本兼容性**
   - 查看 pom.xml 中的 Spring Boot 版本
   - 确认 Jakarta Servlet API 版本

2. **尝试添加 @CrossOrigin 注解**
   ```java
   @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
   @RestController
   @RequestMapping("/api/user")
   public class UserController {
   ```

3. **检查是否有其他拦截器干扰**
   - 查看 WebConfig.java
   - 确认没有其他 Filter 或 Interceptor 清除 Session

4. **考虑使用 Redis Session**
   - 如果内存 Session 不稳定，可以切换到 Redis
   - 添加依赖: `spring-session-data-redis`

## 成功标志

当看到以下情况时，说明 Session 已正常工作：

✅ 登录请求返回 Set-Cookie 响应头
✅ 后续请求自动携带 Cookie 请求头
✅ 后端日志显示相同的 Session ID
✅ `/api/user/info` 返回 200 和用户信息
✅ 登录后自动跳转到对应页面（admin → /admin, user → /home）
✅ 页面顶部显示用户下拉菜单而不是"登录/注册"按钮
