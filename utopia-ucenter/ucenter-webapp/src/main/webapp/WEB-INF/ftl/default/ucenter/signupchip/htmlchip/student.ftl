<#import "../module.ftl" as com>
<@com.page title="学生" t=2>
<h1 class="reg_title">
    <#if dataKey?? && dataKey?has_content>
        <span class="rt">已有一起教育账号？<a href="/ssologinbind.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>" class="clrblue">绑定账号</a></span>
    </#if>
    完善个人信息
</h1>
<div class="reg_step">
    <p class="s_2"></p>
</div>
<#if dataKey?? && dataKey?has_content>
    <input name="dataKey" id="dataKey" type="hidden" value="${dataKey}">
<#else>
    <input name="dataKey" id="dataKey" type="hidden" value="">
</#if>
<div class="reg_from">
    <!--reg_student_info-->
    <ul class="loginbox" id="signup_content_box">
        <li class="inp"><b class="tit"><i>*</i> 真实姓名(必填)：</b>
            <input name="realname" class="require" data-label="真实姓名" type="text" value="${defUserName!''}" id="realname" autocomplete="off">
            <span class="hint"></span>
        </li>
        <#--<li class="inp"><b class="tit"><i>*</i> 密码(必填)：</b>
            <input name="password" type="password" class="require" data-label="密码" value="" id="password" autocomplete="off">
            <span class="hint"></span>
        </li>
        <li class="inp"><b class="tit"><i>*</i> 确认密码(必填)：</b>
            <input name="verify_password" type="password" class="require" data-label="确认密码" value="" id="verify_password" autocomplete="off">
            <span class="hint"></span>
        </li>-->

        <#--<li class="inp"><b class="tit">手机号码：</b>
            <input type="text" name="mobile" class="required" data-label="家长手机号" id="mobile" autocomplete="off" placeholder="手机可以用于登录和找回密码"/>
            <span class="hint"></span>
        </li>

        <li style="padding-bottom: 10px;" class="pad phoneType">
            <a id="get_captcha_but" style="padding: 5px 10px;" class="reg_btn reg_btn_orange reg_btn_small" href="javascript:void(0);">
                <span>免费获取短信验证码</span>
            </a>
            <span class="hint"></span>
        </li>

        <li class="inp"><b class="tit">验证码：</b>
            <input type="text" name="captcha" class="required" data-label="验证码" id="captcha_box" autocomplete="off" placeholder="验证码" data-role="student" data-content-id="smsCodeBox"/>
            <span class="hint"></span>
        </li>-->

        <li class="inp" id="selectCodeBox"><b class="tit"><i>*</i>老师给你号码了？</b>
            <a href="javascript:void(0);" class="w-int click-select-code" data-type="yes" style="width: 194px; text-align: center;overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">点击输入老师给的号码</a>
            <span class="hint"></span>
            <#--<a href="javascript:void(0);" class="w-int click-select-code" data-type="no" style="width: 78px; text-align: center;">没有号码</a>-->
            <#--<input name="clazzId" type="text" value="" data-label="班级编号" id="clazzId" autocomplete="off">-->
            <#--<span class="hint"></span>-->
        </li>
        <#--<li class="inp"><b class="tit">邀请人(可不填)：</b>
            <input name="invite_info" type="text" value="" id="invite_info" autocomplete="off" data-role="student">
            <span class="hint"></span>
        </li>-->
        <li class="inp txt pad">
            <span class="rememberme">
                <s id="accept_protocol" class="checku"><i></i></s>我已经阅读并接受
                <a class="clrblue" title="用户协议" href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/agreement.vpage" target="_blank">《一起教育用户协议》</a>
                <span class="hint"></span>
            </span>
        </li>
        <li class="inp pad"><a id="register_student_btn" href="javascript:void(0);" class="reg_btn submitBtn" style=" width: 134px;">提交</a>
        </li>
    </ul>
    <div class="type_three">
        <span class="tt-icon student">
            <strong>我是学生</strong>
            <#if !invitation??><a id="forback" href="/signup/index.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>" class="reg_btn reg_btn_well">重新选择用户类型</a></#if>
        </span>
    </div>
    <div class="clear"></div>
</div>
<script type="text/html" id="T:输入班级号">
    <div id="loadClazzBox">
        <ul class="loginbox" style="text-align: left; width: auto; float: left;">
            <li class="inp">
                <b class="tit"><i>*</i> 号码：</b>
                <input name="code" class="w-int" id="clazzId" type="text" value="" placeholder="老师手机号或老师id"/>
                <span class="hint" style="padding-left: 163px;"></span>
            </li>
            <li class="inp pad" style="padding-bottom: 20px;"><a href="javascript:void(0);" class="w-btn w-btn-well click-clazzId-submit">确定</a></li>
        </ul>
        <div style="color: #ccc; clear: both;">
            注：如果老师给你的是账号和密码，请返回登录
        </div>
    </div>
</script>
<script type="text/html" id="T:选择班级列表">
    <div style="text-align: left;" id="allSearchClazzItem">
        <%for(var i = 0; i < clazzList.length; i++){%>
            <a href="javascript:void(0);" class="w-int click-select-code" data-type="clazzList" data-clazzid="<%=clazzList[i].clazzId%>" data-clazzname="<%=clazzList[i].clazzName%>" title="<%=clazzList[i].clazzName%>" style="margin-right: 11px; width: 78px; text-align: center; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; margin-bottom: 7px;"><%=clazzList[i].clazzName%></a>
        <%}%>
    </div>
    <div style="margin: 20px 0 20px -8px;">
        <a href="javascript:void(0);" class="w-btn w-btn-well click-select-submit">确定</a>
    </div>
</script>
<script type="text/javascript">
    $(function () {
        $17.tongji("注册1-学生");
        <#if invitation??>$17.tongji("学生分享分享-进入学生注册页");</#if>

        var captchaBox = $("#captcha_box");
        var mobile = $("#mobile");
        var realname = $("#realname");
        var webSource = "";//记录是否填写班级
        var $classCode = "";//设置班级编号
        var $classCodeName = "";//设置班级名称
        var $teacherId = "";//设置老师ID

        //选择编号
        $(document).on("click", ".click-select-code", function(){
            var $this = $(this);
            $this.siblings().removeClass("w-int-active");
            $this.siblings('span.hint').html("<i></i>").closest('li').removeClass('err');
            if($this.attr("data-type") == "yes"){
                $.prompt(template("T:输入班级号", {}), {
                    title: "输入老师给你的号码",
                    focus : 0,
                    buttons: {  },
                    position:{width : 500}
                });
            }else{
                $this.addClass("w-int-active");
            }
        });

        //确认提交编号
        $(document).on("click", ".click-clazzId-submit", function(){
            var $this = $(this);
            var $clazzId = $("#clazzId");

            if($this.hasClass("dis")){
                return false;
            }

            if( $17.isBlank($clazzId.val()) ){
                $clazzId.addClass("w-int-error");
                return false;
            }else{
                $clazzId.removeClass("w-int-error");
            }

            var $classIdValue = $clazzId.val();
            if($classIdValue.search(/c/i) > -1){
                $classIdValue =  $classIdValue.replace(/c/i, "");
            }

            $this.addClass("dis");
            $.post("/signup/checkclazzinfo.vpage", {id : $classIdValue}, function(data){
                if(data.success){
                    if(data.clazzList.length > 0){
                        $("#loadClazzBox").html( template("T:选择班级列表", {clazzList : data.clazzList}) );
                        $(".jqititle").text("选择你的班级");
                        // 天津新体系，记录老师id
                        if (data.clazzList[0].creatorType == "SYSTEM") {
                            $teacherId = $classIdValue;
                        }
                    }else{
                        $this.removeClass("dis");
                        $clazzId.parent().addClass("err");
                        $clazzId.siblings(".hint").html("<i></i>没有找到班级");
                    }
                }else{
                    $this.removeClass("dis");
                    $clazzId.parent().addClass("err");
                    $clazzId.siblings(".hint").html("<i></i>"+data.info);
                }
            });
        });

        //确认选择班级
        $(document).on("click", ".click-select-submit", function(){
            var selectCodeBox = $("#selectCodeBox");
            var allSearchClazzItem = $("#allSearchClazzItem");

            if(!allSearchClazzItem.find("a").hasClass("w-int-active")){
                return false;
            }

            $classCode = allSearchClazzItem.find("a.w-int-active").attr("data-clazzid");
            $classCodeName = allSearchClazzItem.find("a.w-int-active").attr("data-clazzname");
            selectCodeBox.addClass('cor').children('span.hint').html('<i></i>');

            $.prompt.close();

            selectCodeBox.find(".click-select-code[data-type='yes']").text($classCodeName);
            selectCodeBox.find(".click-select-code[data-type='yes']").attr("title", $classCodeName);
            selectCodeBox.find(".click-select-code[data-type='no']").hide();
            selectCodeBox.find("b").html("<i>*</i>加入班级");
        });

        $("#forback").on("click", function(){
            $17.tongji("注册2-学生-重选账号");
        });

        /*免费获取短信验证码*/
        $("#get_captcha_but").on('click',function(){
            var $this = $(this);
            var mobileNumber = $("#mobile").val();
            if($17.isBlank(mobileNumber)){
                $this.siblings(".hint").html("<i></i>请填写正确的手机号码").css({'color' : 'red'});
                return false;
            }

            if($this.hasClass("btn_disable")){
                return false;
            }

            if($17.isMobile(mobileNumber)){
                $.post("/signup/smsignsvc.vpage", {mobile : mobileNumber, cid: "${contextId}"}, function(data){
                    var smsCodeBox = $("input[data-content-id=smsCodeBox]");
                    smsCodeBox.next("span").html("<b class='vox_custom_icon vox_custom_icon_1'></b><span class='text_azure'>请将手机收到的验证码数字填写到此处</span>");
                    $this.siblings(".hint").html(data.info).css({'color' : 'red'});
                    $17.getSMSVerifyCode($this, data);
                });
            }
        });

        mobile.on("keyup", function(){
            if($17.isBlank($(this).val())){
                captchaBox.val('');
                return false;
            }
        });

        //提交注册
        $('#register_student_btn').on('click', function () {
            var $this = $(this);
            var success = validate();
            var dataKey = $("#dataKey");

            if(captchaBox.val() && !mobile.val()){
                mobile.parent().addClass("err").end().siblings(".hint").html("<i></i>手机号码不可为空");
                return false;
            }

            if(mobile.val() && !captchaBox.val()){
                captchaBox.parent().addClass("err").end().siblings(".hint").html("<i></i>请输入验证码");
                return false;
            }

            if($17.isBlank($classCode)){
                var selectCodeBox = $("#selectCodeBox");
                selectCodeBox.addClass("err").children("span.hint").html("<i></i>请输入老师给的号码");
                return false;
            }else{
                $classCode = $17.getClassId($classCode);
                webSource = "classCode";
            }
            if (success) {
                if (realname.val().length > 20) {
                    realname.parent().addClass("err").end().siblings(".hint").html("<i></i>请不要使用过长的名称");
                    return false;
                }

                // 密码随机生成
                var randomNum = "";
                for(var i=0;i<6;i++) {
                    randomNum += Math.floor(Math.random()*10);
                }

                var data = {
                    role            : 'ROLE_STUDENT',
                    userType        : 3,
                    realname        : $('#realname').val(),
                    password        : randomNum,
                    childRole       : 'ROLE_STUDENT',
                    clazzId         : $classCode,
                    registerType    : 0,
                    inviteInfo      : $('#invite_info').val(),
                    mobile          : $("#mobile").val(),
                    code            : $("#captcha_box").val(),
                    dataKey         : dataKey.val(),
                    webSource        :  webSource,
                    invitation      : "${invitation!}",
                    teacherId       : $teacherId
                };

                $17.tongji("注册2-学生-提交");

                App.postJSON('/signup/signup.vpage', data, function (data) {
                    if (data && data.success) {
                        $17.tongji("signup/htmlchip/student.vpage_#register_student_btn","regist_success_student");
                        signupSuccess({j_username: data.row, j_password: randomNum, _spring_security_remember_me: true }, '${(ProductConfig.getMainSiteBaseUrl())!''}/student/index.vpage', 'student');
                    } else {
                        var attrs = data.attributes;
                        if(attrs){
                            $.each(attrs, function (key, value) {
                                var el = $('#' + key);

                                if(key == "dirty"){
                                    $17.alert("班级人数已到达上限.");
                                    $17.tongji("注册4-学生-加入的班级人已满");
                                }else{
                                    if (el.length > 0) {
                                        el.parent().addClass('err');
                                        el.siblings("span").html("<i></i>" + value);
                                    } else {
                                        if (attrs.none) {
                                            $17.alert(attrs.none);
                                        }else{
                                            $17.alert(value);
                                        }
                                    }
                                }
                            });
                        }else{
                            $17.alert(data.clazzId || '网络错误，请刷新页面重新输入');
                        }
                    }
                }, function (data) {
                    alert("网络请求失败，请稍等重试或者联系客服");
                });
            }
            return false;
        });
    });
</script>
</@com.page>