<!DOCTYPE html>
<html>
<head>
    <title>一起作业</title>
    <#include "../../nuwa/meta.ftl" />
    <@sugar.capsule js=["jquery", "core", "alert", "jcarousel"] css=["plugin.alert", "jquery.jcarousel", "common.so", "teacher.widget"] />
    <style>
        ul,li{ list-style: none;}
        .sms_invite_box { padding-top: 20px;}
        .sms_invite_box li .picture .checkboxs{ position: absolute; right: 0; bottom: 0;}
    </style>
</head>
<body>
    <div id="student_invite_teacher_box" class="apply_cf_box">
        <h4 class="text_red text_big">尊敬的${realname!""}老师：</h4>
        <div class="text_small">您好！欢迎回到一起作业！一起作业增加了很多新功能，赶快体验一下吧！</div>
    </div>
    <#--邀请我的学生 start-->
    <#if studentList?? && studentList?has_content>
        <div class="text_small text_gray_6 text_bold spacing_vox_top">请选择邀请您的学生，只可以选择一个学生：</div>
        <div class="sms_invite_box">
            <div id="invite_student_list" class="personnel_label studentList">
                <ul class="jcarousel-skin-tango" style="float: left;">
                    <#list studentList as s>
                        <li style=" position: relative;" class="clazz_book_list_item">
                            <label>
                                <b class="picture">
                                    <i <#if s.choose> class="checkboxs checkboxs_active" <#else> class="checkboxs" style=" display: none; " </#if> value="${s.userId!}" type="${s.type!}"></i>
                                    <img onerror="this.onerror='';this.src='<@app.avatar href=""/>'" src="<@app.avatar href="${s.userAvatar!}"/>" />
                                </b>
                                <span>${s.userName!}<br/>(${s.userId!})</span>
                            </label>
                        </li>
                    </#list>
                </ul>
            </div>
            <div class="clear"></div>
        </div>
    </#if>
    <#--邀请我的学生 end-->

    <p class="text_red text_small text_center">*只有当您布置并检查一份有10名以上学生完成的作业时，您最后选择的邀请人方可获得邀请奖励。</p>
    <div class="text_center" style="padding: 20px 0 10px;">
        <a id="to_homework_btn" class="btn_mark btn_mark_primary" href="javascript:void(0);" ><strong>去布置作业</strong></a>
        <a id="ignore_current_btn" class="btn_mark btn_mark_small btn_mark_link" href="javascript:void(0);" ><strong>忽略本次</strong></a>
        <a id="ignore_all_btn" class="btn_mark btn_mark_small btn_mark_link" href="javascript:void(0);" ><strong>不再提示</strong></a>
    </div>
    <script type="text/javascript">
        function selectUser(_this){
            var _s = _this.find('i');
            if(_s.hasClass('checkboxs_active')){
                _s.removeClass('checkboxs_active').hide();
            }else{
                _this.closest("ul").find("i[class*='checkboxs']").each(function(){
                    $(this).removeClass('checkboxs_active').hide();
                });
                _s.addClass('checkboxs_active').show();
            }
        }

        $(function(){
            var jcarouselLiLength = $(".jcarousel-skin-tango").find("li").length;

            if(jcarouselLiLength > 4){
                $(".teacherstList, .studentList").find(".jcarousel-skin-tango").jcarousel({scroll : 4, itemFallbackDimension: 570}).show();
            }

            var inviteAction = new $17.Model({
                to_homework_btn     : $("#to_homework_btn"),
                ignore_current_btn  : $("#ignore_current_btn"),
                ignoreallUrl        : "/teacher/invite/ignoreall.vpage",
                ignore_all_btn      : $("#ignore_all_btn"),
                nomoreUrl           : "/teacher/invite/nomore.vpage"
            });
            inviteAction.extend({
                init: function(){
                    var $this = this;

                    $this.to_homework_btn.on("click", function(){
                        var student = $("#invite_student_list").find("i.checkboxs_active");

                        if(student.length == 0){
                            alert("请选择要布作业的学生");
                            return false;
                        }

                        App.postJSON("/teacher/invite/studentactivateteacher.vpage", {
                            userList : [{
                                userId  : student.attr("value"),
                                type    : student.attr("type")
                            }]
                        }, function(data){
                            if(data.success){
                                setTimeout(function(){ parent.location.href = "/teacher/homework/batchassignhomework.vpage"; }, 200);
                            }else{
                                alert(data.info);
                                parent.jQuery.prompt.close();
                            }
                        });
                        return false;
                    });

                    $this.ignore_current_btn.on("click", function(){
                        $.post($this.ignoreallUrl, function(data){
                            if(!data.success){
                                alert("啊哦，设置失败了！");
                            }
                            parent.jQuery.prompt.close();
                        });
                    });

                    $this.ignore_all_btn.on("click", function(){
                        $.post($this.nomoreUrl, function(data){
                            if(!data.success){
                                alert("啊哦，设置失败了！");
                            }
                            parent.jQuery.prompt.close();
                        });
                    });
                }
            }).init();

            $("#gotohomework_but").live("click", function(_this){
                var studentIds = [];
                var teacherIds = [];
                var _num       = 0;

                $('.studentList .addtwo').each(function(){
                    studentIds.push({userId:$(this).attr("value"), type:$(this).attr("type")});
                });

                $(".teacherstList .addtwo").each(function(){
                    studentIds.push({userId:$(this).attr("value"), type:$(this).attr("type")}) ;
                    teacherIds.push({userId:$(this).attr("value"), type:$(this).attr("type")});
                });

                if(studentIds.length == 0 && teacherIds.length == 0){
                    alert("请选择您要邀请的老师或者学生。");
                    return false;
                }

                if(teacherIds.length > 1 ){
                    alert("最多可以选择一位邀请的老师");
                    return false;
                }

                teacherIds.length == 0 ? _num = 1 : _num = 2;
                if ( studentIds.length > _num ) {
                    alert( "最多可以选择 1 个学生！" );
                    return false;
                }
                _this.postJSON("/teacher/invite/studentactivateteacher.vpage", {userList : studentIds}, function( data ){});
            }, 1000);

            $(".picture").on("click", function(){
                selectUser($(this));
            });

            $(".picture_t").on("click", function(){
                selectUser($(this));
            });
        });
    </script>
</body>
</html>