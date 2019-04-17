<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="我创建的学校" navBar="hidden" pageJs= "createdSchoolList">
    <@sugar.capsule css=['audit'] />
<div class="crmList-box resources-box">
    <div class="c-main">
        <div class="tab-main" style="clear:both;background: #f1f2f5;">
            <div class="c-list">
                <#if schools??>

                    <#list schools as item>
                            <div class="adjustmentExamine-box" style="margin-top: .5rem;padding:.1rem 1.025rem 0;">
                                <div class="adjust-head" style="border-bottom: 0;">
                                    <div class="">${item.schoolName!}(${item.schoolId!})</div>
                                    <p style="display: inline-block">
                                        <#if item.level??><#if item.level == 1>小学<#elseif item.level == 2>初中<#elseif item.level == 4>高中<#elseif item.level == 5>学前</#if></#if>/${item.regionName!''}
                                        <div class="right">${item.createTime?string("MM-dd")}</div>
                                    </p>
                                </div>
                            </div>
                    </#list>
                </#if>
            </div>
        </div>
        <div style="font-size:.7rem;text-align:center;width:100%;position: fixed;bottom:3%;z-index: -1">说明：仅供查询近30天内创建的学校</div>
    </div>
</div>
</@layout.page>
