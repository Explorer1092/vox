<#if patternData?has_content>
<div class="r-table">
    <table>
        <thead>
        <tr>
            <#if currentUser.isResearchStaffForCounty()>
                <td rowspan="2">学校名称</td>
            <#else>
                <td rowspan="2">区域名称</td>
            </#if>
            <#if patternData?has_content>
                <#list patternData.patternRank?keys as key>
                    <td colspan="2">${key}</td>
                </#list>
            </#if>
        </tr>
        <tr>
            <#list patternData.patternRank?keys as key>
                <td>做题题量</td>
                <td>正确率</td>
            </#list>
        </tr>
        </thead>
        <tbody>
            <#list patternData.patternUnitList as patternUnit>
            <tr>
                <#if currentUser.isResearchStaffForCounty()>
                    <td>${patternUnit.name}</td>
                <#else>
                    <td>${patternUnit.name}</td>
                </#if>
                <#list patternData.patternRank?keys as key>
                    <#if (patternUnit.patternMap[key])?has_content>
                        <#--前五名加颜色-->
                        <td class="<#if key_index lt 5>r-tableColor-${key_index + 1}</#if>">${patternUnit.patternMap[key].finishCount}</td>
                        <td>${(patternUnit.patternMap[key].correctRate)?string("#0.00")}%</td>
                    <#else>
                        <td class="<#if key_index lt 5>r-tableColor-${key_index + 1}</#if>"></td>
                        <td></td>
                    </#if>
                </#list>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<div class="mb-info">
    <h5>结论</h5>
    <ul>
        <li>${(currentUser.formatManagedRegionStr())!}共有
            <#if currentUser.isResearchStaffForProvince() >
                <#if patternData.patternUnitList?size gt 1>${patternData.patternUnitList?size - 1}</#if>个市
                ${patternData.countyCount}
                个区
            </#if>
            <#if currentUser.isResearchStaffForCity() >
                <#if patternData.patternUnitList?size gt 1>${patternData.patternUnitList?size - 1}</#if>区
            </#if>
            ${patternData.schoolCount}所小学参与了题型数据使用分析，其中有效数据学校${patternData.validSchoolCount}所。</li>
        <li>
            ${(currentUser.formatManagedRegionStr())!}小学排名前五的常用题型依次为：<#list patternData.patternRank?keys as key><#if key_index lt 5>${key}<#if key_index lt 4>、</#if></#if></#list>
                <p style="padding-left: 10px;">其中</p>
                <#list patternData.patternRank?keys as key>
                    <#if key_index lt 5>
                    <p style="padding-left: 25px;">
                        ${key}题做题数量${patternData.patternRank[key]}道
                    </p>
                    </#if>
                </#list>

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