<#import "../layout.ftl" as faq>
<@faq.page title="自动答疑" pageJs="">
<style type="text/css">
    .homework_box .list li{margin: 0 30px 6px;}
    .homework_box .list li p.state{height: auto;  position: relative;  text-indent: 0;  top: 11px;  width: 100%;}
</style>

<div class="main">
    <div class="content">
        <div class="homework_box">
            <ul class="list">
                <#if catalogs?? && catalogs?size gt 0>
                    <#list catalogs as catalog>
                        <li>
                            <a href="${(catalog.url)!}">
                                <p class="state ">
                                <span class="back text_blue">
                                    ${catalog.title!0} &#10142;
                                </span>
                                </p>
                            </a>
                            <div class="clear"></div>
                        </li>
                    </#list>
                <#else>
                    <li>暂无数据</li>
                </#if>
            </ul>
        </div>
    </div>
</div>
</@faq.page>