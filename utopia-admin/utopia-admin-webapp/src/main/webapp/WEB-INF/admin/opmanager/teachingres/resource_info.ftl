<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑资源" page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
    <#--<@app.css href="/public/plugin/admineditor/css/editor-min.css"/>-->
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js"
        type="text/javascript"></script>
<script type="text/javascript" src="/public/plugin/wxeditor/js/colorpicker-min.js"></script>


<style>
    .ckfield-controls {
        padding-top: 5px;
    }

    .reqiured {
        color: red;
        font-size: 20px;
    }

</style>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑资源&nbsp;&nbsp;
        <#if goods??> (<#if goods.status??>${goods.status.getDesc()!''}<#else>离线</#if>)</#if>
        <a title="返回" href="javascript:window.history.back();" class="btn">
            <i class="icon-share-alt"></i> 返 回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保 存
        </a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" enctype="application/x-www-form-urlencoded" action="savegoods.vpage"
                      method="post">
                    <input id="resource-id" name="id" value="${resource.id!}" type="hidden">
                    <input id="main-image" name="image" value="${resource.image!}" type="hidden">
                    <input id="app-image" name="appImage" value="${resource.appImage!}" type="hidden">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label">学科<span class="reqiured">*</span></label>
                            <div class="controls ckfield-controls">
                                <label style="display: inline-block"><input type="checkbox" name="subject" data-subject="CHINESE"/> 语文</label>
                                <label style="display: inline-block"><input type="checkbox" name="subject" data-subject="MATH"/> 数学</label>
                                <label style="display: inline-block"><input type="checkbox" name="subject" data-subject="ENGLISH"/> 英语</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">年级<span class="reqiured">*</span></label>
                            <div class="controls ckfield-controls">
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="1"/> 1年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="2"/> 2年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="3"/> 3年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="4"/> 4年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="5"/> 5年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="6"/> 6年级</label>
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
                                <small style="color:red">针对教学专题、每周福利有效</small>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">标题<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control input_txt"
                                       value="<#if resource??>${resource.name!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">副标题</label>
                            <div class="controls">
                                <input type="text" id="name" name="subHead" class="form-control input_txt"
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
                                            <div class="editor">
                                                <div class="wxeditor">
                                                    <div class="clearfix">
                                                        <#--<div class="left clearfix" style="width: 420px;">
                                                            <div class="tabbox clearfix" style="width: 69px;">
                                                                <ul class="tabs" id="tabs" style="height: 580px;">
                                                                    <li><a href="javascript:void (0);" tab="tab1"
                                                                           class="">关注</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab2"
                                                                           class="current">标题</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab3"
                                                                           class="">内容</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab4"
                                                                           class="">互推</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab5"
                                                                           class="">分割</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab6"
                                                                           class="">原文引导</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab7"
                                                                           class="">节日</a></li>
                                                                    <li><a href="javascript:void (0);" tab="tab8"
                                                                           class="">表格</a></li>
                                                                </ul>
                                                                <em class="fr"></em>
                                                            </div>
                                                            <div id="styleselect" class="clearfix">
                                                                <div class="tplcontent" style="width: 350px;">
                                                                    <div id="colorpickerbox"></div>
                                                                    <div>
                                                                        <div style="background:#fff;">
                                                                            <#include '../../advisory/tab/tab1.ftl'>
                                                                                <#include '../../advisory/tab/tab2.ftl'>
                                                                                <#include '../../advisory/tab/tab3.ftl'>
                                                                                <#include '../../advisory/tab/tab4.ftl'>
                                                                                <#include '../../advisory/tab/tab5.ftl'>
                                                                                <#include '../../advisory/tab/tab6.ftl'>
                                                                                <#include '../../advisory/tab/tab7.ftl'>
                                                                                <#include '../../advisory/tab/tab8.ftl'>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <div class="goto"></div>
                                                            </div>
                                                        </div>-->
                                                        <div class="right" style="background:#fff; width: 50%">
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
                                <input type="file" id="imageSquare">
                                <small style="color:red">每周活动：660px * 170px,其他栏目：240px * 170px </small>
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <img id="preview-image" src="" width="210" height="140">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">老师APP首页图</label>
                            <div class="controls">
                                    <input type="file" id="appImage">
                                    <small style="color:red">300px * 165px </small>
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                            <img id="preview-appImage" src="" width="300" height="165">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">任务<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if categories??>
                                    <select id="task" name="task" style="width: 500px;" <#if forbidEditTask!false>disabled</#if>>
                                        <#list tasks as t >
                                            <option value="${t.name()!}" <#if resource?? && ((resource.task!'') == t.name() || (t.name() == 'PRO_SURVIVAL_5' && ((resource.task!'') == 'PRO_SURVIVAL_6' || (resource.task!'') == 'PRO_SURVIVAL_7')))>
                                                    selected </#if>>${t.getConfigDesc()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                                <small style="color:red">针对每周福利有效</small>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">任务有效期(天)<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="validity-period" name="validityPeriod" class="form-control"
                                       value="<#if resource??>${resource.validityPeriod!}</#if>" data-ispositivte="1"
                                       required <#if forbidEditTask!false>disabled</#if>/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">可见用户<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if limitUserTypes??>
                                    <select id="visitLimited" name="visitLimited" style="width: 500px;" >
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
                                    <select id="receiveLimited" name="receiveLimited" style="width: 500px;">
                                        <#list limitUserTypes as t >
                                            <option value="${t.name()!}" <#if resource?? && ((resource.receiveLimited!'') == t.name())>
                                                    selected </#if>>${t.getDescription()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                                <small style="color:red">针对同步课件有效</small>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">资料URL<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="file-url" name="fileUrl" class="form-control input_txt"
                                       value="<#if resource??>${resource.fileUrl!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">置顶排序</label>
                            <div class="controls">
                                <input type="text" id="display-order" name="displayOrder" class="form-control"
                                       value="<#if resource??>${resource.displayOrder!}</#if>"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">首页展示</label>
                            <div class="controls">
                                <input type="checkbox" id="featuring" name="featuring" data-grade="1"/>
                            </div>
                        </div>
                    </div>
                </form>
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
<script type="text/javascript">
    $(function () {
        $('#info_frm').validator();

        var c = UE.getEditor("editor", {
            serverUrl: "/advisory/ueditorcontroller.vpage",
            topOffset: 0,
            zIndex: 1040,
            autoHeightEnabled: false,
            initialFrameHeight: 473,
            initialFrameWidth: 950,
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

            <#if resource??>
                /* 处理 ‘product.description’ 传过来的数据 把相对应的 ‘\n’ 处理成空字符串 */
                setTimeout(function () {
                    c.setContent('${(resource.desc!'')?replace('\n','')?replace("'" , "\\'")!''}');
                }, 600);
            </#if>
        });

        var b = ["borderTopColor", "borderRightColor", "borderBottomColor", "borderLeftColor"], d = [];
        $.each(b, function (a) {
            d.push(".itembox .wxqq-" + b[a])
        });

        var subjects = '${resource.subject!''}'.split(',');
        $("input[name=subject]").each(function (index, field) {
            var subject = $(field).data("subject");
            if ($.inArray(subject, subjects) >= 0) {
                $(this).prop("checked", true);
            }
        });

        var grades = '${resource.grade!''}'.split(',');
        $("input[name=grade]").each(function (index, field) {
            var grade = $(field).data("grade");
            if ($.inArray(grade.toString(), grades) >= 0) {
                $(field).prop("checked", true);
            }
        });

        <#if resource.featuring!false>
            $("#featuring").prop("checked", true);
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

            if (params.category != "WEEK_WELFARE") {
                if (params.task != "NONE" && params.task != "FREE") {
                    alert("只有每周福利才可以配置任务")
                    return false;
                }
            } else {
                if (params.task == "NONE" || params.task == "FREE") {
                    alert("每周福利不能免费或无资源")
                    return false;
                }
            }
            if (params.workType != "无") {
                if (params.category != "TEACHING_SPECIAL" && params.category != "WEEK_WELFARE") {
                    alert("只有教学专题和每周福利才可以配置作业类型")
                    return false;
                }
            }
            if ((params.receiveLimited != "All") && (params.category != "SYNC_COURSEWARE")) {
                alert("只有同步课件才能限制领取用户")
                return false;
            }

            params.desc = c.getContent();
            if (params.desc.trim() == '') {
                alert("正文不能为空!");
                return;
            }

            params.subject = $("input[name=subject]").filter(":checked")
                    .map(function () {
                        return $(this).data("subject");
                    }).get().join(",");

            if (params.subject.trim() == '') {
                alert("学科是必填项!");
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

            if (params.validityPeriod.trim() == '') {
                alert("有效期不能为空!");
                return;
            }

            var numberReg = /^\+?[0-9]*$/;
            if (!numberReg.test(params.validityPeriod)) {
                alert("有效期必须为正整数");
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

            params.featuring = $("input#featuring").is(":checked");

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

        $('#save_info').on('click', function () {
            if (confirm("是否确认保存？")) {
                $('#info_frm').submit();
            }
        });

        $(document).on("change", "#imageSquare", function () {
            // 拼formData
            var formData = new FormData();
            var file = $(this)[0].files[0];

            formData.append('path', "teaching_resource");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('id', $('#resource-id').val());
            // 发起请求
            $.ajax({
                url: '/toolkit/uploadphoto.vpage',
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

            formData.append('path', "teaching_resource");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('id', $('#resource-id').val());
            // 发起请求
            $.ajax({
                url: '/toolkit/uploadphoto.vpage',
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
</@layout_default.page>