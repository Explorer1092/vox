<#import "../layout.ftl" as homework>
<@homework.page title="${studentName!''}的成绩单" pageJs="">
<div class="schoolReport">
    <div class="content">
        <h2 class="nope"></h2>
        <#if report?? && report?has_content>
            <#assign homeworkTotalCount = report.homeworkTotalCount!0>
            <#assign completeHomeworkCount = report.completeHomeworkCount!0>
            <#assign curWeekHomeworkCount = report.curWeekHomeworkCount!0>
            <#assign curWeekCompleteCount = report.curWeekCompleteCount!0>
            <#assign teacher = report.teacher![]>

            <#if homeworkTotalCount == 0>
                <p class="title">${teacher}老师<span class="text_red">尚未布置作业，</span>请及时联系老师来布置作业。 </p>
            <#elseif (homeworkTotalCount > 0 && curWeekHomeworkCount == 0)>
                <p class="title">${teacher}老师<span class="text_red">上周没有布置作业，</span>请及时联系老师来布置作业。 </p>
                <h3>作业完成情况：</h3>
                <div class="schoolReport_con">
                    <p>老师总共布置了<span>${homeworkTotalCount}</span>次作业</p>

                    <P>${studentName!''}完成了<span>${completeHomeworkCount!0}</span>次</P>
                </div>
            <#elseif (curWeekHomeworkCount > 0 && curWeekCompleteCount == 0)>
                <h3>作业完成情况：</h3>
                <div class="schoolReport_con">
                    <P>老师总共布置了<span>${homeworkTotalCount}</span>次作业</P>

                    <P>${studentName!''}完成了<span>${completeHomeworkCount!0}</span>次</P>
                </div>
            <#else>
                <#assign curWeekAvgScore = report.curWeekAvgScore!0>
                <#assign lastWeekAvgScore = report.lastWeekAvgScore!0>
                <#if curWeekAvgScore gt lastWeekAvgScore>
                    <p class="title">
                        我的孩子上周英语作业<span>进步了</span>，请继续保持。
                    </p>
                <#elseif (curWeekAvgScore < lastWeekAvgScore)>
                    <!-- 学生退步了 -->
                <#else>
                    <!-- 成绩稳定 -->
                    <p class="title">我的孩子近期英语作业<span>稳定</span>，请继续保持。</p>
                </#if>

                <#if report.first?? && report.first.studentId != (studentId)!0>
                    <ul class="list">
                        <li>
                            <h3>词汇量</h3>
                            <div class="schoolReport_con">
                                <p>
                                    教学要求掌握${report.needLearnWordsCount!0}个，班平均掌握
                                    <#if report.avglearnwords??>
                                        ${report.avglearnwords!0}
                                    <#else>
                                        0
                                    </#if>
                                    个
                                </p>
                                <#if report.learnWordsCount??>
                                    <p>
                                        ${studentName!''}掌握
                                        <span class="text_red">${report.learnWordsCount!0}</span>个
                                    </p>
                                </#if>
                            </div>
                        </li>

                        <#if (report.clazzlevel?? && report.clazzlevel?number gt 3) && ((report.listenrate)?? || (report.avglistenrate)??)>
                            <li>
                                <h3>听力题</h3>

                                <div class="schoolReport_con">
                                    <#if report.avglistenrate??>
                                        <p>班平均正确率<span class="text_red">${report.avglistenrate!0}%</span></p>
                                    </#if>
                                    <#if report.listenrate??>
                                        <p>
                                            ${studentName!''}正确率
                                            <span class="text_red">${report.listenRate!''}%</span>
                                        </p>
                                    </#if>
                                </div>
                            </li>
                        </#if>
                    </ul>
                <#else>
                    <#assign learnWordRank = report.learnWordRank>
                    <#assign listenRateRank = report.listenRateRank>
                    <ul class="list">
                        <li>
                            <div class="schoolReport_con">
                                <#if ( report.learnWordRank??  && learnWordRank gt 0)>
                                    <p>词汇掌握量在班内排第<span class="text_red">${learnWordRank}</span>名</p>
                                </#if>
                                <#if ( report.listenRateRank??  && listenRateRank gt 0)>
                                    <p>听力正确率在班内排第<span class="text_red">${listenRateRank}</span>名</p>
                                </#if>
                            </div>
                        </li>
                    </ul>
                </#if>
            </#if>
        <#else>
            未查询到作业周报
        </#if>
        <div class="schoolReport_foot">
            <dl>
                <dt></dt>
                <dd>
                    <p>关心孩子学习，从关注<span>一起作业家长通</span>开始</p>
                    <p>欢迎关注微信号：<span>yiqijiazhang </span>或搜 <span>一起作业家长通</span></p>
                </dd>
                <dd>
                    <p class="sf">一起作业网</p>
                </dd>
            </dl>
        </div>
    </div>
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'weekly_report',
                op: 'weekly_report_pv_share_report'
            })
        })
    }
</script>
</@homework.page>

