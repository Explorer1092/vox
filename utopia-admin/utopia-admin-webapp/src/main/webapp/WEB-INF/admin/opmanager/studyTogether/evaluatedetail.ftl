<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/修改作业点评" page_num=9 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<style>

    .layer {
        background:#020516;
        width:100%;
        height:100%;
        opacity:0.4;
        filter:alpha(opacity=40);
        position:fixed;
        left:0;
        top:0; z-index:1000;
        display:none;
    }
    .loading{
        width:38px;
        height:38px;
        background:url(/public/img/loading.gif) no-repeat;
        position:fixed;
        left:50%;
        top:50%;
        margin-left:-16px;
        margin-top:-16px;
        z-index:4000;
        display:none;
    }

</style>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/修改作业点评
        <a type="button" id="btn_cancel" href="workervaluate.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存作业点评"/>
    </legend>

    <div>
        <ul class="inline">
            <li>
                <label>学生姓名&nbsp;
                    <input type="text" id="searchCourseId" name="searchCourseId" value="${studentName!''}" readonly/>
                </label>
            </li>
            <li>
                <label>朗读作业&nbsp;
                    <audio controls="controls" id="audioBox">
                        <source src="${readUrl!''}" type="audio/mpeg">
                    </audio>
                </label>
            </li>
        </ul>
    </div>

    <hr/>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="courseForm" name="detail_form" enctype="multipart/form-data" action="save.vpage" method="post">
                    <input id="evaluateId" name="evaluateId" value="${evaluateId!}" type="hidden" class="js-postData">
                    <input id="studentId" name="studentId" value="${studentId!}" type="hidden" class="js-postData">
                    <input id="parentId" name="parentId" value="${parentId!}" type="hidden" class="js-postData">
                    <input id="courseId" name="courseId" value="${courseId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <#-- 点评课ID-->
                        <#if evaluateId?has_content>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">ID</label>
                            <div class="controls">
                                <input type="text" id="evaluateId" name="evaluateId" class="form-control" value="${evaluateId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 作业点评 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">作业点评 <span style="color: red">*</span></label>
                            <div class="controls">
                                <textarea id="fullComment" name="fullComment" class="form-control js-postData" style="width:336px;"
                                          placeholder="请在这里输入作业点评，长度不超过100个汉字">${(evaluate.fullComment)!}</textarea>
                                <span style="font-size: 10px;color: red">必填</span>
                            </div>
                        </div>

                        <#-- 点评人 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">点评人 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="teacherName" name="teacherName" class="form-control js-postData" type="text" value="${teacherName!''}" readonly/>
                                <span style="font-size: 10px;color: red">默认为当前登录用户,无法修改</span>
                            </div>
                        </div>

                        <#-- 录音点评 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">录音点评 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="commentVoiceUrl" name="commentVoiceUrl" class="form-control js-postData" type="text" value="<#if evaluate??>${evaluate.commentVoiceUrl!''}</#if>" style="width: 530px"/>
                                <span><button type="button" id="preview_video" class="btn btn-success btn-small">预览</button></span>
                                <input class="fileUpBtn" type="file" accept="audio/*" style="left"/>
                                <input type="hidden" id="relativeUrl" name="relativeUrl" class="form-control js-postData" value="${relativeUrl!''}">
                                <span style="font-size: 10px;color: red">大小不能超过10M</span>
                            </div>
                        </div>

                        <#-- 作业等级 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">作业等级 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="level" name="level" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择作业等级--</option>
                                    <#if levels??>
                                        <#list levels as lels>
                                            <option <#if evaluate?? && evaluate.level??><#if evaluate.level == lels> selected="selected"</#if></#if> value = ${lels!}>
                                                <#if lels?? && lels == 0>
                                                    普通作品
                                                <#elseif lels?? && lels == 1>
                                                    优秀作品
                                                </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                                <span style="font-size: 10px;color: red">必填</span>
                            </div>
                        </div>

                        <#-- 学员问题 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学员问题 <span style="color: red">*</span></label>
                            <div class="controls ckfield-controls">
                                <input type="checkbox" name="question" class="js-postData" data-qst="1"/> 咬字不清
                                <input type="checkbox" name="question" class="js-postData" data-qst="2"/> 语言不流畅
                                <input type="checkbox" name="question" class="js-postData" data-qst="3"/> 识字错误
                                <input type="checkbox" name="question" class="js-postData" data-qst="4"/> 固定语势
                                <input type="checkbox" name="question" class="js-postData" data-qst="5"/> 感情不够投入
                                <input type="checkbox" name="question" class="js-postData" data-qst="6"/> 节奏单一
                                <input type="checkbox" name="question" class="js-postData" data-qst="7"/> 节奏变化幅度过大
                                <span style="font-size: 10px;color: red">选填</span>
                            </div>
                        </div>
                    </div>
                </form>
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

<div class="layer loading_layer" id="loading_layer"></div>
<div class="loading" id="loading"></div>

<script type="text/javascript">
$(function () {

    //验证表单
    var validateForm = function () {
        var msg = "";
        if($('#fullComment').val() == ''){
            msg += "作业点评不能为空！\n";
        }
        if($('#fullComment').val().length > 100) {
            msg += "作业点评超过100个汉字！\n";
        }
        if($('#level').val() == ''){
            msg += "请选择作业等级！\n";
        }
        if (msg.length > 0) {
            alert(msg);
            return false;
        }
        return true;
    };

    var questions = '${evaluate.question!''}'.split(',');
    $("input[name=question]").each(function (index, field) {
        var question = $(field).data("qst");
        if ($.inArray(question.toString(), questions) >= 0) {
            $(field).prop("checked", true);
        }
    });

    //保存提交
    $(document).on("click",'#save_ad_btn',function () {
        if(validateForm()){
            var post = {};
            $(".js-postData").each(function(i,item){
                post[item.name] = $(item).val();
            });
            post.question = $("input[name=question]").filter(":checked").map(function () {
                        return $(this).data("qst");
                    }).get().join(",");
            $.post('saveevaluate.vpage',post,function (res) {
                if(res.success){
                    alert("保存成功");
                    location.href= 'workervaluate.vpage';
                }else{
                    alert("保存失败");
                }
            });
        }
    });

    var upload = function (object, callback) {
        showLoadingFn();
        if(object.val() !== ''){
            var formData = new FormData();
            formData.append('inputFile', object[0].files[0]);
            //控制大小为10MB
            var fileSize = (object[0].files[0].size / 1024 / 1012).toFixed(4);
            console.info(fileSize);
            if (fileSize >= 10) {
                alert("录音过大，重新选择。");
                return false;
            }
            $.ajax({
                url: 'uploadevaluate.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    hideLoadingFn();
                    if (data.success) {
                        callback(data.path, data.relativeUrl);
                        alert("上传成功");
                    } else {
                        alert("上传失败");
                    }
                }
            });
        }
    };

    $(".fileUpBtn").change(function () {
        var container = $(this);
        var ext = container.val().split('.').pop().toLowerCase();
        if ($.inArray(ext, ['mp3']) === -1) {
            alert("仅支持以下格式的音频【'mp3'");
            return false;
        }
        upload($(this), function (path, relativeUrl) {
            $("#commentVoiceUrl").val(path);
            $("#relativeUrl").val(relativeUrl);
        });
    });
});

    $('#preview_video').click(function () {
        var src = $('#commentVoiceUrl').val();
        $('#myVideoModal').modal({
            show: true,
            backdrop: 'static'
        });
        $('#myVideoModal video').attr('src', src);
    });

    $('#myVideoModal button').click(function () {
        $('#myVideoModal video').removeAttr('src');
    });

    function showLoadingFn() {
        $('#loading_layer').show();
        $('#loading').show();
    }

    function hideLoadingFn() {
        $('#loading_layer').hide();
        $('#loading').hide();
    }
</script>
</@layout_default.page>
