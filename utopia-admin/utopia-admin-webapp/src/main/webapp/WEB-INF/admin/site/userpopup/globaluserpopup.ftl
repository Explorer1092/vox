<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">

    <legend>
        <a href="userpopuplist.vpage">弹窗广告</a> &nbsp;&nbsp;
        <a href="batchpopuphomepage.vpage">批量弹窗</a> &nbsp;&nbsp;
        全局弹窗
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <a href="addglobaluserpopup.vpage">新加全局弹窗</a>
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>ID</td>
                        <td>标题</td>
                        <td>内容</td>
                        <td>弹窗对象</td>
                        <td>开始时间</td>
                        <td>结束时间</td>
                    </tr>
                    <#if globalUserPopups ?? >
                        <#list globalUserPopups as popup >
                            <tr>
                                <td>${popup.id!}</td>
                                <td><a href="addglobaluserpopup.vpage?popupId=${popup.id!}">${popup.title!}</a></td>
                                <td>${popup.content!}</td>
                                <td>${popup.popupRules!}</td>
                                <td>${popup.startDatetime!}</td>
                                <td>${popup.endDatetime!}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</div>

</@layout_default.page>
