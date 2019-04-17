<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <form method="post" action="batchcreateteacher.vpage">
        <ul class="inline">
            <li>
                <label>输入生成老师内容(老师姓名 老师手机 学校ID 学科 [班级ID(可选)])：<textarea name="content" cols="45" rows="10" placeholder="张三 13811111111 12345 ENGLISH [12345]"></textarea></label>
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
        <label>成功记录：</label>
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>姓名</th>
                    <th>手机</th>
                    <th>密码</th>
                </tr>
            </thead>
            <tbody>
                <#if successlist??>
                    <#list successlist as result>
                        <tr>
                            <td>${result.ID!}</td>
                            <td>${result.NAME!}</td>
                            <td>${result.PHONE!}</td>
                            <td>${result.PASSWORD!}</td>
                        </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>
</@layout_default.page>
