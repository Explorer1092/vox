<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <form method="post" action="recreatehomeworkreport.vpage">
        <ul class="inline">
            <li>
                <label>输入重新检查作业内容(不重新奖励园丁豆)(学科 作业ID)：<textarea name="content" cols="45" rows="10" placeholder="ENGLISH abdcefg"></textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <input class="btn" type="submit" value="提交" />
            </li>
        </ul>
    </form>
    <div>
        <label>统计：</label>
        <table class="table table-bordered">
            <tr>
                <td>成功：</td><td><#if successlist??>${successlist?size}</#if>件</td>
                <td>失败：</td><td><#if failedlist??>${failedlist?size}</#if>件</td>
            </tr>
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
