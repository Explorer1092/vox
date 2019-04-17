<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="span9">
    <@h.head/>
    <fieldset>
        <legend>批量合并学校</legend>

        <form id="form" method="post" action="/crm/school/batchmergeschool.vpage">
            <ul class="inline">
                <li>
                    <label>
                        输入内容：<textarea id="batchContext" name="batchContext" cols="100" rows="10"
                                       placeholder="格式为：被合并学校ID 合并到的学校ID 描述 备注各项请以空格分隔，注意内容中的空格（请在excel里编辑好，直接贴进来，多条记录请用多行处理）"></textarea>
                        例如：12002 12003 学校重复
                    </label>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <input class="btn" type="button" onclick="batch()" value="提交" />
                </li>
            </ul>
        </form>
        <br>
        <div>
            <label>结果统计：</label>
            <br>
            <table>
                <tr>
                    <#if totalRecord??><td> 总共提交：</td> <td>${totalRecord!0} 条</td></#if>
                </tr>
                <tr>
                    <#if successlist??><td>成功：</td><td>${successlist?size}条</td></#if>
                </tr>
                <tr style="color: red">
                    <#if failedlist??><td>失败：</td><td> ${failedlist?size}条</td></#if>
                </tr>
            </table>
            <#if failedlist??>
                <br>
                <label>失败记录：</label>
                <table class="table table-bordered">
                    <#list failedlist as l>
                        <tr>
                            <td style="color: red">${l}</td>
                        </tr>

                    </#list>
                </table>
            </#if>
            <br>
            <#if successlist??>
                <br>
                <label>成功记录：</label>
                <table class="table table-bordered">
                    <#list successlist as l>
                        <tr>
                            <td style="color: green">${l}</td>
                        </tr>
                    </#list>
                </table>
            </#if>

        </div>
    </fieldset>
</div>
<script>
    function batch(){

        if($("#batchContext").val().trim() == '') {
            alert("请输入内容");
            return false;
        }
        $("#form").submit();
    }
</script>
</@layout_default.page>