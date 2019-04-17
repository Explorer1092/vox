<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='课程质量监督平台' page_num=25>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>

<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<div id="main_container" class="span9">
    <legend>${sectionName!}|${variantName!}</legend>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>前测id+后测id</td>
                <td>课程id</td>
                <td>课程名称</td>
                <td>课程命中数</td>
                <td>打开课程数</td>
                <td>完成课程数</td>
                <td>课程完成率</td>
                <td>后测Q1完成率</td>
                <td>后测Q1正确率</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.preId!} + ${e.postId!}</td>
                        <td>${e.courseId!}</td>
                        <td>${e.courseName!}</td>
                        <td>${e.courseTargetNum!}</td>
                        <td>${e.courseBeginNum!}</td>
                        <td>${e.courseFinishNum!}</td>
                        <td>${e.courseCompleteRate!}</td>
                        <td>${e.postCompleteRate!}</td>
                        <td>${e.postRightRate!}</td>
                        <td>
                            <button type="button" name="detail" seriesId="${e.seriesId!}" bookId="${e.bookId!}" unitId="${e.unitId!}"  sectionId="${e.sectionId!}" variantId="${e.variantId!}"
                                    courseId="${e.courseId!}" preId="${e.preId!}" postId="${e.postId!}" courseName="${e.courseName!}" class="btn btn-primary">报告详情</button>
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
        <input type="hidden" id="unitId" value="${unitId!}"/>
        <input type="hidden" id="sectionId" value="${sectionId!}"/>
        <input type="hidden" id="variantId" value="${variantId!}"/>
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
            var unitId = $("#unitId").val();
            var sectionId = $("#sectionId").val();
            var variantId = $("#variantId").val();
            window.location = "/crm/course/monitor/variant/list.vpage?pageNumber=" + pageNumber + "&seriesId=" + seriesId + "&bookId=" + bookId + "&unitId=" + unitId + "&sectionId=" + sectionId + "&variantId=" + variantId;
        }
            $(function () {
            $('button[name=detail]').on('click', function () {
                var seriesId = $(this).attr("seriesId");
                var bookId = $(this).attr("bookId");
                var unitId = $(this).attr("unitId");
                var sectionId = $(this).attr("sectionId");
                var variantId = $(this).attr("variantId");
                var courseId = $(this).attr("courseId");
                var preId = $(this).attr("preId");
                var postId = $(this).attr("postId");
                var courseName = $(this).attr("courseName");
                window.location = "/crm/course/monitor/variant/course/detail.vpage?seriesId="+seriesId +"&bookId=" +bookId + "&unitId=" + unitId + "&sectionId=" + sectionId + "&variantId="
                        + variantId + "&courseId=" + courseId + "&preId=" + preId + "&postId=" + postId + "&courseName=" + courseName;
            });
        });
    </script>
</@layout_default.page>