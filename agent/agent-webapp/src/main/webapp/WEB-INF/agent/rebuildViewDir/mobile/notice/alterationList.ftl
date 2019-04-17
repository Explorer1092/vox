<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="任务提醒" pageJs="" navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
<div class="flow" style="background-color:#f1f2f5;">
    <#if notifyList?? && notifyList?size gt 0>
        <#list notifyList as an>
            <div class="examineNotice-box">
                <input <#if an.readFlag?? && !an.readFlag>name="js-ipt"</#if> type="hidden" value="${an.id!0}" />
                <div class="examineTitle">
                    <div class="time">${an.createDatetime?string("MM-dd")}</div>
                ${an.notifyTitle!''}
                </div>
                <div class="examineSide" style="clear:both;overflow:hidden">
                    <#if an.tagList?? && an.tagList?size gt 0>
                        <#list an.tagList as tag>
                            <#if tag == 'REJECT'>
                                <div class="rejectInfo"><span></span></div>
                            </#if>
                        </#list>
                    </#if>
                    <div class="subTitle">
                    ${an.notifyContent!''}
                    </div>
                    <a onclick="openSecond('${an.notifyUrl!}')" style="float:right;">点击查看详情</a>
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