<#--TODO load方法，貌似这步会导致这个warning：Synchronous XMLHttpRequest on the main thread is deprecated because of its detrimental effects to the end user's experience.-->
<#--TODO 需要找前端看看-->
<@sugar.capsule js=["clazz.clazzlist","flexslider"] css=["plugin.flexslider"]/>

<#assign multiSubject = false/>

<#--转让班级-添加班级-下载班级名单-->
<#include "../../block/builtFlowClazz.ftl"/>

<!--w-base template - 调整班级-->
<#include "editclazz.ftl"/>

<!--w-base tempalte - 添加老师-->
<#include "linkteacher.ftl"/>

<!--w-base tempalte - 转让班级-->
<#include "transferclazz.ftl"/>


<#--无阅卷机号的学生-->
<#if noScanNumberStudents?? && noScanNumberStudents?has_content>
    <#if klxScanMachineFlag?has_content && klxScanMachineFlag == true>
    <#list noScanNumberStudents as noScanNumberClazz>
        <div class="title-bar clearfix bg">
            <div class="btn clearfix">
                <a class="green" href="#/teacher/clazz/clazzsdetail.vpage?clazzId=${noScanNumberClazz.clazzId!}">去添加</a>
            </div>
            <div class="title">${noScanNumberClazz.clazzName!}</div>
            <div class="title">
                ${noScanNumberClazz.noSNCount!}名学生没有阅卷机填涂号，这些学生的试卷无法扫描哦！
            </div>
        </div>
    </#list>
    </#if>
</#if>
<#--待处理的收到的请求-->
<#if receiveApplications?? && receiveApplications?has_content>
    <#list receiveApplications?keys as key >
        <#list receiveApplications[key] as application>
            <div class="title-bar clearfix bg">
                <div class="title">${application.clazzName}</div>
                <div class="title">
                    <#if application.type == "LINK" >
                        ${application.applicantSubject.value}老师${application.applicantName}申请和你一起教${application.clazzName}的学生
                    <#elseif application.type == "TRANSFER"  >
                        ${application.applicantSubject.value}老师${application.applicantName}请你在${application.clazzName}担任${application.respondentSubject.value}老师
                    <#elseif application.type == "REPLACE"  >
                        ${application.applicantSubject.value}老师${application.applicantName}申请代替你在${application.clazzName}教${application.respondentSubject.value}
                    </#if>
                </div>
                <div class="btn clearfix">
                    <a class="btn-title-reject" data-recordId="${application.id}" data-applicationType="${application.type}" data-applicantSubject="${application.applicantSubject}" data-applicantSubjectText="${application.applicantSubject.value}" data-respondentSubject="${application.respondentSubject}"
                       href="javascript:void (0);">拒绝</a>
                    <a class="green btn-title-approve" data-recordId="${application.id}" data-applicationType="${application.type}" data-applicantSubject="${application.applicantSubject}" data-applicantSubjectText="${application.applicantSubject.value}" data-respondentSubject="${application.respondentSubject}"
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
                <div class="title-bar clearfix bg">
                    <div class="btn clearfix">
                        <a data-clazzName="${clazz.clazzName!}" data-clazzId="${clazz.clazzId!}" data-subject="${clazz.groupSubject!''}" class="btn-joinback" href="javascript:void (0);">继续教</a>
                        <a data-clazzName="${clazz.clazzName!}" data-clazzId="${clazz.clazzId!}" data-subject="${clazz.groupSubject!''}" data-subjectText="${clazz.groupSubject.value!''}" class="green btn-transfer-delete" href="javascript:void (0);">转给新老师</a>
                    </div>
                    <div class="title">${clazz.clazzName!}</div>
                    <div class="title">${clazz.studentCount!0}名学生还没有找到新${clazz.groupSubject.value!}老师！</div>
                </div>
                <div class="class-module" style="margin-top: -20px; border: 0px">
                    <div class="transfer-panel">
                    </div>
                </div>
            </div>
        </#if>
    </#list>
</#if>

<#--我教的班级列表-->
<#if teachClazzs??>
    <#list teachClazzs as clazz>
        <#if clazz.clazzType == "PUBLIC">
            <#--行政班-->
            <!--w-base template-->
            <div class="class-module mt-30 reset-class-module">
                <div class="module-head bg-f6 clearfix">
                    <div class="title">${clazz.clazzName!}</div>
                    <#--高中行政班展示文理科（不分文理科和老数据不展示）-->
                    <#if (currentTeacherDetail.isSeniorTeacher())!false>
                    <div class="title title-tag">
                        <#if (((clazz.artScienceType)!"") == "ART")>文科</#if>
                        <#if (((clazz.artScienceType)!"") == "SCIENCE")>理科</#if>
                    </div>
                    <#--<select class="v-changeArtScience" style="margin: 0 20px;" data-clazzId="${clazz.clazzId!}">
                        <option value="UNKNOWN" <#if (((clazz.artScienceType)!"") == "" || ((clazz.artScienceType)!"") == "UNKNOWN")>selected="true"</#if>>未选择</option>
                        <option value="SCIENCE" <#if (((clazz.artScienceType)!"") == "SCIENCE")>selected="true"</#if>>理科</option>
                        <option value="ART"<#if (((clazz.artScienceType)!"") == "ART")>selected="true"</#if>>文科</option>
                        <option value="ARTSCIENCE"<#if (((clazz.artScienceType)!"") == "ARTSCIENCE")>selected="true"</#if>>不分文理</option>
                    </select>
                    <div class="global-ques center">
                        <div class="text">在组卷出题时，根据班级文理科，为您推荐更准确的题目</div>
                    </div>-->
                    </#if>
                    <#--<#if sendApplications?has_content-->
                    <#--&& sendApplications[clazz.clazzId?string]?has_content-->
                    <#--&& (sendApplications[clazz.clazzId?string]["LINK"]?has_content-->
                    <#--|| sendApplications[clazz.clazzId?string]["REPLACE"]?has_content) >-->
                        <#--<div class="w-base-ext">-->
                            <#--<span class="w-bast-ctn">-->
                                <#--<i class="w-specialFeel-icon w-magR-10"></i>需请对方通过申请-->
                                <#--<a href="http://dwz.cn/1G48fj" target="_blank" class="w-blue">对方没响应？点这里</a>-->
                            <#--</span>-->
                        <#--</div>-->
                    <#--</#if>-->
                </div>
                <div class="transfer-panel">
                </div>
                <div class="link-panel" data-clazzName="${clazz.clazzName!}" data-clazzId="${clazz.clazzId!}">
                </div>
                <div class="module-info clearfix">
                    <!--//start-->
                    <div class="info">
                        <div class="title">班级学生：${clazz.studentCount!0}人</div>
                        <a class="btn v-cm-main" href="#/teacher/clazz/clazzsdetail.vpage?clazzId=${clazz.clazzId!}">学生详情</a>
                    </div>
                    <div class="flexslider js-teacherCard_list">
                        <ul class="slides teacher-list">
                        <#list clazz.teachers?keys as subject>
                            <#if clazz.teachers[subject]?has_content >
                                <#assign teacher = clazz.teachers[subject]>
                                <li class="teacher">
                                    <div class="face">
                                        <img src="<@app.avatar href='${teacher.imageUrl!}'/>" >
                                        <#if subject == curSubject!''><i class="state w-icon-arrow w-icon-greenInfo"></i></#if>
                                    </div>
                                    <div class="name" data-teacherSubject="${teacher.subject!}" data-teacherName="${teacher.teacherName!}">${teacher.subject.value!}老师：${teacher.teacherName!}</div>
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
                                                    <a class="add btn-transfer-exist"
                                                       data-clazzName="${clazz.clazzName!}"
                                                       data-clazzId="${clazz.clazzId!}" data-subject="${curSubject!}">转让班级</a>
                                                </#if>
                                            </#if>
                                        <#else>
                                            <#if !isFakeTeacher!false>
                                                <a class="add btn-transfer-exist"
                                                   data-clazzName="${clazz.clazzName!}"
                                                   data-clazzId="${clazz.clazzId!}" data-subject="${curSubject!}">转让班级</a>
                                            </#if>
                                        </#if>
                                    </#if>
                                </li>
                            <#else>
                                <li class="teacher">
                                    <div class="face">
                                        <img src="<@app.avatar href=''/>" >
                                    </div>
                                    <div class="name">
                                        <#if subject?has_content>
                                        ${validSubjects[subject]!}老师：无
                                        </#if>
                                    </div>
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
                                        <a class="add btn-checkapp" data-subject="${subject!}">已发送添加老师请求</a>
                                    <#else>
                                        <a class="add v-linkteacher" data-subject="${subject!}">添加老师</a>
                                    </#if>
                                </li>
                            </#if>
                        </#list>
                        </ul>
                    </div>
                    <!--end//-->
                </div>
            <#--TODO 与之前重复，可否优化？v-checkapp-->
                <@sendApplicationsList sendApplications=sendApplications clazz=clazz/>
            </div>
        <#else>
            <!--教学班-->
            <div class="class-module mt-30 reset-class-module">
                <div class="module-head bg-f6 clearfix ">
                    <div class="title">${clazz.clazzName!}</div>
                    <#--教学班展示分层信息（不分层或老数据不展示）-->
                    <div class="title title-tag">
                        <#if ((clazz.stageType!'') != 'UNKNOWN') && ((clazz.stageType!'') != '')>${clazz.stageType!}层</#if>
                    </div>
                    <#--<select class="v-changeArtScience" style="margin: 0 20px" data-clazzId="${clazz.clazzId!}">-->
                        <#--<option value="UNKNOWN" <#if (((clazz.artScienceType)!"") == "" || ((clazz.artScienceType)!"") == "UNKNOWN")>selected="true"</#if>>未选择</option>-->
                        <#--<option value="SCIENCE" <#if (((clazz.artScienceType)!"") == "SCIENCE")>selected="true"</#if>>理科</option>-->
                        <#--<option value="ART"<#if (((clazz.artScienceType)!"") == "ART")>selected="true"</#if>>文科</option>-->
                        <#--<option value="ARTSCIENCE"<#if (((clazz.artScienceType)!"") == "ARTSCIENCE")>selected="true"</#if>>不分文理</option>-->
                    <#--</select>-->
                    <#--<div class="global-ques center">-->
                        <#--<div class="text">在组卷出题时，根据班级文理科，为您推荐更准确的题目</div>-->
                    <#--</div>-->
                </div>
                <div class="transfer-panel">
                </div>
                <div class="module-info clearfix" data-clazzName="${clazz.clazzName!}" data-clazzId="${clazz.clazzId!}">

                    <div class="link-panel">
                    </div>
                    <!--//start-->
                    <div class="info">
                        <div class="title">班级学生：${clazz.studentCount!0}人</div>
                        <a class="btn v-cm-main" href="#/teacher/clazz/clazzsdetail.vpage?clazzId=${clazz.clazzId!}">学生详情</a>
                    </div>

                    <div class="flexslider js-teacherCard_list">
                        <ul class="slides teacher-list">
                            <#list clazz.teachers?keys as subject>
                                <#if clazz.teachers[subject]?has_content >
                                    <#assign teacher = clazz.teachers[subject]>
                                    <li class="teacher">
                                        <div class="face">
                                            <img src="<@app.avatar href='${teacher.imageUrl!}'/>" >
                                        </div>
                                        <#if subject == curSubject!''><i
                                                class="state w-icon-arrow w-icon-greenInfo"></i></#if>
                                        <div class="name">${teacher.subject.value!}老师：${teacher.teacherName!}</div>
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
                                                        <a class="add btn-transfer-exist"
                                                           data-clazzName="${clazz.clazzName!}"
                                                           data-clazzId="${clazz.clazzId!}" data-subject="${curSubject!}">转让班级</a>
                                                    </#if>
                                                </#if>
                                            <#else>
                                                <#if !isFakeTeacher!false>
                                                    <a class="add btn-transfer-exist"
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
                </div>
            </div>
        </#if>
    </#list>
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
            var $checkApp = $this.parents(".module-info").siblings(".v-checkapp");
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

        $(".v-changeArtScience").on("change", function () {
            var $this = $(this);

            var type = $this.val();
            var clazzId = $this.attr("data-clazzId");
            if (type == "UNKNOWN") return false;
            $.post('/teacher/clazz/kuailexue/changeartscience.vpage', {clazzId: clazzId, artScienceType: type}, function(data){
                if(data.success){
                    $17.alert("修改班级文理科成功！");
                }else{
                    $17.alert(data.info);
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
                        minItems : 4,
                        maxItems : 4,
                        move : 4,
                        directionNav: true,
                        controlNav: true
                    });
                    if($(this).find('.teacher-list>li').length <= 4){
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
                                <td>申请代替${application.respondentName}老师在这个班教${application.respondentSubject.value}</td>
                                <td>${application.clazzName!}</td>
                                <td>等待对方同意</td>
                                <td>
                                    <a class="cancel-replaceapp" data-recordId="${application.id}" data-applicantSubject="${application.applicantSubject!}"
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
                            <td>请${application.respondentName}老师在这个班担任${application.respondentSubject.value}老师</td>
                            <td>${clazz.clazzName!}</td>
                            <td>等待对方同意</td>
                            <td>
                                <a class="cancel-transferapp" data-recordId="${application.id}" data-applicantSubject="${application.applicantSubject!}"
                                   href="javascript:void (0);">取消</a>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                    <#if sendApplications[clazz.clazzId?string]["LINK"]?has_content>
                        <#list sendApplications[clazz.clazzId?string]["LINK"] as application>
                        <tr>
                            <td>申请在此班教${application.applicantSubject.value!}，与${application.respondentName}老师一起教此班</td>
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
                            <td>申请代替${application.respondentName}老师在这个班教${application.respondentSubject.value}</td>
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
</#macro>