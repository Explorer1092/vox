<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='点读机教材管理' page_num=4>
<div class="span9">
    <fieldset>
        <legend>点读机教材管理</legend>
    </fieldset>

    <form id="ad-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                教材来源：<select id="searchSourceType" name="searchSourceType">
                <option value="">全部</option>
                <option value="SELF_DEVELOP">自研</option>
            </select>
            </span>
            <span style="white-space: nowrap;">
                教材学科：<select id="searchSubject" name="searchSubject">
                <option value="">全部</option>
                <option value="101">语文</option>
                <option value="103">英语</option>
            </select>
            </span>
            <span style="white-space: nowrap;">
                教材出版社：<select id="shortPublisher" name="shortPublisher">
                <option value="">全部</option>
                <#if shortPublisherList??>
                    <#list shortPublisherList as shortPublisher>
                        <option value="${shortPublisher!''}">${shortPublisher!''}</option>
                    </#list>
                </#if>
            </select>
            </span>
            <span style="white-space: nowrap;">
                BookId：<input type="text" id="searchBookId" name="searchBookId"/>
            </span>
        </div>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <button class="btn btn-primary" type="button" id="resetFilters">重置查询条件</button>
    <a class="btn btn-warning" id="newTextBook" name="book_detail">新建教材</a>
    <a class="btn btn-warning" id="importTextBook" name="import_book">导入更新教材列表页名称</a>
    <a class="btn btn-warning" id="exportTextBook" name="export_book">导出教材</a>
    <button class="btn btn-info" style="position:relative" id="uploadFile">
        <input class="fileUpBtn" type="file"
               accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" name="file"
               size="10"
               style="opacity: 0;position: absolute;left: 0;top: 0;width: 80px;"
        />上传文件
    </button>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>BookId</th>
                        <th>教材名称</th>
                        <th>教材来源</th>
                        <th>出版社</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if textBookList?? && textBookList?size gt 0>
                            <#list textBookList as textBook>
                            <tr>
                                <td>${textBook.bookId!''}</td>
                                <td>${textBook.bookName!''}</td>
                                <td>${textBook.bookSourceType!''}</td>
                                <td>${textBook.bookPublisher!''}</td>
                                <td>
                                    <a class="btn btn-primary"
                                       data-book_id="${textBook.bookId!''}" name="book_detail">详情</a>
                                    <button class="btn btn-danger" type="button" name="deleteBook"
                                            data-book_id="${textBook.bookId!''}">删除
                                    </button>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td>暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">


    $(function () {
        <#if searchSourceType??>
            var searchSourceType = "${(searchSourceType)!''}";
            $('#searchSourceType').val(searchSourceType);
        </#if>
        <#if searchSubject??>
            var searchSubject = "${(searchSubject)!''}";
            $('#searchSubject').val(searchSubject);
        </#if>

        <#if searchBookId??>
            var searchBookId = "${(searchBookId)!''}";
            $('#searchBookId').val(searchBookId);
        </#if>
        <#if shortPublisher??>
            $('#shortPublisher').val('${(shortPublisher)!''}');
        </#if>
        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        };
        $(document).on("click", "#resetFilters", function () {
            console.info("resetFilters");
            $("#searchSourceType").val("");
            $("#searchBookId").val("");
            $("#searchSubject").val("");
            $("#shortPublisher").val("");

        });

        $('#searchBtn').on("click", function () {
            $("#pageNum").val(1);
            $("#ad-query").submit();
        });

        $(".fileUpBtn").on("change", function () {

            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['xlsx']) == -1) {
                    alert("仅支持以下格式【'xlsx'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('source_file', $this[0].files[0]);
                $.ajax({
                    url: 'uploadTextBookData.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            alert("上传成功");
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });


        //新建/编辑
        $("a[name='book_detail']").on("click", function () {
            var book_id = $(this).data("book_id");
            var currentPage = $('#pageNum').val();
            if (book_id == undefined) {
                book_id = '';
            }
            var url = '/textbook/upsertTextBookData.vpage?bookId=' + book_id + '&currentPage=' + currentPage;
            window.open(url, '详情');
//            location.href = '/textbook/upsertTextBookData.vpage?bookId=' + book_id + '&currentPage=' + currentPage;
        });

        //新建/编辑
        $("a[name='import_book']").on("click", function () {
            location.href = '/textbook/importBookPage.vpage';
        });

        $("a[name='export_book']").on("click", function () {
            location.href = '/textbook/exportAll.vpage';
        });


        $("button[name='deleteBook']").on("click", function () {
            var book_id = $(this).data("book_id");
            if (confirm("确定删除？")) {
                $.ajax({
                    type: 'post',
                    url: 'deleteTextBook.vpage',
                    data: {
                        bookId: book_id
                    },
                    success: function (data) {
                        if (data.success) {
                            alert("删除成功");
                            location.reload();
                        } else {
                            alert("删除失败");
                        }
                    }
                });
            }
        });
    });


    function pagePost(pageNumber) {

        $("#pageNum").val(pageNumber);
        $("#ad-query").submit();
    }
</script>
</@layout_default.page>