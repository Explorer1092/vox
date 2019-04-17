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
        <legend>批量查询论坛老师信息</legend>
        <div>
            <form id="s_form" action="${requestContext.webAppContextPath}/site/teacher/batchgetshippingaddress.vpage"
                  method="post" class="form-horizontal">
                <fieldset>
                    <legend>各项均为必填项，请按规定格式填写</legend>
                </fieldset>
                <ul class="inline">
                    <li>
                        论坛姓名：<textarea name="userNames" cols="45" rows="10" placeholder="换行符分隔，一行一条"></textarea>
                    </li>
                    <li>
                        <button id="editBut" type="submit" class="btn btn-primary">查询</button>
                    </li>
                </ul>
            </form>
        </div>
        <div>
            查询结果：
            <table class="table table-bordered">
                <#if dataMap?has_content>
                    <#list dataMap?keys as key>
                        <tr>
                            <td <#if !(dataMap[key]??)>style="color: red" </#if>>${key!}</td>
                            <td>${(dataMap[key].userId)!}</td>
                            <td>${(dataMap[key].userName)!}</td>
                            <td>${(dataMap[key].pname)!}</td>
                            <td>${(dataMap[key].cname)!}</td>
                            <td>${(dataMap[key].aname)!}</td>
                            <td>${(dataMap[key].address)!}</td>
                            <td>${(dataMap[key].logisticType)!}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </fieldset>
</div>
</@layout_default.page>