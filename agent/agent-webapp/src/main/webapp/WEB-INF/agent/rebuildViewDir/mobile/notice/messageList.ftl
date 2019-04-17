<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="系统消息" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
<div class="flow" style="background-color:#f1f2f5;">
    <#if notifyList?? && notifyList?size gt 0>
        <#list notifyList as an>
            <div class="examineNotice-box" style="position: relative">
                <input <#if an.readFlag?? && !an.readFlag>name="js-ipt"</#if> type="hidden" value="${an.id!0}" />
                <#if an.readFlag?? && !an.readFlag>
                    <div class="icon" style="position: absolute;top:0.05rem;right:0.2rem;width:.5rem;height:.5rem;background-color: red;border-radius: 100%"></div>
                </#if>
                <div class="examineTitle">
                    <div class="time">${an.createDatetime?string("MM-dd HH:mm")}</div>
                    ${an.notifyTitle!''}
                </div>
                <div class="examineSide">
                    <div class="subTitle">
                        ${an.notifyContent!''}
                    </div>
                    <#if an.rejectReason??>
                        <div style="color: red;">
                            ${an.rejectReason!''}
                        </div>
                    </#if>
                    <#if an.notifyUrl??>
                        <#if an.notifyTitle == '陪同反馈'>
                            <div style="text-align:right">
                                <a onclick="openSecond('/view/mobile/crm/workrecord/accompany_detail.vpage?workRecordId=${an.workRecordId!""}')">点击查看详情</a>
                            </div>
                        <#elseif an.notifyTitle == '陪访反馈'>
                            <div style="text-align:right">
                                <a onclick="openSecond('/view/mobile/crm/visit/visit_detail.vpage?recordId=${an.workRecordId!""}')">点击查看详情</a>
                            </div>
                        <#elseif an.notifyTitle == '上层资源申请'>
                            <div style="text-align:right">
                                <a onclick="openSecond('/view/mobile/crm/message/resource_detail.vpage?${an.notifyUrl!""}')">点击查看详情</a>
                            </div>
                        <#else>
                            <#if an.notifyUrl?has_content>
                                <div style="text-align:right">
                                    <a onclick="openSecond('${an.notifyUrl!""}',1)">点击查看详情</a>
                                </div>
                            </#if>
                        </#if>
                    </#if>
                </div>
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
        var notifyIdsStr = "";
        $("input[name='js-ipt']").each(function(){
            notifyIdsStr += $(this).val() + ",";
        });
        $.post("readNoticeList.vpage",{notifyIds:notifyIdsStr},function(){

        });
    });
</script>
</@layout.page>
