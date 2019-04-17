<#import "../apps/afenti/order/module.ftl" as com>
<@com.page step=-1 title="一起作业">
<div class="main">
    <div class="payMainBox">
        <div class="curaddress">购买内容</div>
        <div class="tabbox">
            <!--//start-->
            <div class="alexTable">
                <div class="info-message" style="font-size: 14px">
                    <#if error??>
                            ${error}
                    <#else>
                        请确认要购买的商品
                    </#if>
                </div>
                <#if error??>
                <#else>
                <table style="width: 60%;">
                    <tbody>
                    <tr>
                        <th style="width: 100px;">学 号</th>
                        <td>${user.id!}</td>
                    </tr>
                    <tr>
                        <th style="width: 100px;">姓 名</th>
                        <td>${user.profile.realname!}</td>
                    </tr>
                    <tr>
                        <th style="width: 100px;">应用名称</th>
                        <td>${appName!}</td>
                    </tr>
                    <tr>
                        <th style="width: 100px;">商品名称</th>
                        <td>${appOrder.productName!}</td>
                    </tr>
                    <tr>
                        <th style="width: 100px;">商品价格</th>
                        <td>${(appOrder.orderPrice)!}
                            <#if (appOrder.payType == 'INTEGRAL')!false>学豆
                            <#elseif  appOrder.payType == 'HWCOIN'>作业币
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <th style="width: 100px;">拥有<#if (appOrder.payType == 'INTEGRAL')!false>学豆<#elseif  appOrder.payType == 'HWCOIN'>作业币</#if></th>
                        <td>
                            <#if (appOrder.payType == 'INTEGRAL')!false>${userIntegral.usable!}学豆
                            <#elseif (appOrder.payType == 'HWCOIN')!false>${userFinance.balance!} 作业币
                                <#if (appOrder.orderPrice gt userFinance.balance)!false>&nbsp;&nbsp;&nbsp;&nbsp;(作业币不足，您可以<a target="_blank" href="/student/center/recharging.vpage?types=recharging" style="color: #39f;">点此</a>进行充值)</#if>
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <th style="width: 100px;">支付密码</th>
                        <td><#if user.paymentPassword?has_content>
                                <input type="password" id="paymentPassword" style="border: solid 1px #ccc; border-radius:5px; width:200px; padding: 7px 6px; font:14px/120% '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#666; vertical-align:middle; outline: none; box-shadow:1px 1px 2px #eee inset;">
                            <#else>
                                <a href="/student/center/index.vpage" style="color: #589de2">设置支付密码</a>
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <th style="width: 100px;">验证码</th>
                        <td>
                            <input id="captchaCode" type="text" name="code"  value="" style="border: solid 1px #ccc; border-radius:5px; width:80px; padding: 7px 6px; font:14px/120% '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#666; vertical-align:middle; outline: none; box-shadow:1px 1px 2px #eee inset;" >
                            &nbsp;
                            <img id='captchaImage' />&nbsp;
                            看不清？<a href="javascript:refreshCaptcha();" style="color: #39f">换一个</a>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <div class="button-box">
                    <a href="javascript:void(0);" class="getOrange getOrange_gray" style="cursor: pointer;">关闭</a>
                    <a href="javascript:void(0);" class="getOrange" id="pay_for_it">立即购买</a>
                </div>
                <input type="hidden" id="order_id" value="${orderId!''}"/>
                <input type="hidden" id="order_token" value="${appOrder.orderToken}"/>
                <input type="hidden" id="app_key" value="${appOrder.productServiceType}"/>
                <#--<input type="hidden" id="session_key" value="${appOrder.sessionKey}"/>-->
                </#if>
            </div>
            <!--end//-->
        </div>
        <div class="clear"></div>
    </div>
</div>

<#--提示框-->
<div id="payColorBox" style="display: none; ">
    <div class="alpha_back"></div>
    <div class="alpha_content_layer" style="margin: -109px 0 0 -180px">
        <div class="alpha_layer_ie">
            <div class="payColorBox" id="payStep_1">
                <div class="close"></div>
                <div class="clrgray" style="margin:10px;">
                    <div class="alicenter">
                        <s class="iblock iexclamation"></s><span style="display:inline-block; vertical-align:middle; line-height:22px;">对不起，您的作业币不足！</span>
                        <p class="padten">
                            <a href="/student/center/recharging.vpage?types=recharging-go" target="_blank" class="publicBtn redBtnsl" id="toRecharge"><i class="lB"></i><i class="tB"><span>去充值</span></i><i class="rB"></i></a>
                        </p>
                    </div>
                </div>
            </div>
            <div class="payColorBox" style="display: none;" id="payStep_2">
                <div class="successBox paypopBox">
                    <s class="iblock infoview"></s>
                    <div class="content">
                        <p class="ctn">请您在新打开的页面上完成充值。<br/>充值完成前请不要关闭此窗口。</p>
                        <a href="javascript:void(0);" class="publicBtn greenBtn" id="finish"><i class="lB"></i><i class="tB" style="padding: 0;"><span>充值完成返回立即购买</span></i><i class="rB"></i></a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    function refreshCaptcha() {
        $('#captchaImage').attr('src', "/captcha?" + $.param({
            'module': 'findAccount',
            'token': '${captchaToken!''}',
            't': new Date().getTime()
        }));
    }
    $(function(){
        $('#pay_for_it').on('click',function(){
            var $this = $(this);
            if( $this.hasClass("dis") ){return false;}

            $.prompt("<div class='w-ag-center'>亲爱的同学，购买该款学习产品，要先征得爸爸妈妈的同意哦！</div>", {
                focus: 1,
                title: "系统提示",
                buttons: { "取消": false, "确定": true },
                position: {width: 500},
                submit : function(e, v){
                    if(v){
                        //start
                        $this.addClass("dis");
                        $.post('confirm.vpage',{
                            order_id:$('#order_id').val(),
                            order_token:$('#order_token').val(),
                            app_key:$('#app_key').val(),
                            session_key:$('#session_key').val(),
                            paymentPassword:$('#paymentPassword').val(),
                            captchaToken:"${captchaToken!''}",
                            captchaCode:$('#captchaCode').val()
                        },function(data){
                            if(!data.success){
                                $this.removeClass("dis");
                                if(data.message.indexOf("作业币不足") >= 0 ){
                                    $("#payColorBox").show();
                                    $("#payStep_1").show();
                                    $("#payStep_2").hide();
                                }else{
                                    $17.alert(data.message);
                                }
                                $('#captchaCode').val('');
                                refreshCaptcha();
                            }else{
                                $17.alert("购买成功", function(){
                                    window.close();
                                });
                            }
                        });
                        //end
                    }
                }
            });
        });

        $("#toRecharge").on("click", function(){
            $("#payStep_1").hide();
            $("#payStep_2").show();
        });

        $("#finish").on("click", function(){
            $("#payColorBox").hide();
            location.reload();
        });

        refreshCaptcha();
    });
</script>
</@com.page>
