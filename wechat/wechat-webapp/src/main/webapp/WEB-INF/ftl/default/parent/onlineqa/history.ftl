<#import "../layout.ftl" as comment>
<@comment.page title='提问历史' pageJs="onLineqaHistory">
    <@sugar.capsule css=['onlineqa'] />
    <div class="grade-list-wrap m-wrap" data-bind="template: {name : 'historyListBox'}">

    </div>

    <div data-bind="visible: !isLast() && isShowNull()" style="text-align: center;padding: 20px">
        <a data-bind="click: showMoreBtn" href="javascript:void(0);">查看更多</a>
    </div>
    <span data-bind="event: getProductType('${productType!'no'}')"></span>

    <script id="historyListBox" type="text/html">
        <span></span>
        <!-- ko if: $root.historyList().length > 0 -->
            <!-- ko foreach: {data : historyList, as : 'hks'} -->
                <div class="grade-list-main">
                    <div class="grade-list-info">
                        <div data-bind="click : $root.jumpUrl.bind($data,hks.url)" style="overflow: hidden; position: relative;">
                            <div class="grade-play-box"></div>
                            <img width="100%" src="" data-bind="attr: {src : hks.imageUrl}">
                        </div>
                        <div class="grade-list-des">
                            <div class="grade-list-des-l">
                                <p>科目：<span data-bind="text: hks.subjectCn"></span></p>
                                <p>时间：<span data-bind="text: hks.createDatetime"></span></p>
                            </div>

                            <div class="grade-list-des-m" data-bind="if:hks.comments.length > 0,visible: hks.comments.length > 0">
                                <p>打分</p>
                                <p>
                                    <!-- ko foreach: ko.utils.range(0,4) -->
                                    <!--ko if: ($index() < hks.comments[0].stars) -->
                                    <span class="star-yellow"></span>
                                    <!-- /ko -->
                                    <!--ko ifnot: $index() < hks.comments[0].stars -->
                                    <span></span>
                                    <!-- /ko -->
                                    <!-- /ko -->
                                </p>
                            </div>

                            <div class="grade-list-des-r">
                                <a data-bind="attr: {href : '/parent/onlineqa/commentlist.vpage?qid='+hks.id+'&pn='+$root.currentpage()}" class="evaluate-btn evaluate-btn-pos evaluate-btn-text" href="javascript:void(0)">点击评价</a>
                            </div>
                            <div style="clear: both;"></div>
                        </div>
                    </div>
                </div>
            <!-- /ko -->
        <!-- /ko -->

        <div data-bind="if: $root.historyList().length == 0 && $root.isShowNull, visible : $root.historyList().length == 0 && $root.isShowNull" style="padding: 200px 0 0; text-align: center; font-size: 40px;">
            暂无提问历史，<a href="/parent/onlineqa/ask.vpage">去提问</a>
        </div>
    </script>
    <script>
        var productType=${json_encode(productType)};
    </script>
</@comment.page>