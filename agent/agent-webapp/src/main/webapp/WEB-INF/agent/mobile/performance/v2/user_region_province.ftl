<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="省内排行榜">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="/mobile/performance/index.vpage" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText">省内排行榜</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <#if userRegionProvinces?has_content>
        <#list userRegionProvinces?keys as key>
            <li>
                <a href="/mobile/performance/city_rank_province.vpage?provinceCode=${key!""}" class="link link-ico">
                    <div class="side-fl">${userRegionProvinces[key]!''}</div>
                </a>
            </li>
        </#list>
    </#if>
</ul>
</@layout.page>