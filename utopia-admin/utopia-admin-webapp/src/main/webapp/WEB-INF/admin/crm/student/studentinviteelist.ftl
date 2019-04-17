<#-- @ftlvariable name="inviteeId" type="java.lang.Long" -->
<#-- @ftlvariable name="studentName" type="java.lang.String" -->
<#-- @ftlvariable name="studentId" type="java.lang.Long" -->
<#-- @ftlvariable name="inviteHistoryList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <fieldset>
        <legend>用户<a href="studenthomepage.vpage?studentId=${studentId!}">${studentName!}</a>邀请老师历史</legend>
    </fieldset>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>被邀请人</th>
            <th>是否成功</th>
        </tr>
        <#if inviteHistoryList?has_content>
            <#list inviteHistoryList as inviteHistory>
                <tr>
                    <td>${inviteHistory.createTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${inviteHistory.updateTime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td><a href="../user/userhomepage.vpage?userId=${inviteHistory.inviteeId!}">${inviteHistory.inviteeName!}</a></td>
                    <td>${inviteHistory.success?string('是', '否')}</td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
</@layout_default.page>