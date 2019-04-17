<%
	var rankList             = integral_rank_list || [],
		current_student_info = rankList[current_student_rank - 1];
%>

<div class="listReward-box">
    <div class="l-banner">
        <#--此处需要做兼容处理-->
        <a href="javascript:void(0);" class="link doTrack" id="J-do-show-pop" style="font-size: .6rem;line-height: 1.2rem;position: absolute;right: 1rem;top: 0.1rem;margin: 0.45rem auto;color: #fff;height: 1.2rem;display: block;" data-track="${trackModule}|help" data-tip_content=".doStarTip">榜单说明&gt;</a>
        <h1 class="icon-crown">第<%= current_student_rank %>名</h1>
        <p class="l-info"><%= current_student_info.real_name  %>本月获得学豆奖励：<%= current_student_info.teacher_award_amount + current_student_info.parent_receive_amount %></p>
    </div>

    <div class="l-main">
        <div class="l-title">
            <div class="right">学豆奖励</div>
            本月排行
        </div>
        <div class="l-column">
            <ul>
                <%
                    rankList.forEach(function(rank, index){
                        var integral_rank = rank.integral_rank,
                        teacher_award = rank.teacher_award_amount,
                        parent_receive = rank.parent_receive_amount,
                        schloar = rank.schloar_count,
                        realName = rank.real_name || "";

                    realName = realName.replace((realName.slice(4) || {}), "...");
                %>
                <li>
                    <div class="l-side">
                        <div class="right"><%= [teacher_award, parent_receive].join(' + ') %></div>
                        <div class="left">
                            <span class="icon-rank <%= index < 3 ? 'n1' : 'rank' %>"><%= integral_rank %></span>
                            <span class="name"><%= realName %></span>
                            <%if(schloar > 0){ %>
                                <span class="tag">学霸<%= schloar  %>次</span>
                            <% } %>
                        </div>
                    </div>
                    <% if(rank.integral_rank === current_student_rank){ %>
                    <div class="l-content">
                        <p>老师发放奖励：<%= teacher_award %>学豆</p>
                        <p>家长领取奖励：<%= parent_receive %>学豆</p>
                        <p>*检查作业后家长可在作业动态领取额外学豆</p>
                    </div>
                    <%} %>
                </li>
                <% }); %>
            </ul>
        </div>
    </div>
</div>
${buildAutoTrackTag(trackModule + "|reward_scroll", true)}