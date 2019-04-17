<#if ProductDevelopment.isDevEnv()>
    <#assign wechatUrlHeader = "10.200.4.97:8180">
<#elseif ProductDevelopment.isTestEnv()>
    <#assign wechatUrlHeader = "wechat.test.17zuoye.net">
<#elseif ProductDevelopment.isStagingEnv()>
    <#assign wechatUrlHeader = "wechat.staging.17zuoye.net">
<#elseif ProductDevelopment.isProductionEnv()>
    <#assign wechatUrlHeader = "wechat.17zuoye.com">
</#if>