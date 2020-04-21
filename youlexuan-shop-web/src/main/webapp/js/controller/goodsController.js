 //控制层
 app.controller('goodsController' ,function($scope,$controller   ,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承

	 /**
	  * 商品展示,设置审核状态数组
	  */
	 $scope.status=['未审核','已审核','审核未通过','关闭']
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
	 	var id = $location.search()['id'];
	 	if (id == null){
	 		return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//先富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//显示图片列表
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//显示规格
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//显示sku列表
				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	 /**
	  * 根据规格名称和规格项目名名称,返回是否勾选
	  */
	 $scope.checkAttributeValue=function(specName,optionName){
	 	var items = $scope.entity.goodsDesc.specificationItems;
	 	var object = $scope.searchObjectByKey(items,'attributeName',specName);
	 	if (object==null){
	 		return false;
		}else {
	 		if (object.attributeValue.indexOf(optionName)>=0){
	 			return true;
			}else {
	 			return false;
			}
		}
	 }
	
	//保存 
	$scope.save=function(){
		//获取富文本编辑器的值
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			$scope.add();//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//修改成功后跳会商品列表页
					location.href="goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//保存
	$scope.add=function () {
		//获取富文本编辑器的值
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(
			function (response) {
				if (response.success){
					alert("保存成功");
					$scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };
					editor.html('');
				}else{
					alert(response.message);
				}
			}
		)
	}
	 /**
	  * 上传图片
	  */
	 $scope.uploadFile = function () {
		 uploadService.uploadFile().success(
		 	function (response) {
				if (response.success){
					$scope.image_entity.url=response.message;
				}else{
					alert(response.message);
				}
			}
		 ).error(
		 	function () {
				alert("上传发生错误");
			}
		 )
	 }
	 /**
	  * 保存图片：就是讲图片的颜色和url路径添加到entity对象
	  * 用于保存
	  */
	 $scope.entity={goods:{},goodsDesc:{itemImages:[]}};
	 $scope.add_image_entity=function () {
		 $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	 }
	 /**
	  * 在图片列表中移除图片
	  */
	 $scope.remove_image_entity=function (index) {
         $scope.entity.goodsDesc.itemImages.splice(index,1);
     }
     /**
      * 读取一级分类
      */
     $scope.selectItemCat1List=function () {
         itemCatService.findByParentId(0).success(
             function (response) {
                 $scope.itemCat1List=response;
             }
         )
     }
	 /**
	  * 读取二级分类：$watch检查某个值，当值变动时触发回调函数，newValue代表新的值，oldValue代表老的值
	  */
	 $scope.$watch("entity.goods.category1Id",function (newValue, oldValue) {
		 //判断一级分类已经选择了具体的分类，然后再去获取二级分类
		 if (newValue){
		 	//根据选择的值，查询二级分类
			 itemCatService.findByParentId(newValue).success(
			 	function (response) {
					$scope.itemCat2List = response;
				}
			 )
		 }
	 })
	 /**
	  * 读取三级分类
	  */
	 $scope.$watch("entity.goods.category2Id",function (newValue, oldValue) {
		 if (newValue){
		 	//根据选择的值，查询三级分类
			 itemCatService.findByParentId(newValue).success(
			 	function (response) {
					$scope.itemCat3List = response;
				}
			 )
		 }
	 })
	 /**
	  * 读取模板ID
	  */
	 $scope.$watch("entity.goods.category3Id",function (newValue, olcValue) {
		 //判断三级分类是否选择了
		 if (newValue){
		 	itemCatService.findOne(newValue).success(
		 		function (response) {
					$scope.entity.goods.typeTemplateId=response.typeId;
				}
			)
		 }
	 })
	 /**
	  * 选择模板ID后，更新品牌列表和扩展属性
	  */
	 $scope.$watch("entity.goods.typeTemplateId",function (newValue, oldValue) {
		 //判断模板ID是否已经存在
		 if (newValue){
		 	typeTemplateService.findOne(newValue).success(
		 		function (response) {
					//获取整个模板信息
					$scope.typeTemplate = response;
					//获取整个模板信息中的品牌信息
					$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);

					//获取模板表中的扩展属性
					if($location.search()['id']==null){
						$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
					}

					//查询规格列表
					typeTemplateService.findSpecList(newValue).success(
						function (response) {
							$scope.specList = response;
						}
					)
				}
			)
		 }
	 })
	 $scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};
	 $scope.updateSpecAttribute=function ($event, name, value) {
		 //搜索规格,看指定规格名字的规格是否存在

		 var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
		 /*如果规格存在：可以直接在attributeValue直接增删。
		 	如果规格不存在，算是首次添加，需要添加规格和规格值
		 */
		 if (object != null){
		 	//判断复选框选中的状态
			 if ($event.target.checked){
				//复选框选中，把对应的规格项的值插入到当前对应规格项数组中
				 object.attributeValue.push(value);
			 }else {
				 object.attributeValue.splice( object.attributeValue.indexOf(value),1);

				 //如果选项都取消了，将此条记录移除
				 if (object.attributeValue.length==0){
					 $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				 }
			 }
		 }else {
			 //如果规格指定名字的规格不存在，算是首次加入，要加入规格以及规格项

			 $scope.entity.goodsDesc.specificationItems.push( {"attributeName":name , "attributeValue":[value]});
		 }
	 }
	 /**
	  * 生成SKU列表，（深克隆）
	  */
	$scope.createItemList=function () {
		//用spec存储sku对应的规格
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];
		//定义变量items指向 用户选中的规格集合
		var items = $scope.entity.goodsDesc.specificationItems;
		//遍历用户选中的规格集合
		for (var i = 0; i < items.length; i++) {
			//编写增加sku规格的方法 addColumn(),其中有三个参数，参数1：sku规格列表，参数2：规格名称，参数3：规格选项
			$scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}

	//增加sku规格类的方法
	 addColumn=function (list,attributeName,attributeValue) {
		 //待返回的新集合
		 var newList = [];
		 //遍历sku规格列表
		 for (var i = 0; i < list.length; i++) {
			 //读取每行sku数据,赋值给遍历oldRow
			 var oldRow = list[i];
			 //遍历规格选项
			 for (var j = 0; j < attributeValue.length; j++) {
				 //深克隆当前行sku数据 为newRow
				 var newRow = JSON.parse(JSON.stringify(oldRow));
				 //在新行扩展列(列名是规格名),值是规格项的值
				 newRow.spec[attributeName]=attributeValue[j];
				 //保存行sku行,到待返回返回的集合
				 newList.push(newRow);
			 }
		 }
		 return newList;
	 }

	 /**
	  * 根据页面的分类id,异步返回商品分类名称
	  */
	 $scope.itemCatList=[];//初始化分类列表

	 $scope.findItemCatList=function () {
		 itemCatService.findAll().success(
		 	function (response) {
				for (var i = 0; i < response.length; i++) {
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		 )
	 }
 });












