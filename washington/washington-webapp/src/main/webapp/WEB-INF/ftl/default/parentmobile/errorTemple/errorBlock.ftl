<#if showTop!false>
    <#assign topType = "topTitle">
    <#assign topTitle = "服务器错误">
    <#include "../top.ftl" >
</#if>

<#assign result = result!{
    "success" : false,
    "info" : "",
    "errorCode": "404"
}>

<#assign errorCode = errorCode?exists?string(errorCode!'', result.errorCode!"")?string?trim templateFile="" showTitle = false>
<#assign errorInfo = info?exists?string(info!'', result.info!"")?string?trim templateFile="" showTitle = false>

<#-- @shuai.huan 沟通 400 都属于逻辑错误 -->
<#if errorCode == '900'>
    <script>
        setTimeout(function(){
			try{
				window.external.redirectLogin("");
			} catch (e) {
				window.location.href = "/";
			}
		}, 100);
    </script>
<#elseif "404, 400, no_bind_class"?index_of(errorCode) == -1>
    <#assign templateFile = "common">
<#else>
    <#assign templateFile = errorCode>
</#if>


<#if templateFile != "">
    <#include "${templateFile}.ftl">
</#if>

<script>
    window.error_info = [
        "${errorCode}",
        "${errorInfo}"
    ];
</script>

