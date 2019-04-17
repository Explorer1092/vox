<#import "../layout.ftl" as ucenter>
<@ucenter.page title='阿分题' pageJs="product">
    <#if (available)!false>
        <#assign productType = 'AfentiExam'>
        <#include "module.ftl">
    <#else>
        <div class="main body_background" style="height: 30%;">
            <h1 class="logo"></h1>
        </div>
        <div style="text-align: center; margin-top: 50px;">
            您所在的地区暂未开放，敬请期待，请 <a href="javascript:history.back();" data-rel="back">返回</a>
        </div>
    </#if>
</@ucenter.page>