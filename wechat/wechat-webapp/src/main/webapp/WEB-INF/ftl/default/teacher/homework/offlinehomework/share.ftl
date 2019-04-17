<#import "../../layout_view.ftl" as activityMain>
<@activityMain.page title="作业单分享" pageJs="offlineHomeworkShare">
    <@sugar.capsule css=["offlinehomework"] />
<div class="jc-tips bg-none offlineHomeworkShare">
    <p class="blue"><i class="icon"></i></p>
    <p class="blue">作业单已发送给家长！</p>
</div>
<div class="jc-loadTips offlineHomeworkShare">
    <p class="txt">以上班级的家校群已经开通<br>下载一起作业老师APP，查看家校群</p>
    <div class="btnBox">
        <a href="javascript:void(0)" class="w-btn" data-bind="click:$root.downloadApp">立即下载</a>
    </div>
</div>
<div class="jc-footer offlineHomeworkShare">
    <div class="innerBox">
        <div class="inner">
            <a href="javascript:void(0)" class="w-btn w-btn-lightBlue" data-bind="click:$root.fowardReport">完成</a>
        </div>
    </div>
</div>
</@activityMain.page>