<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='品牌馆'
pageJs=["remarkSearch"]
pageJsFile={"remarkSearch" : "public/script/mobile/mizar/remarkSearch"}
pageCssFile={"remarkIndex" : ["public/skin/mobile/mizar/css/remark"]}>

<div class="search-box">
    <input type="text" data-bind="textInput: searchContent" class="txt" maxlength="50" placeholder="搜索我要投票/点评的机构">
    <a href="javascript:void(0);" class="sure-btn" data-bind="click: searchBtn,visible: searchContent" style="display: none;">搜索</a>
    <a href="javascript:void(0);" class="sure-btn" style="color: #999;" data-bind="visible: searchContent() == ''" >搜索</a>
    <#--<a href="javascript:void(0);" class="sure-btn" data-bind="if: !isSelected,visible: !isSelected" style="display: none;">取消搜索</a>-->
</div>
<div class="agList-top" data-bind="visible: searchDetail" style="display: none;">
    <!-- ko foreach : {data : searchDetail, as : '_list'} -->
    <dl data-bind="click: $root.gotoDetailBtn">
        <dt><img src="" data-bind="attr:{src: _list.photo}" alt=""></dt>
        <dd class="agList-item">
            <div class="head"><!--ko text: _list.name--><!--/ko--></div>
            <div class="starBg">
                <!--ko foreach:ko.utils.range(1,5)-->
                <a href="javascript:void(0);" data-bind="css:{'cliBg' : $index()+1 <= _list.ratingStar}" ></a>
                <!--/ko-->
                <span><!--ko text: _list.ratingCount || 0--><!--/ko--> 条</span>
            </div>
            <div class="distance"><!--ko text: _list.tradeArea--><!--/ko--> <!--ko text: _list.distance.toFixed(2)--><!--/ko-->km</div>
            <div class="tip">

                <span><!--ko text: _list.secondCategory.join(',')--><!--/ko--></span>
            </div>
        </dd>
    </dl>
    <!--/ko-->
    <div data-bind="scroll: pageNum() < totalPage(), scrollOptions: { loadFunc: scrolled, offset: 50 }" style="text-align: center; width: 100%; color: #D8D8D8; position: absolute;">loading</div>
</div>

<div data-bind="visible: searchDetail().length == 0" style="display: none; text-align: center;color: #999;font-size: .8rem;">
    没有内容，请搜索
</div>

</@layout.page>
