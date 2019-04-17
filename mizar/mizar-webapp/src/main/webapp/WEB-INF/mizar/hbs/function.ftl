<#--兼容String pageJs or Array pageJs-->
<#function getPageJs(items)>
    <#if items?has_content>
        <#compress >
            <#if ((items?is_string))!false>
                <#return "'${items}'"/>
            <#else>
                <#assign names = ""/>
                <#list items as i>
                    <#assign names = (i_has_next)?string("${names + i }'" +  ", '", "'${names + i}'")/>
                </#list>
                <#return names>
            </#if>
        </#compress>
    </#if>
</#function>