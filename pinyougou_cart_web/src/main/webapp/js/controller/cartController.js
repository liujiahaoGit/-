app.controller("cartController", function ($scope, cartService) {

    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.tatalValue = cartService.sum($scope.cartList)
            })
    };

    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList() //刷新列表
                } else {
                    alert(response.message)
                }

            }
        )
    };

    $scope.findListByUserId = function () {
        cartService.findListByUserId().success(
            function (response) {
                $scope.addressList = response;
            }
        )
    };

    $scope.selectAddress = function (address) {
        $scope.address = address;
    };

    $scope.isSelectAddress = function (address) {
        if (address == $scope.address) {
            return true
        } else {
            return false;
        }

    }

    $scope.order = {paymentType: '1'};

    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type
    }

    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success){
                    if ($scope.order.paymentType=='1') {
                        location.href="pay.html";
                    }else {
                        location.href="paysuccess.html";
                    }
                } else {
                    location.href="payfail.html";
                }
            }
        )
    }
});