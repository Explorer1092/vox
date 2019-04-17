<#import "../layout/webview.layout.ftl" as temp />
<@temp.page
title="科技改变教学"
bodyClass=""
pageJs=["newstudapprove"]
pageJsFile={"newstudapprove" : "public/script/project/newstudapprove"}
pageCssFile={"index" : ["public/skin/project/activity/newstudapprove/css/newstudapprove"]}>
    <#include "../layout/project.header.ftl"/>
<div class="kj-wrap" data-bind="visible: isLoaded" style="display:none;">
    <div class="headerBox">
        <div class="banner"></div>
        <i class="handIcon"></i>
        <!--活动规则入口-->
        <a href="javascript:void(0)" class="ruleLink" data-bind="click: openActivDetail,visible: activDetailBtn"></a>
    </div>
    <!--ko ifnot:participate -->
    <div class="joinBtn-box">
        <a href="javascript:void(0)" class="joinBtn" data-bind="click:joinActiv"></a>
    </div>
    <!--/ko-->
    <div data-bind="visible: isShowProg" style="display: none;">
    <div class="progressBox">
        <div class="kj-title">
            <img src="<@app.link href="public/skin/project/activity/newstudapprove/images/kj-icon10.png"/>">
            <p class="sTxt">当前新生学生数：<span data-bind="text: newStudNum"></span>人</p>
            <p class="sTxt">每天<span>5</span>点更新</p>
            <p class="sTxt">参加活动时间：<!--ko text: particTime --><!--/ko--></p>
        </div>
        <div class="progressMain" data-bind="template: {name: 'T:progress', data: dataStudent()}"></div>
    </div>
    <div class="stateBox">
        <p>说明：</p>
        <p>1. 新学生数是指：数学老师认证后，在活动期间完成3次以上数学作业的新学生数。本次活动前完成过3次以上数学作业的学生不包括在内</p>
        <p>2. 奖励在满足条件后的72小时内发放</p>
    </div>
    </div>
    <div class="rankingBox">
        <div class="kj-title">
            <img src="<@app.link href="public/skin/project/activity/newstudapprove/images/kj-icon09.png"/>">
            <p class="sTxt">每天<span>5</span>点更新</p>
        </div>
        <div class="rankMain">
            <div class="tHead">
                <div class="column01">排名</div>
                <div class="column02">地区</div>
                <div class="column03">学校</div>
                <div class="column04">教师</div>
                <div class="column05">新学生数</div>
            </div>
            <div class="tMain">
                <ul data-bind="foreach: rankListAll,visible:rankListAll().length > 0 " style="display:none">
                    <li>
                        <div class="column01" data-bind="text: $index()+ 1"></div>
                        <div class="column02" data-bind="text: regionName"></div>
                        <div class="column03" data-bind="text: schoolName"></div>
                        <div class="column04" data-bind="text: teacherName"></div>
                        <div class="column05" data-bind="text: studentCount"></div>
                    </li>
                </ul>
                <ul data-bind="visible:rankListAll().length == 0 " style="display:none">
                    <li class="tips-rank" style="line-height: 120px; color:#fff;text-align: center">暂无排名，请您加油哦！</li>
                </ul>
            </div>
            <div class="tCount">
                <div>我的排名：<span data-bind="text: isPartic"></span></div>
                <div>学生数：<span data-bind="text: newStudNum"></span></div>
            </div>
        </div>
    </div>
</div>
<div class="rulesPopup" data-bind="visible: activDetail" style="display: none">
    <div class="popInner">
        <div class="popHd"></div>
        <div class="popClose" data-bind="click: closeActivDetail">×</div>
        <div class="popTitle">活动规则详解</div>
        <div class="popMain">
            <div class="txt">
                <p class="title">活动时间：</p>
                <p>2018年2月26日 0点- 2018年4月15日 24点</p>
            </div>
            <div class="txt">
                <p class="title">活动对象：</p>
                <p>小学数学老师</p>
            </div>
            <div class="txt">
                <p class="title">活动条件及奖励：</p>
                <div data-bind="template: {name: 'T:activStudNum', data: activLevel}"></div>
            </div>
            <div class="txt">
                <p class="title">领奖有效期：</p>
                <p>1.2018年2月26日之前认证的老师，在2月26日-4月15日之间达到条件即有效</p>
                <p>2.2018年2月26日-2018年4月15日之间认证的老师，在认证后30天达到条件即有效</p>
            </div>
            <div class="txt">
                <p class="title">奖励发放：</p>
                <p>在满足条件后的72小时内发放</p>
            </div>
            <div class="txt">
                <p class="title">进度及排行更新时间：</p>
                <p>每天5点更新前一天的数据</p>
            </div>
            <div class="txt">
                <p class="title">奖励特殊说明：</p>
                <p>不同学段的奖励设置略有不同，具体以老师在活动页面所见为准；如老师在活动期间发生转校行为，将按照参与活动时的学校奖励设置进行发放</p>
            </div>
            <div class="copyRight">本次活动最终解释权归一起作业所有</div>
        </div>
        <div class="popFt"></div>
    </div>
</div>
<script type="text/html" id="T:progress">
    <div data-bind="template: {name: 'T:levelList', data: $root.database}"></div>
</script>
<script type="text/html" id="T:levelList">
    <!--completed——已完成-->
    <div class="column" data-bind="css: {'completed': $parent.auth}">
        <span class="num">1</span>
        <div class="desc">完成认证</div>
        <div class="state state01" data-bind="click:$root.linkAuthPag">如何认证<i class="qIcon"></i></div>
        <div class="state state02">已完成</div>
    </div>

    <!-- ko foreach:num1-->
    <div class="column" data-bind="css:{'completed':$root.newStudNum() >= $data }">
        <span class="num" data-bind="text:$index() + 2"></span>
        <div class="desc"><!--ko text:$data--><!--/ko-->名新学生</div>
        <div class="reward"><!--ko if:$index() == 0-->30<!--/ko--><!--ko if:$index() == 1-->60<!--/ko--><!--ko if:$index() == 2-->90<!--/ko-->元流量</div>
        <div class="state state01" data-bind="click: $root.AssignmentBtn.bind($data,$index())">去布置作业</div>
        <div class="state state02">已完成</div>
    </div>
    <!--/ko-->
</script>

<script type="text/html" id="T:activStudNum">
    <p class="details">参加活动的认证数学老师班级中，在奖励有效期期间累计完成该老师布置的3次数学作业的学生人数分别达到
        <!-- ko if:schoolLevel == 'A' -->
        15/30/45人，可累计获得30/60/90
        <!--/ko-->
        <!-- ko if:schoolLevel == 'B' -->
        20/40/60人，可累计获得30/60/90
        <!--/ko-->
        <!-- ko if:schoolLevel == 'C' -->
        30/60/90人，可累计获得30/60/90
        <!--/ko-->
        元流量奖励
    </p>
</script>
    <#include "../layout/project.footer.ftl"/>
</@temp.page>