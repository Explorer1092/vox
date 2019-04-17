<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#if performanceType ??>
    <#if performanceType == 1>
    <#assign header = '完成1套作业英活'>
    </#if>
    <#if performanceType == 2>
        <#assign header = '完成2套作业英活'>
    </#if>
    <#if performanceType == 3>
        <#assign header = '完成1套作业数活'>
    </#if>
    <#if performanceType == 4>
        <#assign header = '完成2套作业数活'>
    </#if>
    <#if performanceType == 5>
        <#assign header = '完成1套试卷扫描'>
    </#if>
</#if>
<@layout.page title="${header!}" pageJs="common" footerIndex=2>
    <@sugar.capsule css=['new_home']/>
<div class="resources-box">
    <div class="rankingList-box">
        <div class="rl-table" id="rankingTable" order="-1">

            <table colspan="0" cellpadding="0" cellspacing="0" style="text-align: center;">
                <tr>
                    <td>老师姓名</td>
                    <td><#if performanceType ??>
                        <#if performanceType == 1>
                            完成1套作业英活学生数量
                        </#if>
                        <#if performanceType == 2>
                            完成2套作业英活学生数量
                        </#if>
                        <#if performanceType == 3>
                            完成1套作业数活学生数量
                        </#if>
                        <#if performanceType == 4>
                            完成2套作业数活学生数量
                        </#if>
                        <#if performanceType == 5>
                            完成1套试卷扫描学生数量
                        </#if>
                    </#if></td>
                </tr>
                <#if performanceInfo?has_content >
                    <#list performanceInfo as info>
                        <tr class="teacher" data-id="${info.teacherId!''}">
                            <td>${info.teacherName!'-'}</td>
                            <td>${info.performanceNum!'-'}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </div>
</div>
<script>
    $(document).on('click','.teacher',function(){
        var teacherId = $(this).data('id');
        location.href = "/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=" + teacherId;
    });
</script>
</@layout.page>