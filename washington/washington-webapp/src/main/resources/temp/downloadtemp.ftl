<#-- 高频错误报告模板 -->
<h4><b>${homeworkHistoryReport.schoolName!''}</b> <span style="font-size: 14px;">${homeworkHistoryReport.className!''}</span></h4>
<h2 style="text-align: center;">高频错误报告</h2>
<#if homeworkHistoryReport?? && homeworkHistoryReport.basicErrorRateList?? && homeworkHistoryReport.basicErrorRateList?size gt 0>
    <h4>基础练习</h4>
    <table border="1" cellpadding="10" bordercolor="#ccc" style="border-collapse: collapse; width: 100%;" >
        <thead>
            <tr>
                <th>序号</th>
                <th style="width: 360px;">知识点</th>
                <#--<#if subject == 'ENGLISH'>
                    <th>题型</th>
                </#if>-->
                <th>班级错误率</th>
                <th>做错人数</th>
            </tr>
        </thead>
        <tbody>
            <#list homeworkHistoryReport.basicErrorRateList as he>
                <tr>
                    <th>${he_index + 1}</th>
                    <th>${he.point!''}</th>
                    <#--<#if subject == 'ENGLISH'>
                        <th>${he.categoryName!''}</th>
                    </#if>-->
                    <th>${he.errorReat!''}%</th>
                    <th>${he.users?size!''}</th>
                </tr>
            </#list>
        </tbody>
    </table>
</#if>

<#if (homeworkHistoryReport.questionInfoMapperList?? && homeworkHistoryReport.questionInfoMapperList?size gt 0)>
    <h4>同步练习</h4>
    <table border="1" cellpadding="10" bordercolor="#ccc" style="border-collapse: collapse; width: 100%;" >
        <thead>
            <tr>
                <th>序号</th>
                <th>知识点</th>
               <#-- <#if subject == 'ENGLISH'>
                    <th>题型</th>
                </#if>-->
                <th>班级错误率</th>
                <th>做错人数</th>
            </tr>
        </thead>
        <tbody>
            <#list homeworkHistoryReport.questionInfoMapperList.errorPointList as questionInfo>
                <tr>
                    <th>${questionInfo_index + 1}</th>
                    <th>
                        <#if questionInfo.knowledgePointList?has_content>
                            <#list questionInfo.knowledgePointList as knowledgePoint>
                                <p>${knowledgePoint}</p>
                            </#list>
                        </#if>
                    </th>
                    <#--<#if subject == 'ENGLISH'>
                        <th>${questionInfo.questionType!}</th>
                    </#if>-->
                    <th>${questionInfo.rate!0}%</th>
                    <th>${questionInfo.users?size}</th>
                </tr>
            </#list>
        </tbody>
    </table>

    <table border="1" cellpadding="10" bordercolor="#ccc" style="border-collapse: collapse; width: 100%; margin-top: 10px;" >
        <thead>
            <tr>
                <td colspan="2">
                    <div style="padding: 30px 0; font: 25px/10px '微软雅黑',Microsoft YaHei,Arial ; ">同步习题高频错误题目</div>
                </td>
            </tr>
        </thead>
        <tbody>

            <#list homeworkHistoryReport.questionInfoMapperList.errorExamList as ql>
                <tr>
                    <td colspan="2">
                        <div style="text-align: right">
                            <span style="display: inline-block; background-color: #ff6600; color: #fff; font-size: 12px; padding: 3px;">
                                班级错误率：${ql.rate!''}%
                            </span>
                        </div>
                        <#--<img src="${ql.picUrl!''}" alt="图片" width="500">-->
                        <div id="examBox"></div>

                    </td>
                </tr>
                <#if ql.errorAnswerList?size gt 0>
                    <tr>
                        <th style="width: 50%">高频错误项</th>
                        <th style="width: 50%">姓名</th>
                    </tr>
                    <#list ql.errorAnswerList as el>
                    <tr>
                        <th>${el.answer!''}</th>
                        <th>
                            <#list el.users as ul>
                                <span>
                                <#if ul.userName != ''>
                                    ${ul.userName!''}
                                <#else>
                                    ${ul.userId!''}
                                </#if>
                                </span>
                            </#list>
                        </th>
                    </tr>
                    </#list>
                </#if>
            </#list>

        </tbody>
    </table>
</#if>
