<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='绘本馆卡片管理' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>

<div id="main_container" class="span9">
    <fieldset>
        <legend><a href="list.vpage"><font color="#00bfff">绘本馆卡片管理</font></a>/新增卡片</legend>
        <div class="modal-header">
            <h3 class="modal-title">${subjectName!''}</h3>
        </div>
    </fieldset>
    <div class="row-fluid">
        <div class="span12">
            <div id="cardList">
                <#if cardList?? && cardList?size gt 0>
                    <#list cardList as  card>
                    <fieldset>
                        <div class="well" style="border: 1px solid #e3e3e3  ; background: white;">
                            <div class="control-group">
                                <label class="control-label" style="display: inline-block;padding: 0 19px;"><span style="color: red;font-size: 20px;">*</span>卡片序号:</label>
                                <div class="controls" style="display: inline-block;padding: 0 19px;">
                                    <input type="text" id="cardName" value="${card.sort!''}" readonly>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" style="display: inline-block;padding: 0 19px;"><span style="color: red;font-size: 20px;">*</span>卡片名称:</label>
                                <div class="controls" style="display: inline-block;padding: 0 19px;">
                                    <input type="text" id="cardName" value="${card.name!''}" readonly>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" style="display: inline-block;padding: 0 19px;"><span style="color: red;font-size: 20px;">*</span>介绍(50字以内)</label>
                                <textarea class="form-control" id="instruction" name="instruction" rows="5" cols="10"
                                          maxlength="200" required style="margin-left: 30px;vertical-align: top" style="display: inline-block;padding: 0 19px;" readonly>${card.description}</textarea>
                            </div>
                            <div class="control-group">
                                <label class="control-label" style="display: inline-block;padding: 0 19px;"><span style="color: red;font-size: 20px;">*</span>碎片数:</label>
                                <div class="controls" style="display: inline-block;padding: 0 19px;">
                                    <label class="control-label" style="display: inline-block;margin: 0 33px 0 83px">
                                        ${card.fragmentNum}
                                    </label>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" style="padding: 0 19px;"><span style="color: red;font-size: 20px;">*</span>图片</label>
                                <img src="${card.imgUrl}" id="adImgView" width="50%" style="padding: 0 19px;"/>
                            </div>
                            <div class="control-group">
                                <#if card.isOnLine == 1>
                                        <button disabled="disabled" class="btn btn-default" onclick="modify('${card.id!''}')">编辑</button>
                                <#else>
                                        <button class="btn btn-success" onclick="modify('${card.id!''}')">编辑</button>
                                </#if>
                            </div>
                        </div>
                    </fieldset>
                    </#list>
                </#if>
            </div>
            <#if isOnline == 0>
                <button class="btn btn-primary" id="addPictureBookCard">添加卡片</button>
            </#if>
        </div>
    </div>
</div>
<div id="add_dialog" class="modal fade hide" aria-hidden="true" role="dialog" style="width: 800px">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title">配置卡片</h3>
            </div>
            <div class="modal-body" style="overflow: auto;max-height: 500px;">
                <form id="add-card-frm" action="save.vpage" method="post" role="form">
                    <input type="hidden" id="cardType" name="type" value="1" required/>
                    <input type="hidden" id="card_id" name="card_id" value=""/>
                    <input type="hidden" id="subjectId" name="subjectId" value="${subjectId!''}" required/>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>卡片序号:</label>
                        <div class="controls">
                            <input type="text" id="cardSort" name="sort" placeholder="必填，数字" required>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>卡片名称:</label>
                        <div class="controls">
                            <input type="text" id="cardName" name="name" placeholder="50字以内" required>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>介绍(50字以内)</label>
                        <textarea class="form-control" id="instruction" name="des" rows="5" cols="10"
                                  maxlength="200" required style="margin-left: 30px;vertical-align: top"></textarea>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>碎片数:</label>
                        <div class="controls" style="display: inline-block">
                            <label class="control-label" style="display: inline-block;margin: 0 33px 0 83px">
                                <input type="radio" class="showType" name="num" id="radio_2" value="2" style="margin: 0" checked/>2
                            </label>
                            <label class="control-label" style="display: inline-block;margin: 0 33px 0 0">
                                <input type="radio" class="showType" name="num" id="radio_3" value="3" style="margin: 0"/>3
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>图片</label>
                        <input type="hidden" id="adUrl" name="imgUrl" value="" required/>
                        <input type="file" class="fileUpBtn" id="adUpload" accept="image/gif, image/jpeg, image/png, image/jpg" style="margin-left: 93px">
                        <br/><img src="" id="adImgView" width="50%"/>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="cancel_button" type="button" class="btn btn-default">取消</button>
                <button id="save-card-submit" type="submit" class="btn btn-primary">提交</button>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    $("#addPictureBookCard").on('click', function () {
        showSubjectDialog();
    });

    $("#searchBtn").on('click', function () {
        $("#pageNum").val(1);
        $("#config-query").submit();
    });

    $("button#cancel_button").click(function () {
        $("#add_dialog").modal("hide");
        $("form#add-card-frm")[0].reset();
        $("#add_dialog #imgView").prop("src","");
        $("#add_dialog #adImgView").prop("src","");
        $("#add_dialog #adUrl").val("");
        $("#add_dialog #imageUrl").val("");
        $("#add_dialog #instruction").text("");
    });

    $("button#save-card-submit").click(function () {
        var frm = $("form#add-card-frm");
        var postData = {};
        var flag = true;
        $.each($("input:radio", frm), function (index, field) {
            var _f = $(field);
            if(_f.prop('checked')){
                postData[_f.attr("name")] = _f.val();
            }
        });
        $.each($("input:text,input:hidden,textarea", frm), function (index, field) {
            var _f = $(field);
            if(_f.prop("required")){
                if(_f.val()===''){
                    alert("请填写全部必填项");
                    flag = false;
                    return false;
                }
            }
            postData[_f.attr("name")] = _f.val();
        });
        if(!flag){
            return false;
        }
        $.post("save.vpage", postData,
                function (data) {
                    if (data.success) {
                        $("#add_dialog").modal("hide");
                        $("form#add-card-frm")[0].reset();
                        alert("保存成功");
                        window.location = "detail.vpage?subjectId="+data.subjectId;
                    }
                    else
                        alert(data.info);
                }
        );
    });
    $("#add_dialog .fileUpBtn").change(function () {
        var container = $(this);
        var ext = container.val().split('.').pop().toLowerCase();
        if (container != '') {
            if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                return false;
            }
            var formData = new FormData();
            formData.append('inputFile', container[0].files[0]);
            var fileSize = (container[0].files[0].size / 1024 / 1012).toFixed(4); //MB
            console.info(fileSize);
            if (fileSize >= 2) {
                alert("图片过大，重新选择。");
                return false;
            }
            $.ajax({
                url: '../recommend/upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                            $("#add_dialog #adImgView").attr('src', data.imgUrl);
                            $("#add_dialog #adImgView").data("file_name", data.imgName);
                            $("#add_dialog #adUrl").val(data.imgUrl);
                    } else {
                        alert("上传失败");
                    }
                }
            });
        }
    });
    function hideSubjectDialog() {
        $("#add_dialog").modal("hide");;
    }
    function showSubjectDialog() {
        $("#add_dialog").modal("show");
    }

    function modify(id) {
        if (id === '') {
            alert("参数错误");
        }
        $.ajax({
            type: "get",
            url: "edit.vpage",
            data: {
                card_id: id
            },
            success: function (data) {
                if (data.success) {
                    $("#add_dialog #card_id").val(data.card.id);
                    $("#add_dialog #radio_"+data.card.fragmentNum).prop("checked",true);
                    $("#add_dialog #cardName").val(data.card.name);
                    $("#add_dialog #cardSort").val(data.card.sort);
                    $("#add_dialog #adImgView").prop("src",data.card.imgUrl);
                    $("#add_dialog #instruction").text(data.card.description);
                    $("#add_dialog #adUrl").val(data.card.imgUrl);
                    $("#add_dialog").modal("show");
                } else {
                    alert("操作失败");
                }
            }
        });
    }

</script>
</@layout_default.page>