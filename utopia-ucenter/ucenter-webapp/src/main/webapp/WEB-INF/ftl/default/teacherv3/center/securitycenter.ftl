<div class="w-base">
    <div class="w-base-title">
        <h3>账号安全 </h3>
    </div>
    <div class="t-security">
        <ul>
            <li>
                <div class="ts-icon"><span class="ts-ic ts-ic-1"></span></div>
                <div class="ts-note">
                    <h3><i class="w-icon-public w-icon-success"></i>登录密码：已设置</h3>
                    <p>安全性高的密码，可以使账号更安全。</p>
                </div>
                <div class="ts-btn">
                    <a class="w-btn w-btn-mini showAndHide" data-ref="#passwordLi" href="javascript:void(0);">修改密码</a>
                </div>
            </li>
            <li id="passwordLi" class="ts-detail" style="display: none;">
                <#if mobile?has_content>
                    <div class="w-form-table">
                        <dl>
                            <dt>验证手机：</dt>
                            <dd>
                                ${mobile!''}
                                &nbsp;&nbsp;&nbsp;&nbsp;
                                <a style="width: 150px" id="send_pwchange_validate_code" class="w-btn w-btn-orange w-btn-mini" href="javascript:void(0);"><span>获取短信验证码</span></a>
                            </dd>
                            <dt>短信验证码：</dt>
                            <dd>
                                <input id="validatecode" type="text" class="w-int changepw-vc" value="">
                                <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                                <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                            </dd>
                            <dt>新的登录密码：</dt>
                            <dd>
                                <input id="password" type="password" class="w-int" value="" placeholder="请输入1-16位任意字符，字母区分大小写">
                                <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                                <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                            </dd>
                            <dt>再次输入新密码：</dt>
                            <dd>
                                <input type="password" id="verify_password" class="w-int" value="" placeholder="请再次输入新的密码">
                                <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                                <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                            </dd>
                            <dd class="form-btn center">
                                <a class="w-btn w-btn-green w-btn-small cancelBtn" href="javascript:void(0);">取消</a>
                                <a class="w-btn  w-btn-small" id="reset_user_password_button" href="javascript:void(0);">确定</a>
                            </dd>
                        </dl>
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
            <li>
                <div class="ts-icon <#if mobile?has_content>ts-icon-success<#else>ts-icon-error</#if>"><span class="ts-ic ts-ic-2"></span></div>
                <div class="ts-note">
                    <h3>
                        <#if mobile?has_content>
                            <i class="w-icon-public w-icon-success"></i> 手机绑定：已设置
                        <#else>
                            <i class="w-icon-public w-icon-error"></i> 手机绑定：未设置
                        </#if>
                    </h3>
                    <p>绑定手机后，您即可享受手机登录、手机找回密码等服务。</p>
                </div>
                <div class="ts-btn">
                    <#if mobile?has_content>
                        <a class="w-btn w-btn-mini showAndHide" data-ref="#mobileLi" href="javascript:void(0);">更换手机</a>
                    <#else>
                        <a class="w-btn w-btn-mini showAndHide" data-ref="#mobileLi" id="v-a-bindmobile" href="javascript:void(0);">绑定手机</a>
                    </#if>
                </div>
            </li>
            <li id="mobileLi" class="ts-detail" style="display: none;">
                <#if mobile?exists && mobile?has_content>
                    <div class="yellow-bg">原号码：${mobile}更换后，原号码不能再作为登录使用！</div>
                </#if>
                <div class="w-form-table">
                    <dl>
                        <dt><#if mobile?exists && mobile?has_content>新</#if>手机号码：</dt>
                        <dd>
                            <input id="newmobile" type="text" class="w-int" value="" placeholder="请输入手机号，可用于登录和找回密码">
                            <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                            <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                        </dd>
                        <dd>
                            <a style="width: 115px" id="send_validate_code" data-oldmobile="${mobile!}" class="w-btn w-btn-orange w-btn-mini" href="javascript:void(0);">获取短信验证码</a>
                            <span class="w-form-misInfo"></span>
                        </dd>
                        <dt>短信验证码：</dt>
                        <dd>
                            <input id="validatecode" type="text" class="w-int bindmobile-vc" value="">
                            <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                            <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                        </dd>
                        <dd class="form-btn center">
                            <a class="w-btn w-btn-green w-btn-small cancelBtn" href="javascript:void(0);">取消</a>
                            <a class="w-btn  w-btn-small" id="confirm_validate_code" href="javascript:void(0);">确定</a>
                        </dd>
                    </dl>
                </div>
            </li>
        <#--小学学段显示绑定邮箱-->
        <#if (currentTeacherDetail.isPrimarySchool())!false>
            <li>
                <div class="ts-icon <#if email?has_content>ts-icon-success<#else>ts-icon-error</#if>"><span class="ts-ic ts-ic-3"></span></div>
                <div class="ts-note">
                    <h3>
                        <#if email?has_content>
                            <i class="w-icon-public w-icon-success"></i> 邮箱绑定：已设置
                        <#else>
                            <i class="w-icon-public w-icon-error"></i> 邮箱绑定：未设置
                        </#if>
                    </h3>
                    <p>绑定邮箱后，您即可享受邮箱登录、邮箱找回密码等服务。</p>
                </div>
                <div class="ts-btn">
                    <#if email?has_content>
                        <a class="w-btn w-btn-mini showAndHide" data-ref="#mailLi" href="javascript:void(0);">修改邮箱</a>
                    <#else>
                        <a class="w-btn w-btn-mini showAndHide" data-ref="#mailLi" href="javascript:void(0);">绑定邮箱</a>
                    </#if>
                </div>
            </li>
            <li id="mailLi" class="ts-detail" style="display: none;">
                <#if email?exists && email?has_content>
                    <div class="yellow-bg">原邮箱地址：${email}更换后，原邮箱地址不能再作为登录使用！</div>
                </#if>
                <div class="w-form-table">
                    <dl>
                        <dt><#if email?has_content>新</#if>邮箱地址: </dt>
                        <dd>
                            <input id="email" type="text" class="w-int" value="" placeholder="请输入邮箱，可用于登录和找回密码">
                            <span class="w-form-misInfo"><strong class="info">（*必填）</strong></span>
                            <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                        </dd>
                        <dd class="form-btn center">
                            <a class="w-btn w-btn-green w-btn-small cancelBtn" href="javascript:void(0);">取消</a>
                            <a class="w-btn  w-btn-small" id="confirm_bind_email" href="javascript:void(0);">确定</a>
                        </dd>
                    </dl>
                </div>
                <div id="rebind_success" class="msginfo" style="display: none;">
                    <s class="v_icon v_icon_tg"></s><span class="text">邮箱修改成功！</span>
                    <p class="font_in padding_ten color_gray" style=" line-height:22px;">
                        请在您的邮箱里点击链接激活邮箱<br>
                    </p>
                </div>
            </li>

            <#--qq解绑-->
            <#if qq?size gt 0>
                <#list qq as qq>
                    <li id="bindQQ_${qq.id!''}">
                        <div class="ts-icon ts-icon-success"><span class="ts-ic ts-ic-7"></span></div>
                        <div class="ts-note">
                            <h3>
                                <i class="w-icon-public w-icon-success"></i> QQ 绑定：已绑定 ${qq.sourceUserName!''}
                            </h3>
                            <p>绑定QQ后，可以使用“QQ登录”功能登录一起作业网</p>
                        </div>
                        <div class="ts-btn">
                            <a data-id="${qq.id!''}" class="w-btn w-btn-mini relieveBut" href="javascript:void(0);">解除绑定</a>
                        </div>
                    </li>
                </#list>
            </#if>
        </#if>
        </ul>
        <div class="w-clear"></div>
    </div>
</div>
<#include "validate.ftl"/>
<script type="text/javascript">
    if(location.pathname == "/teacher/center/securitycenter.vpage"){
        location.href = "/teacher/center/index.vpage#/teacher/center/securitycenter.vpage";
    }
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
            var success = validate("#passwordLi");
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
            // 暂时屏蔽,防止它们再要
//            getCaptchaOccurr++;
//            if(getCaptchaOccurr > 1){
//                textAmverifcation = "，如长时间未收到短信，请点击此处<a href='javascript:void(0);' class='applicationManualVerification'>申请人工验证</a>";
//            }else{
//                textAmverifcation = "";
//            }

            $this.siblings("span").html("正在努力发送，请稍候片刻...");
            var _mobile = $("#newmobile").val();

            App.postJSON("/teacher/center/sendmobilecode.vpage?mobile=" + _mobile, {}, function(data){
                if(data.success){
                    $this.siblings("span").html("验证码已发送" + textAmverifcation);
                }else{
                    $this.siblings("span").html(data.info + textAmverifcation);
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
                        location.href = "#/teacher/center/securitycenter.vpage?type=mobileLi";
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

        //邮箱提交
        $("#confirm_bind_email").on("click",function(){
            var $this = $(this);
            var success = validate("#mailLi");
            if(success){
                if($this.isFreezing()){
                    return false;
                }
                $this.freezing();
                $.post('/teacher/center/sendvalidateEmail.vpage',{email : $("#email").val()},function(data){
                    $this.thaw();
                    if(data.success){
                        $17.alert($("#rebind_success").html(),function(){
                            location.href = "#/teacher/center/securitycenter.vpage?type=mailLi";
                        });
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        })

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
            $("#passwordLi").show();
        }
    });
</script>
</#if>