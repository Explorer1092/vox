<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>假期作业Package（${packageId!}）详情</legend>
        </fieldset>
        <#setting datetime_format="yyyy-MM-dd HH:mm:ss"/>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th>学生ID</th>
                <th>完成时间</th>
                <th>客户端类型</th>
                <th>IP</th>
            </tr>
            <#if homeworkPackageAccomplishments?has_content>
                <#list homeworkPackageAccomplishments as accomplishment>
                    <tr>
                        <td>${accomplishment.studentId!}</td>
                        <td>${accomplishment.accomplishTime!}</td>
                        <td>${accomplishment.clientType!}</td>
                        <td>${accomplishment.ip!}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>