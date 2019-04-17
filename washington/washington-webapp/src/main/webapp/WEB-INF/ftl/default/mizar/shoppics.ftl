<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='机构相册'
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
>
<#include "function.ftl"/>
<div class="banner" style="padding: 0; margin: 0 15px;">
    <#if (picList)?has_content>
        <ul class="bannerImg" style="padding: 20px 0;">
            <#list picList as item>
                <li style="width: 49%; text-align: center; margin-bottom: 20px;"><img src="${pressImage(item!'')}" style="width: 85%; height: auto; border-radius: 30px;"></li>
            </#list>
        </ul>
    <#else>
        <div style="text-align: center; padding: 50px; color: #aaa; font-size: 14px;">暂无数据~</div>
    </#if>
</div>
</@layout.page>