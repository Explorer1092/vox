<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="加载中..."
requireFlag=false
fastClickFlag=false
>
<style>
    html, body{ padding: 0; margin: 0; height: 100%; width: 100%;}
    .main-content{height: 100%; width: 100%; background: url(<@app.link href="public/skin/common/images/loading-app.gif"/>) no-repeat center center; background-size: 20%;}
</style>
<div class="main-content">
<#--content-->
</div>
<script type="text/javascript">
    function getQuery(key) {
        var reg = new RegExp("(^|&)" + key + "=([^&]*)(&|$)"),
                res = window.location.search.substr(1).match(reg);
        return res != null ? decodeURIComponent(res[2]) : null;
    }
    // 判断是直接打开(例如从app首屏)还是从页面(例如从活动页)打开
    // 从页面打开则不关闭当前webview，且使用history.back关掉中间页
    // 直接打开则关闭当前webview，退到最外面
    var isAndroid = /android/.test(window.navigator.userAgent.toLowerCase());
    var isReserve = (history.length > 1);

    // 兼容安卓page_viewable为false时无法打开应用
    if(isAndroid && !isReserve){
        isReserve = true;
        window.vox={
            task: {
                refreshData: function(){
                    if(window.external && window.external.disMissView){
                        window.external.disMissView();
                    }
                }
            }
        };
    }

    var ua = window.navigator.userAgent.toLowerCase(),
            isParent = /17parent/.test(ua);

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

    //是否带http
    function hasUrlHttp(url){
        if(url.substr(0,7) == "http://" || url.substr(0,8) == "https://"){
            return url;
        }
        return window.location.origin + url;
    }

    var productType = "${productType!}";
    var pageAppKey = "${(appKey)!}";

    function AutoOpenApp(){
        if(productType == 'INNER_APPS'){
            getExternal()["innerJump"](JSON.stringify({
                name: pageAppKey.toLowerCase(),
                page_viewable: isReserve
            }));
            setTimeout(function(){
                window.history.back();
            }, 500);
        }else {
            if (getExternal()["openFairylandPage"]) {
                getExternal().openFairylandPage(JSON.stringify({
                    url: hasUrlHttp("${url!''}"),
                    name: "fairyland_app:" + "${appKey!'link'}",
                    useNewCore: "${browser!'system'}",
                    orientation: "${orientation!'landscape'}",
                    initParams: JSON.stringify({hwPrimaryVersion:"V2_4_0"}),
                    page_viewable:isReserve
                }));

                setTimeout(function(){
                    window.history.back();
                }, 500);
            }else{
                location.href = hasUrlHttp("${url!''}");
            }
        }
    }

    window.onload=function(){
        // 家长端错题精讲走此路
        if(isParent && pageAppKey == 'FeeCourse' && getExternal()["openSecondWebview"]){
            getExternal().openSecondWebview(JSON.stringify({
                shareType: 'NO_SHARE_VIEW',
                shareContent: '',
                shareUrl: '',
                type: '',
                url: '/app/redirect/jump.vpage?appKey=FeeCourse&platform=PARENT_APP&productType=APPS'
            }));
            // 不保留则关闭当前webview
            if(!isReserve){
                getExternal()["disMissView"] && getExternal().disMissView();
            }
        }else{
            setTimeout(function(){
                AutoOpenApp();
            }, 500);
        }
    };
</script>
</@layout.page>