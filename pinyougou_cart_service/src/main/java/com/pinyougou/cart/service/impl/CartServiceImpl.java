package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 添加商品到购物车列表中
     *
     * @param cartList
     * @param itemId
     * @param num
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //根据商品SKUid查出SKU商品
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品信息异常");
        }
        //获取商家ID
        String sellerId = item.getSellerId();
        //判断购物车列表中是否存在该商家
        Cart cart = searchCartBySellerId(cartList, sellerId);


        if (cart == null) {
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List orderList = new ArrayList();
            orderList.add(orderItem);
            cart.setOrderItemList(orderList);
            cartList.add(cart);
        } else {
            if (sellerId.equals(cart.getSellerId())) {
                //存在
                //判断为该商家购物车内是否存在要添加的商品
                List<TbOrderItem> orderItemList = cart.getOrderItemList();


                TbOrderItem tbOrderItem = searchOrderItemByItemId(orderItemList, itemId);


                //没有 创建新的商品对象,并放入到购物车中
                if (tbOrderItem == null) {
                    TbOrderItem orderItem = createOrderItem(item, num);
                    orderItemList.add(orderItem);
                } else {
                    //有
                    //商品数量增加相应数量  更新价格
                    tbOrderItem.setNum(tbOrderItem.getNum() + num);
                    tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue() * tbOrderItem.getNum()));
                    //如果数量操作后小于等于0,则移出
                    if (tbOrderItem.getNum() <= 0) {
                        orderItemList.remove(tbOrderItem);
                    }
                    //如果移出后 cart的明细数量为0 则移出cart
                    if (orderItemList.size() == 0) {
                        cartList.remove(cart);
                    }

                }
            }

        }
        return cartList;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中查询购物车"+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 向redis中存入购物车
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存入购物车"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }


    /**
     * 根据商品id查询商品
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if (tbOrderItem.getItemId().longValue() == itemId.longValue()) {
                return tbOrderItem;
            }
        }
        return null;
    }

    /**
     * 创建新的商品对象
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num < 0) {
            throw new RuntimeException("商品信息异常");
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);
        orderItem.setPrice(item.getPrice());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    /**
     * 根据商家ID判断购物车列表中是否存在该商家的购物车
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList.size() != 0) {
            for (Cart cart : cartList) {
                if (cart.getSellerId().equals(sellerId)) {
                    return cart;
                }
            }
        }
        return null;
    }


}

