<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<style>
    span {
        font: "arial";
    }
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>
                UGC活动关联问题管理--当前编辑活动：${record.name!''}
            </legend>
        </fieldset>
    </div>

    <div id="data_table_journal">
        <#if questionList?? >
            <#list questionList as q >
                <p>
                    <label for="ck-${q.id!}">
                        <input type="checkbox" id="ck-${q.id!}" value="${q.id!}" <#if qids??>
                            <#list qids as ids>
                               <#if ids == q.id>checked="checked"</#if>
                            </#list>
                        </#if> name="refCheck"/>  ${q.questionName!''}
                    </label>
                </p
            </#list>
        </#if>
    </div>
    <div class="modal-footer">
        <button id="saveBtn" class="btn btn-primary">保 存</button>
    </div>
</div>
<script type="application/javascript">
    $(function () {

        $("#saveBtn").on("click", function () {
            var checkedList = [];
            $("#data_table_journal input:checked").each(function () {
                checkedList.push($(this).val());
            });
            $.ajax({
                type: "post",
                url: "saverecordquestionref.vpage",
                data: {recordId : ${record.id!''}, questionIds: checkedList.join()},
                success: function (data) {
                    if (data.success) {
                        window.location.href = 'index.vpage';
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>