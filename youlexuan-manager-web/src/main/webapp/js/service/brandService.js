app.service('brandService',function ($http) {
    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param searchEntity
     * @returns {*}
     */
    this.getBrandPage=function (pageNum,pageSize,searchEntity) {
        return $http.post("../brand/search.do?pageNum="+pageNum+"&pageSize="+pageSize,searchEntity)
    }
    /**
     * 添加品牌
     * @param brand
     */
    this.addBrand=function (brand) {
        return $http.post("../brand/addBrand.do",brand)
    }
    /**
     * 修改品牌
     * @param brand
     */
    this.updateBrand=function (brand) {
        return $http.post("../brand/updateBrand.do",brand)
    }
    /**
     * 根据id查询品牌
     * @param id
     */
    this.getBrandById=function (id) {
        return $http.post("../brand/getBrandById.do?id="+id)
    }
    /**
     * 删除品牌
     * @param selectionIds
     */
    this.deleteBrand=function (selectionIds) {
        return $http.get("../brand/deleteBrand.do?ids="+selectionIds)
    }
})