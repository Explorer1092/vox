<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-grayf0"
title="自学礼物"
pageJs=["lottery"]
pageCssFile={"lottery" : ["public/skin/mobile/student/app/wonderland/activity/css/skin"]}
pageJsFile={"lottery" : "public/script/mobile/student/wonderland/activity/lottery"}
>
<div class="gf-lottery-new">
    <div class="lotteryBox-new" data-bind="visible: awardsContent().length" style="display: none;">
        <!-- ko foreach : {data : awardsContent, as : '_award'} -->
        <div class="giftList" data-bind="click: giftBtn.bind($data,$element)"></div>
        <!--/ko-->
    </div>
    <div class="count">今日剩余<span><!--ko text: freeChance--><!--/ko--></span>次</div>
</div>

<div class="gt-snowmanTips">雪人有礼，点击喜欢的礼物盒吧</div>

<div class="gf-lottery" style="display: none;">
    <div class="lotteryBox" data-bind="visible: awardsContent().length" style="display: none;">
        <!-- ko foreach : {data : awardsContent, as : '_award'} -->
        <div class="lotteryList" data-bind="css: 'list-'+($index()+1), attr:{'data-id': _award.id}">
            <i class="l-icon " data-bind="css:{'l-icon02' : _award.category == 'Chuang', 'l-icon01' : _award.category == 'Li','l-icon03' : _award.category == 'Default'}"></i>
            <div class="txt" data-bind="css:{'txt02' : _award.category == 'Default'}">
                <p><!--ko text: _award.name--><!--/ko--></p>
            </div>
        </div>
        <!--/ko-->
    </div>
    <div class="lotteryBtn">
        <p class="tips">今日领取次数：<span><!--ko text: freeChance--><!--/ko--></span>/<span><!--ko text: totalFreeChance--><!--/ko--></span></p>
        <a data-bind="click: lotteryBtn, visible: awardsContent().length" href="javascript:void(0)" class="btn" style="display: none;"></a>
    </div>
</div>
<div class="gf-section">
    <div class="title">我的学习礼物</div>
    <ul data-bind="visible: bingoContent().length" style="display: none;">
        <!-- ko foreach : {data : bingoContent, as : '_bingo'} -->
        <li class="gf-list">
            <i class="icon" data-bind="css:{'icon02' : _bingo.category == 'Chuang', 'icon01' : _bingo.category == 'Li'}"></i>
            <span class="txt"><!--ko text: _bingo.name--><!--/ko--></span>
            <a data-bind="click: useBtn, visible: _bingo.status == 'AVAILABLE'" style="display: none;" href="javascript:void(0)" class="btn">立即使用</a>
            <a data-bind="visible: _bingo.status == 'USED'" style="display: none;" href="javascript:void(0)" class="btn success">已使用</a>
            <span class="label">今日有效</span>
        </li>
        <!--/ko-->
    </ul>
    <!--ko if: bingoContent().length == 0-->
    <div class="gf-empty"></div>
    <!--/ko-->
</div>
<div class="gf-section">
    <div class="title">同学领取动态</div>
    <ul data-bind="visible: latestContent().length > 0" style="display: none;">
        <!-- ko foreach : {data : latestContent, as : '_latest'} -->
        <li class="dyn-list">
            <img src="" data-bind="attr :{src : _latest.studentImg == '' ? '<@app.avatar href=''/>' : '<@app.avatar href='/'/>' + (_latest.studentImg)}" class="avatar">
            <div class="txt01">
                <p class="name"><!--ko text: _latest.studentName--><!--/ko--></p>
                <p class="grade"><!--ko text: _latest.clazzName--><!--/ko--></p>
            </div>
            <div class="txt03"><span><!--ko text: _latest.date--><!--/ko--></span></div>
            <div class="txt02"><!--ko text: _latest.awardName--><!--/ko--></div>
        </li>
        <!--/ko-->
        <li data-bind="visible: latestContent().length >= 30" style="text-align: center;color:#a0a0a0;font-size:.6rem;">只显示最新的30条哦</li>
    </ul>
    <!--ko if: latestContent().length == 0-->
    <div class="dyn-empty"></div>
    <!--/ko-->
</div>

<div data-bind="template : {name: koTemplateName()}"></div>

<script type="text/html" id="lotterySuccess_tem">
    <div class="gf-popup">
        <div class="gf-popupBox">
            <div class="hd"><span class="close" data-bind="click: koTemplateClose"></span></div>
            <div class="mn">
                <div class="info bg">
                    <div class="tips">
                        <div class="icon icon01"></div>
                        <div class="txt"><!--ko text: awardName--><!--/ko--></div>
                    </div>
                </div>
            </div>
            <div class="ft">
                <a href="javascript:void(0)" data-bind="click:usePopupBtn.bind(award())" class="btn">立即使用</a>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="tip_tem">
    <div class="gf-popup">
        <div class="gf-popupBox">
            <div class="hd"><span class="close" data-bind="click: koTemplateClose"></span></div>
            <div class="mn">
                <div class="info">
                    <div class="rules">
                        <h3 class="title">提示</h3>
                        <div class="text"><!--ko text: tipContent--><!--/ko--></div>
                    </div>
                </div>
            </div>
            <div class="ft">
                <a href="javascript:void(0)" data-bind="click: koTemplateClose" class="btn">好的</a>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="lotteryRule_tem">
    <div class="gf-popup">
        <div class="gf-popupBox">
            <div class="hd"><span class="close" data-bind="click: koTemplateClose"></span></div>
            <div class="mn">
                <div class="info">
                    <div class="rules">
                        <h3 class="title">规则说明</h3>
                        <p class="txt">1、所有用户每天可免费领取1次学习礼物</p>
                        <p class="txt">2、领取的学习礼物均当日使用有效，未使用的第二天会过期</p>
                        <p class="txt">3、活动时间：2016.12.08 至 2016.12.31</p>
                    </div>
                </div>
            </div>
            <div class="ft">
                <a href="javascript:void(0)" data-bind="click: koTemplateClose" class="btn">好的</a>
            </div>
        </div>
    </div>
</script>

</@layout.page>