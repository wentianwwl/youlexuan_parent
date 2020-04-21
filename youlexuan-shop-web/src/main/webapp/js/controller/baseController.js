app.controller('baseController',function ($scope) {
    /*更新ids数组*/
    $scope.selectIds=[];
    $scope.updateSelectionIds=function($event,id){
        if ($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    }
    /*分页插件的使用*/
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };
    $scope.reloadList=function(){
        //切换页码
        $scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    /**
     * 提取json串中的某个属性，并将属性值以逗号拼接起来
     * @param jsonString 后台返回的是json对象，但是里面的字段是字符串
     * @param key
     */
    $scope.jsonToString=function (jsonString, key) {
        if (jsonString != null && jsonString != ""){
            var json = JSON.parse(jsonString);
            var value="";
            for (var i = 0; i < json.length; i++) {
                if (i > 0){
                    value+=",";
                }
                value+=json[i][key];
            }
            return value;
        }
    }
    /**
     * 从集合中根据key值查询对象
     */
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }
})
