package com.pinyougou.service;

import java.util.Map;

public interface weixinPayService {

    /**
     * 调用微信sdk 生成二维码
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    Map createNative(String out_trade_no,String total_fee);


    /**
     * 检测二维码支付状态
     * @param out_trade_no
     * @return
     */
    Map queryPayStatus(String out_trade_no);

    /**
     * 关闭微信订单
     * @param out_trade_no
     * @return
     */
    Map closePay(String out_trade_no);
}
