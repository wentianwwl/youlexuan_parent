package com.offcn.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbBrand;
import com.offcn.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;
    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @RequestMapping("search")
    public PageResult search(int pageNum, int pageSize, @RequestBody TbBrand brand){
        return brandService.getBrandPage(pageNum,pageSize,brand);
    }
    /**
     * 添加品牌信息
     * @param brand
     * @return
     */
    @RequestMapping("addBrand")
    public Result addBrand(@RequestBody TbBrand brand){
        try {
            brandService.addBrand(brand);
            return new Result(true,"添加成功！");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"添加失败！");
        }
    }

    /**
     * 根据id查询品牌信息
     * @param id
     * @return
     */
    @RequestMapping("getBrandById")
    public TbBrand getBrandById(long id){
        return brandService.getBrandById(id);
    }
    /**
     * 修改品牌信息
     * @param brand
     * @return
     */
    @RequestMapping("updateBrand")
    public Result updateBrand(@RequestBody TbBrand brand){
        try {
            brandService.updateBrand(brand);
            return new Result(true,"修改成功！");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 删除品牌
     * @param ids
     * @return
     */
    @RequestMapping("deleteBrand")
    public Result deleteBrand(Long[] ids){
        try {
            brandService.deleteBrand(ids);
            return new Result(true,"删除成功！");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"删除失败！");
        }
    }

    /**
     * 模板页品牌下拉列表
     * @return
     */
    @RequestMapping("selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
