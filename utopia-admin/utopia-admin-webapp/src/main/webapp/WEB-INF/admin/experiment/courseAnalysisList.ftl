<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='通用课程实验报告' page_num=25>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>

<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<div id="main_container" class="span9">
    <legend>实验报告预览</legend>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>实验Id</td>
                <td>实验名称</td>
                <td>课程Id</td>
                <td>课程名称</td>
                <td>前测题人数</td>
                <td>前测正确率</td>
                <td>课程命中</td>
                <td>打开课程</td>
                <td>完成课程</td>
                <td>课程完成率</td>
                <td>后测Q_1完成率</td>
                <td>后测Q_1正确率</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.expId!}</td>
                        <td>${e.expName!}</td>
                        <td>${e.courseId!}</td>
                        <td>${e.courseName!}</td>
                        <td>${e.preQuestionDoNum!}</td>
                        <td>${e.preQuestionRightRate!}</td>
                        <td>${e.courseTargetNum!}</td>
                        <td>${e.courseBeginNum!}</td>
                        <td>${e.courseFinishNum!}</td>
                        <td>${e.courseCompleteRate!}</td>
                        <td>${e.postQuestionCompleteRate!}</td>
                        <td>${e.postQuestionRightRate!}</td>
                        <td>
                            <button type="button" name="detail" expGroupId="${e.expGroupId!}" expId="${e.expId!}" courseId="${e.courseId!}" courseName="${e.courseName!}"class="btn btn-primary">查看详情</button>
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
            window.location = "/crm/experiment/diagnosis/courseAnalysis/list.vpage?group=${group}&pageNumber=" + pageNumber;
        }
        $(function () {
            $('button[name=detail]').on('click', function () {
                var expGroupId = $(this).attr("expGroupId");
                var expId = $(this).attr("expId");
                var courseId = $(this).attr("courseId");
                var courseName = $(this).attr("courseName");
                window.location = "/crm/experiment/diagnosis/userCourse/behavior.vpage?expGroupId="+expGroupId +"&expId=" +expId + "&courseId=" + courseId + "&courseName=" + courseName;
            });
        });
    </script>
</@layout_default.page>