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
<div class="readingMachine-content">
    <div class="rem-arrow">活动已结束，下次早点来</div>
    <a class="js-redirect round_btn join_right_now do_open_client" href="javascript:void(0);"
       data-type="parent"
       data-ad_type="news_detail"
       data-ad_val="/view/mobile/parent/learning_tool/huiben/list?app_version=1.6"
       data-yq_type="webview" data-yq_val="/view/mobile/parent/learning_tool/huiben/list?app_version=1.6">进入绘本</a>
</div>
<#else>             <#--活动进行中-->
<div data-bind="template: {name: templateContent(), data : database()}">
<#--template-->
</div>
</#if>

<!--输入手机号码-->
<script type="text/html" id="T:StepModule-Mobile">
    <div class="readingMachine-box">
        <div class="li" style="padding-top:0;margin-right: 0;">
            <div class="rem-image">
                <!-- ko if: $root.userData.avatarUrl != '' -->
                <img src="" data-bind="attr: {src: $root.userData.avatarUrl + '@100w_1o_75q'}" />
                <!-- /ko -->
                <!-- ko if: $root.userData.avatarUrl == '' -->
                <img src="<@app.avatar href=""/>" />
                <!-- /ko -->
                <div class="img-mask"></div>
            </div>
            <div class="rem-name" style="color:transparent;">X</div>
        </div>
        <div class="rem-info" style="float: none;overflow: hidden;">
            <p class="inner-text"><span class="name" data-bind="text: $root.userData.userName">---</span>邀请你参加 Ta 的亲子读书会</p>
        </div>
    </div>

    <div class="readingMachine-column clearfix" style="padding-bottom: 15rem;">
        <p class="verifInfo">
            <input type="text" class="txt" style="margin-right: -10rem;width: 58%;" data-bind="value: $root.mobile, event: {input: function(){$root.justNumber(event, $root.mobile, 11)}}" placeholder="请输入手机号"/>
            <a href="javascript:void(0);" class="js-join join_right_now middle_btn" data-bind="click: $root.joinNow">立即参加</a>
        </p>
        <div class="txt-info" style="clear:both;line-height: 1.5rem;">
            快来亲子读书会一起阅读英文绘本啦！
        </div>
    </div>
</script>

<!--输入图片验证码和短信验证码-->
<script type="text/html" id="T:StepModule-Ver">
    <div class="readingMachine-box">
        <div class="li" style="padding-top:0;margin-right: 0;">
            <div class="rem-image">
                <!-- ko if: $root.userData.avatarUrl != '' -->
                <img src="" data-bind="attr: {src: $root.userData.avatarUrl + '@100w_1o_75q'}" />
                <!-- /ko -->
                <!-- ko if: $root.userData.avatarUrl == '' -->
                <img src="<@app.avatar href=""/>" />
                <!-- /ko -->
                <div class="img-mask"></div>
            </div>
            <div class="rem-name" style="color:transparent;">X</div>
        </div>
        <div class="rem-info" style="float: none;overflow: hidden;">
            <p class="inner-text"><span class="name" data-bind="text: $root.userData.userName">---</span>邀请你参加 Ta 的亲子读书会</p>
        </div>
    </div>

    <div class="readingMachine-column clearfix" style="padding-bottom: 15rem;">
        <p class="verifCode clearfix">手机号：<span data-bind="text: $root.mobile">--</span> </p>
        <p class="verifInfo clearfix">
            <input type="text" data-bind="value: $root.imgCode, event: {input: function(){$root.justNumber(event, $root.imgCode, 4)}}" class="txt" placeholder="输入图形验证码"/>
            <span class="codeImg" data-bind="click: $root.clickImgCode">
                <img class=" round_shadow" data-bind="attr: {src: $root.imgSrc}">
            </span>
        </p>
        <p class="verifInfo clearfix">
            <input type="text" data-bind="value: $root.smsCode, event: {input: function(){$root.justNumber(event, $root.smsCode, 4)}}" class="txt" placeholder="输入短信验证码"/>
            <!-- ko if: $root.recordTime() <= 0 -->
            <a href="javascript:void(0);" class="js-getMobileCode default_btn blue_btn round_shadow" data-bind="click: $root.getMobileCode">获取验证码</a>
            <!-- /ko -->
            <!-- ko if: $root.recordTime() > 0 -->
            <span href="javascript:void(0);" class="default_btn retry_btn"><span data-bind="text:$root.recordTime()"></span>s后重试</span>
            <!-- /ko -->
        </p>
        <a href="javascript:void(0);" class="js-join-old join_right_now" data-bind="click: $root.submitForm" style="margin-top: 2rem;">立即参加</a>
    </div>
</script>

<!--结果页：下载家长通APP-->
<script type="text/html" id="T:StepModule-Over">
    <div class="readingMachine-content">
        <div class="rem-arrow" data-bind="text: $root.database().info"></div>
        <a href="javascript:;" data-type="parent" class="js-download round_btn download_btn do_open_client">下载家长通APP，阅读英文绘本</a>
        <div class="rem-info">家长通里有数百本英文绘本！全部免费哦！</div>
    </div>
</script>

<!--结果页：去建团-->
<script type="text/html" id="T:StepModule-Success">
    <!-- ko ifnot: no_child -->
    <div class="readingMachine-content">
        <div class="rem-arrow" data-bind="text: info">----</div>
        <div style="display: flex;justify-content: center;flex-wrap: wrap;">
            <a class="js-build build_team do_open_client" href="javascript:void(0);"
               data-type="parent"
               data-ad_type="news_detail"
               data-ad_val="/usermobile/xbt/index.vpage?activity=readingZdy"
               data-yq_type="webview"
               data-yq_val="/usermobile/xbt/index.vpage?activity=readingZdy">发起</a>
            <a class="js-redirect join_picbook do_open_client" href="javascript:void(0);"
               data-type="parent"
               data-ad_type="news_detail"
               data-ad_val="/view/mobile/parent/learning_tool/huiben/list?app_version=1.6"
               data-yq_type="webview" data-yq_val="/view/mobile/parent/learning_tool/huiben/list?app_version=1.6">进入绘本</a>
        </div>
        <div class="rem-info" style="clear:both;line-height: 1.5rem;">
            快来亲子读书会一起阅读英文绘本啦！
        </div>
    </div>
    <!-- /ko -->
    <!-- ko if: no_child -->
    <div class="readingMachine-content">
        <div class="rem-arrow" style="padding: .55rem 0;">您还没绑定孩子信息呢，快去家长通绑定吧！</div>
        <a href="javascript:;" data-type="parent" class="js-goBind round_btn join_right_now do_open_client">去绑定</a>
        <div class="rem-info">绑定孩子可以了解孩子在学校的情况哦。</div>
    </div>
    <!-- /ko -->
</script>
<script type="text/javascript">
    var initMode = 'ShareMain';
</script>
</@layout.page>