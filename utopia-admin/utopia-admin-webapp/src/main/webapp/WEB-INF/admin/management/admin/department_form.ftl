<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">部门名称</label>
    <div class="controls">
        <input type="text" name="departmentDescription" value="${departmentDescription!}" required="true" />
        <span class="help-inline">${errorMessage!''} 中文说明</span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">部门标识</label>
    <div class="controls">
        <input type="text" name="departmentName" value="${departmentName!}" required="true" />
        <span class="help-inline">${errorMessage!''} 英文字母，唯一标识用</span>
    </div>
</div>
