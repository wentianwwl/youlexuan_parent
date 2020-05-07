package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class itemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        Map<String,Object> map = new HashMap<String, Object>();
        //1.按关键字查询(高亮)
        map.putAll(searchList(searchMap));
        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        /*//查询品牌和规格列表
        if(categoryList.size() > 0){
            map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
        }*/
        //根据商品分类查询对应的品牌和规格
        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)){
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            if(categoryList.size() > 0){
                map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
            }
        }
        return map;
    }

    /**
     * 用于返回查询列表的结果(结果高亮)
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        //1.创建一个支持高亮查询器对象
        SimpleHighlightQuery highlightQuery = new SimpleHighlightQuery();
        //2.创建高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions();
        //3.设定需要高亮处理字段
        highlightOptions.addField("item_title");
        //4.设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //5.设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //6.管理高亮选项到高亮查询器对象
        highlightQuery.setHighlightOptions(highlightOptions);
        //7.设定查询条件,根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);
        //按分类筛选
        if (!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //按品牌筛选
        if (!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //按规格过滤
        if (searchMap.get("spec") != null){
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //按价格筛选
        if (!"".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");
            if (!prices[0].equals("0")){//如果价格的区间起点不是0
                Criteria filterCriteria = new Criteria("item_price").greaterThan(prices[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
            if (!prices[1].equals("*")){//如果价格的区间终点不是*
                Criteria filterQuery = new Criteria("item_price").lessThan(prices[1]);
                FilterQuery filterQuery1 = new SimpleFilterQuery(filterQuery);
                highlightQuery.addFilterQuery(filterQuery1);
            }
        }
        //分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo== null){
            pageNo=1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            pageSize= 20;//默认页面大小为20条数据
        }
        //从第几条数据查询(当前页-1)*pageSzie
        highlightQuery.setOffset((pageNo-1)*pageSize);
        highlightQuery.setRows(pageSize);

        //排序
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sortValue != null && !sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                highlightQuery.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                highlightQuery.addSort(sort);
            }
        }
        //8.发出带高亮数据的查询请求
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);
        //9.获取查询结果记录集合
        List<TbItem> content = highlightPage.getContent();
        for (TbItem item : content) {
            //获取到针对对象TbItem高亮集合
            List<HighlightEntry.Highlight> highlights = highlightPage.getHighlights(item);
            if (highlights != null && highlights.size() >0){
                //获取第一个高亮对象
                List<String> snipplets = highlights.get(0).getSnipplets();
                System.out.println("高亮: " + snipplets);
                //使用高亮结果替换商品标题
                item.setTitle(snipplets.get(0));
            }
        }
        map.put("rows", highlightPage.getContent());
        map.put("totalPages",highlightPage.getTotalPages());
        map.put("total",highlightPage.getTotalElements());
        return map;
    }

    /**
     * 根据输入的查询信息,查询分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<String>();
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项 (商品分类不能设置分词,要不然分组结果会失败)
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            list.add(tbItemGroupEntry.getGroupValue());
        }
        return list;
    }

    /**
     * 根据分类名称查询品牌和规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category)
    {
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
        if (typeId != null) {
            //根据模板id查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //根据模板id查询规格lb
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map;
    }

    /**
     * 导入数据到solr库中
     * @param list
     */
    @Override
    public void importList(List<TbItem> list) {
        for (TbItem item : list) {
            System.out.println(item.getTitle());
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);//从数据中提取规格json字符串转化为map
            Map map = new HashMap();
           for (Object key : specMap.keySet()){
                map.put("item_spec_"+Pinyin.toPinyin(key.toString(),"").toLowerCase(),specMap.get(key));
           }
           item.setSpecMap(map);//给待动态域注解的字段赋值
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 根据商品的id,从solr库中删除数据
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品的ID"+goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goods_id").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
