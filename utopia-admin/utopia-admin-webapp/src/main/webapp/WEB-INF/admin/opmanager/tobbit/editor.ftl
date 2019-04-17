<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='托比同步课堂' page_num=9>

<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div class="span9">
    <legend>
        <strong><span class="text-info">托比同步课堂</span>&nbsp;/&nbsp;<span
                class="text-success">${subject.getValue()}</span></strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="courseForm" class="well form-horizontal" method="post" action="/opmanager/tobbit/save.vpage">
                <input type="hidden" name="id" value="${id!''}"/>
                <input type="hidden" name="subject" value="${subject!''}"/>
                <fieldset>
                    <legend>基本信息</legend>
                    <div class="control-group">
                        <label class="control-label">课程名称：</label>
                        <div class="controls">
                            <input type="text" placeholder="课程名称" maxlength="30" class="input" value="${name!''}"
                                   name="name" id="name"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">试用年级：</label>
                        <div class="controls" data="options">
                            <select name="level" id="level" data-value="${level!''}">
                                <#list levels as level>
                                    <option value="${level.getLevel()}">${level.getDescription()}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">在线状态：</label>
                        <div class="controls" data="options" id="online"
                             data-value="<#if online!false>true<#else>false</#if>">
                            <div class="radio" style="width: 100px;">
                                <label>
                                    <input type="radio" name="online" value="true">
                                    上线
                                </label>
                            </div>
                            <div class="radio" style="width: 100px;">
                                <label>
                                    <input type="radio" name="online" value="false">
                                    下线
                                </label>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试听：</label>
                        <div class="controls" data="options" id="trial"
                             data-value="<#if trial!false>true<#else>false</#if>">
                            <div class="radio" style="width: 100px;">
                                <label>
                                    <input type="radio" name="trail" value="true">
                                    是
                                </label>
                            </div>
                            <div class="radio" style="width: 100px;">
                                <label>
                                    <input type="radio" name="trail" value="false">
                                    否
                                </label>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="convert">课程封面：</label>
                        <div class="controls">
                            <input type="text" placeholder="封面图片" class="input" value="${convert!''}" name="convert"
                                   id="convert"/>（男孩版）
                            <a href="javascript:void(null);" class="text-primary" data-index="1"
                               data-action="convert-upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="1"
                               data-action="preview">预览</a>
                            <a href="javascript:void(null);" class="text-error">建议图片3M以内（下同）</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="convert"></label>
                        <div class="controls">
                            <input type="text" placeholder="封面图片" class="input" value="${convertGirl!''}" name="convertGirl"
                                   id="convertGirl"/>（女孩版）
                            <a href="javascript:void(null);" class="text-primary" data-index="1"
                               data-action="convert-upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="1"
                               data-action="preview">预览</a>
                            <a href="javascript:void(null);" class="text-error">建议图片3M以内（下同）</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="sequence">序列号：</label>
                        <div class="controls">
                            <input type="number" placeholder="序列号" class="input" value="${sequence!''}" name="sequence"
                                   id="sequence"/>
                        </div>
                    </div>
                    <legend>课文信息</legend>
                    <div class="control-group">
                        <label class="control-label" for="video">课文视频：</label>
                        <div class="controls">
                            <input type="text" placeholder="课文视频" class="input" value="${video!''}" name="video"
                                   id="video"/>（男孩版）
                            <a href="javascript:void(null);" class="text-success" data-index="1"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="convert">视频背景：</label>
                        <div class="controls">
                            <input type="text" placeholder="视频背景" class="input" value="${videoImage!''}" name="videoImage"
                                   id="videoImage"/>（男孩版）
                            <a href="javascript:void(null);" class="text-primary" data-index="1"
                               data-action="convert-upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="1"
                               data-action="preview">预览</a>
                            <a href="javascript:void(null);" class="text-error">建议图片3M以内（下同）</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="video">课程视频：</label>
                        <div class="controls">
                            <input type="text" placeholder="课文视频" class="input" value="${videoGirl!''}" name="videoGirl"
                                   id="videoGirl"/>（女孩版）
                            <a href="javascript:void(null);" class="text-success" data-index="1"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="convert">视频背景：</label>
                        <div class="controls">
                            <input type="text" placeholder="视频背景" class="input" value="${videoImageGirl!''}" name="videoImageGirl"
                                   id="videoImageGirl"/>（女孩版）
                            <a href="javascript:void(null);" class="text-primary" data-index="1"
                               data-action="convert-upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="1"
                               data-action="preview">预览</a>
                            <a href="javascript:void(null);" class="text-error">建议图片3M以内（下同）</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="productName">知识点：</label>
                        <div class="controls">
                            <textarea name="keyPoint" style="width: 430px; height: 150px;"  placeholder="知识点" class="input">${keyPoint!''}</textarea>
                        </div>
                    </div>
                    <legend>练习信息</legend>
                    <div class="control-group">
                        <label class="control-label" for="keyPoint4">课文练习1：</label>
                        <div class="controls">
                            <input type="text" placeholder="作业id" class="input" value="${workId1!''}" name="workId1"
                                   id="workId1"/>
                            <select name="viewType1" id="viewType1" data-value="${viewType1!''}">
                                <option value="1">题干+方选项</option>
                                <option value="2">题干+图片+长选项</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="keyPoint4">2：</label>
                        <div class="controls">
                            <input type="text" placeholder="作业id" class="input" value="${workId2!''}" name="workId2"
                                   id="workId2"/>
                            <select name="viewType2" id="viewType2" data-value="${viewType2!''}">
                                <option value="1">题干+方选项</option>
                                <option value="2">题干+图片+长选项</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="keyPoint4">3：</label>
                        <div class="controls">
                            <input type="text" placeholder="作业id" class="input" value="${workId3!''}" name="workId3"
                                   id="workId3"/>
                            <select name="viewType3" id="viewType3" data-value="${viewType3!''}">
                                <option value="1">题干+方选项</option>
                                <option value="2">题干+图片+长选项</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="keyPoint4">4：</label>
                        <div class="controls">
                            <input type="text" placeholder="作业id" class="input" value="${workId4!''}" name="workId4"
                                   id="workId4"/>
                            <select name="viewType4" id="viewType4" data-value="${viewType4!''}">
                                <option value="1">题干+方选项</option>
                                <option value="2">题干+图片+长选项</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="keyPoint4">5：</label>
                        <div class="controls">
                            <input type="text" placeholder="作业id" class="input" value="${workId5!''}" name="workId5"
                                   id="workId5"/>
                            <select name="viewType5" id="viewType5" data-value="${viewType5!''}">
                                <option value="1">题干+方选项</option>
                                <option value="2">题干+图片+长选项</option>
                            </select>
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
<input type="file" style="display: none;" id="uploader" accept="image/gif, image/jpeg, image/png, image/jpg"/>
<script lang="javascript">
    $(document).ready(function () {
        $("[data-action='key-upload']").click(function () {
            var index = $(this).attr("data-index");
            $("#uploader").attr("data-fire", "keyPoint" + index);
            $("#uploader").click();
        });


        $("[data-action='key-cancel']").click(function () {
            var index = $(this).attr("data-index");
            $("#keyPoint" + index).val("");
        });

        $("[data-action='convert-upload']").click(function () {
            $("#uploader").attr("data-fire", $(this).prev().attr("name"));
            $("#uploader").click();
        });

        $("[data-action='preview']").click(function () {
            var image = $(this).parent().find("input").first().val();
            if (image == '') {
                return;
            }

            window.open(image, "newwindow");
        });

        $("#uploader").change(function () {
            var id = $(this).attr("data-fire");
            if ($(this).val() == '') {
                return;
            }
            var obj = $(this);
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
                        $("#" + id).val(path);
                    } else {
                        alert("上传失败");
                    }
                },
                complete: function () {
                    $("#uploader").val("");
                }
            });
        });

        var valid = function (id) {
            if ($("#" + id).val() == "") {
                alert("请输入" + $("#" + id).attr("placeholder"));
                $("#" + id).focus();
                return true;
            } else {
                return false;
            }
        }

        $("#saveBtn").click(function () {
            if (valid("name") || valid("convert") || valid("sequence") || valid("video")) {
                return;
            }

            $("#courseForm").ajaxSubmit({
                url: "save.vpage",
                type: "post",
                dataType: "json",
                clearForm: false,
                resetForm: false,
                success: function (data) {
                    if (data.success) {
                        document.location.href = "courses.vpage";
                    } else {
                        alert(data.info)
                    }
                }

            });

        });

        $("#level").val($("#level").attr("data-value"))
        $("[name='trail'][value='" + $("#trial").attr("data-value") + "']").attr("checked", "checked");
        $("[name='online'][value='" + $("#online").attr("data-value") + "']").attr("checked", "checked");

        $("select").each(function(){
           var value = $(this).attr("data-value");
           if(value != ''){
               $(this).val(value);
           }
        });
    })
</script>
</@layout_default.page>