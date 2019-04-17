<#-- @ftlvariable name="teachers" type="java.util.List<com.voxlearning.utopia.service.user.api.entities.extension.Teacher>" -->
<#import "../message/module.ftl" as temp>
<@temp.messagePage title="留言板">
    <style>
        .sms_info_box .sms_us_list li b input { display: none; }
        .teacher_ms .select_be .sector_box span { display: inline-block; color: #666; }
        .sms_info_box { width: 640px; clear: both; }
        .sms_info_box .sms_us_list { border: solid 1px #ddd; width: 280px; float: left; background: #fff; border-radius: 5px; height: 100%; overflow: hidden; }
        .sms_info_box .sms_us_list .check_box { margin: 1px; padding: 6px 5px; }
        .sms_info_box .sms_us_list input { vertical-align: middle; }
        .sms_info_box .sms_us_list ul { height: 265px; overflow-x: hidden; overflow-y: auto; position: relative; }
        .sms_info_box .sms_us_list li { float: left; padding: 5px; text-align: center; width: 55px; }
        .sms_info_box .sms_us_list li b { cursor: pointer; clear: both; display: block; margin: 0 auto; border: solid 2px #ddd; padding: 1px; background: #fff; width: 48px; height: 48px; position: relative; }
        .sms_info_box .sms_us_list li b .checkboxs { display: none; }
        .sms_info_box .sms_us_list li b.selected .checkboxs { bottom: -8px; display: block !important; right: -8px; position: absolute; }
        .sms_info_box .sms_us_list li b img { width: 48px; height: 48px; }
        .sms_info_box .sms_us_list li b input { position: absolute; bottom: 0px; right: 0px; width: 15px; height: 15px; }
        .sms_info_box .sms_us_list li span { cursor: pointer; color: #F60; line-height: 20px; height: 20px; width: 60px; overflow: hidden; display: block; clear: both; margin: 2px 0; }
        .sms_info_box .sms_reviews { float: left; margin-left: 12px; width: 320px; }
        .sms_info_box .sms_reviews .s_custom .info { font-size: 12px; text-align: center; padding: 30% 0 0; width: 100%; color: #666; }
        .sms_info_box .sms_reviews .s_custom h4 { height: 22px; font: 12px/1.125 arial; color: #999; }
        .sms_info_box .sms_reviews .s_custom h4 b { color: #333; }
        .sms_info_box .sms_reviews .s_custom h4 span { float: right; font-weight: normal; color: #bbb; margin-right: 30px; cursor: pointer; }
        .sms_info_box .sms_reviews .s_custom textarea { height: 100%; width: 100%; border: solid 1px #ddd; background: #fff; line-height: 20px; border-radius: 6px; }
        .sms_info_box .sms_reviews .s_default { border-radius: 6px; border: solid 3px #bfcde5; background: #fff; margin-bottom: 10px; height: 140px; overflow: auto; display: block; padding: 5px; }
        .sms_info_box .sms_reviews .s_default a { border-bottom: dashed 1px #ddd; line-height: 24px; padding: 2px; color: #999; display: block; }
        .sms_info_box .sms_reviews .s_default a:hover { background: #eee; border-bottom: dashed 1px #ccc; color: #333; text-decoration: none; }
        .teacher_ms { }
        .teacher_ms .select_in { float: left; width: 150px; }
        .teacher_ms .select_in p select { width: 140px; border: solid 1px #ccc; padding: 5px; }
        .teacher_ms .select_be { float: left; width: 330px; }
        .teacher_ms .select_be .sector_box { border: 1px solid #ddd; height: 80px; background: #fff; margin: 5px 0 0; overflow: auto; padding: 5px; color: #333; font: 12px/18px arial; }
        .teacher_ms .select_be .sector_box a { display: inline-block; padding: 2px 4px; border-radius: 3px; color: #999; text-decoration: none; }
        .teacher_ms .select_be .sector_box a:hover { background: #eee; color: #333; }
        .teacher_ms .select_be .content_ms p { padding: 14px 0 0; }
        .teacher_ms .select_be .content_ms textarea { width: 240px; height: 125px; border: solid 1px #ccc; line-height: 24px; }
        .teacher_ms .select_be .select_une select { border: solid 1px #ccc; padding: 5px; }

            /*teachersage*/
        .teachermsg { padding: 0; margin: 0 40px;; }
        .teachermsg li { margin: 0; clear: both; }
        .teachermsg .currentctn dl { width: 100%; border-bottom: 1px solid #ddd; overflow: hidden; height: 100%; }
        .teachermsg .currentctn dl dl{ border:none; border-top: 1px solid #ddd;}
        .teachermsg dt.avatar { float: left; width: 80px; padding: 15px 0 0; }
        .teachermsg dt.avatar .arrowl { display: inline-block; *zoom:1;font-size: 1px; height: 11px; width: 6px; margin: -25px 0 0 75px; }
        .teachermsg .currentctn dt { padding: 12px 0 0; }
        .teachermsg .currentctn dt.avatar span { width: 52px; height: 52px; margin: 0 auto; display: block; }
        .teachermsg .currentctn dt.avatar img { width: 46px; height: 46px; margin: 2px 0 0 3px;border-radius: 100px; overflow: hidden; display: block; }
        .teachermsg .currentctn dd { margin: 0 0 0 80px; }
        .teachermsg .currentctn dd .title { padding: 5px 10px; border-radius: 0 6px 0 0; ; height: 28px; }
        .teachermsg .currentctn dd .title .tL { float: left; color: #5fa918; margin: 7px 0 0; }
        .teachermsg .currentctn dd .title .tR { float: right; }
        .teachermsg .currentctn dd .title .tR b { color: #999; }
        .teachermsg .currentctn dd .ctn { font: 14px/22px arial; padding:5px 12px; color: #666;/* white-space: nowrap; width: 90%; overflow: hidden; text-overflow: ellipsis;*/ }
            /**/
        .teachermsg .replyctn { clear: both; padding: 0 0 0 50px; border-bottom: 1px solid #ddd; }
        .teachermsg .replyctn dl { margin: 0 0 -1px; border: dashed #ddd; border-width: 0 0 1px; }
        .teachermsg .replyctn dt.avatar span { width: 52px; height: 52px; display: block; margin: 0 auto; }
        .teachermsg .replyctn dt.avatar img { width: 46px; height: 46px; margin: 2px 0 0 3px; }
        .teachermsg .replyctn dd { margin: 0 0 0 80px; }
        .teachermsg .replyctn dd .title { padding: 5px 10px; height: 28px; }
        .teachermsg .replyctn dd .title .tL { color: #5fa918; float: left; padding: 7px 0 0; display: block; }
        .teachermsg .replyctn dd .title .tR { float: right; }
        .teachermsg .replyctn dd .ctn { font: 14px/22px arial; padding: 12px; color: #666; word-break: break-all; word-wrap: break-word; }
            /**/
        .teachermsg .replybox .txa { position: relative;  margin: 10px 0;}
        .teachermsg .replybox .txa .tif { position: absolute; bottom: 6px; color: #999; right: 20px; }
        .teachermsg .replybox .txa .tif b { font-size: 16px; color: #666; padding: 0 5px; }
        .teachermsg .replybox .txa textarea { width: 98%; *width:588px; height: 60px;  }
        .teachermsg .replybox .btn { padding: 10px; text-align: right; }

    </style>
    <#include "classmates.ftl"/>
    <div class="t-center-box w-fl-right">
        <div class="t-messages-data">
            <div class="t-messages-title">
                <div class="title-inner-back">
                <#if teachers?? && teachers?size gt 0>
                    <a id="student_create_conversation_btn" class="w-change-btn w-fl-right" href="javascript:void(0)">
                        <strong>给老师留言</strong>
                    </a>
                </#if>
                <#if students?? && students?size gt 0>
                    <a id="student_create_conversation_to_student_btn" class="w-change-btn w-fl-right" href="javascript:void(0)" style="margin-right: 8px;">
                        <strong>给同学留言</strong>
                    </a>
                </#if>
                留言板
                </div>
            </div>

            <div class="teachermsg" id="message_list_box">
            <#--内容显示区-->
            </div>
            <div class="message_page_list" style="float:right; ">
            <#--分页-->
            </div>
        </div>
    </div>

    <#-- 给老师发信息  -->
    <script type="text/html" id="t:给老师发信息">
        <div class="content_box teacher_ms">
            <div class="select_in" style="float:none; width:auto;">
                <div id="sendLetterTeacher">
                    <#if teachers?? && teachers?has_content>
                        <p>给
                            <select style="width: 100px;" class="int_vox">
                                <#list teachers as each>
                                    <option value="${each.id!}">${(each.profile.realname)!}</option>
                                </#list>
                            </select>
                            老师留言
                        </p>
                    <#else>
                        <p>你还没有加入任何班级哦，赶快去加入吧</p>
                    </#if>
                </div>
            </div>
            <div class="select_be">
                <div class="content_ms">
                    <p class="text_gray_9">你发送的留言老师可以看到，尽量避免错别字！</p>
                    <p>
                        <textarea id="send_content_box" maxlength="140" placeholder="填写你要发给老师的留言" name="send_content" style="width:95%" cols="" class="int_vox" rows=""></textarea>
                    </p>
                    <p id="word_limit_box" class="text_right">还可以输入140字。</p>
                </div>
                <div style="padding:10px 0; text-align:right; clear:both;">
                    <span class="error_tip" style="color: red"></span>
                    <a id="student_create_conversation_send_btn" href="javascript:void(0);" class="w-btn w-btn-green">发 送</a>
                </div>
            </div>
            <div class="clear"></div>
        </div>
    </script>

    <script type="text/javascript">
        function createPageList(index) {
            $("#message_list_box").html('<div style="padding: 50px 0;text-align: center;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
            $.get('/student/conversation/conversations.vpage?currentPage='+index, function (data) {
                $("#message_list_box").html(data);
                $.get("/student/bubbles.vpage",function(data){
                    if(data.unreadTotalCount == 0){
                        $("#popinfo").hide();
                        $(".unreadSystemMessageCount").hide();
                    }else{
                        $("#popinfo").show();
                        $(".unreadSystemMessageCount").text(data.unreadTotalCount).show();
                    }

                });
            });
        }
        $(function () {
            /*初始化*/
            createPageList(1);
            /*给老师发信息*/
            $("#student_create_conversation_btn").click(function () {
                $17.tongji("消息中心-留言板-给老师留言");
                var sendToTeacher = {
                    state: {
                        title: "给老师留言",
                        html: template("t:给老师发信息", {}),
                        position: { width: 380 },
                        buttons: {}
                    }
                };
                $.prompt(sendToTeacher, {
                    loaded: function () {
                        $("#send_content_box").focus();
                    }
                });

                /*给老师发信息 --> 发送 */
                $("#student_create_conversation_send_btn").on("click", function () {
                    var $this = $(this);
                    var content = $("#send_content_box").val();
                    var teacherIds = [];
                    var sector = $('#sendLetterTeacher').find('select option:selected');
                    var errorTip = $(".error_tip");
                    teacherIds[0] = sector.val();
                    if (content.length > 140) {
                        errorTip.text("你输入的字数超出了140字，请重新填写。");
                        return false;
                    } else if (content.length == 0) {
                        errorTip.text("您输入的内容为空，请重新填写。");
                        return false;
                    } else {
                        App.postJSON("/student/conversation/createconversation.vpage", {userIds: teacherIds, payload: content}, function (data) {
                            if (data.success) {
                                $17.tongji("学生-留言-给老师写信-成功","学生-留言-给老师写信-成功");
                                errorTip.text("给老师留言成功了！");
                                $this.hide();
                                setTimeout(function () {
                                    //数据更新
                                    $.get('/student/conversation/conversations.vpage?currentPage=1', function (data) {
                                        $.prompt.close();
                                        $("#message_list_box").html(data);
                                    });
                                }, 1000);
                            } else {
                                $17.alert(data.info);
                            }
                        });
                    }
                });

                /* 给老师发信息 */
                $("#send_content_box").on("keyup", function () {
                    $("#word_limit_box").html($17.wordLengthLimit($(this).val().length,140));
                });
            });
        });
    </script>
</@temp.messagePage>
