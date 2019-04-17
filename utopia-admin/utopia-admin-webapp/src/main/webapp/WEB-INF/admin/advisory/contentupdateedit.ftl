<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-更新/编辑内容' page_num=13>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<div class="span9">
    <div class="control-group">
        <label class="control-label">设置标题：</label>
        <div class="controls">
            <input type="text" placeholder="请输入文章标题" id="title" maxlength="50">
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">备注：</label>
        <div class="controls">
            <input type="text" placeholder="默认为空" id="remark" maxlength="50">
        </div>
    </div>
    <div class="control-group" id="edit_source_url">
        <label class="control-label">设置原文连接：</label>
        <div class="controls">
            <input type="text" placeholder="设置原文连接" id="source_url" maxlength="200" width="500px">
        </div>
    </div>
    <div class="control-group" style="display: none;">
        <label class="control-label">设置简介：</label>
        <div class="controls">
            <input type="text" placeholder="请输入文章标题" id="digest" maxlength="50">
        </div>
    </div>

    <div class="control-group">
        <label class="control-label">设置类别：</label>
        <div class="controls">
            <select id="category">
                <option value="2">普通资讯消息</option>
                <option value="1">导流专用</option>
            </select>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">新版编辑内容：</label>
        <div class="controls">
            <!-- 加载编辑器的容器 -->
            <script id="container_new" name="container_new" type="text/plain"></script>
        </div>
    </div>
    <div class="control-group">
        <label class="control-label">旧版编辑内容：</label>
        <span style="color:red;">请编辑新版，旧版误动</span>
        <button type="button" id="old_detail" class="btn btn-primary btn-small">展开旧版</button>
        <div class="controls" hidden="hidden" id="old_editor">
            <!-- 加载编辑器的容器 -->
            <script id="container" name="content" type="text/plain"></script>
        </div>
    </div>

    <a class="btn btn-primary" href="javascript:void (0);" id="contentSubmitBtn">保存</a>
</div>


<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>

<script>
    $(function () {
        var options = {
            filterMode: true, //true时根据 htmlTags 过滤HTML代码，false时允许输入任何代码。
            items: [
                'source', '|', 'undo', 'redo', '|', 'preview', 'template', 'cut', 'copy', 'paste',
                'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
                'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
                'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/',
                'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
                'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image',
                'table', 'hr', 'emoticons', 'pagebreak',
                'anchor', 'link', 'unlink', '|'
            ],
            uploadJson: 'edituploadimage.vpage?rawId=${rawId!}',
            fileManagerJson: 'edituploadimage.vpage',
            allowFileManager: true
        };

        var ue = UE.getEditor('container', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });


        var ue_new = UE.getEditor('container_new', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [14,16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', '|', 'forecolor', 'backcolor', 'insertorderedlist','formatmatch', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                '|', 'preview'
            ]]
        });


        var id = "${id!}";
        var article;
        $.post("/advisory/loadarticlebyid.vpage", {id: id}, function (data) {
            article = data.article;
            console.info(article.content);
            $("#title").val(article.title);
            $("#digest").val(article.digest);
            $("#category").val(article.category);
            $("#source_url").val(article.source_url);
            $("#remark").val(article.remark);
            $("#video_url").val(article.video_url);
            var img_html = '<img src="' + data.imgUrl + '" data-file_name="' + article.video_img + '" style=' + '"width: 150px;height: 150px;">';
            $(".addBox").html(img_html);
            if (article != null) {
                ue.ready(function () {
                    ue.setContent(article.content.replace(/\n/g, '<p><br/></p>'));
                });
                if (article.content_by_album_news != null) {
                    ue_new.ready(function () {
                        ue_new.setContent(article.content_by_album_news.replace(/\n/g, '<p><br/></p>'));
                    });
                }
            }

        });


        $("#old_detail").on("click", function () {
            if ($("#old_detail").text() == "展开旧版") {
                $("#old_detail").text("隐藏旧版");
                $("#old_editor").show();
            } else {
                $("#old_detail").text("展开旧版");
                $("#old_editor").hide();
            }
        });



        //保存
        $('#contentSubmitBtn').on('click', function () {

            var content = ue.getContent();
            var new_content = ue_new.getContent();
            var video_url = $("#video_url").val();
            content = content.replace(/\n/g, "");
            content = content.replace(/>\s+?</g, "><");
            if (new_content.length === 0&&content.length === 0) {
                alert("内容不能为空");
                return false;
            }
            article.content = content;
                article.words_count = ue.getContentTxt().replace(/\n/g, '').length;
                article.title = $("#title").val();
                article.digest = $("#digest").val();
                article.source_url = $("#source_url").val();
                article.remark = $("#remark").val();
                article.category = $("#category").val();
                article.video_url = video_url;
                article.video_img = $(".addBox img").data("file_name");
                article.content_by_album_news = ue_new.getContent();
                $.post('updatearticle.vpage', {article: JSON.stringify(article)}, function (data) {
                    if (data.success) {
                        location.href = 'viewarticles.vpage';
                    }
                });
            }
            );
    });


</script>
</@layout_default.page>