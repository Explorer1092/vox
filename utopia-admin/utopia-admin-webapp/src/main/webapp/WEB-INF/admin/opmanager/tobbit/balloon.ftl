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
                    <legend>选项</legend>
                    <fieldset data-type="questions" data-label="选项">
                        <div class="control-group" data-type="data-item">
                            <label class="control-label">选项1</label>
                            <div class="controls">
                                <fieldset>
                                    <div class="control-group">
                                        <label class="control-label">问题：</label>
                                        <input type="text" required="required" placeholder="问题" class="input"
                                               name="question"/>
                                        <button type="button" data-action="remove" class="btn-danger">移除</button>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">语音提示：</label>
                                        <input type="text" required="required" placeholder="语音提示" class="input"
                                               name="audio"/>
                                        <a href="javascript:void(null);" class="text-primary" data-index="2"
                                           data-action="audio-upload">上传</a>
                                        <a href="javascript:void(null);" class="text-success" data-index="2"
                                           data-action="preview">预览</a>
                                    </div>
                                    <div class="control-group">
                                        <label class="control-label">正确答案 ：</label>
                                        <input type="text" required="required" placeholder="正确答案" class="input"
                                               name="answer"/>
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
                    <legend>干扰项</legend>
                    <div class="control-group" data-type="data-item">
                        <fieldset data-type="distractors" data-label="干扰项">
                            <div class="control-group" data-type="data-item">
                                <label class="control-label">干扰项1</label>
                                <div class="controls">
                                    <fieldset>
                                        <div class="control-group">
                                            <label class="control-label">错误答案：</label>
                                            <input type="text" required placeholder="问题" class="input"
                                                   name="distractors"/>
                                            <button type="button" data-action="remove" class="btn-danger">移除</button>
                                        </div>
                                    </fieldset>
                                </div>
                            </div>
                        </fieldset>
                    </div>
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

            item.find("[name='question']").val(data.question);
            item.find("[name='answer']").val(data.answer);
            item.find("[name='audio']").val(data.voice);
        }

        var distractorBinder = function (item, data) {
            if (null == item || null == data) {
                return;
            }
            item.find("[name='distractors']").val(data);
        }

        var str = $("[name='game']").val();
        var context = null;
        if (null != str && '' != str) {
            context = JSON.parse(str);
        }

        var question_container = $("[data-type='questions']");
        var question_append_button = $("[data-action='append_question']");
        var questionContext = null == context ? null : context.problems;
        starter(question_container, uploader, question_append_button, questionBinder, questionContext);


        var distractor_container = $("[data-type='distractors']");
        var distractor_append_button = $("[data-action='append_distractors']");
        var distractorContext = null == context ? null : context.distracters;

        starter(distractor_container, null, distractor_append_button, distractorBinder, distractorContext);


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