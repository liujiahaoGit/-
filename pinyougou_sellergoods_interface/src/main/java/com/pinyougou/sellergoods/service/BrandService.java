package com.pinyougou.sellergoods.service;


import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    //查询全部品牌(不分页)
    List<TbBrand> findAll();

    //查询全部品牌(分页)
    PageResult findByPage(int pageNum, int pageSize);

    //增加数据
    void add(TbBrand brand);

    //查询单个数据
    TbBrand findOne(Long id);

    //修改数据
    void update(TbBrand brand);

    //删除数据
    void delete(Long[] ids);

    //条件查询
    PageResult findByPage(TbBrand brand,int pageNum, int pageSize);

    //查询品牌列表并封装map集合
    List<Map> selectOptionList();
}
