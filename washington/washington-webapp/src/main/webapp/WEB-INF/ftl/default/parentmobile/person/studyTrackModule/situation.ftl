<div class="parentApp-pathSection  parentApp-pathWhite">
    <div class="parentApp-pathHead">
        <div class="hd">学习概况</div>
    </div>
    <div class="parentApp-pathData">
        <div>
            <div class="hd">${infos.hwAvgScore}<span>%</span></div>
            <div class="ft">准确率</div>
        </div>
        <div>
            <div class="hd">${infos.surpassCount}</div>
            <div class="ft">本月超过同学</div>
        </div>
        <div>
            <div class="hd">${infos.questionNum}</div>
            <div class="ft">本月完成题数</div>
        </div>
    </div>

<#function getLevelDisplay level subjectDisplay>
    <#assign levelDisplay = {
        "level" : level,
        "display" : "未知",
        "code" : 0,
        "style" : "",
        "comment" : ""
    }>

    <#if level lt 70>
        <#assign levelDisplay = {
        "level" : level,
        "display" : "一般",
        "code" : 1,
        "style" : "red",
        "comment" : "${subjectDisplay}能力一般, 请家长多关注${subjectDisplay}作业及完成情况，帮助宝贝提升成绩！"
        }>
    <#elseif level lte 90>
        <#assign levelDisplay = {
        "level" : level,
        "display" : "良好",
        "code" : 2,
        "style" : "yellow",
        "comment" : "${subjectDisplay}能力良好，还有提升空间，请继续加油吧！"
        }>
    <#elseif level lte 100>
        <#assign levelDisplay = {
        "level" : level,
        "display" : "优秀",
        "code" : 3,
        "style" : "green",
        "comment" : "${subjectDisplay}能力优秀，感谢家长对宝贝学习的关注与支持，请继续保持哦！"
        }>
    </#if>

    <#return levelDisplay>
</#function>

<#assign
englisEval = infos.englishEvaluating!-1
mathEval = infos.mathEvaluating!-1
levelObj = [
    {
        "subject" : "ENGLISH",
        "subjectDisplay" : "英语",
        "isExist"  : infos.hasEnglishTeacher,
        "eval" : englisEval,
        "levelInfo" : getLevelDisplay(englisEval, "英语")
    },
    {
        "subject" : "MATH",
        "subjectDisplay" : "数学",
        "isExist"  : infos.hasMathTeacher,
        "eval" : mathEval,
        "levelInfo" : getLevelDisplay(mathEval, "数学")
    }
]
>
    
<#include "../../constants.ftl">
<#assign filterLevelObj = filter(levelObj, "isExist", true)>

    <div class="parentApp-pathText">
        <#list filterLevelObj as leveInfo>
            <#if leveInfo.eval != -1>
                <#if leveInfo_index == 0>${studentName} </#if>${leveInfo.levelInfo.comment}
            </#if>
        </#list>
    </div>
    <div class="parentApp-pathBox">
        <#list filterLevelObj as levelInfo>
            <#if levelInfo.eval != -1>
                <div class="parentApp-pathStatus">
                    <div class="headBox">
                        <div class="line line-${levelInfo.levelInfo.style}"></div>
                        <div class="cont">
                            <div class="white">${levelInfo.subjectDisplay}</div>
                            <#list ["一般", "良好", "优秀"] as levelDisplay>
                                <div <#if (levelDisplay_index + 1) lte levelInfo.levelInfo.code>class="white"</#if> >${levelDisplay}</div>
                            </#list>
                        </div>
                    </div>
                </div>
            </#if>
        </#list>
    </div>
</div>
