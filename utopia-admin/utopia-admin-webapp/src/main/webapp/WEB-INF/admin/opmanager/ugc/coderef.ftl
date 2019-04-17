
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
                UGC活动关联代码管理--当前编辑活动：${record.name!''}，活动关联代码类型：${record.codeType!''}
            </legend>
        </fieldset>
    </div>

    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>活动ID</td>
                <td>关联代码</td>
            </tr>
            <#if refList?? >
                <#list refList as q >
                    <tr>
                        <td>${q.recordId!}</td>
                        <td>${q.code!}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
    <div id="data_table_journal">
        <form method="post" action="savecoderef.vpage">
            <input type="hidden" value="${record.id!}" name="recordId" />
            <ul class="inline">
                <li>
                    <label>请重新输入关联的Code：<textarea name="codes" cols="45" rows="10" placeholder="一行一条"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <input class="btn" type="submit" value="提交" />
                </li>
            </ul>
        </form>
    </div>
    <div>
        <label>统计：</label>
        <table class="table table-bordered">
            <tr>
                <td>成功：</td><td><#if successlist??>${successlist?size}</#if>条</td>
                <td>失败：</td><td><#if failedlist??>${failedlist?size}</#if>条</td>
            </tr>
        </table>
        <label>成功记录：</label>
        <table class="table table-bordered">
            <#if successlist??>
                <#list successlist as l>
                    <tr>
                        <td>${l}</td>
                    </tr>
                </#list>
            </#if>
        </table>
        <label>失败记录：</label>
        <table class="table table-bordered">
            <#if failedlist??>
                <#list failedlist as l>
                    <tr>
                        <td>${l}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>