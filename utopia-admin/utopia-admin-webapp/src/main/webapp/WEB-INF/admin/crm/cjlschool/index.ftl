<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="陈经纶学校同步数据" page_num=3>
<link href="${requestContext.webAppContextPath}/public/css/teachernew/teacherdetail.css" rel="stylesheet">

<div id="main_container" class="span9">
    <legend>
        <strong>陈经纶学校同步数据</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <#if data?? && data?has_content>
                <#list data as teacherInfo>
                    <div class="well">
                        <fieldset class="personalInfo">
                            <span class="basic_info">老师姓名：<span class="txt">${teacherInfo.teacherName}</span></span>
                        </fieldset>
                        <fieldset style="margin-top: 2em;">
                            <#if (teacherInfo.clazzInfo)?? && (teacherInfo.clazzInfo)?has_content>
                                <#assign clazzList = teacherInfo.clazzInfo />
                                <#list clazzList as clazzInfo>
                                    <span class="basic_info"><span class="txt">${clazzInfo.clazzName}</span></span>
                                    <div class="well">
                                        <#if (clazzInfo.students)?? && (clazzInfo.students)?has_content>
                                        <table class="table table-hover table-striped table-bordered">
                                            <#list clazzInfo.students as student>
                                                <#if student_index % 7 == 0><tr></#if>
                                                <td>
                                                    <#if (student.klxStudentId)??> √ </#if>${student.studentName!''}(${student.studentNumber!'--'})
                                                </td>
                                                <#if student_index % 7 == 6 || !student_has_next></tr></#if>
                                            </#list>
                                        </table>
                                        </#if>
                                    </div>
                                </#list>
                            </#if>
                        </fieldset>
                    </div>
                </#list>
            </#if>
        </div>
    </div>
</div>
<script>
    $(function () {

    });
</script>
</@layout_default.page>