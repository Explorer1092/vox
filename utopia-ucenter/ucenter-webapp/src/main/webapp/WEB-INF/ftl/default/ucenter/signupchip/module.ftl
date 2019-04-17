<#--TODO 这个页面不知道海用不用-->
<#macro page title="" t="1">
<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起教育科技_让学习成为美好体验</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "new_teacher.widget", "plugin.register"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>

    <@sugar.capsule css=["plugin.headfoot"] />
    <!--头部-->
    <div class="m-header">
        <div class="m-inner">
            <div class="logo"><a href="javascript:;"></a></div>
        </div>
    </div>

    <div class="register_box">
        <div id="signup_form_box">
            <#nested>
        </div>
        <!-- 注册成功提示 start -->
        <div id="signup_loading_box" class="loginbox" style="display:none; float: none; width: auto;">
            <h1 class="reg_title">注册新账号</h1>
            <div class="reg_step">
                <p class="s_2"></p>
            </div>
            <div class="finsh">
                注册中，请稍后... ...
            </div>
        </div>
        <div id="signup_success_box" style="display: none ; padding-bottom: 30px;">
            <h1 class="reg_title">注册新账号</h1>
            <div class="reg_step">
                <p class="s_3"></p>
            </div>
            <div class="reg_success">
                <div class="left"></div>
                <div class="right"></div>
                <div class="inner">
                    <div class="success_info">
                        <p class="info"><i class="reg_icon reg_icon_4"></i> 恭喜，注册成功！</p>
                        <p class="onbox">
                            <span class="sf">您的一起作业号是：<b id="success_id">-----------</b></span>
                        <span class="cf">
                            <a href="javascript:void(0);" id="download_userinfo_but"><i class="reg_icon reg_icon_2"></i> 下载我的学号</a><br/>
                            <#--<a href="javascript:void(0);" id="user_send_to_mobile"><i class="reg_icon reg_icon_3"></i> 把账号密码发送到手机</a>-->
                        </span>
                        </p>
                        <p class="ctn">
                            <#if t == 1>此号码将作为下次登录的账号，请牢记，如果您担心遗忘，请在【个人中心】安全设置中绑定手机或邮箱，绑定后用手机或邮箱均可登录一起作业。</#if>
                            <#if t == 2>此号码将作为下次登录的账号，请牢记，如果你担心遗忘，请在【个人中心】绑定家长手机号。</#if>
                            <#if t == 3>此号码将作为下次登录的账号，请牢记，如果您担心遗忘，请点击头像右下方【验证手机】，验证后可用手机号码登录一起作业。 </#if>
                        </p>
                        <p class="btn">
                            <#if t=1>
                                <a href="/teacher/guide/bindMobile.vpage" class="gray"><i>教师身份验证</i></a>
                            </#if>
                            <a id="signup_login_btn" href="javascript:void(0);" style="color: #39f;"><#if t=1>暂不验证，</#if>开始一起作业 <i class="reg_icon reg_icon_1"></i></a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
        <!-- 注册成功提示 end -->
    </div>
    <#include "../../layout/project.footer.ftl"/>
    <script type="text/javascript">
    function validate(){
        var errorCount = 0 ;

        if($("#accept_protocol").html()==""){
            $("#accept_protocol").parents('li').addClass("err");
            $("#accept_protocol").siblings(".hint").html("<i></i>您还没有接受协议哦");
            errorCount++;

            return false;
        }

        $(".require").each(function(){
            if($17.isBlank($(this).val())){
                $(this).parent().addClass('err');
                var errorMessage = $(this).data("label") + '不可为空';
                $(this).siblings('span').html("<i></i>"+errorMessage);
                errorCount++;
            }
        });

        if($("li.err").size() == 0 && $("li.cor").size() > 0 && errorCount==0){
            return true;
        }else{
            return false;
        }
    }

        function signupSuccess( data, url, role ) {
            $('#signup_form_box').hide();
            $('#signup_loading_box').show();
            setTimeout( function(){
                $("#signup_loading_box").remove();
                $("#signup_success_box").show();
                $("#success_id").html(data.j_username);
                if(role == "teacher") {
                    $("#send_message_btn").show();
                }
                $('#signup_login_btn').on('click', function(){
                    <#if t == 1>$17.tongji("注册3-老师-开始一起作业");</#if>
                    <#if t == 2>$17.tongji("注册3-学生-开始一起作业");</#if>
                    <#if t == 3>$17.tongji("注册3-家长-开始一起作业");</#if>

                    setTimeout(function(){ top.location.href = url; }, 200);
                    return false;
                });
                $('#send_message_btn').on('click', function(){
                    setTimeout(function(){ top.location.href = '/signup/sendpwmessage.vpage'; }, 200);
                    return false;
                });
            }, 4000);
        }

        $(function(){
            /*发短信/
            $("#user_send_to_mobile").on('click', function(){
                var $html = "<div class='spacing_vox' id='send_mobile_type'>手机号码：" +
                                "<input type='text' value='' id='send_mobile' style='width: 170px;' class='int_vox'/>" +
                                "<span class='err' style='font: 12px/1.125 arial; color: #f00;'></span>" +
                            "</div><div class='alert_vox alert_vox_block'><span class='text_small'>在上面输入手机号码，点击“发送”账号和密码将以短信形式发送到你指定的手机，注意请不要输错哦！</span></div>" +
                            "<script type='text/javascript'>$('#send_mobile').on('keyup', function(){if(!$17.isMobile($('#send_mobile').val())){$('#send_mobile_type .err').html(' 手机号码格式不对');}else{$('#send_mobile_type .err').html('')}});<\/script>";
                $.prompt($html, {
                    title   : "免费发送账号密码到手机",
                    buttons : { "取消" : false, "发送" : true},
                    focus   : 1,
                    submit  : function(e,v,m,f){
                         if(v){
                             var send_mobile = $("#send_mobile");
                             if(!$17.isMobile(send_mobile.val())){
                                 $("#send_mobile_type .err").html(" 手机号码格式不对");
                                 return false;
                             }
                             var data = {
                                 mobile : send_mobile.val()
                             };
                             App.postJSON("/ucenter/sendpwsms.vpage", data, function(data){
                                 if(data.success){
                                     $17.alert("发送成功!");
                                     $("#user_send_to_mobile").hide();
                                 }else{
                                     $17.alert("发送失败!");
                                 }
                             });
                         }
                    }
                });
            });
            /***/

            $("#download_userinfo_but").click(function(){
                <#if t == 1>$17.tongji("注册3-老师-下载学号");</#if>
                <#if t == 2>$17.tongji("注册3-学生-下载学号");</#if>
                <#if t == 3>$17.tongji("注册3-家长-下载学号");</#if>

                $(this).attr("href","/ucenter/fetchaccount.vpage");
            });

            $("#accept_protocol").on("click", function(){
                var _this = $(this);
                var html = $(this).html();
                if(html.toLowerCase() =="<i></i>"){
                    _this.html("");
                    $(".submitBtn").addClass("reg_btn_dis");
                }else{
                    _this.siblings('.hint').html("");
                    _this.html("<i></i>");
                    _this.parents("li").removeClass("err");
                    $(".submitBtn").removeClass("reg_btn_dis");
                }
            });

            $("input").live("focus blur change", function(e){
                var _this = $(this);
                var notice = "";
                var row = _this.parent();
                var _type = _this.attr("id");
                var span = _this.siblings("span");
                var condition = true;
                var errorMessage = "";
                var password = $("#password").val();
                var verify_password = $("#verify_password");
                if(e.type!="blur"){
                    switch (_type)
                    {
                    case "realname":
                        var value = _this.val().replace(/\s+/g, "");
                        condition = !(value.match(/[^\u4e00-\u9fa5]/g));
                        errorMessage = "请输入您的真实姓名,须为中文";
                        if(_this.data("role") == "teacher"){
                            notice = "请输入真实姓名，以便学生找到您";
                        }else{
                            notice = "请输入真实姓名";
                        }
                        break;
                    /*case "password":
                        if(_this.val().length > 16){
                            errorMessage = "密码不可超过16位";
                            condition = false;
                        }else{
                            if( verify_password.val() != ""){
                                if(_this.val() == verify_password.val()){
                                    verify_password.parent().removeClass("err").addClass("cor");
                                    verify_password.siblings('span').html("<i></i>");
                                }else{
                                    verify_password.parent().removeClass("cor").addClass("err");
                                    verify_password.siblings("span").html("<i></i>密码填写不一致，请重新填写");
                                }
                            }
                        }
                        notice = "请输入1—16位任意字符（字母区分大小写）";
                        break;
                    case "verify_password":
                        condition = (password == _this.val());
                        if( $("#password").val() != ""){
                            if(condition == true && _this.val() < 16){
                                $("#password").parent().removeClass("err").addClass("cor");
                                $("#password").siblings('span').html("<i></i>");
                            }
                        }
                        errorMessage = "密码填写不一致，请重新填写";
                        notice = "请再次输入密码";
                        break;*/
                    case "mobile":
                        condition = $17.isMobile(_this.val());
                        errorMessage = "请填写正确的手机号码";
                        notice = "请输入手机号，验证通过后可用于登录、找回密码";
                        break;
                    case "email":
                        condition = $17.isEmail(_this.val());
                        errorMessage = "请填写正确格式的邮箱";
                        notice = "请输入常用邮箱，验证通过后可用于登录和找回密码";
                        break;
                    case "clazzId":
                        var clazzId = _this.getClassId();
                        condition = ((clazzId.length >= 5 && _this.val().toUpperCase().indexOf("C") == 0) || (clazzId.length >= 5 && $17.isNumber(_this.val())));
                        errorMessage = "班级编号无效";
                        notice = "请向您的任课老师询问班级编号";
                        break;
                    case "invite_info":
                        if(_this.data("role") == "student"){
                            notice = "请输入邀请人的一起作业号";
                        }else{
                            notice = "请输入邀请人的一起作业号或手机号";
                        }
                        break;
                    default:
                        break;
                    }
                }
                if(e.type == "focus"){
                    if(!row.hasClass("err") && _this.val()==""){span.html(notice)}
                }else if(e.type == "blur"){
                    if(!row.hasClass("err") && _this.val()==""){
                        span.html("<i></i>");
                    }
                }else if(e.type == "change"){
                    if(!$17.isBlank(_this.val())){
                        if(!condition){
                            row.removeClass("cor").addClass("err");
                        }else{
                            row.removeClass("err").addClass("cor");
                            errorMessage = "";
                        }
                        span.html("<i></i>"+errorMessage);
                    }else{
                        if(_this.hasClass("require")){
                            errorMessage = _this.data("label") + '不可为空';
                            row.removeClass("cor").addClass("err");
                            span.html("<i></i>"+errorMessage);
                        }else{
                            row.removeClass("err");
                            span.html("");
                        }
                    }
                }
            });
        });
    </script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
</#macro>