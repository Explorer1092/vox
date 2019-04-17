<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='报名介绍页配置' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    .form-horizontal .control-label {
        float: left;
        width: 206px;
        padding-top: 5px;
        text-align: right;
    }

    .control-group {
        margin-top: 10px;
        margin-bottom: 80px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>报名介绍</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <div class="control-group">
                <label class="control-label" for="productName">课程ID：</label>
                <div class="controls">
                    <label for="title">
                        <input type="text" value="${(content.lessonId)!''}"
                               name="lessonId" id="lessonId" maxlength="50"
                               style="width: 20%" class="input">
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="productName">背景颜色：</label>
                <div class="controls">
                    <label for="title">
                        <input type="text" value="${(content.bgColor)!''}"
                               name="bgColor" id="bgColor" maxlength="50"
                               style="width: 20%" class="input">
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="productName">头图背景图：</label>
                <div class="controls">
                    <label for="title">
                        <input class="fileUpBtn" type="file"
                               accept="image/gif, image/jpeg, image/png, image/jpg"
                               style="float: left"
                               name="bannerBackgroundImg"
                        />
                    </label>
                    <img src="${(content.bannerBackgroundImg!'')}" id="bannerBackgroundImg" data-file_name="${(bannerBackgroundImgFile!'')}"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="productName">头图：</label>
                <div class="controls">
                    <label for="title">
                        <input class="fileUpBtn" type="file"
                               accept="image/gif, image/jpeg, image/png, image/jpg"
                               style="float: left"
                               name="headImg"
                        />
                    </label>
                    <img src="${(content.headImg!'')}" id="headImg" data-file_name="${(headImgFile!'')}"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">第一部分内容：</label>
                <div class="controls">
                    <label for="title">
                        <!-- 加载编辑器的容器 -->
                        <script id="container_first" type="text/plain"></script>
                    </label>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">课程大纲内容：</label>
                <div class="controls">
                    <label for="title">
                        <!-- 加载编辑器的容器 -->
                        <script id="container_lesson" type="text/plain"></script>
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="productName">课程大纲按钮图：</label>
                <div class="controls">
                    <label for="title">
                        <input class="content_fileUpBtn" type="file"
                               accept="image/gif, image/jpeg, image/png, image/jpg"
                               style="float: left"
                               name="lessonContentImg"
                        />
                    </label>
                    <img src="${(content.lessonContentButtonImg!'')}" id="lessonContentImg"
                         data-file_name="${(contentImgFile!'')}"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">第二部分内容：</label>
                <div class="controls">
                    <!-- 加载编辑器的容器 -->
                    <script id="container_second" type="text/plain"></script>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">底部按钮颜色（填写色值）：</label>
                <div class="controls">
                    <label for="title">
                        <input type="text" id="picker" value="${(content.buttonColor!'')}"/>
                    </label>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <input type="button" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">


    $(function () {

        var ue_first = UE.getEditor('container_first', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'insertvideo', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });


        var ue_second = UE.getEditor('container_second', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload','insertvideo', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });


        var ue_lesson = UE.getEditor('container_lesson', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload','insertvideo', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });
        <#if content??&&content.firstContent??>
            ue_first.ready(function () {
                var firstContent = '${content.firstContent!''}';
                if (firstContent) {
                    ue_first.setContent(firstContent.replace(/\n/g, '<p><br/></p>'));
                }
            });
        </#if>

        <#if content??&&content.secondContent??>
            ue_second.ready(function () {
                var secondContent = '${content.secondContent!''}';
                if (secondContent) {
                    ue_second.setContent(secondContent.replace(/\n/g, '<p><br/></p>'));
                }
            });
        </#if>

        <#if content??&&content.lessonContent??>
            ue_lesson.ready(function () {
                var lessonContent = '${content.lessonContent!''}';
                if (lessonContent) {
                    ue_lesson.setContent(lessonContent.replace(/\n/g, '<p><br/></p>'));
                }
            });
        </#if>
        //上传图片
        $(".fileUpBtn").change(function () {

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
                    url: 'uploadBgImg.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
//                            var img_html = '<img src="' + data.imgUrl + '" data-file_name="' + data.imgName + '">';
                            $("#headImg").attr('src', data.imgUrl);
                            $("#headImg").data("file_name", data.imgName);

                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });
        //上传图片
        $(".content_fileUpBtn").change(function () {

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
                    url: 'uploadBgImg.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
//                            var img_html = '<img src="' + data.imgUrl + '" data-file_name="' + data.imgName + '">';
                            $("#lessonContentImg").attr('src', data.imgUrl);
                            $("#lessonContentImg").data("file_name", data.imgName);

                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });


        $('#lessonId').blur(function () {
            var lessonId = $('#lessonId').val();
            if (lessonId) {
                $.ajax({
                    type: 'post',
                    url: 'check_lesson.vpage',
                    data: {
                        lesson_id: lessonId
                    },
                    success: function (data) {
                        if (!data.success) {
                            alert(data.info);
                            return;
                        }
                    }
                });
            }
        });

        $('#saveBtn').on('click', function () {
            var lessonId = $('#lessonId').val();
            var bgColor = $('#bgColor').val();
            var headImg = $("#headImg").data("file_name");
            var lessonContentImg = $("#lessonContentImg").data("file_name");
            var firstContent = ue_first.getContent();
            var secondContent = ue_second.getContent();
            var lessonContent = ue_lesson.getContent();
            var picker = $('#picker').val();
            var postData = {
                lessonId: lessonId,
                bgColor: bgColor,
                headImg: headImg,
                lessonContentImg: lessonContentImg,
                firstContent: firstContent,
                secondContent: secondContent,
                lessonContent: lessonContent,
                buttonColor: picker
            };

            //数据校验
            if (!lessonId) {
                alert("lessonId不能为空");
                return false;
            }
            if (!bgColor) {
                alert("背景颜色不能为空");
                return false;
            }
            if (!headImg) {
                alert("头图不能为空");
                return false;
            }
            if (!firstContent) {
                alert("第一部分内容不能为空");
                return false;
            }
            if (!secondContent) {
                alert("第二部分内容不能为空");
                return false;
            }
            if (!picker) {
                alert("底部按钮颜色不能为空");
                return false;
            }

            $.getUrlParam = function (name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]);
                return null;
            };
            $.post('check_lesson.vpage', {lesson_id: lessonId}, function (data) {
                if (!data.success) {
                    alert(data.info);
                }
            });

            $.post('saveJoinUpPageDetail.vpage', postData, function (data) {
                if (data.success) {
                    var currentPage = $.getUrlParam('currentPage');
                    location.href = 'joinUpPageList.vpage?page=' + currentPage;
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>