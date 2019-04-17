<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="物料购买" pageJs="workflow_process" navBar= "hidden">
    <@sugar.capsule css=['audit']/>
<div class="crmList-box resources-box">
    <div class="tab-main" style="clear:both">
    <#--待审核-->
        <div>
            <#if applyData?? && applyData.apply??>
                <#assign item = applyData>
                <div class="adjustmentExamine-box">
                    <div class="adjust-content">
                        <p class="title"><span style="color:#ff7d5a">金额：</span>${item.apply.orderAmount?string("#.##")!''}元</p>
                        <p class="stage"><span style="color:#ff7d5a">支付方式：</span><#if item.apply.cityCostMonth?has_content>${item.apply.cityCostMonth!''}月</#if><#if item.apply.paymentMode?? && item.apply.paymentMode == 1>物料费用<#elseif item.apply.paymentMode?? && item.apply.paymentMode == 2>城市支持费用<#elseif item.apply.paymentMode?? && item.apply.paymentMode == 3>自付</#if></p>
                        <div class="commodity-list">
                            <p><span style="color:#ff7d5a">商品明细：</span></p>
                            <#if item.apply.orderProductList?? && item.apply.orderProductList?size gt 0>
                                <ul>
                                    <#list item.apply.orderProductList as order>
                                        <#if order.price?has_content && order.productName?has_content && order.productQuantity?has_content>
                                            <li>
                                                <div class="right">￥${(order.price?string("#.##")?number!0)*(order.productQuantity!0)}</div>
                                                <div class="left">${order.productName!''}×${order.productQuantity!''}</div>
                                            </li>
                                        </#if>
                                    </#list>
                                </ul>
                            </#if>
                        </div>
                        <p class="reason"><span style="color:#ff7d5a">收货信息：</span>${item.apply.accountName!''}，${item.apply.province!""}${item.apply.city!""}${item.apply.county!""}${item.apply.address!''} （${item.apply.mobile!''}）</p>
                    </div>
                </div>
                <div class="adjustmentExamine-box" style="margin-top: .5rem">
                    <p style="font-size:.6rem;color:#898c91;line-height:1rem;height:1rem">审核进度</p>
                    <ul class="schoolClueContent">
                        <#if item.processResultList?? && item.processResultList?size gt 0>
                            <#list item.processResultList as list>
                                <li>
                                    <div>${list.accountName!""}</div>
                                    <div <#if list.result??>style="<#if list.result == "同意">color:#99cc66<#elseif list.result == "驳回" || list.result == "撤销">color:#ff7d5a</#if>"</#if>>${list.result!""}</div>
                                    <div><#if list.processDate??>${list.processDate?string("MM-dd HH:mm")}</#if></div>
                                </li>
                                <#if list.result?? && list.result == "驳回">
                                    <li style="color:#ff7d5a">${list.processNotes!""}</li>
                                </#if>
                            </#list>
                        </#if>
                        <li>
                            <div>${item.apply.accountName!''}</div>
                            <div>发起申请</div>
                            <div><#if item.apply.createDatetime?has_content>${item.apply.createDatetime?string("MM-dd HH:mm")}</#if></div>
                        </li>
                    </ul>
                </div>
                <div class="c-opts gap-line c-flex c-flex-2" style="position:absolute;z-index: 999;bottom:0;background: #fff">
                    <span class="js-submit" data-result="2">驳回</span>
                    <span class="js-submit" data-result="1">同意</span>
                </div>
            </#if>
            <div class="schoolParticular-pop submitBox" id="repatePane" style="display: none;">
                <div class="inner">
                    <h1></h1>
                    <p class="info">是否确认？</p>
                    <div class="btn">
                        <a href="javascript:void(0);" class="white_btn">否</a>
                        <a href="javascript:void(0);" class="submitBtn">是</a>
                    </div>
                </div>
            </div>
            <div class="apply_pop submitBox" style="display: none">
                <div class="inner">
                    <div class="apply_info">填写驳回原因：</div>
                    <div class="apply_text">
                        <textarea></textarea>
                    </div>
                    <div class="apply_btn">
                        <a href="javascript:void(0);" class="btn white_btn">取消</a>
                        <a href="javascript:void(0);" class="btn orange_btn submitBtn">确认</a>
                    </div>
                </div>
            </div>
        </div>
    <#--已通过-->
    </div>
</div>
<script>
    var workflowId =<#if applyData?? && applyData.apply??> ${applyData.apply.workflowId!0}<#else>0</#if>;
    var applyType =<#if applyType??> '${applyType!""}'<#else>""</#if>;
</script>
</@layout.page>
