<#--应试作业 获取当前环境-->
<#macro getCurrentProductDevelopment name=''>
    <#compress>
        <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv())>
        'test'
        <#elseif ProductDevelopment.isStagingEnv()>
        'staging'
        <#elseif ProductDevelopment.isProductionEnv()>
        'prod'
        </#if>
    </#compress>
</#macro>

<#macro teacherAppBanner module="" op="">
<div class="topBanner_text">
    <a id="_bannerBtn" data-bind="click: $root._bannerBtn.bind($data,'${module!''}','${op!''}')" href="javascript:void (0);" data-module="${module!''}" data-op="${op!''}">
        老师app布置，检查作业更方便，用电脑、微信布置暑假作业，开学登录老师app最高可领150园丁豆，点击下载>
    </a>
</div>
</#macro>

<#--java to py-->
<#macro wechatJavaToPython>
    <#compress>
        <#if ProductDevelopment.isDevEnv()>
        //127.0.0.1:5001/
        <#elseif  ProductDevelopment.isTestEnv()>
        //wx.test.17zuoye.net/
        <#elseif ProductDevelopment.isStagingEnv()>
        //wx.staging.17zuoye.net/
        <#elseif ProductDevelopment.isProductionEnv()>
        //wx.17zuoye.com/
        </#if>
    </#compress>
</#macro>

<#--java to website-->
<#macro wechatJavaToWebSite>
    <#compress>
        <#if ProductDevelopment.isDevEnv()>
        //127.0.0.1:8081/
        <#elseif  ProductDevelopment.isTestEnv()>
        //www.test.17zuoye.net/
        <#elseif ProductDevelopment.isStagingEnv()>
        //www.staging.17zuoye.net/
        <#elseif ProductDevelopment.isProductionEnv()>
        //www.17zuoye.com/
        </#if>
    </#compress>
</#macro>