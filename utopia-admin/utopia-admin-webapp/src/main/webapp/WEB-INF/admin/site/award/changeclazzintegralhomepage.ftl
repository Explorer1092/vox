<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <form id="form" method="post" action="changeclazzintegral.vpage">
        <ul class="inline">
            <li>
                <label>
                    输入内容：<textarea id="content" name="content" cols="100" rows="10" placeholder="请在这里输入要充值的班级ID,学科,及学豆数量（请在excel里编辑好，直接贴进来，多个班级请用多行处理）"></textarea>
                    例如：12345 ENGLISH 10
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>
                    充值原因：<textarea id="comment" name="comment" cols="100" rows="5" placeholder="必填"></textarea>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <input class="btn" type="button" onclick="post();" value="提交" />
            </li>
        </ul>
    </form>
    <div>
        <label>统计：</label>
        <table class="table table-bordered">
            <tr>
                <td>发送成功：</td><td><#if successlist??>${successlist?size}</#if>件</td>
                <td>发送失败：</td><td><#if failedlist??>${failedlist?size}</#if>件</td>
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
<script>
    function post(){
        if($("#content").val() == '') {
            alert("请输入内容");
            return false;
        }
        if($("#comment").val() == '') {
            alert("请输入原因");
            return false;
        }
        $("#form").submit();
    }

</script>
</@layout_default.page>
