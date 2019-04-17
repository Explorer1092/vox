<#import "../../../module.ftl" as module>
<@module.page
title="新建/编辑文章"
leftMenu="专辑文章管理"
>
<style>
    a:hover {
        color: #ffffff !important;
    }
</style>

    <@app.css href="/public/plugin/wxeditor/css/editor-min.css"/>
    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 200%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 18px;">
    <div style="position:absolute;top:25%;left:50%;margin:-64px 0 0 -64px;background: url(/public/skin/images/loading.gif) no-repeat center;width:128px;height:128px;"></div>
    <p style="text-align: center;top: 30%;position: relative;font-size: 24px;">上传中…请稍后</p>
</div>
<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业—新建/编辑专辑文章</span>
</div>

<div style="float: left">
    <div class="input-control">
        <label style="width: auto">文章标题(最多30字)：</label>
        <br>
        <input type="text"
               value="${(title)!''}"
               name="title" id="title" maxlength="30" placeholder="输入标题，最多30字"
               class="require item" style="width: 250px">
    </div>
    <div class="input-control">
        <label style="width: auto">资讯内容类型：</label>
        <br>
        <div>
            <select id="newsContentType" name="newsContentType" class="v-select" style="width: 250px;">
                <option value="IMG_AND_TEXT"
                        <#if jxtNewsContentType??&&jxtNewsContentType=='IMG_AND_TEXT'>selected</#if>>图文
                </option>
                <option value="VIDEO" <#if jxtNewsContentType??&&jxtNewsContentType=='VIDEO'>selected</#if>>视频</option>
                <option value="AUDIO" <#if jxtNewsContentType??&&jxtNewsContentType=='AUDIO'>selected</#if>>音频</option>
            </select>
        </div>
    </div>
    <div class="input-control playTime"
         <#if jxtNewsContentType??&&jxtNewsContentType=='IMG_AND_TEXT'>style="display: none"</#if>>
        <label style="width: auto">播放时长(s)：</label>
        <br>
        <input type="text"
               value="${(playTime)!''}"
               name="playTime" id="playTime" maxlength="8" placeholder="音频和视频的播放时间，单位是秒"
               class="require item" style="width: 300px">
    </div>
    <div class="input-control">
        <label style="width: auto">设置封面(建议尺寸：150*150)<span style="color:red">*</span>：</label>
        <br>
        <div class="addBox">
            <#if (imgUrl)??><img src="${(imgUrl)!''}" style="width: 150px;height: 150px;"
                                 data-file_name="${(imgfile)!''}"></#if>
        </div>
        <input type="file" class="js-file" name="file1" id="file1" accept="image/gif, image/jpeg, image/png, image/jpg">
    </div>
    <div class="input-control">
        <label style="width: auto">所属专辑：</label>
        <br>
        <div>
            <select id="albumList" name="albumList" class="v-select" title="">
                <#if mizarUserAlbumList?? && mizarUserAlbumList?size gt 0>
                    <option value="">请选择专辑</option>
                    <#list mizarUserAlbumList as album>
                        <option value="${album.id!''}"
                                <#if currentAlbumId?? &&  album.id==currentAlbumId >selected</#if>>${(album.title)!''}</option>
                    </#list>
                </#if>
            </select>
        </div>
    </div>
    <div class="input-control">
        <label style="width: auto">文章来源(最多30字)：</label>
        <br>
        <input type="text"
               value="${(source)!''}"
               name="source" id="source" maxlength="30"
               class="require item" style="width: 300px">
    </div>
</div>
<div style="clear: both;">
    <div class="mat-content" style="position: relative;overflow: visible;">
        <label style="width: auto">文章编辑器</label>
        <br>
        <a href="javascript:void (0);" class="green-btn"
           style="position: absolute;top:-8px;right:0; color: #fff; padding: 5px;" id="insertHtml">插入内容</a>
        <div class="mat-content">
            <div class="item shop-name">
                <div class="editor" style="position: relative;">
                    <div class="wxeditor">
                        <div class="clearfix">
                            <div class="left clearfix" style="width: 420px;">
                                <div class="tabbox clearfix" style="width: 69px;">
                                    <ul class="tabs" id="tabs">
                                        <li><a href="javascript:void (0);" tab="tab1" class="">关注</a></li>
                                        <li><a href="javascript:void (0);" tab="tab2" class="current">标题</a></li>
                                        <li><a href="javascript:void (0);" tab="tab3" class="">内容</a></li>
                                        <li><a href="javascript:void (0);" tab="tab4" class="">互推</a></li>
                                        <li><a href="javascript:void (0);" tab="tab5" class="">分割</a></li>
                                        <li><a href="javascript:void (0);" tab="tab6" class="">原文引导</a></li>
                                        <li><a href="javascript:void (0);" tab="tab7" class="">节日</a></li>
                                        <li><a href="javascript:void (0);" tab="tab8" class="">表格</a></li>
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
                            <div class="right" style="background:#fff; width: 46%">
                                <div id="bdeditor">
                                    <script type="text/javascript" charset="utf-8"
                                            src="/public/plugin/wxeditor/js/ueditor.config.js"></script>
                                    <script type="text/javascript" charset="utf-8"
                                            src="/public/plugin/wxeditor/js/ueditor.all.min.js"></script>
                                    <script id="editor" type="text/plain" style="margin-top:15px;width:100%;"></script>
                                </div>
                            </div>
                        </div>
                    </div>
                <#--<a href="javascript:void (0);" class="green-btn"-->
                <#--style="position: absolute;top:0;right:0; color: #fff; padding: 5px;" id="insertHtml">插入内容</a>-->
                </div><!--此处为文本编辑器-->
            </div>
        </div>
    </div>
</div>

<input id="uploadUrl" value="/basic/albumnews/uploadaudio.vpage" hidden="hidden"/>

<div class="clearfix mat-footer">
    <a id="clearBtn" class="submit-btn orange-btn" href="javascript:void(0)">清空</a>
    <a id="saveBtn" class="submit-btn green-btn" style="color: #ffffff;" href="javascript:void(0)">保存</a>
</div>
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js"
        type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/lang/zh-cn/zh-cn.js" type="text/javascript"></script>
<script type="text/javascript" src="/public/plugin/wxeditor/js/colorpicker-min.js"></script>


<script>
    $(function () {
        var contentType = $("#newsContentType option:selected").val();
        var source = $("#source");
        if (contentType == "AUDIO" || contentType == "VIDEO") {
            $(".playTime").show();
        } else {
            $(".playTime").hide();
        }
        $("#newsContentType").on("change", function () {
            contentType = $("#newsContentType option:selected").val();
            console.info(contentType);
            if (contentType == "AUDIO" || contentType == "VIDEO") {
                $(".playTime").show();
            } else {
                $(".playTime").hide();
            }
        });
        var title = $('#title');
        var sourceUrl = $('#sourceUrl');

        $(window).resize(function () {
            check();
            var win_height = $(window).height();
            $('#bdeditor').height(win_height);
            var area_height = win_height - 156;
            if (area_height > 800) {
                area_height = 800;
            }

            $('#editor').height(area_height + 40);
            $('#styleselect').height(area_height);
            $('.content').height(area_height);
        }).trigger('resize');

        $('#clearBtn').click(function () {
            if (confirm('是否确认清空内容，清空后内容将无法恢复')) {
                c.setContent('');
            }
        });

        window.onbeforeunload = function () {
            return ("");
        };

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

        $(".js-file").change(function () {

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
                    url: 'edituploadimage.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var img_html = '<img src="' + data.fileUrl + '" data-file_name="' + data.fileName + '" style=' + '"width: 150px;height: 150px;">';
                            $(".addBox").html(img_html);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });


        var c = UE.getEditor("editor", {
            serverUrl: "/basic/albumnews/ueditorcontroller.vpage",
            autoHeightEnabled: false,
            zIndex: 0,
            fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', '|',
                'horizontal', 'date','music', 'time'
            ]]
        });
        c.ready(function () {
            c.addListener('contentChange', function () {
                $("#preview").html(c.getContent());
                $("#wxpreview").html(c.getContent());
            });

            $(".itembox").on("click", function (a) {
                c.execCommand("insertHtml", "<div>" + $(this).html() + "</div><br />")
            })
        });
        $(".tabs li a").on("click", function () {
            $(this).addClass("current").parent().siblings().each(function () {
                $(this).find("a").removeClass("current")
            });
            $("#" + $(this).attr("tab")).show().siblings().hide()
        });

        $('#insertHtml').on('click', function () {
            var value = prompt('插入html代码', '');

            UE.getEditor('editor').execCommand('insertHtml', value)
        });


        function check() {
            var popupLayer = $(".PopupLayer");
            var kk = popupLayer.outerHeight() * -1;//获取元素高度:height+paelement_heighting+margin
            var ww = $(document).height();
            var qq = parseInt(popupLayer.css("top"));//获取元素当前的top值，String类型转换为number类型

            if (qq == kk || qq == ww) {

            }
            else {
                var browser_visible_region_height = document.documentElement.clientHeight;//获取浏览器可见区域高度
                var element_height = popupLayer.outerHeight();//获取元素高度:height+paelement_heighting+margin
                //计算元素显示时的top值
                var element_show_top = (browser_visible_region_height - element_height) / 2;
                popupLayer.stop(true).animate({top: element_show_top}, 1500);
            }
        }

        <#if content?has_content>
            c.ready(function () {
                c.setContent('${(content)?replace('\n','')?replace("'" , "\\'")!''}');
            });
        </#if>

        //保存
        $('#saveBtn').on('click', function () {
            var html = c.getContent();
            var imgUrl = $(".addBox img").data("file_name");
            var playTime = $("#playTime").val();
            if (title.val() == '') {
                alert('文章标题不可为空');
                return false;
            }
            if (typeof (imgUrl) == 'undefined' || imgUrl == '') {
                alert("封面不能为空");
                return false;
            }
            if (html == '') {
                alert('文章内容不可为空');
                return false;
            }
            if ((contentType == "AUDIO" || contentType == "VIDEO") && (playTime == '' || typeof(contentType) == "undefined")) {
                alert("播放时间不能为空");
                return false;
            }

            if (isNaN(playTime)) {
                alert("播放时间只能为数字");
                return false;
            }
            if (source.val() == '') {
                alert('文章来源不可为空');
                return false;
            }
            <#if submitted!false>
                if (confirm('编辑已投稿的文章需要重新投稿，是否确定保存?')) {
                    post();
                }
            <#else>
                post();
            </#if>

            function post() {
                $.post('savenews.vpage', {
                    content: html,
                    title: title.val(),
                    imgUrl: imgUrl,
                    newsId: '${newsId!""}',
                    source: source.val(),
                    playTime: playTime,
                    albumId: $("#albumList option:selected").val(),
                    contentType: $("#newsContentType option:selected").val()
                }, function (data) {
                    if (data.success) {
                        window.onbeforeunload = function () {
                        };
                        location.href = 'index.vpage';
                    } else {
                        alert(data.info);
                    }

                });
            }


        });

        //$('.sidebar').css({'margin-bottom': '1px', 'padding-bottom': "100px"});


    });
</script>
</@module.page>