<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条英语课程管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>
<#--<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">-->
<div id="main_container" class="span9">
    <legend>薯条英语课程管理
        <#--<button type="button" id="create" class="btn btn-primary pull-right">新建(Create)</button>-->
    </legend>
    <div id="data_table_journal">
        <div class="table-responsive table_box">
            <table class="table table-striped table-bordered">
                <tr>
                    <td>产品</td>
                    <td>天数</td>
                    <td>开始日期</td>
                    <td>结束日期</td>
                    <td>操作</td>
                </tr>
                <#if courseList?? && courseList?size gt 0>
                    <#list courseList as e >
                    <tr>
                        <td>${e.productName!}</td>
                        <td>
                          ${e.allUnitNum!}
                        </td>
                        <td>${e.beginDate!}</td>
                        <td>${e.endDate!}</td>
                        <td>
                            <div class="input-append date" id="datetimepicker" onclick="enterClick('${e.productId!}')">
                                <input class="span2" size="16" type="text" >
                                <span class="add-on"><i class="icon-th" ></i></span>
                            </div>
                                <#--<div class="input-append date" id="datetimepicker" data-date="12-02-2012" data-date-format="dd-mm-yyyy">-->
                                    <#--<input class="span2" size="16" type="text" value="12-02-2012">-->
                                    <#--<span class="add-on"><i class="icon-remove"></i></span>-->
                                    <#--<span class="add-on"><i class="icon-th"></i></span>-->
                                <#--</div>-->
                        </td>
                    </tr>
                    </#list>
                <#else >
                    <tr>
                        <td colspan="13"><strong>暂无数据</strong></td>
                    </tr>
                </#if>
            </table>
        </div>
    </div>

    <script type="text/javascript">
        function enterClick(productId, itemId, bookId) {
            window.location.href = "/chips/chips/coursemanager/edit.vpage?productId=" + productId + "&itemId=" + itemId + "&bookId=" + bookId;
        };

    </script>
</@layout_default.page>