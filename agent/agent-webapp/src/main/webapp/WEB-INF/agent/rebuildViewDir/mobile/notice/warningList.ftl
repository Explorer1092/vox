<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="预警信息" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
<style>
    body{
        background-color: rgb(241, 242, 245);
    }
</style>
<div class="flow" style="background-color:#f1f2f5;">
    <#if notifyList?? && notifyList?size gt 0>
        <#list notifyList as an>
            <div class="examineNotice-box" <#if an.readFlag?? && an.readFlag>style="color:#9199BB"</#if>>
                <input <#if an.readFlag?? && !an.readFlag>name="js-ipt"</#if> type="hidden" value="${an.id!0}" />
                <div class="examineTitle">
                    <div class="time">${an.createDatetime?string("MM-dd HH:mm")}</div>
                ${an.notifyTitle!''}
                </div>
                <div class="examineSide">
                    <div class="subTitle">
                    ${an.notifyContent!''}
                    </div>
                    <#if an.notifyUrl??>
                        <div style="text-align:right">
                            <a href="${an.notifyUrl!""}">点击查看详情</a>
                        </div>
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
