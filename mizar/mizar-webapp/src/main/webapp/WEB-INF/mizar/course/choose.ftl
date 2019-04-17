<#--例：layout 1 -->
<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="选择课时"
pageJsFile={"siteJs" : "public/script/course/item"}
pageCssFile={"mizar" : ["/public/skin/css/skin"]}
pageJs=["siteJs"]
>
<#include "bootstrapTemp.ftl">
<div class="op-wrapper orders-wrapper clearfix" style="background: #fff; padding-top: 0;">
    <form id="choose-form" class="form-table" action="/course/manage/choose.vpage" method="get">
        <input type="hidden" name="page" id="choosePage" value="${pageIndex!1}">
        课程ID :
        <input value="<#if Request.course??>${Request["course"]}</#if>" name="course" class="v-select" style="width:200px;" id="course"/>
        &nbsp;&nbsp;&nbsp;&nbsp;
        课时名称 :
        <input value="<#if Request.theme??>${Request["theme"]}</#if>" name="theme" class="v-select" style="width:200px;" id="theme"/>
        <a class="btn btn-success" id="chooseBtn" style="float:right;" href="javascript:void(0)"> <i class="glyphicon glyphicon-search"></i> 查询</a>
    </form>
</div>

<table class="table table-bordered table-striped" style="background-color: #FFF;">
    <thead>
    <tr>
        <th width="25%">ID</th>
        <th>名称</th>
        <th width="35%">时间段</th>
    </tr>
    </thead>
    <tbody>
        <#if courseList?? && courseList?size gt 0>
            <#list courseList as item>
            <tr name="recordRow" data-theme="${item.theme!''}" data-pid="${item.id!''}">
                <td>${item.id!''}</td>
                <td>${item.theme!''}</td>
                <td>${item.startTime?string('yy年MM月dd日 HH:mm')}  ~  ${item.endTime?string('yy年MM月dd日 HH:mm')}</td>
            </tr>
            </#list>
        <#else>
        <tr><td colspan="3" style="text-align: center;"><strong>没有数据</strong></td></tr>
        </#if>
    </tbody>
</table>
<div id="choose-paginator" pageIndex="${(pageIndex!1)}" class="pagination pull-right" totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@layout.page>


