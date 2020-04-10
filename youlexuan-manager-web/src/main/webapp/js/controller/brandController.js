
app.controller("myController",function ($scope,$controller,brandService) {//注入BrandService
    /**
     * 继承baseController
     */
    $controller('baseController',{$scope:$scope});
    /*条件查询和列表查询合在一起*/
    $scope.searchEntity={};
    $scope.search=function(pageNum,pageSize){
        brandService.getBrandPage(pageNum,pageSize,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }
    /*添加或者修改后保存品牌*/
    $scope.save=function(){
        if ($scope.brand.id == null){
            /*添加*/
            brandService.addBrand($scope.brand).success(
                function (response) {
                    if (response.success){
                        alert(response.message);
                        //重定向
                        $scope.reloadList();
                    }else{
                        alert(response.message);
                    }
                }
            )
        }else {
            /*保存*/
            brandService.updateBrand($scope.brand).success(
                function (response) {
                    if (response.success){
                        alert(response.message);
                        //重定向
                        $scope.reloadList();
                    }else{
                        alert(response.message);
                    }
                }
            )
        }

    }
    /*根据id获取品牌信息*/
    $scope.getBrandById=function(id){
       brandService.getBrandById(id).success(
            function (response) {
                $scope.brand=response;
            }
        )
    }


    /*删除品牌*/
    $scope.deleteBrand=function(){
        if ($scope.selectIds.length == 0){
            alert("请选择要删除的内容！");
        }else{
           brandService.deleteBrand($scope.selectIds).success(
                function (response) {
                    if (response.success){
                        alert(response.message);
                        //重定向
                        $scope.reloadList();
                        $scope.selectIds=[];
                    }else{
                        alert(response.message);
                    }
                }
            )
        }
    }
})