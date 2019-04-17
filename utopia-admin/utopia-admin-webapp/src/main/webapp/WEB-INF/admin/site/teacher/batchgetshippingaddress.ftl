<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    span {
        font: "arial";
    }
</style>
<div id="main_container" class="span9">
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
        <label>查询结果：</label>
        <#if !dataList?has_content>
            没有查询到结果。请确认输入的姓名是否正确。
        </#if>
        <table class="table table-bordered">
            <#if dataList?has_content>
                <#list dataList as data>
                    <tr>
                        <td>${data.bbsName!}</td>
                        <td>${data.userId!}</td>
                        <td>${data.userName!}</td>
                        <td>${data.pname!}</td>
                        <td>${data.cname!}</td>
                        <td>${data.aname!}</td>
                        <td>${data.address!}</td>
                        <td>${data.sensitivePhone!}</td>
                        <td>${data.logisticType!}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>
