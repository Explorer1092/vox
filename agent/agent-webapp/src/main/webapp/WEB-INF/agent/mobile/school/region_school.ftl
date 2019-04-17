<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="按区域查看学校">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText">按区域查看学校</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <#if regionType?? && regionType == "COUNTY">
        <li>
            <div class="box link-ico">
                <div class="side-fl side-time">学校</div>
                <div class="side-fr side-time side-width">认证学生</div>
            </div>
        </li>
    </#if>
    <#if regionSchools?has_content>
        <#list regionSchools as school>
            <li>
                <#if school.type == "SCHOOL">
                <a href="/mobile/school/school_info.vpage?schoolId=${school.key!''}" class="link link-ico">
                <#else>
                <a href="/mobile/school/region_school.vpage?regionCode=${school.key!''}&DATATYPE=${school.type!''}" class="link link-ico">
                </#if>
                <#assign color = "black">
                <#if school.type == "SCHOOL" && school.note != "SUCCESS">
                    <#assign color = "red">
                </#if>
                <div class="side-fl" style="width: 60%; line-height: 1.5rem; color: ${color!}">${school.name!'未知'}<#if (school.type) == "SCHOOL_REGION">（指定学校）</#if></div>
                <#if school.type == "SCHOOL">
                    <div class="side-fr side-orange side-width">${school.stuAuthNum!0}</div>
                </#if>
            </a>
            </li>
        </#list>
    </#if>
</ul>
</@layout.page>