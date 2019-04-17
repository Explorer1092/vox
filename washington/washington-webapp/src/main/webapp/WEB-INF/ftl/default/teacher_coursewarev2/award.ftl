<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner", "award", "awardCourses", "awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"award" : "public/script/teacher_coursewarev2/award",
"awardCourses" : "public/script/teacher_coursewarev2/awardCourses",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

<#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div class="indexBox" id="awardContent">
        <div class="courseSection courseSection-red">
            <div class="secInner">
                <!-- 规则说明 -->
                <div class="awardRule clearfix">
                    <p>“首届信息化教学设计大赛”历时3个月，完美落幕！活动共吸引2万名老师报名，展出2514件优秀教学设计，包括近百件曾获国家级奖项课件。经1万名老师线上点评，11位专家专业评选，最终遴选出荣获7大奖项作品。欢迎全体老师观摩学习、下载使用！</p>
                    <p>&nbsp;</p>
                    <p>不忘初心，砥砺前行！一起教育科技诚邀各位老师，继续贯彻落实教育信息化精神，做“互联网+”时代的教学名师！</p>
                    <#--<span class="expandBtn fr">展开全部</span>-->
                </div>
                <!-- 获奖作品 -->
                <div class="awardMain">
                    <!-- 学科导航 -->
                    <ul class="subjectNav clearfix" data-bind="visible: subjectList().length" style="display: none;">
                        <!-- ko foreach: subjectList -->
                        <li data-bind="
                            text: $data,
                            click: $root.choiceSubject.bind($data, $index()),
                            css: {'active': $root.choiceSubjectIndex() == $index()}"></li>
                        <!-- /ko -->
                    </ul>
                    <!-- 获奖详情 -->
                    <!-- 01 最具信息化精神作品 -->
                    <div class="detailPart" data-bind="visible: informationAwardInfo().courseId" style="display:none;">
                        <div class="pTitle">
                            <p>最具信息化<br>精神作品</p>
                        </div>
                        <div class="commentBox commentBox02 clearfix">
                            <!-- 作品信息 -->
                            <div class="awardWork fl" data-bind="click: $root.toDetailPage.bind($data, $root.informationAwardInfo())">
                                <div class="worksPic">
                                    <img data-bind="attr: {src: informationAwardInfo().coverUrl && informationAwardInfo().coverUrl.indexOf('oss-image.17zuoye') > -1 ? (informationAwardInfo().coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : informationAwardInfo().coverUrl}" alt="封面图">
                                </div>
                                <div class="worksInfo">
                                    <div class="info-title">
                                        <span class="workname" data-bind="text: informationAwardInfo().title"></span>
                                        <span class="ticketNum" data-bind="text: informationAwardInfo().votesNum + '票'"></span>
                                    </div>
                                    <div class="info-school">
                                        <span class="school-name" data-bind="text: informationAwardInfo().schoolName"></span>
                                        <span class="name" data-bind="text: informationAwardInfo().teacherName"></span>
                                    </div>
                                    <div class="award-box"><i class="excellent_icon" data-bind="visible: informationAwardInfo().highScore"></i><i class="popular_icon" data-bind="visible: informationAwardInfo().popularity"></i></div>
                                </div>
                            </div>
                            <!-- 专家点评 -->
                            <div class="awardReview awardReviewFirst">
                                <div class="expertInfo">
                                    <p class="label">专家点评</p>
                                    <div class="starBox">
                                        <span class="score" data-bind="text: informationAwardInfo().fraction"></span>
                                        <!-- 点亮星星 5种情况 -->
                                        <div class="info-star yellow-star" data-bind="css: 'star0' + Math.floor(informationAwardInfo().fraction / 22)">
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                        </div>
                                    </div>
                                </div>
                                <p class="detailText" data-bind="text: informationAwardInfo().comments"></p>
                            </div>
                        </div>
                    </div>

                    <!-- 02 最具创新智慧设计作品 -->
                    <div class="detailPart" data-bind="visible: innovationAwardInfo().length" style="display: none;">
                        <div class="pTitle">
                            <p>最具创新智慧<br>设计作品</p>
                        </div>
                        <div class="awardWorkBox">
                            <div class="swiper-container innovateSwiper02">
                                <div class="swiper-wrapper">
                                    <!-- ko foreach: innovationAwardInfo() -->
                                    <div class="swiper-slide swiper-no-swiping" data-bind="click: $root.toDetailPage.bind($data)">
                                        <div class="awardWork">
                                            <div class="worksPic">
                                                <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                            </div>
                                            <div class="worksInfo">
                                                <div class="info-title">
                                                    <span class="workname" data-bind="text: title"></span>
                                                    <span class="ticketNum" data-bind="text: votesNum + '票'"></span>
                                                </div>
                                                <div class="info-school">
                                                    <span class="school-name" data-bind="text: schoolName"></span>
                                                    <span class="name" data-bind="text: teacherName"></span>
                                                </div>
                                                <div class="award-box"><i class="excellent_icon" data-bind="visible: highScore"></i><i class="popular_icon" data-bind="visible: popularity"></i></div>
                                            </div>
                                            <div class="commentBtn" data-bind="
                                                text: $index() === $root.choiceInnovationCourseIndex() ? '收起点评' : '专家点评',
                                                css: {'active': $index() === $root.choiceInnovationCourseIndex()},
                                                click: $root.seeInnovationReview.bind($data, $index())"></div>
                                        </div>
                                    </div>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <!-- ko if: innovationAwardInfo().length > 3 -->
                            <!-- 左点击按钮 -->
                            <div class="awardArrow leftArrow" data-bind="click: clickSwipePrev.bind($data, 2)"></div>
                            <!-- 右点击按钮 -->
                            <div class="awardArrow rightArrow" data-bind="click: clickSwipeNext.bind($data, 2)"></div>
                            <!-- /ko -->
                        </div>
                        <!-- 专家点评 -->
                        <div class="commentBox" data-bind="visible: choiceInnovationCourseIndex() > -1">
                            <div class="awardReview">
                                <div class="expertInfo clearfix">
                                    <p class="label fl">专家点评</p>
                                    <div class="starBox">
                                        <span class="score" data-bind="text: choiceInnovationCourse().fraction"></span>
                                        <!-- 点亮星星 5种情况 -->
                                        <div class="info-star yellow-star" data-bind="css: 'star0' + Math.floor(choiceInnovationCourse().fraction / 22)">
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                        </div>
                                    </div>
                                </div>
                                <p class="detailText" data-bind="text: choiceInnovationCourse().comments"></p>
                            </div>
                        </div>
                    </div>

                    <!-- 03 最具资源整合能力作品 -->
                    <div class="detailPart" data-bind="visible: resourceAwardInfo().length" style="display:none;">
                        <div class="pTitle">
                            <p>最具资源整合<br>能力作品</p>
                        </div>
                        <div class="awardWorkBox">
                            <div class="swiper-container innovateSwiper03">
                                <div class="swiper-wrapper">
                                    <!-- ko foreach: resourceAwardInfo() -->
                                    <div class="swiper-slide swiper-no-swiping" data-bind="click: $root.toDetailPage.bind($data)">
                                        <div class="awardWork">
                                            <div class="worksPic">
                                                <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                            </div>
                                            <div class="worksInfo">
                                                <div class="info-title">
                                                    <span class="workname" data-bind="text: title"></span>
                                                    <span class="ticketNum" data-bind="text: votesNum + '票'"></span>
                                                </div>
                                                <div class="info-school">
                                                    <span class="school-name" data-bind="text: schoolName"></span>
                                                    <span class="name" data-bind="text: teacherName"></span>
                                                </div>
                                                <div class="award-box"><i class="excellent_icon" data-bind="visible: highScore"></i><i class="popular_icon" data-bind="visible: popularity"></i></div>
                                            </div>
                                            <div class="commentBtn" data-bind="
                                                text: $index() === $root.choiceResourceCourseIndex() ? '收起点评' : '专家点评',
                                                css: {'active': $index() === $root.choiceResourceCourseIndex()},
                                                click: $root.seeResourceReview.bind($data, $index())"></div>
                                        </div>
                                    </div>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <!-- ko if: resourceAwardInfo().length > 3 -->
                            <!-- 左点击按钮 -->
                            <div class="awardArrow leftArrow" data-bind="click: clickSwipePrev.bind($data, 3)"></div>
                            <!-- 右点击按钮 -->
                            <div class="awardArrow rightArrow" data-bind="click: clickSwipeNext.bind($data, 3)"></div>
                            <!-- /ko -->
                        </div>
                        <!-- 专家点评 -->
                        <div class="commentBox" data-bind="visible: choiceResourceCourseIndex() > -1">
                            <div class="awardReview">
                                <div class="expertInfo clearfix">
                                    <p class="label fl">专家点评</p>
                                    <div class="starBox">
                                        <span class="score" data-bind="text: choiceResourceCourse().fraction"></span>
                                        <!-- 点亮星星 5种情况 -->
                                        <div class="info-star yellow-star" data-bind="css: 'star0' + Math.floor(choiceResourceCourse().fraction / 22)">
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                        </div>
                                    </div>
                                </div>
                                <p class="detailText" data-bind="text: choiceResourceCourse().comments"></p>
                            </div>
                        </div>
                    </div>

                    <!-- 04 排行榜获奖作品 /优秀教学作品 -->
                    <div class="detailPart" data-bind="visible: excellentYearAwardInfo().length" style="display: none">
                        <div class="pTitle pTitle02">
                            <p>排行榜获奖作品</p>
                        </div>
                        <div class="typeTitle clearfix">
                            <span class="workTitle">优秀教学作品</span>
                            <span class="source">数据来源：高分作品排行榜</span>
                            <div class="fr timeNav">
                                <span data-bind="
                                    css: {'active': choiceExcellentTypeIndex() === 0},
                                    click: choiceExcellentType.bind($data, 0)">年度</span>
                                <span data-bind="
                                    css: {'active': choiceExcellentTypeIndex() === 1},
                                    click: choiceExcellentType.bind($data, 1)">月度</span>
                            </div>
                        </div>
                        <!-- 作品信息-年度 -->
                        <div data-bind="visible: choiceExcellentTypeIndex() === 0">
                            <div class="awardWorkBox awardWorkBox02 clearfix">
                                <!-- ko foreach: excellentYearAwardInfo().slice(choiceExcellentYearPageIndex() * 6, (choiceExcellentYearPageIndex() + 1) * 6) -->
                                <div class="awardWork fl" data-bind="click: $root.toDetailPage.bind($data)">
                                    <#--<div class="rankNum">1</div>-->
                                    <div class="worksPic">
                                        <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title">
                                            <span class="workname" data-bind="text: title"></span>
                                            <#--<span class="ticketNum" data-bind="text: votesNum + '票'"></span>-->
                                        </div>
                                        <div class="info-school">
                                            <span class="school-name" data-bind="text: schoolName"></span>
                                            <span class="name" data-bind="text: teacherName"></span>
                                        </div>
                                        <div class="award-box"><i class="excellent_icon" data-bind="visible: highScore"></i><i class="popular_icon" data-bind="visible: popularity"></i></div>
                                    </div>
                                </div>
                                <!-- /ko -->
                            </div>
                            <!-- 翻页导航 -->
                            <ul class="pagesNav clearfix" id="JS-pagination"></ul>
                        </div>
                        <!-- 作品信息-月度 -->
                        <div data-bind="visible: choiceExcellentTypeIndex() === 1">
                            <div class="awardWorkBox awardWorkBox02 clearfix">
                                <#-- 根据当前的期数索引取对应的区间 -->
                                <!-- ko foreach: excellentMonthAwardInfo().courseware.slice(choiceExcellentMonthPeriodIndex() * 3, (choiceExcellentMonthPeriodIndex() + 1) * 3) -->
                                <div class="awardWork fl" data-bind="click: $root.toDetailPage.bind($data)">
                                    <!-- rankNum01 rankNum02 rankNum03分别对应1,2,3名 -->
                                    <div class="rankNum" data-bind="
                                        text: $index() + 1,
                                        css: 'rankNum0' + ($index() + 1)"></div>
                                    <div class="worksPic">
                                        <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title">
                                            <span class="workname" data-bind="text: title"></span>
                                            <#--<span class="ticketNum" data-bind="text: votesNum + '票'"></span>-->
                                        </div>
                                        <div class="info-school">
                                            <span class="school-name" data-bind="text: schoolName"></span>
                                            <span class="name" data-bind="text: teacherName"></span>
                                        </div>
                                        <div class="award-box"><i class="excellent_icon" data-bind="visible: highScore"></i><i class="popular_icon" data-bind="visible: popularity"></i></div>
                                    </div>
                                </div>
                                <!-- /ko -->
                            </div>
                            <!-- 点评按钮 -->
                            <div class="commentBtnBox">
                                <!-- ko foreach: excellentMonthAwardInfo().timeList -->
                                <div class="commentBtn" data-bind="
                                     text: $data,
                                     css: {'active': $index() === $root.choiceExcellentMonthPeriodIndex()},
                                     click: $root.choiceExcellentWeekPeriod.bind($data, $index())"></div>
                                <!-- /ko -->
                            </div>
                        </div>
                    </div>

                    <!-- 04 排行榜获奖作品 /最具人气作品 -->
                    <div class="detailPart" data-bind="visible: popularYearAwardInfo().length" style="display: none">
                        <div class="typeTitle clearfix">
                            <span class="workTitle">最具人气作品</span>
                            <span class="source">数据来源：人气作品排行榜</span>
                            <div class="fr timeNav">
                                <span data-bind="
                                    css: {'active': choicePopularTypeIndex() === 0},
                                    click: choicePopularType.bind($data, 0)">年度</span>
                                <span data-bind="
                                    css: {'active': choicePopularTypeIndex() === 1},
                                    click: choicePopularType.bind($data, 1)">每周</span>
                            </div>
                        </div>
                        <!-- 作品信息-年度 -->
                        <div data-bind="visible: choicePopularTypeIndex() === 0">
                            <div class="awardWorkBox awardWorkBox02 clearfix">
                                <!-- ko foreach: popularYearAwardInfo() -->
                                <div class="awardWork fl" data-bind="click: $root.toDetailPage.bind($data)">
                                    <!-- rankNum01 rankNum02 rankNum03分别对应1,2,3名 -->
                                    <div class="rankNum rankNum01" data-bind="
                                        text: $index() + 1,
                                        css: 'rankNum0' + ($index() + 1)"></div>
                                    <div class="worksPic">
                                        <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title">
                                            <span class="workname" data-bind="text: title"></span>
                                            <#--<span class="ticketNum" data-bind="text: votesNum + '票'"></span>-->
                                        </div>
                                        <div class="info-school">
                                            <span class="school-name" data-bind="text: schoolName"></span>
                                            <span class="name" data-bind="text: teacherName"></span>
                                        </div>
                                        <div class="award-box"><i class="excellent_icon" data-bind="visible: highScore"></i><i class="popular_icon" data-bind="visible: popularity"></i></div>
                                    </div>
                                </div>
                                <!-- /ko -->
                            </div>
                        </div>
                        <!-- 作品信息-每周 -->
                        <div data-bind="visible: choicePopularTypeIndex() === 1">
                            <div class="awardWorkBox awardWorkBox02 clearfix">
                                <#-- 根据当前的期数索引取对应的区间 -->
                                <!-- ko foreach: popularWeekAwardInfo().courseware.slice(choicePopularWeekPeriodIndex() * 3, (choicePopularWeekPeriodIndex() + 1) * 3) -->
                                <div class="awardWork fl" data-bind="click: $root.toDetailPage.bind($data)">
                                    <!-- rankNum01 rankNum02 rankNum03分别对应1,2,3名 -->
                                    <div class="rankNum rankNum01" data-bind="
                                        text: $index() + 1,
                                        css: 'rankNum0' + ($index() + 1)"></div>
                                    <div class="worksPic">
                                        <img data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}" alt="封面图">
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title">
                                            <span class="workname" data-bind="text: title"></span>
                                            <#--<span class="ticketNum" data-bind="text: votesNum + '票'"></span>-->
                                        </div>
                                        <div class="info-school">
                                            <span class="school-name" data-bind="text: schoolName"></span>
                                            <span class="name" data-bind="text: teacherName"></span>
                                        </div>
                                        <div class="award-box"><i class="excellent_icon" data-bind="visible: highScore"></i><i class="popular_icon" data-bind="visible: popularity"></i></div>
                                    </div>
                                </div>
                                <!-- /ko -->
                            </div>
                            <!-- 点评按钮 -->
                            <div class="commentBtnBox commentBtnBox02 clearfix">
                                <!-- ko foreach: popularWeekAwardInfo().timeList -->
                                <div class="commentBtn active" data-bind="
                                    text: $data,
                                    css: {'active': $index() === $root.choicePopularWeekPeriodIndex()},
                                    click: $root.choicePopularWeekPeriod.bind($data, $index())"></div>
                                <!-- /ko -->
                            </div>
                        </div>
                    </div>

                    <!-- 05 点评专家 -->
                    <div class="detailPart detailPart02" data-bind="visible: professorList().length" style="display: none">
                        <div class="pTitle pTitle02">
                            <p>点评专家</p>
                        </div>
                        <div class="awardWorkBox expertsBox">
                            <div class="swiper-container innovateSwiper07">
                                <div class="swiper-wrapper">
                                    <!-- ko foreach: professorList -->
                                    <div class="swiper-slide swiper-no-swiping">
                                        <div class="awardWork">
                                            <img class="expert-avatar" data-bind="attr: {src: avatar}" alt="">
                                            <p class="expert-name" data-bind="text: name"></p>
                                            <p class="job-name" data-bind="text: job"></p>
                                        </div>
                                    </div>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <!-- ko if: professorList().length > 3 -->
                            <!-- 左点击按钮 -->
                            <div class="awardArrow leftArrow" data-bind="click: clickSwipePrev.bind($data, 7)"></div>
                            <!-- 右点击按钮 -->
                            <div class="awardArrow rightArrow" data-bind="click: clickSwipeNext.bind($data, 7)"></div>
                            <!-- /ko -->
                        </div>
                    </div>

                    <!-- 回到顶部按钮 -->
                    <div class="returntopBtn" id="gotoTop"></div>
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
    var awardCourses = null; // 获奖课件信息
    var professorList = null; // 评委信息
    var awardTeachers = null; // 获奖老师名单
</script>
</@layout.page>