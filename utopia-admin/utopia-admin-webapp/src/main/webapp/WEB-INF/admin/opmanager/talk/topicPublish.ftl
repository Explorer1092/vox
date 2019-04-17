<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>

<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<div class="span9">
    <legend>
        <strong>17说</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="publishForm" class="well form-horizontal" method="post" action="/opmanager/talk/savetopic.vpage">
                <input type="hidden" value="${topic.topicId!''}" name="topicId"/>
                <fieldset>
                    <legend>发布/编辑话题</legend>
                    <div class="control-group">
                        <label class="control-label">主标题（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <input type="text" placeholder="请输入话题" maxlength="15" class="input" value="${topic.title!''}" name="title"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="titleImage">标题图片（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="titleImageFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="titleImage" value="${topic.titleImage!''}" id="titleImage" />
                                <img id="titleImageIMG" width="250" src="${topic.titleImage!''}" data-file_name="">
                            </div>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label">副标题：</label>
                        <div class="controls">
                            <input type="text" placeholder="请输入副标题" maxlength="15" class="input" value="${topic.subtitle!''}" name="subtitle"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="subtitleImage">副标题图片：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="subtitleImageFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="subtitleImage" value="${topic.subtitleImage!''}" id="subtitleImage" />
                                <img id="subtitleImageIMG" width="250" src="${topic.subtitleImage!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">期数（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <input type="text" placeholder="请输入期数" maxlength="15" class="input" value="${topic.topicNumber!''}" name="topicNumber"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">活动时间（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <input id="topicStartTime" name="topicStartTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="开始" value="${topic.topicStartTime!''}">
                            至
                            <input id="topicEndTime" name="topicEndTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="结束" value="${topic.topicEndTime!''}">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">设置背景图（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M）</i>
                                <input class="fileUpBtn" id="backgroudFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="background" value="${topic.background!''}" id="background" />
                                <img id="backgroundIMG" width="250" src="${topic.background!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">立场选项（<font color="red">必填</font>）：</label>
                        <div class="radio controls">
                            <label>
                                <input type="radio" class="form-check-input" value="1" name="choiceCount" checked/>单选
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label"></label>
                        <div class="controls">
                            选项1&nbsp;&nbsp;<input type="text" placeholder="选项" class="input" value="${topic.optionText_0!''}" name="optionText_0" maxlength="3"/>
                            <input type="text" placeholder="选项描述" class="input" value="${topic.optionSummary_0!''}" name="optionSummary_0" maxlength="20"/>
                            <input type="hidden" value="${topic.optionId_0!''}" name="optionId_0">
                            <input type="hidden" id="optionShard_1" value="${topic.optionShard_0!''}" name="optionShard_0"/>
                            <br/>
                            <label data-action="optionShard" data-title="1" style="cursor:pointer;color: #4682b4;">上传海报</label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label"></label>
                        <div class="controls">
                            选项2&nbsp;&nbsp;<input type="text" placeholder="选项" class="input" value="${topic.optionText_1!''}" name="optionText_1" maxlength="3"/>
                            <input type="text" placeholder="选项描述" class="input" value="${topic.optionSummary_1!''}" name="optionSummary_1" maxlength="20"/>
                            <input type="hidden" value="${topic.optionId_1!''}" name="optionId_1">
                            <input type="hidden"  id="optionShard_2" value="${topic.optionShard_1!''}" name="optionShard_1"/>
                            <br/>
                            <label data-action="optionShard" data-title="2" style="cursor:pointer;color: #4682b4;">上传海报</label>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label">话题介绍高度（<font color="red">必填</font>）：<br/>
                            <font color="red">建议设置不小于150高度</font></label>
                        <div class="controls">
                            <input type="number" placeholder="话题介绍高度" maxlength="3" class="input" value="${topic.introHeight!''}" name="introHeight"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">话题介绍（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <textarea style="display: none;" class="form-control span8"
                                      placeholder="请输入话题介绍"
                                      name="introduction" rows="3">${topic.intro!''}</textarea>

                            <script id="container" name="container" type="text/plain"></script>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">商品图片：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M）</i>
                                <input class="fileUpBtn" id="productImgFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="productImg" value="${topic.productImg!''}" id="productImg" />
                                <img id="productImgIMG" width="250" src="${topic.productImg!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">商品价格</label>
                        <div class="controls">
                            <input type="number" placeholder="原价" class="input" value="${topic.originalPrice!''}" name="originalPrice"/>
                            <input type="text" placeholder="原价链接" class="input" value="${topic.originalLink!''}" name="originalLink"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label"></label>
                        <div class="controls">
                            <input type="number" placeholder="优惠" class="input" value="${topic.discountPrice!''}" name="discountPrice"/>
                            <input type="text" placeholder="优惠链接" class="input" value="${topic.discountLink!''}" name="discountLink"/>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label" for="productName">活动奖励图片（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M）</i>
                                <input class="fileUpBtn" id="rewardFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="reward" value="${topic.reward!''}" id="reward" />
                                <img id="rewardIMG" width="250" src="${topic.reward!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">孩子可见（<font color="red">必填</font>）：</label>
                        <div class="controls checkbox">
                            <label><input id="scope" name="scope" class="form-control" <#if topic.scope?exists && topic.scope == "3">checked</#if> type="checkbox" value="3"/>孩子</label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">话题类型（<font color="red">必填</font>）：</label>
                        <div class="controls checkbox">
                            <label>
                                <select data="${topic.period!''}" id="period" class="form-control" name="period">
                                    <option value="1" <#if topic.period?exists && topic.period == 1>selected</#if>>当期</option>
                                    <option value="2" <#if topic.period?exists && topic.period == 2>selected</#if>>往期</option>
                                    <option value="3" <#if topic.period?exists && topic.period == 3>selected</#if>>测试</option>
                                </select>
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="coverBack">当期状态入口背景（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="coverBackFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="coverBack" value="${topic.coverBack!''}" id="coverBack" />
                                <img id="coverBackIMG" width="250" src="${topic.coverBack!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <!--div class="control-group">
                        <label class="control-label" for="coverFlag">当期状态视频图标（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="coverFlagFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="coverFlag" value="${topic.coverFlag!''}" id="coverFlag" />
                                <img id="coverFlagIMG" width="250" src="${topic.coverFlag!''}" data-file_name="">
                            </div>
                        </div>
                    </div-->

                    <div class="control-group">
                        <label class="control-label" for="coverTitle">当期状态标题图片（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="coverTitleFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="coverTitle" value="${topic.coverTitle!''}" id="coverTitle" />
                                <img id="coverTitleIMG" width="250" src="${topic.coverTitle!''}" data-file_name="">
                            </div>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label" for="coverOption">当期状态立场图片（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="coverOptionFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="coverOption" value="${topic.coverOption!''}" id="coverOption" />
                                <img id="coverOptionIMG" width="250" src="${topic.coverOption!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="coverPastTitle">往期状态标题图片（<font color="red">必填</font>）：</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="coverPastTitleFile" data-name="photo" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="coverPastTitle" value="${topic.coverPastTitle!''}" id="coverPastTitle" />
                                <img id="coverPastTitleIMG" width="250" src="${topic.coverPastTitle!''}" data-file_name="">
                            </div>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label">是否直播（<font color="red">必填</font>）：</label>
                        <div class="controls checkbox">
                            <label>
                                <select data="${topic.type!''}" id="topicType" class="form-control" name="topicType">
                                    <option value="1">普通</option>
                                    <option value="2">视频</option>
                                    <option value="3">音频</option>
                                </select>
                            </label>
                        </div>
                    </div>

                    <div class="control-group" id="videoTimeDIV">
                        <label class="control-label">直播时间（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <input id="startTime" name="videoStartTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="开始" value="${topic.startTime!''}">
                            至
                            <input id="endTime" name="videoEndTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="结束" value="${topic.endTime!''}">
                        </div>
                    </div>

                    <div class="control-group" id="viewTimeDIV">
                        <label class="control-label">前端展示的直播时间：<br/>（<font color="red">必填</font>）</label>
                        <div class="controls">
                            <input id="viewStartTime" name="viewStartTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="开始" value="${topic.viewStartTime!''}">
                            至
                            <input id="viewEndTime" name="viewEndTime" type="text" size="16"
                                   class="input-xlarge form_datetime" placeholder="结束" value="${topic.viewEndTime!''}">
                        </div>
                    </div>

                    <div class="control-group" id="videoAddrDIV">
                        <label class="control-label">直播id（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <input type="text" class="input" value="${topic.videoAddr!''}" name="videoAddress"/>
                        </div>
                    </div>

                    <div class="control-group" id="videoAvatarDIV">
                        <label class="control-label">直播图片（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M, 690*300）</i>
                                <input class="fileUpBtn" id="videoAvatarFile"  data-name="videoAvatarFile" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       value="${topic.videoAvatar!''}"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="videoAvatar" id="videoAvatar" value="${topic.videoAvatar!''}" />
                                <img id="videoAvatarIMG" width="250" src="${topic.videoAvatar!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div class="control-group" id="audioDIV" style="display: none;">
                        <label class="control-label">音频文稿：</label>
                        <div class="controls">
                            <select name="audioId" id="audioId">
                                <#if audioMap?? && audioMap?size gt 0>
                                    <#list audioMap?keys as key>
                                        <option value="${key!''}">${audioMap[key]!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>

                    <div class="control-group" id="guestNameDIV" style="display: none;">
                        <label class="control-label">嘉宾名称：</label>
                        <div class="controls">
                            <input type="text" value="${topic.guestName!''}" name="guestName" id="guestName"/>
                        </div>
                    </div>

                    <div class="control-group" id="guestAvatarDIV">
                        <label class="control-label">上传嘉宾信息（<font color="red">必填</font>）：</label>
                        <div class="controls">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M, 690*300）</i>
                                <input class="fileUpBtn" id="guestAvatarFile"  data-name="guestAvatarFile" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       value="${topic.guestAvatar!''}"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="guestAvatar" id="guestAvatar" value="${topic.guestAvatar!''}" />
                                <img id="guestAvatarIMG" width="250" src="${topic.guestAvatar!''}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div style="display: none;" class="control-group" id="guestIntroDIV">
                        <label class="control-label">嘉宾介绍：</label>
                        <div class="controls">
                            <textarea class="form-control span8"
                                      placeholder="请输入嘉宾介绍" name="guestIntroduction"
                                      rows="3" maxlength="200">${topic.guestIntro!''}</textarea>
                            <p class="help-block">最多200字</p>
                        </div>
                    </div>

                    <div class="control-group" id="guestProdIntroDIV" style="display: none;">
                        <label class="control-label">嘉宾产品描述：</label>
                        <div class="controls">
                            <input type="text" value="${topic.guestProdIntro!''}" name="guestProdIntro" id="guestProdIntro"/>
                        </div>
                    </div>

                    <div class="control-group" id="guestProdUrlDIV" style="display: none;">
                        <label class="control-label">嘉宾产品链接：</label>
                        <div class="controls">
                            <input type="text" value="${topic.guestProdUrl!''}" name="guestProdUrl" id="guestProdUrl"/>
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
<!-- 模态框（Modal） -->
<div class="modal fade" id="optionShardModal" tabindex="-1" role="dialog" aria-labelledby="optionShardModal" aria-hidden="true">
    <div class="modal-dialog" style="width: 900px;>
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="optionShardModalLabel">

                </h4>
            </div>
            <div class="modal-body">
                <i class="addIcon">上传图片（小于3M），不支持多张同时上传，690*1100</i>
                <input class="fileUpBtn" id="shardFile" data-name="photo" type="file"
                       accept="image/gif, image/jpeg, image/png, image/jpg"
                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                <br/>
                <div id="imgs" data-title="-1">

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭
                </button>
                <button type="button" class="btn btn-primary" id="btn_imgs">
                    确定
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
<script lang="javascript">
    $(function(){

    });
</script>
<script lang="javascript">

    var video = function () {
        var type = $("#topicType").val();
        if(type == 1){
            $("#videoTimeDIV, #videoAddrDIV, #guestAvatarDIV, #viewTimeDIV, #guestIntroDIV, #videoAvatarDIV, #audioDIV, #guestNameDIV, #guestProdUrlDIV, #guestProdIntroDIV").hide();
        }else if(type == 2) {
            $("#videoTimeDIV, #viewTimeDIV, #videoAddrDIV, #videoAvatarDIV, #guestAvatarDIV, #guestProdUrlDIV, #guestProdIntroDIV").show();
            $("#audioDIV").hide();
        }else if(type == 3){
            $("#videoTimeDIV, #viewTimeDIV, #audioDIV, #guestAvatarDIV, #guestProdUrlDIV, #guestProdIntroDIV").show();
            $("#videoAddrDIV, #videoAvatarDIV").hide();
        }
    };

    var uploadValidate = function(object, target, targetText, width, height){
        var file = object.prop('files')[0];
        var image =  new Image();
        image.onload = function(){
            if(width != -1 && height != -1){
                if(image.width != width || image.height != height){
                    alert("图片尺寸不正确");
                    return;
                }
            }
            upload(object, target, targetText);
        }
        var _URL = window.URL || window.webkitURL;
        image.src = _URL.createObjectURL(file);
    }

    var upload = function (object, targetVal, targetTxt) {
        if(object.val() != ''){

            var formData = new FormData();
            formData.append('inputFile', object[0].files[0]);
            $.ajax({
                url: 'upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        var path = data.path;
                        targetTxt.attr('src',path);
                        targetVal.val(path);
                        alert("上传成功");
                    } else {
                        alert("上传失败");
                    }
                }
            });
        }
    }


    var required = function (object, message) {
        if(object.val() == ''){
            alert(message);
            object.focus();
            return false;
        }else{
            return true;
        }
    }

    $(document).ready(function () {
        var ue = UE.getEditor('container', {
            serverUrl: "/opmanager/talk/ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', '|', 'forecolor', 'backcolor', 'insertorderedlist','formatmatch', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|', 'insertvideo',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                '|', 'preview'
            ]]
        });

        ue.ready(function () {
            ue.setContent($("[name='introduction']").val());
        });


        //直播、音频时间
        $("#startTime").datetimepicker({
            autoclose: true,
            todayBtn: true,
            format: 'yyyy-mm-dd hh:ii',
        }).on("changeDate", function (event) {
            var val = $("#startTime").val();
            if(val == ''){
                return;
            }

            $("#topicStartTime").datetimepicker("setEndDate", val);
            $("#endTime").datetimepicker("setStartDate", val);

        });

        $("#endTime").datetimepicker({
            autoclose: true,
            todayBtn: true,
            format: 'yyyy-mm-dd hh:ii',
        }).on("changeDate", function (event) {
            var val = $("#endTime").val();
            if(val == ''){
                return;
            }

            $("#startTime").datetimepicker("setEndDate", val);
            $("#topicEndTime").datetimepicker("setStartDate", val);
        });

        //话题时间
        $("#topicStartTime").datetimepicker({
            autoclose: true,
            todayBtn: true,
            format: 'yyyy-mm-dd hh:ii',
        }).on("changeDate", function (event) {
            var val = $("#topicStartTime").val();

            $("#startTime").datetimepicker("setStartDate", val);
        });

        $("#topicEndTime").datetimepicker({
            autoclose: true,
            todayBtn: true,
            format: 'yyyy-mm-dd hh:ii',
        }).on("changeDate", function (event) {
            var val =  $("#topicEndTime").val();
            if(val == ''){
                return;
            }

            $("#endTime").datetimepicker("setEndDate", val);
        });

        //展示时间
        $("#viewStartTime").datetimepicker({
            autoclose: true,
            todayBtn: true,
            format: 'yyyy-mm-dd hh:ii',
        });

        $("#viewEndTime").datetimepicker({
            autoclose: true,
            todayBtn: true,
            format: 'yyyy-mm-dd hh:ii',
        })

        $("#titleImageFile").change(function () {
            uploadValidate($(this), $("#titleImage"), $("#titleImageIMG"), -1 , -1);
        });

        $("#subtitleImageFile").change(function () {
            uploadValidate($(this), $("#subtitleImage"), $("#subtitleImageIMG"), -1 , -1);
        });

        $("#coverBackFile").change(function () {
            uploadValidate($(this), $("#coverBack"), $("#coverBackIMG"), -1 , -1);
        });
        $("#coverFlagFile").change(function () {
            uploadValidate($(this), $("#coverFlag"), $("#coverFlagIMG"), -1 , -1);
        });
        $("#coverTitleFile").change(function () {
            uploadValidate($(this), $("#coverTitle"), $("#coverTitleIMG"), -1 , -1);
        });
        $("#coverOptionFile").change(function () {
            uploadValidate($(this), $("#coverOption"), $("#coverOptionIMG"), -1 , -1);
        });
        $("#coverPastTitleFile").change(function () {
            uploadValidate($(this), $("#coverPastTitle"), $("#coverPastTitleIMG"), -1 , -1);
        });

        $("#backgroudFile").change(function () {
            uploadValidate($(this), $("#background"), $("#backgroundIMG"), -1, -1);
        });

        $("#guestAvatarFile").change(function () {
            uploadValidate($(this), $("#guestAvatar"), $("#guestAvatarIMG"), 690, 300);
        });

        $("#videoAvatarFile").change(function () {
            uploadValidate($(this), $("#videoAvatar"), $("#videoAvatarIMG"), 690, 300);
        });

        $("#rewardFile").change(function () {
            uploadValidate($(this), $("#reward"), $("#rewardIMG"), -1, -1);
        });

        $("#productImgFile").change(function () {
            uploadValidate($(this), $("#productImg"), $("#productImgIMG"), -1, -1);
        })

        $("#topicType").change(function () {
            video();
        });


        var topicType = $("#topicType").attr("data");
        if(topicType != ''){
            $("#topicType").val(topicType);
        }


        $("#saveBtn").click(function () {
            $("[name='introduction']").val(ue.getContent());

            $("#publishForm").ajaxSubmit({
                url:"/opmanager/talk/savetopic.vpage",
                type:"post",
                dataType:"json",
                beforeSubmit:function(){

                    if(!required($(":input[name='title']"), "请输入标题")){
                        return false;
                    }

                    if($(":input[name='titleImage']").val() == ''){
                        alert("请上传主标题图片");
                        return false;
                    }

                    if(!required($(":input[name='topicNumber']"), "请输入期数")){
                        return false;
                    }

                    if(!required($(":input[name='topicStartTime']"), "请输入活动开始时间")){
                        return false;
                    }

                    if(!required($(":input[name='topicEndTime']"), "请输入活动结束时间")){
                        return false;
                    }

                    if($(":input[name='background']").val() == ''){
                        alert("上传背景图片");
                        return false;
                    }

                    if(!required($(":input[name='optionText_0']"), "请输入选项1")){
                        return false;
                    }
                    if(!required($(":input[name='optionSummary_0']"), "请输入选项1描述")){
                        return false;
                    }
                    if($(":input[name='optionShard_0']").val() == ''){
                        alert("请上传选项1海报");
                        return false;
                    }
                    if(!required($(":input[name='optionText_1']"), "请输入选项2")){
                        return false;
                    }
                    if(!required($(":input[name='optionSummary_1']"), "请输入选项2描述")){
                        return false;
                    }
                    if($(":input[name='optionShard_1']").val() == ''){
                        alert("请上传选项2海报");
                        return false;
                    }

                    if(!required($("[name='introHeight']"), "请输入话题介绍高度")){
                        return false;
                    }

                    if(!required($("[name='introduction']"), "请输入话题介绍")){
                        return false;
                    }

                    if($(":input[name='reward']").val() == ''){
                        alert("上传活动奖励图片");
                        return false;
                    }

                    if($("[name='topicType']").val() != '1'){

                        if(!required($("#startTime"), "请输入直播开始时间")){
                            return false;
                        }

                        if(!required($("#endTime"), "请输入直播结束时间")){
                            return false;
                        }

                        if(!required($("#viewStartTime"), "请输入前端展示的直播开始时间")){
                            return false;
                        }

                        if(!required($("#viewEndTime"), "请输入前端展示的直播结束时间")){
                            return false;
                        }

                        /*if(!required($(":input[name='guestName']"), "请输入嘉宾名称")){
                            return false;
                        }*/

                        if($("#guestAvatar").val() == ''){
                            alert("上传嘉宾信息");
                            return false;
                        }

                        /*if(!required($("[name='guestIntroduction']"), "请输入嘉宾介绍")){
                            return false;
                        }*/

                        if($("[name='topicType']").val() == '2'){
                            if(!required($("[name='videoAddress']"), "请输入直播ID")){
                                return false;
                            }
                        }

                        if($("#videoAvatar").val() == ''){
                            alert("上传直播图片");
                            return false;
                        }
                    }

                    if($(":input[name='coverBack']").val() == ''){
                        alert("上传当期封面图片");
                        return false;
                    }
                    if($(":input[name='coverFlag']").val() == ''){
                        alert("上传视频标识图片");
                        return false;
                    }
                    if($(":input[name='coverTitle']").val() == ''){
                        alert("上传当期标题图片");
                        return false;
                    }
                    if($(":input[name='coverOption']").val() == ''){
                        alert("上传当期观念图片");
                        return false;
                    }
                    if($(":input[name='coverPastTitle']").val() == ''){
                        alert("上传往期标题图片");
                        return false;
                    }

                    return confirm("确定发布话题么？");
                },
                success:function(data){
                    if(data.success){
                        document.location.href="/opmanager/talk/topiclist.vpage";
                    }else {
                        alert("服务器异常");
                    }
                },
                clearForm:false,
                resetForm:false
            });
        });

        $("button[data-action='add-shard']").click(function () {
            $(this).before($(this).prev().clone().val(""));
        })

        //显示上传图片
        $("label[data-action='optionShard']").click(function () {
            var id = $(this).attr("data-title");

            $("#imgs").attr("data-title", id);
            $("#imgs").empty();

            var str = $("#optionShard_"+id).val();

            if(str != ""){
                var strs = str.split(",");
                for(var i = 0; i < strs.length; i++){
                    var html =  "<div style=\"width: 160px; float: left;margin: 5px;  text-align: center;\">\n" +
                            "                        <img src=\"" + strs[i] + "\" width=\"160px;\">\n" +
                            "                        <br/>\n" +
                            "                        <button style=\"margin: 3px;\">删除</button>\n" +
                            "                    </div>"

                    $("#imgs").html($("#imgs").html() + html);
                }

                $("#imgs button").click(function () {
                    if(confirm("确实要删除么？")) {
                        $(this).parent().remove();
                    }
                })
            }

            $("#optionShardModalLabel").text("选项"+id+"分享图片");

            $("#optionShardModal").modal("show").css({
                width: 'auto',
                'margin-left': function () {
                    return -($(this).width() / 2);
                }
            });
        });

        //上传分享图片
        $("#shardFile").change(function () {
            if($("#imgs img").size() >= 5){
                alert("最多上传5张");
                return;
            }
            var object = $(this);

            var file = object.prop('files')[0];
            var image =  new Image();
            image.onload = function(){
                if(image.width != 690 || image.height != 1100){
                    alert("图片尺寸不正确");
                    return;
                }
                uploadPoster(object);
            }
            var _URL = window.URL || window.webkitURL;
            image.src = _URL.createObjectURL(file);

        });

        var uploadPoster = function (object) {
            object.attr("disabled", true);
            var formData = new FormData();
            formData.append('inputFile', object[0].files[0]);
            $.ajax({
                url: 'upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        var path = data.path;
                        var html = "<div style=\"width: 160px; float: left;margin: 5px;  text-align: center;\">\n" +
                                "                        <img src=\"" + path + "\" width=\"160px;\">\n" +
                                "                        <br/>\n" +
                                "                        <button style=\"margin: 3px;\">删除</button>\n" +
                                "                    </div>";
                        $("#imgs").html($("#imgs").html() + html);
                        $("#imgs button").click(function () {
                            if(confirm("确实要删除么？")) {
                                $(this).parent().remove();
                            }
                        })
                    } else {
                        alert("上传失败");
                    }
                },
                error:function () {
                    alert("服务器错误");
                },
                complete:function () {
                    object.attr("disabled", false);
                }
            });

        }

        //设置分享图片
        $("#btn_imgs").click(function () {
            var id = $("#imgs").attr("data-title");
            var str = "";
            $("#imgs img").each(function () {
                str += $(this).attr("src")+",";
            });
            if(str != ""){
                str = str.substring(0, str.length - 1);
            }
            var valId = "#optionShard_"+id;
            $(valId).val(str);

            $("#optionShardModal").modal("hide");
        });

        video();

    })
</script>
</@layout_default.page>