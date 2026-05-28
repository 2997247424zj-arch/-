#!/bin/bash

# 电商后端API测试脚本
# 使用方法: bash test-api.sh

BASE_URL="http://localhost:8080/api"
COOKIE_FILE="test-cookies.txt"

echo "=========================================="
echo "电商后端API测试"
echo "=========================================="
echo ""

# 清理之前的cookie文件
rm -f $COOKIE_FILE

# 1. 测试商品列表（无需登录）
echo "1. 测试获取商品列表..."
curl -s -X GET "$BASE_URL/product/list" | jq '.data | length' > /dev/null 2>&1
if [ $? -eq 0 ]; then
    PRODUCT_COUNT=$(curl -s -X GET "$BASE_URL/product/list" | jq '.data | length')
    echo "✅ 成功 - 共有 $PRODUCT_COUNT 个商品"
else
    echo "❌ 失败"
fi
echo ""

# 2. 测试用户注册
echo "2. 测试用户注册..."
TIMESTAMP=$(date +%s)
TEST_USERNAME="testuser_$TIMESTAMP"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/user/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$TEST_USERNAME\",\"password\":\"123456\",\"email\":\"test@example.com\",\"phone\":\"13800138000\"}")

REGISTER_CODE=$(echo $REGISTER_RESPONSE | jq -r '.code')
if [ "$REGISTER_CODE" == "200" ]; then
    echo "✅ 成功 - 用户名: $TEST_USERNAME"
else
    echo "❌ 失败 - $(echo $REGISTER_RESPONSE | jq -r '.message')"
fi
echo ""

# 3. 测试用户登录
echo "3. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/user/login" \
  -H "Content-Type: application/json" \
  -c $COOKIE_FILE \
  -d "{\"username\":\"$TEST_USERNAME\",\"password\":\"123456\"}")

LOGIN_CODE=$(echo $LOGIN_RESPONSE | jq -r '.code')
if [ "$LOGIN_CODE" == "200" ]; then
    USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.data.id')
    echo "✅ 成功 - 用户ID: $USER_ID"
else
    echo "❌ 失败 - $(echo $LOGIN_RESPONSE | jq -r '.message')"
    exit 1
fi
echo ""

# 4. 测试获取用户信息
echo "4. 测试获取用户信息..."
USER_INFO_RESPONSE=$(curl -s -X GET "$BASE_URL/user/info" \
  -b $COOKIE_FILE)

USER_INFO_CODE=$(echo $USER_INFO_RESPONSE | jq -r '.code')
if [ "$USER_INFO_CODE" == "200" ]; then
    USERNAME=$(echo $USER_INFO_RESPONSE | jq -r '.data.username')
    echo "✅ 成功 - 用户名: $USERNAME"
else
    echo "❌ 失败 - $(echo $USER_INFO_RESPONSE | jq -r '.message')"
fi
echo ""

# 5. 测试添加到购物车
echo "5. 测试添加商品到购物车..."
ADD_CART_RESPONSE=$(curl -s -X POST "$BASE_URL/cart/add" \
  -H "Content-Type: application/json" \
  -b $COOKIE_FILE \
  -d '{"productId":1,"quantity":2}')

ADD_CART_CODE=$(echo $ADD_CART_RESPONSE | jq -r '.code')
if [ "$ADD_CART_CODE" == "200" ]; then
    echo "✅ 成功 - 已添加商品ID=1，数量=2"
else
    echo "❌ 失败 - $(echo $ADD_CART_RESPONSE | jq -r '.message')"
fi
echo ""

# 6. 测试获取购物车列表
echo "6. 测试获取购物车列表..."
CART_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/cart/list" \
  -b $COOKIE_FILE)

CART_LIST_CODE=$(echo $CART_LIST_RESPONSE | jq -r '.code')
if [ "$CART_LIST_CODE" == "200" ]; then
    CART_COUNT=$(echo $CART_LIST_RESPONSE | jq '.data | length')
    echo "✅ 成功 - 购物车中有 $CART_COUNT 个商品"
else
    echo "❌ 失败 - $(echo $CART_LIST_RESPONSE | jq -r '.message')"
fi
echo ""

# 7. 测试添加地址
echo "7. 测试添加收货地址..."
ADD_ADDRESS_RESPONSE=$(curl -s -X POST "$BASE_URL/user/address/add" \
  -H "Content-Type: application/json" \
  -b $COOKIE_FILE \
  -d '{"receiverName":"张三","receiverPhone":"13800138000","province":"北京市","city":"北京市","district":"朝阳区","detail":"某某街道123号","isDefault":true}')

ADD_ADDRESS_CODE=$(echo $ADD_ADDRESS_RESPONSE | jq -r '.code')
if [ "$ADD_ADDRESS_CODE" == "200" ]; then
    ADDRESS_ID=$(echo $ADD_ADDRESS_RESPONSE | jq -r '.data.id')
    echo "✅ 成功 - 地址ID: $ADDRESS_ID"
else
    echo "❌ 失败 - $(echo $ADD_ADDRESS_RESPONSE | jq -r '.message')"
fi
echo ""

# 8. 测试创建订单
echo "8. 测试创建订单..."
if [ ! -z "$ADDRESS_ID" ]; then
    CREATE_ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/order/create" \
      -H "Content-Type: application/json" \
      -b $COOKIE_FILE \
      -d "{\"addressId\":$ADDRESS_ID}")

    CREATE_ORDER_CODE=$(echo $CREATE_ORDER_RESPONSE | jq -r '.code')
    if [ "$CREATE_ORDER_CODE" == "200" ]; then
        ORDER_NO=$(echo $CREATE_ORDER_RESPONSE | jq -r '.data')
        echo "✅ 成功 - 订单号: $ORDER_NO"
    else
        echo "❌ 失败 - $(echo $CREATE_ORDER_RESPONSE | jq -r '.message')"
    fi
else
    echo "⚠️  跳过 - 没有可用的地址ID"
fi
echo ""

# 9. 测试获取订单列表
echo "9. 测试获取订单列表..."
ORDER_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/order/list" \
  -b $COOKIE_FILE)

ORDER_LIST_CODE=$(echo $ORDER_LIST_RESPONSE | jq -r '.code')
if [ "$ORDER_LIST_CODE" == "200" ]; then
    ORDER_COUNT=$(echo $ORDER_LIST_RESPONSE | jq '.data | length')
    echo "✅ 成功 - 共有 $ORDER_COUNT 个订单"
else
    echo "❌ 失败 - $(echo $ORDER_LIST_RESPONSE | jq -r '.message')"
fi
echo ""

# 10. 测试退出登录
echo "10. 测试退出登录..."
LOGOUT_RESPONSE=$(curl -s -X POST "$BASE_URL/user/logout" \
  -b $COOKIE_FILE)

LOGOUT_CODE=$(echo $LOGOUT_RESPONSE | jq -r '.code')
if [ "$LOGOUT_CODE" == "200" ]; then
    echo "✅ 成功"
else
    echo "❌ 失败 - $(echo $LOGOUT_RESPONSE | jq -r '.message')"
fi
echo ""

# 11. 验证退出后无法访问需要登录的接口
echo "11. 验证退出后无法访问用户信息..."
VERIFY_RESPONSE=$(curl -s -X GET "$BASE_URL/user/info" \
  -b $COOKIE_FILE)

VERIFY_CODE=$(echo $VERIFY_RESPONSE | jq -r '.code')
if [ "$VERIFY_CODE" == "401" ]; then
    echo "✅ 成功 - 正确返回401未授权"
else
    echo "❌ 失败 - 应该返回401，实际返回: $VERIFY_CODE"
fi
echo ""

# 清理cookie文件
rm -f $COOKIE_FILE

echo "=========================================="
echo "测试完成！"
echo "=========================================="
