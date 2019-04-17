<div class="nav tab-head c-flex c-flex-5">
    <a href="/view/mobile/crm/home/index.vpage" class="click_item tab-item n1 <#if footerIndex == 1>the</#if>">
        <span></span>
        首页
    </a>
    <a href="javascript:void(0);" id="resFooterBtn" class="click_item tab-item n2 <#if footerIndex == 2>the</#if>">
        <span></span>
        资源
    </a>
    <div class="tab-item add click_item <#if !requestContext.getCurrentUser().isProductOperator()>js-more<#else>js-operator</#if>">
        <span></span>
        ADD
        <div class="layer01"></div>
        <div class="layer02"></div>
        <div class="layer03"></div>
    </div>
    <#if !requestContext.getCurrentUser().isProductOperator()>
        <a href="javascript:void(0);" class=" tab-item n3 js-message <#if footerIndex == 3>the</#if>">
            <span style="position: relative;"><#if requestContext.getUnreadNotifyCount()?? && requestContext.getUnreadNotifyCount()!=0><div style="border-radius:100%;color: white;width:1rem;height:1rem;display: inline-block;font-size:.6rem;line-height:1rem;background:red;position:absolute;top: -.3rem;right: -.6rem;">${requestContext.getUnreadNotifyCount()!0}</div></#if></span>
            消息
        </a>
    <#else>
        <div href="javascript:void(0);" class=" tab-item n3 js-noTeam">
            <span></span>
            消息
        </div>
    </#if>
    <a href="/mobile/my/index.vpage" class="click_item tab-item n4 <#if footerIndex == 4>the</#if>">
        <span></span>
        我的
    </a>
</div>
<script>
    //清除cookie
    $("#resFooterBtn").on("click",function(){
        var AT = new agentTool();
        AT.cleanAllCookie();
        location.href = "/view/mobile/crm/resource/index.vpage";
    });
</script>