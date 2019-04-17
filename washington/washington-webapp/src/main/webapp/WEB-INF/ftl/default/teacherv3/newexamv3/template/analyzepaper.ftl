<script type="text/html" id="T:AnalyzePaper">
    <div class="results-list" style="display: none;" data-bind="if:$root.status() == 'SUCCESS',visible:$root.status() == 'SUCCESS'">
        <div class="analyse-side">
            <div class="aSide-title">总成绩分析</div>
            <div class="aSide-main">
                <!--总成绩分析图-->
                <div class="aSideChart" style="width: 700px;height: 200px;" data-bind="attr:{'data-title':$root.initChartA($element,achievementAnalysisPart().scoreRate)}"></div>
                <!--分析数据-->
                <div class="aSideTable">
                    <table>
                        <thead>
                        <tr>
                            <td><div class="tdCell01">班级平均分（满分<span class="txtRed" data-bind="text:achievementAnalysisPart().standardScore">100</span>分）</div></td>
                            <td><div class="tdCell02">班级最高分</div></td>
                            <td><div class="tdCell02">班级最低分</div></td>
                            <td><div class="tdCell02">方差</div></td>
                            <td><div class="tdCell02">标准差</div></td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td data-bind="text:achievementAnalysisPart().averScore">0</td>
                            <td data-bind="text:achievementAnalysisPart().maxScore">0</td>
                            <td data-bind="text:achievementAnalysisPart().minScore">0</td>
                            <td data-bind="text:achievementAnalysisPart().varianceScore">0</td>
                            <td data-bind="text:achievementAnalysisPart().standardDeviationScore"></td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="remark"><span class="dotRed"></span>得分率＝实际得分/总分＊100%</div>
                </div>
            </div>
        </div>
        <div class="analyse-side">
            <div class="aSide-title">成绩分布</div>
            <div class="aSide-main" style="padding-top: 0px;">
                <!--成绩分布图-->
                <div class="aSideChart" style="margin-bottom: 0px;">
                    <div data-bind="style:{height:$root.initChartB($element)}" style="width: 700px;height: 300px;"></div>
                </div>
                <div class="aSideChart" style="display: none;">
                    <div class="colorRemark">
                        <span class="square square-yellow"></span>优
                        <span class="square square-blue"></span>良
                        <span class="square square-green"></span>合格
                        <span class="square square-red"></span>待合格
                    </div>
                </div>
                <!--分析数据-->
                <div class="aSideTable">
                    <table>
                        <thead>
                        <tr>
                            <td><div class="tdCell03">等级（得分率）</div></td>
                            <td><div class="tdCell04">人数（共<!--ko text:scoreDistributionPart().joinNum--><!--/ko-->人考试）</div></td>
                            <td><div class="tdCell05">占比</div></td>
                        </tr>
                        </thead>
                        <tbody data-bind="foreach:{data:scoreDistributionPart().scoreDistributions,as:'bution'}">
                        <tr>
                            <td data-bind="text:(bution.decs + bution.scoreDesc)">&bnsp;</td>
                            <td data-bind="text:(bution.num)">&nbsp;</td>
                            <td data-bind="text:(bution.rate + '%')">0%</td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="remark"><span class="dotRed"></span>得分率＝实际得分/总分＊100%</div>
                </div>
            </div>
        </div>
        <div class="analyse-side" data-title="题目模块成绩分析" data-bind="if:$root.focusPaperIndex() >= 0,visible:$root.focusPaperIndex() >= 0">
            <div class="aSide-title">题目模块成绩分析
                <div class="sideRight" style="display: none;" data-bind="if:$root.papers().length > 1,visible:$root.papers().length > 1">
                    试卷：<!--ko foreach:{data:$root.papers(),as:'paper'}--><span class="active" data-bind="css:{'active':$index() == $root.focusPaperIndex()},title:paper.paperName,click:$root.changePaper.bind($data,$index(),$root),text:paper.paperName">&nbsp;</span><!--/ko-->
                </div>
            </div>
            <div class="aSide-main">
                <div class="aSideChart">
                    <div class="aSide-chart03" data-bind="foreach:{data:$root.focusPaper().moduleAchievements,as:'moduleObj'}">
                        <div class="chartLine">
                            <span class="label" data-bind="text:moduleObj.desc">&nbsp;</span>
                            <div class="prenPro"><div class="prenCur" style="width: 1%" data-bind="style:{width:(moduleObj.rate == 0 ? 1 : moduleObj.rate)  + '%'}"><span class="num" data-bind="text:moduleObj.rate + '%'">60%</span></div></div>
                        </div>
                    </div>
                </div>
                <!--分析数据-->
                <div class="aSideTable">
                    <table>
                        <thead>
                        <tr>
                            <td><div class="tdCell03">题目模块</div></td>
                            <td><div class="tdCell04">平均分</div></td>
                            <td><div class="tdCell05">得分率</div></td>
                        </tr>
                        </thead>
                        <tbody data-bind="foreach:{data:$root.focusPaper().moduleAchievements,as:'moduleObj'}">
                            <tr>
                                <td><div class="tdCell03" data-bind="text:moduleObj.desc">&nbsp;</div></td>
                                <td data-bind="text:moduleObj.averScore + '(满分' + moduleObj.standardScore + ')'">&nbsp;</td>
                                <td data-bind="text:moduleObj.rate + '%'">&nbsp;</td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="remark"><span class="dotRed"></span>得分率＝实际得分/总分＊100%</div>
                </div>
            </div>
        </div>
    </div>
    <div class="results-list" style="display: none;padding: 300px 50px;" data-bind="if:$root.status() == 'FAIL',visible:$root.status() == 'FAIL'">
        <p data-bind="text:$root.resText"></p>
    </div>
</script>