<#import "../module.ftl" as temp />
<@temp.page title="收到的礼物">
<div class="section">
    <div class="giftMain">
        <ul class="unstyled_vox">
            <li>
                <div class="giftTabGift">
                    <div class="content" style="border: 0px;">
                        <ul id="receive_gifts_list_box" class="listbox app_init_auto_get_html" dataurl="/student/gift/receive/list.vpage?currentPage=0">
                            <!-- 收到的礼物列表 -->
                            礼物列表加载中...
                        </ul>
                        <div class="clear"></div>
                    </div>
                </div>
            </li>
        </ul>
    </div>
    <div class="clear"></div>
</div>
</@temp.page>