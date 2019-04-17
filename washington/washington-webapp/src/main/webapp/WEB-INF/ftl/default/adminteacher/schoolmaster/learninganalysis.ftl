<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
    title="一起-校长-学情分析"
    pageJs=["learninganalysis", "jquery"]
    pageJsFile={"learninganalysis": "public/script/adminteacher/schoolmaster/learninganalysis"}
    pageCssFile={"index": ["/public/skin/adminteacher/css/skin", "/public/skin/adminteacher/css/common"]}>

    <#include "../header.ftl">
    <div class="outercontainer" id="learninganalysis">
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
                                    <div class="selList" data-bind="visible: analysisDateList().length" style="display: none;">
                                        <span class="selText">时间</span>
                                        <div class="selBox" data-bind="click: showDateSelect, clickBubble: false">
                                            <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp -->
                                            <div class="list"><!-- ko text: choiceDateInfo().name --><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp' : isShowDateSelect}"></i></div>
                                            <!-- 下拉框 -->
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowDateSelect">
                                                <!-- ko foreach: analysisDateList-->
                                                <li data-bind="
                                                    css: {'active': $root.choiceDateInfo().value === value},
                                                    text: name,
                                                    click: $root.choiceDate,
                                                    clickBubble: false"></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="selList" data-bind="visible: analysisGradeList().length" style="display: none;">
                                        <span class="selText">年级</span>
                                        <div class="selBox" data-bind="click: showGradeSelect, clickBubble: false">
                                            <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp -->
                                            <div class="list"><!-- ko text: choiceGradeInfo().name --><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp' : isShowGradeSelect}"></i></div>
                                            <!-- 下拉框 -->
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowGradeSelect">
                                                <!-- ko foreach: analysisGradeList-->
                                                <li data-bind="
                                                    css: {'active': $root.choiceGradeInfo().value === value},
                                                    text: name,
                                                    click: $root.choiceGrade,
                                                    clickBubble: false"></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="selList" data-bind="visible: analysisSubjectList().length" style="display: none;">
                                        <span class="selText">学科</span>
                                        <div class="selBox" data-bind="click: showSubjectSelect, clickBubble: false">
                                            <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp-->
                                            <div class="list"><!-- ko text: choiceSubjectInfo().name --><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp' : isShowSubjectSelect}"></i></div>
                                            <!-- 下拉框 -->
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowSubjectSelect">
                                                <!-- ko foreach: analysisSubjectList-->
                                                <li data-bind="
                                                    css: {'active': $root.choiceSubjectInfo().value === value},
                                                    text: name,
                                                    click: $root.choiceSubject,
                                                    clickBubble: false"></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="selList" data-bind="visible: analysisClazzList().length" style="display: none;">
                                        <span class="selText">班级</span>
                                        <div class="selBox" data-bind="click: showClazzSelect, clickBubble: false">
                                            <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp -->
                                            <div class="list"><!-- ko text: choiceClazzInfo().name --><!-- /ko --><i class="arrow"></i></div>
                                            <!-- 下拉框 -->
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowClazzSelect" data-bind="css: {'arrowUp' : isShowClazzSelect}">
                                                <!-- ko foreach: analysisClazzList-->
                                                <li data-bind="
                                                    css: {'active': $root.choiceClazzInfo().id === id},
                                                    text: name,
                                                    click: $root.choiceClazz,
                                                    clickBubble: false"></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- 单元训练情况 -->
                            <div class="section">
                                <div class="secTableBox02">
                                    <div class="secTableContent" id="unitTestChart"></div>
                                    <div class="secTitle secTitle02 no-marginleft">单元训练情况<div class="pic"><div class="outer"><div class="annotationText maxWidth">单元人均题数：根据单元计算每个学生的平均练习题目数量<br>单元正确率：统计学生做题数据，计算每个单元的平均正确率</div></div></div></div>
                                    <div class="emptyTip" style="display: none;" data-bind="visible: isShowUnitTestChartEmptyTip">暂无数据</div>
                                </div>
                            </div>
                            <!-- 学科能力养成 -->
                            <div class="section">
                                <div class="secTableBox02">
                                    <div class="secTableContent" id="subjectAbilityChart"></div>
                                    <div class="secTitle secTitle02 no-marginleft">学科能力养成<div class="pic"><div class="outer"><div class="annotationText maxWidth">某个能力维度下的做题正确率越高，能力值越高，单项能力最高值为100</div></div></div></div>
                                    <div class="emptyTip" style="display: none;" data-bind="visible: isShowSubjectAbilityChartEmptyTip">暂无数据</div>
                                </div>
                            </div>
                            <!-- 知识板块掌握度 -->
                            <div class="section">
                                <div class="secTableBox02">
                                    <div class="radioContent" data-bind="visible: choiceSubjectInfo().value === 'MATH'" style="display: none;">
                                        <div class="radioBox" data-bind="click: changeKnowledgePlate.bind($data, 1)"><span class="radio" data-bind="css: {'active': knowledgePlateIndex() === 1 }"></span>一级知识板块</div>
                                        <div class="radioBox" data-bind="click: changeKnowledgePlate.bind($data, 2)"><span class="radio" data-bind="css: {'active': knowledgePlateIndex() === 2 }"></span>二级知识板块</div>
                                    </div>
                                    <div class="secTableContent" id="knowledgeModuleChart"></div>
                                    <div class="secTitle secTitle02 no-marginleft">知识板块掌握度<div class="pic"><div class="outer"><div class="annotationText">某个知识板块下的做题正确率越高，该知识板块掌握度越高，单项知识板块最高值为100</div></div></div></div>
                                    <div class="emptyTip" style="display: none;" data-bind="
                                        visible: isShowKnowledgeModuleChartEmptyTip,
                                        text: choiceSubjectInfo().value === 'CHINESE' ? '暂未划分知识板块' : '暂无数据'">暂无数据</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <#include "../footer.ftl">
</@layout.page>