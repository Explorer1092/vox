<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "banner", "index", "awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"banner" : "public/script/teacher_coursewarev2/banner",
"index" : "public/script/teacher_coursewarev2/index",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>

<#include "./module/getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-2.7.6/swiper.css')}">

<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <#include "./module/banner.ftl">
    <#include "./module/nav.ftl">
    <div class="indexBox" id="indexContent">
        <!-- 活动介绍 -->
        <div class="courseSection">
            <div class="secInner">
                <div class="secBox secBox01">
                    <div class="secTitle title01"></div>
                    <div class="secContent secParaBox">
                    <#--<div class="text">首届小学语数英教师信息化教学设计展示活动（以下简称“活动”），以“信息化教学”为基调，需参与教师提交一个真实施教过的完整教学课时教学设计方案，方案包括：教案（模板见“附件”）、配套课件ppt以及实施该教学设计的5分钟课堂实录视频，该方案应从以下四个维度反映教师的教学特色及能力：</div>-->
                        <div class="txtDetail">
                            <p class="text">当互联网浪潮汹涌而来，教师是否做好了准备？</p>
                            <p class="text">当教育迈入信息2.0时代，教师该如何适应新的挑战？</p>
                            <p class="text">当大数据、人工智能越来越多地进入到课堂，教师如何善用信息技术？</p>
                        </div>
                        <div class="txtDetail">
                            <p class="text">为推动信息技术与教育教学的融合创新，中国教师报、中国电化教育杂志社联合一起教育科技共同发起首届小学语数英信息化教学设计展示活动，老师只需以“信息化教学”为基调，提交一个真实施教过的完整教学课时教学设计方案，包括：教案（<a class="needtrack downloadTemplate" data_op="o_1Mvdtjj1tf" href="javascript:void(0);" style="color: #2d0aff;">下载模版</a>）、配套课件ppt、实施该教学方案的5张课堂照片，本活动对老师免费开放，现诚邀各位老师积极参与。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 奖励说明 -->
        <div class="courseSection courseSection-yellow courseSection-award">
            <div class="secInner">
                <div class="secBox">
                    <div class="secTitle title06"></div>
                    <div class="secContent">
                        <ul class="awardList">
                            <li>
                                <div class="awardPic">
                                <#--<p style="font-size: 16px;margin-top:-20px;"><span class="boldTxt">赴国外或港、澳、台进修机会元奖金</span></p>-->
                                    <p>价值<span class="redTxt">10000</span>元奖品</p>
                                    <p>参与证书、奖杯</p>
                                <#--<p>价值<span class="boldTxt">3000</span>元奖品</p>-->
                                </div>
                                <div class="awardName boldTxt">最具信息化教育精神作品</div>
                            </li>
                            <li>
                                <div class="awardPic">
                                    <p>价值<span class="redTxt">3000</span>元奖品</p>
                                    <p>参与证书、奖杯</p>
                                <#--<p>价值<span class="boldTxt">3000</span>元奖品</p>-->
                                </div>
                                <div class="awardName boldTxt">最具创新智慧设计作品</div>
                            </li>
                            <li>
                                <div class="awardPic">
                                <#--<p><span class="redTxt">2000</span>元奖金</p>-->
                                    <p>价值<span class="redTxt">2000</span>元奖品</p>
                                    <p>参与证书、奖杯</p>
                                </div>
                                <div class="awardName boldTxt">最具资源整合能力作品</div>
                            </li>
                            <li>
                                <div class="awardPic">
                                <#--<p><span class="redTxt">1500</span>元奖金</p>-->
                                    <p>价值<span class="redTxt">1000</span>元奖品</p>
                                    <p>参与证书、奖杯</p>
                                </div>
                                <div class="awardName boldTxt">年度最具人气作品</div>
                            </li>
                            <li>
                                <div class="awardPic">
                                <#--<p><span class="redTxt">800</span>元奖金</p>-->
                                    <p>价值<span class="redTxt">800</span>元奖品</p>
                                    <p>参与证书、奖杯</p>
                                </div>
                                <div class="awardName boldTxt">年度优秀教学设计作品</div>
                            </li>
                            <li>
                                <div class="awardPic">
                                <#--<p><span class="redTxt">2000</span>元奖金</p>-->
                                    <p>价值<span class="redTxt">500</span>元奖品</p>
                                    <p>参与证书、奖杯</p>
                                </div>
                                <div class="awardName boldTxt">月度优秀教学设计作品</div>
                            </li>
                            <li>
                                <div class="awardPic">
                                <#--<p><span class="redTxt">2000</span>元奖金</p>-->
                                    <p>价值<span class="redTxt">300</span>元奖品</p>
                                    <p>参与证书、奖杯</p>
                                </div>
                                <div class="awardName boldTxt">每周最具人气作品</div>
                            </li>
                            <li>
                                <div class="awardPic">
                                <#--<p><span class="redTxt">2000</span>元奖金</p>-->
                                    <p>价值<span class="redTxt">199</span>元大礼包</p>
                                </div>
                                <div class="awardName boldTxt">点评达人</div>
                            </li>
                        </ul>
                        <div class="awardExplain certificateExplain">
                            <div class="certificate-box">
                                <p class="title">《参与证书》模板</p>
                                <div class="certificate"></div>
                            </div>
                            <div class="intro-box">
                                <div class="e-title">特别说明</div>
                                <div class="e-list" style="font-weight: bold;">1. 凡审核通过的作品均可获得主办方颁发的《参与证书》。</div>
                                <div class="e-list">2. 各阶段奖项的作品均可获得主办方颁发的《参与证书》、奖杯、不同价值奖品。</div>
                                <div class="e-list">3. 年度优秀作品全程在主办方和承办方官方网站展示。</div>
                                <div class="e-list">4. 部分优秀教师将有机会应邀参加线下教学设计活动交流会。</div>
                                <div class="e-list">5. 年度优秀作品将由组委会编辑成册，正式出版。</div>
                                <div class="e-list">6. 年度优秀作品数量：按各学科实际参与作品数量的10%进行遴选，各学科最高遴选前100名优秀作品。</div>
                                <div class="e-list" style="font-weight: bold;">7. 为保证评价真实有效性，认证老师评分占70%权重，非认证老师评分占30%权重。</div>
                                <div class="e-list">8. 单个作品取最高奖项颁发，不重复发奖</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 专家评审 -->
        <div class="courseSection">
            <div class="secInner">
                <div class="secBox">
                    <div class="secTitle title03"></div>
                    <div class="secContent">
                        <!-- 专家列表 -->
                        <ul class="expertBox clearfix" data-bind="visible: professorList().length > 0" style="display: none;">
                            <!-- ko foreach: professorList -->
                            <li>
                                <div class="expertAvatar">
                                    <img alt="专家头像" data-bind="attr: { src: avatar }">

                                    <div class="hover-statebox">
                                        <div class="hover-arrow"></div>
                                        <div class="hover-state">
                                            <p class="name" data-bind="text: name"></p>
                                            <p class="job" data-bind="html: identity"></p>
                                            <p class="descrption" data-bind="html: description"></p>
                                        </div>
                                    </div>
                                </div>
                                <div class="expertName" data-bind="text: name"></div>
                                <div class="expertJob" data-bind="html: identity"></div>
                            </li>
                            <!-- /ko -->
                        </ul>
                        <!-- 团队导航列表 -->
                        <ul class="teamNav clearfix">
                            <!-- ko foreach: professorTypeList -->
                            <li data-bind="
                                text: $data,
                                css: { 'active': $index() === $root.professorTypeIndex() },
                                click: $root.switchProfessorType.bind($data, $index())
                            "></li>
                            <!-- /ko -->
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <!-- 参赛作品 -->
        <div class="courseSection courseSection-yellow" data-bind="visible: isShowTopThree">
            <div class="secInner">
                <div class="secBox secBox-works">
                    <div class="secTitle title04"></div>
                    <div class="courseRankContent" data-bind="visible: topThreeCourseList().length" style="display: none">
                        <!-- ko foreach: {data: topThreeCourseList, as: 'subjectCourse'} -->
                        <div class="worksPart clearfix" >
                            <div class="worksKind col01" data-bind="css: subjectCourse.englishName.toLowerCase()">
                                <div class="rankTime" data-bind="text: $root.topThreeTimeRange"></div>
                            </div>
                            <ul class="worksList clearfix notFull">
                                <!-- ko foreach: {data: subjectCourse.topthreeList, as: 'course'} -->
                                <li class="col01 col02 hasHover" data-bind="click: $root.toDetailPage">
                                    <div class="worksPic">
                                        <img alt="封面图" data-bind="attr: {src: course.coverUrl && course.coverUrl.indexOf('oss-image.17zuoye') > -1 ? (course.coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : course.coverUrl}">
                                        <span class="award_tip" data-bind="text: (awardLevelId === 5 ? '历史获奖作品' : ('曾获' + awardLevelName + '奖项')), visible: awardLevelId && awardLevelId > 0" style="display: none;"></span>
                                        <div class="operateBox">
                                            <div class="item">
                                                <i class="icon-look"></i>
                                                <span data-bind="text: course.visitNum"></span>
                                            </div>
                                            <div class="item item03">
                                                <i class="icon-download"></i>
                                                <span data-bind="text: course.downloadNum"></span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="worksInfo">
                                        <div class="info-title" data-bind="text: course.title, attr: {title: course.title}"></div>
                                        <div class="info-school">
                                            <span class="school-name" data-bind="text: course.schoolName, attr: {title: course.schoolName}"></span>
                                            <span class="name" data-bind="text: course.teacherName"></span>
                                        </div>
                                        <div class="info-time" data-bind="text: course.createDate"></div>
                                        <!-- 点亮星星 5种情况 -->
                                        <div class="info-star" data-bind="css: 'star0' + Math.floor(course.totalScore / 20)">
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <span class="star"></span>
                                            <i class="score" data-bind="text: course.totalScore"></i>
                                            <i class="comment" data-bind="text: course.commentNum + '评价'"></i>
                                        </div>
                                    </div>
                                    <!-- prizeSign01 prizeSign02 prizeSign03 分别对应1,2,3名 -->
                                    <div class="prizeSign" data-bind="css: 'prizeSign0' + ($index() + 1)"></div>
                                </li>
                                <!-- /ko -->
                            </ul>
                        </div>
                        <!-- /ko -->
                    </div>
                    <!-- 查看更多 -->
                    <div class="worksMore" data-bind="click: seeMoreCourse">查看更多</div>
                </div>
            </div>
        </div>
        <!-- 合作单位 -->
        <div class="courseSection">
            <div class="secInner">
                <div class="secBox secBox-cooperate">
                    <div class="cooperatePic"></div>
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