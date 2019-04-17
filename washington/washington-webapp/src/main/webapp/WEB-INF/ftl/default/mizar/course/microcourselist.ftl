<#import "../../layout/webview.layout.ftl" as layout/>
<#if category?has_content && category == 'MICRO_COURSE_NORMAL'>
    <#assign title = "长期课"/>
<#else>
    <#assign title = "微课堂"/>
</#if>
<@layout.page
title=title
pageJs=["init"]
bodyClass="bg-grey"
pageJsFile={"init" : "public/script/mobile/course/main"}
pageCssFile={"css" : ["public/skin/mobile/course/css/microcourse"]}
>
<#--长期课无顶部-->
    <#if category?has_content && category == 'MICRO_COURSE_NORMAL'><#else>
    <div class="liveL-header02" id="noTopBar" style="display: none;">
        <div class="liveL-fixHd">
            <a data-bind="click: myCourseBtn" href="javascript:void(0)" class="liveL-column">我的课程</a>
        </div>
    </div>
    <div class="liveL-header01" id="hasTopBar" style="display: none;">
        <div class="liveL-fixHd">
            <div class="name">微课堂</div>
            <a data-bind="click: myCourseBtn" href="javascript:void(0)" class="label">我的课程</a>
        </div>
    </div>
    </#if>
<div class="liveL-tab">
<#--<div class="liveL-fixTab noFixed">-->
    <div class="liveL-fixTab <#if category?has_content && category == 'MICRO_COURSE_NORMAL'>liveL-fixTab-top</#if>" id="tabList">
        <ul>
            <#list ["推荐","语文","数学","英语","才艺","家庭教育"] as tag>
                <li <#if tag_index == 0> class="active" </#if> data-bind="click:tagChange" data-index="${tag_index}"> ${tag!''}</li>
            </#list>
        </ul>
    </div>
</div>
    <#if category?has_content && category == 'MICRO_COURSE_OPENING'>
    <div class="liveL-banner" id="bannerContentBox" data-bind="visible: hasTopBanner" style="position: relative;display: none;">
        <ul class="slides">
            <!-- ko foreach : {data : imagesDetail, as : '_img'} -->
            <li data-bind="click: $root.topImgBtn.bind($data,$index())" style="cursor: pointer;">
                <img src="" data-bind="attr: {'src' : _img.img}">
            </li>
            <!--/ko-->
        </ul>
    </div>
    <div class="liveL-adv" id="normalBannerBox" data-bind="visible: hasNormalBanner" style="display: none;">
        <!-- ko foreach : {data : nomalimagesDetail, as : '_img'} -->
        <li data-bind="click: $root.normalBtn" style="cursor: pointer;">
            <img src="" data-bind="attr: {'src' : _img.img}">
        </li>
        <span class="label" data-bind="text:_img.btnContent"></span>
        <!--/ko-->
    </div>
    </#if>
<div data-bind="template: {name: templateBox(), data: database()}"></div>

<script type="text/html" id="T:loading">
    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;">
        数据加载中...
    </div>
</script>

<script type="text/html" id="T:nullContent">
    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;">
        <span data-bind="html: (info ? info : '暂时还没有课程哦!')"></span>
    </div>
</script>


<script type="text/html" id="T:listContent">
    <div>
        <div class="liveL-main">
        <#--<div class="liveL-type"><i class="icon iconVideo"></i><span>今日直播</span></div>-->
            <div data-bind="foreach:rows">
                <a class="liveL-list js-courseItem" href="javascript:void(0);" data-bind="attr:{'data-link':redirectUrl}">
                    <div class="l-title" data-bind="text:title"></div>
                    <!-- ko if:background -->
                    <div class="l-pic"><img src="#" data-bind="attr:{src:background}"></div>
                    <!-- /ko -->
                    <div class="l-text l-text01">
                        <div class="fl"><i class="icon-clock"></i><span class="time" data-bind="text:classTime"></span></div>
                        <!-- ko if: hasTag -->
                        <div class="fr" data-bind="foreach:tags">
                            <span class="l-label" data-bind="text:$data,attr:{class:$parent.tagClass}"></span>
                        </div>
                        <!-- /ko -->
                    </div>
                    <div class="l-text l-text02">
                        <div class="fl">
                            <span class="avatar">
                                <!-- ko if: speakerAvatar -->
                                <img src="#" data-bind="attr:{src:speakerAvatar}">
                                <!-- /ko -->
                                <!-- ko ifnot: speakerAvatar -->
                                <img data-bind="attr :{src : '<@app.avatar href=''/>'}"/>
                                <!-- /ko -->
                                <i class="a-mask"></i>
                            </span>
                            <span class="name" data-bind="text:keynoteSpeaker"></span>
                        </div>
                        <div class="fr">
                            <!-- ko if: soldOut-->
                            <p class="price txtGreen">正在直播</p>
                            <!-- /ko -->
                            <!-- ko ifnot: soldOut-->
                            <!-- ko if:(price && price != 0) -->
                            <p class="price" data-bind="text:'￥'+price"></p>
                            <!-- /ko -->
                            <!-- ko if:(price && price == 0) -->
                            <p class="price" data-bind="text:'限时免费'"></p>
                            <!-- /ko -->
                            <!-- /ko -->
                            <p class="num" data-bind="text:readCount+'人已查看'"></p>
                        </div>
                    </div>
                </a>
            </div>
        </div>
    </div>

    <div style="line-height: 150%; padding: 50px 0 ; text-align: center; color: #bbb; font-size: .625rem;" id="noContent">
        <span data-bind="html: (info ? info : '暂时还没有课程哦')"></span>
    </div>
    <!-- ko if: $root.isShowLoading() -->
    <div class="weui-infinite-scroll" style="font-size: 0.5rem; color: #999;">
        <div class="infinite-preloader"></div>
        正在加载...
    </div>
    <!-- /ko -->
</script>
<script type="text/javascript">
    var initMode = "MicroCourseMode";
    var pageCategory = "${category!'MICRO_COURSE_OPENING'}";
    var wechatHostHeader = function () {
        var hostHeader = "";
        <#if ProductDevelopment.isDevEnv()>
            hostHeader = '10.200.27.149:8180';
        <#elseif ProductDevelopment.isTestEnv()>
            hostHeader = "wechat.test.17zuoye.net";
        <#elseif ProductDevelopment.isStagingEnv()>
            hostHeader = "wechat.staging.17zuoye.net";
        <#elseif ProductDevelopment.isProductionEnv()>
            hostHeader = "wechat.17zuoye.com";
        </#if>
        return hostHeader;
    };

    //检测来自微信内置的浏览器
    var isFromWeChat = function () {
        return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1);
    };
    var logged = ${logged?c},available = false;
    if(!logged && isFromWeChat()){
        location.href = location.protocol + '//' + wechatHostHeader() + '/signup/parent/login.vpage?ref=microCourse';
    }
</script>
<script type="text/javascript" src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<style>
    /*重置轮播图样式*/
    .flex-control-nav {bottom: 0.5rem;z-index: 2;line-height: 0;}
    .flex-control-paging li a{width: 0.3rem; height: 0.3rem; border-radius: 0.3rem; background-color: #9e9d9d;}
    .flex-control-paging li a.flex-active{ background-color: #3cc588;}
</style>
</@layout.page>