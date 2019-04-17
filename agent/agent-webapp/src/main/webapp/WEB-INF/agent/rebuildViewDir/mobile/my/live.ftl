<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="大会直播"  footerIndex=4>

<script>
$(document).on("ready",function(){
    location.replace("${liveUrl}");
});
</script>
</@layout.page>
