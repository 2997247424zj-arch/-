# 🎯 从这里开始

## ⚠️ 重要提示

你之前访问的地址是错误的！

❌ **错误地址**：http://localhost:5175/debug/easy  
✅ **正确地址**：http://localhost:5173/debug/easy

## 🚀 立即访问

### 点击下面的链接（复制到浏览器）：

```
http://localhost:5173/debug/easy
```

或者先访问首页：

```
http://localhost:5173/home
```

## 📋 为什么会出现这个问题？

1. 前端服务实际运行在端口 **5173**
2. 你访问的是端口 **5175**（不存在）
3. 所以显示"无法访问此页面"

## ✅ 解决方案

### 方法一：直接访问正确地址

在浏览器地址栏输入：
```
http://localhost:5173/debug/easy
```

### 方法二：从首页开始

1. 访问：http://localhost:5173/home
2. 页面右下角有蓝色工具按钮
3. 点击进入快速访问面板

### 方法三：访问登录页面

1. 访问：http://localhost:5173/login
2. 输入任意用户名密码（如：demo / demo）
3. 点击登录即可

## 🎉 测试页面功能

访问 http://localhost:5173/debug/easy 后，你会看到：

- 🔐 **自动登录按钮** - 一键设置登录状态
- ✅ **设置测试 Token** - 手动设置 Token
- 🗺️ **页面导航按钮** - 快速跳转到任意页面
- 📊 **状态显示** - 查看当前配置

## 💡 快速测试流程

1. 访问 http://localhost:5173/debug/easy
2. 点击"自动登录"
3. 点击任意页面按钮（如"首页"、"商品列表"等）
4. 开始测试功能

## 🔍 如果还是无法访问

### 检查服务是否运行

打开命令行，运行：
```bash
npm run dev
```

确保看到类似这样的输出：
```
VITE v8.0.9  ready in XXX ms
➜  Local:   http://localhost:5173/
```

### 检查端口是否正确

确保访问的是 **5173** 端口，不是其他端口！

### 清除浏览器缓存

1. 按 Ctrl+Shift+Delete
2. 选择"缓存的图片和文件"
3. 点击"清除数据"
4. 刷新页面

## 📱 所有可用地址

```
超级简单测试：  http://localhost:5173/debug/easy
快速访问面板：  http://localhost:5173/debug/access
首页：          http://localhost:5173/home
登录页面：      http://localhost:5173/login
商品列表：      http://localhost:5173/product/list
数据看板：      http://localhost:5173/dashboard
```

## 🎯 现在就开始

**复制这个地址到浏览器：**

```
http://localhost:5173/debug/easy
```

然后点击"自动登录"，开始测试所有功能！

---

**记住**：端口号是 **5173**，不是 5175！