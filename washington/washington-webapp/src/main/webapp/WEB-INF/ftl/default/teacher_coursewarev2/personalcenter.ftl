<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner", "personalcenter", "awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"personalcenter" : "public/script/teacher_coursewarev2/personalcenter",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

<#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div class="indexBox" id="personalcenterContent">
        <div class="personalCenterBox">
            <!-- 个人资料 -->
            <div class="courseSection courseSection-selfInfo">
                <div class="secInner">
                    <div class="secBox">
                        <div class="secTitle title08"></div>
                        <div class="secContent">
                            <div class="leftInfo">
                                <div class="infoList">
                                    <div class="infoPart">
                                        <span class="label">姓 名：</span>
                                        <span class="text personalcenterUserName"></span>
                                    </div>
                                    <div class="infoPart">
                                        <span class="label">作品总数：</span>
                                        <span class="text personalcenterProductNum"></span>
                                    </div>
                                </div>
                                <div class="infoList">
                                    <div class="infoPart">
                                        <span class="label">主授学科：</span>
                                        <span class="text personalcenterSubject"></span>
                                    </div>
                                    <div class="infoPart">
                                        <span class="label">地 区：</span>
                                        <span class="text personalcenterRegionName"></span>
                                    </div>
                                </div>
                                <div class="infoList">
                                    <div class="infoPart fullLine">
                                        <span class="label">学 校：</span>
                                        <span class="text personalcenterSchoolName"></span>
                                    </div>
                                </div>
                                <div class="infoList personalcenterAward" style="display: none">
                                    <div class="infoPart fullLine">
                                        <span class="label">获奖记录：</span>
                                        <span class="text personalcenterAwardName" style="line-height:1.5em;"></span>
                                    </div>
                                </div>
                            </div>
                            <div class="addBtn needtrack createCourse" data_op="o_7DW3rYNJkC" style="display: none;"></div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 我的作品 -->
            <div class="courseSection courseSection-yellow">
                <div class="secInner">
                    <div class="secBox secBox-works">
                        <div class="secTitle title09"></div>
                        <div class="worksPart my-worksPart clearfix">
                            <!-- 我的作品导航 -->
                            <ul class="worksNav clearfix">
                                <!-- ko foreach: myCoursewareTabList -->
                                <li data-bind="
                            text: tabName,
                            css: { 'active': $index() === $root.myCoursewareTabIndex() },
                            click: $root.switchMycourseState.bind($data, $index())"></li>
                                <!-- /ko -->
                            </ul>

                            <!-- 我的作品列表 -->
                            <ul class="worksList clearfix" data-bind="visible: myCoursewareList().length" style="display: none;">
                                <!-- ko foreach: myCoursewareList() -->

                                <!-- ko if: status === 'PUBLISHED' -->
                                <!-- 已发布(PUBLISHED) -->
                                <li class="col01 hasHover" data-bind="css: {'child04': ($index() + 1) % 4 === 0}, click: $root.toDetailPage">
                                    <div class="worksPic">
                                        <img alt="封面图" data-bind="attr: {src: cover && cover.indexOf('oss-image.17zuoye') > -1 ? (cover + '?x-oss-process=image/resize,w_250/quality,Q_100') : cover}">
                                        <span class="award_tip" data-bind="text: (awardLevelId === 5 ? '历史获奖作品' : ('曾获' + awardLevelName + '奖项')), visible: awardLevelId && awardLevelId > 0" style="display: none;"></span>
                                        <div class="operateBox">
                                            <div class="item item01">
                                                <i class="icon-look"></i>
                                                <span data-bind="text: visitNum"></span>
                                            </div>
                                            <div class="item item03">
                                                <i class="icon-download"></i>
                                                <span data-bind="text: downloadNum"></span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title" data-bind="text: title, attr: {title: title}"></div>
                                        <div class="info-school">
                                            <span class="create-time" data-bind="text: date"></span>
                                        </div>
                                        <!-- 点亮星星 5种情况 -->
                                        <div class="info-star" data-bind="css: 'star0' + Math.floor(score / 20)" style="width: 207px">
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <i class="score" data-bind="text: score"></i>
                                            <i class="comment" data-bind="text: commentNum + '条评价'"></i>
                                        </div>
                                        <div class="stateBtn">
                                            <span class="tip" data-bind="text: statusDesc"></span>
                                        </div>
                                    </div>
                                </li>
                                <!-- /ko -->

                                <!-- ko if: status === 'EXAMINING' -->
                                <!-- 审核中(EXAMINING) -->
                                <li class="col01" data-bind="css: {'child04': ($index() + 1) % 4 === 0}">
                                    <div class="worksPic">
                                        <img alt="封面图" data-bind="attr: {src: cover && cover.indexOf('oss-image.17zuoye') > -1 ? (cover + '?x-oss-process=image/resize,w_250/quality,Q_100') : cover}">
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title" data-bind="text: title, attr: {title: title}"></div>
                                        <div class="info-school">
                                            <span class="create-time" data-bind="text: date"></span>
                                        </div>
                                        <div class="stateBtn">
                                            <span class="tip" data-bind="text: statusDesc"></span>
                                        </div>
                                    </div>
                                </li>
                                <!-- /ko -->

                                <!-- ko if: status === 'DRAFT' -->
                                <!-- 未提交(DRAFT) -->
                                <li class="col01" data-bind="css: {'child04': ($index() + 1) % 4 === 0}">
                                    <div class="worksPic">
                                        <img alt="封面图" data-bind="attr: {src: cover && cover.indexOf('oss-image.17zuoye') > -1 ? (cover + '?x-oss-process=image/resize,w_250/quality,Q_100') : cover}">
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title" data-bind="text: title, attr: {title: title}"></div>
                                        <div class="info-school">
                                            <span class="create-time" data-bind="text: date"></span>
                                        </div>
                                        <!-- 编辑删除按钮 -->
                                        <div class="dealBtn">
                                            <span class="stateSign edit" data-bind="click: $root.editMyCourse.bind($data)" style="display: none;">编辑</span>
                                            <span class="stateSign" data-bind="click: $root.deleteMyCourse.bind($data)">删除</span>
                                        </div>
                                        <div class="stateBtn">
                                            <span class="tip" data-bind="text: statusDesc"></span>
                                        </div>
                                    </div>
                                </li>
                                <!-- /ko -->

                                <!-- ko if: status === 'REJECTED' -->
                                <!-- 被退回(REJECTED) -->
                                <li class="col01" data-bind="css: {'child04': ($index() + 1) % 4 === 0}">
                                    <div class="worksPic">
                                        <img alt="封面图" data-bind="attr: {src: cover && cover.indexOf('oss-image.17zuoye') > -1 ? (cover + '?x-oss-process=image/resize,w_250/quality,Q_100') : cover}">
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title" data-bind="text: title, attr: {title: title}"></div>
                                        <div class="info-school">
                                            <span class="create-time" data-bind="text: date"></span>
                                        </div>
                                        <!-- 编辑删除按钮 -->
                                        <div class="dealBtn">
                                            <span class="stateSign edit" data-bind="click: $root.editMyCourse.bind($data)" style="display: none;">编辑</span>
                                            <span class="stateSign" data-bind="click: $root.deleteMyCourse.bind($data)">删除</span>
                                        </div>
                                        <div class="stateBtn">
                                            <span class="tip reject" data-bind="text: statusDesc"></span>
                                            <span class="seeDetail" data-bind="click: $root.seeRejectDetail">查看原因</span>
                                        </div>
                                    </div>
                                </li>
                                <!-- /ko -->
                                <!-- /ko -->
                            </ul>
                            <!-- 暂无数据 -->
                            <div class="emptyListTip" data-bind="visible: !myCoursewareList().length" style="display: none; padding: 180px 0 120px; background: transparent;">
                                <div class="emptyIcon"></div>
                                <p>暂无数据</p>
                            </div>
                            <!-- 翻页导航 -->
                            <ul class="pagesNav clearfix" id="JS-pagination"></ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 删除 -->
        <div class="coursePopup" style="display: none" data-bind="visible: isShowDeleteCourseSure">
            <div class="popupInner popupInner-del">
                <div class="closeBtn" data-bind="click: function () { $root.isShowDeleteCourseSure(false); }"></div>
                <div class="textBox">
                    <p>删除后的资源将无法再次找回</p>
                    <p class="boldTxt">确定要删除这个资源吗？</p>
                </div>
                <div class="otherContent">
                    <a class="continue" href="javascript:void(0)" data-bind="click: function () { $root.isShowDeleteCourseSure(false); }">取 消</a>
                    <a class="btn02 sure" href="javascript:void(0)" data-bind="click: sureDeleteCourse">确 定</a>
                </div>
            </div>
        </div>
        <#--展示被驳回原因-->
        <div class="coursePopup" style="display: none;" data-bind="visible: isShowRejectDetail">
            <div class="popupInner popupInner-rejectdetail">
                <div class="closeBtn" data-bind="click: function () { $root.isShowRejectDetail(false); }"></div>
                <div class="textBox">作品审核未通过/被退回查看原因，内容如下：<span data-bind="html: rejectCourseInfo"></span></div>
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