<#--转让班级模板-->
<script type="text/html" id="T:转给新老师">
    <div id="transfer-panel" class="w-base-container">
        <div class="teacherListBox" style="">
            <div class="v-transfer-result">
                <div class="newSemesterChange-box">
                    <div class="nsc-con">
                        <%if(authTeachers.length > 0 || unauthTeachers.length > 0){%>
                            <dl class="allTeacherList">
                                <dt>选择同校<%=transferSubjectText%>老师：</dt>
                                <%if(authTeachers.length > 0){%>
                                <dd>
                                    <%for(var i = 0, list = authTeachers; i < list.length; i++){%>
                                    <p data-teacher-name="<%=list[i].name%>" data-teacher-id="<%=list[i].id%>"
                                       class="v-selectTeacher"><span class="w-radio"></span><%=list[i].name%>（<%=list[i].id%>）老师
                                        <i class="w-icon-public w-icon-authVip"></i></p>
                                    <%}%>
                                </dd>
                                <%}%>
                                <dd class="pl">没找到？<a class="w-blue v-moreNoAuthTeacher" href="javascript:void (0);">查看全部</a></dd>
                                <%if(unauthTeachers.length > 0){%>
                                    <dd class="unauthTeachers" <%if(authTeachers.length > 0){%>style="display: none;"<%}%> >
                                        <%for(var i = 0, list = unauthTeachers; i < list.length; i++){%>
                                        <p data-teacher-name="<%=list[i].name%>" data-teacher-id="<%=list[i].id%>"
                                           class="v-selectTeacher"><span class="w-radio"></span><%=list[i].name%>（<%=list[i].id%>）老师
                                        </p>
                                        <%}%>
                                    </dd>
                                <%}%>
                            </dl>
                        <%}else{%>
                            <div style="text-align: center; padding: 20px 75px 0 0;">没找到同校老师</div>
                        <%}%>
                    </div>
                    <div class="nsc-btn w-magT-10">
                        <a class="w-btn w-btn-well w-circular-5 w-btn-cyan w-border-cyan v-cancelTransfer"
                           href="javascript:void (0);">取消</a>
                        <a class="w-btn w-btn-well w-circular-5 w-border-blue v-existTeacher" data-subject="<%=transferSubject%>"
                           href="javascript:void (0);">转给Ta</a>
                    </div>
                </div>
            </div>
            <div class="newSemesterYellow-box teacherAutoRollOutClazz" style="border-bottom: 1px solid #dfdfdf;">
                <dl>
                    <dd>
                        <p class="nsy">填写新老师信息，自动帮您处理<span class="gray">（姓名、手机至少填写一项）：</span></p>

                        <p class="nsy">
                            <span>新老师姓名：<input style="width: 120px;" class="w-int newTeacherName" type="text"></span>
                            <span style="padding-left: 20px;">手机号：<input style="width: 120px;"
                                                                         class="w-int newTeacherMobile"
                                                                         type="text"></span>
                        </p>
                    </dd>
                    <dt style="margin-right: 60px; *display: inline;"><a
                            class="w-btn w-btn-orange w-btn-well w-circular-5 w-border-orange v-notExistTeacher" data-subject="<%=transferSubject%>"
                            href="javascript:void (0);">转给Ta</a></dt>
                </dl>
            </div>
        </div>
    </div>
</script>

<#--转让班级搜索结果模板-->
<script type="text/html" id="T:转让班级老师搜索结果">
    <div class="newSemesterTeacher-box newSemesterChange-box">
        <div class="cus-con allTeacherList">
            <%for(var i = 0, list = teacherList; i < list.length; i++){%>
                <span data-teacher-id="<%=list[i].id%>" data-teacher-name="<%=list[i].profile.realname%>"
                      class="actor v-selectTeacher">
                        <i class="icon-s-card">
                            <%if(list[i].profile.imgUrl == ""){%>
                            <img width="80" height="80" src="<@app.avatar href=''/>">
                            <%}else{%>
                            <img width="80" height="80" src="<@app.avatar href='<%=list[i].profile.imgUrl%>'/>">
                            <%}%>
                        </i>
                <strong>
                    <i class="w-radio"></i>
                    <i><%=list[i].profile.realname%>（<%=list[i].id%>）</i>
                    <%if(list[i].authenticationState == 1){%>
                    <i class="w-icon-public w-icon-authVip"></i>
                    <%}%>
                </strong>
                </span>
            <%}%>
        </div>
        <div class="nsc-btn">
            <a class="w-btn w-btn-well w-circular-5 w-btn-cyan w-border-cyan v-cancelTransfer"
               href="javascript:void (0);">取消</a>
            <a class="w-btn w-btn-well w-circular-5 w-border-blue v-existTeacher" href="javascript:void (0);" data-subject="">转给Ta</a>
        </div>
    </div>
</script>

<script type="text/javascript">
    ////////////////////////////////////////////转让班级面板功能/////////////////////////////////////////////////////

    $(function () {

        var currentSelect = {
            teacherName: null,
            teacherId: null
        };

        var transferPanel = $(".transfer-panel");

        var clazzId = null;
        var clazzName = null;

        // 已删除班级转给新老师
        $('.btn-transfer-delete').on('click', function () {
            var $this = $(this);

            // 转让老师面板
            var $panel = $this.parent().siblings(".transfer-panel");

            if ($panel.hasClass("dis")) {
                $panel.removeClass("dis");
                $panel.empty();
                return;
            }
            $panel.addClass("dis");

            var subject = $this.attr("data-subject");
            var subjectText = $this.attr("data-subjectText");

            // 这里的学科是所退出班级对应学科
            $.get('/teacher/systemclazz/teacherlist.vpage', {subject: subject}, function (data) {
                if (data.success) {
                    $panel.empty().html(template("T:转给新老师", {
                        authTeachers: data.authTeachers,
                        unauthTeachers: data.unauthTeachers,
                        transferSubject: subject,
                        transferSubjectText: subjectText
                    })).show();

                    clazzId = $this.attr("data-clazzId");
                    clazzName = $this.attr("data-clazzName");
                }
            });
        });

        // 退出班级再加回原班级
        $('.btn-joinback').on('click', function () {
            var $this = $(this);
            var clazzName = $this.attr("data-clazzName");
            var subject = $this.attr("data-subject");

            $.prompt("<div class='w-ag-center'>您确定继续教“" + clazzName + "”吗？<br>确定后将加回原来的班级学生</div>", {
                focus: 1,
                title: "系统提示",
                buttons: {"取消": false, "确定": true},
                position: {width: 500},
                submit: function (e, v) {
                    if (v) {
                        $.post('/teacher/systemclazz/joinbackclazz.vpage', {clazzId: $this.attr("data-clazzId"), subject: subject}, function (data) {
                            if (data.success) {
                                $17.alert("加入成功", function() {
                                    location.reload();
                                });
                            } else {
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        // 班级管理中直接转让
        $('.btn-transfer-exist').on('click', function () {
            var $this = $(this);

            var $origPanel = $this.parents(".t-class-manage");
            $origPanel.hide();

            var $panel = $origPanel.siblings(".transfer-panel")

            var subject = $this.attr("data-subject");

            // 这里的学科是老师对应学科
            $.get('/teacher/systemclazz/teacherlist.vpage', {subject: subject}, function (data) {
                if (data.success) {

                    $panel.empty().html(template("T:转给新老师", {
                        authTeachers: data.authTeachers,
                        unauthTeachers: data.unauthTeachers,
                        transferSubject: subject,
                        transferSubjectText: "${curSubjectText!}"
                    })).show();

                    clazzId = $this.attr("data-clazzId");
                    clazzName = $this.attr("data-clazzName");
                }
            });
        });

        // 取消
        transferPanel.on("click", ".v-cancelTransfer", function () {
            var $parent = $(this).parents(".transfer-panel");
            console.log($parent);
            $parent.removeClass("dis");
            $parent.empty().show();

            $parent.siblings(".t-class-manage").show();

            return;
        });

        // 选择老师
        transferPanel.on("click", ".v-selectTeacher", function () {
            var $this = $(this);

            currentSelect.teacherName = $this.attr("data-teacher-name");
            currentSelect.teacherId = $this.attr("data-teacher-id");
            $this.parents(".allTeacherList").find(".v-selectTeacher").removeClass("w-radio-current");
            $this.addClass("w-radio-current");
        });

        // 查看更多老师
        transferPanel.on("click", ".v-moreNoAuthTeacher", function () {
            var $this = $(this);

            $this.parents("dd").hide();
            $this.parents("dd").siblings(".unauthTeachers").show();
        });

        //知道了
        transferPanel.on("click", ".v-closePopupBox", function () {
            location.reload();
        });

        //转给Ta本校已有老师
        transferPanel.on("click", ".v-existTeacher", function () {
            if ($17.isBlank(currentSelect.teacherId)) {
                $17.alert("请选择老师");
                return false;
            }

            var $this = $(this);
            var subject = $this.attr("data-subject");

            $.prompt("<div class='w-ag-center'>您确定将“" + clazzName + "”转给“" + currentSelect.teacherName + "”老师吗？</div>", {
                focus: 1,
                title: "系统提示",
                buttons: {"取消": false, "确定": true},
                position: {width: 500},
                submit: function (e, v) {
                    if (v) {
                        $.get("/teacher/systemclazz/sendtransferapp.vpage", {
                            clazzId: clazzId,
                            respondentId: currentSelect.teacherId,
                            subject: subject
                        }, function (data) {
                            if (data.success) {
                                location.reload();
                            } else {
                                $17.alert(data.info);
                            }
                        });
                    }
                }
            });
        });

        //转给Ta（不想找）
        transferPanel.on("click", ".v-notExistTeacher", function () {
            var $this = $(this);
            var newTeacherMobile = $this.parents("dl").find(".newTeacherMobile");
            var newTeacherName = $this.parents("dl").find(".newTeacherName");

            if ($this.hasClass("dis")) {
                return false;
            }

            if ($17.isBlank(newTeacherMobile.val()) && $17.isBlank(newTeacherName.val())) {
                newTeacherMobile.addClass("w-int-error");
                newTeacherName.addClass("w-int-error");
                return false;
            }

            newTeacherName.removeClass("w-int-error");
            newTeacherMobile.removeClass("w-int-error");

            if (!$17.isBlank(newTeacherMobile.val()) && !$17.isMobile(newTeacherMobile.val())) {
                newTeacherMobile.addClass("w-int-error");
                return false;
            }

            if (!$17.isBlank(newTeacherName.val()) && !$17.isValidCnName(newTeacherName.val())) {
//            if (!$17.isBlank(newTeacherName.val()) && !$17.isCnString(newTeacherName.val())) {
                newTeacherName.addClass("w-int-error");
                return false;
            }

            var $postData = {
                clazzId: clazzId
            };

            if (!$17.isBlank(newTeacherName.val())) {
                $postData.name = $.trim(newTeacherName.val());
            }

            if (!$17.isBlank(newTeacherMobile.val())) {
                $postData.mobile = newTeacherMobile.val();
            }

            $postData.targetSubject = $this.attr("data-subject");

            $this.addClass("dis");

            $.post("/teacher/systemclazz/findlinkteacher.vpage", $postData, function (data) {
                var $parentTeacherList = $this.parents(".teacherListBox");
                if (data.success) {
                    currentSelect.teacherName = null;
                    currentSelect.teacherId = null;
                    $parentTeacherList.find(".v-transfer-result").empty().html(template("T:转让班级老师搜索结果", {teacherList: data.teachers}));
                    $parentTeacherList.children(".teacherAutoRollOutClazz").hide();
                } else {
                    if (data.type == "INVITE_TEACHER") {// 手机号注册
                        $.prompt("<div style='text-align: center; font-size: 22px;'>该老师还未注册！</div>", {
                            title: "系统提示",
                            //buttons: {"取消": false, "邀请Ta": true},
                            buttons: {"知道了": false},
                            submit: function (e, v) {
                                if (v) {
                                    $.post("/teacher/systemclazz/invitetransferteacher.vpage", $postData, function (data) {
                                        if (data.success) {
                                            $17.alert("系统已帮你向此老师发起邀请，请当面提醒Ta登录使用哦！", function () {
                                                location.reload();
                                            });
                                        } else {
                                            $17.alert("发送邀请失败！" + data.info);
                                        }
                                    });
                                }
                            }
                        });
                    } else if (data.type == "NO_TEACHER_FOUND") {
                        $.prompt("<div style='text-align: center; font-size: 22px;'>该老师还未注册！</div>", {
                            title: "系统提示",
                            //buttons: {"取消": false, "邀请Ta": true},
                            buttons: {"知道了": false},
                            submit: function (e, v) {
                                if (v) {
                                    window.location = "${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/invite/index.vpage";
                                }
                            }
                        });
                    } else {
                        $17.alert("<div style='text-align: center'>没有找到符合条件的老师！</div>", function () {

                        });
                    }
                }
                $this.removeClass("dis");
            });
        });
    });
</script>