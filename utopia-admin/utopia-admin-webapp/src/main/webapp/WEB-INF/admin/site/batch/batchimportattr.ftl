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
        <legend>批量导入特殊属性</legend>

        <form method="post" action="/site/batch/batchimportattr.vpage">
            <ul class="inline">
                <li>
                    <label>输入需要导入的老师ID：<textarea name="teacherIds" cols="45" rows="10" placeholder="请在这里输入用户ID，一行一条"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>选择特殊属性</label>
                    <#if activityTypeList??>
                        <ul class="unstyled">
                            <#list activityTypeList as activity>
                                <li>
                                    <label for="bw_${activity.key}">
                                        <input id="bw_${activity.key}" name="activities" value="${activity.key}" type="checkbox" /> ${activity.value}
                                    </label>
                                </li>
                            </#list>
                        </ul>
                    </#if>
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
                    <td>失败：</td><td><#if failedList??>${failedList?size}</#if>个</td>
                </tr>
            </table>
            <label>失败记录：</label>
            <table class="table table-bordered">
                <#if failedList??>
                    <#list failedList as l>
                        <tr>
                            <td>${l}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </fieldset>
</div>
</@layout_default.page>