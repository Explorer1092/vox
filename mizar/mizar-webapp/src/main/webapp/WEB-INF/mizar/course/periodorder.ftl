<#import "../module.ftl" as module>
<@module.page
title="微课堂CRM"
pageJsFile={"micocourse" : "public/script/course/periodorder"}
pageJs=["micocourse"]
leftMenu="课程管理"
>
<#include "bootstrapTemp.ftl">
<h3 class="h3-title">
    课程 <strong>${name!}</strong> 购买/预约数据查看
</h3>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="pagerForm" action="" method="get">
        <input type="hidden" name="period" value="${(id)!}">
        <input type="hidden" id="pageNum" name="pageNum" value="${page!1}">
    </form>
    <div class="item" style="width:auto;margin-right:10px;">
        <a class="btn btn-success" id="backBtn" style="float:left;" href="javascript:window.history.back();"> <i class="glyphicon glyphicon-chevron-left"></i> 返 回</a>
    </div>
    <div class="item" style="width:auto;margin-right:0;">
        <a class="btn btn-success" id="excelBtn" style="float:left;" href="/course/manage/downloadinfo.vpage?period=${(id)!}"><i class="glyphicon glyphicon-plus"></i> 导出Excel </a>
    </div>
</div>
<div id="listbox" style="overflow-x: scroll">
    <table class="table table-bordered table-striped" style="background-color: #FFF; width:1500px;">
    <thead>
        <tr>
            <th>预约/支付时间</th>
            <th>学号</th>
            <th>学生姓名</th>
            <th>家长号</th>
            <th>称谓</th>
            <th>年级</th>
            <th>学生手机号</th>
            <th>家长手机号</th>
            <th>状态</th>
            <th>用户备注</th>
            <th>来源</th>
            <th>外部流水号</th>
        </tr>
        </thead>
        <tbody>
        <#if resultList?? && resultList?size gt 0>
            <#list resultList as item>
            <tr>
                <#list item as info>
                    <td>${info!}</td>
                </#list>
            </tr>
        </#list>
        <#else>
        <td colspan="12" style="<#if error??>color:#ff4d4d;</#if>text-align: center">暂时没有购买/预约数据</td>
        </#if>
        </tbody>
    </table>
</div>
<div id="paginator" pageIndex="${(pageIndex!1)}" class="pagination pull-right clearfix" totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@module.page>