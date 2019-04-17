<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='值得买'
pageJs=["ecsg"]
pageJsFile={"ecsg" : "public/script/mobile/groupon/ecsg"}
pageCssFile={"ecsg" : ["public/skin/mobile/groupon/css/ecsg"]}>

<div class="ec-mask" data-bind="visible: $root.showOtherListBox()" style="display: none"></div><!--展示其它选项是让其显示-->
<div class="headerFixed">
    <div class="headerInner">
        <div class="ec-tab">
            <div class="tabBox">
                <div class="tabRight" data-bind="click: otherBtn, css:{'show': showOtherListBox()}">
                    <span class="tabItem other">其他</span>
                </div>
                <div class="tabLeft" id="categoryListMenuBox">
                    <div class="tabScroll" data-bind="style:{'width':menuList().length*3+'rem'}"><!--3rem*span的个数-->
                        <!-- ko foreach : {data : menuList, as : '_list'} -->
                        <span class="tabItem" data-bind="css:{'active': _list.checked()}, click: $root.categoryBtn,attr:{'data-index':$index()}">
                            <!--ko text: _list.categoryName()--><!--/ko-->
                        </span>
                        <!--/ko-->
                    </div>
                </div>
            </div>
            <div class="otherBox" data-bind="visible: showOtherListBox()" style="display: none;">
                <!-- ko foreach : {data : menuList, as : '_list'} -->
                <span class="otherItem" data-bind="css:{'active': _list.checked()}, click: $root.categoryBtn">
                    <!--ko text: _list.categoryName()--><!--/ko-->
                </span>
                <!--/ko-->
            </div>
        </div>
        <div class="ec-header">
            <ul>
                <!-- ko foreach : {data : childMenuList, as : '_cl'} -->
                <li data-bind="css:{'active': _cl.checked(),'sort':_cl.rank() == 1},click: $root.childMenuBtn">
                    <!--ko text: _cl.name()--><!--/ko-->
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
</div>
<div class="ec-main" data-bind="visible: contentDetail" style="display: none;">
    <!-- ko foreach : {data : contentDetail, as : '_list'} -->
    <div class="ec-list" data-bind="click: $root.goToBuyBtn">
        <div class="pic"><img src="" data-bind="attr: {'src': _list.image}"></div>
        <div class="info">
            <div class="header"><!--ko text: _list.shortTitle || _list.title--><!--/ko--></div>
            <div class="main">
                <div class="price">￥
                    <span class="number"><!--ko text: _list.price--><!--/ko--></span>
                    <del>￥<!--ko text: _list.originalPrice--><!--/ko--></del>
                </div>
                <div class="sales"><span>销量 <!--ko text: _list.saleCount || 0--><!--/ko--></span></div>
            </div>
            <div class="footer">
                <div class="label">
                    <span class="label-t" data-bind="visible: _list.goodsSource == 'tian_mao'" style="display: none;"></span>
                    <span class="label-b" data-bind="visible: _list.isPostFree" style="display: none;">包邮</span>
                </div>
                <div class="btn">
                    <span data-bind="timer: $root.countDown" style="display: none;">
                        <!--ko text: ((_list.endTime - _list.currentTime)/1000).toFixed(0) --><!--/ko-->
                    </span>
                    <span class="btn-buy">去抢购</span>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->

    <div data-bind="visible: contentDetail().length == 0" style="display: none; text-align: center; font-size: .5rem; padding: 2rem; color: #D8D8D8;" >暂无该产品，请重新筛选</div>
    <#--分页-->
    <div data-bind="scroll: pageNum() < totalPage()-1, scrollOptions: { loadFunc: scrolled, offset: 100 }" style="text-align: center; width: 100%; height: 2rem; color: #D8D8D8; font-size: .8rem;">加载中...</div>
</div>

<script>
    var _categoryList = ${(json_encode(categoryList))![]};
</script>
</@layout.page>