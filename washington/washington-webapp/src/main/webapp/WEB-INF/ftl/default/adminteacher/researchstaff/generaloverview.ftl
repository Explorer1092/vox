<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
    title="一起-教研员-总体概览"
    pageJs=["generaloverview", "jquery"]
    pageJsFile={"generaloverview": "public/script/adminteacher/researchstaff/generaloverview"}
    pageCssFile={"index": ["/public/skin/adminteacher/css/skin", "/public/skin/adminteacher/css/common"]}>

    <#include "../header.ftl">
    <div class="outercontainer" id="generaloverviewModal">
        <div class="container">
            <!-- 主体 -->
            <div class="mainBox">
                <div class="mainInner">
                    <div class="topTitle"><a href="">首页</a> > <a href="">上传试卷</a></div>
                    <div class="contentBox">

                        <#include "../nav.ftl">

                        <div class="contentMain">
                            <!-- 老师使用情况 -->
                            <div class="sectionWrap">
                                <div class="section">
                                    <div class="secTitle">老师使用情况<div class="pic"><div class="outer"><div class="annotationText maxWidth">本月使用人数：本月布置练习的老师用户数<br>本月新增人数：本月新注册的老师用户数<br>（每月4号开始更新）</div></div></div></div>
                                    <div class="secState">
                                        <div class="useNum">
                                            <div class="num" data-bind="text: teacherCurMonthUseNum">0</div>
                                            <div class="text">本月使用人数</div>
                                        </div>
                                        <div class="useNum useNum02">
                                            <div class="num" data-bind="text: teacherCurMonthAddNum">0</div>
                                            <div class="text">本月新增人数</div>
                                        </div>
                                    </div>
                                </div>
                                <!-- 学生使用情况 -->
                                <div class="section">
                                    <div class="secTitle">学生使用情况<div class="pic"><div class="outer"><div class="annotationText maxWidth">本月使用人数：本月做练习的学生用户数<br>本月新增人数：本月新注册的学生用户数<br>（每月4号开始更新）</div></div></div></div>
                                    <div class="secState">
                                        <div class="useNum">
                                            <div class="num" data-bind="text: studentCurMonthUseNum">0</div>
                                            <div class="text">本月使用人数</div>
                                        </div>
                                        <div class="useNum useNum02">
                                            <div class="num" data-bind="text: studentCurMonthAddNum">0</div>
                                            <div class="text">本月新增人数</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- 练习情况 -->
                            <div class="section">
                                <div class="secTitle">练习情况<div class="pic"><div class="outer"><div class="annotationText" style="width: 250px;">统计练习数据（每月4号开始更新）</div></div></div></div>
                                <div class="selectWrap">
                                    <div class="selList" data-bind="visible: homeworkDateList().length" style="display: none;">
                                        <span class="selText">时间</span>
                                        <div class="selBox" data-bind="click: showDateSelect, clickBubble: false">
                                            <!-- 下拉框隐藏显示arrow  出现时显示arrowUp -->
                                            <div class="list"><!-- ko text: choiceDateInfo().name --><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp': isShowDateSelect}"></i></div>
                                            <!-- 下拉框 -->
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowDateSelect">
                                                <!-- ko foreach: homeworkDateList-->
                                                <li data-bind="
                                                    text: name,
                                                    css: {'active': $root.choiceDateInfo().value === value},
                                                    click: $root.choiceDate,
                                                    clickBubble: false
                                                    "></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="selList" data-bind="visible: homeworkGradeList().length" style="display: none;">
                                        <span class="selText">年级</span>
                                        <div class="selBox" data-bind="click: showGradeSelect, clickBubble: false">
                                            <!-- 下拉框隐藏显示arrow  出现时显示arrowUp -->
                                            <div class="list"><!-- ko text: choiceGradeInfo().name --><!-- /ko --><i class="arrow" data-bind="css: {'arrowUp': isShowGradeSelect}"></i></div>
                                            <!-- 下拉框 -->
                                            <ul class="hideBox" style="display: none;" data-bind="visible: isShowGradeSelect">
                                                <!-- ko foreach: homeworkGradeList-->
                                                <li data-bind="
                                                    text: name,
                                                    css: {'active': $root.choiceGradeInfo().value === value},
                                                    click: $root.choiceGrade,
                                                    clickBubble: false
                                                    "></li>
                                                <!-- /ko -->
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- 类型占比和类型及数量对比 -->
                            <div class="sectionWrap">
                                <div class="section">
                                    <div class="secTableBox02">
                                        <div class="secTableContent" id="typePercentChart"></div>
                                        <div class="secTitle no-marginleft">类型占比<div class="pic"><div class="outer"><div class="annotationText minWidth">各练习类型的比例</div></div></div></div>
                                        <div class="seeDetail" style="display: none;" data-bind="
                                            click: showChartDetail.bind($data, 'typePercentChart', '类型占比'),
                                            visible: detailChartData() && !isShowTypePercentChartEmptyTip()
                                        ">查看详情</div>
                                        <div class="emptyTip" style="display: none;" data-bind="visible: isShowTypePercentChartEmptyTip">暂无数据</div>
                                    </div>
                                </div>
                                <div class="section">
                                    <div class="secTableBox02">
                                        <div class="secTableContent" id="numContrastChart"></div>
                                        <div class="secTitle no-marginleft">类型及数量对比<div class="pic"><div class="outer"><div class="annotationText" style="width: 240px;">各练习类型的比例及数量对比情况</div></div></div></div>
                                        <div class="seeDetail" style="display: none;" data-bind="
                                            click: showChartDetail.bind($data, 'numContrastChart', '类型及数量对比'),
                                            visible: detailChartData() && !isShowNumContrastChartEmptyTip()
                                        ">查看详情</div>
                                        <div class="emptyTip" style="display: none;" data-bind="visible: isShowNumContrastChartEmptyTip">暂无数据</div>
                                    </div>
                                </div>
                            </div>
                            <!-- 排行榜 -->
                            <div class="sectionWrap">
                                <div class="section">
                                    <div class="secTitle">活跃学校<div class="pic"><div class="outer"><div class="annotationText" style="width: 210px;">根据学校的练习完成次数排名</div></div></div></div>
                                    <div class="secRank">
                                        <div class="rankTitle rankTitle02">
                                            <span class="num01">排名</span>
                                            <span class="num02">学校</span>
                                            <span class="num01">练习次数</span>
                                        </div>
                                        <ul class="rankList" style="display: none;" data-bind="visible: activeSchoolList().length">
                                            <!-- ko foreach: activeSchoolList -->
                                            <li class="rankBox">
                                                <div class="num num01" data-bind="text: rank"></div>
                                                <div class="num02" data-bind="text: school, attr: {title: school}"></div>
                                                <div class="num01" data-bind="text: value"></div>
                                            </li>
                                            <!-- /ko -->
                                        </ul>
                                        <div class="empty-tip" data-bind="visible: !activeSchoolList().length">暂无排名</div>
                                    </div>
                                </div>
                                <div class="section">
                                    <div class="secTitle">活跃老师<div class="pic"><div class="outer"><div class="annotationText" style="width: 210px;">根据老师的练习布置次数排名</div></div></div></div>
                                    <div class="secRank">
                                        <div class="rankTitle rankTitle02">
                                            <span class="num num07">排名</span>
                                            <span class="num08">老师</span>
                                            <span class="num09">学校</span>
                                            <span class="num08">练习次数</span>
                                        </div>
                                        <ul class="rankList" style="display: none;" data-bind="visible: activeTeacherList().length">
                                            <!-- ko foreach: activeTeacherList -->
                                            <li class="rankBox">
                                                <div class="num num07" data-bind="text: rank"></div>
                                                <div class="num08" data-bind="text: teacher, attr: {title: teacher}"></div>
                                                <div class="num09" data-bind="text: school, attr: {title: school}"></div>
                                                <div class="num08" data-bind="text: value"></li>
                                            </li>
                                            <!-- /ko -->
                                        </ul>
                                        <div class="empty-tip" data-bind="visible: !activeTeacherList().length">暂无排名</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 图例弹窗 -->
        <div class="alertChartMask" style="display: none;" data-bind="visible: isShowDeatilChart, style: {height: detailChartMaskHeight}">
            <div class="alertChartBox" data-bind="style: {marginTop: detailChartMarginTop}">
                <div class="closeBtn" data-bind="click: hideDetailChart"></div>
                <div class="title" data-bind="text: detailChartTitle"></div>
                <div class="chartContent" id="detailChart"></div>
            </div>
        </div>
    </div>
    <#include "../footer.ftl">
</@layout.page>