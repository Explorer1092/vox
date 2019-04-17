<#if weekPointMapperList?has_content && weekPointMapperList?size gt 0>
<div class="r-table">
    <table>
        <thead>
        <tr>
            <#if currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForStreet() >
                <td>学校名称</td>
            <#else>
                <td>区域名称</td>
            </#if>
            <td style="width: 23%">词汇</td>
            <td style="width: 23%">语法</td>
            <td style="width: 23%">话题</td>
        </tr>
        </thead>
        <tbody>
            <#list weekPointMapperList as weekPoint>
            <tr>
                <td>${weekPoint.name}</td>
                <td class="break"><#if weekPoint.word?has_content>${weekPoint.word}</#if></td>
                <td class="break"><#if weekPoint.grammar?has_content>${weekPoint.grammar}</#if></td>
                <td class="break"><#if weekPoint.topic?has_content>${weekPoint.topic}</#if></td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<div class="mb-info">
    <h5>结论</h5>
    <#assign replaceName = "区域"/>
    <#if currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForStreet() ><#assign replaceName = "学校"/></#if>
    <ul>
        <li>
            参考时知识点错误数量多少与${replaceName}教学质量不是简单的反比例关系，做题数量越多、参与学生越多，有可能知识点错误率也高。
        </li>
        <li>
            如果${replaceName}做题数量、参与人数、正确率三项均排名靠前，则${replaceName}教学质量可视为优秀。
        </li>
    </ul>
</div>
<#else>
<table>
    <thead>
    <tr><td>暂无相关数据</td></tr>
    </thead>
</table>
</#if>