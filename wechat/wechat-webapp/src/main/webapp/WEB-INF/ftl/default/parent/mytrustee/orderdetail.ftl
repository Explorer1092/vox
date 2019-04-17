<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='订单详情' pageJs="mytrusteeorderdetail">
<@sugar.capsule css=['mytrustee','jbox'] />
<div class="mc-orderDetails mc-wrap mc-margin15">
    <div class="od-header mc-institutionDetail js-trusteeDetailItem" data-bid="${order.branchId!""}">
        <div class="top">
            <div class="left"><img src="${order.branchImg!""}"></div>
            <div class="right">
                <h3 class="textOverflow">${order.branchName!""}<span class="arrow-rg"></span></h3>
                <div class="lf-text"><p>${order.goodsName!""}</p><p class="textOverflow">${order.goodsDesc!""}</p></div>
                <div class="rg-text"><p class="mc-txtOrange">￥${order.price!""}</p><#if order.price lt order.orignalPrice><del class="js-oPrice">￥${order.orignalPrice}</del></#if><p>×${order.count!"0"}</p></div>
            </div>
        </div>
        <div class="footer"><span class="fl">学生姓名：${order.studentName!""}</span><span class="fr">总计：${order.amount!""}元</span></div>
    </div>
    <div class="od-main">
        <div class="innerBox">
            <div class="hd"><div class="name">${order.goodsName!""}</div><div class="number">订单编号：${order.orderId!""}</div></div>
            <div class="mn">
                <div class="number">学习券：<span class="mc-txtOrange">${order.voucher!""}</span></div>
                <#if order.voucherActive>
                    <div class="state mc-txtGreen">已激活</div>
                <#else>
                    <div class="state">未激活</div>
                    <div class="time">兑换截止时间：
                        <#if .now lte (order.voucherEndTime?datetime('yyyy-MM-dd'))!.now>
                            ${order.voucherEndTime!""}
                        <#else>
                            已过期
                        </#if>
                    </div>
                </#if>
            </div>
        </div>
        <input type="hidden" value="${order.status!""}" id="ostatus"/>
        <input type="hidden" value="${goodsType!""}" id="gtype"/>
        <div class="btnBox">
            <a href="javascript:void(0)" class="mc-btn-greenWhite js-refundBtn" data-oid="${order.orderId!""}">申请退款</a>
            <a href="javascript:void(0)" class="mc-btn-orange-s js-continuePayBtn" data-sid="${order.studentId!""}" data-gid="${order.goodsId!""}">续费</a>
        </div>
    </div>
</div>
<#if firstOpen!false>
<div class="mod-flayer js-tipsDiv">
    <div class="flayerInfo">
        <div class="tips js-tipsBtn">
            学习券：<span class="mc-txtOrange font30">${order.voucher!""}</span>
            <p class="time">兑换截止时间：
                <#if .now lte (order.voucherEndTime?datetime('yyyy-MM-dd'))!.now>
                ${order.voucherEndTime!""}
                <#else>
                    已过期
                </#if>
            </p>
        </div>
        <div class="arrow-point"></div>
    </div>
</div>
</#if>
</@trusteeMain.page>