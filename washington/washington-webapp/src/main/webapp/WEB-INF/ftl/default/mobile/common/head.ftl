<meta charset="UTF-8">
<meta name="apple-mobile-web-app-capable" content="yes" />
<#-- 指定的iphone中safari顶端的状态条的样式 -->
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<#--告诉设备忽略将页面中的数字识别为电话号码-->
<meta name="format-detection" content="telephone=no" />
<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=utf-8" />

<#if viewport == "default">
    <meta name="viewport" content="target-densitydpi=device-dpi,width=${viewportWidth!640}, user-scalable=no" />
<#else>
    ${viewport}
</#if>

<meta name="MobileOptimized" content="320" />
<meta name="Iphone-content" content="320" />

<title>${title!''}</title>
