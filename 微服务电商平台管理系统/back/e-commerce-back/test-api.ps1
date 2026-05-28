# 电商后端API测试脚本 (PowerShell版本)
# 使用方法: .\test-api.ps1

$BaseUrl = "http://localhost:8080/api"
$Session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "电商后端API测试" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 测试商品列表（无需登录）
Write-Host "1. 测试获取商品列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/product/list" -Method Get
    if ($response.code -eq 200) {
        $count = $response.data.Count
        Write-Host "✅ 成功 - 共有 $count 个商品" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2. 测试用户注册
Write-Host "2. 测试用户注册..." -ForegroundColor Yellow
$timestamp = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$testUsername = "testuser_$timestamp"
$registerBody = @{
    username = $testUsername
    password = "123456"
    email = "test@example.com"
    phone = "13800138000"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/user/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody
    
    if ($response.code -eq 200) {
        Write-Host "✅ 成功 - 用户名: $testUsername" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 3. 测试用户登录
Write-Host "3. 测试用户登录..." -ForegroundColor Yellow
$loginBody = @{
    username = $testUsername
    password = "123456"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/user/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -SessionVariable Session
    
    if ($response.code -eq 200) {
        $userId = $response.data.id
        Write-Host "✅ 成功 - 用户ID: $userId" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 4. 测试获取用户信息
Write-Host "4. 测试获取用户信息..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/user/info" `
        -Method Get `
        -WebSession $Session
    
    if ($response.code -eq 200) {
        $username = $response.data.username
        Write-Host "✅ 成功 - 用户名: $username" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 5. 测试添加到购物车
Write-Host "5. 测试添加商品到购物车..." -ForegroundColor Yellow
$addCartBody = @{
    productId = 1
    quantity = 2
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/cart/add" `
        -Method Post `
        -ContentType "application/json" `
        -Body $addCartBody `
        -WebSession $Session
    
    if ($response.code -eq 200) {
        Write-Host "✅ 成功 - 已添加商品ID=1，数量=2" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 6. 测试获取购物车列表
Write-Host "6. 测试获取购物车列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/cart/list" `
        -Method Get `
        -WebSession $Session
    
    if ($response.code -eq 200) {
        $count = $response.data.Count
        Write-Host "✅ 成功 - 购物车中有 $count 个商品" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 7. 测试添加地址
Write-Host "7. 测试添加收货地址..." -ForegroundColor Yellow
$addAddressBody = @{
    receiverName = "张三"
    receiverPhone = "13800138000"
    province = "北京市"
    city = "北京市"
    district = "朝阳区"
    detail = "某某街道123号"
    isDefault = $true
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/user/address/add" `
        -Method Post `
        -ContentType "application/json" `
        -Body $addAddressBody `
        -WebSession $Session
    
    if ($response.code -eq 200) {
        $addressId = $response.data.id
        Write-Host "✅ 成功 - 地址ID: $addressId" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 8. 测试创建订单
Write-Host "8. 测试创建订单..." -ForegroundColor Yellow
if ($addressId) {
    $createOrderBody = @{
        addressId = $addressId
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/order/create" `
            -Method Post `
            -ContentType "application/json" `
            -Body $createOrderBody `
            -WebSession $Session
        
        if ($response.code -eq 200) {
            $orderNo = $response.data
            Write-Host "✅ 成功 - 订单号: $orderNo" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⚠️  跳过 - 没有可用的地址ID" -ForegroundColor Yellow
}
Write-Host ""

# 9. 测试获取订单列表
Write-Host "9. 测试获取订单列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/order/list" `
        -Method Get `
        -WebSession $Session
    
    if ($response.code -eq 200) {
        $count = $response.data.Count
        Write-Host "✅ 成功 - 共有 $count 个订单" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 10. 测试退出登录
Write-Host "10. 测试退出登录..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/user/logout" `
        -Method Post `
        -WebSession $Session
    
    if ($response.code -eq 200) {
        Write-Host "✅ 成功" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 失败 - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 11. 验证退出后无法访问需要登录的接口
Write-Host "11. 验证退出后无法访问用户信息..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/user/info" `
        -Method Get `
        -WebSession $Session
    
    Write-Host "❌ 失败 - 应该返回401，但请求成功了" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✅ 成功 - 正确返回401未授权" -ForegroundColor Green
    } else {
        Write-Host "❌ 失败 - 返回了错误的状态码" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "测试完成！" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
