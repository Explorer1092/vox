<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#macro queryPage>
<div>
    <form action="marketerlist.vpage" method="post" class="form-horizontal">
        <fieldset>
            <legend>市场人员查询</legend>
            <ul class="inline">
                <li>
                    <label>市场人员ID：<input name="marketerId" value="${(conditionMap.marketerId)!}" type="text"/></label>
                </li>
                <li>
                    <label>市场人员姓名：<input name="marketerName" value="${(conditionMap.marketerName)!}" type="text"/></label>
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