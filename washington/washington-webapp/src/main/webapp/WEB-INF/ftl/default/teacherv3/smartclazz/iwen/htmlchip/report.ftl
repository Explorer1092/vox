<#if reports?has_content && reports.getContent()?has_content>
<#list reports.getContent() as report>
    <div class="s-exercise-box">
        <div class="se-l s-fl-right">
            <div class="s-tree-detail s-tree-detail-small">
                <p>答题人数：${report.studentAnswerCount} 人  正确率：   ${((report.correctAnswerCount * 100)/report.studentAnswerCount)?int}%</p>
                <ul>
                    <li style="height: ${(report.answerCountA * 100)/report.studentAnswerCount}%" class="s-1 <#if report.answer == "A">green</#if>"><span>${(report.answerCountA)!0}人</span>
                        <#if report.correctAnswerCount gt 0 && report.answer == "A"><i class="s-bean js-reward-bean"></i></#if>
                    </li>
                    <li style="height: ${(report.answerCountB * 100)/report.studentAnswerCount}%" class="s-2 <#if report.answer == "B">green</#if>"><span>${(report.answerCountB)!0}人</span>
                        <#if report.correctAnswerCount gt 0 && report.answer == "B"><i class="s-bean js-reward-bean"></i></#if>
                    </li>
                    <li style="height: ${(report.answerCountC * 100)/report.studentAnswerCount}%" class="s-3 <#if report.answer == "C">green</#if>"><span>${(report.answerCountC)!0}人</span>
                        <#if report.correctAnswerCount gt 0 && report.answer == "C"><i class="s-bean js-reward-bean"></i></#if>
                    </li>
                    <li style="height: ${(report.answerCountD * 100)/report.studentAnswerCount}%" class="s-4 <#if report.answer == "D">green</#if>"><span>${(report.answerCountD)!0}人</span>
                        <#if report.correctAnswerCount gt 0 && report.answer == "D"><i class="s-bean js-reward-bean"></i></#if>
                    </li>
                    <li class="rightstudents" style="display: none;">
                        <#if rightStudents[report.questionId]?has_content>${json_encode(rightStudents[report.questionId])}<#else>[]</#if>
                    </li>
                </ul>
                <div class="st-foot">
                    <p><strong>A</strong></p>
                    <p><strong>B</strong></p>
                    <p><strong>C</strong></p>
                    <p><strong>D</strong></p>
                </div>
            </div>
            <a class="s-btn-mini s-fl" href="/teacher/smartclazz/studentanswer.vpage?id=${report.id}&clazzId=${clazzId}">查看详情<span class="s-all s-arrow-black"></span></a>
        </div>
        <div class="se-r s-fl-left">
            <ul>
                <#if questionMap?has_content && questionMap[report.questionId]?has_content>
                    <li> ${questionMap[report.questionId].topicContent}</li>
                    <#if questionMap[report.questionId].options?has_content>
                        <#list questionMap[report.questionId].options?keys as key>
                            <li> ${key}.${questionMap[report.questionId].options[key]}</li>
                        </#list>
                    </#if>
                <#else>
                    <li>暂时无法查看题的详情</li>
                </#if>
            </ul>
        </div>
        <div class="s-clear"></div>
    </div>
</#list>
<div class="t-show-box">
    <div class="w-turn-page-list">
        <#if !reports.isFirst()>
        <a v="prev" href="javascript:void(0);" data-page="${reports.number}" class="enable back questionPage" style=""><span>上一页</span></a>
        </#if>
        <a href="javascript:void(0);" class="this"><span id="currentPage">${reports.number + 1}</span></a>
        <span>/</span>
        <a href="javascript:void(0);" class="total"><span id="totalPage">${reports.totalPages}</span></a>
        <#if !reports.isLast()>
            <a v="next" href="javascript:void(0);" data-page="${reports.number}" class="enable next questionPage" style=""><span>下一页</span></a>
        </#if>
    </div>
</div>

<#else>
    <div class="s-exercise-box">${msg!"暂无报告"}</div>
</#if>
