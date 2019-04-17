<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <#if subject?? && subject == "MATH">
        <@sugar.capsule js=["plugin.venus"] css=["plugin.venus"] />
    </#if>
    <@sugar.capsule js=["ko","datepicker","jplayer","jquery.flashswf"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework","vacation.winterhomework"] />
<style type="text/css" xmlns="http://www.w3.org/1999/html">
    .e-pictureListNoHover .e-pictureList:hover{
        border:1px solid #dae6ee;
    }
</style>
<#if adValidaty?? && adValidaty>
<div class="win-banner">
    <a target="_blank" rel="noopener noreferrer" href="/teacher/activity/summer/2018/lottery.vpage?track=center">
        <img src="<@app.link href='public/skin/teacherv3/images/winnerhomework/banner.jpg'/>">
    </a>
</div>
</#if>
<div class="w-base" style="position: relative; zoom: 1;  z-index: 5;">
    <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
        <h3>布置作业</h3>
    </div>
    <div class="w-base-container" id="levelsAndBook">
        <div data-bind="template:{name : 't:LOAD_IMAGE',if:$root.loading()}"></div>
        <div class="t-homework-form"  style="overflow: visible;">
            <dl style="display: none;" data-bind="if:!$root.loading() && showClazzList().length > 0,visible:!$root.loading() && showClazzList().length > 0">
                <dt>班级</dt>
                <dd style="overflow: hidden; *zoom:1; position: relative;">
                    <div class="w-border-list t-homeworkClass-list">
                        <ul>
                            <!--ko foreach: batchclazzs-->
                            <!--ko if: $data.length > 0-->
                            <li data-bind="click:$root.levelClick.bind($data,$root, $index() + 1), attr:{'data-level' : $index() + 1},css:{'active' : $root.showLevel() == ($index() + 1)},text:($index() + 1) + '年级'" class="v-level"></li>
                            <!--/ko-->
                            <!--/ko-->
                            <!--ko if: $root.showLevel() > 0-->
                            <li class="pull-down">
                                <!--ko if:$root.showClazzList().length > 1-->
                                <p data-bind="click:$root.chooseOrCancelAll,css:{'w-checkbox-current' : $root.isAllChecked}">
                                    <span class="w-checkbox"></span>
                                    <span class="w-icon-md textWidth">全部</span>
                                </p>
                                <!--/ko-->
                                <!--ko foreach:{data : $root.showClazzList(),as:'clazz'}-->
                                <p data-bind="click : $root.singleClazzAddOrCancel.bind($data,$root,$index()), css:{'w-checkbox-current': clazz.checked()}" class="marginL26" style="width:100px;">
                                    <span class="w-checkbox"></span>
                                    <span class="w-icon-md" data-bind="attr:{title:clazz.className()},text:clazz.className()">&nbsp;</span>
                                </p>
                                <!--/ko-->
                            </li>
                            <!--/ko-->
                        </ul>
                    </div>
                </dd>
            </dl>
            <dl style="overflow: visible; z-index: 12;display: none;" data-bind="if:!$root.loading() && $root.bookId() != null,visible:!$root.loading() && $root.bookId() != null">
                <dt>教材</dt>
                <dd>
                    <div class="text">
                        <span data-bind="text:bookName()">&nbsp;</span>
                        <a class="w-blue" href="javascript:void(0);" style="margin-left: 50px;" data-bind="click:changeBook">
                            更换教材<span class="w-icon-public w-icon-switch w-icon-switchBlue" style="margin-right: -5px; margin-left: 10px; *margin: 3px 0 0 10px;"></span>
                        </a>
                    </div>
                </dd>
            </dl>
        </div>
        <div class="w-noData-box" style="display: none;" data-bind="if:!$root.loading() && showClazzList().length == 0,visible:!$root.loading() && $root.showClazzList().length == 0">
            暂没有可以布置的班级
        </div>
    </div>
</div>

<#--作业形式-->
<div class="w-base h-baseTab" id="hkTabcontent">
    <div class="w-base-title" style="height: 110px;border-bottom: 0;display:none;" data-bind="if:!$root.loading() && $root.weekList().length == 0,visible:!$root.loading() && $root.weekList().length == 0">
        <div class="tabs-empty">暂无假期内容，换一本教材试试吧</div>
    </div>
    <div id="sliderHolder" class="w-base-title tab-title" name="slider" style="display:none;" data-bind="if:!$root.loading() && $root.weekList().length > 0,visible:!$root.loading() && $root.weekList().length > 0">
        <!--ko if: $root.weekList().length > 5-->
        <div id="swipingLeft" class="arrow arrow-l" style="display: none;"><i class="icon"></i></div>
        <div id="swipingRight" class="arrow arrow-r"><i class="icon"></i></div>
        <!--/ko-->
        <div id="slideContainer" class="w-base-switch w-base-two-switch">
            <ul class="Teachertitle" data-bind="foreach:{data : $root.weekList(), as :'week'},style:{width: $root.weekList().length*156+'px',position:'absolute',left:'0px',transition: 'left 1s'}">
                <li class="slideItem" data-bind="css:{'active': week.weekRank == $root.focusWeekRank()},click:$root.weekClick.bind($data,week.weekRank,$root),clickBubble: false">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                        <div class="win-head">
                            <p class="hWeek" data-bind="attr:{title:week.title},text:week.title"></p>
                            <p class="hSub" data-bind="attr:{title:week.scope},text:week.scope"></p>
                        </div>
                    </a>
                </li>
            </ul>
        </div>
    </div>

    <div style="display:none;" data-bind="if: !$root.loading() && $root.dayPlanList().length > 0,visible: !$root.loading() && $root.dayPlanList().length > 0">
        <div class="h-topicPackage" style="margin-bottom: 0">
            <!--ko if: $root.dayPlanList().length > 3-->
            <div class="arrow arrow-l" style="display: none" data-bind="visible:$root.leftEnabled(), click:arrowClick.bind($data,'arrowLeft'),clickBubble: false"><i class="icon"></i></div>
            <div class="arrow arrow-r" data-bind="visible:$root.rightEnabled(), click:arrowClick.bind($data,'arrowRight'),clickBubble: false"><i class="icon"></i></div>
            <!--/ko-->
            <div class="topicBox winWorkBox">
                <ul data-bind="foreach:{data : $root.dayPlanList(),as:'dayPlan'},style:{width: $root.dayPlanList().length*218+'px',position:'absolute',left: ((($root.currentDayPlanIndex() + 1) % 4 == 0) ? (0-$root.currentDayPlanIndex()*218) : ($root.currentDayPlanIndex() > 3 ? (0 - Math.floor($root.currentDayPlanIndex()/3) * 3 *218) : 0)) + 'px',transition: 'left 1s'}"><!--width为li的个数*218px-->
                    <li class="slideItem" data-bind="css:{active: $root.currentDayPlanIndex() == $index()},click:$root.packageClick.bind($data,$index(),$root),clickBubble: false"><!--active状态-->
                        <span class="wTriangle-icon"></span>
                        <div class="wkDays" data-bind="text:'Day  ' + dayPlan.dayRank">&nbsp;</div>
                        <div class="wkInfo" data-bind="text:dayPlan.name">&nbsp;</div><!--题包只要一个字段，添加heightOne-->
                        <span class="wkIcon"></span>
                    </li>
                </ul>
            </div>
        </div>
        <div id="tabContent" class="syn-exercises" style="display: none;" data-bind="if:$root.currentDayPlan() != null && $root.dayPlanDetailList().length > 0,visible: $root.currentDayPlan() != null && $root.dayPlanDetailList().length > 0">
            <div class="syn-title" data-bind="text:$root.currentDayPlan().desc">&nbsp;</div>
            <div data-bind="template:{name:$root.displayMode,foreach:$root.dayPlanDetailList,as:'planDetail'}"></div>
        </div>
    </div>
</div>

<div id="ufoassign" class="h-floatLayer-R">
    <div class="fl-hd">布置假期作业</div>
    <div class="J_UFOInfo fl-mn">
        <p>共<!--ko text:$root.clazzCount()--><!--/ko-->个班</p>
        <p>每班<!--ko text:$root.taskCount()--><!--/ko-->天作业任务</p>
    </div>
    <div class="fl-bot">
        <a href="javascript:void(0)" data-bind="click:$root.assignClick" class="w-btn w-btn-well w-btn-blue">布置</a>
    </div>
</div>


<#include "../templates/homeworkv3/changebook.ftl">
<#include "../templates/vacation/confirm.ftl">

<script id="t:LOAD_IMAGE" type="text/html">
    <div style="height: 200px; background-color: white; width: 98%;">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
    </div>
</script>

<script id="T:VACATION_BASIC_APP" type="text/html">
    <div class="e-lessonsBox">
        <!--ko foreach:{data:planDetail.practices,as:'content'}-->
        <div class="e-lessonsList">
            <div class="el-title" data-bind="text:content.lessonName">词汇练习</div>
            <div class="el-name" data-bind="text:$root.covertSentences(content.sentences)"></div>
            <div class="el-list">
                <ul>
                    <!--ko foreach:{data:content.categories,as:'category'}-->
                    <li>
                        <div class="lessons-text previewText">
                            <i class="e-icons">
                                <img data-bind="attr:{src:$root.getCategroyIconUrl(category.categoryIcon)}">
                            </i>
                            <span class="text" data-bind="text:category.categoryName">&nbsp;</span>
                        </div>
                        <div class="lessons-btn operateBtn">
                            <div data-bind="click:$root.categoryPreview.bind($data,content.lessonId,$root)"><p>预览</p></div>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>

<script id="T:VACATION_NATURAL_SPELLING" type="text/html">
    <div>
        <div data-bind="foreach:{data:planDetail.practices,as:'unit'}">
            <div class="e-lessonsTitle" data-bind="text:unit.unitName">&nbsp;</div>
            <div class="e-lessonsBox">
                <!--ko foreach:{data:unit.lessons,as:'content'}-->
                <div class="e-lessonsList">
                    <div class="el-title" data-bind="text:content.lessonName">&nbsp;</div>
                    <!--ko foreach:{data:content.categoryGroups,as:'ctGroup'}-->
                    <div class="el-name">
                        <!--ko if:ctGroup.newLine && ctGroup.newLine-->
                        <!--ko foreach:{data : ctGroup.sentences,as:'st'}-->
                        <p data-bind="text:st"></p>
                        <!--/ko-->
                        <!--/ko-->
                        <!--ko ifnot:ctGroup.newLine && ctGroup.newLine-->
                        <!--ko text:$root.covertSentences(ctGroup.sentences)--><!--/ko-->
                        <!--/ko-->
                    </div>
                    <div class="el-list">
                        <ul>
                            <!--ko foreach:{data:ctGroup.categories,as:'category'}-->
                            <li>
                                <div class="lessons-text">
                                    <i class="e-icons">
                                        <img data-bind="attr:{src:$root.getCategroyIconUrl(category.categoryIcon)}">
                                    </i>
                                    <span class="text" data-bind="text:category.categoryName">&nbsp;</span>
                                </div>
                                <div class="lessons-btn">
                                    <div data-bind="click:$root.spellingPreview.bind($data,content.lessonId,$root)"><p>预览</p></div>
                                </div>
                            </li>
                            <!--/ko-->
                        </ul>
                    </div>
                    <!--/ko-->
                </div>
                <!--/ko-->
            </div>
        </div>
    </div>
</script>

<script id="t:video_preview_tip" type="text/html">
    <p style="margin-top: -20px; color: #fa7252;">预览视频仅用于演示作答过程，实际内容以页面为准</p>
    <div id="movie">
        <div id="install_flash_player_box" style="margin:20px; display: none;">
            <span id="install_download_tip"
                  style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                您未安装Flash Player插件，请 <a href="http://get.adobe.com/cn/flashplayer/" target="_blank">［点击这里］</a> 下载并安装。
                <br/><br/>
                <span>
                    如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
                </span>
            </span>
        </div>
    </div>
</script>


<script id="T:VACATION_DUBBING" type="text/html">
    <div class="h-set-homework">
        <div class="seth-hd">
            <p class="fl"><span class="noBorder">趣味配音</span></p>
        </div>
        <div class="seth-mn">
            <div class="e-pictureBox">
                <!--ko if:planDetail.practices && planDetail.practices.length > 0-->
                <ul class="clearfix" data-bind="foreach:{data:planDetail.practices,as:'dubbingObj'}">
                    <li class="e-pictureList-2">
                        <div class="picbox">
                            <div class="pic-box" data-bind="click:$root.dubbingView.bind($data,$root)">
                                <img style="width: 100%;" data-bind="attr:{src:dubbingObj.coverUrl}" onerror="this.onerror='';this.src='<@app.link href='public/skin/teacherv3/images/dubbing/img-01.png'/>'">
                                <a class="play-btn" href="javascript:void(0)"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title" data-bind="text:dubbingObj.name">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.albumName">&nbsp;</p>
                            <p class="text" data-bind="text:' 共' + dubbingObj.sentenceSize + '句'">&nbsp;</p>
                            <p class="text" data-bind="text:dubbingObj.topics.join('、')">&nbsp;</p>
                        </div>
                    </li>
                </ul>
                <!--/ko-->
                <!--ko if:!planDetail.practices || planDetail.practices.length == 0-->
                <div class="emptyBox"><i class="empty-icon"></i><p>对不起，还没有满足条件的趣味配音</p></div>
                <!--/ko-->
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="t:SINGLE_DUBBING_PREVIEW">
    <div class="tDubbing-popup">
        <div class="dubPopup-video">
            <div class="video" id="dubbingPlayVideoContainer"></div>
            <span class="playBtn" style="display: none;"></span>
        </div>
        <div class="dubPopup-info">
            <p class="name"><%=dubbingObj.albumName%> • <%=dubbingObj.name%></p>
            <p class="grade" style="display: none;"><%=dubbingObj.clazzLevel%></p>
            <p class="label">
                <%for(var i = 0,iLen = dubbingObj.topics.length; i < iLen; i++){%>
                <span><%=dubbingObj.topics[i]%></span>
                <%}%>
            </p>
        </div>
        <div class="dubPopup-point">
            <div class="describe"><%=dubbingObj.summary%></div>
            <div class="pointTitle">本集知识点</div>
            <div class="pointInfo">
                <p class="txt-type">• 词汇:</p>
                <p class="txt-point">
                    <%for(var z = 0,zLen = dubbingObj.keyWords.length; z < zLen; z++){%>
                    <span><%=dubbingObj.keyWords[z].englishWord%> <%=dubbingObj.keyWords[z].chineseWord%></span>
                    <%}%>
                </p>
            </div>
            <div class="pointInfo">
                <p class="txt-type">• 语法:</p>
                <%for(var j = 0,jLen = dubbingObj.keyGrammars.length; j < jLen; j++){%>
                <p class="txt-point"><%=dubbingObj.keyGrammars[j].grammarName%></p>
                <p class="txt-point"><%=dubbingObj.keyGrammars[j].exampleSentence%></p>
                <%}%>
            </div>
        </div>
    </div>
</script>


<script id="T:VACATION_READING" type="text/html">
    <div class="h-set-homework">
        <div class="seth-hd">
            <p class="fl"><span class="noBorder">绘本阅读</span></p>
        </div>
        <div class="seth-mn">
            <div class="e-pictureBox e-pictureListNoHover" style="height: auto; overflow: hidden">
                <ul class="clearfix" style="width: auto;" data-bind="foreach:{data:planDetail.practices,as:'reading'}">
                    <li class="e-pictureList" data-bind="click:$root.viewReading.bind($data,reading.pictureBookId,planDetail.objectiveConfigType)">
                        <div class="title"><span data-bind="text:reading.pictureBookName">&nbsp;</span></div>
                        <div class="lPic">
                            <img data-bind="attr:{src:reading.pictureBookImgUrl}">
                        </div>
                        <div class="rInfo">
                            <p class="text" style="display: none;" data-bind="visible:reading.pictureBookSeries != null,text:reading.pictureBookSeries != null ? reading.pictureBookSeries : ''">&nbsp;</p>
                            <p class="text" style="display: none;" data-bind="visible:reading.pictureBookClazzLevels && reading.pictureBookClazzLevels.length > 0,text:$root.covertSentences(reading.pictureBookClazzLevels,'、')">&nbsp;</p>
                            <p class="text" style="display: none;" data-bind="visible: reading.pictureBookTopics && reading.pictureBookTopics.length > 0,text:$root.covertSentences(reading.pictureBookTopics,'、')">&nbsp;</p>
                            <p class="text" style="display: none;" data-bind="visible:reading.hasOral">
                                <span class="label">跟读</span>
                            </p>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</script>

<script id="T:VACATION_SINGLE_PACKAGE_STYLE" type="text/html">
    <div class="h-set-homework">
        <div class="seth-hd">
            <p class="fl"><span class="noBorder" data-bind="text:name"></span></p>
        </div>
        <div class="qWei-box">
            <div class="qwList">
                <div class="preBtn" data-bind="click:$root.viewQuestions">预览</div>
                <div class="inner">
                    <!--ko if: ["EXAM","BASIC_KNOWLEDGE","CHINESE_READING","INTELLIGENCE_EXAM"].indexOf(objectiveConfigType) != -1-->
                    <p class="name" data-bind="text:$data.name">&nbsp;</p>
                    <p class="count" data-bind="text:'共' + $data.questions.length + '题'"></p>
                    <!--/ko-->
                    <!--ko if: objectiveConfigType == 'INTERESTING_PICTURE'-->
                    <p class="name" data-bind="text:'主题：'+reading.name"></p>
                    <p class="count" data-bind="text:'共' + reading.questionCount + '道'"></p>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
</script>

<script id="T:VACATION_NEW_READ_RECITE" type="text/html">
    <div class="h-set-homework">
        <div class="seth-hd">
            <p class="fl"><span class="noBorder" data-bind="text:name"></span></p>
        </div>
        <div class="qWei-box" data-bind="foreach:{data:planDetail.packages,as:'packageObj'}">
            <div class="qwList">
                <div class="preBtn" data-bind="click:$root.viewQuestions.bind($data,$parent)">预览</div>
                <div class="inner">
                    <p class="name" data-bind="text:$data.questionBoxName"></p>
                    <p class="count" data-bind="text:'共' + $data.questions.length + '题'"></p>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="T:VACATION_MENTAL" type="text/html">
    <div class="h-set-homework">
        <div class="seth-hd">
            <p class="fl"><span class="noBorder" data-bind="text:name">口算</span></p>
        </div>
        <div class="mCount-box">
            <ul class="mcTable">
                <!--ko foreach:questions-->
                <li data-bind="html:$root.displayQuestionContent($data)"></li>
                <!--/ko-->
            </ul>
        </div>
    </div>
</script>

<script id="T:VACATION_MENTAL_ARITHMETIC" type="text/html">
    <div class="h-set-homework">
        <div class="seth-hd">
            <p class="fl"><span class="noBorder" data-bind="text:name">口算</span></p>
        </div>
        <div class="oralCount" style="border: 0;padding:0;" data-bind="if:planDetail.questionIds.length > 0,visible: planDetail.questionIds.length > 0">
            <div class="t-choose-Knowledge" style="border: 0;">
                <table class="t-questionBox" style="border: 0;">
                    <tbody data-bind="foreach:ko.utils.range(1,Math.ceil(planDetail.questionIds.length/3))">
                        <tr data-bind="foreach:{data:planDetail.questionIds.slice($index() * 3, ($index() + 1) * 3),as:'questionId'}">
                            <td style="background: rgb(255, 255, 255);">
                                <div class="t-question" data-bind="attr:{'id':questionId + '-' + $index()}">正在加载...</div>
                                <div data-bind="text:$root.renderVueQuestion(questionId,$index())"></div>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div class="w-clear"></div>
            </div>
        </div>
    </div>
</script>



<script id="T:VACATION_MATH_INTERESTING_PICTURE" type="text/html">
    <div class="e-lessonsBox">
        <div class="e-lessonsList">
            <div class="el-title" data-bind="text:planDetail.name">趣味关卡</div>
            <div class="el-list">
                <ul>
                    <li>
                        <div class="lessons-text">
                            <span style="width: 118px;padding: 0 5px;" class="text" data-bind="attr:{'title':planDetail.interestingPictureName},text:planDetail.interestingPictureName">&nbsp;</span>
                        </div>
                        <div class="lessons-btn ">
                            <div data-bind="click:$root.picturePreview.bind($data,planDetail.interestingPictureUrl)"><p>预览</p></div>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</script>


<script id="T:UNKNOWN_TEMPLATE" type="text/html">
    <div class="h-set-homework">
        <div class="seth-hd">
            <p class="fl"><span class="noBorder">未知</span></p>
        </div>
        <div class="seth-mn">
            <div class="testPaper-info">
                <div class="inner">
                    <p>未知模板</p>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="t:viewDetailTPL" type="text/html">
    <div style="height: 500px;overflow-y:auto;">
        <div class="finR-video" data-bind="if: hasVideo">
            <div class="frv-left" data-playst="stop" data-bind="attr:{'data-init' : $root.getVideoFlashVars($element)}"></div>
            <div class="frv-right">
                <p data-bind="text: videoInfo.videoSummary"></p>
                <!--ko if: videoInfo.solutionTracks.length > 0-->
                <p>解题技巧：</p>
                <!--ko foreach: videoInfo.solutionTracks-->
                    <p data-bind="text:$data"></p>
                <!--/ko-->
                <!--/ko-->
            </div>
        </div>
        <!--ko if: questions.length > 0-->
        <!--ko foreach: questions.slice(0,$root.showCount())-->
        <div class="h-set-homework">
            <div class="seth-hd">
                <p class="fl">
                    <span data-bind="text:$data.questionType || $data.articleName"></span>
                    <span data-bind="text:$data.paragraphNumber ? ('第' + $data.paragraphNumber + '段') : $data.difficultyName"></span>
                </p>
            </div>
            <div class="seth-mn" data-bind="attr:{id:'subjective_'+$data.id+$index()}">题目加载中...</div>
            <div style="display: none;" data-bind="text:$root.loadQuestionContent($data,$index())"></div>
        </div>
        <!--/ko-->
        <!--/ko-->
        <!--ko if: questions.length > 3 && questions.length > $root.showCount() -->
        <div class="t-dynamic-btn" style="margin: 8px 15px;" data-bind="click:$root.showMoreQuestions">
            <a class="more" href="javascript:void(0);">展开更多</a>
        </div>
        <!--/ko-->
    </div>
</script>

<script id="t:READ_RECITE_WITH_SCORE_QUESTIONS_PREVIEW" type="text/html">
    <div class="aDetails-popup">
        <div class="r-inner showDetails">
            <div class="readTxt-scrol">
                <div class="readTxt-details">
                    <!--ko if: questions.length > 0-->
                    <!--ko foreach: questions.slice(0,$root.showCount())-->
                    <div class="readInner">
                        <i class="itag" style="display: none;" data-bind="visible:$data.paragraphImportant"></i>
                        <div class="r-sub-title subtitle">
                            <!--ko text:'第'+$data.paragraphNumber+'段'--><!--/ko-->
                            <i class="label audio-play" style="display: none;" data-bind="css:{'audio-play':$data.id != $root.playingQuestionId(),'audio-pause':$data.id == $root.playingQuestionId()},visible:$data.listenUrls && $data.listenUrls.length > 0,click:$root.playAudio.bind($data,$root,$element)"></i>
                        </div>
                        <div class="segment" data-bind="attr:{id:'subjective_'+$data.id+$index()}">题目加载中...</div>
                        <div style="display: none;" data-bind="text:$root.loadQuestionContent($data,$index())"></div>
                    </div>
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko if: questions.length > 3 && questions.length > $root.showCount() -->
                    <div class="t-dynamic-btn" data-bind="click:$root.showMoreQuestions">
                        <a class="more" href="javascript:void(0);">展开更多</a>
                    </div>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>
</script>

<script id="t:PREVIEW_IMAGE" type="text/html">
    <div style="background-color: white; width: 98%;">
        <img src="<%=imgUrl%>" style="display:block;margin: 0 auto;max-width: 100%;max-height: 500px;" />
    </div>
</script>


<script type="text/javascript">
    var constantObj = {
        subject             : "${subject!}",
        earliestStartDateTime : "${earliestStartDateTime}",
        defaultStartDateTime: "${defaultStartDateTime}",
        latestStartDateTime : "${latestStartDateTime}",
        earliestEndDateTime : "${earliestEndDateTime}",
        defaultEndDateTime : "${defaultEndDateTime}",
        latestEndDateTime : "${latestEndDateTime}",
        basicIconPrefixUrl  : "<@app.link href='public/skin/teacherv3/images/homework/english-icon/' />",
        readingIconPrefixUrl: "<@app.link href='public/skin/teacherv3/images/homework/' />",
        readingDefaultIcon  : "<@app.link href='public/skin/teacherv3/images/homework/envelope-tea.png'/>",
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>",
        imgDomain           : '${imgDomain!''}',
        domain              : '${requestContext.webAppBaseUrl}/',
        env                 : <@ftlmacro.getCurrentProductDevelopment />,
        adValidaty          : "${(adValidaty!false)?string}"
    };

    function nextHomeWork(){
        $.prompt.close();
    }

    $(function(){
        LeftMenu.focus("${subject!}_vacation");

        $17.vacation.getVacationIndex().run();

        $17.voxLog({
            module : "m_elhqnSjz",
            op     : "page_winter_vacation",
            s0     : "${subject!}"
        });
    });
</script>
<@sugar.capsule js=["vacation.index","homework2nd"] />
</@shell.page>