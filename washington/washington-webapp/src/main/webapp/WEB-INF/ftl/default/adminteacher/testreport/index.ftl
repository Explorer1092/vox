<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
title="一起-测评报告"
pageJs=["generaloverview", "jquery"]
pageJsFile={"generaloverview": "public/script/adminteacher/testreport"}
pageCssFile={"index": ["/public/skin/adminteacher/css/testreport", "/public/skin/adminteacher/css/common"]}>

    <#include "../header.ftl">
    <div class="outercontainer" id="testreport">
        <div class="container">
            <!-- 主体 -->
            <div class="mainBox">
                <div class="mainInner">
                    <div class="topTitle"><a href="">首页</a> > <a href="">上传试卷</a></div>
                    <div class="contentBox">
                        <!-- 操作按钮 -->
                        <div class="operateBox">
                            <div class="unloadBtn" data-bind="click: downloadReport, style: {visibility: isShowDownloadBtn() ? 'visible' : 'hidden'}" style="visibility: hidden;">下载报告</div>
                        </div>
                        <!-- 主要内容 -->
                        <div class="downloadBox" id="downloadContent">
                            <div class="downloadInner">
                                <!-- 首页 -->
                                <div class="loadSection l-section01">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon01.png'/>" alt="" class="l-section01-img1">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon02.png'/>" alt="" class="l-section01-img2">
                                    <div class="fileBox">
                                        <div class="fileName" data-bind="text: examFullName, visible: examFullName()" style="display: none;"></div>
                                        <div class="fileText" data-bind="visible: examFullName()" style="display: none;">数据报告</div>
                                    </div>
                                    <div class="fileLogo"></div>
                                </div>
                                <!-- 目录 -->
                                <div class="loadSection l-section02">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon03.png'/>" alt="" class="l-section02-img1">
                                    <img src="<@app.link href='public/resource/adminteacher/images/download-icon04.png'/>" alt="" class="l-section02-img2">
                                    <div class="catalogTitle">
                                        <div class="chinese">目录</div>
                                        <div class="english">Catalogue</div>
                                    </div>
                                    <ul class="catalogList">
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line"></span>
                                            <span class="title">前言</span>
                                        </li>
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line line02"></span>
                                            <span class="title">1.参与概况</span>
                                        </li>
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line line03"></span>
                                            <span class="title">2.得分状况</span>
                                        </li>
                                        <li>
                                            <span class="dot"></span>
                                            <span class="line line04"></span>
                                            <span class="title">3.学业水平</span>
                                        </li>
                                        <li data-bind="visible: !subjectAbilityEmptyFlag()" style="display: none;">
                                            <span class="dot"></span>
                                            <span class="line line05"></span>
                                            <span class="title">4.学科能力养成</span>
                                        </li>
                                        <li data-bind="visible: !knowledgePlateEmptyFlag()" style="display: none;">
                                            <span class="dot"></span>
                                            <span class="line line06"></span>
                                            <span class="title" data-bind="if: subjectAbilityEmptyFlag()">4.知识板块掌握度</span>
                                            <span class="title" data-bind="ifnot: subjectAbilityEmptyFlag()">5.知识板块掌握度</span>
                                        </li>
                                    </ul>
                                </div>
                                <!-- 前言 page2 -->
                                <div class="loadSection">
                                    <div class="prefaceTitle">前言</div>
                                    <div class="prefaceText">
                                        <p>一起教育科技是全球领先的K12智能教育平台，怀着“让学习成为美好体验”的使命，一起教育科技致力于用前沿的教育科技、优质的教育内容和持续的教育热情，为K12阶段的学校、家庭、社会教育场景，提供更为高效、美好的产品和体验。经过六年的沉淀与发展，如今在全国，已有31个省市、近12万所学校、超过6000万的用户乐于使用我们的平台，开启了智能教育的全新体验。面对未来，一起教育科技将努力让知识和能力一起，构建学生核心素养；让乡镇和城市一起，共享优质教育资源；让科技和教育一起，实现学习美好体验。</p>
                                        <p>一起教育科技的在线测评系统，突破了传统测评活动的各种限制，为学生提供全面、科学的测评内容。学生在线完成测评，系统自动批改，天枢数据平台实时采集批改数据，从正确率、能力、知识板块等多个维度分析区域、学校、班级的测评情况，配合专业的分析解读和教学建议，最终生成具有可操作性的分析报告，帮助教育者实现高效教研、精准教学。</p>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>


                                <!-- 1.参与概况 page3 -->
                                <div class="loadSection l-section03">
                                    <div class="prefaceTitle">1.参与概况</div>
                                    <ul class="surveyBox" data-bind="visible: !examSurveyEmptyFlag()" style="display: none;">
                                        <li data-bind="visible: examRegionLevel" style="display: none;">
                                            <span class="s-title">测评范围：</span>
                                            <span class="blueTxt" data-bind="text: examRegionLevel"></span>
                                        </li>
                                        <li data-bind="visible: gradeName" style="display: none;">
                                            <span class="s-title">年级：</span>
                                            <span class="blueTxt" data-bind="text: gradeName"></span>
                                        </li>
                                        <li data-bind="visible: subject" style="display: none;">
                                            <span class="s-title">科目：</span>
                                            <span class="blueTxt" data-bind="text: subject"></span>
                                        </li>
                                        <li data-bind="visible: examTime" style="display: none;">
                                            <span class="s-title">测评时间：</span>
                                            <span class="blueTxt" data-bind="text: examTime"></span>
                                        </li>
                                        <li>
                                            <span class="s-title">答卷时长：</span>
                                            <span class="blueTxt"><!-- ko text: durationMinutes --><!-- /ko --> 分钟</span>
                                        </li>
                                        <li data-bind="visible: dataLevel() === 'city'" style="display: none;">
                                            <span class="s-title">实际参与区域数：</span>
                                            <span class="blueTxt"><!-- ko text: examSurvey().actualjoinregions --><!-- /ko --> 个</span>
                                        </li>
                                        <li data-bind="visible: dataLevel() === 'city' || dataLevel() === 'country'" style="display: none;">
                                            <span class="s-title">实际参与学校数：</span>
                                            <span class="blueTxt"><!-- ko text: examSurvey().actualjoinschools --><!-- /ko --> 所</span>
                                        </li>
                                        <li>
                                            <span class="s-title">实际参与班级数：</span>
                                            <span class="blueTxt"><!-- ko text: examSurvey().actualjoinclazzs --><!-- /ko --> 个</span>
                                        </li>
                                        <li>
                                            <span class="s-title">实际参与学生数：</span>
                                            <span class="blueTxt"><!-- ko text: examSurvey().actualjoinstudents --><!-- /ko --> 人</span>
                                        </li>
                                    </ul>
                                    <p class="noData" data-bind="visible: examSurveyEmptyFlag()" style="display: none;">暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 参与概况--表格 page4 -->
                                <div class="loadSection" data-bind="visible: !examSurveyEmptyFlag()">
                                    <div class="prefaceTable">
                                        <table>
                                            <thead data-bind="visible: viewRegionLevel" style="display: none;">
                                            <tr>
                                                <th>序号</th>
                                                <th data-bind="text: viewRegionLevel"></th>
                                                <th data-bind="visible: dataLevel() === 'city'">实际参与学校数</th>
                                                <th data-bind="visible: dataLevel() === 'city' || dataLevel() === 'country'">实际参与班级数</th>
                                                <th>实际参与学生数</th>
                                            </tr>
                                            </thead>
                                            <tbody data-bind="visible: examSurveyDetail().length" style="display: none;">
                                            <!-- ko foreach: examSurveyDetail -->
                                            <tr>
                                                <td data-bind="text: orderNum"></td>
                                                <td data-bind="text: name"></td>
                                                <td data-bind="text: actualjoinschools, visible: $root.dataLevel() === 'city'"></td>
                                                <td data-bind="text: actualjoinclazzs, visible: $root.dataLevel() === 'city' || $root.dataLevel() === 'country'"></td>
                                                <td data-bind="text: actualjoinstudents"></td>
                                            </tr>
                                            <!-- /ko -->
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>


                                <!--2.得分状况 page5 -->
                                <div class="loadSection l-section04">
                                    <div class="prefaceTitle">2.得分状况</div>
                                    <!-- canvas -->
                                    <div class="canvasBox examScoreStateChart1" data-bind="visible: !examScoreEmptyFlag()"></div>
                                    <div class="tipsBox" data-bind="visible: !examScoreEmptyFlag()">
                                        <div class="tipsText">共<span class="blueTxt" data-bind="text: examScoreWholeScoreDetail().length"></span>个<span class="blueTxt" data-bind="text: viewRegionLevel"></span>参与本次测评，整体得分率为<span class="blueTxt" data-bind="text: (examScoreWholeScore().wholeScoreRate || 0) + '%'"></span></div>
                                        <div class="noteText margin-t">
                                            <p>注：</p>
                                            <p>得分率＝得分／总分</p>
                                        </div>
                                    </div>
                                    <p class="noData" data-bind="visible: examScoreEmptyFlag()" style="display: none;">暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 得分状况--表格 page6 -->
                                <div class="loadSection" data-bind="visible: !examScoreEmptyFlag()">
                                    <div class="prefaceTable">
                                        <table>
                                            <thead data-bind="visible: viewRegionLevel" style="display: none;">
                                            <tr>
                                                <th>序号</th>
                                                <th data-bind="text: viewRegionLevel"></th>
                                                <th>总分平均分</th>
                                                <th>得分率</th>
                                            </tr>
                                            </thead>
                                            <tbody data-bind="visible: examScoreWholeScoreDetail().length" style="display: none;">
                                            <!-- ko foreach: examScoreWholeScoreDetail -->
                                            <tr>
                                                <td data-bind="text: orderNum"></td>
                                                <td data-bind="text: name"></td>
                                                <td data-bind="text: averagescore"></td>
                                                <td data-bind="text: scorerate + '%', css: {'active': topScoreRate == 1}"></td>
                                            </tr>
                                            <!-- /ko -->
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="tipsBox">
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>蓝色色块为该列最高值</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 得分率 page7 -->
                                <div class="loadSection" data-bind="visible: !examScoreEmptyFlag()">
                                    <div class="smallTitle margin-b">得分率</div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02 examScoreStateChart2"></div>
                                    <div class="tipsBox">
                                        <div class="noteText">
                                            <p data-bind="if: examScoreTopThreeData().length > 1">
                                                1.表现较好的<span class="blueTxt"><!-- ko text: viewRegionLevel --><!-- /ko --></span>依次是<!-- ko foreach: examScoreTopThreeData--><span class="blueTxt"><!-- ko text: name--><!-- /ko -->（<!-- ko text: scorerate--><!-- /ko -->%）<!-- ko if: $index() !== ($root.examScoreTopThreeData().length - 1)-->、<!-- /ko --></span><!-- /ko -->
                                            </p>
                                            <p data-bind="if: examScoreTopThreeData().length === 1">1.表现较好的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>是<span class="blueTxt"><!-- ko text: (examScoreTopThreeData()[0] || {}).name--><!-- /ko -->（<!-- ko text: (examScoreTopThreeData()[0] || {}).scorerate--><!-- /ko -->%）</span></p>
                                            <p data-bind="if: examScoreLastData().name">2.表现一般的<span class="redTxt" data-bind="text: viewRegionLevel"></span>是<span class="redTxt"><!-- ko text: examScoreLastData().name--><!-- /ko -->（<!-- ko text: examScoreLastData().scorerate--><!-- /ko -->%）</span></p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 得分离散程度 page8 -->
                                <div class="loadSection l-section05" data-bind="visible: !examScoreEmptyFlag()">
                                    <div class="smallTitle margin-b">得分离散程度</div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox03 examScoreStateChart3"></div>
                                    <div class="tipsBox">
                                        <div class="noteText">
                                            <p data-bind="visible: examScoreScatterPointMinMap().name" style="display: none">1.学生成绩差异<span class="blueTxt">较小</span>的是<span class="blueTxt" data-bind="text: examScoreScatterPointMinMap().name"></span>，且其得分率排第<span class="blueTxt" data-bind="text: examScoreScatterPointMinMap().scoreRateLevel"></span></p>
                                            <p data-bind="visible: examScoreScatterPointMaxMap().name" style="display: none">2.学生成绩差异<span class="redTxt">较大</span>的是<span class="redTxt" data-bind="text: examScoreScatterPointMaxMap().name"></span>，且其得分率排第<span class="redTxt" data-bind="text: examScoreScatterPointMaxMap().scoreRateLevel"></span></p>

                                            <p data-bind="visible: examScoreCoefficient() < -0.4" style="display: none"><!-- ko if: examScoreScatterPointMinMap().name && examScoreScatterPointMaxMap().name -->3.<!-- /ko -->标准差与平均分的相关系数为<span class="blueTxt" data-bind="text: examScoreCoefficient">0</span>，反映成绩越好的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>，学生间的成绩差异<span class="blueTxt">越小</span></p>
                                            <p data-bind="visible: examScoreCoefficient() > -0.4 && examScoreCoefficient() < 0.4" style="display: none"><!-- ko if: examScoreScatterPointMinMap().name && examScoreScatterPointMaxMap().name -->3.<!-- /ko -->标准差与平均分的相关系数为<span class="blueTxt" data-bind="text: examScoreCoefficient">0</span>，反映各<span class="blueTxt" data-bind="text: viewRegionLevel"></span>成绩高低与学生间的成绩差异大小没有相关性</p>
                                            <p data-bind="visible: examScoreCoefficient() > 0.4" style="display: none"><!-- ko if: examScoreScatterPointMinMap().name && examScoreScatterPointMaxMap().name -->3.<!-- /ko -->标准差与平均分的相关系数为<span class="blueTxt" data-bind="text: examScoreCoefficient">0</span>，反映成绩越好的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>，学生间的成绩差异<span class="blueTxt">越大</span></p>
                                        </div>
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>1.根据各<span data-bind="text: viewRegionLevel"></span>的平均分和标准差构成的多个坐标点，形成散点图（一个散点代表一个<span data-bind="text: viewRegionLevel"></span>）,观察坐标点分布，可分析各<span data-bind="text: viewRegionLevel"></span>成绩高低与学生间成绩差异程度的相关性。</p>
                                            <p>2.总分标准差越大，学生间的成绩差异越大。</p>
                                            <p>3.处于第四象限的<span data-bind="text: viewRegionLevel"></span>，成绩较好且学生间的成绩差异小，处于第二象限的<span data-bind="text: viewRegionLevel"></span>，成绩较差且学生间的成绩差异大。</p>
                                            <p>4.相关系数<-0.4，表示成绩越好的<span data-bind="text: viewRegionLevel"></span>，学生间的成绩差异越小；相关系数>0.4，表示成绩越好的<span data-bind="text: viewRegionLevel"></span>，学生间的成绩差异越大 ；-0.4≤相关系数≤0.4，表示各<span data-bind="text: viewRegionLevel"></span>成绩高低与学生间的成绩差异大小没有相关性。</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>


                                <!-- 3.学业水平 page9 -->
                                <div class="loadSection l-section06">
                                    <div class="prefaceTitle">3.学业水平</div>
                                    <div class="tableBox clearfix" data-bind="visible: !studyLevelEmptyFlag()">
                                        <!-- 左侧表格 -->
                                        <div class="leftTable">
                                            <table data-bind="visible: studyLevelInfo().length" style="display: none;">
                                                <thead>
                                                <tr>
                                                    <th>等级</th>
                                                    <th>人数</th>
                                                    <th>比例</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <!-- ko foreach: studyLevelInfo -->
                                                <tr>
                                                    <td data-bind="text: levelCname"></td>
                                                    <td data-bind="text: studentnum"></td>
                                                    <td data-bind="text: levelrate + '%'"></td>
                                                </tr>
                                                <!-- /ko -->
                                                <tr class="">
                                                    <td rowspan="2" colspan="3" class="blueTxt">整体合格率：<!-- ko text: studyLevelWholeQualifiledRatio --><!-- /ko -->%</td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                        <!-- canvas图表 -->
                                        <div class="rightTable canvasBox studyLevelChart1"></div>
                                    </div>
                                    <div class="tipsBox" data-bind="visible: !studyLevelEmptyFlag()">
                                        <div class="noteText">
                                            <p>1.整体合格率达到<span class="blueTxt"><!-- ko text: studyLevelWholeQualifiledRatio --><!-- /ko -->%</span></p>
                                            <p>2.整体优秀率达到<span class="blueTxt"><!-- ko text: (studyLevelInfo()[0] || {}).levelrate --><!-- /ko -->%</span></p>
                                        </div>
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>1.等级定义：①优秀：得分率≥85%；②良好：75%≤得分率<85%；③ 合格：60%≤得分率<75%；④待合格：得分率<60％； </p>
                                            <p>2.合格率=（优秀人数+良好人数+合格人数）/总人数</p>
                                        </div>
                                    </div>
                                    <p class="noData" data-bind="visible: studyLevelEmptyFlag()" style="display: none;">暂无数据</p>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 学业水平-表格 page10 -->
                                <div class="loadSection" data-bind="visible: !studyLevelEmptyFlag()">
                                    <div class="prefaceTable">
                                        <table data-bind="visible: studyLevelGridDataList().length" style="display: none;">
                                            <thead>
                                            <tr>
                                                <th>序号</th>
                                                <th data-bind="text: viewRegionLevel"></th>
                                                <th>优秀人数比例</th>
                                                <th>良好人数比例</th>
                                                <th>合格人数比例</th>
                                                <th>待合格人数比例</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <!-- ko foreach: studyLevelGridDataList -->
                                            <tr>
                                                <td data-bind="text: orderNum"></td>
                                                <td data-bind="text: name"></td>
                                                <td data-bind="text: excellentStuRatio + '%', css: {'active': topExcellentStuRatio == 1}"></td>
                                                <td data-bind="text: goodStuRatio + '%', css: {'active': topGoodStuRatio == 1}"></td>
                                                <td data-bind="text: qualifiledStuRatio + '%', css: {'active': topQualifiledStuRatio == 1}"></td>
                                                <td data-bind="text: unqulifiledStuRatio + '%', css: {'active': topUnqulifiledStuRatio == 1}"></td>
                                            </tr>
                                            <!-- /ko -->
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="tipsBox">
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>蓝色色块为该列最高值</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 纯canvas page11 -->
                                <div class="loadSection" data-bind="visible: !studyLevelEmptyFlag()">
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02 studyLevelChart2"></div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 优秀率 page12 -->
                                <div class="loadSection l-section04" data-bind="visible: !studyLevelEmptyFlag()">
                                    <div class="smallTitle margin-b">优秀率</div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02 studyLevelChart3"></div>
                                    <div class="tipsBox" data-bind="visible: studyLevelExcellentTopThreeData().length" style="display: none;">
                                        <div class="noteText">
                                            <p data-bind="if: studyLevelExcellentTopThreeData().length > 1">表现较好的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>依次为<!-- ko foreach: studyLevelExcellentTopThreeData --><span class="blueTxt"><!-- ko text: name--><!-- /ko -->（<!-- ko text: excellentStuRatio--><!-- /ko -->%）<!-- ko if: $index() !== ($root.studyLevelExcellentTopThreeData().length - 1)-->、<!-- /ko --></span><!-- /ko --></p>
                                            <p data-bind="if: studyLevelExcellentTopThreeData().length === 1">表现较好的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>为<span class="blueTxt"><!-- ko text: (studyLevelExcellentTopThreeData()[0] || {}).name--><!-- /ko -->（<!-- ko text: (studyLevelExcellentTopThreeData()[0] || {}).excellentStuRatio--><!-- /ko -->%）</span></p>
                                        </div>
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>优秀率＝优秀人数／总人数</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 良好率 page13 -->
                                <div class="loadSection l-section04" data-bind="visible: !studyLevelEmptyFlag()">
                                    <div class="smallTitle margin-b">良好率</div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02 studyLevelChart4"></div>
                                    <div class="tipsBox" data-bind="visible: studyLevelGoodTopThreeData().length" style="display: none;">
                                        <div class="noteText">
                                            <p data-bind="if: studyLevelGoodTopThreeData().length > 1">表现较好的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>依次为<!-- ko foreach: studyLevelGoodTopThreeData --><span class="blueTxt"><!-- ko text: name--><!-- /ko -->（<!-- ko text: excellentgoodRatio--><!-- /ko -->%）<!-- ko if: $index() !== ($root.studyLevelGoodTopThreeData().length - 1)-->、<!-- /ko --></span><!-- /ko --></p>
                                            <p data-bind="if: studyLevelGoodTopThreeData().length === 1">表现较好的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>为<span class="blueTxt"><!-- ko text: (studyLevelGoodTopThreeData()[0] || {}).name--><!-- /ko -->（<!-- ko text: (studyLevelGoodTopThreeData()[0] || {}).excellentStuRatio--><!-- /ko -->%）</span></p>
                                        </div>
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>良好率＝（优秀人数＋良好人数）／总人数</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 合格率 page14 -->
                                <div class="loadSection l-section06" data-bind="visible: !studyLevelEmptyFlag()">
                                    <div class="smallTitle margin-b">合格率</div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02 studyLevelChart5"></div>
                                    <div class="tipsBox">
                                        <div class="noteText">
                                            <p data-bind="if: studyLevelQulifiledTopThreeData().length > 1">1.合格率较高的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>依次为<!-- ko foreach: studyLevelQulifiledTopThreeData --><span class="blueTxt"><!-- ko text: name--><!-- /ko -->（<!-- ko text: excellentgoodqualifiledRatio--><!-- /ko -->%）<!-- ko if: $index() !== ($root.studyLevelQulifiledTopThreeData().length - 1)-->、<!-- /ko --></span><!-- /ko --></p>
                                            <p data-bind="if: studyLevelQulifiledTopThreeData().length === 1">1.合格率较高的<span class="blueTxt" data-bind="text: viewRegionLevel"></span>为<span class="blueTxt"><!-- ko text: (studyLevelQulifiledTopThreeData()[0] || {}).name--><!-- /ko -->（<!-- ko text: (studyLevelQulifiledTopThreeData()[0] || {}).excellentStuRatio--><!-- /ko -->%）</span></p>
                                            <p data-bind="if: studyLevelQulifiledLastData().name">2.合格率较低的<span class="redTxt" data-bind="text: viewRegionLevel"></span>是<span class="redTxt"><!-- ko text: studyLevelQulifiledLastData().name--><!-- /ko -->（<!-- ko text: studyLevelQulifiledLastData().excellentgoodqualifiledRatio --><!-- /ko -->%）</span>、低于平均水平<span class="redTxt" data-bind="text: studyLevelQulifiledLastDataDiff() + '%'"></span>，建议多进行不同<span class="redTxt" data-bind="text: viewRegionLevel"></span>间的教学、教研交流互动。</p>
                                        </div>
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>合格率＝（优秀人数＋良好人数＋合格人数）／总人数</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 待合格率 page15 -->
                                <div class="loadSection" data-bind="visible: !studyLevelEmptyFlag()">
                                    <div class="smallTitle margin-b">待合格率</div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02 studyLevelChart6"></div>
                                    <div class="tipsBox">
                                        <div class="smallText">
                                            <p>注：</p>
                                            <p>待合格率＝待合格人数／总人数</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>


                                <!-- 4.学科能力养成 page16 -->
                                <!-- ko if: !subjectAbilityEmptyFlag() -->
                                <div class="loadSection l-section06">
                                    <div class="prefaceTitle">4.学科能力养成</div>
                                    <div class="tableBox clearfix">
                                        <!-- 左侧表格 -->
                                        <div class="leftTable">
                                            <table data-bind="visible: subjectAbilityInfo().length" style="display: none;">
                                                <thead>
                                                <tr>
                                                    <th>能力</th>
                                                    <th>平均分</th>
                                                    <#--<th>总分</th>-->
                                                    <th>得分率</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <!-- ko foreach: subjectAbilityInfo -->
                                                <tr>
                                                    <td data-bind="text: subjectability"></td>
                                                    <td data-bind="text: averagescore"></td>
                                                    <#--<td data-bind="text: totalscore"></td>-->
                                                    <td data-bind="text: scorerate + '%'"></td>
                                                </tr>
                                                <!-- /ko -->
                                                </tbody>
                                            </table>
                                        </div>
                                        <!-- canvas图表 -->
                                        <div class="rightTable canvasBox subjectAbilityChart1" style="width:400px;"></div>
                                    </div>
                                    <div class="tipsBox">
                                        <div class="noteText" data-bind="visible: subjectAbilityInfo().length >= 2" style="display: none;">
                                            <p data-bind="visible: subjectAbilityMax().subjectability">1.表现较好的能力是<span class="blueTxt" data-bind="text: '“' + subjectAbilityMax().subjectability + '”'"></span>，得分率为<span class="blueTxt" data-bind="text: subjectAbilityMax().scorerate + '%'"></span>，掌握程度<span class="blueTxt" data-bind="text: subjectAbilityMax().rateLevelName"></span></p>
                                            <p data-bind="visible: subjectAbilityMin().subjectability">2.表现一般的能力是<span class="redTxt" data-bind="text: '“' + subjectAbilityMin().subjectability + '”'"></span>，得分率为<span class="redTxt" data-bind="text: subjectAbilityMin().scorerate + '%'"></span>，掌握程度<span class="redTxt" data-bind="text: subjectAbilityMin().rateLevelName"></span></p>
                                        </div>
                                        <div class="smallText margin-t" data-bind="visible: subject" style="display: none">
                                            <p>注：</p>
                                            <p data-bind="if: subject() === '英语'">1.英语学科能力：听、说、读、写。</p>
                                            <p data-bind="if: subject() === '数学'">1.数学学科能力：数感、符号意识、空间观念、几何直观、数据分析观念、运算能力、推理能力、模型思想。</p>
                                            <p data-bind="if: subject() === '语文'">1.语文学科能力：拼音知识运用、字词积累与运用、句子积累与运用、篇章与文常积累、阅读能力、习作（写话）能力，实践探究能力、口语交际能力。</p>
                                            <p>2.掌握程度：①优秀：得分率≥85%；②良好：75%≤得分率<85%；③ 合格：60%≤得分率<75%；④待提升：得分率<60％</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 学科能力养成-表格 page17 -->
                                <div class="loadSection">
                                    <div class="prefaceTable">
                                        <table class="subjectTable">
                                            <thead>
                                            <tr class="firstTr">
                                                <th rowspan="2" class="b-bottom">序号</th>
                                                <th rowspan="2" class="b-bottom" data-bind="text: viewRegionLevel"></th>
                                                <!-- ko foreach: subjectAbilityGrid().gridHead -->
                                                <th colspan="2" data-bind="text: $data"></th>
                                                <!-- /ko -->
                                            </tr>
                                            <tr>
                                                <!-- ko foreach: subjectAbilityGrid().gridHead -->
                                                <th>平均分</th>
                                                <th>得分率</th>
                                                <!-- /ko -->
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <!-- ko foreach: { data: subjectAbilityGrid().gridData, as: 'subjectAbility' } -->
                                            <tr>
                                                <td data-bind="text: subjectAbility.orderNo"></td>
                                                <td data-bind="text: subjectAbility.name"></td>
                                                <!-- ko foreach: { data: abilityList, as: 'ability' } -->
                                                <td data-bind="text: ability.averagescore"></td>
                                                <td data-bind="text: ability.scorerate + '%', css: {'active': ability.topScorerate === 1}"></td>
                                                <!-- /ko -->
                                            </tr>
                                            <!-- /ko -->
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="tipsBox">
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>蓝色色块为该列最高值</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 能力-听(说、读、写等) page18 -->
                                <!-- ko foreach: {data: subjectAbilityMapList, as: 'subjectAbilityMap'} -->
                                <div class="loadSection">
                                    <div class="smallTitle margin-b" data-bind="text: subjectAbilityMap.legendData"></div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02" data-bind="css: 'subjectAbilityChart' + ($index() + 2)"></div>
                                    <div class="tipsBox">
                                        <div class="noteText">
                                            <p data-bind="if: subjectAbilityMap.topThree.length > 1">1.表现较好的<span class="blueTxt" data-bind="text: $root.viewRegionLevel"></span>依次是<!-- ko foreach: {data: topThree, as: 'tp'} --><span class="blueTxt"><!-- ko text: tp.name--><!-- /ko -->（<!-- ko text: tp.scorerate--><!-- /ko -->%）<!-- ko if: $index() !== (subjectAbilityMap.topThree.length - 1)-->、<!-- /ko --></span><!-- /ko --></p>
                                            <p data-bind="if: subjectAbilityMap.topThree.length === 1">1.表现较好的<span class="blueTxt" data-bind="text: $root.viewRegionLevel"></span>是<span class="blueTxt"> <!-- ko text: subjectAbilityMap.topThree[0].name--><!-- /ko -->（<!-- ko text: subjectAbilityMap.topThree[0].scorerate--><!-- /ko -->%）</span></p>
                                            <p data-bind="if: subjectAbilityMap.lastOne.name">2.表现一般的<span class="redTxt" data-bind="text: $root.viewRegionLevel"></span>是<span class="redTxt"><!-- ko text: subjectAbilityMap.lastOne.name --><!-- /ko -->（<!-- ko text: subjectAbilityMap.lastOne.scorerate --><!-- /ko -->%）</span>，得分率低于平均水平<span class="redTxt" data-bind="text: subjectAbilityMap.diff + '%'"></span>，建议多做相关练习以提升能力</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- /ko -->
                                <!-- /ko -->


                                <!-- 5.知识板块掌握度 page22 -->
                                <!-- ko if: !knowledgePlateEmptyFlag() -->
                                <div class="loadSection l-section06">
                                    <div class="prefaceTitle" data-bind="if: subjectAbilityEmptyFlag()">4.知识板块掌握度</div>
                                    <div class="prefaceTitle" data-bind="ifnot: subjectAbilityEmptyFlag()">5.知识板块掌握度</div>
                                    <div class="tableBox clearfix">
                                        <!-- 左侧表格 -->
                                        <div class="leftTable">
                                            <table data-bind="visible: knowledgePlateInfo().length" style="display: none;">
                                                <thead>
                                                <tr>
                                                    <th>知识板块</th>
                                                    <th>平均分</th>
                                                    <#--<th>总分</th>-->
                                                    <th>得分率</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <!-- ko foreach: knowledgePlateInfo -->
                                                <tr>
                                                    <td data-bind="text: knowledgeplate"></td>
                                                    <td data-bind="text: averagescore"></td>
                                                    <#--<td data-bind="text: totalscore"></td>-->
                                                    <td data-bind="text: scorerate + '%'"></td>
                                                </tr>
                                                <!-- /ko -->
                                                </tbody>
                                            </table>
                                        </div>
                                        <!-- canvas图表 -->
                                        <div class="rightTable canvasBox knowledgePlateChart1" style="width:400px;"></div>
                                    </div>
                                    <div class="tipsBox">
                                        <div class="noteText" data-bind="visible: knowledgePlateInfo().length >= 2" style="display: none;">
                                            <p data-bind="visible: knowledgePlateMax().knowledgeplate">1.表现较好的知识板块是<span class="blueTxt" data-bind="text: '“' + knowledgePlateMax().knowledgeplate + '”'"></span>，得分率为<span class="blueTxt" data-bind="text: knowledgePlateMax().scorerate + '%'"></span>，掌握程度<span class="blueTxt" data-bind="text: knowledgePlateMax().rateLevelName"></span></p>
                                            <p data-bind="visible: knowledgePlateMin().knowledgeplate">2.表现一般的知识板块是<span class="redTxt" data-bind="text: '“' + knowledgePlateMin().knowledgeplate + '”'"></span>，得分率为<span class="redTxt" data-bind="text: knowledgePlateMin().scorerate + '%'"></span>，掌握程度<span class="redTxt" data-bind="text: knowledgePlateMin().rateLevelName"></span></p>
                                        </div>
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p data-bind="if: subject() === '英语'">1.英语学科知识板块：字母、语音、单词短语、句式语法、话题功能。</p>
                                            <p data-bind="if: subject() === '数学'">1.数学学科知识板块：数与代数、图形与几何、统计与概率、综合与实践。</p>
                                            <p>2.掌握程度：①优秀：得分率≥85%；②良好：75%≤得分率<85%；③ 合格：60%≤得分率<75%；④待提升：得分率<60％</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- 知识板块掌握度-表格 page23 -->
                                <div class="loadSection">
                                    <div class="prefaceTable">
                                        <table class="subjectTable knowledgeTable">
                                            <thead>
                                            <tr class="firstTr">
                                                <th rowspan="2" class="b-bottom">序号</th>
                                                <th rowspan="2" class="b-bottom" data-bind="text: viewRegionLevel"></th>
                                                <!-- ko foreach: knowledgePlateGrid().gridHead -->
                                                <th colspan="2" data-bind="text: $data"></th>
                                                <!-- /ko -->
                                            </tr>
                                            <tr>
                                                <!-- ko foreach: knowledgePlateGrid().gridHead -->
                                                <th>平均分</th>
                                                <th>得分率</th>
                                                <!-- /ko -->
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <!-- ko foreach: { data: knowledgePlateGrid().gridData, as: 'knowledgePlate' } -->
                                            <tr>
                                                <td data-bind="text: knowledgePlate.orderNo"></td>
                                                <td data-bind="text: knowledgePlate.name"></td>
                                                <!-- ko foreach: { data: knowledgePlateList, as: 'knowledge' } -->
                                                <td data-bind="text: knowledge.averagescore"></td>
                                                <td data-bind="text: knowledge.scorerate + '%', css: {'active': knowledge.topScorerate === 1}"></td>
                                                <!-- /ko -->
                                            </tr>
                                            <!-- /ko -->
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="tipsBox">
                                        <div class="smallText margin-t">
                                            <p>注：</p>
                                            <p>蓝色色块为该列最高值</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num">23</span>
                                    </div>
                                </div>
                                <!-- 知识板块－字母(语音、单词短语、句式语法、话题功能等) page24 -->
                                <!-- ko foreach: {data: knowledgePlateMapList, as: 'knowledgePlateMap'} -->
                                <div class="loadSection">
                                    <div class="smallTitle margin-b" data-bind="text: knowledgePlateMap.legendData"></div>
                                    <!-- canvas -->
                                    <div class="canvasBox canvasBox02" data-bind="css: 'knowledgePlateChart' + ($index() + 2)"></div>
                                    <div class="tipsBox">
                                        <div class="noteText">
                                            <p data-bind="if: knowledgePlateMap.topThree.length > 1">1.表现较好的<span class="blueTxt" data-bind="text: $root.viewRegionLevel"></span>依次是<!-- ko foreach: {data: topThree, as: 'tp'} --><span class="blueTxt"><!-- ko text: tp.name--><!-- /ko -->（<!-- ko text: tp.scorerate--><!-- /ko -->%）<!-- ko if: $index() !== (knowledgePlateMap.topThree.length - 1)-->、<!-- /ko --></span><!-- /ko --></p>
                                            <p data-bind="if: knowledgePlateMap.topThree.length === 1">1.表现较好的<span class="blueTxt" data-bind="text: $root.viewRegionLevel"></span>是<span class="blueTxt"><!-- ko text: knowledgePlateMap.topThree[0].name--><!-- /ko -->（<!-- ko text: knowledgePlateMap.topThree[0].scorerate--><!-- /ko -->%）</span></p>
                                            <p data-bind="if: knowledgePlateMap.lastOne.name">2.表现一般的<span class="redTxt" data-bind="text: $root.viewRegionLevel"></span>是<span class="redTxt"><!-- ko text: knowledgePlateMap.lastOne.name --><!-- /ko -->（<!-- ko text: knowledgePlateMap.lastOne.scorerate --><!-- /ko -->%）</span>，得分率低于平均水平<span class="redTxt" data-bind="text: knowledgePlateMap.diff + '%'"></span>，建议多做相关练习以提升能力</p>
                                        </div>
                                    </div>
                                    <div class="pageNum">
                                        <i class="pageLogo"></i>
                                        <span class="num"></span>
                                    </div>
                                </div>
                                <!-- /ko -->
                                <!-- /ko -->


                                <!-- 结束页 -->
                                <div class="loadSection">
                                    <div class="endLogo"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="totop-btn" id="gotoTop" style="display: none"></div>
        </div>
    </div>

    <#include "./template.ftl">

    <script>
        var idType = "${idType!'schoolmaster'}";
        var echartImg1 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAcCAYAAAFUntxhAAAAAXNSR0IArs4c6QAAAnFJREFUWAntV89rE1EQnnnZZCto0yJW8FwP4i/YxEKKrXhUPBRBi7pJ9CAeaq8FT+JF0av2IJ5si5IeRFDiTWlrWqxtDip6sGexijQpomy7+8b3bVz9G1LeQDJv9tu3M/Md3jePyNp/Bg7Vij2IvLni7djXSqvwuVr5XRzPlC/snz6T6f086nozpWE82zKWrxW/opncbPkmfN+b0s6+l/5hrA0hz+BztWKzFftVeGvtzAB7s8V5drgggd5bPz61kl+8KBJqWu6f4AMzw/vcdMdHifR4fWDyqjfrLyrXOdL4saNj5eTdIL90SfSvqFkfnOjyXvtniVVFsT6/1D/12Lzb4HQqu1x4yO1MkK3dMrC1GPDm/GqiXFAxHOhQNHTpGXVLDnrEUL1EACAG3l81jDEjEolKIoZ4JOqp2EmdINb3AXR9XzstxJ3uHncIMbGMq23OKSx7q6OuOX4Ohg49QBxuyA1m7sHRg5gcuabc1BiW3iu/V5h3OxE9RWzNMmAZsAxYBiwDloEWA615Pq0KJEJmjm9SRHnM9YDz8/45cpxHelMTm6lctB6uH52aBgbBzbC7YMQ4yxlF+ncYz/zAINKd29fmMPsj1kH4dv1n9wDuAIjNFHDPCPqIbGiTU5obEhQ+HKt8ijFzL2ClKqYcUmllFD6M7wcxZgSdUrTEjsqiINnUC/XByX5gKkzRkITRCxZZJVFXkiYANnZ1P9FBdMdsWaeQbwVfgn8TQZxYeEREvpkmnjsZvo49MBTshHTZFPkeP6yTJoDjXezBXjLfSJoAFucwuZATuVEDnsPi2kyNqBU1o/YWYv8tA5YBy0C7MfAHrIMfHRdraaAAAAAASUVORK5CYII=";
    </script>
    <#include "../footer.ftl">
</@layout.page>