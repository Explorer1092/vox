<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>书籍列表</legend>
    </fieldset>
    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <span style="white-space: nowrap;">
                <#--课程ID：<select id="selectLessonId" name="selectLessonId">-->
                <#--<#if lessonIds?? && lessonIds?size gt 0>-->
                <#--<#list lessonIds as lessonId>-->
                <#--<option value="${lessonId}" <#if (((selectLessonId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>-->
                <#--</#list>-->
                <#--<#else>-->
                <#--<option value="">暂无数据</option>-->
                <#--</#if>-->
                <#--</select>-->
                书籍ID：<input type="text" id="searchBookId" name="searchBookId" value="${searchBookId!''}"/>
        </span>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <a class="btn btn-warning" id="createBook" name="create_book">新建书籍</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>书名</th>
                        <th>说明内容</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  bookInfo>
                            <tr>
                                <td>${bookInfo.id!''}</td>
                                <td>${bookInfo.title!''}</td>
                                <td>${bookInfo.comment!''}</td>
                                <td>
                                    <a class="btn btn-primary"
                                       data-book_id="${bookInfo.id!''}" name="group_edit">修改</a>
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

            $("#searchBtn").on('click', function () {
                $("#pageNum").val(1);
                $("#op-query").submit();
            });


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
            $("#createBook").on('click', function () {
                window.open("bookDetail.vpage");
            });

            //编辑
            $("a[name='group_edit']").on('click', function () {
                var bookId = $(this).data("book_id");
                if (!bookId) {
                    console.log("bookId null");
                    return;
                }
                var url = "bookDetail.vpage?bookId=" + bookId + "&currentPage=" +${currentPage!};
                window.open(url);
            });
        });
    </script>
</@layout_default.page>