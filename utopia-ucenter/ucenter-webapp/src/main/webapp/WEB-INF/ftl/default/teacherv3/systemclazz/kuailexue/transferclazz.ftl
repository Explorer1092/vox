<#--转让班级模板-->
<script type="text/html" id="T:转给新老师">
    <div class="module-transfer">
        <%if(authTeachers.length > 0 || unauthTeachers.length > 0){%>
            <div class="title">选择同校<%=transferSubjectText%>老师：</div>
            <div class="label-list clearfix">
                <%if(authTeachers.length > 0){%>
                    <%for(var i = 0, list = authTeachers; i < list.length; i++){%>
                        <a href="javascript:;" class="label v-selectTeacher" data-teacher-name="<%=list[i].name%>" data-teacher-id="<%=list[i].id%>">
                            <div class="box"><%=list[i].name%>（<%=list[i].id%>）老师<i class="icon w-icon-new-authVip"></i></div>
                        </a>
                    <%}%>
                <%}%>
            </div>
            <div class="label-list clearfix v-klxUnauthTeachers" <%if(authTeachers.length > 0){%>style="display: none;"<%}%> >
                <%if(unauthTeachers.length > 0){%>
                    <%for(var i = 0, list = unauthTeachers; i < list.length; i++){%>
                        <a href="javascript:;" class="label v-selectTeacher" data-teacher-name="<%=list[i].name%>" data-teacher-id="<%=list[i].id%>">
                            <div class="box"><%=list[i].name%>（<%=list[i].id%>）老师</div>
                        </a>
                    <%}%>
                <%}%>
            </div>
            <div class="module-foot">
                <%if(authTeachers.length > 0){%>
                    <div class="prom">没找到？<a href="javascript:;" class="v-moreNoAuthTeacher">查看全部</a></div>
                <%}%>
                <div class="box">
                    <a href="javascript:;" class="btn white v-cancelTransfer">取消</a>
                    <a href="javascript:;" class="btn v-existTeacher" data-subject="<%=transferSubject%>">转给Ta</a>
                </div>
            </div>
        <%}else{%>
        <div style="text-align: center; padding: 20px 75px 0 0;">没找到同校老师</div>
        <%}%>
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
            var $frame = $this.parent().parent().siblings(".class-module");
            var $panel = $frame.children(".transfer-panel");

            if ($panel.hasClass("dis")) {
                $panel.removeClass("dis");
                $frame.css("border","0px");
                $panel.empty();
                return;
            }
            $panel.addClass("dis");
            $frame.css("border","1px #ebebeb solid");

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
            // 判断是否是第三方，是则禁止操作
            if (isThirdParty == 'true') {
                isThirdPartyTip();
                return false;
            }

            var $this = $(this);

            var $origPanel = $this.parents(".module-info");
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
            $parent.removeClass("dis");
            $parent.parent().css("border","0px");
            $parent.empty().show();

            $parent.siblings(".module-info").show();
        });

        // 选择老师
        transferPanel.on("click", ".v-selectTeacher", function () {
            var $this = $(this);

            currentSelect.teacherName = $this.attr("data-teacher-name");
            currentSelect.teacherId = $this.attr("data-teacher-id");
            $this.parents(".label-list").find(".v-selectTeacher").removeClass("active");
            $this.addClass("active");
        });

        // 查看更多老师
        transferPanel.on("click", ".v-moreNoAuthTeacher", function () {
            var $this = $(this);

            $this.parents(".prom").hide();
            $this.parents(".module-foot").siblings(".v-klxUnauthTeachers").show();
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