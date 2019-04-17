<div class="tabbox js-commentDiv" style="display:none">
    <div class="agencyDetails-box">
        <!--ko if: remarkList().length != 0-->
        <div class="aeg-column" style="display: none;" data-bind="visible:  remarkList">
            <!-- ko foreach : {data : remarkList, as : '_list'} -->
            <dl class="aeg-comment">
                <dt>
                    <img width="100%" src="" data-bind="attr: {src: _list.avatar}">
                </dt>
                <dd>
                    <div class="title">
                        <!--ko text: _list.userName--><!--/ko-->
                        <div class="right">
                            <!--ko text: _list.ratingDate--><!--/ko-->
                        </div>
                    </div>
                    <div class="right"></div>
                    <div class="starBg">
                        <!--ko foreach:ko.utils.range(1,5)-->
                        <a href="javascript:void(0);" data-bind="css:{'cliBg' : $index()+1 <= _list.rating}" ></a>
                        <!--/ko-->
                    </div>
                    <div class="pro pro-show">
                        <p>
                            <!--ko text: _list.content--><!--/ko-->
                        </p>
                    </div>

                    <!--ko if: _list.photos-->
                    <div class="commentImg">
                        <!-- ko foreach : {data : _list.photos, as : '_photo'} -->
                        <div class="image">
                            <a href="javascript:void (0);" data-bind="attr: {href : _photo}" ><img src="" data-bind="attr: {src: _photo}" width="100%"></a>
                        </div>
                        <!--/ko-->
                    </div>
                    <!--/ko-->
                </dd>
            </dl>
            <!--/ko-->
        </div>
        <div data-bind="scroll: pageNum() < totalPage(), scrollOptions: { loadFunc: scrolled, offset: 50 }" style="text-align: center; width: 100%; color: #D8D8D8; position: absolute;">loading</div>
        <!--/ko-->
        <!--ko if: remarkList().length == 0-->
            <div class="linshi" style="width:100%;height: 10.7rem;background:#fff; margin:0 auto ;text-align:center;padding:5rem 0;font-size:0.75rem">
                暂无点评，快来写一条吧。
            </div>
        <!--/ko-->
        <div class="footer">
            <div class="inner">
                <a href="javascript:void(0);" class="w-commentBtn"><i class="c-icon"></i>写点评</a>
            </div>
        </div>
    </div>

    <script>
        var mizarMap = {
            avatar: '<@app.avatar href=""></@app.avatar>'
        };
    </script>
    <script type="text/javascript">
        var baseData = {
            shopId: "${(goods.shopId)!}${(shop.shopId)!}",
            goodsId : "${(goods.id)!}",
            shopName: "${(shop.name)!}",
            activityId: "${activityId!}",
            isVip: ${((shop.isVip)!true)?string}
        };
    </script>
</div>

