<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
    title="一起-教研员-测评"
    pageJs=["systemtest", "jquery"]
    pageJsFile={"systemtest": "public/script/adminteacher/researchstaff/systemtest"}
    pageCssFile={"index": ["/public/skin/adminteacher/css/skin",  "/public/skin/adminteacher/css/common"]}>

    <#include "../header.ftl">
    <div class="outercontainer" id="systemtest">
        <div class="container">
            <!-- 主体 -->
            <div class="mainBox">
                <div class="mainInner">
                    <div class="topTitle"><a href="">首页</a> > <a href="">上传试卷</a></div>
                    <div class="contentBox">

                        <#include "../nav.ftl">

                        <div class="contentMain">
                            <div class="section">
                                <div class="selectWrap selectWrap02">
                                    <div class="selList" data-bind="visible: testDateList().length" style="display: none;">
                                        <span class="selText">时间</span>
                                        <div class="selBox" data-bind="click: showDateSelect, clickBubble: false">
                                            <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp-->
                                            <div class="list"><!-- ko text: choiceDateInfo().name --><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp': isShowDateSelect}"></i></div>
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowDateSelect">
                                                <!-- ko foreach: testDateList-->
                                                <li data-bind="
                                                    text: name,
                                                    css: {'active': $root.choiceDateInfo().value === value},
                                                    click: $root.choiceDate,
                                                    clickBubble: false"></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="selList" data-bind="visible: testGradeList().length" style="display: none;">
                                        <span class="selText">年级</span>
                                        <div class="selBox" data-bind="click: showGradeSelect, clickBubble: false">
                                            <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp -->
                                            <div class="list"><!-- ko text: choiceGradeInfo().name --><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp': isShowGradeSelect}"></i></div>
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowGradeSelect">
                                                <!-- ko foreach: testGradeList -->
                                                <li data-bind="
                                                    text: name,
                                                    css: {'active': $root.choiceGradeInfo().value === value},
                                                    click: $root.choiceGrade,
                                                    clickBubble: false"></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="section section02">
                                <div class="secRank">
                                    <div class="rankTitle">
                                        <span class="num03">测评名称</span>
                                        <span class="num04">测评时间</span>
                                        <span class="num05">答卷时长</span>
                                        <span class="num06">题量</span>
                                        <span class="num06">测评报告</span>
                                    </div>
                                    <ul class="rankList" style="display: none;" data-bind="visible: !systemTestListEmptyTip()">
                                        <!-- ko foreach: systemTestList -->
                                        <li class="rankBox">
                                            <div class="num03" data-bind="text: name, attr: {title: name}"></div>
                                            <div class="num04" data-bind="text: examTime"></div>
                                            <div class="num05" data-bind="text: durationMinutes + '分钟'"></div>
                                            <div class="num05" data-bind="text: totalNum"></div>
                                            <div class="num06">
                                                <#--平台报告存在 且 测试报告不存在-->
                                                <!-- ko if: reportLink1 && !reportLink2  -->
                                                <a data-bind="attr: { href: reportLink1 }" target="_blank">查看</a>
                                                <!-- /ko -->

                                                <#--平台报告不存在 且 测试报告存在-->
                                                <!-- ko if: !reportLink1 && reportLink2  -->
                                                <a data-bind="attr: { href: reportLink2 }" target="_blank">查看</a>
                                                <!-- /ko -->

                                                <#-- 有两份报告、或者两份报告都没有 -->
                                                <!-- ko if: (reportLink1 && reportLink2) || (!reportLink1 && !reportLink2) -->
                                                <a data-bind="click: $root.showChoiceReportPop"  href="javascript:void(0)">查看</a>
                                                <!-- /ko -->
                                            </div>
                                        </li>
                                        <!-- /ko -->
                                    </ul>
                                    <div class="empty-tip" data-bind="visible: systemTestListEmptyTip()">暂无数据</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    <#--选择报告弹窗-->
        <div class="pop_up_Wrap" data-bind="visible: isShowChoicePopup()" style="display: none;">
            <div class="content-box">
                <div class="pop-header">
                    <span class="pop-header-left">提示</span><span class="pop-header-right" id="popUpClose" data-bind="click: hideChoicePopup">X</span>
                </div>
                <div class="pop-main">
                    <div class='small-title' >请选择报告类型</div>
                    <div class="checked-box">
                        <a class="left-box" target="_blank" data-bind="attr: {href : reportUrlList()[0]}">
                            <div class="icon_box">
                                <img src="<@app.link href='public/skin/adminteacher/images/studyreport.png'/>" alt=""  >
                                <p class="title">学业成就分析</p>
                            </div>
                            <p class="del-text">关注学生是否达到了教学要求以及市（区/校）学生整体表现分布，
                                帮助教育决策者了解群体差异，更加合理的优化教育资源。</p>
                        </a>
                        <a class="right-box" target="_blank" data-bind="attr: {href : reportUrlList()[1]}">
                            <div class="icon_box">
                                <img src="<@app.link href='public/skin/adminteacher/images/testreport.png'/>" alt=""  >
                                <p class="title">诊断测评研究</p>
                            </div>
                            <p class="del-text">关注学生的学习进展情况，精细地诊断出学生学习过程的问题，
                                从而促进学生学习、帮助教师改进和强化教学活动。</p>
                        </a>
                    </div>
                    <div class="btn" data-bind="click: hideChoicePopup"><img src="<@app.link href='public/skin/adminteacher/images/ivonBtn.png'/>" alt="" > </div>
                </div>
            </div>
        </div>
    </div>
    <#include "../footer.ftl">
<script>
    var regionLevel = "${regionLevel!''}";
</script>
</@layout.page>