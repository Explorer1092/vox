<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-专辑管理' page_num=13 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<div class="span9">
    <fieldset>
        <legend>专辑管理</legend>
    </fieldset>

    <fieldset>
        <div>
            <span>
                专辑状态：<select id="filter_status">
                <option value="all">全部</option>
                <option value="offline">未上线</option>
                <option value="online">已上线</option>
            </select>
            </span>
            <span>
                付费类型：<select id="filter_type">
                <option value="">全部</option>
                <option value="free">免费</option>
                <option value="unfree">付费</option>
            </select>
            </span>
            <span>
                专辑类型：<select id="album_type">
                <option value="">全部</option>
                <option value="INSIDE">内部</option>
                <option value="EXTERNAL_MIZAR">Mizar外部</option>
            </select>
                <span>
                专辑内容类型：<select id="album_content_type">
                <option value="">全部</option>
                <option value="VIDEO">视频专辑</option>
                <option value="AUDIO">音频专辑</option>
                <option value="IMG_AND_TEXT">图文专辑</option>
                <option value="MIX_AUDIO_AND_VIDEO">音视频混合专辑</option>
            </select>
            </span>
                <br>
            <span>
                创建人：<input type="text" placeholder="输入发布人crm用户名" id="searchPushUser">
            </span>
            <br>
            专辑id:<input type="text" id="filterId"/>
            <div class="control-group">
                <div class="controls">
                    <label class="control-label" for="productName">新标签：
                        <button data-toggle="modal"
                                data-target="#chooseTagTree" class="btn btn-primary btn-small">选择
                        </button>
                        <input type="text" class="selectValue" id="selectedtags" readonly>
                    </label>
                </div>
            </div>
            <button class="btn btn-primary" id="searchBtn">查询</button>
            <a class="btn btn-primary" href="${requestContext.webAppContextPath}/advisory/albumedit.vpage"
               id="contentCreateBtn">新建专辑</a>

        </div>
        <div id="articleBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>

<div class="modal fade" id="chooseTagTree" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true" style="display: none">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">选择标签</h4>
            </div>
            <div class="modal-body">
                <div id="tagTree" class="sampletree"
                     style="width:60%; height: 410px; float: left; "></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>
<script type="text/html" id="articleBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>id</th>
            <th>专辑名称</th>
            <th>状态</th>
            <th>付费类型</th>
            <th>专辑类型</th>
            <th>内容类型</th>
            <th>创建时间</th>
            <th>上线时间</th>
            <th>订阅数</th>
            <th>创建人</th>
            <th>专辑属主</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
        <%for(var i = 0; i < content.length;i++){%>
        <tr>
            <td style="cursor: pointer;" class="coryBtn" data-clipboard-text="<%=content[i].id%>"><%=content[i].id%>
            </td>
            <td><%=content[i].title%></td>
            <%if(content[i].isOnline){%>
            <td>已上线</td>
            <%}else{%>
            <td>未上线</td>
            <%}%>
            <td><%if(content[i].free){%>免费<%}else{%>付费<%}%></td>
            <td><%if(content[i].albumInsideType=="EXTERNAL_MIZAR"){%>Mizar外部<%}else{%>内部<%}%>
            </td>
            <td><%=content[i].albumContentType%></td>
            <td><%=content[i].createTime%></td>
            <td><%=content[i].onlineTime%></td>
            <td><%=content[i].subCount%></td>
            <td><%=content[i].editor%></td>
            <td><%=content[i].mizarUserName%></td>
            <td data-album_id="<%=content[i].id%>">
                <a class="btn btn-primary" href="albumedit.vpage?albumId=<%=content[i].id%>&currentPage=<%=current%>"
                   target="_blank">编辑</a>
                <%if(content[i].isOnline){%>
                <button class="btn btn-danger" id="offLineBtn" data-album_type="<%=content[i].albumInsideType%>">下线
                </button>
                <%}else{%>
                <button class="btn btn-success" id="onLineBtn">上线</button>
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

    var _currentPage = 1;
    var currentSearchPushUser = "";
    var currentSearchStatus = "all";
    var currentSearchType = "";
    var currentSearchAlbumType = "";
    var currentSearchAlbumContentType = "";
    var currentSearchId = "";
    var currentSearchTag = "";

    var selectMap = {label: [], category: []};
    function getArticleList(page) {
        var postData = {
            currentPage: page,
            status: currentSearchStatus,
            pushUser: currentSearchPushUser,
            type: currentSearchType,
            id: currentSearchId,
            tags: currentSearchTag,
            albumType: currentSearchAlbumType,
            albumContentType: currentSearchAlbumContentType
        };
//        if(selectMap['label'].length==0){
//            postData.tag=0;
//        }else{
//            postData.tag=selectMap['label'][0];
//        }

        console.info(postData);

        $.post('getAlbumList.vpage', postData, function (data) {
            if (data.success) {
                $('#articleBox').html(template("articleBox_tem", {
                    content: data.albumList,
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
        var tagTree = [];
        $.ajax({
            url: 'loadtagtree.vpage',
            type: 'POST',
            async: false,
            success: function (data) {
                if (data.success) {
                    $('#tagTree').fancytree({
                        extensions: [],
                        source: data.tagTree,
                        checkbox: true,
                        selectMode: 2,
                        select: function (event, data) {
                            // 重算选中的ids
                            var currentSelectedTags = [];
                            data.tree.getSelectedNodes().forEach(function (item) {
                                currentSelectedTags.push(item.data.name);
                            });
                            $("#selectedtags").val(currentSelectedTags.join(","));
                        }
                    });
                } else {
                    console.info("获取标签数据失败");
                }
            }
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
            var albumId = $this.closest('td').data('album_id');
            if (confirm("确定上线？")) {
                $.post("jxtnewsalbumonline.vpage", {albumId: albumId}, function (data) {
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
            var albumId = $this.closest('td').data('album_id');
            var cofirmContent;
            if ($this.data('album_type') == 'EXTERNAL_MIZAR') {
                cofirmContent = confirm("外部专辑下线后专辑内的文章将全部下线，是否下线专辑？");
            } else {
                cofirmContent = confirm("确定下线？");
            }
            if (cofirmContent) {
                $.post("jxtnewsalbumoffline.vpage", {albumId: albumId}, function (data) {
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
            currentSearchPushUser = $("#searchPushUser").val().trim();
            currentSearchType = $("#filter_type option:selected").val();
            currentSearchAlbumType = $("#album_type option:selected").val();
            currentSearchAlbumContentType = $("#album_content_type option:selected").val();
            currentSearchId = $("#filterId").val().trim();
            var tree = $("#tagTree").fancytree("getTree");
            var ids = [];
            tree.getSelectedNodes().forEach(function (item) {
                ids.push(item.data.id);
            });
            currentSearchTag = JSON.stringify(ids);
            _currentPage = 1;
            getArticleList(_currentPage);
        });


//        var tree = $("#tagTree").fancytree("getTree");
//        for (var i = 0; i < initTagIds.length; i++) {
//            console.info(initTagIds[i]);
//            var node = tree.getNodeByKey(parseInt(initTagIds[i]));
//            node.setSelected(true);
//            // expand all the parent
//            var currentNode = node;
//            while (true) {
//                currentNode = currentNode.getParent();
//                if (currentNode == null) {
//                    break;
//                } else {
//                    currentNode.setExpanded(true);
//                }
//            }
//        }


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
            $("#filter_status").val("online");
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