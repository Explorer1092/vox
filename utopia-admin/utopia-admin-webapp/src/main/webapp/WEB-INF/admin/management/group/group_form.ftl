<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">名称</label>
    <div class="controls">
        <input type="text" name="groupDescription" value="${groupDescription!}"   required="true" />
        <span class="help-inline">${errorMessage!''} 中文说明</span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">标识ID</label>
    <div class="controls">
        <input type="text" name="groupName" value="${groupName!}" <#if readOnly??>readonly="true"</#if> required="true" />
        <span class="help-inline">${errorMessage!''} 唯一标识ID，英文字母</span>
    </div>
</div>