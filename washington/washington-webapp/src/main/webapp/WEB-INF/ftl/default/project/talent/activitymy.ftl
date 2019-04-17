<!doctype html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>2013暑假单词挑战赛 - 一起作业网</title>
    <@app.css href="public/skin/project/talent/css/skin.css" />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div class="header">
    <#if inAreaForAfenti?? && inAreaForAfenti>
        <a href="/student/index.vpage#index=0&url=/student/afenti/talent/index.vpage&fullScreen=1" onclick="statistics()" class="myParticipate"></a>
    <#else>
        <a href="javascript:void(0);" onclick="statistics()" class="myParticipate"></a>
    </#if>
</div>
<div class="main">
<p class="processOne">
    <span style="color:#f00;">1. 购买单词达人即可参加我爱背单词大赛</span> &nbsp;&nbsp;
        <#if inAreaForAfenti?? && inAreaForAfenti>
            <a href="/apps/afenti/order/talent-cart.vpage" class="learnMore">立即购买</a>
        </#if>
       <br>
    2. 大赛分为：1-2年级组、3-4年级组、5-6年级组<br/>
    3. 每个年级组分为日榜、周榜、总榜<br/>
    4. 比赛从每个关卡的第一关开始，根据参赛者掌握的单词数量、准确率和答题时长进行排名<br/>
    5. 本次大赛只针对以下五个地区开放：<br/>
    &nbsp;&nbsp;&nbsp;北京海淀区、辽宁省沈阳市沈河区、吉林省四平市铁西区、湖南省长沙市天心区、湖南省湘潭市雨湖区。
</p>
<p class="processTwo">
    1. 每个年级组日榜排名第一名奖励<strong class="clrOrange">30学豆</strong>，每天凌晨更新<br/>
    2. 每个年级组周榜排名第一名奖励<strong class="clrOrange">100学豆</strong>，每周日凌晨更新<br/>
    3. 每个年级组总榜排名第一名奖励<strong class="clrOrange">10000学豆</strong>，大赛结束后统一发放<br />
    说明：各排行榜按照掌握单词数量进行排名，掌握相同单词数量的情况再按照准确率进行排名，在相同准确率的情况再按照答题时长进行排名。
</p>
<div class="billboardMain">
<h1 class="schoolyard"></h1>

<#if myTotalRank?exists && myTotalRank gt 0>
    <#if myDayRank <= 500 && myDayRank gt 0>
        <#if myTotalRank <= 500 >
            <p class="billInfo">您现在日榜<b>排名第${myDayRank}名</b>，总榜排名<b>第${myTotalRank}名</b>，继续努力，加油！</p>
        <#else>
            <p class="billInfo">您现在日榜<b>排名第${myDayRank}名</b>，总榜排名<b>已在500名之外</b>，还需要继续努力哦！</p>
        </#if>
    <#else>
        <#if myTotalRank <= 500>
            <p class="billInfo">您现在日榜<b>已在500名之外</b>，总榜排名<b>第${myTotalRank}名</b>，继续努力，加油！</p>
        <#else>
            <p class="billInfo"> 您现在日榜<b>已在500名之外</b>，总榜排名<b>已在500名之外</b>，还需要继续努力哦！</p>
        </#if>
    </#if>
</#if>


<ul class="listBox">
    <li class="title">日榜</li>
    <#if dayReport?? && dayReport?size gt 0>
        <@studentTalentInfo dayReport/>
    <#else>
        <li>数据更新中</li>
    </#if>
</ul>
<ul class="listBox">
    <li class="title title_1">周榜</li>
    <#if weekReport?? && weekReport?size gt 0>
        <@studentTalentInfo weekReport/>
    <#else>
        <li>数据更新中</li>
    </#if>
</ul>
<ul class="listBox">
    <li class="title title_2">总榜</li>
    <#if totalReport?? && totalReport?size gt 0>
        <@studentTalentInfo totalReport/>
    <#else>
       <li>数据更新中</li>
    </#if>
</ul>
</div>
<p class="foot">
    <a href="http://weibo.com/yiqizuoye" class="postnAbs" target="_blank" style="width: 144px; height: 46px; top: 140px; left: 273px;" title="关注一起作业"></a>
    <a href="http://17zuoyeweixin.diandian.com/post/2013-03-14/40049678671" target="_blank" class="postnAbs" style="width: 194px; height: 46px; top: 140px; left: 704px;" title="关注一起作业微信"></a>
</p>
</div>
<div class="footer">声明：如有举报作弊行为经查证属实，将取消参赛资格，一起作业网对本次活动拥有最终解释权。</div>
<@sugar.site_traffic_analyzer_end />
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>

<#--榜单中个人信息的显示-->
<#macro studentTalentInfo talentReport>
    <#list talentReport as afentiTalentReport>
    <li class="rank_${afentiTalentReport_index + 1}">
        <dl>
            <dt>${afentiTalentReport_index + 1}</dt>
            <dd>
                <p class="picture"><img src="<@app.avatar href="${(afentiTalentReport.imgUrl)!}"/>" onerror="this.onerror='';this.src='<@app.avatar href=""/>'" alt="头像"></p>
                <p class="names">姓名：${(afentiTalentReport.userName)!""}</p>
                <#-- <p class="school">学校：<span>${(afentiTalentReport.schoolName)!}</span></p> -->
                ${controller.getSafeStudentSchoolInfoHtml((afentiTalentReport.schoolName)!)}
                <p class="statis">
                    <span class="word">单词数：${(afentiTalentReport.passWordNum)!0}</span>
                    <span class="exact">准确率${((afentiTalentReport.passRate)!0)?string("###.00")}%</span>
                </p>
            </dd>
        </dl>
        <p class="clear"></p>
    </li>
    </#list>
</#macro>