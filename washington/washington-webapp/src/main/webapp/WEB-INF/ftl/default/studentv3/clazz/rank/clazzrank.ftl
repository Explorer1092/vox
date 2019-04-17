<#--<#import 'module.ftl' as temp>-->
<#--<@temp.page title="排名及奖励">-->
<#--<div class="rank-container">-->
    <#--<div class="inner">-->
        <#--<div class="rk-list">-->
            <#--<div class="info">-->
                <#--<#if ['01', '02', '03', '04']?seq_contains(.now?string("dd"))>-->
                    <#--全国前3名（当前为${.now?string("MM")}月的分数和排名，${.now?string("MM")}月分数和排名将在${.now?string("MM")}月05日00:00开始统计）：-->
                <#--<#else>-->
                    <#--全国前3名（${.now?string("MM")}月<span class="currentMonthCount"></span>日24:00时的排名为最终奖励排名，奖品届时发放）：-->
                <#--</#if>-->
            <#--</div>-->
            <#--<ul>-->
                <#--<#list rankList as rl>-->
                    <#--<#if rl_index lt 3>-->
                        <#--<li class="m-${rl_index+1}">-->
                            <#--<div class="sl-name">${rl.schoolName} ${rl.clazzName} </div>-->
                            <#--<div class="sl-level">-->
                                <#--<span class="sl-l">${rl.level}级</span>-->
                            <#--</div>-->
                            <#--<div class="sl-count">${rl.score}分</div>-->
                        <#--</li>-->
                    <#--</#if>-->
                <#--</#list>-->
                <#--<li class="m-4">-->
                    <#--<div class="sl-name">${(currentStudentDetail.studentSchoolName)!} <span style="display:inline-block;">${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}</span></div>-->
                    <#--<#if ['01', '02', '03', '04']?seq_contains(.now?string("dd"))>-->
                        <#--<#if (myClazzRank.rank)?? && myClazzRank.rank != 0>-->
                        <#--<div class="sl-level">-->
                            <#--<span class="sl-r">第${myClazzRank.rank}名</span>-->
                            <#--<a href="javascript:void(0);" class="sl-l data-myLevelBtn">${myClazzRank.level!0}级</a>-->
                        <#--</div>-->
                            <#--<div class="sl-count">-->
                            <#--${myClazzRank.score!0}分-->
                            <#--</div>-->
                        <#--<#else>-->
                            <#--<div class="sl-count" style="margin-top: 34px;"><span style="font-size: 16px;">本月排名5日开始统计</span></div>-->
                        <#--</#if>-->
                    <#--<#else>-->
                        <#--<div class="sl-level">-->
                            <#--<span class="sl-r">第<#if (myClazzRank.rank)?? && myClazzRank.rank != 0>${myClazzRank.rank}<#else>10000+</#if>名</span>-->
                            <#--<a href="javascript:void(0);" class="sl-l data-myLevelBtn">${myClazzRank.level!0}级</a>-->
                        <#--</div>-->
                        <#--<div class="sl-count">-->
                            <#--${myClazzRank.score!0}分-->
                        <#--</div>-->
                    <#--</#if>-->
                    <#--<div class="level-block-content data-myLevelContent" style="display: none ; ">-->
                        <#--<div class="location-bar">-->
                            <#--<div class="m-in" style="width: <#if myClazzRank.level?? && myClazzRank.level gt 0>${(myClazzRank.level!0)*16.6}</#if>%;">-->
                                <#--<div class="m-int">${myClazzRank.score!0}</div>-->
                            <#--</div>-->
                        <#--</div>-->
                        <#--<#assign myClazzContent = "无特权" myClazzBoxLeft = 20 myClazzArrowLeft = 50 />-->
                        <#--<#switch myClazzRank.level!0>-->
                            <#--<#case 0><#assign myClazzContent = "无特权" myClazzBoxLeft = 20 myClazzArrowLeft = 50 /><#break />-->
                            <#--<#case 1><#assign myClazzContent = "达到1级，赠送PK套装，男-【暗影骑士】，女-【魔女】套装" myClazzBoxLeft = 60 myClazzArrowLeft = 50 /><#break />-->
                            <#--<#case 2><#assign myClazzContent = "达到2级，全班每日砸蛋/抽奖次数+1" myClazzBoxLeft = 138 myClazzArrowLeft = 50 /><#break />-->
                            <#--<#case 3><#assign myClazzContent = "达到1级，赠送PK套装，男-【暗影骑士】，女-【魔女】套装" myClazzBoxLeft = 221 myClazzArrowLeft = 50 /><#break />-->
                            <#--<#case 4><#assign myClazzContent = "达到4级，送超稀有，超值属性的10级绿色武器——【玄机重剑】or【玄机魔枪】or【玄机法杖】" myClazzBoxLeft = 221 myClazzArrowLeft = 130 /><#break />-->
                            <#--<#case 5><#assign myClazzContent = "达到5级，全班每人每次完成作业或者测验额外增加1个学豆" myClazzBoxLeft = 221 myClazzArrowLeft = 210 /><#break />-->
                            <#--<#default><#assign myClazzContent = "更多特权！即将开放！！！" myClazzBoxLeft = 221 myClazzArrowLeft = 300 /><#break />-->
                        <#--</#switch>-->
                        <#--<div class="kt-info" style="left: ${myClazzBoxLeft}px;">-->
                            <#--<div class="arrow" style="left: ${myClazzArrowLeft}px"></div>-->
                            <#--${myClazzContent}-->
                        <#--</div>-->
                        <#--<div class="bot-content">-->
                            <#--<#switch (myClazzRank.level!0)+1>-->
                                <#--<#case 1>达到1级，赠送PK套装，男-【暗影骑士】，女-【魔女】套装<#break />-->
                                <#--<#case 2>达到2级，全班每日砸蛋/抽奖次数+1<#break />-->
                                <#--<#case 3>达到3级，全班每日PK活力提高1点<#break />-->
                                <#--<#case 4>达到4级，送超稀有，超值属性的10级绿色武器——【玄机重剑】or【玄机魔枪】or【玄机法杖】<#break />-->
                                <#--<#case 5>达到5级，全班每人每次完成作业或者测验额外增加1个学豆<#break />-->
                                <#--<#default>更多特权！即将开放！！！<#break>-->
                            <#--</#switch>-->
                        <#--</div>-->
                    <#--</div>-->
                <#--</li>-->
            <#--</ul>-->
        <#--</div>-->
        <#--<div class="it-info">-->
            <#--<h2 class="white">全国班级学分排名奖励内容：</h2>-->
            <#--<div class="rank-clazz-table">-->
                <#--<table>-->
                    <#--<thead>-->
                    <#--<tr>-->
                        <#--<th>名次</th>-->
                        <#--<th>学豆 <span class="rk-icon rk-icon-1"></span></th>-->
                        <#--<th>通天塔 <span class="rk-icon rk-icon-2"></span></th>-->
                        <#--<th>水晶 <span class="rk-icon rk-icon-3"></span></th>-->
                        <#--<th>精力卡&lt;#&ndash; <span class="rk-icon rk-icon-4"></span>&ndash;&gt;</th>-->
                        <#--<th>PK活力 <span class="rk-icon rk-icon-5"></span></th>-->
                        <#--<th>通天塔道具</th>-->
                        <#--<th>PK时装/武装</th>-->
                    <#--</tr>-->
                    <#--</thead>-->
                    <#--<tbody>-->
                    <#--<tr class="odd">-->
                        <#--<th>第1名</th>-->
                        <#--<th>100</th>-->
                        <#--<th>100</th>-->
                        <#--<th>30</th>-->
                        <#--<th>30</th>-->
                        <#--<th>10</th>-->
                        <#--<th>9</th>-->
                        <#--<th>1套时装</th>-->
                    <#--</tr>-->
                    <#--<tr>-->
                        <#--<th>第2名</th>-->
                        <#--<th>50</th>-->
                        <#--<th>70</th>-->
                        <#--<th>30</th>-->
                        <#--<th>30</th>-->
                        <#--<th>5</th>-->
                        <#--<th>6</th>-->
                        <#--<th>1件时装</th>-->
                    <#--</tr>-->
                    <#--<tr class="odd">-->
                        <#--<th>第3名</th>-->
                        <#--<th>30</th>-->
                        <#--<th>50</th>-->
                        <#--<th>30</th>-->
                        <#--<th>30</th>-->
                        <#--<th>2</th>-->
                        <#--<th>3</th>-->
                        <#--<th>1件时装</th>-->
                    <#--</tr>-->
                    <#--<tr>-->
                        <#--<th>第4至100名</th>-->
                        <#--<th>10</th>-->
                        <#--<th>30</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>--</th>-->
                        <#--<th>1</th>-->
                        <#--<th>1件武装</th>-->
                    <#--</tr>-->
                    <#--<tr class="odd">-->
                        <#--<th>101至1000名</th>-->
                        <#--<th>--</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                        <#--<th>1件武装</th>-->
                    <#--</tr>-->
                    <#--<tr>-->
                        <#--<th>1001至5000名</th>-->
                        <#--<th>--</th>-->
                        <#--<th>15</th>-->
                        <#--<th>15</th>-->
                        <#--<th>15</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                    <#--</tr>-->
                    <#--<tr class="odd">-->
                        <#--<th>第17名额外奖励</th>-->
                        <#--<th>--</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                    <#--</tr>-->
                    <#--<tr>-->
                        <#--<th>第170名额外奖励</th>-->
                        <#--<th>--</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                    <#--</tr>-->
                    <#--<tr class="odd">-->
                        <#--<th>第1700名额外奖励</th>-->
                        <#--<th>--</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>20</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                        <#--<th>--</th>-->
                    <#--</tr>-->
                    <#--</tbody>-->
                <#--</table>-->
            <#--</div>-->
        <#--</div>-->
        <#--<div class="it-info">-->
            <#--<h2 class="white">第4名至100名：</h2>-->
            <#--<div class="rank-clazz-table">-->
                <#--<table>-->
                    <#--<thead>-->
                    <#--<tr>-->
                        <#--<th style="width: 120px;">名次</th>-->
                        <#--<th>班级</th>-->
                        <#--<th style="width: 120px;">学分</th>-->
                    <#--</tr>-->
                    <#--</thead>-->
                <#--</table>-->
                <#--<div style="overflow: hidden; overflow-y: auto; height: 412px;">-->
                    <#--<table>-->
                        <#--<tbody>-->
                            <#--<#list rankList as rl>-->
                            <#--<#if rl_index gt 2>-->
                                <#--<tr <#if rl_index%2 == 0>class="odd"</#if>>-->
                                    <#--<th style="width: 120px;">第${rl_index+1}名</th>-->
                                    <#--<th>${rl.schoolName} ${rl.clazzName}</th>-->
                                    <#--<th style="width: 120px;">${rl.score}分</th>-->
                                <#--</tr>-->
                            <#--</#if>-->
                            <#--</#list>-->
                        <#--</tbody>-->
                    <#--</table>-->
                <#--</div>-->
            <#--</div>-->
        <#--</div>-->
    <#--</div>-->
<#--</div>-->
<#--<script type="text/javascript">-->
    <#--$(function(){-->
        <#--var myLevelContent = $(".data-myLevelContent");-->
        <#--$(".data-myLevelBtn").hover(function(){-->
            <#--myLevelContent.show();-->
        <#--}, function(){-->
            <#--myLevelContent.hide();-->
        <#--});-->

        <#--$(".currentMonthCount").text( $17.getMonthTotalDay() );-->
    <#--});-->
<#--</script>-->
<#--</@temp.page>-->
