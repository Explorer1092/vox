<#if students?? && students?has_content>
    <script type="text/html" id="t:给学生留言">
        <div class="sms_info_box">
            <div class="content_box teacher_ms">
                <div class="select_in">
                    <div class="sms_us_list" style="width:480px;">
                        <ul id="sendLetterStudents">
                            <#list students as s>
                                <li>
                                    <label>
                                        <b>
                                            <img style="width:60px, height:60px;" src="<@app.avatar href="${s.fetchImageUrl()!}"/>">
                                            <i class="radios" data-student_id="${s.id!}" style="display:block; top:-2px; right:-5px; position:absolute;"></i>
                                        </b>
                                        <span>${s.profile.realname}</span>
                                    </label>
                                </li>
                            </#list>
                        </ul>
                        <div class="clear"></div>
                    </div>
                    <div style="width:480px; text-align:right;">
                        <textarea id="sendLetterContent" style="width:470px; height:50px; margin: 10px 0 0;" name="sendLetterContent" maxlength="140" placeholder="请在这里填写你要发送的留言..." class="w-int" cols="" rows=""></textarea>
                        <p id="word_limit_box" style="color:#999;">还可以输入140个字</p>
                        <p class="error_tip_box" style="text-align:center; color:#FF0000; font-size:14px;"></p>
                        <a id="s_s_conversation_but" href="javascript:void(0)" class="w-btn w-btn-green send_letter_but">
                            <strong>发 送</strong>
                        </a>
                    </div>
                </div>
            </div>
            <div class="clear"></div>
        </div>
    </script>

    <script type="text/javascript">
        $(function(){
            $("#student_create_conversation_to_student_btn").on('click', function(){
                $17.tongji("消息中心-留言板-给同学留言");
                var $this = $(this);
                var sendToClassmates = {
                    state: {
                        title: "给同学留言",
                        html: template("t:给学生留言", {}),
                        position: { width: 540 },
                        buttons: {}
                    }
                };
                $.prompt(sendToClassmates, {
                    loaded: function () {
                        $("#send_content_box").focus();
                    }
                });

                $("#sendLetterStudents li").on('click', function(){
                    var $this = $(this);
                    $this.find('.radios').addClass('radios_active');
                    $this.siblings().find(".radios").removeClass('radios_active');
                });

                /*发送*/
                $("#s_s_conversation_but").on('click', function(){
                    var $this = $(this);
                    var studentId = $(".radios.radios_active").data("student_id");
                    var content = $("#sendLetterContent").val();
                    var errorTip = $(".error_tip_box");
                    if($17.isBlank(studentId)){
                        errorTip.text("选择你要发送的同学！");
                        return false;
                    }
                    if($17.isBlank(content)){
                        errorTip.text("填写留言内容！");
                        return false;
                    }

                    if(content.length > 140){
                        errorTip.text("留言内容的字数超过了140，重新填写！");
                        return false;
                    }
                    App.postJSON("/student/conversation/createconversation.vpage", {userIds: [studentId], payload: content}, function(data){
                        if(data.success){
                            $17.tongji("学生-留言-给同学写信-成功","学生-留言-给同学写信-成功");
                            errorTip.text("发送成功！");
                            $this.hide();
                            setTimeout(function(){
                                //数据更新
                                $.get('/student/conversation/conversations.vpage?currentPage=1', function (data) {
                                    $.prompt.close();
                                    $("#message_list_box").html(data);
                                });
                            },1000);

                        }else{
                            errorTip.text(data.info);
                        }
                    });
                });

                /*留言字数*/
                $('#sendLetterContent').on("keyup", function(){
                    $("#word_limit_box").html($17.wordLengthLimit($(this).val().length,140));
                });
            });
        });
    </script>
</#if>
