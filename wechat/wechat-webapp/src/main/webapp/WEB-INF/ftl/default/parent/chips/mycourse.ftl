<#import "../layout.ftl" as layout>
<@layout.page title="已购课程" pageJs="chipsIndex">
    <@sugar.capsule css=['chips'] />

<style>
    html, body, input{
        font: inherit;
    }
</style>
<div class="purchaseWrap">
    <#if orders?? && orders?size gt 0 >
    <div class="purchaseMain">
        <ul>
                <#list orders as order>
                    <li>
                        <div class="purchaseImg">
                            <img src="/public/images/parent/chips/buyicon.png" alt="">
                        </div>
                        <div class="purchaseMessage">
                            <p>${order.productName ! ''}</p>
                            <span class="stage">第${order.rank ! '1'}期</span>
                        </div>
                        <div class="success">购买成功</div>
                    </li>
                </#list>

        </ul>
    </div>
    <#else>
                <!-- 没有购买课程 -->
        <div class="noPurchase">
            <p class="noClass">没有课程...</p>
            <a href="/chips/center/robin.vpage" class="goPurchase"><span>去购买课程</span></a>
        </div>
    </#if>


</div>

<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            // 我的课程页_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'mycourse_load'
            })
        })
    }
</script>

</@layout.page>

