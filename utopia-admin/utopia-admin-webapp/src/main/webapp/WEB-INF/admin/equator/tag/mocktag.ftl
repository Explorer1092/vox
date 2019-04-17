<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="打标签" page_num=24>
<style>

    div.float-tag {
        display: inline-block;
        height: 30px;
        line-height: 30px;
        width: 250px;
        text-align: center;
        font-size: 12px;
        margin: 10px 10px 10px 0;
        border-radius: 5px;
        padding: 3px;
    }

    /* 未被选中的颜色 */
    .tag-unselected {
        background: #EEEED1;
    }

    /* 被选中的颜色 */
    .tag-selected {
        background: #34EE7A;
    }

</style>

<div id="main_container" class="span9" style="font-size: 14px">

    <h3>用户标签</h3>

    <form class="form-horizontal" action="/equator/newwonderland/tag/domocktag.vpage" method="post" id="mockForm">
        <ul class="inline">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" readonly/>
            <input type="hidden" id="targetName" name="targetName" value="" />
            <input type="button" class="btn btn-default" value="返回" onclick="javascript:history.back(-1);"/>
            <input type="submit" class="btn btn-default hidden" id="submitBtn" value="修改"/>
        </ul>
    </form>

    <#if targetTagConfigList ?? && targetTagConfigList?size gt 0 >
        <#list targetTagConfigList as targetTagConfig>
            <div class="float-tag tag-unselected">
                ${targetTagConfig.name}
            </div>
        </#list>
    </#if>

</div>
<script>
    $(function () {

        $("#submitBtn").click(function () {
            if (!$('#targetName').val()) {
                alert("请选择标签。");
                return false;
            }
            $('#mockForm').submit();
        });

        $(".float-tag").click(function () {
            $('#targetName').val($(this).text().trim());
            $(".float-tag").removeClass('tag-selected').addClass('tag-unselected');
            $(this).addClass('tag-selected');
            $("#submitBtn").css('visibility', 'visible');
        });
    });
</script>
</@layout_default.page>