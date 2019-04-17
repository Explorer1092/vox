<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <script>window.HELP_IMPROVE_VIDEOJS = false;window.VIDEOJS_NO_DYNAMIC_STYLE = true;</script>
    <@app.css href="public/plugin/video-js-6.11.0/video-js.min.css" />
    <@app.script href="public/plugin/video-js-6.11.0/ie8/videojs-ie8.min.js" />
    <@sugar.capsule js=["ko"] css=["new_teacher.carts","homeworkv3.homework"] />
<div id="arrangehomework">
    <div class="w-base" style="position: relative; zoom: 1;  z-index: 5;">
        <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
            <h3>课堂资源</h3>
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
<#--练习形式-->
    <div class="w-base h-baseTab" id="hkTabcontent">
        <div id="objectiveTabs" class="w-base-title w-baseTab-box" style="margin-bottom: 23px;" data-bind="template:{name:'t:教学子目标形式'}">
        </div>
        <div class="homework-way" style="display: none;" id="J_HomeworkWay">
            <div class="home-content" id="tabContent">

            </div>
        </div>
    </div>
<#---练习统计-->
</div>

<#import "../templates/homeworkv3/levelandclazzs.ftl" as gradegroup>
<@gradegroup.levelandclazzs yqxueteacher="${((currentTeacherDetail.is17XueTeacher())!false)?string}" />
<#include "../templates/homeworkv3/changebook.ftl">
<#include "homeworktypetemplate.ftl">

<script id="t:video_js_preview_popup" type="text/html">
    <p style="margin-top: -20px; color: #fa7252;">该视频仅供老师课堂播放，不用于学生练习</p>
    <video id="my-video" class="video-js" controls preload="auto" controlslist="nodownload"  width="<%=(width ? width : 640)%>" height="<%=(height ? height : 300)%>"
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
        subject          : "${subject!}",
        batchclazzs      : ${batchclazzs![]},
        hasStudents      : ${hasStudents?string('true','false')},
        tabIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/tab-icon/' />',
        categoryIconPrefixUrl : '<@app.link href='public/skin/teacherv3/images/homework/english-icon/' />',
        imgDomain        : '${imgDomain!''}',
        domain           : '${requestContext.webAppBaseUrl}/',
        env              : <@ftlmacro.getCurrentProductDevelopment />,
        logModule        : "m_aHrND8yNXX"
    };

    function nextHomeWork(){
        $.prompt.close();
    }

    $(function(){
        window.LeftMenu && (LeftMenu.focus("clazzresource"));

        var log = $17.getQuery("log") || "leftMenu";
        var jqFrom;
        if(log === "leftMenu"){
            jqFrom = "导航栏_课堂资源";
        }else if(log === "popup"){
            jqFrom = "首页_弹窗";
        }else if(log === "message"){
            jqFrom = "首页_消息";
        }else if(log === "cardList"){
            jqFrom = "首页_卡片_课堂资源"
        }else{
            jqFrom = ""
        }
        $17.voxLog({
            module  : constantObj.logModule,
            op      : "firstpage_resource_load",
            s0      : jqFrom
        });
    });
</script>
    <@app.script href="public/plugin/video-js-6.11.0/video.min.js" />
    <@sugar.capsule js=["clazzresource.englishindex"] />
</@shell.page>