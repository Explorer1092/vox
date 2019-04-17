<#import '../../../layout/layout.ftl' as temp>
<@temp.page pageName='homeworkreport'>
    <@sugar.capsule js=["jquery.flashswf","plugin.venus-pre"] css=["plugin.venus-pre","homeworkv3.wordrecognitionandreading","homeworkhistory.report","homeworkhistory.studentwordteachandpracticedetail"] />
<style type="text/css">
    /*课文读背题目内容样式*/
    .readInner .r-part .paraNotShiGe{
        text-indent: 0!important;
        display: inline;
    }
    .readInner .r-part .ruby {
        display: inline-block;
        margin: 0 2px 6px;
        text-align: center;
        line-height: 14px;
        font-size: 14px;
        color: #4b4b4b;
    }
    .readInner .r-part .rt {
        display: block;
        margin-bottom: 2px;
    }
    .readInner .r-part .tRed{
        color: #ff7563;
    }
    .readInner .r-part .word{
        display: inline-block;
    }
</style>
<style type="text/css">
    .w-tableBox{ margin:23px 0; overflow: hidden; clear: both;}
    .w-tableBox table caption{ color: #383a4b; font-size: 14px; border-top: 1px solid #eaeaea; background-color: #eef6f9; padding: 12px; text-align: left; font-weight: bold;}
    .w-tableBox table{ border-collapse: collapse; margin: 0 0 0 -1px; padding: 0; background-color: #fff; width: 100%;}
    .w-tableBox table td, .w-tableBox table th{ padding: 10px 27px; border: solid #eaeaea; border-width: 1px 0 1px 1px;}
    .w-tableBox table thead td, .w-tableBox table thead th{ color: #383a4b; font-weight: bold; line-height: 120%;}
    .w-tableBox table tbody td, .w-tableBox table tbody th{ color: #999; font-weight: normal; font-size: 12px; line-height: 150%; }
    .w-tableBox table th{ font-weight: normal;}
    .w-tableBox table tbody td a, .w-tableBox table tbody th a{ color: #00aced; text-decoration: none; display: inline-block; margin-right: 10px; }
</style>
<div class="t-center-container">
    <div class="breadcrumb" style="padding: 15px 0;">
        <span><a class="w-blue" href="/student/index.vpage">首页</a> &gt;</span>
        <span><a class="w-blue" href="/student/learning/history/newhomework/homeworkreport.vpage?homeworkId=${homeworkId}&subject=${subjectName!}">作业详情</a> &gt;</span>
        <span>答题详情</span>
    </div>
    <div class="leftLayer-Box">
        <div class="top"></div>
        <div class="main"><ul class="J_navigation"></ul></div>
        <div class="footer"></div>
    </div>
    <div class="J_mainConent h-answerDetails h-historyBox" id="tabContentHolder"></div>
</div>
<script type="text/html" id="BASIC_APP">
    <div class="h-header" style="margin-bottom: 15px;">
        <div class="h-title-2"><span class="left-text">作业题目 -- 答题详情</span></div>
    </div>
    <div class="w-tableBox">
        <table>
            <thead>
            <tr>
                <td colspan="2">课时</td>
                <td>应用</td>
                <td>更多操作</td>
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
                    <td class="operation"><a class="operation" target="_blank" data-bind="attr:{href:'/student/learning/history/categorydetail.vpage?hid=' + $root.homeworkId + '&categoryId=' + category.categoryId + '&lessonId=' + lesson.lessonId + '&objectiveConfigType=' + $root.tab},visible:category.averageScore != null">查看详情</a></td>
                </tr>
                <!--/ko-->
                <!--/ko-->
                <!--/ko-->
                <!--/ko-->
            </tbody>
        </table>
    </div>
</script>
<script type="text/html" id="LS_KNOWLEDGE_REVIEW">
    <div class="h-header" style="margin-bottom: 15px;">
        <div class="h-title-2"><span class="left-text">作业题目 -- 答题详情</span></div>
    </div>
    <div class="w-tableBox">
        <table>
            <thead>
            <tr>
                <td colspan="2">课时</td>
                <td>应用</td>
                <td>更多操作</td>
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
                <td class="operation"><a class="operation" target="_blank" data-bind="attr:{href:'/student/learning/history/categorydetail.vpage?hid=' + $root.homeworkId + '&categoryId=' + category.categoryId + '&lessonId=' + lesson.lessonId + '&objectiveConfigType=LS_KNOWLEDGE_REVIEW'},visible:category.averageScore != null">查看详情</a></td>
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
    <div class="h-title">
        <span class="left-text">作业练习——答题详情</span>
    </div>
    <div class="pb-stuDetails">
        <table cellpadding="0" cellspacing="0">
            <thead>
            <tr>
                <td class="name">绘本</td>
                <td class="time">用时</td>
                <td class="operation">更多操作</td>
            </tr>
            </thead>
            <tbody>
            <!--ko if:result.studentAchievement && result.studentAchievement.length > 0-->
            <!--ko foreach:{data:result.studentAchievement,as:'sa'}-->
            <tr>
                <td class="name">
                    <div class="e-pictureList" data-bind="css:{'diffNew' : $root.tab == 'LEVEL_READINGS'}">
                        <div class="lPic" style="cursor:pointer;" data-bind="click:$root.viewReading.bind($data,sa.pictureBookId,sa.keywords,$root)">
                            <img data-bind="attr: { src : $root.imgsrc($element,sa.pictureBookThumbImgUrl) }">
                        </div>
                        <div class="rInfo">
                            <div class="title"><a href="javascript:void(0)" data-bind="click:$root.viewReading.bind($data,sa.pictureBookId,sa.keywords,$root),text:sa.pictureBookName">&nbsp;</a></div>
                            <!--ko if:$root.tab == 'READING'-->
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
                <td class="time" data-bind="text:$root.convertSecondToMin(sa.duration)">&nbsp;</td>
                <td class="operation">
                    <a class="operation" data-bind="attr:{href:'/student/learning/history/readingdetail.vpage?homeworkId=' + $root.homeworkId + '&readingId=' + sa.pictureBookId + '&type=' + $root.tab}">查看详情</a>
                </td>
            </tr>
            <!--/ko-->
            <!--/ko-->
            </tbody>
        </table>
    </div>
</script>

<script type="text/html" id="NEW_READ_RECITE">
    <div class="h-title">
        <span class="left-text">作业题目 -- 答题详情</span>
    </div>
    <div class="read-aDetails">
        <div class="r-title">课文读背</div>
        <!--ko foreach:{data : $root.result,as:'item'}-->
        <!--ko if: item.data.length > 0-->
        <div class="aDetails-section">
            <div class="r-title" data-bind="text:item.name"></div>
            <!--ko foreach:item.data-->
            <div class="r-inner" data-bind="css:{'showDetails':$data.showDetail()}">
                <div class="readTxt-list">
                    <div class="frAside">
                        <span class="playIcon voicePlayer" data-bind="click:$root.playAudio.bind($data,$element,$root)"></span>
                        <div style="display: inline-block;" data-bind="click:$root.showDetailBtn">
                            <span data-bind="text: $data.showDetail() ? '收起段落':'展开段落'"></span>
                            <span class="arrow"></span>
                        </div>
                    </div>
                    <div class="fl-aside">
                        <p class="name">
                            <span data-bind="text:$data.lessonName"></span>
                            <span class="levelIcon" data-bind="css: $data.levelIcon"></span>
                        </p>
                        <p class="textGray" data-bind="text:item.name+'：第'+ $data.paragraphDescription +'段'"></p>
                    </div>
                </div>
                <div class="readTxt-details">
                    <!--ko foreach: $data.paragraphDetaileds-->
                    <div class="readInner">
                        <div class="r-title">
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

<script type="text/html" id="ORAL_PRACTICE">
    <div class="h-header" style="margin-bottom: 15px;">
        <div class="h-title-2"><span class="left-text">作业题目 -- 答题详情</span></div>
        <div class="jf-intro">
            <span class="hoverText"等级计算说明</span>
            <div class="hoverInfo">
                <span class="jf-arrow"></span>
                <p>等级计算关系</p>
                <table>
                    <tbody>
                        <tr>
                            <td>A</td><td>B</td><td>C</td><td>D</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <!--ko if:$root.questionDetailList && $root.questionDetailList.length > 0-->
    <!--ko foreach:{data:$root.questionDetailList,as:'questionDetail'}-->
    <div class="h-answerD-list">
        <div class="hd">
            <div class="box" data-bind="attr:{id:'previewQuestionImg' + $index()}"></div>
            <div data-bind="text:$root.loadExamImg('previewQuestionImg' + $index(),'preExamImg',questionDetail.qid,$index())"></div>
        </div>
        <div class="mn" style="display: none;" data-bind="visible:questionDetail.answerList && questionDetail.answerList.length > 0">
            <!--ko foreach:{data:questionDetail.answerList,as:'answer'}-->
            <div class="speakWorks">
                <div class="speak-header" data-bind="text:questionDetail.answerList.length > 1 ? '学生答案：' + '第' + ($index() + 1) + '小题' : '学生答案'">学生答案</div>
                <div class="speak-main">
                    <div class="sw-list">
                        <div class="name" style="display: none;">张三</div>
                        <div class="speak voicePlayer" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                            <span class="icon" data-bind="text: answer.score, css:{'iconGreen' : answer.realScore <= 60}">A</span>
                        </div>
                    </div>
                </div>
            </div>
            <!--/ko-->
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->
</script>


<script type="text/html" id="DUBBING">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>趣味配音</h3>
        </div>
        <div class="h-video-tab">
            <table class="video-info-box">
                <thead>
                <tr data-bind="css:{'column4':$root.tab == 'DUBBING_WITH_SCORE'}">
                    <td class="item">配音内容</td>
                    <td class="item">用时</td>
                    <td class="item">作品</td>
                </tr>
                </thead>
                <tbody data-bind="foreach:{data : $root.result.studentAchievement,as:'dubbingObj'}">
                <tr data-bind="css:{'column4':$root.tab == 'DUBBING_WITH_SCORE'}">
                    <td>
                        <p class="v-name" data-bind="text:dubbingObj.name">&nbsp;</p>
                        <p data-bind="text:dubbingObj.albumName">&nbsp;</p>
                        <p data-bind="text:dubbingObj.clazzLevel + ' 共' + dubbingObj.sentenceSize + '句'">&nbsp;</p>
                        <p data-bind="text:dubbingObj.topics.join(' / ')">&nbsp;</p>
                    </td>
                    <td data-bind="text:$root.convertSecondToMin(dubbingObj.duration)">0分0秒</td>
                    <td>
                        <!--ko if:dubbingObj.syntheticSuccess-->
                        <a class="play-btn" href="javascript:void(0)" data-bind="click:$root.playVideoPopup.bind($data,$root)"></a>
                        <!--/ko-->
                        <!--ko ifnot:dubbingObj.syntheticSuccess-->
                        <a href="javascript:void(0)" title="配音正在合成，请稍后再试或联系客服解决~">配音合成中</a>
                        <!--/ko-->
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</script>

<script type="text/html" id="READ_RECITE_WITH_SCORE">
    <div class="read-aDetails">
        <div class="r-title">课文读背</div>
        <!--ko foreach:{data:result,as:'typeObj'}-->
        <!--ko if:typeObj.lessonList.length > 0-->
        <div class="aDetails-section">
            <div class="r-title" data-bind="text:typeObj.name">&nbsp;</div>
            <!--ko foreach:{data:typeObj.lessonList,as:'lesson'}-->
            <div class="r-inner sink-student">
                <div class="readTxt-list2 ">
                    <div class="frAside playAll">
                        <span class="audio-play" data-bind="css:{'audio-play' : $root.playingBoxId() != lesson.questionBoxId,'audio-pause':$root.playingBoxId() == lesson.questionBoxId},click:$root.playLessonAudio.bind($data,$root)"></span>
                        播放全部
                    </div>
                    <div class="fl-aside">
                        <p class="name"  data-bind="text:lesson.lessonName">&nbsp;</p>
                        <p class="textGray">
                            <!--ko text:typeObj.name--><!--/ko-->段落：第<!--ko text:$root.generateParagraphOrders(lesson.paragraphDetails)--><!--/ko-->段
                        </p>
                    </div>
                </div>
                <div class="readTxt-details" style="display: block;" data-bind="if:lesson.paragraphDetails.length > 0,visible:lesson.paragraphDetails.length > 0">
                    <!--ko foreach:{data:lesson.paragraphDetails,as:'paragraph'}-->
                    <div class="readInner">
                        <i class="itag" data-bind="if:paragraph.paragraphDifficultyType,visible:paragraph.paragraphDifficultyType"></i>
                        <div class="r-sub-title subtitle" data-bind="click:$root.paragraphShowOrHide.bind($data,$root,lesson.questionBoxId,$index())">
                            第<!--ko text:paragraph.paragraphOrder--><!--/ko-->段
                            <span class="label" data-bind="css:{'noreach':!paragraph.standard},text:paragraph.standard ? '达标' : '未达标'">&nbsp;</span>
                            <div class="frAside2" data-bind="css:{'showDetails' : $root.showParagraphId() == (lesson.questionBoxId + '_' + $index())}"><!-- showDetails 展开段落 -->
                                <span data-bind="css:{'audio-play':$root.playingParagrapId() != (lesson.questionBoxId + '_' + $index()),'audio-pause':$root.playingParagrapId() == (lesson.questionBoxId + '_' + $index())},click:$root.playAudio.bind($data,$root,lesson.questionBoxId,$index()),clickBubble:false"></span>
                                <div class="paragr-btn">
                                    <span class="arrow"></span>
                                </div>
                            </div>
                        </div>
                        <div class="r-inner r-part" style="display: none;" data-bind="visible:$root.showParagraphId() == (lesson.questionBoxId + '_' + $index()),attr:{id:'c_' + lesson.questionBoxId + '_' + $index()}">
                            题目加载中...
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


<script type="text/html" id="WORD_RECOGNITION_AND_READING">
    <div class="read-aDetails">
        <div class="r-title w-base-title" style="height: 26px;">
            生字认读
        </div>
        <!--ko if:result.length > 0-->
        <div class="aDetails-section">
            <div class="r-title">生字认读</div>
            <!--ko foreach:{data:result,as:'lesson'}-->
            <div class="r-inner sink-student">
                <div class="readTxt-list2">
                    <div class="frAside playAll" data-bind="click:$root.playLessonAudio.bind($data,$root)">
                        <!-- audio-pause -->
                        <span class="audio-play" data-bind="css:{'audio-play' : $root.playingBoxId() != lesson.questionBoxId,'audio-pause':$root.playingBoxId() == lesson.questionBoxId}"></span>
                        播放全部
                    </div>
                    <div class="fl-aside">
                        <p class="name" data-bind="text:lesson.lessonName">&nbsp;</p>
                        <p class="textGray" data-bind="text:lesson.standardStr">&nbsp;</p>
                    </div>
                </div>
                <div class="readTxt-details" style="display: block;" data-bind="if:lesson.detailList.length > 0,visible:lesson.detailList.length > 0">
                    <!--ko foreach:{data:lesson.detailList,as:'question'}-->
                    <div class="readInner">
                        <i class="itag" data-bind="if:question.paragraphDifficultyType,visible:question.paragraphDifficultyType"></i>
                        <div class="r-sub-title subtitle" style="line-height: 40px;" data-bind="click:$root.paragraphShowOrHide.bind($data,$root,lesson.questionBoxId,$index())">
                            <!--ko text:($index() + 1)--><!--/ko-->
                            <div class="characters">
                                <p class="phonetic" data-bind="text:question.pinYinMark">&nbsp;</p>
                                <p class="charaWord" data-bind="text:question.chineseWordContent">&nbsp;</p>
                            </div>
                            <span class="label" data-bind="css:{'noreach':!question.standard},text:question.standard ? '达标' : '未达标'">&nbsp;</span>
                            <div class="frAside2 playAll" data-bind="css:{'showDetails' : $root.showParagraphId() == (lesson.questionBoxId + '_' + $index())}">
                                <span data-bind="css:{'audio-play':$root.playingParagrapId() != (lesson.questionBoxId + '_' + $index()),'audio-pause':$root.playingParagrapId() == (lesson.questionBoxId + '_' + $index())},click:$root.playAudio.bind($root,{id:lesson.questionBoxId + '_' + $index(),audioUrl:$data.voices}),clickBubble:false"></span>
                                <div class="paragr-btn">
                                    <span class="arrow"></span>
                                </div>
                            </div>
                        </div>
                        <div class="r-inner r-part" data-bind="if:$root.showParagraphId() == (lesson.questionBoxId + '_' + $index()),visible:$root.showParagraphId() == (lesson.questionBoxId + '_' + $index())">
                            <word-recognition-and-reading params="question:$root.getQuestion(question.questionId),playing:question.questionId == $root.playingParagrapId(),playAudio:$root.playAudio.bind($root),clientRole:'student'"></word-recognition-and-reading>
                        </div>
                    </div>
                    <!--/ko-->
                </div>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
    </div>
</script>

<#include "../../../../teacherv3/templates/homeworkv3/chinese/recognitionreadingquestion.ftl">

<script type="text/html" id="ORAL_COMMUNICATION">
    <div class="hPreview-main">
        <div class="hp-title">
            <h3>口语交际</h3>
        </div>
        <div class="h-video-tab">
            <table class="video-info-box">
                <thead>
                <tr class="column4">
                    <td class="item">情景主题</td>
                    <td class="item">得分</td>
                    <td class="item">用时</td>
                    <td class="item">录音记录</td>
                </tr>
                </thead>
                <tbody data-bind="foreach:{data : $root.result.studentAchievement,as:'oralItem'}">
                    <tr class="column4">
                        <td>
                            <p class="v-name" data-bind="text:oralItem.topicName">&nbsp;</p>
                        </td>
                        <td data-bind="text:oralItem.score != null ? oralItem.score : '--'"></td>
                        <td data-bind="text:$root.convertSecondToMin(oralItem.duration)">0分0秒</td>
                        <td>
                            <a class="w-blue" rel="noopener noreferrer" target="_blank" data-bind="attr:{href:'/student/learning/history/newhomework/singleoralcommunicationpackagedetail.vpage?hid=' + $root.homeworkId + '&stoneId=' + oralItem.stoneId  + '&studentId=' + $root.studentId + '&objectiveConfigType=' + $root.tab}">查看详情</a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</script>

<script type="text/html" id="WORD_TEACH_AND_PRACTICE">
    <div class="cultureBox">
        <p class="cultureTitle">字词讲练</p>
        <!--ko foreach:{data:resultList,as:'sectionObj'}-->
        <div class="taskTitle"><div class="wordTitle" data-bind="text:sectionObj.sectionName">&nbsp;</div></div>
        <div class="taskDetail" data-bind="if:sectionObj.wordExerciseModuleData,visible:sectionObj.wordExerciseModuleData">
            <div class="taskDetailCard">
                <span class="taskDetailName">字词讲练</span>
                <div data-bind="css:{'taskDetailCon':sectionObj.wordExerciseModuleData.hasIntervention,'taskDetailCon2':!sectionObj.wordExerciseModuleData.hasIntervention}">
                    <div class="taskInfo">
                        <p class="taskFrac"><i data-bind="text:sectionObj.wordExerciseModuleData.firstScore">0</i>分</p>
                        <p class="taskExp" data-bind="if:sectionObj.wordExerciseModuleData.hasIntervention,visible:sectionObj.wordExerciseModuleData.hasIntervention">首次作答</p>
                    </div>
                    <div class="arrowPic" data-bind="if:sectionObj.wordExerciseModuleData.hasIntervention,visible:sectionObj.wordExerciseModuleData.hasIntervention">
                        <i class="arrow"></i>
                    </div>
                    <div class="taskInfo" data-bind="if:sectionObj.wordExerciseModuleData.hasIntervention,visible:sectionObj.wordExerciseModuleData.hasIntervention">
                        <p class="taskFrac"><i data-bind="text:sectionObj.wordExerciseModuleData.finalScore">0</i>分</p>
                        <p class="taskExp">干预讲解后</p>
                    </div>
                </div>
            </div>
        </div>
        <!--ko if:$root.sectionModuleLoaded.indexOf(sectionObj.sectionId + '_' + sectionObj.stoneId) != -1-->
        <!--ko foreach:{data:sectionObj.wordExerciseModuleData.wordExerciseQuestionData,as:'question'}-->
        <div class="h-set-homework examTopicBox">
            <div class="seth-hd">
                <p class="fl">
                    <span data-bind="text:question.contentType">&nbsp;</span>
                    <span class="border-none" data-bind="text:$root.getQuestionDifficultyName(question.difficulty)">&nbsp;</span>
                </p>
            </div>
            <div class="seth-mn iconWrapper">
                <div style="padding: 20px 0 40px;">
                    <ko-venus-question params="questions:$root.getQuestion(question.qid,sectionObj.stoneId),showUserAnswer:true,contentId: 'subject_'  + sectionObj.sectionId + '_' + sectionObj.stoneId + '_' + question.qid,formulaContainer:'tabContentHolder'"></ko-venus-question>
                </div>
                <div class="testPaper-info">
                    <div class="linkGroup">
                        <a class="view_exam_answer" style="display: none;" href="javascript:void(0);">查看答案与解析</a>
                    </div>
                </div>
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
        <div class="personDetailCard" data-bind="if:sectionObj.imageTextRhymeModuleData,visible:sectionObj.imageTextRhymeModuleData">
            <p class="detailTitle">图文入韵</p>
            <ul class="detailConBox" data-bind="foreach:{data:sectionObj.imageTextRhymeModuleData.imageTextRhymeDataList,as:'imageTextObj'}">
                <li class="taskCon">
                    <span class="taskdetailTitle" data-bind="text:imageTextObj.title">&nbsp;</span>
                    <span class="starIcon">
                        <!--ko foreach:ko.utils.range(1, imageTextObj.star)-->
                            <i class="starYellow"></i>
                        <!--/ko-->
                        <!--ko foreach:ko.utils.range(1, 3 - imageTextObj.star)-->
                            <i class="sterGray"></i>
                        <!--/ko-->
                    </span>
                    <span class="prev" data-bind="click:$root.previewChapter.bind($data,$root)"><u>预览</u></span>
                </li>
            </ul>
        </div>
        <div class="personDetailCard" data-bind="if:sectionObj.chineseCultureModuleData,visible:sectionObj.chineseCultureModuleData">
            <p class="detailTitle">汉字文化</p>
            <ul class="detailConBox" data-bind="foreach:{data:sectionObj.chineseCultureModuleData.chineseCharacterCultureData, as: 'chineseCalure'}">
                <li class="taskCon">
                    <span class="taskdetailTitle2" data-bind="text:chineseCalure.courseName">&nbsp;</span>
                    <span class="finish" data-bind="text:chineseCalure.finished ? '完成' : '未完成'">&nbsp;</span>
                </li>
            </ul>
        </div>
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
<script type="text/javascript">
    var answerDetailData = {
        env : <@ftlmacro.getCurrentProductDevelopment />,
        domain : '${requestContext.webAppBaseUrl}/',
        examUrl:'${examUrl!''}',
        defaultPicUrl:"<@app.link href="public/skin/studentv3/images/homework-history/upflie-img.png"/>",
        movie: "<@app.link href="resources/apps/flash/voicePlayer/VoiceReplayer.swf"/>",
        defaultReadingImg : "<@app.link href="public/skin/studentv3/images/homework-history/envelope-stu.png"/>",
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
    };
</script>
    <@sugar.capsule js=["ko","homework2nd","jplayer","chinese.recognitionreadingquestion","studentreport.detail"] />
</@temp.page>




