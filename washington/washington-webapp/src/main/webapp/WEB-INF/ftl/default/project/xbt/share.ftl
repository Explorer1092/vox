<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="期末英雄团"
pageJs=['init']
pageJsFile={"init" : "public/script/project/xbt"}
pageCssFile={"css" : ["public/skin/project/xbt/css/skin"]}
>
<div class="readingMachine-top" style="margin: 0;">
    <div class="rem-banner">
        <img src="<@app.link href="public/skin/project/xbt/images/banner01.png"/>">
    </div>
</div>
<#if isEnd!false>   <#--活动已结束-->
<div class="readingMachine-content">
    <div class="rem-info" style="padding: 2rem 0;">活动已结束，下次早点来</div>
    <a class="js-redirect default_btn do_open_client" href="javascript:void(0);"
       data-type="parent"
       data-ad_type="news_detail"
       data-ad_val="/view/mobile/parent/learnTools_guide?app_version=1.6"
       data-yq_type="webview" data-yq_val="/view/mobile/parent/learnTools_guide?app_version=1.6"
       style="width: 5.25rem;padding-left:0;padding-right:0;">进入点读机</a>
</div>
<#else>             <#--活动进行中-->
<div data-bind="template: {name: templateContent(), data : database()}">
<#--template-->
</div>
</#if>

<script type="text/html" id="T:StepModule-Mobile">
    <div class="readingMachine-box">
        <div class="rem-icon">
            <!-- ko if: $root.userData.avatarUrl != '' -->
            <img src="" data-bind="attr: {src: $root.userData.avatarUrl + '@100w_1o_75q'}" />
            <!-- /ko -->
            <!-- ko if: $root.userData.avatarUrl == '' -->
            <img src="<@app.avatar href=""/>" />
            <!-- /ko -->
        </div>
        <div class="rem-info"><span class="name" data-bind="text: $root.userData.userName">---</span>邀请你参加他的期末英雄团</div>
    </div>
    <!--输入验证码-->
    <div class="readingMachine-column">
        <p class="verifCode">输入手机号码</p>
        <p class="verifInfo">
            <input type="text" data-bind="value: $root.mobile" class="txt" maxlength="11" placeholder="请输入手机号"/>
            <a href="javascript:void(0);" class="js-join default_btn" data-bind="click: $root.joinNow">立即参加</a>
        </p>
        <p class="verifCode" style="line-height: 1.5rem;padding-top:1rem;">
            同学们期末一起使用点读机 组团复习效果棒！<br>
            大家赶快行动起来吧！
        </p>
    </div>
</script>

<script type="text/html" id="T:StepModule-Ver">
    <div class="readingMachine-box">
        <div class="rem-icon">
            <!-- ko if: $root.userData.avatarUrl != '' -->
            <img src="" data-bind="attr: {src: $root.userData.avatarUrl + '@100w_1o_75q'}" />
            <!-- /ko -->
            <!-- ko if: $root.userData.avatarUrl == '' -->
            <img src="<@app.avatar href=""/>" />
            <!-- /ko -->
        </div>
        <div class="rem-info"><span class="name" data-bind="text: $root.userData.userName">李鹏</span>邀请你参加他的期末英雄团</div>
    </div>
    <!--输入验证码-->
    <div class="readingMachine-column">
        <p class="verifCode">手机号：<span data-bind="text: $root.mobile">--</span> </p>
        <p class="verifInfo">
            <input type="text" data-bind="value: $root.imgCode" class="txt" maxlength="4" placeholder="输入图形验证码"/>
            <span class="codeImg" style="width: 4.5rem; cursor: pointer" data-bind="click: $root.clickImgCode"><img src="" data-bind="attr: {src: $root.imgSrc}"/></span>
        </p>
        <p class="verifInfo">
            <input type="text" data-bind="value: $root.smsCode" class="txt" maxlength="4" placeholder="输入短信验证码"/>
            <!-- ko if: $root.recordTime() <= 0 -->
                <a href="javascript:void(0);" class="js-getMobileCode default_btn yellow_btn" data-bind="click: $root.getMobileCode">获取验证码</a>
            <!-- /ko -->
            <!-- ko if: $root.recordTime() > 0 -->
                <span href="javascript:void(0);" class="default_btn gray_btn"><span data-bind="text:$root.recordTime()"></span>s后重试</span>
            <!-- /ko -->
        </p>
    </div>
    <div class="readingMachine-btn">
        <a href="javascript:void(0);" class="js-join-old default_btn" data-bind="click: $root.submitForm">立即参加</a>
    </div>
</script>

<script type="text/html" id="T:StepModule-Over">
    <div class="readingMachine-content">
        <div class="rem-arrow" data-bind="text: $root.database().info"></div>
        <a href="javascript:;" data-type="parent" class="js-download default_btn do_open_client">下载家长通APP，使用点读机</a>
        <div class="rem-info">下载家长通有好多惊喜哦！</div>
    </div>
</script>

<script type="text/html" id="T:StepModule-Success">
    <!-- ko ifnot: no_child -->
    <div class="readingMachine-content">
        <div class="rem-arrow" data-bind="text: info">----</div>

        <a class="js-build default_btn do_open_client" href="javascript:void(0);"
           data-type="parent"
           data-ad_type="news_detail"
           data-ad_val="/usermobile/xbt/index.vpage"
           data-yq_type="webview"
           data-yq_val="/usermobile/xbt/index.vpage"
           style="width: 5.25rem;padding-left:0;padding-right:0;">建团</a>

        <a class="js-redirect default_btn do_open_client" href="javascript:void(0);"
           data-type="parent"
           data-ad_type="news_detail"
           data-ad_val="/view/mobile/parent/learnTools_guide?app_version=1.6"
           data-yq_type="webview" data-yq_val="/view/mobile/parent/learnTools_guide?app_version=1.6"
           style="width: 5.25rem;padding-left:0;padding-right:0;">进入点读机</a>

        <div class="rem-info">建立自己的期末英雄团一起来战！</div>
        <div class="rem-info" style="padding-top:.8rem;">和好友组团使用点读机复习 效果翻倍哦！</div>
    </div>
    <!-- /ko -->

    <!-- ko if: no_child -->
    <div class="readingMachine-content">
        <div class="rem-arrow">您还没绑定孩子信息呢，<br/>快去家长通绑定吧！</div>
        <a href="javascript:;" data-type="parent" class="js-goBind default_btn do_open_client">去绑定</a>
        <div class="rem-info">绑定孩子可以了解孩子在学校的情况哦。</div>
    </div>
    <!-- /ko -->
</script>

<script type="text/javascript">
    var initMode = 'ShareMain';
</script>
</@layout.page>