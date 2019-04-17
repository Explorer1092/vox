<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="布置作业抽大奖"
pageJs=["teacheraward"]
pageJsFile={"teacheraward" : "public/script/project/teacheraward"}
pageCssFile={"teacheraward" : ["public/skin/project/teacheraward/css/skin"]}>

<#include "../../layout/project.header.ftl">
<style>
	.m-footer,.m-footer .m-inner{
		/*background-color: #fad5d9;*/
		border-top:0;
	}
	.m-footer .copyright {
		color: #677384;
	}
	.m-footer .m-foot-link a{
		/*color: #b49d9f;*/
	}
	.m-footer .link {
		display: none !important;
	}
</style>
<!--抽奖头图-->
<div class="lotteryBanner"></div>
<!--抽奖说明-->
<div class="lotteryExplain">
	<p class="txtWhite font22">9月狂欢  惊喜不断  布置有礼</p>
	<p class="txtYellow font30">更有iPhone 7等着你！</p>
	<div class="time">
		<i class="icon clockIcon"></i><span>时间：2017.9.1-9.30</span>
		<i class="icon objectIcon"></i><span>对象：小学认证老师</span>
	</div>
</div>
<!--抽奖内容-->
<div class="lotteryMain">
	<div class="section section01">
		<h4 class="tips">每天用电脑布置作业<br>每天可获得<span class="txtOrange">2次</span>抽奖机会！</h4>
		<a href="/teacher/new/homework/batchassignhomework.vpage" target="_blank" class="assignBtn" id="assignBtn">去布置</a>
		<div class="countTips">推荐使用老师APP布置   每天奖励5次机会</div>
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
	<div class="section section02" >
		<!--转盘-->
		<p class="titleState">共获得<span data-bind="text: useTime() + numTime()"></span>次抽奖机会，已抽取<span data-bind="text: useTime"></span>次，剩余<span data-bind="text: numTime"></span>次机会</p>
		<div class="lotteryBox">
			<ul id="lottery">
				<li class="l-num01" data-type="7">
					<div class="reward beans"></div>
					<p class="name">1园丁豆</p>
				</li>
				<li class="l-num02" data-type="1">
					<div class="reward iphone"></div>
					<p class="name">iPhone7 1台</p>
				</li>
				<li class="l-num03" data-type="6">
					<div class="reward beans"></div>
					<p class="name">10园丁豆</p>
				</li>
				<li class="l-num04" data-type="3">
					<div class="reward kindle"></div>
					<p class="name">kindle1台</p>
				</li>
				<li class="l-num05" data-type="8">
					<div class="reward cartoon"></div>
					<p class="name">谢谢参与</p>
				</li>
				<li class="l-num06" data-type="4">
					<div class="reward pen"></div>
					<p class="name">凌美定制钢笔1支</p>
				</li>
				<li class="l-num07" data-type="5">
					<div class="reward beans"></div>
					<p class="name">100园丁豆</p>
				</li>
				<li class="l-num08" data-type="2">
					<div class="reward iPad"></div>
					<p class="name">ipad mini4 1台</p>
				</li>
			</ul>
			<a href="javascript:void(0)" class="drawBtn" id="lotterySubmit"></a>
		</div>
	</div>
	<div class="section section03" style="position: relative;">
		<!--抽奖记录-->
		<h2 class="titleTag">我的抽奖记录</h2>
		<p class="titleState">实物奖品将于10月07日后统一寄送</p>
		<div class="recordBox" data-bind="if: resultList().length > 0">
			<div class="recordList hd">
				<span class="cell cell01">抽奖次数</span>
				<span class="cell cell02">抽奖时间</span>
				<span class="cell cell03">抽奖奖励</span>
			</div>
			<div data-bind="foreach: resultList">
				<div class="recordList">
					<span class="cell cell01">第<span data-bind="text: _countIndex"></span>次</span>
					<span class="cell cell02"><span data-bind="text: _date"></span>&nbsp;<span data-bind="text: _time"></span></span>
					<span class="cell cell03"data-bind="text: _awardNamePara"></span>
				</div>
			</div>
		</div>
		<div style="text-align:center; width: 100%; position: absolute; left: 0; top: 50%;" data-bind="if: resultList().length === 0">您还未参与抽奖</div>
	</div>
	<div class="section section04" style="position: relative;">
		<!--大奖动态-->
		<h2 class="titleTag">大奖动态</h2>
		<p class="titleState">显示最新的20条记录</p>
		<div class="dynamicBox" data-bind="foreach: bigList">
			<div class="recordList">
				<span class="cell cell01" data-bind="text: _date"></span>
				<span class="cell cell02" data-bind="text: _time"></span>
				<span class="cell cell03"><span data-bind="text: cityName"></span><span data-bind="text: _teacherName"></span></span>
				<span class="cell cell04">获得了<span data-bind="text: awardName"></span></span>
			</div>
		</div>
		<div style="text-align:center; width: 100%; position: absolute; left: 0; top: 50%;" data-bind="if: bigList().length === 0">暂未发布大奖</div>
	</div>
</div>

<!--弹窗-->
<div class="w-popup" style="display: none;" data-bind="visible: showPop">
	<!--实物奖励-->
	<div class="w-popupInner" data-bind="visible: showPopProductAward">
		<div class="close" data-bind="click: closePopProductAward"></div>
		<div class="title">中奖啦！</div>
		<div class="main">
			<div class="inner">
				<h3 class="tips">恭喜您获得<span data-bind="text: awardName"></span>！</h3>
				<p class="font12">奖品型号图片供参考，具体以实物为准。<br>奖品于10月07日之后统一寄送，请注意查收电话通知哦~</p>
			</div>
		</div>
		<div class="btnBox"><a href="javascript:void(0)" class="p-btn" data-bind="click: closePopProductAward">确定</a></div>
	</div>
	<!--中奖学豆-->
	<div class="w-popupInner" data-bind="visible: showPopDousAward">
		<div class="close" data-bind="click: closePopDousAward"></div>
		<div class="title">中奖啦！</div>
		<div class="main">
			<div class="inner">
				<i class="beansIcon"></i>
				<h3 class="tips">恭喜您获得<span data-bind="text: awardName"></span>！</h3>
			</div>
		</div>
		<div class="btnBox"><a href="javascript:void(0)" class="p-btn" data-bind="click: closePopDousAward">确定</a></div>
	</div>
	<!--没中奖-->
	<div class="w-popupInner" data-bind="visible: showPopNoAward">
		<div class="close" data-bind="click: closePopNoAward"></div>
		<div class="title">再接再厉</div>
		<div class="main">
			<div class="inner">
				<h3 class="tips">很遗憾，奖品溜走了！</h3>
			</div>
		</div>
		<div class="btnBox"><a href="javascript:void(0)" class="p-btn" data-bind="click: closePopNoAward">知道了</a></div>
	</div>
    <!--未认证 / 报错弹窗-->
    <div class="w-popupTxt" data-bind="visible: showPopNoCheck">
        <div class="txtInner">
            <i class="markIcon"></i>
            <p class="txt" data-bind="html: noCheckPara"></p>
        </div>
        <div class="btnBox"><a href="javascript:void(0)" class="p-btn" data-bind="click: closePopNoCheck">确定</a></div>
    </div>
</div>

<script>
	var initMode = "teacherAwardMode";
</script>
<#include "../../layout/project.footer.ftl">
</@layout.page>