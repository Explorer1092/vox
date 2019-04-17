<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='电子书详情编辑' page_num=9>
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
        <legend>电子书详情</legend>
    </fieldset>
<#--<div class="span12">-->
    <div class="control-group">
        <label class="control-label" for="productName">bookId：</label>
        <label for="title">
            <input type="text" value="${bookId!''}"
                   name="bookId" id="bookId" maxlength="50"
                   style="width: 20%" class="input">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">页码：</label>
        <label for="title">
            <input type="text" value="${pageNum!''}"
                   name="pageNum" id="pageNum" maxlength="50"
                   style="width: 20%" class="input">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">页面Id：</label>
        <label for="title">
            <input type="text" value="${id!''}"
                   name="pageId" id="pageId" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">内容类型：</label>
        <div class="control-group span3">
            <div class="radio">
                <label>
                    <input type="radio" name="optionsRadios" id="textRadio" value="1">
                    纯文本
                </label>
            </div>
        </div>
        <div class="span3 control-group">
            <div class="radio">
                <label>
                    <input type="radio" name="optionsRadios" id="picRadio" value="2">
                    图片+音频
                </label>
            </div>
        </div>
        <div class="span3 control-group">
            <div class="radio">
                <label>
                    <input type="radio" name="optionsRadios" id="textAudioRadios" value="3">
                    文本+音频
                </label>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="text-content-area">
                <div class="text-content-1">
                    <div class="control-group">
                        <label class="control-label" for="productName">一级标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="firstTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">二级标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="secondTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">三级标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="thirdTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div style="margin-top: 10px">
                        <label class="control-label" for="productName">文本内容：</label>
                        <div class="controls">
                            <label for="content">
                                <textarea name="content" rows="10" cols="30" style="width: 1000px"></textarea>
                            </label>
                        </div>
                    </div>
                    <div class="control-group poetry">
                        <label class="control-label" for="productName">诗词标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="poetryTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group poetry">
                        <label class="control-label" for="productName">诗词作者：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="poetryAuthor" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group peotryEntity poetry">
                        <label class="control-label" for="productName">诗词内容：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="peotryEntity" maxlength="200"
                                       style="width: 40%" class="input">
                            </label>
                        </div>
                    </div>
                    <div style="text-align: left" class="cp1 poetry">
                        <button name="clonePeotry-1" class="btn btn-small btn-warning">再来一块诗词内容</button>
                        <button name="delPeotry-1" class="btn btn-small btn-danger">删除一块诗词内容</button>
                    </div>
                </div>
            </div>
            <div style="text-align: left;margin-top: 20px" id="ct1">
                <input type="button" id="cloneText-1" value="再来一块文本" class="btn btn-small btn-success">
                <input type="button" id="delText-1" value="删除一块文本" class="btn btn-small btn-danger">
            </div>
            <div class="pic-area">
                <div class="control-group">
                    <label class="control-label" for="productName">内容图片：</label>
                    <div class="controls">
                        <label for="title">
                            <input class="fileUpBtn" type="file"
                                   accept="image/gif, image/jpeg, image/png, image/jpg"
                                   style="float: left"
                                   name="uploadImg"
                            />
                        </label>
                        <img src="" id="contentImg" data-file_name=""/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="productName">内容音频：</label>
                    <div class="controls">
                        <label for="title">
                            <input type="text"
                                   style="float: left"
                                   name="audio"
                                   id="audio-1"
                            />
                            <span><button type="button" name="preview_video"
                                          class="btn btn-success btn-small">预览</button></span>
                            <span style="color:red;">请输入“https://”地址</span>
                        </label>
                    </div>
                </div>
            </div>
            <div class="audio-text-area">
                <div class="control-group">
                    <label class="control-label" for="productName">内容音频：</label>
                    <div class="controls">
                        <label for="title">
                            <input type="text"
                                   style="float: left"
                                   name="audio"
                                   id="audio-2"
                            />
                            <span><button type="button" name="preview_video"
                                          class="btn btn-success btn-small">预览</button></span>
                            <span style="color:red;">请输入“https://”地址</span>
                        </label>
                    </div>
                </div>
                <div class="text-content-2">
                    <div class="control-group">
                        <label class="control-label" for="productName">一级标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="firstTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">二级标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="secondTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">三级标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="thirdTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div style="margin-top: 10px">
                        <label class="control-label" for="productName">文本内容：</label>
                        <div class="controls">
                            <label for="content">
                                <textarea name="content" rows="10" cols="30" style="width: 1000px"></textarea>
                            </label>
                        </div>
                    </div>
                    <div class="control-group poetry">
                        <label class="control-label" for="productName">诗词标题：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="poetryTitle" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group poetry">
                        <label class="control-label" for="productName">诗词作者：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="poetryAuthor" maxlength="50"
                                       style="width: 20%" class="input">
                            </label>
                        </div>
                    </div>
                    <div class="control-group peotryEntity poetry">
                        <label class="control-label" for="productName">诗词内容：</label>
                        <div class="controls">
                            <label for="title">
                                <input type="text" value=""
                                       name="peotryEntity" maxlength="200"
                                       style="width: 40%" class="input">
                            </label>
                        </div>
                    </div>
                    <div style="text-align: left;margin-top: 20px" class="cp1 poetry">
                        <button name="clonePeotry-1" class="btn btn-small btn-warning">再来一块诗词内容</button>
                        <button name="delPeotry-1" class="btn btn-small btn-danger">删除一块诗词内容</button>
                    </div>
                </div>
            </div>
            <div style="text-align: left;margin-top: 20px" id="ct2">
                <input type="button" id="cloneText-2" value="再来一块文本" class="btn btn-small btn-success">
                <input type="button" id="delText-2" value="删除一块文本" class="btn btn-small btn-danger">
            </div>
            <div class="control-group comment">
                <label class="control-label" for="productName">说明内容：</label>
                <div class="controls">
                    <label for="comment">
                        <input type="text" value="${comment!''}"
                               name="comment" maxlength="64"
                               style="width: 50%" class="input">
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


        $(function () {
            var show = "${pageType!'1'}";
            var bookType = "${bookType!'0'}";
            if (bookType !== '1') {
                $(".poetry").css("display", "none");
            }
            var showType = $("input[name='optionsRadios']");
            $("input:radio[name='optionsRadios'][value=" + show + "]").attr("checked", true);
            showSwitch(show);
            console.log($(".text-content-1 .cp1").html());
            if (show === '1') {
                <#if textContent??&&textContent?size gt 0>
                    <#list textContent as content>
                        <#if (content_index?number == 0)>
                            $(".text-content-1 input[name='firstTitle']").val("${content.firstTitle!''}");
                            $(".text-content-1 input[name='secondTitle']").val("${content.secondTitle!''}");
                            $(".text-content-1 input[name='thirdTitle']").val("${content.thirdTitle!''}");
                            $(".text-content-1 textarea[name='content']").val(`${content.content!''}`);
                            $(".text-content-1 input[name='poetryTitle']").val("${content.poetryTitle!''}");
                            $(".text-content-1 input[name='poetryAuthor']").val("${content.poetryAuthor!''}");
                            <#if content.poetryEntity??&&content.poetryEntity?size gt 0>
                                <#list content.poetryEntity as entity>
                                    <#if (entity_index?number == 0)>
                                    $(".text-content-1 .peotryEntity input[name='peotryEntity']").val(`${entity!''}`);
                                    <#else>
                                         var cloneEntity1 = $(".text-content-1 .peotryEntity:first").clone();
                                         cloneEntity1.find("input").val(`${entity!''}`);
                                         cloneEntity1.insertBefore(".text-content-1 .cp1");
                                        // $(".text-content-1 .cp1").prepend(cloneEntity1);
                                    </#if>
                                </#list>
                            </#if>
                        <#else>
                            var cloneDiv1 = $(".text-content-1:first").clone();
                            cloneDiv1.find("input,textarea").each(function () {
                                $(this).val("");
                            });
                            cloneDiv1.find(".peotryEntity").each(function (index) {
                                if (index === 0) {
                                    return true;
                                }
                                $(this).remove();
                            });
                            cloneDiv1.find("input[name='firstTitle']").val("${content.firstTitle!''}");
                            cloneDiv1.find("input[name='secondTitle']").val("${content.secondTitle!''}");
                            cloneDiv1.find("input[name='thirdTitle']").val("${content.thirdTitle!''}");
                            cloneDiv1.find("textarea[name='content']").val(`${content.content!''}`);
                            cloneDiv1.find("input[name='poetryTitle']").val("${content.poetryTitle!''}");
                            cloneDiv1.find("input[name='poetryAuthor']").val("${content.poetryAuthor!''}");
                            <#if content.poetryEntity??&&content.poetryEntity?size gt 0>
                                <#list content.poetryEntity as entity>
                                    <#if (entity_index?number == 0)>
                                        cloneDiv1.find(".peotryEntity input[name='peotryEntity']").val(`${entity!''}`);
                                    <#else>
                                         var cloneEntity1_1 = cloneDiv1.find(".peotryEntity:first").clone();
                                         cloneEntity1_1.find("input").each(function () {
                                             $(this).val("");
                                         });
                                         cloneEntity1_1.find("input[name='peotryEntity']").val(`${entity!''}`);
                                         cloneEntity1_1.insertBefore(cloneDiv1.find(".cp1"));
                                        // cloneDiv1.find(".cp1").prepend(cloneEntity1_1);
                                    </#if>
                                </#list>
                            </#if>
                            $(".text-content-area").append(cloneDiv1);
                        </#if>
                    </#list>
                </#if>
            }
            if (show === '2') {
                <#if audioUrl??>
                    $("#audio-1").val("${audioUrl!''}");
                </#if>
                <#if contentImg_url??>
                    $("#contentImg").attr("src", "${contentImg_url!''}");
                    $("#contentImg").attr("data-file_name", "${contentImg_file!''}");
                </#if>
            }
            if (show === '3') {
                <#if audioUrl??>
                    $("#audio-2").val("${audioUrl!''}");
                </#if>
                <#if textContent??&&textContent?size gt 0>
                    <#list textContent as content>
                        <#if (content_index?number == 0)>
                            $(".text-content-2 input[name='firstTitle']").val("${content.firstTitle!''}");
                            $(".text-content-2 input[name='secondTitle']").val("${content.secondTitle!''}");
                            $(".text-content-2 input[name='thirdTitle']").val("${content.thirdTitle!''}");
                            $(".text-content-2 textarea[name='content']").val(`${content.content!''}`);
                            $(".text-content-2 input[name='poetryTitle']").val("${content.poetryTitle!''}");
                            $(".text-content-2 input[name='poetryAuthor']").val("${content.poetryAuthor!''}");
                            <#if content.poetryEntity??&&content.poetryEntity?size gt 0>
                                <#list content.poetryEntity as entity>
                                    <#if (entity_index?number == 0)>
                                    $(".text-content-2 .peotryEntity input[name='peotryEntity']").val(`${entity!''}`);
                                    <#else>
                                         var cloneEntity2 = $(".text-content-2 .peotryEntity:first").clone();
                                         cloneEntity2.find("input").val(`${entity!''}`);
                                         cloneEntity2.insertBefore(".text-content-1 .cp1");
                                        // $(".text-content-2 .cp1").prepend(cloneEntity2);
                                    </#if>
                                </#list>
                            </#if>
                        <#else>
                            var cloneDiv2 = $(".text-content-2:first").clone();
                            cloneDiv2.find("input,textarea").each(function () {
                                $(this).val("");
                            });
                            cloneDiv2.find(".peotryEntity").each(function (index) {
                                if (index === 0) {
                                    return true;
                                }
                                $(this).remove();
                            });
                            cloneDiv2.find("input[name='firstTitle']").val("${content.firstTitle!''}");
                            cloneDiv2.find("input[name='secondTitle']").val("${content.secondTitle!''}");
                            cloneDiv2.find("input[name='thirdTitle']").val("${content.thirdTitle!''}");
                            cloneDiv2.find("textarea[name='content']").val(`${content.content!''}`);
                            cloneDiv2.find("input[name='poetryTitle']").val("${content.poetryTitle!''}");
                            cloneDiv2.find("input[name='poetryAuthor']").val("${content.poetryAuthor!''}");
                            <#if content.poetryEntity??&&content.poetryEntity?size gt 0>
                                <#list content.poetryEntity as entity>
                                    <#if (entity_index?number == 0)>
                                        cloneDiv2.find(".peotryEntity input[name='peotryEntity']").val(`${entity!''}`);
                                    <#else>
                                         var cloneEntity2_1 = cloneDiv2.find(".peotryEntity:first").clone();
                                         cloneEntity2_1.find("input").each(function () {
                                             $(this).val("");
                                         });
                                         cloneEntity2_1.find("input[name='peotryEntity']").val(`${entity!''}`);
                                         cloneEntity1_1.insertBefore(cloneDiv2.find(".cp1"));
                                        // cloneDiv2.find(".cp1").prepend(cloneEntity2_1);
                                    </#if>
                                </#list>
                            </#if>
                            $(".audio-text-area").append(cloneDiv2);
                        </#if>
                    </#list>
                </#if>
            }

            showType.on('click', function () {
                show = $(this).val();
                showSwitch(show);
            });

            $('button[name="preview_video"]').click(function () {
                var audio1 = $('#audio-1').val();
                var audio2 = $('#audio-2').val();
                var src = '';
                if (audio1) {
                    src = audio1;
                } else if (audio2) {
                    src = audio2;
                }
                $('#myVideoModal').modal({
                    show: true,
                    backdrop: 'static'
                });
                $('#myVideoModal video').attr('src', src);
            });

            $("#cloneText-1").on('click', function () {
                var cloneDiv = $(".text-content-1:first").clone();
                cloneDiv.find("input,textarea").each(function () {
                    $(this).val("");
                });
                cloneDiv.find(".peotryEntity").each(function (index) {
                    if (index === 0) {
                        return true;
                    }
                    $(this).remove();
                });
                $(".text-content-area").append(cloneDiv);
            });

            $("#cloneText-2").on('click', function () {
                var cloneDiv2 = $(".text-content-2:first").clone();
                cloneDiv2.find("input,textarea").each(function () {
                    $(this).val("");
                });
                cloneDiv2.find(".peotryEntity").each(function (index) {
                    if (index === 0) {
                        return true;
                    }
                    $(this).remove();
                });
                $(".audio-text-area").append(cloneDiv2.clone(false));
            });


            $("#delText-1").on('click', function () {
                var length = $(".text-content-1").length;
                if (length < 2) {
                    alert("就剩一个了哦~~~~");
                    return false;
                }
                $(".text-content-1:last").remove();
            });
            $("#delText-2").on('click', function () {
                var length = $(".text-content-2").length;
                if (length < 2) {
                    alert("就剩一个了哦~~~~");
                    return false;
                }
                $(".text-content-2:last").remove();
            });


            $(".text-content-area").on('click', "button[name='clonePeotry-1']", function () {
                var $perParent = $(this).parent().parent();
                var pe = $perParent.find(".peotryEntity:first").clone();
                pe.find("input:first").val("");
                pe.insertBefore($(this).parent());
            });

            $(".text-content-area").on('click', "button[name='delPeotry-1']", function () {
                var $perParent = $(this).parent().parent();
                var length = $perParent.find(".peotryEntity").length;
                if (length < 2) {
                    alert("就剩一个了哦~~~~");
                    return false;
                }
                $perParent.find(".peotryEntity:last").remove();
            });

            $(".audio-text-area").on('click', "button[name='delPeotry-1']", function () {
                var $perParent = $(this).parent().parent();
                var length = $perParent.find(".peotryEntity").length;
                if (length < 2) {
                    alert("就剩一个了哦~~~~");
                    return false;
                }
                $perParent.find(".peotryEntity:last").remove();
            });

            $(".audio-text-area").on('click', "button[name='clonePeotry-1']", function () {
                var $perParent = $(this).parent().parent();
                var pe = $perParent.find(".peotryEntity:first").clone();
                pe.find("input:first").val("");
                pe.insertBefore($(this).parent());
                // $(this).parent().parent().find(".cp1").prepend(pe);
            });

            //上传图片
            $(".fileUpBtn").change(function () {

                var $this = $(this);
                var ext = $this.val().split('.').pop().toLowerCase();
                if ($this.val() !== '') {
                    if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) === -1) {
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
                        url: 'uploadImg.vpage',
                        type: 'POST',
                        data: formData,
                        processData: false,
                        contentType: false,
                        success: function (data) {
                            if (data.success) {
//                            var img_html = '<img src="' + data.imgUrl + '" data-file_name="' + data.imgName + '">';
                                $("#contentImg").attr('src', data.imgUrl);
                                $("#contentImg").data("file_name", data.imgName);

                            } else {
                                alert("上传失败");
                            }
                        }
                    });
                }
            });


            $('#bookId').blur(function () {
                var bookId = $('#bookId').val();
                if (bookId) {
                    $.ajax({
                        type: 'post',
                        url: 'checkBookId.vpage',
                        data: {
                            bookId: bookId
                        },
                        success: function (data) {
                            if (!data.success) {
                                alert(data.info);

                            }
                        }
                    });
                }
            });

            $('#saveBtn').on('click', function () {
                var bookId = $('#bookId').val();
                // var bgColor = $('#bgColor').val();
                // var headImg = $("#headImg").data("file_name");
                // var lessonContentImg = $("#lessonContentImg").data("file_name");
                // var picker = $('#picker').val();
                var textContentArray = [];
                var contentImg = "";
                var contentAudio = "";
                var pageNum = $("#pageNum").val();
                var pageId = $("#pageId").val();
                var comment = $("input[name='comment']").val();
                switch (show) {
                    case '1':
                        $(".text-content-area .text-content-1").each(function () {
                            var firstTitle = $(this).find("input[name='firstTitle']").val();
                            var secondTitle = $(this).find("input[name='secondTitle']").val();
                            var thirdTitle = $(this).find("input[name='thirdTitle']").val();
                            var content = $(this).find("textarea[name='content']").val();
                            var poetryTitle = $(this).find("input[name='poetryTitle']").val();
                            var poetryAuthor = $(this).find("input[name='poetryAuthor']").val();
                            var poetryEntity = [];
                            $(this).find("input[name='peotryEntity']").each(function () {
                                poetryEntity.push($(this).val());
                            });
                            if (!firstTitle && !secondTitle && !thirdTitle && !content && !poetryTitle && !poetryAuthor && !poetryEntity) {
                                return true;
                            }
                            var textContent = {
                                first_title: firstTitle,
                                second_title: secondTitle,
                                third_title: thirdTitle,
                                content: content,
                                poetry_title: poetryTitle,
                                poetry_author: poetryAuthor,
                                poetry_entity: poetryEntity
                            };
                            textContentArray.push(textContent);
                        });
                        break;
                    case '2':
                        contentImg = $("#contentImg").data("file_name");
                        contentAudio = $("#audio-1").val();
                        if (!contentAudio) {
                            alert("音频不能为空");
                            return;
                        }
                        break;
                    case '3':
                        contentAudio = $("#audio-2").val();
                        if (!contentAudio) {
                            alert("音频不能为空");
                            return;
                        }
                        $(".audio-text-area .text-content-2").each(function () {
                            var firstTitle = $(this).find("input[name='firstTitle']").val();
                            var secondTitle = $(this).find("input[name='secondTitle']").val();
                            var thirdTitle = $(this).find("input[name='thirdTitle']").val();
                            var content = $(this).find("textarea[name='content']").val();
                            var poetryTitle = $(this).find("input[name='poetryTitle']").val();
                            var poetryAuthor = $(this).find("input[name='poetryAuthor']").val();
                            var poetryEntity = [];
                            $(this).find("input[name='peotryEntity']").each(function () {
                                poetryEntity.push($(this).val());
                            });
                            if (!firstTitle && !secondTitle && !thirdTitle && !content && !poetryTitle && !poetryAuthor && !poetryEntity) {
                                return true;
                            }
                            var textContent = {
                                first_title: firstTitle,
                                second_title: secondTitle,
                                third_title: thirdTitle,
                                content: content,
                                poetry_title: poetryTitle,
                                poetry_author: poetryAuthor,
                                poetry_entity: poetryEntity
                            };
                            textContentArray.push(textContent);
                        });
                        break;
                    default:
                        alert("获取页面数据错误");
                        break;
                }

                console.log(textContentArray);
                console.log(show);
                console.log(contentImg);
                console.log(contentAudio);
                var postData = {
                    bookId: bookId,
                    contentImg: contentImg,
                    contentAudio: contentAudio,
                    textContentArray: JSON.stringify(textContentArray),
                    pageType: show,
                    pageNum: pageNum,
                    pageId: pageId,
                    comment: comment
                };
                // //数据校验
                if (!bookId) {
                    alert("bookId不能为空");
                    return false;
                }
                if ((show === '2' || show === '3') && !checkURL(contentAudio)) {
                    alert("输入的音、视频地址不合法，请输入https地址！");
                    return false;
                }
                if ((show === '1' || show === '3') && (textContentArray.length === 0 || (!textContentArray[0].content && !textContentArray[0].poetry_entity[0]))) {
                    alert("书页文本内容为空，请检查");
                    return false;
                }
                $.getUrlParam = function (name) {
                    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                    var r = window.location.search.substr(1).match(reg);
                    if (r != null) return unescape(r[2]);
                    return null;
                };
                // $.post('check_lesson.vpage', {lesson_id: lessonId}, function (data) {
                //     if (!data.success) {
                //         alert(data.info);
                //     }
                // });
                //
                $.post('saveBookPageDetail.vpage', postData, function (data) {
                    if (data.success) {
                        var currentPage = $.getUrlParam('currentPage');
                        location.href = 'pageList.vpage?page=' + currentPage;
                    } else {
                        if (data.page_error) {
                            confirm(data.page_error);
                            location.href = 'pageList.vpage?page=' + currentPage;
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });
        });

        function showSwitch(showType) {
            switch (showType) {
                case '1':
                    $(".pic-area").css("display", "none");
                    $(".audio-text-area").css("display", "none");
                    $("#ct2").css("display", "none");
                    $(".text-content-area").css("display", "block");
                    $("#ct1").css("display", "block");
                    // document.getElementById("img2").style.display="block";
                    break;
                case '2':
                    $(".pic-area").css("display", "block");
                    $(".audio-text-area").css("display", "none");
                    $("#ct2").css("display", "none");
                    $(".text-content-area").css("display", "none");
                    $("#ct1").css("display", "none");
                    break;
                case '3':
                    $(".pic-area").css("display", "none");
                    $(".audio-text-area").css("display", "block");
                    $("#ct2").css("display", "block");
                    $(".text-content-area").css("display", "none");
                    $("#ct1").css("display", "none");
                    break;
                default:
                    break;
            }
        }

        function checkURL(URL) {
            var str = URL;
            //判断URL地址的正则表达式为:http(s)?://([\w-]+\.)+[\w-]+(/[\w- ./?%&=]*)?
            //下面的代码中应用了转义字符"\"输出一个字符"/"
            var Expression = /^(https)?:\/\/([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/;
            var objExp = new RegExp(Expression);
            return objExp.test(str) === true;
        }
    </script>
</@layout_default.page>