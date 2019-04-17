<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='课程质量监督平台' page_num=25>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>

<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<div id="main_container" class="span9">
    <legend>课程质量监控平台</legend>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>教材系列</td>
                <td>教材名称</td>
                <td>线上课程个数</td>
                <td>已使用课程个数</td>
                <td>平均课程完成率</td>
                <td>平均课程纠错率</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.seriesName!}</td>
                        <td>${e.bookName!}</td>
                        <td>${e.courseNumOnline!}</td>
                        <td>${e.courseNumUsed!}</td>
                        <td>${e.courseCompleteRateAvg!}</td>
                        <td>${e.courseRightRateAvg!}</td>
                        <td>
                            <button type="button" name="detail" seriesId="${e.seriesId!}" bookId="${e.bookId!}" sectionName="${e.sectionName!}"
                                    variantName="${e.variantName!}" bookName="${e.bookName!}" class="btn btn-primary">查看详情</button>
                        </td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="15"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
    </div>
    <ul class="pager">
        <#if (pageData.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if (pageData.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共 ${pageData.totalPages!} 页|</li>
        <li>共 ${total !} 条</li>
    </ul>

   <script type="text/javascript">
        function pagePost(pageNumber) {
            $("#pageNumber").val(pageNumber);
            window.location = "/crm/course/monitor/course/list.vpage?pageNumber=" + pageNumber
        }
        $(function () {
            $('button[name=detail]').on('click', function () {
                var seriesId = $(this).attr("seriesId");
                var bookId = $(this).attr("bookId");
                var sectionName = $(this).attr("sectionName");
                var variantName = $(this).attr("variantName");
                var bookName = $(this).attr("bookName");
                window.location = "/crm/course/monitor/book/preview.vpage?seriesId=" + seriesId + "&bookId=" + bookId + "&sectionName=" + sectionName
                        + "&variantName=" + variantName + "&bookName=" + bookName;
            });
        });
    </script>
</@layout_default.page>