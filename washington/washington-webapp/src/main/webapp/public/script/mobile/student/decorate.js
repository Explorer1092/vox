define(['jquery', 'knockout', 'weui', 'voxLogs'], function ($, ko) {
    var defaultInitMode;

    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'RewardMode':
                defaultInitMode = new RewardMode();
                break;
            case 'PrivilegeMode':
                defaultInitMode = new PrivilegeMode();
                break;
            default:
            //intiMode null
        }
    }

    /*升级礼包*/
    function RewardMode() {
        var $this = this;
        $this.reward = ko.observableArray();
        $this.headWearId = ko.observable();
        $this.popupBox = $('#js-rewardFlayer');

        /*领取饰品*/
        $this.clickPopup = function (opt, event) {
            if($(event.currentTarget).hasClass("w-getBtn-disabled")){
                return false;
            }
            $.ajax({
                url: "/studentMobile/growth/reward/receive.vpage",
                type: "post",
                data: {level: opt.level},
                success: function (data) {
                    if (data.success) {
                        $this.headWearId(opt.headWearId);
                        $this.popupBox.show();

                        $(event.currentTarget).addClass("w-getBtn-disabled").text("已领取");

                        YQ.voxLogs({
                            module: 'm_6ekgjcuw',
                            op : 'grade_prize_collect_success',
                            s0 : opt.headWearId.toString()
                        });

                        if (getExternal()['sendNotification']) {
                            getExternal().sendNotification(30003);
                        }
                    } else {
                        $.alert(data.info);

                        YQ.voxLogs({
                            module: 'm_6ekgjcuw',
                            op : 'grade_prize_collect_error',
                            s0 : encodeURI(data.info)
                        });
                    }
                }
            })
        };

        /*立即使用*/
        $this.clickUse = function () {
            if ($this.headWearId) {
                $.ajax({
                    url: "/studentMobile/center/headwear/change.vpage",
                    type: "post",
                    data: {headWearId: $this.headWearId()},
                    success: function (data) {
                        if (data.success) {
                            if (getExternal()['sendNotification']) {
                                getExternal().sendNotification(30000);
                            }
                        } else {
                            $.alert(data.info);
                        }
                        $this.popupBox.hide();
                    }
                });
            } else {
                $.alert("数据出了点小问题");
            }
        };

        /*关闭弹窗*/
        $this.clickClose = function () {
            $this.popupBox.hide();
        };

        $.ajax({
            url: "/studentMobile/growth/rewards.vpage",
            type: "get",
            success: function (data) {
                $this.reward(data.rewards);
            }
        });

        YQ.voxLogs({
            module: 'm_6ekgjcuw',
            op : 'page_grade_prize_load_success'
        });
    }

    /*我的饰品*/
    function PrivilegeMode() {
        var $this = this;

        $this.nullContent = ko.observable();
        $this.privileges = ko.observableArray();
        $this.notOwned = ko.observableArray();
        $this.selectPrivilege = ko.observable();
        $this.currentTarget = ko.observable();
        $this.type = ko.observable('default');
        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "46b654");
        }
        $this.clickPopup = function (arg, event) {
            if (!arg.effective && arg.leftValidTime>-1) {
                if(arg.origin == "CREDIT_MALL") {
                    location.href = "/view/mobile/student/wonderland/credit";
                    return false;
                }else {
                    location.href = "/view/mobile/student/center/rewarddetail?new_page=blank&productId=" + arg.relateProductId;
                    return false;
                }
            }
            $this.selectPrivilege(arg);
            $this.currentTarget(event.currentTarget);
            $this.type('default');
            $("#js-popupBox").show();
        };
        $this.clickDown = function (arg, event) {
            // var dataPrivilege = ["hw1","hw2","hw3","hw4","hw5","hw6"];
            // if (dataPrivilege.indexOf(arg.privilegeId) == -1 && arg.relateProductId != null){
            //     location.href = "/studentMobile/center/rewarddetail.vpage?productId=" + arg.relateProductId;
            //     return false;
            // }

            $this.selectPrivilege(arg);
            $this.currentTarget(event.currentTarget);
            $this.type('notOwned');
            $("#js-popupBox").show();
        };
        $this.gotoMore = function () {
            location.href="/studentMobile/center/reward.vpage?new_page=blank";
        }
        $this.clickUse = function () {
            if ($this.selectPrivilege()) {
                $.ajax({
                    url: "/studentMobile/center/headwear/change.vpage",
                    type: "post",
                    data: {headWearId: $this.selectPrivilege().privilegeId},
                    success: function (data) {
                        if (data.success) {
                            YQ.voxLogs({
                                module: 'm_Reo8J9vz',
                                op : 'button_my_dress_confirm_click',
                                s0: $this.selectPrivilege().relateProductId
                            });
                            $this.getInitList();
                            if (window['external'] && window.external['sendNotification']) {
                                window.external.sendNotification(30000);
                            }
                        } else {
                            $.alert(data.info);
                        }
                        $this.clickClose();
                    }
                });
            } else {
                $.alert("数据出了点小问题");
            }
        };

        $this.clickClose = function () {
            $("#js-popupBox").hide();
            $(".achievement-pop").hide();
        };

        $this.getInitList = function(){
            $.ajax({
                url: "/studentMobile/center/privilege.vpage",
                type: "get",
                data: {
                    type: 'Head_Wear'
                },
                success: function (data) {
                    /*data.privileges = [
                        {privilegeId: "hw1", "name": "塔克天团", imgUrl: "hw2.png", type: "", current: false},
                        {privilegeId: "hw2", "name": "樱桃小丸子", imgUrl: "hw3.png", type: "", current: false}
                    ];*/
                    if (data.success) {
                        var privilegesSetFlag = true;
                        var privilegesSetValid = true;
                        for(var i = 0; i < data.privileges.length; i++){
                            if(data.privileges[i].current){
                                privilegesSetFlag = false;
                                break;
                            }
                        }
                        // for(var i = 0; i < data.privileges.length; i++){
                        //     if(data.privileges[i].effective){
                        //         privilegesSetValid = false;
                        //         break;
                        //     }
                        // }

                        var privilegesDefault = [{privilegeId: 'default', "name": "默认", imgUrl: "hw2.png", type: "",origin:"EXCHANGE", current: privilegesSetFlag,effective:privilegesSetValid,leftValidTime:-2,relateProductId:""}];

                        $this.privileges(privilegesDefault.concat(data.privileges));
                        $this.notOwned(data.notOwned || privilegesDefault);
                    }
                }
            })
        };

        $this.getInitList();
    }

    if (defaultInitMode) {
        ko.applyBindings(defaultInitMode);
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