<#import "../../module.ftl" as module>
<@module.page
title="编辑专辑"
leftMenu="专辑管理"
>

    <@app.css href="/public/plugin/wxeditor/css/editor-min.css"/>
    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
<style>
    a:hover {
        color: #ffffff !important;
    }

    .uploadBox .addBox img {
        width: 150px;
        height: 150px;
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

    .articleClass a:hover {
        background: red
    }
</style>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 200%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 18px;">
    <div style="position:absolute;top:25%;left:50%;margin:-64px 0 0 -64px;background: url(/public/skin/images/loading.gif) no-repeat center;width:128px;height:128px;"></div>
    <p style="text-align: center;top: 30%;position: relative;font-size: 24px;">上传中…请稍后</p>
</div>
<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业—专辑编辑</span>
</div>

<div style="float: left;">
    <div class="input-control">
        <label style="width: auto">专辑名称：</label>
        <input type="text" <#if albumInfo["title"]??>value="${albumInfo["title"]!''}"</#if>
               name="title" id="title" maxlength="20" placeholder="输入标题，最多20字"
               style="width: 250px;" class="require item" readonly disabled>
    </div>
    <div class="input-control">
        <label style="width: auto">封面：</label>
        <#if albumInfo["headImg"]?has_content>
            <img src="${albumInfo["headImg"]!''}"
                 data-file_name="${albumInfo["headImgName"]!''}" style="width:150px;height: 150px;">
        </#if>

    </div>
    <div class="input-control">
        <label style="width: auto">详情：</label>
        <button type="button" id="albumDetailBtn" class="btn btn-default btn-small">展开详情</button>
    </div>

    <div class="control-group" id="albumDetail" hidden="hidden">
        <div class="control-group">
            <div class="controls">
                <div style="height: 630px;">
                    <div class="mat-content">
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
                                                        <#include 'news/tab/tab1.ftl'>
                                                                                <#include 'news/tab/tab2.ftl'>
                                                                                <#include 'news/tab/tab3.ftl'>
                                                                                <#include 'news/tab/tab4.ftl'>
                                                                                <#include 'news/tab/tab5.ftl'>
                                                                                <#include 'news/tab/tab6.ftl'>
                                                                                <#include 'news/tab/tab7.ftl'>
                                                                                <#include 'news/tab/tab8.ftl'>
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
                                                    src="/public/plugin/wxeditor/js/ueditor.all.min.js"></script>
                                            <script id="editor" type="text/plain"
                                                    style="margin-top:15px;width:100%;" readonly="readonly"></script>
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

    <div class="control-group" hidden="hidden">
        <div class="control-group">
            <div class="controls">
                <input type="button" id="clearPublicContent" class="btn btn-danger" value="清空内容">
            </div>
        </div>
    </div>
    <div class="control-group">
        <div class="control-group">
            <div class="controls" id="categoryDiv">
                <#if albumInfo["newsRankMap"]?? && albumInfo["newsRankMap"]?size gt 0>
                    <table class="table table-hover table-striped table-bordered data-table articleTab">
                        <thead>
                        <tr>
                            <td>标题</td>
                            <td>审核状态</td>
                            <td>文章排序</td>
                            <td>操作</td>
                        </tr>
                        </thead>
                        <tbody>
                            <#list albumInfo["newsRankMap"]?keys as newsId>
                            <tr id="articleTr" class="articleClass">
                                <input type="hidden" value="${newsId}" class="articleId"/>
                                <td><input type="text" <#if albumInfo["newsTitleMap"]??>
                                           value="${albumInfo["newsTitleMap"][newsId]!''}"</#if>
                                           class="articleTitle" disabled="disabled"/></td>
                                <td><input type="text" <#if albumInfo["newsStatusMap"]??>
                                           value="${albumInfo["newsStatusMap"][newsId]!''}"</#if>
                                           class="articleTitle" disabled="disabled"/></td>
                                <td><input type="text"
                                           value="${albumInfo["newsRankMap"][newsId]!''}"
                                           class="articleRank" hidden/><a href="#" class="up">上移</a> <a
                                        href="#" class="down">下移</a> <a href="#" class="top">置顶</a> <a
                                        href="#" class="stick">置底</a></td>
                                <td><input type="button" class="offlineArticle"
                                           value="下线"<#if !albumInfo["newsOnlineMap"][newsId]>
                                           style="display: none" </#if> data-news_id="${newsId}">
                                </td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </#if>
            </div>
        </div>
    </div>
    <div class="control-group">
        <div class="controls">
            <a id="saveBtn" class="submit-btn green-btn" style="color: #ffffff;" href="javascript:void(0)">保存</a>
        </div>
    </div>
</div>
<input id="uploadUrl" value="/basic/albumnews/uploadaudio.vpage" hidden="hidden"/>
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js"
        type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/lang/zh-cn/zh-cn.js" type="text/javascript"></script>
<script type="text/javascript" src="/public/plugin/wxeditor/js/colorpicker-min.js"></script>

<script type="text/javascript">


    $(function () {
        var c = UE.getEditor("editor", {
            serverUrl: "/common/ueditorcontroller.vpage",
            topOffset: 0,
            zIndex: 1040,
            autoHeightEnabled: false,
            readonly: true,
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
                'horizontal', 'date', 'music', 'time', 'preview'
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

        $(".offlineArticle").click(function () {
            var $this = $(this);
            var newsId = $this.data("news_id");
            if (confirm("文章将被下线，文章下线后如需再次上线需要联系管理员")) {
                $.ajax({
                    url: 'jxtnewsoffline.vpage',
                    type: 'POST',
                    data: {newsId: newsId},
                    async: false,
                    success: function (data) {
                        if (data.success) {
                            $this.css('display', 'none');
                        } else {
                            alert("下线失败");
                        }
                    }
                });
            }
        });

        $(".addArticle").click(function () {
            var $tr = $(".articleTemplate").clone(true);
            $tr.addClass("articleClass").removeClass("articleTemplate");
            $tr.show();
            $(this).next().prepend($tr);
        });
        //保存
        $('#saveBtn').on('click', function () {
            var emptyArticleTitle = false;
            var newsList = [];
            var $articleTab = $(".articleTab");
            $articleTab.find('.articleClass').each(function () {
                var articleId = $(this).find(".articleId").val();
                var articleRank;
                if ($(this).find(".articleRank").val() == '') {
                    articleRank = 0
                } else {
                    articleRank = $(this).find(".articleRank").val();
                }
                newsList.push(articleId);
                var articleTitle = $(this).find(".articleTitle").val();
                if (articleTitle == '') {
                    emptyArticleTitle = true;
                    return false;
                }
            });
            if (emptyArticleTitle) {
                alert("文章标题不能为空!");
                return false;
            }

            var albumId = $('#albumId').val();
            var content = c.getContent();
            content = content.replace(/\n/g, "");
            var detail = content.replace(/>\s+?</g, "><");

            var newsRankList = JSON.stringify(newsList);
            var postData = {
                albumId: '${albumInfo["albumId"]!''}',
                detail: content,
                newsRankList: newsRankList
            };

            $.getUrlParam = function (name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]);
                return null;
            };

            $.post('saveAlbum.vpage', postData, function (data) {
                if (data.success) {
                    var currentPage = $.getUrlParam('currentPage');
                    location.href = 'index.vpage';
                } else {
                    alert(data.info);
                }
            });
        });
        /**
         * 专辑文章上移、下移操作
         */
        //上移
        var $up = $(".up")
        $up.click(function () {
            var $tr = $(this).parents("tr");
            if ($tr.index() != 0) {
                $tr.fadeOut().fadeIn();
                $tr.prev().before($tr);
            }
        });
        //下移
        var $down = $(".down");
        var len = $down.length;
        $down.click(function () {
            var $tr = $(this).parents("tr");
            if ($tr.index() != len - 1) {
                $tr.fadeOut().fadeIn();
                $tr.next().after($tr);
            }
        });
        //置顶
        var $top = $(".top");
        $top.click(function () {
            var $tr = $(this).parents("tr");
            $tr.fadeOut().fadeIn();
            $(".table").prepend($tr);
        });
        //置底
        var $stick = $(".stick");
        $stick.click(function () {
            var $tr = $(this).parents("tr");
            $tr.fadeOut().fadeIn();
            $(".table").append($tr);
        });
    });
</script>
</@module.page>