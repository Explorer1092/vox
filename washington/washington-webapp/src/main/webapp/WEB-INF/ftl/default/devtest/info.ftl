请求来源IP：${realRemoteIp}<br />
<#-- 当前环境：${ProductDevelopment.getStage()}<br /> -->
使用测试数据？${ProductDevelopment.isUsingTestData()?string("是", "否")}<br />
使用正式数据？${ProductDevelopment.isUsingProductionData()?string("是", "否")}<br />

.now?long?string: ${.now?long?string}<br />

json_encode:
${json_encode(testJsonArray)?html}<br />
${json_encode(testJsonList)?html}<br />
${json_encode(testJsonMap)?html}<br />

<script type="text/javascript">
    var user = ${json_encode(testJsonUser)};
</script>

<#if requestContext.isRequestFromOffice() >
请求来自办公室<br />
<#else>
请求来自办外部<br />
</#if>

<#list httpHeaderLines as l>
${l?html}<br />
</#list>