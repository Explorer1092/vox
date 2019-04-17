<#import "../../layout.ftl" as homeworkReport>
<@homeworkReport.page title="作业报告" pageJs="crd">
    <@sugar.capsule css=['homework','jbox'] />
    <div class="mhw-homeworkCorrect" style="height: 100%; overflow: hidden;">
        <div class="mhw-header mar-b14 fixTop">
            <div class="header-inner">
                <h2 class="title">作业详情</h2>
                <div class="switch-box slideUp"><!--通过slideUp切换箭头-->
                    <div class="mhw-menuBox" data-bind="css: {'slideUp' : $data.showViewHomeworkSelectBox}">
                        <div class="slideItem" data-bind="click: $data.showViewHomeworkTitleClick">切换作业类型<span class="arrow"></span></div>
                        <ul class="slideInfo" data-bind="visible: $data.showViewHomeworkSelectBox" style="display: none">
                            <!--ko foreach: {data: categoryList, as : "_c"}-->
                            <li data-bind="text: _c.name(), visible: _c.show(), css: {'active': _c.checked()},click: $root.showViewHomeworkClick">--</li>
                            <!--/ko-->
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <!--ko if: $root.selectedTab().value != 'BASIC_APP' && $root.selectedTab().value != 'READING'-->
        <div id="homeworkListBox" class="homeworkCorrect-main" style="height: 100%; overflow: hidden; overflow-y: scroll;-webkit-overflow-scrolling : touch;" data-bind="event: { scroll: $root.homeworkScrolled }">
            <div class="mhw-emptyBox"></div>
            <div class="hcm-list" data-bind="visible: selectedTab().name" style="display: none;">
                <div class="list-hd">
                    <span data-bind="text: selectedTab().name">&nbsp;</span>
                    <a data-bind="visible:selectedTab().value == 'ORAL_PRACTICE'" href="/teacher/homework/report/scorerule.vpage?homeworkType=ORAL_PRACTICE" style="display:none;font-weight:400;float: right;font-size: 0.7rem;color: #189cfb;">分数计算规则</a>
                </div>
                <!--ko foreach: {data: $root.showReportDetail(), as : "_de"}-->
                <div class="mhw-base mar-b20">
                    <div class="mb-hd clearfix">
                        <div class="fl txt-grey">
                            <!--ko if: $root.selectedTab().value == 'READ_RECITE' -->
                            <span data-bind="text: _de.answerWay()">--</span>
                            <span data-bind="text: _de.paragraphCName()">--</span>
                            <span data-bind="text: _de.articleName()">--</span>
                            <!--/ko-->

                            <!--ko if: $root.selectedTab().value != 'READ_RECITE' -->
                            <span data-bind="text: _de.contentType()">--</span>
                            <span data-bind="text: $root.difficulty(_de.difficulty())">--</span>
                            <!--/ko-->

                        </div>
                    </div>
                    <div class="mb-mn pad-30">
                        <div>
                            <div data-bind="attr:{id : 'examImg' + $index()}"></div>
                            <div data-bind="text:$root.loadExamImg(_de.qid(),$index())"></div>
                        </div>
                    </div>
                    <div class="mb-hd clearfix" data-bind="if:$root.selectedTab().value != 'ORAL_PRACTICE',visible:$root.selectedTab().value != 'ORAL_PRACTICE'">
                        <div class="fl txt-grey" data-bind="visible: ($root.selectedTab().value == 'PHOTO_OBJECTIVE' || $root.selectedTab().value == 'VOICE_OBJECTIVE') && _de.errorAnswerList" style="display: none;"><span>同学答案</span></div>
                        <div class="fl txt-grey" data-bind="visible: ($root.selectedTab().value != 'PHOTO_OBJECTIVE' && $root.selectedTab().value != 'VOICE_OBJECTIVE') && _de.errorAnswerList" style="display: none;"><span>答案及相关的同学</span></div>
                    </div>
                    <div class="mb-mn pad-30" data-bind="if: $root.selectedTab().value != 'ORAL_PRACTICE' && _de.errorAnswerList,visible: $root.selectedTab().value != 'ORAL_PRACTICE' && _de.errorAnswerList">
                        <!--ko foreach: {data: _de.errorAnswerList(), as : "_err"}-->
                        <div class="innerCon" data-bind="text: _err.answer() || '未作答',visible: ($root.selectedTab().value != 'PHOTO_OBJECTIVE' && $root.selectedTab().value != 'VOICE_OBJECTIVE' && $root.selectedTab().value != 'READ_RECITE')"></div>
                        <div class="innerCon">
                        <#--showType=0 只有题目  showType=1图片  showType=2音频-->
                            <!--ko if: _de.showType() == 0 -->
                            <!--ko foreach: {data: _err.users(), as : "_user"}-->
                            <span data-bind="text: _user.userName()"></span>
                            <span data-bind="text:',', visible: $index() != _err.users().length -1 " style="display: none;"></span>
                            <!--/ko-->
                            <!--/ko-->

                            <!--ko if: _de.showType() == 1 -->
                            <ul class="hcm-questionBox" data-bind="visible: _de.showType() == 1" style="display: none;">
                                <!--ko foreach: {data: _err.users(), as : "_user"}-->
                                <li>
                                    <p class="name" data-bind="text: _user.userName()">--</p>
                                    <div data-bind="if: _user.showPics().length > 0,click: $root.checkBtn.bind($data,_de.qid(),_de.showType())">
                                        <img src="" data-bind="attr: {'src' : _user.showPics()[0]+$root.pictureQuality}">
                                        <i data-bind="visible: _user.correction()=='WRONG' " class="mark-icon mark-icon-red">×</i>
                                        <i data-bind="visible: _user.correction()=='RIGHT' " class="mark-icon">√</i>
                                        <i data-bind="visible: _user.correction()=='EXCELLENT' " class="mark-icon">优</i>
                                        <i data-bind="visible: _user.correction()=='GOOD' " class="mark-icon">良</i>
                                        <i data-bind="visible: _user.correction()=='FAIR' " class="mark-icon">中</i>
                                        <i data-bind="visible: _user.correction()=='PASS' " class="mark-icon mark-icon-red">差</i>
                                        <i data-bind="visible: _user.correction()=='FAIL' " class="mark-icon mark-icon-red">没通过</i>
                                        <i data-bind="visible: _user.correction()== null && (_user.review() || _user.review() != null)"  class="mark-icon">阅</i>
                                    </div>
                                    <div data-bind="if: _user.showPics().length == 0">
                                        <img src="" data-bind="attr: {'src' : $root.defaultPicture}">
                                    </div>
                                </li>
                                <!--/ko-->
                            </ul>
                            <!--/ko-->

                            <!--ko foreach: {data: _err.users(), as : "_user"}-->
                            <div class="hcm-videoBbox" data-bind="visible: _de.showType() == 2" style="display: none;">
                                <div class="left play playAudioBtn" data-bind="click:$root.voicePlayOrStopBtn.bind($data,$element, _user.showPics()[0])">
                                    <i data-bind="visible: _user.correction()=='EXCELLENT' " class="mark-icon">优</i>
                                    <i data-bind="visible: _user.correction()=='GOOD' " class="mark-icon">良</i>
                                    <i data-bind="visible: _user.correction()=='FAIR' " class="mark-icon">中</i>
                                    <i data-bind="visible: _user.correction()=='PASS' " class="mark-icon mark-icon-red">差</i>
                                    <i data-bind="visible: _user.correction()== null && (_user.review() || _user.review() != null)"  class="mark-icon mark-icon-yue">阅</i>
                                </div>
                                <div class="right">
                                    <p class="name" data-bind="text: _user.userName()">--</p>
                                    <a href="javascript:void(0)" class="btn btn-blue" data-bind="click: $root.checkBtn.bind($data,_de.qid(),_de.showType())">打分</a>
                                </div>
                            </div>
                            <!--/ko-->
                        </div>
                        <!--/ko-->
                    </div>
                    <!--ko if:_de.answerList && _de.answerList().length > 0-->
                    <!--ko foreach:{data:_de.answerList(), as:'asl'}-->
                    <div class="mb-hd clearfix" style="display: none;" data-bind="visible:$root.selectedTab().value == 'ORAL_PRACTICE'">
                        <div class="fl txt-grey"><span data-bind="text:_de.answerList().length > 1 ? '学生答案：第' + ($index() + 1) + '小题' : '学生答案'">&nbsp;</span></div>
                    </div>
                    <div class="mb-mn pad-30 details-box" style="padding: 0 0.8rem;display: none;" data-bind="visible:$root.selectedTab().value == 'ORAL_PRACTICE'">
                        <div class="d-container">
                            <ul class="d-list">
                                <!--ko foreach: {data: asl.users(), as : "_user"}-->
                                <li>
                                    <p class="name" data-bind="text:_user.userName">王五</p>
                                    <div class="play playAudioBtn" data-bind="click:$root.voicePlayOrStopBtn.bind($data, $element, _user.userVoiceUrls())">
                                        <i class="d-gradeIcon" data-bind="text: _user.score, css:{'iconGreen' : _user.realScore <= 60}">B</i>
                                    </div>
                                </li>
                                <!--/ko-->
                            </ul>
                        </div>
                    </div>
                    <!--/ko-->
                    <!--/ko-->
                </div>
                <!--/ko-->
            </div>
            <div class="mhw-emptyBox"></div>
        </div>
        <!--/ko-->


        <!--ko if: $root.selectedTab().value == 'BASIC_APP'-->
        <#include "basicappreport.ftl">
        <!--/ko-->
        <p data-bind="text: $root.selectedTab().value"></p>
        <!--ko if: $root.selectedTab().value == 'READING'-->
        <#include "readingreport.ftl">
        <!--/ko-->
        <div class="footer-empty" data-bind="visible: $root.showQuickCheckBtn" style="display: none;">
            <div class="btns pad-30 fixFooter">
                <a data-bind="click: $data.quickCheckClick" href="javascript:void(0)" class="w-btn">一键批改</a>
            </div>
        </div>
    </div>

    <div class="mhw-returnTop" data-bind="visible: $root.divScrollTop() > 0, click: $root.gotoTop" style="display: none;"></div>

<div href="javascript:void (0);" id="jplayerId"></div>
    <#--检查选择框-->
    <#include "checkbox.ftl">

    <script type="text/javascript">
        var homeworkConstant = {
            imgDomain: '<@app.link_shared href='' />',
            domain : '${(requestContext.webAppBaseUrl)!}',
            env : <@ftlmacro.getCurrentProductDevelopment />
        };
    </script>
</@homeworkReport.page>