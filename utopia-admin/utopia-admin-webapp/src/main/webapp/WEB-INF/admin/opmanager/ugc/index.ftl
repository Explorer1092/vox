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
                UGC收集活动管理
                <a href="editrecord.vpage" role="button" class="btn btn-success">添加活动</a>
                <a href="questionindex.vpage" role="button" class="btn btn-success">题目管理</a>
            </legend>
        </fieldset>
    </div>

    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>ID</td>
                <td>活动名称</td>
                <td>角色</td>
                <td>开始日期</td>
                <td>结束日期</td>
                <td>范围类型</td>
                <td>是否有效</td>
                <td>操作</td>
            </tr>
            <#if recordList?? >
                <#list recordList as record >
                    <tr>
                        <td>${record.id!}</td>
                        <td>${record.name!}</td>
                        <td>${record.userType!}</td>
                        <td>${record.startDate?string("yyyy-MM-dd HH:mm:ss")!}</td>
                        <td>${record.endDate?string("yyyy-MM-dd HH:mm:ss")!}</td>
                        <td>${record.codeType!}</td>
                        <td><#if record.disabled>否<#else>是</#if></td>
                        <td>
                            <a href="editrecord.vpage?recordId=${record.id!}" role="button" class="btn btn-success">编辑</a>
                            <a href="questionref.vpage?recordId=${record.id!}" role="button" class="btn btn-success">关联题目</a>
                            <a href="coderef.vpage?recordId=${record.id!}" role="button" class="btn btn-success">关联范围</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>