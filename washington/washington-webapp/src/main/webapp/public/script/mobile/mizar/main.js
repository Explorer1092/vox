define(["jquery", "template", "flexSlider", "voxLogs","weui"], function ($) {
    var PopupContent = $("#PopupContent");
    var SID = getSid();

    $(document).on("click", ".js-reservationBtn", function () {
        $.ajax({
            url : "/mizar/loadparents.vpage",
            type: "POST",
            data : {
                sid: SID,
                shopId: baseData.shopId
            },
            success : function(data){
                if(data.success){
                    if(data.reserveFlag){
                        PopupContent.html( template("T:预约成功", {
                            reserveFlag : "1"
                        }) );
                    }else{
                        PopupContent.html( template("T:预约试听", {
                            success : true,
                            mobileMap: data.mobileMap || {}
                        }) );
                    }
                }else{
                    PopupContent.html( template("T:预约试听", {
                        success: false,
                        info : data.info
                    }) );
                }
            },
            error: function(e){
                PopupContent.html( template("T:预约试听", {
                    success: false,
                    info : "获取家长" + e.status + "错误"
                }) );
            }
        });
    });

    $("#merchantShowBtn").on('click',function(){
        PopupContent.html( template("T:商家入驻", {
            info : "联系我们补全课程和门店信息，帮助更多家长了解您的教学方式，建立更好沟通。联系邮箱 jiazhangtong@17zuoye.com"
        }) );
    });

    /*其他小伙伴还看了 推广*/
    if(location.pathname == "/mizar/shopdetail.vpage" && !baseData.isVip) {
        $.post('/mizar/shoprecommend.vpage', {shopId: getQueryString('shopId')}, function (data) {
            if(data.success && data.shopList.length > 0){
                $('#generalizeBox').html(template('generalizeBox_tem',{content: data.shopList}));
            }
        });
    }

    $(document).on("click", ".js-clickSelectParent", function(){
        var $this = $(this);
        var $dataType = $this.attr("data-type");


        $this.addClass("active");
        $this.siblings().removeClass("active");
        if($dataType == "autoMobile"){
            $this.focus();
        }
    });

    $(document).on("click", ".js-freeAppointmentSubmit", function(){
        var $currentData = $(".js-clickSelectParent[class*='active']");

        var $mobile = "";

        if($currentData.attr("data-type") == "autoMobile"){
            $mobile = $currentData.find("input").val();
        }

        YQ.voxLogs({
            database: 'parent',
            module: 'm_KoeVwt6X',
            op : "o_qHZvjCgB",
            s0 : getQueryString('shopId') || ""
        });

        if(!isMobile($mobile) && $currentData.attr("data-type") == "autoMobile"){
            popupInfo("请选择家长或输入正确的手机号码！");
            return false;
        }

        if(!$currentData.hasClass('active')){
            popupInfo("请选择预约方式！");
            return false;
        }

        var $postName = {
            sid : SID,
            goodsId : baseData.goodsId,
            shopId : baseData.shopId,
            parentId: _getCookie('uid') || 0,
            showName: $currentData.attr("data-showname") || "",
            callName: $currentData.attr("data-callname") || "",
            studentName: $currentData.attr("data-studentname") || "",
            mobile : $mobile
        };

        $.ajax({
            url : "/mizar/reserve.vpage",
            type: "POST",
            data : $postName,
            success : function(data){
                if(data.success){
                    PopupContent.html( template("T:预约成功", {
                        reserveFlag : "0"
                    }) );

                    YQ.voxLogs({
                        database: 'parent',
                        module: 'm_BUYMyC36',
                        op : "reserve_success",
                        s0 : getQueryString('shopId') || ""
                    });
                }else{
                    PopupContent.html( template("T:预约试听", {
                        success: false,
                        info : data.info
                    }) );
                    YQ.voxLogs({
                        database: 'parent',
                        module: 'm_BUYMyC36',
                        op : "reserve_fail",
                        s0 : getQueryString('shopId') || ""
                    });
                }
            },error: function(e){
                PopupContent.html( template("T:预约试听", {
                    success: false,
                    info : "预约" + e.status + "错误"
                }) );
            }
        });
    });

    $(document).on("click", ".js-closeTemplate", function () {
        closeTemplate();
    });

    $(".vipBannerBox").flexslider({
        animation : "slide",
        slideshowSpeed: 3000, //展示时间间隔ms
        direction : "horizontal",//水平方向
        animationLoop : false,
        controlNav:false,
        directionNav:false,
        itemWidth : 180,
        minItems : 3,
        maxItems : 3
    });

    $(".headerBanner").flexslider({
        animation : "slide",
        slideshowSpeed: 3000, //展示时间间隔ms
        direction : "horizontal",//水平方向
        animationLoop : false,
        controlNav:false,
        directionNav:false
    });

    $(".vipHeaderBanner").flexslider({
        animation : "slide",
        slideshowSpeed: 3000, //展示时间间隔ms
        direction : "horizontal",//水平方向
        animationLoop : false,
        controlNav:false,
        directionNav:false,
        itemWidth : 80,
        minItems : 4,
        maxItems : 4
    });

    $(document).on("click", "[data-logs]", function(){
        var $this = $(this);
        var $logsString = $this.attr("data-logs");
        var $logsJson = {};
        var $logsItems = {
            database: 'parent',
            module: 'm_BUYMyC36',
            s0 : getQueryString('shopId') || getQueryString('goodsId') || getQueryString('brandId') || "",
            s1 : ""
        };

        if($logsString != ""){
            $logsJson = eval("(" + $logsString + ")");
            // m : $logsJson.m, op: $logsJson.op, s0:$logsJson.s0, s1 : $logsJson.s1
            if($logsJson.m){ $logsItems.module = $logsJson.m; }
            if($logsJson.op){ $logsItems.op = $logsJson.op; }
            if($logsJson.s0){ $logsItems.s0 = $logsJson.s0; }
            if($logsJson.s1){ $logsItems.s1 = $logsJson.s1; }

            YQ.voxLogs($logsItems);
        }
    });

    /*加载更多同学*/
    $('#showSameMoreStudentsBtn').on('click',function(){
        $('.sameSchoolReserveList').find('li').show();
        $(this).closest('div.ah-btn').hide();
    });

    $('#showMoreStudentsBtn').on('click',function(){
        $('.otherSchoolReserveList').find('li').show();
        $(this).closest('div.ah-btn').hide();
    });


    /*写评价*/
    $("#remarkBtn").on('click',function(){
        location.href = '/mizar/remark/remark.vpage?_from=shopdetail&shopId='+getQueryString('shopId')+"&shopName="+baseData.shopName;
    });

    /*点赞*/
    $("#supportBtn").on('click', function () {
        var $this = $(this);
        var likeCount = $("#likeCount");
        if($this.hasClass('licked')){
            return false;
        }
        $.showLoading();
        $.post("/mizar/likeshop.vpage", {
            shopId: getQueryString('shopId'),
            activityId: baseData.activityId
        }, function (data) {
            $.hideLoading();
            if(data.success){
                $this.addClass('licked');
                likeCount.text(+likeCount.text() + 1).siblings('span').text('已点赞').siblings('i.phone-2').addClass('praiseRed');
                $.toast("点赞成功");
            }else {
                $.alert(data.info);
            }
        });
    });

    $(document).on("click", ".JS-GoToPage", function () {
        var $this = $(this);

        YQ.voxLogs({
            database: 'parent',
            module: "m_KoeVwt6X",
            op: "o_wjf9REF2",
            s1: $this.attr("data-url")
        });

        location.href = $this.attr("data-url");
    });

    function closeTemplate() {
        PopupContent.empty();
    }

    function getSid(){
        if(getQueryString('sid')){
            return getQueryString('sid')
        }else{
            return _getCookie('sid') || 0
        }
    }

    function popupInfo(info){
        var $box = $(".js-errorInfo");
        $box.val(info);

        setTimeout(function(){
            $box.val("");
        }, 2000);
    }

    //验证是否手机号
    function isMobile(value){
        value = value + "";
        //严格判定
        var _reg = /^0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-8])[0-9]{8}$/;
        //简单判定
        var reg = /^1[0-9]{10}$/;
        if(!value || value.length != 11 || !reg.test(value)){
            return false;
        }
        return true;
    }

    function _getCookie(name){
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if(arr=document.cookie.match(reg))
            return unescape(arr[2]);
        else
            return null;
    }

    //加载打点
    var locationLink = location.pathname;

    if(locationLink == "/mizar/shopdetail.vpage"){
        YQ.voxLogs({
            database: 'parent',
            module: 'm_BUYMyC36',
            op : "shop_detail_view",
            s0 : getQueryString('shopId') || ""
        });
    }

    if(locationLink == "/mizar/goodsdetail.vpage"){
        YQ.voxLogs({
            database: 'parent',
            module: 'm_BUYMyC36',
            op : "goods_order_view",
            s0 : getQueryString('shopId') || "",
            s1 : getQueryString('goodsId') || ""
        });
    }

    if(typeof(initMode) == "string" && initMode == "BrandDetail"){
        getLocation();

        YQ.voxLogs({
            database: 'parent',
            module: 'm_BUYMyC36',
            op : "brand_detail_view",
            s0 : getQueryString('brandId') || ""
        });
    }

    if(typeof(initMode) == "string" && initMode == "ShopSelectDetail"){
        reserveSelectMode();

        YQ.voxLogs({
            database: 'parent',
            module: 'm_KoeVwt6X',
            op : "o_OyWe3nkA"
        });
        $(document).on("click", ".JS-shopSelect", function(){
            $.showLoading("正在加载...");

            getLocation();
        });

        $(document).on("click", ".JS-clickShopName", function(){
            var $self = $(this);
            baseData.shopId = $self.attr("data-shopid");

            reserveSelectMode();

            YQ.voxLogs({
                database: 'parent',
                module: 'm_KoeVwt6X',
                op : "o_md97E7j6",
                s0: baseData.shopId
            });
        });
    }
});

//品牌详情页
if(typeof(initMode) == "string" && (initMode == "BrandDetail" || initMode == "ShopSelectDetail")){
    //获取位置回调
    var postData = {
        longitude: 0,
        latitude: 0
    };

    var vox = vox || {};
    vox.task = vox.task || {};
    vox.task.setLocation = function (res) {
        var resJson = JSON.parse(res);
        var errorCode = parseInt(resJson.errorCode || 0);

        if (errorCode > 0) {
            onError({
                code: errorCode
            })
        } else {
            onSuccess({
                coords: {
                    longitude: resJson.longitude,
                    latitude: resJson.latitude,
                    address: resJson.address
                }
            });
        }
    };

    function getLocation() {
        var options = {
            enableHighAccuracy: true,
            maximumAge: 1000
        };

        if (window['external'] && ('getLocation' in window.external)) {
            window.external.getLocation();
        } else {
            if (navigator['geolocation'] && navigator.geolocation['getCurrentPosition']) {
                navigator.geolocation.getCurrentPosition(onSuccess, onError, options);
            } else {
                //alert("位置反解析失败,重新定位");
                loadData();
            }
        }
    }

//成功时
    function onSuccess(position) {
        postData.latitude = position.coords.latitude;
        postData.longitude = position.coords.longitude;

        loadData();
    }

//失败时
    function onError(error) {
        switch (error.code) {
            case 1:
                //alert("位置服务被拒绝,重新定位");
                break;
            case 2:
                //alert("暂时获取不到位置信息,重新定位");
                break;
            case 3:
                //alert("获取信息超时,重新定位");
                break;
            case 4:
                //alert("未知错误,重新定位");
                break;
            default:
        }

        loadData();
    }

    function loadData(){
        if(initMode == "BrandDetail"){
            var RecentBusinesses = $("#RecentBusinesses");
            $.ajax({
                url : "/mizar/loadbrandnearshop.vpage",
                type: "POST",
                data : {
                    brandId: brandId,
                    longitude: postData.longitude,
                    latitude: postData.latitude
                },
                success : function(data){
                    if(data.success && data.shopMap && data.shopMap.shopId){
                        RecentBusinesses.html( template("T:最近商户", {
                            shopMap : data.shopMap
                        }) );

                        $("#FooterReservation").html( template("T:预约试听Foot", {
                            shopMap : data.shopMap
                        }) );
                    }
                }
            });
        }

        if(initMode == "ShopSelectDetail"){
            var ShopSelectDetail = $("#ShopSelectDetail");
            $.ajax({
                url : "/mizar/loadshopselect.vpage",
                type: "POST",
                data : {
                    brandId: brandId,
                    longitude: postData.longitude,
                    latitude: postData.latitude
                },
                success : function(data){
                    if(data.success && data.mappers){
                        ShopSelectDetail.html( template("T:ShopSelectDetail", {
                            mappers : data.mappers,
                            shopId: baseData.shopId
                        }) );
                    }
                    $.hideLoading();
                },
                error: function(){
                    $.hideLoading();
                }
            });

            YQ.voxLogs({
                database: 'parent',
                module: 'm_KoeVwt6X',
                op : "o_Hg6iTtY4"
            });
        }
    }

    function reserveSelectMode(){
        var ShopSelectDetail = $("#ShopSelectDetail");
        $.ajax({
            url : "/mizar/loadreserveselect.vpage",
            type: "POST",
            data : {
                shopId: baseData.shopId
            },
            success : function(data){
                if(data.success){
                    baseData.shopId = data.shopId;
                    baseData.goodsId = data.brandId;
                    brandId = data.brandId;

                    ShopSelectDetail.html( template("T:预约试听Page", {
                        data : data
                    }) );
                }
            }
        });
    }
}

//获取App版本
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}