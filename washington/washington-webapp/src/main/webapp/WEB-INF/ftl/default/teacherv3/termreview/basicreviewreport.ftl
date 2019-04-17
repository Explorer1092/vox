<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="basicreviewreport" showNav="hide">
    <@sugar.capsule js=["ko"] css=["new_teacher.goal","termreview.finalreview"] />

<div id="mainContent" class="w-base" style="margin-top:20px;">
    <!--ko if:$root.clazzList && $root.clazzList().length > 0 -->
    <div class="tE-tabGrade slider" id="tE-tabGrade" name="slider">
        <div class="arrow arrow-disabled arrow-l" id="sliderL"><i class="icon"></i></div>
        <div class="arrow arrow-r" id="sliderR"><i class="icon"></i></div>
        <ul data-bind="style:{width:$root.clazzList().length*157+'px',position:'absolute',left:'0px',transition: 'left 1s'}">
            <!--ko foreach:{data:$root.clazzList,as:'item'}-->
            <li class="slideItem" data-bind="css:{active:$root.focusClazz() && item.clazzId == $root.focusClazz().clazzId},click:$root.changeClazz.bind($data,$index(),$root)">
                <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                <span data-bind="text:item.clazzName"></span>
            </li>
            <!--/ko-->
        </ul>
    </div>
    <div class="tE-tabGrade-line"></div>
    <div class="tE-unitInfo">
        <div class="title">
            <span id="termMonth" data-bind="text:$root.focusStage() ? $root.focusStage().stageName : ''"></span>
            <div class="h-slide on">
                <span class="slideText">复习任务<em class="w-icon-arrow w-icon-arrow-blue"></em></span><!--向上w-icon-arrow-topBlue-->
                <div class="h-slide-box" id="h-slide-box" style="max-height: 280px;">
                    <!--ko foreach:{data : $root.stageList(),as : 'dateObj'}-->
                    <label style="cursor: pointer;" class="defaultClass" data-bind="css:{'w-radio-current':$root.focusStage() && dateObj.homeworkId == $root.focusStage().homeworkId},click:$root.stageClick.bind($data,$index(),$root)">
                        <span class="w-radio"></span> <span class="w-icon-md name" data-bind="text:dateObj.stageName">&nbsp;</span>
                    </label>
                    <!--/ko-->
                </div>
            </div>
        </div>
    </div>

    <div class="swch-tab">
        <!--ko if:$root.subject == 'ENGLISH'-->
        <a data-bind="css:{'active' : $root.typeContent().switchType != 'content'},click:$root.switchTypeClick.bind($data,'content')" href="javascript:;">内容掌握情况<i></i></a>
        <!--/ko-->
        <a class="rt" data-bind="css:{'active' : $root.typeContent().switchType != 'student'},click:$root.switchTypeClick.bind($data,'student')" href="javascript:;">学生掌握情况</a>
    </div>
    <div>
        <div data-bind="if:$root.subject == 'ENGLISH' && $root.typeContent().switchType == 'content',visible:$root.subject == 'ENGLISH' && $root.typeContent().switchType == 'content'">
            <div class="clazz-info" data-bind="if:($root.typeContent().words && $root.typeContent().words.length > 0) || ($root.typeContent().sentences && $root.typeContent().sentences.length > 0),visible:($root.typeContent().words && $root.typeContent().words.length > 0) || ($root.typeContent().sentences && $root.typeContent().sentences.length > 0)">
                <p data-bind="if:$root.typeContent().words && $root.typeContent().words.length > 0,visible:$root.typeContent().words && $root.typeContent().words.length > 0">
                    高频错误单词：<!--ko text:$root.typeContent().words.join(' / ')--><!--/ko-->
                </p>
                <p data-bind="if:$root.typeContent().sentences && $root.typeContent().sentences.length > 0,visible: $root.typeContent().sentences && $root.typeContent().sentences.length > 0">
                    高频错误句子：<!--ko text:$root.typeContent().sentences.join(' / ')--><!--/ko-->
                </p>
            </div>
            <!--ko if: $root.typeContent().wordAnalysisList && $root.typeContent().wordAnalysisList.length > 0-->
            <div class="text-tit">单词（<!--ko text: $root.typeContent().wordFinishUserNum--><!--/ko-->名学生完成）</div>
            <div class="tab-box">
                <table>
                    <thead>
                    <tr>
                        <td>班级仍不熟悉的单词</td>
                        <td>写错学生数</td>
                        <td>认错学生数</td>
                    </tr>
                    </thead>
                    <tbody data-bind="foreach:{data:$root.typeContent().wordAnalysisList,as:'wordObj'}">
                    <tr>
                        <td data-bind="text:wordObj.word">&nbsp;</td>
                        <td data-bind="text:wordObj.mishearNum">&nbsp;</td>
                        <td data-bind="text:wordObj.misLookNum">&nbsp;</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <!--/ko-->
            <!--ko if:$root.typeContent().sentenceAnalysisList && $root.typeContent().sentenceAnalysisList.length > 0-->
            <div class="text-tit">课文重点句（<!--ko text:$root.typeContent().sentenceFinishNum--><!--/ko-->名学生完成）</div>
            <div class="tab-box">
                <table>
                    <thead>
                    <tr>
                        <td>班级仍不熟悉的课文句子</td>
                        <td>做错学生数</td>
                    </tr>
                    </thead>
                    <tbody data-bind="foreach:{data:$root.typeContent().sentenceAnalysisList,as:'stObj'}">
                    <tr>
                        <td data-bind="text:stObj.sentence">&nbsp;</td>
                        <td data-bind="text:stObj.wrongNum">&nbsp;</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <!--/ko-->
            <!--ko ifnot:$root.typeContent().wordAnalysisList && $root.typeContent().wordAnalysisList.length > 0 && $root.typeContent().sentenceAnalysisList && $root.typeContent().sentenceAnalysisList.length > 0 -->
            <div class="tab-box">
                <table>
                    <thead>
                    <tr>
                        <td>暂无数据</td>
                    </tr>
                    </thead>
                </table>
            </div>
            <!--/ko-->
        </div>
        <div style="display: none;" data-bind="if:$root.typeContent().switchType == 'student',visible:$root.typeContent().switchType == 'student'">
            <div class="clazz-info" data-bind="if:$root.typeContent().wrongMostUserName && $root.typeContent().wrongMostUserName.length > 0,visible:$root.typeContent().wrongMostUserName && $root.typeContent().wrongMostUserName.length > 0">
                出错最多的5个学生：
                <!--ko foreach:{data:$root.typeContent().wrongMostUserName,as:'userName'}-->
                <span data-bind="text:userName">&nbsp;</span> <!--ko if:$index() < ($root.typeContent().wrongMostUserName.length - 1)-->、<!--/ko-->
                <!--/ko-->
            </div>
            <div class="tab-box">
                <table data-bind="if:$root.typeContent().studentPersonalAnalysisList && $root.typeContent().studentPersonalAnalysisList.length > 0,visible:$root.typeContent().studentPersonalAnalysisList && $root.typeContent().studentPersonalAnalysisList.length > 0">
                    <thead>
                    <tr>
                        <td>学生姓名</td>
                        <!--ko foreach:{data:$root.typeContent().tabList,as:'tabName'}-->
                        <td data-bind="text:tabName">&nbsp;</td>
                        <!--/ko-->
                    </tr>
                    </thead>
                    <tbody data-bind="foreach:{data:$root.typeContent().studentPersonalAnalysisList,as:'sp'}">
                    <tr>
                        <td data-bind="text:sp.userName">&nbsp;</td>
                        <!--ko foreach:{data:sp.details,as:'dt'}-->
                        <td data-bind="text:dt">&nbsp;</td>
                        <!--/ko-->
                    </tr>
                    </tbody>
                </table>
                <table data-bind="ifnot:$root.typeContent().studentPersonalAnalysisList && $root.typeContent().studentPersonalAnalysisList.length > 0">
                    <thead>
                        <tr>
                            <td>此作业还没有学生完成！</td>
                        </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
    <div class="reportTips" style="color: #b9b9b9;margin: 10px 12px;">
        <div class="r-left">报告说明:</div>
        <div class="r-right">
            1.此报告数据来源于老师布置的期末基础必过复习作业。<br>
            2.删除作业，或换班可能对统计数据造成影响。<br>
            3.如发现布置的作业有错误，请<a class="w-blue" href="javascript:void(0);" data-bind="click:$root.deleteBasicReview.bind($data,$element)">点击此处</a>删除作业！
        </div>
    </div>
    <!--/ko-->
    <div data-bind="if:$root.success() && (!$root.clazzList() || $root.clazzList().length == 0)" class="w-noData-box">
        您尚未布置期末基础必过复习，请先去布置吧！
    </div>
</div>

    <@sugar.capsule js=["basicreviewreport"] />
</@shell.page>