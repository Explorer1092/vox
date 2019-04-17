<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="小组"
bodyClass="bg-grey"
pageJs=['init']
pageJsFile={"init" : "public/script/project/herogroup"}
pageCssFile={"css" : ["public/skin/project/xbt/css/skin"]}
>
<div data-bind="template: {name: templateContent(), data : database()}">
<#--template-->
</div>

<script type="text/html" id="loading">
    <div style="text-align: center; padding: 50% 0 0;">正在加载...</div>
</script>

<script type="text/html" id="hero_group_list">
    <div class="h-listBox">
        <ul class="h-list">
            <li>
                <div class="avatar"><img src="" data-bind="attr: { src: owner_info.avatar_url}"></div>
                <div class="name" data-bind="text: owner_info.owner_name"></div>
                <!-- ko if: owner_info.is_used -->
                <span class="state">已使用</span>
                <!-- /ko -->
                <!-- ko if: !(owner_info.is_used) -->
                <span class="state state-red">未使用</span>
                <!-- /ko -->
            </li>
        </ul>
        <ul class="h-list" data-bind="foreach: member_list">
            <li>
                <div class="avatar"><img src="" data-bind="attr: { src: avatar_url}"></div>
                <div class="name" data-bind="text: member_name"></div>
                <!-- ko if: is_used -->
                <span class="state">已使用</span>
                <!-- /ko -->
                <!-- ko if: !is_used -->
                <span class="state state-red">未使用</span>
                <!-- /ko -->
            </li>
        </ul>
    </div>
</script>
<div class="h-footer">
    <div class="h-footerFixed">
        <ul>
            <li class="log_tab" data-bind="click:pointRead">进入点读机</li>
            <li class="log_tab" data-bind="click:heroActivity">英雄团活动</li>
        </ul>
    </div>
</div>
</@layout.page>