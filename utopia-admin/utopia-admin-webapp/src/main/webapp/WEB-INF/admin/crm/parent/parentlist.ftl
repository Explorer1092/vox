<#import "../../layout_default.ftl" as layout_default>
<#import "parentquery.ftl" as parentQuery>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="家长查询" page_num=3>
<style>
    .table_soll{ overflow-y:hidden; overflow-x: auto;}
    .table_soll table td,.table_soll table th{white-space: nowrap;}
</style>
<div id="main_container" class="span9">
    <@headsearch.headSearch/>
    <@parentQuery.queryPage/>
    <#if parentList?has_content>
    <div class="table_soll">
        <table class="table table-striped table-bordered">
            <tr>
                <td>ID</td>
                <td>姓名</td>
                <td>手机</td>
                <#--<td>邮箱</td>-->
                <td>是否验证手机</td>
                <#--<td>是否验证邮箱</td>-->
                <td>孩子姓名/班级/学校/是否是关键家长</td>
            </tr>
            <#list parentList as parent>
                <tr>
                    <td><a href="parenthomepage.vpage?parentId=${parent.parentId}" target="_blank">${parent.parentId!''}</a></td>
                    <td>${parent.parentName!''}</td>
                    <td><#if parent.parentId??><button type="button" id="query_user_phone_${parent.parentId!''}" class="btn btn-info">查 看</button></#if></td>
                    <#--<td>${parent.parentEmail!''}</td>-->
                    <td>${parent.verifyMobile?string('是','')}</td>
                    <#--<td>${parent.verifyEmail?string('是','')}</td>-->
                    <td style="padding: 0;">
                        <table class="table table-striped table-hover">
                            <#list parent.childList as child>
                                <tr>
                                    <td><a href="../student/studenthomepage.vpage?studentId=${child.childId!''}" target="_blank">${child.childName!''}</a>(${child.childId!''})</td>
                                    <td>
                                        <#if child.clazzId??>
                                            ${child.clazzLevel!''}年级${child.clazzName!''}(${child.clazzId!''})
                                        </#if>
                                    </td>
                                    <td>
                                        <#if child.schoolId??>
                                            <a href="../school/schoolhomepage.vpage?schoolId=${child.schoolId!''}" target="_blank">${child.schoolName!''}</a>(${child.schoolId!''})
                                        </#if>
                                    </td>
                                    <td style="margin: 0px 0px 0px 0px;">
                                        <#if child.keyParent??>
                                            ${child.keyParent?string('是','')}
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                        </table>
                    </td>
                </tr>
            </#list>
        </table>
        </div>
    </#if>
</div>
</@layout_default.page>