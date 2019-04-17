<#include "../index.ftl" />

<div class="container">
    <div class="row-fluid">
        <div class="span12 well">
            <legend>部门列表： <#if showAdmin><a href="department_new.vpage" class="btn btn-primary pull-right">添加新的部门</a></#if></legend>
            <table class="table table-striped table-bordered">
                <tr>
                    <td></td>
                    <td>部门标识</td>
                    <td>部门名称</td>
                    <td>管理员</td>
                </tr>
                <#if departmentList?has_content>
                <#list departmentList as departmentInfo>
                <tr>
                    <td>${departmentInfo_index+1}</td>
                    <td>${departmentInfo.name!''}</td>
                    <td>${departmentInfo.description!''}</td>
                    <td>
                        ${masterList[departmentInfo.name]!}
                        <#if departmentListForWrite?contains(departmentInfo.name) ><a href="department_admin.vpage?name=${departmentInfo.name!''}" class="btn btn-small pull-right">管理</a></#if>
                    </td>
                </tr>
                </#list>
                </#if>
            </table>
        </div>
    </div>
</div>
