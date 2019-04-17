$(function(){
    function changeName(clazzId, userId, name, subject){
        $17.tongji("教师端-学生管理-修改姓名");

        App.postJSON("/teacher/clazz/resetname.vpage", {
            clazzId : clazzId,
            userId  : userId,
            name    : name,
            subject : subject
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
                $17.alert(data.info ? data.info : "修改学生姓名失败！");
            }
        });
    }

    //修改名字
    $(document).on("click", '.v-setname', function(){
        var $self = $(this);
        if ($self.attr("data-usert") == "junior"){
            setInfoFun($self);
        }else{
            setNameFun($self);
        }
        return false;
    });

    function setNameFun($self){
        var Wind = {
            state0: {
                title   : "修改名字",
                html    : template("t:修改名字", {"studentName": $self.attr("data-name")}),
                focus   : 1,
                buttons : { "取消": false, "确定": true },
                submit  : function(e, v){
                    if(v){
                        var newName = $(".v-newname").val();
                        //if($17.isBlank(newName) || !$17.isValidCnName(newName)){
                        if($17.isBlank(newName) || !$17.isChinaString(newName)){
                            e.preventDefault();
                            $.prompt.goToState('state1');
                        }else if(newName.length > 16){
                            e.preventDefault();
                            $.prompt.goToState('state2');
                        }else{
                            $.post("/signup/filtersensitiveusername.vpage",{userName:$.trim(newName)},function (data) {
                                if(data.success){
                                    changeName($self.attr("data-clazzid"), $self.attr("data-studentid"), $.trim(newName), $self.attr("data-subject"));
                                }else{
                                    $.prompt("<div style='text-align: center'> 输入的姓名不合适哦,请重新输入<br/>  有疑问请联系客服</div>", {
                                        title: "系统提示",
                                        buttons: { "取消": false ,"联系客服": true },
                                        submit: function(e,v){
                                            if(v){
                                                window.open("http://www.17zuoye.com/redirector/onlinecs_new.vpage?type=teacher&question_type=question_account&origin=PC-班级管理修改名字");
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            },
            state1: {
                html    : '<h4 class="text_red" style="text-align:center;">请输入正确的学生姓名.</h4>',
                buttons : { "知道了": true },
                submit  : function(e, v){
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state2: {
                html    : '<h4 class="text_red" style="text-align:center;">填写的学生名过长.</h4>',
                buttons : { "知道了": true },
                submit  : function(e, v){
                    e.preventDefault();
                    $.prompt.goToState("state0");
                }
            }
        };
        $.prompt(Wind);
    }
    // 点击编辑学生弹窗 借读生标记(说明，对于弹窗中的事件，由于弹窗中的dom初始化时不存在，故绑定事件时通过documeng来委托绑定)
    $(document).on('click', '.JS-transientTag', function (event) {
        var $thisNode = $(event.currentTarget);
        $thisNode.hasClass('active') ? $thisNode.removeClass('active') : $thisNode.addClass('active');
    });
    function setInfoFun($self) {
        var editPass = {
            state0: {
                title: "更新" + $self.attr("data-name") + "的资料",
                html: template("t:编辑学生", {
                    "studentName": $self.attr("data-name"),
                    "studentNumber": $self.attr("data-studentnumber"),
                    "studentScanNumber": $self.attr("data-scannumber")
                }),
                focus: 1,
                buttons: {"取消": false, "确定": true},
                position: {width: 580},
                submit: function (e, v) {
                    e.preventDefault();
                    if (v) {
                        var studentName = $(".v-student-name").val();
                        var studentNumber = $(".v-student-number").val();
                        var studentScanNumber =  $(".v-student-scan-number").val();
                        var isMarked = $('.JS-transientTag').hasClass('active');
                        if ($17.isBlank(studentName) || $17.isBlank(studentNumber) || $17.isBlank(studentScanNumber)) {
                            $.prompt.goToState('state1');
                        } else if (!$17.isChinaString(studentName)) {//学生姓名是否为纯汉字，不符合则提示——请输入正确的学生姓名
                            $.prompt.goToState('state2');
                        } else if ($.trim(studentName).length > 16) {//姓名是否<=16个字符，不符合则提示——填写的学生名过长
                            $.prompt.goToState('state3');
                        } else if (!$17.isNumber(studentNumber)) {//校内学号是否为纯数字，不符合则提示——请输入纯数字学号
                            $.prompt.goToState('state4');
                        } else if (studentNumber.length > 14) {//校内学号是否<=14个数字，不符合则提示——填写的校内学号过长
                            $.prompt.goToState('state5');
                        } else if (!$17.isNumber(studentScanNumber)) {
                            $.prompt.goToState('state6');
                        } else {
                            $.post("/teacher/clazz/kuailexue/editklxstudentinfo.vpage", {
                                clazzId: $self.attr("data-clazzid"),
                                studentId: $self.attr("data-studentid"),
                                klxStudentUserName: $self.attr("data-studentusername"),
                                klxStudentName: studentName,
                                klxStudentNumber: studentNumber,
                                klxStudentScanNumber: studentScanNumber,
                                isMarked: isMarked
                            }, function (data) {
                                if (data.success) {
                                    $.prompt('<h4 class="text_red" style="text-align: center;padding: 30px 0px;">修改成功</h4>', {
                                        title: "系统提示",
                                        buttons: {"知道了": true},
                                        submit: function () {
                                            setTimeout(function () {
                                                location.reload()
                                            }, 200);
                                        }
                                    });
                                } else {
                                    $.prompt.goToState('state7', false, function () {
                                        $("#editstudenterror").text(data.info);
                                    });
                                }
                            });
                        }
                    } else {
                        $.prompt.close();
                    }
                }
            },
            state1: {//表单未填写完
                title: "系统提示",
                html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">您有未输入的信息</h4>',
                buttons: {"知道了": true},
                submit: function (e, v) {
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state2: {
                title: "系统提示",
                html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">请输入正确的学生姓名</h4>',
                buttons: {"知道了": true},
                submit: function (e, v) {
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state3: {
                title: "系统提示",
                html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">填写的学生名过长</h4>',
                buttons: {"知道了": true},
                submit: function (e, v) {
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state4: {
                title: "系统提示",
                html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">请输入纯数字学号</h4>',
                buttons: {"知道了": true},
                submit: function (e, v) {
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state5: {
                title: "系统提示",
                html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">填写的校内学号过长</h4>',
                buttons: {"知道了": true},
                submit: function (e, v) {
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state6: {
                title: "系统提示",
                html: '<h4 class="text_red" style="text-align: center;padding: 30px 0px;">请输入纯数字阅卷机号</h4>',
                buttons: {"知道了": true},
                submit: function (e, v) {
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            },
            state7: {
                title: "系统提示",
                html: '<h4 id="editstudenterror" class="text_red" style="text-align: center;padding: 30px 0px;"></h4>',
                buttons: {"知道了": true},
                submit: function (e, v) {
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            }
        };
        $.prompt(editPass);
        // 获取当前的借读生状态（是否是借读生）
        $self.attr("data-ismarked") === 'true' ? $('.JS-transientTag').addClass('active') : $('.JS-transientTag').removeClass('active');
    }

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
    $(document).on("click", '.v-setpassword-student', function(){
        // 判断是否是第三方，是则禁止操作
        if (isThirdParty == 'true') {
            isThirdPartyTip();
            return false;
        }

        var $self = $(this);
        var $data = $self.parents("tr");
        var bindMobile = $self.data("studentbindmobile");

        if (!$17.isBlank(bindMobile) && bindMobile) {
            $.prompt(template("t:绑定手机重置安全密码", {studentName: $data.attr("data-name"), mobile: $data.attr("data-mobile")}), {
                title   : "系统提示",
                focus   : 1,
                buttons : { "取消": false, "重置密码": true },
                submit  : function(e, v){
                    if(v){
                        changePassword($data.attr("data-clazzid"), $data.attr("data-id"), "", "");
                    }
                }
            });
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
                                changePassword($data.attr("data-clazzid"), $data.attr("data-id"), $(".v-confirmPassword").val(), $(".v-confirmPassword").val());
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
    $(document).on("click", '.v-delete-student', function(){
        // 判断是否是第三方，是则禁止操作
        if (isThirdParty == 'true') {
            isThirdPartyTip();
            return false;
        }

        var $self = $(this);
        var klxStudentUserName = $self.attr("data-studentusername");
        var info = "(删除后重新导入，学生学情记录将无法保留；<br/>如需更换班级请联系客服)";
        if ($17.isBlank(klxStudentUserName)) {
            info = "";
        }

        var deletePass = {
            state0: {
                title: "系统提示",
                html: "<div style='text-align: center; padding: 30px 0 10px;'>你确定要删除" + ($self.attr("data-studentname") || "此") + "学生吗？<br/>" + (info) + "</div>",
                focus: 1,
                buttons: {"取消": false, "确定": true},
                submit: function (e, v) {
                    e.preventDefault();
                    if (v) {
                        $17.tongji("教师端-学生管理-删除学生");
                        var isKlx = $self.attr("data-klx");
                        if (isKlx) {//如果是快乐学学生
                            $.post("/teacher/clazz/kuailexue/getrelatedgroupinfo.vpage?clazzId=" + $self.attr("data-clazzid") + "&studentId=" + $self.attr("data-studentid") + "&klxStudentUsername=" + $self.attr("data-studentusername"), function (data) {
                                if (data.success) {
                                    if (data.relatedTeacher && data.relatedTeacher.length > 0) {
                                        var warnInfo = "该学生关联了";
                                        for (var i = 0; i < data.relatedTeacher.length; i++) {
                                            if (i == data.relatedTeacher.length - 1) {
                                                warnInfo = warnInfo + data.relatedTeacher[i].subject + "" + data.relatedTeacher[i].teacherName;
                                            } else {
                                                warnInfo = warnInfo + data.relatedTeacher[i].subject + "" + data.relatedTeacher[i].teacherName + "、";
                                            }
                                        }
                                        warnInfo = warnInfo + "<br/>若删除该学生将影其他老师使用，确定删除吗？";
                                        $.prompt.goToState('state1', false, function () {
                                            $("#klxreleatedteacherinfo").html(warnInfo);
                                        });
                                    } else {
                                        $.get("/teacher/clazz/removestudent.vpage?clazzId=" + $self.attr("data-clazzid") + "&studentId=" + $self.attr("data-studentid") + "&klxStudentUsername=" + $self.attr("data-studentusername"), function (data) {
                                            if (data.success) {
                                                setTimeout(function () {location.reload();}, 200);
                                            } else {
                                                $17.alert(data.info != ''? data.info : "删除学生失败");
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            $.get("/teacher/clazz/removestudent.vpage?clazzId=" + $self.attr("data-clazzid") + "&studentId=" + $self.attr("data-studentid") + "&klxStudentUsername=" + $self.attr("data-studentusername"), function (data) {
                                if (data.success) {
                                    setTimeout(function () {location.reload();}, 200);
                                } else {
                                    $17.alert(data.info != ''? data.info : "删除学生失败");
                                }
                            });
                        }
                    } else {
                        $.prompt.close();
                    }
                }
            },
            state1: {
                title: "系统提示",
                html: '<div id="klxreleatedteacherinfo" class="text_red" style="text-align: center;padding: 30px 0px;"></div>',
                buttons: {"取消": false, "确定": true},
                submit: function (e, v) {
                    e.preventDefault();
                    if(v){
                        $.get("/teacher/clazz/removestudent.vpage?clazzId=" + $self.attr("data-clazzid") + "&studentId=" + $self.attr("data-studentid") + "&klxStudentUsername=" + $self.attr("data-studentusername"), function (data) {
                            if (data.success) {
                                setTimeout(function () {location.reload();}, 200);
                            } else {
                                $17.alert(data.info != ''? data.info : "删除学生失败");
                            }
                        });
                    }else{
                        $.prompt.goToState('state0');
                    }
                }
            }
        };
        $.prompt(deletePass);

        return false;
    });

    //删除学生
    $(document).on("click", '.v-delete-studentname', function(){
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

    $(".v-checkStuPhone").on("click", function() {
        var $self = $(this);
        var $data = $self.parents("tr");
        $.get("/teacher/clazz/getmobilebystuid.vpage?studentId=" + $data.attr("data-id"), function(data){
            if(data.success){
                var msg = $data.attr("data-name") + "同学已绑定的手机号是：" + data.mobile;
                $.prompt("<div class='w-ag-center'>" + msg + "</div>", { title: "学生手机号", buttons: { "知道了": true }, position: {width: 500}});
            }
        });
    });
    // ------------------------------------------------------------------------------------------
    // 快乐学批量导入注册学生账号
    $(".v-batchimportklxstudent").on("click",function () {
        // 判断是否是第三方，是则禁止操作
        if (isThirdParty == 'true') {
            isThirdPartyTip();
            return false;
        }

        // 导入之前检查
        $.post("/teacher/clazz/checkbeforeimport.vpage", {}, function(res) {
            if (res.success) {
                $.prompt(importKlxStudentPass);
            } else {
                $17.alert(res.info);
            }
        });

        var toState2_From = 0, toState3_From = 0; // 记录从哪切换到state2、state3的
        // 当前班级
        var nowClassName = '';
        for (var i = 0, len1 = LeftMenu.menuInfo.length; i < len1; i++) {
            for (var j = 0, len2 = LeftMenu.menuInfo[i].child.length; j < len2; j++) {
                if (LeftMenu.menuInfo[i].url === window.location.pathname &&
                    LeftMenu.menuInfo[i].child.length > 0 &&
                    LeftMenu.menuInfo[i].child[j].url === window.location.hash) {
                    nowClassName = LeftMenu.menuInfo[i].child[j].text;
                }
            }
        }

        // 弹窗state，默认显示第一个，此处，由于是后期增加功能，state未按照数组顺序排列，新增功能从10开始
        var importKlxStudentPass = {
            state10: {
                title: "添加学生账号",
                html: template("t:选择导入学生方式", {
                    nowClassName: nowClassName
                }),
                buttons: { }
            },
            state11: {
                title: "在线添加学生弹窗",
                html: template("t:在线添加学生弹窗", {}),
                position: {width: 640},
                focus: 1,
                buttons: {'取消': false, '确定': true },
                submit: function (e, v) {
                    e.preventDefault();
                    if (v) {
                        // window.alert('功能还在完善中。。。')

                        var onlineAddText = $('.JS-onlineText').val();
                        if (!onlineAddText) {
                            $('.JS-errorInfo').text('请输入需要添加的学生姓名和学生号');
                            return ;
                        }
                        // 第一次请求，判断是否重复
                        var onlineAddStudentData = {
                            batchContext: onlineAddText,
                            checkRepeatedStudent: true,
                            checkTakeUpStudent: false,
                            clazzId: $('.JS-onlineText').attr('data-clazzId'),
                            teacherId: $('.JS-onlineText').attr('data-teacherId')
                        };
                        $.ajax({
                            url:"/teacher/clazz/kuailexue/addstudentsonline.vpage",
                            type:"POST",
                            data: onlineAddStudentData,
                            success:function (data) {
                                if(data.success){
                                    if (data.repeatedStudentList && data.repeatedStudentList.length != 0) { // 有重复列表
                                        var repeateNames = data.repeatedStudentList.join("、");
                                        if (data.moreStudent) repeateNames += "等";
                                        $(".js-klxRepeateStudentName").html(repeateNames); // 重复弹窗文案
                                        $.prompt.goToState('state2'); // 显示重复学生弹窗
                                        toState2_From = 11;
                                    } else { // 通过第一次重复校验
                                        /*
                                        * success回调里面这样写请求代码不太优雅，对于这种的请求，应该将代码优化一下，可以将请求抽离出去，通过传参来实现不停的调用
                                        * 但此处由于先前在线结构已经是这样的，所有新增的在线添加的功能就仿照写了，代码就不再优化了
                                        **/
                                        // 开始第二次占用校验
                                        var onlineAddText = $('.JS-onlineText').val();
                                        if (!onlineAddText) {
                                            $('.JS-errorInfo').text('请输入需要添加的学生姓名和学生号');
                                            return ;
                                        }
                                        var onlineAddStudentData = {
                                            batchContext: onlineAddText,
                                            checkRepeatedStudent: false,
                                            checkTakeUpStudent: true,
                                            clazzId: $('.JS-onlineText').attr('data-clazzId'),
                                            teacherId: $('.JS-onlineText').attr('data-teacherId')
                                        };
                                        $.ajax({
                                            url: "/teacher/clazz/kuailexue/addstudentsonline.vpage",
                                            type: "POST",
                                            data: onlineAddStudentData,
                                            success: function (data) {
                                                if (data.success) {
                                                    if (data.isTakeUp) { // 存在占用
                                                        var importStudentNames = "";
                                                        var takeUpSutTeacherInfo = "";
                                                        if (data.importNames && data.importNames.length > 0) {
                                                            var names = [];
                                                            for (var l = 0; l < 3 && l < data.importNames.length; ++l) {
                                                                names.push(data.importNames[l]);
                                                            }
                                                            importStudentNames = names.join("、");
                                                            if(data.importNames.length >= 3){
                                                                importStudentNames += "等";
                                                            }
                                                        }
                                                        if (data.takeUpInfo && data.takeUpInfo.length > 0) {
                                                            for (var i = 0; i < data.takeUpInfo.length; i++) {
                                                                takeUpSutTeacherInfo = takeUpSutTeacherInfo + data.takeUpInfo[i].teacherName + " " + data.takeUpInfo[i].clazzName + " " + data.takeUpInfo[i].studentName + "<br/>"
                                                            }
                                                        }
                                                        $(".js-klxTakeUpStudentNames").html(importStudentNames);
                                                        $(".js-klxTakeUpStuTeacherInfo").html(takeUpSutTeacherInfo);
                                                        $.prompt.goToState("state3");
                                                        toState3_From = 11;
                                                    } else {
                                                        // 第二次校验通过，直接显示成功弹窗state1
                                                        $(".js-klxImportNewSignNum").html(data.newSignNum);
                                                        $(".js-klxImportUpdateNum").html(data.updateNum);
                                                        $.prompt.goToState("state1");
                                                    }
                                                } else {
                                                    $('.JS-errorInfo').text(data.info);
                                                }
                                            }
                                        });
                                    }
                                }else{
                                    $('.JS-errorInfo').text(data.info);
                                }
                            }
                        });
                    } else {
                        $.prompt.goToState("state10");
                    }
                }
            },
            state0: {
                title: '通过excel添加账号',
                html: template("t:通过excel添加账号", {}),
                focus: 1,
                buttons: {'取消': false, '确定': true},
                position: {width: 580},
                submit: function (e, v) {
                    e.preventDefault();
                    if (v) {
                        var fileInput = $('#v-fileupload');

                        var fileName = fileInput.val();
                        if (fileName.substring(fileName.length - 4) != ".xls"
                            && fileName.substring(fileName.length - 5) != ".xlsx") {
                            $("#v-errMsg").html("仅支持上传excel文档");
                            $("#v-errMsg").show();
                            return false;
                        }

                        var formData = new FormData();
                        var file = fileInput[0].files[0];
                        formData.append('adjustExcel', file);
                        formData.append('clazzId', fileInput.attr("data-clazzId"));
                        formData.append('checkRepeatedStudent',true);

                        $.ajax({
                            url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                            type: "POST",
                            data: formData,
                            processData: false,
                            contentType: false,
                            async: true,
                            timeout: 5 * 60 * 1000,
                            success: function (data) {
                                if (data.success) {
                                    if (data.repeatedStudentList && data.repeatedStudentList.length > 0) {//跳转到提示班级内有重复学生的弹窗
                                        var repeateNames = data.repeatedStudentList.join("、");
                                        if (data.moreStudent) repeateNames += "等";
                                        $(".js-klxRepeateStudentName").html(repeateNames);
                                        $.prompt.goToState("state2");
                                        toState2_From = 0;
                                    } else { //查询是否有学生被占用
                                        var newFormData = new FormData();
                                        newFormData.append('adjustExcel', file);
                                        newFormData.append('clazzId', fileInput.attr("data-clazzId"));
                                        newFormData.append('checkRepeatedStudent', false);
                                        newFormData.append('checkTakeUpStudent', true);
                                        $.ajax({
                                            url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                                            type: "POST",
                                            data: newFormData,
                                            processData: false,
                                            contentType: false,
                                            async: true,
                                            timeout: 5 * 60 * 1000,
                                            success: function (data) {
                                                if (data.success) {
                                                    if (data.isTakeUp) {//跳转到提示有学生被占用的弹窗
                                                        var importStudentNames = "";
                                                        var takeUpSutTeacherInfo = "";
                                                        if (data.importNames && data.importNames.length > 0) {
                                                            var names = [];
                                                            for (var l = 0; l < 3 && l < data.importNames.length; ++l) {
                                                                names.push(data.importNames[l]);
                                                            }
                                                            importStudentNames = names.join("、");
                                                            if(data.importNames.length >= 3){
                                                                importStudentNames += "等";
                                                            }
                                                        }
                                                        if (data.takeUpInfo && data.takeUpInfo.length > 0) {
                                                            for (var i = 0; i < data.takeUpInfo.length; i++) {
                                                                takeUpSutTeacherInfo = takeUpSutTeacherInfo + data.takeUpInfo[i].teacherName + " " + data.takeUpInfo[i].clazzName + " " + data.takeUpInfo[i].studentName + "<br/>"
                                                            }
                                                        }
                                                        $(".js-klxTakeUpStudentNames").html(importStudentNames);
                                                        $(".js-klxTakeUpStuTeacherInfo").html(takeUpSutTeacherInfo);
                                                        $.prompt.goToState("state3");
                                                        toState3_From = 0;
                                                    } else {//跳转到更新成功的弹窗
                                                        $(".js-klxImportNewSignNum").html(data.newSignNum);
                                                        $(".js-klxImportUpdateNum").html(data.updateNum);
                                                        $.prompt.goToState("state1");
                                                    }
                                                } else {
                                                    $("#v-errMsg").html(data.info);
                                                    $("#v-errMsg").show();
                                                    return false;
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    $("#v-errMsg").html(data.info);
                                    $("#v-errMsg").show();
                                    return false;
                                }
                            }
                        });
                    }else{
                        $.prompt.goToState("state10");
                        // $.prompt.close();
                    }
                }
            },
            state1:{
                title: "添加学生账号",
                html: template("t:批量导入快乐学学生成功", {}),
                buttons: {"确定": true},
                position: {width: 580, height: 358},
                submit: function(){
                    setTimeout(function(){location.reload()}, 200);
                }
            },
            state2:{
                title: "系统提示",
                html: template("t:快乐学重复学生", {}),
                buttons: {"取消": false,"确定，更新学生信息": true},
                focus: 1,
                position: {width: 580, height: 358},
                submit  : function(e, v){
                    e.preventDefault();
                    if(v){
                        if (toState2_From === 11) { // 从在线添加进入的
                            // 已经显示重复列表弹窗
                            // 开始第二次占用校验
                            var onlineAddText = $('.JS-onlineText').val();
                            if (!onlineAddText) {
                                $('.JS-errorInfo').text('请输入需要添加的学生姓名和学生号');
                                return ;
                            }
                            var onlineAddStudentData = {
                                batchContext: onlineAddText,
                                checkRepeatedStudent: false,
                                checkTakeUpStudent: true,
                                clazzId: $('.JS-onlineText').attr('data-clazzId'),
                                teacherId: $('.JS-onlineText').attr('data-teacherId')
                            };
                            $.ajax({
                                url: "/teacher/clazz/kuailexue/addstudentsonline.vpage",
                                type: "POST",
                                data: onlineAddStudentData,
                                success: function (data) {
                                    if (data.success) {
                                        if (data.isTakeUp) { // 存在占用
                                            var importStudentNames = "";
                                            var takeUpSutTeacherInfo = "";
                                            if (data.importNames && data.importNames.length > 0) {
                                                var names = [];
                                                for (var l = 0; l < 3 && l < data.importNames.length; ++l) {
                                                    names.push(data.importNames[l]);
                                                }
                                                importStudentNames = names.join("、");
                                                if(data.importNames.length >= 3){
                                                    importStudentNames += "等";
                                                }
                                            }
                                            if (data.takeUpInfo && data.takeUpInfo.length > 0) {
                                                for (var i = 0; i < data.takeUpInfo.length; i++) {
                                                    takeUpSutTeacherInfo = takeUpSutTeacherInfo + data.takeUpInfo[i].teacherName + " " + data.takeUpInfo[i].clazzName + " " + data.takeUpInfo[i].studentName + "<br/>"
                                                }
                                            }
                                            $(".js-klxTakeUpStudentNames").html(importStudentNames);
                                            $(".js-klxTakeUpStuTeacherInfo").html(takeUpSutTeacherInfo);
                                            $.prompt.goToState("state3");
                                            toState3_From = 2;
                                        } else {
                                            // 第二次校验通过，直接显示成功弹窗state1
                                            $(".js-klxImportNewSignNum").html(data.newSignNum);
                                            $(".js-klxImportUpdateNum").html(data.updateNum);
                                            $.prompt.goToState("state1");
                                        }
                                    } else {
                                        $("#v-errMsg").html(data.info);
                                        $("#v-errMsg").show();
                                        return false;
                                    }
                                }
                            });
                        } else if (toState2_From === 0) { // 从excel添加进入的
                            var fileInput = $('#v-fileupload');
                            var formData = new FormData();
                            var file = fileInput[0].files[0];
                            formData.append('adjustExcel', file);
                            formData.append('clazzId', fileInput.attr("data-clazzId"));
                            formData.append('checkRepeatedStudent',false);
                            formData.append('checkTakeUpStudent',true);

                            $.ajax({
                                url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                                type: "POST",
                                data: formData,
                                processData: false,
                                contentType: false,
                                async: true,
                                timeout: 5 * 60 * 1000,
                                success: function (data) {
                                    if (data.success) {
                                        if (data.isTakeUp) {//跳转到提示有学生被占用的弹窗
                                            var importStudentNames = "";
                                            var takeUpSutTeacherInfo = "";
                                            if (data.importNames && data.importNames.length > 0) {
                                                var names = [];
                                                for (var l = 0; l < 3 && l < data.importNames.length; ++l) {
                                                    names.push(data.importNames[l]);
                                                }
                                                importStudentNames = names.join("、");
                                                if(data.importNames.length >= 3){
                                                    importStudentNames += "等";
                                                }
                                            }
                                            if (data.takeUpInfo && data.takeUpInfo.length > 0) {
                                                for (var i = 0; i < data.takeUpInfo.length; i++) {
                                                    takeUpSutTeacherInfo = takeUpSutTeacherInfo + data.takeUpInfo[i].teacherName + " " + data.takeUpInfo[i].clazzName + " " + data.takeUpInfo[i].studentName + "<br/>"
                                                }
                                            }
                                            $(".js-klxTakeUpStudentNames").html(importStudentNames);
                                            $(".js-klxTakeUpStuTeacherInfo").html(takeUpSutTeacherInfo);
                                            $.prompt.goToState("state3");
                                            toState3_From = 2;
                                        } else {//跳转到更新成的弹窗
                                            $(".js-klxImportNewSignNum").html(data.newSignNum);
                                            $(".js-klxImportUpdateNum").html(data.updateNum);
                                            $.prompt.goToState("state1");
                                        }
                                    } else {
                                        $("#v-errMsg").html(data.info);
                                        $("#v-errMsg").show();
                                        return false;
                                    }
                                }
                            });
                        }
                    }else{
                        if (toState2_From === 11) { // 从在线添加进入的
                            $.prompt.goToState("state11");
                        } else if (toState2_From === 0) { // 从excel添加进入的
                            $.prompt.goToState("state2");
                        }
                    }
                }
            },
            state3: {
                title: "系统提示",
                html: template("t:快乐学填涂号占用信息", {}),
                buttons: {"取消": false, "确定,使用随机填涂号": true},
                focus: 1,
                position: {width: 580, height: 358},
                submit: function (e, v) {
                    e.preventDefault();
                    if (v) {
                        if (toState3_From === 11) { // 从在线添加来的
                            // 第三次确认提交（checkRepeatedStudent和checkTakeUpStudent都置为false）
                            var onlineAddText = $('.JS-onlineText').val();
                            if (!onlineAddText) {
                                $('.JS-errorInfo').text('请输入需要添加的学生姓名和学生号');
                                return ;
                            }
                            var onlineAddStudentData = {
                                batchContext: onlineAddText,
                                checkRepeatedStudent: false,
                                checkTakeUpStudent: false,
                                clazzId: $('.JS-onlineText').attr('data-clazzId'),
                                teacherId: $('.JS-onlineText').attr('data-teacherId')
                            };
                            $.ajax({
                                url: "/teacher/clazz/kuailexue/addstudentsonline.vpage",
                                type: "POST",
                                data: onlineAddStudentData,
                                success: function (data) {
                                    if (data.success) {
                                        $(".js-klxImportNewSignNum").html(data.newSignNum);
                                        $(".js-klxImportUpdateNum").html(data.updateNum);
                                        $.prompt.goToState("state1");
                                    } else {
                                        $("#v-errMsg").html(data.info);
                                        $("#v-errMsg").show();
                                        return false;
                                    }
                                }
                            });
                         } else if (toState3_From === 0) { // 从excel上传来的
                            var fileInput = $('#v-fileupload');
                            var formData = new FormData();
                            var file = fileInput[0].files[0];
                            formData.append('adjustExcel', file);
                            formData.append('clazzId', fileInput.attr("data-clazzId"));
                            formData.append('checkRepeatedStudent',false);
                            formData.append('checkTakeUpStudent',false);
                            $.ajax({
                                url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                                type: "POST",
                                data: formData,
                                processData: false,
                                contentType: false,
                                async: true,
                                timeout: 5 * 60 * 1000,
                                success: function (data) {
                                    if (data.success) {
                                        $(".js-klxImportNewSignNum").html(data.newSignNum);
                                        $(".js-klxImportUpdateNum").html(data.updateNum);
                                        $.prompt.goToState("state1");
                                    } else {
                                        $("#v-errMsg").html(data.info);
                                        $("#v-errMsg").show();
                                        return false;
                                    }
                                }
                            });
                        }
                    } else {
                        if (toState3_From == 11) {
                            $.prompt.goToState("state11");
                        } else if (toState3_From == 0) {
                            $.prompt.goToState("state0");
                        } else if (toState3_From == 2) {
                            $.prompt.goToState("state2");
                        }
                    }
                }
            }
        };

        // $.prompt(importKlxStudentPass);

        // 在线添加
        $(document).on("click", ".JS-addByOnline", function () {
            $.prompt.goToState('state11');
        });
        // excel添加
        $(document).on("click", ".JS-addByExcel", function () {
            $.prompt.goToState('state0');
        });

        $(document).on("click",".v-uploadKlxDoc",function () {
            var ie = !-[1,];
            if(ie){
                $('#v-fileupload').trigger('click').trigger('change');
            }else{
                $('#v-fileupload').trigger('click');
            }
        }).on("change","#v-fileupload",function () {
            // 截掉前面的路径，只留文件名
            var fileInput = $("#v-fileupload").val();
            fileInput = fileInput.substring(fileInput.lastIndexOf("\\") + 1);
            $(".v-fileName").html(fileInput);
            $(".v-fileName").show();
        }).on("click",".v-downloadTemplate", function () {
            $("body").append("<iframe style='display:none;' src='/teacher/clazz/kuailexue/clazzstutemplate.vpage'/>");
        });
    });


    // ----------------------------------------------------------------------------------------------------------------

    //添加学生
    $(".v-add-student").on("click", function(){
        // 判断是否是第三方，是则禁止操作
        if (isThirdParty == 'true') {
            isThirdPartyTip();
            return false;
        }

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

});