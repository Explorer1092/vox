<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='机构介绍'
pageJs=["remarkDetail"]
pageJsFile={"remarkDetail" : "public/script/mobile/mizar/remarkDetail"}
pageCssFile={"remarkIndex" : ["public/skin/mobile/mizar/css/remark"]}>


<div class="agency-top">
    <div class="acy-image">
        <img src="${(shopMap.shopImg)!}" alt="">
    </div>
    <div class="acy-title">${(shopMap.shopName)!}</div>
    <div class="acy-info">${(shopMap.likeCount)!0}人已投票，当前排名${(shopMap.rank)!0}</div>
    <a href="javascript:void(0);" class="w-orderedBtn w-btnWid" data-bind="if: !liked(),visible:!liked(), click: voteBtn" style="display: none;">参与投票</a>
    <a href="javascript:void(0);" class="w-orderedBtn w-btnWid w-btn-disable" data-bind="if: liked(), visible:liked()" style="display: none">已投票</a>
    <div class="acy-tips">为自己支持的机构投票+10学豆</div>
</div>
<div class="comment-title">
    <div class="comment-right">
        优质点评+100学豆
        <a href="javascript:void (0);" data-bind="click: remarkBtn" class="w-btn-small">写点评</a>
    </div>
    家长点评
</div>
<div class="aeg-top topDif">
    <div class="aeg-column" data-bind="visible: remarkList" style="display: none">
        <!-- ko foreach : {data : remarkList, as : '_list'} -->
        <dl class="aeg-comment">
            <dt><img src="" data-bind="attr: {'src': _list.avatar}" alt=""></dt>
            <dd>
                <div class="title">
                    <!--ko text: _list.userName--><!--/ko-->
                    <div class="right"><!--ko text: _list.ratingDate--><!--/ko--></div>
                </div>
                <div class="starBg">
                    <!--ko foreach:ko.utils.range(1,5)-->
                    <a href="javascript:void(0);" data-bind="css:{'cliBg' : $index()+1 <= _list.rating}" ></a>
                    <!--/ko-->

                </div>
                <div class="pro">
                    <p><!--ko text: _list.content--><!--/ko--></p>
                    <#--<a href="javascript:void(0);" class="text">全文</a>-->
                </div>
                <div class="commentImg" data-bind="visible; _list.photos">
                    <!-- ko foreach : {data : _list.photos, as : '_photo'} -->
                    <div class="image">
                        <img src="" data-bind="attr: {'src' : _photo}" alt="">
                    </div>
                    <!--/ko-->
                </div>
                <#--<div class="icon-praise praise-1"><span class="num">134</span></div>-->
            </dd>
        </dl>
        <!--/ko-->
    </div>
    <#--分页-->
    <div data-bind="scroll: pageNum() < totalPage(), scrollOptions: { loadFunc: scrolled, offset: 50 }" style="text-align: center; width: 100%; color: #D8D8D8; position: absolute;">loading</div>
</div>
<script>
    var remarkDetailMap = {
        activityId: '${activityId!}',
        liked: '${(liked!false)?string}',
        avatar: '<@app.avatar href=""></@app.avatar>',
        shopName: '${(shopMap.shopName)!}'
    };
</script>

</@layout.page>