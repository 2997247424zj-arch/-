# 组件分类管理说明

本目录按照用户角色对组件进行分类管理，确保清晰的组件层级结构和访问权限控制。

## 目录结构

```
src/components/
├── admin/              # 管理员专用组件
│   ├── AdminLayout.vue
│   └── ...
├── operator/           # 运营人员专用组件
│   └── ...
├── passenger/          # 乘客专用组件
│   ├── PassengerLayout.vue
│   └── ...
├── shared/             # 共享组件（所有角色可用）
│   ├── ModalPrompt.vue
│   ├── NotFound.vue
│   ├── Home.vue
│   ├── Register.vue
│   └── ThemeSettings.vue
└── dashboard/          # 仪表板组件（按角色分类）
    ├── admin/
    ├── operator/
    └── passenger/
```

## 组件分类规则

### 1. 管理员组件 (admin/)
- **用途**: 仅管理员角色可访问的组件
- **权限**: `role === 'admin'`
- **示例**: 
  - `AdminLayout.vue` - 管理员布局
  - `AdminDashboard.vue` - 管理员仪表板

### 2. 运营人员组件 (operator/)
- **用途**: 仅运营人员角色可访问的组件
- **权限**: `role === 'operator'`
- **示例**:
  - `OperatorDashboard.vue` - 运营人员仪表板
  - 运营相关的业务组件

### 3. 乘客组件 (passenger/)
- **用途**: 仅乘客角色可访问的组件
- **权限**: `role === 'passenger'`
- **示例**:
  - `PassengerLayout.vue` - 乘客布局
  - `PassengerDashboard.vue` - 乘客仪表板

### 4. 共享组件 (shared/)
- **用途**: 所有角色都可以使用的通用组件
- **权限**: 无限制
- **示例**:
  - `ModalPrompt.vue` - 模态提示框
  - `NotFound.vue` - 404页面
  - `Home.vue` - 登录页
  - `Register.vue` - 注册页
  - `ThemeSettings.vue` - 主题设置

### 5. 仪表板组件 (dashboard/)
- **用途**: 按角色分类的仪表板相关组件
- **子目录**:
  - `admin/` - 管理员仪表板组件
  - `operator/` - 运营人员仪表板组件
  - `passenger/` - 乘客仪表板组件

## 权限控制机制

### 在组件中使用权限检查

```typescript
import { computed } from 'vue'
import store from '../services/store'

// 检查用户角色
const canAccess = computed(() => {
  return store.hasPermission(['admin']) // 或 ['operator'], ['passenger']
})
```

### 在路由中使用权限守卫

```typescript
router.beforeEach((to, from, next) => {
  const requiredRole = to.meta.role
  if (requiredRole && !store.hasPermission([requiredRole])) {
    next('/unauthorized')
  } else {
    next()
  }
})
```

## 组件迁移计划

### 当前组件位置 → 新位置

1. **管理员组件**
   - `AdminLayout.vue` → `admin/AdminLayout.vue`
   - `dashboard/AdminDashboard.vue` → `dashboard/admin/AdminDashboard.vue`

2. **运营人员组件**
   - `dashboard/OperatorDashboard.vue` → `dashboard/operator/OperatorDashboard.vue`

3. **乘客组件**
   - `layout/PassengerLayout.vue` → `passenger/PassengerLayout.vue`
   - `dashboard/PassengerDashboard.vue` → `dashboard/passenger/PassengerDashboard.vue`

4. **共享组件**
   - `ModalPrompt.vue` → `shared/ModalPrompt.vue`
   - `NotFound.vue` → `shared/NotFound.vue`
   - `Home.vue` → `shared/Home.vue`
   - `Register.vue` → `shared/Register.vue`
   - `ThemeSettings.vue` → `shared/ThemeSettings.vue`

## 维护指南

1. **添加新组件时**:
   - 根据组件用途确定所属角色分类
   - 将组件放置在对应的目录下
   - 更新本README文档

2. **修改组件时**:
   - 确保不破坏权限控制机制
   - 更新相关文档和注释

3. **删除组件时**:
   - 检查是否有其他组件依赖
   - 更新路由配置
   - 更新本README文档

