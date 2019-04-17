<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="申请管理" page_num=21>
<div class="span9">
    <fieldset><legend>创建申请</legend></fieldset>
    <div>
        <a href="${requestContext.webAppContextPath}/audit/apppush/apppushapply.vpage" class="btn btn-info">
            <i class="icon-envelope icon-white"></i> AppPush消息
        </a>

        <a href="${requestContext.webAppContextPath}/audit/wechat/wechatapply.vpage" class="btn btn-success">
            <i class="icon-envelope icon-white"></i> 微信模板消息
        </a>

    </div>
</div>

<script type="text/javascript">
    $(function(){

    });
</script>
</@layout_default.page>
