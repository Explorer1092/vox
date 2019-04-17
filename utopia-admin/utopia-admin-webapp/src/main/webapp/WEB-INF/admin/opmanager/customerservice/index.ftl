<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='Opertaion Manager' page_num=9>
    <div class="span9">
        <ul class="inline">
            <li>
                <legend><a href="?contentType=all">全部</a></legend>
            </li>
            <li>
                <legend><a href="?contentType=permanent">长期</a></legend>
            </li>
            <li>
                <legend><a href="?contentType=temporary">临时</a></legend>
            </li>
        </ul>
        <#if CSSIndexPageBlockContent??>${CSSIndexPageBlockContent!}
        <#else>
            <div class="span9">
                <div class="hero-unit">
                    <h1>每日要闻</h1>
                </div>
            </div>
        </#if>
    </div>
</@layout_default.page>