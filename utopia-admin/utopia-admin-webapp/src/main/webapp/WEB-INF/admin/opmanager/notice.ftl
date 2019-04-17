<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Notice' page_num=4>
<div class="span9">
    <div class="hero-unit">
        <h3>Surprise!</h3>
        <h3>您访问的节点已经移动到 <a href="/opmanager/opindex.vpage">运营管理平台</a> 下</h3> <h4><span id="num">5</span> 秒后跳转</h4>
        <pre>Dear ${requestContext.getCurrentAdminUser().adminUserName}：
    先前位于 [网站管理] 功能下的 [积分流通活动管理]、[短信管理平台]、[UGC活动管理]，以及 [每日要闻]
    已经移至 [运营管理] 功能下，请前往运营管理平台完成你想要的操作。
    PS：
    不管此次是通过什么方式看到了此则通知(应该访问不到)，下次请不要使用这个链接了。
    如有其他问题(最好没有)，请联系<a href="mailto:yuechen.wang@17zuoye.com">王悦晨</a>
                                                                        By 2016-04-28
</pre>
    </div>
</div>

<script>
    $(function(){
        function jump(count){
            window.setTimeout(function(){
                count--;
                if(count > 0) {
                    $('#num').html(count);
                    jump(count);
                } else {
                    location.href="${requestContext.webAppContextPath}/opmanager/opindex.vpage";
                }
            }, 1000);
        }
        jump(5);
    })
</script>
</@layout_default.page>
