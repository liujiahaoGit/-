package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Cart;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 5000)
    private CartService cartService;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人:" + username);
        System.out.println("从cookies中读取");
        //从cookies中读取
        String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        if (cartList == null || "".equals(cartList)) {
            cartList = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartList, Cart.class);

        if (username.equals("anonymousUser")) {
            return cartList_cookie;

        } else {
            //从redis 中读取
            System.out.println("从redis中读取");

            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size() > 0) {
                System.out.println("执行了合并购物");
                //如果cookie中有数据 则合并
                cartListFromRedis = cartService.mergeCartList(cartListFromRedis, cartList_cookie);
                //将合并后的购物车存进redis中
                cartService.saveCartListToRedis(username,cartListFromRedis);
                //删除cookie中的数据
                CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println(username+"删除cookie");
            }
            return cartListFromRedis;
        }

    }


    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人:" + username);
        List<Cart> cartList = findCartList();
        cartList = cartService.addGoodsToCartList(cartList, itemId, num);
        try {
            if (username.equals("anonymousUser")) {//未登录
                System.out.println("向cookies中添加");
                //向cookies中添加
                cartList = cartService.addGoodsToCartList(cartList, itemId, num);
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {
                System.out.println("向redis中添加");
                //向redis中添加
                cartService.saveCartListToRedis(username, cartList);
            }

            return new Result(true, "加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "加入购物车失败");
        }


    }
}
