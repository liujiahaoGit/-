package com.pinyougou.sellergoods.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pinyougou.entity.PageResult;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.service.SpecificationService;
import org.springframework.transaction.annotation.Transactional;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    @Transactional
    public void add(Specification specification) {
        specificationMapper.insert(specification.getSpecification());

        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();

        for (TbSpecificationOption option : specificationOptionList) {
            option.setSpecId(specification.getSpecification().getId());
            specificationOptionMapper.insert(option);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        //保存修改的规格
        specificationMapper.updateByPrimaryKey(specification.getSpecification());

        //删除原表中的数据
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(specification.getSpecification().getId());
        specificationOptionMapper.deleteByExample(example);


        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();

        for (TbSpecificationOption option : specificationOptionList) {
            option.setSpecId(specification.getSpecification().getId());
            specificationOptionMapper.insert(option);
        }

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        //查询规格
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

        //查询规格选项列表
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        if (id != 0) {
            criteria.andSpecIdEqualTo(id);
        }
        List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(example);

        //构建组合实体类返回
        Specification specification = new Specification();
        specification.setSpecification(tbSpecification);
        specification.setSpecificationOptionList(tbSpecificationOptions);
        return specification;


    }

    /**
     * 批量删除
     */
    @Override
    @Transactional
    public void delete(Long[] ids) {
        //删除规格
        for (Long id : ids) {
            specificationMapper.deleteByPrimaryKey(id);

            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> selectSpecList() {

        List<Map> list = new ArrayList<>();

        List<TbSpecification> tbSpecifications = specificationMapper.selectByExample(null);
        for (TbSpecification specification : tbSpecifications) {
            Map<String, Object> map = new HashMap();
            map.put("id", specification.getId());
            map.put("text", specification.getSpecName());
            list.add(map);
        }
        return list;
    }

}
