<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-评论混排' page_num=13>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <div style="position: relative; height: 80px;widows: 100%;z-index: 100">
        <div style="position: fixed;background-color:yellow;">
            最早：<input id="startTime" type="text" class="input-xlarge" placeholder="可空"
                      data-bind="value:filterStartTime">
            最晚：<input id="endTime" type="text" class="input-xlarge" placeholder="可空"
                      data-bind="value:filterEndTime">
            <button class="btn btn-info" data-bind="click:refreshCurrent" style="float:right">重载数据</button>
            <br>
            评论字数过滤:<input type="text" data-bind="value:filterWordLength" class="input input-small">
            <span style="float:right"><button class="btn btn-default" data-bind="click:batchShowComments,disable:checkedComments().length==0">批量展示</button></span>
            <span style="float:right">已展示：<input type="checkbox" id="filter_edited"
                                                 data-bind="checked:isShow,event:{ change: isShowChanged}">&nbsp;&nbsp;|&nbsp;&nbsp;</span>
            <span>当前有<span data-bind="text:comments().length"></span>条评论</span>
        </div>
    </div>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th>序号</th>
            <th style="width:22%">评论</th>
            <th>评论时间</th>
            <th>更新时间</th>
            <th style="width: 15%">文章标题</th>
            <th>用户名</th>
            <th>用户ID</th>
            <th>用户头像</th>
            <th style="width: 10%;">回复内容</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:pagedComments">
        <tr>
            <td>
                <span data-bind="text:$index()+1"></span><br>
                <input type="checkbox" data-bind="value:$data.commentId,checked:$parent.checkedComments,visible:!$data.isShow()"></input>
            </td>
            <td data-bind="text:$data.comment"></td>
            <td data-bind="text:$data.createTime"></td>
            <td data-bind="text:$data.updateTime"></td>
            <td data-bind="text:$data.articleTitle"></td>
            <td data-bind="text:$data.userName"></td>
            <td data-bind="text:$data.userId"></td>
            <td><img data-bind="attr:{'src':$data.avatar}" style="width: 100px;height: 100px"/></td>
            <td data-bind="text:$data.replyComment"></td>
            <td>
                <button class="btn btn-danger" data-bind="visible:!$data.isShow(),click:$parent.showComment">
                    展示评论
                </button>
                <button class="btn btn-danger" data-bind="visible:$data.isShow(),click:$parent.hideComment">
                    取消展示评论
                </button>
                <!--ko if: $data.replyComment-->
                <button class="btn btn-danger" data-bind="visible:!$data.replyIsShow(),click:$parent.showReply">展示回复
                </button>
                <button class="btn btn-danger" data-bind="visible:$data.replyIsShow(),click:$parent.hideReply">取消展示回复
                </button>
                <!-- /ko -->
                <!--ko ifnot:$data.replyComment-->
                <button class="btn btn-danger" data-bind="click:$parent.showReplyModal">回复</button>
                <!--/ko-->
            </td>
        </tr>
        </tbody>
    </table>
    <div class="message_page_list"></div>
</div>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在查询，请等待……</p>
</div>
<div class="modal fade" id="replyContentBox">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">评论回复</h4>
            </div>
            <div id="replyBox">

            </div>

        </div>
    </div>
</div>
<script type="text/html" id="replyBox_tem">
    <div class="modal-body">
        <label for="replyContent"></label><textarea id="replyContent" style="width: 97%" placeholder="请输入回复内容[1000字内]"
                                                    maxlength="1000"></textarea>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
        <button type="button" class="btn btn-primary" id="saveReplyContentBtn" data-comment_id="<%=commentId%>">保存
        </button>
    </div>
</script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<script>
    var defaultStartTime = "${startTime}";
    var defaultEndTime = "${endTime}";
    console.info(defaultStartTime + "," + defaultEndTime);
    $('#startTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        defaultDate: defaultStartTime
    });
    $('#endTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        defaultDate: defaultEndTime
    });
    $("#startTime").datepicker("setDate", defaultStartTime);
    $("#endTime").datepicker("setDate", defaultEndTime);
    // modal
    function replyContentBoxShowORHide(sh) {
        $('#replyContentBox').modal(sh);
    }
    // view model
    function CommentViewModel(articleTitle, avatar, comment, commentId, createTime, isShow, replyComment, replyIsShow, userId, userName) {
        this.articleTitle = ko.observable(articleTitle);
        this.avatar = ko.observable(avatar);
        this.comment = ko.observable(comment);
        this.commentId = ko.observable(commentId);
        this.createTime = ko.observable(createTime);
        this.isShow = ko.observable(isShow);
        this.replyComment = ko.observable(replyComment);
        this.replyIsShow = ko.observable(replyIsShow);
        this.userId = ko.observable(userId);
        this.userName = ko.observable(userName);
    }
    function CommentsViewModel(allComments) {
        // 当前页
        this.currentPage = ko.observable(0);
        // 页大小
        this.pageSize = ko.observable(300);
        // 批量展示选中的评论
        this.checkedComments=ko.observableArray([]);
        // 字数过滤器
        this.filterWordLength = ko.observable(20);
        // 时间头
        this.filterStartTime = ko.observable(defaultStartTime);
        this.filterEndTime = ko.observable("");
        // 时间尾
        // 评论列表
        this.allComments = ko.observableArray([]);

        this.comments = ko.computed(function () {
            console.info("compute qualified comments");
            var len = this.filterWordLength();
            var comments = this.allComments();
            if (len > 0) {
                return ko.utils.arrayFilter(comments, function (item) {
                    return item.comment().length >= len;
                });
            } else {
                return comments;
            }
        }.bind(this));
        this.comments.subscribe(function (comments) {
            console.info("subscribe comments,comments length " + comments.length);
            this.showPage(0);
        }.bind(this));
        this.pagedComments = ko.computed(function () {
            console.info("compute page comments");
            var pageSize = this.pageSize();
            var comments = this.comments();
            var currentPage = this.currentPage();
            return comments.slice(currentPage * pageSize, (currentPage + 1) * pageSize);
        }.bind(this));
        // 评论已展示或者未展示
        this.isShow = ko.observable(false);
        this.isShowChanged = function () {
            this.currentPage(0);
            console.info("isShowChanged,reload page again");
            this.loadComments();
        };
        this.batchShowComments = function () {
            console.info("batch show comments");
            var checkedComments=this.checkedComments();
            console.info(checkedComments);
            if(!confirm("确定批量展示这些品论？")){
                return false;
            }
            $.post("batchshowcomments.vpage",{"comments":checkedComments.join(",")},function(data){
                console.info(data);
                if(data.success){
                    this.allComments.remove(function (item) {
                        return checkedComments.indexOf(item.commentId())!=-1;
                    })
                }else{
                    console.info("展示失败");
                }
            }.bind(this))
        }.bind(this);
        this.showComment = function (comment) {
            console.info("showComment " + comment.commentId());
            var commentId = comment.commentId();
            if (confirm("确定展示评论？")) {
                $.post("onliecomment.vpage", {commentId: commentId}, function (data) {
                    if (data.success) {
                        this.allComments.remove(comment);
                    } else {
                        alert(data.info);
                    }
                }.bind(this));
            }
        }.bind(this);
        this.hideComment = function (comment) {
            console.info("hideComment " + comment.commentId());
            var commentId = comment.commentId();
            if (confirm("确定取消展示评论？")) {
                $.post("offlinecomment.vpage", {commentId: commentId}, function (data) {
                    if (data.success) {
//                        this.loadComments(this.currentPage());
                        this.allComments.remove(comment);
                    } else {
                        alert(data.info);
                    }
                }.bind(this));
            }
        }.bind(this);
        this.showReply = function (comment) {
            console.info("showReply " + comment.commentId());
            var commentId = comment.commentId();
            if (confirm("确定展示回复？")) {
                $.post("onliecommentreply.vpage", {commentId: commentId}, function (data) {
                    if (data.success) {
                        comment.replyIsShow(true);
                    } else {
                        alert(data.info);
                    }
                }.bind(this));
            }
        }.bind(this);
        this.hideReply = function (comment) {
            console.info("hideReply " + comment.commentId());
            var commentId = comment.commentId();
            if (confirm("确定取消回复？")) {
                $.post("offlinecommentreply.vpage", {commentId: commentId}, function (data2) {
                    if (data2.success) {
                        comment.replyIsShow(false);
                    } else {
                        alert(data2.info);
                    }
                }.bind(this));
            }
        }.bind(this);
        this.showReplyModal = function (data) {
            console.info("showReplyModal " + data.commentId());
            var commentId = data.commentId();
            $("#replyBox").html(template("replyBox_tem", {commentId: commentId}));
            replyContentBoxShowORHide('show');
        };
        this.refreshCurrent = function () {
            this.currentPage(0);
            this.loadComments();
        }.bind(this);
        this.loadComments = function () {
            var startTimeLoad = new Date();
            $("#loadingDiv").show();
            $.get("/advisory/getrecentcomments.vpage", {
                isShow: this.isShow(),
                startTime: this.filterStartTime(),
                endTime: this.filterEndTime()
            }, function (data) {
                console.info(data);
                var endTimeLoad = new Date();
                var elapse = endTimeLoad - startTimeLoad;
                console.info("loading elapse " + elapse + " microseconds");
                $("#loadingDiv").hide();
                if (data.success) {
                    this.allComments(ko.mapping.fromJS(data.allComments)().slice(0));
                } else {
                    alert(data.info);
                }
            }.bind(this))
        }.bind(this);
        // pageNo start from 0
        this.showPage = function (pageNo) {
            this.currentPage(pageNo);
            var length = this.comments().length;
            var pageSize = this.pageSize();
            var d = (length - length % pageSize) / pageSize;
            var total = length % pageSize == 0 ? d : d + 1;
            $(".message_page_list").page({
                total: total,
                current: pageNo + 1,
                autoBackToTop: false,
                jumpCallBack: function (index) {
                    this.showPage(index - 1);
                }.bind(this)

            });
        }.bind(this);
        this.generate_time_str = function (timestamp) {
            var a = new Date(timestamp);
            var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
            var year = a.getFullYear();
            var month = a.getMonth() + 1;
            var date = a.getDate();
            var hour = a.getHours();
            var min = a.getMinutes();
            var sec = a.getSeconds();
            var time = year + '-' + month + '-' + date + ' ' + hour + ':' + min + ':' + sec
            return time;
        };
        this.init = function () {
            // 一次将最近所有评论全部加载完成
            this.loadComments();
        }.bind(this);
    }
    viewModel = new CommentsViewModel();
    // binding
    viewModel.init();
    ko.applyBindings(viewModel);
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
                var theComment = ko.utils.arrayFirst(viewModel.comments(), function (item) {
                    return item.commentId() == commentId;
                });
                theComment.replyComment(content);
                replyContentBoxShowORHide('hide');
            } else {
                alert(data.info);
            }
        });
    });
</script>
</@layout_default.page>