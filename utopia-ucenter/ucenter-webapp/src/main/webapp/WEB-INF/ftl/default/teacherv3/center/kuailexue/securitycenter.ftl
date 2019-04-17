<div class="class-module mt-20">
    <div class="module-head bg-f6 clearfix">
        <div class="title">账号安全</div>
    </div>
    <div class="accountSecurity-box">
        <div class="acs-list">
            <a href="javascript:;" class="green_btn pad20 fr showAndHide" data-ref="#v-modifyPW">修改密码</a>
            <div class="acs-image"><img src="<@app.link href="public/skin/teacherv3/images/personal/image01.png"/>"></div>
            <div class="acs-title">
                <p class="t-1">登录密码：已设置<span class="icon-current"></span></p>
                <p class="t-2">安全性高的密码，可以使账号更安全</p>
            </div>
        </div>
        <li id="v-modifyPW" class="personalData-list" style="display: none;">
            <#if mobile?has_content>
                <dl>
                    <dt>验证手机：</dt>
                    <dd>
                        <div class="telephone">${mobile!''} <a id="send_pwchange_validate_code" href="javascript:;" class="code_btn"><span>获取短信验证码</span></a></div>
                    </dd>
                    <dt>短信验证码：</dt>
                    <dd>
                        <input id="validatecode" type="text" class="txt changepw-vc">
                        <span class="icon-arrow">（*必填）</span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    </dd>
                    <dt>新的登录密码：</dt>
                    <dd>
                        <input id="password" type="text" class="txt" placeholder="请输入1-16位任意字符，字母区分大小写">
                        <span class="icon-arrow">（*必填）</span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    </dd>
                    <dt>再次输入新密码：</dt>
                    <dd>
                        <input id="verify_password" type="text" class="txt" placeholder="请输入1-16位任意字符，字母区分大小写">
                        <span class="icon-arrow">（*必填）</span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    </dd>
                </dl>
                <div class="module-foot">
                    <a href="javascript:;" class="btn gray_btn cancelBtn">取消</a>
                    <a id="reset_user_password_button" href="javascript:;" class="btn">确定</a>
                </div>
            <#else>
                <div class="w-form-table">
                    <div style="text-align: center; padding: 20px 0 15px 0;">请先绑定手机，以免修改密码后遗忘不能找回</div>
                    <dl>

                        <dd class="form-btn center">
                            <a class="w-btn w-btn-green w-btn-small cancelBtn" style="display: none" href="javascript:void(0);">取消</a>
                            <a class="w-btn  w-btn-small" id="gotobindmobile" href="javascript:void(0);">绑定手机</a>
                        </dd>
                    </dl>
                </div>
            </#if>
        </li>
    </div>
    <div class="accountSecurity-box">
        <div class="acs-list">
            <#if mobile?has_content>
                <a href="javascript:;" class="green_btn pad20 fr showAndHide" data-ref="#v-modifyMobile">更换手机</a>
            <#else >
                <a href="javascript:;" class="green_btn pad20 fr showAndHide" data-ref="#v-modifyMobile">绑定手机</a>
            </#if>
            <div class="acs-image"><img src="<@app.link href="public/skin/teacherv3/images/personal/image02.png"/>"></div>
            <div class="acs-title">
                <#if mobile?has_content>
                    <p class="t-1">手机绑定：已设置<span class="icon-current"></span></p>
                <#else >
                    <p class="t-1">手机绑定：未设置<span class="icon-current"></span></p>
                </#if>
                <p class="t-2">绑定手机后，您即可享受手机登录、手机找回密码等服务。</p>
            </div>
        </div>
        <li id="v-modifyMobile" class="acs-main" style="display: none;">
            <div class="acs-head">
                <#if mobile?exists && mobile?has_content>
                    原号码：${mobile!}更换后，原号码不能再作为登录使用
                </#if>
            </div>
            <div class="personalData-list">
                <dl>
                    <dt><#if mobile?exists && mobile?has_content>新</#if>手机号码：</dt>
                    <dd>
                        <input id="newmobile" type="text" class="txt" placeholder="请输入手机号，可用于登录和找回密码">
                        <span class="icon-arrow">（*必填）</span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                        <p class="acs-code">
                            <a id="send_validate_code" data-oldmobile="${mobile!}" href="javascript:;" class="code_btn">获取短信验证码</a>
                        </p>
                    </dd>
                    <dt>短信验证码：</dt>
                    <dd>
                        <input id="validatecode" type="text" class="txt bindmobile-vc">
                        <span class="icon-arrow">（*必填）</span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    </dd>
                </dl>
                <div class="module-foot">
                    <a href="javascript:;" class="btn gray_btn cancelBtn">取消</a>
                    <a id="confirm_validate_code" href="javascript:;" class="btn">确定</a>
                </div>
            </div>
        </li>
    </div>
</div>
<#include "../validate.ftl"/>
<script type="text/javascript">
    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("safety");

        //修改按钮的展开和收起
        $("a.showAndHide").on("click",function(){
            var target = $(this).attr("data-ref");
            $(target).find("a.cancelBtn").trigger("click");
        });

        if($17.getHashQuery("type") != ""){
            $("#"+$17.getHashQuery("type")).show();
        }

        //取消按钮
        $("a.cancelBtn").on("click",function(){
            var $li = $(this).closest("li");
            $li.toggle("fast");
            //清除INPUT框的内容
            $li.find("input").each(function(){
                //清除input错误样式
                $(this).removeClass("w-int-error");
                $(this).val('');
                //清除错误消息内容
                $(this).siblings("span.errorMsg").empty();
            });
        });

        //密码提交
        $("#reset_user_password_button").on("click", function(){
            var $this = $(this);
            var success = validate("#v-modifyPW");
            if(success){
                var data = {
                    verify_code :   $(".changepw-vc").val(),
                    new_password : $("#verify_password").val()
                };
                if($this.isFreezing()){
                    return false;
                }
                $this.freezing();
                $.post('/ucenter/resetpwbycode.vpage',data, function(data) {
                    $this.thaw();
                    $17.alert(data.info,function(){
                        if(data.success){
                            $this.siblings("a.cancelBtn").trigger("click");

                            $17.voxLog({
                                module : "centerEditPassword",
                                op : "success"
                            });

//                            if($17.getHashQuery("type") != "v-modifyPW"){
//                                location.href = "#/teacher/center/securitycenter.vpage?type=v-modifyPW";
//                            } else {
//                                location.reload();
//                            }

                            // 修改密码成功之后，退出重新登录
                            $("a.sign-out").click();
                        }
                    });
                });
            }
        });

        $("#gotobindmobile").on("click", function() {
            $(this).siblings(".cancelBtn").trigger("click");
            if (!$("#mobileLi").is(":visible")) {
                $("#v-a-bindmobile").trigger("click");
            }

        });

        //手机验证
        var getCaptchaOccurr = 0;
        // 发送验证码
        $("#send_validate_code").on("click", function(){
            var $this = $(this);
            var $that = $("#newmobile");
            var $span = $that.siblings("span.errorMsg");
            if(!$17.isMobile($("#newmobile").val())){
                $span.html(template("t:错误提示_20140821151943",{errorMessage : "请填写正确的手机号码"}));
                $that.addClass("w-int-error");
                return false;
            }else{
                $that.removeClass("w-int-error");
                $span.html("");
            }
            var textAmverifcation = "";

            $this.siblings("span").html("正在努力发送，请稍候片刻...");
            var _mobile = $("#newmobile").val();

            App.postJSON("/teacher/center/sendmobilecode.vpage?mobile=" + _mobile, {}, function(data){
                if(data.success){
                    $span.html("验证码已发送" + textAmverifcation);
                }else{
                    $span.html(template("t:错误提示_20140821151943",{errorMessage : data.info + textAmverifcation}));
                }

                // var smsCodeBox = $("input[id='validatecode']");
               // smsCodeBox.next("span").html("<strong class="info">（*必填）请将您手机收到的验证码数字填写到此处</strong>");
                $17.getSMSVerifyCode($this, data);
            })
        });

        // 发送修改密码手机验证码
        $("#send_pwchange_validate_code").on("click", function() {
            var $this = $(this);
            $.post("/teacher/center/sendTCPWcode.vpage", {}, function(data) {
                if (data.success || data.timer) {
                    $17.getSMSVerifyCode($this, data);
                } else {
                    $17.alert(data.info);
                }
            })
        });


        $("#confirm_validate_code").on("click", function(){
            var success = validate("#mobileLi");
            if(!success){
                return false;
            }
            App.postJSON("/teacher/center/validatemobile.vpage?latestCode=" + $(".bindmobile-vc").val(),{},function(data){
                if(data.success){
                    $17.alert("手机修改成功",function(){
                        if($17.getHashQuery("type") != "v-modifyMobile"){
                            location.href = "#/teacher/center/securitycenter.vpage?type=v-modifyMobile";
                        } else {
                            location.reload();
                        }
                    });
                    return false;
                }else{
                    $17.alert(data.info);
                    return false;
                }
            })
        });

        //申请人工验证
        $("body").on("click", ".applicationManualVerification", function(){
            $.post("/teacher/nocertificationcodereceived.vpage", {mobile: $("#newmobile").val()}, function(data){
                if(data.success){
                    $17.alert("您的信息已提交，请保持手机畅通，我们会尽快与您联系！");
                    $17.tongji("申请人工验证 - 人工认证")
                }else{
                    $17.alert(data.info);
                }
            });
        });

        //解除绑定
        $(".relieveBut").on('click',function(){
            var id = $(this).data('id');
            $.prompt('<div style="text-align: center;"><h3>确定解绑？</h3> <br /><span>解绑后，将不能使用此QQ一键登录一起作业</span></div>',{
                title : '系统提示',
                buttons : {'解除绑定':true, "不解除": false},
                submit : function(e,v){
                    e.preventDefault();
                    if(v){
                        $.post('/teacher/center/unbindsso.vpage',{id : id},function(data){
                            if(data.success){
                                $('#bindQQ_'+id).remove();
                                $.prompt.close();
                            }else{
                                $17.alert('解除绑定失败！');
                            }
                        });
                    }else{
                        $.prompt.close();
                    }
                }
            });
        });
    });
</script>
<#if pandaria?? && pandaria>
<script type="text/javascript">
    $.prompt('<div style="text-align: center;">系统监测到您当前的账号密码安全性较低，请修改您的密码~</div>',{
        title : '系统提示',
        buttons : {'知道了':true},
        submit : function(){
            // 打开修改密码tab
            $("#v-modifyPW").show();
        }
    });
</script>
</#if>