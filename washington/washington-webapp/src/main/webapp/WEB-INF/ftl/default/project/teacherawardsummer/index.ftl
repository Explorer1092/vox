<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="假期作业抽奖"
pageJs=["teacheraward"]
pageJsFile={"teacheraward" : "public/script/project/teacherawardsummer"}
pageCssFile={"teacheraward" : ["public/skin/project/teacherawardsummer/css/skin"]}>

<#include "../../layout/project.header.ftl">
<!--抽奖头图-->
<div class="lotteryBanner"></div>
<!--抽奖内容-->
<div class="lotteryMain">
    <div class="section section01">
        <h1 class="titleTag"><i class="tagIcon01"></i>今天布置 每天<span>3</span>次抽奖机会<i class="tagIcon02"></i></h1>
        <a href="javascript:void(0)" class="assignBtn" data-bind="css: {disabled: isAssign}, click: toAssign.bind($data)"></a>
        <p class="assignTips">7月1日后学生可自由开启假期作业</p>
        <div class="lotteryState">
            <h3 class="titleTag" data-bind="html: pageTipTitle">早布置福利</h3>
            <p class="assignTips" data-bind="html: pageTipContent"></p>
            <div class="prize-img"></div>
        </div>
    </div>
    <div class="section section02" >
        <!--转盘-->
        <div class="lotteryBox">
            <ul id="lottery">
                <li class="l-num01" data-type="1">
                    <div class="reward reward01"></div>
                </li>
                <li class="l-num02" data-type="4">
                    <div class="reward reward02"></div>
                </li>
                <li class="l-num03" data-type="3">
                    <div class="reward reward03"></div>
                </li>
                <li class="l-num04" data-type="6">
                    <div class="reward reward06"></div>
                </li>
                <li class="l-num05" data-type="2">
                    <div class="reward reward04"></div>
                </li>
                <li class="l-num06" data-type="5">
                    <div class="reward reward05"></div>
                </li>
                <li class="l-num07" data-type="7">
                    <div class="reward reward04"></div>
                </li>
                <li class="l-num08" data-type="8">
                    <div class="reward reward03"></div>
                </li>
            </ul>
            <a href="javascript:void(0)" class="drawBtn" id="lotterySubmit">
                <p>剩余<span data-bind="text: numTime">0</span>次</p>
            </a>
        </div>
    </div>
    <div class="section section03" style="position: relative;">
        <!--大奖动态-->
        <h1 class="titleTag"></h1>
        <p class="titleState diff">次日凌晨更新</p>
        <!-- ko if: bigAwardList().length > 0 -->
        <div class="dynamicBox" style="display: none;" data-bind="visible: bigAwardList().length > 0">
            <!-- ko foreach: bigAwardList-->
            <div class="recordList">
                <span class="cell cell01" data-bind="text: date, attr: {title: date}"></span>
                <span class="cell cell02" data-bind="text: time, attr: {title: time}"></span>
                <span class="cell cell03" data-bind="text: userName, attr: {title: userName}"></span>
                <span class="cell cell04" data-bind="css: {txtRed: awardId == 1}, attr: {title: '获得了' + awardName}">获得了<span data-bind="text: awardName"></span></span>
            </div>
            <!-- /ko -->
        </div>
        <!-- /ko -->
        <!-- ko if: bigAwardList().length === 0-->
        <div style="text-align:center; width: 100%; position: absolute; left: 0; top: 50%;">暂时还没有人中奖哦~</div>
        <!-- /ko -->
    </div>
    <div class="section section04" style="position: relative;">
        <!--抽奖记录-->
        <h2 class="titleTag">我的抽奖记录</h2>
        <p class="titleState">实物奖品于9月10日后统一寄送</p>
        <!-- ko if: historyList().length > 0-->
        <div class="recordList hd" style="display: none;" data-bind="visible: historyList().length > 0">
            <span class="cell cell01">抽奖次数</span>
            <span class="cell cell02">抽奖时间</span>
            <span class="cell cell03">抽奖奖励</span>
        </div>
        <div class="recordBox" style="display: none;" data-bind="visible: historyList().length > 0">
            <!-- ko foreach: historyList-->
            <div class="recordList">
                <span class="cell cell01" data-bind="text: no, attr: {title: no}"></span>
                <span class="cell cell02" data-bind="text: time, attr: {title: time}"></span>
                <span class="cell cell03" data-bind="text: award, attr: {title: award}"></span>
            </div>
            <!-- /ko -->
        </div>
        <!-- /ko -->
        <!-- ko if: historyList().length === 0 -->
        <div style="text-align:center; width: 100%; position: absolute; left: 0; top: 50%;">您还没有抽中奖励哦~</div>
        <!-- /ko -->
    </div>
    <div class="section section05">
        <!--推荐用老师app布置-->
        <h2 class="titleTag">推荐用老师app布置<br>每天更多抽奖机会</h2>
        <div class="downloadTips">
            <div class="code">
                <div class="codeImg"></div>
                <p class="txt">扫一扫下载APP</p>
            </div>
            <div class="info">
                <p class="txt">1.随时随地布置、检查作业</p>
                <p class="txt">2.学情分析更一目了然</p>
                <p class="txt">3.海量习题提分更快速</p>
            </div>
        </div>
    </div>
    <div class="section section06">
        <!--抽奖规则-->
        <h2 class="titleTag">抽奖规则</h2>
        <div class="ruleBox">
            <p class="txtRed">活动时间：</p>
            <p>6月13日 - 7月22日</p>
            <p class="txtRed">活动对象：</p>
            <p>布置假期作业的小学老师</p>
            <p class="txtRed">如何活的抽奖机会：</p>
            <p class="marB">1.布置假期作业，从布置当天算起每天可获得3次抽奖机会(当天的抽奖机会当天过期)</p>
            <p class="marB">2.假期作业开始后，分享班级进度给家长，每个班级每天最多获得4次抽奖机会：分享到班群+2次、分享到微信/QQ+2次(分享到微信/QQ成功后，需返回老师端，才能增加抽奖次数)</p>
            <p class="marB">3.删除假期作业后，后续抽奖机会将不再增加</p>
            <p>小贴士：假期作业可以设置作业开始时间，老师越早布置获得奖励的机会越大哦</p>
        </div>
    </div>
</div>
<div class="w-popup" style="display: none;" data-bind="visible: showPopup">
    <!--纯文案弹窗-->
    <div class="w-popupTxt" style="display: block">
        <div class="pop-close" data-bind="click: clickAlertClose"></div>
        <div class="txtInner">
            <h2 class="title" data-bind="text: popupTitle">温馨提示</h2>
            <div class="txtBox">
                <p data-bind="html: popupContent">出错了，稍后重试！</p>
            </div>
            <a href="javascript:void(0)" class="pop-btn" data-bind="text: popupBtnText, click: clickAlertBtn">知道了</a>
        </div>
    </div>
</div>
<script>
    var initMode = "teacherAwardMode";
</script>
    <#include "../../layout/project.footer.ftl">
</@layout.page>