<#import './layout.ftl' as layout>

    <@layout.page className='HomeworkReport' title="学校表现" pageJs="second">

    <#escape x as x?html>

        <#assign topType = "topTitle">
        <#assign topTitle = "星星奖励">
        <#include "./top.ftl" >

        <#if result.success>

            <div class="starBean-main" >

                <#if isBindClazz>

                    <div class="sea-topbar">
                        <div class="topBtn doShowTip doTrack" data-track="star|help" data-tip_content = ".doStarTip">了解星星奖励</div>
                    </div>

                    <div class="sea-title">
                        <div class="st-bg"></div>
                        <div class="st-list">
                            <ul>
                                <li id="doRankMonth" class="sk">
                                </li>
                                <li id="doRankTerm">
                                </li>
                            </ul>
                        </div>
                    </div>

                    <div class="sea-con doTabBlock">
                        <div class="sc-tab">
                            <#assign baseUrl = "/parentMobile/reward/getstarrank.vpage?sid=${sid}">

                            <#assign rangType = [
                            {
                                "key" : "0",
                                "name" : "本月",
                                "trackType" : "month",
                                "data" : {
                                "ajaxUrl"  : '${baseUrl}&currentMonth=0',
                                "tabTargetEl" : '#tabContent',
                                "tabTemplateEl" : '#tempDom'
                            }
                            },
                            {
                                "key" : "1",
                                "name" : "学期",
                                "trackType" : "term",
                                "data" : {
                                "ajaxUrl"  : '${baseUrl}&currentMonth=1',
                                "tabTargetEl" : '#tabContent',
                                "tabTemplateEl" : '#tempDom'
                            }
                            }
                            ]>

                            <#list rangType as range >
                                <#assign  data = range.data>
                                <a href="javascript:;" class="<#if range_index ==0 >active</#if> doTab doTrack"
                                   data-track = "star|${range.trackType}tab"
                                   data-tab_ajax_url = "${data.ajaxUrl!''}"
                                   data-tab_template_el = "${data.tabTemplateEl!''}"
                                   data-tab_target_el = "${data.tabTargetEl!''}"
                                        >${range.name}</a>
                            </#list>
                        </div>

                        <div class="sc-table" id="tabContent">
                        </div>
                    </div>

                    <script id="tempDom" type="text/html">
                        <div class="sl-con">
                            <table>
                                <thead>
                                <tr> <td>目前排名</td> <td>姓名</td> <td>星星数量</td> <td>排名奖励</td> </tr>
                                </thead>
                                <tbody>
                                <% [].concat(myRank, starRank).forEach(function(startInfo, index){ %>
                                    <%
                                        var firstClassName = "font-purple",
                                            fontClassName = index==0 ? firstClassName : "";
                                    %>

                                    <% if(+startInfo.userId === +myRank.userId && index > 0){ %>
                                    <% }else{ %>
                                        <tr>
                                            <td class="gray <%= fontClassName %>"><%= startInfo.rank %></td>
                                            <td class="<%= fontClassName %>"><%= startInfo.userName %></td>
                                            <td class="gray <%= fontClassName %>"><%= startInfo.star %></td>
                                            <td class="<%= fontClassName %>"><%= startInfo.integral %>学豆</td>
                                        </tr>
                                        <% if(fontClassName === firstClassName){ %>
                                            <tr>
                                                <td colspan="4" class="textBox">
                                                    <div>
                                                        <p> 老师检查作业，奖励：<%= myRank.star - (myRank.bindWechatParentCount > 1 ? (2*10) : (myRank.bindWechatParentCount*10))%>个星星</p>
                                                        <%
                                                            var bindWechatParentCount =  +myRank.bindWechatParentCount || 0;
                                                            if(bindWechatParentCount === 1){
                                                        %>
                                                            <p>1位家长使用家长通，奖励： 10个星星<br/></p>
                                                        <%
                                                            }else if(bindWechatParentCount > 1){
                                                        %>
                                                            <p>已有2位家长使用家长通，奖励：20个星星<br/></p>
                                                        <%
                                                            }
                                                        %>
                                                        <p>当前排名第<%=myRank.rank%>，可领奖励：<%=myRank.integral%> 学豆</p>
                                                    </div>
                                                </td>
                                            </tr>
                                        <% } %>
                                    <% } %>
                                <% }) %>
                                </tbody>
                            </table>
                            <div style="text-align: center; margin: 20px 0;">
                            </div>
                        </div>
                    </script>

                    <#include "./what_is_star_reward.ftl">

                    <script type="text/html" id="getRank">
                        <%
                        var rankInfo = {
                        "0" : "月",
                        "1" : "学期"
                        };

                        var display = rankInfo[currentMonth],
                        trackType = display === "月" ? "month" : "term";
                        %>
                        <span class="doRankMonth">上<%= display %>排名奖励：<%= myRank.integral %>学豆</span>
                        <%
                        if(receivedStarRankReward){
                        %>
                        <a class="btn-s btn-s-disable" href="javascript:;">已奖励</a>
                        <% }else{ %>
                        <a class="btn-s doRewardbystarrank doTrack" data-track="star_<%=trackType>bean" data-starrank_month="<%= currentMonth %>" data-sid="${sid}" href="javascript:;">领取奖励</a>
                        <% } %>
                    </script>

                <#else>
                    <#assign tipType = "card">
                    <#assign tipText = "还没有加入班级,请向老师申请加入">
                    <#include "./tip.ftl">
                </#if>

            </div>
        <#else>
            <p class="hide doAutoTrack" data-track="star|fail"></p>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>
        <p class="hide doScrollTrack" data-track = "star|scroll"></p>
    </#escape>

</@layout.page>

