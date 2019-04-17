<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-外部文章管理' page_num=13>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <fieldset>
        <legend>外部文章管理</legend>
    </fieldset>

    <fieldset>
        <div>
            <span>
                当前审核状态：<select id="filter_status">
                <option value="lv1">待审核</option>
                <option value="">全部</option>
                <option value="processed">通过</option>
                <option value="rejected">驳回</option>
            </select>
            </span>
            <span>
                审核类型:<select id="filter_style_type" style="width:120px;">
                <option value="EXTERNAL_ALBUM_NEWS">外部专辑文章</option>
                <option value="">全部</option>
            </select>
            </span>
            <br>
            <span>
                提交时间：<input id="startTime" name="sendTime" type="text" class="input-xlarge" placeholder="最早">至
                                <input id="endTime" name="sendTime" type="text" class="input-xlarge" placeholder="最晚">
            </span>
            <button class="btn btn-primary" id="searchBtn">查询</button>
        </div>
        <div id="articleBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>
<script type="text/html" id="articleBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>审核状态</th>
            <th>文章标题</th>
            <th>创建人</th>
            <th>提交时间</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
        <%for(var i = 0; i < content.length;i++){%>
        <tr>
            <td><%=content[i].checkType%></td>
            <td><%=content[i].title%></td>
            <td><%=content[i].commitUser%></td>
            <td><%=content[i].updateTime%></td>
            <td data-news_id="<%=content[i].newsId%>">
                <%if(content[i].checkType=="待审核"){%>
                <a class="btn btn-primary"
                   href="getCheckNewsDetail.vpage?newsId=<%=content[i].newsId%>&currentPage=<%=current%>"
                   target="_blank">审核</a>
                <%}%>
                <%if(content[i].isOnline&&content[i].checkType=="通过"){%>
                <button class="btn btn-danger" id="offLineBtn">下线</button>
                <a class="btn btn-primary"
                   href="getCheckNewsDetail.vpage?newsId=<%=content[i].newsId%>&currentPage=<%=current%>"
                   target="_blank">查看</a>
                <%}else if(!content[i].isOnline&&content[i].checkType=="通过"){%>
                <button class="btn btn-success" id="onLineBtn">上线</button>
                <a class="btn btn-primary"
                   href="getCheckNewsDetail.vpage?newsId=<%=content[i].newsId%>&currentPage=<%=current%>"
                   target="_blank">查看</a>
                <%}else if(content[i].checkType=="驳回"){%>
                <a class="btn btn-primary"
                   href="getCheckNewsDetail.vpage?newsId=<%=content[i].newsId%>&currentPage=<%=current%>"
                   target="_blank">查看</a>
                <%}%>
            </td>
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
    $('#startTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss'
    });
    $('#endTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss'
    });

    var currentSearchStatus = "lv1";
    var currentSearchStyleType = "EXTERNAL_ALBUM_NEWS";
    var currentSearchStartTime = "";
    var currentSearchEndTime = "";

    var _currentPage = 1;

    var selectMap = {label: [], category: []};
    function getArticleList(page) {
        var postData = {
            currentPage: page,
            status: currentSearchStatus,
            styleType: currentSearchStyleType,
            startTime: currentSearchStartTime,
            endTime: currentSearchEndTime

        };
        console.info(postData);

        $.post('getCheckNewsList.vpage', postData, function (data) {
            if (data.success) {
                console.info(data.jxtNewsList);
                $('#articleBox').html(template("articleBox_tem", {
                    content: data.jxtNewsList,
                    current: data.currentPage
                }));

                $(".message_page_list").page({
                    total: data.totalPage,
                    current: data.currentPage,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        _currentPage = index;
                        getArticleList(index);
                    }
                });
            }
        });
    }

    $(function () {
        $(".form_datetime").datetimepicker({
            autoclose: true,
            startDate: "${.now?string('yyyy-MM-dd HH:mm:ss')}",
            minuteStep: 5,
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        };

        _currentPage = $.getUrlParam('currentPage');
        getArticleList(_currentPage);


        $(document).on('click', '#onLineBtn', function () {
            var $this = $(this);
            var newsId = $this.closest('td').data('news_id');
            if (confirm("确定上线？")) {
                $.post("/advisory/jxtnewsonline.vpage", {newsId: newsId}, function (data) {
                    if (data.success) {
                        console.info(_currentPage);
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
            if (confirm("确定下线？")) {
                $.post("/advisory/jxtnewsoffline.vpage", {newsId: newsId}, function (data) {
                    if (data.success) {
                        getArticleList(_currentPage);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
        //标题筛选
        $(document).on('click', "#searchBtn", function () {
            currentSearchStatus = $("#filter_status").val().trim();
            currentSearchStyleType = $("#filter_style_type").val().trim();
            currentSearchStartTime = $("#startTime").val().trim();
            currentSearchEndTime = $("#endTime").val().trim();
            _currentPage = 1;
            getArticleList(_currentPage);
        });


        $("#labelText").on("click", function () {
            var $this = $(this);
            var tags = $this.data("ids");
            if (!!tags) {
                if (tags.toString().indexOf(',') != -1) {
                    selectMap.label = tags.split(',');
                } else {
                    selectMap.label = [tags];
                }
            }
            $.get("gettaglist.vpage", function (data) {
                if (data.success) {
                    $("#modalBox").empty().html(template("modalBox_tem", {
                        tagList: data.tagList,
                        type: "label",
                        selectMap: selectMap
                    }));
                    $('#myModal').modal('show');
                }
            });
        });

        $(document).on('click', '.selectLabelOrCategory', function () {
            var $this = $(this);
//            $(".selectLabelOrCategory").removeClass("btn-success");
            $this.toggleClass('btn-success');
        });

    });
</script>
</@layout_default.page>