<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='批量查询用户最近登录时间' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {  font: "arial";  }
    .title{padding:5px 0;margin-bottom:5px;font-weight: bold;border:1px solid #0f92a8;background:#27a9bf;border-radius:2px;text-align: center;color:#fff;}
    .clear-btn{padding:5px 0;margin-bottom:5px;font-weight: bold;background-color:#fda700;border:1px solid #fd8f00;border-radius:2px;text-align: center;color:#fff;}
    .input-user-id{resize: none;overflow-y: auto; overflow-x: hidden; margin-top: 10px; height: 70%;}
</style>
<div class="span9">

    <@h.head/>

    <fieldset>
        <legend> <strong>批量查询用户最近登录时间</strong> </legend>
        <div style="width: 25%; float: left;">
            <button class="title" id="query-btn" onclick="$('#frm').submit();" style="width: 20%;"> 查  询 </button>
            <button class="clear-btn" onclick="$('#userIds').text('');" style="width: 20%; margin-left: 30px;"> 清 空 </button>
            <form id="frm" method="post" action="/site/batch/queryuserlogin.vpage">
                <textarea id="userIds" name="userIds" cols="15" rows="20" placeholder="请在这里输入要查询的用户ID，一行一条" class="input-user-id">${userIds!''}</textarea>
            </form>
        </div>

        <div style="width: 75%; float: right; ">
            <div class="title" style="width: 40%;"> 查 询 结 果 </div>

            <table class="table table-hover table-striped table-bordered" >
                <thead>
                    <th>用户ID</th><th>最近登录时间</th>
                    <th>用户ID</th><th>最近登录时间</th>
                    <th>用户ID</th><th>最近登录时间</th>
                </thead>
                <tbody>
                <#if (loginResult?? && loginResult?has_content)>
                    <#list loginResult as info>
                        <#if info_index % 3 == 0><tr></#if>
                        <td>${info.userId!'--'}</td><td><#if info.loginDate??>${info.loginDate?string('yyyy-MM-dd HH:mm:ss')}<#else>未登录</#if> </td>
                        <#if info_index % 3 == 2 || !info_has_next>
                            <#if info_index % 3 == 1><td colspan="2"></td></#if>
                            <#if info_index % 3 == 0><td colspan="2"></td><td colspan="2"></td></#if>
                        </tr>
                        </#if>
                    </#list>
                <#else>
                    <tr class="success"><td colspan="9" style="text-align: center; color:red;">请于左边输入框输入用户ID</td></tr>
                </#if>
                </tbody>
            </table>
        </div>
    </fieldset>

</div>
</@layout_default.page>