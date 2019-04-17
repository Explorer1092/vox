<#import "../layout_new_no_group.ftl" as layout>
<@layout.page group="" title="错误页面">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">错误:${code!}</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info">
    <div style="text-align: center;line-height: 40px;">
        ${info!}(CODE:${code!})<br>
        <#if errorMessage??>${errorMessage!""}</#if><br>
        <#if url?? && url?has_content>
            <a href="${url!'/mobile/index.vpage'}" style="background-color: #ff7d5a;color: white;padding: 5px;text-align: center;">点此返回</a>
        </#if>
    </div>
</div>
</@layout.page>