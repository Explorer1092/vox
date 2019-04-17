<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='机构相册' pageJs="branchalbum">
<@sugar.capsule css=['mytrustee'] />
<div class="mc-picture mc-wrap mc-margin15">
    <ul>
        <#if imgs?? && imgs?size gt 0>
            <#list imgs as img>
            <#if img.src?has_content>
                <li>
                    <img src="${img.src}@1e_1c_0o_0l_270h_270w_80q" class="js-imgItem">
                    <p class="name">${img.tag!""}</p>
                </li>
            </#if>
            </#list>
        </#if>
    </ul>
</div>
<script>
<#if config_signature?has_content>
    var wechatConfig = {};
    wechatConfig.signature = "${config_signature}";
    wechatConfig.appid = "${appid}";
    wechatConfig.timestamp = "${config_timestamp}";
    wechatConfig.noncestr = "${config_nonceStr}";
</#if>
</script>
</@trusteeMain.page>