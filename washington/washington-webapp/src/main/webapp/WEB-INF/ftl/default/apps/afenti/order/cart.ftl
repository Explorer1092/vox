<#import "module.ftl" as com>
<@com.page step=1 title="阿分题购物车 - 一起作业">
<#-- 仅用于续费自动下单跳转 -->

<div style="margin: 50px; text-align: center">
    正在进入续费页面 ... ... 请稍候 ...
</div>
<form action='${targetPageUri}' method='post' id='frm'>
    <input type='hidden' name='productId' value='${productId}' />
</form>
<script type="text/javascript">
    $(function(){
        window.document.getElementById('frm').submit();
    });
</script>
</@com.page>
