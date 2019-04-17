<#-- @ftlvariable name="userType" type="java.lang.Integer" -->
<#import "./student/sendmessage.ftl" as sendToStudent/>
<#import "./parent/sendmessage.ftl" as sendToParent/>
<@sugar.capsule css=["new_teacher.message"] />
<div class="w-base" id="mainContainer">
    <div class="w-base-title">
        <h3>消息中心</h3>
        <div class="w-base-right w-base-switch">
            <ul id="containerTab" style="right: auto; left: 10px;">
                <li data-content="systemMessage" data-is_load="false">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>系统消息
                    </a>
                </li>
            <#if (currentTeacherDetail.isPrimarySchool())!false>
                <li data-content="parentMessage" data-is_load="false">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>家长/学生留言板
                    </a>
                </li>
                <#--<li data-content="studentMessage" data-is_load="false">-->
                    <#--<a href="javascript:void(0);">-->
                        <#--<span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>学生留言板-->
                    <#--</a>-->
                <#--</li>-->
            </#if>
            </ul>
        </div>
    </div>
    <#--「20170321」老师pc端下线家长、学生留言板功能 -->
    <#--<#if (currentTeacherDetail.isPrimarySchool())!false>-->
    <#--<div class="w-base-title" style="background-color: #f3f3f3;">-->
        <#--<div style="padding-top: 7px;" class="w-base-right">-->
            <#--<a style="width: 120px;" id="send_to_parent" class="w-btn w-btn-mini" href="javascript:void(0);">-->
                <#--<span class="w-icon w-icon-white w-icon-3"></span>-->
                <#--<span class="w-icon-md w-ft-well">给家长留言</span>-->
            <#--</a>-->
            <#--<a style="width: 120px;" id="send_to_student" class="w-btn w-btn-green w-btn-mini" href="javascript:void(0);">-->
                <#--<span class="w-icon w-icon-white w-icon-3"></span>-->
                <#--<span class="w-icon-md w-ft-well">给学生留言</span>-->
            <#--</a>-->
            <#--&lt;#&ndash;<a class="w-icon-md w-pad-5 w-magL-10" href="javascript:void(0);"><span class="w-icon w-icon-17"></span><span class="w-icon-md w-ft-well">清空</span></a>&ndash;&gt;-->
        <#--</div>-->
    <#--</div>-->
    <#--</#if>-->
    <div class="w-base-container">
        <!--//start-->
            <div class="systemMessage"></div>
            <div class="parentMessage" style="display: none"></div>
            <div class="studentMessage" style="display: none;"></div>
        <!--end//-->
        <div class="w-clear"></div>
    </div>
</div>
    <@sendToStudent.send_message/>
    <@sendToParent.send_message/>
<script type="text/javascript">
    function changeUnreadCount($this, obj, text){
        $.get('/teacher/bubbles.vpage', function(data){
            if(data.success){
                $this.text(data[obj] + text);
                $(".unreadConversationCount").text(data.pendingApplicationCount + data.unreadNoticeCount + data.unreadLetterAndReplyCount);
                if(data.pendingApplicationCount==0 && data.unreadNoticeCount==0 && data.unreadLetterAndReplyCount==0){
                    $("#popinfo").hide();
                }
            }
        });
    }

    <#--用于获得分页中的回调函数-->
    var loadMessage = null;

    $(function () {
        loadMessage = new $17.Model({
            tabTarget: $("#containerTab").find("li")
        });
        loadMessage.extend({
            showHideContent: function (type, scope) {
                $(".systemMessage, .parentMessage, .studentMessage", scope).hide("fast").filter(function () {
                    return $(this).hasClass(type);
                }).show("fast");
            },
            loadSystemMessage: function (pageIndex) {
                $(".systemMessage").html('<div class="w-ag-center" style="padding: 50px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                $.get("/teacher/message/list.vpage?currentPage="+pageIndex, function (data) {
                    $(".systemMessage").html(data);
                    changeUnreadCount($("#unreadNoticeCount"),'unreadNoticeCount','条新通知');
                });
            },
            loadParentMessage: function (pageIndex) {
                $(".parentMessage").html('<div class="w-ag-center" style="padding: 50px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                $.get("/teacher/conversation/parentconversations.vpage?currentPage="+pageIndex, function (_data) {
                    $(".parentMessage").html(_data);
                    changeUnreadCount($("#unreadLetterAndReplyCount"),'unreadLetterAndReplyCount','条新留言');
                });
            },
            loadStudentMessage: function (pageIndex) {
                $(".studentMessage").html('<div class="w-ag-center" style="padding: 50px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                $.get("/teacher/conversation/studentconversations.vpage?currentPage="+pageIndex, function (_data) {

                    $(".studentMessage").html(_data);
                    changeUnreadCount($("#unreadLetterAndReplyCount"),'unreadLetterAndReplyCount','条新留言');
                });
            },
            init: function () {
                var $this = this;

                $this.tabTarget.on("click", function () {
                    var $that = $(this);
                    if($that.hasClass("active")){
                       return false;
                    }
                    $this.showHideContent($that.radioClass("active").data("content"), $that.closest("#mainContainer"));
                    if (!$that.data("is_load")) {
                        switch ($that.data("content")) {
                            case "parentMessage":
                                $this.loadParentMessage(1);
                                break;
                            case "studentMessage":
                                $this.loadStudentMessage(1);
                                break;
                            case "systemMessage":
                                $this.loadSystemMessage(1);
                                break;
                        }
                        $that.data("is_load", true);
                    }
                });

                $this.tabTarget.eq(${userType!0}).trigger("click");
            }
        }).init();
    });

    /*回复提交后 回调*/
    function reply_list_complete($this) {
        $this.parent().find(".search_reply_box").slideToggle();
        $this.find("span").toggle();
    }

    $(function () {
        LeftMenu.changeMenu();
        LeftMenu.focus("message");

        /*回复--输入框*/
        $(".reply_but").live("click", function () {
            var $this = $(this);
            var letterId = $this.data("letter_id");
            $("#surplus_count_" + letterId).text("140");
            $("#reply_content_box_" + letterId).on("keyup", function () {
                var $self = $(this);
                var replyContent = $self.val();
                var replySurplusCount = 140 - replyContent.length;
                if (replySurplusCount >= 0) {
                    $("#surplus_count_" + letterId).text(replySurplusCount);
                } else {
                    $17.alert("您输入的字数超出了140字，请重新填写。");
                    replyContent = replyContent.substring(0, 140);
                    $("#message_content_" + letterId).val(replyContent);
                    $("#surplus_count_" + letterId).text(0);
                }
            });

            $("#reply_box_" + letterId).toggle();
        });

        /*删除信息*/
        $(".delete_content_but").live("click", function () {
            var $this = $(this);
            var letterId = $this.data("letter_id");
            var deleteType = $this.data("delete_type");
            var postUrl = null;
            var postData = null;
            if (deleteType == "deleteSystemMsg") {
                postUrl = '/teacher/message/deleteSysMessage.vpage?messageId='+letterId;
                postData = {};
            } else {
                postUrl = '/teacher/conversation/deleteconversation.vpage';
                postData = {uniqueId: letterId};
            }
            $.prompt("确定删除该条信息吗?",{
                title : "系统提示",
                buttons : {"取消" : false ,"确定" : true},
                position : {width : 400},
                focus : 1,
                submit : function(e,v){
                    e.preventDefault();
                    if(v){
                        App.postJSON(postUrl, postData, function (data) {
                            if (data.success) {
                                $.prompt.close();
                                $this.closest(".t-notice-box").remove();
                            } else {
                                $17.alert("参数错误,请刷新页面重试");
                            }
                        });
                    }else{
                        $.prompt.close();
                    }
                }
            });
        });
    });

    /**
     * ------------------------------------------------------------------------------------------
     */
    $(function () {
        /*给学生留言*/
        $('#send_to_student').on('click', function () {
            $17.tongji("老师-给学生留言","老师-给学生留言");
            $.get("/teacher/conversation/clazzlist.vpage", function (data) {
                var states = {
                    state: {
                        title: "给学生留言",
                        html: template("t:给学生留言", {clazzList: data.clazzList}),
                        position: { width: 590},
                        buttons: {"发送": true},
                        submit: function () {
                            var _content = $("#sendLetterContent").val();
                            var _contentLength = _content.length;
                            var sector1 = $('#sector1');
                            var rows = $('span.receiver', sector1);
                            var students = [];
                            if (sector1.text() == "") {
                                $(".show_error_box").html("<p class='none_student_box' style='margin-left:65px;color: red;'>请选择您要留言的学生</p>");
                                return false;
                            } else if (_contentLength > 140) {
                                $(".show_error_box").html("<p class='none_student_box' style='margin-left:65px;color: red;''>您输入的字数超出了140个，请重新输入</p>");
                                return false;
                            } else if (_contentLength == 0) {
                                $(".show_error_box").html("<p class='none_student_box' style='margin-left:65px;color: red;'>请输入您要发送的内容</p>");
                                return false;
                            }

                            $.each(rows, function (index, row) {
                                var id = $(row).attr('id');
                                if (id) {
                                    students.push(id);
                                }
                            });

                            App.postJSON("/teacher/conversation/createconversation.vpage?userType=3", {userIds: students, content: _content}, function (data) {
                                if (data.success) {
                                    $17.tongji("老师-给学生留言-成功","老师-给学生留言-成功");
                                    //更新学生留言板
                                    $.get("/teacher/conversation/studentconversations.vpage?currentPage=1",function(data){
                                        $(".studentMessage").html(data);
                                    });
                                } else {
                                    $17.alert("发送失败，请重新发送！");
                                }
                            });
                        }
                    }
                }
                $.prompt(states);
            });
        });

        /*给家长留言*/
        $('#send_to_parent').on('click', function () {
            $17.tongji("老师-给家长留言","老师-给家长留言");
            $.get("/teacher/conversation/clazzlist.vpage", function (data) {
                var states = {
                    state: {
                        title: "给家长留言",
                        html: template("t:给家长留言", {clazzList: data.clazzList}),
                        position: { width: 590},
                        buttons: {"发送": true},
                        submit: function () {
                            var _content = $("#sendLetterContent_parent").val();
                            var _contentLength = _content.length;
                            var sector1 = $('#sector1_parent');
                            var rows = $('span.receiver_parent', sector1);
                            var students = [];
                            var errorBox = $(".show_error_box_parent");
                            if (sector1.text() == "") {
                                errorBox.html("<p class='none_student_box_parent' style='margin-left:65px;color: red;'>请选择您要留言的家长</p>");
                                return false;
                            } else if (_contentLength > 140) {
                                errorBox.html("<p class='none_student_box_parent' style='margin-left:65px;color: red;'>您输入的字数超出了140个，请重新输入</p>");
                                return false;
                            } else if (_contentLength == 0) {
                                errorBox.html("<p class='none_student_box_parent' style='margin-left:65px;color: red;'>请输入您要发送的内容</p>");
                                return false;
                            }

                            $.each(rows, function (index, row) {
                                var id = $(row).attr('id');
                                if (id) {
                                    students.push(id);
                                }
                            });

                            <#--发送的是学生的ID，以-1标记在后台转变为家长ID-->
                            App.postJSON("/teacher/conversation/createconversation.vpage?userType=-1", {userIds: students,content: _content}, function (data) {
                                if (data.success) {
                                    $17.tongji("老师-给家长留言-成功","老师-给家长留言-成功");
                                    //更新家长留言板
                                    $.get("/teacher/conversation/parentconversations.vpage?currentPage=1",function(data){
                                        $(".parentMessage").html(data);
                                    });
                                } else {
                                    $17.alert("发送失败，请重新发送！");
                                }
                            });
                        }
                    }
                };
                $.prompt(states);
            });
        });

        //log
        $17.voxLog({
            module: 'message',
            op:  'load'
        },'teacher');
    });
</script>