<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "alert"] css=["plugin.alert", "new_teacher.widget", "plugin.register"] />
    <@sugar.site_traffic_analyzer_begin />
    <style>
        .register_box .reg_type{ padding: 50px 0; margin: 0;}
        .register_box .reg_type ul{ margin: 0 93px; position: relative; height: 340px;}
        .register_box .reg_type li.student{ position: absolute; top: 0; left: 280px; margin: 0;}
        .teacherinfo-strong { width: 240px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;}
    </style>
</head>
<body>
<#include "../../layout/project.header.ftl"/>
<div class="register_box resetNav">
    <h1 class="reg_title">请选择你要登录的账号</h1>
    <div class="reg_type">
        <ul id="loginList">
        <#list candidates as t>
            <#if t.userType == "TEACHER"><#assign className="teacher", title="老师"/></#if>
            <#if t.userType == "PARENT"><#assign className="parents", title="家长"/></#if>
            <#if t.userType == "STUDENT"><#assign className="student", title="学生"/></#if>
            <#if (t.affairTeacher)!false><#assign className="teacher", title="教务老师"/></#if>
            <li class="${className!'teacher'?html}">
                <form method="post" action="/j_spring_security_check">
                    <a href="javascript:void(0);" class="mytc" title="我是${title!'老师'?html}">
                        <strong class="teacherinfo-strong"><#if (t.affairTeacher)!false>教务 </#if>${t.realname?html}</strong>
                        ${title!'老师'?html} ${t.useId?html}
                    </a>
                    <input type="hidden" value="${key?html}" name="j_key" />
                    <input type="hidden" value="${t.userType?html}" name="j_userType" />
                    <input type="hidden" value="${dataKey!?html}" name="dataKey" />
                </form>
            </li>
        </#list>
        </ul>
        <div style="clear:both; width: 100%;"></div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("#loginList li").on("click", function(){
            $(this).find("form").submit();
            return false;
        });
    });
</script>
<#include "../../layout/project.footer.ftl"/>
<@sugar.site_traffic_analyzer_end />
</body>
</html>