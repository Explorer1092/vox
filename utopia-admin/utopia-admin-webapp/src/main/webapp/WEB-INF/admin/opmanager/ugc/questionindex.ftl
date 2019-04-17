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
                UGC题目管理
                <a href="editquestion.vpage" role="button" class="btn btn-success">添加问题</a>
            </legend>
        </fieldset>
    </div>

    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>ID</td>
                <td>题干</td>
                <td>问题类型</td>
                <td>问题选项</td>
                <td>是否有效</td>
                <td>操作</td>
            </tr>
            <#if questionList?? >
                <#list questionList as q >
                    <tr>
                        <td>${q.id!}</td>
                        <td>${q.questionName!}</td>
                        <td>${q.questionType!}</td>
                        <td>${q.questionOptions!}</td>
                        <td><#if q.disabled>否<#else>是</#if></td>
                        <td>
                            <a href="editquestion.vpage?questionId=${q.id!}" role="button" class="btn btn-success">编辑</a>

                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>