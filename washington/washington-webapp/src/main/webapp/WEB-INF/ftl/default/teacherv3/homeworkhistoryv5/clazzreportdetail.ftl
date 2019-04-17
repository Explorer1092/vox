<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="">
    <@sugar.capsule js=["jquery.flashswf","plugin.venus-pre"] css=["plugin.venus-pre","homeworkv3.homework","plugin.flexslider","homeworkv5.clazzwordteachandpracticedetail"]/>

<div class="h-homeworkCorrect">
    <h4 class="link">
        <a href="/">首页</a>&gt;<a href="/teacher/new/homework/report/list.vpage">检查作业</a>&gt;<a href="/teacher/new/homework/report/detail.vpage?homeworkId=${homeworkId}">作业报告</a>&gt;<span>作业详情</span>
    </h4>
    <div class="w-base" style="border-top: 0;">
        <div class="J_eventBind">
            <div class="J_mainContentHolder hc-main" id="tabContentHolder"></div>
        </div>
    </div>
    <!--作业类型按钮-->
    <div id="homeworkTypeExchange" class="bExercisesBtns" style="margin-bottom:15px; display: none;" data-bind="if:objectiveConfigTypeRanks().length > 1,visible:objectiveConfigTypeRanks().length > 1">
        <a href="javascript:void(0)" class="w-btn w-btn-small" data-bind="click:$root.prevOrNextClick.bind($data,$root.focusIndex() - 1),text:'上一个：' + $root.getTypeName($root.focusIndex() - 1) ">&nbsp;</a>
        <a href="javascript:void(0)" class="w-btn w-btn-small" data-bind="click:$root.prevOrNextClick.bind($data,$root.focusIndex() + 1), text:'下一个：' + $root.getTypeName($root.focusIndex() + 1) ">&nbsp;</a>
    </div>
</div>

<div id="showBigPic" style="display:none;">
    <div class="t-viewPicture-box-mask"></div>
    <div class="t-viewPicture-box">
        <div class="flex-viewport">
            <div class="head">
                <div class="title"></div>
                <div class="close">×</div>
            </div>
            <div style=" position: relative;">
                <ul class="J_bigPicItem list"></ul>
            </div>
            <ul class="flex-direction-nav">
                <li><a class="flex-prev flex-disabled" href="javascript:void(0);">Previous</a></li>
                <li><a class="flex-next flex-disabled" href="javascript:void(0);">Next</a></li>
            </ul>
            <div class="J_checkSubjectRegion column"></div>
        </div>
    </div>
</div>
<script type="text/html" id="BASIC_APP">
    <div class="bExercises-tab">
        <!--ko if:$root.resultList().length > ($root.displayCount - 1)-->
        <div class="arrow arrow-l" data-bind="visible:$root.leftEnabled(),click:arrowClick.bind($data,'arrowLeft')"><i class="icon"></i></div><!--arrow默认隐藏，当li的个数大于3时，arrow显示-->
        <div class="arrow arrow-r" data-bind="visible:$root.rightEnabled(),click:arrowClick.bind($data,'arrowRigth')"><i class="icon"></i></div>
        <!--/ko-->
        <div class="e-pictureBox" style="overflow-x: hidden">
            <ul class="clearfix" data-bind="style:{width:$root.resultList().length * 250 + 'px'}"><!--width为li的个数*250px-->
                <!--ko foreach:{data:$root.displayList,as:'basic'}-->
                <li class="bExercises-list" data-bind="css:{'active' : basic.unitId == $root.focusBasic().unitId && basic.lessonId == $root.focusBasic().lessonId && basic.categoryId == $root.focusBasic().categoryId},click:$root.basicClick.bind($data,$root,$index())">
                    <p class="unitName" data-bind="text:basic.lessonName">&nbsp;</p>
                    <p class="unitType" data-bind="text:basic.categoryName">单词跟读</p>
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
    <div class="bExercises-title">
        <!--ko foreach:{data:$root.focusTags, as : 'tag'}-->
        <span data-bind="click:$root.tagClick.bind($data,$root),css:{'first' : $root.focusTags().length > 1 && $index() == 0,'active' : tag.key != $root.focusTagKey()},text:tag.name">&nbsp;</span>
        <!--/ko-->
    </div>
    <#--跟读类--学生得分情况-->
    <div class="h-basicExercises" style="display: none;" data-bind="if:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'student',visible:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'student'">
        <div class="bExercises-box">
            <table cellpadding="0" cellspacing="0" class="table-cell04">
                <thead>
                <tr>
                    <td>
                        <span class="name">学生</span>
                        <span class="grade">得分</span>
                        <span class="audio">播放</span>
                        <span class="link">操作</span>
                    </td>
                    <td class="last-td">
                        <span class="name">学生</span>
                        <span class="grade">得分</span>
                        <span class="audio">播放</span>
                        <span class="link">操作</span>
                    </td>
                </tr>
                </thead>
                <tbody>
                <!--ko foreach:ko.utils.range(1,($root.personalStatistics().length/2 + 1))-->
                <tr data-bind="css:{'even' : $index() % 2 != 0}">
                    <!--ko foreach:{data:$root.personalStatistics().slice($index() * 2,($index() * 2 + 2)),as:'record'}-->
                    <td data-bind="css:{'last-td' : ($index() + 1) % 2 == 0}">
                        <span class="name" data-bind="text:record.userName">&nbsp;</span>
                        <span class="grade" data-bind="text:record.score" data-title="本字段统一从后端取，有可能是分数，有可能显示等级">&nbsp;</span>
                        <!--ko if: record.voiceScoringMode == "Normal"-->
                        <span class="audio" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                            <i class="h-playIcon voicePlayer" data-bind="css:{'h-playDisabled' : !$root.haveAudio(record.voiceUrls)}"></i>
                        </span>
                        <!--/ko-->
                        <!--ko ifnot:record.voiceScoringMode == "Normal"-->
                        <span class="audio">(听读模式)</span>
                        <!--/ko-->
                        <span class="link"><a target="_blank" rel="noopener noreferrer" href="javascript:void(0)" data-bind="attr:{href:'/teacher/new/homework/report/stubasicdetail.vpage?hid=' + $root.homeworkId + '&categoryId=' + $root.focusBasic().categoryId + '&lessonId=' + $root.focusBasic().lessonId + '&studentId=' + record.userId + '&objectiveConfigType=' + $root.tab}">查看详情</a></span>
                    </td>
                    <!--/ko-->
                </tr>
                <!--/ko-->
                </tbody>
            </table>
        </div>
    </div>
    <#--跟读类--内容得分情况-->
    <div style="display: none;" data-bind="if:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'content',visible:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'content'" >
        <!--ko foreach:{data:$root.contentStatistics(),as:'tagContent'}-->
        <div class="h-basicExercises bExercises-table">
            <div class="bExercises-box">
                <div class="switchHeader hover" data-bind="css:{'show' : tagContent.show()},click:$root.contentClick.bind($data,$root),singleBasicContentHover:true"><#--默认是收起，点击下拉箭头添加类show展开表格内容-->
                    <span class="unitName" data-bind="text:$root.getSentenceText(tagContent.sentences)">&nbsp;</span>
                    <div class="clazzGrade">（班级平均成绩<span class="red" data-bind="text:tagContent.appOralScoreLevel()">A</span>）
                        <em class="w-icon-arrow"></em>
                    </div><#--鼠标滑过时添加类w-icon-arrow-blue，箭头变蓝-->
                </div>
                <table cellpadding="0" cellspacing="0" class="table-cell03" style="display: none;" data-bind="style:{display : tagContent.show() ? 'table':'none' }">
                    <thead>
                    <tr>
                        <td>
                            <span class="name">学生</span>
                            <span class="grade">得分</span>
                            <span class="time">播放</span>
                        </td>
                        <td>
                            <span class="name">学生</span>
                            <span class="grade">得分</span>
                            <span class="time">播放</span>
                        </td>
                        <td class="last-td">
                            <span class="name">学生</span>
                            <span class="grade">得分</span>
                            <span class="time">播放</span>
                        </td>
                    </tr>
                    </thead>
                    <tbody>
                    <!--ko foreach:ko.utils.range(1,(tagContent.studentContentInfo().length/3 + 1))-->
                    <tr data-bind="css:{'even' : $index() % 2 != 0}">
                        <!--ko foreach:{data:tagContent.studentContentInfo().slice($index() * 3,($index() * 3 + 3)),as:'record'}-->
                        <td data-bind="css:{'last-td' : ($index() + 1) % 3 == 0}">
                            <span class="name" data-bind="text:record.userName">&nbsp;</span>
                            <span class="grade" data-bind="text:record.appOralScoreLevel" data-title="本字段统一从后端取，有可能是分数，有可能显示等级">&nbsp;</span>
                            <!--ko if: record.voiceScoringMode() == "Normal"-->
                                <span class="time" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                                    <i class="h-playIcon voicePlayer" data-bind="css:{'h-playDisabled' : !$root.haveAudio(record.voiceUrls()}"></i>
                                </span>
                            <!--/ko-->
                            <!--ko ifnot:record.voiceScoringMode() == "Normal"-->
                            <span class="time">(听读模式)</span>
                            <!--/ko-->
                        </td>
                        <!--/ko-->
                    </tr>
                    <!--/ko-->
                    </tbody>
                </table>
            </div>
        </div>
        <!--/ko-->
    </div>

    <#--基础练习作业详情-非跟读类-->
    <div style="display: none;" data-bind="if:!$root.focusBasic().isNeedRecord,visible:!$root.focusBasic().isNeedRecord">
        <!--ko foreach:{data:$root.contentStatistics(),as:'tagContent'}-->
        <div class="h-set-homework" >
            <div class="seth-mn iconWrapper">
                <div class="innerBox">
                    <p data-bind="text:$root.getSentenceText(tagContent.sentences)">&nbsp;</p>
                </div>
                <div class="icon-error-b icon-b" style="display: none;" data-bind="if:!$root.focusBasic().isAlien && tagContent.rate() > 0,visible:!$root.focusBasic().isAlien && tagContent.rate() > 0">
                    <div class="inner">
                        <div class="text">失分率</div>
                        <div class="item" data-bind="text:tagContent.rate() + '%'">10%</div>
                    </div>
                </div>
                <div class="icon-correct-b icon-b" style="display: none;" data-bind="if:!$root.focusBasic().isAlien && tagContent.rate() == 0,visible:!$root.focusBasic().isAlien && tagContent.rate() == 0">
                    <div class="inner">
                        <div class="text">全部正确</div>
                        <div class="item"><span class="icon-correct-s"></span></div>
                    </div>
                </div>
            </div>
            <div class="t-error-info w-table">
                <table style="display: none;" data-bind="if:!$root.focusBasic().isAlien,visible:!$root.focusBasic().isAlien">
                    <thead>
                        <tr><td style="width: 190px;">答案</td><td>对应同学</td></tr>
                    </thead>
                    <tbody>
                        <tr class="odd" data-bind="if:tagContent.errorStudentInformation && tagContent.errorStudentInformation().length > 0,visible:tagContent.errorStudentInformation && tagContent.errorStudentInformation().length > 0">
                            <td class="txt-red">答案错误</td>
                            <td data-bind="text:$root.joinPropertyValue(tagContent.errorStudentInformation(),'userName')">&nbsp;</td>
                        </tr>
                        <tr data-bind="if:tagContent.rightStudentInformation && tagContent.rightStudentInformation().length > 0,visible:tagContent.rightStudentInformation && tagContent.rightStudentInformation().length > 0">
                            <td class="txt-green">答案正确</td>
                            <td data-bind="text:$root.joinPropertyValue(tagContent.rightStudentInformation(),'userName')">&nbsp;</td>
                        </tr>
                    </tbody>
                </table>
                <table style="display: none;" data-bind="if:$root.focusBasic().isAlien,visible:$root.focusBasic().isAlien">
                    <thead>
                        <tr><td style="width: 190px;"></td><td>对应同学</td></tr>
                    </thead>
                    <tbody title="一定有学生完成作业整个练习形式，所以这一定有学生">
                        <tr data-bind="if:tagContent.rightStudentInformation && tagContent.rightStudentInformation().length > 0,visible:tagContent.rightStudentInformation && tagContent.rightStudentInformation().length > 0">
                            <td class="txt-green">已完成</td>
                            <td data-bind="text:$root.joinPropertyValue(tagContent.rightStudentInformation(),'userName')">&nbsp;</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>
<script type="text/html" id="LS_KNOWLEDGE_REVIEW">
    <div class="bExercises-tab">
        <!--ko if:$root.resultList().length > ($root.displayCount - 1)-->
        <div class="arrow arrow-l" data-bind="visible:$root.leftEnabled(),click:arrowClick.bind($data,'arrowLeft')"><i class="icon"></i></div><!--arrow默认隐藏，当li的个数大于3时，arrow显示-->
        <div class="arrow arrow-r" data-bind="visible:$root.rightEnabled(),click:arrowClick.bind($data,'arrowRigth')"><i class="icon"></i></div>
        <!--/ko-->
        <div class="e-pictureBox" style="overflow-x: hidden">
            <ul class="clearfix" data-bind="style:{width:$root.resultList().length * 250 + 'px'}"><!--width为li的个数*250px-->
                <!--ko foreach:{data:$root.displayList,as:'basic'}-->
                <li class="bExercises-list" data-bind="css:{'active' : basic.unitId == $root.focusBasic().unitId && basic.lessonId == $root.focusBasic().lessonId && basic.categoryId == $root.focusBasic().categoryId},click:$root.basicClick.bind($data,$root,$index())">
                    <p class="unitName" data-bind="text:basic.lessonName">&nbsp;</p>
                    <p class="unitType" data-bind="text:basic.categoryName">单词跟读</p>
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
    <div class="bExercises-title">
        <!--ko foreach:{data:$root.focusTags, as : 'tag'}-->
        <span data-bind="click:$root.tagClick.bind($data,$root),css:{'first' : $root.focusTags().length > 1 && $index() == 0,'active' : tag.key != $root.focusTagKey()},text:tag.name">&nbsp;</span>
        <!--/ko-->
    </div>
    <#--跟读类--学生得分情况-->
    <div class="h-basicExercises" style="display: none;" data-bind="if:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'student',visible:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'student'">
        <div class="bExercises-box">
            <table cellpadding="0" cellspacing="0" class="table-cell04">
                <thead>
                <tr>
                    <td>
                        <span class="name">学生</span>
                        <span class="grade">得分</span>
                        <span class="audio">播放</span>
                        <span class="link">操作</span>
                    </td>
                    <td class="last-td">
                        <span class="name">学生</span>
                        <span class="grade">得分</span>
                        <span class="audio">播放</span>
                        <span class="link">操作</span>
                    </td>
                </tr>
                </thead>
                <tbody>
                <!--ko foreach:ko.utils.range(1,($root.personalStatistics().length/2 + 1))-->
                <tr data-bind="css:{'even' : $index() % 2 != 0}">
                    <!--ko foreach:{data:$root.personalStatistics().slice($index() * 2,($index() * 2 + 2)),as:'record'}-->
                    <td data-bind="css:{'last-td' : ($index() + 1) % 2 == 0}">
                        <span class="name" data-bind="text:record.userName">&nbsp;</span>
                        <span class="grade" data-bind="text:record.score" data-title="本字段统一从后端取，有可能是分数，有可能显示等级">&nbsp;</span>
                        <!--ko if: record.voiceScoringMode == "Normal"-->
                        <span class="audio" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                            <i class="h-playIcon voicePlayer" data-bind="css:{'h-playDisabled' : !$root.haveAudio(record.voiceUrls)}"></i>
                        </span>
                        <!--/ko-->
                        <!--ko ifnot:record.voiceScoringMode == "Normal"-->
                        <span class="audio">(听读模式)</span>
                        <!--/ko-->
                        <span class="link"><a target="_blank" rel="noopener noreferrer" href="javascript:void(0)" data-bind="attr:{href:'/teacher/new/homework/report/stubasicdetail.vpage?hid=' + $root.homeworkId + '&categoryId=' + $root.focusBasic().categoryId + '&lessonId=' + $root.focusBasic().lessonId + '&studentId=' + record.userId + '&objectiveConfigType=LS_KNOWLEDGE_REVIEW'}">查看详情</a></span>
                    </td>
                    <!--/ko-->
                </tr>
                <!--/ko-->
                </tbody>
            </table>
        </div>
    </div>
    <#--跟读类--内容得分情况-->
    <div style="display: none;" data-bind="if:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'content',visible:$root.focusBasic().isNeedRecord && $root.focusTagKey() == 'content'" >
        <!--ko foreach:{data:$root.contentStatistics(),as:'tagContent'}-->
        <div class="h-basicExercises bExercises-table">
            <div class="bExercises-box">
                <div class="switchHeader hover" data-bind="css:{'show' : tagContent.show()},click:$root.contentClick.bind($data,$root),singleBasicContentHover:true"><#--默认是收起，点击下拉箭头添加类show展开表格内容-->
                    <span class="unitName" data-bind="text:$root.getSentenceText(tagContent.sentences)">&nbsp;</span>
                    <div class="clazzGrade">（班级平均成绩<span class="red" data-bind="text:tagContent.appOralScoreLevel()">A</span>）
                        <em class="w-icon-arrow"></em>
                    </div><#--鼠标滑过时添加类w-icon-arrow-blue，箭头变蓝-->
                </div>
                <table cellpadding="0" cellspacing="0" class="table-cell03" style="display: none;" data-bind="style:{display : tagContent.show() ? 'table':'none' }">
                    <thead>
                    <tr>
                        <td>
                            <span class="name">学生</span>
                            <span class="grade">得分</span>
                            <span class="time">播放</span>
                        </td>
                        <td>
                            <span class="name">学生</span>
                            <span class="grade">得分</span>
                            <span class="time">播放</span>
                        </td>
                        <td class="last-td">
                            <span class="name">学生</span>
                            <span class="grade">得分</span>
                            <span class="time">播放</span>
                        </td>
                    </tr>
                    </thead>
                    <tbody>
                    <!--ko foreach:ko.utils.range(1,(tagContent.studentContentInfo().length/3 + 1))-->
                    <tr data-bind="css:{'even' : $index() % 2 != 0}">
                        <!--ko foreach:{data:tagContent.studentContentInfo().slice($index() * 3,($index() * 3 + 3)),as:'record'}-->
                        <td data-bind="css:{'last-td' : ($index() + 1) % 3 == 0}">
                            <span class="name" data-bind="text:record.userName">&nbsp;</span>
                            <span class="grade" data-bind="text:record.appOralScoreLevel" data-title="本字段统一从后端取，有可能是分数，有可能显示等级">&nbsp;</span>
                            <!--ko if: record.voiceScoringMode() == "Normal"-->
                            <span class="time" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                                    <i class="h-playIcon voicePlayer" data-bind="css:{'h-playDisabled' : !$root.haveAudio(record.voiceUrls()}"></i>
                                </span>
                            <!--/ko-->
                            <!--ko ifnot:record.voiceScoringMode() == "Normal"-->
                            <span class="time">(听读模式)</span>
                            <!--/ko-->
                        </td>
                        <!--/ko-->
                    </tr>
                    <!--/ko-->
                    </tbody>
                </table>
            </div>
        </div>
        <!--/ko-->
    </div>

    <#--基础练习作业详情-非跟读类-->
    <div style="display: none;" data-bind="if:!$root.focusBasic().isNeedRecord,visible:!$root.focusBasic().isNeedRecord">
        <!--ko foreach:{data:$root.contentStatistics(),as:'tagContent'}-->
        <div class="h-set-homework" >
            <div class="seth-mn iconWrapper">
                <div class="innerBox">
                    <p data-bind="text:$root.getSentenceText(tagContent.sentences)">&nbsp;</p>
                </div>
                <div class="icon-error-b icon-b" style="display: none;" data-bind="if:tagContent.rate() > 0,visible:tagContent.rate() > 0">
                    <div class="inner">
                        <div class="text">失分率</div>
                        <div class="item" data-bind="text:tagContent.rate() + '%'">10%</div>
                    </div>
                </div>
                <div class="icon-correct-b icon-b" style="display: none;" data-bind="if:tagContent.rate() == 0,visible:tagContent.rate() == 0">
                    <div class="inner">
                        <div class="text">全部正确</div>
                        <div class="item"><span class="icon-correct-s"></span></div>
                    </div>
                </div>
            </div>
            <div class="t-error-info w-table">
                <table>
                    <thead>
                    <tr><td style="width: 190px;">答案</td><td>对应同学</td></tr>
                    </thead>
                    <tbody>
                    <tr class="odd" data-bind="if:tagContent.errorStudentInformation && tagContent.errorStudentInformation().length > 0,visible:tagContent.errorStudentInformation && tagContent.errorStudentInformation().length > 0">
                        <td class="txt-red">答案错误</td>
                        <td data-bind="text:$root.joinPropertyValue(tagContent.errorStudentInformation(),'userName')">&nbsp;</td>
                    </tr>
                    <tr data-bind="if:tagContent.rightStudentInformation && tagContent.rightStudentInformation().length > 0,visible:tagContent.rightStudentInformation && tagContent.rightStudentInformation().length > 0">
                        <td class="txt-green">答案正确</td>
                        <td data-bind="text:$root.joinPropertyValue(tagContent.rightStudentInformation(),'userName')">&nbsp;</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <!--/ko-->
    </div>
</script>
<script id="READING" type="text/html">
    <div class="e-pictureBook e-pictureTab">
        <div class="e-title"><span>绘本阅读成绩详情</span></div>
        <!--ko if:$root.pictureBookList().length > ($root.displayPackageCnt-1)-->
        <div class="arrow arrow-l" data-bind="visible:$root.leftEnabled(),click:arrowClick.bind($data,'arrowLeft')"><i class="icon"></i></div><!--arrow默认隐藏，当li的个数大于3时，arrow显示-->
        <div class="arrow arrow-r" data-bind="visible:$root.rightEnabled(),click:arrowClick.bind($data,'arrowRigth')"><i class="icon"></i></div>
        <!--/ko-->

        <div class="e-pictureBox" style="overflow: hidden">
            <ul class="clearfix" data-bind="style:{width:$root.pictureBookList().length * ($root.tab == 'LEVEL_READINGS' ? 280 : 262) + 'px'}"><!--width为li的个数*262px-->
                <!--ko foreach:{data:currentPackageList,as:'package'}-->
                <li class="e-pictureList active" data-bind="style:{width:($root.tab == 'LEVEL_READINGS' ? 248 : 230) + 'px'},css:{'active' : package.pictureBookId == $root.focusPackage().pictureBookId},click:$root.bookClick.bind($data,$root,$index())">
                    <div class="lPic">
                        <img data-bind="attr: { src : $root.imgsrc($element,package.pictureBookThumbImgUrl) }" />
                    </div>
                    <div class="rInfo" style="width: 147px;">
                        <div class="title" data-bind="style:{padding: ($root.tab == 'LEVEL_READINGS' ? '0':'5px 0 10px 0')}">
                            <span href="javascript:void(0)" data-bind="text:package.pictureBookName">&nbsp;</span>
                        </div>
                        <!--ko if:$root.tab == 'LEVEL_READINGS'-->
                        <p class="text" data-bind="text:package.pictureBookClazzLevelName">&nbsp;</p>
                        <!--/ko-->
                        <!--ko if:$root.tab == 'READING'-->
                        <p class="text" data-bind="text:package.pictureBookClazzLevels.join('、')">&nbsp;</p>
                        <!--/ko-->
                        <p class="text" data-bind="text:package.pictureBookTopics.join('、')">&nbsp;</p>
                        <p class="text" data-bind="text:package.pictureBookSeries">&nbsp;</p>
                        <p class="text" data-bind="if:package.practices && package.practices.length > 0,visible:package.practices && package.practices.length > 0">
                            <!--ko foreach:{data:package.practices,as:'practice'}-->
                            <span class="radioIcon selected"><i></i><!--ko text:practice.typeName--><!--/ko--></span>
                            <!--/ko-->
                        </p>
                    </div>
                    <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                </li>
                <!--/ko-->
            </ul>
        </div>
    </div>
    <!--ko if:$root.pictureBookList && $root.pictureBookList().length > 0-->
    <div class="summary-list" style="border-top: 1px solid #dae6ee; margin-top: -1px;">
        <div class="numCollect" style="border-bottom: 0;">
            <ul style="padding: 10px 20px">
                <li><!--ko text:$root.focusPackage().finishedCount--><!--/ko-->/<!--ko text:$root.focusPackage().totalUserNum--><!--/ko--><span class="text">已完成</span></li>
                <li><!--ko text:$root.focusPackage().avgScore--><!--/ko--><span class="text">/班平均分</span></li>
                <li><!--ko text:$root.convertSecondToMin($root.focusPackage().avgDuration)--><!--/ko--><span class="text">/平均时长</span></li>
            </ul>
            <a href="javascript:void(0);" class="yl-link" data-bind="click:$root.viewReading.bind($data,$root.tab)">预览绘本</a>
        </div>
    </div>
    <div class="h-basicExercises" data-bind="if:$root.tab != 'LEVEL_READINGS',visible:$root.tab != 'LEVEL_READINGS'">
        <div class="bExercises-box">
            <table cellpadding="0" cellspacing="0" class="table-cell03">
                <thead>
                <tr>
                    <td><span class="name">姓名</span><span class="grade">分数</span><span class="time">用时</span></td>
                    <td><span class="name">姓名</span><span class="grade">分数</span><span class="time">用时</span></td>
                    <td class="last-td"><span class="name">姓名</span><span class="grade">分数</span><span class="time">用时</span></td>
                </tr>
                </thead>
                <tbody>
                <!--ko foreach:ko.utils.range(1,($root.focusStudents().length/3 + 1))-->
                <tr data-bind="css:{'even' : $index() % 2 != 0}">
                    <!--ko foreach:{data:$root.focusStudents().slice($index()*3, ($index()*3 + 3)),as:'record'}-->
                    <td data-bind="css:{'last-td' : ($index() + 1) % 3 == 0}">
                        <span class="name" data-bind="text:record.userName">&nbsp;</span>
                        <span class="grade" data-bind="text:record.score + '分'">&nbsp;</span>
                        <span class="time" data-bind="text:$root.convertSecondToMin(record.duration)">&nbsp;</span>
                    </td>
                    <!--/ko-->
                </tr>
                <!--/ko-->
                </tbody>
            </table>
        </div>
    </div>
    <div class="h-basicExercises" data-bind="if:$root.tab == 'LEVEL_READINGS',visible:$root.tab == 'LEVEL_READINGS'">
        <div class="bExercises-box">
            <table cellpadding="0" cellspacing="0" class="table-cell03 new-table">
                <thead>
                <tr>
                    <td><span class="name">学生姓名</span></td>
                    <td><span class="name">阅读得分</span></td>
                    <td><span class="name">配音</span></td>
                    <td class="last-td"><span class="name">作品</span></td>
                </tr>
                </thead>
                <tbody data-bind="foreach:{data:$root.focusStudents(),as:'student'}">
                    <tr class="even" data-bind="css:{'even':$index()%2 != 0}">
                        <td><span class="name" data-bind="text:student.userName">&nbsp;</span></td>
                        <td><span class="name" data-bind="text:student.score"></span></td>
                        <td><span class="name" data-bind="text:student.dubbingId ? student.dubbingScoreLevel : '--'">&nbsp;</span></td>
                        <td class="last-td" data-bind="click:$root.playDubbingVideo.bind($data,$root)">
                            <span class="time" data-bind="visible:student.dubbingId"><i class="h-playIcon h-playVideo"></i></span>
                            <span class="time" data-bind="visible:!student.dubbingId">--</span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <!--/ko-->
</script>

<script type="text/html" id="ORAL_PRACTICE">
    <div class="w-base-title" style="background-color: #e1f0fc;">
        <h3>口语习题</h3>
        <div class="w-base-right">
            <span class="hoverText">分数计算说明</span>
            <div class="hoverInfo">
                <span class="jf-arrow"></span>
                <p>1、本作业总成绩为所有题目平均分</p>
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
            <div class="seth-mn" style="overflow-x: auto;overflow-y: hidden;padding: 20px 50px 5px;" data-bind="if:$root.questionLoading && !$root.questionLoading(),visible:$root.questionLoading && !$root.questionLoading()">
                <ko-venus-question params="questions:$root.getQuestion(sa.qid),contentId: 'subject_' + sa.qid,formulaContainer:'tabContentHolder'"></ko-venus-question>
            </div>
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
                <!--ko foreach:item.users-->
                <div class="sw-list">
                    <div class="name" data-bind="text:userName"></div>
                    <div class="voicePlayer speak" data-bind="click:$root.playAudio.bind($data,$element,$root),attr:{index:$index(),qid:sa.qid,uid:userId},css: {listen: userVoiceUrls && userVoiceUrls.length == 0}">
                        <!--ko if:userVoiceUrls && userVoiceUrls.length == 0-->听读模式<!--/ko-->
                        <span class="icon" data-bind="text:score"></span>
                    </div>
                </div>
                <!--/ko-->
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
            <!--ko foreach:{data:item.data,as:'lesson'}-->
            <div class="r-inner">
                <div class="readTxt-list2">
                    <div class="frAside" data-bind="css:{'showDetails':$root.showParagraphId() == (lesson.questionBoxId + '_' + $index())},click:$root.showDetailBtn">
                        <span data-bind="text: $root.showParagraphId() == (lesson.questionBoxId + '_' + $index()) ? '收起段落':'展开段落'"></span>
                        <span class="arrow"></span>
                    </div>
                    <div class="fl-aside">
                        <p class="name" data-bind="text:$data.lessonName"></p>
                        <p class="textGray" data-bind="text:item.name+'：第'+ $data.paragraphDescription +'段'"></p>
                    </div>
                </div>
                <div class="readTxt-details" style="display:none;" data-bind="visible: $root.showParagraphId() == (lesson.questionBoxId + '_' + $index())">
                    <!--ko foreach: $data.paragraphDetaileds-->
                    <div class="readInner">
                        <div class="r-sub-title">
                            <span data-bind="text:'第'+$data.paragraphOrder+'段'"></span>
                            <!--ko if:$data.paragraphDifficultyType -->
                            <span class="label">（重点段落）</span>
                            <!--/ko-->
                        </div>
                        <div class="r-inner">
                            <div style="overflow-x: auto;overflow-y: hidden;">
                                <ko-venus-question params="questions:$root.getQuestion($data.questionId),contentId: 'subject_' + $data.questionId,formulaContainer:'tabContentHolder'"></ko-venus-question>
                            </div>
                        </div>
                    </div>
                    <!--/ko-->
                </div>
                <ul class="student-list clearfix">
                    <!--ko foreach: $data.users-->
                    <li class="list-item">
                        <p class="name" data-bind="text:$data.userName"></p>
                        <div class="tag-box">
                            <span class="play voicePlayer" data-bind="click:$root.playAudio.bind($data,$element,$root)">播放</span>
                            <span class="levelIcon" data-bind="singleHover:$data.checkHover,css: $data.levelIcon">
                                <span data-bind="text:$data.correct_des"></span>
                                <a class="level-option level-top" href="javascript:void(0);" style="display:none;">
                                    <i class="posit-opt"></i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'阅','REVIEW',$parent.questionBoxId)">阅</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'优','EXCELLENT',$parent.questionBoxId)">优</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'良','GOOD',$parent.questionBoxId)">良</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'中','FAIR',$parent.questionBoxId)">中</i>
                                    <i data-bind="click:$root.checkQuestion.bind($data,'差','PASS',$parent.questionBoxId)" class="bord-none">差</i>
                                </a>
                            </span>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
</script>

<script id="DUBBING" type="text/html">
    <div class="box-detail">
        <h5 class="de-title">趣味配音成绩详情</h5>
        <div class="pad-btm">
            <div class="e-pictureBook" style="height:190px;overflow: hidden;">
                <!--ko if:$root.dubbingList().length > ($root.displayPackageCnt-1)-->
                <div class="arrow-2 arrow-l" style="display: block" data-bind="visible:$root.leftEnabled(),click:arrowClick.bind($data,'arrowLeft')"><i class="icon"></i></div><!--arrow默认隐藏，当li的个数大于3时，arrow显示-->
                <div class="arrow-2 arrow-r" style="display: block" data-bind="visible:$root.rightEnabled(),click:arrowClick.bind($data,'arrowRigth')"><i class="icon"></i></div>
                <!--/ko-->
                <ul class="clearfix" data-bind="style:{width:$root.dubbingList().length * 192 + 'px'}"><!--width为li的个数*192px-->
                    <!--ko foreach:{data:currentPackageList,as:'package'}-->
                    <li class="e-pictureList-2" data-bind="css:{'active' : package.dubbingId == $root.focusPackage().dubbingId},click:$root.dubbingClick.bind($data,$root,$index())">
                        <div class="picbox" data-bind="click:$root.viewDubbing.bind($data,package.coverUrl,package.videoUrl,'dubbing_video'),clickBubble:false">
                            <div class="pic-box">
                                <img style="width: 100%;" data-bind="attr: { src : $root.imgsrc($element,package.coverUrl) }">
                                <a class="play-btn" href="javascript:void(0)"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title" data-bind="text:package.name">&nbsp;</p>
                            <p class="text" data-bind="text:package.albumName">&nbsp;</p>
                            <p class="text" data-bind="text:package.clazzLevel  + ' 共' + package.sentenceSize + '句'">&nbsp;</p>
                            <p class="text" data-bind="text:package.topics.join(' / ')">&nbsp;</p>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
        </div>
        <!--ko if:$root.dubbingList && $root.dubbingList().length > 0-->
        <div class="text-info">
            <div class="s-text"><span><!--ko text:$root.focusPackage().finishedNum--><!--/ko-->/<!--ko text:$root.focusPackage().totalUserNum--><!--/ko--></span>已完成</div>
            <div class="s-text"><span><!--ko text:$root.convertSecondToMin($root.focusPackage().avgDuration)--><!--/ko--></span>/平均时长</div>
        </div>
        <div class="stud-table">
            <ul class="m-tab">
                <li class="t-head" data-bind="css:{'column4':$root.tab == 'DUBBING_WITH_SCORE'}">
                    <span>姓名</span>
                    <span data-bind="if:$root.tab == 'DUBBING_WITH_SCORE',visible:$root.tab == 'DUBBING_WITH_SCORE'">得分</span>
                    <span>用时</span>
                    <span class="r-line">作品</span>
                    <span>姓名</span>
                    <span data-bind="if:$root.tab == 'DUBBING_WITH_SCORE',visible:$root.tab == 'DUBBING_WITH_SCORE'">得分</span>
                    <span>用时</span>
                    <span>作品</span>
                </li>
                <!--ko foreach:ko.utils.range(1,($root.focusStudents().length/2 + 1))-->
                <li data-bind="css:{'column4':$root.tab == 'DUBBING_WITH_SCORE'}">
                    <!--ko foreach:{data:$root.focusStudents().slice($index()*2, ($index()*2 + 2)),as:'record'}-->
                    <span data-bind="text:record.userName">&nbsp;</span>
                    <span data-bind="if:$root.tab == 'DUBBING_WITH_SCORE',visible:$root.tab == 'DUBBING_WITH_SCORE'"><!--ko text:record.score ? record.score : '--' --><!--/ko--></span>
                    <span data-bind="text:$root.convertSecondToMin(record.duration)">&nbsp;</span>
                    <span class="r-line">
                        <!--ko if:record.syntheticSuccess-->
                        <a data-bind="click:$root.viewDubbing.bind($data,record.coverUrl,record.videoUrl,'student_dubbing_video'),clickBubble:false" class="icon-video" href="javadcript:;"></a>
                        <!--/ko-->
                        <!--ko ifnot: record.syntheticSuccess-->
                        <a href="javascript:void(0)" title="配音正在合成，请稍后再试或联系客服解决~">配音合成中</a>
                        <!--/ko-->
                    </span>
                    <!--/ko-->
                </li>
                <!--/ko-->
            </ul>
        </div>
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="t:MENTAL_ARITHMETIC">
    <div class="hc-main">
        <div class="w-base-title" style="background-color: #e1f0fc;">
            <h3>口算</h3>
        </div>
        <div class="detailsOral">
            <ul class="tabType">
                <li class="active mentalArithmeticTab" data-ref="viewMentalStudent">按照学生查看</li>
                <li class="mentalArithmeticTab" data-ref="viewMentalQuestion">按照题目查看</li>
            </ul>
            <div class="detailMain">
                <!--按照学生查看-->
                <div class="detailMain01" id="viewMentalStudent">
                    <table cellspacing="0" cellpadding="0">
                        <thead>
                        <tr>
                            <td width="235">排名</td>
                            <td width="235">学生姓名</td>
                            <td width="235">得分</td>
                            <td width="235">时间</td>
                        </tr>
                        </thead>
                        <tbody>
                        <% for(var m = 0,mLen = studentList.length; m < mLen; m++){%>
                        <tr>
                            <td><%=(m + 1)%></td>
                            <td><%= studentList[m].userName%></td>
                            <%if(studentList[m].finished){%>
                            <td><%= studentList[m].score%>分</td>
                            <%}else if(studentList[m].repair){%>
                            <td>补做</td>
                            <%}else{%>
                            <td>未完成</td>
                            <%}%>
                            <td><%= studentList[m].durationStr%></td>
                        </tr>
                        <%}%>
                        </tbody>
                    </table>
                </div>
                <!--按照题目查看-->
                <div class="detailMain02" id="viewMentalQuestion">

                </div>
            </div>
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
                    <div class="fl-aside">
                        <p class="name" data-bind="text:lesson.lessonName">&nbsp;</p>
                        <p class="textGray"><!--ko text:typeObj.name--><!--/ko-->段落：第<!--ko text:lesson.paragraphs.length > 5 ? lesson.paragraphs.slice(0,5).join('、') + '...' : lesson.paragraphs.join('、')--><!--/ko-->段</p>
                    </div>
                </div>
                <ul class="achieve-list clearfix" data-bind="foreach:{data:lesson.users,as:'user'}">
                    <li class="list-item" style="cursor: pointer;" data-bind="click:$root.forwardStudentDetail.bind($data,$root)">
                        <p class="name" data-bind="text:user.userName">&nbsp;</p>
                        <p class="isreach" data-bind="style:{'color': user.standard ? '#4e5656' : '#ff7563'},text:user.standard ? '达标' : '未达标'">&nbsp;</p>
                        <div class="audio-bar" data-bind="if:user.voices && user.voices.length > 0,visible:user.voices && user.voices.length > 0">
                            <i class="audio-btn" data-bind="css:{'audio-play':(lesson.questionBoxId + '_' + user.userId) != $root.playingUserId(),'audio-pause':(lesson.questionBoxId + '_' + user.userId) == $root.playingUserId()},click:$root.playAudio.bind($data,$root,lesson), clickBubble: false"></i>
                            <i class="time" data-bind="text:user.durationStr">&nbsp;</i>
                        </div>
                    </li>
                </ul>
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
            <div class="w-base-right" style="padding: 0;">
                <span class="hoverText" style="padding:0;">达标规则</span>
                <div class="hoverInfo" style="width: 230px;height: 24px;right: 0px;" >
                    <span class="jf-arrow"></span>
                    <p>达标的字数超过80%，则这篇课文生字达标</p>
                </div>
            </div>
        </div>
        <div class="aDetails-section">
            <div class="r-title">生字认读</div>
            <!--ko foreach:{data:resultList,as:'lesson'}-->
            <div class="r-inner">
                <div class="readTxt-list2 ">
                    <div class="fl-aside">
                        <p class="name" data-bind="text:lesson.lessonName">&nbsp;</p>
                        <p class="textGray" data-bind="text:'共' + lesson.detailList.length + '个生字'"></p>
                    </div>
                </div>
                <ul class="achieve-list clearfix" data-bind="foreach:{data:lesson.users,as:'user'}">
                    <li class="list-item" style="cursor: pointer;" data-bind="click:$root.forwardStudentDetail.bind($data,$root)">
                        <p class="name" data-bind="text:user.userName">&nbsp;</p>
                        <p class="isreach" data-bind="style:{'color': user.standard ? '#4e5656' : '#ff7563'},text:user.standard ? '达标' : '未达标'">&nbsp;</p>
                        <div class="audio-bar" data-bind="if:user.voices && user.voices.length > 0,visible:user.voices && user.voices.length > 0">
                            <i class="audio-btn" data-bind="css:{'audio-play':(lesson.questionBoxId + '_' + user.userId) != $root.playingUserId(),'audio-pause':(lesson.questionBoxId + '_' + user.userId) == $root.playingUserId()},click:$root.playAudio.bind($data,$root,lesson), clickBubble: false"></i>
                            <i class="time" data-bind="text:user.duration">&nbsp;</i>
                        </div>
                    </li>
                </ul>
            </div>
            <!--/ko-->
        </div>
    </div>
</script>

<script id="ORAL_COMMUNICATION" type="text/html">
    <div class="box-detail">
        <h5 class="de-title">口语交际成绩详情</h5>
        <div class="pad-btm">
            <div class="e-pictureBook" style="height:190px;overflow: hidden;">
                <!--ko if:$root.dubbingList().length > ($root.displayPackageCnt-1)-->
                <div class="arrow-2 arrow-l" style="display: block" data-bind="visible:$root.leftEnabled(),click:arrowClick.bind($data,'arrowLeft')"><i class="icon"></i></div><!--arrow默认隐藏，当li的个数大于3时，arrow显示-->
                <div class="arrow-2 arrow-r" style="display: block" data-bind="visible:$root.rightEnabled(),click:arrowClick.bind($data,'arrowRigth')"><i class="icon"></i></div>
                <!--/ko-->
                <ul class="clearfix" data-bind="style:{width:$root.dubbingList().length * 192 + 'px'}"><!--width为li的个数*192px-->
                    <!--ko foreach:{data:currentPackageList,as:'package'}-->
                    <li class="e-pictureList-2" data-bind="css:{'active' : package.stoneId == $root.focusPackage().stoneId},click:$root.oralItemClick.bind($data,$root,$index())">
                        <div class="picbox">
                            <div class="pic-box">
                                <img style="width: 100%;" data-bind="attr: { src : $root.imgsrc($element,package.thumbUrl) }">
                                <a class="play-btn" href="javascript:void(0)" style="display: none;"><span class=""></span></a>
                            </div>
                        </div>
                        <div class="video-info">
                            <p class="title" data-bind="attr:{title:package.topicName},text:package.topicName">&nbsp;</p>
                            <p class="text" style="width: 85%;" data-bind="text: package.sentences ? package.sentences.join(' / ') : ''">&nbsp;</p>
                        </div>
                    </li>
                    <!--/ko-->
                </ul>
            </div>
        </div>
        <!--ko if:$root.dubbingList && $root.dubbingList().length > 0-->
        <div class="text-info">
            <div class="s-text"><span><!--ko text:$root.focusPackage().finishedNum--><!--/ko-->/<!--ko text:$root.focusPackage().totalUserNum--><!--/ko--></span>已完成</div>
            <div class="s-text"><span><!--ko text:$root.convertSecondToMin($root.focusPackage().avgDuration)--><!--/ko--></span>/平均时长</div>
        </div>
        <div class="stud-table">
            <ul class="m-tab">
                <li class="t-head column4">
                    <span>姓名</span>
                    <span>得分</span>
                    <span>用时</span>
                    <span class="r-line">录音记录</span>
                    <span>姓名</span>
                    <span>得分</span>
                    <span>用时</span>
                    <span>录音记录</span>
                </li>
                <!--ko foreach:ko.utils.range(1,($root.focusStudents().length/2 + 1))-->
                <li class="column4">
                    <!--ko foreach:{data:$root.focusStudents().slice($index()*2, ($index()*2 + 2)),as:'record'}-->
                    <span data-bind="text:record.userName">&nbsp;</span>
                    <span><!--ko text:record.score ? record.score : '--' --><!--/ko--></span>
                    <span data-bind="text:$root.convertSecondToMin(record.duration)">&nbsp;</span>
                    <span class="r-line">
                        <a class="w-blue" rel="noopener noreferrer" target="_blank" data-bind="attr:{href:'/teacher/new/homework/report/singleoralcommunicationpackagedetail.vpage?hid=' + $root.homeworkId + '&stoneId=' + $root.focusPackage().stoneId  + '&studentId=' + $data.userId + '&objectiveConfigType=' + $root.tab}">查看详情</a>
                    </span>
                    <!--/ko-->
                </li>
                <!--/ko-->
            </ul>
        </div>
        <!--/ko-->
    </div>
</script>

<script type="text/html" id="WORD_TEACH_AND_PRACTICE">
    <div class="cultureBox">
        <p class="cultureTitle">字词讲练</p>
        <!--ko if:$data.resultList && $data.resultList.length > 0-->
        <!--ko foreach:{data:resultList,as:'result'}-->
        <div class="topicCard">
            <div class="culTitleCard">
                <div class="culTitle" data-bind="text:result.sectionName">&nbsp;</div>
                <div class="culRegCard" style="display: none;">
                    <span><i>2/137</i>已完成</span>
                    <span><i>57</i>/班平均分</span>
                </div>
            </div>
            <div class="tabBox-word" data-bind="if:result.wordExerciseModuleClazzData,visible:result.wordExerciseModuleClazzData">
                <ul class="tHead">
                    <li>
                        <span class="tName">字词训练</span>
                        <span></span>
                        <span class="tNumber" data-bind="click:$root.viewModuleDetail.bind($root,result.stoneId,'WORDEXERCISE')">查看答题详情</span>
                    </li>
                    <li>
                        <span class="tName">题号</span>
                        <span>正确率</span>
                        <span>操作</span>
                    </li>
                </ul>
                <ul class="tBody" data-bind="foreach:{data:result.wordExerciseModuleClazzData.questionReportDetails,as:'question'}">
                    <li>
                        <span data-bind="text:'第' + ($index() + 1) + '题'">&nbsp;</span>
                        <span class="accuracy">
                            <i class="first">首次<!--ko text:question.firstProportion--><!--/ko-->%</i>
                            <div class="progressBar">
                                <div class="progressWri" data-bind="css:{'progressError' : question.proportion < 60},style:{width: (question.proportion + '%')}"></div>
                                <i data-bind="css:{'allCorrect' : question.proportion > 80}"><!--ko if:question.hasIntervention-->干预讲解后<!--/ko--><!--ko text:question.proportion--><!--/ko-->%</i>
                            </div>
                        </span>
                        <span class="tNumber" data-bind="click:$root.viewSingleExam.bind($data,question.questionId)">详情</span>
                    </li>
                </ul>
            </div>
            <div class="tabBox-word-2" data-bind="if:result.imageTextRhymeModuleClazzData,visible:result.imageTextRhymeModuleClazzData">
                <ul class="tHead">
                    <li>
                        <span class="tName">图文入韵</span>
                        <span class="tNumber" data-bind="click:$root.viewModuleDetail.bind($root,result.stoneId,result.imageTextRhymeModuleClazzData.wordTeachModuleType)">查看答题详情</span>
                    </li>
                </ul>
                <ul class="tBody" data-bind="foreach:{data:result.imageTextRhymeModuleClazzData.imageTextRhymeChapterDatas,as:'imageObj'}">
                    <li>
                        <span data-bind="text:imageObj.title">&nbsp;</span>
                        <span><!--ko text:imageObj.finishNum--><!--/ko-->人已完成</span>
                    </li>
                </ul>
            </div>
            <div class="tabBox-word-2" data-bind="if:result.chineseCharacterCultureModuleClazzData,visible:result.chineseCharacterCultureModuleClazzData">
                <ul class="tHead">
                    <li>
                        <span class="tName">汉字文化</span>
                        <span class="tNumber" data-bind="click:$root.viewModuleDetail.bind($root,result.stoneId,result.chineseCharacterCultureModuleClazzData.wordTeachModuleType)">查看答题详情</span>
                    </li>
                </ul>
                <ul class="tBody" data-bind="foreach:{data:result.chineseCharacterCultureModuleClazzData.courseDatas,as:'chineseModuleObj'}">
                    <li>
                        <span data-bind="text:chineseModuleObj.title">&nbsp;</span>
                        <span><!--ko text:chineseModuleObj.finishNum--><!--/ko-->人已完成</span>
                    </li>
                </ul>
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
    </div>
</script>

<script id="t:PREVIEW_QUESTION" type="text/html">
    <div class="topicDetails-dialog" style="margin-top:-20px;">
        <div class="tips tips-grey">
            <p>
                <!--ko text:$root.submitNum-->0<!--/ko-->人提交，
                <!--ko text:$root.errorNum-->0<!--/ko-->人错误
            </p>
        </div>
        <div class="h-set-homework">
            <div class="seth-hd" style="display: none;">
                <p class="fl"><span>&nbsp;</span><span class="noBorder">&nbsp;</span></p>
            </div>
            <div class="seth-mn">
                <div class="testPaper-info" id="tabContent_201811081212">
                    <div class="inner">
                        <ko-venus-question params="questions:$root.getQuestion($root.questionId),contentId:'mathExamImgSourceElem',formulaContainer:'tabContent_201811081212'"></ko-venus-question>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>


<#--趣味配音预览视频模板-->
<script type="text/html" id="t:PLAY_VIDEO_OF_SINGLE_DUBBING_POPUP">
    <div class="tDubbing-popup">
        <div class="dubPopup-video">
            <div class="video" id="dubbingPlayVideoContainer"></div>
            <span class="playBtn" style="display: none;"></span>
        </div>
    </div>
</script>

<#--阅读预览模板-->
<script id="t:预览" type="text/html">
    <div id="showViewContent">
        <div id="install_flash_player_box" style="margin:20px; display: none;">
            <div id="install_download_tip" style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                <a href="<@app.client_setup_url />" target="_blank">您的系统组件需要升级。请点这里<span style="color:red;">下载</span>并<span style="color:red;">运行</span> “一起作业安装程序”。</a>
            </div>
        </div>
    </div>
</script>
<div id="jquery_jplayer_1" class="jp-jplayer"></div>

<script type="text/javascript">
    var answerDetailData = {
        env : <@ftlmacro.getCurrentProductDevelopment />,
        imgDomain : '${imgDomain!''}',
        domain : '${requestContext.webAppBaseUrl}/',
        examUrl:'${examPcUrl!''}',
        defaultPicUrl:"<@app.link href="public/skin/teacherv3/images/homework/upflie-img.png"/>",
        movie: "<@app.link href="resources/apps/flash/voicePlayer/VoiceReplayer.swf"/>",
        ttsurl : "${tts_url!}",
        readingFlashUrl : "${readingFlashUrl!}",
        defaultReadingImg : "<@app.link href="public/skin/teacherv3/images/homework/envelope-tea.png"/>",
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>",
        defaultDubbingImg : '<@app.link href="public/skin/teacherv3/images/dubbing/img-01.png"/>'
    };

    function nextHomeWork(){
        $.prompt.close();
    }
</script>
<@sugar.capsule js=["ko",'homework2nd',"jplayer","homeworkv5.clazzreportdetail"] />
</@shell.page>