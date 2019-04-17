define(['jquery', 'weui', 'voxLogs'], function ($, ko) {
    //规则操作
    $(document).on("click", "#js-clickPopup", function () {
        $("#js-rules").show();
    });
    $(document).on("click", "#js-closeRules", function () {
        $("#js-rules").hide();
    });

    var activeLevel = 1;
    var activeId;
    var selected;
    //tab切换
    $(document).on("click", ".js-clickTab", function () {
        activeLevel = parseInt($(this).attr("data-level"));
        $(this).addClass("active").siblings().removeClass("active");
        var $listBox = $(".js-list");
        var $curBox = $(".js-list[dataLevel = " + activeLevel + "]");
        var $info = $(".js-info[dataLevel = " + activeLevel + "]");
        $info.show().siblings('.js-info').hide();
        if ($curBox.length > 0) {
            $(".js-noneTips").hide();
            $listBox.hide();
            $curBox.show();
        } else {
            $listBox.hide();
            $(".js-noneTips").show();
        }
    });
    $(".js-clickTab:first").click();

    //选择唤醒
    $(document).on("click", "li.towake", function () {
        $("li.towake").removeClass("active");
        $(this).addClass("active");
        activeId = $(this).attr("dataId");
        activeLevel = $(this).parent().attr("dataLevel");
        selected = $(this);
    });

    //发出唤醒
    $(document).on("click", "#js-towake", function () {
        if ($(this).hasClass("disabled")) {
            return false;
        }

        if(activeId == null || activeId == ''){
            $.alert("请选择同学");
            return false;
        }

        if(waterCount > 0){
            pushLoadActive();
        }else{
            $.confirm("是否消耗5学豆对同学发出唤醒", function() {
                pushLoadActive();
            }, function() {
                //取消操作
            });
        }
    });

    function pushLoadActive(){
        $.post("/student/magic/active.vpage", {activeId: activeId, activeLevel: activeLevel, source: 'mobile'}, function (data) {
            if (data.success) {
                $.alert("成功发出唤醒~<br/>快去当面提醒同学做作业吧~");
                activeId = "";
                selected.remove();

                //列表为空
                if(activeLevel != "" && $('.js-list[datalevel="'+ activeLevel +'"] li').length < 1){
                    $('.js-noneTips').show();
                }

                if(waterCount > 0){
                    waterCount -= 1;

                    $(".JS-waterCountBox").text(waterCount);
                }
            } else {
                $.alert(data.info);
            }
        });
    }

    //点击打点
    $(document).on("click", "[data-logs]", function(){
        var $this = $(this);
        var $logsString = $this.attr("data-logs");
        var $logsItems = {
            module: 'm_L1A9uQwp'
        };

        if($logsString != ""){
            var $logsJson = eval("(" + $logsString + ")");
            // m : $logsJson.m, op: $logsJson.op, s0:$logsJson.s0, s1 : $logsJson.s1
            if($logsJson.m){ $logsItems.module = $logsJson.m; }
            if($logsJson.op){ $logsItems.op = $logsJson.op; }
            if($logsJson.s0){ $logsItems.s0 = $logsJson.s0; }
            if($logsJson.s1){ $logsItems.s1 = $logsJson.s1; }

            YQ.voxLogs($logsItems);
        }
    });

    var pathName = location.pathname;
    if(pathName == "/studentMobile/magic/index.vpage"){
        YQ.voxLogs({
            module: 'm_L1A9uQwp',
            op: 'o_KRUpcETF'
        });
    }

    if(pathName == "/studentMobile/magic/active.vpage"){
        YQ.voxLogs({
            module: 'm_L1A9uQwp',
            op: 'o_MJfqAZjx'
        });
    }

    //设置title
    if (getExternal()["updateTitle"]) {
        getExternal().updateTitle(document.title, "ffffff", "50bcfa");
    }

    function getExternal() {
        var _WIN = window;
        if (_WIN['yqexternal']) {
            return _WIN.yqexternal;
        } else if (_WIN['external']) {
            return _WIN.external;
        } else {
            return _WIN.external = function () {
            };
        }
    }

    //时间提醒
    function runtime() {
        $(".js-time").each(function () {
            var myDate = new Date();
            var curDate = myDate.getTime();//当前日期时间戳
            var endDate = new Date($(this).attr("endDate")).getTime();//截止日期时间戳
            curDate += 1000;
            var t = endDate - curDate;
            var d = Math.floor(t / 1000 / 60 / 60 / 24);
            var h = Math.floor(t / 1000 / 60 / 60 % 24);
            var m = Math.floor(t / 1000 / 60 % 60);
            var s = Math.floor(t / 1000 % 60);
            if (s < 0) {
                return false;
            }
            if (d < 10) {
                d = "0" + d;
            }
            if (h < 10) {
                h = "0" + h;
            }
            if (m < 10) {
                m = "0" + m;
            }
            if (s < 10) {
                s = "0" + s;
            }
            $(this).find(".hour i").eq(0).html((h + "").substring(0, 1));
            $(this).find(".hour i").eq(1).html((h + "").substring(1, 2));
            $(this).find(".minute i").eq(0).html((m + "").substring(0, 1));
            $(this).find(".minute i").eq(1).html((m + "").substring(1, 2));
            $(this).find(".second i").eq(0).html((s + "").substring(0, 1));
            $(this).find(".second i").eq(1).html((s + "").substring(1, 2));
        });
    }

    runtime();
    setInterval(runtime, 1000);
});