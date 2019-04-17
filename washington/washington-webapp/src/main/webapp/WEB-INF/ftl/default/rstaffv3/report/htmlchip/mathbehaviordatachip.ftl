<div class="r-table">
    <table>
        <thead>
        <tr class="blue">
            <td rowspan="3" style="width: 8%">区</td>
            <td rowspan="3">学校</td>
            <td colspan="2">认证老师</td>
            <td colspan="2">认证学生</td>
        </tr>
        <tr class="blue">
            <td colspan="2">练习</td>
            <td colspan="2">练习</td>
        </tr>
        <tr class="blue">
            <td style="width: 8%">人数</td>
            <td style="width: 8%">人次</td>
            <td style="width: 8%">人数</td>
            <td style="width: 8%">人次</td>
        </tr>
        </thead>
    </table>
    <div class="allAreaContainer">
    <#if behaviorList?has_content && behaviorList?size gt 0>
        <#if summaryMapper?has_content && (currentUser.isResearchStaffForCity() || currentUser.isResearchStaffForProvince())>
            <table>
                <tbody>
                <tr>
                    <td style="width: 8%">
                        <#if currentUser.isResearchStaffForCity()>
                          ${currentUser.region.cityName}
                        <#elseif currentUser.isResearchStaffForProvince()>
                            ${currentUser.region.provinceName}
                        </#if>
                    </td>
                    <td>${summaryMapper.schoolNum}</td>
                    <td style="width: 8%">${summaryMapper.homeworkTeacherNum}</td>
                    <td style="width: 8%">${summaryMapper.homeworkTeacherTime}</td>
                    <td style="width: 8%">${summaryMapper.homeworkStuNum}</td>
                    <td style="width: 8%">${summaryMapper.homeworkStuTime}</td>
                </tr>
                </tbody>
            </table>
        </#if>
        <#list behaviorList as behavior>
            <table class="area-${behavior_index}">
                <thead>
                <tr <#if currentUser.isResearchStaffForCounty()> class="blue" </#if>>
                    <td style="width: 8%">${behavior.name}</td>
                    <td>${behavior.schoolNum}</td>
                    <td style="width: 8%">${behavior.homeworkTeacherNum}</td>
                    <td style="width: 8%">${behavior.homeworkTeacherTime}</td>
                    <td style="width: 8%">${behavior.homeworkStuNum}</td>
                    <td style="width: 8%">${behavior.homeworkStuTime}</td>
                </tr>
                </thead>
                <tbody class="area-${behavior_index}-child" <#if currentUser.isResearchStaffForCity()> style="display: none;" </#if>>
                    <#if behavior.childBehaviorData?has_content && behavior.childBehaviorData?size gt 0>
                        <#list behavior.childBehaviorData as childData>
                        <tr>
                            <td></td>
                            <td>${childData.name}</td>
                            <td>${childData.homeworkTeacherNum}</td>
                            <td>${childData.homeworkTeacherTime}</td>
                            <td>${childData.homeworkStuNum}</td>
                            <td>${childData.homeworkStuTime}</td>
                            <#if !currentUser.isResearchStaffForCounty()><td></td></#if>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </#list>
    <#else>
        <table>
            <tbody>
            <tr>
                <td colspan="12">暂无相关数据</td>
            </tr>
            </tbody>
        </table>
    </#if>
    </div>
</div>