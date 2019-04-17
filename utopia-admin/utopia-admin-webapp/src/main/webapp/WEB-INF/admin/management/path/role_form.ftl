<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">名称</label>
    <div class="controls">
        <input type="text" name="roleDescription" value="${roleDescription!}" placeholder='默认'  required="true" />
        <span class="help-inline">${errorMessage!''} 中文描述</span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">标识ID</label>
    <div class="controls">
        <input type="text" name="roleName" value="${roleName!}" placeholder='default' required="true" />
        <span class="help-inline">${errorMessage!''} 唯一标识ID，英文表示</span>
    </div>
</div>