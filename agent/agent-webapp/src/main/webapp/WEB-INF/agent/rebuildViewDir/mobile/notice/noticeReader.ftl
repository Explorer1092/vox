<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="消息查看" pageJs="">
<@sugar.capsule css=['new_home','notice']/>
<style>
    html {
        -webkit-user-select:none;-ms-user-select:none
    }
</style>
<div class="flow">
    <div class="main-content" style="text-align: center;">
        <h2 style="margin-bottom: 10px;line-height: 1.4;font-weight: 400;font-size: 24px;">${title!}</h2>
        <div>${content!''}</div>
    </div>
</div>
<script>
    $(document).ready(function(){
        $(document).on("click",".js-readTipBtn",function () {
            var noticeId = $(this).data("tid");
            $.post("readNotice.vpage",{noticeId:noticeId},function (res) {
                location.reload();
            });
        })
    });
</script>
</@layout.page>