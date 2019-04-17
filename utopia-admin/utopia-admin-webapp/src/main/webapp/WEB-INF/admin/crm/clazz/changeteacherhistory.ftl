<#-- @ftlvariable name="changeTeacherHistoryList" type="java.util.List<java.util.Map>" -->
<#-- @ftlvariable name="clazzId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <fieldset><legend>组${groupId!}</a>任课历史</legend></fieldset>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>更新时间</th>
            <th>老师名称</th>
            <th>是否有效</th>
        </tr>
        <#if changeTeacherHistoryList?has_content>
                <#list changeTeacherHistoryList as changeTeacherHistory>
                    <tr>
                        <td>${(changeTeacherHistory.updateDatetime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                        <td><a href="../teacher/teacherhomepage.vpage?teacherId=${changeTeacherHistory.teacherId!}">${changeTeacherHistory.teacherName!}</a>(${changeTeacherHistory.teacherId!})</td>
                        <td>${(changeTeacherHistory.disabled?string('否', '是'))!}</td>
                    </tr>
                </#list>
        </#if>
    </table>
</div>
</@layout_default.page>