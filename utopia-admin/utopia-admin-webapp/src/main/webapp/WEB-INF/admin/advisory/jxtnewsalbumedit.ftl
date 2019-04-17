<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-专辑管理-新建/编辑' page_num=13 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/clockpicker/bootstrap-clockpicker.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/clockpicker/bootstrap-clockpicker.min.js"></script>
    <@app.css href="/public/plugin/wxeditor/css/editor-min.css"/>
<style>
    .uploadBox {
        height: 100px;
    }

    .uploadBox .addBox {
        cursor: pointer;
        width: 326px;
        height: 326px;
        border: 1px solid #ccc;
        text-align: center;
        color: #ccc;
        float: left;
        margin-right: 20px;
    }

    .uploadBox .addBox img {
        width: 326px;
        height: 326px;
    }

    .uploadBox_bigImg {
        height: 100px
    }

    .uploadBox_bigImg .addBox_bigImg {
        cursor: pointer;
        width: 670px;
        height: 370px;
        border: 1px solid #ccc;
        text-align: center;
        color: #ccc;
        float: left;
        margin-right: 20px;
    }

    .uploadBox_bigImg .addBox_bigImg img {
        width: 670px;
        height: 370px;
    }

    ul.fancytree-container {
        width: 280px;
        height: 400px;
        overflow: auto;
        position: relative;
    }

    .form-horizontal .controls table th .control-label {
        float: left;
        margin-left: -180px;
        width: 160px;
        text-align: right;
    }

    .form-horizontal .controls table th label {
        text-align: left;
    }

    #colorpickerbox input {
        width: 50px;
    }

    input[type="text"] {
        height: 30px;
    }
</style>

<div class="span9">
    <fieldset>
        <legend>专辑编辑</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">类型<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="free">免费
                                        <input type="radio" name="type" id="free" value="free"/>
                                    </label></td>
                                    <td><label for="unfree">付费
                                        <input type="radio" name="type" id="unfree" value="unfree"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">专辑名称<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" <#if albumInfo["title"]??>value="${albumInfo["title"]!''}"</#if>
                                       name="title" id="title" maxlength="20" placeholder="输入标题，最多20字"
                                       style="width: 50%" class="input">
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">专辑副标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if albumInfo["subTitle"]??>value="${albumInfo["subTitle"]!''}"</#if>
                                       name="title" id="subTitle" maxlength="20" placeholder="输入副标题，最多20字"
                                       style="width: 50%" class="input">
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">ID：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" <#if albumInfo["albumId"]??>value="${albumInfo["albumId"]!''}"</#if>
                                       name="albumId" id="albumId" maxlength="500" placeholder="albumId" class="input"
                                       disabled style="width: 50%">
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">设置封面（小图）<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <p style="font-size: 12px" class="text-error">建议尺寸(326*326)</p>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <div class="uploadBox">
                                    <div class="addBox">
                                        <span class="imgShowBox">
                                            <#if albumInfo["headImgName"]?has_content>
                                                <img src="${albumInfo["headImg"]!''}"
                                                     data-file_name="${albumInfo["headImgName"]!''}">
                                            </#if>
                                        </span>
                                        <input class="fileUpBtn" type="file"
                                               accept="image/gif, image/jpeg, image/png, image/jpg"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">设置封面（大图）<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <p style="font-size: 12px" class="text-error">建议尺寸(670*370)</p>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <div class="uploadBox_bigImg">
                                    <div class="addBox_bigImg">
                                        <span class="imgShowBox_bigImg">
                                            <#if albumInfo["bigImgName"]?has_content>
                                                <img src="${albumInfo["bigImg"]!''}"
                                                     data-file_name="${albumInfo["bigImgName"]!''}">
                                            </#if>
                                        </span>
                                        <input class="fileUpBtn_bigImg" type="file"
                                               accept="image/gif, image/jpeg, image/png, image/jpg"
                                               style="float: left"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">专辑类型<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <table>
                                <tr>
                                    <td><label for="insideType">内部
                                        <input type="radio" name="innertype" id="insideType" value="INSIDE"/>
                                    </label></td>
                                    <td><label for="externalType">外部
                                        <input type="radio" name="innertype" id="externalType" value="EXTERNAL_MIZAR"/>
                                    </label></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="control-group" id="contentType">
                        <label class="control-label">专辑内容类型<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <select id="sl_contentType">
                                <option value="">请选择</option>
                                <option value="VIDEO">视频专辑</option>
                                <option value="AUDIO">音频专辑</option>
                                <option value="IMG_AND_TEXT">图文专辑</option>
                                <option value="MIX_AUDIO_AND_VIDEO">音视频混合专辑</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group" id="div_mizarUserName">
                        <label class="control-label" for="productName">专辑属主<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text"
                                       <#if albumInfo["albumOwner"]??>value="${albumInfo["albumOwner"]!''}"</#if>
                                       name="mizarUserName" id="mizarUserName" placeholder="输入Mizar用户名"
                                       style="width: 50%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">作者<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" <#if albumInfo["author"]??>value="${albumInfo["author"]!''}"</#if>
                                       name="author" id="author" maxlength="8" placeholder="输入作者，最多8字"
                                       style="width: 50%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group" id="products">
                        <label class="control-label">产品类型：</label>
                        <div class="controls">
                            <select id="sl_product">
                                <option value="">请选择</option>
                                <#list products as product>
                                    <option value="${product.id}">${product.name}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group" id="div_price">
                        <label class="control-label">价格<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <input type="text" id="price" placeholder="价格:最多保留两位小数,如8.10"
                                   <#if albumInfo['price']??>value="${albumInfo['price']!''}"</#if>/>
                        </div>
                    </div>
                    <div class="control-group" id="div_originalPrice">
                        <label class="control-label">原价：</label>
                        <div class="controls">
                            <input type="text" id="originalPrice" placeholder="原价:最多保留两位小数，如8.10"
                                   <#if albumInfo['originalPrice']??>value="${albumInfo['originalPrice']!''}"</#if>/>
                        </div>
                    </div>
                    <div class="control-group" id="div_updateDate">
                        <label class="control-label">更新时间：</label>
                        <div class="controls">
                            <input type="checkbox" name="weekDay"
                                   value="1"/>周一&nbsp;
                            <input type="checkbox" name="weekDay"
                                   value="2"/>周二&nbsp;
                            <input type="checkbox" name="weekDay"
                                   value="3"/>周三&nbsp;
                            <input type="checkbox" name="weekDay"
                                   value="4"/>周四&nbsp;
                            <input type="checkbox" name="weekDay"
                                   value="5"/>周五&nbsp;
                            <input type="checkbox" name="weekDay"
                                   value="6"/>周六&nbsp;
                            <input type="checkbox" name="weekDay"
                                   value="7"/>周日&nbsp;
                            <input type="checkbox" name="theEnd"
                                   value="8"/>已完结&nbsp;
                            <div class="input-group clockpicker">
                                <input type="text" class="form-control"
                                       <#if albumInfo["albumUpdateTime"]??>value="${albumInfo["albumUpdateTime"]!''}"</#if>
                                       id="update_time">
                                <span class="input-group-addon">
                                    <span class="glyphicon glyphicon-time">
                                    </span>
                                </span>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">详情<span style="color:red">*</span>：</label>
                        <div class="controls">
                            <button type="button" id="albumDetailBtn" class="btn btn-primary btn-small">展开详情</button>
                        </div>
                    </div>
                    <div class="control-group" id="albumDetail" hidden="hidden">
                        <div class="control-group">
                            <div class="controls">
                                <div style="height: auto;">
                                    <div class="mat-content">
                                        <div class="item shop-name">
                                            <div class="editor">
                                                <div class="wxeditor">
                                                    <div class="clearfix">
                                                        <div class="left clearfix" style="width: 420px;">
                                                            <div class="tabbox clearfix" style="width: 69px;">
                                                                <ul class="tabs" id="tabs" style="height: 580px;">
                                                                    <li><a href="javascript:void (0);" tab="tab1"
                                                                           class="">关注</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab2"
                                                                           class="current">标题</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab3"
                                                                           class="">内容</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab4"
                                                                           class="">互推</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab5"
                                                                           class="">分割</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab6"
                                                                           class="">原文引导</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab7"
                                                                           class="">节日</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab8"
                                                                           class="">表格</a></li>
                                                                </ul>
                                                                <em class="fr"></em>
                                                            </div>
                                                            <div id="styleselect" class="clearfix">
                                                                <div class="tplcontent" style="width: 350px;">
                                                                    <div id="colorpickerbox"></div>
                                                                    <div>
                                                                        <div style="background:#fff;">
                                                                            <#include 'tab/tab1.ftl'>
                                                                                <#include 'tab/tab2.ftl'>
                                                                                <#include 'tab/tab3.ftl'>
                                                                                <#include 'tab/tab4.ftl'>
                                                                                <#include 'tab/tab5.ftl'>
                                                                                <#include 'tab/tab6.ftl'>
                                                                                <#include 'tab/tab7.ftl'>
                                                                                <#include 'tab/tab8.ftl'>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <div class="goto"></div>
                                                            </div>
                                                        </div>
                                                        <div class="right"
                                                             style="background:#fff; width: 500px; margin-left: 430px; position: absolute">
                                                            <div id="bdeditor">
                                                                <script type="text/javascript" charset="utf-8"
                                                                        src="/public/plugin/wxeditor/js/ueditor.config.js"></script>
                                                                <script type="text/javascript" charset="utf-8"
                                                                        src="/public/plugin/wxeditor/js/ueditor.all.js"></script>
                                                                <script id="editor" type="text/plain"
                                                                        style="margin-top:15px;width:100%;"></script>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div><!--此处为文本编辑器-->
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="control-group" hidden="hidden">
                        <div class="control-group">
                            <div class="controls">
                                <input type="button" id="clearPublicContent" class="btn btn-danger" value="清空内容">
                            </div>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">新标签：</label>
                        <div class="controls">

                            <button data-toggle="modal"
                                    data-target="#chooseTagTree" class="btn btn-primary btn-small">选择
                            </button>

                            <input type="text" class="selectValue" id="selectedtags" readonly>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="control-group">
                            <div class="controls" id="categoryDiv">
                                <#if albumInfo["newsRankMap"]??>
                                    <table class="table table-hover table-striped table-bordered categoryTab"
                                           id="categoryTable">
                                        <tr class="category">
                                            <td>
                                                <input type="button" class="addArticle" value="添加文章"
                                                       <#if albumInfo['albumType']??&&albumInfo['albumType']=="EXTERNAL_MIZAR">style="display:none;"</#if>>
                                                <table class="table table-hover table-striped table-bordered articleTab">
                                                    <thead>
                                                    <tr>
                                                        <td>id</td>
                                                        <td>标题</td>
                                                        <td>文章排序</td>
                                                        <td>类型</td>
                                                        <td>操作</td>
                                                    </tr>
                                                    </thead>
                                                    <#list albumInfo["newsRankMap"]?keys as newsId>
                                                        <tr id="articleTr" class="articleClass">
                                                            <td><input type="text" value="${newsId}" class="articleId">
                                                            </td>
                                                            <td><input type="text" <#if albumInfo["newsTitleMap"]??>
                                                                       value="${albumInfo["newsTitleMap"][newsId]!''}"</#if>
                                                                       class="articleTitle" disabled="disabled"></td>
                                                            <td><input type="text"
                                                                       value="${albumInfo["newsRankMap"][newsId]!''}"
                                                                       class="articleRank"></td>
                                                            <td>
                                                                <label class="newsType"><#if albumInfo["newsFreeMap"][newsId]!'' == '' || albumInfo["newsFreeMap"][newsId] == 'true'>
                                                                    免费<#else >付费</#if></label>
                                                            </td>
                                                            <td><input type="button" class="deleteArticle" value="刪除文章">
                                                            </td>
                                                        </tr>
                                                    </#list>
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                                <#else>
                                    <table class="table table-hover table-striped table-bordered categoryTab"
                                           id="categoryTable">
                                        <tr class="category">
                                            <td>
                                                <input type="button" id="addArticle" class="addArticle" value="添加文章"
                                                       <#if albumInfo['albumType']??&&albumInfo['albumType']=="EXTERNAL_MIZAR">style="display:none;"</#if>>
                                                <table class="table table-hover table-striped table-bordered articleTab">
                                                    <thead>
                                                    <tr>
                                                        <td>id</td>
                                                        <td>标题</td>
                                                        <td>文章排序</td>
                                                        <td>类型</td>
                                                        <td>操作</td>
                                                    </tr>
                                                    </thead>
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                                </#if>
                                <table class="table table-hover table-striped table-bordered categoryTemplate"
                                       id="categoryTemplate" style="display: none">
                                    <tr class="category">
                                        <td>
                                            <input type="button" class="addArticle" value="添加文章"
                                                   <#if albumInfo['albumType']??&&albumInfo['albumType']=="EXTERNAL_MIZAR">style="display:none;"</#if>>
                                            <table class="table table-hover table-striped table-bordered articleTab">
                                                <thead>
                                                <tr>
                                                    <td>id</td>
                                                    <td>标题</td>
                                                    <td>文章排序</td>
                                                    <td>类型</td>
                                                    <td>操作</td>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr id="articleTr" class="articleClass">
                                                    <td><input type="text" value="" class="articleId"></td>
                                                    <td><input type="text" value="" class="articleTitle"
                                                               disabled="disabled"></td>
                                                    <td><input type="text" value="" class="articleRank"></td>
                                                    <td><label class="newsType"></label></td>
                                                    <td><input type="button" class="deleteArticle" value="刪除文章"></td>
                                                </tr>
                                                <tr id="articleTemplate" class="articleTemplate" style="display: none">
                                                    <td><input type="text" value="" class="articleId"></td>
                                                    <td><input type="text" value="" class="articleTitle"
                                                               disabled="disabled"></td>
                                                    <td><input type="text" value="" class="articleRank"></td>
                                                    <td><label class="newsType"></label></td>
                                                    <td><input type="button" class="deleteArticle" value="刪除文章"></td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

//新标签
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
//新标签

<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js"
        type="text/javascript"></script>
<script type="text/javascript" src="/public/plugin/wxeditor/js/colorpicker-min.js"></script>
<script type="text/javascript">


    $(function () {

        $('.clockpicker').clockpicker();
        var tagTree = [];
        $.ajax({
            url: 'loadtagtree.vpage',
            type: 'POST',
            async: false,
            success: function (data) {
                if (data.success) {
                    tagTree = data.tagTree;
                } else {
                    console.info("获取标签数据失败");
                }
            }
        });
        var albumInfo =${json_encode(albumInfo)!"{}"};
        var initTagIds = [];
        var initTagNames = [];
        if (albumInfo.tags) {
            for (var i in albumInfo.tags) {
                if (!albumInfo.hasOwnProperty(i)) {
                    initTagNames.push(albumInfo.tags[i]);
                    initTagIds.push(i);
                }
            }
        }

        var weekDays = albumInfo.weekDays;
        if (weekDays) {
            for (var day in weekDays) {
                if (weekDays[day] == 8) {
                    $("input:[name='theEnd']").attr('checked', true);
                    $("input:[name='weekDay']").attr("disabled", true);
                    $("#update_time").attr("disabled", true);
                    break;
                }
                $("input:[name='weekDay']").each(function (i) {
                    if (parseInt($(this).val()) == weekDays[day]) {
                        $(this).attr('checked', true);
                    }
                });
            }
        }

        //获取选择的类型
        function getType() {
            var type = '';
            $('input[name="type"]').each(function (i) {
                if ($(this).is(':checked')) {
                    type = $(this).val();
                }
            });

            console.log('type:' + type);
            return type;
        }


        function getInnerType() {
            var type = '';
            $('input[name="innertype"]').each(function (i) {
                if ($(this).is(':checked')) {
                    type = $(this).val();
                }
            });

            console.log('type:' + type);
            return type;
        }

        var c = UE.getEditor("editor", {
            serverUrl: "/advisory/ueditorcontroller.vpage",
            topOffset: 0,
            zIndex: 1040,
            autoHeightEnabled: false,
            initialFrameHeight: 473,
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', '|',
                'horizontal', 'date', 'time', 'preview'
            ]]

        });

        c.ready(function () {
            $(".itembox").on("click", function (a) {
                c.execCommand("insertHtml", "<div>" + $(this).html() + "</div><br />")
            })
        });

        var b = ["borderTopColor", "borderRightColor", "borderBottomColor", "borderLeftColor"], d = [];
        $.each(b, function (a) {
            d.push(".itembox .wxqq-" + b[a])
        });
        $("#colorpickerbox").ColorPicker({
            flat: true,
            color: "#00bbec",
            onChange: function (a, e, f) {
                $(".itembox .wxqq-bg").css({
                    backgroundColor: "#" + e
                });
                $(".itembox .wxqq-color").css({
                    color: "#" + e
                });
                $.each(d, function (g) {
                    $(d[g]).css(b[g], "#" + e)
                })
            }
        });

        $(".tabs li a").on("click", function () {
            $(this).addClass("current").parent().siblings().each(function () {
                $(this).find("a").removeClass("current")
            });
            $("#" + $(this).attr("tab")).show().siblings().hide()
        });

        <#if albumInfo["detail"]?has_content>
            c.ready(function () {
                c.setContent('${(albumInfo["detail"])?replace('\n','')?replace("'" , "\\'")!''}');
            });
        </#if>

        $("#albumDetailBtn").on("click", function () {
            if ($("#albumDetailBtn").text() == "展开详情") {
                $("#albumDetailBtn").text("隐藏详情");
                $("#albumDetail").show();
                $('#clearPublicContent').show();
            } else {
                $("#albumDetailBtn").text("展开详情");
                $("#albumDetail").hide();
                $('#clearPublicContent').hide();
            }
        });

        $('#clearPublicContent').click(function () {
            if (confirm('是否确认清空内容，清空后内容将无法恢复')) {
                c.setContent('');
            }
        });


        //选择类型后，处理价格的显示与隐藏
        $('input[name="type"]').on("click", function () {
            var type = getType();

            if (type.length == 0) {
                alert('未选择类型');
                return;
            }

            if (type == 'free') {
                $('#div_price').hide();
                $('#div_originalPrice').hide();
                $('#products').hide();
                $('#sl_product').val('');
                $('#price').val('');
                $('#originalPrice').val('');
            } else if (type == 'unfree') {
                $('#div_price').show();
                $('#div_originalPrice').show();
                $('#products').show();
            }
        });


        //选择类型后，处理专辑属主的显示和隐藏
        $('input[name="innertype"]').on("click", function () {
            var type = getInnerType();

            if (type.length == 0) {
                alert('未选择类型');
                return;
            }
            if (type == 'INSIDE' || type == 'UNKNOWN') {
                $('#div_mizarUserName').hide();
                $('#mizarUserName').val('');
                $('#addArticle').show();
            } else if (type == 'EXTERNAL_MIZAR') {
                $('#div_mizarUserName').show();
                $('#addArticle').hide();
            }
        });

        //以下初始化代码要放到radio事件绑定之后
        <#if albumInfo??>
            var free = true;
            <#if albumInfo['free']??>
                free = ${albumInfo['free']?c};
            </#if>
            var price = '${albumInfo['price']!''}';
            var originalPrice = '${albumInfo['originalPrice']!''}';
            var productId = '${albumInfo['productId']!0}';
            var albumContentType = '${albumInfo['albumContentType']!''}';

            //初始化类型和价格
            $('#price').val(price);
            $('#originalPrice').val(originalPrice);
            $('#sl_product').val(productId);
            $('input[name="type"]').each(function (i) {
                if ($(this).val() == 'free' && free) {
                    $(this).attr('checked', 'checked');
                    $(this).click();
                } else if ($(this).val() == 'unfree' && !free) {
                    $(this).attr('checked', 'checked');
                    $(this).click();
                }
            });
            var albumInsideType = "";
            <#if albumInfo['albumType']??>
                albumInsideType = '${albumInfo['albumType']!''}';
            </#if>
            $('input[name="innertype"]').each(function (i) {
                if (($(this).val() == 'INSIDE' && albumInsideType == 'INSIDE') || ($(this).val() == 'INSIDE' && albumInsideType == 'UNKNOWN')) {
                    $(this).attr('checked', 'checked');
                    $(this).click();
                } else if ($(this).val() == 'EXTERNAL_MIZAR' && albumInsideType == 'EXTERNAL_MIZAR') {
                    $(this).attr('checked', 'checked');
                    $(this).click();
                }
            });
            //如果是编辑，则不能修改类型
            <#if albumInfo['albumId']??>
                $('input[name="type"]').each(function (i) {
                    $(this).attr('disabled', 'disabled');
                });
                $('input[name="innertype"]').each(function (i) {
                    $(this).attr('disabled', 'disabled');
                    $("#mizarUserName").attr('disabled', 'disabled');
                });
            </#if>
            $("#sl_contentType").find("option[value='" + albumContentType + "']").attr("selected", "true");
        </#if>


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


        $(document).on('click', '.selectLabelOrCategory', function () {
            var $this = $(this);
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
            selectMap[type] = ids;
            $('#myModal').modal('hide');
        });


        $('.articleId').blur(function () {
            var $this = $(this);
            var newsId = $this.val();
            $.post('getJxtNewsTitle.vpage', {newsId: newsId}, function (data) {
                if (data.success) {
                    $this.parent().parent().find(".articleTitle").attr("value", data.title);
                    $this.parent().parent().find(".newsType").text(data.free == null || data.free ? "免费" : "付费");
                }
            });
        });


        $(".deleteArticle").click(function () {
            if (confirm("移出专辑的文章将被下线，是否将文章移出专辑")) {
                $(this).parent().parent().remove();
            }
        });
        $(".addArticle").click(function () {
            var $tr = $(".articleTemplate").clone(true);
            $tr.addClass("articleClass").removeClass("articleTemplate");
            $tr.show();
            $(this).next().prepend($tr);
        });

        $('input[name="theEnd"]').on('click', function () {
            if ($(this).prop("checked") == true) {
                $("input:[name='weekDay']").attr("disabled", true);
                $("input:[name='weekDay']").attr("checked", false);
                $("#update_time").attr("disabled", true);
                $("#update_time").attr("value", "");
            } else {
                $("input:[name='weekDay']").attr("disabled", false);
                $("#update_time").attr("disabled", false);
            }
        });

        //上传图片
//        $('#uploadPhotoButton').click(function () {
//            $('#uploadphotoBox').modal('show');
//        });
        //上传小图的，为了兼容以前的gridfs,就放在这里不动了，下面上传大图的，用aliyun
        $(".fileUpBtn").change(function () {

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
                console.info(fileSize);
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
                            $(".addBox").find('span.imgShowBox').html(img_html);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });


        $(".fileUpBtn_bigImg").change(function () {

            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                    alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                var fileSize = ($this[0].files[0].size / 1024 / 1012).toFixed(4); //MB
                console.info(fileSize);
                if (fileSize >= 2) {
                    alert("图片过大，重新选择。");
                    return false;
                }
                $.ajax({
                    url: 'uploadImgToOss.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var img_html = '<img src="' + data.imgUrl + '" data-file_name="' + data.imgName + '">';
                            $(".addBox_bigImg").find('span.imgShowBox_bigImg').html(img_html);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });


        //保存
        $('#saveBtn').on('click', function () {
            var newsRankMap = {};
            var emptyArticleTitle = false;
            $(".categoryTab").each(function () {
                var newsList = [];
                var $categoryTable = $(this);
                var $articleTab = $categoryTable.find(".articleTab");
                $articleTab.find('.articleClass').each(function () {
                    var articleId = $(this).find(".articleId").val();

                    var articleRank;
                    if ($(this).find(".articleRank").val() == '') {
                        articleRank = 0
                    } else {
                        articleRank = $(this).find(".articleRank").val();
                    }
                    newsRankMap[articleId] = articleRank;
                    newsList.push(articleId);
                    var articleTitle = $(this).find(".articleTitle").val();
                    if (articleTitle == '') {
                        emptyArticleTitle = true;
                        return false;
                    }
                });
            });
            if (emptyArticleTitle) {
                alert("文章标题不能为空!");
                return false;
            }

            var title = $('#title').val();
            var albumId = $('#albumId').val();
            var headImg = $(".imgShowBox img").data('file_name');
            var bigImg = $(".imgShowBox_bigImg img").data('file_name');
            var author = $("#author").val();
            var subTitle = $("#subTitle").val();
            var contentType = $("#sl_contentType").val();

            var content = c.getContent();
            content = content.replace(/\n/g, "");
            var detail = content.replace(/>\s+?</g, "><");

            var newsRank = JSON.stringify(newsRankMap);
            var type = getType();
            var innerType = getInnerType();
            var price = $('#price').val();
            var originalPrice = $('#originalPrice').val();
            var productId = $('#sl_product option:selected').val();
            //内部/外部专辑的标识
            var albumType = getInnerType();
            var mizarUserName = $("#mizarUserName").val();
            var update_time = $('#update_time').val();

            var day_array = [];
            $('input[name="weekDay"]:checked').each(function () {
                day_array.push($(this).val());//向数组中添加元素
            });
            $('input[name="theEnd"]:checked').each(function () {
                day_array.push($(this).val());//向数组中添加元素
            });
            var dayStr = day_array.join(',');//将数组元素连接起来以构建一个字符串

            var tree = $("#tagTree").fancytree("getTree");
            var ids = [];
            tree.getSelectedNodes().forEach(function (item) {
                ids.push(item.data.id);
            });
            console.info(ids);
            var tagStr = ids.join(',');
            console.info(tagStr);
            var postData = {
                albumId: '${albumInfo["albumId"]!''}',
                title: title,
                subTitle: subTitle,
                albumContentType: contentType,
                headImg: headImg,
                bigImg: bigImg,
                author: author,
                detail: content,
                tagStr: tagStr,
                newsRank: newsRank,
                innerType: innerType,
                type: type,
                price: price,
                originalPrice: originalPrice,
                productId: productId,
                mizarUserName: mizarUserName,
                dayStr: dayStr,
                updateAlbumTime: update_time,
                albumType: albumType
            };

            //判断资讯内容
        if (title == '')
        {
            alert("标题不能为空");
            return false;
        }
            <#if !albumInfo['albumId']??>
            else {
                var titleFlag = false;
                $.ajax({
                    url: 'checkAlbumTitle.vpage',
                    type: 'post',
                    async: false,
                    data: {title: title},
                    success: function (data) {
                        if (data.success && data.flag) {
                            alert("专辑标题重复");
                            titleFlag = true;
                        }
                    }
                });
                if (titleFlag) {
                    return false;
                }
            }
                var confirmContent = confirm("专辑类型和专辑属主保存后将不可更改，是否保存");
                if (!confirmContent) {
                    return false;
                }
            </#if>
            if (innerType == 'EXTERNAL_MIZAR' && (mizarUserName == '' || typeof (mizarUserName) == 'undefined')) {
                alert('外部专辑的属主不能为空');
                return false;
            }
            if (typeof (headImg) == 'undefined' || headImg == '') {
                alert("小图不能为空");
                return false;
            }
            if (typeof (bigImg) == 'undefined' || bigImg == '') {
                alert("大图不能为空");
                return false;
            }
            if (author == '') {
                alert("作者不能为空");
                return false;
            }
            if (detail == '') {
                alert("详情不能为空");
                return false;
            }
            if (typeof (tagStr) == 'undefined' || tagStr == '') {
                alert("标签不能为空");
                return false;
            }
            if (update_time != '' && !checkTime(update_time)) {
                alert("时间格式错误！");
                return false;
            }
            if (contentType == '') {
                alert("内容类型不能为空");
                return false;
            }
            if (newsRankMap != {}) {
                var onlineFlag = false;
                var albumFlag = false;
                var newsFlag = false;
                $.ajax({
                    url: 'checkNewsExist.vpage',
                    type: 'post',
                    async: false,
                    data: {"newsRankMap": JSON.stringify(newsRankMap)},
                    success: function (data) {
                        if (data.success) {
                            onlineFlag = true;
                        } else {
                            alert("id为：" + data.newsIds + "的文章不存在，不能添加到专辑里！");
                        }
                    }
                });
                $.ajax({
                    url: 'checkNewsInAlbum.vpage',
                    type: 'post',
                    async: false,
                    data: {"newsRankMap": JSON.stringify(newsRankMap), "albumId": albumId},
                    success: function (data) {
                        if (data.success) {
                            albumFlag = true;
                        } else {
                            alert("id为：" + data.newsIds + "的文章已经存在所属专辑，不能添加到专辑里！");
                        }
                    }
                });
                //检查是否免费专辑里添加了付费资讯
                if (type == 'free') {
                    var newsIds = [];
                    $('.articleId').each(function (i) {
                        if ($(this).val().length > 0) {
                            newsIds.push($(this).val());
                        }
                    });
                    $.ajax({
                        url: 'checkNewsFree.vpage',
                        type: 'post',
                        async: false,
                        data: {"newsIds": JSON.stringify(newsIds)},
                        success: function (data) {
                            if (data.success) {
                                newsFlag = true;
                            } else {
                                alert(data.info);
                            }
                        }
                    });
                }
                if (!onlineFlag || !albumFlag || (type == 'free' && !newsFlag)) {
                    return false;
                }
            }
            $.getUrlParam = function (name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]);
                return null;
            };

            $.post('saveAlbum.vpage', postData, function (data) {
                if (data.success) {
                    var currentPage = $.getUrlParam('currentPage');
                    location.href = 'albumManage.vpage';
                } else {
                    alert(data.info);
                }
            });
        });
    });


    function checkTime(time) {
        var str = time;

        var Expression = /^([0-5][0-9]):([0-5][0-9])$/;
        var objExp = new RegExp(Expression);
        return objExp.test(str) == true;
    }
</script>
</@layout_default.page>