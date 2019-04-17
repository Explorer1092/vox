<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="省内排行榜">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">省内排行榜</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <#if provinces??>
       <#list provinces?values as value>

            <#if value??>
                <li>
                    <a href="province_top_city.vpage?province=${value['provCode']!0}&back=list" class="link link-ico">
                        <div class="side-fl">${value['provName']!''}</div>
                    </a>
                </li>
            </#if>
       </#list>
    </#if>
</ul>
</@layout.page>