package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.security.auth.login.CredentialException;
import javax.swing.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

    /**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//设置未申请状态
		goods.getGoods().setAuditStatus("0");
		//插入商品表
		goodsMapper.insert(goods.getGoods());
		//设置id
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//插入商品扩展数据
		goodsDescMapper.insert(goods.getGoodsDesc());

		//调用保存sku信息的方法
		saveItemList(goods);


	}
	/**
	 * 设置sku条目信息
	 */
	private void setItemValues(Goods goods,TbItem item){
		item.setGoodsId(goods.getGoods().getId());//商品spu编号
		item.setSellerId(goods.getGoods().getSellerId());//商家编号
		item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号(3级)
		item.setCreateTime(new Date());//创建时间
		item.setUpdateTime(new Date());//更新时间
		//设置品牌名称
		TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(tbBrand.getName());
		//设置分类名称
		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(tbItemCat.getName());
		//设置商家名称
		TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(tbSeller.getNickName());
		//设置图片地址(spu的第一个图片地址)
		List<Map> maps = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (maps.size() > 0){
			item.setImage((String) maps.get(0).get("url"));
		}
	}
	/**
	 * 保存sku列表数据
	 */
	private void saveItemList(Goods goods){
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			for (TbItem item : goods.getItemList()) {
				//获取标题
				String title = goods.getGoods().getGoodsName();
				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				for (String s : specMap.keySet()) {
					title+=" "+specMap.get(s);
				}

				item.setTitle(title);
				//调用保存sku条目的方法
				setItemValues(goods,item);
				itemMapper.insert(item);
			}
		}else {
			TbItem item = new TbItem();
			//当没有规格禁用的时候,sku的title属性就是spu的名字
			item.setTitle(goods.getGoods().getGoodsName());
			item.setPrice(goods.getGoods().getPrice());
			item.setStatus("1");
			item.setIsDefault("1");
			item.setNum(99999);
			item.setSpec("{}");
			setItemValues(goods,item);
			itemMapper.insert(item);
		}
	}
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//设置未申请状态
		goods.getGoods().setAuditStatus("0");
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//删除原有的sku列表数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//添加新的sku列表
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		goods.setGoodsDesc(tbGoodsDesc);
		//查询sku商品列表
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);//根据商品ID查询
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
							/**
							 *因为是获取当前商家的商品,因此要根据商家ID获取查询列表
							 */
							criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
