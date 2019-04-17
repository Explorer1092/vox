<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='微课堂'
pageJs=["lessonlist"]
pageJsFile={"lessonlist" : "public/script/mobile/course/live/lessonlist"}
pageCssFile={"live" : ["public/skin/mobile/course/css/live"]}
bodyClass="bg-grey">
<div data-bind="if: showTopBanner, visible: showTopBanner" class="liveL-header" style="display: none;">
    <div class="liveL-fixHd" <#--style="background: -webkit-linear-gradient(left,#24d08c,#53cdbd) #3bcea4;"-->>
        <div class="name" style="color: #484848;">微课堂</div>
        <a data-bind="click: myCourseBtn" href="javascript:void (0);" class="label" style="color: #484848;">我的课程</a>
    </div>
</div>

<a data-bind="click: myCourseBtn, if: !showTopBanner(), visible: !showTopBanner()" href="javascript:void(0)" class="liveL-column" style="display: none;">
    我的课程
</a>

<!--ko if: menuList().length > 0-->
<#--fix 暂时不显示-->
<div class="liveL-tab" style="display: none;">
    <div class="liveL-fixTab noFixed">
        <ul>
            <!-- ko foreach : {data : menuList, as : '_cl'} -->
            <li data-bind="css:{'active': _cl.checked},click: $root.categoryBtn"><!--ko text: _cl.categoryName--><!--/ko--></li>
            <!--/ko-->
        </ul>
    </div>
</div>
<!--/ko-->

<!--ko if: imagesDetail().length > 0-->
<div class="liveL-banner">
    <!-- ko foreach : {data : imagesDetail, as : '_id'} -->
    <img src="" data-bind="attr:{'src' : _id.img}, click: $root.topBannerBtn">
    <!--/ko-->
</div>
<!--/ko-->

<div class="liveL-main" data-bind="visible: contentDetail" style="display: none;">
    <!-- ko foreach : {data : contentDetail, as : '_cd'} -->
    <a class="liveL-list" data-bind="click: $root.redirectUrlBtn" href="javascript:void(0)">
        <div class="l-title">
            <!-- ko foreach : {data : _cd.tags, as : '_cdt'} -->
            <span class="l-label" data-bind="css: _cdt.color"><!--ko text: _cdt.name--><!--/ko--></span>
            <!--/ko-->
            <!--ko text: _cd.title--><!--/ko-->
        </div>

        <!--ko if: _cd.background-->
        <div class="l-pic">
            <img src="" data-bind="attr:{src: _cd.background}">
        </div>
        <!--/ko-->

        <div class="l-text l-text01">
            <!--ko if: _cd.classTime-->
            <div class="fl">
                <i class="icon-clock"></i>
                <span class="time"><!--ko text: _cd.classTime--><!--/ko--></span>
            </div>
            <!--/ko-->

            <!--ko if:  _cd.readCount > 0-->
            <div class="fr"><!--ko text: _cd.readCount--><!--/ko--> 人已查看</div>
            <!--/ko-->
        </div>
        <div class="l-text l-text02">
            <div class="fl">
                <!--ko if: _cd.speakerAvatar-->
                <span class="avatar">
                    <img src="" data-bind="attr:{src: _cd.speakerAvatar}">
                </span>
                <!--/ko-->

                <!--ko if: _cd.keynoteSpeaker-->
                <span class="name"><!--ko text: _cd.keynoteSpeaker--><!--/ko--></span>
                <!--/ko-->
            </div>
            <div class="fr">
                <span class="price"><!--ko text: _cd.price--><!--/ko--></span>
            </div>
        </div>
    </a>
    <!--/ko-->
</div>

<div data-bind="visible: contentDetail().length == 0 && ajaxLoadFinished" style="display: none; text-align: center; color: #ccc;">暂无课程列表</div>

<div data-bind="visible: pageNum() < totalPage()-1" class="weui-infinite-scroll" style="font-size: 0.5rem; color: #999; display: none;">
    <div class="infinite-preloader"></div>
    正在加载...
</div>
</@layout.page>