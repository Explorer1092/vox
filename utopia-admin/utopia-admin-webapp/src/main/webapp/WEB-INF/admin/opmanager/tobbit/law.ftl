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
                <legend>语音</legend>
                <div class="controls">
                    <fieldset>
                        <div class="control-group" data-type="voice">
                            <label class="control-label">语音：</label>
                            <input type="text" required="required" placeholder="语音提醒" class="input" name="voice"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="audio-upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </fieldset>
                </div>
                <legend>背景</legend>
                <div class="controls">
                    <fieldset>
                        <div class="control-group" data-type="image">
                            <label class="control-label">背景：</label>
                            <input type="text" required="required" placeholder="背景" class="input" name="background"
                                   value=""/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="audio-upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </fieldset>
                </div>
                <legend>问题</legend>
                <div class="controls" data-type="problem">
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label">图片：</label>
                            <input type="text" required="required" placeholder="图片" class="input"
                                   name="p_i"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="audio-upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                        <div class="control-group">
                            <label class="control-label">X：</label>
                            <table style="margin: 0">
                                <tr>
                                    <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                        <input type="number" style="width: 80px" required="required"
                                               placeholder="X" class="input" name="p_x"/>
                                    </td>
                                    <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                        <label class="control-label" style="width: 25px;">Y：</label>
                                        <input type="number" style="width: 80px" required="required"
                                               placeholder="Y" class="input" name="p_y"/>
                                    </td>
                                    <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                        <label class="control-label" style="width: 25px;">W：</label>
                                        <input type="number" style="width: 80px" required="required"
                                               placeholder="W" class="input" name="p_w"/>
                                    </td>
                                    <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                        <label class="control-label" style="width: 25px;">H：</label>
                                        <input type="number" style="width: 80px" required="required"
                                               placeholder="H" class="input" name="p_h"/>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </fieldset>
                </div>
                <fieldset>
                    <legend>选项</legend>
                    <fieldset data-type="questions" data-label="选项">
                        <div class="control-group" data-type="data-item">
                            <label class="control-label">选项1</label>
                            <div class="controls">
                                <fieldset>
                                    <div class="control-group">
                                        <label class="control-label">图片：</label>
                                        <input type="text" required="required" placeholder="图片" class="input"
                                               name="q_i"/>
                                        <a href="javascript:void(null);" class="text-primary" data-index="2"
                                           data-action="audio-upload">上传</a>
                                        <a href="javascript:void(null);" class="text-success" data-index="2"
                                           data-action="preview">预览</a>
                                        <button type="button" data-action="remove" class="btn-danger">移除</button>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">X：</label>
                                        <table style="margin: 0">
                                            <tr>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="X" class="input" name="q_x"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">Y：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="Y" class="input" name="q_y"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">W：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="W" class="input" name="q_w"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">H：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="H" class="input" name="q_h"/>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                                </fieldset>
                            </div>
                            <label class="control-label">答案</label>
                            <div class="controls">
                                <fieldset>
                                    <div class="control-group">
                                        <label class="control-label">图片：</label>
                                        <input type="text" required="required" placeholder="图片" class="input"
                                               name="a_i"/>
                                        <a href="javascript:void(null);" class="text-primary" data-index="2"
                                           data-action="audio-upload">上传</a>
                                        <a href="javascript:void(null);" class="text-success" data-index="2"
                                           data-action="preview">预览</a>

                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">X：</label>
                                        <table style="margin: 0">
                                            <tr>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="X" class="input" name="a_x"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">Y：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="Y" class="input" name="a_y"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">W：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="W" class="input" name="a_w"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">H：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="H" class="input" name="a_h"/>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                                </fieldset>
                            </div>
                        </div>
                    </fieldset>
                </fieldset>

                <div class="control-group">
                    <fieldset>
                        <div class="control-label">
                            <button type="button" class="btn-success" data-action="append_question">新增问题</button>
                        </div>
                    </fieldset>
                </div>

                <fieldset>
                    <legend>干扰</legend>
                    <fieldset data-type="distractors" data-label="干扰">
                        <div class="control-group" data-type="data-item">
                            <label class="control-label">干扰1</label>
                            <div class="controls">
                                <fieldset>
                                    <div class="control-group">
                                        <label class="control-label">图片：</label>
                                        <input type="text" required="required" placeholder="图片" class="input"
                                               name="d_i"/>
                                        <a href="javascript:void(null);" class="text-primary" data-index="2"
                                           data-action="audio-upload">上传</a>
                                        <a href="javascript:void(null);" class="text-success" data-index="2"
                                           data-action="preview">预览</a>
                                        <button type="button" data-action="remove" class="btn-danger">移除</button>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">X：</label>
                                        <table style="margin: 0">
                                            <tr>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="X" class="input" name="d_x"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">Y：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="Y" class="input" name="d_y"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">W：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="W" class="input" name="d_w"/>
                                                </td>
                                                <td style="margin: 0; padding-left: 0; padding-top: 0;">
                                                    <label class="control-label" style="width: 25px;">H：</label>
                                                    <input type="number" style="width: 80px" required="required"
                                                           placeholder="H" class="input" name="d_h"/>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                                </fieldset>
                            </div>
                        </div>
                    </fieldset>
                </fieldset>

                <div class="control-group">
                    <fieldset>
                        <div class="control-label">
                            <button type="button" class="btn-success" data-action="append_distractors">新增干扰</button>
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

        var itemLoader = function (wrapper, item, event) {
            item.find("input").val("");
            item.find("label:first").text(wrapper.attr("data-label") + wrapper.find("[data-type='data-item']").length);
            item.find("[data-action='remove']").click(function () {

                if (wrapper.find("[data-type='data-item']").length === 1) {
                    return;
                }

                item.remove();
                var items = wrapper.find("[data-type='data-item']");
                for (var index = 0; index < items.length; index++) {
                    var text = wrapper.attr("data-label") + (index + 1);
                    items.eq(index).find("label:first").text(text);
                }
            });
            if (null != event) {
                event(item);
            }
        };

        var appender = function (wrapper, event) {
            var item = wrapper
                    .find("[data-type='data-item']:first")
                    .clone()
                    .appendTo(wrapper);

            itemLoader(wrapper, item, event);
            return item;
        }

        var starter = function (wrapper, event, button, binder, data) {
            var item = wrapper.find("[data-type='data-item']:first");
            itemLoader(wrapper, item, event);

            button.click(function () {
                appender(wrapper, event);
            });

            if (binder != null && data != null) {
                var length = data.length;
                for (var i = 1; i < length; i++) {
                    button.click();
                }
                var items = wrapper.find("[data-type='data-item']");
                for (var i = 0; i < length; i++) {
                    binder(items.eq(i), data[i]);
                }
            }

        };

        var uploader = function (object) {
            object.find("[data-action='audio-upload']").click(function () {
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
            object.find("[data-action='preview']").click(function () {
                var image = $(this).parent().find("input").first().val();
                if (image == '') {
                    return;
                }

                window.open(image, "newwindow");
            });
        };

        var questionBinder = function (item, data) {
            if (null == item || null == data) {
                return;
            }

            item.find("[name='q_i']").val(data.question.path);
            item.find("[name='q_x']").val(data.question.x);
            item.find("[name='q_y']").val(data.question.y);
            item.find("[name='q_w']").val(data.question.w);
            item.find("[name='q_h']").val(data.question.h);

            item.find("[name='a_i']").val(data.answer.path);
            item.find("[name='a_x']").val(data.answer.x);
            item.find("[name='a_y']").val(data.answer.y);
            item.find("[name='a_w']").val(data.answer.w);
            item.find("[name='a_h']").val(data.answer.h);

        }


        var distractorBinder = function (item, data) {
            if (null == item || null == data) {
                return;
            }
            item.find("[name='d_i']").val(data.path);
            item.find("[name='d_x']").val(data.x);
            item.find("[name='d_y']").val(data.y);
            item.find("[name='d_w']").val(data.w);
            item.find("[name='d_h']").val(data.h);
        }

        var str = $("[name='game']").val();
        var context = null;
        if (null != str && '' != str) {
            context = JSON.parse(str);
            $("[name='background']").val(context.image);
            $("[name='voice']").val(context.voice);
            if(null != context.problem){
                $("[name='p_i']").val(context.problem.path);
                $("[name='p_x']").val(context.problem.x);
                $("[name='p_y']").val(context.problem.y);
                $("[name='p_w']").val(context.problem.w);
                $("[name='p_h']").val(context.problem.h);
            }
        }

        var question_container = $("[data-type='questions']");
        var question_append_button = $("[data-action='append_question']");
        var questionContext = null == context ? null : context.items;
        starter(question_container, uploader, question_append_button, questionBinder, questionContext);

        var distractor_container = $("[data-type='distractors']");
        var distractor_append_button = $("[data-action='append_distractors']");
        var distractorContext = null == context ? null : context.others;

        starter(distractor_container, uploader, distractor_append_button, distractorBinder, distractorContext);

        uploader($("[data-type='image']"));
        uploader($("[data-type='voice']"));
        uploader($("[data-type='problem']"));


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