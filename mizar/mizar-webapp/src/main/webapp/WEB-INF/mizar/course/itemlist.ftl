<#import "../module.ftl" as module>
<@module.page
title="微课堂CRM"
pageJsFile={"micocourse" : "public/script/course/item"}
pageJs=["micocourse"]
leftMenu="列表管理"
>
<#include "bootstrapTemp.ftl">
<div class="op-wrapper orders-wrapper clearfix">
    <form id="pagerForm" action="" method="get">
        <input type="hidden" id="pageNum" name="page" value="${page!1}">
        <div class="item">
            <p><strong>课时ID</strong></p>
            <input value="${title!}" name="title" class="v-select form-control" style="width: 180px;" id="title"/>
        </div>
        <div class="item" style="margin-left: 15px;">
            <p><strong>课程分类</strong></p>
            <select name="category" class="v-select form-control sel" id="category">
                <option value="MICRO_COURSE_OPENING" <#if category?? && category=='MICRO_COURSE_OPENING'>selected</#if>>公开课</option>
                <option value="MICRO_COURSE_NORMAL" <#if category?? && category=='MICRO_COURSE_NORMAL'>selected</#if>>长期课</option>
            </select>
        </div>
        <div class="item">
            <p><strong>状态</strong></p>
            <select name="status" class="v-select form-control sel" id="status">
                <option value="">全部</option>
                <option value="ONLINE" <#if status?? && status=='ONLINE'>selected</#if>>上线</option>
                <option value="OFFLINE" <#if status?? && status=='OFFLINE'>selected</#if>>下线</option>
            </select>
        </div>
    </form>
    <div class="item" style="width:auto;margin-right:10px;">
        <p style="color:transparent;">.</p>
        <a class="btn btn-success" id="searchBtn" style="float:left;" href="javascript:void(0)"> <i class="glyphicon glyphicon-search"></i> 查 询</a>
    </div>
    <div class="item" style="width:auto;margin-right:10px;float:right;">
        <p style="color:transparent;">.</p>
        <a class="btn btn-primary" style="float:left;" href="/course/manage/itemdetail.vpage"> <i class="glyphicon glyphicon-plus"></i> 新 增</a>
    </div>
</div>
<div id="listbox">
    <table class="table table-bordered table-striped" style="background-color: #FFF;">
    <thead>
        <tr>
            <th style="width: 35%">课时名称</th>
            <#--<th>描述</th>-->
            <th style="width: 12%">上课时间</th>
            <th style="width: 8%;">优先级</th>
            <th style="width: 8%;">类型</th>
            <th style="width: 8%;">状态</th>
            <th style="width: 10%;">查看人数</th>
            <th style="width: 140px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <#if itemList?? && itemList?size gt 0>
            <#list itemList as item>
            <tr>
                <td><a href="/course/manage/periodinfo.vpage?periodId=${item.periodId!}&mode=view">${item.title!''}</a></td>
                <#--<td>${item.desc!''}</td>-->
                <td>${item.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                <td>${item.priority!0}</td>
                <td>
                    <#switch item.category>
                        <#case "MICRO_COURSE_OPENING">
                            公开课
                            <#break>
                        <#case "MICRO_COURSE_NORMAL">
                            长期课
                            <#break>
                        <#default>
                    </#switch>
                </td>
                <td>
                    <#switch item.status>
                        <#case "ONLINE">
                            上 线
                            <#break>
                        <#case "OFFLINE">
                            下 线
                            <#break>
                        <#default>
                    </#switch>
                </td>
                <td>${item.viewCnt!0}</td>
                <td>
                    <a href="itemdetail.vpage?itemId=${item.id!}" target="_blank" class="btn btn-success btn-xs js-editBtn" title="编辑"> <i class="glyphicon glyphicon-edit"></i></a>
                    <a href="javascript:void(0);" class="btn btn-danger btn-xs js-delBtn" data-item="${item.id!}" title="删除"> <i class="glyphicon glyphicon-trash"></i></a>
                    <a href="periodorder.vpage?period=${item.periodId!}" target="_blank" class="btn btn-warning btn-xs" data-item="${item.id!}" title="订单数据"> <i class="glyphicon glyphicon-list-alt"></i></a>
                    <a href="statistic.vpage?id=${item.periodId!}" target="_blank" class="btn btn-info btn-xs" data-item="${item.id!}" title="统计数据"> <i class="glyphicon glyphicon-leaf"></i></a>
                </td>
            </tr>
        </#list>
        <#else>
        <td colspan="6" style="<#if error??>color:#ff4d4d;</#if>text-align: center">没有数据</td>
        </#if>
        </tbody>
    </table>
</div>
<div id="paginator" pageIndex="${(pageIndex!1)}" class="pagination pull-right clearfix" totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@module.page>