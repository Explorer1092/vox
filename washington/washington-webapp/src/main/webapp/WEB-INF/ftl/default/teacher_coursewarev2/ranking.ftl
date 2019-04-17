<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner", "ranking", "awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"ranking" : "public/script/teacher_coursewarev2/ranking",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

    <#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div id="rankingContent">
        <div class="rankBox">
            <div class="courseSection">
                <div class="secInner">
                    <!-- 排行榜nav -->
                    <ul class="rankNav clearfix">
                        <li data-bind="css: {'active': activeBigRankType() === 0}, click: choiceBigRankType.bind($data, 0)">
                            <div class="icon icon01"></div>
                            <div class="rTitle">
                                <p class="kind">最具人气</p>
                                <p>排行榜</p>
                            </div>
                        </li>
                        <li data-bind="css: {'active': activeBigRankType() === 1}, click: choiceBigRankType.bind($data, 1)">
                            <div class="icon icon02"></div>
                            <div class="rTitle">
                                <p class="kind">高分作品</p>
                                <p>排行榜</p>
                            </div>
                        </li>
                        <li data-bind="css: {'active': activeBigRankType() === 2}, click: choiceBigRankType.bind($data, 2)">
                            <div class="icon icon03"></div>
                            <div class="rTitle">
                                <p class="kind">点评达人</p>
                                <p>排行榜</p>
                            </div>
                        </li>
                    </ul>
                    <!-- 排行榜 -->
                    <div class="workRankBox">
                        <!-- 选择分类 -->
                        <div class="wTitleBox clearfix">
                            <div class="titleLeft">
                                <span class="title" data-bind="text: rankTitle"></span>
                                <span class="time" data-bind="text: rankUpdateTime"></span>
                                <span class="rule" data-bind="click: showRule">查看规则说明</span>
                            </div>
                            <div class="titleRight">
                                <div class="bar_box">
                                    <div class="bar_content" style="display: none;" data-bind="
                                        visible: activeBigRankType() !== 2,
                                        css: {'open_active': showSubjectList},
                                        click: clickSubject">
                                        <div class="label_name"><!--ko text: choiceSubjectInfo().name --><!--/ko--><i class="arrow_down"></i></div>
                                        <div class="tag_list" data-bind="visible: showSubjectList" style="display: none">
                                            <!-- ko foreach: subjectList -->
                                            <span data-bind="
                                                text: name,
                                                css: { 'active': id === $root.choiceSubjectInfo().id },
                                                click: $root.choiceSubject"></span>
                                            <!-- /ko -->
                                        </div>
                                    </div>
                                    <div class="bar_content" data-bind="
                                        css: {'open_active': showDateTypeList},
                                        click: clickDateType">
                                        <div class="label_name"><!--ko text: choiceDateTypeInfo().name --><!--/ko--><i class="arrow_down"></i></div>
                                        <div class="tag_list" data-bind="visible: showDateTypeList" style="display: none">
                                            <!-- ko foreach: dateTypeList -->
                                            <span data-bind="
                                                text: name,
                                                css: { 'active': id === $root.choiceDateTypeInfo().id },
                                                click: $root.choiceDateType">总榜</span>
                                            <!-- /ko -->
                                        </div>
                                    </div>

                                    <div class="dayNum" data-bind="visible: choiceDateTypeInfo().id !== 3, text: choiceDatePeriodText" style="display: none;"></div>
                                    <!--日榜-->
                                    <div class="bar_content bar_date" style="display: none;" data-bind="
                                        visible: choiceDateTypeInfo().id === 0 && dateList().length,
                                        css: {'open_active': showDateList},
                                        click: clickDate">
                                        <div class="label_name"><!--ko text: choiceDateInfo() --><!--/ko--><i class="arrow_down"></i></div>
                                        <div class="tag_list" data-bind="visible: showDateList" style="display: none">
                                            <!-- ko foreach: dateList -->
                                            <span data-bind="
                                                text: $data,
                                                css: { 'active': $data === $root.choiceDateInfo() },
                                                click: $root.choiceDate"></span>
                                            <!-- /ko -->
                                        </div>
                                    </div>
                                    <!--周榜-->
                                    <div class="bar_content bar_date" style="display: none;" data-bind="
                                        visible: choiceDateTypeInfo().id === 1 && weekList().length,
                                        css: {'open_active': showWeekList},
                                        click: clickWeek">
                                        <div class="label_name"><!--ko text: choiceWeekInfo().text --><!--/ko--><i class="arrow_down"></i></div>
                                        <div class="tag_list" data-bind="visible: showWeekList" style="display: none">
                                            <!-- ko foreach: weekList -->
                                            <span data-bind="
                                                text: text,
                                                css: { 'active': period === $root.choiceWeekInfo().period },
                                                click: $root.choiceWeek"></span>
                                            <!-- /ko -->
                                        </div>
                                    </div>
                                    <!--月榜-->
                                    <div class="bar_content bar_date" style="display: none;" data-bind="
                                        visible: choiceDateTypeInfo().id === 2 && monthList().length,
                                        css: {'open_active': showMonthList},
                                        click: clickMonth">
                                        <div class="label_name"><!--ko text: choiceMonthInfo().text --><!--/ko--><i class="arrow_down"></i></div>
                                        <div class="tag_list" data-bind="visible: showMonthList" style="display: none">
                                            <!-- ko foreach: monthList -->
                                            <span data-bind="
                                                text: text,
                                                css: { 'active': id === $root.choiceMonthInfo().id },
                                                click: $root.choiceMonth"></span>
                                            <!-- /ko -->
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- 重要提示 -->
                        <div class="importantTip" data-bind="visible: rankNotificationFlag" style="display: none;">
                            <span class="icon icon-horn"></span>
                            <span>重要提示：<!-- ko text: rankNotification --><!-- /ko --></span>
                            <span class="icon icon-close" data-bind="click: closeNotification"></span>
                        </div>
                        <!-- 01 最具人气排行榜 -->
                        <div class="wRankList" data-bind="visible: activeBigRankType() === 0" style="display: none">
                            <ul class="rankDetailBox" data-bind="visible: popularRankList().length" style="display: none;">
                                <!-- ko foreach: popularRankList -->
                                <li data-bind="click: $root.toDetailPage, css: {'topThree': rankIndex <= 3 }">
                                    <div class="wInfoWrap">
                                        <div class="placeBox">
                                            <!-- 第一至三名 num01 num02 num03 -->
                                            <div class="place" data-bind="visible: rankIndex <= 3"><i class="num" data-bind="css: 'num0' + rankIndex"></i></div>
                                            <div class="place" data-bind="visible: rankIndex > 3, text: 'No.' + rankIndex"></div>
                                            <!-- 升降箭头 state01上升 state02下降 state03显示new  -->
                                            <div class="state" data-bind="
                                                css: {
                                                    'state01': dynamicRank === 'UP',
                                                    'state02': dynamicRank === 'DOWN',
                                                    'state03': dynamicRank === 'NEW_COURSE'
                                                },
                                                text: dynamicRank === 'FLAT' ? '-' : ''"></div>
                                        </div>
                                        <!-- 中间信息 -->
                                        <div class="midIntro">
                                            <div class="pic">
                                                <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                                <span class="award_tip" data-bind="text: (awardLevelId === 5 ? '历史获奖作品' : ('曾获' + awardLevelName + '奖项')), visible: awardLevelId && awardLevelId > 0" style="display: none;"></span>
                                            </div>
                                            <div class="infoBox">
                                                <div class="w-title" data-bind="text: title, attr: {title: title}"></div>
                                                <div class="text school">
                                                    <span class="schoolName" data-bind="text: schoolName, attr: {title: schoolName}"></span>
                                                    <span class="name" data-bind="text: teacherName"></span>
                                                </div>
                                                <div class="text assessStar">
                                                    <!-- 点亮星星 5种情况 star01至star05 -->
                                                    <div class="info-star evaluate-star" data-bind="css: 'star0' + Math.floor(totalScore / 20)">
                                                        <span class="star"></span>
                                                        <span class="star"></span>
                                                        <span class="star"></span>
                                                        <span class="star"></span>
                                                        <span class="star"></span>
                                                    </div>
                                                    <span class="score" data-bind="text: totalScore"></span>
                                                    <span class="peopleNum" data-bind="text: '（' + commentNum + '人评价过）'"></span>
                                                </div>
                                                <div class="operateBox operateBox02 clearfix">
                                                    <div class="item">
                                                        <i class="icon-look"></i>
                                                        <span data-bind="text: visitNum || 0"></span>
                                                    </div>
                                                    <div class="item item02">
                                                        <i class="icon-assess"></i>
                                                        <span data-bind="text: downloadNum || 0"></span>
                                                    </div>
                                                    <span class="date" data-bind="text: createDate"></span>
                                                </div>
                                            </div>
                                        </div>
                                        <!-- 右侧人气值 -->
                                        <div class="popularityBox">
                                            <p class="popularity" data-bind="text: num"></p>
                                            <p class="label">人气值</p>
                                        </div>
                                    </div>
                                </li>
                                <!-- /ko -->
                            </ul>
                            <!-- 暂无数据 -->
                            <div class="emptyListTip" data-bind="visible: !popularRankList().length" style="display: none;">
                                <div class="emptyIcon"></div>
                                <p>暂无数据</p>
                            </div>
                        </div>
                        <!-- 02 优秀作品排行榜 -->
                        <div class="wRankList" data-bind="visible: activeBigRankType() === 1" style="display: none">
                            <ul class="rankDetailBox" data-bind="visible: excellentRankList().length" style="display: none;">
                                <!-- ko foreach: excellentRankList -->
                                <li data-bind="click: $root.toDetailPage, css: {'topThree': rankIndex <= 3 }">
                                    <div class="wInfoWrap">
                                        <div class="placeBox">
                                            <!-- 第一至三名 num01 num02 num03 -->
                                            <div class="place" data-bind="visible: rankIndex <= 3"><i class="num" data-bind="css: 'num0' + rankIndex"></i></div>
                                            <div class="place" data-bind="visible: rankIndex > 3, text: 'No.' + rankIndex"></div>
                                            <!-- 升降箭头 state01上升 state02下降 state03显示new  -->
                                            <div class="state"></div>
                                            <div class="state" data-bind="
                                                css: {
                                                    'state01': dynamicRank === 'UP',
                                                    'state02': dynamicRank === 'DOWN',
                                                    'state03': dynamicRank === 'NEW_COURSE'
                                                },
                                                text: dynamicRank === 'FLAT' ? '-' : ''"></div>
                                        </div>
                                        <!-- 中间信息 -->
                                        <div class="midIntro">
                                            <div class="pic">
                                                <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                                <span class="award_tip" data-bind="text: (awardLevelId === 5 ? '历史获奖作品' : ('曾获' + awardLevelName + '奖项')), visible: awardLevelId && awardLevelId > 0" style="display: none;"></span>
                                            </div>
                                            <div class="infoBox">
                                                <div class="w-title" data-bind="text: title, attr: {title: title}"></div>
                                                <div class="text school">
                                                    <span class="schoolName" data-bind="text: schoolName, attr: {title: schoolName}"></span>
                                                    <span class="name" data-bind="text: teacherName"></span>
                                                </div>
                                                <div class="text" data-bind="text: createDate"></div>
                                                <div class="operateBox operateBox02 clearfix">
                                                    <div class="item">
                                                        <i class="icon-look"></i>
                                                        <span data-bind="text: visitNum || 0"></span>
                                                    </div>
                                                    <div class="item item02">
                                                        <i class="icon-assess"></i>
                                                        <span data-bind="text: downloadNum || 0"></span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <!-- 右侧人气值 -->
                                        <div class="popularityBox popularityBox02">
                                            <div class="text assessStar">
                                                <!-- 点亮星星 5种情况 star01至star05 -->
                                                <div class="info-star evaluate-star" data-bind="css: 'star0' + Math.floor(totalScore / 20)" style="display: none;">
                                                    <span class="star"></span>
                                                    <span class="star"></span>
                                                    <span class="star"></span>
                                                    <span class="star"></span>
                                                    <span class="star"></span>
                                                </div>
                                            </div>
                                            <p class="popularity" data-bind="text: score + '分'"></p>
                                            <p class="label" data-bind="text: '（' + commentNum + '人评价过）'" style="display:none;"></p>
                                        </div>
                                    </div>
                                </li>
                                <!-- /ko -->
                            </ul>
                            <!-- 暂无数据 -->
                            <div class="emptyListTip" data-bind="visible: !excellentRankList().length" style="display: none;">
                                <div class="emptyIcon"></div>
                                <p>暂无数据</p>
                            </div>
                        </div>
                        <!-- 03 大众评委达人排行榜 -->
                        <div class="wRankList wRankList02" data-bind="visible: activeBigRankType() === 2" style="display: none">
                            <ul class="rankDetailBox" data-bind="visible: juageRankList().length" style="display: none;">
                                <!-- 用户自己的数据，首屏才展示 -->
                                <li data-bind="visible: juageUserData().teacherId && pageIndex() === 1">
                                    <div class="wInfoWrap">
                                        <div class="placeBox"></div>
                                        <!-- 中间信息 -->
                                        <div class="midIntro">
                                            <div class="pic">
                                                <img data-bind="attr: {src: juageUserData().avatar ? '<@app.avatar href='/'/>' + juageUserData().avatar : '<@app.avatar href=''/>'}">
                                            </div>
                                            <div class="infoBox">
                                                <div class="w-title" data-bind="text: juageUserData().teacherName"></div>
                                                <div class="text school" data-bind="text: juageUserData().schoolName"></div>
                                                <div class="text">
                                                    <span data-bind="text: '小学' + juageUserData().teacherSubject"></span>
                                                <#--<span class="grade">三年级</span>-->
                                                </div>
                                            </div>
                                        </div>
                                        <!-- 右侧人气值 -->
                                        <div class="popularityBox">
                                            <p class="popularity" data-bind="text: juageUserData().totalNum"></p>
                                            <p class="label">达人值</p>
                                        </div>
                                    </div>
                                </li>

                                <!-- ko foreach: juageRankList -->
                                <li>
                                    <div class="wInfoWrap">
                                        <div class="placeBox">
                                            <!-- 前三名是红色 placeRed -->
                                            <div class="place" data-bind="text: 'No.' + rankIndex, css: {'placeRed': rankIndex <= 3}"></div>
                                            <!-- 升降箭头 state01上升 state02下降 state03显示new -->
                                            <div class="state"></div>
                                            <div class="state" data-bind="
                                                css: {
                                                    'state01': dynamicRank === 'UP',
                                                    'state02': dynamicRank === 'DOWN',
                                                    'state03': dynamicRank === 'NEW_COURSE'
                                                },
                                                text: dynamicRank === 'FLAT' ? '-' : ''"></div>
                                        </div>
                                        <!-- 中间信息 -->
                                        <div class="midIntro">
                                            <div class="pic">
                                                <img data-bind="attr: {src: avatar ? '<@app.avatar href='/'/>' + avatar : '<@app.avatar href=''/>'}">
                                            </div>
                                            <div class="infoBox">
                                                <div class="w-title" data-bind="text: teacherName"></div>
                                                <div class="text school" data-bind="text: schoolName"></div>
                                                <div class="text">
                                                    <span data-bind="text: '小学' + teacherSubject"></span>
                                                    <#--<span class="grade">三年级</span>-->
                                                </div>
                                            </div>
                                        </div>
                                        <!-- 右侧人气值 -->
                                        <div class="popularityBox">
                                            <p class="popularity" data-bind="text: totalNum"></p>
                                            <p class="label">达人值</p>
                                        </div>
                                    </div>
                                </li>
                                <!-- /ko -->
                            </ul>
                            <!-- 暂无数据 -->
                            <div class="emptyListTip" data-bind="visible: !juageRankList().length" style="display: none;">
                                <div class="emptyIcon"></div>
                                <p>暂无数据</p>
                            </div>
                        </div>
                    </div>
                    <!-- 翻页导航 -->
                    <ul class="pagesNav clearfix" id="JS-pagination"></ul>
                </div>
            </div>
        </div>
        <!--规则-->
        <div class="coursePopup courseRuleAlert" data-bind="visible: showRuleAlert()" style="display: none;">
            <div class="popupInner popupInner-notice">
                <div class="closeBtn closeCourseRuleAlert" data-bind="click: function(){$root.showRuleAlert(false);}"></div>
                <div class="noticeTilte">规则说明</div>
                <div class="noticeDetail">
                    <!--人气榜-->
                    <div class="detailContent" data-bind="visible: activeBigRankType() === 0">
                        <p style="font-weight: bold;">人气作品榜</p>
                        <p>榜单维度：日榜、周榜、总榜</p>
                        <p>日榜：每天展示前一天从00：00至23:59的人气值。</p>
                        <p>周榜：每周一展示上周从周一00：00至周日23:59的人气值。</p>
                        <p>总榜：实时展示截止至当日前一天23:59的人气值。</p>
                        <p>榜单奖励：</p>
                        <p>1.每周周榜前三作品，上榜”每周最具人气作品“，获得官方证书、奖杯、价值300元奖品。</p>
                        <p>2.截止至12月20日23:59，总榜前三作品，上榜“年度最具人气作品”，获得官方证书、奖杯、价值1000元奖品。</p>
                        <p>人气值统计规则：</p>
                        <p>1.作品每被分享1次、下载1次或评论1次，统计1个人气值；</p>
                        <p>2.从“一起小学老师”APP内的作品页分享作品才有效；</p>
                        <p>3.同一个用户对同一个作品的分享和下载分别只计算一次。</p>
                        <p>备注：</p>
                        <p>1.若单个作品重复上榜，则取最高榜单项发放。</p>
                        <p>2.如果有分数并列，依次按照作品人气值、评分次数、分享次数、下载次数降序排序。</p>
                        <p>如存在恶意刷票等作弊行为，一经证实，将取消评选资格，一起教育科技对本活动拥有最终解释权。</p>
                    </div>
                    <!--优秀作品榜-->
                    <div class="detailContent" data-bind="visible: activeBigRankType() === 1">
                        <p style="font-weight: bold;">高分作品榜</p>
                        <p>榜单维度：周榜、月榜、总榜</p>
                        <p>周榜：每周一展示上周从周一00：00至周日23:59的得分。</p>
                        <p>月榜：</p>
                        <p>第一期月榜，11月19日展示从发布开始至11月18日23:59的得分；</p>
                        <p>第二期月榜，12月17日展示从11月19日00:00至12月16日23:59的得分。</p>
                        <p>总榜：实时展示作品从发布开始至当日前一天23:59的得分。</p>
                        <p>作品得分：作品得分统计大众老师评价分数和作品下载情况的综合分数。一起小学APP认证老师的评价分数占比70%，非认证老师占比30%。</p>
                        <p>榜单奖励：</p>
                        <p>因线上展示自10月20日起开始，为了阶段性鼓励评分高的作品，同时规避刷分风险，保证作品是经过真实检验的，设置如下奖励机制：</p>
                        <p>1.两期月榜前三作品，评为“月度优秀教学设计作品”，获得官方证书、奖杯、价值500元奖品。</p>
                        <p>2.截止至12月20日23:59，总榜前10%作品进入年度专家评审（各科最高取100份）。</p>
                        <p>备注：</p>
                        <p>1.若单个作品重复上榜，则取最高榜单项发放。</p>
                        <p>2.如果有分数并列，依次按照作品分数、人气值、评分次数、分享次数、下载次数降序排序。</p>
                        <p>3.如果作品的评价人数排在所有作品的后10%，且评价人数少于10人，此作品不参与月榜榜单奖励，不进入年度专家评审。</p>
                        <p>如存在恶意刷票等作弊行为，一经证实，将取消评选资格，一起教育科技对本活动拥有最终解释权。</p>
                    </div>
                    <!--大众达人榜-->
                    <div class="detailContent" data-bind="visible: activeBigRankType() === 2">
                        <p style="font-weight: bold;">点评达人榜</p>
                        <p>榜单维度：日榜、周榜、总榜</p>
                        <p>日榜：每天展示前一天从00:00至23:59的达人值。</p>
                        <p>周榜：每周一展示上周从周一00:00至周日23:59的达人值。</p>
                        <p>总榜：实时展示截止至当日前一天23:59的达人值。</p>
                        <p>榜单奖励：每周周榜前五的老师，评为“点评达人”，获得价值199元大礼包。</p>
                        <p>达人值统计规则：</p>
                        <p>1.老师每评价1次，或下载1次，或分享1次，统计1个达人值；</p>
                        <p>2.从“一起小学老师”APP内的作品页分享作品才有效；</p>
                        <p>3.同一个老师对同一个作品的分享和下载分别只计算一次，对同一个作品只能评价一次。</p>
                        <p>备注：</p>
                        <p>1.每个老师最多可获得3次“点评达人”的机会，超过不再重复发奖。</p>
                        <p>2.如果有分数并列，依次按照评价、分享、下载次数降序排序。</p>
                        <p>如存在恶意刷票等作弊行为，一经证实，将取消评选资格，一起教育科技对本活动拥有最终解释权。</p>
                    </div>
                    <div class="agreeBtn" data-bind="click: function(){$root.showRuleAlert(false);}">知道了</div>
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