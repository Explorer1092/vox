<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title='口碑机构'
pageJs=["remarkIndex"]
pageJsFile={"remarkIndex" : "public/script/mobile/mizar/remarkIndex"}
pageCssFile={"remarkIndex" : ["public/skin/mobile/mizar/css/remark"]}>

<div class="agencyDetails-box">
    <div class="vote-top">
        <img src="<@app.link href='public/skin/mobile/mizar/images/brand-banner.png'/>">
    </div>
    <div class="vote-side">
        <div class="vote-text">
            <input type="text" onclick="window.location.href='/mizar/remark/search.vpage?_from=top'" class="txt" placeholder="搜索我要投票/点评的机构">
        </div>
        <div class="vote-info">
            <p>投票选出口碑机构：参与投票+10学豆</p>
            <p>点评教育机构：优质点评+100学豆</p>
        </div>
    </div>
    <div data-bind="visible: listDetail" style="display: none;">
        <!-- ko foreach : {data : listDetail, as : '_detail'} -->
            <div class="aeg-top topDif" data-bind="click: $root.gotoDetailBtn">
                <dl class="topPad">
                    <dt><img src="" data-bind="attr: {'src': _detail.shopLogo}" alt="logo"></dt>
                    <dd>
                        <div class="head"><!--ko text: _detail.shopName--><!--/ko--></div>
                        <div class="starBg">
                            <!--ko foreach:ko.utils.range(1,5)-->
                            <a href="javascript:void(0);" data-bind="css:{'cliBg' : $index()+1 <= _detail.shopStar}" ></a>
                            <!--/ko-->
                        </div>
                    </dd>
                </dl>
                <div class="aeg-column">
                    <dl class="aeg-comment">
                        <dt><img src="" data-bind="attr: {'src' : _detail.userAvatar }" alt=""></dt>
                        <dd>
                            <div class="title">
                                <!--ko text: _detail.userName--><!--/ko-->
                                <div class="right"><!--ko text: _detail.ratingTime--><!--/ko--></div>
                            </div>
                            <div class="starBg">
                                <!--ko foreach:ko.utils.range(1,5)-->
                                <a href="javascript:void(0);" data-bind="css:{'cliBg' : $index()+1 <= _detail.ratingStar}" ></a>
                                <!--/ko-->
                            </div>
                            <div class="pro">
                                <p><!--ko text: _detail.ratingContent--><!--/ko--></p>
                            <#--<a href="javascript:void(0);" class="text">全文</a>-->
                            </div>
                            <div class="commentImg">
                                <!-- ko foreach : {data : _detail.photo, as : '_photo'} -->
                                <div class="image">
                                    <img src="" data-bind="attr:{'src': _photo}" alt="">
                                </div>
                                <!--/ko-->
                            </div>
                            <#--<div class="icon-praise"><span class="num">134</span></div>-->
                        </dd>
                    </dl>
                </div>
            </div>
        <!--/ko-->
    </div>
    <div data-bind="scroll: pageNum() < totalPage(), scrollOptions: { loadFunc: scrolled, offset: 50 }" style="text-align: center; width: 100%; color: #D8D8D8; position: absolute;">loading</div>
    <div class="footer footerHei">
        <div class="inner">
            <a href="/mizar/remark/mechanismrank.vpage" class="w-orderedBtn w-btn-green w-btnWid">查看排名</a>
            <a href="/mizar/remark/search.vpage?_from=bottom" class="w-orderedBtn w-btnWid">参与投票</a>
        </div>
    </div>
</div>
<script>
    var remarkMap = {
        time : '${time!}',
        avatar: '<@app.avatar href=""></@app.avatar>'
    };
</script>
</@layout.page>