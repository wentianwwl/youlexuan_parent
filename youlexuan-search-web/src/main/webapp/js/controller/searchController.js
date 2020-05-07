app.controller("searchController",function ($scope, $location,searchService) {
    //搜索对象
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':''};

    //搜索
    $scope.search=function () {
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索放回的结果。
                buildPageLabel();
            }

        )
    }
    //添加搜索项
    $scope.addSearchItem=function (key, value) {
        $scope.searchMap.pageNo=1;
        if (key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();//执行搜索
    }
    //移除搜索条件
    $scope.removeSearchItem=function (key) {
        $scope.searchMap.pageNo=1;
        if (key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]="";
        }else{
            delete $scope.searchMap.spec[key];
        }
        $scope.search();//执行搜索
    }

    //构建分页标签
    buildPageLabel=function () {
        $scope.pageLabel=[];//新增分页栏属性
        var maxPageNo=$scope.resultMap.totalPages;//总页码数
        var firstPage=1;//开始页码
        var lastPage = maxPageNo;//结束页码

        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后面有点

        if (maxPageNo > 5){//如果总页数大于5,显示部分页码
            if ($scope.searchMap.pageNo<=3){//显示前五页页码
                lastPage=5;
                $scope.firstDot=false;//前面没有点
            }else if ($scope.searchMap.pageNo>=lastPage-2){//显示后五页页码
                firstPage = maxPageNo-4;
                $scope.lastDot=false;//后面没有点
            }else {//显示当前页前后的五页页码
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else {//总页数小于等于5页前后均没有点
            $scope.firstDot=false;
            $scope.lastDot=false;
        }
        //循环产生页码标签
        for (var i = firstPage; i <lastPage ; i++) {
            $scope.pageLabel.push(i);
        }
    }
    //根据页码查询
    $scope.queryByPage=function (pageNo) {
        //验证页码,页码为负数或者页码大于总页码,验证不通过
        if (pageNo<1 || pageNo > $scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }
    /**
     * 判断当前页为第一页
     */
    $scope.isTopPage=function () {
        if ($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }
    /**
     * 判断当前页是否为最后一页,
     */
    $scope.resultMap={"totalPages":1};//如果不定义,会报totalPages未定义异常
    $scope.isEndPage=function () {
        if ($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 当前页页码数字为红色
     */
    $scope.ispage=function (p) {
        if (parseInt(p)==parseInt($scope.searchMap.pageNo)){
            return true;
        }else{
            return false;
        }
    }
    /**
     * 设置排序规则
     */
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }
    /**
     * 判断关键字是不是品牌
     */
    $scope.keywordsIsBrand=function () {
        for (var i = 0;i<$scope.resultMap.brandList.length;i++){
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }
    /**
     * 接收index主页跳转过来的查询字符串
     */
    $scope.loadkeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }

})