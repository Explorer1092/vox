<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='增值equator' page_num=24>
<!--/span-->
<div class="span9">
    <div class="hero-unit">
        <h1 id="indexContent">增值equator后台欢迎你！</h1>
    </div>
</div>
<!--/row-->
<script>
    if (window.location.origin.indexOf("admin.staging.17zuoye.net") >= 0 || window.location.origin.indexOf("admin.17zuoye.net") >= 0) {
        $("#indexContent").html("<span style = 'color:red;'>线上</span>增值equator后台欢迎你！");
    }
</script>
</@layout_default.page>