<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_48,h_48/auto-orient,1">
<#assign evaluateList = ["1分/差","2分/一般","3分/好","4分/很好","5分/标杆"]>
<@layout.page title="陪访详情" pageJs="" footerIndex=4 navBar="hidden">
<@sugar.capsule css=['intoSchool']/>
<div class="visit_content">
    <#if visitSchoolRecord?has_content>
    <div class="title">基础信息</div>
    <div class="list">
        <ul>
            <li>
                <p class="txt">时间</p>
                <p class="item">${visitSchoolRecord.workTime!""}</p>
            </li>
            <li>
                <p class="txt">陪访对象</p>
                <p class="item"> ${visitSchoolRecord.interviewerName!""}</p>
            </li>
            <li>
                <p class="txt">学校名称</p>
                <p class="item">${visitSchoolRecord.schoolName!""}</p>
            </li>
            <li>
                <p class="txt">签到地址</p>
                <p class="item">${visitSchoolRecord.address!""}</p>
            </li>
            <li>
                <p class="txt">陪访目的</p>
                <p class="item">
                    <#if visitSchoolRecord.workTitle?has_content>
                        <#if visitSchoolRecord.workTitle == '1'>
                            专员技能辅导
                        <#elseif visitSchoolRecord.workTitle == '2'>
                            重点学校跟进
                        <#elseif visitSchoolRecord.workTitle == '3'>
                            市场情况了解
                        </#if>
                    </#if>
                </p>
            </li>
        </ul>
    </div>
    </#if>
</div>
    <#if visitSchoolRecord?has_content>
    <div class="visit_content">
        <div class="title">
            陪访反馈
        </div>
    </div>
        <div style="padding:.5rem 1.025rem 0 1.025rem;font-size:.65rem;color:#7f86ad;">基础评价：</div>
        <div class="visit_box">
            <div class="v_side evaluate">
                <div class="list">
                    <p class="subtitle">进校准备充分度</p>
                    <ul class="preparationScore">
                        <li <#if (1 lte (visitSchoolRecord.preparationScore!0))>class="active"</#if> ><span></span></li>
                        <li <#if (2 lte (visitSchoolRecord.preparationScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (3 lte (visitSchoolRecord.preparationScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (4 lte (visitSchoolRecord.preparationScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (5 lte (visitSchoolRecord.preparationScore!0))>class="active"</#if>><span></span></li>
                    </ul>
                    <p class="per">
                        <#if visitSchoolRecord.preparationScore?? && visitSchoolRecord.preparationScore gte 1 && visitSchoolRecord.preparationScore lte evaluateList?size>
                            ${evaluateList[(visitSchoolRecord.preparationScore!0)-1]}
                        </#if>
                    </p>
                </div>
                <div class="list">
                    <p class="subtitle">产品/话术熟练度</p>
                    <ul class="productProficiencyScore">
                        <li <#if (1 lte (visitSchoolRecord.productProficiencyScore!0))>class="active"</#if> ><span></span></li>
                        <li <#if (2 lte (visitSchoolRecord.productProficiencyScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (3 lte (visitSchoolRecord.productProficiencyScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (4 lte (visitSchoolRecord.productProficiencyScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (5 lte (visitSchoolRecord.productProficiencyScore!0))>class="active"</#if>><span></span></li>
                    </ul>
                    <p class="per">
                        <#if visitSchoolRecord.productProficiencyScore?? && visitSchoolRecord.productProficiencyScore gte 1 && visitSchoolRecord.productProficiencyScore lte evaluateList?size>
                            ${evaluateList[(visitSchoolRecord.productProficiencyScore!0)-1]}
                        </#if>
                    </p>
                </div>
                <div class="list">
                    <p class="subtitle">结果符合预期度</p>
                    <ul class="resultMeetExpectedResultScore">
                        <li <#if (1 lte (visitSchoolRecord.resultMeetExpectedResultScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (2 lte (visitSchoolRecord.resultMeetExpectedResultScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (3 lte (visitSchoolRecord.resultMeetExpectedResultScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (4 lte (visitSchoolRecord.resultMeetExpectedResultScore!0))>class="active"</#if>><span></span></li>
                        <li <#if (5 lte (visitSchoolRecord.resultMeetExpectedResultScore!0))>class="active"</#if>><span></span></li>
                    </ul>
                    <p class="per">
                        <#if visitSchoolRecord.resultMeetExpectedResultScore?? && visitSchoolRecord.resultMeetExpectedResultScore gte 1 && visitSchoolRecord.resultMeetExpectedResultScore lte evaluateList?size>
                            ${evaluateList[(visitSchoolRecord.resultMeetExpectedResultScore!0)-1]}
                        </#if>
                    </p>
                </div>
            </div>
        </div>
        <#if visitSchoolRecord.partnerSuggest??>
            <div class="visit_content">
                <div class="title">发现的问题&改进建议：</div>
                <div class="visit_box" style="background: #fff;color:#636880;height:1rem;line-height:1rem;-ms-word-break: break-all;word-break: break-all;font-size: .65rem;;">${visitSchoolRecord.partnerSuggest!""}</div>
            </div>
        </#if>
    </#if>
</@layout.page>