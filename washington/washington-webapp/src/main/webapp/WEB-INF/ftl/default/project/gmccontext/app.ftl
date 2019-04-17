<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="索尼GMC比赛活动"
pageJs=['gmc']
pageJsFile={"gmc" : "public/script/project/gmc"}
pageCssFile={"demo" : ["public/skin/project/gmccontext/app/css/skin"]}
>
<div class="snMain">
    <div class="header">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/gmc_01.png"/>">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/gmc_02.png"/>">
    </div>
    <div class="main01">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/gmc_03.png"/>">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/gmc_04.png"/>">
        <div class="btnBox">
            <a href="javascript:void(0)" class="btn btn01"></a>
            <a href="javascript:void(0)" class="btn btn02 js-clicklink"></a>
        </div>
    </div>
    <div class="main02">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame05.png?v=002"/>">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame06.png?v=002"/>">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame07.png?v=002"/>">
    </div>
    <div class="main03">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame08.png"/>">
        <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame09.png"/>">
        <div class="btnBox">
            <a href="https://www.global-math.com/pr/quiz/63?utm_source=17zy&utm_medium=lp&utm_campaign=zh_gmc4th_17zy_lp_a1" class="btn btn03"></a>
        </div>
    </div>
    <#if (currentUser.userType == 2)!false>
        <div class="main04">
            <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame10.jpg"/>">
            <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame11.jpg"/>">
            <div class="btnBox">
                <a href="/parentMobile/ucenter/shoppinginfo.vpage?productType=Stem101" class="btn btn04"></a>
            </div>
        </div>
    </#if>

        <div class="footer">
           <img src="<@app.link href="public/skin/project/gmccontext/app/images/snGame12.png"/>">
        </div>
</div>
<script type="text/javascript">

    var $userType = ${(currentUser.userType)!0};
    var $userId = ${(currentUser.id)!0};
    var _sid = 0;
    signRunScript = function(){
        function _getCookie(name){
            var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
            if(arr=document.cookie.match(reg))
                return unescape(arr[2]);
            else
                return null;
        }

        //Get Query
        function getQueryString(name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]); return null;
        }

        var _SID = _getCookie("sid") || getQueryString("sid") || 0;

        console.info(_SID);
        _sid = _SID;
    }
</script>
</@layout.page>