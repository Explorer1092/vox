<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="小组成员"
bodyClass="bg-grey"
pageJs=['init']
pageJsFile={"init" : "public/script/project/member"}
pageCssFile={"css" : ["public/skin/project/xbt/css/skin"]}
>
<div data-bind="template: {name: templateContent(), data : database()}">
<#--template-->
</div>
<div class="h-footer">
    <div class="h-footerFixed">
        <ul>
            <li class="log_tab" data-bind="click:pointRead">进入绘本</li>
            <li class="log_tab" data-bind="click:heroActivity">绘本活动</li>
        </ul>
    </div>
</div>

<script type="text/html" id="loading">
    <div style="text-align: center; padding: 50% 0 0;">正在加载...</div>
</script>
<script type="text/html" id="member_list">
    <div class="h-listBox">
        <ul class="h-list">
            <li style="padding:0;margin-bottom: -.05rem">
                <a class="log_avatar anchor" style="float:left;" data-bind="attr: { href: '/project/picbook/detail.vpage?member_id=' + owner_info.owner_id + '&sid=' + sid}">
                    <div class="avatar"><img src="" data-bind="attr: { src: owner_info.avatar_url}"></div>
                </a>
                <div style="overflow: hidden;border-bottom:.05rem solid #d8d8d8;">
                    <a class="log_avatar"  style="float:left;max-width: 46%;" data-bind="attr: { href: '/project/picbook/detail.vpage?member_id=' + owner_info.owner_id + '&sid=' + sid}">
                        <div class="m_name" data-bind="text: owner_info.owner_name"></div>
                    </a>
                    <a style="float:right;max-width: 54%;"  data-bind="css:{'log_picbook': owner_info.is_used}, attr: {'href': owner_info.is_used ? getUrl(owner_info.picbook_id) : 'javascript:void(0);', 'picbook_id': owner_info.is_used ? owner_info.picbook_id : ''}">
                        <!-- ko if: owner_info.is_used -->
                        <span class="m_state" data-bind="text: owner_info.picbook_name"></span>
                        <!-- /ko -->
                        <!-- ko if: !(owner_info.is_used) -->
                        <span class="m_state state-red">未阅读</span>
                        <!-- /ko -->
                    </a>
                </div>
            </li>
        </ul>
        <ul class="h-list" data-bind="foreach: member_list">
            <li style="padding:0;margin-bottom: -.05rem">
                <a class="log_avatar anchor" style="float:left;" data-bind="attr: {'href': '/project/picbook/detail.vpage?member_id=' + member_id + '&sid=' + $parent.sid}">
                    <div class="avatar"><img src="" data-bind="attr: { src: avatar_url}"></div>
                </a>
                <div style="overflow: hidden;border-bottom:.05rem solid #d8d8d8;">
                    <a class="log_avatar" style="float:left;max-width: 46%;" data-bind="attr: {'href': '/project/picbook/detail.vpage?member_id=' + member_id + '&sid=' + $parent.sid}">
                        <div class="m_name" data-bind="text: member_name"></div>
                    </a>
                    <a style="float:right;max-width: 54%;"  data-bind="css:{'log_picbook': is_used}, attr: {'href': is_used ? $parent.getUrl(picbook_id) : 'javascript:void(0);', picbook_id: is_used ? picbook_id : ''}">
                        <!-- ko if: is_used -->
                        <span class="m_state" data-bind="text: picbook_name"></span>
                        <!-- /ko -->
                        <!-- ko if: !is_used -->
                        <span class="m_state state-red">未阅读</span>
                        <!-- /ko -->
                    </a>
                </div>
            </li>
        </ul>
    </div>
</script>
<script>
    var env = <@ftlmacro.getCurrentProductDevelopment /> || 'test';
</script>
</@layout.page>