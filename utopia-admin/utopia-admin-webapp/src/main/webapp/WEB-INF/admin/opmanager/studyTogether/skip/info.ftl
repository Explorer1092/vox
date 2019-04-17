<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="跳转详情" page_num=9 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        <span style="color: #00a0e9">跳转管理/</span>跳转详情
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="skip_form" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转ID</label>
                            <div class="controls">
                                <input type="text" id="skipId" name="skipId" class="form-control" value="${content.id!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" class="form-control js-postData" value="${content.skuId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">适用年级 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="grade" name="grade" class="form-control js-postData" value="${content.grade!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="hidden" id="type" name="type" class="form-control js-postData" value="${content.type!''}" style="width: 336px" disabled/>
                                <input type="text" id="type1" name="type1" class="form-control js-postData"
                                       value="<#if content?? && content.type == 1>电子书<#elseif content?? && content.type == 2>毕业证<#elseif content?? && content.type == 3>分享</#if>"
                                       style="width: 336px" disabled/>

                            </div>
                        </div>
                        <div id="train_id" hidden>
                            <div id="train_id" class="control-group">
                                <label class="col-sm-2 control-label">直通车图标出现时间 </label>
                                <div class="controls">
                                    <input id="appearDate" name="appearDate" class="form-control js-postData" type="text" value="${content.appearDate!''}" style="width: 336px" disabled/>
                                </div>
                            </div>
                            <div id="train_id" class="control-group">
                                <label class="col-sm-2 control-label">直通车图标消失时间 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="disappearDate" name="disappearDate" class="form-control js-postData" type="text" value="${content.disappearDate!''}" style="width: 336px" disabled/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">按钮-报名 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="buttonSign" name="buttonSign" class="form-control js-postData" type="text" value="${content.buttonSign!''}"  style="width: 336px" disabled/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">按钮-未报名 <span style="color: red">*</span><span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="buttonNoSign" name="buttonNoSign" class="form-control js-postData" type="text" value="${content.buttonNoSign!''}" style="width: 336px" disabled/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">图标文案-报名 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="iconTextSign" name="iconTextSign" class="form-control js-postData" type="text" value="${content.iconTextSign!''}" style="width: 336px" disabled/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">图标文案-未报名 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="iconTextNoSign" name="iconTextNoSign" class="form-control js-postData" type="text" value="${content.iconTextNoSign!''}" style="width: 336px" disabled/>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转地址 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="jumpUrl" name="jumpUrl" class="form-control js-postData" type="text" value="${content.jumpUrl!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">目标课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="targetSkuId" name="targetSkuId" class="form-control js-postData" type="text" value="${content.targetSkuId!''}" style="width: 336px" disabled/>
                                <span id="targetName"></span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="<#if content??>${content.createUser!''}</#if>" style="width: 336px" disabled/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<div class="layer loading_layer" id="loading_layer"></div>
<div class="loading" id="loading"></div>

<script type="text/javascript">
    $(function () {
        var value = $("#type").val();
        if (value === '1') {
            $("#train_id").show();
        }
    });
</script>
</@layout_default.page>

