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
        <legend>批量班级充值学豆</legend>

        <form id="form" method="post" action="/site/award/changeclazzintegral.vpage">
            <ul class="inline">
                <li>
                    <label>
                        输入内容：<textarea id="content" name="content" cols="100" rows="10" placeholder="请在这里输入要充值的班级ID,老师ID,及学豆数量（请在excel里编辑好，直接贴进来，多个班级请用多行处理）"></textarea>
                        例如：12345 161234 10
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
    </fieldset>
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