# 前端开发服务器重启脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "前端开发服务器重启脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查后端是否运行
Write-Host "1. 检查后端服务..." -ForegroundColor Yellow
$backendRunning = $false
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/product/list" -TimeoutSec 3 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ 后端服务正常运行在 http://localhost:8080" -ForegroundColor Green
        $backendRunning = $true
    }
} catch {
    Write-Host "❌ 后端服务未运行或无法访问" -ForegroundColor Red
    Write-Host "   请先在IDEA中启动后端服务" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "按Enter键退出"
    exit 1
}
Write-Host ""

# 2. 检查Node.js版本
Write-Host "2. 检查Node.js版本..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js版本: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js未安装" -ForegroundColor Red
    Write-Host "   请先安装Node.js: https://nodejs.org/" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "按Enter键退出"
    exit 1
}
Write-Host ""

# 3. 检查依赖是否安装
Write-Host "3. 检查依赖..." -ForegroundColor Yellow
if (-not (Test-Path "node_modules")) {
    Write-Host "⚠️  依赖未安装，开始安装..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 依赖安装失败" -ForegroundColor Red
        Read-Host "按Enter键退出"
        exit 1
    }
    Write-Host "✅ 依赖安装完成" -ForegroundColor Green
} else {
    Write-Host "✅ 依赖已安装" -ForegroundColor Green
}
Write-Host ""

# 4. 杀死占用5173端口的进程
Write-Host "4. 检查端口占用..." -ForegroundColor Yellow
$port5173 = netstat -ano | findstr :5173 | findstr LISTENING
if ($port5173) {
    Write-Host "⚠️  端口5173被占用，尝试释放..." -ForegroundColor Yellow
    $processId = ($port5173 -split '\s+')[-1]
    try {
        Stop-Process -Id $processId -Force
        Start-Sleep -Seconds 2
        Write-Host "✅ 端口已释放" -ForegroundColor Green
    } catch {
        Write-Host "❌ 无法释放端口，请手动关闭占用进程" -ForegroundColor Red
    }
} else {
    Write-Host "✅ 端口5173可用" -ForegroundColor Green
}
Write-Host ""

# 5. 启动开发服务器
Write-Host "5. 启动开发服务器..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

npm run dev
