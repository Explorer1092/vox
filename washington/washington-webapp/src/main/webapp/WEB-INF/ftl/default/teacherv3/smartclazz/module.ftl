<#macro pagecontent mainmenu="" submenu="">
<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page show="smartclazz" showNav="hide">
<@sugar.capsule js=["jmp3", "DD_belatedPNG"] css=["new_teacher.smartclazz"] />
<div class="s-head-box">
    <div class="title">
        <h2>
            <a href="javascript:void(0);" id="smartClazzNewbieGuide" class="smart_newbie_guide">新手引导</a>
            ${clazz.formalizeClazzName()}
            <#include "../block/switchsubjcet.ftl"/>
            <a href="/teacher/smartclazz/list.vpage">其他班级<span>﹥</span></a>
        </h2>
    </div>
    <div class="title_list">
        <ul>
            <li <#if mainmenu=="classroom_reward">class="active"</#if>><a href="/teacher/smartclazz/clazzdetail.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon-well PNG_24 s-icon-well-01"></i>课堂奖励</a></li>
            <li <#if mainmenu=="classroom_ask">class="active"</#if>><a href="/teacher/smartclazz/myquestion.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon-well PNG_24 s-icon-well-02"></i>课堂提问</a></li>
            <#if currentUser.subject == "ENGLISH">
            <li <#if mainmenu=="classroom_resource">class="active"</#if>><a href="/teacher/smartclazzresource/index.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon-well PNG_24 s-icon-well-03"></i>课堂资源</a></li>
            </#if>
        </ul>
    </div>
</div>
<#if mainmenu=="classroom_reward">
    <div class="s-contain-box">
        <div class="list-con-box">
            <div class="s-bean-nav" id="exchangeHoverEnvet">
                <p><strong id="totalIntegral">${(clazzIntegralPool.fetchTotalIntegral())!'--'}</strong></p>
                <a href="javascript:void (0);">兑换学豆</a>
                <div class="t-reward-flowerBean" style="display: none;">
                    <div class="slideFlayerBox">
                        <span class="arrow"></span>
                        <a href="javascript:void(0)" id="exchange_but" data-clazz_id="${clazz.id!''}" style="border: none;">园丁豆兑换</a>
                        <a href="/teacher/flower/exchange.vpage?ref=smartclazz&clazzId=${clazz.id!''}" target="_blank">点赞兑换</a>
                    </div>
                </div>
                <div class="line"></div>
            </div>
            <ul class="s-fl-right lc-box s-magT-10">
                <li id="random_but">
                    <a href="javascript:void(0);"><i class="s-icon s-icon-01 PNG_24"></i><span>随机选择<i class="w-icon-arrow"></i></span></a>
                    <div class="lcb-rang">
                        <p data-value="1">随机选择1名</p>
                        <p data-value="2">随机选择2名</p>
                        <p data-value="3">随机选择3名</p>
                        <p data-value="4">随机选择4名</p>
                        <p data-value="5">随机选择5名</p>
                    </div>
                </li>
                <li><a href="javascript:void(0);"  <#if studentList?? && studentList?size gt 1>id="multiChoiceBtn"<#else> style="cursor: default;"</#if>><i class="s-icon s-icon-02 PNG_24"></i><span>选择多个</span></a></li>
                <li><a href="javascript:void (0);" id="reset_but"><i class="s-icon s-icon-03 PNG_24"></i><span>重置显示数据</span></a></li>
                <li <#if submenu="rewardhistory">class="active"</#if>><a href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/integral/clazzintegral.vpage?ref=smartclazz&clazzId=${clazz.id!''}" target="_blank"><i class="s-icon s-icon-04 PNG_24"></i><span>班级学豆</span></a></li>
                <li <#if submenu="timer">class="active"</#if>><a href="/teacher/smartclazz/timer.vpage?clazzId=${clazz.id!''}&subject=${curSubject!}" target="_blank"><i class="s-icon s-icon-05 PNG_24"></i><span>计时工具</span></a></li>
                <li id="sort_btn">
                    <a href="javascript:void (0);">
                        <i class="s-icon s-icon-06 PNG_24"></i>
                        <span>排序<i class="w-icon-arrow"></i></span>
                    </a>
                    <div class="lcb-rang">
                        <p data-value="INTEGRAL_DESC">按学豆降序排列</p>
                        <p data-value="INTEGRAL_ASC">按学豆升序排列</p>
                        <p data-value="LETTER_ASC">按字母A-Z排序</p>
                        <p data-value="NUMBER_ASC">按学号顺序排序</p>
                        <p data-value="GROUP_ASC">按班级小组排序</p>
                    </div>
                </li>
            </ul>
            <div class="s-clear"></div>
        </div>
    </div>
</#if>

<#if mainmenu=="classroom_ask">
    <div class="s-contain-box">
        <div class="list-con-box">
            <ul>
                <li <#if submenu="myquestion">class="active"</#if>><a href="/teacher/smartclazz/myquestion.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon s-icon-07 PNG_24"></i><span>我的问题</span></a></li>
                <li <#if submenu="synscan">class="active"</#if>><a href="/teacher/smartclazz/questionscan.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon s-icon-08 PNG_24"></i><span>同步扫描</span></a></li>
                <li <#if submenu="myreport">class="active"</#if>><a href="/teacher/smartclazz/questionreport.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon s-icon-09 PNG_24"></i><span>我的报告</span></a></li>
                <#--移动端确认后，打开-->
                <li <#if submenu="iwen">class="active"</#if>><a href="/teacher/smartclazz/iwencard.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon s-icon-10 PNG_24"></i><span>爱提问&学生卡片</span></a></li>
                <li <#if submenu="help">class="active"</#if>><a href="/teacher/smartclazz/help.vpage?clazzId=${clazz.id}&subject=${curSubject!}"><i class="s-icon s-icon-11 PNG_24"></i><span>使用帮助</span></a></li>
            </ul>
        </div>
    </div>
</#if>
<div style="width: 1000px; margin: 0 auto;">
    <#nested>
</div>
<script type="text/javascript">
    $(function(){
        $("#smartClazzNewbieGuide").on("click", function(){
            $17.tongji("互动课堂-新手引导");
            $.prompt(template("t:新手引导", {}), {
                title: "新手引导",
                buttons: {},
                position:{width : 900}
            });
        });
    });
</script>
<#--新手引导-->
<script id="t:新手引导" type="text/html">
    <embed quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" scale="exactfit" src="//cdn.17zuoye.com/static/project/iwen/smartclazz_v2.swf" width="900" height="600" wmode="opaque">
</script>
</@temp.page>
</#macro>