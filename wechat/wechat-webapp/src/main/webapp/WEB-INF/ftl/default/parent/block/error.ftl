<#import "../layout.ftl" as homework>
<@homework.page title="提示" pageJs="">
    <@sugar.capsule css=['base'] />
<div class="main body_background" style="height: 30%;">
    <h1 class="logo"></h1>
</div>
<div style="text-align: center; margin-top: 50px;">
    ${errmsg!'访问出问题了~~'}
</div>
<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            logger.log({
                module: 'errorPage',
                op: 'error_page_pv_index',
                s1 : '${errmsg!}',
                reffer: '${reffer!}'
            })
        })
    }
</script>
</@homework.page>
