app.service("brandService", function ($http) {
    this.findAll = function () {
        return $http.get("/brand/findAll.do");
    };

    this.findByPage = function (page, size) {
        return $http.get("/brand/findByPage.do?pageNum=" + page + "&pageSize=" + size)
    };

    this.add = function (entity) {
        return $http.post("/brand/add.do", entity)
    };

    this.update = function (entity) {
        return $http.post("/brand/update.do", entity)
    };

    this.findOne = function (id) {
        return $http.get("/brand/findOne.do?id=" + id)
    };

    this.delet = function (selectIds) {
        return $http.post("/brand/delete.do", selectIds)
    };

    this.search = function (page, size, searchEntity) {
        return $http.post("/brand/search.do?pageNum=" + page + "&pageSize=" + size, searchEntity);
    };

    this.selectOptionList=function () {
        return $http.get("/brand/selectOptionList.do");
    };

});