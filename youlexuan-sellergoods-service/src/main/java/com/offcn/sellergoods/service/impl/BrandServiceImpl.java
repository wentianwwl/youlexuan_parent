package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbBrandMapper;
import com.offcn.pojo.TbBrand;
import com.offcn.pojo.TbBrandExample;
import com.offcn.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;
    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    public PageResult getBrandPage(int pageNum, int pageSize, TbBrand brand) {
        PageHelper.startPage(pageNum,pageSize);
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if (brand != null){
            if (brand.getName() != null && brand.getName() != ""){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (brand.getFirstChar() != null && brand.getFirstChar() != ""){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        PageInfo<TbBrand> info = new PageInfo<TbBrand>(brandMapper.selectByExample(example));

        return new PageResult(info.getTotal(),info.getList());
    }

    /**
     * 添加商品品牌
     * @param brand
     */
    public void addBrand(TbBrand brand) {
        brandMapper.insertSelective(brand);
    }

    /**
     * 根据id查询品牌信息
     * @param id
     * @return
     */
    public TbBrand getBrandById(long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }

    /**
     * 修改品牌信息
     * @param brand
     */
    public void updateBrand(TbBrand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /**
     * 删除集合中id对应的品牌
     * @param ids
     */
    public void deleteBrand(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }
}
