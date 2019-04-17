<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="加载中..."
pageJs=['jquery', 'YQ', 'versionCompare']
fastClickFlag=false
>
<style>
    html, body{ padding: 0; margin: 0; height: 100%; width: 100%;}
    .main-content{height: 100%; width: 100%; background: url(<@app.link href="public/skin/common/images/loading.png"/>) no-repeat center center; background-size: 50%;}
</style>
<div class="main-content">
<#--content-->
</div>
<script type="text/javascript">
    signRunScript = function ($, YQ, versionCompare) {
        var isAndroid = window.navigator.userAgent.toLowerCase().indexOf('android') > -1;
        var appKey = YQ.getQuery("appKey");
        var supportVersion = isAndroid ? YQ.getQuery("androidVersion") : YQ.getQuery("iosVersion");
        function AutoOpenApp(){
            if (versionCompare(YQ.getAppVersion(), supportVersion) > -1) {
                if(YQ.getQuery('pagegoto') == 'yes' && YQ.getQuery('screen') == 'all' && YQ.getQuery('url') != ''){
                    if (YQ.getExternal()["openFairylandPage"]) {
                        YQ.getExternal().openFairylandPage(JSON.stringify({
                            url: hasUrlHttp(YQ.getQuery('url')),
                            name: "fairyland_app:" + (appKey || "link"),
                            useNewCore: YQ.getQuery('system') || "system",
                            orientation: YQ.getQuery('sensor') || "sensor",
                            page_viewable: false,
                            initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                        }));

                        setTimeout(function(){
                            window.history.back();
                        }, 500);
                    }
                }else if(YQ.getQuery('pagegoto') == 'yes' && YQ.getQuery('screen') == 'new' && YQ.getQuery('url') != ''){
                    YQ.getExternal().openSecondWebview(JSON.stringify({
                        url: hasUrlHttp(YQ.getQuery('url')),
                        page_close: true
                    }));
                }else{
                    if (YQ.getExternal()['innerJump']) {
                        YQ.getExternal().innerJump(JSON.stringify({
                            name: appKey.toLowerCase(),
                            page_viewable: false
                        }));

                        setTimeout(function(){
                            window.history.back();
                        }, 500);
                    }
                }
            }else{
                location.href = 'https://wx.17zuoye.com/download/'+ itemMethod() +'?new_page=blank';
            }
        }

        function itemMethod(){
            var item = "17studentapp";
            if(YQ.getQuery("item") == 'parent'){
                item = "17parentapp"
            }else if(YQ.getQuery("item") == 'teacher'){
                item = "17teacherapp"
            }
            return item;
        }

        //是否带http
        function hasUrlHttp(url){
            if(url.substr(0,7) == "http://" || url.substr(0,8) == "https://"){
                return url;
            }

            return location.protocol + '//' + location.host + url;
        }

        if(!YQ.isBlank(appKey)){
            setTimeout(function(){
                AutoOpenApp();
            }, 300);
        }
    }
</script>
</@layout.page>