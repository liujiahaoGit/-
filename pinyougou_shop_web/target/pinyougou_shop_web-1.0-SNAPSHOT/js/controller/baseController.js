app.controller("baseController", function ($scope) {
    //重新加载列表数据
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)
    };

    $scope.paginationConf = {
        currentPage: 1,  //默认当前页
        totalItems: 10,  //总记录数
        itemsPerPage: 10, //每页的数据
        perPageOptions: [5, 10, 20, 25, 30], //每页选项
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };

    $scope.selectIds = [];
    $scope.deleteSelect = function ($event, id) {
        if ($event.target.checked) {

            var arr = angular.element($scope.selectIds);
            /*判断数组中是否包含此id*/
            if (($.inArray(id, arr)) == -1) {
                $scope.selectIds.push(id);
            }
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);
        }
    };


    $scope.isChecked = function (id) {
        return $scope.selectIds.indexOf(id) != -1;
    };


    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += "，";
            }
            value += json[i][key];
        }
        return value;
    };


    //在list中根据键值查找对象值
    $scope.searchObjectByKey=function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key]==keyValue){
                return list[i]
            }
        }
        return null;
    }
});