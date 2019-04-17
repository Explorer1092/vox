<script id="t:弹出框" type="text/html">
    <div id="smartclassGettip">
        <div class="smartclass_gettip">
            <div class="smartclass_getcon">
                <dl>
                    <dt>
                        <%if (img != '') {%>
                        <img src="<@app.avatar href='<%=img%>'/>" width="80" height="80px">
                        <%}else{%>
                        <img src="<@app.link href="public/skin/teacherv3/images/smartclazz/null-user.png"/>" width="80" height="80px">
                        <%}%>
                    </dt>
                    <dd>
                        <i class="praiseIcon-gray praiseIcon-gray-<%=li_index%>"></i>
                        <%==content%>
                        <%if(li_index != '') {%>
                        <i class="smartclass_get_icon micon_<%=li_index%>"></i>
                        <%}%>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</script>

<script id="t:多人随机弹出框" type="text/html">
    <div class="s-kidsRecord-box">
        <div class="cus-con">
            <span class="actor" style="display: none;">
            </span>
            <span class="actor" style="display: none;">
            </span>
            <span class="actor" style="display: none">
            </span>
            <span class="actor" style="display: none">
            </span>
            <span class="actor" style="display: none">
            </span>
        </div>
    </div>
</script>

<script id="t:随机选择单人渲染" type="text/html">
    <i class="icon-s-card">
    <%if(student.studentImg != ''){%>
    <img height="80" width="80" src="<@app.avatar href='<%=student.studentImg%>'/>">
    <%}else{%>
    <img height="80" width="80" src="<@app.link href="public/skin/teacherv3/images/smartclazz/null-user.png"/>">
    <%}%>
    </i>
    <strong><%=student.studentName%></strong>
</script>

<script type="text/html" id="t:exchange">
    <div class="historyReward historyAllTableBox">
        <div id="tip_box" class="dropDownBox_tip smartAlertInfo">
            <div style="padding: 10px;" class="tip_content">
                <span>1园丁豆=5学豆，请输入5的倍数 最大上限500 </span>
            </div>
        </div>
        <div class="jqicontent" style="text-align: center;">
            <div class="w-addSub-int">
                <a class="w-btn w-btn-mini minusBtn" href="javascript:void (0)">-</a>
                <input class="w-int tempNum"id="tempNumBox" name="tempNumBox" maxlength="3" type="text" value="100" style="width: 140px;" >
                <a class="w-btn w-btn-mini plusBtn" href="javascript:void (0)">+</a>
            </div>
        </div>
        <p class="silverInfo jqicontent" style="text-align: center; font-size:14px;">兑换<strong class="w-blue silverCountBox">100</strong>个学豆会消耗您<strong class="w-red goldCountBox">20</strong>个园丁豆，您确定这样做吗？</p>
        <div id="v-exchangeVerifyCode" class="w-form-table" style="display: none">
            <dl style="padding-left:50px;">
                <dt>验证手机号：</dt>
                <dd>
                    <span id="v-exchangeMobile"></span>&nbsp;&nbsp;&nbsp;&nbsp;<a class="w-btn w-btn-mini v-getSmsCode" href="javascript:void(0);" style="width: 150px;"><span>免费获取验证码</span></a>
                </dd>
                <dt>短信验证码：</dt>
                <dd>
                    <input type="text" maxlength="6" class="w-int v-smsCode" value="" placeholder="请输入收到的短信验证码">
                    <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                    <span class="w-form-misInfo w-form-info-error errorMsg" style="display: none;"><i class="w-icon-public w-icon-error"></i><strong class="info"></strong></span>
                </dd>
            </dl>
        </div>
        <p class="errorInfo jqicontent" style="display:none;">园丁豆不足!</p>
    </div>
</script>

<script type="text/html" id="t:学生头像">
    <%for(var i = 0; i < students.length; i++){%>
    <span class="actor" <%if(i >= 5){%> style="display:none;"<%}%>>
    <i>
        <%if (students[i].studentImg != '') {%>
        <img width="80" height="80" src="<@app.avatar href='<%=students[i].studentImg%>'/>"/>
        <%}else{%>
        <img width="80" height="80" src="<@app.link href="public/skin/teacherv3/images/smartclazz/null-user.png"/>" style="width: 80px; height: 80px;"/>
        <%}%>
    </i>
    <strong><%=students[i].studentName%></strong>
    </span>
    <%}%>
</script>

<#--奖励项 + 设置学豆数-->
<script type="text/html" id="t:奖励项和学豆">
    <div class="smartclazz_award_popup">
        <div class="ra_list">
            <p class="title">奖励对象：</p>
            <div class="icon icon_arrow_before icon_disable" style="display: none;"></div>
            <div class="icon icon_arrow_back icon_disable" style="display: none;"></div>
            <div class="con" id="selectStudentList">学生列表</div>
        </div>
        <div class="ra_list">
            <p class="title">请选择学生表现： </p>
            <div id="rewardItemList" class="remark">
                <ul>
                    <li class="active" data-content="回答问题非常棒" data-reward_type='ANSWER_BEST' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/answerbest.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>回答很棒</span></a>
                    </li>
                    <li data-content="在团队合作中表现非常棒" data-reward_type='TEAMWORK' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/teamwork.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>团队合作</span></a>
                    </li>
                    <li data-content="在课上积极发言，表现非常棒" data-reward_type='ACTIVELY_SPEAK' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/activelyspeak.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>积极发言</span></a>
                    </li>
                    <li data-content="认真听讲，表现非常棒" data-reward_type='LISTEN_CAREFULLY' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/activelyspeak.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>认真听讲</span></a>
                    </li>
                    <li data-content="遵守纪律，表现非常棒" data-reward_type='FOLLOW_RULES' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/teamwork.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>遵守纪律</span></a>
                    </li>
                    <li data-content="认真思考，表现非常棒" data-reward_type='THINK_SERIOUSLY' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/answerbest.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>认真思考</span></a>
                    </li>
                    <li data-content="学习进步，表现非常棒" data-reward_type='MAKE_GREAT_PROGRESS' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/answerbest.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>学习进步</span></a>
                    </li>
                    <li data-content="态度认真，表现非常棒" data-reward_type='NICE_ATTITUDE' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/teamwork.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>态度认真</span></a>
                    </li>
                    <li data-content="" data-reward_type='CUSTOM_TAG' data-audio_url='<@app.link href="public/skin/teacherv3/images/smartclazz/teamwork.mp3"/>'>
                        <a href="javascript:void (0)"><i class="icon icon_star"></i><span>自定义</span></a>
                    </li>
                </ul>
            </div>
            <div id="customTag" class="remark" style="display: none; position: relative;margin-top: 10px;">
                <label for="customTagContent" style="position: absolute; left: 10px; top: 2px; color: #999;">请输入学生表现(最大长度20个字符)</label>
                <input id="customTagContent" type="text" name="customTagContent" value="" style="width: 285px;" maxlength="20" class="w-int">
            </div>
        </div>
        <div class="ra_list">
            <p class="title">请选择奖励学豆数量： </p>
            <div class="w-addSub-int">
                <a href="javascript:void (0)" id="diffIntegral" class="w-btn w-btn-mini">-</a>
                <input id="rewardIntegralCnt" style="width: 138px" type="text" name="rewardIntegralCnt" value="1" maxlength="3" class="w-int">
                <a href="javascript:void (0)" id="addIntegral" class="w-btn w-btn-mini">+</a>
            </div>
            <div class="t-pubfooter-btn">
                <a href="javascript:void(0);" id="pop_reward_cancel_btn" class="v-wind-close w-btn w-btn-green w-btn-small">取消</a>
                <a href="javascript:void(0);" id="pop_reward_ok_btn" data-clazzid="14831" data-contentnum="1" class="v-wind-submit w-btn w-btn-small">确定</a>
            </div>
        </div>
    </div>
</script>
<script type="text/html" id="t:奖励成功结果">
    <div class="smartclazz_award_success_popup">
        <div class="hb_bg">
            <p>此页面将在 5 秒 后关闭</p>
        </div>
        <div class="hb_con">
            <h2><%=content%> <span class="smart_cloud smart_cloud_2"></span><strong>+<%=integralCnt%></strong></h2>
            <div class="con">
                <% if(students.length > 5){%>
                <span class="actor">
                    <i><img width="80" height="80" src="<@app.link href="public/skin/teacherv3/images/smartclazz/null-user.png"/>"></i>
                    <strong>共<%=students.length%>人</strong>
                </span>
                <%}else{
                for(var i = 0; i < students.length; i++){
                %>
                <span class="actor" <%if(i >= 5){%> style="display:none;"<%}%>>
                <i>
                    <%if (students[i].studentImg != '') {%>
                    <img width="80" height="80" src="<@app.avatar href='<%=students[i].studentImg%>'/>">
                    <%}else{%>
                    <img width="80" height="80" src="<@app.link href="public/skin/teacherv3/images/smartclazz/null-user.png"/>">
                    <%}%>
                </i>
                <strong><%=students[i].studentName%></strong>
                </span>
                <%}}%>
            </div>
        </div>
    </div>
</script>

<#--加载图标-->
<script id="t:业务处理图标" type="text/html">
    <div style="height: 100%;" class="loadingImg">
        <span class="loading_big"></span>
        <span class="inline_df_share text_big">处理中…</span>
        <span class="inline_em_share"></span>
    </div>
</script>
