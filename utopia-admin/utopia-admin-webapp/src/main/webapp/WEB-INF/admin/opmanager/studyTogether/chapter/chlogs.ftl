<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>
            章节管理操作日志
            <a type="button" id="btn_cancel" href="chindex.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        </legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <input type="hidden" id="chapterId" name="chapterId" value="${chapterId!''}"/>
    </form>

    <#include "../coursecomment.ftl">
</div>
<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${total_page!1},
            current: ${page!1},
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