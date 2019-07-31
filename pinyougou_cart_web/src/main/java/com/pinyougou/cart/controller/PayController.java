package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.Result;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.weixinPayService;
import org.omg.CORBA.TIMEOUT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private weixinPayService payService;


    @Reference
    private OrderService orderService;


    @RequestMapping("/createNative")
    public Map createNative() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = orderService.searchPayLogFromRedis(username);
        if (tbPayLog!=null){
            return payService.createNative(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee()+"");

        }else {
            return new HashMap();
        }


    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int count=0;
        while (true){
            Map<String,String> map = payService.queryPayStatus(out_trade_no);
            if (map==null){
                result=new  Result(false,"支付错误");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                result=new  Result(true,"支付成功");
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count>=100){
                    result=new Result(false,"二维码超时");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return  result;
    }
}
