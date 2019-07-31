package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.service.weixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class weixinPayServiceImpl implements weixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${notifyurl}")
    private String notifyurl;

    @Value("${partnerkey}")
    private String partnerkey;

    /**
     * 调用微信sdk 生成二维码
     *
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        //封装参数
        Map<String, String> map = new HashMap<>();
        map.put("appid", appid);//微信支付分配的公众账号ID
        map.put("mch_id", partner);//商户账号
        map.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        map.put("body", "品优购");//商品描述
        map.put("out_trade_no", out_trade_no);//商户订单号
        map.put("total_fee", total_fee);//总金额（分）
        map.put("spbill_create_ip", "127.0.0.1");//IP
        map.put("notify_url", notifyurl);//回调地址(随便写)
        map.put("trade_type", "NATIVE");//交易类型

       /* Map param=new HashMap();
        param.put("appid", "wx8397f8696b538317");//公众账号ID
        param.put("mch_id", "1473426802");//商户
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");
        param.put("out_trade_no", "4567");//交易订单号
        param.put("total_fee", "1");//金额（分）
        param.put("spbill_create_ip", "127.0.0.1");
        param.put("notify_url", "http://www.itcast.cn");
        param.put("trade_type", "NATIVE");//交易类型*/

        try {

            String xml = WXPayUtil.generateSignedXml(map, partnerkey);
            System.out.println("===========================>>" + xml);

            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setXmlParam(xml);
            httpClient.setHttps(true);
            httpClient.post();
            //接收参数
            String content = httpClient.getContent();
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            System.out.println(xmlToMap);
            Map resultMap = new HashMap();
            resultMap.put("code_url", xmlToMap.get("code_url"));//二维码
            resultMap.put("out_trade_no", out_trade_no);//订单号
            resultMap.put("total_fee", total_fee);//总金额
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }

    @Override
    public Map queryPayStatus(String out_trade_no) {

        Map map = new HashMap();
        map.put("appid", appid);
        map.put("mch_id", partner);
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        map.put("out_trade_no", out_trade_no);
        try {
            String xml = WXPayUtil.generateSignedXml(map, partnerkey);
            System.out.println(xml);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xml);
            httpClient.post();
            String content = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            System.out.println("=========================>>>" + resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closePay(String out_trade_no) {
        Map param=new HashMap();
        param.put("appid", appid);//公众账号 ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/closeorder";
        try {
            String xml = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client=new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xml);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
