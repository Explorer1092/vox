<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="用户标签查询" page_num=24>
<style>
    span {
        margin-left: 10px;
    }

    div.tag {
        width: 250px;
    }

    div.tag-info {
        width: 50px;
    }

    div.float-tag {
        display: inline-block;
        height: 30px;
        line-height: 30px;
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
<span class="span9" style="font-size: 14px">

    <#include '../userinfotitle.ftl' />

    <form class="form-horizontal" action="/equator/newwonderland/tag/user.vpage" method="post" id="studentTagsForm">
        <ul class="inline">
            <input type="text" id="studentId" name="studentId" value="${studentId?default("")}" autofocus="autofocus" placeholder="输入学生ID">
            <input type="submit" class="btn btn-primary" id="submit_query" name="submit_query" value="查询">
            <#if studentId??>
                <#if studentName??>
                    <span>${studentName}拥有${matchedTags?size}个标签。</span>
                    <div class="tag-info float-tag tag-selected">拥有</div><div class="tag-info float-tag tag-unselected">未拥有</div>
                    <div title="※请确保您在修改的用户为测试用户。" style="margin-top: 20px">
                        <a class="btn btn-primary" href="/equator/newwonderland/tag/mocktag.vpage?studentId=${studentId!''}">修改用户标签</a>
                    </div>
                    <#else>
                    请输入正确的用户id。
                </#if>
            </#if>
        </ul>
    </form>

    <div class="inline">
        <#if studentName ?? && targetTagConfigList ?? && targetTagConfigList?size gt 0 >
            <#list targetTagConfigList as targetTagConfig>
                <div class="tag float-tag <#if matchedTags?seq_contains(targetTagConfig.id)>tag-selected<#else>tag-unselected</#if>">
                ${targetTagConfig.name}
            </div>
            </#list>
        </#if>
    </div>
</span>

<script>
    $(function () {
        $('#studentTagsForm').submit(function () {
            if (!$('#studentId').val()) {
                alert('请输入学生ID。');
                return false;
            }
        });
    });
</script>
</@layout_default.page>