<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-发布配置' page_num=13 jqueryVersion ="1.7.2">
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

<div class="span9">
    <fieldset>
        <legend>发布配置</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName">标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" <#if jxtNewsInfo["title"]??>value="${jxtNewsInfo["title"]!''}"</#if>
                                       name="title" id="title" maxlength="50" placeholder="输入标题，最多30字"
                                       style="width: 60%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">ID：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["articleId"]??>value="${jxtNewsInfo["articleId"]!''}"</#if>
                                       name="articleId" id="articleId" maxlength="500" placeholder="articleId"
                                       class="input" disabled style="width: 60%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">是否付费可见：</label>
                        <div class="controls">
                            <input type="checkbox" id="free"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">资讯内容样式：</label>
                        <div class="controls">
                            <label for="title">
                                <select id="newsStyleType" name="newsStyleType">
                                    <#assign jxtNewsStyleType=jxtNewsInfo["styleType"]!''/>
                                    <#list jxtNewsInfo["styleTypeList"]?keys as key>
                                        <#assign typeValue=jxtNewsInfo["styleTypeList"][key]/>
                                        <option name="${key}" value="${key}"
                                                <#if key == jxtNewsStyleType>selected="selected"</#if>>${typeValue}</option>
                                    </#list>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">资讯内容类型：</label>
                        <div class="controls">
                            <label for="title">
                                <select id="newsContentType" name="newsContentType">
                                    <#assign jxtNewsContentType=jxtNewsInfo["contentType"]!''/>
                                    <#list jxtNewsInfo["totalContentType"]?keys as key>
                                        <#assign typeValue=jxtNewsInfo["totalContentType"][key]/>
                                        <option value="${key}"
                                                <#if key == jxtNewsContentType>selected="selected"</#if>>${typeValue}</option>
                                    </#list>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group playTime"
                         style="<#if jxtNewsContentType=="IMG_AND_TEXT"||jxtNewsContentType=="OFFICIAL_ACCOUNT">display: none;</#if>">
                        <label class="control-label" for="productName">播放时长(s)：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["playTime"]??>value="${jxtNewsInfo["playTime"]!''}"</#if>
                                       name="playTime" id="playTime" maxlength="8" placeholder="音频和视频的播放时间，单位是秒"
                                       class="input" style="width: 30%">
                            </label>
                        </div>
                    </div>
                    <div class="control-group playTime"
                         style="<#if jxtNewsContentType=="IMG_AND_TEXT"||jxtNewsContentType=="OFFICIAL_ACCOUNT">display: none;</#if>">
                        <label class="control-label">音、视频地址：</label>
                        <div class="controls">
                            <input type="text" placeholder="音、视频地址" id="video_url" maxlength="200" width="500px"
                                   <#if jxtNewsInfo["video_url"]??>value="${jxtNewsInfo["video_url"]!''}"</#if>>
                            <span><button type="button" id="preview_video" class="btn btn-success btn-small">预览</button></span>
                            <span style="color:red;">请输入“https://”地址</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">页面版式：</label>
                        <div class="controls">
                            <label for="title">
                                <select id="newsType" name="newsType">
                                    <#assign jxtNewsType=jxtNewsInfo["type"]!''/>
                                    <#list jxtNewsInfo["totalType"]?keys as key>
                                        <#assign typeValue=jxtNewsInfo["totalType"][key]/>
                                        <option id="${key}" value="${key}"
                                                <#if key == jxtNewsType>selected="selected"</#if>>${typeValue}</option>
                                    </#list>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">封面：</label>
                        <div class="controls sizeType">

                            <p style="font-size: 12px"
                               class="text-error">  <#if jxtNewsInfo["type"]?has_content> <#if jxtNewsInfo["type"]=="BIG_IMAGE">
                                建议尺寸(670*240) <#elseif jxtNewsInfo["type"]=="TEXT"> <#else>
                                建议尺寸(210*140)</#if> </#if></p>

                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <div class="uploadBox">
                                    <#assign imgUrls=jxtNewsInfo["imgUrl"]!{}>
                                    <#assign fileNames=jxtNewsInfo["fileName"]!{}>

                                    <div id="addBox0" class="addBox" data-pic_index="0">
                                        <#assign imgUrl=imgUrls["url0"]!''/>
                                        <#assign fileName=fileNames["url0"]!''/>
                                        <span class="imgShowBox">
                                            <#if imgUrl!="">
                                                <img src="${imgUrl}" data-file_name="${fileName}">
                                            <#else>
                                                <i class="addIcon">+</i>
                                            </#if>
                                        </span>
                                    <#--模拟file上传-->
                                        <input class="fileUpBtn" type="file"
                                               accept="image/gif, image/jpeg, image/png, image/jpg"
                                               style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                    </div>
                                    <div id="addBox1" class="addBox" data-pic_index="1">
                                        <#assign imgUrl=imgUrls["url1"]!''/>
                                        <#assign fileName=fileNames["url1"]!''/>
                                        <span class="imgShowBox">
                                            <#if imgUrl!="">
                                                <img src="${imgUrl}" data-file_name="${fileName}">
                                            <#else>
                                                <i class="addIcon">+</i>
                                            </#if>
                                        </span>
                                    <#--模拟file上传-->
                                        <input class="fileUpBtn" type="file"
                                               accept="image/gif, image/jpeg, image/png, image/jpg"
                                               style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                    </div>
                                    <div id="addBox2" class="addBox" data-pic_index="2">
                                        <#assign imgUrl=imgUrls["url2"]!''/>
                                        <#assign fileName=fileNames["url2"]!''/>
                                        <span class="imgShowBox">
                                            <#if imgUrl!="">
                                                <img src="${imgUrl}" data-file_name="${fileName}">
                                            <#else>
                                                <i class="addIcon">+</i>
                                            </#if>
                                        </span>
                                    <#--模拟file上传-->
                                        <input class="fileUpBtn" type="file"
                                               accept="image/gif, image/jpeg, image/png, image/jpg"
                                               style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="control-group controls" id="snapshot">
                        <label class="control-group" for="productName">视频截图：</label>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">新标签：</label>
                        <div class="controls">

                            <button data-toggle="modal"
                                    data-target="#chooseTagTree" class="btn btn-default">选择
                            </button>

                            <input type="text" class="selectValue" id="selectedtags" readonly>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">文章来源：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["source"]??>value="${jxtNewsInfo["source"]!''}"</#if>
                                       name="articleSource" id="articleSource" maxlength="30" placeholder=""
                                       style="width: 60%" class="input">
                            </label>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label" for="productName">原文章链接：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["sourceUrl"]?has_content>value="${jxtNewsInfo["sourceUrl"]!''}"</#if>
                                       disabled name="articleUrl" id="articleUrl" maxlength="30" placeholder=""
                                       style="width: 60%" class="input">
                                <#if jxtNewsInfo["sourceUrl"]?has_content>
                                    <a href="${jxtNewsInfo["sourceUrl"]!''}" target="_blank">查看</a>
                                </#if>
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">群引导文案：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["chatGroupWelcomeContent"]??>value="${jxtNewsInfo["chatGroupWelcomeContent"]!''}"</#if>
                                       name="chatGroupWelcomeContent" id="chatGroupWelcomeContent" placeholder=""
                                       style="width: 60%" class="input">
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">群号：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["chatGroupId"]??>value="${jxtNewsInfo["chatGroupId"]!''}"</#if>
                                       name="chatGroupId" id="chatGroupId" placeholder="" style="width: 60%"
                                       class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">是否显示广告：</label>
                        <div class="controls">
                            <label class="radio-inline">
                                <select id="showAd" name="showAd">
                                    <option value="0">否</option>
                                    <option value="1"<#if jxtNewsInfo["showAd"]!false>selected="selected"</#if>>是
                                    </option>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">专辑ID：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["albumId"]??>value="${jxtNewsInfo["albumId"]!''}"</#if>
                                       name="albumId" id="albumId" placeholder=""
                                       style="width: 60%" class="input">
                                <input class="btn btn-primary" id="checkAlbumId" name="checkAlbumId" type="button"
                                       value="检查"/>
                                <span id="checkResult"></span>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">该文章在专辑的排序：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if jxtNewsInfo["newsRank"]??>value="${jxtNewsInfo["newsRank"]!''}"</#if>
                                       name="newsRank" id="newsRank" placeholder="请输入整数数字"
                                       style="width: 60%" class="input">
                                <span id="checkResult">建议填写：1-999的数字，数字越大，越靠上显示</span>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName" style="color: red">文章是否置顶：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="radio" name="isNewsTop" id="isTop" class="input"
                                       value="true" <#if jxtNewsInfo["isTop"]!false>checked</#if>>是
                                <input type="radio" name="isNewsTop" id="notTop" class="input"
                                       value="false" checked>否
                            </label>
                        </div>
                    </div>
                    <div class="control-grou top-order">
                        <label class="control-label" for="productName" style="color: red">文章置顶顺序：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" name="topOrder" id="topOrder" style="width: 60%" class="input"
                                       <#if jxtNewsInfo["topOrder"]??>value="${jxtNewsInfo["topOrder"]!''}"</#if>>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">推送范围：</label>
                        <div class="controls">
                            <select id="pushType" name="pushType">
                                <option value="1"
                                        <#if jxtNewsInfo["pushType"]?has_content && jxtNewsInfo["pushType"] == 1>selected="selected"</#if>>
                                    单个用户
                                </option>
                                <option value="2"
                                        <#if jxtNewsInfo["pushType"]?has_content && jxtNewsInfo["pushType"] == 2>selected="selected"</#if>>
                                    全部用户
                                </option>
                                <option value="3"
                                        <#if jxtNewsInfo["pushType"]?has_content && jxtNewsInfo["pushType"] == 3>selected="selected"</#if>>
                                    区域投放
                                </option>
                            </select>
                            <input id="availableUserId"
                                   <#if !jxtNewsInfo["pushType"]?has_content || jxtNewsInfo["pushType"] == 1>style="display: block "
                                   <#else >style="display: none" </#if> value="${jxtNewsInfo["availableUserId"]!0}"
                                   type="text">
                        </div>
                    </div>
                    <div id="regionDiv" style="display: none;">
                        <div class="controls"><input type="text" id="regionNames" readonly="true"
                                                     value="${jxtNewsInfo["regionNames"]!''}"
                                                     style="cursor: pointer;width: 600px;"></div>
                        <input type="hidden" name="regionIds" id="regionIds" value="${jxtNewsInfo["regionIds"]!''}"/>
                        <div id="cardregiontree" class="controls"></div>
                        <div class="controls">
                            <button id="select_all" type="button" class="btn btn-default" data-dismiss="modal">全选
                            </button>
                            <button id="cancel_all" type="button" class="btn btn-default" data-dismiss="modal">全不选
                            </button>
                            <button id="reversed_select" type="button" class="btn btn-default" data-dismiss="modal">反选
                            </button>
                        </div>
                    </div>

                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="saveBtn" value="保存配置" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
            <textarea id="articleContent" style="visibility:hidden" <#if jxtNewsInfo["content"]??>
                      value="${jxtNewsInfo["content"]!''}"</#if>></textarea>
        </div>
    </div>
</div>

<#--<#include "uploadphoto.ftl" />-->

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


<div id="myVideoModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true"
     style="display: none">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            </div>
            <div class="modal-body">
                <video src="" controls="controls" style="width: 500px;height: 300px;"></video>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    var initNewsType;
    var pic_index;
    var tagTree =${tagTree!''};
    var jxtNewsInfo =${json_encode(jxtNewsInfo)!"{}"};
    var initTagIds = [];
    var initTagNames = [];
    var isSnapshot = false;
    var isTop =<#if jxtNewsInfo["isTop"]??>${jxtNewsInfo["isTop"]?c}<#else > false </#if>;
    if (jxtNewsInfo.tags) {
        for (var i in jxtNewsInfo.tags) {
            if (!jxtNewsInfo.hasOwnProperty(i)) {
                initTagNames.push(jxtNewsInfo.tags[i]);
                initTagIds.push(i);
            }
        }
    }
    var recommendHeadFigures = jxtNewsInfo.recommendHeadFigures;
    if (recommendHeadFigures) {
        // 给了推荐图了
        if (recommendHeadFigures.length > 0) {
            // 有图，就是一张小图模式
            $("#newsType").val("SMALL_IMAGE");
            // 顺便把第一个图片的地址填入;
        } else {
            // 无图，就是文本模式
            $("#newsType").val("TEXT");
        }
    }
    $("#selectedtags").val(initTagNames.join(","));

    //初始化付费checkbox
    if (jxtNewsInfo.free != null && !jxtNewsInfo.free) {
        $('#free').attr('checked', 'checked');
    }

    if (isTop) {
        $("#isTop").attr("checked","checked");
        $(".top-order").show();
    } else {
        $("#notTop").attr("checked","checked");
        $(".top-order").hide();
    }

    $(function () {

        // 初始化资讯内容样式，不能选择公众号
        $("select#newsStyleType option[name='OFFICIAL_ACCOUNT']").attr("disabled", true);
        $("select#newsStyleType option[name='OFFICIAL_ACCOUNT_SUBMIT']").attr("disabled", true);
        $("select#newsStyleType option[name='EXTERNAL_ALBUM_NEWS']").attr("disabled", true);
        //todo:这里上枚举的时候先disable掉，然后真正业务上的时候再放开
        // $("select#newsStyleType option[name='KOL_VOLUNTEER_NEWS']").attr("disabled", true);
        // $("select#newsStyleType option[name='KOL_RECOMMEND_NEWS']").attr("disabled", true);
        $("select#newsStyleType option[name='STUDY_TOGETHER_MATH_CODING']").attr("disabled", true);


        $("input[name='isNewsTop']").each(function () {
            $(this).click(function () {
                var isNewsTop = $(this).val();
                if (isNewsTop === "true") {
                    $(".top-order").show();
                }
                if (isNewsTop === "false") {
                    $(".top-order").hide();
                }
            });
        });

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
        // collapse all initially;
//        $("#tagTree").fancytree("getRootNode").visit(function (node) {
//            node.setExpanded(false);
//        });

        // 选中标签，展开父标签树
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

        loadRegion();
        loadContentType();

        console.info(${json_encode(jxtNewsInfo)});
        var selectMap = {label: [], category: []};
        if ($("#video_url").val() != '' && $("#newsContentType option:selected").val() == 'VIDEO') {
            $("#snapshot").show();
            generateSnapshots($("#video_url").val());
        } else {
            $("#snapshot").hide();
        }
        $(".fileUpBtn").change(function () {
            pic_index = $(this).closest('div.addBox').data("pic_index");
            isSnapshot = false;
            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                    alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('imgFile', $this[0].files[0]);
                var fileSize = ($this[0].files[0].size / 1024 / 1012).toFixed(4); //MB
                if (fileSize >= 2) {
                    alert("图片过大，重新选择。");
                    return false;
                }
                $.ajax({
                    url: 'edituploadimage.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var img_html = '<img src="' + data.url + '" data-file_name="' + data.fileName + '">';
                            $("#addBox" + pic_index).find('span.imgShowBox').html(img_html);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        $("#labelBtn").on("click", function () {
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

        $('#checkAlbumId').on('click', function () {
            var albumId = $('#albumId').val();
            $.post('checkAlbum.vpage', {"albumId": albumId}, function (data) {
                if (data.success) {
                    $("#checkResult").html(data.albumTitle);
                } else {
                    alert("专辑ID不正确");
                }
            });
        });

        $("#categoryBtn").on("click", function () {
            var $this = $(this);
            var category = $this.data("ids");
            if (!!category) {
                if (category.toString().indexOf(',') != -1) {
                    selectMap.category = category.split(',');
                } else {
                    selectMap.category = [category];
                }
            }
            $.get("getcategorylist.vpage", function (data) {
                if (data.success) {
                    $("#modalBox").empty().html(template("modalBox_tem", {
                        tagList: data.tagList,
                        type: "category",
                        selectMap: selectMap
                    }));
                    $('#myModal').modal('show');
                }
            });
        });

        $(document).on('click', '.selectLabelOrCategory', function () {
            var $this = $(this);
            $this.toggleClass('btn-success');
//            var num = $(".selectLabelOrCategory.btn-success").length;
//            if (num > 4) {
//                alert("最多可选四个");
//                $this.removeClass('btn-success');
//            }
        });

        $('#preview_video').click(function () {
            var src = $('#video_url').val();
            $('#myVideoModal').modal({
                show: true,
                backdrop: 'static'
            });
            $('#myVideoModal video').attr('src', src);
        });

        $('#myVideoModal button').click(function () {
            $('#myVideoModal video').removeAttr('src');
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
            selectMap[type] = ids;
            $('#myModal').modal('hide');
        });

        //
        $('#saveBtn').on('click', function () {
            var title = $('#title').val();
            var articleId = $('#articleId').val();
            var imgStr = [];
            var videoSnapshotUrl = '';
            $(".addBox img").each(function () {
                if ($(this).attr("style") != "display: none;") {
                    imgStr.push($(this).data('file_name'));
                }
            });
            if (isSnapshot) {
                videoSnapshotUrl = $("#addBox0").find("img").attr("src");
            }
            //var imgStr = $("#imgUlr").data('file_name');
            var newsType = $("#newsType option:selected").val();
            //此种类型需要的图片数
            var needImgCount = 0;
            if (newsType == "BIG_IMAGE" || newsType == "SMALL_IMAGE") {
                needImgCount = 1;
            } else if (newsType == "THREE_IMAGES") {
                needImgCount = 3;
            }
            console.info(needImgCount);
            console.info(imgStr);
            if (imgStr.length < needImgCount) {
                alert("请上传" + needImgCount + "张头图");
                return false;
            }
            imgStr = imgStr.slice(0, needImgCount);
            // get all select tag ids;
            var tree = $("#tagTree").fancytree("getTree");
            var ids = [];
            tree.getSelectedNodes().forEach(function (item) {
                ids.push(item.data.id);
            });
            console.info(ids);
            var tagStr = ids.join(',');
            console.info(tagStr);
            var source = $("#articleSource").val();
            var sourceUrl = $("#articleUrl").val();
            var availableUserId = $("#availableUserId").val();
            var newsType = $("#newsType option:selected").val();
            var contentType = $("#newsContentType option:selected").val();
            var styleType = $("select#newsStyleType").val();
            var chatGroupId = $("#chatGroupId").val();
            var chatGroupWelcomeContent = $("#chatGroupWelcomeContent").val();
            var showAd = $("#showAd option:selected").val();
            var regionIds = $("#regionIds").val();
            var pushType = $("#pushType option:selected").val();
            var articleContent = $("#articleContent").val();
            var albumId = $("#albumId").val();
            var playTime = $("#playTime").val();
            var newsRank = $("#newsRank").val();
            var video_url = $("#video_url").val();
            var isNewsTop = $("input[name='isNewsTop']:checked").val();
            var topOrder = $("#topOrder").val();

            var postData = {
                newsId: '${jxtNewsInfo["newsId"]!''}',
                title: title,
                articleId: articleId,
                imgStr: imgStr.toString(),
                tagStr: tagStr,
                source: source,
                sourceUrl: sourceUrl,
                availableUserId: availableUserId,
                contentType: contentType,
                styleType: styleType,
                newsType: newsType,
                chatGroupId: chatGroupId,
                chatGroupWelcomeContent: chatGroupWelcomeContent,
                showAd: showAd,
                regionIds: regionIds,
                pushType: pushType,
                articleContent: articleContent,
                albumId: albumId,
                newsRank: newsRank,
                playTime: playTime,
                video_url: video_url,
                free: !$('#free').is(':checked'),
                isSnapshot: isSnapshot,
                videoSnapshotUrl: videoSnapshotUrl,
                isNewsTop: isNewsTop,
                topOrder: topOrder

            };

            //判断资讯内容
            if (title == '') {
                alert("标题不能为空");
                return false;
            }


            if (!tagStr && styleType != "KOL_VOLUNTEER_NEWS" && styleType != "KOL_RECOMMEND_NEWS") {
                alert("标签不能为空");
                return false;
            }

            if (source == '') {
                alert("文章来源不能为空");
                return false;
            }

            //判断推送方式
            if (pushType == 1 && availableUserId == '') {
                alert("推送对象ID不能为空");
                return false;
            }
            if (pushType == 3 && regionIds == '') {
                alert("推送区域不能为空");
                return false;
            }
            //判断资讯内容类型
            if (contentType == '') {
                alert("资讯内容类型不能为空");
                return false;
            }

            if ((contentType == "AUDIO" || contentType == "VIDEO") && (playTime == '' || typeof(contentType) == "undefined")) {
                alert("播放时间不能为空");
                return false;
            }
            if ((contentType == "AUDIO" || contentType == "VIDEO")) {
                if (video_url == '' || typeof(video_url) == "undefined") {
                    alert("播放url不能为空");
                    return false;
                }
                if (!checkURL(video_url)) {
                    alert("输入的音、视频地址不合法，请输入https地址！");
                    return false;
                }
            }

            if (isNaN(playTime)) {
                alert("播放时间只能为数字");
                return false;
            }
            if (albumId != '' && (newsRank == '' || typeof(newsRank) == 'undefined')) {
                alert("未填写资讯在专辑中的排序");
                return false;
            }
            if (newsRank != '' && isNaN(newsRank)) {
                alert("排序只能是数字");
                return false;
            }


            $.getUrlParam = function (name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]);
                return null;
            };

            $.post('savejxtnews.vpage', postData, function (data) {
                if (data.success) {
                    var currentPage = $.getUrlParam('currentPage');
                    location.href = 'jxtnewslist.vpage?currentPage=' + currentPage;
                } else {
                    alert(data.info);
                }
            });
        });

        $("#newsType").on("change", function () {
            var newsType = $("#newsType option:selected").val();
            console.info("changed");
            if (newsType == 'BIG_IMAGE' || newsType == 'SMALL_IMAGE') {
                $(".uploadBox").show();
                $(".addBox").hide();
                $("#addBox0").show();
                if (newsType == 'BIG_IMAGE') {
                    $(".sizeType p").text("建议尺寸(670*240)");
                } else {
                    $(".sizeType p").text("建议尺寸(210*140)");
                }
            } else if (newsType == 'TEXT') {
                $(".uploadBox").hide();
                $(".sizeType p").text("");
            } else {
                $(".uploadBox").show();
                $(".addBox").show();
                $(".sizeType p").text("建议尺寸(210*140)");
            }
        });

        $("#newsContentType").on("change", function () {
            var contentType = $("#newsContentType option:selected").val();
            console.info(contentType);
            $.ajax({
                url: 'contentTypeAndNewsType.vpage',
                type: "GET",
                data: {"contentType": contentType},
                async: false,
                success: function (data) {
                    if (data.success) {
                        var map = data.newsTypeMapForVideo;
                        $("#newsType").find("option").remove();
                        $.each(map, function (key, value) {
                            console.info(key);
                            console.info(value);
                            $("#newsType").append("<option value=" + key + ">" + value + "</option>");

                        })
                    } else {
                        console.info(data.info);
                    }
                }
            });
//            $.get('contentTypeAndNewsType.vpage', {"contentType": contentType}, function (data) {
//                console.info(data);
//                if (data.success) {
//                    var map = data.newsTypeMapForVideo;
//                    $("#newsType").find("option").remove();
//                    $.each(map, function (key, value) {
//                        console.info(key);
//                        console.info(value);
//                        $("#newsType").append("<option value=" + key + ">" + value + "</option>");
//
//                    })
//                }
//            })
            if (contentType == "AUDIO" || contentType == "VIDEO") {
                $(".playTime").show();
                $("#newsType").trigger('change');
            } else {
                $(".playTime").hide();
            }
            if (contentType == "VIDEO") {
                $("#snapshot").show();
            } else {
                $("#snapshot").hide();
            }

        });

        $("#video_url").blur(function (e) {
            //解决失去焦点时alert无法关闭的问题。判断是否是该元素失去焦点，如果非该元素失去，就不做之后的事情（比如整个页面失去焦点）
            if (!document.hasFocus()) {
                return;
            }
            var video_url = $("#video_url").val();
            if (video_url != '' && $("#newsContentType option:selected").val() == 'VIDEO') {
                $("#snapshot").show();
                $(".snapshot").remove();
                generateSnapshots(video_url);
            }
        });

        $('input[name="select_snapshot"]').live('click', function () {
            isSnapshot = true;
            var select_snapshot_url = '';
            select_snapshot_url = $(this).val();
            if (select_snapshot_url != '') {
                var img_html = '<img src="' + select_snapshot_url + '" data-file_name="' + $(this).data('file_name') + '">';
                $("#addBox0").find('span.imgShowBox').html(img_html);
            }
        });


        $("#pushType").on("change", function () {
            var pushType = $("#pushType option:selected").val();
            if (pushType == 2) {
                $("#availableUserId").hide();
                $("#regionDiv").hide();
                $("#availableUserId").val(0);
            } else if (pushType == 3) {
                $("#availableUserId").hide();
                loadRegion();
            } else {
                $("#regionDiv").hide();
                $("#availableUserId").show();
            }

        });

        //获取某个具体推送或者资讯的区域列表
        function loadRegion() {
            var pushType = $("#pushType option:selected").val();
            var regionDiv = $("#regionDiv");
            var $regiontree = $("#cardregiontree");
            try {
                $regiontree.fancytree('destroy');
            } catch (e) {

            }
            if (pushType == 3) {
                //选择区域
                $regiontree.fancytree({
                    source: {
                        url: "load_region.vpage?type=jxt_news&typeId=" + '${jxtNewsInfo["newsId"]!''}',
                        cache: false
                    },
                    checkbox: true,
                    selectMode: 2,
                    select: function () {
                        updateRegion();
                    }

                });
                regionDiv.show();
            } else {
                regionDiv.hide();
            }
        }

        //点击区域选中时。更新选中的区域Id和名称
        function updateRegion() {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if (regionNodes == null || regionNodes == "undefined") {
                $('#regionIds').val('');
                $('#regionNames').val('');
                return;
            }
            var selectRegionNameList = new Array();
            var selectRegionIdList = new Array();
            $.map(regionNodes, function (node) {
                selectRegionIdList.push(node.key);
                selectRegionNameList.push(node.title);
            });
            $('#regionIds').val(selectRegionIdList.join(','));
            $('#regionNames').val(selectRegionNameList.join(','));
        }

        function loadContentType() {
            var contentType = $("#newsContentType option:selected").val();
            console.info(contentType);
            $.get('contentTypeAndNewsType.vpage', {"contentType": contentType}, function (data) {
                console.info(data);
                if (data.success) {
                    var map = data.newsTypeMapForVideo;
                    $("#newsType").find("option").remove();
                    $.each(map, function (key, value) {
                        $("#newsType").append("<option value=" + key + ">" + value + "</option>");

                    })
                    initNewsType = "${jxtNewsInfo["type"]!''}";
                    initNewsType = initNewsType == "" ? "BIG_IMAGE" : initNewsType;
                    console.info("init news type is :" + initNewsType);
                    $("#newsType").val(initNewsType).trigger("change");
                }
            })
        }

        //全选
        $("#select_all").on("click", function () {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function (rootNode) {
                regionTree.rootNode.visit(function (currentNode) {
                    if (rootNode.key == currentNode.key) {
                        currentNode.setSelected(true);
                    }
                });
            });
        });
        //全不选
        $("#cancel_all").on("click", function () {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function (rootNode) {
                regionTree.rootNode.visit(function (currentNode) {
                    currentNode.setSelected(false);
                });
            });
        });
        //反选
        $("#reversed_select").on("click", function () {
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function (rootNode) {
                regionTree.rootNode.visit(function (currentNode) {
                    if (rootNode.key == currentNode.key) {
                        if (currentNode.selected) {
                            currentNode.setSelected(false);
                        } else {
                            currentNode.setSelected(true);
                        }
                    }
                });
            });
        });

    });

    function checkURL(URL) {
        var str = URL;
        //判断URL地址的正则表达式为:http(s)?://([\w-]+\.)+[\w-]+(/[\w- ./?%&=]*)?
        //下面的代码中应用了转义字符"\"输出一个字符"/"
        var Expression = /^(https)?:\/\/([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/;
        var objExp = new RegExp(Expression);
        return objExp.test(str) == true;
    }

    function generateSnapshots(video_url) {
        var video_url_arr = video_url.split("https://v.17zuoye.cn/");
        console.log(video_url_arr[1]);
        if (!video_url_arr[1]) {
            var video_url_oss_arr = video_url.split("https://17zy-content-video.oss-cn-beijing.aliyuncs.com/");
            video_url_arr[1] = video_url_oss_arr[1];
        }
        if (!video_url_arr[1]) {
            alert("无法获取视频截图，请检查视频地址是否是https或地址域名是否是v.17zuoye.cn或者17zy-content-video.oss-cn-beijing.aliyuncs.com");
            return false;
        }
        $.ajax({
            url: "generateSnapshotFromVideo.vpage",
            type: "POST",
            data: {"video_url": video_url_arr[1]},
            success: function (data) {
                if (data.success) {
                    var snapshot_urls = data.snapshots;
//                    var snapshot_url = "https://v.17zuoye.cn/" + snapshot_urls[0];
                    var isSuccess = chenkImgUrl(snapshot_urls);
                    if (isSuccess) {
                        generateSnapshotImgs(snapshot_urls);
                    }
//                    else {
//                        $.ajax(this);
//                    }
//                    var parent_snapshot_div = $('#snapshot');
//                    $.each(snapshot_urls, function (index, value) {
//                        var snapshot_url = "https://v.17zuoye.cn/" + value;
//                        var snapshot_inner_div = $('<div class="control-group"></div>');
//                        var img_html = '<img src="' + snapshot_url + '" width="120px;" height="120px;">';
//                        var radio_html = '<input type="radio" name="select_snapshot" value="' + snapshot_url + '" data-file_name="' + value + '" height="98px;">';
//                        snapshot_inner_div.attr('id', 'snapshot' + index);
//                        snapshot_inner_div.html(radio_html);
//                        snapshot_inner_div.append(img_html);
//                        snapshot_inner_div.appendTo(parent_snapshot_div);
////                        $('#snapshot').append(img_html, radio_html);
//                    });
//                    if (res.fileName.indexOf('oss-image') != -1) {
//                        trail = '@1e_1c_0o_0l_200h_200w_80q'
//                    }
//                    $('#imgDiv').html('<img src="' + res.fileName + trail + '" width="98px;" height="98px;">');
//                    $("#portrait").val(res.fileName);
                } else {
                    alert(data.info);
                }
            },
            error: function (e) {
                console.log(e);
            }
        })
    }

    //    function getSelectSnapshotUrl() {
    //        var select_snapshot_url = '';
    //        $('input[name="select_snapshot"]').each(function (i) {
    //            if ($(this).is(':checked')) {
    //                select_snapshot_url = $(this).val();
    //            }
    //        });
    //
    //        console.log('select_snapshot_url:' + select_snapshot_url);
    //        return select_snapshot_url;
    //    }
    function chenkImgUrl(snapshot_urls) {
        var isSuccess = snapshot_urls.length;
        $.each(snapshot_urls, function (index, value) {
            var image = new Image();
            image.onerror = function () {
                isSuccess--;
            };
            image.src = "https://v.17zuoye.cn/" + value;
//            var snapshot_url = "https://v.17zuoye.cn/" + value;
//            $.ajax({
//                url: snapshot_url,
//                type: "GET",
//                async: false,
//                error: function (xhr) {
//                    if (xhr.status == 404) {
//                        isSuccess--;
//
//                    }
//                }
//            });
        });
        if (isSuccess == snapshot_urls.length) {
            return true;
        } else {
            chenkImgUrl(snapshot_urls);
        }
    }

    function generateSnapshotImgs(snapshot_urls) {
        var parent_snapshot_div = $('#snapshot');
        $.each(snapshot_urls, function (index, value) {
            if (index == 0) {
                return true;
            }
            var snapshot_url = "https://v.17zuoye.cn/" + value;
            var snapshot_inner_div = $('<div class="control-group snapshot"></div>');
            var img_html = '<img src="' + snapshot_url + '" width="120px;" height="120px;">';
            var radio_html = '<input type="radio" name="select_snapshot" value="' + snapshot_url + '" data-file_name="' + value + '" height="98px;">';
            snapshot_inner_div.attr('id', 'snapshot' + index);
            snapshot_inner_div.html(radio_html);
            snapshot_inner_div.append(img_html);
            snapshot_inner_div.appendTo(parent_snapshot_div);
//                        $('#snapshot').append(img_html, radio_html);
        });
    }
</script>
</@layout_default.page>