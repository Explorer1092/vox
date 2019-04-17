<#include "../../transComn.ftl">
<%
	var prev_month = new window.Date().getMonth() || 12,
		current_month = prev_month === 12 ? 1 : (prev_month + 1);

	lastMonthLoginIntegral = lastMonthLoginIntegral || 0;
%>
<div class="listDynamic-box">
    <div class="l-banner">
        <div class="l-tip">感谢您对<%= currentStudentName || "" %>学习的关注 <br>
            <%if(lastMonthLoginIntegral === 0){ %>
            	<%= prev_month %>月家长未签到，奖励学豆：0
            <%}else if(lastMonthLoginIntegral === 5){ %>
            	<%= prev_month %>月1位家长签到，奖励学豆：5
            <%}else{ %>
            	<%= prev_month %>月已有2位家长签到，奖励学豆：20
            <%}%>
		</div>
        <div class="l-container">
            <div class="l-module last">
                <div class="month"><%= prev_month %>月</div>
                <% if(lastSignFlag){ %>
                	<%if(hasReceivedLoginReward){ %>
                		<a href="javascript:void(0);" class="login-btn notLogged" >已领取<%= lastMonthLoginIntegral %>学豆</a>
                	<%}else{%>
                	<% if(isHitDownLoad){ %>
                		<a href="javascript:void(0);" onclick='location.href="//${wechatUrlHeader}/parent/reward/receive.vpage"' class="login-btn doTrack" data-track = "m_p0JRVPx3|o_lW4Zb9uZ">领取签到奖励</a>
                	<% }else{ %>
                		<a href="javascript:void(0);" class="login-btn doTrack doReceiveParentReward" data-track = "m_p0JRVPx3|o_lW4Zb9uZ">领取签到奖励</a>
                	<% } %>
                <%}%>
                <% }else{ %>
                	<a href="javascript:void(0);" class="login-btn notLogged">未签到</a>
                <% } %>
            </div>
            <div class="l-module next">
                <div class="month"><%= current_month %>月</div>
                <%if(showFlag){ %>
                	<%if(signFlag){ %>
                		<a href="javascript:void(0);" class="login-btn notLogged">已签到</a>
                	<%}else{ %>
                		<a href="javascript:void(0);" class="login-btn  doSign doTrack" data-track = "m_p0JRVPx3|o_av4hhIQM">点我签到</a>
                	<%} %>
                <%} %>
            </div>
        </div>
        <div class="l-down">
            <#--<a href="javascript:void(0);" class="view-btn doSignDetail doTrack" data-track = "m_p0JRVPx3|o_QF8yRqg6">查看签到详情</a>-->
            <p>*每月1位家长签到奖5学豆；1位以上不同身份家长签到奖20</p>
            <#--<p>*每月作业送花下月可转化为免费班级学豆</p>-->
        </div>
    </div>
    <#--<div class="l-main">
        <table colspan="0" cellpadding="0" cellspacing="0">
            <thead>
            <tr>
                <td><%= current_month %>月排名</td>
                <td></td>
                <td>送花</td>
                <td>可兑换学豆</td>
            </tr>
            </thead>
            <tbody>
				<style>
					.hide_important { display: none !important; }
					.mine {background-color: #eff2f6;}
				</style>
				<%
					(rankList || []).forEach(function(rank, index){
						var slice_studentName = rank.studentName || "";
						slice_studentName = slice_studentName.replace((slice_studentName.slice(5) || {}), "...");
				%>

				<tr <%= rank.studentId === currentStudentId ? 'class=mine' : '' %>>
					<td><span class="icon-rank <%= index < 3 ? 'n1' : 'rank' %>"><%= index + 1 %></span></td>
					<td>
						<p><%= slice_studentName %>家长</p>
						<% if(rank.isDoubleAttention || false){ %>
							<span class="tagBlue">双倍关注</span>
						<% } %>
						<%
							var vipClass = {
								NONE    : "hide_important",
								VALID   : "tagRed",
								EXPIRED : "tagGray"
							};
						%>
						<span class="<%= vipClass[rank.isVip] %>">VIP</span>
					</td>
					<td><span class="flower"><%= rank.flowerCount || "0" %></span></td>
					<td><span class="beans"><%= rank.loginReward || "0" %></span></td>
				</tr>
				<% }); %>
            </tbody>
        </table>
    </div>-->
	<div id="sign_main">
        <ul class="sign-info" id="sign_in_detail">
        </ul>
	</div>
</div>
${buildAutoTrackTag(trackModule + "|jzlogin_scroll", true)}