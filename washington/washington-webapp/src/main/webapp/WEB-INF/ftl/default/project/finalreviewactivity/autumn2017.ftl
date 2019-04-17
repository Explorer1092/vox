<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起复习，领双重奖学金"
pageJs=["finalreview"]
pageJsFile={"finalreview" : "public/script/project/finalreview_autumn2017"}
pageCssFile={"finalreview" : ["public/skin/project/finalreviewactivity/autumn2017/css/skin"]}>

<#include "../../layout/project.header.ftl">
    <div class="header">
        <img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/banner.jpg"/>" alt="">
    </div>
    <div class="wrapper">
        <div class="winn-teach">
            <div class="info">
                <span class="txt">昨日获奖老师：</span>
                <div style="float:left;cursor: pointer;" data-bind="template: {name : templateName(),data: dailyWinnerData()},click: dailyWinnerButton"></div>
                <script type="text/html" id="dailyWinnerMode">
                    <span class="pic"><img src="" data-bind="attr:{src:$root.pressImage(img,100)}"></span>
                    <span class="name" data-bind="text: name"></span>
                    <span class="school" data-bind="text: school"></span>
                </script>
                <script type="text/html" id="dailyWinnerNull">
                    <span class="name">待评选</span>
                </script>
            </div>
        </div>
        <div class="container">
            <div class="l-content md-box">
                <i class="i-book"></i>
                <i class="i-convex"></i>
                <div class="title">“高效科学”总复习奖学金</div>
                <div class="main-box">
                    <p class="tips">积极参与本次期末复习的老师即可申领</p>
                    <a class="exam" href="javascript:;" data-bind="click: scholarshipRules">查看评奖细则<i class="icon"></i></a>
                    <!-- ko if:finalSubject() != "CHINESE"-->
                    <div class="details">
                        <span>1、布置基础必过</span>
                        <i class="color-1" data-bind="visible: detail().basicReviewNum >= 1" style="display:none;">已达成</i>
                        <i class="color-2" data-bind="visible: detail().basicReviewNum < 1" style="display:none;">未达成</i>
                    </div>
                    <!--/ko-->
                    <div class="details">
                        <span><!-- ko if:finalSubject() != "CHINESE"-->2、<!--/ko-->布置5次以上复习作业</span>
                        <i class="color-1" data-bind="visible: detail().termReviewNum >= 5" style="display:none;">已达成</i>
                        <i class="color-2" data-bind="visible: detail().termReviewNum < 5" style="display:none;">未达成</i>
                    </div>
                    <div class="btn-box">
                        <a href="javascript:;" class="<#--JS-final--> disabled" <#--data-bind="text: finalText,click: finalScholarship"-->>已结束</a>
                    </div>
                    <p class="tips-2">将从已达标的<!--ko text:finalAttendNum--><!--/ko-->人中抽选</p>
                    <div class="divi-line"></div>
                    <div class="md-prize">
                        <p class="text-1">共10个老师获奖名额</p>
                        <p class="text-2">¥2999教育基金<span>(每人）</span></p>
                        <p class="img"><img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/i-money.png"/>" alt=""></p>
                    </div>
                    <div class="md-prize">
                        <p class="text-1">老师获奖，将同时为班级获得</p>
                        <p class="text-2">乐高积木<span>(每人）</span></p>
                        <p class="text-4">学生奖学金10名</p>
                        <p class="text-3">优异复习学生＊5名</p>
                        <p class="text-3">学生进步＊5名</p>
                        <p class="img"><img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/i-lego.png"/>" alt=""></p>
                    </div>
                    <div class="md-prize">
                        <p class="text-2">360智能儿童手表<span>(每人）</span></p>
                        <p class="text-4">家长奖学金5名</p>
                        <p class="text-3">最关注学生家长＊5名</p>
                        <p class="img"><img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/i-watch.png"/>" alt=""></p>
                    </div>
                </div>
            </div>
            <div class="r-content">
                <div class="scholarship md-box">
                    <i class="i-convex"></i>
                    <div class="title">每日复习奖学金</div>
                    <a class="exam" href="javascript:;" data-bind="click: scholarshipRules">查看评奖细则<i class="icon"></i></a>
                    <div class="descrip">
                        <div class="l-text">
                            <div class="subtit">今日布置期末复习</div>
                            <p class="inner">学生完成越多，累计的复习数据越丰富，<br/> 老师和班级均有可能得到奖学金</p>
                        </div>
                        <div class="r-pic">
                            <img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/i-kindle.png"/>" alt="">
                        </div>
                    </div>
                    <div class="btn-box">
                        <a href="javascript:;" class="<#--JS-daily--> disabled"<#-- data-bind="text: dailyText,click: dailyScholarship"-->>已结束</a>
                    </div>
                </div>
                <div class="result-list md-box">
                    <div class="title">奖学金评选结果</div>
                    <div class="text">期末总复习奖学金</div>
                    <div class="tab-list">
                        <div class="scorl-box">
                            <div class="resultEmpty" style="display:none;">待复习结束时评选</div>
                            <table style="display:block;">
                                <thead>
                                <tr>
                                    <td class="name">老师姓名</td>
                                    <td class="schol"><i>学校</i></td>
                                    <td class="winn">获奖学生&家长</td>
                                </tr>
                                </thead>
                                <tbody data-bind="foreach:finalreviewData">
                                <tr>
                                    <td class="name"><i data-bind="text:userName"></i></td>
                                    <td class="schol"><i data-bind="text:schoolName"></i></td>
                                    <td class="winn"><a class="viewlist" href="javascript:;" data-bind="click: $root.lookFinalReview.bind($data)">查看名单</a></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="text text-2">每日复习奖学金</div>
                    <p class="r-tips">次日早6点更新结果</p>
                    <div class="tab-list tab-list-2">
                        <div class="scorl-box">
                            <div class="resultEmpty" style="display:none;" data-bind="visible: dailyList().length == 0">今日的获奖老师，明日公布</div>
                            <table data-bind="visible:dailyList().length > 0" style="display:none;">
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
                                    <td class="name"><i data-bind="text: name"></i></td>
                                    <td class="schol"><i data-bind="text: school"></i></td>
                                    <td class="winn"><i data-bind="text: award"></i></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "../../layout/project.footer.ftl">
    <div data-bind="visible: dialogShow,template: {name : 'dialogDialogMode',data: dialogData()}" style="display:none;"></div>
    <script type="text/html" id="dialogDialogMode">
        <!--ko if:type == 'dialog' -->
        <div class="popup">
            <div class="popinner">
                <a class="btn-close" href="javascript:;" data-bind="click: $root.closeDialog"></a>
                <p class="pic" data-bind="css:{
                    'pic-beans':button == 'receivedBeans' || button == 'finalSuccess',
                    }"></p>
                <p class="txt-1" data-bind="text: dialogTitle"></p>
                <p class="txt-2" data-bind="html: dialogContent"></p>
                <a class="btn-homework"  href="javascript:;" data-bind="click: $root.dialogButton,text: buttonText"></a>
            </div>
        </div>
        <!--/ko -->
        <!--ko if:type == 'rules' -->
        <div class="popup">
            <div class="rule-descr">
                <a class="btn-close" href="javascript:;" data-bind="click: $root.closeDialog"></a>
                <div class="scorl-box-2">
                    <h2>奖学金评奖细则</h2>
                    <div class="foreword">
                        <span class=""><i></i>前言：</span>
                        <p>为了帮助老师更好地进行期末复习，一起作业精心准备了包含基础、重点专项，试卷等模块，并设立期末复习奖学金， 奖励认真复习的老师、</p>
                    </div>
                    <div class="foreword">
                        <span class=""><i></i>奖学金设置：</span>
                        <p><img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/pic-tab.png"/>" alt=""></p>
                    </div>
                    <div class="foreword">
                        <span class=""><i></i>评选规则：</span>
                    </div>
                    <div class="pic">
                        <img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/i-tit-1.png"/>" alt="">
                        <p>每天布置指定类型的复习作业，参加每日评奖，布置的越好，<br/>覆盖学生越多，完成学生越多，学生成绩越好，得奖几率越大</p>
                    </div>
                    <div class="list-txt">
                        <h4>老师参评条件：</h4>
                        <p class="l-txt">当日布置过期末复习作业的认证老师</p>
                        <p class="l-txt">
                            <span> ▪ 当天布置了期末复习作业</span>
                            <span>▪ 曾布置期末复习基础必过，且未删除</span>
                        </p>
                    </div>
                    <div class="list-txt">
                        <h4>评选规则：</h4>
                        <p>1.从所有符合条件的老师中随机评选</p>
                        <p>2.布置的班级越多，学生积累的复习数据越多，随机概率越大</p>
                        <p style="margin-top:6px;">全国参与老师数量巨大，为保证评奖公平性，评选参考复习数据同时会增加一定的随机性。</p>
                    </div>
                    <div class="list-txt">
                        <h4>实物奖品：</h4>
                        <p>每天一个kindle阅读器，共30名 </p>
                    </div>
                    <div class="list-txt">
                        <h4>结果公布：</h4>
                        <p>1.每日公布前一天获奖名单，获奖老师将收到获奖通知短信，老师将根据短信提示领取奖学金</p>
                        <p>2.所有奖品将在活动结束后15天内，即1月25日统一发出</p>
                    </div>
                    <div class="pic pic-2">
                        <img src="<@app.link href="public/skin/project/finalreviewactivity/autumn2017/images/i-tit-2.png"/>" alt="">
                    </div>
                    <div class="list-txt">
                        <h4>老师参评条件：</h4>
                        <p>1.布置过“基础必过”复习作业</p>
                        <p>2.布置并检查过5次以上期末复习作业（除基础必过外）</p>
                        <p>3.且为认证老师</p>
                    </div>
                    <div class="list-txt">
                        <h4>评选规则：</h4>
                        <p>1.老师奖学金10名，将从以下3类老师中随机挑选；老师需要满足以下至少1个条件：</p>
                        <div class="text-box">
                            <p class="t-text"><i>成绩优异：</i>期末复习，学生平均分高于80。复习效果好，班级进步大。</p>
                            <p class="t-text"><i>科学复习：</i>期末复习作业覆盖了所有的单元。布置最科学、覆盖知识全面。</p>
                            <p class="t-text"><i>积极复习：</i>班级学生平均完成人数>50，完成率>80%。学生参与人数多，完成率高。</p>
                            <p style="margin-top:6px;">全国参与老师数量巨大，为保证评奖公平性，评选参考复习数据同时会增加一定的随机性。</p>
                        </div>

                        <p>2.学生奖学金100名，家长奖学金50名。每个获奖老师的学生10名，家长5名：</p>
                        <div class="text-box">
                            <p class="t-text"><i>期末复习-优异之星：</i>老师班级，复习作业和基础必过全部完成，且普通作业平均分最高的前5名</p>
                            <p class="t-text"><i>期末复习-勤奋之星：</i>老师班级，复习作业和基础必过全部完成，且基础必过完成时间最早的前5名</p>
                            <p class="t-text"><i>最关注孩子家长：</i>老师班级，家长看作业报告活跃天数和活跃时长最长的 家长，前5名</p>
                        </div>
                    </div>
                    <div class="list-txt list-txt-2">
                        <h4>实物奖品：</h4>
                        <p>老师：2999元教育基金，共10名（将以京东E卡方式发放，供老师教育使用）</p>
                        <p>学生：乐高积木+阿分题30天，共100名</p>
                        <p>家长：360儿童智能手表，共50名</p>
                    </div>
                    <div class="list-txt ">
                        <h4>结果公布：</h4>
                        <p class="l-txt-2">1.1月10日公布获奖名单，获奖老师将收到获奖通知短信，<br/>老师将根据短信提示领取奖学金 2.所有奖品将在活动结束后15天内</p>
                        <p class="l-txt-2">2.所有奖品将在活动结束后15天内，即1月25日统一发出</p>
                    </div>
                    <div class="btn-partic"><a class="btn" href="javascript:;" data-bind="click: $root.batchAssignHomework">立即布置 领双重奖学金</a></div>
                </div>
            </div>
        </div>
        <!--/ko -->
    </script>
    <div data-bind="template: {name: templateBox(), data: finalreviewList()},visible: finalReviewListShow" style="display:none;"></div>
    <script type="text/html" id="finalReviewMode">
        <div class="popup">
            <div class="popinner">
                <a class="btn-close" href="javascript:;" data-bind="click: $root.closeListShow"></a>
                <div class="winnersList">
                    <p class="titleLabel">获奖名单</p>
                    <p class="fontBig" data-bind="text:schoolName"></p>
                    <p class="winnerGroup">老师：<!--ko text:userName--><!--/ko--></p>
                    <p class="winnerGroup">学生奖学金（10）：<!--ko text:stuList--><!--/ko--></p>
                    <p class="winnerGroup">最佳家长（5）：<!--ko text:parentList--><!--/ko-->等5位学生的家长</p>
                </div>
                <div class="tips-gray">注：详细获奖者信息会另行通知老师，请保持通讯畅通</div>
            </div>
        </div>
    </script>

</@layout.page>