 //控制层 
app.controller('itemController' ,function($scope,$controller){
	
	
	//数量操作
    $scope.addNum=function (x) {
        $scope.num=$scope.num+=x;
        if ($scope.num<1){
            $scope.num=1        }
    }

   
    //记录用户选择的规格
    $scope.specificationItems={};
    //用户选择规格
    $scope.selectSpecification=function (key,value) {
      $scope.specificationItems[key]=value;
	  searchSku();
    };

    //判断用户是否选中规格
    $scope.isSelect=function (key, value) {
        if ($scope.specificationItems[key] == value) {
            return true;
        }else {
            return false;
        }
    }
	
	//加载默认SKU
	$scope.sku={};
    $scope.loadSku=function () {
        $scope.sku=skuList[0];
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec))
    }
	//判断两个对象内容是否相同
	matchObject = function (map1, map2) {
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map2[k] != map1[k]) {
                return false;
            }
        }
        return true;
    };
	
	//用户选择规格后sku随之变动
    searchSku = function () {
        for (var i = 0; i < skuList.length; i++) {
            if (matchObject(skuList[i].spec, $scope.specificationItems)) {
              $scope.sku =skuList[i] ;
                return;
            }
        }
        $scope.sku = {id: 0, title: "------", price: 0}
    }
	//加入购物车
	 $scope.addToCart=function () {
        alert($scope.sku.id)
    }

    
});	
