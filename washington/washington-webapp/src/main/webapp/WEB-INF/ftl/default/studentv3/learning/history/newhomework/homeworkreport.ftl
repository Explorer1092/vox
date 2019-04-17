<#import '../../../layout/layout.ftl' as temp>
<@temp.page pageName='homeworkreport'>
    <@sugar.capsule css=["homeworkhistory.report"] />
<div class="t-center-container">
    <div class="breadcrumb" style="padding: 15px 0;">
        <span><a class="w-blue" href="/student/index.vpage">首页</a> &gt;</span>
        <#if navLink?? && navLink == "history">
            <span><a class="w-blue" href="/student/learning/history/earlylist.vpage?subject=${subjectName}">作业历史</a> &gt;</span>
        <#else>
            <span><a class="w-blue" href="/student/learning/history/list.vpage?subject=${subjectName}">作业历史</a> &gt;</span>
        </#if>

        <span>作业详情</span>
    </div>
    <div class="h-historyBox">
        <div class="h-title">
            <span class="left-text">作业详情</span>
            <span class="J_endTime right-text"></span>
        </div>
        <div class="h-jobsReport">
            <div class="J_unitInfo h-jobsR-intro"></div>
            <div class="J_commentInfo h-jobsR-review"></div>
            <div class="J_practice h-jobsR-content"></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    var $studentv3HomeworkReport = {
        env : <@ftlmacro.getCurrentProductDevelopment />
    };
</script>
    <@sugar.capsule js=["studentreport.report"] />
<#--作业详情页，只有学生完成作业才能看到此页面,学生没有完成作业，作业列表显示补做按钮-->

<script id="t:baseTemplate" type="text/html">
    <div class="contentList">
        <div class="hd clear">
            <div class="title listLeft"><%=title%></div>
            <%if(tab != 'OCR_MENTAL_ARITHMETIC' && tab != 'OCR_DICTATION'){%>
            <div class="hWork-btns listRight">
                <a href="javascript:void(0);" url="/student/learning/history/newhomework/homeworkdetail.vpage?homeworkId=<%=homeworkId%>&type=<%=tab%>&subject=<%=subject%>" class="hw-btn hw-btn-green">查看答题详情</a>
            </div>
            <%}%>
        </div>
        <div class="mn clear">
            <%
             if(tab == 'BASIC_APP' || tab == 'LS_KNOWLEDGE_REVIEW' || tab == 'NATURAL_SPELLING'){
                include('t:basicapp')
                include('t:scoretime')
             }else if(tab == 'READING'){
                include('t:reading')
                include('t:scoretime')
             }else if(tab == 'PHOTO_OBJECTIVE' || tab == 'VOICE_OBJECTIVE' || tab == 'READ_RECITE'){
                include('t:subjective')
             }else if(tab == 'ORAL_PRACTICE'){
                include('t:oral_practice')
                include('t:scoretime')
             }else if(tab == 'NEW_READ_RECITE'){
                include('t:newreadrecite')
             }else if(tab == 'DUBBING'){
                include('t:dubbing')
             }else if(tab == 'READ_RECITE_WITH_SCORE'){
                include('t:READ_RECITE_WITH_SCORE')
             }else if(tab == 'OCR_MENTAL_ARITHMETIC' || tab == 'OCR_DICTATION'){
                include('t:exam')
                include('t:OCR_MENTAL_ARITHMETIC_DESC')
             }else if(tab == 'ORAL_COMMUNICATION'){
                include('t:scoretime')
            }else{
                include('t:exam')
                include('t:scoretime')
            }%>
        </div>
    </div>
</script>
<script id="t:exam" type="text/html">
    <div class="listLeft">
        <div class="judgeBox textGreen"><i class="judge-icon"></i>正确<span class="font18"><%=rightCount%></span>题</div>
        <div class="judgeBox textRed"><i class="judge-icon"></i>错误<span class="font18"><%=wrongCount%></span>题</div>
    </div>
</script>
<script id="t:subjective" type="text/html">
    <div class="listLeft">
        <div class="judgeBox textGreen"><i class="judge-icon-2"></i>已完成<span class="font18"><%=rightCount%></span>题</div>
    </div>
</script>
<script id="t:basicapp" type="text/html">
    <div class="listLeft" >
        <div class="judgeBox textGreen"><i class="judge-icon-2"></i>完成<span class="font18"><%=completePracticeCount%></span>个练习</div>
    </div>
</script>
<script id="t:reading" type="text/html">
    <div class="listLeft" >
        <div class="judgeBox textGreen"><i class="judge-icon-2"></i>完成<span class="font18"><%=completePracticeCount%></span>本</div>
    </div>
</script>
<script id="t:oral_practice" type="text/html">
    <div class="listLeft">
        <div class="judgeBox textGreen"><i class="judge-icon-2"></i>完成<span class="font18"><%=completePracticeCount%></span>道题</div>
    </div>
</script>
<script id="t:newreadrecite" type="text/html">
    <div class="listLeft">
        <div class="judgeBox textGreen"><i class="judge-icon"></i>已完成<span class="font18"><%=completePracticeCount%></span>篇</div>
    </div>
    <div class="listRight">
        <div class="time textRed"><%=time%></div>
        <div class="time textGreen"><%=state%></div>
    </div>
</script>
<script id="t:dubbing" type="text/html">
    <div class="listLeft">
        <div class="judgeBox textGreen"><i class="judge-icon"></i>完成<span class="font18"><%=completePracticeCount%></span>集</div>
    </div>
    <div class="listRight">
        <div class="time textRed"><%=time%></div>
    </div>
</script>
<script id="t:scoretime" type="text/html">
    <div class="listRight">
        <div class="time textRed"><%=time%></div>
    </div>
</script>

<script id="t:errorQuestionCorrect" type="text/html">
    <div class="reviewCount">
        <div class="rText">有<%=(totalNeedCorrectedNum - finishCorrectedCount)%>道题需要订正</div>
        <div class="hWork-btns btn-right">
            <%if(totalNeedCorrectedNum > finishCorrectedCount){%>
            <a href="javascript:void(0);" data-url="/student/homework/index.vpage?from=history&homeworkId=<%=homeworkId%>" class="goCorrect hw-btn hw-btn-yellow">错题订正</a>
            <%}else{%>
            <a href="javascript:void(0);" class="hw-btn hw-btn-disabled">错题订正</a>
            <%}%>
        </div>
    </div>
</script>

<script id="t:READ_RECITE_WITH_SCORE" type="text/html">
    <div class="listLeft">
        <div class="judgeBox textGreen"><i class="judge-icon"></i>达标<span class="font18"><%=rightCount%></span>篇</div>
        <div class="judgeBox textRed"><i class="judge-icon"></i>未达标<span class="font18"><%=wrongCount%></span>篇</div>
    </div>
    <div class="listRight">
        <div class="time textRed"></div>
        <div class="time textGreen"></div>
    </div>
</script>

<script id="t:OCR_MENTAL_ARITHMETIC_DESC" type="text/html">
    <div class="listRight">
        <div class="time">打开"一起作业学生"APP查看详情</div>
    </div>
</script>
</@temp.page>




