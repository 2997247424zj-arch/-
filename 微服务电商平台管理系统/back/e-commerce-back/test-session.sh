#!/bin/bash

# Session 测试脚本
# 用于测试登录和Session持久化

echo "=========================================="
echo "Session 测试脚本"
echo "=========================================="
echo ""

# 测试登录并保存Cookie
echo "1. 测试登录 (admin/123456)..."
LOGIN_RESPONSE=$(curl -i -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:5173" \
  -d '{"username":"admin","password":"123456"}' \
  -c cookies.txt \
  2>/dev/null)

echo "$LOGIN_RESPONSE"
echo ""

# 提取 JSESSIONID
JSESSIONID=$(grep JSESSIONID cookies.txt | awk '{print $7}')
echo "JSESSIONID: $JSESSIONID"
echo ""

# 等待一秒
sleep 1

# 使用保存的Cookie测试获取用户信息
echo "2. 测试获取用户信息 (使用Cookie)..."
USER_INFO_RESPONSE=$(curl -i -X GET http://localhost:8080/api/user/info \
  -H "Origin: http://localhost:5173" \
  -b cookies.txt \
  2>/dev/null)

echo "$USER_INFO_RESPONSE"
echo ""

# 清理临时文件
rm -f cookies.txt

echo "=========================================="
echo "测试完成"
echo "=========================================="
echo ""
echo "检查要点:"
echo "1. 登录响应应该包含 Set-Cookie: JSESSIONID=..."
echo "2. 登录响应 body 应该是 {\"code\":200,...}"
echo "3. 用户信息响应应该是 200 OK"
echo "4. 用户信息响应 body 应该包含用户数据"
echo ""
echo "如果用户信息返回 401，说明 Session 没有正确持久化"
