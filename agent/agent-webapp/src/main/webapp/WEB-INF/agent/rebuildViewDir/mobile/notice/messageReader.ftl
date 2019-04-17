<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="消息" pageJs="noticeIndex" footerIndex=3 navBar="show">
    <@sugar.capsule css=['new_home','notice']/>
<div class="crmList-box">
    <div class="c-head fixed-head">
        <span>消息<#if requestContext.getUnreadNotifyCount()?has_content && requestContext.getUnreadNotifyCount() !=0>(${requestContext.getUnreadNotifyCount()!0})</#if></span>
    </div>
</div>

<div class="new_registered_message" style="background:#f1f2f5">
    <ul>
        <li class="js-list" data-info="alteration_clazz_new">
            <div class="icon-notice"></div>
            <div class="message_content">
                <div class="title">
                    任务提醒
                    <div class="time"> <#if alteration_clazz_new??>${alteration_clazz_new.time!""}</#if></div>
                </div>
                <div class="info">
                    <#if alteration_clazz_new??><#if alteration_clazz_new.unreadCount?? && alteration_clazz_new.unreadCount gt 0>
                        <div class="icon">${alteration_clazz_new.unreadCount!0}</div>
                    </#if>
                    </#if>
                    <p><#if alteration_clazz_new??>${alteration_clazz_new.title!""}</#if></p>
                </div>
            </div>
        </li>
        <#if new_teacher??>
            <li class="js-list" data-info="new_teacher">
                <div class="icon-teacher"></div>
                <div class="message_content">
                    <div class="title">
                        新注册老师
                        <div class="time"> ${new_teacher.time!""}</div>
                    </div>
                    <div class="info">
                        <#if new_teacher.unreadCount?? && new_teacher.unreadCount gt 0>
                            <div class="icon">${new_teacher.unreadCount!0}</div>
                        </#if>
                        <p>${new_teacher.title!""}</p>
                    </div>
                </div>
            </li>
        </#if>

        <li class="js-list" data-info="system" style="position:relative">
            <#if system??>
                <#if system.unreadCount?? && system.unreadCount gt 0>
                    <div class="icon" style="position: absolute;top:.3rem;right:0.95rem;width:.5rem;height:.5rem;background-color: red;border-radius: 100%"></div>
                </#if>
            </#if>
            <div class="icon-message"></div>
            <div class="message_content">
                <div class="title">
                    系统消息
                    <div class="time"> <#if system??>${system.time!""}</#if></div>
                </div>
                <div class="info">
                    <p><#if system??>${system.title!""}</#if></p>
                </div>
            </div>
        </li>
        <#if warning??>
        <li class="js-list" data-info="warning">
            <div class="icon-message"></div>
            <div class="message_content">
                <div class="title">
                    预警信息
                    <div class="time"> <#if warning??>${warning.time!""}</#if></div>
                </div>
                <div class="info">
                    <#if warning??><#if warning.unreadCount?? && warning.unreadCount gt 0>
                        <div class="icon">${warning.unreadCount!0}</div>
                    </#if>
                    </#if>
                    <p><#if warning??>${warning.title!""}</#if></p>
                </div>
            </div>
        </li>
        </#if>
    </ul>
</div>
<script>
    $('.icon,.red').each(function(){
       if($(this).html() >= 100){
           $(this).html("99+");
       }else if($(this).html() == 0){
        $(this).removeClass('red');
           $(this).html("");
       }
    });
    $(document).ready(function(){
        $('.js-noticeBox').show();
    });

    $(document).on('click','.js-todoCount',function(){
        openSecond("/mobile/audit/todo_list.vpage");
    });
</script>
</@layout.page>
