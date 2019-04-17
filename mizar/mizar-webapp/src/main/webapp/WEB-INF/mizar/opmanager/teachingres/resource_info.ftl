<#import "../../module.ftl" as module>
<#import "../../common/pager.ftl" as pager />
<@module.page
title="江西版-教学资源-添加/编辑资源"
leftMenu="图文"
>
<link  href="${ctx}/public/plugin/bootstrap-2.3.0/css/bootstrap.min.css" rel="stylesheet">
<script src="${ctx}/public/plugin/jquery/jquery-1.9.1.min.js"></script>
<script src="${ctx}/public/plugin/bootstrap-2.3.0/js/bootstrap.min.js"></script>
<script src="${ctx}/public/plugin/validator.min.js"></script>
<script src="${ctx}/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js"></script>
<script src="${ctx}/public/plugin/wxeditor/js/colorpicker-min.js"></script>
<style>
    body { padding-bottom: 40px; background-color: #f5f5f5; }
    a, input, button, select{ outline:none !important;}
    .ckfield-controls { padding-top: 5px; }
    .reqiured { color: red; font-size: 20px; }
    select{ display: inline-block; width: 300px; }
    .control-group .control-label{ width: 100px; }
    .control-group .controls{ margin-left: 120px; }
    .checkbox-controls label{ display: inline-block; width: 70px; }
    .checkbox-controls label input{ margin-top: 0; }
    .checkbox-controls label span{ vertical-align: middle; }
</style>

<div>
    <div class="op-wrapper orders-wrapper clearfix">
        <span class="title-h1">添加/编辑资源</span>
    </div>
    <div class="op-wrapper orders-wrapper clearfix">
        <a title="返回" href="javascript: void(0)" id="goback" class="btn btn-success">
            <i class="icon-chevron-left icon-white"></i> 返 回
        </a>
    </div>
    <div class="op-wrapper marTop clearfix">
        <div class="row-fluid">
            <div class="well" style="background: #fff;">
                <form id="info_frm" name="info_frm" enctype="application/x-www-form-urlencoded" action="savegoods.vpage"
                      method="post">
                    <input id="resource-id" name="id" value="${resource.id!}" type="hidden">
                    <input id="main-image" name="image" value="${resource.image!}" type="hidden">
                    <input id="app-image" name="appImage" value="${resource.appImage!}" type="hidden">
                    <div class="form-horizontal">
                        <#--<div class="control-group">
                            <label class="control-label">学科<span class="reqiured">*</span></label>
                            <div class="controls ckfield-controls checkbox-controls">
                                <label><input type="checkbox" name="subject" data-subject="CHINESE" /> 语文</label>
                                <label><input type="checkbox" name="subject" data-subject="MATH"/> 数学</label>
                                <label><input type="checkbox" name="subject" data-subject="ENGLISH"/> 英语</label>
                            </div>
                        </div>-->
                        <div class="control-group">
                            <label class="control-label">年级<span class="reqiured">*</span></label>
                            <div class="controls ckfield-controls checkbox-controls">
                                <label><input type="checkbox" name="grade" data-grade="1"/> 1年级</label>
                                <label><input type="checkbox" name="grade" data-grade="2"/> 2年级</label>
                                <label><input type="checkbox" name="grade" data-grade="3"/> 3年级</label>
                                <label><input type="checkbox" name="grade" data-grade="4"/> 4年级</label>
                                <label><input type="checkbox" name="grade" data-grade="5"/> 5年级</label>
                                <label><input type="checkbox" name="grade" data-grade="6"/> 6年级</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">资源分类<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if categories??>
                                    <select id="category" name="category">
                                        <#list categories as c >
                                            <option value="${c.name()!}" <#if resource?? && ((resource.category!'') == c.name())>
                                                    selected </#if>>${c.getDesc()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">标签<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if labels??>
                                    <select id="label" name="label">
                                        <#list labels as c >
                                            <option value="${c.name()!}" <#if resource?? && ((resource.label!'') == c.name())>
                                                    selected </#if>>${c.getDesc()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">作业类型<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if workType??>
                                    <select id="workType" name="workType">
                                        <#list workType as c >
                                            <option value="${c.name()!}" <#if resource?? && ((resource.workType!'') == c.name())>
                                                    selected </#if>>${c.getDesc()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">标题<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control input_txt" style="width: 300px;height: 30px;"
                                       value="<#if resource??>${resource.name!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">副标题</label>
                            <div class="controls">
                                <input type="text" id="name" name="subHead" class="form-control input_txt" style="width: 300px;height: 30px;"
                                       value="<#if resource??>${resource.subHead!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">正文<span class="reqiured">*</span></label>
                        <#--<div class="controls">
                            <textarea id="description" name="description" class="intro"
                                      placeholder="请填写课程简介"><#if goods??>${goods.desc}</#if></textarea>
                        </div>-->
                            <div class="controls">
                                <div style="height: auto;">
                                    <div class="mat-content">
                                        <div class="item shop-name">
                                            <div class="editor" style="width: 560px;">
                                                <div class="wxeditor">
                                                    <div class="clearfix">
                                                        <div class="right">
                                                            <div id="bdeditor">
                                                                <script type="text/javascript" charset="utf-8"
                                                                        src="/public/plugin/wxeditor/js/ueditor.config.js"></script>
                                                                <script type="text/javascript" charset="utf-8"
                                                                        src="/public/plugin/wxeditor/js/ueditor.all.js"></script>
                                                                <script id="editor" type="text/plain"
                                                                        style="margin-top:15px;width:100%;"></script>
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
                        <div class="control-group">
                            <label class="col-sm-2 control-label">题图<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input readonly type="text" class="form-control input_txt" id="imageSquareTrigger" style="width: 300px;height: 30px; background: #fff; cursor: pointer;" value="请选择上传文件"/>
                                <input type="file" id="imageSquare" accept="image/gif,image/jpeg,image/jpg,image/png,image/svg" style="display: none;">
                                <small style="color:red">240px * 170px </small>
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <img id="preview-image" src="" style="display: inline-block; width: 80px; height: 80px;">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">是否有资源<span class="reqiured">*</span></label>
                            <div class="controls">
                                <select id="task" name="task" <#if forbidEditTask!false>disabled</#if>>
                                    <#list tasks as t >
                                    <option value="${t.name()!}"
                                        <#if resource?? && ((resource.task!'') == t.name())>selected </#if>>
                                        ${t.getConfigDesc()!}
                                        </option>
                                    </#list>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">可见用户<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if limitUserTypes??>
                                    <select id="visitLimited" name="visitLimited">
                                        <#list limitUserTypes as t >
                                            <option value="${t.name()!}" <#if resource?? && ((resource.visitLimited!'') == t.name())>
                                                    selected </#if>>${t.getDescription()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">领取用户<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if limitUserTypes??>
                                    <select id="receiveLimited" name="receiveLimited">
                                        <#list limitUserTypes as t >
                                            <option value="${t.name()!}" <#if resource?? && ((resource.receiveLimited!'') == t.name())>
                                                    selected </#if>>${t.getDescription()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">资料URL<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="file-url" name="fileUrl" class="form-control input_txt" style="width: 300px;height: 30px;"
                                       value="<#if resource??>${resource.fileUrl!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">置顶排序</label>
                            <div class="controls">
                                <input type="text" id="display-order" name="displayOrder" class="form-control" style="width: 300px;height: 30px;"
                                       value="<#if resource??>${resource.displayOrder!}</#if>"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">首页展示</label>
                            <div class="controls ckfield-controls checkbox-controls">
                                <label><input type="radio" name="featuring" value="yes" /> 是</label>
                                <label><input type="radio" name="featuring" value="no" /> 否</label>
                            </div>
                        </div>
                    </div>
                </form>
                <div class="op-wrapper orders-wrapper clearfix" style="margin-left: 120px;">
                    <a title="保存" href="javascript:void(0);" class="btn btn-info" id="save_info">
                        <i class="icon-check icon-white"></i> 保 存
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="uploaderDialog" class="modal fade hide" style="width:550px; height: 300px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div style="float: left; width: 280px;">
            <div style="height: 200px; width: 280px;">
                <img id="imgSrc" src="" alt="预览" style="height: 200px; width: 280px;"/>
            </div>
        </div>
        <div style="float: right">
            <div style="display: block;">
                <textarea placeholder="请填写描述" id="uploadDesc" style="resize: none;"></textarea>
            </div>
            <div style="display: block;">
                <a href="javascript:void(0);" class="uploader">
                    <input type="file" name="file" id="uploadFile" accept="image/*" onchange="previewImg(this)">选择素材
                </a>
            </div>
        </div>
        <input type="hidden" id="uploadField" value="photo">
    </div>
    <div class="modal-footer">
        <button title="确认上传" class="uploader" id="upload_confirm">
            <i class="icon-ok"></i>
        </button>
        <button class="uploader" data-dismiss="modal" aria-hidden="true"><i class="icon-trash"></i></button>
    </div>
</div>

<script>
    $(function () {
        $('#info_frm').validator();
        var c = UE.getEditor("editor", {
            serverUrl: "/common/ueditorcontroller.vpage",
            topOffset: 0,
            zIndex: 1040,
            autoHeightEnabled: false,
            initialFrameHeight: 500,
            initialFrameWidth: 560,
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', '|',
                'horizontal', 'date', 'time', 'preview'
            ]]

        });

        c.ready(function () {
            $(".itembox").on("click", function (a) {
                c.execCommand("insertHtml", "<div>" + $(this).html() + "</div><br />")
            })

            console.log('123','${(resource.desc!'')?replace('\n','')?replace("'" , "\\'")!''}')
            <#if resource??>
                /* 处理 ‘product.description’ 传过来的数据 把相对应的 ‘\n’ 处理成空字符串 */
                c.setContent('${(resource.desc!'')?replace('\n','')?replace("'" , "\\'")!''}');
            </#if>
        });

        var b = ["borderTopColor", "borderRightColor", "borderBottomColor", "borderLeftColor"], d = [];
        $.each(b, function (a) {
            d.push(".itembox .wxqq-" + b[a])
        });

        var grades = '${resource.grade!''}'.split(',');
        $("input[name=grade]").each(function (index, field) {
            var grade = $(field).data("grade");
            if ($.inArray(grade.toString(), grades) >= 0) {
                $(field).prop("checked", true);
            }
        });
        <#if resource.featuring!false>
            $("input[name='featuring']").get(0).checked = 'true';
        <#else>
            $("input[name='featuring']").get(1).checked = 'true';
        </#if>

        var imageUrl = '${resource.image!''}';
        if(imageUrl.trim() != ''){
            $("#preview-image").prop("src", imageUrl);
        }
        var appImageUrl = '${resource.appImage!''}';
        if(appImageUrl.trim() != ''){
            $("#preview-appImage").prop("src", appImageUrl);
        }

        $('#info_frm').on('submit', function (e) {
            e.preventDefault();
            var params = {};

            $("input,select", this).each(function (index, field) {
                params[$(field).attr("name")] = $(field).val();
            });

            params.desc = c.getContent();
            if (params.desc.trim() == '') {
                alert("正文不能为空!");
                return;
            }

            params.grade = $("input[name=grade]").filter(":checked")
                    .map(function () {
                        return $(this).data("grade");
                    }).get().join(",");

            if (params.grade.trim() == '') {
                alert("年级是必填项!");
                return;
            }

            if (params.name.trim() == '') {
                alert("标题是必填项!");
                return;
            }

            if (params.fileUrl.trim() == '') {
                alert("资料URL是必填项!");
                return;
            }

            if(params.image.trim() == ''){
                alert("题图为必填项!");
                return;
            }

            params.featuring = $("input[name='featuring']:checked").val() === 'yes' ? true : false;

            $.ajax({
                type: 'post',
                url: 'save.vpage',
                contentType: 'application/json;charset=UTF-8',
                data: JSON.stringify(params),
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = "./index.vpage";
                    } else {
                        alert("保存失败:" + res.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#goback').on('click', function () {
            if (confirm("返回将丢失未保存数据，是否返回？")) {
                window.history.back();
            }
        });

        $('#save_info').on('click', function () {
            if (confirm("是否确认保存？")) {
                $('#info_frm').submit();
            }
        });

        $(document).on("click", "#imageSquareTrigger", function () {
            $("#imageSquare").click();
        });
        $(document).on("click", "#appImageTrigger", function () {
            $("#appImage").click();
        });

        $(document).on("change", "#imageSquare", function () {
            // 拼formData
            var formData = new FormData();
            var file = $(this)[0].files[0];
            if(!file) return ;
            $("#imageSquareTrigger").val(file.name);

            formData.append('path', "teaching_resource");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('id', $('#resource-id').val());
            // 发起请求
            $.ajax({
                url: '/toolkit/common/uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        $("#main-image").val(res.info);
                        $("#preview-image").prop("src", res.info);
                    } else {
                        alert(res.info);
                    }
                }
            });
        });
        $(document).on("change", "#appImage", function () {
            // 拼formData
            var formData = new FormData();
            var file = $(this)[0].files[0];
            if(!file) return ;
            $("#appImageTrigger").val(file.name);

            formData.append('path', "teaching_resource");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('id', $('#resource-id').val());
            // 发起请求
            $.ajax({
                url: '/toolkit/common/uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        $("#app-image").val(res.info);
                        $("#preview-appImage").prop("src", res.info);
                    } else {
                        alert(res.info);
                    }
                }
            });
        });
    })
    ;

    function previewImg(file) {
        var prevDiv = $('#imgSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }
</script>
</@module.page>