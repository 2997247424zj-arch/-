# Session 测试脚本 (PowerShell 版本)
# 用于测试登录和Session持久化

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Session 测试脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 测试登录
Write-Host "1. 测试登录 (admin/123456)..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "123456"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/user/login" `
        -Method POST `
        -Headers @{
            "Content-Type" = "application/json"
            "Origin" = "http://localhost:5173"
        } `
        -Body $loginBody `
        -SessionVariable session `
        -UseBasicParsing

    Write-Host "登录响应状态: $($loginResponse.StatusCode)" -ForegroundColor Green
    Write-Host "登录响应内容:" -ForegroundColor Green
    Write-Host $loginResponse.Content
    Write-Host ""

    # 检查 Set-Cookie 头
    $setCookie = $loginResponse.Headers['Set-Cookie']
    if ($setCookie) {
        Write-Host "Set-Cookie 头: $setCookie" -ForegroundColor Green
    } else {
        Write-Host "警告: 没有找到 Set-Cookie 头!" -ForegroundColor Red
    }
    Write-Host ""

    # 等待一秒
    Start-Sleep -Seconds 1

    # 测试获取用户信息
    Write-Host "2. 测试获取用户信息 (使用Session)..." -ForegroundColor Yellow

    $userInfoResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/user/info" `
        -Method GET `
        -Headers @{
            "Origin" = "http://localhost:5173"
        } `
        -WebSession $session `
        -UseBasicParsing

    Write-Host "用户信息响应状态: $($userInfoResponse.StatusCode)" -ForegroundColor Green
    Write-Host "用户信息响应内容:" -ForegroundColor Green
    Write-Host $userInfoResponse.Content
    Write-Host ""

    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "测试成功! Session 正常工作" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green

} catch {
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host "测试失败!" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP 状态码: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 401) {
            Write-Host ""
            Write-Host "Session 未能正确持久化，请检查:" -ForegroundColor Yellow
            Write-Host "1. 后端是否正确设置了 Set-Cookie 响应头" -ForegroundColor Yellow
            Write-Host "2. application.yml 中的 Session 配置是否正确" -ForegroundColor Yellow
            Write-Host "3. CORS 配置是否允许 credentials" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "检查要点:" -ForegroundColor Cyan
Write-Host "1. 登录响应应该包含 Set-Cookie: JSESSIONID=..." -ForegroundColor White
Write-Host "2. 登录响应 body 应该是 {`"code`":200,...}" -ForegroundColor White
Write-Host "3. 用户信息响应应该是 200 OK" -ForegroundColor White
Write-Host "4. 用户信息响应 body 应该包含用户数据" -ForegroundColor White
Write-Host ""
Write-Host "如果用户信息返回 401，说明 Session 没有正确持久化" -ForegroundColor Yellow
