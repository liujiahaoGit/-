app.service("userService", function ($http) {
    //增加
    this.add=function(entity,code){
        return  $http.post('../user/add.do?code='+code,entity );
    };

    //发送验证码
    this.sendCode=function(phone){
        return  $http.get('../user/sendCode.do?phone='+phone );
    }
});
