<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-评论筛选' page_num=13 jqueryVersion ="1.7.2">
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>

<div class="span9">
    <fieldset>
        <div>
            <span>
                评论状态：<select id="status">
                            <option value="pending">待处理</option>
                            <option value="spam">垃圾评论</option>
                            <option value="show">展示</option>
                            <option value="hide">隐藏</option>
                            <option value="all">全部</option>
                        </select>
            </span>
            <span>
                评论时间：<input id="startTime" name="startTime" type="text" size="16" class="input-xlarge form_datetime" placeholder="最早" value="${startTime!''}">至
                     <input id="endTime" name="endTime" type="text" size="16" class="input-xlarge form_datetime" placeholder="最晚">
            </span>
            <span>
                字数：<input id="wordLength" name="wordLength" type="text" size="4"  placeholder="10">
            </span>
        </div>
        <div>
            <span>
                用户名：<input id="userName" name="userName" type="text" size="4" placeholder="输入用户名">
            </span>
            <span>
                用户id：<input id="userId" name="userId" type="text" size="4" placeholder="输入用户id">
            </span>
            <span>
                文章标题：<input id="articleTitle" name="articleTitle" type="text" placeholder="输入文章标题">
            </span>
        </div>
        <div>
            <button class="btn btn-primary" id="searchBtn">查询</button>
            <button class="btn btn-danger hideBtn" id="hideBtn" style="float: right">隐藏当前页面所有评论</button>
        </div>

        <div id="commentBox">
        </div>
        <div><button class="btn btn-danger hideBtn" id="hideBtn2" style="float: right">隐藏当前页面所有评论</button></div>
        <div class="message_page_list"></div>
        <div style="display: block;"><span id="totalCount" style="float: right"></span></div>

    </fieldset>
</div>

<div class="modal fade" id="replyContentBox">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">评论回复  <font color="red">回复内容提交后不可修改！！</font></h4>
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
            <th width="3%">序号</th>
            <th width="8%">评论时间</th>
            <th width="8%">处理时间</th>
            <th width="20%">评论</th>
            <th width="15%">文章标题</th>
            <th width="8%">用户名</th>
            <th width="6%">用户ID</th>
            <th width="5%">展示评论数</th>
            <th>用户头像</th>
            <th>回复内容</th>
            <th width="8%">操作</th>
        </tr>
        <%if(content.length > 0){%>
        <%for(var i = 0; i < content.length;i++){%>
        <tr>
            <td><%=i+1%></td>
            <td><%=content[i].createTime%></td>
            <td><%=content[i].updateTime%></td>
            <td><%=content[i].comment%></td>
            <td><%=content[i].articleTitle%></td>
            <td><%=content[i].userName%></td>
            <td><%=content[i].userId%></td>
            <td><%=content[i].showCommentCount%></td>
            <td><img src = "<%=content[i].avatar%>" style="width: 100px;height: 100px" /></td>
            <td><%=content[i].replyComment%></td>
            <td data-comment_id="<%=content[i].commentId%>" class="hand">
                <%if(content[i].isShow){%>
                <button class="btn btn-danger" id="cancelShowBtn">取消展示评论</button>
                <%}else{%>
                <button class="btn btn-primary" id="showBtn">展示评论</button>
                <%}%>
                <%if(content[i].replyIsShow){%>
                <button class="btn btn-danger" id="cancelShowReplyBtn">取消展示回复</button>
                <%}else if(content[i].replyComment){%>
                <button class="btn btn-primary" id="showReplyBtn">展示回复</button>
                <%}%>
                <%if(!content[i].replyComment) {%>
                <button class="btn btn-success" id="replyBtn">回复</button>
                <%}%>
            </td>
        </tr>
        <%}%>
        <%}else{%>
        <tr>
            <td colspan="11">暂无数据</td>
        </tr>
        <%}%>
    </table>
</script>

<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<script type="text/javascript">
    $(".form_datetime").datetimepicker({
        autoclose: true,
        minuteStep: 5,
        format: 'yyyy-mm-dd hh:ii:ss'
    });

    var status = 'pending';
    var startTime = $('#startTime').val();
    var endTime = '';
    var wordLength = 15;
    var userName = '';
    var userId = 0;
    var articleTitle = '';

    var _currentPage = 1;

    function getCommentList(page) {
        var postData = {
            currentPage: page,
            status: status,
            startTime: startTime,
            endTime: endTime,
            wordLength: wordLength,
            userName: userName,
            userId: userId,
            articleTitle: articleTitle
        };

        console.info(postData);

        $.post('getcommentpicklist.vpage', postData, function (data) {
            if (data.success) {
                $('#commentBox').html(template("commentBox_tem", {
                    content: data.commentList,
                    current: data.currentPage
                }));

                $('#totalCount').html("总共" + data.commentCount + "条记录");

                $(".message_page_list").page({
                    total: data.totalPage,
                    current: data.currentPage,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        _currentPage=index;
                        getCommentList(index);
                    }
                });
            } else {
                alert(data.info);
            }
        });
    }

    function replyContentBoxShowORHide(sh) {
        $('#replyContentBox').modal(sh);
    }

    $(function(){
        $.getUrlParam = function(name)
        {
            var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r!=null) return unescape(r[2]); return null;
        };

        _currentPage= $.getUrlParam('currentPage');
        getCommentList(_currentPage);

        $('#status').click( function () {
            if ($('#status').val() != "pending") {
                $('.hideBtn').hide();
            } else {
                $('.hideBtn').show();
            }
        });

        if ($('#status').val() != "pending") {
            $('.hideBtn').hide();
        } else {
            $('.hideBtn').show();
        }

        $(document).on('click', '#searchBtn', function () {
            status = $('#status').val().trim();
            startTime = $('#startTime').val().trim();
            endTime = $('#endTime').val().trim();
            wordLength = $('#wordLength').val().trim();
            userName = $('#userName').val().trim();
            userId = $('#userId').val().trim();
            articleTitle = $('#articleTitle').val().trim();
            _currentPage = 1;
            getCommentList(_currentPage);
        });

        //取消展示评论
        $(document).on('click', '#cancelShowBtn', function () {
            var $this = $(this);
            var commentId = $this.closest('td').data('comment_id');
            $.post("offlinecomment.vpage", {commentId: commentId}, function (data) {
                if (data.success) {
                    if ($('#status').val().trim() == "pending") {
                        $this.attr("class", "btn btn-primary");
                        $this.attr("id", "showBtn");
                        $this.text("展示评论");
                        $this.parent().attr("class", "hand");
                    } else {
                        getCommentList(1);
                    }
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
                    //产品的变态需求。待处理列表展示评论后不刷新。。有可能要把剩下的评论给隐藏掉，所以只改变按钮状态，等操作人手动刷新再获取新的列表
                    if ($('#status').val().trim() == "pending") {
                        $this.attr("class", "btn btn-danger");
                        $this.attr("id", "cancelShowBtn");
                        $this.text("取消展示评论");
                        $this.parent().attr("class", "nothand");
                    } else {
                        getCommentList(1);
                    }

                } else {
                    alert(data.info);
                }
            });
        });

        //展示回复
        $(document).on('click', '#showReplyBtn', function () {
           var $this = $(this);
           var commentId = $this.closest('td').data('comment_id');
           $.post("onliecommentreply.vpage", {commentId: commentId}, function (data) {
              if (data.success) {
                  if ($('#status').val().trim() == "pending") {
                      $this.attr("class", "btn btn-danger");
                      $this.attr("id", "cancelShowReplyBtn");
                      $this.text("取消展示回复");
                  } else {
                      getCommentList(1);
                  }
              } else {
                  alert(data.info);
              }
           });
        });

        //取消展示回复
        $(document).on('click', '#cancelShowReplyBtn', function () {
            var $this = $(this);
            var commentId = $this.closest('td').data('comment_id');
            $.post("offlinecommentreply.vpage", {commentId: commentId}, function (data) {
                if (data.success) {
                    if ($('#status').val().trim() == "pending") {
                        $this.attr("class", "btn btn-primary");
                        $this.attr("id", "showReplyBtn");
                        $this.text("展示回复");
                    } else {
                        getCommentList(1);
                    }
                } else {
                    alert(data.info);
                }
            });
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
                    $('tr').each(function () {
                        if ($(this).children().last().data('comment_id') == commentId) {
                            $(this).children().last().prev().text(content);
                            $(this).children().last().append('<button class=' + '"btn btn-primary"' + 'id=' + '"showReplyBtn"' + '>展示回复</button>');
                            $(this).children().last().find('#replyBtn').remove();
                        }
                    });
                    replyContentBoxShowORHide('hide');
                } else {
                    alert(data.info);
                }
            });
        });

        $(document).on('click', '.hideBtn', function () {
            var commentIds = [];
            $('.hand').each(function () {
                commentIds.push($(this).data("comment_id"));
            })
            console.info(commentIds);

            $.post("hidecomments.vpage", {commentIdsStr: commentIds.toString()}, function (data) {
                if (data.success) {
                    getCommentList(1)
                } else {
                    alert(data.info);
                }
            });

        });
    })
</script>

</@layout_default.page>