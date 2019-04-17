<#if errorInfo??>
    错误!:${errorInfo}
</#if>
<#if userInfo??>
    <#list userInfo as ui>
        <#list ui?keys as key>
            ${key} : ${ui[key]} <br>
        </#list>
        ------------------------------------<br>
    </#list>
</#if>
