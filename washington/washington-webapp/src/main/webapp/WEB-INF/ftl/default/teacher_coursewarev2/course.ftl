<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner", "course", "awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"course" : "public/script/teacher_coursewarev2/course",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

<#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div class="indexBox" id="courseContent">
        <div class="allWorksBox">
            <!-- 筛选类别 -->
            <div class="courseSection">
                <div class="secInner choiceNavBox">
                    <div class="searchBox">
                        <input type="text" placeholder="请输入想要搜索的资源" class="inputKeyword" data-bind="
                            value: inputKeyWord,
                            valueUpdate: 'afterkeydown',
                            event: { keyup: inputKeyWordKeyUp }"><i class="searchIcon" data-bind="click: searchKeyWord"></i>
                    </div>
                    <div class="choiceList">
                        <span class="label">选择学科：</span>
                        <div class="choiceItem">
                            <!-- ko foreach: subjectList -->
                            <span data-bind="
                                text: name,
                                css: { 'active': $root.choiceSubjectInfo().id === id },
                                click: $root.choiceSubject"></span>
                            <!-- /ko -->
                        </div>
                    </div>
                    <div class="choiceList">
                        <span class="label">选择年级：</span>
                        <div class="choiceItem">
                            <!-- ko foreach: gradeList -->
                            <span data-bind="
                                text: name,
                                css: { 'active': $root.choiceGradeInfo().id === id },
                                click: $root.choiceGrade"></span>
                            <!-- /ko -->
                        </div>
                    </div>
                    <div class="choiceList">
                        <span class="label">选择获奖级别：</span>
                        <div class="choiceItem">
                            <!-- ko foreach: awardList -->
                            <span data-bind="
                                text: name,
                                css: { 'active': $root.choiceAwardInfo().id === id },
                                click: $root.choiceAward"></span>
                            <!-- /ko -->
                        </div>
                    </div>
                </div>
            </div>
            <!-- 我的作品 -->
            <div class="courseSection courseSection-yellow">
                <div class="secInner">
                    <div class="secBox secBox-works">
                        <div class="worksPart my-worksPart clearfix">
                            <p class="search-result" data-bind="visible: serarchKeyWordFlag()">共<span data-bind="text: totalNum"></span>条相关搜索</p>
                            <!-- 我的作品导航 -->
                            <ul class="clearfix worksNav worksNav02">
                                <!-- ko foreach: orderList -->
                                <li data-bind="
                                    text: tabName,
                                    css: { 'active': $root.choiceOrderInfo().tabStatus === tabStatus },
                                    click: $root.choiceOrder"></li>
                                <!-- /ko -->
                            </ul>
                            <!-- 我的作品列表 -->
                            <ul class="clearfix worksList allWorksList" data-bind="visible: courseList().length" style="display: none;">
                                <!-- ko foreach: courseList -->
                                <li class="col01 col02 col03 hasHover" data-bind="click: $root.toDetailPage">
                                    <div class="worksPic">
                                        <img alt="封面图" data-bind="attr: {src: coverUrl && coverUrl.indexOf('oss-image.17zuoye') > -1 ? (coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : coverUrl}">
                                        <span class="award_tip" data-bind="text: (awardLevelId === 5 ? '历史获奖作品' : ('曾获' + awardLevelName + '奖项')), visible: awardLevelId && awardLevelId > 0" style="display: none;"></span>
                                        <div class="operateBox">
                                            <#--评论数-->
                                            <#--<div class="item">-->
                                                <#--<i class="icon-assess"></i>-->
                                                <#--<span data-bind="text: commentNum || 0"></span>-->
                                            <#--</div>-->
                                            <#--阅读数-->
                                            <div class="item">
                                                <i class="icon-look"></i>
                                                <span  data-bind="text: visitNum || 0"></span>
                                            </div>
                                            <#--下载数-->
                                            <div class="item item03">
                                                <i class="icon-download"></i>
                                                <span data-bind="text: downloadNum || 0"></span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title" data-bind="text: title, attr: {title: title}"></div>
                                        <div class="award-box"><i class="excellent_icon" data-bind="visible: monthExcellentTop3 && monthExcellentRank > 0 && monthExcellentRank <= 3"></i><i class="popular_icon" data-bind="visible: weekPopularityTop3 && weekPopularityRank > 0 && weekPopularityRank <= 3"></i></div>
                                        <div class="info-school">
                                            <span class="school-name" data-bind="text: schoolName, attr: {title: schoolName}"></span>
                                            <span class="name" data-bind="text: teacherName"></span>
                                        </div>
                                        <div class="info-time" data-bind="text: createDate"></div>
                                        <!-- 点亮星星 5种情况(总分100，20分一颗星) -->
                                        <div class="info-star" data-bind="css: 'star0' + Math.floor(totalScore / 20)">
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <i class="score" data-bind="text: totalScore"></i>
                                            <i class="comment" data-bind="text: commentNum + '条评价'"></i>
                                        </div>
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