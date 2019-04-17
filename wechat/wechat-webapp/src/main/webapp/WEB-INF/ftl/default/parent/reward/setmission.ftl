<#import "../layout.ftl" as setMission>
<@setMission.page title='添加新任务' pageJs="setmission">
<@sugar.capsule css=['jbox','ucentersetmission'] />
<style>
    body {
        background-color: #f1f3f6;
    }
    a,a:visited { color: #333; }
    a { text-decoration: none; }
</style>
<div style="display: none;" id="sidValue">${studentId!0}</div>
<div class="pr-box" id="missionBox" data-bind="visible: !showAddBox()">
    <ul class="pr-list">
        <#if !arranged>
            <li class="clearfix">
                <div class="pic pic-gold"><span>+10</span></div>
                <div class="txt">
                    <div>10学豆奖励</div>
                    <div>每月免费赞助家长10学豆</div>
                </div>
                <div class="box">
                    <a href="#" class="white">查看</a>
                    <a href="#" class="green set-mission set_integral_mission" data-key="beans" data-bind="click: addTenReward">添加</a>
                </div>
            </li>
        </#if>
        <li class="clearfix set-mission" data-key="auto">
            <div class="pic pic-add"></div>
            <div class="txt">
                <div>自定义奖励</div>
                <div></div>
            </div>
            <div class="box"><a href="#" class="white">查看</a><a href="#" class="green" data-bind="click: addSelfReward">添加</a></div>
        </li>
    </ul>
</div>

<div class="view-detailMore-main" id="set_customize_mission" data-bind="visible: showAddBox()">
    <div style="" class="check-class-box">
        <h2>目标次数</h2>
        <div class="cc-b">
            <ul id="customize_mission_count" class="showClazzListByEdu">
                <li data-num="1"><span class="clazz-t active"><i class="icon-check-purple"></i>1次</span></li>
                <li data-num="2"><span class="clazz-t"><i class="icon-check-purple"></i>2次</span></li>
                <li data-num="3"><span class="clazz-t"><i class="icon-check-purple"></i>3次</span></li>
                <li data-num="5"><span class="clazz-t"><i class="icon-check-purple"></i>5次</span></li>
                <li data-num="7"><span class="clazz-t"><i class="icon-check-purple"></i>7次</span></li>
                <li data-num="10"><span class="clazz-t"><i class="icon-check-purple"></i>10次</span></li>
            </ul>
        </div>
    </div>
    <div style="" class="check-class-box">
        <h2>目标内容</h2>
        <input data-bind="value: contents" class="w-input" maxlength="20" type="text" placeholder="扫地、早睡、考试99分等可按次数度量的目标">
        <h2>家长奖励</h2>
        <input class="w-input" maxlength="20" type="text" placeholder="除10学豆以外，奖励均由家长提供" data-bind="value: missionReward,visible: !showTenReward()">
        <span style="margin: 0 0 0 50px;" data-bind="visible: showTenReward()">10学豆</span>
    </div>
    <a data-bind="click: createMissionSub" id="do_set_customize_mission" class="btn_mark_purple" style="color: #ffffff;" href="javascript:void (0);">确定</a>
</div>

<div class="pr-wave">
    ${promotionBanner!""}
    <div class="top"></div>
    <div class="bot"></div>
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'parentreward',
                op: 'parentreward_do_set_mission'
            })
        })
    }
</script>
</@setMission.page>