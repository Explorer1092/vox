<div style="text-align:center;font-size: 14px;">
    <div style="font-size: 16px; margin: 10px 0;">${quizHistory.quizName!}</div>
    <div style="margin: 5px 0;">出卷人：${(quizHistory.paperAuthor)!''}</div>
    <div style="margin: 5px 0 30px;">${quizHistory.schoolName!} ${quizHistory.clazzName!} 总人数:${quizHistory.studentCount!} 参与人数：${quizHistory.joinCount!}</div>
</div>
<#--听力-->
<#if subject == "ENGLISH" && quizHistory.listeningDetails?has_content>
    <table width="550" cellpadding="5" style="border-collapse:collapse; border:1px solid #333; " align="center">
        <tr>
            <td style="border: 1px solid #333; text-align: center; white-space: nowrap;" width="48" nowrap>听力</td>
            <td style="border: 1px solid #333; text-align: center; " width="109">题型</td>
            <td style="border: 1px solid #333; text-align: center; " width="184">考点</td>
            <td style="border: 1px solid #333; text-align: center; " width="64" nowrap>失分率</td>
            <td style="border: 1px solid #333; text-align: center; " width="86" nowrap>正确人数</td>
            <td style="border: 1px solid #333; text-align: center; " width="86" nowrap>错误人数</td>
        </tr>
        <#list quizHistory.listeningDetails as led>
            <tr>
                <td rowspan="2" style="border: 1px solid #333; text-align: center;">${led_index+1}</td>
                <td style="border: 1px solid #333; text-align: center;word-break:break-all; word-wrap:break-word;">${led.questionPattern}</td>
                <td style="border: 1px solid #333; text-align: center;word-break:break-all; word-wrap:break-word;">${led.questionPoint}</td>
                <td style="border: 1px solid #333; text-align: center;">${led.loseScoreRate}%</td>
                <td style="border: 1px solid #333; text-align: center;">${led.rightPersonNum}</td>
                <td style="border: 1px solid #333; text-align: center;">${led.wrongPersonNum}</td>
            </tr>
            <tr>
                <td colspan="5" style="border: 1px solid #333; text-align: center;">
                    <img src="${examImgUrlPrefix}/exam-preview-ENGLISH-${led.questionId}" width="500"/>
                </td>
            </tr>
        </#list>
    </table>
</#if>
<#--英语笔试-->
<#if subject == "ENGLISH" && quizHistory.writtenDetails?has_content>
    <table width="550" cellpadding="5" style="border-collapse:collapse; border:1px solid #333; " align="center">
        <tr>
            <td style="border: 1px solid #333; text-align: center; white-space: nowrap;" width="48" nowrap>笔试</td>
            <td style="border: 1px solid #333; text-align: center; " width="109">题型</td>
            <td style="border: 1px solid #333; text-align: center; " width="184">考点</td>
            <td style="border: 1px solid #333; text-align: center; " width="64" nowrap>失分率</td>
            <td style="border: 1px solid #333; text-align: center; " width="86" nowrap>正确人数</td>
            <td style="border: 1px solid #333; text-align: center; " width="86" nowrap>错误人数</td>
        </tr>
        <#list quizHistory.writtenDetails as wtd>
            <tr>
                <td rowspan="2" style="border: 1px solid #333; text-align: center;">${wtd_index+1}</td>
                <td style="border: 1px solid #333; text-align: center;word-break:break-all; word-wrap:break-word;">${wtd.questionPattern}</td>
                <td style="border: 1px solid #333; text-align: center;word-break:break-all; word-wrap:break-word;">${wtd.questionPoint}</td>
                <td style="border: 1px solid #333; text-align: center;">${wtd.loseScoreRate}%</td>
                <td style="border: 1px solid #333; text-align: center;">${wtd.rightPersonNum}</td>
                <td style="border: 1px solid #333; text-align: center;">${wtd.wrongPersonNum}</td>
            </tr>
            <tr>
                <td colspan="5" style="border: 1px solid #333; text-align: center;">
                    <img src="${examImgUrlPrefix}/exam-preview-ENGLISH-${wtd.questionId}" width="500"/>
                </td>
            </tr>
        </#list>
    </table>
</#if>
<#--数学笔试-->
<#if subject == "MATH" && quizHistory.questionDetail?has_content>
    <table width="550" cellpadding="5" style="border-collapse:collapse; border:1px solid #333; " align="center">
        <tr>
            <td style="border: 1px solid #333; text-align: center; white-space: nowrap;" width="48" nowrap>笔试</td>
            <td style="border: 1px solid #333; text-align: center; " width="109">题型</td>
            <td style="border: 1px solid #333; text-align: center; " width="184">考点</td>
            <td style="border: 1px solid #333; text-align: center; " width="64" nowrap>失分率</td>
            <td style="border: 1px solid #333; text-align: center; " width="86" nowrap>正确人数</td>
            <td style="border: 1px solid #333; text-align: center; " width="86" nowrap>错误人数</td>
        </tr>
        <#list quizHistory.questionDetail as wtd>
            <tr>
                <td rowspan="2" style="border: 1px solid #333; text-align: center;">${wtd_index+1}</td>
                <td style="border: 1px solid #333; text-align: center;word-break:break-all; word-wrap:break-word;">${wtd.questionType}</td>
                <td style="border: 1px solid #333; text-align: center;word-break:break-all; word-wrap:break-word;"><#list wtd.points as points>${points}<#if !wtd.points?exists>,</#if></#list></td>
                <td style="border: 1px solid #333; text-align: center;">${wtd.rate}%</td>
                <td style="border: 1px solid #333; text-align: center;">${wtd.correctCount}</td>
                <td style="border: 1px solid #333; text-align: center;">${wtd.wrongCount}</td>
            </tr>
            <tr>
                <td colspan="5" style="border: 1px solid #333; text-align: center;">
                    <img src="${examImgUrlPrefix}/exam-preview-MATH-${wtd.questionId}" width="500"/>
                </td>
            </tr>
        </#list>
    </table>
</#if>