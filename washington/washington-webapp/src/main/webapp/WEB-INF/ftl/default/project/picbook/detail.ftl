<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="个人页"
bodyClass="bg-grey"
pageJs=['init']
pageJsFile={"init" : "public/script/project/member"}
pageCssFile={"css" : ["public/skin/project/xbt/css/detail"]}
>
<div data-bind="template: {name: templateContent(), data : database()}">
<#--template-->
</div>

<script type="text/html" id="loading">
    <div style="text-align: center; padding: 50% 0 0;">正在加载...</div>
</script>

<script type="text/html" id="member-detail">
    <div class="study-wrap">
        <div class="study-face">
            <div class="face"><img data-bind="attr: {src: avatar_url}"></div>
            <div class="name" data-bind="text: name"></div>
            <div class="text" data-bind="if: day_count">已坚持阅读<span data-bind="text: day_count"></span>天</div>
        </div>
    </div>
    <div class="study-wrap">
        <div class="title" data-bind="text: title || '推荐阅读'"></div>
        <ul class="huiben-list clearfix" data-bind="foreach: pic_book_list">
            <li>
                <a href="javascript:void(0);" class="log_book huiben-book" data-bind="attr: {'href': $parent.getUrl(picture_book_id), 'picbook_id': picture_book_id}">
                    <div class="book">
                        <img data-bind="attr: {src: picture_book_img}">
                        <div class="title" data-bind="text: picture_book_name"></div>
                    </div>
                </a>
            </li>
        </ul>
    </div>

</script>
<script>
    var env = <@ftlmacro.getCurrentProductDevelopment /> || 'test';
</script>
</@layout.page>