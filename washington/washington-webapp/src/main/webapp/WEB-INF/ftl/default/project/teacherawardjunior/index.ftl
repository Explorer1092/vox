<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="假期作业抽奖"
pageJs=["teacheraward"]
pageJsFile={"teacheraward" : "public/script/project/teacherawardjunior"}
pageCssFile={"teacheraward" : ["public/skin/project/teacherawardjunior/css/skin"]}>

<#include "../../layout/project.header.ftl">
<!--抽奖头图-->
<div class="lotteryBanner"></div>
<!--抽奖内容-->
<div class="lotteryMain">
    <div class="section section01">
        <h3 class="titleTag"><i class="tagIcon01"></i>PC布置 每天<span>2</span>次抽奖机会<i class="tagIcon02"></i></h3>
        <a href="javascript:void(0)" class="assignBtn" data-bind="text: assignBtnText, css: {disabled: isAssign}, click: toAssign.bind($data)">去布置</a>
        <div class="lotteryState">
            <h3 class="titleTag">老师将有机会赢得</h3>
            <div class="fl-reward">
                <div class="left">iPhone 7</div>
                <div class="right">×4</div>
            </div>
            <div class="fl-reward">
                <div class="left">Kindle</div>
                <div class="right">×8</div>
            </div>
            <div class="fl-reward diff">
                <div class="left">JBL音响</div>
                <div class="right">×12</div>
            </div>
            <div class="fl-reward">
                <div class="left">蓝牙耳机</div>
                <div class="right">×16</div>
            </div>
        </div>
    </div>
    <div class="section section02" >
        <!--转盘-->
        <div class="lotteryBox">
            <ul id="lottery">
                <li class="l-num01" data-type="6">
                    <div class="reward reward01"></div>
                </li>
                <li class="l-num02" data-type="8">
                    <div class="reward reward02"></div>
                </li>
                <li class="l-num03" data-type="2">
                    <div class="reward reward03"></div>
                </li>
                <li class="l-num04" data-type="7">
                    <div class="reward reward04"></div>
                </li>
                <li class="l-num05" data-type="4">
                    <div class="reward reward05"></div>
                </li>
                <li class="l-num06" data-type="5">
                    <div class="reward reward06"></div>
                </li>
                <li class="l-num07" data-type="1">
                    <div class="reward reward07"></div>
                </li>
                <li class="l-num08" data-type="3">
                    <div class="reward reward08"></div>
                </li>
            </ul>
            <a href="javascript:void(0)" class="drawBtn" id="lotterySubmit">
                <p>剩余<span data-bind="text: numTime">0</span>次</p>
            </a>
        </div>
    </div>
    <div class="section section03" style="position: relative;">
        <!--大奖动态-->
        <h2 class="titleTag">大奖动态</h2>
        <p class="titleState diff">动态实时更新，越早布置机会越大</p>
        <!-- ko if: bigAwardList().length > 0 -->
        <div class="dynamicBox" style="display: none;" data-bind="visible: bigAwardList().length > 0">
            <!-- ko foreach: bigAwardList-->
            <div class="recordList">
                <span class="cell cell01" data-bind="text: date, attr: {title: date}"></span>
                <#--<span class="cell cell02" data-bind="text: time, attr: {title: time}"></span>-->
                <span class="cell cell03" data-bind="text: cityName + userName, attr: {title: cityName + userName}"></span>
                <span class="cell cell04" data-bind="attr: {title: '获得了' + awardName}">获得了<span data-bind="text: awardName"></span></span>
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
        <h2 class="titleTag">我的中奖记录</h2>
        <p class="titleState">实物奖品于4月9日后统一寄出</p>
        <!-- ko if: historyList().length > 0-->
        <div class="recordList hd" style="display: none;" data-bind="visible: historyList().length > 0">
            <span class="cell cell01">中奖次数</span>
            <span class="cell cell02">中奖时间</span>
            <span class="cell cell03">奖励</span>
        </div>
        <div class="recordBox" style="display: none;" data-bind="visible: historyList().length > 0">
            <!-- ko foreach: historyList-->
            <div class="recordList">
                <span class="cell cell01" data-bind="text: _no, attr: {title: _no}"></span>
                <span class="cell cell02" data-bind="text: date, attr: {title: date}"></span>
                <span class="cell cell03" data-bind="text: awardName, attr: {title: awardName}"></span>
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
        <h2 class="titleTag">推荐用老师APP布置<br>每天更多抽奖机会</h2>
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
            <p class="txtOrange">活动时间：</p>
            <p class="marB">2018.03.12——2018.04.08</p>
            <p class="txtOrange">活动对象：</p>
            <p class="marB">部分初中英语老师</p>
            <p class="txtOrange">如何获得抽奖机会：</p>
            <p>1、每天通过APP布置作业，当天可获得5次抽奖机会；</p>
            <p class="marB">2、每天通过PC布置作业，当天可获得2次抽奖机会；</p>
            <#--<p class="marB">小贴士：假期作业可设置作业开始时间，老师越早布置获得奖励的机会越大哦～</p>-->
            <p class="copyright">本次活动最终解释权归一起作业所有</p>
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
    var assignLink = "${(ProductConfig.getJuniorSchoolUrl())!''}/teacher/assign/index";
</script>
    <#include "../../layout/project.footer.ftl">
</@layout.page>