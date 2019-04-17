<div class="w-form-table" style="padding: 15px 15px 0;">
<#if (vendorApps.cname)??>
    <h2>购买商品信息 <span style="display: inline-block; color: #f00; margin-left: 30px; font-size: 14px;">学生只能在电脑上使用本产品</span></h2>
    <dl>
        <dt>应用名称：</dt>
        <dd style="margin-bottom: 15px;">${(vendorApps.cname)!''}</dd>
        <dt>商品名称：</dt>
        <dd style="margin-bottom: 15px;">
            ${(vendorAppsOrder.productName)!''}
            <strong style="display: inline-block; margin: 0 0 0 100px;">
                商品价格：<span class="w-orange">${(vendorAppsOrder.totalPrice)!''}</span> 作业币
            </strong>
        </dd>
    </dl>
</#if>
    <h2>选择支付方式：<#if (vendorAppsOrder.totalPrice)??>需要支付 <strong class="w-orange">${(vendorAppsOrder.totalPrice)!''}</strong> 作业币</#if></h2>
</div>

<form action="/finance/recharge/recharge_submit.vpage" method="post" id="payConfirm" target="_blank">
    <input type="hidden" name="payMethod" id="payMethod" value=""/>
    <!--选择支付方式-->
    <div class="payMainBox" style="margin: 30px auto; overflow: hidden;">
        <div class="switchMenuPopUp">
            <span data-type="moduleWeChat" data-paymethod="wechatpay_pcnative" data-money="hide">微信</span>
            <span data-type="moduleParty" data-paymethod="alipay/directPay">支付宝</span>
            <span data-type="moduleBack" data-paymethod="">银行卡</span>
            <#--<span data-type="moduleMobileCard" data-paymethod="heepay/13" data-money="hide">手机充值卡</span>-->
            <#--暂时关闭移动手机短信-->
            <#--<span data-type="moduleMobile" data-paymethod="umpay" data-money="hide"> </span>-->
            <#--<span data-type="moduleSmartCard" data-paymethod="heepay/10" data-money="hide">骏卡</span>-->
            <#--<span data-type="moduleGrand" data-paymethod="heepay/41" data-money="hide">盛大一卡通</span>-->
            <#--<span data-type="moduleQQGrand" data-paymethod="heepay/57" data-money="hide">Q卡</span>-->
        </div>
        <div class="tabbox" style="width: 610px; float: right; clear: none; border-radius: 0 6px 6px 6px; ">
            <div class="payTypeBox" style="height: 550px;">
                <div class="payTypeTitleBox">
                    <p class="title">您正在使用 <strong id="paymentDataType">手机充值卡</strong> 充值作业币</p>

                    <p class="balance">作业币账户余额：<strong>${balance!0}</strong>作业币</p>
                </div>

                <dl>
                    <dt>充值的用户：</dt>
                    <dd>
                        <div style="font: bold 18px/42px arial;">${currentUser.profile.realname}(${currentUser.id})</div>
                    </dd>
                </dl>

                <dl id="amountOfMoney">
                    <dt>充值的金额：</dt>
                    <dd>
                        <input type="text" value="" name="payAmount" id="payAmount" class="int" maxlength="4"/>
                        <span class="text_gray_9">(1元=1个作业币)</span>
                        <div class="actualMoney text_gray_9">单笔充值金额必须小于1000元</div>
                    </dd>
                </dl>

                <#--template 微信支付-->
                <dl id="moduleWeChat" class="module-template" style="display: none;">
                    <dt>充值的金额：</dt>
                    <dd>
                        <div class="payment-code-box">
                            <input type="text" value="${(vendorAppsOrder.totalPrice)!10}" name="payAmount" class="int js-payAmountPublic" maxlength="4" disabled="disabled"/>
                            <#if !(vendorAppsOrder.totalPrice)??>
                                <a href="javascript:;" class="editAmount">修改</a>
                            </#if>
                            <span class="text_gray_9">(1元=1个作业币)</span>
                            <div class="actualMoney text_gray_9">单笔充值金额必须小于1000元</div>
                        </div>
                    </dd>
                    <dt>二维码：</dt>
                    <dd>
                        <div class="payment-code-box">
                            <div class="code-img" id="weChatImage">二维码生成中...</div>
                            <div class="code-info">
                                请使用微信扫描<br/>二维码以完成支付
                            </div>
                        </div>
                    </dd>
                </dl>

                <#--template 银行卡-->
                <dl id="moduleBack" class="module-template" style="display: none;">
                    <dt>选择银行：</dt>
                    <dd>
                        <div class="bank-box" style="background:#fff;">
                            <ul class="bank-list">
                            <#list banks as bank>
                                <li <#if bank_index == 0>class="active" </#if>><label><input type="hidden"
                                                                                             name="pay_bank"
                                                                                             value="${bank.payMethod}"/><span
                                        class="icon-bank ${bank.bankName}"></span></label></li>
                            </#list>
                            </ul>
                        </div>
                    </dd>
                </dl>
                <#--template 1-->
                <dl id="moduleMobileCard" class="module-template" data-title="手机充值卡">
                    <dt>选择充值方式：</dt>
                    <dd>
                        <div class="bank-box">
                            <ul class="bank-list">
                                <li class="active"><label><input type="hidden" name="pay_bank" value="heepay/13"/><span
                                        class="icon-bank UMPAY"></span></label></li>
                                <li><label><input type="hidden" name="pay_bank" value="heepay/14"/><span
                                        class="icon-bank CHINAUNICOM"></span></label></li>
                                <li><label><input type="hidden" name="pay_bank" value="heepay/15"/><span
                                        class="icon-bank TELECOM"></span></label></li>
                            </ul>
                        </div>
                    </dd>
                    <dt>充值卡面额：</dt>
                    <dd>
                        <div class="priceBox">
                            <#list [30, 50, 100] as c>
                                <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#list>
                            <div class="actualMoney">
                                你将兑换<strong class="v-money">0</strong>作业币
                            </div>
                        </div>
                    </dd>
                    <dd>
                        <div class="info">
                            提示：
                            <p>1、充值卡可以在报刊亭、便利店、手机营业厅购买哦。
                            <p>2、充值时请选择正确的充值卡和卡面所示金额，否则引起的交易失败或交易金额丢失将由用户承担。</p>
                        </div>
                    </dd>
                </dl>
                <#--template 1-->
                <dl id="moduleParty" class="module-template" style="display: none;">
                    <dt>选择充值方式：</dt>
                    <dd>
                        <div class="bank-box">
                            <ul class="bank-list">
                                <li class="active"><label><input type="hidden" name="pay_bank"
                                                                 value="alipay/directPay"/><span
                                        class="icon-bank ALIPAY"></span></label></li>
                            </ul>
                        </div>
                    </dd>
                </dl>
                <#--template 1-->
                <dl id="moduleSmartCard" class="module-template" style="display: none;">
                    <dt>充值卡面额：</dt>
                    <dd>
                        <div class="priceBox">
                            <#list [10, 20, 30, 50, 100] as c>
                                <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#list>
                            <div class="actualMoney">
                                你将兑换<strong class="v-money">0</strong>作业币
                            </div>
                        </div>
                    </dd>
                    <dd>
                        <div class="info">
                            提示：
                            <p>1、购买地点：骏卡可以在报刊亭、软件店、电脑城购买哦。</p>
                            <p>2、使用流程：按照您所购买的面额选择充值额度
                                点击立即支付进入支付页面，填写卡上的密码和账号即可</p>
                        </div>
                    </dd>
                </dl>
                <#--移动手机短信充值 1-->
                <dl id="moduleMobile" class="module-template" style="display: none;">
                    <dt>选择支付方式：</dt>
                    <dd>
                        <div class="bank-box">
                            <ul class="bank-list">
                                <li class="active"><label><input type="hidden" name="pay_bank" value="umpay"/><span class="icon-bank UMPAY"></span></label></li>
                            </ul>
                        </div>
                    </dd>
                    <dt>手机号码：</dt>
                    <dd>
                        <input type="text" value="" name="payMobile" id="payMobile" class="int"/>
                    </dd>
                    <dt>充值卡面额：</dt>
                    <dd>
                        <div class="priceBox">
                            <#list [25] as c>
                                <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c*0.76}"><span>${c}</span><strong>元</strong></a>
                            </#list>
                            <div class="actualMoney">
                                你将兑换<strong class="v-money">0</strong>作业币
                            </div>
                        </div>
                    </dd>
                    <dd>
                        <div class="info">
                            提示：短信充值的费率为<strong class="text_red">24%</strong>，由运营商收取。
                        </div>
                    </dd>
                </dl>
                <#--template 1-->
                <dl id="moduleGrand" class="module-template" style="display: none;">
                    <dt>充值卡面额：</dt>
                    <dd>
                        <div class="priceBox">
                            <#list [10, 20, 30, 50, 100] as c>
                                <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#list>
                            <div class="actualMoney">
                                你将兑换<strong class="v-money">0</strong>作业币
                            </div>
                        </div>
                    </dd>
                    <dd>
                        <div class="info">
                            提示：
                            <p>1、盛大一卡通可以在报刊亭、软件店、电脑城购买哦。</p>
                            <p>2、充值时请选择卡面所示金额，否则引起的交易失败或交易金额丢失将由用户承担。</p>
                        </div>
                    </dd>
                </dl>
                <#--template moduleQQGrand-->
                <dl id="moduleQQGrand" class="module-template" style="display: none;">
                    <dt>充值卡面额：</dt>
                    <dd>
                        <div class="priceBox">
                            <#list [5, 10, 15, 30, 60, 100, 200] as c>
                                <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#list>
                            <div class="actualMoney">
                                你将兑换<strong class="v-money">0</strong>作业币<#-- (Q卡充值的费率为<strong>15%</strong>，由运营商收取)-->
                            </div>
                        </div>
                    </dd>
                    <dd>
                        <div class="info">
                            提示：
                            <p>1、购买地点：Q卡可以在报刊亭、软件店、电脑城购买哦。</p>
                            <p>2、使用流程：按照您所购买的面额选择充值额度点击立即支付进入支付页面，填写卡上的密码和账号即可。</p>
                        </div>
                    </dd>
                </dl>
                <dl>
                    <dt></dt>
                    <dd>
                        <#if vendorAppsOrder??><input type="hidden" id="vendorOrderId" name="vendorOrderId" value="${vendorAppsOrder.id!}"></#if>
                        <a href="javascript:void(0);" class="w-btn w-btn-green v-studentVoxLogRecord" data-op="immediate-recharge" id="pay">立即充值</a>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</form>
<script type="text/javascript">
    $(function () {
        var supportedMobilePaymentMobileRegEx = /${supportedMobilePaymentMobileRegEx}/;
        var switchMenuPopUp = $(".switchMenuPopUp"); //左边支付类型
        var paymentDataType = $("#paymentDataType"); //类型名称
        var amountOfMoney = $("#amountOfMoney"); //金额框
        var payMethod = $("#payMethod"); //提交类型
        var payAmount = $("#payAmount"); //提交充值金额
        var payMobile = $("#payMobile"); //提交手机
        var recordType = "moduleBack";
        var eventTongJiVal;//记录支付方式名称
        var currentPayAmount;

        //点击面额
        $(".priceBox a").on("click", function () {
            var $this = $(this);
            var actualMoney = $this.siblings(".actualMoney");
            var $thisMoney = $this.data("actual-money");
            var $thisMoneyVar = $thisMoney * 40;

            payAmount.val($this.attr("data-value"));
            actualMoney.find(".v-money").text($this.data("actual-money"));//费率后共计
            $this.addClass("active").siblings().removeClass("active");
            <#if .now gt '2014-11-05 00:00:00'?datetime('yyyy-MM-dd HH:mm:ss')>
                actualMoney.find(".v-giveBeans").html("(赠送" + $thisMoneyVar + "学豆)");//赠送
            </#if>
        });

        //点击充值方式
        $(".bank-list li").on("click", function () {
            var $this = $(this);

            $("#payMethod").val($this.find("input").val());
            $this.addClass("active").siblings().removeClass("active");
        });

        //点击左边菜单类型
        var weChatPaymentUrl, getPaymentResult, recordFlowId;
        switchMenuPopUp.find("span").on("click", function () {
            var $this = $(this);

            if ($this.hasClass("sel")) {
                return false;
            }

            recordType = $this.attr("data-type");

            $this.addClass("sel").siblings().removeClass("sel");
            paymentDataType.text($this.text());
            eventTongJiVal = $this.text();

            $("#" + recordType).show().siblings(".module-template").hide();
            payMethod.val($this.attr("data-paymethod"));
            if ($this.data("money") == "hide") {
                amountOfMoney.hide();
            } else {
                amountOfMoney.show();
                amountOfMoney.find("input").val("");
            }

            $(".priceBox, .bank-list").each(function () {
                var $that = $(this);
                var _tempName = $that.closest(".module-template").attr("id");

                $that.find("li:first").addClass("active").siblings().removeClass("active");

                if (recordType == _tempName) {
                    $that.find("li:first").click();
                }
            });

            //生成二维码支付
            if(recordType == "moduleWeChat"){
                $('#pay').hide();
                getWeChatCode();
            }else{
                $('#pay').show();
                clearInterval(getPaymentResult);
            }
        });

        //生成二维码支付
        $("#moduleWeChat .editAmount").on("click", function(){
            var $this = $(this);
            var $int = $this.siblings(".int");

            if( !$17.isNumber($int.val()) || $int.val() < 1){
                $int.focus();
                return false;
            }

            if($this.text() != "修改"){
                $this.text("修改");
                $int.attr("disabled", "disabled");

                getWeChatCode();
            }else{
                $this.text("确定");
                $int.removeAttr("disabled");
                $int.focus();
            }
        });

        //生成二维码支付
        function getWeChatCode(){
            var $weChatImage = $("#weChatImage");
            var $int = $("#moduleWeChat .js-payAmountPublic");
            $.post("/finance/recharge/paymentqrcode/link.vpage", {
                vendorOrderId : $("#vendorOrderId").val(),
                amount : $int.val()
            }, function(data){
                if(data.success){
                    weChatPaymentUrl = "/qrcode?m=" + data.qrcode_url;
                    recordFlowId = data.flowId;
                    $weChatImage.html("<img src='"+ weChatPaymentUrl +"' style='width:200px; height: 200px; display: block;'/>");
                    getPaymentResult = setInterval(getPaymentStart, 1500);
                }else{
                    $weChatImage.html(data.info);
                }
            });
        }

        //生成二维码支付
        function getPaymentStart(){
            $.get("/finance/recharge/paymentqrcode/result.vpage", {flowId : recordFlowId }, function(data){
                if(data.success && data.paid){
                    clearInterval(getPaymentResult);
                    location.href = "recharging.vpage?types=recharging-result&flowId=" + recordFlowId;
                }
            });
        }

        if($17.getQuery("item")){
            switchMenuPopUp.find("span[data-type='" + $17.getQuery("item") + "']").click();
        }else{
            switchMenuPopUp.find("span").eq(0).click();
        }

        //限制金额两们小数
        payAmount.on("keyup", function(){
            var keyN =  /^-?\d+\.?\d{0,2}$/;
            var $thisVal = $(this).val();

            if(!keyN.test($thisVal)){
                if($thisVal.length > 0){
                    $(this).val(currentPayAmount);
                }else{
                    currentPayAmount = "";
                }
            }else{
                currentPayAmount = $thisVal;
            }
        });

        $(".js-payAmountPublic").on("keyup", function(){
            var keyN =  /^-?\d+\.?\d{0,2}$/;
            var $thisVal = $(this).val();

            if(!keyN.test($thisVal)){
                if($thisVal.length > 0){
                    $(this).val(currentPayAmount);
                }else{
                    currentPayAmount = "";
                }
            }else{
                if($thisVal >= 1000){
                    $(this).val(currentPayAmount);
                }else{
                    currentPayAmount = $thisVal;
                }
            }
        });

        //提交
        $("#pay").on("click", function () {
            if (payMethod.val() == "umpay") {
                if (!supportedMobilePaymentMobileRegEx.test(payMobile.val())) {
                    $17.alert('短信支付仅支持中国移动的手机号。如果您不是中国移动的手机用户，可以使用支付宝进行支付，支持各大银行卡和信用卡');
                    return false;
                }
            }

            if (payAmount.val() >= 1000) {
                $17.alert("单笔充值金额必须小于1000元");
                return false;
            }

            if ($17.isBlank(payMethod.val())) {
                $17.alert("支付方式不能为空");
                return false;
            }

            if (!$17.isNumber(payAmount.val())) {
                $17.alert("请输入正确的充值金额");
                return false;
            }

            $("#payConfirm").submit();

            $17.tongji("充值页面-点击立即支付-"+eventTongJiVal);

            var _tempContentBtn = (recordType == "moduleBack" || recordType == "moduleParty" ? { "查看充值结果": true, "充值卡支付": false} : { "查看充值结果": true});

            $.prompt("<div style='padding: 20px; line-height: 24px;'>请您在新打开的页面上完成付款。<br/>付款完成前请不要关闭此窗口。</div>", {
                title: "充值",
                buttons: _tempContentBtn,
                position: { width: 400},
                submit: function (e, v, m, f) {
                    if (v) {
                        location.href = "recharging.vpage?types=recharging";
                    } else {
                        $.prompt.close();
                        switchMenuPopUp.find("span[data-type='moduleMobileCard']").click();
                    }
                }
            });

            $17.voxLog({
                app : "student",
                module: "studentOperationTrack",
                op: "immediate-recharge"
            }, "student");

            return false;
        });
    });
</script>
<@app.css href="public/skin/project/afenti/css/public-order.css?1.0.3" />