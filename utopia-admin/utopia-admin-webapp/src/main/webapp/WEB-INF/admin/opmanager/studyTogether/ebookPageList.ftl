<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>书页列表</legend>
    </fieldset>
    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <input type="hidden" id="bookType" name="bookType" value="${bookType!'0'}"/>
        <span style="white-space: nowrap;">
                书籍名称：<select id="selectBookId" name="selectBookId">
                <option value="">请选择</option>
                <#if bookMap?? && bookMap?size gt 0>
                    <#list bookMap as book>
                        <option value="${book.id!''}"
                                <#if (((bookId)!'') == book.id)>selected="selected"</#if>>${book.title!''}</option>
                    </#list>
                <#else>
                    <option value="">暂无数据</option>
                </#if>
        </select>
            书籍ID：<input type="text" id="searchBookId" name="searchBookId" value="${searchBookId!''}"/>
        </span>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <a class="btn btn-warning" id="createPage" name="create_page">新建书页</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>页码</th>
                        <th>说明内容</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  pageInfo>
                            <tr>
                                <td>${pageInfo.id!''}</td>
                                <td><#if pageInfo.pageNum!=0>${pageInfo.pageNum!''}<#else>未设置</#if></td>
                                <td>${pageInfo.comment!''}</td>
                                <td>
                                    <a class="btn btn-primary"
                                       data-page_id="${pageInfo.id!''}" name="group_edit">修改</a>
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
            $("#createPage").on('click', function () {
                var selectBookId = $("#selectBookId").val();
                var searchBookId = $("#searchBookId").val();
                if (!selectBookId) {
                    if (!searchBookId) {
                        alert("请选择一个书籍再进行添加");
                        return false;
                    } else {
                        selectBookId = searchBookId;
                    }
                }
                var bookType =${bookType!'0'};
                var currentPage =${currentPage!};
                $.ajax({
                    url: 'get_ebook_by_id.vpage',
                    type: 'GET',
                    data: {"bookId": selectBookId},
                    async: false,
                    success: function (data) {
                        if (data.success) {
                            bookType = data.ebookType;
                            window.open("pageDetail.vpage?bookId=" + selectBookId + "&bookType=" + bookType + "&currentPage=" + currentPage);
                        } else {
                            alert("输入的bookId有误，请检查！");
                        }
                    }
                });
            });

            //编辑
            $("a[name='group_edit']").on('click', function () {
                var pageId = $(this).data("page_id");
                var bookType =${bookType!'0'};
                var currentPage =${currentPage!};
                if (!pageId) {
                    console.log("pageId null");
                    return;
                }
                var url = "pageDetail.vpage?pageId=" + pageId + "&bookType=" + bookType + "&currentPage=" + currentPage;
                window.open(url);
            });
        })
        ;
    </script>
</@layout_default.page>