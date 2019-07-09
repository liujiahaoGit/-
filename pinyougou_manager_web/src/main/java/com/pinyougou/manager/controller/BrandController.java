package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.github.pagehelper.PageInfo;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll.do")
    public List findAll() {
        List<TbBrand> list = brandService.findAll();
        return list;
    }

    @RequestMapping("/findByPage.do")
    public PageResult findByPage(@RequestParam(value = "pageNum", defaultValue = "1") int page, @RequestParam(value = "pageSize", defaultValue = "10") int size) {

        PageResult result = brandService.findByPage(page, size);
        return result;
    }


    @RequestMapping("/add.do")
    public Result add(@RequestBody TbBrand brand, @RequestParam(value = "pageNum", defaultValue = "1") int page, @RequestParam(value = "pageSize", defaultValue = "10") int size) {
        Result result = new Result();
        PageResult pageResult = brandService.findByPage(brand, page, size);
        if (pageResult.getTotal() > 0 && pageResult.getRows().size() > 0) {
            result.setSuccess(false);
            result.setMessage("数据已存在");
            return result;
        }
        try {
            brandService.add(brand);
            result.setSuccess(true);
            result.setMessage("新增成功");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("新增失败");
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping("/findOne.do")
    public TbBrand findOne(long id) {
        TbBrand brand = brandService.findOne(id);
        return brand;
    }


    @RequestMapping("/update.do")
    public Result update(@RequestBody TbBrand brand, @RequestParam(value = "pageNum", defaultValue = "1") int page, @RequestParam(value = "pageSize", defaultValue = "10") int size) {
        Result result = new Result();
        PageResult pageResult = brandService.findByPage(brand, page, size);
        if (pageResult.getTotal() > 0 && pageResult.getRows().size() > 0) {
            result.setSuccess(false);
            result.setMessage("数据已存在");
            return result;
        }
        try {
            brandService.update(brand);
            result.setSuccess(true);
            result.setMessage("修改成功");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("修改失败");
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping("/delete.do")
    public Result delete(@RequestBody Long[] ids) {
        Result result = new Result();

        try {
            brandService.delete(ids);
            result.setSuccess(true);
            result.setMessage("删除成功");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("删除失败");
            e.printStackTrace();
        }
        return result;
    }


    @RequestMapping("/search.do")
    public PageResult find(@RequestBody TbBrand brand, @RequestParam(value = "pageNum", defaultValue = "1") int page, @RequestParam(value = "pageSize", defaultValue = "10") int size) {
        PageResult result = brandService.findByPage(brand, page, size);
        return result;
    }

    @RequestMapping("/selectOptionList.do")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }
}
