<#import "../../../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bgList"
title="期末测评"
pageJs=["termquiz"]
pageJsFile={"termquiz" : "public/script/mobile/student/activity/afenti/termquiz"}
pageCssFile={"termquiz" : ["public/skin/mobile/student/app/activity/afenti/css/skin"]}
>

<div class="te-listBox">
    <a href="javascript:void(0);" data-bind="click: openAppBtn.bind($data,'AfentiMath')" class="te-list">
        <img src="<@app.link href="/public/skin/mobile/student/app/activity/afenti/images/te-card01.png?v=1.0.0"/>">
        <div class="txt">
            <h2>数学提分测评</h2>
            <p>专题复习，提升分数若探囊取物</p>
        </div>
    </a>
    <a href="javascript:void(0);" data-bind="click: openAppBtn.bind($data,'AfentiExam')" class="te-list">
        <img src="<@app.link href="/public/skin/mobile/student/app/activity/afenti/images/te-card02.png?v=1.0.0"/>">
        <div class="txt">
            <h2>英语提分测评</h2>
            <p>精心模拟考试题，让你胸有成竹</p>
        </div>
    </a>
    <a href="javascript:void(0);" data-bind="click: openAppBtn.bind($data,'AfentiChinese')" class="te-list">
        <img src="<@app.link href="/public/skin/mobile/student/app/activity/afenti/images/te-card03.png?v=1.0.0"/>">
        <div class="txt">
            <h2>语文提分测评</h2>
            <p>教材完全同步，专注期末涨分</p>
        </div>
    </a>
</div>
<#--<div class="te-tips"></div>-->
<div class="te-footer">本页面的自学产品非老师布置的学校作业，请自愿开通使用。是否开通，不影响校内作业。</div>

<div class="gf-popup" data-bind="visible: showPopup" style="display: none;">
    <div class="gf-popupBox">
        <div class="hd"><span class="close" data-bind="click: closePopupBtn"></span></div>
        <div class="mn">
            <div class="info">
                <div class="rules">
                    <div class="title"><!--ko text: selectedSubjectMap().name--><!--/ko-->期末测评</div>
                    <div class="text"><!--ko text: selectedSubjectMap().qc--><!--/ko-->道题测试你的知识点掌握情况</div>
                </div>
            </div>
        </div>
        <div class="ft">
            <a href="javascript:void(0)" data-bind="visible: selectedSubjectMap().status == 'QUIZ' && questionIds().length != 0, click: gotoWorkBtn" style="display: none;" class="btn">开始测评</a>
            <a href="javascript:void(0)" data-bind="visible: selectedSubjectMap().status == 'REPORT' || questionIds().length == 0, click: gotoReportBtn" style="display:none;" class="btn">查看测评报告</a>
        </div>
    </div>
</div>

</@layout.page>