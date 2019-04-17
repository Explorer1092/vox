<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='课程质量监督平台' page_num=25>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>

<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<div id="main_container" class="span9">
    <legend>${bookName}</legend>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>单元</td>
                <td>课时</td>
                <td>变式名称</td>
                <td>平均课程完成率</td>
                <td>平均后测完成率</td>
                <td>对照后测完成率</td>
                <td>平均课程纠错率</td>
                <td>对照课程纠错率</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.unitName!}</td>
                        <td>${e.sectionName!}</td>
                        <td>${e.variantName!}</td>
                        <td>${e.courseCompleteRateAvg!}</td>
                        <td>${e.postCompleteRateAvg!}</td>
                        <td>${e.controlPostCompleteRate!}</td>
                        <td>${e.courseRightRateAvg!}</td>
                        <td>${e.controlCourseRightRate!}</td>
                        <td>
                            <button type="button" name="detail" seriesId="${e.seriesId!}" bookId="${e.bookId!}" unitId="${e.unitId!}"  sectionId="${e.sectionId!}"
                                    variantId="${e.variantId!}" sectionName="${e.sectionName!}" variantName="${e.variantName!}" class="btn btn-primary">查看详情</button>
                        </td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="15"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
        <input type="hidden" id="seriesId" value="${seriesId!}"/>
        <input type="hidden" id="bookId" value="${bookId!}"/>
        <input type="hidden" id="bookName" value="${bookName!}"/>
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
            var seriesId = $("#seriesId").val();
            var bookId = $("#bookId").val();
            var sectionName = $("#sectionName").val();
            var variantName = $("#variantName").val();
            var bookName = $("#bookName").val();
            window.location = "/crm/course/monitor/book/preview.vpage?pageNumber=" + pageNumber + "&seriesId=" + seriesId + "&bookId=" + bookId
                    + "&sectionName=" + sectionName + "&variantName=" + variantName + "&bookName=" + bookName;
        }
        $(function () {
            $('button[name=detail]').on('click', function () {
                var seriesId = $(this).attr("seriesId");
                var bookId = $(this).attr("bookId");
                var unitId = $(this).attr("unitId");
                var sectionId = $(this).attr("sectionId");
                var variantId = $(this).attr("variantId");
                var sectionName = $(this).attr("sectionName");
                var variantName = $(this).attr("variantName");
                window.location = "/crm/course/monitor/variant/list.vpage?seriesId=" + seriesId + "&bookId=" + bookId + "&unitId=" + unitId + "&sectionId=" + sectionId
                        + "&variantId=" + variantId  + "&sectionName=" + sectionName + "&variantName=" + variantName;
            });
        });
    </script>
</@layout_default.page>