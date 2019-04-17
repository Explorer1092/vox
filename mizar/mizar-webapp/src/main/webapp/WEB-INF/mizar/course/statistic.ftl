<#import "../module.ftl" as module>
<@module.page
title="一起作业"
leftMenu="数据统计"
pageJsFile={"micocourse" : "public/script/course/statistic"}
pageJs=["micocourse"]
>
<#include "bootstrapTemp.ftl">
<div class="op-wrapper orders-wrapper clearfix">
    <form id="pagerForm" action="" method="get">
        <input type="hidden" id="pageNum" name="page" value="${page!1}">
        <div class="item">
            <p><strong>课时ID</strong></p>
            <input value="${id!}" name="id" class="v-select form-control" style="width: 186px;" id="pid"/>
        </div>
        <div class="item" style="margin-left: 15px;">
            <p><strong>开始时间</strong></p>
            <input value="${start!}" name="start" class="v-select form-control sel" style="width:170px;" id="start"/>
        </div>
        <div class="item">
            <p><strong>结束时间</strong></p>
            <input value="${end!}" name="end" class="v-select form-control sel" style="width:170px;" id="end"/>
        </div>
        <div class="item">
            <p><strong>直播/回放</strong></p>
            <select name="live" class="v-select form-control sel" id="live" style="width:120px;">
                <option value="1" <#if live?? && live>selected</#if>>直播</option>
                <option value="0" <#if live?? && !live>selected</#if>>回放</option>
            </select>
        </div>
    </form>
    <div class="item" style="width:auto;margin-right:10px;">
        <p style="color:transparent;">.</p>
        <a class="btn btn-success" id="searchBtn" style="float:left;" href="javascript:void(0)"> <i class="glyphicon glyphicon-search"></i> 查 询</a>
    </div>
</div>
<div id="listbox"  style="overflow-x: scroll">
    <table class="table table-bordered table-striped" style="background-color: #FFF;width:1200px;">
        <thead>
        <tr>
            <th>用户ID</th>
            <th>进入教室时间</th>
            <th>离开教室时间</th>
            <th>IP地址</th>
            <th>地理位置</th>
            <th>终端类型</th>
            <th>浏览器</th>
            <th>停留时间</th>
        </tr>
        </thead>
        <tbody>
            <#if dataList?? && dataList?size gt 0>
                <#list dataList as item>
                <tr>
                    <td>${item.userId!}</td>
                    <td>${item.joinTime!}</td>
                    <td>${item.leaveTime!}</td>
                    <td>${item.ip!}</td>
                    <td>${item.location!}</td>
                    <td>${item.fetchTerminal()!}（${item.os!}）</td>
                    <td>${item.userAgent!}</td>
                    <td>${item.durationTime!}(${item.duration!0}秒)</td>
                </tr>
                </#list>
            <#else>
            <td colspan="8" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!'没有数据'}</td>
            </#if>
        </tbody>
    </table>
</div>
<#if dataList?? && dataList?size gt 0>
<div class="btn btn-info" id="exportBtn" style="float:left;margin: 20px 0;" href="javascript:void(0);"> <i class="glyphicon glyphicon-export"></i> 导出本页</div>
</#if>
<div id="paginator" pageIndex="${(page!1)}" class="pagination pull-right clearfix" totalPage="${totalPage!1}"></div>
</@module.page>