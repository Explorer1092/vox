<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-内容发布' page_num=13>
<div class="span9">
    <fieldset>
        <legend>内容发布</legend>
    </fieldset>

    <fieldset>
        <div id="articleBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>

<script type="text/html" id="articleBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>newsId</th>
            <th>标题</th>
            <th>发布时间</th>
            <th>阅读数</th>
            <th>有帮助数</th>
            <th>没帮助数</th>
            <th>收藏数</th>
            <th>评论点赞数</th>
            <th>评论数</th>
            <th>分享数</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
            <%for(var i = 0; i < content.length;i++){%>
                <tr>
                    <td style="cursor: pointer;" class="coryBtn" data-clipboard-text="<%=content[i].newsId%>"><%=content[i].newsId%></td>
                    <td><%=content[i].title%></td>
                    <td><%=content[i].createDate%></td>
                    <td><%=content[i].readCount%></td>
                    <td><%=content[i].voteCount%></td>
                    <td><%=content[i].unHelpCount%></td>
                    <td><%=content[i].collectCount%></td>
                    <td><%=content[i].commentVoteCount%></td>
                    <td><%=content[i].commentCount%></td>
                    <td><%=content[i].shareCount%></td>
                    <td data-news_id="<%=content[i].newsId%>">
                        <a class="btn btn-primary" href="jxtnewssubjectedit.vpage?newsId=<%=content[i].newsId%>&currentPage=<%=current%>">编辑</a>
                        <%if(content[i].isOnline){%>
                        <button class="btn btn-danger" id="offLineBtn">下线</button>
                        <%}else{%>
                        <button class="btn btn-success" id="onLineBtn">上线</button>
                        <%}%>
                        <a class="btn btn-info" href="commentlist.vpage?newsId=<%=content[i].newsId%>">查看评论</a>
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
    var currentSearchVal = '';
    var _currentPage=1;
    function getArticleList(page) {
        var postData = {
            currentPage: page
        };
        if(currentSearchVal != ''){
            postData.source = currentSearchVal;
        }

        $.post('getjxtnewssubjectList.vpage', postData, function (data) {
            if (data.success) {
                $('#articleBox').html(template("articleBox_tem", {
                    content: data.jxtNewsList,
                    current: data.currentPage
                }));

                $(".message_page_list").page({
                    total: data.totalPage,
                    current: data.currentPage,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        _currentPage=index;
                        getArticleList(index);
                    }
                });
            }
        });
    }

    $(function () {

        $.getUrlParam = function(name)
        {
            var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r!=null) return unescape(r[2]); return null;
        };

        _currentPage= $.getUrlParam('currentPage');
        if(_currentPage!=null){
            getArticleList(_currentPage);
        }else{
            getArticleList(1);
        }


        $(document).on('click', '#onLineBtn', function () {
            var $this = $(this);
            var newsId = $this.closest('td').data('news_id');
            if(confirm("确定上线？")){
                $.post("jxtnewsonline.vpage", {newsId: newsId}, function (data) {
                    if (data.success) {
                        getArticleList(_currentPage);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $(document).on('click', '#offLineBtn', function () {
            var $this = $(this);
            var newsId = $this.closest('td').data('news_id');
            if(confirm("确定下线？")){
                $.post("jxtnewsoffline.vpage", {newsId: newsId}, function (data) {
                    if (data.success) {
                        getArticleList(_currentPage);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        //标题筛选
        $(document).on('click',"#searchBtn",function(){
            currentSearchVal = $("#searchVal").val();
            getArticleList(1);
        });

        var clipboard = new Clipboard('.coryBtn');
        clipboard.on('success', function (e) {
            alert("复制成功: " + e.text);
        });
        clipboard.on('error', function (e) {
            alert("复制失败，请手动复制");
        });


    });
</script>
</@layout_default.page>