<#-- @ftlvariable name="teacherName" type="java.lang.String" -->
<#-- @ftlvariable name="changeClazzHistoryList" type="java.util.List<java.util.Map>" -->
<#-- @ftlvariable name="teacherId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="${teacherName!}换班历史" page_num=3>
<div class="span9">
    <fieldset><legend>老师<a href="/crm/teachernew/teacherdetail.vpage?teacherId=${teacherId!}">${teacherName!}</a>(${teacherId!})换班历史</legend></fieldset>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>更新时间</th>
            <th>班级名称</th>
            <th>是否有效</th>
            <th>是否管理（班级创建者/任课）</th>
        </tr>
        <#if changeClazzHistoryList?has_content>
            <#list changeClazzHistoryList as changeClazzHistory>
                <tr>
                    <td>${(changeClazzHistory.updateDatetime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                    <td><a target="_blank" href="/crm/clazz/groupinfo.vpage?groupId=${changeClazzHistory.groupId!}">${changeClazzHistory.clazzName!}</a>(${changeClazzHistory.groupId!})</td>
                    <td>${(changeClazzHistory.disabled?string('否', '是'))!}</td>
                    <td>${((changeClazzHistory.refType = 0)?string('否', '是'))!}</td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
</@layout_default.page>