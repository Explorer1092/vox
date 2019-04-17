<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>
            <p style="color: #00a0e9">SKU管理</p>/操作日志
            <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        </legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <input type="hidden" id="skuId" name="skuId" value="${skuId!''}"/>
        <input type="hidden" id="type" name="type" value="${type!''}"/>
    </form>

    <#include "../coursecomment.ftl">
</div>
<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });
    });
</script>
</@layout_default.page>