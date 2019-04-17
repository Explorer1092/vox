<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='绘本馆卡片管理' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>

<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>

<div id="main_container" class="span9">
    <legend><a href="list.vpage"><font color="#00bfff">绘本馆彩蛋卡管理</font></a>/彩蛋卡</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" id="add-color-frm" style="background-color: #fff;width: 800px">
                <div id="cardList">
                    <input type="hidden" id="card_type" name="type" value="2"/>
                    <input type="hidden" id="card_id" name="card_id" value="${id!''}"/>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>卡片名称:</label>
                        <div class="controls">
                            <input type="text" id="cardName" name="name" value="${name!''}" required <#if isOnLine==1>readonly</#if>/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>介绍(50字以内)</label>
                        <textarea class="form-control" id="instruction" name="des" rows="5" cols="10"
                                  maxlength="200" required style="margin-left: 30px;vertical-align: top" <#if isOnLine==1>readonly</#if>>${des!''}</textarea>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>图片</label>
                        <input type="hidden" id="adUrl" name="imgUrl" value="${imgUrl!''}" required/>
                        <#if isOnLine==0>
                            <input type="file" class="fileUpBtn" id="adUpload" accept="image/gif, image/jpeg, image/png, image/jpg" style="margin-left: 93px">
                        </#if>
                        <br/><img src="${imgUrl!''}" id="adImgView" width="50%"/>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>开始时间:</label>
                        <div class="controls" style="display: inline-block;margin-left: 30px">
                            <#if isOnLine==1>
                                <input type="text" id="startDate" name="start" placeholder="开始时间"
                                       class="form-control js-postData" value="${startTime!''}" style="width: 205px;"
                                       autocomplete="OFF" <#if isOnLine==1>readonly</#if>/>
                            <#else>
                                <input type="text" id="startDate" name="start" placeholder="开始时间"
                                       class="form-control js-postData" value="${startTime!''}" style="width: 205px;"
                                       onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});" autocomplete="OFF"/>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"><span style="color: red;font-size: 20px;">*</span>持续时间:</label>
                        <div class="controls" style="display: inline-block;margin-left: 30px">
                            <input type="text" id="weekNum" name="weekNum" value="${weekNum!''}" style="width: 90px;"required <#if isOnLine==1>readonly</#if>>周
                        </div>
                    </div>

                    <div style="vertical-align: middle;text-align:center">
                        <a href="list.vpage" class="btn btn-primary" id="cancelButton">返回</a>
                        <button id="save-color-submit" type="button" class="btn btn-primary">保存</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">

    $("button#save-color-submit").click(function () {
        var frm = $("form#add-color-frm");
        var postData = {};
        var flag = true;
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
        $.post("../save.vpage", postData,
                function (data) {
                    if (data.success) {
                        $("form#add-color-frm")[0].reset();
                        alert("保存成功");
                        window.location = "list.vpage";
                    }
                    else
                        alert(data.success);
                }
        );
    });
    $("#add-color-frm .fileUpBtn").change(function () {
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
                url: '../../recommend/upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                            $("#adImgView").attr('src', data.imgUrl);
                            $("#adImgView").data("file_name", data.imgName);
                            $("#adUrl").val(data.imgUrl);
                    } else {
                        alert("上传失败");
                    }
                }
            });
        }
    });
</script>
</@layout_default.page>