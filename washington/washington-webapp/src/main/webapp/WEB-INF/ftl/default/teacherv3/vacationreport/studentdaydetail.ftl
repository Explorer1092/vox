<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="">
    <@sugar.capsule js=["jquery.flashswf"] css=["homeworkv3.homework","new_teacher.module"]/>
<div id="mainContent" class="h-homeworkCorrect">
    <h4 class="link">
        <a href="/">首页</a>&gt;<a data-bind="attr:{href:'/teacher/vacation/report/list.vpage?subject='+$root.subject()}">假期作业列表</a>&gt;<a data-bind="attr:{href:'/teacher/vacation/report/clazzreport.vpage?packageId='+$root.packageId()}">作业报告</a>&gt;<a data-bind="attr:{href: $root.href()}">任务列表</a>&gt;<span>作业详情</span>
    </h4>
    <div class="w-base" style="border-top: 0;">
        <div class="J_eventBind">
            <div class="J_mainContentHolder hc-main" id="tabContentHolder"></div>
        </div>
    </div>
    <#--评语奖励-->
        <div id="ufo" class="t-homework-total t-homework-total-static" style="width: 999px;" data-bind="if:!webLoading(),visible:!webLoading()">
            <div class="t-homework-total-inner" style="width: 999px;">
                <dl>
                    <dd style="margin-left: 100px;">
                        <div class="t-btn">
                            <a href="javascript:void(0);" style="width: 115px;" data-op="a_key_comment_btn" id="showComment" class="w-btn w-btn-small" >写评语</a>
                            <a href="javascript:void(0);" style="width: 115px;" class="w-btn w-btn-small" id="rewardBeans" >发奖励</a>
                        </div>
                    </dd>
                </dl>
            </div>
        </div>
</div>
<div id="clazzHomeworkReportEventDiv"></div>
<div id="showBigPic" style="display:none;">
    <div class="t-viewPicture-box-mask"></div>
    <div class="t-viewPicture-box">
        <div class="flex-viewport">
            <div class="head">
                <div class="title"></div>
                <div class="close">×</div>
            </div>
            <div style=" position: relative;">
                <ul class="J_bigPicItem list" style="margin-bottom: 30px;"></ul>
            </div>
            <ul class="flex-direction-nav" style="display: none;">
                <li><a class="flex-prev flex-disabled" href="javascript:void(0);">Previous</a></li>
                <li><a class="flex-next flex-disabled" href="javascript:void(0);">Next</a></li>
            </ul>
        </div>
    </div>
</div>
<script type="text/html" id="BASIC_APP">
    <div class="w-base-title" style="background-color: #e1f0fc;">
        <h3 data-bind="text:tabName">&nbsp;</h3>
    </div>
    <div class="h-basicExercises">
        <table class="table-cell02" cellpadding="0" cellspacing="0">
            <thead>
            <tr>
                <td colspan="2" class="time">课时</td>
                <td class="apps">应用</td>
                <td class="grade">得分</td>
            </tr>
            </thead>
            <tbody>
            <!--ko if:result.length > 0-->
            <!--ko foreach:{data:result,as:'unit'}-->
            <!--ko foreach:{data:unit.lessons,as:'lesson'}-->
            <!--ko foreach:{data:lesson.categories,as:'category'}-->
            <tr>
                <!--ko if:$parentContext.$index() == 0 && $index() == 0-->
                <td class="unit" data-bind="attr:{rowspan:$root.rowspanCount(unit.lessons)},text:unit.unitName">&nbsp;</td>
                <!--/ko-->
                <!--ko if:$index() == 0-->
                <td class="lesson" data-bind="attr:{rowspan:lesson.categories.length}"><span class="name" data-bind="text:lesson.lessonName">&nbsp;</span></td>
                <!--/ko-->
                <td class="apps" data-bind="text:category.categoryName">&nbsp;</td>
                <td class="grade" data-bind="text:category.averageScore != null ? category.averageScore : ''">&nbsp;</td>
            </tr>
            <!--/ko-->
            <!--/ko-->
            <!--/ko-->
            <!--/ko-->
            </tbody>
        </table>
    </div>
</script>
<script type="text/html" id="READING">
    <div class="w-base-title" style="background-color: #e1f0fc;">
        <!--ko if:result.finishedCount >= result.totalPictureBook-->
        <h3>绘本阅读（平均得分<!--ko text:result.avgScore--><!--/ko-->分，完成用时<!--ko text:$root.convertSecondToMin(result.avgDuration)--><!--/ko-->）</h3>
        <!--/ko-->
        <!--ko ifnot:result.finishedCount >= result.totalPictureBook-->
        <h3>绘本阅读（完成<!--ko text:result.finishedCount--><!--/ko-->本，共<!--ko text:result.totalPictureBook--><!--/ko-->本）</h3>
        <!--/ko-->
    </div>
    <div class="pb-stuDetails">
        <table cellpadding="0" cellspacing="0">
            <thead>
            <tr>
                <td class="name">绘本</td>
                <td class="score">得分</td>
                <td class="time">用时</td>
                <#--<td class="operation">更多操作</td>-->
            </tr>
            </thead>
            <tbody>
            <!--ko if:result.studentAchievement && result.studentAchievement.length > 0-->
            <!--ko foreach:{data:result.studentAchievement,as:'sa'}-->
            <tr>
                <td class="name">
                    <div class="e-pictureList">
                        <div class="lPic" style="cursor: pointer;" data-bind="click:$root.viewReading.bind($data,$root)">
                            <img data-bind="attr: { src : $root.imgsrc($element,sa.pictureBookThumbImgUrl) }">
                        </div>
                        <div class="rInfo">
                            <div class="title"><a href="javascript:void(0);" data-bind="click:$root.viewReading.bind($data,$root),text:sa.pictureBookName">&nbsp;</a></div>
                            <p class="text" data-bind="text:sa.pictureBookSeries">&nbsp;</p>
                            <!--ko if:$root.tab == "READING"-->
                            <p class="text" data-bind="text:sa.pictureBookClazzLevels.join('、')">&nbsp;</p>
                            <!--/ko-->
                            <!--ko if:$root.tab == 'LEVEL_READINGS'-->
                            <p class="text" data-bind="text:sa.pictureBookClazzLevelName">&nbsp;</p>
                            <!--/ko-->
                            <p class="text" data-bind="text:sa.pictureBookTopics.join('、')">&nbsp;</p>
                            <p class="text" data-bind="text:sa.pictureBookSeries">&nbsp;</p>
                            <p class="text" data-bind="if:sa.practices && sa.practices.length > 0,visible:sa.practices && sa.practices.length > 0">
                                <!--ko foreach:{data:sa.practices,as:'practice'}-->
                                <span class="radioIcon selected"><i></i><!--ko text:practice.typeName--><!--/ko--></span>
                                <!--/ko-->
                            </p>
                        </div>
                    </div>
                </td>
                <td class="score" data-bind="text:sa.score + '分'">&nbsp;</td>
                <td class="time" data-bind="text:$root.convertSecondToMin(sa.duration)">0分钟</td>
                <#--<td class="operation">-->
                    <#--<a class="operation" target="_blank" data-bind="click:$root.viewPicBookClick,attr:{href:'/teacher/new/homework/report/readingdetail.vpage?homeworkId=' + $root.homeworkId + '&studentId=' + $root.studentId + '&readingId=' + sa.pictureBookId}">查看详情</a>-->
                <#--</td>-->
            </tr>
            <!--/ko-->
            <!--/ko-->
            <!--ko ifnot:result.studentAchievement && result.studentAchievement.length > 0-->
            <tr>
                <td colspan="4">没有绘本完成</td>
            </tr>
            <!--/ko-->
            </tbody>
        </table>
    </div>
</script>
<script type="text/html" id="ORAL_PRACTICE">
    <div class="w-base-title" style="background-color: #e1f0fc;">
        <h3>口语习题</h3>
        <div class="w-base-right">
            <span class="hoverText">分数计算说明</span>
            <div class="hoverInfo">
                <span class="jf-arrow"></span>
                <p>1、本练习总成绩为所有题目平均分</p>
                <p>2、等级计算关系</p>
                <table>
                    <tr><td>A</td><td>B</td><td>C</td><td>D</td></tr>
                    <tr><td>100分</td><td>85分</td><td>70分</td><td>60分</td></tr>
                </table>
            </div>
        </div>
    </div>
    <!--ko foreach:{data:result,as:'sa'}-->
    <div class="h-set-homework" data-bind="attr: {qid:sa.qid }">
        <div class="seth-hd">
            <p class="fl"><span data-bind="text:sa.contentType"></span><span class="border-none" data-bind="text:$root.showDifficulty(sa.difficulty)"></span></p>
        </div>
        <div>
            <div data-bind="attr: {id:'subject_'+sa.qid}" class="seth-mn"></div>
            <div data-bind="text:$root.renderSubject(sa.qid)"></div>
        </div>
        <!--ko if:sa.answerList && sa.answerList.length > 0-->
        <div class="speakWorks">
            <!--ko foreach:{data:sa.answerList,as:'item'}-->
            <div class="speak-header">学生答案
                <!-- ko if: sa.answerList.length > 1 -->
                <span data-bind="text:$root.answerIndex($index())"></span>
                <!--/ko-->
            </div>
            <div class="speak-main">
                <div class="sw-list">
                    <div class="voicePlayer speak" data-bind="click:$root.playAudio.bind($data,$element,$root),attr:{index:$index(),qid:$parent.qid},css: {listen: item.userVoiceUrls && item.userVoiceUrls.length == 0}">
                        <!--ko if:item.userVoiceUrls && item.userVoiceUrls.length == 0-->听读模式<!--/ko-->
                        <span class="icon" data-bind="text:item.score"></span>
                    </div>
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
    </div>
    <!--/ko-->
</script>

<script type="text/html" id="NEW_READ_RECITE">
    <div class="read-aDetails">
        <div class="r-title">课文读背</div>
        <!--ko foreach:{data : $root.result,as:'item'}-->
        <!--ko if: item.data.length > 0-->
        <div class="aDetails-section">
            <div class="r-title" data-bind="text:item.name"></div>
            <!--ko foreach:item.data-->
            <div class="r-inner">
                <div class="readTxt-list2">
                    <div class="frAside">
                        <span class="playIcon voicePlayer" data-bind="click:$root.playAudio.bind($data,$element,$root)"></span>
                        <div class="paragr-btn" data-bind="css:{'showDetails':$data.showDetail()},click:$root.showDetailBtn">
                            <span data-bind="text: $data.showDetail() ? '收起段落':'展开段落'"></span>
                            <span class="arrow"></span>
                        </div>
                    </div>
                    <div class="fl-aside">
                        <p class="name">
                            <span data-bind="text:$data.lessonName"></span>
                            <span class="levelIcon" style="display: none;">
                                <span data-bind="text:$data.correctionInfo"></span>
                                <a class="level-option level-top" href="javascript:void(0);" style="display:none;">
                                    <i class="posit-opt"></i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'阅','REVIEW')">阅</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'优','EXCELLENT')">优</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'良','GOOD')">良</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'中','FAIR')">中</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'差','PASS')" class="bord-none">差</i>
                                </a>
                            </span>
                        </p>
                        <p class="textGray" data-bind="text:item.name+'：第'+ $data.paragraphDescription +'段'"></p>
                    </div>
                </div>
                <div class="readTxt-details" style="display:none;" data-bind="visible: $data.showDetail()">
                    <!--ko foreach: $data.paragraphDetaileds-->
                    <div class="readInner">
                        <div class="r-sub-title">
                            <span data-bind="text:'第'+$data.paragraphOrder+'段'"></span>
                            <!--ko if:$data.paragraphDifficultyType -->
                            <span class="label">（重点段落）</span>
                            <!--/ko-->
                        </div>
                        <div class="r-inner" data-bind="attr:{id: 'subject_' + $data.questionId}">题目加载中...</div>
                        <div class="r-playBtn"><span class="playIcon voicePlayer" data-bind="click:$root.playAudio.bind($data,$element,$root)"></span></div>
                    </div>
                    <!--/ko-->
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="DUBBING">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>趣味配音</h3>
        </div>
        <div class="h-video-tab">
            <table class="video-info-box">
                <thead>
                <tr>
                    <td class="item">配音内容</td>
                    <td class="item">用时</td>
                    <td class="item">作品</td>
                </tr>
                </thead>
                <tbody data-bind="foreach:{data : $root.result.studentAchievement,as:'dubbingObj'}">
                <tr>
                    <td>
                        <p class="v-name" data-bind="text:dubbingObj.name">&nbsp;</p>
                        <p data-bind="text:dubbingObj.albumName">&nbsp;</p>
                        <p data-bind="text:dubbingObj.clazzLevel + ' 共' + dubbingObj.sentenceSize + '句'">&nbsp;</p>
                        <p data-bind="text:dubbingObj.topics.join(' / ')">&nbsp;</p>
                    </td>
                    <td data-bind="text:$root.convertSecondToMin(dubbingObj.duration)">0分0秒</td>
                    <td><a class="play-btn" href="javascript:void(0)" data-bind="click:$root.playVideoPopup.bind($data,$root)"></a></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</script>

<script type="text/html" id="READ_RECITE_WITH_SCORE">
    <div class="read-aDetails">
        <div class="r-title w-base-title" style="height: 26px;">
            课文读背
            <div class="w-base-right" style="padding: 0;">
                <span class="hoverText" style="padding:0;">达标规则</span>
                <div class="hoverInfo" style="width: 218px;height: 24px;right: 0px;" >
                    <span class="jf-arrow"></span>
                    <p>达标的段落数超过60%，则该篇课文达标</p>
                </div>
            </div>
        </div>
        <!--ko foreach:{data:result,as:'typeObj'}-->
        <!--ko if:typeObj.lessonList.length > 0-->
        <div class="aDetails-section">
            <div class="r-title" data-bind="text:typeObj.name">&nbsp;</div>
            <!--ko foreach:{data:typeObj.lessonList,as:'lesson'}-->
            <div class="r-inner">
                <div class="readTxt-list2 ">
                    <div class="frAside playAll" data-bind="click:$root.playLessonAudio.bind($data,$root)">
                        <!-- audio-pause -->
                        <span class="audio-play" data-bind="css:{'audio-play' : $root.playingBoxId() != lesson.questionBoxId,'audio-pause':$root.playingBoxId() == lesson.questionBoxId}"></span>
                        播放全部
                    </div>
                    <div class="fl-aside">
                        <p class="name" data-bind="text:lesson.lessonName">&nbsp;</p>
                        <p class="textGray">
                            <!--ko text:typeObj.name--><!--/ko-->段落：第<!--ko text:$root.generateParagraphOrders(lesson.paragraphDetails)--><!--/ko-->段
                        </p>
                    </div>
                </div>
                <div class="readTxt-details" data-bind="if:lesson.paragraphDetails.length > 0,visible:lesson.paragraphDetails.length > 0">
                    <!--ko foreach:{data:lesson.paragraphDetails,as:'paragraph'}-->
                    <div class="readInner">
                        <i class="itag" data-bind="if:paragraph.paragraphDifficultyType,visible:paragraph.paragraphDifficultyType"></i>
                        <div class="r-sub-title subtitle" data-bind="click:$root.paragraphShowOrHide.bind($data,$root,lesson.questionBoxId,$index())">
                            第<!--ko text:paragraph.paragraphOrder--><!--/ko-->段
                            <span class="label" data-bind="css:{'noreach':!paragraph.standard},text:paragraph.standard ? '达标' : '未达标'">&nbsp;</span>
                            <div class="frAside2" data-bind="css:{'showDetails' : $root.showParagraphId() == (lesson.questionBoxId + '_' + $index())}">
                                <span data-bind="css:{'audio-play':$root.playingParagrapId() != (lesson.questionBoxId + '_' + $index()),'audio-pause':$root.playingParagrapId() == (lesson.questionBoxId + '_' + $index())},click:$root.playAudio.bind($data,$root,lesson.questionBoxId,$index()),clickBubble:false"></span>
                                <div class="paragr-btn">
                                    <span class="arrow"></span>
                                </div>
                            </div>
                        </div>
                        <div class="r-inner r-part" style="display: none;" data-bind="visible:$root.showParagraphId() == (lesson.questionBoxId + '_' + $index()),attr:{id:'c_' + lesson.questionBoxId + '_' + $index()}">
                        </div>
                    </div>
                    <!--/ko-->
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:PLAY_VIDEO_OF_SINGLE_DUBBING_POPUP">
    <div class="tDubbing-popup">
        <div class="dubPopup-video">
            <div class="video" id="dubbingPlayVideoContainer"></div>
            <span class="playBtn" style="display: none;"></span>
        </div>
    </div>
</script>

<div id="jquery_jplayer_1" class="jp-jplayer"></div>
    <#include "../homeworkhistoryv3/htmlchip/studentcommentchip.ftl">
    <#include "../homeworkhistoryv3/htmlchip/singlerewardbeanschip.ftl">
<script type="text/javascript">
    var answerDetailData = {
        env : <@ftlmacro.getCurrentProductDevelopment />,
        imgDomain : '${imgDomain!''}',
        domain : '${requestContext.webAppBaseUrl}/',
        examUrl:'${examPcUrl!''}',
        defaultPicUrl:"<@app.link href="public/skin/teacherv3/images/homework/upflie-img.png"/>",
        movie: "<@app.link href="resources/apps/flash/voicePlayer/VoiceReplayer.swf"/>",
        defaultReadingImg : "<@app.link href="public/skin/teacherv3/images/homework/envelope-tea.png"/>",
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
    };

    var stuDayDetail = {
        subject : "${subject!}"
    };
</script>
<script type="text/javascript">
    var constantObj = {
        homeworkId       : "${homeworkId!}",
        homeworkType     : "${homeworkType!}",
        clazzId          : "${clazzId!}",
        subject          : "${(curSubject)!}",
        tabIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/tab-icon' />',
        userAuth           : !!"${((currentUser.fetchCertificationState())?? && currentUser.fetchCertificationState() == "SUCCESS")?string}",
        debug              : !$17.isBlank($17.getQuery("test")),
        imgDomain        : '${imgDomain!''}',
        domain           : '${requestContext.webAppBaseUrl}/',
        env              : <@ftlmacro.getCurrentProductDevelopment />
    };
    $(function(){
        $17.backToTop();

        $(window).on("scroll", function(){
            var $Ufo = $("#ufo");
            if($Ufo.length > 0){
                if(($Ufo.offset().top - $(window).height() >  $(window).scrollTop()) ){
                    $Ufo.removeClass("t-homework-total-static");
                }else{
                    $Ufo.addClass("t-homework-total-static");
                }
            }
        });
    });
</script>
    <@sugar.capsule js=["ko","homework2nd","jplayer","vacationhistory.studentdaydetail"] />
</@shell.page>