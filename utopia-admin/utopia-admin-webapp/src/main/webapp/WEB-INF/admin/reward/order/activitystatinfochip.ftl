<#-- @ftlvariable name="adminDictList" type="java.util.List<com.voxlearning.utopia.admin.persist.entity.AdminDict>" -->
<div>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th style="width:30px;">ID</th>
            <th>捐赠单位</th>
            <th>捐赠数量</th>
            <th>学豆总数</th>
        </tr>
    <#if results?has_content>
        <#list results as result>
            <tr>
                <th>${result.activityId!}</th>
                <th>${result.priceUnit!}</th>
                <th>${result.totalNums!}</th>
                <td>${(result.priceUnit!"0")?number * (result.totalNums!"0")?number}</td>
            </tr>
        </#list>
    </#if>
    </table>
</div>