<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <script>window.HELP_IMPROVE_VIDEOJS = false;window.VIDEOJS_NO_DYNAMIC_STYLE = true;</script>
    <@app.css href="public/plugin/video-js-6.11.0/video-js.min.css" />
    <@app.script href="public/plugin/video-js-6.11.0/ie8/videojs-ie8.min.js" />
    <@sugar.capsule js=["plugin.venus-pre"] css=["plugin.venus-pre"] />
    <@sugar.capsule js=["ko","datepicker","jquery.flashswf"] css=["plugin.datepicker","homeworkv5.dictation","new_teacher.carts","homeworkv3.homework"] />
<div id="arrangehomework">
    <div class="w-base" style="position: relative; zoom: 1;  z-index: 5;">
        <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
            <h3>布置作业</h3>
        </div>
        <div class="w-base-container">
            <div id="level_and_clazzs" class="t-homework-form" style="overflow: visible;">
                <dl data-bind="if:showClazzList().length > 0,visible:showClazzList().length > 0">
                    <dt>班级</dt>
                    <dd style="overflow: hidden; *zoom:1; position: relative;">
                        <div data-bind="template:{name : 't:年级和班级'}" class="w-border-list t-homeworkClass-list"></div>
                    </dd>
                </dl>
                <div data-bind="if:showClazzList().length == 0,visible:showClazzList().length == 0,css:{'w-noData-box' : showClazzList().length == 0}">
                    <!--ko if:$root.hasStudents() != null && $root.hasStudents()-->
                    您需要检查班级上一次作业，才可以布置新作业，去<a href="/teacher/new/homework/report/list.vpage?subject=${subject!}" class="w-blue">检查作业</a>
                    <!--/ko-->
                    <!--ko ifnot:$root.hasStudents() != null && $root.hasStudents()-->
                    您班级的学生数为0，需添加后才可以布置新作业，去<a class="w-blue" href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage">添加学生</a>
                    <!--/ko-->
                </div>
            </div>
            <div id="bookInfo" class="t-homework-form" style="overflow: visible;">
                <!--if:bookId() != null-->
                <dl style="overflow: visible; z-index: 12;">
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
                                    <!--ko if:moduleList().length > 0-->
                                    <!--ko foreach:{data:moduleList(),as:'module'}-->
                                    <label>
                                        <span class="w-icon-md" style="overflow: hidden; text-align: left;text-overflow: ellipsis; white-space: nowrap; width: 200px;" data-bind="text:module.moduleName,attr:{title:module.moduleName}"></span>
                                    </label>
                                    <!--ko foreach:{data:module.units,as:'unit'}-->
                                    <label style="cursor: pointer;" data-bind="click:$root.changeUnit.bind($root,unit.indexRank,'all-unit'),css:{'w-radio-current':(unit.indexRank == $root.focusUnitIndex())}">
                                        <span class="w-radio"></span>
                                        <span class="w-icon-md" style="overflow: hidden; text-align: left;text-overflow: ellipsis; white-space: nowrap; width: 200px;" data-bind="text:unit.cname,attr:{title:unit.cname}"></span>
                                    </label>
                                    <!--/ko-->
                                    <!--/ko-->
                                    <!--/ko-->

                                    <!--ko if:moduleList().length == 0-->
                                    <!--ko foreach:{data:unitList(),as:'unit'}-->
                                    <label style="cursor: pointer;" data-bind="click:$root.changeUnit.bind($root,$index(),'all-unit'),css:{'w-radio-current':($index() == $root.focusUnitIndex())}">
                                        <span class="w-radio"></span>
                                        <span class="w-icon-md" style="overflow: hidden; text-align: left;text-overflow: ellipsis; white-space: nowrap; width: 200px;" data-bind="text:unit.cname,attr:{title:unit.cname}"></span>
                                    </label>
                                    <!--/ko-->
                                    <!--/ko-->
                                </div>
                            </div>
                        </div>
                    </dd>
                </dl>
                <!--/ko-->
                <!--ifnot:bookId() != null-->
                <div data-bind="template:{name:'t:加载中'},visible:loadingImgShow"></div>
                <!--/ko-->
            </div>
        </div>
    </div>
<#--作业形式-->
    <div class="w-base h-baseTab" id="hkTabcontent">
        <div id="objectiveTabs" class="w-base-title w-baseTab-box" style="margin-bottom: 23px;" data-bind="template:{name:'t:教学子目标形式'}">
        </div>
        <div class="homework-way" style="display: none;" id="J_HomeworkWay">
            <div class="subtitle-bar h-switch" id="homeworkTypeTabs">
                <!--ko if:tabs().length > $root.displayCount-->
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
<#---作业统计-->
</div>
<div id="ufo" class="h-floatLayer-R">
    <div class="fl-hd" data-time="0">预计<span data-time="0" id="assignTotalTime">0</span>分钟完成</div>
    <div class="J_UFOInfo fl-mn">
        <p type="BASIC_APP"></p>
        <p type="EXAM"></p>
        <p type="INTELLIGENCE_EXAM"></p>
        <p type="READING"></p>
        <p type="ORAL_PRACTICE"></p>
        <p type="UNIT_QUIZ"></p>
        <p type="MID_QUIZ"></p>
        <p type="END_QUIZ"></p>
        <p type="LISTEN_PRACTICE"></p>
        <p type="FALLIBILITY_QUESTION"></p>
        <p type="NATURAL_SPELLING"></p>
        <p type="DUBBING"></p>
        <p type="DUBBING_WITH_SCORE"></p>
        <p type="LEVEL_READINGS"></p>
        <p type="INTELLIGENT_TEACHING"></p>
        <p type="ORAL_INTELLIGENT_TEACHING"></p>
        <p type="ORAL_COMMUNICATION"></p>
        <p type="DICTATION"></p>
    </div>
    <div class="fl-bot">
        <a href="javascript:void(0)" id="previewBtn" class="preview w-btn w-btn-well w-btn-green">预览</a>
        <a href="javascript:void(0)" id="saveHomworkBtn" class="w-btn w-btn-well w-btn-blue">布置</a>
    </div>
</div>


    <#import "../templates/homeworkv5/levelandclazzs.ftl" as gradegroup>
    <@gradegroup.levelandclazzs yqxueteacher="${((currentTeacherDetail.is17XueTeacher())!false)?string}" />

    <#include "../templates/homeworkv5/changebook.ftl">
    <#import "../templates/homeworkv5/tabs.ftl" as tabs>
    <@tabs.tabTemplate subject="${subject!}"/>
    <#include "../templates/homeworkv5/homeworkreview.ftl">
    <#include "../templates/homeworkv5/confirm.ftl">



<div id="fcttsurl" data-ttsurl="${tts_url!}" style="display: none;"></div>
<div id="fcflashurl" data-flashurl="${readingFlashUrl!}" data-imgdomain="<@app.link_shared href='' />"
     data-webappbaseurl="${requestContext.webAppBaseUrl}" style="display: none;"></div>

<div id="previewDiv"></div><#--查看，预览试题使用的div-->

<script id="t:LOAD_IMAGE" type="text/html">
    <div style="height: 200px; background-color: white; width: 98%;">
        <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
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
<script id="t:video_js_preview_popup" type="text/html">
    <p style="margin-top: -20px; color: #fa7252;display: none;">预览视频仅用于演示作答过程，实际内容以页面为准</p>
    <video id="my-video" class="video-js" controls preload="auto" width="<%=(width ? width : 640)%>" height="<%=(height ? height : 300)%>"
           poster="<%=poster%>" data-setup="{}">
        <%for(var i = 0,iLen = videoList.length; i < iLen; i++){%>
        <source src="<%=videoList[i]%>" type='video/mp4'>
        <%}%>
        <p class="vjs-no-js">
            To view this video please enable JavaScript, and consider upgrading to a web browser that
            <a href="http://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a>
        </p>
    </video>
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
        categoryIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/english-icon/' />',
        tabIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/tab-icon/' />',
        imgDomain        : '${imgDomain!''}',
        domain           : '${requestContext.webAppBaseUrl}/',
        env              : <@ftlmacro.getCurrentProductDevelopment />,
        _homeworkContent : {},
        ucenterUrl       : "${ProductConfig.getUcenterUrl()}",
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
    };

    function nextHomeWork(){
        $.prompt.close();
    }

    $(function(){
        window.LeftMenu && (LeftMenu.focus("ENGLISH_homework"));

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
            jqFrom = ""
        }

        $17.voxLog({
            module: "m_H1VyyebB",
            op : "page_assign_load",
            s0 : "${subject!}",
            s1 : jqFrom
        });
    });
</script>
    <@app.script href="public/plugin/video-js-6.11.0/video.min.js" />
    <@sugar.capsule js=["homeworkv5.english"] />
</@shell.page>