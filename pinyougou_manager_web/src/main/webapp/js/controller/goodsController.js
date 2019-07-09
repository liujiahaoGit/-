//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //查询实体
    $scope.findOne = function () {


        var id = $location.search().id;
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //读取富文本编辑器里的内容
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片列表
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格列表
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems)
                //SKU列表规格列展示
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
                    
                }
            }
        );


    };

    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var objcet = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (objcet!=null&&objcet.attributeValue.indexOf(optionName)>=0){
            return true
        }else {
            return false;
        }
    };

    //保存
    $scope.save = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    alert(" 保存成功");
                    location.href="goods.html";
                    $scope.entity={};
                    editor.html("");
                } else {
                    alert(response.message);
                }
            }
        );
    };
    $scope.add = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    alert("保存成功");
                    $scope.entity = {};
                    editor.html('');
                } else {
                    alert(response.message)
                }
            }
        )
    };


    //批量删除
    /*$scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };*/
    $scope.dele = function () {
        goodsService.dele($scope.selectIds).success(
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

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //文件上传

    $scope.upload = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.image_entity.url = response.message
                } else {
                    alert(response.message);
                }
            }
        ).error(function () {
            alert("上传发生错误")
        })
    };


    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};


    $scope.updateSpecAttribute = function ($event, name, value) {
        /*判断集合中有没有此属性*/
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value)
            } else {
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);

                //如果选项全部取消了
                if (object.attributeValue.length == 0) {
                    //则删除该属性值的框
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }

        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]})
        }

    };


    //深克隆 创建SKU列表
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];//初始

        var items = $scope.entity.goodsDesc.specificationItems;

        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    };

    //添加列值
    addColumn = function (list, columnName, conlumnValues) {
        var newList = [];//新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < conlumnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    };


    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);

    };

    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
        $scope.image_entity = {};
    };

    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (resopnse) {
                $scope.itemCat1List = resopnse;
            })
    };

    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {

        itemCatService.findByParentId(newValue).success(
            function (resopnse) {
                $scope.itemCat2List = resopnse;
            })


    });

    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (resopnse) {
                $scope.itemCat3List = resopnse;
            })
    });


    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {

        itemCatService.findOne(newValue).success(
            function (resopnse) {
                $scope.itemCat4List = resopnse;
            })
    });


    $scope.$watch('itemCat4List.typeId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (resopnse) {
                $scope.typeTemplate = resopnse;

                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);

                //如果url中没有ID 则加载扩展属性
                if ($location.search().id == null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems)
                }
            });
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;
            }
        )
    })


    $scope.itemCatList = [];
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name
                }
            })
    }

    $scope.updateStatus=function (status) {
        goodsService.updateStatus($scope.selectIds,status).success(
            function (response) {
                if (response.success){
                    $scope.reloadList();
                    $scope.selectIds=[]
                } else {
                    alert(response.message)
                }
            }
        )

    }
})
;
