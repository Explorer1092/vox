<#-- @ftlvariable name="ProductConfig" type="com.voxlearning.utopia.core.runtime.ProductConfig" -->
<#import "../layout/project.module.ftl" as temp />
<@temp.page title="进入论坛">
<div style=" width: 960px; margin: 0 auto; padding: 150px 0;">
    <div class="text_center">
        <a class="btn_mark btn_mark_primary" href="partner.vpage?url=${(ProductConfig.getBbsSiteBaseUrl())!}/open.php?mod=register&urlInput=${(urlInput!)?url}">进入论坛</a>
    </div>
</div>
</@temp.page>
