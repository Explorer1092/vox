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
        <legend>批量导出老师邀请关系</legend>

        <form method="post" action="/site/batch/downloadteacherinvites.vpage">
            <ul class="inline">
                <li>
                    <label>输入老师ID：<textarea name="teacherIds" cols="45" rows="10" placeholder="请在这里输入用户ID，一行一条"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <input class="btn" type="submit" value="导出" />
                </li>
            </ul>
        </form>
    </fieldset>
</div>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>