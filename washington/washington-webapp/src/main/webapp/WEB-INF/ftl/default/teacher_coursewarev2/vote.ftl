<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner","vote","awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"vote" : "public/script/teacher_coursewarev2/vote",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

    <#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div id="voteContent">
        <!-- 主要内容 -->
        <div class="courseSection courseSection-f2">
            <div class="secInner">
                <!-- 投标倒计时 -->
                <div class="voteDateContent">
                    <div class="voteDateBox">
                        <div class="topBox" data-bind="css: {'timeFinished': voteOver}">
                            <p class="timeLabel">距离投票结束还有：</p>
                            <div class="timeBox">
                                <span class="num" data-bind="text: leftTimeObj().day || '00'"></span>
                                <span>天</span>
                                <span class="num" data-bind="text: leftTimeObj().hour || '00'"></span>
                                <span>时</span>
                                <span class="num" data-bind="text: leftTimeObj().min || '00'"></span>
                                <span>分</span>
                                <span class="num" data-bind="text: leftTimeObj().sec || '00'"></span>
                                <span>秒</span>
                            </div>
                            <p class="timeOver">投票已结束</p>
                            <p class="lastTicket lastTicket02" style="display: none;">今日剩余投票数：<span data-bind="text: leftVoteTime"></span></p>
                        </div>
                        <!-- 规则说明，当不需要展开按钮时，手动增加style，需要时去掉 -->
                        <div class="voteRule" style="padding:30px 40px;">
                            <div>规则说明：</div>
                            <!--展开前-->
                            <ul class="ruleBox">
                                <li>
                                    认证老师拥有更多投票机会
                                    <!-- ko if: !isAuth() -->，<a href="/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage" target="_blank" style="color:#ff6600;">去认证>></a><!-- /ko -->。
                                    <br><span style="color:#ff6600;">已认证</span>：每日可投<span style="color:#ff6600;font-size:18px;font-weight:bold;">10</span>次，每个作品可投<span style="color:#ff6600;font-size:18px;font-weight:bold;">2</span>次
                                    <br><span style="color:#ff6600;font-weight:bold;">未认证</span>：每日可投<span style="color:#ff6600;font-size:18px;font-weight:bold;">5</span>次，每个作品可投<span style="color:#ff6600;font-size:18px;font-weight:bold;">1</span>次</li>
                                <li>如发现恶意刷票等作弊行为，将取消评选资格。</li>
                                <li>一起教育科技对本活动拥有最终解释权。</li>
                            </ul>
                            <!--展开后，真实情况规则较少，不需要折叠-->
                            <#--<ul class="ruleBox" data-bind="visible: isShowAllRule()" style="display: none">
                                <li>***</li>
                            </ul>-->
                            <#--<div class="retractBtn" data-bind="click: showAllRule, text: isShowAllRule() ? '收起' : '展开'"></div>-->
                        </div>
                    </div>
                </div>
                <p class="lastTicket" data-bind="visible: !voteOver()">今日剩余投票数：<span data-bind="text: leftVoteTime"></span></p>
                <!-- 作品排行 -->
                <div class="workRankMain">
                    <div class="wr-nav clearfix">
                        <div class="genre">
                            <!-- ko foreach: orderList -->
                            <span data-bind="
                                text: name,
                                css: { 'active': $root.choiceOrderInfo().id === id },
                                click: $root.choiceOrder"></span>
                            <span class="line" data-bind="visible: $index() < $root.orderList().length - 1" style="display: none;">|</span>
                            <!-- /ko -->
                        </div>
                        <div class="search-input">
                            <input class="input" type="text" placeholder="输入作品名称"  data-bind="
                                value: inputKeyWord,
                                valueUpdate: 'afterkeydown',
                                event: { keyup: inputKeyWordKeyUp }">
                            <i class="searchIcon" data-bind="click: searchKeyWord"><span class="icon-search"></span></i>
                        </div>
                        <div class="choice-nav">
                            <!-- ko foreach: subjectList -->
                            <span data-bind="
                                text: name,
                                css: { 'active': $root.choiceSubjectInfo().id === id },
                                click: $root.choiceSubject"></span>
                            <!-- /ko -->
                        </div>
                    </div>

                    <!-- 我的作品列表 -->
                    <ul class="clearfix worksList rankWorksList " data-bind="visible: courseList().length" style="display: none">
                        <!-- ko foreach: courseList -->
                        <li class="hasHover" data-bind="click: $root.toDetailPage">
                            <!-- rankNum01 rankNum02 rankNum03分别对应1,2,3名 -->
                            <#--<div class="rankNum" data-bind="text: rankIndex, css: 'rankNum0' + rankIndex, visible: rankIndex <= 3"></div>-->
                            <#--<div class="rankNum" data-bind="text: rankIndex, visible: rankIndex > 3"></div>-->
                            <div class="worksPic">
                                <img alt="封面图" data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}">
                                <div class="operateBox">
                                    <div class="item" data-bind="text: (awardLevelId === 5 ? '历史获奖作品' : ('曾获' + awardLevelName + '奖项')), visible: awardLevelId && awardLevelId > 0"></div>
                                    <div class="item item03" data-bind="text: totalCanvassNum + '票'"></div>
                                </div>
                            </div>
                            <div class="worksInfo">
                                <div class="info-title" data-bind="text: title"></div>
                                <div class="info-school">
                                    <span class="school-name" data-bind="text: schoolName, attr: {title: schoolName}"></span>
                                    <span class="name" data-bind="text: teacherName"></span>
                                </div>
                                <div class="voteBtn" data-bind="
                                    css: {'voted': !surplus},
                                    text: surplus ? '为TA投票' : '已投票',
                                    click: $root.voteCourse,
                                    clickBubble: false,
                                    visible: !$root.voteOver()"></div>
                                <div class="voteBtn voted" data-bind="
                                    click: $root.voteCourse,
                                    clickBubble: false,
                                    visible: $root.voteOver">已结束</div>
                            </div>
                        </li>
                        <!-- /ko -->
                    </ul>
                    <!-- 暂无数据 -->
                    <div class="emptyListTip" data-bind="visible: !courseList().length" style="display: none; padding: 180px 0 120px; background: transparent;">
                        <div class="emptyIcon"></div>
                        <p data-bind="text: serarchKeyWordFlag() ? '没有找到相关内容' : '暂无数据'"></p>
                    </div>

                    <!-- 翻页导航 -->
                    <ul class="pagesNav clearfix" id="JS-pagination"></ul>
                </div>
            </div>
        </div>

        <!-- 公用弹窗 -->
        <div class="coursePopup popup-rights" style="display: none;" data-bind="visible: isShowCommonAlert">
            <div class="popupInner">
                <div class="closeBtn" data-bind="click: function () { $root.isShowCommonAlert(false); }"></div>
                <div class="topContent">
                    <!-- error 错误icon, vote-success 投票成功, vote-fail 投票失败-未认证, vote-share 投票失败-已认证 -->
                    <div class="icon-success" data-bind="css: commonAlertOpt().state"></div>
                    <div class="text" data-bind="text: commonAlertOpt().title1"></div>
                    <div class="small-text" data-bind="text: commonAlertOpt().title2"></div>
                </div>
                <div class="botContent">
                    <div class="contentBox" data-bind="html: commonAlertOpt().content"></div>
                    <div class="otherContent">
                        <a class="btn02" href="javascript:void(0)" data-bind="
                            text: commonAlertOpt().left_btn_text,
                            click: commonAlertOpt().left_btn_cb"></a>
                        <a class="continue" href="javascript:void(0)" data-bind="
                            text: commonAlertOpt().right_btn_text,
                            click: commonAlertOpt().right_btn_cb"></a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "./module/alert.ftl">
    <#include "./module/footer.ftl">
</div>
<script src="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.js')}"></script>
<script>
    var cdnHeader = "<@app.link href='/'/>";
    var userInfo = {};
    var awardTeachers = null; // 获奖老师名单
</script>
</@layout.page>