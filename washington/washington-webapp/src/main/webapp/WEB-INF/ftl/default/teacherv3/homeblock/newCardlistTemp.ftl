<script id="AUTHENTICATION_TEMP" type="text/html"> <#--认证-->
<li class="missionCenter-item">
    <div class="missionCenter-top">
        <p class="title"><%= cardName %></p>
        <p class="info"><%= progress %></p>
    </div>
    <div class="missionCenter-image">
        <img src="<@app.link href='public/skin/teacherv3/images/cardlist/auth-banner.png'/>">
    </div>
    <div class="missionCenter-set">
    <#--name和mobile不为空，第一步完成-->
    <#--bindMobileCount大于等于3，第二步完成-->
    <#--finishThreePlusCount大于等于8，第三步完成-->
        <% var finishCount = 0,btnText = "",firstStrongText = "未完成",secondStrongText = "",thirdStrongText = "",showFirst=false,showSecond=false,showThird=false%>
        <% if(cardDetailsJson.name && cardDetailsJson.name != "" && cardDetailsJson.mobile && cardDetailsJson.mobile !=""){%>
        <%    finishCount++; btnText = "邀请学生加入"; firstStrongText="已完成";showSecond=true;%>
        <% } else { %>
        <%    btnText="去设置";firstStrongText = "未完成";showFirst=true; %>
        <% } %>
        <% if(cardDetailsJson.bindMobileCount && cardDetailsJson.bindMobileCount >= 3){%>
        <%    finishCount++; btnText = "邀请学生加入"; secondStrongText="已完成" ;showSecond=false;showThird=true;%>
        <% } else { %>
        <%    var secondNeedCount = 3-cardDetailsJson.bindMobileCount %>
        <%    secondStrongText = "还差"+secondNeedCount+"人"; %>
            <% if(showFirst){%>
            <% showSecond = false;btnText = "去设置"; %>
            <% }else{ %>
            <% showSecond = true; %>
            <% } %>
        <% } %>
        <% if(cardDetailsJson.finishThreePlusCount && cardDetailsJson.finishThreePlusCount >= 8){%>
        <%    finishCount++; btnText = "认证审核中"; thirdStrongText="已完成" ;showSecond=false;showThird=true;%>
        <% } else { %>
        <%    var thirdNeedCount = 8-cardDetailsJson.finishThreePlusCount %>
        <%    thirdStrongText = "还差"+thirdNeedCount+"人";%>
            <% if(showFirst || showSecond){%>
            <% showThird = false; %>
            <% }else{ %>
            <% showThird = true; %>
            <% } %>
        <% } %>
        <ul class="set-list">
            <!--success完成状态、active当前状态-->
            <% for(var i=0;i< 3;i++ ){ %>
                <li class="<% if(i == finishCount){%>active <% }%><% if(i < finishCount){%>success<% }%>">
                    <i class="line"></i><span class="circleIcon js-switchCardStatusBtn" data-index="<%= i+1%>"></span>
                </li>
            <% } %>
        </ul>
        <p class="tips">
            <span class="js-progressInfo" data-index="1" style="display: <% if(!showFirst){ %>none;<%}%>">
                设置姓名并绑定手机<span class="fontBlue">［<%=firstStrongText%>］</span>
            </span>
            <span class="js-progressInfo" data-index="2" style="display: <% if(!showSecond){ %>none;<%}%>">
                至少3名学生加入班级并绑定手机<span class="fontBlue">［<%=secondStrongText%>］</span>
            </span>
            <span class="js-progressInfo" data-index="3" style="display: <% if(!showThird){ %>none;<%}%>">
                至少8名学生完成3次作业<span class="fontBlue">［<%=thirdStrongText%>］</span>
            </span>
        </p>
    </div>

    <div class="missionCenter-btn">
        <a href="javascript:;" class="w-btn <%if(finishCount == 3){%>w-btn-gray<%}%> w-btn-orange js-inviteStudentBtn js-cardListBtn" data-mid="<%=cardDetailsJson.mobile%>" data-aid="<%if(showFirst){%>1<%}else{%>0<%}%>" data-type="<%=cardType%>"><%= btnText%></a>
    </div>
</li>
</script>
<script id="HOMEWORK_TEMP" type="text/html"><#--作业-->
<% var subType = cardDetailsJson.taskType; %>
<%if(subType == "DAILY_HOMEWORK"){ %> <#--日常作业-->
<li class="missionCenter-item">
    <div class="cardBox">
        <div class="daily-card">
            <div class="missionCenter-top topBlue">
                <p class="title"><%= cardName%></p>
                <p class="info"><%= cardDetailsJson.progress%></p>
            </div>
            <div class="missionCenter-image">
                <img src="<@app.link href='public/skin/teacherv3/images/cardlist/daily-banner.png'/>">
            </div>
            <div class="missionCenter-set schedule">
                <ul class="set-list">
                    <% var currentDay = cardDetailsJson.taskDetails.currentDay || 0 %>
                    <% var dayDetails = cardDetailsJson.taskDetails.dayDetails || [] %>
                    <!--success完成状态、active当前状态、disabled未达状态-->
                    <% var dayList = ['一','二','三','四','五','六','日'] %>
                    <% for(var j=0;j< dayList.length;j++){ %>
                    <% if(currentDay >=j){ %>
                    <% currentLiClass = ""; %>
                    <% if(dayDetails[j].finished){ %>
                    <% currentLiClass = "success" %>
                    <% } %>
                    <% } else if(currentDay < j){ %>
                    <% currentLiClass = "disabled" %>
                    <% } %>
                    <li class="<%= currentLiClass%> <%if(currentDay == j){%> active<%}%>">
                        <i class="line"></i>
                        <span class="circleIcon js-switchCardDayBtn" data-index="<%=j+1%>"><%=dayList[j]%></span>
                    </li>
                    <% } %>
                </ul>
                <% for(var i=0;i< dayDetails.length;i++){ %>
                <ul class="set-column js-classListCon" data-index="<%= i+1%>" style="<%if(currentDay!=i) { %>display: none; <% } %>">
                    <% var clazzDetails = dayDetails[i].clazzDetails || []%>
                    <% for(var k=0;k< clazzDetails.length;k++){ %>
                    <li data-gid="<%= clazzDetails[k].groupId%>">
                        <p class="name"><%= clazzDetails[k].clazzName%></p>
                        <p class="count">
                            <a href="javascript:void(0);" class="set_btn"><%= clazzDetails[k].status%></a>
                        </p>
                    </li>
                    <% } %>
                </ul>
                <% } %>
            </div>
            <div class="missionCenter-btn">
                <p class="rule-info js-rulDescBtn">规则说明</p>
                <% var homeWorkStatus = cardDetailsJson.taskStatus,homeworkCardBtn=""; %>
                <% if(homeWorkStatus == "UNFINISHED" || homeWorkStatus == "REWARDED") {%> <#--日常作业未完成和已领取状态都能布置新作业-->
                <a href="javascript:void(0);" class="w-btn js-createNewHomeworkBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-type="<%=subType%>">布置新作业</a>
                <% }else if(homeWorkStatus == "FINISHED") {%>
                <a href="javascript:void(0);" class="w-btn js-getHomeworkRewardBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-nid="<%=cardDetailsJson.integralCount%>" data-type="<%=subType%>">领取奖励</a>
                <% } %>
            </div>
        </div>
        <div class="ruleExplain-card" style="display: none;">
            <div class="title">规则说明</div>
            <div class="info">1.一周内布置<%= cardDetailsJson.taskDetails.taskNeedDays %>天（及以上）作业，即可领取奖励。</div>
            <div class="info">2.任务每周一凌晨更新，更新后奖品自动重置，完成任务请及时领取。</div>
            <div class="info">3.获取的奖励可前往个人中心-我的园丁豆中查看。</div>
            <div class="info">本任务最终解释权归一起小学所有。</div>
            <div class="missionCenter-btn">
                <a href="javascript:void(0);" class="w-btn js-rollBackBtn">确定</a>
            </div>
        </div>
    </div>
</li>
<% } %>
<%if(subType == "WEEKEND_HOMEWORK"){ %> <#--周末作业-->
<li class="missionCenter-item">
    <div class="cardBox">
        <div class="daily-card">
            <div class="missionCenter-top topBlue">
                <p class="title"><%= cardName%></p>
                <p class="info"><%= cardDetailsJson.progress%></p>
            </div>
            <div class="missionCenter-image">
                <img src="<@app.link href='public/skin/teacherv3/images/cardlist/weekend-banner.png'/>">
            </div>
            <div class="missionCenter-set schedule">
                <ul class="set-column js-classListCon" data-index="<%= i+1%>">
                    <% var clazzDetails = cardDetailsJson.taskDetails.clazzDetails || []%>
                    <% for(var k=0;k< clazzDetails.length;k++){ %>
                    <li data-gid="<%= clazzDetails[k].groupId%>">
                        <p class="name"><%= clazzDetails[k].clazzName%></p>
                        <p class="count">
                            <a href="javascript:void(0);" class="set_btn"><%= clazzDetails[k].status%></a>
                        </p>
                    </li>
                    <% } %>
                </ul>
            </div>
            <div class="missionCenter-btn">
                <p class="rule-info js-rulDescBtn">规则说明</p>
                <% var homeWorkStatus = cardDetailsJson.taskStatus,homeworkCardBtn="",currentDay=cardDetailsJson.taskDetails.currentDay || 0; %>
                <% if(homeWorkStatus == "UNFINISHED") {%>
                    <a href="javascript:void(0);" class="w-btn js-createNewHomeworkBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-aid="<%if(currentDay==4){%>1<%}else{%>2<%}%>" data-type="<%=subType%>">布置新作业</a>
                <% }else if(homeWorkStatus == "FINISHED") {%>
                    <a href="javascript:void(0);" class="w-btn js-getHomeworkRewardBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-nid="<%=cardDetailsJson.integralCount%>" data-type="<%=subType%>">领取奖励</a>
                <% }else if(homeWorkStatus == "REWARDED") {%>
                    <a href="javascript:void(0);" class="w-btn w-btn-gray js-hasRewardBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-type="<%=subType%>">奖励已领取</a>
                <% } %>
            </div>
        </div>
        <div class="ruleExplain-card" style="display: none;">
            <div class="title">规则说明</div>
            <div class="info">1.周五布置作业且周末完成人数达到10人，即可领取奖励。</div>
            <div class="info">2.任务每周五凌晨更新，更新后奖品自动重置，完成任务请及时领取。</div>
            <div class="info">3.获取的奖励可前往个人中心-我的园丁豆中查看。</div>
            <div class="info">本任务最终解释权归一起小学所有。</div>
            <div class="missionCenter-btn">
                <a href="javascript:void(0);" class="w-btn js-rollBackBtn">确定</a>
            </div>
        </div>
    </div>
</li>
<% } %>
 <#--TODO 这两种作业目前没有 -->
<%if(subType == "VACATION_HOMEWORK"){ %> <#--假期作业-->

<% } %>
<%if(subType == "ACTIVITY_HOMEWORK"){ %> <#--运营活动作业-->
<li class="missionCenter-item">
    <div class="cardBox">
        <div class="daily-card">
            <div class="missionCenter-top topBlue">
                <p class="title"><%= cardName%></p>
                <p class="info"><%= cardDetailsJson.progress%></p>
            </div>
            <div class="missionCenter-image">
                <% if(cardDetailsJson.pcImgUrl){ %>
                    <img src="<%=cardDetailsJson.pcImgUrl%>">
                <% } else {%>
                    <img src="<@app.link href='public/skin/teacherv3/images/cardlist/weekend-banner.png'/>">
                <% } %>
            </div>
            <div class="missionCenter-set schedule">
                <ul class="set-column js-classListCon" data-index="<%= i+1%>">
                    <% var clazzDetails = cardDetailsJson.taskDetails.clazzDetails || []%>
                    <% for(var k=0;k< clazzDetails.length;k++){ %>
                    <li data-gid="<%= clazzDetails[k].groupId%>">
                        <p class="name"><%= clazzDetails[k].clazzName%></p>
                        <p class="count">
                            <a href="javascript:void(0);" class="set_btn"><%= clazzDetails[k].status%></a>
                        </p>
                    </li>
                    <% } %>
                </ul>
            </div>
            <div class="missionCenter-btn">
                <p class="rule-info js-rulDescBtn">规则说明</p>
                <% var homeWorkStatus = cardDetailsJson.taskStatus,showToast = cardDetailsJson.taskDetails.showToast,toast = cardDetailsJson.taskDetails.toast; %>
                <% if(homeWorkStatus == "UNFINISHED") {%>
                <a href="javascript:void(0);" class="w-btn js-createNewHomeworkBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-aid="<%if(showToast){%>3<%}else{%>1<%}%>" data-type="<%=subType%>" data-toast="<%=toast%>">布置新作业</a>
                <% }else if(homeWorkStatus == "FINISHED") {%>
                <a href="javascript:void(0);" class="w-btn js-getHomeworkRewardBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-nid="<%=cardDetailsJson.integralCount%>" data-type="<%=subType%>">领取奖励</a>
                <% }else if(homeWorkStatus == "REWARDED") {%>
                <a href="javascript:void(0);" class="w-btn w-btn-gray js-hasRewardBtn js-cardListBtn" data-tid="<%=cardDetailsJson.taskId%>" data-type="<%=subType%>">奖励已领取</a>
                <% } %>
            </div>
        </div>
        <div class="ruleExplain-card" style="display: none;">
            <div class="title">规则说明</div>
            <% var taskRules = cardDetailsJson.taskRules; %>
            <% for(var i=0;i< taskRules.length;i++){ %>
                <div class="info"><%=taskRules[i]%></div>
            <% } %>
            <div class="missionCenter-btn">
                <a href="javascript:void(0);" class="w-btn js-rollBackBtn">确定</a>
            </div>
        </div>
    </div>
</li>
<% } %>
</script>
<script id="ACTIVITY_TEMP" type="text/html"><#--活动-->
<li class="missionCenter-item">
    <div class="missionCenter-top topGreen">
        <p class="title"><%= cardName %></p>
    </div>
    <div class="missionCenter-image">
        <img src="<%= imgUrl%>" alt="">
    </div>
    <div class="missionCenter-box">
        <p class="rule"><%= cardDescription %></p>
    </div>
    <div class="missionCenter-btn">
        <a href="javascript:void(0);" class="w-btn w-btn-green js-activityDetailBtn js-cardListBtn" target="_blank" data-link="<%= detailUrl%>" data-type="<%=cardType%>"><%if(btnContent){%><%=btnContent%><%}else{%>查看详情<%}%></a>
    </div>
</li>
</script>

<!--下载老师端-->
<script id="DownLoadTeacherApp_TEMP" type="text/html"><#--活动-->
<li class="missionCenter-item">
    <div class="missionCenter-top topGreen">
        <p class="title">下载老师端</p>
    </div>
    <div class="missionCenter-image">
        <img src="<@app.link href='public/skin/teacherv3/images/publicbanner/download-teacherapp-banner.jpg'/>" alt="">
    </div>
    <div class="missionCenter-box">
        <p class="rule"><i class="icon-circle"></i>随时随地，布置作业</p>
        <p class="rule"><i class="icon-circle"></i>每天免费5次抽奖，平板等你拿</p>
        <p class="rule"><i class="icon-circle"></i>作业/活动信息，及时接收</p>
    </div>
    <div class="missionCenter-btn">
        <a href="/help/downloadApp.vpage?refrerer=pc&count=0" class="w-btn w-btn-green js-cardListBtn" target="_blank" data-type="<%=cardType%>">立即下载</a>
    </div>
</li>
</script>

<#--邀请学生加入弹窗-->
<script id="addStudentDialog_temp" type="text/html">
<div class="addStudent-box">
    <div class="title">布置老师：通过<span>校讯通、微信群、QQ群</span>通知学生、或家长帮助学生注册帐号，体验作业</div>
    <div class="column">
        <i class="icon-triangle"></i>
        <p>Hi 同学们！我布置了网上作业，更轻松更有趣，快加入班级做作业吧。</p>
        <p>进班步骤：</p>
        <p>1、网站地址:<a href="http://www.17zuoye.com" target="_blank">http://www.17zuoye.com</a> (有手机的可下载一起小学学生APP做作业）</p>
        <p>2、填写我的号码 <span class="js-mobileOrId"><%if(mobile){%><%= mobile%><%}else{%>${(currentUser.id)!}<%}%> ，加入班级</span></p>
        <p class="sub js-dialogTeacherInfo">（${(currentTeacherDetail.getSubject().getValue())!}老师：${(currentUser.profile.realname)!}）</p>
    </div>
    <div class="info">
        <a href="javascript:void(0);" class="js-switchNumberBtn" data-mid="${(currentUser.id)!}" data-index="1">不方便公布手机号</a>
        <a href="javascript:void(0);" class="js-switchNumberBtn" data-mid="<%= mobile%>" style="display: none;" data-index="2">方便公布手机号</a>
    </div>
    <div class="btn">
        <a class="w-btn"  href="javascript:void(0);" id="clip_container_to_student" style="position: relative;margin: 0 auto;display: block;padding: 0;">
            <span id="clip_button_to_student" style="display: block; line-height: 45px; width: 100%;">复制上面内容</span>
        </a>
    </div>
<textarea disabled="disabled" readonly="readonly" id="copyToStudentInfo" style="display: none;">
Hi 同学们！我布置了网上作业，更轻松更有趣，快加入班级做作业吧。
进班步骤：
1、网站地址: http://www.17zuoye.com (有手机的可下载一起小学学生APP做作业）
2、填写我的号码</textarea>
</div>
</script>

<#--成功领取作业奖励弹窗-->
<script id="successRewordDialog_temp" type="text/html">
<div class="receiveReward-pop">
    <p class="item">＋<%=number%>园丁豆 <span class="icon-bean"></span></p>
    <p class="tips">（可在个人中心查看我的园丁豆）</p>
</div>
</script>
