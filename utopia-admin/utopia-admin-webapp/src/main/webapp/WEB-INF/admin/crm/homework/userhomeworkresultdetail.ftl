<#-- @ftlvariable name="stResultDetailList" type="java.util.Map<String, Object>" -->
<#-- @ftlvariable name="homeworkId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<style>
    blockquote{margin: 0;}
</style>
<div id="main_container" class="span9">
    <fieldset>
        <legend>
            <a href="${requestContext.webAppContextPath}/crm/student/studenthomepage.vpage?studentId=${studentId!}">${realName}</a>的作业
            <a href="${requestContext.webAppContextPath}/crm/homework/homeworkhomepage.vpage?homeworkId=${homeworkId!}&homeworkSubject=${subject!}&studentId=${studentId!}">${homeworkId!}</a>答题详情</legend>
    </fieldset>

    <div>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th style="width: 150px;">ID</th>
                <th style="width: 150px;">名称</th>
                <th>分数</th>
                <th style="width: 300px;">学生提交时间</th>
                <th>客户端类型</th>
                <th>详情</th>
            </tr>
            <#if stResultDetailList?? && stResultDetailList?size gt 0>
                <#list stResultDetailList as detail>
                    <tr>
                        <td>${detail.itemId}</td>
                        <td>${detail.itemName}</td>
                        <td>${(detail.score)!0}</td>
                        <td>${(detail.createAt)!""}</td>
                        <td>${(detail.clientType)!}</td>
                        <td>${(detail.content)!}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>