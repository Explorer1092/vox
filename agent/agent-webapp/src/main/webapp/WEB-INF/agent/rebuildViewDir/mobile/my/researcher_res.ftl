<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="教研员资源" pageJs="researcher_res" footerIndex=4>
<@sugar.capsule css=['researchers']/>
<div class="tea-staffRes-main">
    <div class="vir-title"><i class="titleIco ico05"></i>教研员列表</div>
    <#if researchersList?? && researchersList?size gt 0>
        <div class="tea-list">
            <table cellpadding="0" cellspacing="0">
            <#list researchersList as rl>
                <tr>
                    <td>
                        <div class="name">${rl.name!}
                            <span class="icon-box">
                        <#if rl.subject == "ENGLISH">
                            <i class="icon-ying"></i>
                        <#elseif rl.subject == "MATH">
                            <i class="icon-shu"></i>
                        <#elseif rl.subject == "CHINESE">
                            <i class="icon-yu"></i>
                        </#if>
                        <#if rl.level == 1>
                            <i class="icon-sheng"></i>
                        <#elseif rl.level == 2>
                            <i class="icon-shichang"></i>
                        <#elseif rl.level == 3>
                            <i class="icon-qu"></i>
                        </#if>
                            </span>
                        </div>
                    </td>
                    <td><a class="orange_font" onclick="openSecond('/view/mobile/crm/workrecord/researcher_record.vpage?researchersId=${rl.id!0}')">拜访记录</a></td>
                    <td><a class="orange_font" onclick="openSecond('/view/mobile/crm/researcher/edit_researcher.vpage?id=${rl.id!0}')">编辑</a></td>
                </tr>
            </#list>
            </table>
        </div>
    <#else>
        <div class="nonInfo">
            暂无教研员信息
        </div>
    </#if>
</div>
<script>
    var AT = new agentTool();
    $(document).ready(function () {
        reloadCallBack();
    })
</script>
</@layout.page>