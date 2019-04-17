<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="学校业绩" pageJs="">
    <@sugar.capsule css=['home']/>

    <#macro noneDataBlock>
    <tr>
        <td colspan="3" style="text-align: center;">
            暂无
        </td>
    </tr>
    </#macro>

<div class="primary-box">
    <div class="tab-main">
        <div class="pr-side">
            <table class="sideTable">
                <tbody>
                <tr>
                    <td style="text-align: left;padding-left: 1rem;">学校名称</td>
                    <td>本月</td>
                    <td>昨日</td>
                </tr>
                    <#if contentItem?has_content>
                        <#list contentItem as ci>
                        <tr>
                            <td style="text-align: left;padding-left: 1rem;"><a href="/mobile/resource/school/card.vpage?schoolId=${ci.schoolId!'0'}">${ci.schoolName!"--"}<#if ci.visited!false> <i class="icon_visit"></i></a></#if></td>
                            <td style="width: 2rem;vertical-align: middle;">${ci.thisMonthCount!0}</td>
                            <td style="width: 2rem;vertical-align: middle;">${ci.yesterdayCount!0}</td>
                        </tr>
                        </#list>
                    <#else>
                        <@noneDataBlock/>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>

</@layout.page>