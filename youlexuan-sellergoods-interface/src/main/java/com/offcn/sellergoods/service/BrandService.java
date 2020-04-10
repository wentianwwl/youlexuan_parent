package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbBrand;

import java.util.List;

public interface BrandService {
    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    public PageResult getBrandPage(int pageNum,int pageSize,TbBrand brand);
    /**
     * 添加品牌
     * @param brand
     */
    public void addBrand(TbBrand brand);

    /**
     * 根据id查询品牌信息
     * @param id
     * @return
     */
    public TbBrand getBrandById(long id);

    /**
     * 修改品牌信息
     * @param brand
     */
    public void updateBrand(TbBrand brand);

    /**
     * 删除集合中对应id值的品牌
     * @param ids
     */
    public void deleteBrand(Long[] ids);
}
