<#if (!data.taskMapper.parentWechatBinded)!false>
    <script type="text/html" id="T:noviceStrongTieApp">
        <div class="t-set-password t-homework-task">
            <div id="novicePopupContent"></div>
            <div style="text-align: right; padding: 10px 20px; clear: both; position: relative; z-index: 2; float: right;">
                <#--<a href="/help/kf/index.vpage?menu=student" style="color: #95a7af; text-decoration: underline;">帮助中心</a>-->
                <#--<a href="/ucenter/logout.vpage"  style="color: #95a7af; text-decoration: underline; display: inline-block; margin-left: 10px;">退出</a>-->
                <a href="javascript:void(0);" style="color: #95a7af; text-decoration: underline; display: inline-block; margin-left: 10px;" class="js-nextAuthBtn">下次认证</a>
            </div>
        </div>
    </script>

    <script type="text/html" id="T:strongTie-mobile">
        <h1>绑定手机</h1>
        <div class="sp-step">
            <ul style="display: block;">
                <li class="active" style="width: 80%; float: none; margin: 0 auto;">
                    <span class="sp-icon sp-icon-2" style="float: left;"></span>
                    <p style="margin-left: 125px; text-align: left; line-height: 80px;">绑定保密手机，防止学号或密码丢失无法找回</p>
                </li>
            </ul>
            <div class="w-clear"></div>
        </div>
        <div class="sp-form">
            <#--template content start-->
            <div class="w-form-table" style="padding: 30px 0;">
                <dl>
                    <dt>绑定家长手机：</dt>
                    <dd style="margin-bottom: 10px;">
                        <input id="mobile_box" type="text" maxlength="11" value="" class="w-int" placeholder="请输入家长手机号">
                        <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none" data-title="请输入正确的手机号码">
                            <i class="w-spot w-icon-error"></i>
                            <strong class="info">请输入正确的手机号码</strong>
                        </span>
                    </dd>
                    <dt>短信验证码：</dt>
                    <dd style="margin-bottom: 10px;">
                        <input id="captcha_box" type="text" class="w-int" style="width: 70px;" maxlength="4">
                        <a id="strongTie_getCaptcha_but" href="javascript:void(0);" class="w-btn-dic w-btn-gray-normal"><span>免费获取短信验证码</span></a>
                        <span class="w-form-misInfo w-form-info-error qtip_n" style="display: none">
                            <i class="w-spot w-icon-error"></i>
                            <strong class="info">请输入正确的短信验证码</strong>
                        </span>
                    </dd>
                </dl>
            </div>
            <#--template content end-->
        </div>
        <div class="sp-btn w-ag-center">
            <a class="w-btn-dic w-btn-green-well" href="javascript:void(0);" id="strongTie_mobile_but">确定，下一步</a>
        </div>
    </script>

    <style>
        #strongTieAppStep2 .cur-tips-main { position: relative; display: inline-block;}
        #strongTieAppStep2 .cur-tips-main .tips-box{ left: 0; top:28px; position: absolute; display: none; background: url(<@app.link href="public/skin/studentv3/images/JZT_QR/code-info.png"/>) no-repeat; width: 210px; height: 42px; padding: 10px 0 0 5px; line-height: 120%; font-size: 12px;color: #da9cec; text-align: left; }
        #strongTieAppStep2 .cur-tips-main:hover .tips-box{ display: block;}
        #strongTieAppStep2 .sp-form .bs-ic{width: 16px; height: 16px; display: inline-block; border-radius: 50px; line-height: 16px; text-align: center; font-size: 12px; color: #fff; background-color: #87dcd1;}
        #strongTieAppStep2 .send-appName-btn {padding: 0 5px; height: 22px; line-height: 22px; background-color: #eef6f9; color: #666; font-size: 12px; border: 1px solid #d4e4ea; box-shadow: 1px 1px 2px #d4e4ea inset; display: inline-block; border-radius: 5px; text-align: center; }
        #strongTieAppStep2 .send-appName-btn:hover{ background-color: #e0eff4; border-color: #c8dde5; color: #333;}
    </style>
    <script type="text/html" id="T:strongTie-app">
        <div id="strongTieAppStep2">
            <h1>家长认证</h1>
            <div class="sp-step">
                <ul style="display: block;overflow: visible;">
                    <li class="active" style="width: 100%; float: none; text-align: center; padding-bottom: 15px;">
                        <p style="line-height: 30px;">
                            为确保家长监督孩子学习，一起作业需要经过家长认证：<br/>
                            下载并登录
                            <a href="javascript:void(0)" class="cur-tips-main" style="color: #58bfe0;">家长通<span class="tips-box">全国大多数老师推荐下载家长通用于<br/>接收老师通知，作业报告等</span></a>
                            ，学生才可以使用一起作业
                        </p>
                    </li>
                </ul>
                <div class="w-clear"></div>
            </div>
            <div class="sp-form">
            <#--template content start-->
                <div class='weiXinSideDetail' style='text-align: center;padding: 30px 0;'>
                    <dl>
                        <dt style="float: right; width: 220px;">
                            <img src="<@app.link href="public/skin/studentv3/images/JZT_QR/novice_strongtie_code.png"/>" width="150"/>
                            <p>微信扫描二维码</p>
                        </dt>
                        <dd style="text-align: left; margin-left: 70px;">
                            <h4 style="font-size: 16px; color: #666;">家长通下载方式：</h4>
                            <p style="color: #666; padding: 5px 0;"><span class=bs-ic>1</span> 扫描右侧二维码：</p>
                            <#--<p style="color: #666;" class="strongTie-infoApp-tg"><span class=bs-ic>2</span> 点击刚收到的验证码短信中的下载链接</p>-->
                            <#if (data.taskMapper.mobileVerfied)!false>
                            <p style="color: #666;"><span class=bs-ic>2</span> 发送下载链接到已绑定手机号
                                <a href="javascript:void(0);" class="send-appName-btn" id="strongTie-sendAppName">点击发送</a>
                            </p>
                            </#if>

                            <h4 style="color: #a5a5a5; padding: 20px 0 5px;">家长通登录方式>></h4>
                            <p style="color: #a5a5a5;">通过孩子学号及密码登录或者已绑定的手机号登录</p>
                        </dd>
                    </dl>
                    <div class="w-clear"></div>
                    <div style="border-radius: 5px; background-color: #fff3d8; color: #fa7252; padding: 6px 15px; margin: 0 150px; margin-top: 6px; display: none;" class="strongTie-infoApp">您还未认证，请下载并登录家长通</div>
                </div>
            <#--template content end-->
            </div>
            <div class="sp-btn w-ag-center">
                <a class="w-btn-dic w-btn-green-well" href="javascript:void(0);" id="strongTie_app_but">认证成功</a>
            </div>
        </div>
    </script>

    <script type="text/html" id="T:strongTie-complete">
        <div class="sp-step sp-step-bg"><#--恭喜认证完成--></div>
        <div class="sp-form"></div>
        <div class="sp-btn w-ag-center">
            <a class="w-btn-dic w-btn-green-well" href="javascript:void(0);" id="noviceDownLoadUserId">确定</a>
        </div>
    </script>
</#if>
<script type="text/javascript">
    (function($){
        //没有家长号的强弹绑定框
        function noviceStrongTieApp(){
            $17.voxLog({
                module : "noviceStrongtieLog",
                op : "login-gray"
            }, "student");
        <#if (!data.taskMapper.parentWechatBinded)!false>
            var popupTemplate = "T:noviceStrongTieApp";
            var state = {
                mobile : ${data.taskMapper.mobileVerfied?string},
                weixin : ${data.taskMapper.parentWechatBinded?string}
            };
            /*var stateStep = state.mobile ? "T:strongTie-app" : "T:strongTie-mobile";*/
            var stateStep = "T:strongTie-app";

            if(!state.weixin){
                if(!$17.getCookieWithDefault("noviceStrongTie")){
                    $17.setCookieOneDay("noviceStrongTie", "3", 3);
                    $.prompt(template(popupTemplate, {btnName : stateStep, state : state}),{
                        position    : { width: 620},
                        buttons     : {},
                        classes : {
                            close: 'w-hide'
                        },
                        loaded : function(){
                            $("#novicePopupContent").html( template(stateStep, {state : state}) );
                        }
                    });
                }
            }

            $17.voxLog({
                module : "noviceStrongtieLog",
                op : "load"
            }, "student");

            /*获取验证码*/
            $(document).on('click','#strongTie_getCaptcha_but', function(){
                var $this = $(this);
                var mobileBox = $("#mobile_box");
                if(!$17.isMobile(mobileBox.val())){
                    mobileBox.siblings("span").show().find(".info").html(mobileBox.siblings("span").data("title"))
                    mobileBox.focus();
                    return false;
                }
                if($this.hasClass("btn_disable")){return false;}

                $this.addClass("btn_disable");
                if($17.isMobile(mobileBox.val())){
                    $.post("/student/sendmobilecode.vpage", {mobile : mobileBox.val()}, function(data){
                        if(!data.success){
                            mobileBox.siblings("span").children('strong').text(data.info);
                            mobileBox.siblings("span").show();
                        }
                        $17.getSMSVerifyCode($this, data);
                    });
                }
            });

            $(document).on('blur', 'dd input', function(){
                $(".qtip_n").hide();
            });

            //绑定手机
            $(document).on('click', '#strongTie_mobile_but', function(){
                var mobileBox = $("#mobile_box");
                if(!$17.isMobile(mobileBox.val())){
                    mobileBox.siblings("span").show().find(".info").html(mobileBox.siblings("span").data("title"))
                    mobileBox.focus();
                    return false;
                }

                var captchaBox = $("#captcha_box");
                if(!$17.isNumber(captchaBox.val())){
                    captchaBox.siblings("span").show();
                    captchaBox.focus();
                    return false;
                }

                $.post('/student/nonameverifymobile.vpage', {code : captchaBox.val()}, function(data){
                    if(data.success){
                        state.mobile = true;

                        stateStep = "T:strongTie-app";
                        $("#novicePopupContent").html( template(stateStep, {state : state}) );
                    }else{
                        captchaBox.siblings("span").children('strong').text(data.info);
                        captchaBox.siblings("span").show();
                    }
                });
            });

            //绑定App - 认证成功
            $(document).on('click', '#strongTie_app_but', function(){
                $.get('/student/hasparent.vpage', {}, function(data){
                    if(data.success && data.hasParent){
                        stateStep = "T:strongTie-complete";
                        $("#novicePopupContent").html( template(stateStep, {state : state}) );
                    }else{
                        $(".strongTie-infoApp").show();
                    }

                    $17.voxLog({
                        module : "noviceStrongtieLog",
                        op : "bind"
                    }, "student");
                });
            });

            //确定完成
            $(document).on('click', '#noviceDownLoadUserId', function(){
                $.prompt.close();
            });

            //点击发送短信
            $(document).on("click", "#strongTie-sendAppName", function(){
                var $this = $(this);
                var $timeCount = 60;

                if($this.hasClass("dis")){
                    return false;
                }

                $this.addClass("dis");
                sendSms();

                var startTime = setInterval(function(){
                    $timeCount--;

                    $this.text($timeCount + "秒之后可重新发送");

                    if($timeCount == 0){
                        clearInterval(startTime);
                        $this.text("重新发送").removeClass("dis");
                    }
                }, 1000);

                $17.voxLog({
                    module : "noviceStrongtieLog",
                    op : "sms"
                }, "student");
            });

            $(document).on("click", ".js-nextAuthBtn", function(){
                $.prompt.close();

                $17.voxLog({
                    module : "noviceStrongtieLog",
                    op : "nextAuth"
                }, "student");
            });

            //发送家长通
            function sendSms(){
                $.get("/student/sendbindjzt.vpage", {}, function(){});
            }
        </#if>
        }

        $.extend({
            noviceStrongTieApp : noviceStrongTieApp
        });

    })($);
</script>