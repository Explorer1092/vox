<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="自学乐园"
pageJs=["fairyland"]
pageJsFile={"fairyland" : "public/script/mobile/student/fairyland"}
pageCssFile={"fairyland" : ["public/skin/studentmobile/css/fairyland"]}
>
<div class="clearfix"></div>

<!-- ko if : isShowBanner -->
<div class="fairylandBanner" data-bind="foreach: headerBanner().data">
    <a href="javascript:;" data-bind="click: $parent.clickOpenBanner">
        <img src="" height="100%" data-bind="attr: {src : $parent.headerBanner().imgDoMain + 'gridfs/' + img}">
    </a>
</div>
<!-- /ko -->

<!-- ko if : success-->
<div class="fairylandTips" data-bind="visible: (appUserMessage().length > 0 && isMessageShow), foreach : appUserMessage" style="display: none;">
    <p class="text" data-bind="text: title, click : $parent.clickOpenGame " style="cursor: pointer;">-----</p>
</div>

<div class="fairylandBox" data-bind="visible: (appsInfo().length > 0), foreach : appsInfo" style="display: none;">
    <!-- ko ifnot: appKey == 'BookListen'-->
    <a href="javascript:void(0)" class="fairyland-list" data-bind="click : $parent.clickOpenGame, css :{ hot : appStatus == 0}">
        <div class="list-item">
            <div class="img">
                <img data-bind="attr :{src : '<@app.avatar href='/'/>' + productIcon}"/>
            </div>
            <div class="info">
                <p class="title">
                    <span data-bind="text : productName"></span><span class="label-hot" data-bind="visible : hotFlag">热门</span><span class="label-hot" data-bind="visible : newFlag">新</span><span class="label-hot" data-bind="visible : recommendFlag">推荐</span>
                </p>
                <p class="describe" data-bind="text : productDesc"></p>
                <span class="state state-yellow" data-bind="visible: (appStatus == 0)">未开通</span>
                <span class="state state-yellow" data-bind="visible: (appStatus == 1)">已过期</span>
                <span class="state" data-bind="visible: (appStatus == 2)">已开通</span>
            </div>
        </div>

        <div class="list-user">
            <div class="flex" data-bind="foreach : studentList">
                <!-- ko if: studentImg-->
                <span><img data-bind="attr :{src : '<@app.avatar href='/'/>' + studentImg}"/></span>
                <!-- /ko -->
                <!-- ko ifnot: studentImg-->
                <span><img data-bind="attr :{src : '<@app.avatar href=''/>'}"/></span>
                <!-- /ko -->
            </div>
            <span>等<i data-bind="text : usingUserNum"><#--count--></i>人已开通</span>
        </div>

        <div class="list-tips" data-bind="visible : operationMessage"><!--内部消息提示-->
            <i class="horn-icon"></i>
            <span data-bind="text: operationMessage"></span>
        </div>
    </a>
    <!-- /ko -->

    <!-- ko if: appKey == 'BookListen'-->
    <a href="javascript:void(0)" class="fairyland-list" data-bind="click : $parent.clickOpenGame">
        <div class="list-item">
            <div class="img">
                <img data-bind="attr :{src : '<@app.avatar href='/'/>' + productIcon}"/>
            </div>
            <div class="info">
                <p class="title">
                    <span data-bind="text : productName"></span><span class="label-hot" data-bind="visible : hotFlag">热门</span><span class="label-hot" data-bind="visible : newFlag">新</span>
                </p>
                <p class="describe" data-bind="text : productDesc"></p>
            </div>
        </div>

        <div class="list-tips" data-bind="visible : operationMessage"><!--内部消息提示-->
            <i class="horn-icon"></i>
            <span data-bind="text: operationMessage"></span>
        </div>
    </a>
    <!-- /ko -->
</div>

<div data-bind="visible: (appsInfo().length == 0) && ajaxLoadFinished" style="display: none;"><p style="line-height: 10; text-align: center; color: #aaa;">敬请期待！</p></div>
<!-- /ko -->

<!-- ko ifnot : success-->
<div data-bind="visible: !success()" style="display: none;"><p style="line-height: 10; text-align: center; color: #aaa;">数据请求失败！</p></div>
<!-- /ko -->
</@layout.page>