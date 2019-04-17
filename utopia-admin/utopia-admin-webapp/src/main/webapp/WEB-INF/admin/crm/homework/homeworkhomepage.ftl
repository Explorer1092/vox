<#-- @ftlvariable name="category" type="java.lang.String" -->
<#-- @ftlvariable name="homeworkInfo" type="java.util.Map<String, Object>" -->
<#-- @ftlvariable name="homeworkSubject" type="java.lang.String" -->
<#-- @ftlvariable name="homeworkId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="作业查询" page_num=3>
<style>
    blockquote {
        margin: 0;
    }

    .span9 .question-box a {
        width: 30%;
        display: inline-block;
    }
</style>
<div id="main_container" class="span9">
    <ul class="nav nav-tabs">
        <li${((category == 'middle')?string('', ' class="active"'))!}><a data-toggle="tab" href="#primary">小学</a></li>
    </ul>

    <div class="tab-content">
        <div id="primary" class="tab-pane fade${((category == 'middle')?string('', ' in active'))!}">
            <div>
                <fieldset>
                    <legend>小学作业查询</legend>
                </fieldset>
                <ul class="inline">
                    <li>
                        <form action="?" method="get">
                            旧英语作业ID：<input name="homeworkId"/>
                            <input type="hidden" name="homeworkSubject" value="ENGLISH"/>
                            <input type="submit" class="btn" value="搜索"/>
                        </form>
                    </li>
                </ul>
            </div>
            <fieldset>
                <legend>
                    <#if homeworkSubject??> <#if homeworkSubject == 'ENGLISH'>英语作业${homeworkId!}详情</#if></#if>
                </legend>
            </fieldset>

            <div>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th style="width: 150px;">开始时间</th>
                        <th style="width: 150px;">截止时间</th>
                        <th>作业详情</th>
                        <th style="width: 320px;">学生完成时间</th>
                    </tr>
                    <#if homeworkInfo??>
                        <tr>
                            <td>${homeworkInfo.startDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>${homeworkInfo.endDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>
                                <div class="question-box">
                                <#--${homeworkInfo.homeworkPrettyJson!}-->
                                    <#if homeworkDetails??>
                                        <#list homeworkDetails as homeworkDetail>
                                            <#if homeworkSubject == 'ENGLISH'>
                                                <#if homeworkDetail.type == 'homework'>
                                                    <a target="_blank"
                                                       href="http://www.17zuoye.com/flash/loader/selfstudy-${homeworkDetail.practiceId!}-${studentId!}-${homeworkDetail.bookId!}-${homeworkDetail.unitId!}-${homeworkDetail.lessonId!}.vpage">${homeworkDetail.practiceName!}</a>
                                                <#else>
                                                    <a target="_blank"
                                                       href="http://www.17zuoye.com/container/viewpaper.vpage?qid=${homeworkDetail.exam}">${homeworkDetail.exam}</a>
                                                </#if>
                                            </#if>
                                        </#list>
                                    </#if>
                                </div>
                            </td>
                            <td>
                                <#list studentAccomplishmentList as accomplishment>
                                    <#assign clientType="icon-globe"  />
                                    <#if (accomplishment.clientType??)>
                                        <#if (accomplishment.clientType == 'mobile')>
                                            <#assign clientType="icon-hand-up"  />
                                        </#if>
                                    </#if>
                                    <#if accomplishment.finished>
                                        <i class="icon-ok"></i>
                                        <#assign ca = accomplishment.accomplishTime?string('yyyy-MM-dd HH:mm:ss')/>
                                    <#else>
                                        <i class="icon-remove"></i>
                                        <#assign ca = "--"/>
                                    </#if>
                                    <i class="${clientType}"></i>
                                    <#if (accomplishment.studentId == studentId)>
                                        <strong><a
                                                href="${requestContext.webAppContextPath}/crm/student/studenthomeworkdetail.vpage?userId=${accomplishment.studentId}">${accomplishment.studentId}</a></strong>
                                        =>
                                        <strong>${ca}</strong>
                                    <#else>
                                    ${accomplishment.studentId} => ${ca}
                                    </#if>
                                    <a href="${requestContext.webAppContextPath}/crm/homework/userhomeworkresultdetail.vpage?userId=${accomplishment.studentId}&homeworkId=${homeworkId!}&subject=${homeworkSubject}"><i
                                            class="icon-barcode"></i></a>
                                    <br>
                                </#list>
                            </td>
                        </tr>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</@layout_default.page>