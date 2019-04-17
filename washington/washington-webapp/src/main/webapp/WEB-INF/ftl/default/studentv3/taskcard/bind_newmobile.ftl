<script type="text/html" id="T:bindNewMobile">
    <div class="t-set-password t-homework-task">
        <h1 class="safety-warn">安全提醒</h1>
        <div id="bindNewMobileContainer">
            <div class="sp-step">
                <ul>
                    <li class="active" style="width: auto; text-align: center;">
                        <span class="sp-icon sp-icon-2" style="display: inline-block;"><#--<span class="sp-icon sp-arrow"></span>--></span>
                        <p>验证手机号 ${(data.mobile)!''}</p>
                    </li>
                </ul>
                <div class="w-clear"></div>
                <p class="sub">你的账号存在风险，请验证手机号提高安全性。</p>
            </div>
            <div class="sp-form">
                <#--//start-->
                <div class="w-ag-center" style="padding-bottom: 10px; color: #d58d00; font-size: 16px;">换了新号码？ <a href="javascript:void(0);" id="replaceNewMobile" style="text-decoration: underline;color: #d58d00;">点击绑定新手机号</a></div>
                <div class="w-form-table">
                    <dl>
                        <dt>原手机号：</dt>
                        <dd style="margin-bottom: 10px;">
                            <input id="mobile_box" type="text" maxlength="11" value="" class="w-int" placeholder="请输入手机号"/>
                        </dd>
                        <dd style="margin-bottom: 10px;"><a id="getBind_captcha_but" href="javascript:void(0);" class="w-btn-dic w-btn-gray-normal"><span>免费获取短信验证码</span></a></dd>
                        <dt>短信验证码：</dt>
                        <dd style="margin-bottom: 10px;">
                            <input id="captcha_box" type="text" value="" class="w-int" style="width: 125px" placeholder="请输入短信验证码" maxlength="6"/>
                        </dd>
                    </dl>
                </div>
                <#--end//-->
            </div>
        </div>
        <div class="sp-btn w-ag-center">
            <a class="w-btn-dic w-btn-gray-well" id="logoutBtn2" href="/ucenter/logout.vpage">退出登录</a>
            <a class="w-btn-dic w-btn-green-well" id="bindNewMobileSubmit" href="javascript:void(0);">确定</a>
        </div>
    </div>
</script>

<script type="text/html" id="T:notBindMobile">
    <div class="sp-step">
        <ul>
            <li class="active" style="width: auto; text-align: center;">
                <span class="sp-icon sp-icon-2" style="display: inline-block;"><#--<span class="sp-icon sp-arrow"></span>--></span>
                <p>绑定新手机号</p>
            </li>
        </ul>
        <div class="w-clear"></div>
        <p class="sub">你的账号存在风险，请验证手机号提高安全性。</p>
    </div>
    <div class="sp-form">
    <#--//start-->
        <div class="w-form-table">
            <dl>
                <dt>新手机号码：</dt>
                <dd style="margin-bottom: 10px;">
                    <input id="mobile_box" type="text" maxlength="11" value="" class="w-int" placeholder="请输入手机号"/>
                </dd>
                <dd style="margin-bottom: 10px;"><a id="getBind_captcha_but" href="javascript:void(0);" class="w-btn-dic w-btn-gray-normal"><span>免费获取短信验证码</span></a></dd>
                <dt>短信验证码：</dt>
                <dd style="margin-bottom: 10px;">
                    <input id="captcha_box" type="text" value="" class="w-int" style="width: 125px" placeholder="请输入短信验证码" maxlength="6"/>
                </dd>
            </dl>
        </div>
    <#--end//-->
    </div>
</script>

<script type="text/javascript">
    (function($){
        var dataPostOpts = {
            type : "verify",//验证手机
            getCode : "/student/sendvmmobilecode.vpage",
            bindPostUrl : "/student/validatemobile.vpage"
        };

        function popup(){
            var statesHtml = {
                state0 : {
                    focus: 1,
                    position : { width: 620},
                    buttons : {},
                    classes : {
                        close: "${((data.force)!false)?string("w-hide", "")}"
                    },
                    html : template("T:bindNewMobile", {})
                },state1: {
                    html:'<div id="serverDataInfo" style="text-align: center;">数据错误！</div>',
                    buttons: {"知道了": 0 },
                    submit:function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.goToState('state0');
                    }
                }
            };

            $.prompt.setDefaults({
                classes : {
                    close: "${((data.force)!false)?string("w-hide", "")}"
                }
            });

            $.prompt(statesHtml,{
                close : function(){
                    $17.voxLog({
                        module : "bindNewMobile",
                        bind : "close",
                        op : "<#if (data.force)!false>1<#else>2</#if>-" + dataPostOpts.type
                    }, "student");
                }
            });

            $17.voxLog({
                module : "bindNewMobile",
                bind : "load",
                op : "<#if (data.force)!false>1<#else>2</#if>-" + dataPostOpts.type
            }, "student");

            <#if !((data.mobile)?has_content)>
                /*如果手机不存在-直接绑定新手机*/
                setTimeout(function(){
                    $("#replaceNewMobile").click();
                }, 200);
            </#if>
        }

        //切换绑定新手机
        $(document).on("click", "#replaceNewMobile", function(){
            dataPostOpts.type = "replace";//更换新手机
            dataPostOpts.getCode = "/student/center/sendmobilecode.vpage";
            dataPostOpts.bindPostUrl = "/student/center/validatemobile.vpage";

            $("#bindNewMobileContainer").html( template("T:notBindMobile", {}) );
            $17.voxLog({
                module : "bindNewMobile",
                bind : "load",
                op : "<#if (data.force)!false>1<#else>2</#if>-" + dataPostOpts.type
            }, "student");
        });

        //get code
        $(document).on('click','#getBind_captcha_but', function(){
            var $this = $(this);
            var mobileBox = $("#mobile_box");

            if(!$17.isMobile(mobileBox.val())){
                alertInfo("请输入手机号");
                return false;
            }

            if($this.hasClass("btn_disable")){return false;}

            $this.addClass("btn_disable");
            $.post(dataPostOpts.getCode, {mobile : mobileBox.val()}, function(data){
                if(!data.success){
                    alertInfo(data.info);
                }

                $17.getSMSVerifyCode($this, data);

                $17.voxLog({
                    module : "bindNewMobile",
                    bind : "getCode",
                    op : "<#if (data.force)!false>1<#else>2</#if>-" + dataPostOpts.type
                }, "student");
            });
        });

        //绑定手机
        $(document).on('click', '#bindNewMobileSubmit', function(){
            var mobileBox = $("#mobile_box");
            var captchaBox = $("#captcha_box");

            if(!$17.isMobile(mobileBox.val())){
                alertInfo("请输入手机号");
                return false;
            }

            if($17.isBlank(captchaBox.val())){
                alertInfo("请输入短信验证码");
                return false;
            }

            var callBack = function(data){
                if(data.success){
                    $17.alert("绑定成功");
                }else{
                    alertInfo(data.info);
                }
            };
            var postItems = {
                mobile : mobileBox.val(),
                code : captchaBox.val()
            };

            if(dataPostOpts.type == "replace"){
                postItems = {
                    latestCode : captchaBox.val()
                };
            }

            $.post(dataPostOpts.bindPostUrl, postItems).done(callBack);

            $17.voxLog({
                module : "bindNewMobile",
                bind : "bindPost",
                op : "<#if (data.force)!false>1<#else>2</#if>-" + dataPostOpts.type
            }, "student");
        });

        function alertInfo(message, callback){
            $("#serverDataInfo").text(message ? message : "数据错误！");
            $.prompt.goToState('state1', true);

            if(callback){
                callback();
            }
        }

        //退出
        $(document).on("click", "#logoutBtn2", function(){
            $17.voxLog({
                module : "bindNewMobile",
                bind : "logout",
                op : "<#if (data.force)!false>1<#else>2</#if>-" + dataPostOpts.type
            }, "student");
        });

        $.extend($, {
            bindNewMobile : popup
        });
    }($));
</script>