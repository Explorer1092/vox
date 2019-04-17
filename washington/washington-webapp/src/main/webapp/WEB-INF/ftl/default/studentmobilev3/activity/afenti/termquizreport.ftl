<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="期末测评活动"
pageJs=["termquizreport"]
pageJsFile={"termquizreport" : "public/script/mobile/student/activity/afenti/termquizreport"}
pageCssFile={"termquiz" : ["public/skin/mobile/student/app/activity/afenti/css/skin"]}
>

<div class="re-header">
    <div class="num"><!--ko text: score-->0<!--/ko--><span>分</span></div>
    <div class="tips">
        <span class="fontB"><!--ko text: setScoreTip().status-->--<!--/ko-->，</span>
        <span class="fontM"><!--ko text: setScoreTip().title-->--<!--/ko--></span>
        <i class="beansIcon"></i>
        <span>×<!--ko text: integral-->0<!--/ko--> </span>
    </div>
</div>
<div class="re-main">
    <div class="title">知识点掌握情况</div>
    <div class="graphBox">
        <div class="inner">
            <#--<span class="percent pTop">20%</span>
            <span class="percent pFooter">80%</span>-->
            <p id="radial"></p>
        </div>
    </div>
    <div class="tips">
        <!--ko if: (100-score() == 0)-->
        知识点已全部掌握
        <!--/ko-->
        <span data-bind="visible: (100-score() != 0)" style="display: none;">
            <span class="red" ><!--ko text: (100-score())--><!--/ko-->%</span>
            知识点没有掌握
        </span>
    </div>
</div>
<div class="re-main">
    <div class="title">出错知识点详情</div>
    <div class="infoBox" data-bind="visible: errorPointsList().length != 0,if: errorPointsList().length != 0" style="display: none;">
        <!-- ko foreach : {data : errorPointsList, as : '_error'} -->
        <p class="txt"><!--ko text: ($index()+1)--><!--/ko-->、<!--ko text: _error.name--><!--/ko--></p>
        <!--/ko-->
    </div>
    <div class="infoEmpty" data-bind="visible: errorPointsList().length == 0" style="display: none;">
        <i class="zan-icon"></i>
    </div>
    <div class="tips" data-bind="visible: errorPointsList().length == 0" style="display: none;">真棒，没有出错的知识点</div>
</div>
<div class="re-footer">
    <div class="footerFixed">
        <div class="tips" data-bind="visible: errorPointsList().length == 0" style="display: none;">阿分题提高复习效率，专注期末提分，来试试吧</div>
        <div class="tips" data-bind="visible: errorPointsList().length != 0" style="display: none;">错题已收集在阿分题错题本中，快去订正它们吧</div>
        <div class="inner">
            <a href="javascript:void(0)" data-bind="click: openAppBtn" class="btn">前往阿分题</a>
        </div>
    </div>
</div>

</@layout.page>