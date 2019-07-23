var app = angular.module("com.pinyougou", []);

app.filter("trustHtml",['$sce',function ($sce) {
    return function (data) { //要过滤的数据
        return $sce.trustAsHtml(data); //过滤后的数据
    }
}]);
