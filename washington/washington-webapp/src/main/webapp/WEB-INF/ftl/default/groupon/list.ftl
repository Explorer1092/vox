<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='值得买'
bodyClass='bgB8'
pageJs=["index"]
pageJsFile={"index" : "public/script/mobile/groupon/index"}
pageCssFile={"index" : ["public/skin/mobile/groupon/css/index"]}>
<style>
    .flex-control-nav{ bottom: 0;z-index: 2}
    .flex-control-paging li a.flex-active{background-color: #3cc588;}
    .flex-control-paging li a{background-color: #fff}
</style>
<#include "../mizar/function.ftl"/>
<div class="loveReading-box">
    <div class="tabPoFix">
        <div class="lor-tab" id="categoryListMenuBox">
            <ul data-bind="style:{'width':menuList().length*4+'rem'}">
                <!-- ko foreach : {data : menuList, as : '_list'} -->
                <li data-bind="css:{'active': _list.checked()}, click: $root.categoryBtn,attr:{'data-index':$index()}">
                    <a href="javascript:void(0);"><!--ko text: _list.categoryName()--><!--/ko--></a>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>

    <#if bannerSpecialTopicList?? && bannerSpecialTopicList?size gt 0>
        <div class="lor-banner" id="bannerContentBox" data-bind="visible: selectedCategoryCode() == ''" style="display: none;">
            <ul class="bannerImg slides">
                <#list bannerSpecialTopicList as banner>
                    <li>
                        <a href="${(banner.url)!''}">
                            <img src="${pressImageAutoW((banner.coverImg)!,640)}" class="pic">
                        </a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
    <div class="lor-content" data-bind="visible: selectedCategoryCode() == ''" style="display: none;">
        <#if rightBottomSpecialTopicList?? && rightBottomSpecialTopicList?size gt 0>
            <#if leftSpecialTopicList?? && leftSpecialTopicList?size gt 0>
                <div class="con-side">
                    <a href="${(leftSpecialTopicList[0].url)!}">
                        <img src="${pressImageAutoW((leftSpecialTopicList[0].coverImg)!,330)}" alt="" class="photo">
                    </a>
                </div>
            </#if>

            <#if rightTopSpecialTopicList?? && rightTopSpecialTopicList?size gt 0>
                <div class="con-side s-right">
                    <a href="${(rightTopSpecialTopicList[0].url)!}">
                        <img src="${pressImageAutoW((rightTopSpecialTopicList[0].coverImg)!,380)}" alt="" class="photo">
                    </a>

                </div>
            </#if>
            <#if rightBottomSpecialTopicList?? && rightBottomSpecialTopicList?size gt 0>
                <div class="con-side s-right">
                    <a href="${(rightBottomSpecialTopicList[0].url)!}">
                        <img src="${pressImageAutoW((rightBottomSpecialTopicList[0].coverImg)!,380)}" alt="" class="photo">
                    </a>
                </div>
            </#if>
        </#if>
    </div>
    <div data-bind="visible: contentDetail" style="display: none;">
        <div class="lor-title">
            精品推荐 <i class="titleIco"></i>
        </div>
        <div class="lor-main">
            <!-- ko foreach : {data : contentDetail, as : '_list'} -->
            <dl class="lor-list" data-bind="click: $root.gotoBookDetail">
                <dt class="lor-pic"><img src="" data-bind="attr: {'src': _list.image}" alt=""></dt>
                <dd class="lor-column">
                    <div class="bookTitle"><!--ko text: _list.shortTitle || _list.title--><!--/ko--></div>
                    <div class="price">
                        <!--ko if: _list.price-->
                        ¥
                        <!--/ko-->
                        <!--ko text: _list.price--><!--/ko-->
                        <!--ko if: _list.specialTag.length > 0-->
                        <!-- ko foreach : {data : _list.specialTag, as : '_tag'} -->
                        <!--ko if: $root.specialTag[_tag]-->
                        <span class="bookTag" data-bind="attr:{'class':'bookTag '+$root.specialTag[_tag].color}"><!--ko text: $root.specialTag[_tag].name--><!--/ko--></span>
                        <!--/ko-->
                        <!--/ko-->
                        <!--/ko-->
                    </div>
                    <div class="website"><!--ko text: $root.goodsSource[_list.goodsSource].name--><!--/ko--></div>
                    <div class="time"><!--ko text: _list.deployDay--><!--/ko--></div>
                </dd>
            </dl>
            <!--/ko-->
            <div data-bind="visible: contentDetail().length == 0 && ajaxLoadFinished" style="display: none; text-align: center; font-size: .8rem; padding: 2rem; color: #D8D8D8;" >暂无该产品，请重新筛选</div>
            <#--分页-->
            <#--<div data-bind="scroll: pageNum() < totalPage()-1, scrollOptions: { loadFunc: scrolled, offset: 150 }">加载中...</div>-->
            <div data-bind="visible: pageNum() < totalPage()-1" class="weui-infinite-scroll" style="font-size: 0.5rem; color: #999;">
                <div class="infinite-preloader"></div>
                正在加载...
            </div>
        </div>
    </div>
</div>

<script>
    var _categoryList = ${(json_encode(categoryList))![]};
</script>
</@layout.page>