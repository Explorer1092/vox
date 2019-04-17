/*-----------书店管理相关-----------*/
define(["jquery", "prompt", "datetimepicker", "paginator", "jqform", "template"], function ($) {

    Array.prototype.indexOf = function (val) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == val) return i;
        }
        return -1;
    };

    Array.prototype.contains = function (val) {
        return this.indexOf(val) >= 0;
    };

    Array.prototype.remove = function (val) {
        var index = this.indexOf(val);
        if (index > -1) {
            this.splice(index, 1);
        }
    };

    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    /*********       门店列表相关          *********/

    $("#shopName").val(getQuery("bookStoreName"));
    $("#shopId").val(getQuery("bookStoreId"));
    $("#phNum").val(getQuery("bookStorePhone"));
    $("#linkman").val(getQuery("contactName"));
    //查询
    $("#js-filter").on("click", function (e) {

        var shopName = $('#shopName').val();
        var shopId = $('#shopId').val();
        var phNum = $('#phNum').val();
        var linkman = $('#linkman').val();

        var pageIndex = $("#pageIndex").val();
        if (e.hasOwnProperty("originalEvent")) {
            pageIndex = 1;
        }

        location.href = '/bookstore/manager/list.vpage?bookStoreName=' + shopName
            + '&bookStoreId=' + shopId + '&bookStorePhone=' + phNum + '&contactName=' + linkman
            + '&page=' + pageIndex;
    });

    //分页插件
    var paginator = $('#paginator');
    if (paginator.length > 0) {
        paginator.jqPaginator({
            totalPages: parseInt(paginator.attr("totalPages")),
            visiblePages: 5,
            currentPage: parseInt(paginator.attr("pageIndex") || 1),
            first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
            prev: '<li class="prev"><a href="javascript:void(0);">上一页<\/a><\/li>',
            next: '<li class="next"><a href="javascript:void(0);">下一页<\/a><\/li>',
            last: '<li class="last"><a href="javascript:void(0);">尾页<\/a><\/li>',
            page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
            onPageChange: function (pageIndex, opType) {
                if (opType == 'change') {
                    $('#pageIndex').val(pageIndex);
                    $('#js-filter').trigger("click");
                }
            }
        });
    }

    // // 查询
    // $("#js-filter").on("click", function () {
    //     $("#filter-form").submit();
    // });
    $("#shopName").bind('change', function (event) {
        if (event.keyCode == '13') {
            $("#filter-form").submit();
        }
    });
    $("#shopId").bind('keypress', function (event) {
        if (event.keyCode == '13') {
            $("#filter-form").submit();
        }
    });
    $("#phNum").bind('keypress', function (event) {
        if (event.keyCode == '13') {
            $("#filter-form").submit();
        }
    });
    $("#linkman").bind('keypress', function (event) {
        if (event.keyCode == '13') {
            $("#filter-form").submit();
        }
    });


    // 验输入
    var requireInputs = $(".require");

    function isEmptyInput() {
        var isTrue = false;
        requireInputs.each(function () {
            if ($(this).val() == '' && procode != '820000' && procode != '710000') {
                $(this).addClass("error").vl("请填写" + $(this).attr("data-title"));
                isTrue = true;
            }
        });
        if (procode != '820000' && procode != '710000' && ($('#cmbCity').val() == '')) {
            $('#cmbCity').addClass('error');
        }

        if (procode != '820000' && procode != '710000' && $(".require.error").length > 0) {
            return true;
        }
        return isTrue;
    }

    $(document).on("focus", ".require.error", function () {
        $(this).removeClass('error').val('');
    });

    // 获取省市
    getProvince();

    function getProvince() {
        $.post("/bookstore/manager/provinces.vpage", function (res) {
            if (res.success) {
                if (res.data.provinces) {
                    dataList = res.data.provinces;
                    for (var i = 0; i < dataList.length; i++) {
                        province = dataList[i];
                        procode = province.code;
                        var provinceOption = document.createElement("option");
                        $(provinceOption).attr('value', province.name);
                        $(provinceOption).attr('code', province.code);
                        $(provinceOption).text(province.name);
                        $('#cmbProvince').append(provinceOption);
                    }
                    $("#cmbProvince").prepend("<option value='' code=''>请选择</option>");
                    $("#cmbProvince").val('请选择');
                }
            }
        });
    }

    var dataList = [], province = {}, procode = '', cityList = [], cName = '';

    var setAddress = function () {

        var cmbProvince = $('#cmbProvince').attr('value');
        var cmbProvinceCode = $('#cmbProvince').attr('code');
        // console.log('#cmbCity',cmbCity);
        var cmbCity = $('#cmbCity').attr('value');
        var cmbCityCode = $('#cmbCity').attr('code');
        var detailArea = $('#detailArea').attr('value');
        var AResult = JSON.stringify({
            provinceCode: cmbProvinceCode,
            provinceName: cmbProvince,
            countryCode: cmbCityCode,
            cityName: cmbCity,
            detailAddress: detailArea
        });
        $('#address').attr('value', AResult);
    };

    $('#cmbProvince').on('change', function () {
        $("#cmbCity").empty();
        procode = $("#cmbProvince").find("option:selected").attr('code');
        if (procode != '820000' && procode != '710000') {
            $("#cmbCity").val('请选择');
            if (procode == '请选择') return;
        }
        getCity(procode);
        setAddress();
    })

    function getCity() {
        $.get("/bookstore/manager/regionlist.vpage?regionCode=" + procode, function (res) {
            if (res.success) {
                if (res.data.regionList) {
                    cityList = res.data.regionList;
                    for (var i = 0; i < cityList.length; i++) {
                        cName = cityList[i].name;
                        var cityOption = document.createElement("option");
                        $(cityOption).attr('value', cName);
                        $(cityOption).text(cName);
                        $('#cmbCity').append(cityOption);
                    }
                    if (procode != '820000' && procode != '710000') {
                        $('#cmbCity').prepend("<option value=''>请选择</option>");
                    } else if (procode = '820000') {
                        $('#cmbCity').prepend("<option value=''></option>");
                    } else if (procode = '710000') {
                        $('#cmbCity').prepend("<option value=''></option>");
                    }
                }
            }
        });
    }

    // 获取 市信息
    $('#cmbCity').on('change', function (procode) {
        // getCity(procode);
        $("#cmbCity").find("option:selected").val();
        setAddress();
    });
    $('#detailArea').on('keyup', function () {
        setAddress();
    })


    // 新建
    $("#add-save-newpage").on("click", function () {
        setAddress();
        if (isEmptyInput()) {
            return false;
        }
        if (!$("#cmbProvince").val()) {
            $.prompt("<div style='text-align:center;'>" + ( "请填写书店地址！") + "</div>", {
                title: "提示",
                buttons: {"确定": true},
                focus: 1,
                submit: function( e,v ){
                    // if ( v ) {
                    //     window.location.href = '/bookstore/manager/list.vpage';
                    // }
                },
                useiframe: true

            });
            return;
        };
        if (!$("#title-image").val()) {
            $.prompt("<div style='text-align:center;'>" + ( "请上传图片！") + "</div>", {
                title: "提示",
                buttons: {"确定": true},
                focus: 1,
                submit: function( e,v ){
                },
                useiframe: true
            });
            return;
        };
        $.prompt("<div style='text-align:center;'>是否确认保存？</div>", {
            title: "操作提示",
            buttons: {"取消": false, "确定": true},
            focus: 1,
            submit: function (e, v) {
                if (v) {
                    ;
                    var phone = $('#mobile').val();
                    if (!(/^1[3|4|5|7|8][0-9]{9}$/.test(phone))) {
                        alert("手机号码有误，请重填");
                        return false;
                    }
                    $("#add-form").ajaxSubmit(function (res) {
                        if (res.success) {
                            $.prompt("<div style='text-align:center;'>保存成功</div>", {
                                title: "提示",
                                buttons: {"确定": true},
                                focus: 1,
                                submit: function (e, v) {
                                    console.log(e, v)
                                    if (v) {
                                        location.href = '/bookstore/manager/list.vpage';
                                    }
                                },
                                useiframe: true
                            });
                        } else {
                            $.prompt("<div style='text-align:center;'>" + (res.info || "保存失败！") + "</div>", {
                                title: "提示",
                                buttons: {"确定": true},
                                focus: 1,
                                useiframe: true
                            });
                        }
                    });
                }
            },
            useiframe: true
        });
    });
    //点击上传图片
    $(document).on("click", "#imageSquareTrigger", function () {
        $("#imageSquare").click();
    });

    $(document).on("change", "#imageSquare", function () {
        $("#imageSquareTrigger").text("图片上传中...");
        // 拼formData
        var $img = $("#preview-image");
        var _this =  this;
        $("#noneImage").hide();
        $img.show();
        var formData = new FormData();
        var file = $(this)[0].files[0];
        if(!file) return ;
        $("#imageSquareTrigger").val(file.name);
        if(file.size > 5242880){
            $.prompt("<div style='text-align:center; font-size: 12px'>" +  "图片过大！" + "</div>", {
                title: "提示",
                buttons: {"确定": true},
                focus: 1,
                useiframe: true
            });
            $("#imageSquare").val('');
            $("#imageSquareTrigger").val('');
            $("#imageSquareTrigger").html("+&nbsp&nbsp点击上传图片");
            return false;
        }
        formData.append('path', "bookStore");
        formData.append('file', file);
        formData.append('file_size', file.size);
        formData.append('file_type', file.type);

        // 发起请求
        $.ajax({
            url: '/bookstore/manager/tool/uploadphoto.vpage',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function (res) {
                if (res.success) {
                    $("#title-image").val(res.info);
                    var file = $(_this)[0].files[0];
                    var reader = new FileReader();
                    reader.readAsDataURL(file);
                    reader.onload = function(e) {

                        // 图片base64化
                        var newUrl = this.result;
                        $("#preview-image").prop("src", newUrl);
                        $("#imageSquareTrigger").html("+&nbsp&nbsp点击上传图片");


                    };



                } else {
                    alert(res.info);
                }
            }
        });
    });
    //这段代码用于解决一些JQuery版本缺少的一个函数

});