<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    table td {font-size: 14px;}
    input {
        width: 197px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>点评反馈列表</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>学生ID&nbsp;
                        <input type="text" id="searchStudentId" name="searchStudentId" value="${searchStudentId!''}"/>
                    </label>
                </li>
                <li>
                    <label>点评课程ID&nbsp;
                        <input type="text" id="searchCourseId" name="searchCourseId" value="${searchCourseId!''}"/>
                    </label>
                </li>
                <li>
                    <label>点评结果&nbsp;
                        <select id="satisfaction" name="satisfaction">
                            <option value="-1">全部</option>
                            <option value="0" <#if satisfaction??&&satisfaction == 0>selected</#if>>不满意</option>
                            <option value="1" <#if satisfaction??&&satisfaction == 1>selected</#if>>满意</option>
                        </select>
                    </label>
                </li>
                <li>
                    <button type="button" class="btn btn-primary" id="searchBtn">查询</button>
                </li>
                <li>
                    <button class="btn btn-primary" type="button" id="exportExcel">导出数据</button>
                </li>
            </ul>
        </div>
    </form>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>家长ID</th>
                        <th>学生姓名</th>
                        <th>学生ID</th>
                        <th>古诗课程ID</th>
                        <th>点评课程ID</th>
                        <th>古诗名称</th>
                        <th>反馈日期</th>
                        <th>点评人</th>
                        <th>反馈结果</th>
                        <th>其他留言</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as feed>
                            <tr>
                                <td>${feed.parentId!''}</td>
                                <td>${feed.studentName!''}</td>
                                <td>${feed.studentId!''}</td>
                                <td>${feed.lessonId!''}</td>
                                <td>${feed.courseId!''}</td>
                                <td>${feed.title!''}</td>
                                <td>${feed.createDate!''}</td>
                                <td>${feed.teacherName!''}</td>
                                <td>
                                    <#if feed.satisfaction == 0>
                                        <span class="label label-success">不满意</span>
                                    <#elseif feed.satisfaction == 1>
                                       <span class="label label-success">满意</span>
                                    </#if>
                                </td>
                                <td>${feed.desc!''}</td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="12" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <div class="message_page_list"></div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(function () {

        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });

        $("#exportExcel").on('click', function () {
            var studentId = $("#searchStudentId").val();
            var courseId = $("#searchCourseId").val();
            var satisfaction = $("#satisfaction").val();
            location.href = "exportbackdata.vpage?studentId=" + studentId + "&courseId=" + courseId + "&satisfaction=" + satisfaction;
        });
    });

</script>
</@layout_default.page>