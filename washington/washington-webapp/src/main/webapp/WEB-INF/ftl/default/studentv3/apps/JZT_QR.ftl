<#--
    @description :  因为家长通暂且不支持动态获取其二维码，暂且使用静态文件来代替。
    @channel :   渠道号
    @AUtoBuildTag : 是否需要帮你自动构建html img标签

    @return  构建完的图片src or 图片html标签
-->
<#function get_JZT_QR channel AutoBuildTag>
    <#assign JZT_QR_SRC>
        <@app.link href="public/skin/studentv3/images/JZT_QR/${channel?trim}.png"/>
    </#assign>

    <#assign imgTag = "none">

    <#if AutoBuildTag>
        <#assign imgTag>
            <img src="${JZT_QR_SRC!}" alt="家长通二维码"/>
        </#assign>
    </#if>

    <#return (imgTag == "none")?string(JZT_QR_SRC, imgTag)?trim>
</#function>
