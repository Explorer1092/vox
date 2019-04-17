<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="索尼GMC比赛活动"
pageJs=['gmca']
pageJsFile={"gmca" : "public/script/project/gmca"}
pageCssFile={"demo" : ["public/skin/project/gmccontext/pc/css/skin"]}
>
<div class="snMain">
    <div class="bgMain">
        <div class="bg bg01"></div>
        <div class="bg bg02"></div>
        <div class="bg bg03"></div>
        <div class="bg bg04"></div>
        <div class="bg bg05"></div>
    </div>
    <div class="banner"></div>
    <div class="container">
        <div class="main main01">
            <div class="btnBox">
                <a href="/redirector/apps/go.vpage?app_key=GlobalMath" class="btn btn01"></a>
                <a href="/redirector/apps/go.vpage?basic=gmc_4th&app_key=GlobalMath" class="btn btn02"></a>
            </div>
        </div>
        <div class="main main02"></div>
        <div class="main main03">
            <div class="btnBox">
                <a href="https://www.global-math.com/pr/quiz/63?utm_source=17zy&utm_medium=lp&utm_campaign=zh_gmc4th_17zy_lp_a1" class="btn btn03" target="_blank"></a>
            </div>
        </div>
        <div class="main main04">
            <div class="bg01"></div>
            <div class="bg02"></div>
            <div class="btnBox">
                <a href="/student/apps/index.vpage?app_key=Stem101" class="btn btn04"></a>
            </div>
        </div>
        <div class="footer"></div>
    </div>
</div>
<script>
    var $userType = ${(currentUser.userType)!0};
    var $userId = ${(currentUser.id)!0};

</script>
</@layout.page>