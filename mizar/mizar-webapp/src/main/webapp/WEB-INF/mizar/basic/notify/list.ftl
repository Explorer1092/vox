<#import "../../module.ftl" as module>
<@module.page
title="消息列表"
pageJsFile={"siteJs" : "public/script/basic/notifylist"}
pageJs=["siteJs"]
leftMenu="我的消息"
>
<div class="op-wrapper orders-wrapper clearfix">
    <span class="title-h1">消息列表</span>
    <span class="item" style="width:auto; float: right;">
        <#if currentUser.isOperator() || currentUser.isAdmin()>
        <a class="blue-btn" href="edit.vpage">发送消息</a>
        </#if>
    </span>
    <div style="margin-top: 15px;">
    <#if notifyList?? && notifyList?size gt 0>
        <#list notifyList as page>
        <table class="data-table one-page <#if page_index == 0>displayed</#if>">
            <thead>
            <tr>
                <th>类型</th>
                <th>标题</th>
                <th>内容</th>
                <th>时间</th>
                <th>附件</th>
                <th style="width:120px;">操作</th>
            </tr>
            </thead>
            <tbody>
                <#if page?has_content && page?size gt 0>
                    <#list page as n>
                    <tr>
                        <td>${n.type.getDesc()!""}</td>
                        <td>${n.title!""}</td>
                        <td>${n.content!""}</td>
                        <td>${n.createAt!""}</td>
                        <td>
                            <#if n.files?has_content && n.files?size gt 0>
                                <#list n.files as f>
                                    <a href="${f.url!'#'}" target="_blank" class="op-btn"> ${f.name!''} </a>
                                </#list>
                            </#if>
                        </td>
                        <td>
                            <#if !(n.read!false)>
                                <a class="op-btn js-readBtn" href="javascript:void(0);" data-sid="${n.id!'0'}"> 标记已读 </a>
                            <#else>
                                已读
                            </#if>
                            <#if (n.url!'') != "">
                                <a href="${n.url!'#'}" class="op-btn"> 查看 </a>
                            </#if>
                            <a href="javascript:void(0);" class="op-btn js-delBtn" data-sid="${n.id!'0'}"> 删除 </a>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
        </#list>
    <#else>
        <table class="data-table one-page displayed">
            <thead>
            <tr>
                <th>类型</th>
                <th>标题</th>
                <th>内容</th>
                <th>时间</th>
                <th>附件</th>
                <th style="width:120px;">操作</th>
            </tr>
            </thead>
            <tbody>
                <tr>
                    <td colspan="6" style="text-align: center;">暂无消息</td>
                </tr>
            </tbody>
        </table>
    </#if>
    <div id="paginator" data-startPage="${page!1}" class="paginator clearfix"></div>
    </div>
</div>
</@module.page>