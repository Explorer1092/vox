/**
 * @author huihui.li
 * @description "奖品中心改版"
 * @createDate 2017/1/6
 */

define(['jquery', 'knockout','YQ', 'flexSlider', 'weui', 'voxLogs', 'voxSpread'], function ($, ko,yq) {
    var defaultInitMode;

    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'PrizeIndexMode':
                defaultInitMode = new PrizeIndexMode();// 奖品中心首页
                break;
            case 'IntegralMode':
                defaultInitMode = new IntegralMode();// 学豆记录
                break;
            case 'EnergyBoxMode':
                defaultInitMode = new EnergyBoxMode();//  我的能量箱
                break;
            case 'WishListMode':
                defaultInitMode = new WishListMode();//  心愿池
                break;
            default:
            //initMode null
        }
    }

    //奖品中心首页 成长乐园
    function PrizeIndexMode() {
        var $this = this;
        $this.isShowPageNull = ko.observable(true);// 是否显示空内容
        $this.isShowStaticBanner = ko.observable(false);
        $this.headerStaticBanner = ko.observable();
        $this.headerStaticimgDoMain = ko.observable();
        $this.activityShow = ko.observable();
        $this.locationHref =  ko.observable();
        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "37b84f");
        }
        YQ.voxLogs({
            module: "m_Reo8J9vz",
            op : "page_prize_center_load_success",
            s0 : "prizecenter"
        });
        $this.imagesDetail = ko.observableArray([]);
        $this.myOrderBoxClick = function () {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: "channel_my_prize_center_click"
            });
            location.href = "/view/mobile/student/center/energybox?new_page=blank";
        }
        $this.myprivilegeBoxClick = function () {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: "channel_my_dress_click"
            });
            location.href = "/view/mobile/student/center/myprivilege?new_page=blank";
        }
        $this.mywishBoxClick = function () {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: "channel_my_wish_click"
            });
            location.href = "/view/mobile/student/center/mywish?new_page=blank";
        }
        $this.privilegeMoreClick = function (headWearCategoryId) {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: "button_virtual_prizes_more_click"
            });
            location.href = "/view/mobile/student/center/privilegelist?new_page=blank&productType=JPZX_TIYAN&categoryId=" + headWearCategoryId;
        }

        $this.weikeMoreClick = function (miniCourseCategoryId) {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: "button_class_more_click"
            });
            location.href = "/view/mobile/student/center/learnrewardlist?new_page=blank&productType=JPZX_WEIKE&categoryIds=" + miniCourseCategoryId;
        }

        $this.prizeMoreClick = function () {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: "button_prize_more_click"
            });
            location.href = "/view/mobile/student/center/prizerewardlist?new_page=blank&productType=JPZX_SHIWU";
        }

        //点击换一换
        $this.orderByrandom = function () {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: "button_change_like_click"
            });
            location.href = "/view/mobile/student/center/reward?new_page=blank&orderBy=random";
        }
        $this.prizeProductClick = function (productId,op,froms) {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: op,
                s0: froms,
                s1:productId
            });
            location.href = "/view/mobile/student/center/rewarddetail?new_page=blank&productId=" + productId;
        }

        YQ.voxSpread({
            keyId: 320901
        }, function(items){
            if(items.success && items.data && items.data.length > 0){
                $this.headerStaticBanner(items);
                $this.isShowStaticBanner(true);
                $("#bannerContentBox").flexslider({
                    animation: "fade",
                    directionNav: false,
                    pauseOnAction: true,
                    after: function (slider) {
                        if (!slider.playing) {
                            slider.play();
                        }
                    },
                    slideshowSpeed: 4000,
                    animationSpeed: 400
                });
                $this.isShowPageNull(false);

            }
        });
        $.ajax({
            url: "/studentMobile/center/reward/activities.vpage",
            type: "GET",
            success: function (data) {
                if (data.success){
                    if ((data.onGoingActivities.length + data.finishedActivities.length) > 1 ){
                        $this.activityShow(true);
                        $this.locationHref("/view/mobile/student/center/activities?new_page=blank");
                    }else{
                        if ((data.onGoingActivities.length + data.finishedActivities.length) == 0 ){
                            $this.activityShow(false);
                        }else{
                            $this.activityShow(true);
                            if (data.onGoingActivities.length == 1){
                                $this.locationHref("/view/mobile/student/center/activity?new_page=blank&activityId=" + data.onGoingActivities[0].id);
                            }else if(data.finishedActivities.length == 1){
                                $this.locationHref("/view/mobile/student/center/activity?new_page=blank&activityId=" + data.finishedActivities[0].id);
                            }
                        }

                    }
                }
            }
        });
        $this.activitiesBanner = function (logs) {
            var ops;
            if (logs == "more"){
                ops = "button_gongyi_more_click";
            }else{
                ops = "gongyi_banner_click";
            }
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op: ops
            });
            location.href = $this.locationHref();
        }
    }

    //学豆记录
    function IntegralMode() {
        var $this = this;
        var originalData = [];
        $this.integralList = ko.observableArray();
        $this.isShowLoading = ko.observable(true);
        $this.totalIntegral = ko.observable(0);
        $this.usableIntegral = ko.observable(0);
        $this.getIntegral = function (num, callback) {
            $.post('/studentMobile/center/integralchip.vpage', {
                pageNumber: num,
            }, function (data) {
                if(data.success){
                    $this.totalIntegral(data.pagination.totalIntegral);
                    $this.usableIntegral(data.pagination.usableIntegral)
                    if(data.pagination.content.length < 20){
                        $this.isShowLoading(false);
                    }
                    originalData = originalData.concat(data.pagination.content);
                    $this.integralList(originalData);
                }
            });
        }

        $this.getIntegral(1);

        var loading = false;//状态标记;
        var pageNumber = 1;//page
        $(document.body).infinite().on("infinite", function() {
            if(loading || !$this.isShowLoading()) return;
            loading = true;
            setTimeout(function() {
                $this.getIntegral(pageNumber += 1);
                loading = false;
            }, 500);   //模拟延迟
        });

        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "463085");
        }

        YQ.voxLogs({
            module: "m_Reo8J9vz",
            op : "wealth_get_records_click"
        });
    }

    //我的能量箱
    function EnergyBoxMode() {
        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "989187");
        }
        var $this = this;
        $this.orderDeWishTrue = ko.observable(true);
        $this.productId = ko.observable();
        $this.energyProductClick = function (isConvertible,productType,productId,Id) {
            if (isConvertible){
                if (productType == "JPZX_TIYAN" || productType == "COUPON"){
                    location.href = "/view/mobile/student/center/rewarddetail?new_page=blank&productId=" + productId;
                }else if(productType == "ACTIVITY"){
                    location.href = "/view/mobile/student/center/activity?new_page=blank&activityId=" + Id;
                }
            }else{
                $(".dialog_show2").fadeIn(150);
            }
        }
        //点击取消兑换
        $this.cancelOrderClick = function (productId) {
            event.stopPropagation();
            $this.productId(productId);
            $(".dialog_show").fadeIn(150);
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op : "cancle_exchange_click"
            });
        }
        $this.EnergyBoxClick = function () {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op : "wealth_get_records_click"
            });
            location.href = "/resources/apps/mobile/LearnBeanCultivation/V1_0_0/index.html?from=energybox";
        }
        $this.dialogDefaultClick = function () {
            $(".dialog_show").fadeOut(150);
        }
        $this.dialogOkClick = function () {
            $(".dialog_show2").fadeOut(150);
        }
        $this.dialogPrimaryClick = function () {
            if ($this.orderDeWishTrue()){
                $this.orderDeWishTrue(false);
                $.post('/reward/order/removeorder.vpage', {orderId: $this.productId()}, function (data) {
                    if (data.success) {
                        $(".dialog_show").fadeOut(150);
                        setTimeout(function () {
                            location.reload();
                        }, 300);
                    }else{
                        $.alert(data.info)
                        $this.orderDeWishTrue(true);
                    }
                });
            }
        }
    }

    // 心愿池
    function WishListMode() {
        var $this = this;
        $this.wishDetailClick = function (productId,wishOrderId) {
            location.href = "/view/mobile/student/center/rewarddetail?new_page=blank&productId=" + productId + "&wishOrderId=" + wishOrderId;
        }
        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "ec5661");
        }
    }

    //点击打点
    $(document).on("click", "[data-logs]", function(){
        try {
            var $self = $(this);
            var $logsString = $self.attr("data-logs");
            var $logsItems = {};

            if($logsString != ""){
                var $logsJson = eval("(" + $logsString + ")");
                if($logsJson.m){ $logsItems.module = $logsJson.m; }
                if($logsJson.op){ $logsItems.op = $logsJson.op; }
                $logsItems.s0 = $logsJson.s0 || $self.attr('data-s0');
                $logsItems.s1 = $logsJson.s1 || $self.attr('data-s1');

                YQ.voxLogs($logsItems);
            }
        }catch (e){
            console.info(e);
        }
    });
    if (defaultInitMode) {
        defaultInitMode.nullContent = ko.observable();
        ko.applyBindings(defaultInitMode);
    }

    function getExternal(){
        var _WIN = window;
        if(_WIN['yqexternal']){
            return _WIN.yqexternal;
        }else if(_WIN['external']){
            return _WIN.external;
        }else{
            return _WIN.external = function(){};
        }
    }

});
