<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='托比同步课堂' page_num=9>

<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div class="span9">
    <legend>
        <strong><span class="text-info">托比同步课堂</span>
            &nbsp;/&nbsp;
            <span class="text-info">${level.description}</span>
            &nbsp;/&nbsp;
            <span class="text-info">${subject.value}</span>
            &nbsp;/&nbsp;
            <span class="text-info">${course}</span>
            &nbsp;/&nbsp;
            <span class="text-success">${type.text}</span>
        </strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="gameForm" class="well form-horizontal" method="post"
                  action="/opmanager/tobbit/game/save.vpage?courseId=${courseId}&type=${type.name()}&id=${id!''}">
                <input type="hidden" name="courseId" value="${courseId!''}"/>
                <input type="hidden" name="type" value="${type!''}"/>
                <input type="hidden" name="id" value="${id!''}"/>

                <fieldset>
                    <legend>语音</legend>
                    <div class="controls">
                        <fieldset>
                            <div class="control-group">
                                <label class="control-label">语音：</label>
                                <input type="text" required="required" placeholder="语音提醒" class="input" name="voice"
                                       value="${voice!''}"/>
                                <a href="javascript:void(null);" class="text-primary" data-index="2"
                                   data-action="audio-upload">上传</a>
                                <a href="javascript:void(null);" class="text-success" data-index="2"
                                   data-action="preview">预览</a>
                            </div>
                        </fieldset>
                    </div>
                </fieldset>

                <div class="control-group">
                    <fieldset>
                        <div class="control-label">
                            <button type="button" class="btn-success" data-action="append_question">新增问题</button>
                        </div>
                    </fieldset>
                </div>

                <div class="control-group">
                    <fieldset>
                        <div class="control-label">
                            <button type="button" class="btn-primary btn-large" id="game_save_btn">保存</button>
                        </div>
                    </fieldset>
                </div>
            </form>
        </div>
    </div>
</div>
<textarea style="display: none" name="game">${game!''}</textarea>
<input type="file" style="display: none;" id="uploader" accept="image/gif, image/jpeg, image/png, image/jpg"/>
<script lang="javascript">
    $(document).ready(function () {

        $("[data-action='audio-upload']").click(function () {
            var target = $(this).prev();
            $("#uploader").click().change(function () {
                var form = new FormData();
                form.append("inputFile", $(this).context.files[0]);
                $.ajax({
                    url: "upload.vpage",
                    type: 'POST',
                    data: form,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var path = data.path;
                            target.val(path);
                        } else {
                            alert("上传失败");
                        }
                    },
                    complete: function () {
                        $("#uploader").val("");
                    }
                });
            });
        });


        var str = $("[name='game']").val();
        var context = null;
        if (null != str && '' != str) {
            context = JSON.parse(str);
            $("[name='voice']").val(context.voice);
        }

        var serialize = function (form) {
            var controls = form.find("input");
            var length = controls.length;
            var map = {};
            for (var i = 0; i < length; i++) {
                var control = controls.eq(i);
                var k = control.attr("name");
                var v = control.val();
                if (control.attr("required") === 'required' && v == '') {
                    control.focus();
                    return null;
                }
                var value = map[k];

                if (null != value) {
                    v = value + "," + v;
                }

                map[k] = v;
            }
            return map;
        }

        $("#game_save_btn").click(function () {
            var content = serialize($("#gameForm"));
            $.ajax({
                url: "/opmanager/tobbit/game/save.vpage",
                dataType: "json",
                type: "post",
                data: content,
                success: function (result) {
                    if (result.success) {
                        document.location.reload();
                    } else {
                        alert(result.info);
                    }
                }
            });
        });
        $("[data-action='preview']").click(function () {
            var image = $(this).parent().find("input").first().val();
            if (image == '') {
                return;
            }

            window.open(image, "newwindow");
        });
    });
</script>
</@layout_default.page>