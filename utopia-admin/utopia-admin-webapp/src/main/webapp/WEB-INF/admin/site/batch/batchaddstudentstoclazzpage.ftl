<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
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
        <legend>批量添加学生进班</legend>

        <form method="post" action="/site/batch/batchaddstudentstoclazz.vpage">
            <ul class="inline">
                <li>
                    <label>
                        <p>输入添加学生进班内容(学生ID, 老师ID, 班级ID)</p>

                        <textarea id="content" name="content" cols="45" rows="10" placeholder="30001 125074 123456"></textarea>
                    </label>
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
                <#if (info.failed)??>
                    <#list (info.failed) as l>
                        <tr>
                            <td>${l}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
        <br />
        <ul id="preview_fails" style="color: red;"></ul>
    </fieldset>
</div>

<script type="text/javascript">
    $(function(){
    });
</script>
</@layout_default.page>