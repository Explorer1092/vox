<!doctype html>
<html>
<head>
    <meta name="viewport" content="target-densitydpi=device-dpi, width=640, user-scalable=no"/>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "template"] css=[] />
    <@sugar.site_traffic_analyzer_begin />
    <style type="text/css">
        html, body, h1, h2, h3, h4, h5, h6, ul, li, p, input, dl, dt, dd{padding: 0; margin: 0;}
        html, body{ width: 100%; background-color: #3998f1; }
        html, body, input{font: 28px/160% "Adobe 黑体 Std", regular; color: #464646; }
        ul, li{ list-style: none; }
        a, input{ outline: none;}
        a{ text-decoration: none; color: #6691fe; }
        /*btn_mark*/
        .btn_mark{ background-color: #53c451; color: #fff; border-radius: 6px; cursor: pointer; display: inline-block; padding: 14px 10px; text-align: center; text-decoration: none; font-size: 28px;}
        /*.btn_mark:hover{ background-color: #87eb86;}*/
        .btn_mark_block{ display: block;}
        .btn_orange{ background-color: #ffcc33;}
        .btn_orange:hover{ background-color: #ffcc33;}
        .btn_disable, .btn_disable:hover{ background-color: #bababa; cursor: default;}
        /*form_main*/
        .form_main { margin: 12px 0;}
        .form_main h2 { font-size: 34px; font-weight: normal; margin-bottom: 40px; clear: both; color: #fff; text-align: center;}
        .form_main p.fix_box{ margin: 120px 0 30px; text-align: center; color: #333;}
        .form_main ul.fm_box{border-radius: 6px; background-color: #fff; overflow: hidden; }
        .form_main li {  clear: both; overflow: hidden; border-bottom: 1px solid #e5e5e5; position: relative;}
        .form_main li .txt{ padding: 17px 0; line-height: 190%; width: 100%; text-indent: 170px; display: block; background-color: #fff; color: #464646; border: 0; font-size: 32px;}
        .form_main li .person, .form_main li .mis-psd, .form_main li .mis-mobile{ background: url(<@app.link href="public/skin/mobile/pc/images/icon-logic.png?1.0.1"/>) no-repeat 0 0; width: 68px; height: 101px; display: inline-block; position: absolute; left: 60px; top: 0;}
        .form_main li .mis-psd{ background-position: 0 -101px;}
        .form_main li .mis-mobile{ background-position: 0 -188px;}
        /*main*/
        .main{ padding: 0 24px; min-width: 320px; max-width: 640px; margin: 0 auto;}
        .main .inde-title{ color: #fff; font-size: 26px; line-height: 160%; margin: 40px 0 0 0; text-align: center;}
        .main .form_main .submit_box{ margin: 30px 0;}

        .select-clazz{ margin: 20px 0; overflow: hidden;}
        .select-clazz ul{ overflow: hidden; padding: 5px 0;}
        .select-clazz li{float: left; font-size: 28px; border: 3px solid #eee; padding:10px 0 10px 45px;; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-radius: 8px; cursor: pointer; width: 224px; margin: 0 20px 15px 0; background: url(<@app.link href="public/skin/mobile/pc/images/n-icon-radio.png"/>) no-repeat #fff 2px -32px;}
        .select-clazz li.active{ border-color: #1779d4; background-color: #55adff; color: #fff; background-position: 2px 16px;}
        .infoBox{display: none;}
    </style>
</head>
<body>
    <div id="moduleContainer">
        <div class="main" data-reg-step="1">
            <h1 style="background: url(<@app.link href="public/skin/mobile/pc/images/logo-english.png"/>) no-repeat center 0; height: 130px; margin: 70px 0 45px;"></h1>
            <div class="inde-title" style="">
                请输入老师的手机号，设置老师：
            </div>
            <div class="form_main">
                <ul class="fm_box">
                    <li>
                        <i class="mis-mobile"></i>
                        <input class="txt" type="tel" id="teacherMobile" value="" placeholder="输入老师手机号" maxlength="11">
                    </li>
                </ul>
                <div class="submit_box infoBox" style="color: #fff; "></div>
                <div class="submit_box">
                    <a href="javascript:void(0);" id="joinClazz" class="btn_mark btn_mark_block">确定</a>
                </div>
                <div style="padding: 10px 0; color: #fff; font-size: 18px; text-align: center;">
                    注意：提醒老师用自己手机号注册并激活班级，否则学生无法使用！
                </div>
            </div>
        </div>
    </div>

    <script type="text/html" id="T:data-reg-step-2">
    <div class="main" data-reg-step="2">
        <div class="inde-title" style="text-align: left;">请选择你的班级： </div>
        <div class="select-clazz">
            <div class="cc-b">
                <ul>
                    <%for(var i = 0; i < clazzList.length; i++){%>
                    <li data-clazzid="<%=clazzList[i].clazzId%>" class="v-selectClazzId"><%=clazzList[i].clazzName%></li>
                    <%}%>
                </ul>
            </div>
        </div>
        <div class="inde-title" style="text-align: left;">注册学生账号加入班级： </div>
        <div class="form_main">
            <ul class="fm_box">
                <li>
                    <i class="mis-mobile"></i>
                    <input id="studentMobile" class="txt" type="tel" value="" placeholder="输入自己的手机号" maxlength="11">
                </li>
                <li>
                    <i class="person"></i>
                    <input id="userName" class="txt" type="text" value="" placeholder="请输入姓名" maxlength="6">
                </li>
                <li>
                    <i class="mis-psd"></i>
                    <input id="userPwd" class="txt" type="password" value="" placeholder="请输入密码" maxlength="16">
                </li>
                <li>
                    <i class="mis-psd"></i>
                    <input id="userVerPwd" class="txt" type="password" value="" placeholder="请输入确认密码" maxlength="16">
                </li>
            </ul>
            <div class="submit_box infoBox" style="color: #fff; "></div>
            <div class="submit_box">
                <a href="javascript:void(0);" id="submitRegister" class="btn_mark btn_mark_block">注册</a>
            </div>
        </div>
    </div>
    </script>

    <script type="text/html" id="T:data-reg-step-3">
        <div class="main" data-reg-step="3">
            <div class="inde-title" style="text-align: left;">绑定自己的手机：</div>
            <div class="form_main">
                <ul class="fm_box">
                    <li>
                        <input class="txt" id="mobileSecurityCode" type="tel" maxlength="6" value="" placeholder="输入短信验证码" style="padding-left: 20px;">
                    </li>
                </ul>
                <div class="submit_box infoBox" style="color: #fff; "></div>
                <div style="height: 88px; padding-top: 15px;">
                    <p style="float: left; color: #fff; line-height: 120%">验证码已发送到手机：<br/><span class="tel"><%=userMobile%></span></p>
                    <a href="javascript:void(0);" class="btn_mark btn_orange v-getCodeBtn" style="float: right;"><span>免费获取短信验证码</span></a>
                </div>
                <div class="submit_box">
                    <a href="javascript:void(0);" id="submitVerCodeBtn" class="btn_mark btn_mark_block">确定</a>
                </div>
            </div>
        </div>
    </script>

    <script type="text/html" id="T:data-reg-step-4">
        <div class="main" data-reg-step="4">
            <div class="inde-title">注册成功！请牢记你的学号！<br/>建议截屏或拍照保存。</div>
            <div style="line-height: 180%; font-size: 40px; color: #fff; text-align: left; padding: 50px 0; text-indent: 120px;">
                <p>账号：<span><%=data.userMobile%></span></p>
                <p>密码：<span><%=data.userPass%></span></p>
            </div>
            <div class="submit_box">
                <a href="<%=data.backLink%>&uid=<%=data.userId%>" class="btn_mark btn_mark_block">查看学习资料</a>
            </div>
        </div>
    </script>


    <script type="text/javascript">
        $(function(){
            var _templateData = {
                teacherId : null,
                clazzId : null,
                userMobile : null,
                userId : null,
                userName : null,
                userPass : null,
                code : null,
                backLink : $17.getQuery("backLink") || "javascript:void(0);",
                webSource : $17.getQuery("webSource") || "o2oMobile"
            };
            $17.tongji("O2O学生注册-loading");
            //step-1输入手机号码
            $(document).on("click", "#joinClazz", function(){
                var teacherMobile = $("#teacherMobile").val();

                if( !$17.isNumber(teacherMobile) ){
                    infoAlert("请输入正确的手机号", "#teacherMobile");
                    return false;
                }

                $.post("/signup/checkclazzinfo.vpage", { id : teacherMobile}, function(data){
                    if(data.success){
                        _templateData.teacherId = teacherMobile;
                        showFlag(2, {
                            clazzList : data.clazzList
                        });
                    }else{
                        infoAlert(data.info, "#teacherMobile");
                    }
                });

                $17.tongji("O2O学生注册-老师手机号提交");
            });

            //step-2选择班级
            $(document).on("click", ".v-selectClazzId", function(){
                var $this = $(this);

                _templateData.clazzId = $this.attr("data-clazzid");
                $this.addClass("active").siblings().removeClass("active");

                $17.tongji("O2O学生注册-选择班级");
            });

            //step-2提交注册
            $(document).on("click", "#submitRegister", function(){
                var $ts = $(this);
                var studentMobile = $("#studentMobile");
                var userName = $("#userName");
                var userPwd = $("#userPwd");
                var userVerPwd = $("#userVerPwd");

                if( $ts.hasClass("dis") ){
                    return false;
                }

                if( !$17.isNumber(_templateData.clazzId) ){
                    infoAlert("请选择要加入的班级", ".v-selectClazzId");
                    return false;
                }

                if( !$17.isNumber(studentMobile.val()) ){
                    infoAlert("请输入正确的手机号", "#studentMobile");
                    return false;
                }

                if( $17.isBlank(userName.val()) || !$17.isValidCnName(userName.val()) ){
//                if( $17.isBlank(userName.val()) || !$17.isCnString(userName.val()) ){
                    infoAlert("请输入中文姓名", "#userName");
                    return false;
                }

                if( $17.isBlank(userPwd.val()) ){
                    infoAlert("请输入密码", "#userPwd");
                    return false;
                }

                if( $17.isBlank(userVerPwd.val()) ){
                    infoAlert("请输入确认密码", "#userVerPwd");
                    return false;
                }

                if( userPwd.val() != userVerPwd.val() ){
                    infoAlert("密码不一致", "#userVerPwd");
                    return false;
                }

                _templateData.userMobile = studentMobile.val();
                _templateData.userName = $.trim(userName.val());
                _templateData.userPass = userVerPwd.val();


                $ts.addClass("dis");
                $.post("/signup/smsignsvc.vpage", {mobile : _templateData.userMobile, cid: "${contextId}"}, function(data){
                    if(data.success){
                        showFlag(3, {userMobile:_templateData.userMobile}, function(){
                            setTimeout(function(){
                                $17.getSMSVerifyCode($(".v-getCodeBtn"), data);
                            }, 100);
                        });
                    }else{
                        infoAlert(data.info, "#studentMobile");
                        $ts.removeClass("dis");
                    }
                });

                $17.tongji("O2O学生注册-提交注册");
            });

            //获取验证码
            $(document).on("click", ".v-getCodeBtn", function(){
                var $this = $(this);

                if($this.hasClass("btn_disable")){
                    return false;
                }

                $.post("/signup/smsignsvc.vpage", {mobile : _templateData.userMobile, cid: "${contextId}"}, function(data){
                    if(data.success){
                        $17.getSMSVerifyCode($this, data);
                    }
                });
                $17.tongji("O2O学生注册-获取验证码");
            });

            //提交验证码注册
            $(document).on("click", "#submitVerCodeBtn", function(){
                var $this = $(this);
                _templateData.code = $("#mobileSecurityCode").val();

                if( !$17.isNumber(_templateData.code) ){
                    infoAlert("请输入正确的验证码", "#mobileSecurityCode");
                }

                if($this.hasClass("dis")){
                    return false;
                }

                var data = {
                    role            : 'ROLE_STUDENT',
                    userType        : 3,
                    realname        : _templateData.userName,
                    password        : _templateData.userPass,
                    childRole       : 'ROLE_STUDENT',
                    clazzId         : _templateData.clazzId,
                    registerType    : 0,
                    mobile          : _templateData.userMobile,
                    code            : _templateData.code,
                    inviteInfo      : "",
                    dataKey         : "",
                    webSource        : _templateData.webSource,
                    invitation      : "",
                    teacherId       : _templateData.teacherId
                };

                $17.tongji("O2O学生注册-确认验证码");
                $this.addClass("dis");
                App.postJSON('/signup/signup.vpage', data, function (data) {
                    if (data && data.success) {
//                        $.get("/student/systemclazz/linkteacher.vpage", {clazzId : _templateData.clazzId, teacherId : _templateData.teacherId, clazzName : ""}, function(data) {});

                        //成功
                        _templateData.userId = data.row;

                        showFlag(4, {data:_templateData});
                        $17.tongji("O2O学生注册-注册成功");
                    } else {
                        //失败
                        var attrs = data.attributes;
                        if(attrs){
                            $.each(attrs, function (key, value) {
                                var el = $('#' + key);

                                if(key == "userId"){
                                    infoAlert("您已经注册姓名为"+_templateData.userName+"的账号"+value+"请直接【<a href='"+_templateData.backLink+"'>登录</a>】", "#mobileSecurityCode");
                                }

                                if(key == "dirty"){
                                    infoAlert("班级人数已到达上限", "#mobileSecurityCode");
                                }else{
                                    infoAlert(value, "#mobileSecurityCode");
                                }
                            });
                        }else{
                            infoAlert(data.info, "#mobileSecurityCode");
                        }
                    }
                    $this.removeClass("dis");
                }, function (data) {
                    infoAlert("网络请求失败，请稍等重试或者联系客服!");
                    $this.removeClass("dis");
                });
            });

            //错误提示
            function infoAlert(info, focusId){
                $(".infoBox").slideDown().html(info);

                if(focusId){
                    $(focusId).focus();

                    setTimeout(function(){
                        $(".infoBox").slideUp();
                    }, 3000);
                }
            }

            //显示第几步
            function showFlag(key, opt, callBack){
                $("#moduleContainer").html( template("T:data-reg-step-"+key, opt) );
                if(callBack){
                    callBack();
                }
            }
        });
    </script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
