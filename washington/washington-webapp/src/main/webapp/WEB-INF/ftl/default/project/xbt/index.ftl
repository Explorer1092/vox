<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="期末英雄团"
pageJs=['init']
pageJsFile={"init" : "public/script/project/xbt"}
pageCssFile={"css" : ["public/skin/project/xbt/css/skin"]}
>
<div class="readingMachine-top">
    <#--<div class="rem-head">
        <div class="inner">
            <a href="javascript:void(0);" class="back"></a>
            <a href="javascript:void(0);" class="share_btn">分享</a>
        </div>
    </div>-->
    <div class="rem-banner">
        <img src="<@app.link href="public/skin/project/xbt/images/banner01.png"/>">
    </div>
</div>

<#if isEnd!false>   <#--活动已结束-->
<div class="readingMachine-content clearfix">
    <div class="rem-info" style="clear:both;padding-left:.9rem;padding-right:.9rem;text-align:center;padding-bottom:1.75rem;line-height:1.2rem;">
        <#if integral gt 0>
            <p>活动期间共邀请${memberCount}人</p>
            <p style="margin-top: .8rem;">奖励${integral}学豆会在1周内发放到学生账户</p>
        <#else>
            <p>活动期间共邀请${memberCount}人，遗憾未得奖</p>
            <p style="margin-top: .8rem;">下次活动再来努力吧！</p>
        </#if>
    </div>
</div>
<#else>             <#--活动进行中-->
<div data-bind="template: {name: templateContent()}">
<#--template-->
</div>
</#if>

<script type="text/html" id="T:StepModuleIndex">
    <div class="readingMachine-mode">
        <a href="javascript:void(0);" class="js-rule rem-right" data-bind="click: $root.clickRule">详细规则</a>
        <a href="javascript:;" class="js-invite rem-left" data-bind="click: $root.clickInvite" style="width: 11rem;">
            <span class="info1">呼唤朋友 >></span>
            <div class="info2">邀请好友一起来玩哦！</div>
        </a>
    </div>
    <#if (hasGroup && memberList?has_content)!false>
        <div class="readingMachine-list">
            <div class="rem-title"><span class="num">${memberList?size}个好友</span>已参团使用点读机,继续努力哦！</div>
            <ul>
                <#list memberList as item>
                <li>
                    <div class="rem-image"><img src="${item.avatarUrl}@100w_1o_75q"/></div>
                    <div class="rem-name">${(item.studentName)!'--'}</div>
                </li>
                <#--<li class="noImage">
                    <div class="rem-image"><img src="${item.avatarUrl}@100w_1o_75q"/></div>
                    <div class="rem-name">${(item.studentName)!'--'}</div>
                </li>-->
                </#list>
                <li class="addImage">
                    <a href="javascript:;" class="js-invite for-log" data-bind="click: $root.clickInvite">
                        <div class="rem-image"><img src="<@app.link href='public/skin/project/xbt/images/add.png'/>"></div>
                    </a>
                </li>
            </ul>
        </div>
    </#if>
    <div class="readingMachine-content">
        <div class="rem-info" style="clear:both;text-align: left;padding-left:.9rem;padding-right:.9rem;padding-bottom:1.75rem;line-height:1.2rem;">
            <p style="margin-bottom:1rem">
                建团的好处：<br>
                1、小伙伴参团后可以一起使用点读机复习，互相鼓励互相督促，效果棒棒哒。<br>
                2、参团人数越多，学豆奖励越多。<br>
            </p>
            <p style="margin-bottom:1rem">
                如何让更多人参团：<br>
                1、要尽早的分享出去，让同学第一时间加入你的团。<br>
                2、每天至少分享一次，让更多同学看到。<br>
                3、让爸爸妈妈一起分享，这样家长好朋友的孩子也能加入你的团。<br>
            </p>
            <p>
                为何分享给很多小伙伴，团队人数却不增加：<br>
                1、小伙伴在你分享之前已经加入其他团了。<br>
                2、小伙伴也许没看见呢，试试再分享几次。<br>
                3、分享给新用户后，用户得注册账号才能入团。
            </p>
        </div>
    </div>
</script>

<!--活动规则-->
<div class="readingMachine-pop" style="display: none;" data-bind="visible: isRuleShow">
    <div class="inner" style="height: auto; padding-bottom: 15px;">
        <div class="close" data-bind="click: clickRule"></div>
        <div class="title">活动规则</div>
        <div class="content">
            <div><p class="num">1、</p><p class="con">活动期间，成功拉同学入团使用点读机可获得对应的学豆奖励</p></div>
            <div><p class="num"> </p><p class="con">参团人数达到20人，奖励60学豆</p></div>
            <div><p class="num"> </p><p class="con">参团人数达到10人，奖励30学豆</p></div>
            <div><p class="num"> </p><p class="con">参团人数达到6人，奖励20学豆</p></div>
            <div><p class="num"> </p><p class="con">参团人数达到3人，奖励10学豆</p></div>
            <div><p class="num"> </p><p class="con">同学最多可获得一档奖励哦！</p></div>
            <div><p class="num">2、</p><p class="con">组团规则</p></div>
            <div><p class="num"> </p><p class="con">每个同学只可以建立一个团</p></div>
            <div><p class="num"> </p><p class="con">每个同学只可以参加一个团</p></div>
            <div><p class="num">3、</p><p class="con">奖励发放：我们会在活动结束后一周内，统一向同学发放学豆奖励</p></div>
            <div><p class="con" style="margin-top:1.2rem;">请使用1.6.5以上版本</p></div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var initMode = 'IndexMain';
    var userData = {
        userType: "${(currentUser.userType)!0}",
        currentUserName: "${currentUserName!}",
        currentUserAvatarUrl: "${currentUserAvatarUrl!}",
        userId: "${userId!}"
    };
    var hasGroup = '未成团';
    <#if (hasGroup && memberList?has_content)!false>
        hasGroup = '已成团';
    </#if>
</script>
</@layout.page>