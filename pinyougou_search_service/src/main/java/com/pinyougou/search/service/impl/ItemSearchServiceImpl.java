package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap<>();
        String keywords = (String) searchMap.get("keywords");
        String s = keywords.replaceAll(" ", "");
        searchMap.put("keywords",s);
        //按关键字查询产品列表
        map.putAll(searchList(searchMap));

        //按分类列表查询
        List list = searchCategoryList(searchMap);
        map.put("categoryList", list);


        //品牌规格列表

        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)){//如果有分类名称
            Map brandAndSpecList = searchBrandAndSpecList(categoryName);
            map.putAll(brandAndSpecList);
        }else {
            if (list.size()>0){//如果没有分类名称，按照第一个查询
                Map brandAndSpecList = searchBrandAndSpecList((String) list.get(0));
                map.putAll(brandAndSpecList);
            }
        }


        return map;
    }



    private Map searchList(Map searchMap) {
        Map<String, Object> map = new HashMap<>();
        //高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);
        //按关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //按分类查询
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //按品牌查询
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //按规格查询
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            Set<String> set = specMap.keySet();
            for (String key : set) {
                Criteria filterCriteria=new Criteria("item_spec_"+key).is(searchMap.get(key));
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //按价格查询
        if (!"".equals(searchMap.get("price"))){
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("-");
            if (!price[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //分页显示
        Integer pageNo = (Integer) searchMap.get("pageNo");//当前页
        if (pageNo==null){
            pageNo=1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页显示的条数
        if (pageSize==null){
            pageSize=20;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //排序
        String sortValue = (String) searchMap.get("sort"); //ASC升序  DESC降序
        String sortFiled = (String) searchMap.get("sortFiled");
        if (sortValue!=null&&!"".equals(sortValue)){
            if (sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortFiled);
                query.addSort(sort);
            }else {
                Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortFiled);
                query.addSort(sort);
            }
        }

        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //循环高亮入口
        List<HighlightEntry<TbItem>> highlighted = tbItems.getHighlighted();
        for (HighlightEntry<TbItem> h : highlighted) {
            TbItem entity = h.getEntity();
            if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                entity.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", tbItems.getContent());
        map.put("totalPages",tbItems.getTotalPages());//总页数
        map.put("total",tbItems.getTotalElements());//总记录数
        return map;
    }


    private List searchCategoryList(Map searchMap) {
        Query query = new SimpleQuery();
        List list = new ArrayList();

        //按关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);


        //设置分组选项
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");

        query.setGroupOptions(groupOptions);
        GroupPage<TbItem> tbItems = solrTemplate.queryForGroupPage(query, TbItem.class);

        GroupResult<TbItem> item_category = tbItems.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //根据分类名称获取模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            //根据模板Id获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);
            //根据模板Id获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsList) {
        System.out.println("商品id:"+goodsList);
        SolrDataQuery query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
