<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='预约成功' pageJs="booksuccess">
    <@sugar.capsule css=['trusteetwo'] />
<div class="active-wrap active-bgpink">
    <div class="active05-box">
        <div class="ab05-top"></div>
        <div class="ab05-inner inner01">
            <h2></h2>
            <p>• 机构名：${shop.shopName!""}</p>
            <p>• 地址：${shop.address!""}</p>
            <p>• 电话：${shop.phone!""}</p>
        </div>
        <div class="ab05-inner inner02">
            <h2></h2>
            <#if shop.privileges?has_content>
                <#list shop.privileges as item>
                    <p><span>${item_index+1}</span>${item!""}</p>
                </#list>
            </#if>
        </div>
        <a href="javascript:void(0)" class="link js-activityInfoBtn">查看活动详情</a>
        <div class="ab05-footer">
            <a href="javascript:void(0);" class="active-bottom-know order_icon js-payNowBtn">立即购买</a><!--按下 添加类active-->
        </div>
    </div>
</div>
<script>
    var shopId = "${shop.shopId!""}";
    ga('trusteeTracker.send', 'pageview');
</script>
</@trusteeMain.page>