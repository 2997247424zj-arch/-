package com.example.airplanesalehouduan.pay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.stereotype.Component;

@Component
public class PayUtil {
    // appid
    private final String APP_ID = "9021000158665768";
    // 应用私钥
    private final String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQChkKuxqT76TBD6033iWeMxLVV1v7aQTKiWKBiKi0jx9/pB49HoqK/cpDBiR7uvAWbzaVIIIKwxnkjQMLZ4ux8Il5kCB8u05m12jiFIS8RIvWSovwFX8/HwPaV8ZRzUHvTZsjm6iaJrnwYlhiEYMafQkGa7MvmTrU8Wm0bduuts7odXN8oTc6op9q6HEihZD4XlpX7AUwD8RuZ00+bjvH8fb5cA/MQNnQmuJ9ZzHP4CDoEiSZRQ9x3cMZCbVZ9R1R498x9WVrUEEvwgLqmNIuNf3G+3MKERyavOIFe5bJpbiiU6ivPMT/1QTy5covrBLfuLRak4nknh65edl2pXcxY9AgMBAAECggEAVSeZNwSLSwU4ldtOqDTUSCEqv+sR4DBnUu1DMhKDmfUnk/7trgDq4BIx5jsEsxK8HmNOdzYxS92vZvul0TBfBPpSDP3q4ifpXek/pYk5pVUdbstte9v5krQLOutYLH08dFk5kBXm425noPN3EfdqCcVyQAx3+twHqVSiH0/bsgeYH9XzTiZR7yd0LG3JzZ8q5Z2BBl/oCCWs5QjjSkVCSgJX1b/TRlHmY3zVVf4iJHxzTuTmA1OapQaJVSvjYpOQayKidjj3nWi5+raFK6lmCfKTJKH/OaTan98TVG0GPFVlYwScUTRt7kFAFhy2OW8p16eTXNvc2t0VQmq4RsV0AQKBgQDQUvFW0qD/sWndIS4KkBKxSROn9o8gxZbN57DlDzr24As0Sr7QphW7zPUnDBicsalAg06fWj1EZc/OG6TfI/SoqvHCEvxBjjUX+Bsq4CyWWw05KcFc8XA6Zqoa0lZGASYLud3vglEOxTm5nloruMHL1u5cngQXcwRb3Pkn1BdSgQKBgQDGikU2I6ATcq9UbhphQvwV5ikc8bCtecbe07zzm1ELJp52IPOydz8c/BO2/ETm0OcKQb656LyAvrE4gM7STcDGsVqkt1gJQRD9MiqmA2clo+Fc1IPbDVTcX31UOOPiLgtyt2aR9tX7yiFPwD/41NR0EyKr9D1sxPLS3zJ+rnqtvQKBgQCBSPg9ybng9VmdwlmwdHklTRTFyF5LGFTetEsz3J/bVm+dpEWHd4tFYY1DDl634AoJ33OLsZUUPZsZbk6DrOzJQE2JA/9JL9gDaBZ0JWuGZDtwnllCNRRRKiWnP6ILD+tsm40Adv+XCw8oAv4y5BZhSqHjGK2xX8/0ss5tyVIwAQKBgBNHggkL1JOO42+zcWRYeNo3i26AGcP+u00QCp0tS+VlTqXx9TgzVDGgWNnADYDMcGMSl06+Ru/JlLQD7hGF815YApFULHxZkSjFIlcUpYOGMnAtQtCWrtLngugWzio8hmgRXyh1a7oTmsB/zrZ/FFJhOVCxWvWg+mN3wROEJe2RAoGBAJsEw/jgTu8qzp6Jb09MBeRPd26NHdEPae960cMwIOY+261hxTt5/w0dtFghj5JIkEaah5V6vxndellXo8ifwO6OBOGLZdwPo6AspycXU0GCajSqrmipkGDVdXJ9Upwsoi4NvKMJ01mY4ISLWlADlPnYKq6gk1erZ+NerleyNTKD";
    private final String CHARSET = "UTF-8";
    // 支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArz4myMLjGInJDpjJwdsaF7WDNcLG5OD5ji/jLhZAzT9hL5ZfflvaFCWW7qzmVy4CM71uVgKTPKmaR5X3yV74RF0ULYdO5cUOctwthTHfrP6miYOr2Mf8dO2M4NSaPrMCme6waQh3NnNXYyrrM3xYVuQb8WqYrMlK9vSE5UmNNrgqZWzBiggIv5E6i0/ZEu9n+rTMzyo/l0ANomrXTsRB97nOWZEvTfSt7rvoVIwCeQ3VUlR4EJ6beojRFrbQigeu/ijENh3KtVKXMcL1vS4YQV7ADmTmiMydmKcLULEdSmxQu+pYyVCbGbLM3avFrnmeqJNr150EujnTA2m921bElQIDAQAB";
    // 这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    // 签名方式
    private final String SIGN_TYPE = "RSA2";
    // 支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址
    private final String NOTIFY_URL = "http://4e9f57bde6414891.natapp.cc/api/alipay/toSuccess";
    // 支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    private final String RETURN_URL = "http://localhost:8080/api/alipay/toSuccess";
    private AlipayClient alipayClient = null;

    // 支付宝官方提供的接口
    public String sendRequestToAlipay(String outTradeNo, Float totalAmount, String subject) throws AlipayApiException {
        // 获得初始化的AlipayClient
        alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);

        // 设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);

        // 商品描述（可空）
        String body = "";
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        // 请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        System.out.println("返回的结果是：" + result);
        return result;
    }

    // 通过订单编号查询
    public String query(String id) {
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
        }

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", id);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body = null;
        try {
            response = alipayClient.execute(request);
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response != null && response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return body;
    }
}