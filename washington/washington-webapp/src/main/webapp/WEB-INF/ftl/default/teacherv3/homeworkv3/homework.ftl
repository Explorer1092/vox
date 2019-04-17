<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@sugar.capsule js=["plugin.venus-pre"] css=["plugin.venus-pre"] />
    <#if subject?? && subject == "CHINESE">
        <@sugar.capsule js=["jplayer"] css=["homeworkv3.wordrecognitionandreading","homeworkv5.wordteachandpractice"]/>
    </#if>
    <#if subject?? && subject == "MATH">
        <@sugar.capsule css=["homeworkv3.ocrmental"] />
    </#if>
    <style>
        .seth-type{
            position: absolute;
            right: 0;
            top: 0;
            width: 43px;
            height: 41px;
            background:url(../../../public/skin/teacherv3/images/homework/typeIcon.png) no-repeat
        }
        .sub-type{
            display: inline-block;
            width: 50px;
            text-indent: 10px;
            text-align: center;
            color: #fff;
            transform:rotate(45deg);
            -ms-transform:rotate(45deg); /* Internet Explorer 9*/
            -moz-transform:rotate(45deg); /* Firefox */
            -webkit-transform:rotate(45deg); /* Safari 和 Chrome */
            -o-transform:rotate(45deg); /* Opera */
        }
    </style>
    <@sugar.capsule js=["ko","datepicker"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework"] />
    <div id="arrangehomework">
        <div class="w-base" style="position: relative; zoom: 1;  z-index: 5;">
            <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
                <h3>布置作业</h3>
            </div>
            <div class="w-base-container">
                <div id="level_and_clazzs" class="t-homework-form" style="overflow: visible;">
                    <dl style="display: none;" data-bind="if:showClazzList().length > 0,visible:showClazzList().length > 0">
                        <dt>班级</dt>
                        <dd style="overflow: hidden; *zoom:1; position: relative;">
                            <div data-bind="template:{name : 't:年级和班级'}" class="w-border-list t-homeworkClass-list"></div>
                        </dd>
                    </dl>
                    <div style="display: none;" data-bind="if:showClazzList().length == 0,visible:showClazzList().length == 0,css:{'w-noData-box' : showClazzList().length == 0}">
                        <!--ko if:$root.hasStudents() != null && $root.hasStudents()-->
                        您需要检查班级上一次作业，才可以布置新作业，去<a href="/teacher/new/homework/report/list.vpage?subject=${subject!}" class="w-blue">检查作业</a>
                        <!--/ko-->
                        <!--ko ifnot:$root.hasStudents() != null && $root.hasStudents()-->
                        您班级的学生数为0，需添加后才可以布置新作业，去<a class="w-blue" href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage">添加学生</a>
                        <!--/ko-->
                    </div>
                </div>
                <div id="bookInfo" class="t-homework-form" style="overflow: visible;">
                    <dl style="overflow: visible; z-index: 12;display: none;" data-bind="if:bookId() != null,visible:bookId() != null">
                        <dt>教材</dt>
                        <dd  style=" position: relative;">
                            <div class="text">
                                <span data-bind="text:bookName()"></span>
                                <a class="w-blue" href="javascript:void(0);" style="margin-left: 50px;" data-bind="click:changeBook">
                                    更换教材<span class="w-icon-public w-icon-switch w-icon-switchBlue" style="margin-right: -5px; margin-left: 10px; *margin: 3px 0 0 10px;"></span>
                                </a>
                            </div>
                        </dd>
                        <dt data-bind="if: focusUnit() != null">单元</dt>
                        <dd style="position: relative; zoom: 1; height: 24px;" data-bind="if: focusUnit() != null">
                            <div class="text"><span data-bind="text:focusUnit().cname" style="float: left;"></span>
                                <div class="h-slide" data-bind="allUnitHover:'on'">
                                    <span class="slideText">更换单元<em id="arrowUnit" class="w-icon-arrow w-icon-arrow-blue"></em></span><!--向上w-icon-arrow-topBlue-->
                                    <div class="h-slide-box allunit">
                                        <!--ko foreach:{data:unitList(),as:'unit'}-->
                                        <label style="cursor: pointer;" data-bind="click:$root.changeUnit.bind($root,$index(),'all-unit'),css:{'w-radio-current':($index() == $root.focusUnitIndex())}">
                                            <span class="w-radio"></span>
                                            <span class="w-icon-md" style="overflow: hidden; text-align: left;text-overflow: ellipsis; white-space: nowrap; width: 200px;" data-bind="text:unit.cname,attr:{title:unit.cname}"></span>
                                        </label>
                                        <!--/ko-->
                                    </div>
                                </div>
                            </div>
                        </dd>
                    </dl>
                    <dl style="display: none;" data-bind="if: focusUnit() != null && bookId() != null,visible:focusUnit() != null && bookId() != null">
                        <dd>
                            <div class="h-unit-box">
                                <div class="unit-info">
                                    <ul>
                                        <!--ko foreach:{data:focusUnit().sections,as:'section'}-->
                                        <li data-bind="css:{'w-checkbox-current' : section.checked},click:$root.sectionClick.bind($data,$root,$index())"><span class="w-checkbox"></span><span data-bind="text:section.cname" class="project-name"></span></li>
                                        <!--/ko-->
                                    </ul>
                                </div>
                            </div>
                        </dd>
                    </dl>
                    <div data-bind="template:{name:'t:加载中'},visible:bookId() == null"></div>
                </div>
            </div>
        </div>
        <#--作业形式-->
        <div class="w-base h-baseTab" id="hkTabcontent">
            <div id="objectiveTabs" class="w-base-title w-baseTab-box" style="margin-bottom: 23px;" data-bind="template:{name:'t:教学子目标形式'}">
            </div>
            <div class="homework-way" style="display: none;" id="J_HomeworkWay">
                <div class="subtitle-bar h-switch" id="homeworkTypeTabs">
                    <!--ko if:$root.tabs().length > $root.displayCount-->
                    <div class="h-arrow h-arrow-L" data-bind="click:arrowClick.bind($data,'arrowLeft')"><i class="h-arrow-icon" data-bind="css:{'h-arrow-iconLhover' : leftEnabled()}"></i></div>
                    <div class="h-arrow h-arrow-R" data-bind="click:arrowClick.bind($data,'arrowRight')"><i class="h-arrow-icon h-arrow-iconR" data-bind="css:{'h-arrow-iconRhover' : rightEnabled()}"></i></div>
                    <!--/ko-->
                    <ul class="topBar" data-bind="foreach:{data:$root.currentTabs(),as:'tab'}">
                        <li style="overflow: hidden;text-overflow:ellipsis;white-space: nowrap;cursor: pointer;" data-bind="click:$root.tabClick.bind($data,$root),css:{'active' : tab.objectiveConfigId() == $root.objectiveConfigId()},text:tab.name()">&nbsp;</li>
                    </ul>
                </div>

                <div class="home-content" id="tabContent">

                </div>
            </div>
        </div>
    </div>
    <#---作业统计-->
    <div id="ufo" class="h-floatLayer-R">
        <div class="fl-hd" data-time="0">预计<span data-time="0" id="assignTotalTime">0</span>分钟完成</div>
        <div class="J_UFOInfo fl-mn">
            <p type="EXAM"></p>
            <p type="INTELLIGENCE_EXAM"></p>
            <p type="BASIC_KNOWLEDGE"></p>
            <p type="CHINESE_READING"></p>
            <p type="MENTAL"></p>
            <p type="KEY_POINTS"></p>
            <p type="INTERESTING_PICTURE"></p>
            <p type="WORD_PRACTICE"></p>
            <p type="READ_RECITE"></p>
            <p type="NEW_READ_RECITE"></p>
            <p type="UNIT_QUIZ"></p>
            <p type="MID_QUIZ"></p>
            <p type="END_QUIZ"></p>
            <p type="KNOWLEDGE_REVIEW"></p>
            <p type="FALLIBILITY_QUESTION"></p>
            <p type="PHOTO_OBJECTIVE"></p>
            <p type="VOICE_OBJECTIVE"></p>
            <p type="MENTAL_ARITHMETIC"></p>
            <p type="READ_RECITE_WITH_SCORE"></p>
            <p type="LEVEL_READINGS"></p>
            <p type="INTELLIGENT_TEACHING"></p>
            <p type="OCR_MENTAL_ARITHMETIC"></p>
            <p type="WORD_RECOGNITION_AND_READING"></p>
            <p type="CALC_INTELLIGENT_TEACHING"></p>
            <p type="ORAL_COMMUNICATION"></p>
            <p type="WORD_TEACH_AND_PRACTICE"></p>
        </div>
        <div class="fl-bot">
            <a href="javascript:void(0)" id="previewBtn" class="w-btn w-btn-well w-btn-green preview">预览</a>
            <a href="javascript:void(0)" id="saveHomworkBtn" class="w-btn w-btn-well w-btn-blue">布置</a>
        </div>
    </div>

    <div id="jquery_jplayer_1" class="jp-jplayer"></div>
    <#if subject?has_content && subject == "MATH">
        <#include "../templates/teacherapp.ftl">
    </#if>

    <#import "../templates/homeworkv3/levelandclazzs.ftl" as gradegroup>
    <@gradegroup.levelandclazzs yqxueteacher="${((currentTeacherDetail.is17XueTeacher())!false)?string}" />
    <#--<#include "../templates/homeworkv3/levelandclazzs.ftl">-->
    <#include "../templates/homeworkv3/changebook.ftl">
    <#import "../templates/homeworkv3/tabs.ftl" as tabs>
    <@tabs.tabTemplate subject="${subject!}"/>
    <#include "../templates/homeworkv3/homeworkreview.ftl">
    <#include "../templates/homeworkv3/confirm.ftl">

<script id="t:LOAD_IMAGE" type="text/html">
    <div style="height: 200px; background-color: white; width: 98%;">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
    </div>
</script>

<script type="text/javascript">

    var constantObj = {
        term             : ${term!1},
        subject          : "${subject!}",
        currentDateTime  : "${currentDateTime}",
        endDate          : "${endDate!}",
        endTime          : "${endTime!}",
        extData          : "${extData!}",
        batchclazzs      : ${batchclazzs![]},
        hasStudents      : ${hasStudents?string('true','false')},
        homeworkImgUrl   : "<@app.link href='public/skin/teacherv3/images/homework/' />",
        tabIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/tab-icon/' />',
        imgDomain        : '${imgDomain!''}',
        domain           : '${requestContext.webAppBaseUrl}/',
        env              : <@ftlmacro.getCurrentProductDevelopment />,
        _homeworkContent : {},
        isTermEnd        :  "false",
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>",
        ucenterUrl       : "${ProductConfig.getUcenterUrl()}",
        mentalChangeTab  : ${((currentTeacherWebGrayFunction.isAvailable("TeacherMentalArithmetic", "ChangeTab"))!false)?string}
    };


    $(function(){

        window.LeftMenu && (LeftMenu.focus("${subject!}_homework"));

        var log = $17.getQuery("log") || "leftMenu";
        var jqFrom;
        if(log === "leftMenu"){
            jqFrom = "导航栏_布置作业";
        }else if(log === "popup"){
            jqFrom = "首页_弹窗";
        }else if(log === "message"){
            jqFrom = "首页_消息";
        }else if(log === "cardList"){
            jqFrom = "首页_卡片_布置新作业"
        }else{
            jqFrom = "";
        }

        $17.voxLog({
            module: "m_H1VyyebB",
            op : "page_assign_load",
            s0 : "${subject!}",
            s1 : jqFrom
        });

    });
</script>
    <#if subject?? && subject == "CHINESE">
        <@sugar.capsule js=["chinese.recognitionreadingquestion","homeworkv3.chinese.compontents"]/>
    </#if>
    <@sugar.capsule js=["homeworkv3.homework","homework2nd"] />
</@shell.page>