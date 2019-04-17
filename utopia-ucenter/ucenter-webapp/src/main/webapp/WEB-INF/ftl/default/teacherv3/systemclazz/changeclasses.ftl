<script type="text/html" id="T:大使协助换班功能">
    <style>
        form{ padding: 0; margin: 0;}
        .big-module-auth{ overflow: hidden; overflow-y: auto; height: 230px;}
        .big-module .w-bast-ctn{ white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; width: 370px;}
    </style>
<%if(item.length > 0){%>
    <#--待处理的收到的请求-->
    <div class="w-base w-base-red" style="border-width: 0; margin: -40px -20px -20px; position: relative;">
        <#--//start-->
        <div class="big-module <%if(item.length > 5){%>big-module-auth<%}%>">
            <%var subjectMe = { "CHINESE" : "语文", "ENGLISH" : "英语", "MATH" : "数学" }%>
            <%for(var i = 0; i < item.length; i++){%>
            <div class="w-base-title applicantBoxContent-box" data-id="<%=item[i].id%>">
                <h3 title="<%=item[i].clazzName%>" style="width: 130px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; line-height: 120%;"><%=item[i].clazzName%></h3>
                <div class="w-base-ext">
                    <#--首页-->
                    <%if(opt.type == "home"){%>
                        <%if(item[i].type == "LINK" ){%><#--申请加入-->
                        <span class="w-bast-ctn"><i
                                class="w-specialFeel-icon w-magR-10"></i>
                                <%=subjectMe[item[i].applicantSubject]%>老师<%=item[i].applicantName%>申请和你一起教<%=item[i].clazzName%>的学生</span>
                        <%}else if(item[i].type == "TRANSFER"){%><#--转让-->
                        <span class="w-bast-ctn"><i
                                class="w-specialFeel-icon w-magR-10"></i>
                                <%=subjectMe[item[i].applicantSubject]%>老师<%=item[i].applicantName%>请你在<%=item[i].clazzName%>担任<%=subjectMe[item[i].respondentSubject]%>老师</span>
                        <%}else if(item[i].type == "REPLACE"){%><#--接管-->
                        <span class="w-bast-ctn"><i
                                class="w-specialFeel-icon w-magR-10"></i>
                                <%=subjectMe[item[i].applicantSubject]%>老师<%=item[i].applicantName%>申请代替你在<%=item[i].clazzName%>教<%=subjectMe[item[i].respondentSubject]%></span>
                        <%}%>
                    <%}%>

                    <#--大使-->
                    <%if(opt.type == "amb"){%>
                        <%if(item[i].type == "LINK" ){%><#--申请加入-->
                        <span class="w-bast-ctn"><i
                                class="w-specialFeel-icon w-magR-10"></i><%=item[i].applicantName%>
                                老师申请加入 <%=item[i].respondentName%> 老师的这个班级</span>
                        <%}else if(item[i].type == "TRANSFER"){%><#--转让-->
                        <span class="w-bast-ctn"><i
                                class="w-specialFeel-icon w-magR-10"></i><%=item[i].applicantName%>
                                老师申请转让此班级给 <%=item[i].respondentName%> 老师</span>
                        <%}else if(item[i].type == "REPLACE"){%><#--接管-->
                        <span class="w-bast-ctn"><i
                                class="w-specialFeel-icon w-magR-10"></i><%=item[i].applicantName%>
                                老师申请接管  <%=item[i].respondentName%>  老师的这个班</span>
                        <%}%>
                    <%}%>
                </div>
                <div class="w-base-right">
                    <a style="width: 60px;" class="w-btn w-btn-mini w-btn-green btn-title-reject" data-index="<%=i%>" href="javascript:void (0);">拒绝</a>
                    <a style="width: 60px;" class="w-btn w-btn-mini btn-title-approve" data-index="<%=i%>" href="javascript:void (0);">允许</a>
                </div>
            </div>
            <%}%>
        </div>
        <#--end//-->
    </div>
<%}else{%>
    <div style="font-size: 16px; text-align: center; padding: 30px;">
        暂时没有同校老师换班请求！
    </div>
<%}%>
</script>
<script type="text/javascript">
    //设置值
    var $itemDataCapl = ${(data.capl)!"[]"};//初始数据
    var systemClazzObj = {
        page : "",
        applicantName : "",
        respondentName : "",
        clazzName : "",
        applicantId : "",
        respondentId : "",
        id : "",
        type : "",
        subjectPopup : "home",
        titlePopup : "待处理请求",
        applicantSubject : "",
        respondentSubject : ""
    };
    var subjectMe = { "CHINESE" : "语文", "ENGLISH" : "英语", "MATH" : "数学" };

    $(function(){
        //点击大使协助换班
        $(document).on("click", ".v-clickAmbChangePopup", function(){
            systemClazzObj.subjectPopup = "amb";

            $.post("/ambassador/ambgpapps.vpage ", {}, function(data){
                if(data.success){
                    $itemDataCapl = data.capl;
                    if($itemDataCapl.length > 0){
                        $.prompt(changeClassesPopup($itemDataCapl, {
                            type : systemClazzObj.subjectPopup,
                            title : systemClazzObj.titlePopup
                        }));
                    }else{
                        $17.alert("暂无换班老师！");
                    }
                }else{
                    //请求失败
                }
            });
        });

        // 班级标题行拒绝
        $(document).on("click", ".btn-title-reject", function () {
            var $this = $(this);
            var $index = $this.attr("data-index");
            var message = "";
            systemClazzObj.applicantName = $itemDataCapl[$index].applicantName;
            systemClazzObj.respondentName = $itemDataCapl[$index].respondentName;
            systemClazzObj.clazzName = $itemDataCapl[$index].clazzName;
            systemClazzObj.applicantId = $itemDataCapl[$index].applicantId;
            systemClazzObj.respondentId = $itemDataCapl[$index].respondentId;
            systemClazzObj.id = $itemDataCapl[$index].id;
            systemClazzObj.type = $itemDataCapl[$index].type;
            systemClazzObj.applicantSubject = $itemDataCapl[$index].applicantSubject;
            systemClazzObj.respondentSubject = $itemDataCapl[$index].respondentSubject;

            console.log($itemDataCapl[$index]);

            switch (systemClazzObj.type) {
                case "LINK":
                    message = '拒绝和对方一起教这个班？';
                    systemClazzObj.page = "rejectlinkapp.vpage";
                    break;
                case "TRANSFER":
                    message = '拒绝在这个班担任' + subjectMe[systemClazzObj.applicantSubject] + '老师？';
                    systemClazzObj.page = "rejecttransferapp.vpage";
                    break;
                case "REPLACE":
                    message = '拒绝对方代替你教这个班？';
                    systemClazzObj.page = "rejectreplaceapp.vpage";
                    break;
            }

            if(systemClazzObj.subjectPopup == 'amb'){
                $.prompt.goToState('state2',function(){
                    $(".v-systemClazzContent").html("拒绝 <span style='color: #f00;'>"+systemClazzObj.applicantName+"</span> 老师的请求？如果 "+systemClazzObj.applicantName+" 是虚假老师，请举报。");
                });
            }else{
                $.prompt.goToState('state4',function(){
                    $(".v-systemRejectContent").html(message);
                });
                //systemClazzPage();
            }
        });

        // 班级标题行同意
        $(document).on("click", ".btn-title-approve", function () {
            var $this = $(this);
            var $index = $this.attr("data-index");
            var message = "允许此申请，班级、学生将出现变动。确定？";

            systemClazzObj.applicantName = $itemDataCapl[$index].applicantName;
            systemClazzObj.respondentName = $itemDataCapl[$index].respondentName;
            systemClazzObj.clazzName = $itemDataCapl[$index].clazzName;
            systemClazzObj.applicantId = $itemDataCapl[$index].applicantId;
            systemClazzObj.respondentId = $itemDataCapl[$index].respondentId;
            systemClazzObj.id = $itemDataCapl[$index].id;
            systemClazzObj.type = $itemDataCapl[$index].type;
            systemClazzObj.applicantSubject = $itemDataCapl[$index].applicantSubject;
            systemClazzObj.respondentSubject = $itemDataCapl[$index].respondentSubject;

            switch (systemClazzObj.type) {
                case "LINK":
                    var applicantSubject = $this.attr("data-applicantSubject");
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
                    systemClazzObj.page = "approvelinkapp.vpage";
                    break;
                case "TRANSFER":
                    message = "允许后，你将在这个班担任" + subjectMe[systemClazzObj.applicantSubject] + "老师。确定？";
                    systemClazzObj.page = "approvetransferapp.vpage";
                    break;
                case "REPLACE":
                    message = "允许后，你将不再担任该班" + subjectMe[systemClazzObj.applicantSubject] + "老师。确定？";
                    systemClazzObj.page = "approvereplaceapp.vpage";
                    break;
            }

            $.prompt.goToState('state3',function(){
                if(systemClazzObj.subjectPopup == 'amb'){
                    if(systemClazzObj.type == "LINK"){
                        message = "允许此请求， <span style='color: #f00;'>"+systemClazzObj.applicantName+"</span> 老师将和 "+systemClazzObj.respondentName+" 一起教此班"+systemClazzObj.clazzName+"。";
                    }
                    if(systemClazzObj.type == "TRANSFER"){
                        message = "允许此请求， <span style='color: #f00;'>"+systemClazzObj.respondentName+"</span> 老师将开始教 "+systemClazzObj.clazzName+"。"+systemClazzObj.applicantName+" 老师将离开。";
                    }
                    if(systemClazzObj.type == "REPLACE"){
                        message = "允许此请求， <span style='color: #f00;'>"+systemClazzObj.applicantName+"</span> 老师将开始教 "+systemClazzObj.clazzName+"。"+systemClazzObj.respondentName+" 老师将离开。";
                    }
                    $(".v-systemAllowContent").html(message);
                }else{
                    $(".v-systemAllowContent").html(message);
                }
            });
        });
    });

    //拒绝
    function systemClazzPage(canFlag){
        var _tempData = {
            recordId: systemClazzObj.id,
            subject: systemClazzObj.respondentSubject
        };

        if(systemClazzObj.subjectPopup == 'amb'){
            _tempData.respondentId = systemClazzObj.respondentId;
        }

        $.get("/teacher/systemclazz/" + systemClazzObj.page, _tempData, function (data) {
            if (data.success) {
                $.prompt.goToState('stateInfo', function(){
                    $(".v-stateContent").html("已拒绝该请求");
                    $(".applicantBoxContent-box[data-id='"+ systemClazzObj.id +"']").remove();
                });
            } else {
                $.prompt.goToState('stateInfo', function(){
                    $(".v-stateContent").html(data.info);
                });
            }
        });

        if(canFlag){
            $.post("/teacher/invite/reportTeacher.vpage", {
                type : 1,
                teacherId : systemClazzObj.applicantId,
                teacherName : systemClazzObj.applicantName,
                reason : "不是真实老师"
            }, function(data){});
        }
    }

    //允许
    function systemAllowPage(){
        var _tempData = {
            recordId : systemClazzObj.id,
            subject : systemClazzObj.respondentSubject
        };

        if(systemClazzObj.subjectPopup == 'amb'){
            _tempData.respondentId = systemClazzObj.respondentId;
        }

        $.get("/teacher/systemclazz/" + systemClazzObj.page, _tempData, function (data) {
            if (data.success) {
                $.prompt.goToState('stateInfo', function(){
                    $(".v-stateContent").html("已同意该请求");
                    $(".applicantBoxContent-box[data-id='"+ systemClazzObj.id +"']").remove();
                });
            } else {
                $.prompt.goToState('stateInfo', function(){
                    $(".v-stateContent").html(data.info);
                });
            }
        });
    }

    //setPopup
    function changeClassesPopup(item, opt, callback){
        return {
            state0 : {
                title: opt.title,
                html : template("T:大使协助换班功能", { item : item, opt : opt }),
                position : {width:720},
                buttons: {},
                loaded : function(){
                    if(callback){callback()}
                }
            },
            stateInfo : {
                title: "系统提示",
                html : "<div class='v-stateContent' style='text-align: center; font-size: 14px;'></div>",
                buttons: { "确定" : true },
                focus: 1,
                submit:function(e,v){
                    if($(".applicantBoxContent-box").length < 1){
                        $.prompt.close();
                        return false;
                    }
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state2 : {
                title: "系统提示",
                html : "<div class='v-systemClazzContent' style='text-align: center; font-size: 14px;'></div>",
                buttons: { "拒绝并举报" : false, "直接拒绝" : true },
                focus: 1,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        systemClazzPage();
                    }else{
                        systemClazzPage(true);
                    }
                }
            },
            state3 : {
                title: "系统提示",
                html : "<div class='v-systemAllowContent' style='text-align: center; font-size: 14px;'></div>",
                buttons: { "取消" : false, "确定" : true },
                focus: 1,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        systemAllowPage();
                    }else{
                        $.prompt.goToState('state0');
                    }
                }
            },
            state4 : {
                title: "系统提示",
                html : "<div class='v-systemRejectContent' style='text-align: center; font-size: 14px;'></div>",
                buttons: { "取消" : false, "拒绝" : true },
                focus: 1,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        systemAllowPage();
                    }else{
                        $.prompt.goToState('state0');
                    }
                }
            }

        }
    }
</script>