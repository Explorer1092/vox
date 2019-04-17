<#import "../module.ftl" as temp />
<@temp.page title="收到的礼物">
<div class="section">
    <ul class="unstyled_vox">
        <li>
            <div class="giftTabGift">
                <div class="content" style="border: 0px;">
                    <ul id="receive_gifts_list_box" class="listbox app_init_auto_get_html" dataurl="/teacher/gift/receive/list.vpage?currentPage=0">
                        <!-- 收到的礼物列表 -->
                        <div class="text_center" style="padding: 50px 0;"><img class="throbber" src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>
                    </ul>
                    <div class="clear"></div>
                </div>
            </div>
        </li>
    </ul>
    <div class="clear"></div>
</div>
</@temp.page>