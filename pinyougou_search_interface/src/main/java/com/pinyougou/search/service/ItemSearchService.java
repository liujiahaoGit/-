package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    Map<String,Object> search(Map searchMap);

    /**
     * 导入数据
     * @param list
     */
    void importList(List list);

    /**
     * 删除数据
     * @param goodsList
     */
    void deleteByGoodsIds(List goodsList);
}
