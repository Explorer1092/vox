<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='订单支付'
pageJs=["paymentmobile"]
pageJsFile={"paymentmobile" : "public/script/paymentmobile/confirm"}
pageCssFile={"paymentmobile" : ["public/skin/paymentmobile/css/paymentskin"]}
>
    <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv())!false>
        <#assign weChatLinkHost = "//wechat.test.17zuoye.net">
    <#elseif (ProductDevelopment.isStagingEnv())!false>
        <#assign weChatLinkHost = "//wechat.staging.17zuoye.net">
    <#elseif (ProductDevelopment.isProductionEnv())!false>
        <#assign weChatLinkHost = "//xue.17xueba.com/p17wechat">
    </#if>

    <#assign hasAfentiPay = ((type == "afenti")!false)/>
    <#assign modesRecharge = {
    'payType' : 0,
    'className': 'p-icon-0',
    'hasOpen' : false,
    'hasActive' : "null",
    'payment' : 'recharge',
    'name' : '学贝支付 <span class="js-rechargeInfoText"></span>',
    'info': '<div class=js-rechargeInfoName></div>'
    }/>
    <#assign modesList = [
        <#--Finance_Open-->
        {
        'payType' : 0,
        'className': 'p-icon-0',
        'hasOpen' : false,
        'hasActive' : "null",
        'payment' : 'recharge',
        'name' : '学贝支付 <span class="js-rechargeInfoText"></span>',
        'info': '<div class=js-rechargeInfoName></div>'
        },
        {
        'payType' : 1,
        'className': 'p-icon-1',
        'hasOpen' : false,
        'hasActive' : "null",
        'payment' : '${((currentUser.userType == 2)!false)?string("wechatpay_parent", "wechatpay_studentapp")}',
        'name' : '微信支付',
        'info': '<p class="describe">推荐微信5.0及以上版本的用户使用</p>'
        },
        {
        'payType' : 2,
        'className': 'p-icon-2',
        'hasOpen' : false,
        'hasActive' : 'null',
        'payment' : '${((currentUser.userType == 2)!false)?string("alipay_parentapp", "alipay_studentapp")}',
        'name' : '支付宝支付',
        'info': '<p class="describe">推荐有支付宝账号的用户使用</p>'
        },
        {
        'payType' : 6,
        'className': 'p-icon-1',
        'hasOpen' : false,
        'hasActive' : 'null',
        'payment' : '${((currentUser.userType == 2)!false)?string("wechatpay_h5_parentapp", "wechatpay_h5_studentapp")}',
        'name' : '微信支付',
        'info': '<p class="describe">推荐微信5.0及以上版本的用户使用</p>'
        },
        {
        'payType' : 5,
        'className': 'p-icon-3',
        'hasOpen' : false,
        'hasActive' : 'null',
        'payment' : '${((currentUser.userType == 2)!false)?string("alipay_wap_parentapp", "alipay_wap_studentapp")}',
        'name' : '支付宝支付',
        'info': '<p class="describe">推荐有支付宝账号的用户使用</p>'
        }
    ]/>
<div class="js-confirmmain <#if isFinancePayment?? && isFinancePayment>finance<#else>confirm</#if>">
<div class="js-payDiv">
<#--<#if (((appKey == "Arithmetic")!false) || ((appKey == "FeeCourse" && currentUser.userType == 2)!false) || (hideTopTitle!false))>-->
    <#--&lt;#&ndash;满足以上三个条件之一的新绘本才显示title栏&ndash;&gt;-->
    <#--<#if ((appKey == "ELevelReading")!false)>-->
        <#--<div class="header-title">-->
            <#--<div class="p-inner">-->
                <#--<a href="javascript:void(0);" class="back JS-returnPreviousBtn"></a>-->
                <#--<div>订单支付</div>-->
            <#--</div>-->
        <#--</div>-->
    <#--</#if>-->
<#--<#else>-->
    <#--<div class="header-title">-->
        <#--<div class="p-inner">-->
            <#--<a href="javascript:void(0);" class="back JS-returnPreviousBtn"></a>-->
            <#--<div>订单支付</div>-->
        <#--</div>-->
    <#--</div>-->
<#--</#if>-->
<div class="header-title JS-header-title" style="display: none;">
    <div class="p-inner">
        <a href="javascript:void(0);" class="back JS-returnPreviousBtn"></a>
        <div>订单支付</div>
    </div>
</div>

<#if (error?has_content)!false>
     <div class="payment-mobile-box" id="paymentReleaseBox">
        <div class="pm-text" style="line-height: 150%; font-size: 1rem; text-align: center; padding: 150px 20px 0;">
            ${error!'----'}
        </div>
     </div>
<#else>
   <div class="paymentBox" id="paymentReleaseBox">
       <div class="paySection">
           <div class="p-list">
               <p class="p-info">${(productName)!'--'}</p>
               <div class="paySectionBox">
                   <p class="txtGray orderNum"><span><i>价</i><i class="orderI">格</i></span><var>：¥ ${(amount)!'--'}</var></p>
                   <p class="txtGray orderNum"><span>优惠减免</span>：- ¥ <var class="js-couponMoney" style="font-style: normal">0.00</var></p>
                   <p class="txtGray orderNum"><span><i>总</i><i class="orderI">计</i></span>：¥ <var class="js-finalPrice" style="font-style: normal">${(amount)!'--'}</var></p>
               </div>
           </div>
       </div>
       <div class="paySection JS-isAuditCccount" style="display: none;">
           <div class="p-list js-couponStyle">
               <p class="p-info">选择优惠方式：</p>
               <div class="typeList01">
                   <div class="tl-left">
                       <span class="name">优惠券</span>
                       <span class="labelYellow labelGray"><span class="js-couponLength">0</span>张可用</span>
                   </div>
                   <div class="tl-right">
                       <span class="js-DisplayCoupon <#--txtRed-->">未使用</span>
                   <#--<span>未使用</span>-->
                   </div>
               </div>
           </div>
       </div>
       <div class="paySection">
           <div class="p-list v-payAllList">
               <p class="p-info p-info-nob">选择支付方式</p>
                <#--<#if isFinancePayment?? && isFinancePayment>-->
                    <div class="recharge_box" style="display: none;">
                        <a href="javascript:void(0);" class="typeList02 ${(modesRecharge.className)!'default'} js-selectPayment"
                           data-active="${(modesRecharge.hasActive)!false}" data-paytype="${(modesRecharge.payType)!0}"
                           data-payment="${(modesRecharge.payment)!'default'}" style="display: ${(modesRecharge.hasOpen!false)?string('block', 'none')};"
                        >
                            <div class="inner">
                                <p class="type">${(modesRecharge.name)!'---'}</p>
                            ${(modesRecharge.info)!''}
                            </div>
                        </a>
                    </div>
                <#--<#else>-->
                    <div class="other_box" style="display: none;">
                        <#list modesList as md>
                            <!--active控制是否支付-->
                            <a href="javascript:void(0);" class="typeList02 ${(md.className)!'default'} js-selectPayment"
                               data-active="${(md.hasActive)!false}" data-paytype="${(md.payType)!0}"
                               data-payment="${(md.payment)!'default'}" style="display: ${(md.hasOpen!false)?string('block', 'none')};"
                            >
                                <div class="inner">
                                    <p class="type">${(md.name)!'---'}</p>
                                ${(md.info)!''}
                                </div>
                            </a>
                        </#list>
                    </div>
                <#--</#if>-->
           </div>
       </div>
   </div>
        <div class="p-footer">
            <div class="footerInner">
            <#--<#if isFinancePayment?? && isFinancePayment>-->
            <#if IOSFinanceRecharge?? && IOSFinanceRecharge>
                <div class="footerInnerDiv JS-footer-finance">
                    总计：<i>¥</i><span class="js-finalPrice"> ${(amount)!'--'}</span>
                    <p class="finance-p">将消耗<em class="js-finalPrice">${(amount)!'--'}</em>学贝</p>
                </div>
            <#else>
                <div class="footerInnerDiv JS-footer-confirm">
                    总计：<i>¥</i><span class="js-finalPrice"> ${(amount)!'--'}</span>
                </div>
            </#if>
                <a href="javascript:void(0);"
                   class="submit-btn js-submitConfirm"
                   data-appkey="${(appKey)!''}"
                   data-sessionkey="${(sessionKey)!''}"
                   data-order_seq="${(orderSeq)!0}"
                   data-order_id="${(orderId)!0}"
                   data-type="${type!'other'}"
                   data-paytype=""
                   data-payment=""
                   data-parent_userid="${(currentUser.id)!0}"
                   data-hide_toptitle="${(hideTopTitle!false)?string('true', 'false')}"
                   data-hide_apptitle="${(hideAppTitle!false)?string('true', 'false')}">
                    确认支付
                </a>
            </div>
        </div>
</#if>
</div>
<div class="couponDiv js-hasCoupon" style="display: none;">
<div class="header-title">
    <div class="p-inner">
        <a href="javascript:void(0);" class="back" id="couponReturnBtn"></a>
        <div class="js-couponTitle">选择优惠券</div>
    </div>
</div>
<div class="js-chooseCouponDiv">
    <a class="explained js-explainedBtn"><i>?</i><span>优惠券说明</span></a>
    <div class="notUse js-noCouponItem">
        <div class="titBox js-noCoupon">
            <span>不使用优惠券</span>
            <i class="js-noIcon"></i>
        </div>
        <p class="noText"><i></i>优惠券不能使用多张。</p>
        <p class="noText"><i></i>每张优惠券只能使用1次，使用后不退还。</p>
    </div>
    <div class="couponBox js-couponBox"></div>
    <div class="p-footer-coupon">
        <div class="footerInner">
            <a href="javascript:void(0);" class="submit-btn js-sureCouponBtn">
                确认
            </a>
        </div>
    </div>
</div>
<div class="js-couponExplain couponExplainBox" style="display: none;">
    <div class="y-wrapper">
        <div class="y-section">
            <h4 class="title">1.如何获取优惠券？</h4>
            <div class="txtBox">
                <p class="innerTxt"><i class="dot"></i>一起教育科技会在各种活动中向用户发放优惠券，请注意留意活动专区的各种活动哦。</p>
            </div>
        </div>
        <div class="y-section">
            <h4 class="title">2.如何使用优惠券？</h4>
            <div class="txtBox">
                <p class="innerTxt"><i class="dot"></i>在订单支付页面可选择优惠券进行支付。</p>
            </div>
        </div>
        <div class="y-section">
            <h4 class="title">3.优惠券使用过程中有什么限制？</h4>
            <div class="txtBox">
                <p class="innerTxt"><i class="dot"></i>优惠券只能用于指定的产品类型，如“阿分题英语优惠券”只能用户购买阿分题英语相关的产品；</p>
                <p class="innerTxt"><i class="dot"></i>每张订单只能使用一张优惠券；</p>
                <p class="innerTxt"><i class="dot"></i>每张优惠券只能被使用一次，被使用后不退还；</p>
                <p class="innerTxt"><i class="dot"></i>优惠券不兑现金、不开发票、不设找零、不能赠送；</p>
                <p class="innerTxt"><i class="dot"></i>优惠券需要在有效期内使用，过期作废。</p>
            </div>
        </div>
    </div>
</div>
</div>

<#--提示-->
<div class="dialog-alert" style="display: none;" id="paymentDialogAlert">
    <div class="da-mask"></div>
    <div class="da-content">
        <div class="da-hd js-content">数据错误！</div>
        <div class="da-ft">
            <a class="btn_dialog primary js-submit" data-type="cancel">取消</a>
            <a class="btn_dialog primary js-submit">知道了</a>
        </div>
    </div>
</div>

<div id="wapForm"></div>
<script type="text/html" id="T:家长列表">
    <div class="p-list" style="margin: 0; padding: 0; background:none; text-align: left; overflow: auto; height: 15rem;">
        <p class="p-info">选择学贝支付账户</p>
        <%for(var i = 0; i < financeList.length; i++){%>
        <a data-user_id="<%=financeList[i].userId%>" href="javascript:;" style="padding-left: 0;" class="typeList02 js-rechargeParent <%=(userId == financeList[i].userId ? 'active' : '')%>"
           data-finance="<%=financeList[i].finance%>"
           data-callname="<%=financeList[i].callName%>">
            <%=financeList[i].callName%> 余额 <b><%=financeList[i].finance%></b> 学贝
        </a>
        <%}%>
    </div>
</script>

<script type="text/html" id="T:支付完成奖励礼券Page">
    <div class="bg-packet">
        <div class="packet-box">
            <div class="packet-top"></div>
            <div class="packet-inner">
                <p class="packet-title">恭喜您获得自学礼券</p>
                <div class="packet-icon <%=(couponType == 'Amount' ? '' : 'gift-icon')%>"></div>
                <div class="packet-tip">
                    <span><%==name%></span>
                    <p>（<%==desc%>）</p>
                </div>
                <p class="packet-select">在订单支付页可选择使用此礼券哦</p>
                <p class="packet-contain" style="text-align: center;">请在领取后15天内使用。</p>
                <div class="packet-btn"><a href="javascript:;" class="JS-returnPreviousBtn">返回学习</a></div>
            </div>
            <div class="packet-btm"></div>
        </div>
    </div>
</script>
</div>
<#include "paytest.ftl"/>
<#include "password.ftl" />

<#assign studentIdsList = (pageBlockContentGenerator.getPageBlockContentHtml('StudentApp', 'ReviewAccounts'))!''/>

<script type="text/javascript">
    var productName = "${(productName)!'--'}";
    var devTestSwitch = ${((ftlmacro.devTestSwitch)!false)?string};
    var weChatLinkHost = "${weChatLinkHost!}";
    var appLinkHost = "${(ProductConfig.getMainSiteBaseUrl())!''}";
    var orderToken = "${orderToken!}";
    var returnUrl = "${returnUrl!}";
    var currentType = "${(currentUser.userType)!3}";
    var currentUserId = "${(currentUser.id)!0}";
    var pageAppKey = "${(appKey)!}";
    var currentOrderId = "${(orderId)!0}",
        currentPrice = "${(amount)!0}",
        loadCouponFlag = true;
    <#if (error?has_content)!false>
        loadCouponFlag = false;
    </#if>
    var openRecharge;
    <#if (currentUser.userType == 3)!false>
        <#if (currentStudentDetail.isJuniorStudent() || currentStudentDetail.isSeniorStudent())!false>
            openRecharge = false;
        <#else>
            openRecharge = true;
        </#if>
    <#else>
            openRecharge = true;
    </#if>
    var isFinancePayment = "${(isFinancePayment!false)?string}" == 'true' ? true : false;  // 学生端 分贝支付 灰度
    var hideWechatpay = "${(hideWechatpay!false)?string}" == 'true' ? true : false;
    var studentAppList =  "${studentIdsList!''}";
    var openH5payment = "${(isOpenH5payment!false)?string}" == 'true' ? true : false;
    var hideTopTitle = "${(hideTopTitle!false)?string('true', 'false')}";
    var hideAppTitle = "${(hideAppTitle!false)?string('true', 'false')}";
    var IOSFinanceRecharge = "${(IOSFinanceRecharge!false)?string}" == 'true' ? true : false;

</script>
</@layout.page>