<#--关联班级模板-->
<script type="text/html" id="T:关联老师">
    <div class="module-add message" data-subject="<%=subject%>">
        <div class="title">请填写老师信息！（姓名、手机号至少填写一项）</div>
        <div class="label">
            <div><span>姓名：</span><input class="newTeacherName" type="text"></div>
            <div><span>手机号：</span><input class="newTeacherMobile" type="text"></div>
        </div>
        <div class="module-foot">
            <a href="javascript:;" class="btn white v-cancelLink">取消</a>
            <a href="javascript:;" class="btn v-confirmLink">添加</a>
        </div>
    </div>
</script>

<#--添加老师搜索结果模板-->
<script type="text/html" id="T:添加老师搜索结果">
    <div class="module-add pics">
        <div class="list-box clearfix">
            <%for(var i = 0, list = teacherList; i < list.length; i++){%>
                <a href="javascript:;" class="list v-selectLinkTeacher" data-teacher-id="<%=list[i].id%>" data-teacher-name="<%=list[i].profile.realname%>">
                    <div class="face">
                        <%if(list[i].profile.imgUrl == ""){%>
                            <img src="<@app.avatar href=''/>">
                        <%}else{%>
                            <img src="<@app.avatar href='<%=list[i].profile.imgUrl%>'/>">
                        <%}%>
                    </div>
                    <div class="label-list">
                        <div class="label">
                            <div class="box">
                                <%=list[i].profile.realname%>（<%=list[i].id%>）
                                    <%if(list[i].authenticationState == 1){%>
                                        <i class="w-icon-public w-icon-authVip"></i>
                                    <%}%>
                            </div>
                        </div>
                    </div>
                </a>
            <%}%>
        </div>
        <div class="module-foot">
            <a href="javascript:;" class="btn white v-cancelLink">取消</a>
            <a href="javascript:;" class="btn v-confirmLink">添加</a>
        </div>
    </div>
</script>

<script type="text/javascript">
    $(function(){

        ////////////////////////////////////////////关联班级面板功能/////////////////////////////////////////////////////

        // 关联老师页面
        $(".v-linkteacher").on('click', function() {
            // 判断是否是第三方，是则禁止操作
            if (isThirdParty == 'true') {
                isThirdPartyTip();
                return false;
            }

            var isFake = "${(isFakeTeacher!false)?string('yes','no')}";
            if (isFake === 'yes') {
                $.prompt("<div style='text-align: center; font-size: 16px; line-height: 24px;'>您的账号使用存在异常，该功能受限<br>如有疑议，请进行申诉</div>", {
                    title: "系统提示",
                    focus: 1,
                    buttons: {"知道了": false, "去申诉": true},
                    submit : function(e, v) {
                        if (v) {
                            e.preventDefault();
                            window.open ('${(ProductConfig.getMainSiteBaseUrl())!''}/ucenter/teacherfeedback.vpage?type=FAKE', 'feedbackwindow', 'height=500, width=700,top=200,left=450');
                        }
                    }
                });
            } else {
                var $this = $(this);

                var $managePanel = $this.parents(".module-info");
                var $linkPanel = $managePanel.siblings(".link-panel");
                if ($linkPanel.hasClass("dis")) {
                    $linkPanel.removeClass("dis");
                    $linkPanel.hide();
                    $managePanel.show();
                    return;
                }
                $managePanel.hide();
                $linkPanel.addClass("dis");
                var subject = $this.attr("data-subject");
                $linkPanel.empty().html(template("T:关联老师", {subject: subject})).show();
                $linkPanel.show();
            }
        });

        var linkPanel = $(".link-panel");

        // 取消
        linkPanel.on("click", ".v-cancelLink", function() {
            var $this = $(this);

            var $linkPanel = $this.parents(".link-panel");
            var $managePanel = $linkPanel.siblings(".module-info");
            if ($linkPanel.hasClass("dis")) {
                $linkPanel.removeClass("dis");
                $linkPanel.hide();
                $managePanel.show();
                return;
            }
            $managePanel.hide();
            $linkPanel.addClass("dis");
            $linkPanel.empty().html(template("T:关联老师", {})).show();
            $linkPanel.show();
        });

        // 转给Ta（直接输入学号/手机号）
        linkPanel.on("click", ".v-confirmLink", function() {
            var $this = $(this);

            var container = $this.parents(".link-panel");
            var clazzId = container.attr("data-clazzId");
            var clazzName = container.attr("data-clazzName");
            var name = container.find(".newTeacherName");
            var mobile = container.find(".newTeacherMobile");

            var nameVal = name.val();
            var mobileVal = mobile.val();

            if( $17.isBlank(nameVal) && $17.isBlank(mobileVal)){
                name.addClass("w-int-error");
                mobile.addClass("w-int-error");
                return false;
            }

            if (!$17.isBlank(mobileVal) && !$17.isMobile(mobileVal)) {
                mobile.addClass("w-int-error");
                return false;
            }

            if (!$17.isBlank(nameVal) && !$17.isValidCnName(nameVal)) {
//            if (!$17.isBlank(nameVal) && !$17.isCnString(nameVal)) {
                name.addClass("w-int-error");
                return false;
            }

            name.removeClass("w-int-error");
            mobile.removeClass("w-int-error");

            var subject = $this.parents(".module-add").attr("data-subject");

            var $postData = {
                name: $.trim(nameVal),
                mobile: mobileVal,
                clazzId : clazzId,
                targetSubject : subject
            };

            // 查找老师
            $.post("/teacher/systemclazz/findlinkteacher.vpage", $postData, function(data){
                if (data.success) {
                    var $parentTeacherList = $this.parents(".link-panel");
                    $parentTeacherList.empty().html(template("T:添加老师搜索结果", {teacherList: data.teachers})).show();

                    var currentSelect = {
                        teacherName : null,
                        teacherId : null
                    };

                    // 选择老师
                    $parentTeacherList.on("click", ".v-selectLinkTeacher", function() {
                        var $this = $(this);

                        currentSelect.teacherName = $this.attr("data-teacher-name");
                        currentSelect.teacherId = $this.attr("data-teacher-id");
                        $this.siblings().removeClass("active");
                        $this.addClass("active");
                    });

                    $parentTeacherList.on("click", ".v-confirmLink", function() {
                        if (!currentSelect.teacherId) {
                            $17.alert("请选择一个老师")
                            return false;
                        }

                        $.get("/teacher/systemclazz/sendlinkapp.vpage", {
                            clazzId: clazzId,
                            respondentId: currentSelect.teacherId,
                            subject: "${curSubject!}",
                            linkSubject: subject
                        }, function (data) {
                            if (data.success) {
                                if (data.added) {
                                    $17.alert("添加老师成功！", function () {
                                        location.reload();
                                    });
                                } else {
                                    $17.alert("申请成功<br><br>请当面提醒对方老师通过您的请求", function () {
                                        location.reload();
                                    });
                                }
                            } else {
                                $17.alert(data.info);
                            }
                        });
                    });
                } else {
                    if (data.type == "INVITE_TEACHER") {// 手机号注册
                        $.prompt("<div style='text-align: center; font-size: 22px;'>该老师还未注册！</div>", {
                            title: "系统提示",
                            //buttons: {"取消": false, "邀请Ta": true},
                            buttons: {"知道了": false},
                            submit : function(e, v) {
                                if (v) {
                                    $.post("/teacher/systemclazz/invitelinkteacher.vpage", $postData, function(data){
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
                        $.prompt("<div style='text-align: center; font-size: 22px;'>该老师还未注册或账号异常！</div>", {
                            title: "系统提示",
                            //buttons: {"取消": false, "邀请Ta": true},
                            buttons: {"知道了": false},
                            submit : function(e, v) {
                                if (v) {
                                    <#--console.log("${(ProductConfig.getMainSiteBaseUrl())!''}");-->
                                    window.location = "${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/invite/index.vpage";
                                }
                            }
                        });
                    } else {
                        $17.alert("<div style='text-align: center'>没有找到符合条件的老师！</div>", function() {

                        });
                    }

                }
            });
        });
    })
</script>