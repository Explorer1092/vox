<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用开发商管理" page_num=10>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9">
    <form class="form-horizontal" action="saveappresg.vpage" method="post" id="app_resg_edit_form">
        <input type="hidden" id="appId" name="appId" value="${vendorApp.id}"/>
        <div class="control-group">
            <label class="col-sm-2 control-label">受限访问资源组</label>
            <div class="controls">
                <#if resgList??>
                    <#list resgList as resgItem>
                    <input type="checkbox" name="resgList" value="${resgItem.id!}"
                        <#if appRestList??>
                            <#list appRestList as appRes>
                                <#if resgItem.id == appRes.resgId>checked </#if>
                            </#list>
                        </#if>
                            />
                    ${resgItem.cname!}<br/>
                    </#list>
                </#if>
            </div>
        </div>

        <div class="control-group">
            <div class="controls">
                <button type="button" id="btn_cancel" name="btn_cancel" class="btn">取消</button> &nbsp;&nbsp;
                <button type="button" id="btn_save"  name="btn_save" class="btn btn-primary">保存</button>
            </div>
        </div>
    </form>
</div>
<script type="text/javascript">

    $(function() {
        $('#btn_cancel').on('click', function () {
            $("#app_resg_edit_form").attr("action", "appindex.vpage");
            $("#app_resg_edit_form").submit();
        });

        $('#btn_save').on('click',function(){
            var appId = $('#appId').val();
            var resgList = '';
            $("input[name='resgList']").each(function(){
                if ("checked" == $(this).attr("checked")) {
                    resgList = resgList + $(this).val() + ",";
                }
            });

            $.post('saveappresg.vpage',{
                appId: appId,
                resgList: resgList
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });

</script>
</@layout_default.page>