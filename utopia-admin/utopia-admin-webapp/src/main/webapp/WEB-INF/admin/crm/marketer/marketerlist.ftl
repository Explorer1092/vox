<#-- @ftlvariable name="marketerSnapshotList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default/>
<#import "marketerquery.ftl" as marketerQuery/>
<@layout_default.page page_title="市场人员查询" page_num=3>
<div class="span9">
    <@marketerQuery.queryPage/>
    <table class="table table-hover table-striped table-bordered">
        <#if marketerSnapshotList?has_content>
            <tr>
                <th>创建时间</th>
                <th>用户ID</th>
                <th>用户姓名</th>
            </tr>
            <#list marketerSnapshotList as marketerSnapshot>
                <tr>
                    <td>${(marketerSnapshot.createTime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                    <td>${marketerSnapshot.marketerId!}</td>
                    <td><a href="marketerhomepage.vpage?marketerId=${marketerSnapshot.marketerId!}">${marketerSnapshot.marketerName!}</a></td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
</@layout_default.page>