<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-文章统计' page_num=13>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <fieldset>
        <legend>文章统计</legend>
    </fieldset>

    <fieldset>
        <div>
            <span>
                文章状态：<select id="filter_status">
                <option value="all">全部</option>
                <option value="offline">未上线</option>
                <option value="online">已上线</option>
            </select>
            </span>
            <span>
                是否置顶：<select id="filter_top">
                <option value="all">全部</option>
                <option value="true">是</option>
                <option value="false">否</option>
            </select>
            </span>
            <span>
                内容样式:<select id="filter_style_type" style="width:120px;">
                <option value="">全部</option>
                <option value="NEWS">资讯</option>
                <option value="EXTERNAL_ALBUM_NEWS">外部专辑文章</option>
                <option value="KOL_RECOMMEND_NEWS">KOL精选</option>
                <option value="KOL_VOLUNTEER_NEWS">KOL志愿者风采</option>
                <option value="PAID_NEWS">付费资讯</option>
            </select>
            </span>
            <span>
                文章类型：<select id="filter_content_type">
                <option value="">全部</option>
                <option value="IMG_AND_TEXT">图文</option>
                <option value="VIDEO">视频</option>
                <option value="AUDIO">音频</option>
            </select>
            </span>
            <span>
                标签：<input type="text" id="labelText" value="" placeholder="全部">
                <input type="hidden" id="labelValue" value="" placeholder="全部">
            </span>
            <br>
            <span>
                标题：<input type="text" placeholder="输入标题" id="searchTitle">
                文章源：<input type="text" placeholder="输入来源" id="searchSource">
                发布人：<input type="text" placeholder="输入发布人crm用户名" id="searchPushUser">
                <br>
                上线时间：<input id="startTime" name="sendTime" type="text" class="input-xlarge" placeholder="最早">至
                                <input id="endTime" name="sendTime" type="text" class="input-xlarge" placeholder="最晚">
            </span>
            <br>
            文章id:<input type="text" id="filterId">
            <br>
            <button class="btn btn-primary" id="searchBtn">查询</button>
            <button class="btn btn-primary" id="resetFilters">重置查询条件</button>
            <button class="btn btn-primary" id="timerSubmitNews">定时发布</button>
        </div>
        <div id="articleBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>
<div class="modal hide fade" id="myTimerModal">
    <div class="modal-header">
        <button class="close" type="button" data-dismiss="modal">×</button>
        <h3 id="myModalLabel">定时发布</h3>
    </div>
    <div class="modal-body">
        <div id="submitReview">
            <span>将要对这些文章执行定时发布操作</span>
            <table class="table table-bordered" id="resultReview">
                <tr>
                    <th>newsId</th>
                    <th>文章标题</th>
                </tr>
            </table>
        </div>
        <div class="input-append date form_datetime">
            <input id="form_datetime" size="16" type="text" readonly>
            <span class="add-on"><i class="icon-remove"></i></span>
            <span class="add-on"><i class="icon-th"></i></span>
        </div>
        <div>
            <button class="btn btn-primary" id="submitTimer">确定</button>
            <button class="btn btn-danger" data-dismiss="modal">取消</button>
        </div>
    </div>
</div>

<script type="text/html" id="articleBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>标题</th>
            <th>状态</th>
            <th>上线时间</th>
            <th>阅读数</th>
            <th>有帮助数</th>
            <th>没帮助数</th>
            <th>收藏数</th>
            <th>评论点赞数</th>
            <th>评论数</th>
            <th>分享数</th>
            <th>字数</th>
            <th>来源</th>
            <th>内容类型</th>
            <th>发布人</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
        <%for(var i = 0; i < content.length;i++){%>
        <tr>
            <td><%=content[i].title%></td>
            <%if(content[i].isOnline){%>
            <td>已上线</td>
            <%}else{%>
            <td>未上线</td>
            <%}%>
            <td><%=content[i].pushDate%></td>
            <td><%=content[i].readCount%></td>
            <td><%=content[i].voteCount%></td>
            <td><%=content[i].unhelpCount%></td>
            <td><%=content[i].collectCount%></td>
            <td><%=content[i].commentVoteCount%></td>
            <td><%=content[i].commentCount%></td>
            <td><%=content[i].shareCount%></td>
            <td><%=content[i].wordsCount%></td>
            <td><%=content[i].source%></td>
            <td><%=content[i].contentType%></td>
            <td><%=content[i].pushUser%></td>
            <td data-news_id="<%=content[i].newsId%>">
                <a class="btn btn-info" href="commentlist.vpage?newsId=<%=content[i].newsId%>" target="_blank">查看评论</a>
                <button class="btn btn-primary coryBtn" style="cursor: pointer;"
                        data-clipboard-text="<%=content[i].newsId%>">复制ID
                </button>
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

<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div id="modalBox"></div>

        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<script type="text/html" id="modalBox_tem">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
            &times;
        </button>
        <h4 class="modal-title">
            选择
            <%if(type == 'label'){ %>
            标签
            <% } else { %>
            内容类别
            <% } %>
        </h4>
    </div>

    <div class="modal-body">
        <div class="control-group">
            <div class="controls" style="">
                <label for="title" style="line-height: 32px;">
                    <%for(var i = 0; i < tagList.length; i++) {%>
                    <%for(var j in tagList[i]) {%>
                    <%if(selectMap[type].length > 0){%>
                    <button class="btn btn-default selectLabelOrCategory <%for(var k = 0; k < selectMap[type].length; k++) {%><%if(selectMap[type][k] == tagList[i][j]){%> btn-success<%}%><%}%>"
                            data-id="<%=tagList[i][j]%>" type="button" data-tag="<%=j%>" id=""><%=j%>
                    </button>
                    <%}else{%>
                    <button class="btn btn-default selectLabelOrCategory" data-id="<%=tagList[i][j]%>" data-tag="<%=j%>"
                            type="button"
                            id=""><%=j%>
                    </button>
                    <%}%>
                    <%}%>
                    <%}%>
                </label>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-primary selectBtn" data-type="<%=type%>"> 确 定</button>
    </div>
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

    var currentSearchSource = '';
    var currentSearchTitle = '';
    var currentSearchStatus = "all";
    var currentSearchContentType = "";
    var currentSearchTag = "";
    var currentSearchStartTime = "";
    var currentSearchEndTime = "";
    var currentSearchId = "";
    var currentSearchPushUser = "";
    var currentSearchTagId = 0;
    var currentSearchStyleType = "";
    var currentPageFlag = true;
    var currentIsTop = "all";

    var _currentPage = 1;

    var selectMap = {label: [], category: []};

    function getArticleList(page) {
        var postData = {
            currentPage: page,
            status: currentSearchStatus,
            contentType: currentSearchContentType,
            pushUser: currentSearchPushUser,
            id: currentSearchId,
            styleType: currentSearchStyleType,
            startTime: currentSearchStartTime,
            endTime: currentSearchEndTime,
            countFlag: currentPageFlag,
            tag: currentSearchTagId,
            isTop: currentIsTop
        };
        if (currentSearchSource != '') {
            postData.source = currentSearchSource;
        }
        if (currentSearchTitle != '') {
            postData.title = currentSearchTitle;
        }
//        if(selectMap['label'].length==0){
//            postData.tag=0;
//        }else{
//            postData.tag=selectMap['label'][0];
//        }

        console.info(postData);

        $.post('getArticleList.vpage', postData, function (data) {
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
            format: 'yyyy-mm-dd hh:ii'
        });

        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        };

        _currentPage = $.getUrlParam('currentPage');
        getArticleList(_currentPage);
        //标题筛选
        $(document).on('click', "#searchBtn", function () {
            currentSearchSource = $("#searchSource").val().trim();
            currentSearchTitle = $("#searchTitle").val().trim();
            currentSearchStatus = $("#filter_status").val().trim();
            currentSearchContentType = $("#filter_content_type").val().trim();
            currentSearchId = $("#filterId").val().trim();
            currentSearchStartTime = $("#startTime").val().trim();
            currentSearchEndTime = $("#endTime").val().trim();
            currentSearchPushUser = $("#searchPushUser").val().trim();
            currentSearchStyleType = $("#filter_style_type").val().trim();
            currentSearchTagId = $("#labelValue").val().trim();
            currentIsTop = $("#filter_top").val().trim();
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

        $(document).on("click", "#resetFilters", function () {
            console.info("resetFilters");
            $("#filter_status").val("all");
            $("#filter_content_type").val("");
            $("#labelText").val("");
            $("#searchTitle").val("");
            $("#searchTitle").val("");
            $("#searchSource").val("");
            $("#searchPushUser").val("");
            $("#startTime").val("");
            $("#endTime").val("");
            $("#filterId").val("");
            $("#labelText").val("");
            $("#labelValue").val("");
        });

        $(document).on('click', ".selectBtn", function () {
            var $this = $(this);
            var type = $this.data('type');
            var ids = [], names = [];
            $(".selectLabelOrCategory.btn-success").each(function () {
                var that = $(this);
                ids.push(that.data('id'));
                names.push(that.data("tag"));
            });

            $("#" + type + "Btn").data('ids', ids.join(',')).siblings('input.selectValue').val(names.join(','));
            console.info(ids);
            selectMap[type] = ids;
            $("#labelText").val(names.join(","));
            $("#labelValue").val(ids.join(","));
            console.info(selectMap);
            $('#myModal').modal('hide');
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