<#import "module.ftl" as temp>
<@temp.page title="资源" level="${curSubject!}资源">
    <!-- 共享资源 -->
    <iframe class="vox17zuoyeIframe" style="width: 100%;height: 1050px;border: 0;" frameborder="no" src="/ucenter/partner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}/open.php?mod=resource%26subject%3d${curSubject!}"></iframe>
</@temp.page>
