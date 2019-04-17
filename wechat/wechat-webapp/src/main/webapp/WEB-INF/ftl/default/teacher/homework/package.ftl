<#import "../layout.ftl" as homeworkIndex>
<@homeworkIndex.page title="布置作业" pageJs="package">
    <@sugar.capsule css=['homework','picker','jbox'] />
    <div class="mhw-selected" data-bind="visible : showHomeworkPackageList()">
        <div class="ms-box">
            <div class="title">
                <div class="fl left">作业列表</div>
                <div class="fr right">
                    <a data-bind="visible: $data.showQuestionsTotalCount() > 0 && !$data.showHomeworkFinishBtn(), click: $data.homeworkEditClick" href="javascript:void(0)" style="display: none;">编辑</a>
                    <a data-bind="visible: $data.showHomeworkFinishBtn(), click: $data.homeworkFinishClick" href="javascript:void(0)" style="display: none;">完成</a>
                </div>
            </div>
            <ul class="list" data-bind="visible: $data.packageDetail().length > 0" style="display: none;">
                <!-- ko foreach: {data : $data.packageDetail(), as : '_package'} -->
                <!-- ko if: _package.show() -->
                <li>
                    <div class="left fl txt-overflow">
                        <i class="icon" data-bind="css: $root.icon(_package.type())"></i>
                        <span data-bind="text: _package.name"></span>
                    </div>
                    <div class="right fr clearfix">
                        <div class="fl text txt-r" data-bind="text: _package.count()+ _package.unit()"></div>
                        <div class="fr btn" data-bind="click: $root.topicsBtn, visible: !$root.showHomeworkFinishBtn()"><a href="javascript:void(0)" class="w-btn w-btn-s">去选题</a></div>
                        <div class="fr btn" data-bind="click: $root.clearAllBtn, visible: $root.showHomeworkFinishBtn() && _package.count() != 0"><a href="javascript:void(0)" class="w-btn w-btn-s">清空</a></div>
                        <div class="fr btn" data-bind="visible: $root.showHomeworkFinishBtn() && _package.count() == 0"><a href="javascript:void(0)" class="w-btn w-btn-s" data-bind="css : {'disabled' :  _package.count() == 0}">去选题</a></div>
                    </div>
                </li>
                <!-- /ko -->
                <!-- /ko -->
            </ul>

            <div class="title" id="loadingBox">数据努力加载中...</div>
            <div class="title" data-bind="visible : $data.packageDetail().length == 0 && !$data.ajaxLoading" style="display: none;">暂无作业数据</div>
        </div>

        <div class="mhw-btns btns-2">
            <!--ko if: $data.showQuestionsTotalCount() == 0 -->
            <a href="javascript:void(0)" class="w-btn disabled">预览作业</a>
            <a href="javascript:void(0)" class="w-btn disabled">布置作业</a>
            <!--/ko-->
            <a data-bind="visible : $data.showQuestionsTotalCount() != 0,click: $root.viewHomeworkBtn" href="javascript:void(0)" style="display: none;" class="w-btn w-btn-lightBlue">预览作业</a>
            <a data-bind="visible : $data.showQuestionsTotalCount() != 0,click: $root.gotoConfirm" href="javascript:void(0)" style="display: none;" class="w-btn">布置作业</a>
        </div>
    </div>

    <div data-bind="visible : $data.showHomeworkPackageDetailBox" style="height: 100%; overflow: hidden; display: none;">
        <div id="topMenuListBox" class="mhw-header mar-b14 overflow-x fixTop">
            <ul class="mhw-tab">
                <!-- ko foreach: {data : $data.packageDetail(), as : '_package'} -->
                <!-- ko if: _package.show() -->
                <li data-bind="click : $root.homeworkTypeClick, text: _package.name(), css: {'active' : _package.type() == $root.selectedHomeworkType()}"></li>
                <!--/ko-->
                <!--/ko-->
            </ul>
        </div>

        <div class="mhw-header">
            <div class="fixTop">
                <div class="header-inner">
                    <div class="fl" data-bind="text: $root.selectedHomeworkName()">--</div>
                    <div class="fr"><a href="javascript:void(0)" class="switch-link" data-bind="click: $root.changeQuestionTypeBtn">切换作业类型</a></div>
                </div>
            </div>
        </div>

        <div class="mhw-slideBox" data-bind="visible: $root.changeQuestionTypeBox" style="display: none;">
            <div class="mask"></div>
            <div class="innerBox">
                <div class="hd">切换作业类型<span class="close" data-bind="click: function() {$root.changeQuestionTypeBox(false)}">×</span></div>
                <div class="topicTpye mhw-selected mhw-slideOverflow">
                    <ul class="ms-box">
                        <!-- ko foreach: {data : $data.packageDetail(), as : '_package'} -->
                        <!-- ko if: _package.show() -->
                        <li class="list" data-bind="click : $root.homeworkTypeClick, css: {'active' : _package.type() == $root.selectedHomeworkType()}">
                            <i class="icon" data-bind="css: $root.icon(_package.type())"></i>
                            <span class="text" data-bind="text: _package.name()"></span>
                        </li>
                        <!--/ko-->
                        <!--/ko-->
                    </ul>
                </div>
            </div>
        </div>

        <div id="scrollListBox" class="mhw-main"  style="height: 100%; overflow: hidden; overflow-y: scroll;-webkit-overflow-scrolling : touch;" data-bind="event: { scroll: $root.examScrolled }">
            <div class="mhw-emptyBox"></div>
            <#include "exam.ftl">
            <#include "mental.ftl">
            <#include "quiz.ftl">
            <#include "photoobjective.ftl">
            <#include "voiceobjective.ftl">
            <#--<#include "wordpractice.ftl">-->
            <#--<#include "readrecite.ftl">-->
            <#include "basicapp.ftl">
            <#include "reading.ftl">
            <#include "oralpractice.ftl">
            <#include "intelligenceexam.ftl">
            <#include "knowledge_review.ftl">
            <#include "fallibility_question.ftl">
            <#include "general_error.ftl">
            <div class="mhw-emptyBox"></div>
        </div>
        <div class="mhw-footer" >
            <div class="inner-box">
                <div class="inner">
                    已选<span data-bind="text: $data.showQuestionsTotalCount()"></span>道题
                    <a data-bind="visible : $data.showQuestionsTotalCount() == 0" href="javascript:void(0)" class="btn w-btn w-btn-s disabled">选好了 去布置</a>
                    <a data-bind="visible : $data.showQuestionsTotalCount() != 0,click: $data.selectFinishedBtn" href="javascript:void(0)" class="btn w-btn w-btn-s">选好了 去布置</a>
                </div>
            </div>
        </div>
        <div class="mhw-returnTop" data-bind="visible: $root.divScrollTop() > 0 && ($data.selectedHomeworkType() == 'EXAM' || $data.selectedHomeworkType() == 'READING'), click: $root.gotoTop" style="display: none;"></div>
    </div>


    <#--二次确认页-->
    <#include "confirm.ftl">
    <#--题包详情预览-->
    <#include "viewpackage.ftl">
    <#--测验预览-->
    <#include "viewquiz.ftl">
    <#--预览作业-->
    <#include "viewhomework.ftl">
    <#--知识点选择-->
    <#include "knowledgepoints.ftl">

    <#--推荐使用-->
    <div data-bind="template : {name: koTemplateName()}"></div>
    <#include "templatelist.ftl">


<script type="text/javascript">
        var homeworkConstant = {
            bookId: '${bookId!0}',
            unitId: '${unitId!0}',
            sections: '${sections!0}',
            clazzIds: '${clazzIds!0}',
            startDate: '${.now?string('yyyy-MM-dd')}',
            currentHour: '${.now?string('H')?number}',
            imgDomain: '<@app.link_shared href='' />',
            tabIconPrefixUrl : '<@app.link_shared href='/resources/mobile/teacher/points-icon/english-view/' />',
            domain: '${(requestContext.webAppBaseUrl)!}',
            env: <@ftlmacro.getCurrentProductDevelopment />,
            isProductionEnv: ${ProductDevelopment.isProductionEnv()?string},
            _homeworkContent: {}
        };
    </script>
</@homeworkIndex.page>

