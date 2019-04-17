<#import "../../module.ftl" as module>
<@module.page
title="公众号"
leftMenu="素材管理"
>
<style>
    .wrapper {
        width: 1300px;
    }

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
    <span class="title-h1">一起作业公众号—新建/编辑文章</span>
</div>

<div class="matManage-box">
    <div class="input-control">
        <p>生成的链接：</p>
        <input title="生成的链接" value="${(generateUrl)!''}" readonly disabled class="require item" style="width: 250px;">
    </div>
    <div class="input-control">
        <p>文章标题：</p>
        <input id="title" value="${(title)!''}" maxlength="200" title="文章标题" class="require item" style="width: 250px;">
    </div>
    <div class="input-control">
        <p>原文链接：</p>
        <input id="sourceUrl" value="${(sourceUrl)!''}" maxlength="500" title="原文链接" class="require item"
               style="width: 250px;">
    </div>
</div>

<div class="mat-content">
    <div class="item shop-name">
        <p>文章编辑器</p>
        <div class="editor" style="width: 1000px;position: relative;">
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
            <a href="javascript:void (0);" class="green-btn"
               style="position: absolute;top:0;right:0; color: #fff; padding: 5px;" id="insertHtml">插入内容</a>
        </div><!--此处为文本编辑器-->

    </div>
</div>
<div class="clearfix mat-footer">
    <a id="clearBtn" class="submit-btn orange-btn" href="javascript:void(0)">清空</a>
    <a id="saveBtn" class="submit-btn green-btn" style="color: #ffffff;" href="javascript:void(0)">保存</a>
</div>
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js"
        type="text/javascript"></script>
<script src="/public/plugin/ueditor-1-4-3/lang/zh-cn/zh-cn.js" type="text/javascript"></script>
<script type="text/javascript" src="/public/plugin/wxeditor/js/colorpicker-min.js"></script>

<input id="uploadUrl" value="/basic/officialaccount/uploadaudio.vpage" hidden="hidden"/>
<script>
    $(function () {
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
            return confirm("确定离开此页面吗？");
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

        var c = UE.getEditor("editor", {
            serverUrl: "/basic/officialaccount/ueditorcontroller.vpage",
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
                'link', 'music', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', '|',
                'horizontal', 'date', 'time'
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
            if (title.val() == '') {
                alert('文章标题不可为空');
                return false;
            }

            /*if(sourceUrl.val() == ''){
                alert('文章描述不可为空');
                return false;
            }*/

            if (html == '') {
                alert('文章内容不可为空');
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
                $.post('/basic/officialaccount/savematerial.vpage', {
                    content: html,
                    title: title.val(),
                    source_url: sourceUrl.val(),
                    newsId: '${newsId!""}'
                }, function (data) {
                    if (data.success) {
                        window.onbeforeunload = function () {
                        };
                        location.href = '/basic/officialaccount/material.vpage';
                    } else {
                        alert(data.info);
                    }

                });
            }


        });

        $('.sidebar').css({'margin-bottom': '1px', 'padding-bottom': "100px"});
    });
</script>
</@module.page>