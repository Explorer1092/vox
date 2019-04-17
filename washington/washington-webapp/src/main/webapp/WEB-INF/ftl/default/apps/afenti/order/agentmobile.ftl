<#import "module.ftl" as com>
<@com.page title="让你的家长帮忙付款吧 - 一起作业" stepOnOff = "Canceled">
<div class="main">
    <div class="payMainBox">
        <div class="curaddress">让好友/家长帮你付款吧</div>
        <!--pay-->
        <div class="tabbox">
            <div class="tabLevel">
                <!--init-->
                <div class="agentMobileBox">
                    <dl>
                        <dt>
                            <div class="mobile_icon"></div>
                            <p>发短信告诉TA<br/>让TA帮你付款</p>
                        </dt>
                        <dd style="height: 125px;">
                            <h4 class="info">请输入帮你付款的人的手机号</h4>
                            <p class="int_box">
                                <input type="text" value="${mobile!}" id="mobileInt"/>
                                <input type="hidden" value="${orderId!}" id="orderIdInt"/>
                                <a href="javascript:void(0);" class="submitGreenBtn"><strong>发送</strong></a>
                            </p>
                            <p class="init" id="initInfo">
                                <#--<span class="error">手机号码无效，请重新填写吧</span>
                                <span class="success">家长收到消息，稍等他们为你付款哦</span>-->
                            </p>
                        </dd>
                        <dd style="padding: 0;">
                            <div class="send_info_message">
                                短信样式：<br>
                                我在【一起作业网】订购了一款英语学习产品，无法付款，需要你帮忙代付。请用你的“手机号”和<br/>“提取码”登录p.17zuoye.com查看并付款。提取码：1234<br>
                                发自【张三】同学
                            </div>
                        </dd>
                    </dl>
                </div>
                <!--success-->
                <div class="successBox" style=" display: none;">
                    <div style="width: 280px; margin: 0 auto;">
                        <s class="iblock ireceiving"></s>
                        <div class="content">
                            <p class="txt">发送成功！</p>
                        </div>
                    </div>
                    <div style="clear: both; height: 50px;"></div>
                    <p class="ctn" style="margin-bottom: 20px; text-align: center;">帮你代付的人已收到短信，请及时提醒TA为你付款哦！</p>
                    <p class="ctn" style="text-align: center;"><a style="margin-right:25px " title="返回首页" class="public_send_btn public_blue_btn" href="/">返回首页</a><span id="setTime">10</span> 秒后自动跳转到首页</p>
                </div>
            </div>
        </div>
        <!--//-->
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var mobileInt = $("#mobileInt");
        var initInfo = $("#initInfo");

        mobileInt.on("keydown", function(){
            initInfo.html("");
        });

        //确定发送短信
        $(".submitGreenBtn").on("click", function(){
            var mobileInt_val = mobileInt.val();

            if( $17.isBlank(mobileInt_val) ){
                initInfo.html('<span class="error">请填写手机号码</span>');
                return false;
            }

            if( !$17.isMobile(mobileInt_val) ){
                initInfo.html('<span class="error">手机号码无效，请重新填写吧~</span>');
                return false;
            }

            if( mobileInt.is(":disabled") ){
                return false;
            }

            mobileInt.prop("disabled", true);
            initInfo.html('<span class="success">提交中...</span>');

            $.post("validateagentmobile.vpage", {
                mobile      : mobileInt_val,
                orderId     : $("#orderIdInt").val()
            }, function(data){
                if(data.success){
                    //显示成功提示
                    $(".successBox").show();
                    $(".agentMobileBox").hide();

                    $17.tongji("找人代付-给家长发送短信-点击确定按钮")

                    var setTime = 10;
                    setInterval(function(){
                        setTime--;
                        $("#setTime").text(setTime);
                        if(setTime == 0){
                            location.href = "/";
                        }
                    }, 1000);
                }else{
                    initInfo.html('<span class="error">'+ data.info +'</span>');
                    mobileInt.prop("disabled", false);

                    $17.tongji("找人代付-给家长发送短信-失败")
               }
            });
        });
    });
</script>
</@com.page>
