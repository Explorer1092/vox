<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='我的托管班' pageJs="mytrusteeorderlist">
<@sugar.capsule css=['mytrustee','jbox'] />
<#if orders?? && orders?size gt 0>
    <div class="mc-wrap mc-myInstitution">
        <div class="top-title">托管班订单</div>
           <div class="list">
            <ul>
                <#list orders as order>
                    <li class="li-list" data-oid="${order.orderId!""}">
                        <div class="mcList-box">
                            <div class="title">${order.branchName!""}
                                <#if order.status == "NOT_PAY">
                                    <span class="mc-txtRed fl js-statu" data-type="${order.status!""}">未付款</span>
                                <#elseif order.status == "COMPLETE_PAY">
                                    <span class="fl js-statu" data-type="${order.status!""}">已购买</span>
                                <#elseif order.status == "ARRIVED">
                                    <span class="fl js-statu" data-type="${order.status!""}">已激活</span>
                                <#elseif order.status == "APPLY_REFUND">
                                    <span class="fl js-statu" data-type="${order.status!""}">申请退款</span>
                                <#elseif order.status == "REFUNDING">
                                    <span class="fl js-statu" data-type="${order.status!""}">退款中</span>
                                <#elseif order.status == "REFUNDED">
                                    <span class="fl js-statu" data-type="${order.status!""}">已退款</span>
                                <#elseif order.status == "CANCEL_REFUND">
                                    <span class="fl js-statu" data-type="${order.status!""}">取消退款</span>
                                </#if>
                            </div>
                            <div class="inner mc-institutionDetail">
                                <div class="top">
                                    <div class="left"><img src="${order.branchImg+"@1e_1c_0o_0l_108h_148w_90q"!""}"></div>
                                    <div class="right">
                                        <div class="info">
                                            <div class="lf-text">
                                                <h3 class="textOverflow">${order.branchName!""}</h3>
                                                <p>${order.goodsName!""}</p>
                                                <p class="textOverflow">${order.goodsDesc!""}</p>
                                            </div>
                                            <div class="rg-text"><p>￥${order.price!"0"}</p><p>×${order.count!"0"}</p></div>
                                        </div>
                                        <#if !order.voucherActive>
                                            <#if order.status == "NOT_PAY">
                                                <div class="intro"><a href="javascript:void(0)" class="mc-btn-orange-s btn goToPayBtn" data-oid="${order.orderId!""}">付款</a></div>
                                            </#if>
                                        </#if>
                                    </div>
                                </div>
                                <div class="footer"><span class="fl">学生姓名：${order.studentName!""}</span><span class="fr">总计：${order.amount!""}元</span></div>
                            </div>
                        </div>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
<#else>
    <div class="managedClass mc-wrap">
        <div class="mc-trusteeEmpty emptys">
            <div class="bg"></div>
            <p class="txt-green">现在还没有托管班订单哦<br>快去下单吧！</p>
            <p><a href="/parent/trustee/index.vpage" class="btn-look">去看看</a></p>
        </div>
    </div>
</#if>
</@trusteeMain.page>