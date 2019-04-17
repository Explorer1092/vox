<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#macro queryPage>
<div>
    <form action="temporarylist.vpage" method="post" class="form-horizontal">
        <fieldset>
            <legend>临时员工查询</legend>
            <ul class="inline">
                <li>
                    <label>临时员工ID：<input name="temporaryId" value="${(conditionMap.temporaryId)!}" type="text"/></label>
                </li>
                <li>
                    <label>临时员工姓名：<input name="temporaryName" value="${(conditionMap.temporaryName)!}" type="text"/></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <button class="btn btn-primary" type="submit">查询</button>
                </li>
            </ul>
        </fieldset>
    </form>
</div>
</#macro>