<#if regionPatternStats?? && regionPatternStats?has_content>
    <#list regionPatternStats as r>
    <tr>
        <td>${r_index + 1}</td>
        <td>${r.smallPattern!''}</td>
        <td>${r.doUseCount!''}</td>
    </tr>
    </#list>
<#else>
<tr>
    <th colspan="3" class="text_gray_9">暂无数据</th>
</tr>
</#if>