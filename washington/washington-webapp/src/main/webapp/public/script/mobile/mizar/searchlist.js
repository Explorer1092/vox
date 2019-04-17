//获取位置回调
var postData = {
    firstCategory: "",
    secondCategory: "",
    regionCode: "",
    tradeArea: "",
    orderBy: "smart",
    shopName: "",
    pageNum: 0,
    longitude: 0,
    latitude: 0
};

var vox = vox || {};
vox.task = vox.task || {};
vox.task.setLocation = function(res){
    var resJson = JSON.parse(res);
    //var resJson = res;

    var errorCode = parseInt(resJson.errorCode || 0);

    if(errorCode > 0){
        onError({
            code: errorCode
        })
    }else{
        onSuccess({
            coords : {
                longitude : resJson.longitude,
                latitude : resJson.latitude,
                address: resJson.address
            }
        });
    }
};

var moduleName = "m_S4ZqUOgY",
    categoryFlag = true,
    areaFlag = true;

define(['jquery', 'template', 'weui', 'voxLogs', 'voxSpread'], function ($, template) {
    /*vox.task.setLocation({
     errorCode : 0,
     longitude: 117.10,
     latitude :	40.13
     });*/

    (function(){
        var external = window.external || {};

        (external['webDidLoad'] || $.noop).call(
            external,
            JSON.stringify({
                time : Date.now() + '',
                url: location.protocol + '//' + location.hostname + location.pathname.replace(/(\/){2,}/g, '/')
            })
        );
    })();

    $(document).on("click", ".js-headerBar li", function () {
        var $this = $(this);

        if ($this.hasClass("active")) {
            $this.removeClass("active");
            $("#headerBar").children().eq($this.index()).hide();
        } else {
            if ($this.index() == 3) {
                //显示搜索
                $("#searchModal").show();

            } else {
                $this.addClass('active').siblings().removeClass('active');
                $("#headerBar").children().eq($this.index()).show().siblings().hide();
                if ($this.index() == 1) {

                    if (categoryFlag) {
                        $($(".js-firstCategory")[2]).click();//默认定位到少儿外语
                    }
                    categoryFlag = false;
                }
                if ($this.index() == 2) {
                    if ($(".js-areaItem")) {
                        postData.regionCode = $($(".js-areaItem")[0]).data("code");
                    } else {
                        postData.regionCode = "";
                    }
                    if (areaFlag) {
                        $($(".js-areaItem")[0]).click();
                    }
                    areaFlag = false;

                }
            }
        }

    });

    //一级目录
    $(document).on("click", "li.js-firstCategory", function () {
        var $this = $(this),
            target_text = $this.text().trim();

        $this.addClass('active').siblings().removeClass('active');

        if (target_text != "全部") {

            var childrenNode = $("#secondCategoryList").find('li[data-parent="' + target_text + '"]');
            childrenNode.show().siblings('li[data-parent!="' + target_text + '"]').hide();
            $("#allSecondCate").show();
            postData.firstCategory = target_text;
        } else {
            $(".js-secondCategory").show();
            postData.firstCategory = "";
        }

    });

    //二级目录
    $(document).on("click", "li.js-secondCategory", function () {
        var $this = $(this),
            index = $this.parents(".ageList-pop").data("index"),
            target_text = $this.text().trim();
        $this.parents(".ageList-pop").hide();
        $this.addClass("active").siblings().removeClass("active");
        postData.pageNum = 0;
        var text = postData.firstCategory;
        if (text == "") {
            text = "全部分类";
        }

        $(".js-headerBar").children().eq(index).html('<a href="javascript:void(0);">' + text + '</a>').removeClass("active");
        if (target_text == "全部") {
            target_text = "";
        }

        postData.secondCategory = target_text;
        getShopList();
    });

    //智能排序
    $(document).on("click", ".js-orderBtn", function () {
        var $this = $(this),
            index = $this.parents(".ageList-pop").data("index"),
            name = $this.text().trim(),
            order = $this.data("order"),
            s0Name = "";
        $this.parents(".ageList-pop").hide();
        $this.addClass("active").siblings().removeClass("active");
        $(".js-headerBar").children().eq(index).html('<a href="javascript:void(0);">' + name + '</a>').removeClass("active");

        postData.pageNum = 0;
        postData.orderBy = order;
        getShopList();
    });

    //区域
    $(document).on("click", ".js-areaItem", function () {
        var $this = $(this),
            code = $this.data("code"),
            target_text = $this.text().trim();

        $this.addClass('active').siblings().removeClass('active');

        var childrenNode = $("#tradeItemList").find('li[data-parent="' + code + '"]');
        childrenNode.show().siblings('li[data-parent!="' + code + '"]').hide();
        $("#allTradeItem").show();

        postData.pageNum = 0;
        postData.regionCode = code;
    });

    //商区
    $(document).on("click", ".js-tradeItem", function () {
        var $this = $(this),
            index = $this.parents(".ageList-pop").data("index"),
            code = $this.data("parent"),
            name = $this.text().trim();
        $this.parents(".ageList-pop").hide();
        postData.pageNum = 0;
        if (name == "全部") {
            postData.tradeArea = "";
        } else {
            postData.tradeArea = name;
        }

        var topTitle = $('.js-areaItem[data-code="' + code + '"]').text().trim();
        if ($(".js-areaItem.active").length != 0) {
            topTitle = $(".js-areaItem.active").text().trim();
        }

        $(".js-headerBar").children().eq(index).html('<a href="javascript:void(0);">' + topTitle + '</a>').removeClass("active");
        getShopList();
    });

    //进详情
    $(document).on("click", ".js-shopDetail", function () {
        var $this = $(this),
            id = $this.data("id");

        YQ.voxLogs({
            database: 'parent',
            module: moduleName,
            op: "o_n7XLsUn4",
            s0: id
        });

        setTimeout(function () {
            location.href = "/mizar/shopdetail.vpage?shopId=" + id;
        }, 50);

    });

    //搜索
    $(document).on("click", ".js-searchBtn", function () {
        var name = $("#searchInput").val();
        postData.pageNum = 0;
        postData.shopName = name;

        getShopList();
    });

    //查看更多
    $(document).on("click", ".js-refreshBtn", function () {
        postData.pageNum = postData.pageNum + 1;
        getShopList(true);
    });

    //重新获取位置
    $(document).on("click", ".placeFail", function () {
        getLocation();
    });

    getLocation();

    /*APP 低于 1.6版本需要一个标题条*/
    (function () {
        window.isFromParent = window.navigator.userAgent.toLowerCase().indexOf("17parent") > -1;

        if (window.isFromParent) {
            var titleDOM = $("#parentAppTopHeader");

            if (titleDOM.length > 0) {
                var app_version = getQueryString("app_version");
                if (app_version && parseFloat(app_version) < parseFloat("1.6")) {
                    titleDOM.show();
                }
            }
        }
    })();

    //ActivityBannerBox 221001  :test 220801
    YQ.voxSpread({
        keyId: 221001
    }, function (result) {
        if (result.success && result.data &&  result.data.length > 0) {
            $("#ActivityBannerBox").html( template("T:ActivityBannerBox", {result: result}) );
        }
    });

    YQ.voxLogs({
        database: 'parent',
        module: moduleName,
        op: "o_YUFDjG32"
    });
});

//Get Query
function getQueryString(item) {
    var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
    return svalue ? decodeURIComponent(svalue[1]) : '';
}

function getShopList(flag) {
    YQ.voxLogs({
        database: 'parent',
        module: moduleName,
        op: "o_ZI8NtvJO",
        s0: JSON.stringify(postData)
    });

    $.showLoading("卖力查找中……");
    /*postData.longitude = 116.486834;
     postData.latitude = 40.000967;*/
    $.post("/mizar/loadshops.vpage", postData, function (res) {
        $("#searchModal").hide();
        $.hideLoading();
        //初始化搜索条件
        postData.shopName = "";
        if (res.success) {
            if (res.rows && res.rows.length > 0) {
                var rows = res.rows;

                for (var i = 0; i < rows.length; i++) {
                    if (rows[i].distance < 1 && rows[i].distance > 0) {
                        rows[i]["fDistance"] = parseFloat(rows[i].distance * 1000).toFixed(0) + "m";
                    } else if(rows[i].distance > 1) {
                        rows[i]["fDistance"] = parseFloat(rows[i].distance).toFixed(2) + "km";
                    } else {
                        rows[i]["fDistance"] = "";
                    }

                    if (rows[i].secondCategory.length != 0) {
                        //目前二级分类目录支持一个
                        rows[i]["fSecondCate"] = rows[i].secondCategory[0];
                    } else {
                        rows[i]["fSecondCate"] = "";
                    }
                }

                if (flag) {
                    $("#shopListCon").append(template("shopItem", {rows: rows, pressImage: pressImage}));
                } else {
                    $("#shopListCon").html(template("shopItem", {rows: rows, pressImage: pressImage}));
                }

                if (res.pageNum + 1 == res.totalPage) {
                    $('.js-refreshBtn').hide();
                } else {
                    $('.js-refreshBtn').html("查看更多").show();
                }
            } else {
                //$.alert("暂无对应条件商家");
                $("#shopListCon").empty();
                $(".js-refreshBtn").html("暂无对应条件商家").show().removeClass("js-refreshBtn");
            }
        } else {
            $.alert(res.info);
        }
    })
}

function getLocation() {
    var options = {
        enableHighAccuracy: true,
        maximumAge: 1000
    };

    if(window['external'] && ('getLocation' in window.external)){
        window.external.getLocation();
    }else{
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(onSuccess, onError, options);
        } else {
            footBarInfo("位置反解析失败,重新定位");
            getShopList();
        }

        if(getQueryString("firstCategory") != ""){
            postData.firstCategory = decodeURI(getQueryString("firstCategory"));

            $(".js-secondCategory:first").click();
        }

        footBarInfo("定位失败", "fail")
    }
}

//成功时
function onSuccess(position) {
    postData.latitude = position.coords.latitude;
    postData.longitude = position.coords.longitude;

    footBarInfo("我的位置:" + (position.coords.address || "无名"), "success");

    if(getQueryString("firstCategory") != ""){
        postData.firstCategory = decodeURI(getQueryString("firstCategory"));

        $(".js-secondCategory:first").click();
    }else{
        getShopList();
    }
}

//失败时
function onError(error) {
    switch (error.code) {
        case 1:
            footBarInfo("位置服务被拒绝,重新定位");
            break;
        case 2:
            footBarInfo("暂时获取不到位置信息,重新定位");
            break;
        case 3:
            footBarInfo("获取信息超时,重新定位");
            break;
        case 4:
            footBarInfo("未知错误,重新定位");
            break;
    }

    getShopList();
}

function footBarInfo(text, type){
    var _locationPlace = $("#locationPlace");

    if(type && type == 'fail'){
        _locationPlace.find(".place-btn").html(text);
        _locationPlace.hide();
    }else{
        _locationPlace.find(".place-btn").html(text);
        _locationPlace.show();
    }

    if(type && type == 'success'){
        _locationPlace.find(".place-btn").removeClass("placeFail");
    }
}

function pressImage(link, w){
    var defW = 200;
    if(w){
        defW = w;
    }

    if(link && link != "" && link.indexOf('oss-image.17zuoye.com') > -1 ){
        return link + '@' + defW + 'w_1o_75q';
    }else{
        return link;
    }
}