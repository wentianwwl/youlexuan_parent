app.controller('contentController',function ($scope, contentService) {
    $scope.contentList=[];//存储所有广告的集合
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId]=response;
            }
        )
    }
    /**
     * 点击搜索,跳转到搜索页
     */
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
})