package com.junting.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.junting.gulimall.order.vo.PayVo;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000119600394";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCW2EzUUQZ0bjTp5B3RYtOnZdAB6Xl1UeLfpUhr8cpOuxJ7r25yimIshBC8Ooik3x/2Kwo+h0uSihy7dpNZ8I8X3ue7yATIaFrTgQl4ycVGg2G7SAyuZ4d8R3SxAkTImtSTcLrHrftceSrLWYTMnFl2lhzWCZ6SkwMgilhxUZdvLOauTtn0hL+ah6YzudJMKZmSzzm/2spP6lU1bYfH33yazKKDSJHLo54SAutFmMutCLIRRAUNR5Cz8DQvZFoG1md2eiyTkF38R4P8XaQqGkMr7cqBxUbfnYDLmTu83jmYUF5N4eJRr1YyOk3VlqHi92wlPDJcI2lnRwRu6vwhJ2pNAgMBAAECggEAekWciWGgTNJkW87ICMkF7aQOpu4cwOd+cnCksrMMnSLwiYebjIb1q0Xl1t3Pk201mDItHKBItuDzB1XHfVkMdHn/92vbzKMiNF1dUyainVSz7L4rYVeUMD4vrae8H6u5ckYncOJrZPrmoGzQw5Zcwk5N04V6ny9BkqfxpvFPQhKwWQt/LXsSzKY67CXDBTo+oFuBslhn+SJJEvF9rzPn+wzQIxMDlh8ozOfacllh46eFRHZSOOUQkLFKcql+iCRA96C0nPIn0oLaqXqXJLIt/8doUOXdKTKdriflMwE+s/pTtVfmwBEGQ9n3QxhXVAg6YV4Qu8xxEHBwRJDiCkWGgQKBgQDYjRti3yOVhgQU15ImO1lebA114KTYzE9iR0n2EyZ3HB8081COfVdmJG/mYPw4pfQZa0sNJlQ8a6GmEFnii0uTPr8VHeTovGko/KHmnZHl3BwFxF9OjxEQN06okl1ExNDW/DMI8JAFKoPzpjyWJG8TMhEHTVFBJhxBQ4AsVAkFoQKBgQCyUvjzpS/kvaV7MNQtX8RGm6uN14nSTH0zQg3rTqOPwdJXNSJ4UMGzjLWKb7GZNw+yG3KIu0KQ33rP7ggpjSgj2jINbGAAzf4Cu4JuCLPRpcifIEOf6OIbmOLIC6LISHSMLtuhtxguy+hIDP6AhD+2o1qneIZjgTp8+xO6uOBNLQKBgElkEUsB+upbABkcMjD43kHs9ubjWpsLK0BQjtVyChBGHKFycPPJsQwx/yah6fk87SyMO2RagCP2ClPgqMXplZRyqIn8lq714+H7NNeWwXKYlXqy4eYbAJpxVBw3dpqu5WRnFtjJxtWpYgz6YwAzmt1zk2fZIVemFd9chB0B4gjhAoGAQNlNsYUcAqo4+LDMKGgQP8EyxRViCHlKeo6F81GYx5mrvY0UKP7tQjdkeaCezGIqshGrODApR/gNeHpOu5WayFb8JseHriM7QZEhIJTHl/EbIUshbJDLUU/rAObCXuWChMwxkQC9qZ77rOAU2XW+mdOUDx0UejEvrO29wgSjhU0CgYEAhq9QwsbvUzrE29bBoSlhlyUw0f0MTC+kPU57Lv6Gs8FejvC+0xO0rY1nJgOXO6L7QPGaGXLHM2uqYjuWdSim0vLQonYY+KDfsa0U7OuWLYauRJ9K4ZfMsYxSxMC8cvmuEYlaWhO9q+URM6XnHJBTu9GXPqLJ3S7sI1woKqamGos=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk0QZtcHZNJ8ZKklC5itGvboY6ik/rQlS3pOQHat0HguOC52SAQyveJzLmweJKCXwvUAEPLYm2tbQmDLifK+yC0UtjFb9YFbdIFlf/ZF5UDi79py2DgxSwDnVWK7oGb/NMcCBHXdPFU4C2u2Wu1sxxwbGy0v0k885RaeTgrK/WrNHN9hQ3OgUcmp2jToMgHJFdkB5DupjgI26ms/Aen8ZaQJ9qia6pG4BlYn47+pp3tdgSkOXqYq81PEzOJVBMw/lex3IlyHlpOv0cGrKbtRCtaEPqEK9Y/mLDl0JULJPdfSpB3U/fWoN1bWYHmcrDgKsQLaejbM5ZG0GqkceYc2DTwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    //TODO 异步回调跳转失败（未收到请求），链接postman可访问，同步页面可跳转
    private  String notify_url = "http://6bcv4w.natappfree.cc/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        //TODO 可添加自动收单时间参数（订单取消后支付功能要关闭）
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
