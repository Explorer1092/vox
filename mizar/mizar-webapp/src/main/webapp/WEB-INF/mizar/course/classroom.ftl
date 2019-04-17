<#import "../module.ftl" as module>
<@module.page
title="我的课程"
leftMenu="课程管理"
>
<#include "bootstrapTemp.ftl">
<#if period?? && period?has_content>
<div class="op-wrapper orders-wrapper clearfix">
    <ol class="breadcrumb">
        <li><a href="/course/manage/index.vpage">我的课程</a></li>
        <li class="active">${(course.courseName)!''}</li>
    </ol>
    <h4>${(course.courseName)!''}</h4>
</div>
<div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="headingOne">
            <h5 class="panel-title">
                <strong>课时信息：</strong>
            </h5>
        </div>
        <div id="collapseOne" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="headingOne">
            <div class="panel-body">
                <div class="js-LecturerList">
                    <div class="courseInfo-list">
                        <div class="cl-info">
                            <div class="img"><img src="${period.photo[0]!'#'}"></div>
                            <div class="info">
                                <div class="txt title">
                                    ${(period.theme)!''}
                                </div>
                                <div class="txt">
                                    ${period.startTime?string('yyyy-MM-dd HH:mm:ss')!''} -- ${period.endTime?string('yyyy-MM-dd HH:mm:ss')!''}
                                </div>
                                <div class="txt">
                                <#if role?? && role == 'Assistant' >
                                    主讲：
                                    <#if course?? && course?has_content>
                                        <#if (course.lecturers)?has_content && (course.lecturers)?size gt 0>
                                            <#list (course.lecturers) as a > ${a.name!""} </#list>
                                        </#if>
                                    </#if>
                                </#if>
                                    &nbsp;&nbsp;&nbsp;&nbsp;助教：
                                    <#if course?? && course?has_content>
                                        <#if (course.assistants)?has_content && (course.assistants)?size gt 0>
                                            <#list (course.assistants) as a > ${a.name!""} </#list>
                                        </#if>
                                    </#if>
                                </div>
                            </div>
                        </div>
                        <div class="cl-btn pull-right">
                            <#if role?? && role == 'Lecturer'>
                                <#if protocol?? && protocol?has_content>
                                    <a href="${protocol!'javascript:void(0);'}" class="btn btn-warning"> <i class="glyphicon glyphicon-play-circle"></i> 去上课</a>
                                </#if>
                            <#elseif role?? && role == 'Assistant' >
                                <#if liveUrl?has_content>
                                    <a href="${(liveUrl)!'javascript:void(0);'}" class="btn btn-warning" target="_blank"> <i class="glyphicon glyphicon-play-circle"></i> 去上课</a>
                                </#if>
                            </#if>
                        </div>
                        <#if role?? && role == 'Lecturer'>
                            <p class="pull-right">
                                <#if helpUrl?? && helpUrl?has_content>
                                    无法打开？试试 <a href="${helpUrl!'javascript:void(0);'}" target="_blank">账号密码登录</a>
                                </#if>
                            </p>
                        </#if>
                        <#if error?? && error?has_content>
                            <p class="pull-right">
                                错误信息： ${error!}
                            </p>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</#if>
</@module.page>