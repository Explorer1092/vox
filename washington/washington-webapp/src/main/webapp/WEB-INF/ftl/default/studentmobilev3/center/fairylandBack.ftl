<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="自学乐园"
pageJs=["fairyland"]
pageJsFile={"fairyland" : "public/script/mobile/student/fairyland"}
pageCssFile={"fairyland" : ["public/skin/studentmobile/back/css/skin"]}
>
<div class="clearfix"></div>
<#if (ftlmacro.devTestSwitch)!false>
    <a href="javascript:void (0);" data-bind="click: bannerBtn">抽奖测试入口</a>
</#if>

<!-- ko if : isShowBanner -->
<div class="fairylandBanner" data-bind="foreach: headerBanner().data">
    <a href="javascript:;" data-bind="click: $parent.clickOpenBanner">
        <img src="" height="100%" data-bind="attr: {src : $parent.headerBanner().imgDoMain + 'gridfs/' + img}">
    </a>
</div>
<!-- /ko -->

<!-- ko if : success-->
<div class="sf-listWrap">
    <div class="bubbleTip" data-bind="visible: (appUserMessage().length > 0 && isMessageShow), foreach : appUserMessage" style="display: none;">
        <div style="cursor: pointer" data-bind="click : $parent.clickOpenGame">
            <span data-bind="text: title">--</span>
            <span class="btn">查看详情</span>
        </div>
    </div>

    <div class="fairylandBox" data-bind="visible: (appsInfo().length > 0), foreach : appsInfo" style="display: none;">
        <!-- ko ifnot: appKey == 'BookListen'-->
        <a href="javascript:void(0)" class="sf-list" data-bind="click : $parent.clickOpenGame, css :{ 'hot' : hotFlag, 'new' : newFlag, 'tj' : recommendFlag}">
            <div class="pic">
                <img data-bind="attr :{src : '<@app.link href='gridfs/'/>' + (backgroundImage|| productIcon)}"/>
            </div>
            <div class="info">
                <div class="box">
                    <p class="name">
                        <span data-bind="text : productName"></span>
                        <span class="state red" data-bind="visible: (appStatus == 0)">未开通</span>
                        <span class="state yellow" data-bind="visible: (appStatus == 1)">已过期</span>
                        <span class="state" data-bind="visible: (appStatus == 2)">已开通</span>
                    </p>
                    <p class="intro" data-bind="text : productDesc">每日同步练习，奖学豆</p>
                    <p class="count" data-bind="text: operationMessage">你们班已经有13个人在学 ></p>
                </div>
            </div>
        </a>
        <!-- /ko -->

        <!-- ko if: appKey == 'BookListen'-->
        <a href="javascript:void(0)" class="sf-list" data-bind="click : $parent.clickOpenGame">
            <div class="pic">
                <img data-bind="attr :{src : '<@app.link href='gridfs/'/>' + (backgroundImage|| productIcon)}"/>
            </div>
            <div class="info">
                <div class="box">
                    <p class="name">
                        <span data-bind="text : productName"></span>
                    </p>
                    <p class="intro" data-bind="text : productDesc">每日同步练习，奖学豆</p>
                    <p class="count" data-bind="text: operationMessage">你们班已经有13个人在学 ></p>
                </div>
            </div>


        </a>
        <!-- /ko -->
    </div>
</div>

<div data-bind="visible: (appsInfo().length == 0) && ajaxLoadFinished" style="display: none;"><p style="line-height: 10; text-align: center; color: #aaa;">敬请期待！</p></div>
<!-- /ko -->

<!-- ko ifnot : success-->
<div data-bind="visible: !success()" style="display: none;"><p style="line-height: 10; text-align: center; color: #aaa;">数据请求失败！</p></div>
<!-- /ko -->

<script>
    var _fairylandMap = {
        page: 'old'
    };
</script>
</@layout.page>