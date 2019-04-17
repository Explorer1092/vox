<#import "../../layout_default.ftl" as layout_default />
<#import "../batch/head.ftl" as h/>
<@layout_default.page page_title='CRM-批量查询学校' page_num=4>
<div class="span9">
    <@h.head/>
    <fieldset>
        <legend>批量查询学校</legend>
    <form method="post" action="batchsearchschool.vpage">
        <ul class="inline">
            <li>
                <label>输入批量查询学校的信息：<br><br><textarea name="content" cols="150" rows="10" style="width: 60%" placeholder="请在这里输入要查询的学校信息，格式为：学校ID 学校名称（请在excel里编辑好，直接贴进来，多条记录请用多行处理）"></textarea></label>
                <label style="color: red">Tips:<br>考虑到学校信息综合查询API的调用不能过于频繁，每查询一条学校信息可能会花费1s或更久</label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <input class="btn" type="submit" value="提交" />
            </li>
        </ul>
    </form>
    </fieldset>
</div>
</@layout_default.page>
