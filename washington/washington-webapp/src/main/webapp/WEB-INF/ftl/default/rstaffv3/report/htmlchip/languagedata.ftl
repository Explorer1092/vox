<#if languageMapper?has_content>
<div class="r-table">
    <table>
        <thead>
        <tr>
            <td>语言知识</td>
            <td colspan="3">词汇</td>
            <td colspan="3">语法</td>
            <td colspan="3">话题</td>
            <td rowspan="2">学生数量</td>
        </tr>
        <tr>
            <#if currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForStreet() >
                <td>学校名称</td>
            <#else>
                <td>区域名称</td>
            </#if>
            <#list 1..3 as index>
                <td>总题量</td>
                <td>人均做题</td>
                <td>正确率</td>
            </#list>
        </tr>
        </thead>
        <tbody>
            <#--取出最高的实体-->
            <#assign wordsFirst=["0","0","0"] grammarsFirst=["0","0","0"] topicFirst=["0","0","0"] />
            <#list languageMapper.knowledgeUnitList as language>
            <tr>
                <td>${language.name}</td>
                <#--词汇-->
                <#if language.wordDetail?has_content>
                    <td class="<#if language.wordDetail.rank == 1>r-tableColor-1</#if>">${language.wordDetail.finishCount}</td>
                    <td>${language.wordDetail.countPerStudent}</td>
                    <td>${(language.wordDetail.correctRate)?string("#0.00")}%</td>
                    <#if language.wordDetail.rank == 1>
                        <#assign wordsFirst = ["${language.wordDetail.finishCount}","${language.wordDetail.countPerStudent}","${(language.wordDetail.correctRate)?string('##.00')}%"] />
                    </#if>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
                <#--语法--->
                <#if language.gramDetail?has_content>
                    <td class="<#if language.gramDetail.rank == 1>r-tableColor-1</#if>">${language.gramDetail.finishCount}</td>
                    <td>${language.gramDetail.countPerStudent}</td>
                    <td>${(language.gramDetail.correctRate)?string("#0.00")}%</td>
                    <#if language.gramDetail.rank == 1>
                        <#assign grammarsFirst = ["${language.gramDetail.finishCount}","${language.gramDetail.countPerStudent}","${(language.gramDetail.correctRate)?string('##.00')}%"] />
                    </#if>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
                <#--话题--->
                <#if language.topicDetail?has_content>
                    <td class="<#if language.topicDetail.rank == 1>r-tableColor-1</#if>">${language.topicDetail.finishCount}</td>
                    <td>${language.topicDetail.countPerStudent}</td>
                    <td>${(language.topicDetail.correctRate)?string("#0.00")}%</td>
                    <#if language.topicDetail.rank == 1>
                        <#assign topicFirst = ["${language.topicDetail.finishCount}","${language.topicDetail.countPerStudent}","${(language.topicDetail.correctRate)?string('##.00')}%"] />
                    </#if>
                <#else>
                    <td></td>
                    <td></td>
                    <td></td>
                </#if>
                <#--学生数量-->
                <td>${language.studentCount}</td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<div class="mb-info">
    <h5>结论</h5>
    <ul>
        <li>
            ${(currentUser.formatManagedRegionStr())!}
            合计
            <#if currentUser.isResearchStaffForProvince()>
                <#if languageMapper.knowledgeUnitList?size gt 1>${languageMapper.knowledgeUnitList?size - 1}</#if>个市
                ${languageMapper.countyCount}
                个区
            <#elseif currentUser.isResearchStaffForCity()>
                <#if languageMapper.knowledgeUnitList?size gt 1>${languageMapper.knowledgeUnitList?size - 1}</#if>个区
            </#if>
            ${languageMapper.schoolCount}所小学参与到一起作业平台语言知识类题目使用，其中有效数据学校${languageMapper.validSchoolCount}所<#if (languageMapper.schoolCount - languageMapper.validSchoolCount) gt 0>, ${languageMapper.schoolCount - languageMapper.validSchoolCount}所学校使用学生数量低于10人，视为无效数据，不参与数据排名。<#else>。</#if>
        </li>
        <li>
            ${(currentUser.formatManagedRegionStr())!}有效使用学生总数量为${languageMapper.totalStudentCount}个，<#if currentUser.isResearchStaffForCity()>行政区<#else>学校</#if>使用学生数量最高可达${languageMapper.maxStudentCount}个。
        </li>
        <li>
            语言知识分析如下：
            <p style="padding-left: 25px;">词汇练习题量最高的<#if currentUser.isResearchStaffForProvince()>市<#elseif currentUser.isResearchStaffForCity()>区<#else>学校</#if>，总题量为${wordsFirst[0]} 题，人均${wordsFirst[1]}题，正确率${wordsFirst[2]}</p>
            <p style="padding-left: 25px;">语法练习题量最高的<#if currentUser.isResearchStaffForProvince()>市<#elseif currentUser.isResearchStaffForCity()>区<#else>学校</#if>，总题量为${grammarsFirst[0]}题，人均${grammarsFirst[1]}题，正确率${grammarsFirst[2]}</p>
            <p style="padding-left: 25px;">话题练习题量最高的<#if currentUser.isResearchStaffForProvince()>市<#elseif currentUser.isResearchStaffForCity()>区<#else>学校</#if>，总题量为${topicFirst[0]}题，人均${topicFirst[1]}题，正确率${topicFirst[2]}</p>
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