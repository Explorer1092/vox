<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-fff"
title='教学设计展示活动'
pageJs=["common", "detail", "awardTeachers"]
pageJsFile={
"common" : "public/script/teacher_coursewarev2/common",
"detail" : "public/script/teacher_coursewarev2/detail",
"awardTeachers" : "public/script/teacher_coursewarev2/awardTeachers"
}
pageCssFile={"skin" : ["public/skin/teacher_coursewarev2/css/skin"]}>
<div class="coursewareWrap">
    <#include "./module/header.ftl">
    <div class="indexBox" id="detailContent">
        <!-- 顶部作品信息 -->
        <div class="courseSection">
            <div class="secInner">
                <div class="worksInfoBox">
                    <!-- 左侧信息 -->
                    <div class="infoLeft">
                        <div class="worksImg">
                            <img alt="封面图" style="display: none;" data-bind="
                                attr: {src: courseDetailInfo().coverUrl && courseDetailInfo().coverUrl.indexOf('oss-image.17zuoye') > -1 ? (courseDetailInfo().coverUrl + '?x-oss-process=image/resize,w_250/quality,Q_100') : courseDetailInfo().coverUrl},
                                visible: courseDetailInfo().coverUrl">
                            <span class="award_tip" data-bind="text: (courseDetailInfo().awardLevelId === 5 ? '历史获奖作品' : ('曾获' + courseDetailInfo().awardLevelName + '奖项')), visible: courseDetailInfo().awardLevelId && courseDetailInfo().awardLevelId > 0" style="display: none;"></span>
                        </div>
                        <div class="infoDetail" data-bind="visible: courseDetailInfo() !== {}" style="display: none;">
                            <div class="infoItem">
                                <span class="title" data-bind="text: courseDetailInfo().title, attr: {title: courseDetailInfo().title}"></span>
                                <span class="time" data-bind="text: courseDetailInfo()._formatDate"></span>
                            </div>
                            <div class="award-box"><i class="excellent_icon" data-bind="visible: courseDetailInfo().monthExcellentTop3 && courseDetailInfo().monthExcellentRank > 0 && courseDetailInfo().monthExcellentRank <= 3"></i><i class="popular_icon" data-bind="visible: courseDetailInfo().weekPopularityTop3  && courseDetailInfo().weekPopularityRank > 0 && courseDetailInfo().weekPopularityRank <= 3"></i></div>
                            <div class="infoItem grade">
                                <span class="edition" data-bind="text: courseDetailInfo()._descriptionInfo, attr: {title: courseDetailInfo()._descriptionInfo}"></span>
                            </div>
                            <div class="infoItem name" data-bind="text: courseDetailInfo().teacherName + ' ' + courseDetailInfo().schoolName, attr:{title: courseDetailInfo().teacherName + ' ' + courseDetailInfo().schoolName}"></div>
                            <div class="infoItem num">
                                <span class="look"><i class="icon"></i><!-- ko text: courseDetailInfo().visitNum || 0 --><!-- /ko --></span>
                                <span class="download"><i class="icon"></i><!-- ko text: courseDetailInfo().downloadNum || 0 --><!-- /ko --></span>
                                <span class="share"><i class="icon"></i><!-- ko text: courseDetailInfo().shareNum || 0 --><!-- /ko --></span>
                            </div>
                            <div class="infoItem tag tag01 clearfix">
                                <div class="evaluateBox">
                                    <!-- ko foreach: (courseEvaluationInfo().labelInfo || []).slice(0, 2) -->
                                    <span class="eItem"><!-- ko text: labelName --><!-- /ko --><i data-bind="text: labelNum > 1000 ? '999+' : labelNum"></i></span>
                                    <!-- /ko -->
                                </div>
                            </div>
                            <div class="infoItem lookAll leftLookAll" data-bind="
                                click: seeAllEvaluation.bind($data, 'all'),
                                visible: (courseEvaluationInfo().labelInfo || []).length > 2">查看全部</div>
                        </div>
                    </div>
                    <!-- 右侧评价 -->
                    <div class="infoRight">
                        <!-- 评分 -->
                        <div class="scoreBox">
                            <div class="scoreNum" data-bind="text: courseEvaluationInfo().totalScore"></div>
                            <div class="scoreStar">
                                <!-- 点亮星星 5种情况 star01至star05 -->
                                <div class="info-star" data-bind="css: 'star0' + Math.floor(courseEvaluationInfo().totalScore / 20)">
                                    <span class="star"></span>
                                    <span class="star"></span>
                                    <span class="star"></span>
                                    <span class="star"></span>
                                    <span class="star"></span>
                                </div>
                                <div class="personNum" data-bind="text: (courseEvaluationInfo().commentNum >= 3 ? courseEvaluationInfo().commentNum : '少于3') + ' 人评价'"></div>
                            </div>
                        </div>
                        <!-- 我的评价 -->
                        <div class="myEvaluateBox">
                            <div class="myEvaluate">
                                <span class="label">我的评价</span>
                                <!-- 点亮星星 5种情况 star01至star05 -->
                                <div class="info-star evaluate-star" data-bind="css: 'star0' + ownEvaluationInfo().star">
                                    <span class="star"></span>
                                    <span class="star"></span>
                                    <span class="star"></span>
                                    <span class="star"></span>
                                    <span class="star"></span>
                                </div>
                            </div>
                            <!-- 评价前显示 -->
                            <#--<div class="evaBtn" data-bind="visible: !ownHasEvaluate(), click: evaluateCourse" style="display:none;">我要评价</div>-->
                            <!-- 评价后显示 -->
                            <div data-bind="visible: ownHasEvaluate()" style="display: none;">
                                <div class="eva-result" data-bind="text: ownEvaluationInfo().keyWord"></div>
                                <div class="infoItem tag clearfix">
                                    <div class="evaluateBox">
                                        <span class="eItem" data-bind="text: (ownEvaluationInfo().commentList || [])[0], visible: (ownEvaluationInfo().commentList || []).length"></span>
                                    </div>
                                    <div class="lookAll" data-bind="click: seeAllEvaluation.bind($data, 'own'), visible: (ownEvaluationInfo().commentList || []).length > 1">查看全部</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 作品详情 -->
        <div class="courseSection courseSection-gray">
            <div class="secInner">
                <div class="leftWorksDetail">
                    <div class="articleBox" id="JS-previewBox"></div>
                    <div class="btnBox">
                        <div class="button" data-bind="visible: courseDetailInfo().leftTime > 0 && courseDetailInfo().canvassItem" style="display: none;">
                            <#--<div class="btn" data-bind="visible: !ownHasEvaluate(), click: evaluateCourse" style="display: none;">我要评价</div>
                            <div class="btn share">
                                <span>分享资源</span>
                                <div class="codeImg">
                                    <div class="qrcode-box" id="coureQrcode"></div>
                                </div>
                            </div>-->
                            <div class="btn" data-bind="
                                click: canvassCourse,
                                text: '为TA拉票（' + (courseDetailInfo().canvassHelperNum || 0) + '次）'"></div>
                            <div class="btn" data-bind="
                                click: voteCourse,
                                text: '为TA投票（' + (courseDetailInfo().canvassNum || 0) + '次）',
                                css: {'disabled': !courseDetailInfo().surplus}"></div>
                        </div>
                        <div class="button" data-bind="visible: !(courseDetailInfo().leftTime > 0 && courseDetailInfo().canvassItem)" style="display: none;">
                            <div class="btn" data-bind="click: shareCourse">分 享</div>
                        </div>
                    </div>
                </div>
                <div class="rightWorksIntro">
                    <div class="introPart" data-bind="visible: courseDetailInfo().description">
                        <div class="partTitle">
                            <span>作品简介</span>
                        </div>
                        <div class="partText" data-bind="text: courseDetailInfo().description"></div>
                    </div>
                    <div class="introPart">
                        <div class="partTitle clearfix">
                            <span>作品资源（点击可预览）</span>
                            <span class="download" data-bind="click: downloadCourse, visible: courseDetailInfo().zipFileUrl" style="display: none;"><i class="icon"></i>下载资源</span>
                        </div>
                        <ul class="articleList" data-bind="visible: (courseDetailInfo()._previewList || []).length" style="display: none;">
                            <!-- ko foreach: courseDetailInfo()._previewList -->
                            <li data-bind="click: $root.previewCourse">
                                <#-- default: zip, zip/ppt/word/pic-->
                                <div class="file-type zip" data-bind="css: _fileType"></div>
                                <span class="art-name" data-bind="text: filterName, attr: {title: name}"></span>
                            </li>
                            <!-- /ko -->
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <#--弹窗-->
        <!-- 01 公用弹窗 无下载权限等 -->
        <div class="coursePopup popup-rights" style="display: none;" data-bind="visible: isShowCommonAlert">
            <div class="popupInner">
                <div class="closeBtn" data-bind="click: function () { $root.isShowCommonAlert(false); }"></div>
                <div class="topContent">
                    <!-- error 错误icon -->
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
        <!-- 02 全部评价 -->
        <div class="coursePopup evaluatePopup" style="display: none;" data-bind="visible: isShowAllEvaluationAlert">
            <div class="popupInner">
                <div class="closeBtn" data-bind="click: function () {$root.isShowAllEvaluationAlert(false);}"></div>
                <div class="p-title">全部评价</div>
                <div class="tagBox">
                    <!-- ko foreach: allEvaluationList -->
                    <span class="eItem"><!-- ko text: labelName --><!--/ko--><i data-bind="text: labelNum, visible: labelNum"></i></span>
                    <!-- /ko -->
                </div>
            </div>
        </div>
        <!-- 03 评价当前作品 -->
        <div class="coursePopup evaluatePopup evaluatePopup02" style="display: none;" data-bind="visible: isShowEvaluateAlert">
            <div class="popupInner">
                <div class="closeBtn" data-bind="click: closeEvaluateAlert"></div>
                <div class="p-title" data-bind="text: evaluationAlertTitle"></div>
                <div class="mainBox">
                    <!-- 点亮星星 5种情况 star01至star05 -->
                    <div class="info-star evaluate-star" data-bind="css: 'star0' + evaluationStar()">
                        <!-- ko foreach: new Array(5) -->
                        <span class="star" data-bind="click: $root.choiceStar.bind($data, $index())"></span>
                        <!-- /ko -->
                    </div>
                    <div>
                        <div class="eva-result" data-bind="text: evaluationWord"></div>
                        <!-- 第一种情况 -->
                        <div class="tagBox" data-bind="visible: evaluationStar() === 5" style="display: none">
                            <div class="tagList">
                                <ul class="tab_list">
                                    <!-- ko foreach: fiveStarEvaluationTipList-->
                                    <li data-bind="
                                    text: tipText,
                                    css: {'active': isChoice},
                                    click: $root.choiceFiveEvaluationTip"></li>
                                    <!-- /ko -->
                                </ul>
                            </div>
                        </div>
                        <!-- 第二种情况 -->
                        <div data-bind="visible: evaluationStar() > 0 && evaluationStar() < 5" style="display: none">
                            <div class="tabBox-nav clearfix">
                                <p class="t_text">值得点赞</p>
                                <p class="t_text">有待提升</p>
                            </div>
                            <div class="tagBox tagBox02">
                                <div class="tagList">
                                    <ul class="tab_list">
                                        <!-- ko foreach: otherStarGoodEvaluationTipList-->
                                        <li data-bind="
                                        text: tipText,
                                        css: {'active': isChoice},
                                        click: $root.choiceOtherEvaluationTip.bind($data, $index(), 'good')"></li>
                                        <!-- /ko -->
                                    </ul>
                                </div>
                                <div class="tagList">
                                    <ul class="tab_list">
                                        <!-- ko foreach: otherStarBadEvaluationTipList-->
                                        <li data-bind="
                                        text: tipText,
                                        css: {'active': isChoice},
                                        click: $root.choiceOtherEvaluationTip.bind($data, $index(), 'bad')"></li>
                                        <!-- /ko -->
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="enough-tip" data-bind="text: evaluateErrorTip"></div>
                    <div class="btnContent clearfix">
                        <a class="cancel" href="javascript:void(0)" data-bind="click: closeEvaluateAlert">取消</a>
                        <#-- 未选择星级 或 选择星级小于5 时按钮不可点击（0 -->
                        <a class="submit" href="javascript:void(0)" data-bind="
                            css: { 'disabled': !evaluateBtnActive() },
                            click: commitEvaluate">提交</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "./module/footer.ftl">
</div>
<script>
    var cdnHeader = "<@app.link href='/'/>";
    var userInfo = {};
    var awardTeachers = null; // 获奖老师名单
</script>
</@layout.page>