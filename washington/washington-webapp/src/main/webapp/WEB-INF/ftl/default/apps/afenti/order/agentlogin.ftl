<#import "module.ftl" as com>
<@com.page title="代付登录 - 一起作业" stepOnOff = "Canceled">
<div class="main">
    <div class="payMainBox">
        <!--pay-->
        <div class="tabbox" style="overflow: visible; position: relative; width: 580px; margin: 50px auto 0;">
            <div class="tabLevel">
                <!--init-->
                <div class="agentMobileBox">
                    <h1 class="title">代付登录</h1>
                    <ul>
                        <li class="inp" style="height: 70px;">
                            <span class="title_name">请输入手机号</span>
                            <p class="int_box">
                                <input type="text" id="mobileInt" name="mobileInt" value=""/>
                            </p>
                        </li>
                        <li class="inp" style="height: 80px;">
                            <span class="title_name">请输入提取码</span>
                            <p class="int_box">
                                <input type="text" id="mobileCode" name="mobileCode" value=""/>
                            </p>
                            <p class="init" id="initInfo" style="color: #c33; padding-top: 7px; "></p>
                        </li>
                        <li class="btn">
                            <a id="agentLoginSubmit" href="javascript:void(0);" class="getOrange gPaygetGreen">立即提取</a>
                        </li>
                    </ul>
                    <div class="agentLoginInfo">
                        代付说明：
                        <p>1、请使用你收到短信的手机号码和提取码登录。</p>
                        <p>2、提取码有效期为7天，登录后请尽快完成付款。</p>
                    </div>
                </div>
            </div>
        </div>
        <!--//-->
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var mobileInt = $("#mobileInt");
        var mobileCode = $("#mobileCode");
        var initInfo = $("#initInfo");
        var agentLoginSubmit = $("#agentLoginSubmit");

        $("#mobileInt, #mobileCode").on("keydown", function(){
            initInfo.html("");
        });

        agentLoginSubmit.hover(function(){
            $(this).addClass("login_btn_hover");
        },function(){
            $(this).removeClass("login_btn_hover");
        });

        agentLoginSubmit.on("click", function(){
            var mobileInt_val = mobileInt.val();
            var mobileCode_val = mobileCode.val();

            if( $17.isBlank(mobileInt_val) ){
                mobileInt.focus();
                initInfo.html('<span class="error">请填写手机号码</span>');
                return false;
            }

            if( $17.isBlank(mobileCode_val) ){
                mobileCode.focus();
                initInfo.html('<span class="error">请填写提取码</span>');
                return false;
            }

            if( !$17.isMobile(mobileInt_val) ){
                mobileInt.focus();
                initInfo.html('<span class="error">手机号码无效，请重新填写吧~</span>');
                return false;
            }

            if( mobileInt.is(":disabled") ){
                return false;
            }

            if( mobileCode.is(":disabled") ){
                return false;
            }

            mobileInt.prop("disabled", true);
            mobileCode.prop("disabled", true);
            initInfo.html('<span class="success">提交中...</span>');

            $.post("agent/validate.vpage", {
                mobile  : mobileInt_val,
                code    : mobileCode_val
            }, function(data){
                if(data.success){
                    $17.tongji("代付提取-成功");
                    //显示成功提示
                    setTimeout(function(){
                        location.href = "/apps/afenti/order/agent.vpage?orderId="+data.orderId+"&mobile="+data.mobile;
                    }, 100);
                }else{
                    initInfo.html('<span class="error">'+ data.info +'</span>');
                    mobileInt.prop("disabled", false);
                    mobileCode.prop("disabled", false);
                    $17.tongji("代付提取-失败");
                }
            });
        });
    });
</script>
</@com.page>