<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='期末总复习'
pageJs=["jquery", "voxLogs"]
pageCssFile={"beanreward" : ["public/skin/mobile/student/app/activity/coolrun/css/skin"]}
>
<div class="kpf-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/coolrun/images/kpf-bg01.jpg"/>"></div>
<div class="kpf-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/coolrun/images/kpf-bg02.jpg"/>"></div>
<div class="kpf-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/coolrun/images/kpf-bg03.jpg"/>"></div>
<div class="kpf-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/coolrun/images/kpf-bg04.jpg"/>"></div>
<div class="kpf-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/coolrun/images/kpf-bg05.jpg"/>"></div>
<div class="kpf-bg"><img src="<@app.link href="/public/skin/mobile/student/app/activity/coolrun/images/kpf-bg06.jpg"/>"></div>
<div class="kpf-footer">
    <div class="kpf-btnBox">
        <span class="txt">现在就来学习吧！</span>
        <a href="javascript:void(0)" class="btn doClickOpenGame">立即体验</a>
    </div>
</div>
<script type="text/javascript">
signRunScript = function () {
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
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }
    //是否带http
    function hasUrlHttp(url){
        if(url.substr(0,7) == "http://" || url.substr(0,8) == "https://"){
            return url;
        }

        return window.location.origin + url;
    }

    $(document).on("click", ".doClickOpenGame", function () {
        var url = "${url}",
            browser = "${useNewCore!}",
            orientation = "${orientation!}";
            
        if(getQueryString('pagegoto') == 'no' && getExternal()["pageQueueNew"]) {
            getExternal().pageQueueNew(JSON.stringify({
                url:  hasUrlHttp(url),
                name: "fairyland_app:GreatAdventure",
                useNewCore: browser || "system",
                orientation: orientation || "sensor",
                initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
            }));
        }else if ( getExternal()["openFairylandPage"]) {
            getExternal().openFairylandPage(JSON.stringify({
                url: hasUrlHttp(url),
                name: "fairyland_app:GreatAdventure",
                useNewCore: browser || "system",
                orientation: orientation || "sensor",
                initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
            }));
        }else {
            //打不开酷跑学单词
            YQ.voxLogs({
                module: 'fairyland_app',
                op: 'error',
                s0 : 'App openURL = ' + url
            });
        }
    });
};
</script>
</@layout.page>