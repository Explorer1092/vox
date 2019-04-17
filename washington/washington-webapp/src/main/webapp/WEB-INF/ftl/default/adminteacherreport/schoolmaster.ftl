<#import "../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起-教研员测评报告"
pageJs=["generaloverview", "jquery"]
pageJsFile={"generaloverview": "public/script/adminteacherreport/schoolmaster"}
pageCssFile={"index": ["/public/skin/adminteacherreport/css/skin", "/public/skin/adminteacherreport/css/common"]}>

    <#include "./header.ftl">
    <div class="report_wrap city">
    <#--城市报告-->
        <!--头部 标题-->
        <div class="r_header">
            <a href="/" class="logo"></a>
            <div class="text01 title">大连市四年级测试报告</div>
            <div class="active_text01 download"><i class="icon_download"></i>下载报告</div>
        </div>
        <!--左边栏 目录-->
        <div class="r_sidebar_a">
            <!--1.测评情况-->
            <div class="menu_box">
                <div class="text03 title active_menu">1.测评情况</div>
            </div>
            <!--2.项目实施情况-->
            <div class="menu_box">
                <div class="text03 title">2.项目实施情况<i class="arrow arrow_blue"></i></div>
                <ul class="drop_menu text04">
                    <li>2.1项目背景</li>
                    <li>2.2测试工具开发原则</li>
                    <li>2.3施测情况</li>
                    <li>2.4数据分析与反馈报告</li>
                </ul>
            </div>
            <!--3.学生整体情况-->
            <div class="menu_box">
                <div class="text03 title">3.学生整体情况<i class="arrow arrow_blue"></i></div>
                <ul class="drop_menu text04">
                    <li>3.1各地区四率与达标率</li>
                    <li>3.2各地区平均得分与离散程度</li>
                    <li>3.3各地区跨次测评变化情况</li>
                </ul>
            </div>
            <!--4.学生学科表-->
            <div class="menu_box">
                <div class="text03 title">4.学生学科表现<i class="arrow arrow_blue"></i></div>
                <ul class="drop_menu text04">
                    <li>4.1知识点——技能掌握的程度</li>
                    <li>4.2 各地区的技能掌握程度</li>
                    <li>4.3变式任务——能力素养的发</li>
                    <li>4.4各地区邓丽素养的发展程度</li>
                </ul>
            </div>
            <!--5.评价总结与建议-->
            <div class="menu_box">
                <div class="text03 title">5.评价总结与建议<i class="arrow arrow_blue"></i></div>
                <ul class="drop_menu text04">
                    <li>5.1附表：题目属性及各区表现</li>
                </ul>
            </div>
        </div>
        <!--右边栏 报告内容-->
        <div class="r_main">
            <div class="r_sidebar_b">
                <div class="content_box">
                    <!--1.测评情况-->
                    <div class="download_box">
                        <div class="text01 big_title">1.测评情况</div>
                        <div class="download_middle">
                            <ul class="del_box">
                                <li class="text04 del_txt">本次测评覆盖了大连市的 10 个区县，188 所学校，9688 名小学四年级学生。测查的学科为小
                                    学四年级数学学科，测评的内容不仅包括学生在基础知识、基本技能方面所达到的水平，而且还
                                    包括时代发展所要求的小学生所必备的分析与解决问题与实践能力等核心素养。根据该次测评所
                                    收集的有效数据，我们的报告分两部分呈现了测评结果，分别为学生总体情况和学生学科表现。
                                </li>
                            </ul>
                        </div>
                    </div>
                    <!--2.项目实施情况-->
                    <div class="download_box">
                        <div class="text01 big_title">2.项目实施情况</div>
                        <div class="download_middle">
                            <div class="text02 small_title">2.1 项目背景</div>
                            <ul class="del_box">
                                <li class="text04 del_txt">使学生受到必要的数学教育，具有一定的数学素养” 是我国基础教育中数学教育的目标。一
                                    个学生具备了数学素养，则意味着这个学生具备了实际中应用数学知识的能力。在培养具备数学
                                    素养学生的过程中，需要全面了解学生数学学习过程和结果，才能更好的激励学生的学习和改进
                                    教师的教学，因此就需要建立评价目标、评价方法多元化的评价体系。本测评项目着眼于学科内
                                    知识点、技能、变式任务、能力素养的多维度诊断测评，希望能为学生成长、教师教学提供助力。
                                </li>
                            </ul>
                        </div>
                        <!--2.2 测试工具开发原则-->
                        <div class="download_middle">
                            <div class="text02 small_title">2.2 测试工具开发原则</div>
                            <ul class="del_box">
                                <li class="text04 del_txt">
                                    本项目的测评工具研发遵循了以下命题原则:
                                </li>
                                <li class="text04 del_txt">1. 命题基于《全日制义务教育数学课程标准 (修改稿)》(以下简称《标准》)，体现《标准》的基
                                    本理念和课程目标。
                                </li>
                                <li class="text04 del_txt">2. 试题注重考查学生对数学学科基本知识、技能的理解和掌握，在此基础上考查学生的数感及
                                    运算、符号意识、空间观念、抽象能力、数据分析、推理能力和综合运用所学知识在不同情
                                    境下解决实际问题的能力以及对重要的数学思想方法的理解。
                                </li>
                                <li class="text04 del_txt">
                                    3. 试题所隶属的内容维度、技能、能力维度和变式任务协调统一，试题比例恰当。
                                </li>
                                <li class="text04 del_txt">4. 考虑到不同地区可能使用不同的教材，采用本地教研员命题 (或审题) 的方式，以确保试题内
                                    容为所有学生都学过的内容。
                                </li>
                                <li class="text04 del_txt">5. 试题的整体布局合理科学，试卷结构良好，充分发挥每一道题的功能，使试卷达到一定的信
                                    度、效度和区分度，实现测试目标。
                                </li>
                            </ul>
                        </div>
                        <!--2.3 施测情况-->
                        <div class="download_middle">
                            <div class="text02 small_title">2.3 施测情况</div>
                            <ul class="del_box">
                                <li class="text04 del_txt">在市教研员、各区县教研员、校长及教师的组织下，测评项目得以顺利开展，表 1 呈现了各
                                    区县的学生参与人数，表 2 呈现了此次测评的主要内容。
                                </li>
                                <li class="table">
                                    <div class="text04 table_name">表 1: 该次测评各地区实际参与情况</div>
                                    <div class="table_box">
                                        <table class="table2_01">
                                            <thead class="text05">
                                            <tr>
                                                <th>地区名称</th>
                                                <th>学校数</th>
                                                <th>班数</th>
                                                <th>教师人数</th>
                                                <th>学生人数</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr class="table_active">
                                                <td>中山区</td>
                                                <td>5</td>
                                                <td>10</td>
                                                <td>8</td>
                                                <td>208</td>
                                            </tr>
                                            <tr>
                                                <td>中山区</td>
                                                <td>5</td>
                                                <td>10</td>
                                                <td>8</td>
                                                <td>208</td>
                                            </tr>
                                            <tr>
                                                <td>中山区</td>
                                                <td>5</td>
                                                <td>10</td>
                                                <td>8</td>
                                                <td>208</td>
                                            </tr>
                                            <tr>
                                                <td>中山区</td>
                                                <td>5</td>
                                                <td>10</td>
                                                <td>8</td>
                                                <td>208</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="text04 table_del">注意试题注重考查学生对数学学科基
                                        本知识、技能的理试题注重考查学生对数学学科基本知识、
                                        技能的理试题注重考查学生对数学学科基本知识、技能的理
                                    </div>
                                </li>
                                <li class="table">
                                    <div class="text04 table_name">表 2: 知识点——技能双向细目表</div>
                                    <div class="table_box">
                                        <table class="table2_02">
                                            <thead class="text05">
                                            <tr>
                                                <th class="th01">内容范畴</th>
                                                <th class="th02">知识点/技能</th>
                                                <th class="th03">理解</th>
                                                <th class="th03">运算能力</th>
                                                <th class="th03">运算能力</th>
                                                <th class="th03">运算能力</th>
                                                <th class="th03">运算能力</th>
                                                <th class="th03">运算能力</th>
                                                <th class="th03">运算能力</th>
                                                <th class="th03">运算能力</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr class="meg_line">
                                                <td rowspan="5" class="left_line td01">数与代数</td>
                                                <td class="td02">乘法分配律乘法分配律乘法分配</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr>
                                                <td class="td02">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr>
                                                <td class="td02">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr>
                                                <td class="td02">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr>
                                                <td class="td02">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr class="meg_line">
                                                <td rowspan="3" class="td01">图形与几何</td>
                                                <td class="td02">乘法分配律乘法分配律乘法分配</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr>
                                                <td class="td02">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr>
                                                <td class="td02">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr class="meg_line">
                                                <td rowspan="1" class="td01">统计与概率</td>
                                                <td class="td02">乘法分配律乘法分配律乘法分配</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                                <td class="td03">1</td>
                                            </tr>
                                            <tr class="t_footer">
                                                <td class="td01"></td>
                                                <td class="td02">合计</td>
                                                <td class="td03">0</td>
                                                <td class="td03"></td>
                                                <td class="td03">33</td>
                                                <td class="td03">1</td>
                                                <td class="td03"></td>
                                                <td class="td03">66</td>
                                                <td class="td03">1</td>
                                                <td class="td03">200</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="text04 table_del">注意试题注重考查学生对数学学科基
                                        本知识、技能的理试题注重考查学生对数学学科基本知识、
                                        技能的理试题注重考查学生对数学学科基本知识、技能的理
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <!--2.4 数据分析与反馈报告-->
                        <div class="download_middle">
                            <div class="text02 small_title">2.4 数据分析与反馈报告</div>
                            <ul class="del_box">
                                <li class="text04 del_txt">在施测之后，为了确保分析数据的质量，本次测试按照数据清理原则和严格的程序对数据进
                                    行了质量控制和数据清理。数据清理之后，分别进行了描述性统计和推论性统计，对于学生的能
                                    力估计采用了项目反应理论模型。
                                    本次测试采用项目反应理论及经典测验理论中的项目分析方法对测试工具进行分析; 通过项
                                    目反应理论，主要分析项目难度估计值 (Estimate) 等。采用经典测验理论，对每个题目进行初步选
                                    项分析，以及通过率和区分度分析。
                                    根据数据结果分别呈现五个类型的报告，区域报告 (教研员)，学校报告 (校长) 班级报告 (教师)
                                    及个体报告 (学生) 及测评工具质量分析报告。
                                </li>
                            </ul>
                        </div>
                    </div>
                    <!--3.学生整体情况-->
                    <div class="download_box">
                        <div class="text01 big_title">3.学生整体情况</div>
                        <div class="download_middle">
                            <ul class="del_box">
                                <li class="text04 del_txt">该部分呈现了此次测评学生成绩的总体分布，并从横向和纵向两个方向对学生的总体成绩进
                                    行了描述和对比。其中横向指的是跨区县之间的对比，而纵向指的是本区县和上一次测评结果的
                                    对比。通过多维度的比较，可以为教育决策部门针对性地进行教研活动、资源分配提供依据。
                                </li>
                            </ul>
                        </div>
                        <!--3.1 各地区四率与达标率-->
                        <div class="download_middle">
                            <div class="text02 small_title">3.1 各地区四率与达标率</div>
                            <ul class="del_box">
                                <li class="canvas3_01">
                                    <div class="canvas_box"></div>
                                    <div class="text04 canvas_name">图 1: 各地区的四率分布</div>
                                    <div class="text04 canvas_del">注: 红色虚线为全市平均掌握程度变式任务，可能同时考察一个或多个能力素养指标。
                                        当其中某些能力指标发展程度较低时，可能变式任务，可能同时考察一个或多个能力素养指标
                                        。当其中某些能力指标发展程度较低时，可能
                                    </div>
                                </li>
                                <li class="canvas3_02">
                                    <div class="canvas_box"></div>
                                    <div class="text04 canvas_name">图 2: 各地区的达标率</div>
                                    <div class="text04 canvas_del">
                                        注: 红色虚线为全市平均掌握程度变式任务，可能同时考察一个或多个能力素养指标。
                                        当其中某些能力指标发展程度较低时，可能变式任务，可能同时考察一个或多个能力素养指标
                                        。当其中某些能力指标发展程度较低时，可能
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <!--3.2 各地区平均得分与离散程度-->
                        <div class="download_middle">
                            <div class="text02 small_title">3.2 各地区平均得分与离散程度</div>
                            <ul class="del_box">
                                <li class="canvas3_03">
                                    <div class="canvas_box"></div>
                                    <div class="text04 canvas_name">图 3: 各地区得分与离散程度</div>
                                    <div class="text04 canvas_del">
                                        注: 图中彩色的箱子越狭长表示离散程度越大，即该区县学生间的差异越明显。彩色箱子中间
                                        黑色的竖线表示区县平均分，竖线越靠右说明改区县的平均成绩越高。
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <!--3.3 各地区跨次测评变化情况-->
                        <div class="download_middle">
                            <div class="text02 small_title">3.3 各地区跨次测评变化情况</div>
                            <ul class="del_box">
                                <li class="text04 del_txt">由于此次测评为第一次考试，因此未能根据历史数据做出跨次测评情况比较。
                                </li>
                            </ul>
                        </div>
                    </div>
                    <!--4 学生学科表现-->
                    <div class="download_box">
                        <div class="text01 big_title">4 学生学科表现</div>
                        <div class="download_middle">
                            <ul class="del_box">
                                <li class="text04 del_txt">通过呈现学科内知识点、技能、变式任务、能力素养间的关系，帮助教研员了解学生的总体
                                    学科表现，进而辅助教学。
                                </li>
                            </ul>
                        </div>
                        <!--4.1 知识点——技能的掌握程度-->
                        <div class="download_middle">
                            <div class="text02 small_title">4.1 知识点——技能的掌握程度</div>
                            <ul class="del_box">
                                <li class="text04 del_txt">知识点——技能掌握程度关系表（表 3），主要目标在于表明每一个知识点在不同技能维度下
                                    学生的掌握情况，以 0-5 之间的数值来表示，即 0 为未掌握，5 为完全掌握。同时，为了方便识别，
                                    我们增加了渐变颜色，颜色越深表示掌握程度越高。
                                </li>
                                <li class="table">
                                    <div class="text04 table_name">表 3: 知识点——技能的掌握程度</div>
                                    <div class="table_box">
                                        <table class="table4_01">
                                            <thead class="text05">
                                            <tr>
                                                <th class="th41">知识点/技能</th>
                                                <th class="th42">理解</th>
                                                <th class="th42">计算</th>
                                                <th class="th42">操作</th>
                                                <th class="th42">运用</th>
                                                <th class="th42">分析</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr>
                                                <td class="td41">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42 active">2</td>
                                                <td class="td42"></td>
                                            </tr>
                                            <tr>
                                                <td class="td41">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42">0</td>
                                            </tr>
                                            <tr>
                                                <td class="td41">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42">2</td>
                                            </tr>
                                            <tr>
                                                <td class="td41">乘法分配律乘法分配律乘法分配律</td>
                                                <td class="td42">2</td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                                <td class="td42"></td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <!--4.2 各地区的技能掌握程度-->
                        <div class="download_middle">
                            <div class="text02 small_title">4.2 各地区的技能掌握程度</div>
                            <ul class="del_box">
                                <li class="canvas4_01">
                                    <div class="canvas_box"></div>
                                    <div class="text04 canvas_name">图 4: 各地区的技能掌握程度</div>
                                    <div class="text04 canvas_del">注: 红色虚线为全市平均掌握程度。</div>
                                </li>
                            </ul>
                        </div>
                        <!--4.3 变式任务——能力素养的发展情况-->
                        <div class="download_middle">
                            <div class="text02 small_title">4.3 变式任务——能力素养的发展情况</div>
                            <ul class="del_box">
                                <li class="text04 del_txt">变式任务是学科知识及其任务的一种描述，包括了概念知识体系及其教学过程中的任务，也
                                    包括了考试题目中的任务，而且包含其在不同情景下迁移和应用的问题。所以是一个综合表达。以
                                    知识点圆的面积为例，如下表所示:
                                </li>
                                <li class="table">
                                    <div class="table_box">
                                        <table class="table4_02">
                                            <thead class="text05">
                                            <tr>
                                                <th class="th41">说明</th>
                                                <th class="th42">举例</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr>
                                                <td class="td41">概念知识体系及其教学过程中的任务</td>
                                                <td class="td42">探究圆的面积</td>
                                            </tr>
                                            <tr>
                                                <td class="td41">概念知识体系及其教学过程中的任务</td>
                                                <td class="td42">探究圆的面积</td>
                                            </tr>
                                            <tr>
                                                <td class="td41">概念知识体系及其教学过程中的任务</td>
                                                <td class="td42">探究圆的面积</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </li>
                                <li class="text04 del_txt">
                                    变式任务——能力素养关系表（表 4），在于阐明变式任务对于能力素养发展的作用。同一个
                                    变式任务，可能同时考察一个或多个能力素养指标。当其中某些能力指标发展程度较低时，可能
                                    导致该变式任务难以完成。我们以 0-5 之间的数值来表示，即 0 为未掌握，5 为完全掌握。同时，
                                    为了方便识别，我们增加了渐变颜色，颜色越深表示掌握程度越高。
                                </li>
                            </ul>
                            <ul class="del_box">
                                <li class="table">
                                    <div class="text04 table_name">表 4: 变式任务——能力素养的发展程度</div>
                                    <div class="table_box">
                                        <table class="table4_03">
                                            <thead class="text05">
                                            <tr>
                                                <th class="th4_31">说明</th>
                                                <th class="th4_32">数感</th>
                                                <th class="th4_32">运算能力</th>
                                                <th class="th4_32">运算能力</th>
                                                <th class="th4_32">运算能力</th>
                                                <th class="th4_32">运算能力</th>
                                                <th class="th4_32">运算能力</th>
                                                <th class="th4_32">运算能力</th>
                                                <th class="th4_32">运算能力</th>
                                                <th class="th4_32">运算能力</th>
                                            </tr>
                                            </thead>
                                            <tbody class="text04">
                                            <tr>
                                                <td class="td4_31">知平均数和部分统计数据求两个数</td>
                                                <td class="td4_32 active">3</td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                            </tr>
                                            <tr>
                                                <td class="td4_31">知平均数和部分统计数据求两个数</td>
                                                <td class="td4_32">3</td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                            </tr>
                                            <tr>
                                                <td class="td4_31">知平均数和部分统计数据求两个数</td>
                                                <td class="td4_32">3</td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                                <td class="td4_32"></td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <!--4.4 各地区能力素养发展程度-->
                        <div class="download_middle">
                            <div class="text02 small_title">4.4 各地区能力素养发展程度</div>
                            <ul class="del_box">
                                <li class="canvas4_02">
                                    <div class="text04 canvas_del">图 5 呈现的是本市级各区县学生在各能力素养指标的发展情况。</div>
                                    <div class="canvas_box"></div>
                                    <div class="text04 canvas_name">图 5: 各地区的能力素养发展情况</div>
                                    <div class="text04 canvas_name">注: 红色虚线为全市平均掌握程度。</div>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <!--5 评价总结与建议-->
                    <div class="download_box">
                        <div class="text01 big_title">5 评价总结与建议</div>
                        <div class="download_middle">
                            <ul class="del_box">
                                <li class="text04 del_txt">
                                    从本次测评结果中可以发现:
                                </li>
                                <li class="text04 del_txt">1. 各区县学生水平存在明显差异。达标率介于 51.3%-81.3%，其中西岗区的优秀率和达标率均最
                                    高。
                                </li>
                                <li class="text04 del_txt">2. 知识点——技能掌握层面，在解方程的计算技能以及认识统计折线图的分析技能上表现优秀;
                                    但在平均数的理解和分析技能上以及三角形内角和的分析技能上掌握不佳。
                                </li>
                                <li class="text04 del_txt">3. 变式任务——能力素养发展层面，学生具备看折线统计图提取信息的数据分析能力; 但在知
                                    图形中多个角度数求指定角度数的空间想象能力较为薄弱。
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "./footer.ftl">
</@layout.page>