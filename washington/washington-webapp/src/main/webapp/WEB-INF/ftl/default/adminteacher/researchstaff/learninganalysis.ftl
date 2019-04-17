<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起-教研员-学情分析"
pageJs=["learninganalysis", "jquery"]
pageJsFile={"learninganalysis": "public/script/adminteacher/researchstaff/learninganalysis"}
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
                                <div class="selList" data-bind="visible: analysisDateList().length"
                                     style="display: none;">
                                    <span class="selText">时间</span>
                                    <div class="selBox" data-bind="click: showDateSelect, clickBubble: false">
                                        <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp -->
                                        <div class="list"><!-- ko text: choiceDateInfo().name --><!-- /ko --><i
                                                class="arrow" data-bind="css: {'arrowUp' : isShowDateSelect}"></i></div>
                                        <ul class="hideBox" style="display: none;"
                                            data-bind="visible: isShowDateSelect">
                                            <!-- ko foreach: analysisDateList -->
                                            <li data-bind="
                                                    css: {'active': $root.choiceDateInfo().value === value},
                                                    text: name,
                                                    click: $root.choiceDate,
                                                    clickBubble: false"></li>
                                            <!-- /ko -->
                                        </ul>
                                    </div>
                                </div>
                                <div class="selList" data-bind="visible: analysisGradeList().length"
                                     style="display: none;">
                                    <span class="selText">年级</span>
                                    <div class="selBox" data-bind="click: showGradeSelect, clickBubble: false">
                                        <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp-->
                                        <div class="list"><!-- ko text: choiceGradeInfo().name --><!-- /ko --><i
                                                class="arrow" data-bind="css: {'arrowUp' : isShowGradeSelect}"></i>
                                        </div>
                                        <ul class="hideBox" style="display: none;"
                                            data-bind="visible: isShowGradeSelect">
                                            <!-- ko foreach: analysisGradeList -->
                                            <li data-bind="
                                                    css: {'active': $root.choiceGradeInfo().value === value},
                                                    text: name,
                                                    click: $root.choiceGrade,
                                                    clickBubble: false"></li>
                                            <!-- /ko -->
                                        </ul>
                                    </div>
                                </div>
                                <div class="selList" data-bind="visible: analysisCityList().length"
                                     style="display: none;">
                                    <span class="selText">学校</span>
                                    <div class="selBox" data-bind="
                                            click: showCitySelect,
                                            clickBubble: false,
                                            event: {
                                                mouseover: mouseoverCitySchoolSelect,
                                                mouseout: mouseoutCitySchoolSelect
                                            }">
                                        <!-- 提示框 -->
                                        <div class="point" style="display: none;" data-bind="
                                                text: choiceSchoolInfo().schoolName,
                                                click: function () {return false;},
                                                clickBubble: false,
                                                event: {
                                                    mouseout: mouseoutCitySchoolTip
                                                },
                                                visible: isShowCitySchoolNameTip"></div>
                                        <!-- 下拉框隐藏时只显示arrow  出现时加上arrowUp -->
                                        <div class="list list02"><!-- ko text: choiceSchoolInfo().schoolName--><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp': isShowCitySelect"></i></div>

                                        <#--一级下拉（区list）-->
                                        <ul class="hideBox JS-CityBox" style="display: none;" data-bind="
                                                visible: isShowCitySelect,
                                                event: {
                                                    mouserover: $root.mouseoverCityList,
                                                    mouserout: $root.mouseoverCityList,
                                                }">
                                            <!-- ko foreach: analysisCityList -->
                                            <li data-bind="
                                                    text: cityName,
                                                    click: function () {return false;},
                                                    clickBubble: false,
                                                    css: {
                                                        'active': $root.choiceCityInfo().cityId === cityId
                                                    },
                                                    event: {
                                                        mouseover: $root.mouseoverCity.bind($data, $index()),
                                                        mouseout: $root.mouseoutCity
                                                    }"></li>
                                            <!-- /ko -->
                                        </ul>

                                        <#--二级下拉（学校list）-->
                                        <ul class="schoolBox JS-SchoolBox" style="display: none;" data-bind="
                                                visible: isShowSchoolSelect,
                                                style: {
                                                    top: schoolSelectTopValue
                                                },
                                                event: {
                                                    mouseover: $root.mouseoverSchool,
                                                    mouseout: $root.mouseoutSchool
                                                }">
                                            <!-- ko foreach: analysisSchoolList -->
                                            <li data-bind="
                                                    text: schoolName,
                                                    css: {
                                                        'active': $root.choiceSchoolInfo().schoolId === schoolId
                                                    },
                                                    click: $root.choiceSchool,
                                                    clickBubble: false"></li>
                                            <!-- /ko -->
                                        </ul>
                                    </div>
                                </div>
                                <div class="selList" data-bind="visible: analysisCityList().length"
                                     style="display: none;">
                                    <div class="selBox selBoxSearch">
                                        <div class="list">
                                            <input class="searchInput" placeholder-class="placeholder" type="text"
                                                   placeholder="输入学校名称" data-bind="
                                                    value: searchInputText,
                                                    valueUpdate: 'keyup',
                                                    event: {keyup: searchSchool}">
                                            <i class="search" data-bind="click: searchSchool"></i>
                                        </div>
                                        <ul class="schoolBox schoolBox02" style="display: none"
                                            data-bind="visible: (isShowSearchSchoolSelect() && analysisSearchSchoolList().length)">
                                            <!-- ko foreach: analysisSearchSchoolList -->
                                            <li data-bind="
                                                    text: schoolName,
                                                    click: $root.choiceSearchSchool,
                                                    clickBubble: false"></li>
                                            <!-- /ko -->
                                        </ul>
                                        <ul class="hideBox hideBox03" style="display: none"
                                            data-bind="visible: (isShowSearchSchoolSelect() && !analysisSearchSchoolList().length)">
                                            <div class="noResult">暂无该学校</div>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- 单元训练情况 -->
                        <div class="section">
                            <div class="secTableBox02">
                                <div class="secTableContent" id="unitTestChart"></div>
                                <div class="secTitle secTitle02 no-marginleft">单元训练情况
                                    <div class="pic">
                                        <div class="outer">
                                            <div class="annotationText maxWidth">单元人均题数：根据单元计算每个学生的平均练习题目数量<br>单元正确率：统计学生做题数据，计算每个单元的平均正确率
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="emptyTip" style="display: none;"
                                     data-bind="visible: isShowUnitTestChartEmptyTip">暂无数据
                                </div>
                            </div>
                        </div>
                        <!-- 学科能力养成 -->
                        <div class="section">
                            <div class="secTableBox02">
                                <div class="secTableContent" id="subjectAbilityChart"></div>
                                <div class="secTitle secTitle02 no-marginleft">学科能力养成
                                    <div class="pic">
                                        <div class="outer">
                                            <div class="annotationText maxWidth">某个能力维度下的做题正确率越高，能力值越高，单项能力最高值为100</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="emptyTip" style="display: none;"
                                     data-bind="visible: isShowSubjectAbilityChartEmptyTip">暂无数据
                                </div>
                            </div>
                        </div>
                        <!-- 知识板块掌握度 -->
                        <div class="section">
                            <div class="secTableBox02">
                                <#if currentUser?? && currentUser.subject == 'MATH'>
                                <div class="radioContent">
                                    <div class="radioBox" data-bind="click: changeKnowledgePlate.bind($data, 1)"><span class="radio" data-bind="css: {'active': knowledgePlateIndex() === 1 }"></span>一级知识板块</div>
                                    <div class="radioBox" data-bind="click: changeKnowledgePlate.bind($data, 2)"><span class="radio" data-bind="css: {'active': knowledgePlateIndex() === 2 }"></span>二级知识板块</div>
                                </div>
                                </#if>
                                <div class="secTableContent" id="knowledgeModuleChart"></div>
                                <div class="secTitle secTitle02 no-marginleft">知识板块掌握度
                                    <div class="pic">
                                        <div class="outer">
                                            <div class="annotationText">某个知识板块下的做题正确率越高，该知识板块掌握度越高，单项知识板块最高值为100</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="emptyTip" style="display: none;" data-bind="visible: isShowKnowledgeModuleChartEmptyTip"><#if currentUser?? && currentUser.subject == 'CHINESE'>暂未划分知识板块<#else>暂无数据</#if></div>
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