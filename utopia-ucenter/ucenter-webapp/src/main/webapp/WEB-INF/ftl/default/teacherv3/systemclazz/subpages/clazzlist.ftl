<#--TODO load方法，貌似这步会导致这个warning：Synchronous XMLHttpRequest on the main thread is deprecated because of its detrimental effects to the end user's experience.-->
<#--TODO 需要找前端看看-->
<@sugar.capsule js=["clazz.clazzlist","flexslider"] css=["plugin.flexslider"]/>

<#--转让班级-添加班级-下载班级名单-->
<#include "../../block/builtFlowClazz.ftl"/>

<!--w-base template - 调整班级-->
<#if (currentTeacherDetail.isSeniorTeacher())!false
    || ((currentTeacherDetail.isJuniorTeacher())!false && currentTeacherDetail.subject == 'MATH')>
    <#assign multiSubject = false/>
    <#include "../kuailexue/editclazz.ftl"/>
<#elseif (currentTeacherDetail.isJuniorTeacher())!false>
<#--是否多学科-->
    <#assign multiSubject = false/>
    <#include "../junior/editclazz.ftl"/>
<#else>
    <#include "editclazz.ftl"/>
</#if>

<!--w-base tempalte - 添加老师-->
<#include "linkteacher.ftl"/>

<!--w-base tempalte - 转让班级-->
<#include "transferclazz.ftl"/>

<div class="w-clear"></div>
<#if (receiveApplications?? && receiveApplications?size gt 0) || (exitClazzs?? && exitClazzs?size gt 0)>
<div class="w-base w-base-blue">
<#--待处理的收到的请求-->
    <#if receiveApplications?? && receiveApplications?has_content>
        <#list receiveApplications?keys as key >
            <#list receiveApplications[key] as application>
                <div class="w-base-title v-checkapp-base">
                    <h3>${application.clazzName}</h3>

                    <div class="w-base-ext">
                        <#if application.type == "LINK" >
                            <span class="w-bast-ctn"><i
                                    class="w-specialFeel-icon w-magR-10"></i>
                            ${(application.applicantSubject.value)!}老师${application.applicantName!}申请和你一起教${application.clazzName!}的学生</span>
                        <#elseif application.type == "TRANSFER"  >
                            <span class="w-bast-ctn"><i
                                    class="w-specialFeel-icon w-magR-10"></i>
                            ${(application.applicantSubject.value)!}老师${application.applicantName!}请你在${application.clazzName!}担任${(application.respondentSubject.value)!}老师</span>
                        <#elseif application.type == "REPLACE"  >
                            <span class="w-bast-ctn"><i
                                    class="w-specialFeel-icon w-magR-10"></i>
                            ${(application.applicantSubject.value)!}老师${application.applicantName!}申请代替你在${application.clazzName!}教${(application.respondentSubject.value)!}</span>
                        </#if>
                    </div>
                    <div class="w-base-right">
                        <a style="width: 60px;" class="w-btn w-btn-mini btn-title-reject"
                           data-recordId="${application.id!}" data-applicationType="${application.type!}" data-applicantSubject="${application.applicantSubject!}" data-applicantSubjectText="${application.applicantSubject.value}" data-respondentSubject="${application.respondentSubject!}"
                           href="javascript:void (0);">拒绝</a>
                        <a style="width: 60px;" class="w-btn w-btn-mini btn-title-approve"
                           data-recordId="${application.id!}" data-applicationType="${application.type!}" data-applicantSubject="${application.applicantSubject!}" data-applicantSubjectText="${application.applicantSubject.value}" data-respondentSubject="${application.respondentSubject!}"
                           href="javascript:void (0);">允许</a>
                    </div>
                </div>
            </#list>
        </#list>
    </#if>
<#--退出的班级列表-->
    <#if exitClazzs?? && exitClazzs?has_content >
        <#list exitClazzs as clazz>
            <#if !(clazz.clazzId?has_content && sendApplications?has_content && sendApplications[clazz.clazzId?string]?has_content) >
                <div>
                    <div class="w-base-title v-checkapp-base">
                        <h3>${clazz.clazzName!}</h3>

                        <div class="w-base-ext">
                                    <span class="w-bast-ctn"><i
                                            class="w-specialFeel-icon w-magR-10"></i>${clazz.studentCount!0}
                                        名学生还没有找到新${clazz.groupSubject.value!}老师！</span>
                        </div>

                        <div data-clazzName="${clazz.clazzName!}" data-clazzId="${clazz.clazzId!}" data-subject="${clazz.groupSubject!''}" data-subjectText="${clazz.groupSubject.value!''}"
                             class="w-base-right btn-transfer-delete">
                            <a class="w-btn w-btn-mini" href="javascript:void (0);">转给新老师<i
                                    class="w-icon-exclamation-white"></i></a>
                        </div>
                        <div data-clazzName="${clazz.clazzName!}" data-clazzId="${clazz.clazzId!}" data-subject="${clazz.groupSubject!''}"
                             class="w-base-right btn-joinback">
                            <a class="w-btn w-btn-mini" href="javascript:void (0);" style="background-color: rgb(26, 188, 156);border-color: rgb(8, 151, 123);width:80px;">继续教</a>
                        </div>
                    </div>
                    <@sendApplicationsList sendApplications=sendApplications clazz=clazz/>
                    <div class="transfer-panel">
                    </div>
                </div>
            </#if>
        </#list>
    </#if>
</div>
</#if>

<#--我教的班级列表-->
<#if teachClazzs??>
    <#if (teachClazzs?? && teachClazzs?size gt 0)>
        <div style="position: relative; z-index: 2;">
            <div style="position: absolute; right: 16px; top: 59px; width: 150px;">
                <div class="w-popup-info-small">
                    <div class="is-close" id="js_close" style="right:5px;top:5px;"><span class="w-icon-arrow w-icon-arrow-white" style="margin: 0"></span></div>
                    <h3>小提示</h3>
                    <div class="is-content">
                        <a href="https://mp.weixin.qq.com/s/F_AGNEja-gzBoy7UgY8S0A" target="_blank" class="w-blue">换班问题？点这里</a>
                    </div>
                </div>
            </div>
        </div>
    </#if>
    <#list teachClazzs as clazz>
        <#if clazz.clazzType == "PUBLIC">
        <!--w-base template-->
        <div class="w-base">
            <div class="w-base-title">
                <h3>${clazz.clazzName!}<#if multiSubject>(${clazz.subjectText!})</#if></h3>
                <#if sendApplications?has_content
                && sendApplications[clazz.clazzId?string]?has_content
                && (sendApplications[clazz.clazzId?string]["LINK"]?has_content
                || sendApplications[clazz.clazzId?string]["REPLACE"]?has_content) >
                    <div class="w-base-ext">
                        <span class="w-bast-ctn">
                            <i class="w-specialFeel-icon w-magR-10"></i>需请对方通过申请
                            <a href="http://dwz.cn/1G48fj" target="_blank" class="w-blue">对方没响应？点这里</a>
                        </span>
                    </div>
                </#if>
                <#if currentTeacherDetail.isPrimarySchool()!false>
                    <div class="w-base-right">
                        <a href="/teacher/systemclazz/integral/clazzintegral.vpage?clazzId=${clazz.clazzId!}<#if multiSubject>&subject=${curSubject}</#if>" class="w-btn w-btn-mini w-btn-light v-cm-main" style="padding: 5px 0;">班级学豆：${(clazz.clazzIntegral)!0}个</a>
                        <#--<a href="#/teacher/clazz/tinygroup/index.vpage?clazzId=${clazz.clazzId!}&subject=${curSubject}" class="w-btn w-btn-mini w-btn-light v-cm-main" style="width: 90px;"><span class="w-icon-md" style="padding-bottom: 2px;">小组管理</span><span class="w-icon-public w-icon-leader" style="margin: 0;"></span></a>-->
                    </div>
                </#if>
            </div>
            <div class="w-base-container v-checkapp-base" data-clazzName="${clazz.clazzName!}"
                 data-clazzId="${clazz.clazzId!}">
                <div class="transfer-panel">
                </div>
                <div class="link-panel">
                </div>
                <!--//start-->
                <div class="t-class-manage">
                    <dl>
                        <dt>
                        <div class="ic-font">
                            <p class="w-blue">班级学生：${clazz.studentCount!0}人</p>
                        </div>
                        <div class="ic_btn">
                            <a class="w-btn w-btn-small v-cm-main"
                               href="#/teacher/clazz/clazzsdetail.vpage?clazzId=${clazz.clazzId!}"><#if currentUser.fetchCertificationState() == "SUCCESS">
                                学生管理<#else>学生详情</#if></a>
                        </div>
                        </dt>
                        <dd>
                            <div class="flexslider js-teacherCard_list">
                                <ul class="slides teacher-list">
                            <#list clazz.teachers?keys as subject>
                                <#if clazz.teachers[subject]?has_content >
                                    <#assign teacher = clazz.teachers[subject]>
                                    <li class="cm-box">
                                        <div class="avatar">
                                            <div class="back"></div>
                                            <div class="image">
                                                <img src="<@app.avatar href='${teacher.imageUrl!}'/>" width="80"
                                                     height="80">
                                            </div>
                                            <#if subject == curSubject!''><i
                                                    class="state w-icon-arrow w-icon-greenInfo"></i></#if>
                                        </div>
                                        <p class="name v-sharedTeacher" data-teacherSubject="${teacher.subject!}"
                                           data-teacherName="${teacher.teacherName!}">${teacher.subject.value!}老师：${teacher.teacherName!}</p>
                                        <#if (subject == curSubject)!false >
                                            <#if sendApplications?has_content && sendApplications[clazz.clazzId?string]?has_content>
                                                <#if sendApplications[clazz.clazzId?string]["TRANSFER"]?has_content>
                                                    <p><a class="w-blue btn-checkapp"
                                                          data-clazzName="${clazz.clazzName!}"
                                                          data-clazzId="${clazz.clazzId!}">已发送转让班级请求</a></p>
                                                <#elseif sendApplications[clazz.clazzId?string]["REPLACE"]?has_content>
                                                    <p><a class="w-blue btn-checkapp"
                                                          data-clazzName="${clazz.clazzName!}"
                                                          data-clazzId="${clazz.clazzId!}">已发送接管班级请求</a></p>
                                                <#else>
                                                    <#if !isFakeTeacher!false>
                                                        <p><a class="w-blue btn-transfer-exist js-adjustment"
                                                              data-clazzName="${clazz.clazzName!}"
                                                              data-clazzId="${clazz.clazzId!}" data-subject="${curSubject!}">转让班级</a></p>
                                                    </#if>
                                                </#if>
                                            <#else>
                                                <#if !isFakeTeacher!false>
                                                    <p><a class="w-blue btn-transfer-exist js-adjustment"
                                                          data-clazzName="${clazz.clazzName!}"
                                                          data-clazzId="${clazz.clazzId!}" data-subject="${curSubject!}">转让班级</a></p>
                                                </#if>
                                            </#if>
                                        <#else>
                                        <#--<p><a href="javascript:void(0);" data-clazzid="${(clazz.clazzId)!}" data-teacherid="${(teacher.id)!}" data-teachername="${teacher.profile.realname!}" class="v-delete-one w-blue">删除老师</a></p>-->
                                        </#if>
                                    </li>
                                <#else>
                                    <li class="cm-box">
                                        <div class="avatar">
                                            <div class="back"></div>
                                            <div class="image">
                                                <img src="<@app.avatar href=''/>" width="80" height="80">
                                            </div>
                                        </div>
                                        <p class="name">
                                            <#if subject?has_content>
                                                ${validSubjects[subject]!}老师：无
                                            </#if>
                                        </p>
                                        <#assign isSend = false >
                                        <#if sendApplications?has_content
                                        && sendApplications[clazz.clazzId?string]?has_content
                                        && sendApplications[clazz.clazzId?string]["LINK"]?has_content>
                                            <#list sendApplications[clazz.clazzId?string]["LINK"] as app >
                                                <#if app.respondentSubject == subject>
                                                    <#assign isSend = true >
                                                </#if>
                                            </#list>
                                        </#if>
                                        <#if isSend == true >
                                            <p><a class="w-blue btn-checkapp" data-subject="${subject!}">已发送添加老师请求</a>
                                            </p>
                                        <#else>
                                            <p><a class="w-blue v-linkteacher" data-subject="${subject!}">添加老师</a></p>
                                        </#if>
                                    </li>
                                </#if>
                            </#list>
                                </ul>
                            </div>
                        </dd>
                    </dl>
                </div>
                <!--end//-->
            </div>
        <#--TODO 与之前重复，可否优化？v-checkapp-->
            <@sendApplicationsList sendApplications=sendApplications clazz=clazz/>
        </div>
        <#else>
        <!--教学班-->
        <div class="w-base">
            <div class="w-base-title">
                <h3>${clazz.clazzName!}
                    <span style="font-size: 16px;">
                        (教学班<#if ((clazz.stageType!'') != 'UNKNOWN') && ((clazz.stageType!'') != '')> ${clazz.stageType!}层</#if>)
                    </span>
                </h3>
            </div>
            <div class="w-base-container v-checkapp-base" data-clazzName="${clazz.clazzName!}"
                 data-clazzId="${clazz.clazzId!}">
                <div class="transfer-panel">
                </div>
                <div class="link-panel">
                </div>
                <!--//start-->
                <div class="t-class-manage">
                    <dl>
                        <dt>
                        <div class="ic-font">
                            <p class="w-blue">班级学生：${clazz.studentCount!0}人</p>
                        </div>
                        <div class="ic_btn">
                            <a class="w-btn w-btn-small"
                               href="#/teacher/clazz/clazzsdetail.vpage?clazzId=${clazz.clazzId!}"><#if currentUser.fetchCertificationState() == "SUCCESS">
                                学生管理<#else>学生详情</#if></a>
                        </div>
                        </dt>
                        <dd>
                            <div class="flexslider js-teacherCard_list">
                                <ul class="slides teacher-list">
                                    <#list clazz.teachers?keys as subject>
                                        <#if clazz.teachers[subject]?has_content >
                                            <#assign teacher = clazz.teachers[subject]>
                                            <li class="cm-box">
                                                <div class="avatar">
                                                    <div class="back"></div>
                                                    <div class="image">
                                                        <img src="<@app.avatar href='${teacher.imageUrl!}'/>" width="80"
                                                             height="80">
                                                    </div>
                                                    <#if teacher.id == currentUser.id><i
                                                            class="state w-icon-arrow w-icon-greenInfo"></i></#if>
                                                </div>
                                                <p class="name v-sharedTeacher" data-teacherSubject="${teacher.subject!}"
                                                   data-teacherName="${teacher.teacherName!}">${teacher.subject.value!}
                                                    老师：${teacher.teacherName!}</p>
                                                <#if (subject == curSubject)!false >
                                                    <#if sendApplications?has_content && sendApplications[clazz.clazzId?string]?has_content>
                                                        <#if sendApplications[clazz.clazzId?string]["TRANSFER"]?has_content>
                                                            <a class="add btn-checkapp"
                                                               data-clazzName="${clazz.clazzName!}"
                                                               data-clazzId="${clazz.clazzId!}">已发送转让班级请求</a>
                                                        <#elseif sendApplications[clazz.clazzId?string]["REPLACE"]?has_content>
                                                            <a class="add btn-checkapp"
                                                               data-clazzName="${clazz.clazzName!}"
                                                               data-clazzId="${clazz.clazzId!}">已发送接管班级请求</a>
                                                        <#else>
                                                            <#if !isFakeTeacher!false>
                                                                <a class="add btn-transfer-exist w-blue"
                                                                   data-clazzName="${clazz.clazzName!}"
                                                                   data-clazzId="${clazz.clazzId!}" data-subject="${curSubject!}">转让班级</a>
                                                            </#if>
                                                        </#if>
                                                    <#else>
                                                        <#if !isFakeTeacher!false>
                                                            <a class="add btn-transfer-exist w-blue"
                                                               data-clazzName="${clazz.clazzName!}"
                                                               data-clazzId="${clazz.clazzId!}" data-subject="${curSubject!}">转让班级</a>
                                                        </#if>
                                                    </#if>
                                                </#if>
                                            </li>
                                        </#if>
                                    </#list>
                                </ul>
                            </div>
                        </dd>
                    </dl>
                </div>
                <!--end//-->
            </div>
        </div>
        </#if>
    </#list>
</#if>

<#if graduatedClazzs?? && graduatedClazzs?has_content>
<div class="t-graduation-class">
    <div class="switch-me">
        <a id="showMoreGraduated" href="javascript:void(0);">
            <span class="w-icon-md">查看已毕业班级</span><span class="w-icon-arrow"></span>
        </a>
    </div>
    <div id="graduated_clazz_list" style="display: none;">
        <div class="gc-list">
            <#list graduatedClazzs as clazz>
                <div class="w-base">
                    <div class="w-base-title">
                        <h3>${clazz.clazzName!}</h3>

                        <div class="w-base-ext">
                            <span class="w-bast-ctn">班号：C${clazz.clazzId!}</span>
                        </div>
                        <div class="have-gc">已毕业</div>
                    </div>
                </div>
            </#list>
        </div>
    </div>
</div>
</#if>

<script type="text/javascript">
    $(function () {

        $("#js_close").click(function(){
            $(this).parent().parent().slideUp("fast");
        });

        // 取消转让申请
        $(".cancel-transferapp").on('click', function () {
            var $this = $(this);
            var recordId = $this.attr("data-recordId");
            var subject = $this.attr("data-applicantSubject");

            $.get("/teacher/systemclazz/canceltransferapp.vpage<#if multiSubject>?subject="+subject+"</#if>", {recordId: recordId}, function (data) {
                if (data.success) {
                    $17.alert("取消申请成功。", function () {
                        location.reload();
                    })
                } else {
                    $17.alert(data.info, function () {
                        location.reload();
                    })
                }
            })
        });

        // 取消共享申请
        $(".cancel-linkapp").on('click', function () {
            var $this = $(this);
            var recordId = $this.attr("data-recordId");
            var subject = $this.attr("data-applicantSubject");

            $.get("/teacher/systemclazz/cancellinkapp.vpage<#if multiSubject>?subject="+subject+"</#if>", {recordId: recordId}, function (data) {
                if (data.success) {
                    $17.alert("取消申请成功。", function () {
                        location.reload();
                    })
                } else {
                    $17.alert(data.info, function () {
                        location.reload();
                    })
                }
            })
        });

        // 取消接管申请
        $(".cancel-replaceapp").on('click', function () {
            var $this = $(this);
            var recordId = $this.attr("data-recordId");
            var subject = $this.attr("data-applicantSubject");

            $.get("/teacher/systemclazz/cancelreplaceapp.vpage<#if multiSubject>?subject="+subject+"</#if>", {recordId: recordId}, function (data) {
                if (data.success) {
                    $17.alert("取消申请成功。", function () {
                        location.reload();
                    })
                }
            })
        });

        // 查看请求
        $(".btn-checkapp").on('click', function () {
            var $this = $(this);
            var $checkApp = $this.parents(".v-checkapp-base").siblings(".v-checkapp");
            if ($checkApp.hasClass("dis")) {
                $checkApp.removeClass("dis");
                $checkApp.hide();
                return;
            }
            $checkApp.addClass("dis");
            $checkApp.show();
        });


        // 班级标题行拒绝
        $(".btn-title-reject").on('click', function () {
            var $this = $(this);
            var recordId = $this.attr("data-recordId");
            var type = $this.attr("data-applicationType");
            var applicantSubjectText = $this.attr("data-applicantSubjectText");
            var respondentSubject = $this.attr("data-respondentSubject");

            var page = "",message = "";
            switch (type) {
                case "LINK":
                    message = "拒绝和对方一起教这个班？";
                    page = "rejectlinkapp.vpage";
                    break;
                case "TRANSFER":
                    message = "拒绝在这个班担任" + applicantSubjectText + "老师？";
                    page = "rejecttransferapp.vpage";
                    break;
                case "REPLACE":
                    message = "拒绝对方代替你教这个班？";
                    page = "rejectreplaceapp.vpage";
                    break;
            }

            $.prompt("<div class='w-ag-center'>" + message + "</div>", {
                focus: 1,
                title: "系统提示",
                buttons: {"取消": false, "拒绝": true},
                position: {width: 500},
                submit: function (e, v) {// 发送关联请求
                    if (v) {
                        $.get("/teacher/systemclazz/" + page + "<#if multiSubject>?subject="+respondentSubject+"</#if>", {recordId: recordId}, function (data) {
                            if (data.success) {
                                $17.alert("已拒绝该请求", function () {
                                    location.reload();
                                });
                            } else {
                                $17.alert(data.info, function () {
                                    location.reload();
                                })
                            }
                        });
                    }
                }
            });
        });

        // 班级标题行同意
        $(".btn-title-approve").on('click', function () {
            var $this = $(this);
            var recordId = $this.attr("data-recordId");
            var type = $this.attr("data-applicationType");
            var page = "";
            var message = "允许此申请，班级、学生将出现变动。确定？";
            var applicantSubject = $this.attr("data-applicantSubject");
            var applicantSubjectText = $this.attr("data-applicantSubjectText");
            var respondentSubject = $this.attr("data-respondentSubject");
            switch (type) {
                case "LINK":
                    var teachers = $this.parents(".w-base-title").siblings(".v-checkapp-base").find(".v-sharedTeacher");
                    message = "允许后，你将和对方一起教这个班。确定？";
                    for (var i = 0; i < teachers.length; i++) {
                        var $teacher = teachers[i];
                        var subject = $teacher.dataset.teachersubject;
                        var name = $teacher.dataset.teachername;
                        if (subject == applicantSubject) {
                            message = "该科目已关联" + name + "老师，同意关联后，新老师将替代原有老师，确定关联吗？";
                        }
                    }
                    page = "approvelinkapp.vpage";
                    break;
                case "TRANSFER":
                    message = "允许后，你将在这个班担任" + applicantSubjectText + "老师。确定？";
                    page = "approvetransferapp.vpage";
                    break;
                case "REPLACE":
                    message = "允许后，你将不再担任该班" + applicantSubjectText + "老师。确定？";
                    page = "approvereplaceapp.vpage";
                    break;
            }

            $.prompt("<div class='w-ag-center'>" + message + "</div>", {
                focus: 1,
                title: "系统提示",
                buttons: {"取消": false, "确定": true},
                position: {width: 500},
                submit: function (e, v) {// 发送关联请求
                    if (v) {
                        $.get("/teacher/systemclazz/" + page + "<#if multiSubject>?subject="+respondentSubject+"</#if>", {recordId: recordId}, function (data) {
                            if (data.success) {
                                $17.alert("已同意该请求", function () {
                                    location.reload();
                                });
                            } else {
                                $17.alert(data.info, function () {
                                    location.reload();
                                });
                            }
                        });
                    }
                }
            });
        });

        var flexSliderLoop = setInterval(function () {
            if(jQuery.hasOwnProperty('flexslider')){
                clearInterval(flexSliderLoop);
                $(".js-teacherCard_list").each(function(){
                    $(this).flexslider({
                        animation : "slide",
                        animationLoop : true,
                        slideshow : false,
                        slideshowSpeed: 4000, //展示时间间隔ms
                        animationSpeed: 400, //滚动时间ms
                        itemWidth : 90,
                        direction : "horizontal",//水平方向
                        minItems : 3,
                        maxItems : 3,
                        move : 3,
                        directionNav: true,
                        controlNav: true
                    });
                    if($(this).find('.teacher-list>li').length <= 3){
                        $(this).find('.flex-direction-nav').hide();
                    }
                });
            }
        },100);
        
    });
</script>

<#macro allSendApplicationsList sendApplications>
    <#if sendApplications?has_content>
    <div class="w-base-container v-checkapp" style="border-bottom: 1px solid #dfdfdf;">
        <div class="w-table">
            <table data-bind="if: content().length &gt; 0, visible: content().length &gt; 0" style="">
                <thead>
                <tr>
                    <td>申请内容</td>
                    <td>班级</td>
                    <td>状态</td>
                    <td>操作</td>
                </tr>
                </thead>
                <tbody>
                    <#list sendApplications?keys as key >
                        <#if sendApplications[key]["TRANSFER"]?has_content>
                            <#list sendApplications[key]["TRANSFER"] as application>
                            <tr>
                                <td>请${application.respondentName}老师在这个班担任${application.respondentSubject.value}老师</td>
                                <td>${application.clazzName!}</td>
                                <td>等待对方同意</td>
                                <td>
                                    <a class="cancel-transferapp" data-recordId="${application.id}" data-applicantSubject="${application.applicantSubject!}"
                                       href="javascript:void (0);">取消</a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                        <#if sendApplications[key]["LINK"]?has_content>
                            <#list sendApplications[key]["LINK"] as application>
                            <tr>
                                <td>申请在此班教${application.applicantSubject.value!}，与${application.respondentName}老师一起教此班</td>
                                <td>${application.clazzName!}</td>
                                <td>等待对方同意</td>
                                <td>
                                    <a class="cancel-linkapp" data-recordId="${application.id}" data-applicantSubject="${application.applicantSubject!}"
                                       href="javascript:void (0);">取消</a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                        <#if sendApplications[key]["REPLACE"]?has_content>
                            <#list sendApplications[key]["REPLACE"] as application>
                            <tr>
                                <td>申请代替${(application.respondentName)!}老师在这个班教${(application.respondentSubject.value)!}</td>
                                <td>${(application.clazzName)!}</td>
                                <td>等待对方同意</td>
                                <td>
                                    <a class="cancel-replaceapp" data-recordId="${(application.id)!}" data-applicantSubject="${(application.applicantSubject)!}"
                                       href="javascript:void (0);">取消</a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </#list>
                </tbody>
            </table>
        </div>
    </div>
    </#if>
</#macro>

<#macro sendApplicationsList sendApplications clazz>
    <#if clazz.clazzId?has_content && sendApplications?has_content && sendApplications[clazz.clazzId?string]?has_content >
    <div class="w-base-container v-checkapp" style="display: none;border-bottom: 1px solid #dfdfdf;">
        <div class="w-table">
            <table data-bind="if: content().length &gt; 0, visible: content().length &gt; 0" style="">
                <thead>
                <tr>
                    <td>申请内容</td>
                    <td>班级</td>
                    <td>状态</td>
                    <td>操作</td>
                </tr>
                </thead>
                <tbody>
                    <#if sendApplications[clazz.clazzId?string]["TRANSFER"]?has_content>
                        <#list sendApplications[clazz.clazzId?string]["TRANSFER"] as application>
                        <tr>
                            <td>请${application.respondentName!}老师在这个班担任${(application.respondentSubject.value)!}老师</td>
                            <td>${clazz.clazzName!}</td>
                            <td>等待对方同意</td>
                            <td>
                                <a class="cancel-transferapp" data-recordId="${application.id!}" data-applicantSubject="${application.applicantSubject!}"
                                   href="javascript:void (0);">取消</a>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                    <#if sendApplications[clazz.clazzId?string]["LINK"]?has_content>
                        <#list sendApplications[clazz.clazzId?string]["LINK"] as application>
                        <tr>
                            <td>申请在此班教${application.applicantSubject.value!}，与${application.respondentName!}老师一起教此班</td>
                            <td>${clazz.clazzName!}</td>
                            <td>等待对方同意</td>
                            <td>
                                <a class="cancel-linkapp" data-recordId="${application.id}" data-applicantSubject="${application.applicantSubject!}"
                                   href="javascript:void (0);">取消</a>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                    <#if sendApplications[clazz.clazzId?string]["REPLACE"]?has_content>
                        <#list sendApplications[clazz.clazzId?string]["REPLACE"] as application>
                        <tr>
                            <td>申请代替${application.respondentName}老师在这个班教${(application.respondentSubject.value)!}</td>
                            <td>${clazz.clazzName!}</td>
                            <td>等待对方同意</td>
                            <td>
                                <a class="cancel-replaceapp" data-recordId="${application.id}" data-applicantSubject="${application.applicantSubject!}"
                                   href="javascript:void (0);">取消</a>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
    </#if>
<script>
    var loc = location.href.indexOf('adjustment') > -1;
    if (loc){
        $(".adjustment").show();
    }
    if ($(".js-adjustment").length > 0){
        $(".adjustmentImg").show();
        var tops = $(".js-adjustment:first").offset().top;
        var lefts = $(".js-adjustment:first").offset().left;

        $(".adjustmentBox").css({
            "top":(tops - 175)+"px",
            "left":(lefts - 130) + "px"
        });
    }else{
        $(".adjustmentBox").css({
            "top":"30%",
            "left":"45%"
        });
    }

    $(".adjustmentBox").on("click",function () {
        $(".adjustment").hide();
    });

</script>
</#macro>