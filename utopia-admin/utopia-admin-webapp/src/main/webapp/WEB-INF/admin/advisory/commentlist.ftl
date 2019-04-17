<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-评论管理' page_num=13>
<div class="span9">
    <fieldset>
        <legend>评论管理</legend>
    </fieldset>

    <fieldset>
        <div id="commentBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>

<div class="modal fade" id="replyContentBox">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">评论回复</h4>
            </div>
            <div id="replyBox">

            </div>

        </div>
    </div>
</div>

<script type="text/html" id="replyBox_tem">
    <div class="modal-body">
        <label for="replyContent"></label><textarea id="replyContent" style="width: 97%" placeholder="请输入回复内容[1000字内]" maxlength="1000"></textarea>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
        <button type="button" class="btn btn-primary" id="saveReplyContentBtn" data-comment_id="<%=commentId%>">保存</button>
    </div>
</script>

<script type="text/html" id="commentBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th width="5%">序号</th>
            <th width="10%">用户ID</th>
            <th width="10%">用户名</th>
            <th width="35%">评论</th>
            <th width="30%">回复</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
            <%for(var i = 0; i < content.length;i++){%>
                <tr>
                    <td><%=i+1%></td>
                    <td><%=content[i].userId%></td>
                    <td><%=content[i].userName%></td>
                    <td><%=content[i].comment%></td>
                    <td><%=content[i].replyComment%></td>
                    <td data-comment_id="<%=content[i].commentId%>">
                        <%if(content[i].isShow){%>
                            <button class="btn btn-danger" id="cancelShowBtn">取消展示评论</button>
                        <%}else{%>
                            <button class="btn btn-primary" id="showBtn">展示评论</button>
                        <%}%>
                        <%if(content[i].replyComment != null){%>
                            <%if(content[i].replyIsShow){%>
                                <button class="btn btn-info" id="cancelReplyBtn">取消展示回复</button>
                            <%}else{%>
                                <button class="btn btn-info" id="showReplyBtn">展示回复</button>
                            <%}%>
                        <%}%>
                        <button class="btn btn-success" id="replyBtn">回复</button>
                    </td>
                </tr>
            <%}%>
        <%}else{%>
            <tr>
                <td colspan="6">暂无数据</td>
            </tr>
        <%}%>
    </table>
</script>

<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<script type="text/javascript">
    function getCommentList(page) {
        $.post('getcommentlist.vpage', {currentPage: page, newsId: '${newsId!''}'}, function (data) {
            if (data.success) {
                $('#commentBox').html(template("commentBox_tem", {
                    content: data.commentList
                }));

                $(".message_page_list").page({
                    total: data.totalPage,
                    current: data.currentPage,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        getCommentList(index);
                    }
                });
            }
        });
    }

    function replyContentBoxShowORHide(sh) {
        $('#replyContentBox').modal(sh);
    }

    $(function () {
        getCommentList(1);

        //取消展示评论
        $(document).on('click', '#cancelShowBtn', function () {
            var $this = $(this);
            var commentId = $this.closest('td').data('comment_id');
            $.post("offlinecomment.vpage", {commentId: commentId}, function (data) {
                if (data.success) {
                    $this.attr("class", "btn btn-primary");
                    $this.attr("id", "showBtn");
                    $this.text("展示评论");
                } else {
                    alert(data.info);
                }
            });
        });

        //展示评论
        $(document).on('click', '#showBtn', function () {
            var $this = $(this);
            var commentId = $this.closest('td').data('comment_id');
            $.post("onliecomment.vpage", {commentId: commentId}, function (data) {
                if (data.success) {
                    $this.attr("class", "btn btn-danger");
                    $this.attr("id", "cancelShowBtn");
                    $this.text("取消展示评论");
                } else {
                    alert(data.info);
                }
            });
        });


        //取消展示回复
        $(document).on('click', '#cancelReplyBtn', function () {
            var $this = $(this);
            var commentId = $this.closest('td').data('comment_id');
            if (confirm("确定取消回复？")) {
                $.post("offlinecommentreply.vpage", {commentId: commentId}, function (data) {
                    if (data.success) {
                        getCommentList(1);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        //展示回复
        $(document).on('click', '#showReplyBtn', function () {
            var $this = $(this);
            var commentId = $this.closest('td').data('comment_id');
            if (confirm("确定展示回复？")) {
                $.post("onliecommentreply.vpage", {commentId: commentId}, function (data) {
                    if (data.success) {
                        getCommentList(1);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        //回复
        $(document).on('click', '#replyBtn', function () {
            var $this = $(this);
            var commentId = $this.closest('td').data('comment_id');
            $("#replyBox").html(template("replyBox_tem", {commentId: commentId}));
            replyContentBoxShowORHide('show');

        });

        //保存回复
        $(document).on('click', '#saveReplyContentBtn', function () {
            var $this = $(this);
            var commentId = $this.data('comment_id');
            var content = $('#replyContent').val();
            if (content == '') {
                alert('回复内容不能为空');
                return false;
            }

            $.post("replycomment.vpage", {commentId: commentId, replyComment: content}, function (data) {
                if (data.success) {
                    getCommentList(1);
                    replyContentBoxShowORHide('hide');
                } else {
                    alert(data.info);
                }
            });
        });

    });
</script>
</@layout_default.page>