<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='亲子活动'
pageJs=["list"]
pageJsFile={"list" : "public/script/mobile/mizar/familyactivity/list"}
pageCssFile={"familyActivity" : ["public/skin/mobile/mizar/css/familyActivity"]}
bodyClass="bg-grey">
<style>
    .flex-control-nav{ bottom: 722px;z-index: 2}
    .flex-control-paging li a.flex-active{background-color: #3cc588;}
    .flex-control-paging li a{background-color: #fff}
</style>


<div class="activity-header" data-bind="if: text().length != 0 && imagesDetail().length != 0,visible: text().length != 0 && imagesDetail().length != 0">
    <div class="actHd" id="bannerContentBox" data-bind="visible: imagesDetail" style="display: none;">
        <ul class="slides">
            <!-- ko foreach : {data : imagesDetail, as : '_img'} -->
            <li data-bind="click: $root.topImgBtn.bind($data,$index())">
                <img src="" data-bind="attr: {'src' : _img.img}">
            </li>
            <!--/ko-->

        </ul>
    </div>
    <!--ko if: text.length != 0-->
    <div class="avtHd-intro" data-bind="visible: text" style="display: none;">
        <!-- ko foreach : {data : text, as : '_t'} -->
        <span data-bind="click: $root.topImgBtn.bind($data,$index())">
            <!--ko text: _t.content--><!--/ko-->
        </span>
        <!--/ko-->
    </div>
    <!--/ko-->
</div>
<div class="activity-main" data-bind="visible: contentDetail" style="display: none;">
    <!-- ko foreach : {data : contentDetail, as : '_value'} -->
    <div class="actMn-list" data-bind="click: $root.actBtn">
        <div class="infoBox">
            <div class="pic"><img src="" data-bind="attr:{'src' : _value.cover || ''}"></div>
            <div class="intro">
                <div class="name"><!--ko text: _value.title--><!--/ko--></div>
                <!-- ko foreach : {data : _value.desc, as : 'desc'} -->
                <div class="text"><!--ko text: desc--><!--/ko--></div>
                <!--/ko-->
            </div>
        </div>
        <div class="labelBox">
            <div class="label">
                <!-- ko foreach : {data : _value.tags, as : '_tags'} -->
                <span class="blue"><!--ko text: _tags--><!--/ko--></span>
                <!--/ko-->
            </div>
            <#--<div class="count"><span class="number">2645</span></div>-->
        </div>
    </div>
    <!--/ko-->
</div>
<div data-bind="visible: pageNum() < totalPage()-1" class="weui-infinite-scroll" style="font-size: 0.5rem; color: #999; display: none;">
    <div class="infinite-preloader"></div>
    正在加载...
</div>
</@layout.page>