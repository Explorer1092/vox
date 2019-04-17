<div class="control-group">
    <label class="control-label">部门</label>
    <div class="controls">
        <select id="depart_select" name="departmentName" required="true">
            <option value="">All</option>
            <#if departmentList??>
            <#list departmentList as departmentItem>
                <option value="${departmentItem.name!}" <#if departmentItem.name == departmentName > selected="selected" </#if>>${departmentItem.description!}</option>
            </#list>
            </#if>
        </select>
        <span class="help-inline"></span>
    </div>
</div>
<#if showAdmin>
<div class="control-group">
    设为超级管理员：<input type="checkbox" name="superAdmin" value="true" <#if superAdmin!>checked</#if>>
</div>
</#if>
<div class="control-group ">
    <label class="control-label">用户名</label>
    <div class="controls">
        <input type="text" name='userName' value="${userName!''}" required="true" <#if isEdit??>readonly="true" </#if> />
        <span class="help-inline">用户名，用户的唯一标识，不可重复</span>
    </div>
</div>
<div class="control-group ">
    <label class="control-label">真实姓名</label>
    <div class="controls">
        <input type="text" name='realName' value="${realName!''}" required="true" />
        <span class="help-inline"></span>
    </div>
</div>
<div class="control-group ">
    <label class="control-label">密码</label>
    <div class="controls">
        <input type="text" name='password' value="${password!''}"  <#if isEdit??><#else>required="true"</#if> />
        <span class="help-inline"></span>
    </div>
</div>
<div class="control-group ">
    <label class="control-label">客服CC-agentId</label>
    <div class="controls">
        <input type="text" name='agentId' value="${agentId!''}"/>
        <span class="help-inline"></span>
    </div>
</div>

