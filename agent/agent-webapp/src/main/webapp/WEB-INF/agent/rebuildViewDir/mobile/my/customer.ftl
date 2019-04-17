<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="消息" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
    <#assign shortIconTail = "?x-oss-process=image/resize,w_100,h_100/auto-orient,1">
<style>
    .red{border-radius:100%;color: white;width:1rem;height:1rem;display: inline-block;text-align: center;font-size:.60rem;vertical-align: middle;line-height:1rem;background:red}
</style>
<div class="s-list js-noticeBox" style="">
    <div class="item js-list">
        快速处理（转校、建班、绑定解绑手机）
        <div class="s-right">
            <i class="seeArrow"></i>
        </div>
    </div>
    <div class="item js-todoCount">
        人工客服
        <div class="s-right">
            <i class="seeArrow"></i>
        </div>
    </div>
</div>
<script>
$(document).on("click",".js-list",function(){
openSecond("/mobile/task/task_list.vpage");
});
$(document).on("click",".js-todoCount",function(){
    var data = {
        title:'${title!''}',
        desc:'${desc!''}',
        note:'id：'+'${note!''}',
        picture:"${(photoUrl!'') + (shortIconTail!'')}" ,
        actionText:'发送信息'
    };
    <#if type?has_content>
        <#if type == 1>
            data.url = "http://admin.17zuoye.net/crm/school/schoollist.vpage?schoolId=${note!''}";
        <#elseif type == 2>
            data.url = "http://admin.17zuoye.net/crm/teacher/teacherlist.vpage?teacherId=${note!''}";
        </#if>
    </#if>
    do_external('jumpToCustomService',JSON.stringify(data));
});
</script>
</@layout.page>