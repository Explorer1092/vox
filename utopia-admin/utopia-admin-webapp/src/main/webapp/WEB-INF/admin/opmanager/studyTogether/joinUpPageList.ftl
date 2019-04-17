<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>课程介绍页列表</legend>
    </fieldset>
    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <a class="btn btn-warning" id="createPage" name="create_page">新建课程介绍页</a>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>序号</th>
                        <th>课程 id</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  pageInfo>
                            <tr>
                                <td>${pageInfo_index+1!''}</td>
                                <td>${pageInfo.lessonId!''}</td>
                                <td>
                                    <a class="btn btn-primary"
                                       data-lesson_id="${pageInfo.lessonId!''}" name="group_edit">修改</a>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list">
                </ul>
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
                maxNumber: 10,
                jumpCallBack: function (index) {
                    $("#pageNum").val(index);
                    $("#op-query").submit();
                }
            });

            //新建
            $("#createPage").on('click', function () {
                window.open("joinUpPageDetail.vpage");
            });

            //编辑
            $("a[name='group_edit']").on('click', function () {
                var lessonId = $(this).data("lesson_id");
                if (!lessonId) {
                    console.log("lessonId null");
                    return;
                }
                var url = "joinUpPageDetail.vpage?lessonId=" + lessonId + "&currentPage=" +${currentPage!};
                window.open(url);
            });
        });
    </script>
</@layout_default.page>