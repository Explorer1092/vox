<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="/mobile/work_record/index.vpage" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">消息</div>
            <a href="javascript:void(0);" class="headerBtn">三</a>
        </div>
    </div>
</div>
<#if (msgList![])?size gt 0>
    <#list msgList as msg>
        <p>${msg.title}</p>
        <p>${msg.createTime}</p>
        <p>${msg.desc}</p>
    </#list>
<#else>
    <p style="text-align: center;margin-top: 50px;">
        暂无任何消息
    </p>
</#if>

</@layout.page>
