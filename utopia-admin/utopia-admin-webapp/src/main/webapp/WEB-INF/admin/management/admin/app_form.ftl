<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">系统名称</label>
    <div class="controls">
        <input type="text" name="appDescription" value="${appDescription!}" required="true" />
        <span class="help-inline">${errorMessage!''} 中文说明</span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">系统标识</label>
    <div class="controls">
        <input type="text" name="appName" value="${appName!}" required="true" <#if editPage?? && editPage == true>readonly="true" </#if> />
        <span class="help-inline">${errorMessage!''} 英文字母，唯一标识用</span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">登录回调URL</label>
    <div class="controls">
        <input type="text" name="callBackUrl" value="${callBackUrl!'http://'}" required="true" />
        <span class="help-inline">${errorMessage!''} </span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">系统KEY</label>
    <div class="controls">
        <input type="text" name="appKey" value="${appKey!}" required="true" />
        <span class="help-inline">${errorMessage!''} </span>
    </div>
</div>