<#import "../layout.ftl" as homework>
<@homework.page title="提示" pageJs="">
    <@sugar.capsule css=['base'] />
<div class="main body_background" style="height: 30%;">
    <h1 class="logo"></h1>
</div>
<div style="text-align: center; margin-top: 50px;">
${info!}<#if code??>(CODE:${code!})</#if>
    <#if url??>
        <p><a href="${url!'/parent/ucenter/index.vpage'}">点此返回</a></p>
    </#if>
</div>
</@homework.page>