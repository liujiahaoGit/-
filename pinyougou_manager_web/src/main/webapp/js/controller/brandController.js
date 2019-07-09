app.controller("brand", function ($scope,$controller,brandService) {

    $controller("baseController",{$scope:$scope});//继承
    $scope.findAll = function () {
        brandService.findAll().success(
            function (data) {
                $scope.list = data;
            }
        )
    };



    $scope.findByPage = function (page, size) {
        brandService.findByPage(page, size).success(
            function (data) {
                $scope.list = data.pageSize;
                $scope.paginationConf.totalItems = data.total;//更新总记录数
            }
        )
    };

    $scope.save = function () {
        var obj = null;
        if ($scope.entity.id) {
            obj = brandService.update($scope.entity)
        } else {
            obj = brandService.add($scope.entity)
        }
        obj.success(
            function (data) {
                if (data.success) {
                    alert(data.message);
                    $scope.reloadList();//重新加载
                } else {
                    alert(data.message)
                }
            }
        )
    };

    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (data) {
                $scope.entity = data;
            }
        )
    };




    $scope.delet = function () {
        brandService.delet($scope.selectIds).success(
            function (data) {
                if ($scope.selectIds != null && $scope.selectIds.length > 0) {
                    if (confirm("您确定要删除所选数据吗?")) {
                        if (data.success) {
                            alert("删除成功");
                            $scope.reloadList();//重新加载
                            window.location.reload();
                        } else {
                            alert(data.message)
                        }
                    }
                } else {
                    alert("请先选择您要删除的数据")
                }
            }
        )
    };

    $scope.searchEntity = {};
    $scope.search = function (page, size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (data) {
                $scope.list = data.rows;
                $scope.paginationConf.totalItems = data.total;//更新总记录数
            }
        )
    }
});
