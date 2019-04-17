<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Notice' page_num=9>
<div class="span9">
    <div class="hero-unit">
        <h3>Surprise!</h3>
        <h3>Dear ${requestContext.getCurrentAdminUser().adminUserName}：</h3>
        <h3>您访问的节点已经移动到 <a href="/audit/auditindex.vpage">审核平台</a> 下</h3> <h4><span id="num">5</span> 秒后跳转</h4>
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
                    location.href = "${requestContext.webAppContextPath}/audit/apply/apply.vpage";
                }
            }, 1000);
        }
        jump(5);
    })
</script>
</@layout_default.page>
