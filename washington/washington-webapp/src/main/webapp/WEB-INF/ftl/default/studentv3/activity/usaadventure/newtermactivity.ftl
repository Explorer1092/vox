<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page title="走遍美国"
pageJs=["travelAmerica"]
pageCssFile={"travelAmerica":["public/skin/project/travelAmerica/student/css/travelAmerica"]}
pageJsFile={"travelAmerica":"public/script/project/travelAmerica"}>

<div class="ta-header"></div>
<div class="ta-main">
    <div class="main01">
        <div class="title"><i class="star-icon"></i><span>小伙伴叫你来领惊喜礼包啦</span><span class="time">活动时间：2016.9.1~2016.9.30</span></div>
        <div class="name" style="padding:1.5rem 2rem 1rem;">每天学习闯关后，即可领取10学豆<i class="beans-icon" style="width:0.8rem;height:0.8rem;"></i>奖励更有精美人物形象等你来领</div>
        <div class="pic"></div>
    </div>
    <div class="main02">
        <div class="title"><i class="star-icon"></i><span>你有信心在新学期掌握多少个单词</span></div>
        <#if learningGoalType??>
            <#if learningGoalType?has_content>
            <div class="selectBox tab-head disabled">
                <div class="item"><i class="radio <#if learningGoalType == "WORDS_100">active</#if>" data-type="WORDS_100"></i><span>100个</span></div>
                <div class="item"><i class="radio <#if learningGoalType == "WORDS_300">active</#if>" data-type="WORDS_300"></i><span>300个</span></div>
                <div class="item"><i class="radio <#if learningGoalType == "WORDS_500">active</#if>" data-type="WORDS_500"></i><span>500个</span></div>
                <div class="item"><i class="radio <#if learningGoalType == "WORDS_600">active</#if>" data-type="WORDS_600"></i><span>600个</span></div>
            </div>
            </#if>
        <#else>
            <div class="selectBox tab-head">
                <div class="item"><i class="radio active" data-type="WORDS_100"></i><span>100个</span></div>
                <div class="item"><i class="radio" data-type="WORDS_300"></i><span>300个</span></div>
                <div class="item"><i class="radio" data-type="WORDS_500"></i><span>500个</span></div>
                <div class="item"><i class="radio" data-type="WORDS_600"></i><span>600个</span></div>
            </div>
        </#if>

        <div class="tips">打开走遍美国学英语，立即领取惊喜大礼</div>
    </div>
</div>
<div class="ta-footer">
    <div class=""><!--吸低加inner类-->
        <a href="javascript:void(0)" class="js-submit btn" data-appkey="11" data-orientation="${orientation!}" data-browser="${browser!}" data-url="/app/redirect/thirdApp.vpage?appKey=UsaAdventure&platform=STUDENT_APP&productType=APPS">领取礼包</a>
    </div>
</div>
</@layout.page>