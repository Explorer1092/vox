<#if unitWeakPointMapper?has_content>
<div class="r-table">
    <table>
        <thead>
        <tr>
            <#if currentUser.isResearchStaffForCounty()>
                <td>学校名称</td>
            <#else>
                <td>区域名称</td>
            </#if>
            <td>年级</td>
            <td>教材版本</td>
            <td>薄弱知识点</td>
            <#--<#list 1..(unitWeakPointMapper.maxUnitNum) as index>-->
                <#--<td>${index}</td>-->
            <#--</#list>-->
        </tr>
        </thead>
        <tbody>
            <#if unitWeakPointMapper.regionWeakPointMap?has_content && unitWeakPointMapper.regionWeakPointMap?size gt 0>
               <#list unitWeakPointMapper.regionWeakPointMap?keys as areaOrSchool>
                    <#list unitWeakPointMapper.regionWeakPointMap[areaOrSchool] as detail>
                    <tr>
                        <#if detail_index == 0>
                            <td rowspan="${(unitWeakPointMapper.regionWeakPointMap[areaOrSchool])?size}">${areaOrSchool}</td>
                        </#if>
                        <td rowspan="${detail.bookList?size}">${detail.clazzLevel}</td>
                        <#list detail.bookList as book>
                            <td>${book.press!}</td>
                            <td>${book.weakPoints}</td>
                            <#--<#list book.weakPointTags as weekPoint>-->
                                <#--<td>${weekPoint}</td>-->
                            <#--</#list>-->
                            <#--<#if (book.weakPointTags)?size lt (unitWeakPointMapper.maxUnitNum)>-->
                                <#--<#list 1..((unitWeakPointMapper.maxUnitNum)-(book.weakPointTags)?size) as emptyPoint>-->
                                    <#--<td></td>-->
                                <#--</#list>-->
                            <#--</#if>-->
                        </#list>
                    </tr>
                    </#list>
               </#list>
            </#if>
        </tbody>
    </table>
</div>
<div class="mb-info">
    <#if (unitWeakPointMapper.regionWeakPointMap)?has_content && (unitWeakPointMapper.regionWeakPointMap)?size gt 0>
        <h5>结论</h5>
        <ul>
            <#if currentUser.isResearchStaffForCounty()>
                <li>
                    学校教材单元做题数量低于20题，视为无效数据，不计入统计范围
                </li>
            </#if>
            <li>
                ${(currentUser.region.formatManagedRegionStr())!}对${(unitWeakPointMapper.regionWeakPointMap)?size}<#if currentUser.isResearchStaffForCounty()>个学校<#else>个区县</#if>
                    进行了单元薄弱知识点分析，针对3-6年级，选取<#list unitWeakPointMapper.selectedPresses as selectedPress>${selectedPress}<#if selectedPress_has_next>、</#if></#list>${unitWeakPointMapper.selectedPresses?size}教材
            </li>
            <li>
               	单元薄弱知识点分析如下：
                <#if !(unitWeakPointMapper.samePressFlag?has_content && unitWeakPointMapper.samePressFlag)>
                    <p style="padding-left: 25px;">单元薄弱知识点存在教材、地区与年级的差异性</p>
                </#if>
                <p style="padding-left: 25px;">同教材同年级同单元薄弱知识点存在地区差异性</p>
                <p style="padding-left: 25px;">教研员可参考报告，数据可作为单元试卷、期中试卷或期末试卷编写参考依据</p>
            </li>
        </ul>
    </#if>
</div>
<#else>
<table>
    <thead>
    <tr><td>暂无相关数据</td></tr>
    </thead>
</table>
</#if>