<#if successfulApplicationList??>
    <tr style="display: none;">
        <th id="___pageInformation">${successfulApplicationList.getTotalPages()}</th>
        <th></th>
    </tr>
    <#if (successfulApplicationList.getContent()?size > 0)>
        <#list successfulApplicationList.getContent() as content>
            <tr>
                <th>${content.date}</th>
                <th>${content.message}</th>
            </tr>
        </#list>
    <#else>
        <tr>
            <th style="padding:50px 0" colspan="2">暂无数据。</th>
        </tr>
    </#if>
<#else>
    <tr>
        <th style="padding:50px 0" colspan="2">暂无数据。</th>
    </tr>
</#if>