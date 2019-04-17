define(['jquery', 'knockout', 'weui', 'voxLogs'], function ($, ko) {
    var defaultInitMode;

    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'MyLevelMode':
                defaultInitMode = new MyLevelMode();//我的等级
                break;
            case 'GrowingUpRaiders':
                defaultInitMode = new GrowingUpRaiders();//成长攻略
                break;
            default:
            //intiMode null
        }
    }

    function MyLevelMode() {
        var $this = this;
        $this.rewards = ko.observableArray();
        $this.privileges = ko.observableArray([{privilegeId: "001", "name": "默认", imgUrl: "", type: "", current: true}]);
        $this.rankList = ko.observableArray();
        $this.growthLevel = {
            title: ko.observable("初级种子"),
            level: ko.observable(1),
            barCount : ko.observable(0),
            current: ko.observable(1),
            next: ko.observable(1)
        };

        $this.levelMapJson = function(i){
            if(i <= 5){
                return '1';
            }else if(i > 5 && i <= 10){
                return '2';
            }else if(i > 10 && i <= 15){
                return '3';
            }else if(i > 15 && i <= 20){
                return '4';
            }else if(i > 20 && i <= 25){
                return '5';
            }else if(i > 25){
                return '6';
            }else{
                return '1';
            }
        };

        $this.getListInit = function (opt, event) {
            //成长等级
            $.ajax({
                url: "/studentMobile/growth/levelinfo.vpage",
                type: "GET",
                success: function (data) {
                    if (data.success) {
                        if (data.title) {
                            $this.growthLevel.title(data.title);
                        }
                        if (data.level) {
                            $this.growthLevel.level(data.level)
                        }
                        if (data.title) {
                            $this.growthLevel.title(data.title)
                        }
                        if (data.current) {
                            $this.growthLevel.current(data.current)
                        }
                        if (data.next) {
                            $this.growthLevel.next(data.next)
                        }

                        if(data.current && data.next){
                            $this.growthLevel.barCount( (data.current/data.next*100).toFixed(2) );
                        }
                    } else {
                        $.alert(data.info);
                    }
                }
            });

            //升级礼包
            $.ajax({
                url: "/studentMobile/growth/reward.vpage",
                type: "GET",
                success: function (data) {
                    if (data.success) {
                        $this.rewards(data.rewards);
                    }
                }
            });

            //等级排行榜
            $.ajax({
                url: "/studentMobile/growth/clazz/rank.vpage",
                type: "GET",
                data: "",
                success: function (data) {
                    if (data.success) {
                        $this.rankList(data.rank);
                    }
                }
            });

            //我的特权
            $.ajax({
                url: "/studentMobile/center/privilege.vpage",
                type: "get",
                data: "",
                success: function (data) {
                    if(data.success){
                        $this.privileges($this.privileges().concat(data.privileges));
                    }
                }
            });
        };

        $this.getListInit();

        YQ.voxLogs({
            module: 'm_6ekgjcuw',
            op : 'page_grade_information_load_success'
        });
    }

    ///studentMobile/growth/record.vpage
    function GrowingUpRaiders() {
        var $this = this;

        $this.recordList = ko.observableArray();

        $this.levelRecord = function () {
            $.ajax({
                url: "/studentMobile/growth/record.vpage",
                type: "GET",
                data: "",
                success: function (data) {
                    if (data.success) {
                        $this.recordList(data.records);
                    }
                }
            });
        };

        $this.levelRecord();
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

                $logsItems.s0 = $logsJson.s0 || $self.attr('data-s0') || "";
                $logsItems.s1 = $logsJson.s1 || $self.attr('data-s1') || "";

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

    //设置title
    if (getExternal()["updateTitle"]) {
        getExternal().updateTitle(document.title, "ffffff", "91d8ff");
    }
});