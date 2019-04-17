<#function pressImage(link)>
    <#if ((link?string)?index_of("oss-image.17zuoye.com") gt -1)>
        <#return '${link!}@500w_1o'/>
    <#else>
        <#return '${link!}'/>
    </#if>
</#function>

<#function pressImageAutoW(link, w)>
    <#if ((link?string)?index_of("oss-image.17zuoye.com") gt -1)>
        <#return '${link!}@${w!200}w_1o'/>
    <#else>
        <#return '${link!}'/>
    </#if>
</#function>