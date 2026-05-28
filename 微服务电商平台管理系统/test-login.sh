#!/bin/bash

echo "测试后端登录功能..."
echo ""

# 测试用户登录
echo "1. 测试 admin 用户登录（密码: 123456）"
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  -w "\n\n"

echo "2. 测试 testuser 用户登录（密码: 123456）"
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' \
  -w "\n\n"

echo "3. 测试错误密码"
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}' \
  -w "\n\n"

echo "4. 获取商品列表"
curl http://localhost:8080/api/product/list \
  -w "\n\n"
