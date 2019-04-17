<#-- @ftlvariable name="adminDictList" type="java.util.List<com.voxlearning.utopia.admin.persist.entity.AdminDict>" -->
<div>
    <fieldset>
        <legend>GROUP LIST</legend>
    </fieldset>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>ID</th>
            <th>CREATE_DATETIME</th>
            <th>UPDATE_DATETIME</th>
            <th>GROUP_NAME</th>
            <th>GROUP_MEMBER</th>
            <th>DESCRIPTION</th>
        </tr>
        <#if adminDictList?has_content>
            <#list adminDictList as adminDict>
                <tr>
                    <td>${adminDict.id!}</td>
                    <td>${adminDict.createDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${adminDict.updateDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <td>${adminDict.groupName!}</td>
                    <td>${adminDict.groupMember!}</td>
                    <td>${adminDict.description!}</td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
