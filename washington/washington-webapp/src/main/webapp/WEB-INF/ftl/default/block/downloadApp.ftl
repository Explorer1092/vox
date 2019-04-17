<#-- TODO 这个只是针对扫码下载使用 -->
<#include "../parentmobile/constants.ftl">
${buildLoadStaticFileTag("", "js", "public/plugin/downloadApp" + ProductDevelopment.isDevEnv()?string('', '.min') )}
<script>
    window.downloadParentApp("${source!""}", "${cid!""}");
</script>
