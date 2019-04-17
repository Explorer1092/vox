<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="body-backBlue"
title="用户评论"
pageJs=["mizarComment"]
pageJsFile={"mizarComment" : "public/script/mobile/mizar/mizarComment"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
>
<#include 'mizarcomment.ftl'/>
</@layout.page>