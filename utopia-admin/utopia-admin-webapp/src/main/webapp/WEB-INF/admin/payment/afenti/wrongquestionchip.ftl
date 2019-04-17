<#-- @ftlvariable name="adminDictList" type="java.util.List<com.voxlearning.utopia.admin.persist.entity.AdminDict>" -->
<div>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th></th>
            <th>ID</th>
            <th>用户ID</th>
            <th>错题因子数</th>
            <th>类题因子数</th>
        </tr>
    <#if afentiWqStat?has_content>
        <#list afentiWqStat as wqStat>
            <tr>
                <th><input name="statid" type="checkbox" value="${wqStat.id!}"></th>
                <td>${wqStat.id!}</td>
                <td>${wqStat.uid!}</td>
                <td><a class="incorrecthref" onclick="viewWq('${wqStat.uid!}');" href="javascript:void(0);">${wqStat.incorrectCount!}</a></td>
                <td>${wqStat.incorrect2correctCount!}</td>
            </tr>
        </#list>
    </#if>
    </table>
</div>