package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 所搜
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 导入item数据到solr库
     * @param list
     */
    public void importList(List<TbItem> list);

    /**
     * 根据商品的id,从solr库中删除数据
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);
}
