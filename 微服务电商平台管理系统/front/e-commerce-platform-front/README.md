# E-Commerce Platform

一个完整的电商平台项目，包含前端（Vue 3 + TypeScript）和后端（Spring Boot）。

## 项目结构

```
├── e-commerce-back/        # 后端项目（Spring Boot）
├── src/                    # 前端项目（Vue 3 + TypeScript）
├── start-backend.bat       # Windows 后端启动脚本
├── start-backend.sh        # Linux/Mac 后端启动脚本
├── BACKEND_CONNECTION.md   # 后端连接指南
├── DEVELOPMENT.md          # 前端开发文档
└── README.md              # 项目说明
```

## 快速开始

### 环境要求

- **Node.js**: 18+ 
- **Java**: 17+
- **Maven**: 3.6+

### 1. 启动后端服务

#### 使用启动脚本（推荐）

**Windows:**
```bash
start-backend.bat
```

**Linux/Mac:**
```bash
./start-backend.sh
```

#### 手动启动
```bash
cd e-commerce-back
./mvnw spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 2. 启动前端服务

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 http://localhost:5173 启动

### 3. 配置连接模式

编辑 `src/api/useMock.ts`：

```typescript
// Mock 模式（使用虚拟数据）
export const USE_MOCK = true

// 后端模式（连接真实后端）
export const USE_MOCK = false
```

### 4. 测试连接

访问 http://localhost:5173/debug/backend 进行后端连接测试

## 功能特性

### 前端功能

- ✅ 用户注册/登录
- ✅ 商品浏览/搜索/筛选
- ✅ 购物车管理
- ✅ 订单管理
- ✅ 地址管理
- ✅ 个人信息管理
- ✅ 数据看板
- ✅ 响应式设计

### 后端功能

- ✅ 用户认证（JWT）
- ✅ 商品管理
- ✅ 购物车管理
- ✅ 订单管理
- ✅ 支付管理
- ✅ RESTful API

## 技术栈

### 前端
- Vue 3.5 + TypeScript
- Element Plus UI
- Pinia 状态管理
- Vue Router 路由
- Axios HTTP 客户端
- ECharts 图表
- Vite 构建工具

### 后端
- Spring Boot 3.x
- Spring Security
- MyBatis Plus
- MySQL 数据库
- JWT 认证
- Maven 构建

## 开发模式

### Mock 模式
- 使用虚拟数据进行前端开发
- 无需启动后端服务
- 适合前端独立开发

### 后端模式
- 连接真实后端 API
- 需要启动后端服务
- 适合前后端联调

## 项目文档

- [前端开发文档](DEVELOPMENT.md) - 详细的前端功能说明
- [后端连接指南](BACKEND_CONNECTION.md) - 后端连接配置说明
- [API 测试文档](e-commerce-back/API_TEST.md) - 后端 API 接口文档

## 常用命令

### 前端
```bash
npm run dev          # 启动开发服务器
npm run build        # 构建生产版本
npm run type-check   # TypeScript 类型检查
npm run lint         # 代码检查
```

### 后端
```bash
cd e-commerce-back
./mvnw spring-boot:run    # 启动后端服务
./mvnw clean package      # 构建项目
./mvnw test              # 运行测试
```

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 故障排除

### 前端问题
1. 检查 Node.js 版本是否 18+
2. 清除 node_modules 重新安装依赖
3. 检查端口 5173 是否被占用

### 后端问题
1. 检查 Java 版本是否 17+
2. 检查 Maven 是否正确安装
3. 检查端口 8080 是否被占用
4. 检查数据库连接配置

### 连接问题
1. 确保后端服务已启动
2. 检查 Mock 模式配置
3. 使用调试页面测试连接
4. 查看浏览器网络请求

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License

## 联系方式

如有问题，请创建 Issue 或联系开发团队。