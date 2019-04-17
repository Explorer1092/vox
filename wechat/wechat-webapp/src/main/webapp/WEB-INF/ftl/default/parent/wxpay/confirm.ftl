<#import "../layout.ftl" as ucenter>
<@ucenter.page title='订单详情' pageJs="confirmProduct">
    <@sugar.capsule css=['product'] />
    <#assign productType = productType!''>
    <#assign productIntroduction= {
    "AfentiExam": {
        "img": "/public/images/parent/product/detail/afenti.png"
    },
    "TravelAmerica": {
        "img": "/public/images/parent/product/detail/America.png"
    },
    "Walker": {
        "img": "/public/images/parent/product/detail/Walker.png"
    },
    "SanguoDmz": {
        "img": "/public/images/parent/product/detail/sanguo.jpg"
    },
    "A17ZYSPG": {
        "img": "/public/images/parent/product/detail/fairyland.jpg"
    },
    "Stem101": {
        "img": "/public/images/parent/product/stem101.png"
    },
    "PetsWar": {
        "img": "/public/images/parent/product/pets.png"
    },
    "WukongShizi": {
        "img": "/public/images/parent/product/wukongshizi.png"
    },
    "WukongPinyin": {
        "img": "/public/images/parent/product/wukongpinyin.png"
    }
    }>
    <div class="main body_background_gray">
        <p class="msg">课外乐园的应用均由第三方公司提供，请自愿使用！</p>
        <div class="sendBean_box" style="margin:0;border:none;">
            <div class="sb_up" style="line-height: 90px;height:187px;background-color: #f9f9f9;">
                <dl>
                    <dt><img src="${(productIntroduction[productType].img)!}" alt="" width="187" /></dt>
                    <dd>
                        <p style="font-size: 26px;white-space:nowrap;overflow:hidden; text-overflow:ellipsis;" class="text_blue">${productName!''}</p>
                        <p class="content">${info!''}</p>
                    </dd>
                </dl>
            </div>
            <div class="sb_down">
                <div class="price_content">
                    <p>
                        支付金额：<strong class="text_red">￥ ${price!0}</strong>
                    </p>
                    <#if period?? && period?has_content>
                        <p class="js-learnCycle">
                            学习周期：<strong class="text_red">${period}</strong>天
                            <b class="sub">即日起生效</b>
                        </p>
                    </#if>
                    <p class="js-stemLevel" style="display: none;">
                        选择难度：<strong class="text_red js-levelName"></strong>
                    </p>
                    <p>
                        订单编号：${orderId!0}
                    </p>
                </div>
            </div>
        </div>

        <div class="foot_btn_box">
            <div class="info">
                <i class="icon"></i>
                温馨提示：<span class="js-infoText">学生只能在电脑上使用本产品</span>
            </div>
            <div data-href="/parent/wxpay/pay-order.vpage?oid=${orderId!0}" class="btn_mark btn_mark_block js-payNow"
                 style="background-color: #ff9b2f;">
                <span  style="color: #FFFFFF; font-weight: normal;">立即支付</span>
            </div>
        </div>
    </div>
<script type="text/javascript">
var stemLevel = "${productName!''}";
var NameArray = stemLevel.split(" ");
var stemLevelName = NameArray[1] +" "+NameArray[2];
var productType = "${productType!''}";
function pageLog(){
    require(['logger'], function(logger) {
        logger.log({
            module: 'ucenter',
            op: 'ucenter_click_confirm_pay_ok'
        })
    })
}
</script>
</@ucenter.page>