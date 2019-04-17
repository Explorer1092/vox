<#import "module.ftl" as com>
<@com.page step=2 title="${(afentiOrder.productName)!''}确认订单 - 一起作业" paymentType="${payment}">
<#--手机短信支付开头-->
    <#assign smsPaymentStart = ( (!ProductDevelopment.isProductionEnv()) || ProductConfig.get("payment_use_umpay", "") != "" ) && totalPriceSms?? && (!(currentStudentDetail.rootRegionCode)?? || currentStudentDetail.rootRegionCode != 360000) />
    <#macro agentPaymentNameLogic>
        <#if payment != "agent">你<#else><strong>${afentiOrder.userName}</strong></#if>
    </#macro>
<div class="main">
<!--step2-->
<form id="frm" action="confirm.vpage" method="post" target="_blank">
<input type="hidden" name="orderId" value="${orderId}"/>
<input name="payMethod" type="hidden" id="payMethod" value=""/>
<input name="payAmount" type="hidden" id="payAmount" value="${afentiOrder.orderPrice!}"/>

<div class="payMainBox">
<div class="curaddress">请选择您的支付方式
    <span style="display: inline-block; color: #f00; margin-left: 30px; font-size: 12px;">
        <#if ["AfentiMath", "AfentiExam","AfentiChinese"]?seq_contains((afentiOrder.productServiceType)!"")>
            《阿分题<#if afentiOrder.productServiceType == "AfentiExam">英语》
            <#elseif afentiOrder.productServiceType == "AfentiMath">数学》
            <#else >语文》
            </#if>可在手机和电脑同步使用。
        <#else>
            学生只能在电脑上使用本产品
        </#if>
    </span>
</div>
<!--pay-->
<div class="tabbox">
    <table style="width:100%;">
        <thead>
        <tr>
            <td>订单</td>
            <th><b>学生姓名</b></th>
            <#if validityPeriod?has_content>
                <th >有效期</th></#if>
            <th width="120">价格</th>
            <#if modifyOrder && payment != "agent">
                <th width="120">操作</th>
            </#if>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><#if afentiOrder.orderProductServiceType == "AfentiExam"><@ftlmacro.gameAreaVersion/></#if>${afentiOrder.productName}</td>
            <th><b>${afentiOrder.userName}</b></th>
            <#if validityPeriod?has_content>
                <th>${validityPeriod}</th></#if>
            <th><b class="clrred" id="totalPrice">${afentiOrder.orderPrice!}</b>元</th>
            <#if modifyOrder && payment != "agent">
                <th>
                    <#switch afentiOrder.orderProductServiceType>
                        <#case 'AfentiBasic'>
                            <a href='basic-cart.vpage'>修改订单</a>
                            <#break />
                        <#case 'AfentiTalent'>
                            <a href='talent-cart.vpage'>修改订单</a>
                            <#break />
                        <#case 'TravelAmerica'>
                            <a href='travel-cart.vpage'>修改订单</a>
                            <#break />
                        <#case 'AfentiExam'>
                            <a href='exam-cart.vpage'>修改订单</a>
                            <#break />
                        <#case 'KaplanPicaro'>
                            <a href='picaro-cart.vpage'>修改订单</a>
                            <#break />
                        <#case 'Walker'>
                            <a href='walker-cart.vpage'>修改订单</a>
                            <#break />
                        <#case 'A17ZYSPG'>
                            <a href='spg-cart.vpage'>修改订单</a>
                            <#break />
                        <#case 'Stem101'>
                            <a href='stem-cart.vpage'>修改订单</a>
                            <#break />
                        <#default>
                            <a href='${(afentiOrder.orderProductServiceType?lower_case)!}-cart.vpage'>修改订单</a>
                    </#switch>
                </th>
            </#if>
        </tr>
        </tbody>
    </table>
</div>

<#--PC端先暂不开放优惠券,暂且注释掉-->
<#--<div class="tabbox ticketBox">
    <table style="width:100%;">
        <thead>
        <tr>
            <td>选择优惠</td>
        </tr>
        </thead>
        <tbody>
        <tr class="trBox">
            <td>使用优惠券</td>
            <td>
                <div class="selBox">
                    <select name="couponSelect" id="couponSelect" class="js-hasCoupon" style="display:none;">
                    </select>
                    <div class="js-noCoupon" style="display: none;">
                        无可用优惠券
                    </div>
                </div>
            </td>
        </tr>
        <tr class="trbox js-hasCoupon" style="display: none;">
            <td class="n-padd" colspan="2">
                <div class="num"><span><span class="js-couponNum">0</span>张优惠券可用</span></div>
            </td>
        </tr>
        </tbody>
    </table>
</div>-->

<!--选择支付方式-->
<div class="payMainBox">
    <div class="switchMenuPopUp">
        <#--<#if ["Walker-180", "Walker-30", "AfentiExam-30"]?seq_contains(afentiOrder.orderProductServiceType+"-"+afentiOrder.validPeriod)>
            <span data-type="moduleEntityCard" data-paymethod="entity_card">实物卡开通</span>
        </#if>-->
        <span data-type="moduleParty" data-paymethod="alipay/directPay">支付宝<#--/财付通--></span>
        <span data-type="moduleWeChat" data-paymethod="wechatpay_pcnative">微信</span>
        <span data-type="moduleBack" data-paymethod="">银行卡</span>
        <#if (payment != "agent" && balance gt 0)!false>
            <span data-type="moduleBalance" data-paymethod="vox_amount" data-money="hide" data-balance="${balance!0}">余额支付</span>
        </#if>
        <#--暂时关闭移动手机短信-->
        <#--<#if smsPaymentStart>
            <span data-type="moduleMobile" data-paymethod="umpay" data-money="hide" data-price="25">移动手机短信</span>
        </#if>-->
        <#if (afentiOrder.orderPrice lt 100)!false>
            <#--<span data-type="moduleMobileCard" data-paymethod="heepay/13" data-money="hide">手机充值卡</span>-->
            <#--<span data-type="moduleSmartCard" data-paymethod="heepay/10" data-money="hide">骏卡</span>-->
            <#--<span data-type="moduleGrand" data-paymethod="heepay/41" data-money="hide">盛大一卡通</span>-->
        </#if>
        <#--<#if afentiOrder.totalPrice lt 200>
            <span data-type="moduleQQGrand" data-paymethod="heepay/57" data-money="hide">Q卡</span>
        </#if>-->
    </div>

    <div class="tabbox" style="width: 720px; float: right; clear: none; border-radius: 0 6px 6px 6px;">
        <div id="bank_panel" class="payTypeBox" style="height: 700px;">
            <div class="payTypeTitleBox">
                <p class="title">您正在使用 <strong id="paymentDataType">余额支付</strong> 支付 <span style="font-weight: normal; font-size: 12px; color: #f00">&nbsp;&nbsp;开通产品，要先经过爸爸妈妈的同意哦。</span></p>
            </div>
            <#--实体卡开通 - 支付方式-->
            <dl id="moduleEntityCard" class="module-template" style="display: none;">
                <dt>实物卡密码：</dt>
                <dd>
                    <input type="text" value="" name="entityCardPassword" class="int" maxlength="24"/>
                </dd>
                <dt>验证码：</dt>
                <dd>
                    <input type="text" value="" name="verificationCode" class="int" style="width: 80px;" maxlength="4"/>
                    <span  style="display: inline-block; vertical-align: middle; margin-bottom: 10px;" id="captchaClick">
                        <img id='captchaImage'/>
                        看不清？<a href="javascript:void(0);" class="clrblue">换一个</a>
                    </span>
                </dd>
            </dl>

            <dl id="moduleWeChat" class="module-template" style="display: none;">
                <dt></dt>
                <dd style="margin:70px 0 0 250px;">
                    <div class="payment-code-box">
                        <div class="code-img" id="weChatImage">二维码生成中...</div>
                        <div class="code-info">
                            请使用微信扫描<br/>二维码以完成支付
                        </div>
                    </div>
                </dd>
            </dl>

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
            <dl id="moduleParty" class="module-template" style="display: none;">
                <dt>选择支付方式：</dt>
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
            <#if smsPaymentStart>
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
                        <input type="text" value="" name="payMobile" id="payMobile" class="int" maxlength="11"/>
                        <b style="color: #e00;">(注：手机短信支付价格为25元)</b>
                    </dd>
                    <dd style="margin: 0;">
                        <div class="chinaMoblieInfo"
                             style="font:12px/24px arial; color: #666; padding: 0 20px;">
                            <p>1、中国移动通信账户支付是中国移动电子商务的支付方式之一。10658008是中国移动通信账户支付专用短信特服号码。 </p>

                            <p>2、本服务支持移动全球通、动感地带、神州行等品牌的用户。 </p>

                            <p>3、用户使用中国移动通信账户支付，无免费试用，支付成功，即刻扣费，商品价格不含通信费。 </p>

                            <p>4、部分商品可用话费购买的数量有限，若购买失败，请选择其他支付方式。 </p>

                            <p>5、部分省份&ldquo;赠送&rdquo;&ldquo;返还&rdquo;的话费不可用于购买商品。如：北京、黑龙江等。 </p>

                            <p style="color:#f00;">6、业务使用中会产生0.1元/条（或按照您参与的运营商话费套餐标准）短信通信费，具体请咨询10086。</p>

                            <p>7、按次产品无需退订，包月产品如需退订，请发送0000至10658008。 </p>

                            <p>8、因系统数据传输压力原因，部分省份用户在月底最后一天20:00后的扣费可能计入次月账单，请广大用户注意。 </p>

                            <p>9、移动支付客服电话：4006125880（只收市话费，无长途费用）；125880(只支持移动手机，0.3元/分，无长途话费)。 </p>

                            <p>10、一起作业网客服电话：<@ftlmacro.hotline/>。 </p>
                        </div>
                    </dd>
                </dl>
            </#if>

            <dl id="moduleMobileCard" class="module-template" data-title="手机卡" style="display: none;">
                <dt>选择支付方式：</dt>
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
                        <#assign moduleMobileCard = [30, 50, 100]/>
                        <#list moduleMobileCard as c>
                            <#if c gt afentiOrder.orderPrice>
                            <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#if>
                        </#list>
                        <div class="actualMoney">
                            <span class="v-big">你将兑换<strong class="v-money">0</strong>作业币，需付款<strong>${(afentiOrder.orderPrice)!0}</strong>作业币，剩余<strong class="v-recharge">0</strong>作业币将充进<@agentPaymentNameLogic/>的账户。</span>
                            <span class="v-small">选择的面额无法支付，兑换的<strong class="v-money">0</strong>作业币将直接充进你的账户。</span>
                         </div>
                    </div>
                </dd>
                <dd>
                    <div class="info">
                        提示：
                        <p>1、充值卡可以在报刊亭、便利店、手机营业厅购买哦。
                        <p>3、充值时请选择正确的充值卡和卡面所示金额，否则引起的交易失败或交易金额丢失将由用户承担。</p>
                    </div>
                </dd>
            </dl>

            <dl id="moduleSmartCard" data-title="骏卡" class="module-template" style="display: none;">
                <dt>充值卡面额：</dt>
                <dd>
                    <div class="priceBox">
                        <#assign moduleSmartCard = [10, 20, 30, 50, 100]/>
                        <#list moduleSmartCard as c>
                            <#if c gt afentiOrder.orderPrice>
                            <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#if>
                        </#list>
                        <div class="actualMoney">
                            <span class="v-big">你将兑换<strong class="v-money">0</strong>作业币，需付款<strong>${(afentiOrder.orderPrice)!0}</strong>作业币，剩余<strong class="v-recharge">0</strong>作业币将充进<@agentPaymentNameLogic/>的账户。</span>
                            <span class="v-small">选择的面额无法支付，兑换的<strong class="v-money">0</strong>作业币将直接充进你的账户。</span>
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

            <dl id="moduleGrand" data-title="盛大一卡通" class="module-template" style="display: none;">
                <dt>充值卡面额：</dt>
                <dd>
                    <div class="priceBox">
                        <#assign moduleGrand = [5, 10, 30, 35, 45, 100]/>
                        <#list moduleGrand as c>
                            <#if c gt afentiOrder.orderPrice>
                            <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#if>
                        </#list>
                        <div class="actualMoney">
                            <span class="v-big">你将兑换<strong class="v-money">0</strong>作业币，需付款<strong>${(afentiOrder.orderPrice)!0}</strong>作业币，剩余<strong class="v-recharge">0</strong>作业币将充进<@agentPaymentNameLogic/>的账户。</span>
                            <span class="v-small">选择的面额无法支付，兑换的<strong class="v-money">0</strong>作业币将直接充进你的账户。</span>
                           </div>
                    </div>
                </dd>
                <dd>
                    <div class="info">
                        提示：
                        <p>1、盛大一卡通可以在报刊亭、软件店、电脑城购买哦。</p>
                        <p>3、充值时请选择卡面所示金额，否则引起的交易失败或交易金额丢失将由用户承担。</p>
                    </div>
                </dd>
            </dl>

            <#--moduleQQGrand-->
            <dl id="moduleQQGrand" data-title="Q卡支付" class="module-template" style="display: none;">
                <dt>充值卡面额：</dt>
                <dd>
                    <div class="priceBox">
                        <#assign moduleQQGrand = [5, 10, 15, 30, 60, 100, 200]/>
                        <#list moduleQQGrand as c>
                            <#if c gt afentiOrder.orderPrice>
                                <a href="javascript:void(0);" data-value="${c}" data-actual-money="${c}"><span>${c}</span><strong>元</strong></a>
                            </#if>
                        </#list>
                        <div class="actualMoney">
                            <span class="v-big">你将兑换<strong class="v-money">0</strong>作业币，需付款<strong>${(afentiOrder.orderPrice)!0}</strong>作业币，剩余<strong class="v-recharge">0</strong>作业币将充进<@agentPaymentNameLogic/>的账户。</span>
                            <span class="v-small">选择的面额无法支付，兑换的<strong class="v-money">0</strong>作业币将直接充进你的账户。</span>
                            <#--<p style="line-height: 24px;">(Q卡支付的费率为<strong>15%</strong>，由运营商收取)</p>-->
                        </div>
                    </div>
                </dd>
                <dd>
                    <div class="info">
                        提示：
                        <p>1、购买地点：Q卡可以在报刊亭、软件店、电脑城购买哦。</p>
                        <p>2、使用流程：按照您所购买的面额选择充值额度
                            点击立即支付进入支付页面，填写卡上的密码和账号即可</p>
                    </div>
                </dd>
            </dl>

            <dl id="moduleBalance" data-title="余额支付" class="module-template" style="display: none;">
                <dt>您的当前余额：</dt>
                <dd>
                    <div class="price lineHeight">
                        <span>${balance!0}</span><strong>作业币 (1元=1个作业币)</strong>
                    </div>
                    <#if (balance?? && balance lt afentiOrder.orderPrice) || !balance??>
                        <span style="color:#f00;">余额不足，请使用其他支付方式！</span>
                    </#if>
                </dd>
                <#if (balance?? && balance lt afentiOrder.orderPrice) || !balance??>
                    <#--<dd>
                        <a href="/student/center/recharging.vpage?types=recharging-go" target="_blank" class="getOrange gPaygetGreen" onclick="$17.tongji('付费页面-点击立即充值');">立即充值</a>
                    </dd>-->
                <#else>
                    <dt>请输入支付密码：</dt>
                    <dd>
                        <input type="password" value="" name="paymentPwd" id="paymentPwd" class="int" <#if !haspaypwd || (!balance?? || balance lt afentiOrder.orderPrice)>disabled="disabled"</#if>/>
                    </dd>
                    <#if !haspaypwd>
                        <dd>
                            <div class="info">您还没有设置支付密码，无法购买，请立即设置您的支付密码
                                <a href="${(ProductConfig.getUcenterUrl())!''}/student/center/account.vpage?updateType=paymentpassword" target="_blank">马上设置>></a>
                            </div>
                        </dd>
                    </#if>
                </#if>
            </dl>
            <dl>
                <dd>
                    <#if afentiOrder.orderProductServiceType == "KaplanPicaro">
                        <p style="color: #f00;">此产品暂不支持购买~</p>
                        <a href="javascript:void(0);" class="getOrange getOrange_gray">暂停购买</a>
                    <#else>
                        <#--用户当前vip时长小于365天时才能支付-->
                        <#if dayToExpireBiggerThan365?? && dayToExpireBiggerThan365>
                            ${dayToExpireErrorInfo!''}
                        <#else>
                            <a href="javascript:void(0);" class="getOrange v-studentVoxLogRecord" data-op="immediate-payment" id="pay">立即支付</a>
                        </#if>
                    </#if>
                </dd>
            </dl>
            <#--<#if payment != "agent" && afentiOrder.orderProductServiceType != "KaplanPicaro">
                <dl>
                    <dt></dt>
                    <dd>
                        <div class="send_main_pay">
                            <p class="p_1"><i class="arrow arrow_down"></i>没找到适合我的支付方式</p>

                            <p class="content" style="display: none;">你可以找好友或家人替你付款哦。<a
                                    href="agentmobile.vpage?orderId=${afentiOrder.id}" id="agentEnter"><span>找人代付<i
                                    class="arrow arrow_more"></i></span></a></p>
                        </div>
                    </dd>
                </dl>
            </#if>-->
        </div>
    </div>
    <!--//-->
</div>
</div>
</form>
</div>
<!--payPop-->
<div id="payColorBox" style="display: none; ">
    <div class="alpha_back"></div>
    <div class="alpha_content_layer" style="margin: -109px 0 0 -180px">
        <div class="alpha_layer_ie">
            <div class="payColorBox">
                <div class="successBox paypopBox" style="width: auto; margin-left: 30px; padding-top: 50px;">
                    <s class="iblock infoview"></s>

                    <div class="content">
                        <p class="ctn">请您在新打开的页面上完成付款。<br/>付款完成前请不要关闭此窗口。<br/><#--<#if afentiOrder.orderPrice lt 100><span id="popupRepayHeepay_text">无法支付？建议用手机充值卡支付。</span></#if>--></p>
                        <a href="finished.vpage?orderId=${orderId}" class="publicBtn greenBtn" id="finish"><i
                                class="lB"></i><i class="tB" style="padding: 0"><span style="padding: 0; font-size: 12px;">已完成支付</span></i><i class="rB"></i></a>
                        <#--<#if afentiOrder.orderPrice lt 100>
                            <a href="javascript:void(0);" id="popupRepayHeepay" class="publicBtn blueBtn"><i
                                    class="lB"></i><i class="tB" style="padding: 0"><span style="padding: 0; font-size: 12px;">使用手机充值卡支付</span></i><i class="rB"></i></a>
                        </#if>-->
                    </div>
                </div>
                <div class="padten" style="margin-top:10px;"><a href="javascript:void(0);" id="repay"><span
                        class="clrblue"><<返回重新选择支付方式</span></a></div>
            </div>
        </div>
    </div>
</div>
<!--//-->
    <#switch afentiOrder.orderProductServiceType>
        <#case "AfentiExam">
            <#assign productNamePage="阿分题">
            <#break />
        <#case "AfentiBasic">
            <#assign productNamePage="冒险岛">
            <#break />
        <#case "AfentiTalent">
            <#assign productNamePage="单词达人">
            <#break />
    </#switch>
<script type="text/javascript">
    $(function () {
        var supportedMobilePaymentMobileRegEx =/${supportedMobilePaymentMobileRegEx}/;
        var switchMenuPopUp = $(".switchMenuPopUp"); //左边支付类型
        var paymentDataType = $("#paymentDataType"); //类型名称
        var payMethod = $("#payMethod"); //提交类型
        var payAmount = $("#payAmount"); //提交支付面额
        var payMobile = $("#payMobile"); //提交手机
        var recordType = "moduleBack";
        var currentProductPrice = ${afentiOrder.orderPrice!0};
        var eventTongJiVal;//记录支付方式名称

        //点击面额
        $(".priceBox a").on("click", function () {
            var $this = $(this);
            var actualMoney = $this.siblings(".actualMoney");
            var $thisMoney = $this.data("actual-money");
            var $thisMoneyVar = $thisMoney * 40;

            payAmount.val($this.attr("data-value"));

            actualMoney.find(".v-money").text($this.data("actual-money"));//费率后共计
            actualMoney.find(".v-recharge").text(($this.data("actual-money") - currentProductPrice).toFixed(2));//剩余充值余额
            <#if .now gt '2014-11-05 00:00:00'?datetime('yyyy-MM-dd HH:mm:ss')>
                actualMoney.find(".v-giveBeans").html("(赠送" + $thisMoneyVar + "学豆)");//赠送
            </#if>

            if(currentProductPrice > $this.data("actual-money")){
                actualMoney.find(".v-big").hide();
                actualMoney.find(".v-small").show();
            }else{
                actualMoney.find(".v-big").show();
                actualMoney.find(".v-small").hide();
            }

            $this.addClass("active").siblings().removeClass("active");
        });

        //点击充值方式
        $(".bank-list li").on("click", function () {
            var $this = $(this);

            $("#payMethod").val($this.find("input").val());
            $this.addClass("active").siblings().removeClass("active");
        });

        //点击左边菜单类型
        var weChatPaymentUrl, getPaymentResult;
        switchMenuPopUp.find("span").on("click", function () {
            var $this = $(this);

            if ($this.hasClass("sel")) {
                return false;
            }

            if($this.data("paymethod") == "umpay"){
                $("#totalPrice").text($this.data("price"));
            }else{
                $("#totalPrice").text(currentProductPrice);
            }

            recordType = $this.attr("data-type");
            eventTongJiVal = $this.text();

            $this.addClass("sel").siblings().removeClass("sel");
            paymentDataType.text($this.text());

            $("#" + recordType).show().siblings(".module-template").hide();
            payMethod.val($this.attr("data-paymethod"));

            $(".priceBox, .bank-list").each(function () {
                var $that = $(this);
                var _tempName = $that.closest(".module-template").attr("id");

//                $that.find("a:first").addClass("active").siblings().removeClass("active");
                $that.find("li:first").addClass("active").siblings().removeClass("active");

                if (recordType == _tempName) {
//                    $that.find("a:first").click();
                    $that.find("li:first").click();
                }
            });

            if(recordType == "moduleBack" || recordType == "moduleParty"){
                $("#popupRepayHeepay").show();
                $("#popupRepayHeepay_text").show();
            }else{
                $("#popupRepayHeepay").hide();
                $("#popupRepayHeepay_text").hide();
            }

            if(recordType == "moduleBalance" && $this.data("balance") < currentProductPrice){
                $('#pay').hide();
            }else{
                $('#pay').show();
                clearInterval(getPaymentResult);
            }

            //生成二维码支付
            if(recordType == "moduleWeChat"){
                var $weChatImage = $("#weChatImage");

                $('#pay').hide();

                if(weChatPaymentUrl == null){
                    var weChatPayConfirm = function () {
                        $.post("/apps/afenti/paymentqrcode/link.vpage", {oid : "${orderId!0}"}, function(data){
                            if(data.success){
                                weChatPaymentUrl = "/qrcode?m=" + data.qrcode_url;
                                $weChatImage.html("<img src='"+ weChatPaymentUrl +"' style='width:200px; height: 200px; display: block;'/>");

                                getPaymentResult = setInterval(getPaymentStart, 1500);
                            }else{
                                $weChatImage.html(data.info);
                            }
                        });
                    };
                    //PC端先暂不开放优惠券,暂且注释掉
//                    var couponSelectNode = $('#couponSelect').find('option:selected');
//                    if(couponSelectNode.val()){
//                        relatedCouponOrder(couponSelectNode.val(),couponSelectNode.data('cid'),function(){
//                            weChatPayConfirm();
//                        });
//                    }else{
//                        weChatPayConfirm();
//                    }
                    weChatPayConfirm();
                }else{
                    $weChatImage.html("<img src='"+ weChatPaymentUrl +"' style='width:200px; height: 200px; display: block;'/>");

                    getPaymentResult = setInterval(getPaymentStart, 1500);
                }
            }
        }).eq(0).click();

        function getPaymentStart(){
            $.get("/apps/afenti/paymentqrcode/result.vpage", {oid : "${orderId!0}"}, function(data){
                if(data.success && data.paid){
                    clearInterval(getPaymentResult);
                    location.href = "/apps/afenti/order/finished.vpage?orderId=${orderId!0}";
                }
            });
        }

        $("#popupRepayHeepay").on("click", function(){
            switchMenuPopUp.find("span").eq(1).click();
            $('#payColorBox').hide();
        });

        //提交
        $('#pay').click(function () {
            var $this = $(this);
            var moduleEntityCard = $("#moduleEntityCard");//实体卡开通

            if($this.hasClass("getOrange_gray")){
                return false;
            }

            //判断充值卡时必须选择面额
            if(recordType == "moduleSmartCard" && !$("#moduleSmartCard .priceBox a").hasClass("active")){
                $17.alert("请选择充值卡面额。");
                return false;
            }else if(recordType == "moduleMobileCard" && !$("#moduleMobileCard .priceBox a").hasClass("active")){
                $17.alert("请选择充值卡面额。");
                return false;
            }

            //短信支付
            if (payMethod.val() == "umpay") {
                if (!supportedMobilePaymentMobileRegEx.test(payMobile.val())) {
                    $17.alert('短信支付仅支持中国移动的手机号。如果您不是中国移动的手机用户，可以使用支付宝进行支付，支持各大银行卡和信用卡');
                    return false;
                }
            }

            var payConfirm = function () {
                $.prompt("<div class='w-ag-center'>亲爱的同学，购买该款学习产品，要先征得爸爸妈妈的同意哦！</div>", {
                    focus: 1,
                    title: "系统提示",
                    buttons: { "取消": false, "确定": true },
                    position: {width: 500},
                    submit : function(e, v){
                        if(v){
                            $.prompt.close();
                            //start

                            //实体卡开通
                            if (payMethod.val() == "entity_card") {
                                var entityCardPassword = moduleEntityCard.find("input[name='entityCardPassword']").val();
                                var verificationCode = moduleEntityCard.find("input[name='verificationCode']").val();

                                if($17.isBlank(entityCardPassword)){
                                    $17.alert("请输入实物卡密码");
                                    return false;
                                }

                                if($17.isBlank(verificationCode)){
                                    $17.alert("请输入验证码");
                                    return false;
                                }

                                $this.addClass("getOrange_gray");

                                $.post("/apps/afenti/product_card_payment.vpage", {
                                    captchaToken : "${captchaToken!0}",
                                    captchaCode : verificationCode,
                                    orderId : "${orderId!0}",
                                    productCardKey : entityCardPassword
                                }, function(data){
                                    if(data.success){
                                        $17.alert("支付完成", function(){
                                            setTimeout(function(){
                                                location.href = "finished.vpage?orderId=${orderId}";
                                            }, 200);
                                        });
                                    }else{
                                        //失败
                                        $17.alert(data.info);
                                    }
                                    $this.removeClass("getOrange_gray");
                                    refreshCaptcha();
                                });

                                return false;
                            }

                            //余额支付
                            if (payMethod.val() == "vox_amount") {
                                var paymentPwdVal = $("#paymentPwd").val();
                                var balance = ${balance!0};

                                if(balance < currentProductPrice) {
                                    $17.alert("余额不足，请先充值！");
                                    return false;
                                }

                                if ($17.isBlank(paymentPwdVal)) {
                                    $17.alert("请输入支付密码");
                                    return false;
                                }

                                $this.addClass("getOrange_gray");

                                $.post("/apps/afenti/amount_payment.vpage", {paymentPwd: $("#paymentPwd").val(), orderId : "${orderId!0}"}, function (data) {
                                    if (data.success) {
                                        location.href = "/apps/afenti/order/finished.vpage?orderId=${orderId!0}";
                                        if ($17.getQuery("payment") == "agent") {
                                            $17.tongji("找人代付-点击立即支付-"+ eventTongJiVal +"-${productNamePage!''}");
                                        } else {
                                            $17.tongji("付费页面-点击立即支付-"+ eventTongJiVal +"-${productNamePage!''}");
                                        }
                                    } else {
                                        if(data.info == "支付密码错误"){
                                            $17.alert(data.info);
                                        }else{
                                            location.href = "/apps/afenti/order/finished.vpage?orderId=${orderId!0}";
                                        }
                                        $this.removeClass("getOrange_gray");
                                    }
                                });
                                return false
                            }

                            $('#payColorBox').show();
                            $('#frm').submit();
                            if ($17.getQuery("payment") == "agent") {
                                $17.tongji("找人代付-点击立即支付-"+ eventTongJiVal +"-${productNamePage!''}");
                            } else {
                                $17.tongji("付费页面-点击立即支付-"+ eventTongJiVal +"-${productNamePage!''}");
                            }

                            $17.voxLog({
                                app : "student",
                                module: "studentOperationTrack",
                                op: "immediate-payment"
                            }, "student");
                            //end
                        }
                    }
                });
            };
            //PC端先暂不开放优惠券,暂且注释掉
//            var couponSelectNode = $('#couponSelect').find('option:selected');
//            if(couponSelectNode.val()){
//                relatedCouponOrder(couponSelectNode.val(),couponSelectNode.data('cid'),function(){
//                    payConfirm();
//                });
//            }else{
//                payConfirm();
//            }
            payConfirm();
            return false;
        });


        //popup event
        $("#agentEnter").on("click", function () {
            $17.tongji("找人代付-入口-${productNamePage!''}");
        });

        $('#repay').click(function () {
            $('#payColorBox').hide();
        });

        $('#finish').click(function () {
            $('#payColorBox').hide();
        });

        <#if (afentiOrder.orderProductServiceType == 'AfentiExam')!false>
            $17.tongji("阿分题统计", "阿分题_支付页_立即支付");
        </#if>

        /*没找到适合我的支付方式*/
        $(".send_main_pay .p_1").on("click", function () {
            var $this = $(this);
            if (!$this.find(".arrow").hasClass("arrow_up")) {
                $this.find(".arrow").addClass("arrow_up");
                $this.siblings(".content").show();
            } else {
                $this.find(".arrow").removeClass("arrow_up");
                $this.siblings(".content").hide();
            }
        });

        //验证码
        refreshCaptcha();

        $("#captchaClick").on("click", function(){
            refreshCaptcha();
        });

        function refreshCaptcha() {
            $('#captchaImage').attr('src', "/captcha?" + $.param({
                'module': 'findAccount',
                'token': '${captchaToken!0}',
                't': new Date().getTime()
            }));
        }

        // PC端先暂不开放优惠券,暂且注释掉
        <#--function getCouponData() {-->
            <#--$.get('/coupon/loadcoupons.vpage?orderId=${orderId!0}',function (res) {-->
                <#--if(res.success){-->
                    <#--if(res.coupons && res.coupons.length != 0){-->
                        <#--$(".js-couponNum").html(res.coupons.length);-->
                        <#--var temp = '<option value="">==请选择==</option>';-->
                        <#--for(var i = 0;i<res.coupons.length;i++){-->
                            <#--temp += '<option value="'+res.coupons[i].couponUserRefId+'" data-cid="'+res.coupons[i].couponId+'">'+res.coupons[i].couponName+'</option>';-->
                        <#--}-->
                        <#--$("#couponSelect").append(temp);-->
                        <#--$(".js-hasCoupon").show();-->
                        <#--$(".js-noCoupon").hide();-->
                    <#--}else{-->
                        <#--$('.js-noCoupon').show();-->
                        <#--$(".js-hasCoupon").hide();-->
                    <#--}-->
                <#--}else{-->
                    <#--alert(res.info);-->
                <#--}-->
            <#--})-->
        <#--}-->

        //getCouponData();

        <#--var ajaxFlag = 1;-->
        <#--function relatedCouponOrder(refId,cid,callback) {-->
            <#--if (ajaxFlag) {-->
                <#--ajaxFlag = 0;-->
                <#--$.post('/coupon/relatedcouponorder.vpage', {-->
                    <#--orderId: "${orderId!0}",-->
                    <#--refId: refId,-->
                    <#--couponId: cid-->
                <#--}, function (res) {-->
                    <#--ajaxFlag = 1;-->
                    <#--debugger;-->
                    <#--if (res.success) {-->
                        <#--if(typeof callback === 'function'){-->
                            <#--callback();-->
                        <#--}-->
                    <#--}else{-->
                        <#--$17.alert(res.info?res.info:'关联优惠券出错');-->
                    <#--}-->
                <#--});-->
            <#--}-->
        <#--}-->


    });
</script>
</@com.page>
