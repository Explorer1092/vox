<#--学生App-首页卡片导流家长说明页（宝藏版）-->
<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="更多学习更多奖励"
pageCssFile={"treasure" : ["public/skin/project/parentinstruction/treasure"]}
pageJsFile={"treasure" : "public/script/project/treasure"}
pageJs = ["treasure"]
>
<#list [1,2,3,4,5] as item>
    <div class="bz-bg">
        <img src="<@app.link href="public/skin/project/parentinstruction/images/bz-bg0${item}.png"/>">
    </div>
</#list>
<div class="bz-footer">
    <div class="bz-btnBox">
        <a href="https://wx.17zuoye.com/download/17parentapp?cid=202011" class="btn">下载家长通</a>
    </div>
</div>
<script>
    var devFlag = false;
    <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>
    devFlag = true;
    </#if>
</script>
</@layout.page>
