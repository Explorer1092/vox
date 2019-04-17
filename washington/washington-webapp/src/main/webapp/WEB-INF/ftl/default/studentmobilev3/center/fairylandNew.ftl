<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="自学乐园"
pageJs=["fairyland"]
pageJsFile={"fairyland" : "public/script/mobile/student/fairyland"}
pageCssFile={"fairyland" : ["public/skin/studentmobile/back/css/skin-new"]}
>
<#--一起打年兽入口 2017-01-16 20:00:00 以后显示-->
<#if (ProductDevelopment.isProductionEnv())!false>
    <#assign __showNs = (.now gt '2017-01-16 20:00:00'?datetime('yyyy-MM-dd HH:mm:ss'))/>
<#else>
    <#assign __showNs = true/>
</#if>

<!-- ko if : success-->
<div class="fl-advBox messageBox" data-bind="visible: (appUserMessage().length > 0 && isMessageShow)" style="display: none;">
    <ul data-bind="foreach : appUserMessage">
        <li class="fl-adv">
            <a href="javascript:void(0)" data-bind="click : $parent.clickOpenGame, visible: launchUrl != ''" class="link">查看详情</a>
            <div class="adv-left">
                <i class="horn-icon"></i>
                <span class="news" data-bind="text: title">--</span>
            </div>
        </li>
    </ul>
</div>

<#-- 自学乐园banner -->
<div data-bind="click: bannerBtn, visible: appsInfo().length != 0" style="display: none;" class="fl-banner">
    <img src="<@app.link href="public/skin/studentmobile/back/images/little_tree.png"/>">
</div>

<div class="fl-section section01" data-bind="visible: (examList().length > 0)" style="display: none;">
    <div class="title">同步教辅<span>精准同步练习，轻松提高成绩</span></div>
    <ul class="listWrap">
        <!-- ko foreach : {data : examList, as : '_app'} -->
        <!--ko if: _app.catalogDesc == '同步教辅'-->
        <li data-bind="click : $root.clickOpenGame">
            <div class="fl-listInner">
                <div class="pic">
                    <img src="" data-bind="attr :{src : '<@app.link href='gridfs/'/>' + (_app.productIcon)}"/>
                    <!--ko if: $root.setAppStatus(_app.appStatus)-->
                    <span class="tag" data-bind="css : $root.setAppStatus(_app.appStatus).color"><!--ko text: $root.setAppStatus(_app.appStatus).name--><!--/ko--></span>
                    <!--/ko-->
                    <span class="label" data-bind="css :{ 'label-hot' : _app.hotFlag, 'label-new' : _app.newFlag, 'label-com' : _app.recommendFlag}"></span>
                </div>
                <div class="txt">
                    <p class="type"><!--ko text: _app.productName--><!--/ko--></p>
                    <p class="name"><!--ko text: _app.operationMessage--><!--/ko--></p>
                </div>
            </div>
        </li>
        <!--/ko-->
        <!--/ko-->
    </ul>
</div>

<div class="fl-section section02" data-bind="visible: (selfStudyList().length > 0)" style="display: none;">
    <div class="title">课外自学</div>
    <ul class="listWrap">
        <!-- ko foreach : {data : selfStudyList, as : '_app'} -->
        <!-- ko ifnot: appKey == 'BookListen'-->
        <li data-bind="click : $parent.clickOpenGame">
            <div class="fl-btns">
                <a href="javascript:void(0)" class="link">进入学习</a>
                <p class="count"><!--ko text: _app.operationMessage--><!--/ko--></p>
            </div>
            <div class="fl-listInner">
                <div class="pic">
                    <img src="" data-bind="attr :{src : '<@app.link href='gridfs/'/>' + (_app.productIcon)}"/>
                    <!--ko if: $root.setAppStatus(_app.appStatus)-->
                    <span class="tag" data-bind="css : $root.setAppStatus(_app.appStatus).color"><!--ko text: $root.setAppStatus(_app.appStatus).name--><!--/ko--></span>
                    <!--/ko-->
                    <span class="label" data-bind="css :{ 'label-hot' : _app.hotFlag, 'label-new' : _app.newFlag, 'label-com' : _app.recommendFlag}"></span>
                </div>
                <div class="txt">
                    <p class="type"><!--ko text: _app.productName--><!--/ko--></p>
                    <p class="name"><!--ko text: _app.productDesc--><!--/ko--></p>
                </div>
            </div>
        </li>
        <!--/ko-->

        <!-- ko if: appKey == 'BookListen'-->
        <li data-bind="click : $parent.clickOpenGame">
            <div class="fl-btns">
                <a href="javascript:void(0)" class="link">查看详情</a>
                <p class="count"><!--ko text: _app.operationMessage--><!--/ko--></p>
            </div>
            <div class="fl-listInner">
                <div class="pic">
                    <img src="" data-bind="attr :{src : '<@app.link href='gridfs/'/>' + (_app.productIcon)}"/>
                </div>
                <div class="txt">
                    <p class="type"><!--ko text: _app.productName--><!--/ko--></p>
                    <p class="name"><!--ko text: _app.productDesc--><!--/ko--></p>
                </div>
            </div>
        </li>
        <!--/ko-->
        <!--/ko-->
    </ul>
</div>

<div style="font-size: 0.45rem; color: #656565; text-align: center; line-height: 20px; display: none;" data-bind="visible: (appsInfo().length != 0)">
    <p>所有自学产品由第三方提供，请自愿开通使用</p>
    <p>是否开通不影响“一起作业”作业板块的免费使用</p>
</div>

<div data-bind="visible: (appsInfo().length == 0) && ajaxLoadFinished" style="display: none;">
    <p style="line-height: 10; text-align: center; color: #aaa;">敬请期待！</p>
</div>
<!-- /ko -->

<!-- ko ifnot : success-->
<div data-bind="visible: !success()" style="display: none;"><p style="line-height: 10; text-align: center; color: #aaa;">数据请求失败！</p></div>
<!-- /ko -->

<div data-bind="template : {name: koTemplateName()}"></div>
<!--自学乐园首页弹窗-->
<script type="text/html" id="tip_tem">
    <div class="gf-popup">
        <div class="gf-popupBox">
            <div class="hd"><span class="close" data-bind="click: koTemplateClose"></span></div>
            <div class="mn">
                <div class="info bg02">
                </div>
            </div>
            <div class="ft">
                <a href="javascript:;" class="btn" id="toReceiveBtn">立即领取</a>
            </div>
        </div>
    </div>
</script>

<script>
    var _fairylandMap = {
        page: 'new',
        __showNs: ${__showNs?string}
    };
    var current_uid = ${currentUser.id!};
</script>

</@layout.page>