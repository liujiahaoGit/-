app.service("loginService", function ($http) {
    this.loginName = function () {
        return $http.post("/login/name.do")
    }
});