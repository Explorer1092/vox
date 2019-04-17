<#if skillMapper?has_content>
<div class="r-table">
    <table>
        <thead>
        <tr>
            <td>语言技能</td>
            <td colspan="3">听</td>
            <td colspan="3">说</td>
            <td colspan="3">读</td>
            <td colspan="3">写</td>
            <td rowspan="2">学生数量</td>
        </tr>
        <tr>
            <#if currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForStreet() >
                <td>学校名称</td>
            <#else>
                <td>区域名称</td>
            </#if>
            <#list 1..4 as index>
                <td>总题量</td>
                <td>人均做题</td>
                <td>正确率</td>
            </#list>
        </tr>
        </thead>
        <tbody>
            <#--取出每项技能下总题量最高的实体数据记录-->
            <#assign listenFirst=["0","0","0"] speakingFirst=["0","0","0"] readingFirst=["0","0","0"] writtenFirst = ["0","0","0"]  />
            <#list skillMapper.skillUnitList as skill>
            <tr>
                <td>${skill.name}</td>
                <#--听-->
                <#if skill.listening?has_content>
                    <td class="<#if skill.listening.rank == 1>r-tableColor-1</#if>">${skill.listening.finishCount}</td>
                    <td>${skill.listening.countPerStudent}</td>
                    <td>${(skill.listening.correctRate)?string("#0.00")}%</td>
                    <#if skill.listening.rank == 1>
                        <#assign listenFirst = ["${skill.listening.finishCount}","${skill.listening.countPerStudent}","${(skill.listening.correctRate)?string('##.00')}%"] />
                    </#if>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
                <#--说--->
                <#if skill.speaking?has_content>
                    <td class="<#if skill.speaking.rank == 1>r-tableColor-1</#if>">${skill.speaking.finishCount}</td>
                    <td>${skill.speaking.countPerStudent}</td>
                    <td>${(skill.speaking.correctRate)?string("#0.00")}%</td>
                    <#if skill.speaking.rank == 1>
                        <#assign speakingFirst = ["${skill.speaking.finishCount}","${skill.speaking.countPerStudent}","${(skill.speaking.correctRate)?string('##.00')}%"] />
                    </#if>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
                <#--读--->
                <#if skill.reading?has_content>
                    <td class="<#if skill.reading.rank == 1>r-tableColor-1</#if>">${skill.reading.finishCount}</td>
                    <td>${skill.reading.countPerStudent}</td>
                    <td>${(skill.reading.correctRate)?string("#0.00")}%</td>
                    <#if skill.reading.rank == 1>
                        <#assign readingFirst = ["${skill.reading.finishCount}","${skill.reading.countPerStudent}","${(skill.reading.correctRate)?string('##.00')}%"] />
                    </#if>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
                <#--写--->
                <#if skill.written?has_content>
                    <td class="<#if skill.written.rank == 1>r-tableColor-1</#if>">${skill.written.finishCount}</td>
                    <td>${skill.written.countPerStudent}</td>
                    <td>${(skill.written.correctRate)?string("#0.00")}%</td>
                    <#if skill.written.rank == 1>
                        <#assign writtenFirst = ["${skill.written.finishCount}","${skill.written.countPerStudent}","${(skill.written.correctRate)?string('##.00')}%"] />
                    </#if>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
                <#--学生数量-->
                <td>${skill.studentCount}</td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<div class="mb-info">
    <h5>结论</h5>
    <ul>
        <li>
            ${(currentUser.formatManagedRegionStr())!}合计
                <#if currentUser.isResearchStaffForProvince()>
                    <#if skillMapper.skillUnitList?size gt 1>${skillMapper.skillUnitList?size - 1}</#if>
                    个市
                    ${skillMapper.countyCount}
                    个区
                </#if>
                <#if currentUser.isResearchStaffForCity()>
                    <#if skillMapper.skillUnitList?size gt 1>${skillMapper.skillUnitList?size - 1}</#if>
                    个区
                </#if>
            ${skillMapper.schoolCount}所小学参与到一起作业平台语言技能类题目使用，其中有效数据学校${skillMapper.validSchoolCount}所<#if (skillMapper.schoolCount - skillMapper.validSchoolCount) gt 0>, ${skillMapper.schoolCount - skillMapper.validSchoolCount}所学校使用学生数量低于10人，视为无效数据，不参与数据排名。<#else>。</#if>
        </li>
        <li>
            ${(currentUser.formatManagedRegionStr())!}有效使用学生总数量为${skillMapper.totalStudentCount}个，
            <#if currentUser.isResearchStaffForCity()>行政区<#else>学校</#if>使用学生数量最高可达${skillMapper.maxStudentCount}个。
        </li>
        <li>
            语言技能分析如下：
            <p style="padding-left: 25px;">听力练习题量最高的<#if currentUser.isResearchStaffForProvince()>市<#elseif currentUser.isResearchStaffForCity()>区<#else>学校</#if>，总题量为${listenFirst[0]}，人均${listenFirst[1]}题，正确率${listenFirst[2]}</p>
            <p style="padding-left: 25px;">口语练习题量最高的<#if currentUser.isResearchStaffForProvince()>市<#elseif currentUser.isResearchStaffForCity()>区<#else>学校</#if>，总题量为${speakingFirst[0]}，人均${speakingFirst[1]}题，正确率${speakingFirst[2]}</p>
            <p style="padding-left: 25px;">阅读（选择类）练习题量最高的<#if currentUser.isResearchStaffForProvince()>市<#elseif currentUser.isResearchStaffForCity()>区<#else>学校</#if>，总题量为${readingFirst[0]}，人均${readingFirst[1]}题，正确率${readingFirst[2]}</p>
            <p style="padding-left: 25px;">写作（填空类）练习题量最高的<#if currentUser.isResearchStaffForProvince()>市<#elseif currentUser.isResearchStaffForCity()>区<#else>学校</#if>，总题量为${writtenFirst[0]}，人均${writtenFirst[1]}题，正确率${writtenFirst[2]}</p>
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