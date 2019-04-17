<#import "../module.ftl" as module>
<@module.page
title="微课堂CRM"
pageJsFile={"micocourse" : "public/script/course/microcourse"}
pageJs=["micocourse"]
leftMenu="课程管理"
>
<#include "bootstrapTemp.ftl">

<div class="op-wrapper orders-wrapper clearfix">
    <#if currentUser.isMicroTeacher()>
        <ol class="breadcrumb">
            <li>我的课程</li>
        </ol>
    </#if>
    <form id="query-frm" action="/course/manage/index.vpage" method="get">
    <input type="hidden" name="page" value="${pageIndex!1}" id="pageIndex">
    <#if currentUser.isOperator()>
    <div class="item">
        <p><strong>课程名称</strong></p>
        <input value="${course!}" name="course" class="v-select form-control" id="course"/>
    </div>

        <div class="item">
            <p><strong>课程分类</strong></p>
            <input value="${category!}" name="category" class="v-select form-control" id="category"/>
        </div>
        <div class="item">
            <p><strong>课程状态</strong></p>
            <select name="status" class="v-select form-control sel" id="status">
                <option value=0>全部</option>
                <option value=3 <#if status?? && status==3>selected</#if>>线上</option>
                <option value=6 <#if status?? && status==6>selected</#if>>下架</option>
                <option value=9 <#if status?? && status==9>selected</#if>>过期</option>
            </select>
        </div>
    </#if>
    </form>
    <#if currentUser.isOperator()>
    <div class="item" style="width:auto;margin-right:10px;">
        <p style="color:transparent;">.</p>
        <a class="btn btn-success" id="searchBtn" style="float:left;" href="javascript:void(0)"> <i class="glyphicon glyphicon-search"></i> 查询</a>
    </div>
    <div class="item" style="width:auto;margin-right:0;">
        <p style="color:transparent;">.</p>
        <a class="btn btn-success" id="addBtn" style="float:left;" href="detail.vpage"><i class="glyphicon glyphicon-plus"></i> 添加课程</a>
    </div>
    </#if>
</div>
<div id="listbox">
    <#if courseList?? && courseList?size gt 0>
    <#list courseList as item>
        <div>
            <div class="courseInfo-list">
               <strong style="line-height: 32px;"> 课程ID：${(item.courseId)!}</strong>
                <div class="cl-info">
                    <div class="img"><img src="<#if (item.photo)??>${(item.photo)!''}</#if>"></div>
                    <div class="info">
                        <div class="txt title">
                            ${(item.courseName)!''} （${(item.periodRefs)?size}课时） &nbsp;&nbsp;
                            <#--<#switch (item.status.getOrder())!>-->
                                <#--<#case 1>正在直播<#break/>-->
                                <#--<#case 3>在 线<#break/>-->
                                <#--<#case 6>已下架<#break/>-->
                                <#--<#case 9>已过期<#break/>-->
                            <#--</#switch>-->
                        </div>
                        <div class="txt">
                            老师：<#if (item.lecturers)??><#list item.lecturers as t>${t.name!''}<#if t_has_next> / </#if></#list></#if>
                            &nbsp;&nbsp;
                            助教：<#if (item.assistants)??><#list item.assistants as t>${t.name!''}<#if t_has_next> / </#if></#list></#if>
                        </div>
                        <div class="txt">最近上课时间：<#if (item.latestPeriod)??>${(item.latestPeriod.fetchPeriodTime())} ${(item.latestPeriod.theme)!}</#if></div>
                    </div>
                </div>
                <div class="cl-time">后续课时： <#if (item.nextPeriods)??>${item.nextPeriods?join(' / ')}</#if></div>
                <#if currentUser.isOperator()>
                <div class="cl-btn">
                    <#if (item.payAll)>
                    <a href="periodorder.vpage?period=${item.courseId!}" class="btn btn-warning btn-sm"><i class="glyphicon glyphicon-list-alt"></i> 报 表</a>
                    </#if>
                    <#if item.status.name() == "OFFLINE" || item.status.name() == "EXPIRE">
                        <a href="detail.vpage?cid=${item.courseId!}" class="btn btn-success btn-sm js-editBtn"> <i class="glyphicon glyphicon-edit"></i> 编 辑</a>
                        <a href="javascript:void(0)" class="btn btn-success btn-sm js-changeStatusBtn" data-cid="${item.courseId!'0'}" data-type="online"><i class="glyphicon glyphicon-arrow-up"></i> 上 线</a>
                        <a href="javascript:void(0)" class="btn btn-danger btn-sm js-delBtn" data-cid="${item.courseId!'0'}"><i class="glyphicon glyphicon-trash"></i> 删 除</a>
                    <#elseif item.status.name() == "ONLINE" || item.status.name() == "LIVE">
                        <a href="detail.vpage?cid=${item.courseId!}&mode=view" class="btn btn-success btn-sm js-editBtn"> <i class="glyphicon glyphicon-eye-open"></i> 查 看</a>
                        <a href="javascript:void(0)" class="btn btn-success btn-sm js-changeStatusBtn" data-cid="${item.courseId!'0'}" data-type="offline"><i class="glyphicon glyphicon-arrow-down"></i> 下 架</a>
                    </#if>
                </div>
                <#else>
                <div class="cl-btn">
                    <a href="classroom.vpage?cid=${item.courseId!}" class="btn btn-success btn-sm js-editBtn"> <i class="glyphicon glyphicon-eye-open"></i> 查 看</a>
                </div>
                </#if>
            </div>
        </div>
    </#list>
    </#if>
</div>
<div id="paginator" pageIndex="${(pageIndex!1)}" class="pagination pull-right clearfix" totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@module.page>