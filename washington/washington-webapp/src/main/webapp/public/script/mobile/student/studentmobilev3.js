define(['jquery', 'knockout','YQ', 'weui', 'voxLogs'], function ($, ko,yq) {
    var defaultInitMode;

    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'HomeworkMode':
                defaultInitMode = new HomeworkMode();// 作业模块
                break;
            case 'IntegralMode':
                defaultInitMode = new IntegralMode();//学豆
                break;
            case 'RewardMode':
                defaultInitMode = new RewardMode(); //奖励中心
                break;
            case 'LikeListMode':
                defaultInitMode = new LikeListMode(); //集赞
                break;
            case 'TalentMode':
                defaultInitMode = new TalentMode(); //成就达人
                break;
            case 'RewardDetailMode':
                defaultInitMode = new RewardDetailMode(); //成就达人
                break;
            default:
            //intiMode null
        }
    }

    // 作业模块
    function HomeworkMode() {
        var $this = this;

        $this.historyList = ko.observableArray([]);
        $this.chineseText = function (subject) {
            if (subject == "ENGLISH") {
                return "英语";
            }
            if (subject == "CHINESE") {
                return "语文";
            }
            if (subject == "MATH") {
                return "数学";
            }
        };

        $this.getHomework = function () {
            $.get('/studentMobile/homework/app/history.vpage?app_version=' + getAppVersion(), function (data) {
                if (data.success && data.homeworkHistory.length > 0) {
                    $this.historyList(mapToList(data.homeworkHistoryGroupByDay));
                }else{
                    $this.nullContent("还没有作业记录哦～");
                }
            });
        };

        $this.getHomework();

        $this.toDoHomework = function () {
            var $that = this;

            if ($that.homeworkCardSource) {
                var homework = {
                    homework_type: $that.subject,
                    homework_id: $that.homeworkId,
                    hw_card_source: $that.homeworkCardSource,//跳h5还是native
                    hw_card_variety: $that.homeworkCardVariety,//调用的go api
                    is_makeup: true
                };
                if (window.external && ('doHomework' in window.external)) {
                    window.external.doHomework(JSON.stringify(homework));
                } else {
                    $.alert('作业补做失败，请联系客服');
                }
            } else {
                if (window.external && ('reDoHomework' in window.external)) {
                    window.external.reDoHomework($that.homeworkId, $that.subject);
                } else {
                    $.alert('作业补做失败，请联系客服');
                }
            }
        }
    }

    //学豆
    function IntegralMode() {
        var $this = this;
        var originalData = [];

        $this.integralList = ko.observableArray();
        $this.templateBox = ko.observable("T:学豆列表");
        $this.database = ko.observable();
        $this.isShowLoading = ko.observable(true);

        $this.getIntegral = function (num, callback) {
            $.post('/studentMobile/center/integralchip.vpage', {
                pageNumber: num
            }, function (data) {
                if (data.success) {
                    originalData = originalData.concat(data.pagination.content);

                    if(originalData.length > 0){
                        $this.templateBox("T:学豆列表");
                        $this.database({
                            items : ko.observableArray(originalData)
                        });

                        if(data.pagination.content.length < 20){
                            $this.isShowLoading(false);
                        }
                    }else{
                        $this.templateBox("T:PageNull");
                        $this.database({info: data.info || "还没有获取学豆记录～"});
                    }
                }else{
                    $this.templateBox("T:PageNull");
                    $this.database({info: data.info || "还没有获取学豆记录～"});
                }
            });
        };

        $this.getIntegral(1);

        var loading = false;//状态标记;
        var pageNumber = 1;//page
        $(document.body).infinite().on("infinite", function() {
            if(loading || !$this.isShowLoading()) return;

            loading = true;
            setTimeout(function() {
                $this.getIntegral(pageNumber += 1);
                loading = false;
            }, 800);   //模拟延迟
        });

        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "40a455");
        }

        YQ.voxLogs({
            module: 'm_Reo8J9vz',
            op : 'wealth_get_records_click'
        });
    }

    //奖励中心
    function RewardMode() {
        var $this = this;

        $this.rows = ko.observableArray();
        $this.nullContent = ko.observable();
        $this.isCopyright = ko.observable(false);

        $this.getRewardInit = function (categoryId, pageNum) {
            if (categoryId == undefined){
                $(".selectDefault").attr("selected",true);
                $('.js-selectText').text("全部商品");
            }
            $.ajax({
                url: "/studentMobile/center/rewardList.vpage",
                type: "POST",
                data: {
                    categoryId: categoryId || 0,
                    pageNum: pageNum || 0
                },
                success: function (data) {
                    if (data.success) {
                        $this.rows(data.rows);
                    }

                    if($this.rows().length < 1){
                        $this.nullContent("没有发现这个类型的奖品");
                    }else{
                        $this.nullContent('')
                    }
                }
            });
        };

        $this.getShop = function (data) {
            $.alert('请到一起作业电脑端兑换该奖品');

            YQ.voxLogs({
                module: 'm_Reo8J9vz',
                op : 'anyone_prize_click',
                s0 : data.id
            });
        };

        $this.getTiYanShop = function (data) {
            YQ.voxLogs({
                module: 'm_Reo8J9vz',
                op : 'anyone_prize_click',
                s0 : data.id
            });

            setTimeout(function(){
                location.href = '/view/mobile/student/center/rewarddetail?productId=' + data.id;
            }, 200);
        };

        $this.getRewardInit();

        $(document).on('change', '.js-selectShop', function () {
            var $self = $(this);

            $this.getRewardInit($self.val());
            $self.siblings('.js-selectText').text($self.find('option:selected').text());
        });

        var u = navigator.userAgent;
        var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
        if(isiOS){
            $this.isCopyright(true);
        }

        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "40a455");
        }

        YQ.voxLogs({
            module: 'm_Reo8J9vz',
            op : 'page_prize_center_load_success'
        });
    }

    //集赞
    function LikeListMode() {
        var $this = this;

        $this.records = ko.observableArray();
        $this.total = ko.observable(0);
        $this.likerCount = ko.observable(0);
        $this.getListInit = function () {
            $.ajax({
                url: "/studentMobile/center/like/list.vpage",
                type: "GET",
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        $this.records(data.records);
                        if(data.total)$this.total(data.total);
                        if(data.likerCount)$this.likerCount(data.likerCount);
                    }
                }
            });
        };

        $this.getListInit();

        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "91d8ff");
        }

        YQ.voxLogs({
            module: 'm_yCsm37yS',
            op : 'page_like_information_load_success'
        });
    }

    //成就达人
    function TalentMode() {
        var $this = this;
        $this.type = getQueryString("type");
        $this.level = getQueryString("level");
        $this.count = ko.observable(0);
        $this.condition = ko.observable(0);
        //database
        $this.main = ko.observable("T:今日达人");
        $this.database = ko.observable();
        $this.clickLike = function (data, event) {
            var $data = data;
            var $likeBtn = $(event.currentTarget);

            if (!$likeBtn.hasClass("praised")) {
                return;
            }

            innerAjax({
                url: '/studentMobile/rank/like.vpage',
                type: 'POST',
                data: {
                    likedUserId: $data.userId,
                    type: "ACHIEVEMENT_RANK",
                    level: data.level,
                    achievementType: data.type
                },
                success: function (data) {
                    if (data.success) {
                        $likeBtn.removeClass("praised").text($data.likeCount += 1);
                    } else {
                        $.alert(data.info);
                    }
                }
            });
        };

        $this.getListInit = function () {
            $.ajax({
                url: "/studentMobile/achievement/clazz/rank.vpage",
                type: "POST",
                data: {
                    type: $this.type,
                    level: $this.level
                },
                success: function (data) {
                    if (data.success && data.achievements && data.achievements.length > 0) {
                        $this.database(data.achievements);
                        $this.count(data.achievements.length);
                        $this.condition(data.condition);
                        if (getExternal()["updateTitle"]) {
                            getExternal().updateTitle(data.achievements[0].title, "ffffff", "50bcfa");
                        }
                    } else {
                        $this.main("T:PageNull");
                        $this.database({info: '暂时还没有达人～'});
                    }
                }
            });
        };

        $this.getListInit();
    }

    // 单个奖品 详情页
    function RewardDetailMode(){
        var $this = this;
        $this.orderTrue = ko.observable(true);
        $this.orderWishTrue = ko.observable(true);
        $this.orderDeWishTrue = ko.observable(true);
        $this.wishOrderId = ko.observable();
        $this.productId = ko.observable();
        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "eadfc1");
        }
        if (dataNum.categories == "MINI_COURSE") {
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op : "page_class_load_success"
            });
        }
        // 点击兑换并使用
        $this.shopSubmit = function(productId){
            $("#dressUpPop").show();
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op : "button_exchange_and_dress_click",
                s0: productId
            });
        };
        // 点击 关闭按钮
        $this.closePop = function(){
            $("#dressUpPop").hide();
        }
        //点击 我要学习
        $this.videoSubmit = function (productId) {
            $("#videoUpPop").show();
            YQ.voxLogs({
                module: "m_Reo8J9vz",
                op : "button_class_confirm_click",
                s0: productId
            });
        }
        
        $this.couponDetail = function (productId) {
            location.href = '/view/mobile/student/center/coupondetail?&productId='+productId+'&new_page=blank';
        }

        $this.closeChangeCouponPop = function () {
            $("#changeCouponPop").hide();
        }

        //兑换优惠券
        $this.changeCoupon = function () {
            $("#changeCouponPop").show();
        }

        $this.sureChangeCouponClick = function () {
            $.post('/reward/order/exchangedcoupon.vpage',{
                ignoreMobile:true,
                productName:dataNum.productName
            },function (res) {
                if(res.success){
                    $("#changeCouponPop").hide();
                    setTimeout(function(){
                        $("#successStudy").show();
                    },300)
                }else{
                    $.alert(res.info);
                }
            });
        }
        // 点击开始学习按钮
        $this.courseVideoClick = function (videoUrl,category) {
            var locHref;
            if (category == "CHOICEST_ARTICLE"){
                locHref = videoUrl;
            }else{
                locHref = "/view/mobile/student/center/prizevideos?new_page=blank&videoUrl=" + videoUrl;
            }
            location.href = locHref;
        }
        $this.goDetailClick = function () {
            location.href = "/view/mobile/student/center/energybox?new_page=blank";
        }
        // 点击 加入心愿池
        $this.addWishClick = function (productId,op) {
            if ($this.orderWishTrue()) {
                $this.orderWishTrue(false);
                $.ajax({
                    url: "/reward/order/addwishorder.vpage",
                    type: "POST",
                    data: {
                        productId:productId
                    },
                    success: function (data) {
                        if (data.success){
                            setTimeout(function(){
                                $(".tips").fadeIn('100')
                                setTimeout(function () {
                                    location.reload();
                                },1000)
                            },100)
                            YQ.voxLogs({
                                module: "m_Reo8J9vz",
                                op : op,
                                s0: productId
                            });
                        }else{
                            $.alert(data.info)
                            $this.orderWishTrue(true);
                        }
                    }
                });
            }
        }
        $this.dialogDefaultClick = function () {
            $(".dialog_show").fadeOut(150);
        }
        // 点击 移出心愿池
        $this.delectWishClick = function (productId,wishOrderId) {
            $this.wishOrderId(wishOrderId);
            $this.productId(productId);
            $(".dialog_show").fadeIn(150);
        };
        $this.dialogPrimaryClick = function () {
            if ($this.orderDeWishTrue()) {
                $this.orderDeWishTrue(false);
                $.ajax({
                    url: "/reward/order/removewishorder.vpage",
                    type: "POST",
                    data: {
                        productId:$this.productId(),
                        wishOrderId:$this.wishOrderId()
                    },
                    success: function (data) {
                        if (data.success){
                            $(".dialog_show").fadeOut(150);
                            $.toast("已移出心愿池！");
                            setTimeout(function () {
                                location.reload();
                            },500)
                        }else{
                            $.alert(data.info)
                            $this.orderDeWishTrue(true);
                        }
                    }
                });
            }
        }
        // 点击 我要兑换 确定按钮
        $this.WantUsedClick = function (){
            var locationOrder = yq.getQuery("wishOrderId");
            if ($this.orderTrue()){
                $this.orderTrue(false);
                var url = "/reward/order/createorder.vpage";
                var data = {
                    productId:dataNum.productid ,
                    skuId:dataNum.skuid ,
                    quantity: '1',
                    source:'app'
                }
                if (location.href.indexOf("wishOrder") > -1 ){
                    url = "/reward/order/achievewishorder.vpage";
                    data = {
                        productId:dataNum.productid ,
                        skuId:dataNum.skuid ,
                        quantity: '1',
                        wishOrderId:locationOrder
                    }
                }
                $.ajax({
                    url: url,
                    type: "POST",
                    data: data,
                    success: function (data) {
                        if (data.success){
                            $("#dressUpPop").hide();
                            $.toast("兑换成功！");
                            setTimeout(function(){
                                location.href="/view/mobile/student/center/reward?new_page=blank";
                            },500)
                            YQ.voxLogs({
                                module: "m_Reo8J9vz",
                                op: "button_confirm_exchange_click",
                                s0: dataNum.productid
                            });
                        }else{
                            setTimeout(function(){
                                $.alert(data.info)
                            },500)
                            $this.orderTrue(true);
                        }
                    }
                });
            }
        }
        //点击 我要学习
        $this.WantStudyClick = function () {
            var locationOrder = yq.getQuery("wishOrderId");
            if ($this.orderTrue()){
                $this.orderTrue(false);
                var url = "/reward/order/createorder.vpage";
                var data = {
                    productId:dataNum.productid ,
                    skuId:dataNum.skuid ,
                    quantity: '1',
                    source:'app'
                }
                if (location.href.indexOf("wishOrder") > -1 ){
                    url = "/reward/order/achievewishorder.vpage";
                    data = {
                        productId:dataNum.productid ,
                        skuId:dataNum.skuid ,
                        quantity: '1',
                        wishOrderId:locationOrder
                    }
                }
                $.ajax({
                    url: url,
                    type: "POST",
                    data: data,
                    success: function (data) {
                        if (data.success){
                            $("#videoUpPop").hide();
                            setTimeout(function(){
                                $("#successStudy").show();
                            },500)
                            YQ.voxLogs({
                                module: "m_Reo8J9vz",
                                op: "button_confirm_study_click",
                                s0: dataNum.productid
                            });
                        }else{
                            $.alert(data.info)
                            $this.orderTrue(true);
                        }
                    }
                });
            }
        }
        // 点击 关闭按钮
        $this.closeStudyPop = function(){
            $("#videoUpPop").hide();
        }
        $this.closeStudy2Pop = function () {
            $("#successStudy").hide();
            setTimeout(function(){
                location.reload()
            },500)
        }
        $this.shopDressOn = function (productId) {
            $.ajax({
                url: "/studentMobile/center/headwear/change.vpage",
                type: "POST",
                data: {
                    headWearId:dataNum.headWearId
                },
                success: function (data) {
                    if (data.success){
                        YQ.voxLogs({
                            module: "m_Reo8J9vz",
                            op: "button_change_dress_click",
                            s0: productId
                        });
                        $.toast("装扮成功！")
                        setTimeout(function(){
                            location.href="/view/mobile/student/center/reward?new_page=blank";
                        },500)
                    }else{
                        $.alert(data.info)
                    }
                }
            });
        }
    }

    function ClazzAchievement() {
        var $this = this;
        var clazzMapItems = {
            pageNumber : ko.observable(11),
            menuType : ko.observable(2),
            originalItems : ko.observable(),
            items : ko.observableArray()
        };

        $this.main = ko.observable("T:班级成就");
        $this.database = ko.observable();
        $this.getClazzList = function () {
            if(clazzMapItems.items().length < 1){
                $.ajax({
                    url: "/studentMobile/achievement/wall.vpage",
                    type: "GET",
                    data: {},
                    success: function (data) {
                        if (data.success) {
                            clazzMapItems.originalItems(komapping.fromJS(data.achievements));

                            for(var i in data.achievements){
                                clazzMapItems.items(clazzMapItems.items().concat(i));
                            }
                        }

                        if(clazzMapItems.items().length < 1){
                            $this.main("T:PageNull") ;
                            $this.database({info: "还没有获得过个人成就！"});
                        }else{
                            $this.main("T:班级成就");
                            $this.database(clazzMapItems);
                        }
                    }
                });
            }else{
                $this.main("T:班级成就");
                $this.database(clazzMapItems);
            }
        };

        $this.getClazzList();

        $this.viewDetailItem = ko.observable({type: ""});

        $this.clickViewDetail = function(data, event){
            $this.viewDetailItem(data);
        };

        $this.closeView = function(data, event){
            $this.viewDetailItem({type: ""});
        };
    }

    if (defaultInitMode) {
        defaultInitMode.nullContent = ko.observable();

        ko.applyBindings(defaultInitMode);
    }

    //获取App版本
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }

    function getAppVersion() {
        var appVersion = "";

        if (typeof(app_version) != "undefined" && app_version != "") {
            return app_version;
        }

        if (getExternal()["getInitParams"]) {
            var $params = getExternal().getInitParams();

            if ($params) {
                $params = $.parseJSON($params);
                appVersion = $params.native_version;
            }
        } else {
            appVersion = getQueryString("app_version") || '';
        }

        return appVersion;
    }

    function mapToList(map) {
        var $arr = [];
        for (var i in map) {
            $arr.push({
                day: i,
                list: map[i]
            })
        }

        return $arr;
    }

    function innerAjax(opt) {
        /*opt = { url : '', data : {},   type : "GET", success : function(){}, error : function(){} };*/
        $.ajax({
            url: opt.url,
            type: opt.type || 'GET',
            data: opt.data || {},
            success: function (data) {
                if (opt.success)opt.success(data);
            },
            error: function (data) {
                if (opt.error)opt.error(data);
            }
        });
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
    //点击打点
    $(document).on("click", "[data-logs]", function(){
        try {
            var $self = $(this);
            var $logsString = $self.attr("data-logs");
            var $logsItems = {};

            if($logsString != ""){
                var $logsJson = eval("(" + $logsString + ")");
                // m : $logsJson.m, op: $logsJson.op, s0:$logsJson.s0, s1 : $logsJson.s1
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

});
