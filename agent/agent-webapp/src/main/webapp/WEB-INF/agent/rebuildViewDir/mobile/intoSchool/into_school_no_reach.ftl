<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="预警信息" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home','notice',"school"]/>
<style>
    body {
        background-color: rgb(241, 242, 245);
    }
</style>
    <div class="schoolRecord-box" style="padding:.5rem 0;">
        <div class="srd-module">
            <div class="mHead">${date!""}小学专员进校未达标专员共计<#if views?? && views?has_content>${views?size}<#else>0</#if>人:</div>
            <div class="mTable">
                <table cellpadding="0" cellspacing="0">
                    <thead>
                    <tr>
                        <td>分区</td>
                        <td>专员</td>
                        <td>进校（次）</td>
                        <td>校均拜访老师</td>
                    </tr>
                    </thead>
                    <tbody>
                        <#if views?? && views?has_content>
                            <#list views as v>
                            <tr>
                                <td>${v.groupName!""}</td>
                                <td>${v.bdName!""}</td>
                                <td>${v.intoSchoolCount!0}</td>
                                <td>${v.visitTeacherAvg!0.0}</td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</@layout.page>
