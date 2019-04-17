<#-- @ftlvariable name="temporarySnapshotList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default/>
<#import "temporaryquery.ftl" as temporaryQuery/>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <@temporaryQuery.queryPage/>
    <table class="table table-hover table-striped table-bordered">
        <#if temporarySnapshotList?has_content>
            <tr>
                <th>创建时间</th>
                <th>用户ID</th>
                <th>用户姓名</th>
            </tr>
            <#list temporarySnapshotList as temporarySnapshot>
                <tr>
                    <td>${(temporarySnapshot.createTime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                    <td>${temporarySnapshot.temporaryId!}</td>
                    <td><a href="temporaryhomepage.vpage?temporaryId=${temporarySnapshot.temporaryId!}">${temporarySnapshot.temporaryName!}</a></td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
</@layout_default.page>