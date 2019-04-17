<#import "guideLayout.ftl" as temp />
<@temp.page>
<div class="build_head_box build_head_box_isInvite" style="height: 500px;">
    <div class="aph-back"></div>
    <div class="build_head_main">
        <a href="/login.vpage" class="logo"></a>
        <div style="text-align: center;">
            <h2>为确保您的身份真实，请通过手机短信进行验证</h2>
        </div>
        <div class="w-form-table" id="setPasswordContainer" style="position: static; margin: 0 auto;">
            <h3>绑定手机，以便您下次登录</h3>
            <dl>
                <dt>您的姓名：</dt>
                <dd>
                    <input id="username" placeholder="您的姓名" class="w-int">
                </dd>
                <dt>手机号码：</dt>
                <dd>
                    <input id="mobile" placeholder="手机号码" class="w-int" maxlength="11">
                    <a id="getCheckCode" href="javascript:void(0);" class="w-btn w-btn-mini"><strong style="font-weight: normal;">获取身份验证码</strong></a>
                </dd>
                <dt class="checkCodeBar">短信验证码：</dt>
                <dd class="checkCodeBar">
                    <input id="checkCode" placeholder="身份验证码" class="w-int" maxlength="6">
                </dd>
                <dd id="btns" class="form-btn">
                    <a class="w-btn" style="width: 160px; margin: 0;" id="submitBtn" href="javascript:void(0);">确定</a>
                </dd>
            </dl>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        var step = new $17.Model({
            mobile          : $("#mobile"),
            getCheckCodeBtn : $("#getCheckCode"),
            checkCode       : $("#checkCode"),
            checkCodeBar    : $(".checkCodeBar"),
            btns            : $("#btns"),
            submitBtn       : $("#submitBtn")
        });
        step.extend({
            getCheckCodeAction: function(){
                var $this = this;
                App.postJSON('/teacher/keti/guide/sendmobilecode.vpage?mobile=' + $this.mobile.val(), {}, function(data){
                    if(data.success){
                        $17.alert("验证码已发送，部分地区近期短信会出现延迟情况，请耐心等待。");
                        $this.checkCodeBar.show();
                        $this.btns.show();
                    }else{
                        $17.alert(data.info);
                    }
                });
            },
            sendCheckCode: function(){
                var $this = this;
                App.postJSON("/teacher/keti/guide/addnameandmobile.vpage", {
                    name        : $("#username").val(),
                    latestCode    : $this.checkCode.val()
                }, function(data){
                    if(data.success){
                        setTimeout(function(){ location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/index.vpage"; }, 200);
                    }else{
                        $17.alert(data.info);
                        return false;
                    }
                });
            },
            init: function(){
                var $this = this;

                $this.checkCodeBar.show();
                $this.btns.show();

                $this.getCheckCodeBtn.on("click", function(){
                    var _mobile = $this.mobile.val();
                    if($17.isBlank(_mobile) || !$17.isMobile(_mobile)){
                        $this.mobile.val("");
                        $17.alert("您输入的手机号有误，请重新输入");
                        return false;
                    }
                    $this.getCheckCodeAction();
                });

                $this.submitBtn.on("click", function(){
                    var userName = $("#username").val();
                    if($17.isBlank(username) || !$17.isCnString(userName)){
                        $("#username").val("");
                        $17.alert("请填写正确的姓名");
                        return false;
                    }

                    var _checkCode = $this.checkCode.val();
                    if($17.isBlank(_checkCode) || !$17.isNumber(_checkCode)){
                        $this.checkCode.val("");
                        $17.alert("您填写的验证码有误，请确认");
                        return false;
                    }
                    $this.sendCheckCode();
                });
            }
        }).init();
    });
</script>
</@temp.page>