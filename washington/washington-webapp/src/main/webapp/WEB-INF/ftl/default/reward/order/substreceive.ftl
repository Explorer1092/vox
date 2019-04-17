<#import "module.ftl" as temp />
<@temp.page title='substreceive'>
<!--//start--><#--代收-->
<style>
.J_green { color: #24d557 !important; }
</style>
<div class="w-content">
    <div class="t-prizesCenter-box">
        <div class="my_order_box">
            <#if orderDataList?has_content>
                <#list  orderDataList as map>
                    <div class="pc-title express-info"><span style="color:#666;font-weight:bold;">发货时间：<span
                            class="orange-color">${map.deliverDate}</span> ｜ 物流公司：<span
                            class="orange-color">${map.companyName}</span> ｜ 快递单号：<span
                            class="orange-color">${map.logisticNo}</span></div>

                    <div class="my_order_inner_box">
                        <div class="p-column" style="width: 350px;">
                            <p class="float_left my_order_price_box"><span>状态：</span><strong class="<#if map.status ?? && map.status == 'PREPARE'>J_green<#else>J_red</#if> orderPrice">${(map.statusName)!''}</strong></p>
                            <p class="float_left pc-number" style="padding:0 40px"><span class="float_left">
                                ${(map.rewardInfo)!''}
                               </span>
                            </p>
                        </div>
                        <div class="my_order_product_box clearfix">
                            <#if map.image??>
                                <#if map.image?index_of("oss-image.17zuoye.com")!=-1>
                                    <img src="${map.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" class="float_left" />
                                <#else>
                                    <img src="${map.image!''}" class="float_left" />
                                </#if>
                            <#else>
                                <img src="<@app.avatar href="${map.image!''}" />" class="float_left" />
                            </#if>
                            <dl class="float_left">
                                <dt>
                                <p>${map.productName!''}</p>
                                <p class="pc-time">创建时间：${(map.createTime)!''}</p>
                                </dt>
                            </dl>
                        </div>
                    </div>

                </#list>
            <#else>
                <div class="no_order_box" style=" border: 1px solid #f5e6d6; margin-top: -1px;">
                    <div class="no_order_bg"></div>
                    <p class="btn_box font_twenty">您还没有代收奖品呢</p>
                </div>
            </#if>
        </div>
        <div class="message_page_list"></div>
    </div>
</div>
<!--end//-->
</@temp.page>