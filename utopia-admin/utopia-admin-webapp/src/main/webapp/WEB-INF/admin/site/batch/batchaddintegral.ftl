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
        <legend>批量添加积分</legend>

        <form id="form" method="post" action="/toolkit/integral/batchaddintegral.vpage">
            <ul class="inline">
                <li>
                    <label>
                        输入内容：<textarea id="batchAddIntegralContext" name="batchAddIntegralContext" cols="100" rows="10"
                                       placeholder="格式为：用户ID 积分数量 积分类型 备注各项请以空格分隔，注意填写的数量是积分（请在excel里编辑好，直接贴进来，多条积分记录请用多行处理）"></textarea>
                        例如：12345 161234 10 test
                    </label>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <input class="btn" type="button" onclick="batchAddIntegral()" value="提交" />
                </li>
            </ul>
        </form>
        <br>
        <div>
            <label>积分导入结果统计：</label>
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
                <label>失败记录：</label>  <input type="button" name="exportErrorData" id="exportErrorData" value="导出错误数据" >
            <table class="table table-bordered">
                <#list failedlist as l>
                    <tr>
                        <td style="color: red">${l}</td>
                    </tr>

                </#list>

            </#if>
        </table>
        </div>
    </fieldset>
</div>
<script>
    function batchAddIntegral(){

        if($("#batchAddIntegralContext").val().trim() == '') {
            alert("请输入内容");
            return false;
        }
        $("#form").submit();
    }

    $(function(){
        $("#exportErrorData").on("click",function(){
            window.location.href = "/toolkit/integral/exportErrorData.vpage";
        });
    });

</script>
</@layout_default.page>