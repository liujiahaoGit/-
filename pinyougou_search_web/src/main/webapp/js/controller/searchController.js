app.controller("searchController", function ($scope, searchService,$location) {

    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 40,
        'sort': '',
        'sortFiled': ''
    };//搜索对象

    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                buildPageLabel();
            }
        )
    };
    //分页

    buildPageLabel = function () {
        var firstPage = 1; //开始页码
        var lastPage = $scope.resultMap.totalPages;//截止页码
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true; //后面有点
        if ($scope.resultMap.totalPages > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false;
            } else if ($scope.searchMap.pageNo >= $scope.resultMap.totalPages - 2) {
                firstPage = $scope.resultMap.totalPages - 4;
                lastPage = $scope.resultMap.totalPages;
                $scope.lastDot = false;
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
                $scope.firstDot = true;
                $scope.lastDot = true;
            }
        } else {
            $scope.firstDot = false;
            $scope.lastDot = false;
        }

        $scope.pageLabel = [];
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    };
    //根据页码查询
    $scope.queryByPage = function (pageNo) {
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    };

    //是否为第一页
    $scope.isFirstPage = function () {
        /* $scope.searchMap.pageNo==1?true:false;*/
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    };

    //是否为最后页
    $scope.isLastPage = function () {
        /*$scope.searchMap.pageNo==$scope.resultMap.totalPages?true:false;*/
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    };

    //添加面包屑
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value
        }

        $scope.search();
    };
    //移出面包屑
    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    };

    $scope.sortSearch = function (sort, sortFiled) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortFiled = sortFiled;
        $scope.search();
    };
    //按价格升降序
    $scope.flag = true;
    $scope.searchPrice = function (sortFiled) {
        if ($scope.flag) {
            $scope.searchMap.sort = 'ASC';
            $scope.searchMap.sortFiled = sortFiled;
            $scope.search();
            $scope.flag = false;
        } else {
            $scope.searchMap.sort = 'DESC';
            $scope.searchMap.sortFiled = sortFiled;
            $scope.search();
            $scope.flag = true;
        }
    };

    //隐藏品牌列表
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    };

    $scope.loadkeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }
});