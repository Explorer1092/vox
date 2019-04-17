<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="新注册老师" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
    <div style="background: #f1f2f5">
        <#if notifyList?? && notifyList?size gt 0>
            <#list notifyList as notify>
                <div class="new_registered_box">
                    <ul>
                        <input <#if notify.readFlag?? && !notify.readFlag>name="js-ipt"</#if> type="hidden" value="${notify.id!0}" />
                        <li class="teacherCard" data-info="${notify.notifyUrl}">
                            <#if notify.notifyContent??>
                                <#list notify.notifyContent?split("@") as content>
                                    <#if content_index = 0>
                                        <div class="message_name" style=" <#if notify.readFlag?? && notify.readFlag>color:#9199bb</#if>">${content!""}
                                    <#elseif content_index = 1>
                                            <i class="icon-${content!""}"></i>
                                            <span style="float: right;">${notify.createDatetime!""}</span>
                                        </div>
                                    <#elseif content_index = 2>
                                        <div class="message_school" style=" <#if notify.readFlag?? && notify.readFlag>color:#9199bb</#if>">${content!""}</div>
                                    </#if>
                                </#list>
                            </#if>
                        </li>
                    </ul>
                </div>
            </#list>
        </#if>
    </div>
    <script>
        $('.subTitle').each(function(){
            var pattern = $(this).html().trim();
            $(this).html(pattern.replace(/\n/g, '<br />'));
        });
        $(document).ready(function(){
            try{
                var setTopBar = {
                    show:true,
                    rightText:'更多数据>',
                    rightTextColor:"ff7d5a",
                    needCallBack:true
                } ;
                var topBarCallBack = function () {
                    <#if requestContext.getCurrentUser().isBusinessDeveloper()>
                        openSecond("/view/mobile/crm/teacher/new_auth_teacher.vpage?userId=${requestContext.getCurrentUser().getUserId()!0}");
                    <#else>
                        openSecond("/view/mobile/crm/search/search_bussiness.vpage");
                    </#if>
                };
                setTopBarFn(setTopBar,topBarCallBack);
            }catch(e){
                alert(e)
            }
            var notifyIdsStr = "";
            $("input[name='js-ipt']").each(function(){
                notifyIdsStr += $(this).val() + ",";
            });
            $.post("readNoticeList.vpage",{notifyIds:notifyIdsStr},function(){

            });
        });
        $('.teacherCard').on("click",function(){
            var _info = $(this).data('info').split('?')[1];
            openSecond('/view/mobile/crm/teacher/teacher_card_new.vpage?' + _info);
        })
    </script>
</@layout.page>