<#import "module.ftl" as temp />
<@temp.page title='history'>
<div class="my_order_box">
    <p class="my_order_title clearfix">
        <span class="float_left">这些都是你在一起作业努力的成果</span>
        <span class="float_right" style="margin-right:20px;">累计消耗${temp.integarlType!''}：<strong class="J_red">${totalPrice!0}</strong>  个</span>
        <span class="float_right">累计兑换奖品：<strong class="J_red">${historyOrdersPage.totalElements!0}</strong> 个</span>
    </p>
    <#if historyOrdersPage?has_content && historyOrdersPage.content?has_content>
        <div class="my_order_inner_box my_ordered_prize">
            <div class="my_ordered_box">
                    <#list  historyOrdersPage.content as his>
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
                                <dt>${his.productName!''}</dt>
                            </dl>
                            <p class="float_left">价格：${his.price!''}<@ftlmacro.garyBeansText/></p>
                            <p class="float_left my_order_number_box">数量：${his.quantity!''}</span></p>
                            <p class="float_left">${his.productName}</p>
                        </div>
                    </#list>
                <div class="message_page_list"></div>
            </div>
        </div>
    <#else>
        <div class="no_order_box">
            <div class="no_order_bg"></div>
            <p class="btn_box font_twenty">你还没有兑换奖品呢</p>
            <p class="btn_box J_light_gray" style="padding-top:6px;">继续加油吧！</p>
        </div>
    </#if>
</div>

<#if historyOrdersPage?has_content && historyOrdersPage.cotent?has_content>
    <script type="text/javascript">
        $(function(){
            //todo
            $(".message_page_list").page({
                total           : ${(historyOrdersPage.getTotalPages())!'0'},
                current         : ${(historyOrdersPage.getNumber()+1)!''},
                autoBackToTop   : false,
                jumpCallBack    : function(index){
                    location.href = '/reward/order/history.vpage?pageNum='+(index+1);
                }
            });
        });

    </script>
</#if>

</@temp.page>