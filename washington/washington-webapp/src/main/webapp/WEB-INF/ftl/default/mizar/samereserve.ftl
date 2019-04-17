<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="body-backBlue"
title=(shop.name)!"机构详情"
pageJs=["mizar"]
pageJsFile={"mizar" : "public/script/mobile/mizar/main"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
>


<div class="cameToSchoolmate-box">
    <#if sameSchoolReserveList?? && sameSchoolReserveList?size gt 0>
        <div class="cts-main">
            <div class="cts-head">来过的同学（${sameSchoolReserveList?size}）</div>
            <div class="cts-list sameSchoolReserveList">
                <ul>
                    <#list sameSchoolReserveList as same>
                        <li <#if same_index gt 4>style="display: none;" </#if>>
                            <div class="cts-title">${(same.schoolName)!''}</div>
                            <div class="cts-info">${(same.clazzName)!''}<span class="cts-name">${(same.studentName)!''}</span></div>
                            <div class="cts-date">${(same.createDatetime)!''}</div>
                        </li>
                    </#list>
                </ul>
            </div>
            <#if sameSchoolReserveList?size gt 5>
                <div class="ah-btn">
                    <a id="showSameMoreStudentsBtn" href="javascript:void(0);" class="refresh-btn">加载更多</a>
                </div>
            </#if>
        </div>
    </#if>

    <#if otherSchoolReserveList?? && otherSchoolReserveList?size gt 0>
        <div class="cts-main">
            <div class="cts-head">其他小伙伴</div>
            <div class="cts-list otherSchoolReserveList">
                <ul>
                    <#list otherSchoolReserveList as other>
                        <li <#if other_index gt 4>style="display: none;"</#if>>
                            <div class="cts-title">${(other.schoolName)!''}</div>
                            <div class="cts-info">${(other.clazzName)!''}<span class="cts-name">${(other.studentName)!''}</span></div>
                            <div class="cts-date">${(other.createDatetime)!''}</div>
                        </li>
                    </#list>
                </ul>
            </div>
        </div>
        <#if otherSchoolReserveList?size gt 5>
            <div class="ah-btn">
                <a id="showMoreStudentsBtn" href="javascript:void(0);" class="refresh-btn">加载更多</a>
            </div>
        </#if>
    </#if>
</div>
</@layout.page>