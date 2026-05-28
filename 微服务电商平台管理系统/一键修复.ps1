# 一键修复脚本
# 自动执行所有必要的修复步骤

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "电商平台一键修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查MySQL是否运行
Write-Host "1. 检查MySQL服务..." -ForegroundColor Yellow
$mysqlRunning = $false
try {
    $mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
    if ($mysqlService -and $mysqlService.Status -eq "Running") {
        Write-Host "✅ MySQL服务正在运行" -ForegroundColor Green
        $mysqlRunning = $true
    } else {
        Write-Host "❌ MySQL服务未运行" -ForegroundColor Red
        Write-Host "   请先启动MySQL服务" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  无法检测MySQL服务状态" -ForegroundColor Yellow
}
Write-Host ""

# 2. 清理数据库
if ($mysqlRunning) {
    Write-Host "2. 清理数据库..." -ForegroundColor Yellow
    $cleanDb = Read-Host "是否清理数据库？这将删除所有用户数据 (y/n)"
    
    if ($cleanDb -eq "y" -or $cleanDb -eq "Y") {
        Write-Host "   请手动执行以下SQL脚本：" -ForegroundColor Yellow
        Write-Host "   文件位置: back/e-commerce-back/清理数据库.sql" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "   或在MySQL中执行：" -ForegroundColor Yellow
        Write-Host "   USE e-commerce-platform;" -ForegroundColor Cyan
        Write-Host "   TRUNCATE TABLE user;" -ForegroundColor Cyan
        Write-Host "   TRUNCATE TABLE cart_item;" -ForegroundColor Cyan
        Write-Host "   TRUNCATE TABLE address;" -ForegroundColor Cyan
        Write-Host ""
        Read-Host "   执行完成后按Enter继续"
        Write-Host "✅ 数据库清理完成" -ForegroundColor Green
    } else {
        Write-Host "⚠️  跳过数据库清理" -ForegroundColor Yellow
    }
} else {
    Write-Host "2. 跳过数据库清理（MySQL未运行）" -ForegroundColor Yellow
}
Write-Host ""

# 3. 检查后端是否运行
Write-Host "3. 检查后端服务..." -ForegroundColor Yellow
$backendRunning = $false
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/product/list" -TimeoutSec 3 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ 后端服务正在运行" -ForegroundColor Green
        $backendRunning = $true
    }
} catch {
    Write-Host "❌ 后端服务未运行" -ForegroundColor Red
    Write-Host "   请在IDEA中重启后端服务" -ForegroundColor Yellow
    Write-Host "   1. 停止当前运行的应用" -ForegroundColor Cyan
    Write-Host "   2. 重新运行 ECommerceBackApplication" -ForegroundColor Cyan
    Write-Host ""
    $continueAnyway = Read-Host "是否继续？(y/n)"
    if ($continueAnyway -ne "y" -and $continueAnyway -ne "Y") {
        exit 1
    }
}
Write-Host ""

# 4. 检查前端目录
Write-Host "4. 检查前端项目..." -ForegroundColor Yellow
if (Test-Path "front/e-commerce-platform-front") {
    Write-Host "✅ 前端项目目录存在" -ForegroundColor Green
    
    # 检查node_modules
    if (Test-Path "front/e-commerce-platform-front/node_modules") {
        Write-Host "✅ 前端依赖已安装" -ForegroundColor Green
    } else {
        Write-Host "⚠️  前端依赖未安装" -ForegroundColor Yellow
        Write-Host "   正在安装依赖..." -ForegroundColor Yellow
        Set-Location "front/e-commerce-platform-front"
        npm install
        Set-Location "../.."
        Write-Host "✅ 依赖安装完成" -ForegroundColor Green
    }
} else {
    Write-Host "❌ 前端项目目录不存在" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 5. 启动前端
Write-Host "5. 启动前端服务..." -ForegroundColor Yellow
$startFrontend = Read-Host "是否启动前端开发服务器？(y/n)"

if ($startFrontend -eq "y" -or $startFrontend -eq "Y") {
    Write-Host "   正在启动前端..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "前端开发服务器启动中..." -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    Set-Location "front/e-commerce-platform-front"
    npm run dev
} else {
    Write-Host "⚠️  跳过前端启动" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "修复完成！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "下一步：" -ForegroundColor Yellow
    Write-Host "1. 确保后端在IDEA中运行" -ForegroundColor Cyan
    Write-Host "2. 手动启动前端：" -ForegroundColor Cyan
    Write-Host "   cd front/e-commerce-platform-front" -ForegroundColor White
    Write-Host "   npm run dev" -ForegroundColor White
    Write-Host ""
    Write-Host "3. 访问 http://localhost:5173" -ForegroundColor Cyan
    Write-Host ""
}
