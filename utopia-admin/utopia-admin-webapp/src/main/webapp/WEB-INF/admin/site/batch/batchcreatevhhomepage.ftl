<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
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
        <legend>批量布置假期迆</legend>

        <form method="post" action="/site/vh/batchcreatevh.vpage">
            <ul class="inline">
                <li>
                    <label>
                        输入生成作业内容(老师ID列表)：
                        <textarea name="content" cols="45" rows="10" placeholder="116404 11378"></textarea>
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
                <#if failedlist??>
                    <#list failedlist as l>
                        <tr>
                            <td>${l}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </fieldset>
    <br/>
</div>
</@layout_default.page>