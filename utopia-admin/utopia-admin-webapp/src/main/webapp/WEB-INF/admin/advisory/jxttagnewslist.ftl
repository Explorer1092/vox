<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-查询tag下文章的数量' page_num=13>
<div class="span9">
    <fieldset>
        <legend>tag文章的数量</legend>
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
            <th>tagId</th>
            <th>tag名称</th>
            <th>文章数量</th>

        </tr>
        <%if(content.length > 0){%>
            <%for(var i = 0; i < content.length;i++){%>
                <tr>
                    <td><%=content[i].tagId%></td>
                    <td><%=content[i].tagName%></td>
                    <td><%=content[i].tagNewsNum%></td>
                </tr>
            <%}%>
        <%}else{%>
            <tr>
                <td colspan="8">暂无数据</td>
            </tr>
        <%}%>
    </table>
</script>

<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<script type="text/javascript">
    var _currentPage=1;
    function getArticleList(page) {
        var postData = {
            currentPage: page
        };
        $.post('loadNewsCountByTag.vpage', postData, function (data) {
            if (data.success) {
                $('#articleBox').html(template("articleBox_tem", {
                    content: data.tagNewsCount,
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
        getArticleList(1);
    });
</script>
</@layout_default.page>