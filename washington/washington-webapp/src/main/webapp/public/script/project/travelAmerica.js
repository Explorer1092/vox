define(["jquery", "voxLogs"], function ($) {
    $(".tab-head").children(".item").on("click", function () {
        $(this).children().addClass("active");
        $(this).siblings().children().removeClass("active");
    });
    var native_version = "";
    if (window["external"] && window.external["getInitParams"]) {
        var $params = window.external.getInitParams();
        if ($params) {
            $params = $.parseJSON($params);
            native_version = $params.native_version;
        }
    }
    function getExternal() {
        var _WIN = window;
        if (_WIN['yqexternal']) {
            return _WIN.yqexternal;
        } else if (_WIN['external']) {
            return _WIN.external;
        } else {
            return _WIN.external = function () {};
        }
    }

    var hasUrlHttp = function (url) {
        if (url.substr(0, 7) == "http://" || url.substr(0, 8) == "https://") {
            return url;
        }
        return window.location.origin + url;
    };

    //获取App版本
    function getAppVersion() {
        var native_version = "";

        if (window["external"] && window.external["getInitParams"]) {
            var $params = window.external.getInitParams();

            if ($params) {
                $params = $.parseJSON($params);
                native_version = $params.native_version;
            }
        }

        return native_version;
    }

    //App版本 Android:>=2.6.6 , IOS : IOS>=2.7.0
    function versionValidate() {
        var native_version = getAppVersion(),
            version = native_version.split('.'),
            part1 = parseInt(version[0]),
            part2 = parseInt(version[1]),
            part3 = parseInt(version[2]);

        var u = navigator.userAgent;
        var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
        if(isiOS){  //IOS>=2.7.0
            if (part1 > 2) {
                return true;
            }
            else if (part1 == 2 && part2 > 7) {
                return true;
            }
            else if (part1 == 2 && part2 == 7 && part3 >= 0) {
                return true;
            }
        }else{      //Android>2.6.6
            if (part1 > 2) {
                return true;
            }
            else if (part1 == 2 && part2 > 6) {
                return true;
            }
            else if (part1 == 2 && part2 == 6 && part3 >= 6) {
                return true;
            }
        }

        return false;
    }
    var isHighVersion = versionValidate();

    $(".js-submit").on("click", function () {
        if(!isHighVersion){
            location.href = "/studentMobile/teacherDay/bless/update.vpage";
        }else{
            var $this = $(this);
            if ($this.attr('data-goaltype') == null ) {
                $.post("/student/activity/usaadventure/newtermactivity/receivegift.vpage", {
                    learningGoalType: $(".active").attr('data-type')
                }, function (res) {
                    if (res.success) {
                        $(".selectBox").addClass("disabled");

                        $this.attr('data-goaltype', $(".active").attr('data-type'));

                        gameOpen($this);
                    }
                });
            }else{
                gameOpen($this);
            }
        }
    });

    function gameOpen(id){
        if (getExternal()["openFairylandPage"]) {
            getExternal().openFairylandPage(JSON.stringify({
                url: hasUrlHttp(id.attr('data-url') + "&version=" + native_version),
                name: "fairyland_app:" + (id.attr('data-appkey') || "link"),
                useNewCore: id.attr('data-browser') || "system",
                orientation: id.attr('data-orientation') || "sensor"
            }));
        }else{
            location.href = "/studentMobile/teacherDay/bless/update.vpage";
            YQ.voxLogs({
                module: 'america-app-activity',
                op : 'error'
            });
        }
    }

    YQ.voxLogs({
        module: 'america-app-activity',
        op : 'load'
    });
});