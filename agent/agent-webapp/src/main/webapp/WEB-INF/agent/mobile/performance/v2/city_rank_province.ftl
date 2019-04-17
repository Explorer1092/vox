<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="省内排行榜">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="/mobile/performance/user_region_province.vpage" class="headerBack">&lt;&nbsp;返回</a>

            <div class="headerText">省内排行榜</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <#if regionPerformance?has_content>
        <#list regionPerformance as performance>
            <li>
                <div class="box">
                    <div class="side-fl" style="width:10%;">${performance_index + 1}</div>
                    <div class="side-fl" style="width:22%;">${performance.name!'未知'}</div>
                    <div class="side-fr side-orange">${performance.addStuAuthNum!}</div>
                </div>
            </li>
        </#list>
    <#else>
        <li>暂无数据</li>
    </#if>
</ul>
</@layout.page>