<#function pressImage(link)>
    <#if ((link?string)?index_of("oss-image.17zuoye.com") gt -1)>
        <#return '${link!}?x-oss-process=image/resize,w_500/quality,Q_80'/>
    <#else>
        <#return '${link!}'/>
    </#if>
</#function>

<#function pressImageAutoW(link, w)>
    <#if ((link?string)?index_of("oss-image.17zuoye.com") gt -1)>
        <#return '${link!}?x-oss-process=image/resize,w_${w!200}/quality,Q_80'/>
    <#else>
        <#return '${link!}'/>
    </#if>
</#function>