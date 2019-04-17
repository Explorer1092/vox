<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-外部文章审核' page_num=13 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<style>
    .uploadBox {
        height: 100px;
    }

    .uploadBox .addBox {
        cursor: pointer;
        width: 170px;
        height: 124px;
        border: 1px solid #ccc;
        text-align: center;
        color: #ccc;
        float: left;
        margin-right: 20px;
    }

    .uploadBox .addBox .addIcon {
        vertical-align: middle;
        display: inline-block;
        font-size: 80px;
        line-height: 95px;
    }

    .uploadBox .addBox .addIcon0 {
        vertical-align: middle;
        display: inline-block;
        font-size: 80px;
        line-height: 95px;
    }

    .uploadBox .addBox .addIcon1 {
        vertical-align: middle;
        display: inline-block;
        font-size: 80px;
        line-height: 95px;
    }

    .uploadBox .addBox .addIcon2 {
        vertical-align: middle;
        display: inline-block;
        font-size: 80px;
        line-height: 95px;
    }

    .uploadBox .addBox img {
        width: 170px;
        height: 124px;
    }

    ul.fancytree-container {
        width: 280px;
        height: 400px;
        overflow: auto;
        position: relative;
    }
</style>
<div class="modal hide fade" id="rejectedModal">
    <div class="modal-header">
        <button class="close" type="button" data-dismiss="modal">×</button>
        <h3 id="myModalLabel">填写审核意见</h3>
    </div>
    <div class="modal-body">
        <div id="submitReview">
            <textarea id="checkReason" name="checkReason" class="checkReason" placeholder="注明本次审核的原因"></textarea>
        </div>
        <div>
            <button class="btn btn-primary" id="submitNote">提交</button>
            <button class="btn btn-danger" data-dismiss="modal">取消</button>
        </div>
    </div>
</div>

<div class="span9">
    <fieldset>
        <legend>外部文章审核</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName">标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" <#if newsMap["newsTitle"]??>value="${newsMap["newsTitle"]!''}"</#if>
                                       name="title" id="title" placeholder=""
                                       style="width: 60%" class="input" readonly="readonly">
                                <input type="text" style="display: none"
                                       <#if newsMap["workFlowRecordId"]??>value="${newsMap["workFlowRecordId"]!''}"</#if>
                                       id="workFlowRecordId" name="workFlowRecordId">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">资讯内容类型：</label>
                        <div class="controls">
                            <label for="title">
                            <#--<select id="newsContentType" name="newsContentType" disabled="disabled">-->
                            <#--<#assign jxtNewsContentType=newsMap["contentType"]!''/>-->
                            <#--<#list newsMap["totalContentType"]?keys as key>-->
                            <#--<#assign typeValue=newsMap["totalContentType"][key]/>-->
                            <#--<option value="${key}"-->
                            <#--<#if key == jxtNewsContentType>selected="selected"</#if>>${typeValue}</option>-->
                            <#--</#list>-->
                            <#--</select>-->
                                <input type="text"
                                    <#assign jxtNewsContentType=newsMap["newsContentType"]!''/>
                                       <#if jxtNewsContentType??>value="${jxtNewsContentType!''}"</#if>
                                       name="title" id="title" placeholder=""
                                       style="width: 60%" class="input" readonly="readonly">
                            </label>
                        </div>
                    </div>
                    <div class="control-group playTime"
                         style="<#if jxtNewsContentType=="图文">display: none;</#if>">
                        <label class="control-label" for="productName">播放时长(s)：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if newsMap["playTime"]??>value="${newsMap["playTime"]!''}"</#if>
                                       name="playTime" id="playTime" maxlength="8" placeholder="音频和视频的播放时间，单位是秒"
                                       class="input" style="width: 30%" readonly="readonly">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">封面：</label>
                        <div class="control-group">
                            <div class="controls">
                                <div class="uploadBox">
                                    <#assign imgUrl=newsMap["imgUrl"]!''>
                                    <div id="addBox0" class="addBox" data-pic_index="0">
                                        <span class="imgShowBox">
                                            <#if imgUrl!="">
                                                <img src="${imgUrl}">
                                            </#if>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label" for="productName">文章来源：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if newsMap["source"]??>value="${newsMap["source"]!''}"</#if>
                                       name="articleSource" id="articleSource" maxlength="30" placeholder=""
                                       style="width: 60%" class="input" readonly="readonly">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">专辑名称：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if newsMap["albumName"]??>value="${newsMap["albumName"]!''}"</#if>
                                       name="albumId" id="albumId" placeholder=""
                                       style="width: 60%" class="input" readonly="readonly">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">标签：</label>
                        <div class="controls">
                        <#--<#assign tagskey=''/>-->
                        <#--<#assign tagsIds=''/>-->
                        <#--<#if newsMap["tags"]?has_content>-->
                        <#--<#list newsMap["tags"]?keys as key>-->
                        <#--<#assign tagskey = tagskey + key />-->
                        <#--<#assign tagsIds = tagsIds + newsMap["tags"][key]!'' />-->
                        <#--<#if key_has_next>-->
                        <#--<#assign tagskey = tagskey + "," />-->
                        <#--<#assign tagsIds = tagsIds + "," />-->
                        <#--</#if>-->
                        <#--</#list>-->
                        <#--</#if>-->
                            <button data-toggle="modal"
                                    data-target="#chooseTagTree" class="btn btn-default">选择
                            </button>

                            <input type="text" class="selectValue" id="selectedtags" readonly>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls"
                             <#if newsMap["status"]??&&newsMap["status"]!="待审核">style="display: none" </#if>>
                            <input type="button" id="passBtn" value="通过并上线" class="btn btn-large btn-success">
                            <input type="button" id="rejectBtn" value="驳回" class="btn btn-large btn-danger">
                        </div>
                        <div class="controls">
                            <input type="button" id="previewBtn" value="预览"
                                   class="btn btn-large btn-primary previewBtn" data-id="${newsMap["articleId"]!''}">
                        </div>
                    </div>

                    <div class="form-horizontal">
                        <legend class="legend_title">
                            <strong>审核意见区</strong>
                        </legend>
                        <div style="height: 500px;">
                            <div>
                                <table class="table table-stripped" style="width: 800px;">
                                    <thead>
                                    <tr>
                                        <th>提交时间</th>
                                        <th>提交人</th>
                                        <th>审核时间</th>
                                        <th>审核人</th>
                                        <th>审核结果</th>
                                        <th>处理意见</th>
                                    </tr>
                                    </thead>
                                    <tbody>

                                        <#if newsMap["histories"]??&&newsMap["histories"]?size gt 0>
                                            <#list newsMap["histories"] as key>
                                            <tr>
                                                <td>${(key.commitTime)!''}</td>
                                                <td>${(key.commitUser)!''}</td>
                                                <td>${(key.checkTime)!''}</td>
                                                <td>${(key.checkUser)!''}</td>
                                                <td>${(key.checkResult)!''}</td>
                                                <td>${(key.checkReason)!''}</td>
                                            </tr>
                                            </#list>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>


<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div id="modalBox"></div>

        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
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
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<div id="previewModal" class="modal hide fade" tabindex="-1" style="width: 430px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-body" style="max-height: 900px; width: 400px;" id="previewBox"></div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<script type="text/html" id="previewBox_tem">
    <div class="device" style="" id="layoutInDevice">
        <div class="device-content">
            <div id="iwindow">
                <iframe width="320" height="569" frameborder="0" src="<%=url%>"></iframe>
            </div>
        </div>
    </div>
</script>

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
<script type="text/javascript">


    var selectMap = {label: [], category: []};
    var mainSiteBaseUrl = '${newsMap["mainSiteBaseUrl"]!''}';
    var tagTree = {};
    var newsMap =${json_encode(newsMap)!"{}"};
    var initTagIds = [];
    var initTagNames = [];
    if (newsMap.tags) {
        for (var i in newsMap.tags) {
            if (!newsMap.hasOwnProperty(i)) {
                initTagNames.push(newsMap.tags[i]);
                initTagIds.push(i);
            }
        }
    }
    $("#selectedtags").val(initTagNames.join(","));
    function gettagtree() {
        $.ajax({
            url: '/advisory/loadtagtree.vpage',
            type: 'POST',
            async: false,
            success: function (data) {
                if (data.success) {
                    tagTree = data.tagTree;
                } else {
                    alert("获取标签树失败");
                }
            }
        });
    }

    $(function () {
        gettagtree();
        var rejectModal = $('#rejectedModal');
        $('#rejectBtn').on('click', function () {
            rejectModal.modal('show');
            rejectModal.attr('title', 'reject');
        });
        $('#passBtn').on('click', function () {
//            if (!tagStr) {
//                alert("标签不能为空");
//                return false;
//            }
            rejectModal.modal('show');
            rejectModal.attr('title', 'agree');
        });

        $('#submitNote').on('click', function () {
            submitCheck(rejectModal.attr('title'));
        });

        function submitCheck(operationType) {
            var workFlowRecordId = $('#workFlowRecordId').val();
            var checkReason = $('#checkReason').val();
            var tree = $("#tagTree").fancytree("getTree");
            var ids = [];
            tree.getSelectedNodes().forEach(function (item) {
                ids.push(item.data.id);
            });
            console.info(ids);
            var tagStr = ids.join(',');
            var postData = {
                newsId: "${newsMap['newsId']!''}",
                workFlowRecordId: workFlowRecordId,
                checkReason: checkReason,
                tagStr: tagStr,
                operationType: operationType
            };

            if (checkReason == '' || typeof (checkReason) == 'undefined') {
                alert("请填写审核原因");
                return false;
            }
            if (operationType == 'agree' && tagStr == '') {
                alert("请选择标签");
                return false;
            }
            $.post('checkMizarAlbumNews.vpage', postData, function (data) {
                if (data.success) {
                    alert("操作成功，页面将刷新显示最新记录");
                    var currentPage = $.getUrlParam('currentPage');
                    location.href = 'checkNewsList.vpage?currentPage=' + currentPage;
                } else {
                    alert(data.info);
                }
            });
        }

        $.getUrlParam = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        };

        $('#tagTree').fancytree({
            extensions: [],
            source: tagTree,
            checkbox: true,
            selectMode: 2,
            select: function (event, data) {
                // 重算选中的ids
                var currentSelectedTags = [];
                data.tree.getSelectedNodes().forEach(function (item) {
                    currentSelectedTags.push(item.data.name);
                });
                $("#selectedtags").val(currentSelectedTags.join(","));
            }.bind(this)
        });

        var tree = $("#tagTree").fancytree("getTree");
        for (var i = 0; i < initTagIds.length; i++) {
            console.info(initTagIds[i]);
            var node = tree.getNodeByKey(parseInt(initTagIds[i]));
            node.setSelected(true);
            // expand all the parent
            var currentNode = node;
            while (true) {
                currentNode = currentNode.getParent();
                if (currentNode == null) {
                    break;
                } else {
                    currentNode.setExpanded(true);
                }
            }
        }


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

        $(document).on("click", ".previewBtn", function () {
            var $this = $(this);
            var id = $this.data('id');
            // 推广连接使用https
            var baseUrl = mainSiteBaseUrl.replace('http:', 'https:');
            $("#previewBox").html(template("previewBox_tem", {url: baseUrl + '/view/mobile/parent/information/preview?id=' + id}));
            $("#previewModal").modal("show");
        });

    });
</script>
</@layout_default.page>