app.controller("userController", function ($scope, userService,$location) {

    $scope.reg=function () {

        if ($scope.entity.password!=$scope.password){
            alert("您输入的两次密码不一致,请重新输入")
            return;
        }

        userService.add($scope.entity,$scope.code).success(
            function (response) {
                alert(response.message)
            }
        )
    };
    
    $scope.sendCode=function () {

        if ($scope.entity.phone==null||$scope.entity.phone==""){
            alert("请输入手机号码")
        }

        userService.sendCode($scope.entity.phone).success(
            function (response) {
               alert(response.message)
            }
        ) 
    }

});