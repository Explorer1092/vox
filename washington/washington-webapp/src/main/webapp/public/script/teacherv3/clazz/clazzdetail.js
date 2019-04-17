$(function(){
    function changeName(clazzId, userId, name){
        $17.tongji("教师端-学生管理-修改姓名");

        App.postJSON("/teacher/clazz/resetname.vpage", {
            clazzId : clazzId,
            userId  : userId,
            name    : name
        }, function(data){
            if(data.success){
                $.prompt("修改成功", {
                    title: "系统提示",
                    buttons: { "知道了": true },
                    submit: function(){
                        setTimeout(function(){location.reload()}, 200);
                    }
                });
            }else{
                $.prompt(data.info, {
                    title: "系统提示",
                    buttons: { "知道了": true }
                });
            }
        });
    }

    //修改名字
    $(".v-setname").on("click", function(){
        var $self = $(this);

        var Wind = {
            state0: {
                title   : "修改学生一起作业的姓名",
                html    : template("t:修改名字", {}),
                focus   : 1,
                buttons : { "取消": false, "确定": true },
                submit  : function(e, v){
                    if(v){
                        var newName = $(".v-newname").val();
                        //if($17.isBlank(newName) || !$17.isValidCnName(newName)){
                        if($17.isBlank(newName) || !$17.isCnString(newName)){
                            e.preventDefault();
                            $.prompt.goToState('state1');
                        }else if(newName.length > 6){
                            e.preventDefault();
                            $.prompt.goToState('state2');
                        }else{
                            changeName($self.attr("data-clazzid"), $self.attr("data-studentid"), $.trim(newName));
                        }
                    }
                }
            },
            state1: {
                html    : '<h4 class="text_red">请输入正确的学生姓名.</h4>',
                buttons : { "知道了": true },
                submit  : function(e, v){
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state2: {
                html    : '<h4 class="text_red">填写的学生名过长.</h4>',
                buttons : { "知道了": true },
                submit  : function(e, v){
                    e.preventDefault();
                    $.prompt.goToState("state0");
                }
            }
        };
        $.prompt(Wind);

        return false;
    });

    function changePassword(clazzId, userId, password, confirmPassword){
        $17.tongji("教师端-学生管理-修改密码");

        App.postJSON("/teacher/clazz/resetstudentpassword.vpage", {
            clazzId         : clazzId,
            userId          : userId,
            password        : password,
            confirmPassword : confirmPassword
        }, function(data){
            if(data.success){
                $.prompt("修改成功", {
                    title: "系统提示",
                    buttons: { "知道了": true },
                    submit: function(){
                        setTimeout(function(){location.reload()}, 200);
                    }
                });
            }else{
                $.prompt(data.info, {
                    title: "系统提示",
                    buttons: { "知道了": true }
                });
            }
        });
    }

    //重置密码
    $(".v-setpassword-student").on("click", function(){
        var $self = $(this);
        var bindMobile = $self.attr("data-studentbindmobile");

            if (!$17.isBlank(bindMobile)) {
                $17.alert("不能重置。学生已绑定手机，请告诉学生在首页点击“忘记学号/密码？”自行重置密码。");
            } else {
                var changePass = {
                state0: {
                    title   : "重置学生一起作业的登录密码",
                    html    : template("t:修改密码", {}),
                    focus   : 1,
                    buttons : { "取消": false, "确定": true },
                    submit  : function(e, v){
                        if(v){
                            if($17.isBlank($(".v-password").val()) || $17.isBlank($(".v-confirmPassword").val())){
                                e.preventDefault();
                                $.prompt.goToState('state1');
                            }else if($(".v-password").val() != $(".v-confirmPassword").val()){
                                e.preventDefault();
                                $(".v-password, .v-confirmPassword").val("");
                                $.prompt.goToState('state2');
                            }else{
                                changePassword($self.attr("data-clazzid"), $self.attr("data-studentid"), $(".v-confirmPassword").val(), $(".v-confirmPassword").val());
                            }
                        }
                    }
                },
                state1: {//表单未填写完
                    html    : '<h4 class="text_red">您有未输入的信息.</h4>',
                    buttons : { "知道了": true },
                    submit  : function(e, v){
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                },
                state2: {//两个密码不相同
                    html    : '<h4 class="text_red">密码不一致.</h4>',
                    buttons : { "知道了": true },
                    submit  : function(e, v){
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                }
            };
                $.prompt(changePass);
        }
        return false;
    });

    //删除学生
    $(".v-delete-student").on("click", function(){
        var $self = $(this);

        $.prompt("你确定要删除" + ($self.attr("data-studentname") || "此") + "学生吗？", {
            title   : "系统提示",
            focus   : 1,
            buttons : { "取消": false, "确定": true },
            submit  : function(e, v){
                if(v){
                    $17.tongji("教师端-学生管理-删除学生");

                    $.get("/teacher/clazz/removestudent.vpage?clazzId=" + $self.attr("data-clazzid") + "&studentId=" + $self.attr("data-studentid"), function(data){
                        if(data.success){
                            setTimeout(function(){ location.reload(); }, 200);
                        }
                    });
                }
            }
        });

        return false;
    });

    //删除学生
    $(".v-delete-studentname").on("click", function(){
        var $self = $(this);

        $.prompt("你确定要删除" + ($self.attr("data-studentname") || "此") + "学生吗？", {
            title   : "系统提示",
            focus   : 1,
            buttons : { "取消": false, "确定": true },
            submit  : function(e, v){
                if(v){
                    var postdata = {
                        clazzId: $self.attr("data-clazzid"),
                        studentName: $self.attr("data-studentname")
                    }
                    $.post("/teacher/clazz/removestudentname.vpage", postdata, function(data){
                        if(data.success){
                            setTimeout(function(){ location.reload(); }, 200);
                        } else {
                            $17.alert("删除学生失败");
                        }
                    });
                }
            }
        });

        return false;
    });

    function batchDeleteStudent(clazzId, studentcount, studentIds){
        $17.tongji("教师端-学生管理-批量删除学生");

        App.postJSON("/teacher/clazz/batchremovestudents.vpage", {
            clazzId         : clazzId,
            deleteAll       : studentIds.split(",").length == studentcount,
            studentIdList   : studentIds
        }, function(data){
            if(data.success){
                $.prompt("删除成功", {
                    title   : "系统提示",
                    buttons : { "知道了": true },
                    submit  : function(){
                        setTimeout(function(){location.reload();}, 200);
                    }
                });
            }else{
                $.prompt(data.info, {
                    title   : "系统提示",
                    buttons : { "知道了": true }
                });
            }
        });
    }

    //批量删除学生
    $(".v-batch-delete").on("click", function(){
        var teacherId = $(this).attr("data-teacherId");
        //$17.alert("该功能暂时不能使用");
        $17.voxLog({
            module : "batchdelstudent",
            op : "list",
            tid : teacherId
        });
        var $self = $(this);
        var studentIds = $(".allcheckbox").attr("data-values");

        if($17.isBlank(studentIds)){
            $17.alert("请选择要删除的学生");
            return false;
        }

        $.prompt("是否要批量删除？", {
            title   : "系统提示",
            focus   : 1,
            buttons : { "取消": false, "确定": true },
            submit  : function(e, v){
                if(v){
                    batchDeleteStudent($self.attr("data-clazzid"), $self.attr("data-studentcount"), studentIds);
                }
            }
        });
    });

    //换班下拉
    $(".v-clazz-select").on({
        click: function(){
            $(this).find("ul").show();
        },
        mouseleave: function(){
            $(this).find("ul").hide();
        }
    });

    //查看统计信息
    $("#newJoinStudents").on("click", function(){
        $17.tongji("教师端-学生管理-查看新加入学生");
        var $postData = {
            clazzId : "",
            studentIds : []
        };

        var temp = {
            state0: {
                title        : "新加入学生",
                focus : 1,
                html        : template("t:newJoinStudents", {}),
                buttons     : {"取消" : false, "批量删除" : true},
                position    : { width : 520 },
                submit : function(e,v){
                    if(v){
                        if($postData.studentIds.length < 1){
                            e.preventDefault();
                            $.prompt.goToState('state1');
                            return false;
                        }else{
                            batchDeleteStudent($postData.clazzId, 0, $postData.studentIds.join(","));
                        }
                    }
                }
            },
            state1: {
                title        : "新加入学生",
                html    : '请选择要删除的学生',
                buttons : { "知道了" : true },
                submit  : function(e){
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            }
        };

        $.prompt(temp);

        $(document).on("click", "#newJoinStudentsContents dl", function(){
            var $this = $(this);
            var studentId = $this.attr("data-student-id");

            if($this.hasClass("active")){
                $this.removeClass("active");
            }else{
                $this.addClass("active");
            }

            if($.inArray(studentId, $postData.studentIds) > -1){
                $postData.studentIds.splice($.inArray(studentId, $postData.studentIds), 1);
            }else{
                $postData.studentIds.push($this.attr("data-student-id"));
            }

            $postData.clazzId = $this.closest("#newJoinStudentsContents").attr("data-clazzid");
        });
    });
    $("#loginedFailuredStudents").on("click", function(){
        $17.tongji("教师端-学生管理-查看忘记密码学生");

        $.prompt(template("t:loginedFailuredStudents", {}), {
            title       : "忘记密码的学生",
            position    : { width : 520 },
            buttons     : { "下载学生名单" : true },
            submit: function(e, v){
                if(v){
                    $("#loginedFailuredStudentsForm").submit();
                }
            }
        });
    });
    $("#notLoginedStudents").on("click", function(){
        $17.tongji("教师端-学生管理-查看从未登录的学生");

        $.prompt(template("t:notLoginedStudents", {}), {
            title       : "从未登录的学生",
            position    : { width : 520 },
            buttons     : { "下载学生名单" : true },
            submit : function(e, v){
                if(v){
                    $("#notLoginedStudentsForm").submit();
                }
            }
        });
    });

    //添加学生
    $(".v-add-student").on("click", function(){
        $17.tongji("教师端-学生管理-添加学生");

        var addStudent = {
            state0: {
                title       : "添加学生",
                html        : template("t:添加学生", { type : $(this).attr("data-type")}),
                position    : { width: 500 },
                buttons     : {}
            },
            state1: {
                title       : "系统提示",
                html        : '<h4 class="text_red">请输入学生姓名.</h4>',
                buttons     : { "知道了": true },
                submit      : function(e, v){
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            }
        };

        $.prompt(addStudent);

        //切换上传 Tab
        $("li[data-content]").on("click", function(){
            var $that = $(this);
            $that.radioClass("active");

            $(".t-student-create").hide().find("textarea").val("");
            $(".t-student-create[data-tabnum='" + $that.attr("data-content") + "']").show();
            $("a.v-wind-submit").attr("data-contentnum", $that.attr("data-content"));

            return false;
        });

        //关闭按钮
        $(".v-wind-close").on("click", function(){
            $.prompt.close();

            return false;
        });

        $("a.v-wind-submit").die().live("click", function(){
            var $self = $(this);

            if($self.attr("data-contentnum") == 1){
                if($17.isBlank($("#batch_student_name").val())){
                    $.prompt.goToState('state1');
                    return false;
                }

                $17.tongji("教师端-学生管理-添加学生-无账号");

                var _userNames = $.trim($("#batch_student_name").val()).split('\n');
                for(var i = 0; i < _userNames.length; i++){
                    _userNames[i] = $.trim(_userNames[i]);
                    if(_userNames[i] == ''){
                        _userNames.splice(i, 1);
                    }
                }
                $.prompt.close();
                App.postJSON("/teacher/clazz/bulkregistration.vpage", {
                    clazzId     : $self.attr("data-clazzid"),
                    userNames   : _userNames
                }, function(data){
                    if(data.success){
                        $.prompt("添加成功", {
                            title   : "系统提示",
                            buttons : { "知道了": true },
                            submit  : function(){
                                setTimeout(function(){location.reload();}, 200);
                            }
                        });
                    }else{
                        $.prompt(data.info, {
                            title   : "系统提示",
                            buttons : { "知道了": true }
                        });
                    }
                });
            }else{
                if($17.isBlank($("#batch_student_number").val())){
                    $.prompt.goToState('state1');
                    return false;
                }

                $17.tongji("教师端-学生管理-添加学生-无账号");

                var _userIds = $.trim($("#batch_student_number").val()).split('\n');
                for(var i = 0; i < _userIds.length; i++){
                    _userIds[i] = $.trim(_userIds[i]);
                    if(_userIds[i] == ''){
                        _userIds.splice(i, 1);
                    }
                }
                $.prompt.close();
                App.postJSON("/teacher/clazz/classstudent.vpage", {
                    clazzId     : $self.attr("data-clazzid"),
                    studentIds  : _userIds.toString()
                }, function(data){
                    if(data.success){
                        $.prompt("添加成功", {
                            title   : "系统提示",
                            buttons : { "知道了": true },
                            submit  : function(){
                                setTimeout(function(){location.reload();}, 200);
                            }
                        });
                    }else{
                        if(data['errorType'] == 'PARTLY_FAILED'){
                            $.prompt(template("t:申请结果", {
                                data: data.result
                            }), {
                                title   : "上传结果",
                                position: { width: 600 },
                                buttons : { "知道了": true },
                                submit  : function(){
                                    setTimeout(function(){location.reload();}, 200);
                                }
                            });
                        }else{
                            $17.alert(data.info);
                        }
                    }
                });
            }
        });

        return false;
    });

    $17.modules.checkboxs("#checkboxs", ".allcheckbox", ".nodecheckbox");
});