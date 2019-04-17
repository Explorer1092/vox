<#escape x as x?html>
<#include "../../constants.ftl">
<style>
    .canvasParent{
        width: 170px;
        height: 170px;
        margin: 22px 0;
        float: left;
        overflow: visible;
        -webkit-transform: translateZ(0);
        transform: translateZ(0);
    }
</style>
    <#assign infos = infos!{
    "currentWeekAvgScore" : 0,
    "beforeWeekAvgScore" : 0,
    "learnWordsCount" : 0,
    "avglearnwords" : 0
    }>

    <#assign mathDiff = infos.currentWeekAvgScore - infos.beforeWeekAvgScore englishDiff = infos.learnWordsCount - infos.avglearnwords studentName = studentName!"">

    <#assign rankInfos = [
    <#-- 暂且注释英语 http://project.17zuoye.net/redmine/issues/23727k
    {
        "subjectDisplay" : "英语",
        "myRankInfo" : infos.myRank,
        "firstRankInfo" : infos.firstRank,
        "polarAreaClass" :  "PolarAreaEnglish",
        "lineClass" :  "LineEnglish",
        "haveNoMoreHomeworks" : infos.needLearnWordsCount == 0,
        "isExist"  : infos.hasEnglishTeacher,
        "shareTrack" :  "report|en_transcript_click"
    }
	-->
    <#--  TODO 因数学结构的改变,暂且隐藏
    ,{
        "subjectDisplay" : "数学",
        "myRankInfo" : infos.myRank,
        "firstRankInfo" : infos.firstRank,
        "polarAreaClass" :  "PolarAreaMath",
        "lineClass" :  "LineMath",
        "haveNoMoreHomeworks" : infos.beforeWeekAvgScore == 0 || infos.currentWeekAvgScore == 0,
        "isExist"  : infos.hasMathTeacher,
        "shareTrack" :  "report|math_transcript_open"
    }
    -->
    ]>

    <#function getRankDisplay firstRankInfo myRankInfo subjectDisplay>
        <#assign displayPrefix = "在${subjectDisplay}作业中"  index = myRankInfo.index>

        <#if index == 1>
            <#return "${displayPrefix}，${studentName}获得学霸榜排名第1的好成绩!">
        </#if>

        <#assign notFirstDisplayTemp = "${displayPrefix}, ${studentName}共获得${myRankInfo.smCount}次学霸，与学霸数最大的${firstRankInfo.studentName}同学相差${firstRankInfo.smCount - myRankInfo.smCount}次，" >
        <#if index lte 5>
            <#return "${notFirstDisplayTemp}继续努力就有可能得第一哦">
        <#elseif index lte 10>
            <#return "${notFirstDisplayTemp}有一定进步空间，要继续加油哦">
        <#elseif index lte 20>
            <#return "${notFirstDisplayTemp}进步空间较大哦">
        <#elseif index gt 20>
            <#return "${notFirstDisplayTemp}请家长即使关注分数变化，帮助宝贝巩固成绩">
        </#if>

        <#return "" >
    </#function>

    <#function getChartDisplay subjectDisplay>
        <#if subjectDisplay == "数学">
            <#if mathDiff  == 0>
                <#return "${studentName}本周数学成绩与上周持平，请继续加油！">
            <#elseif mathDiff lt 0>
                <#return "${studentName}本周数学成绩比上周降低${(mathDiff)?abs}分， 要努力哦！">
            <#elseif mathDiff gt 0>
                <#return "${studentName}本周数学成绩比上周提高${mathDiff}分， 有进步哦！">
            </#if>
        <#elseif subjectDisplay == "英语">
            <#assign firstWordDiff = infos.firstWordsCount - infos.learnWordsCount >
            <#if firstWordDiff == 0>
                <#return "${studentName}掌握的词汇量是班级第一！">
            </#if>

            <#if englishDiff  == 0>
                <#return "${studentName}掌握的词汇量与班平均数持平，若要提前完成教学要求，还要多加努力哦！">
            <#elseif englishDiff lt 0>
                <#return "${studentName}掌握的词汇量低于班平均数，多努力才能达到教学要求哦！">
            <#elseif englishDiff gt 0>
                <#return "${studentName}掌握的词汇量已超过班平均数，再增加 ${firstWordDiff}个就是班级第一啦！">
            </#if>
        </#if>

        <#return "" >
    </#function>

    <#list rankInfos as rankInfo>
        <#if rankInfo.isExist >
            <#assign subjectDisplay = rankInfo.subjectDisplay?trim >
        <div class="parentApp-pathSection">
            <div class="parentApp-pathHead">
                <div class="hd">${subjectDisplay}成绩</div>
            </div>

            <div class="parentApp-pathContent">
                <#if rankInfo.haveNoMoreHomeworks >
                    <div class="parentApp-pathNull">本周暂无有效数据</div>
                <#else>

                    <#--  TODO  v1.3.2 暂且隐藏折线图 如折线图需要打开，则将注释打开即可
                    <div class="parentApp-pathCanvas ${rankInfo.lineClass}Block hide">
                        <canvas class="${rankInfo.lineClass}"></canvas>
                        <ul class="list">
                            <li class="purple">班级平均分</li>
                            <li class="green">学生成绩分</li>
                        </ul>
                    </div>
                    -->

                    <div class="parentApp-pathCanvas">
                        <div class="parentApp-pathText">${getChartDisplay(rankInfo.subjectDisplay)}</div>
                        <#if subjectDisplay == "数学">
                            <#if mathDiff gt 0>
                                <div class="round round-green">+${mathDiff}</div>
                            <#elseif mathDiff == 0>
                                <div class="round round-yellow">持平</div>
                            <#else>
                                <div class="round round-red">-${(mathDiff)?abs}</div>
                            </#if>

                            <#if mathDiff gt 0>
                                <#assign diffInfo = {
                                    "display" : "本周成绩趋势提高${mathDiff}分",
                                    "class" : "green"
                                }>
                            <#elseif mathDiff == 0>
                                <#assign diffInfo = {
                                    "display" : "本周数学成绩与上周持平，请继续加油！",
                                    "class" : "yellow"
                                }>
                            <#else>
                                <#assign diffInfo = {
                                    "display" : "本周成绩趋势降低${(mathDiff)?abs}分",
                                    "class" : "red"
                                }>
                            </#if>

                            <ul class="list">
                                <li class="purple">上周平均成绩${infos.beforeWeekAvgScore}分</li>
                                <li class="blue">本周平均成绩${infos.currentWeekAvgScore}分</li>
                                <li class="${diffInfo.class}" >${diffInfo.display}</li>
                            </ul>

                            <#assign
                            sharemodule = "math"
                            shareUrlSearch = "title=${studentName}的数学成绩单&beforeWeekAvgScore=" + (infos.beforeWeekAvgScore!0) + "&currentWeekAvgScore=" + (infos.currentWeekAvgScore!0) + "&diff=" + (diffInfo.display!"")
                            >

                        <#elseif subjectDisplay == "英语">
                            <div class="canvasParent">
                                <canvas class="${rankInfo.polarAreaClass}" style="width: 100%;"></canvas>
                            </div>
                            <ul class="list">
                                <li class="purple">教学要求掌握${infos.needLearnWordsCount}个</li>
                                <li class="blue">班平均掌握${infos.avglearnwords}个</li>
                                <li class="green">${studentName}掌握${infos.learnWordsCount}个</li>
                            </ul>

                            <#assign
                            sharemodule = "english"
                            shareUrlSearch = "title=${studentName}的英语成绩单&needLearnWordsCount=" + (infos.needLearnWordsCount!0) + "&avglearnwords=" + (infos.avglearnwords!0) + "&learnWordsCount=" + (infos.learnWordsCount!0)
                            >

                        </#if>
                    </div>
                    <a href="/parentMobile/homework/share.vpage?module=${sharemodule}&studentName=${studentName}&${shareUrlSearch}" ${buildTrackData(rankInfo.shareTrack)}  class="parentApp-pathBtn doTrack">分享${subjectDisplay}成绩单</a>
                </#if>
            </div>
        </div>
        </#if>
    </#list>

<script>
    PM.PolarAreaChart = {
        English : {
            target : "${infos.needLearnWordsCount}",
            source : "${infos.avglearnwords}",
            result : "${infos.learnWordsCount}"
        }
    };
</script>
</#escape>
