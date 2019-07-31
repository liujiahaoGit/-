package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.oinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.weixinPayService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private weixinPayService payService;


    @Reference
    private SeckillOrderService seckillOrderService;


    @RequestMapping("/createNative")
    public Map createNative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        if (tbSeckillOrder != null) {
            long fen = (long) (tbSeckillOrder.getMoney().doubleValue() * 100);//金额（分
            Map map = payService.createNative(tbSeckillOrder.getId() + "", fen + "");
            return map;

        } else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int count = 0;
        while (true) {
            Map<String, String> map = payService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false, "支付错误");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")) {
                result = new Result(true, "支付成功");
                seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(3);
                count++;
                if (count >= 5) {
                    result = new Result(false, "二维码超时");
                    Map resultMap = payService.closePay(out_trade_no);
                    if (!"SUCCESS".equals(resultMap.get("result_code"))) {//如果返回结果是正常关闭
                        if ("ORDERPAID".equals(resultMap.get("err_code"))) {
                            result = new Result(true, "支付成功");
                            seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no), map.get("transaction_id"));
                        }
                    }
                    if (result.isSuccess() == false) {
                        System.out.println("超时，取消订单");
                        //2.调用删除
                        seckillOrderService.deleteOrderFromRedis(username,Long.valueOf(out_trade_no));
                    }
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
