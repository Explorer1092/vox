<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="活动专区"
pageJs=["activitylist"]
pageJsFile={"activitylist" : "public/script/project/activitylist"}
pageCssFile={"activitylist" : ["public/skin/project/activitylist/skin"]}
>

<!-- ko if : (items().length > 0 && success)-->
<div class="module-box" data-bind="foreach: items">
    <a href="javascript:;" data-bind="click: $root.go_link.bind($data)">
        <div class="module-content">
            <!-- ko if : img-->
            <img src="" data-bind="attr:{ src: '<@app.avatar href="/"/>' + img}">
            <!-- /ko -->
            <div class="m-column">
                <span class="view-btn">查看详情</span>
                <div class="m-left">
                    <p class="title" data-bind="text: description"></p>
                    <p class="info" data-bind="text: content"></p>
                </div>
                <div class="clearfix"></div>
            </div>
        </div>
    </a>
</div>
<!-- /ko -->

<!-- ko ifnot : success && items().length > 0 -->
<div style="text-align: center; color: #888; line-height: 20; font-size: 16px;">暂时没有活动</div>
<!-- /ko -->
</@layout.page>