<#import "module.ftl" as temp />
<@temp.page title='history'>
<!--//start--><#--已发货-->
<div class="w-content">
    <div class="t-prizesCenter-box">
        <ul class="pc-tab">
            <li><a href="/reward/order/myorder.vpage"><span class="h-arrow"></span>待发货</a></li>
            <li class="active"><a href="/reward/order/history.vpage"><span class="h-arrow"></span>已发货</a></li>
        </ul>
        <div class="my_order_box">
            <#if currentUser.userType==1 && studentLogisticInfo?? && studentLogisticInfo?has_content>
                <div class="pc-title express-info" style="background-color:#ffedd1;">
                <#if studentLogisticInfo.logisticNo?has_content && studentLogisticInfo.logisticNo != "" >
                    <span style="color:#666;font-weight:bold;">学生奖品</span>｜ 发货时间：<span class="orange-color">${studentLogisticInfo.deliverDate}</span>   ｜ 物流公司：<span class="orange-color">${studentLogisticInfo.companyName}</span>  ｜ 快递单号：<span class="orange-color">${studentLogisticInfo.logisticNo}</span>
                <#else>
                    <span style="color:#666;font-weight:bold;">学生奖品：</span><span class="orange-color">暂无信息</span>
                </#if>
                </div>
            </#if>
            <#if orderDataList?has_content>
                <#list  orderDataList as map>
                    <#if map.historyOrder>
                        <div class="pc-title express-info"><span style="color:#666;font-weight:bold;">我的奖品</span> ｜ 历史兑换记录</div>
                    <#elseif currentUser.userType==1>
                        <div class="pc-title express-info"><span style="color:#666;font-weight:bold;">我的奖品</span> ｜ 发货时间：<span class="orange-color">${map.deliverDate}</span>   ｜ 物流公司：<span class="orange-color">${map.companyName}</span>  ｜ 快递单号：<span class="orange-color">${map.logisticNo}</span></div>
                    <#elseif currentUser.userType==3>
                        <div class="pc-title express-info"><span style="color:#666;font-weight:bold;">我的奖品</span> ｜ 亲爱的同学： 你兑换的奖品已于<span class="orange-color">${map.deliverDate}</span>寄出，收货老师为<span class="orange-color">${map.subject}老师${map.teacherName}老师</span>，请提醒你的班级老师及时领取吧！</div>
                    </#if>
                    <#list map.orders as his>
                        <div class="my_order_inner_box">
                            <div class="p-column">
                                <p class="float_left my_order_price_box"><span>价格：</span><strong class="J_red orderPrice">${his.price!''}</strong><@ftlmacro.garyBeansText/></p>
                                <p class="float_left pc-number"><span class="float_left">
                            数量：</span><span class="my_order_number float_left">${his.quantity!''}</span>
                                </p>
                            </div>
                            <div class="my_order_product_box clearfix">
                                <#if his.image??>
                                    <#if his.image?index_of("oss-image.17zuoye.com")!=-1>
                                        <img src="${his.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" class="float_left" />
                                    <#else>
                                        <img src="<@app.avatar href="${his.image!''}?x-oss-process=image/resize,w_200/quality,Q_90" />" class="float_left" />
                                    </#if>
                                <#else>
                                    <img src="<@app.avatar href="${his.image!''}" />" class="float_left" />
                                </#if>
                                <dl class="float_left">
                                    <dt>
                                    <p>${his.productName!''}<#if currentUser.userType == 1 && his.price lte 0><i class="tag"></i></#if>
                                    <#if his.rewardInfo != ''>
                                        (${his.rewardInfo!''})
                                    </#if></p>
                                    <p class="pc-time"><#if his.price != 0>兑换<#else>中奖</#if>时间：${(his.createTime)!''}</p>
                                    </dt>
                                </dl>
                            </div>
                        </div>
                    </#list>
                </#list>
                <#else>
                    <div class="no_order_box" style=" border: 1px solid #f5e6d6; margin-top: -1px;">
                        <div class="no_order_bg"></div>
                        <p class="btn_box font_twenty">您还没有已发货奖品呢</p>
                        <p class="btn_box J_light_gray" style="padding-top:6px;">继续加油吧！</p>
                    </div>
            </#if>
        </div>
        <div class="message_page_list"></div>
    </div>
</div>
<!--end//-->

<script type="text/javascript">
    console.log(${json_encode(orderDataList)});
    $(function(){
        <#if (historyOrdersPage.getTotalPages() gt 1)!false>
        $(".message_page_list").page({
            total: ${historyOrdersPage.getTotalPages()!1},
            current: ${(pageNum)!1},
            jumpCallBack: function(index){
                var param = {
                    pageNum : index
                };

                location.href = "/reward/order/history.vpage?" + $.param(param);
            }
        });
        </#if>
        YQ.voxLogs({module: "m_2ekTvaNe", op: "o_suIJl7g7", s1: "${(currentUser.userType)!0}"});
    });
</script>
</@temp.page>