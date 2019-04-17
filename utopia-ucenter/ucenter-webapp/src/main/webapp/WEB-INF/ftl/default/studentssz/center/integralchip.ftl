<li class="title">
    <span class="order-cycles">获得时间</span>
    <span class="order-price">学豆</span>
    <span class="order-time">来源</span>
</li>
<#if pagination.content?has_content>
    <#list pagination.content as page>
        <li>
            <span class="order-cycles">${page.dateYmdString!''}</span>
            <span class="order-price">${page.integral!''}</span>
            <span class="order-time" title="${page.comment!''}">${page.comment!''}</span>
        </li>
    </#list>
<#else>
    <li>
        <div style="padding: 20px 0; text-align: center;">暂无学豆历史记录</div>
    </li>
</#if>

<script type="text/javascript">
    $(function(){
        $(".message_page_list").page({
            total: ${pagination.getTotalPages()},
            current: ${currentPage!},
            jumpCallBack: createPageList
        });
    });
</script>