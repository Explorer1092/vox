<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-专题发布' page_num=13>
<div class="span9">
    <fieldset>
        <legend>专题管理</legend>
        <div style="padding-bottom: 10px">
            <a class="btn btn-primary" href="${requestContext.webAppContextPath}/advisory/subjectedit.vpage" id="contentCreateBtn">新建专题</a>
            <span style="float: right">已发布<input type="checkbox" id="filter_published"></span>
        </div>
    </fieldset>

    <fieldset>
        <div id="subjectManageBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>

<script type="text/html" id="subjectBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>subjectId</th>
            <th>专题名</th>
            <th>编辑者</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
            <%for(var i = 0; i < content.length;i++){%>
                <tr>
                    <td style="cursor: pointer;" class="coryBtn" data-clipboard-text="<%=content[i].subjectId%>"><%=content[i].subjectId%></td>
                    <td><%=content[i].title%></td>
                    <td><%=content[i].editor%></td>
                    <td data-subject_id="<%=content[i].subjectId%>">
                        <%if(content[i].published){%>
                        <a class="btn btn-primary" href="/advisory/jxtnewssubjectedit.vpage?newsId=<%=content[i].newsId%>">已发布</a>
                        <%}else{%>
                        <a class="btn btn-primary" href="/advisory/jxtnewssubjectedit.vpage?subjectId=<%=content[i].subjectId%>">发布编辑</a>
                        <%}%>
                        <button class="btn btn-danger delete">删除</button>
                        <a href="/advisory/subjectedit.vpage?subjectId=<%=content[i].subjectId%>"  class="btn btn-success">编辑</a>
                    </td>
                </tr>
            <%}%>
        <%}else{%>
            <tr>
                <td colspan="9">暂无数据</td>
            </tr>
        <%}%>
    </table>
</script>

<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<script type="text/javascript">
    var currentSearchVal = '';
    var _currentPage=1;
    var published = false;
    function getSubjectList(page) {
        var postData = {
            currentPage: page,
            published: published
        };
        if(currentSearchVal != ''){
            postData.source = currentSearchVal;
        }

        $.post('getSubjectList.vpage', postData, function (data) {
            if (data.success) {
                $('#subjectManageBox').html(template("subjectBox_tem", {
                    content: data.subjectList,
                    current: data.currentPage
                }));

                $(".message_page_list").page({
                    total: data.totalPage,
                    current: data.currentPage,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        _currentPage=index;
                        getSubjectList(index);
                    }
                });
            }
        });
    }

    $(function () {

        $("#filter_published").on("click",function(){
            published=!published;
            getSubjectList(1);
        });

        $.getUrlParam = function(name)
        {
            var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r!=null) return unescape(r[2]); return null;
        };

        _currentPage= $.getUrlParam('currentPage');
        if(_currentPage!=null){
            getSubjectList(currentPage);
        }else{
            getSubjectList(1);
        }

        $(document).on("click",".delete", function () {
            var subjectId = $(this).parent().data("subject_id");
            alert(subjectId);
            if(confirm("你确定删除吗？")){
                $.post("deletesubject.vpage", {subjectId: subjectId}, function (data) {
                    if (data.success) {
                        getSubjectList(_currentPage);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $(document).on('click', '#onLineBtn', function () {
            var $this = $(this);
            var newsId = $this.closest('td').data('subjectId');
            if(confirm("确定上线？")){
                $.post("subjectonline.vpage", {newsId: newsId}, function (data) {
                    if (data.success) {
                        getSubjectList(_currentPage);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $(document).on('click', '#offLineBtn', function () {
            var $this = $(this);
            var newsId = $this.closest('td').data('subjectId');
            if(confirm("确定下线？")){
                $.post("subjectoffline.vpage", {newsId: newsId}, function (data) {
                    if (data.success) {
                        getSubjectList(_currentPage);
                    } else {
                        alert(data.info);
                    }
                });
            }
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