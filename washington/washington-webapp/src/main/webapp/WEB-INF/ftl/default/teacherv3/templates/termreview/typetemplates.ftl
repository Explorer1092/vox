<#macro tabTemplates subject="">
<script id="t:作业形式" type="text/html">
    <!--ko if:currentTabs().length > 0-->
    <div class="w-base-switch w-base-two-switch h-switch">
        <ul class="Teachertitle" style="height: 94px;">
            <!--ko foreach:{data : currentTabs,as:'tab'}-->
            <li data-bind="click:$root.tabClick.bind($data,$root),css:{'active' : tab.type() == $root.focusTabType()}">
                <a href="javascript:void(0);">
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                    <i class="tab-icon" data-bind="style:{backgroundImage : tab.icon()}"></i>
                    <span class="w-icon-new w-icon-public" data-bind="visible:$root.addNewIcon(tab.type(),$root)"></span>
                    <p data-bind="text:typeName"></p>
                </a>
            </li>
            <!--/ko-->
        </ul>
        <!--ko if:tabs().length > 5-->
        <div class="h-arrow h-arrow-L" data-bind="click:arrowClick.bind($data,'arrowLeft')"><i class="h-arrow-icon" data-bind="css:{'h-arrow-iconLhover' : leftEnabled()}"></i></div>
        <div class="h-arrow h-arrow-R" data-bind="click:arrowClick.bind($data,'arrowRigth')"><i class="h-arrow-icon h-arrow-iconR" data-bind="css:{'h-arrow-iconRhover' : rightEnabled()}"></i></div>
        <!--/ko-->
    </div>
    <!--/ko-->
    <!--ko if:tabs().length == 0-->
    <div class="tabs-empty" data-bind="text:$root.info()">当前暂无作业内容，请切换其他内容查看</div>
    <!--/ko-->
</script>
<script id="t:default" type="text/html">
    <div class="w-base-container">
        <div class="w-noData-box">
            没有该TAB下的数据模板
        </div>
    </div>
</script>
<#include "../kopagination.ftl">
<#include "termexam.ftl">
<#include "termbasicword.ftl">
<#if subject=="MATH">
    <#include "math/intelligentteaching.ftl">
</#if>

<script id="T:EXAM_REVIEW" type="text/html" title="同步习题预览">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3 data-bind="text:$root.getHomeworkTypeName(homeworkCt.type)">&nbsp;</h3>
        </div>
        <!--ko foreach:{data:homeworkCt.questions.slice(0,$root.typeDisplayMap[homeworkCt.type]()),as:'question'}-->
        <div class="h-set-homework">
            <div class="seth-hd">
                <p class="fl">
                    <span data-bind="text:question.questionType">&nbsp;</span>
                    <span data-bind="text:question.difficultyName">&nbsp;</span>
                    <span class="noBorder" style="display: none;" data-bind="visible:question.assignTimes > 0">被使用<!-- ko text:question.assignTimes--><!--/ko-->次</span>
                </p>
            </div>
            <div class="seth-mn">
                <div class="box" data-bind="attr:{id : question.questionId + $index()}"></div>
                <div data-bind="text:$root.loadExamImg(question.questionId,$index())"></div>
            </div>
            <div class="h-btnGroup">
                <a href="javascript:void(0);" data-bind="click:$root.removeQuestion.bind($root,{type:homeworkCt.type,questions : [$data]})" class="btn cancel">移除</a>
            </div>
        </div>
        <!--/ko-->
        <!--ko if:homeworkCt.questions && homeworkCt.questions().length > $root.typeDisplayMap[homeworkCt.type]()-->
        <div class="t-dynamic-btn" style="margin: 8px 15px;">
            <a class="more" href="javascript:void(0);" data-bind="click:$root.addDisplayCount.bind($data,$root,homeworkCt.type)">展开更多</a>
        </div>
        <!--/ko-->
    </div>
</script>

<script id="T:BASIC_APP_REVIEW" type="text/html" title="基础练习预览">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>基础练习</h3>
        </div>
        <div class="e-lessonsBox" data-bind="foreach:{data:$root.getLessonGroup(homeworkCt.contents()),as:'lesson'}">
            <div class="e-lessonsList">
                <div class="el-title" data-bind="text:lesson.lessonName">&nbsp;</div>
                <div class="el-name" data-bind="text:lesson.sentences">&nbsp;</div>
                <div class="el-list">
                    <ul data-bind="foreach:{data:lesson.categories,as:'category'}">
                        <li>
                            <div class="lessons-text previewText" data-bind="singleAppHover:true,click:$root.categoryPreview.bind($data,lesson.lessonId,$root)">
                                <div class="preview lessons-mask">预览</div>
                                <i class="e-icons">
                                    <img data-bind="attr:{src:$root.getCategoryIconUrl(category.categoryIcon)}">
                                </i>
                                <span class="text" data-bind="text:category.practiceCategory">&nbsp;</span>
                            </div>
                            <div class="lessons-btn">
                                <div data-bind="click:$root.removeQuestion.bind($root,{type : homeworkCt.type,practice:$data})"><p>移除</p></div>
                            </div>
                            <!--ko if:lesson.teacherAssignTimes > 0-->
                            <div class="w-bean-location"><i class="w-icon w-icon-34"></i></div><#--已布置icon-->
                            <!--/ko-->
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="T:KEY_POINTS_REVIEW" type="text/html" title="视频特训预览">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3 data-bind="text:$root.getHomeworkTypeName(homeworkCt.type)">&nbsp;</h3>
        </div>
        <!--ko foreach:{data:homeworkCt.contents(), as:'content'}-->
        <!--ko foreach:{data: content.questions(), as:'question'}-->
        <div class="h-set-homework">
            <div class="seth-hd">
                <p class="fl">
                    <span data-bind="text:question.questionType">&nbsp;</span>
                    <span data-bind="text:question.difficultyName">&nbsp;</span>
                    <span class="noBorder" style="display: none;" data-bind="visible:question.assignTimes > 0">被使用<!-- ko text:question.assignTimes--><!--/ko-->次</span>
                </p>
            </div>
            <div class="seth-mn">
                <div class="box" data-bind="attr:{id : 'mathExamImgReview' + $parentContext.$index() + '_' + $index()}"></div>
                <div data-bind="text:$root.loadExamImg(question.questionId,$parentContext.$index() + '_' + $index())"></div>
            </div>
            <div class="h-btnGroup">
                <a href="javascript:void(0);" data-bind="click:$root.removeQuestion.bind($root,{type:homeworkCt.type,videoId:content.videoId,questions : [$data]})" class="btn cancel">移除</a>
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
</script>

<script id="T:UNKNOWN_REVIEW" type="text/html" title="未知作业类型预览">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>未知作业类型</h3>
        </div>
        <div class="h-set-homework">
            <div class="seth-mn">没有该作业类型模板</div>
        </div>
    </div>
</script>

<script id="T:TERM_REVIEW_CONFIRM" type="text/html">
    <div id="saveMathDialog" class="h-homework-dialog03 h-homework-dialog" style="width: 100%;">
        <div class="inner">
            <div class="list">布置时间：<span data-bind="text:startDate"></span></div>
            <div class="list">
                <div class="name">布置班级：</div>
                <div class="info grade">
                    <!--ko if:clazzNames() != null && clazzNames().length > 0-->
                    <!--ko foreach:clazzNames-->
                    <span data-bind="text:$data"></span>
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko ifnot:clazzNames() != null && clazzNames().length > 0-->
                    <span>班级未显示</span>
                    <!--/ko-->
                </div>
            </div>
            <div class="list"><div class="name">布置内容：</div>
                <div class="info">
                    <!--ko foreach:{data : tabDetails,as:'detail'}-->
                    <span class="tj"><strong data-bind="text:detail.name"></strong>
                        （共<strong data-bind="text:detail.count">0</strong><!--ko text:(detail.tabType == 'READING' ? '本' : (detail.tabType == 'BASIC_APP' ? '个' : '题'))--><!--/ko-->）
                    </span>
                    <!--/ko-->
                </div>
            </div>
            <div class="list">预计时间：<span data-bind="text:minute()">0</span>分钟<span class="tips-grey" style="padding-left:25px;">(学生实际完成用时受设备和网络影响，预计时长仅供参考)</span></div>
            <div class="list">截止时间：
                <label style="cursor: pointer;" data-bind="click:changeEndDate.bind($data,0,'zero'),css:{'w-radio-current':endLabel() == 'zero'}"><span class="w-radio"></span> <span class="w-icon-md">今天内</span></label>
                <label style="cursor: pointer;" data-bind="click:changeEndDate.bind($data,1,'one'),css:{'w-radio-current':endLabel() == 'one'}"><span class="w-radio"></span> <span class="w-icon-md">明天内</span></label>
                <label style="cursor: pointer;" data-bind="click:changeEndDate.bind($data,2,'two'),css:{'w-radio-current':endLabel() == 'two'}"><span class="w-radio"></span> <span class="w-icon-md">三天内</span></label>
                <label style="cursor: pointer;padding-right: 0px;" data-bind="click:changeEndDate.bind($data,-1,'custom'),css:{'w-radio-current':endLabel() == 'custom'}">
                    <span class="w-radio"></span> <span class="w-icon-md">自定义</span>
                    <input type="text" id="endDateInput" data-bind="value:endDateInput" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.hourSelect,value:focusHour"></select>时
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.minSelect,value:focusMin"></select>分
                </label>
            </div>
            <div class="list tips-grey">提交作业截止时间为<span data-bind="text:displayEndDate"></span></div>
            <p id="confirm_message">
                作业注意事项:
            <div class="t-homework-mis" style="padding: 5px 0 0 0;">
                <textarea id="v-leave-message" class="w-int" maxlength="100" data-bind="textInput:comment.view" style="height: 70px; line-height: 22px;"></textarea>
                <div class="m-info" style="right: 5px; bottom: 0;">还可以输入<strong id="v-leave-message-num" data-bind="text:(100 - comment().length)">100</strong>个字</div>
            </div>
            </p>
            <div class="btn-box"><a href="javascript:void(0)" data-bind="css:{'w-btn-disabled' : beanCount() > maxBeanCount()},click:saveHomework.bind($data,$element)" class="w-btn w-btn-well" style="font-size: 18px; width: 118px; padding: 9px 0;">确认布置</a></div>
        </div>
    </div>
</script>

</#macro>
