<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="亲子读书会"
pageJs=['init']
pageJsFile={"init" : "public/script/project/xbt"}
pageCssFile={"css" : ["public/skin/project/picbook/css/skin"]}
>
<div class="readingMachine-top">
    <img src="<@app.link href="public/skin/project/picbook/images/body_bg_top_low.jpg"/>">
</div>
<#if isEnd!false>   <#--活动已结束-->
<div class="readingMachine-content clearfix">
    <div class="prompt_txt" style="font-size:.8rem;text-align: center;padding-top:2rem;">
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
    <div class="readingMachine-mode-wrapper">
        <div class="readingMachine-mode">
            <div class="rem-right-wrapper">
                <a href="javascript:void(0);" class="js-rule rem-right" data-bind="click: $root.clickRule"></a>
            </div>
            <a href="javascript:void(0);" class="js-invite rem-left" data-bind="click: $root.clickInvite"></a>
        </div>
    </div>
    <!--参团列表-->
    <#if (hasGroup && memberList?has_content)!false>
    <div class="readingMachine-list clearfix">
        <div class="rem-title"><span class="num">${memberList?size}个好友</span>已加入亲子读书会,继续努力哦！</div>
        <div class="friends-list-wrapper">
            <ul class="friends-list">
            <#list memberList as item>
                <li class="li">
                    <div class="rem-image"><img src="${item.avatarUrl}@100w_1o_75q"/><div class="img-mask"></div></div>
                    <div class="rem-name">${(item.studentName)!'--'}</div>
                </li>
            </#list>
            <li class="addImage li">
                <a href="javascript:;" class="js-invite for-log" data-bind="click: $root.clickInvite">
                    <div class="rem-image"><img src="<@app.link href="public/skin/project/picbook/images/plus_green.png"/>"><div class="img-mask"></div></div>
                    <div class="rem-name" style="color:transparent">x</div>
                </a>
            </li>
        </ul>
    </div>
    </#if>
    <!--好处文案-->
    <div class="readingMachine-content clearfix">
        <div class="prompt_txt">
            <p class="bold" style="margin-bottom:1rem">快来亲子读书会一起阅读英文绘本啦！</p>
            <p style="margin-bottom:.25rem">
                <span class="bold">发起读书会的好处：</span><br>
                1、同学加入读书会后可以一起阅读绘本，互相鼓励互相督促，效果棒棒哒。<br>
                2、加入同学越多，学豆奖励越多。
            </p>
            <p style="margin-bottom:.25rem">
                <span class="bold">如何让更多同学加入读书会：</span><br>
                1、要尽早的分享出去，让同学第一时间加入。<br>
                2、每天至少分享一次，让更多同学看到。<br>
                3、让爸爸妈妈一起分享，这样家长好朋友的孩子也能加入你的读书会。<br>
            </p>
            <p style="margin-bottom:.25rem">
                <span class="bold">为何分享给很多同学，读书会人数却不增加：</span><br>
                1、同学在你分享之前已经加入其他读书会了。<br>
                2、同学也许没看见呢，试试再分享几次。<br>
                3、分享给新用户后，用户得注册账号才能加入哦。
            </p>
        </div>
    </div>
</script>

<!--活动规则-->
<div class="readingMachine-pop" style="display: none;" data-bind="visible: isRuleShow">
    <div class="inner">
        <div class="close" data-bind="click: clickRule"></div>
        <div class="title">活动规则</div>
        <div class="content">
            <div>
                <p class="rule-title">1、活动期间，成功邀请同学入会阅读英文绘本可获得对应的学豆奖励</p>
                <div class="rule-detail">
                    <p>入组人数达到15人，奖励20学豆</p>
                    <p>入组人数达到10人，奖励15学豆</p>
                    <p>入组人数达到6人，奖励10学豆</p>
                    <p>入组人数达到3人，奖励5学豆</p>
                    <p style="color: #fffc02">每名同学最多领取一档奖励</p>
                </div>
            </div>
            <div>
                <p class="rule-title">2、读书会规则</p>
                <div class="rule-detail">
                    <p>每个同学只可以建立一个读书会</p>
                    <p>每个同学只可以加入一个读书会</p>
                </div>
            </div>
            <div>
                <p class="rule-title">3、奖励发放</p>
                <div class="rule-detail">
                    <p>我们会在活动结束后一周内，统一向同学发放学豆奖励</p>
                </div>
            </div>
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
    var hasGroup = '未参加';
        <#if (hasGroup && memberList?has_content)!false>
        hasGroup = '已参加';
        </#if>
</script>
</@layout.page>