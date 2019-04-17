<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
    title="一起复习，领双重奖学金"
    pageJs=["finalreview"]
    pageJsFile={"finalreview" : "public/script/project/finalreview_spring2018"}
    pageCssFile={"finalreview" : ["public/skin/project/finalreviewactivity/spring2018/css/skin"]}>

    <#include "../../layout/project.header.ftl">
    <div class="header">
        <div class="pic">
            <img src="<@app.link href="public/skin/project/finalreviewactivity/spring2018/images/pc-banner.png"/>" alt="">
            <p class="text-1"></p>
            <p class="text-2"></p>
            <p class="activ-time">活动时间：5月29日－6月27日</p>
        </div>
    </div>
    <div class="wrapper">
        <div class="winn-teach" data-bind="click: clickLasyDayWinnerBox">
            <div class="info">
                <span class="txt">昨日获奖老师：</span>
                <!-- ko if: !lastDayWinner() -->
                <span class="txt">待评选</span>
                <!-- /ko -->
                <!-- ko if: lastDayWinner() -->
                <span class="name" data-bind="text: lastDayWinner().name"></span>
                <span class="school" data-bind="text: lastDayWinner().school"></span>
                <!-- /ko -->
            </div>
        </div>
        <div class="container">
            <div class="l-content md-box">
                <i class="i-book"></i>
                <i class="i-convex"></i>
                <div class="title">共赢“师生奖学金”</div>
                <div class="main-box">
                    <p class="tips">期末复习结束后，将根据申领老师的班级参与情况，<br/> 奖励给认复习的老师和学生</p>
                    <p class="tips-rule"><a class="exam" href="/project/finalreviewactivity/spring2018_rule.vpage">查看评奖细则<i class="icon"></i></a></p>
                    <!-- ko if:subject() != "CHINESE"-->
                    <div class="details">
                        <span>1、布置基础必过</span>
                        <i class="color-1" data-bind="
                            css: {'color-2': (detailInfo().basicReviewNum <= 0 || !detailInfo())},
                            text: detailInfo().basicReviewNum > 0 ? '已达成' : '未达成'">未达成</i>
                    </div>
                    <!-- /ko -->
                    <div class="details">
                        <span><!-- ko if:subject() != "CHINESE"-->2、<!-- /ko -->布置3次以上复习作业</span>
                        <i class="color-1" data-bind="
                            css: {'color-2': (detailInfo().termReviewNum < 3 || !detailInfo())},
                            text: detailInfo().termReviewNum >= 3 ? '已达成' : '未达成'">未达成</i>
                    </div>
                    <div class="btn-box">
                        <a href="javascript:;" class="disabled">活动结束</a>

                        <!-- 语文学科当finalLottery为true时只需判断termReviewNum一个条件 -->
                        <#--<a href="javascript:;" class="current" data-bind="-->
                            <#--visible: subject() == 'CHINESE',-->
                            <#--css: {-->
                                <#--'disabled': detailInfo().finalLottery && detailInfo().termReviewNum < 3-->
                            <#--},-->
                            <#--click: clickFinalBtn.bind($data)" style="display: none;">-->
                            <#--<span data-bind="visible: !detailInfo().finalLottery">申领</span>-->
                            <#--<span data-bind="visible: detailInfo().finalLottery && detailInfo().termReviewNum < 3">尚未达标</span>-->
                            <#--<span data-bind="visible: detailInfo().finalLottery && detailInfo().termReviewNum >= 3">待评选</span>-->
                        <#--</a>-->
                        <!-- 非语文学科当finalLottery为true时得需判断basicReviewNum和termReviewNum两个条件 -->
                        <#--<a href="javascript:;" data-bind="-->
                            <#--visible: subject() != 'CHINESE',-->
                            <#--css: {-->
                                <#--'disabled': detailInfo().finalLottery && (detailInfo().basicReviewNum <= 0 || detailInfo().termReviewNum < 3)-->
                            <#--},-->
                            <#--click: clickFinalBtn.bind($data, event)" style="display: none;">-->
                            <#--<span data-bind="visible: !detailInfo().finalLottery" style="display: none">申领</span>-->
                            <#--<span data-bind="visible: detailInfo().finalLottery && (detailInfo().basicReviewNum <= 0 || detailInfo().termReviewNum < 3)" style="display: none">尚未达标</span>-->
                            <#--<span data-bind="visible: detailInfo().finalLottery && detailInfo().basicReviewNum > 0 && detailInfo().termReviewNum >= 3" style="display: none">待评选</span>-->
                        <#--</a>-->
                    </div>
                    <div class="divi-line"></div>
                    <div class="md-prize">
                        <p class="text-1">－老师－</p>
                        <p class="text-2">¥2999教育基金<span>(每人）</span></p>
                        <p class="text-3">共10个老师获奖名额</p>
                        <p class="img-1"><img src="<@app.link href="public/skin/project/finalreviewactivity/spring2018/images/pc-prize02.png"/>" alt=""></p>
                    </div>
                    <div class="md-prize">
                        <p class="text-1">－学生－</p>
                        <p class="text-2">米兔儿童电话手表<span>(每人）</span></p>
                        <p>学生6名（获奖老师的班级）</p>
                        <p class="text-3">优异复习学生＊3名</p>
                        <p class="text-3">学生进步＊3名</p>
                        <p class="img-2"><img src="<@app.link href="public/skin/project/finalreviewactivity/spring2018/images/pc-prize03.png"/>" alt=""></p>
                    </div>
                </div>
            </div>
            <div class="r-content">
                <div class="scholarship md-box">
                    <div class="title">每日“老师复习奖学金”</div>
                    <p class="text-tip">每天布置 每天都可参与评奖</p>
                    <p class="tips-rule"><a class="exam" href="/project/finalreviewactivity/spring2018_rule.vpage">查看评奖细则<i class="icon"></i></a></p>
                    <p class="prize-name">¥500教育基金</p>
                    <p class="img-2"><img src="<@app.link href="public/skin/project/finalreviewactivity/spring2018/images/pc-prize01.png"/>" alt=""></p>
                    <div class="btn-box">
                        <a href="javascript:;" class="disabled">活动结束</a>

                        <#--<a href="javascript:;" data-bind="-->
                            <#--text: !detailInfo().dailyLottery ? '申领': '已申领',-->
                            <#--css: {'disabled': detailInfo().dailyLottery},-->
                            <#--click: clickDailyBtn.bind($data, detailInfo().dailyLottery, event)"></a>-->
                    </div>
                    <p class="tips">学生完成越多，累计的复习数据越丰富，老师和班级均有可能得到奖学金</p>
                </div>
                <div class="result-list md-box">
                    <div class="title">奖学金评选结果</div>
                    <div class="text">期末“师生奖学金”</div>
                    <div class="tab-list">
                        <div class="scorl-box">
                            <div class="resultEmpty" data-bind="visible: !finalList().length" style="display:none;">待复习结束时评选</div>
                            <table data-bind="visible: finalList().length" style="display:none;">
                                <thead>
                                    <tr>
                                        <td class="name">老师姓名</td>
                                        <td class="schol"><i>学校</i></td>
                                        <td class="winn">获奖学生</td>
                                    </tr>
                                </thead>
                                <tbody data-bind="foreach: finalList">
                                    <tr>
                                        <td class="name"><i data-bind="text: userName, attr: {title: userName}"></i></td>
                                        <td class="schol"><i data-bind="text: schoolName, attr: {title: schoolName}"></i></td>
                                        <td class="winn"><a class="viewlist" href="javascript:;" data-bind="click: $root.clickShowNameList.bind($data)">查看名单</a></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="text text-2">每日“复习奖学金”</div>
                    <p class="r-tips">次日早6点更新结果</p>
                    <div class="tab-list tab-list-2">
                        <div class="scorl-box">
                            <div class="resultEmpty" data-bind="visible: !dailyList().length" style="display:none;">今日的获奖老师，明日公布</div>
                            <table data-bind="visible: dailyList().length" style="display: none;">
                                <thead>
                                    <tr>
                                        <td class="time">时间</td>
                                        <td class="name">老师姓名</td>
                                        <td class="schol"><i>学校</i></td>
                                        <td class="winn">奖品</td>
                                    </tr>
                                </thead>
                                <tbody data-bind="foreach: dailyList">
                                    <tr>
                                        <td class="time"><i data-bind="text: formatDate"></i></td>
                                        <td class="name"><i data-bind="text: name, attr: {title: name}"></i></td>
                                        <td class="schol"><i data-bind="text: school, attr: {title: school}"></i></td>
                                        <td class="winn"><i data-bind="text: award, attr: {title: award}"></i></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 弹窗-公共 -->
    <div class="popup" data-bind="visible: isShowTipDialog" style="display: none;">
        <div class="popinner">
            <a class="btn-close" href="javascript:;" data-bind="click:
                function ($data) {
                    $data.isShowTipDialog(false);
                }"></a>
            <!-- pic-beans -->
            <p class="pic" data-bind="css: {
                'pic-beans': tipDialogData().type == 3 || tipDialogData().type == 4}"></p>
            <p class="txt-1" data-bind="html: tipDialogData().dialogTitle"></p>
            <p class="txt-2" data-bind="html: tipDialogData().dialogContent"></p>
            <a class="btn-homework"  href="javascript:;" data-bind="text: tipDialogData().btnText, click: tipDialogData().btnCallback"></a>
        </div>
    </div>

    <!-- 弹窗-报错-->
    <div class="popup" data-bind="visible: isShowErrorDialog" style="display: none;">
        <div class="popinner popinner2">
            <a class="btn-close" href="javascript:;" data-bind="click:
                function ($data) {
                    $data.isShowErrorDialog(false);
                }"></a>
            <p class="txt-1-1">系统提示</p>
            <p class="txt-2-2" data-bind="html: errorDialogData().dialogContent"></p>
            <a class="btn-homework"  href="javascript:;" data-bind="click:
                function ($data) {
                    $data.isShowErrorDialog(false);
                }">我知道了</a>
        </div>
    </div>

    <!-- 弹窗-获奖名单 -->
    <div class="popup" data-bind="visible: isShowNameListDialog" style="display: none;">
        <div class="popinner">
            <a class="btn-close" href="javascript:;" data-bind="click:
                function ($data) {
                    $data.isShowNameListDialog(false);
                }"></a>
            <div class="winnersList">
                <p class="titleLabel">获奖名单</p>
                <p class="fontBig" data-bind="text: finalWinnerDialogInfo().schoolName"></p>
                <p class="winnerGroup">老师：<!-- ko text: finalWinnerDialogInfo().userName --><!-- /ko--></p>
                <p class="winnerGroup">学生奖学金（6）：<!-- ko text: finalWinnerDialogInfo().stuList --><!-- /ko--></p>
            </div>
            <div class="tips-gray">注：详细获奖者信息会另行通知老师，请保持通讯畅通</div>
        </div>
    </div>
    <script>
        var initMode = 'indexMode';
    </script>
    <#include "../../layout/project.footer.ftl">
</@layout.page>