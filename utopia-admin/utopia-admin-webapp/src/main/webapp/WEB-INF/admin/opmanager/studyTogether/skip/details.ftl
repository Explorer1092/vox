<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑通知" page_num=9 jqueryVersion ="1.7.2">
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
        <span style="color: #00a0e9">跳转管理/</span>添加|编辑跳转
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存跳转"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="skip_form" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="skipId_hid" name="skipId" value="${skipId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <#-- 跳转ID-->
                        <#if skipId?has_content>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转ID</label>
                            <div class="controls">
                                <input type="text" id="skipId" name="skipId" class="form-control" value="${skipId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 课程ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="skuId" name="skuId" <#if content?? && content.skuId??>disabled</#if> class="form-control js-postData" value="${content.skuId!''}" onblur="checkName('#skuId')" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 适用年级-->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">适用年级 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="grade" name="grade" style="width: 350px;" class="js-postData" <#if content?? && content.grade??>disabled</#if>>
                                <option value="">--请选择年级信息--</option>
                                <#if grades??>
                                    <#list grades as lels>
                                        <option <#if content?? && content.grade??><#if content.grade == lels> selected="selected"</#if></#if> value = ${lels!}>${lels!}</option>
                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 跳转类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 336px;" class="js-postData" <#if content?? && content.type??>disabled</#if>>
                                <option value="">--请选择跳转类型--</option>
                                <#if types??>
                                    <#list types as lels>
                                        <option <#if content?? && content.type??><#if content.type == lels> selected="selected"</#if></#if> value = ${lels!}>
                                            <#if lels?? && lels == 1>直通车<#elseif lels?? && lels == 2>毕业证<#elseif lels?? && lels == 3>分享</#if>
                                        </option>

                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>

                        <div id="train_id" hidden>
                            <div id="train_id" class="control-group">
                                <label class="col-sm-2 control-label">直通车图标出现时间 </label>
                                <div class="controls">
                                    <input id="appearDate" name="appearDate" class="form-control js-postData" type="text" value="${content.appearDate!''}" style="width: 336px"/>
                                </div>
                            </div>
                            <div id="train_id" class="control-group">
                                <label class="col-sm-2 control-label">直通车图标消失时间 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="disappearDate" name="disappearDate" class="form-control js-postData" type="text" value="${content.disappearDate!''}" style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">按钮-报名 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="buttonSign" name="buttonSign" class="form-control js-postData" type="text" value="${content.buttonSign!''}"  style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">按钮-未报名 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="buttonNoSign" name="buttonNoSign" class="form-control js-postData" type="text" value="${content.buttonNoSign!''}" style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">图标文案-报名 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="iconTextSign" name="iconTextSign" class="form-control js-postData" type="text" value="${content.iconTextSign!''}" style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">图标文案-未报名 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="iconTextNoSign" name="iconTextNoSign" class="form-control js-postData" type="text" value="${content.iconTextNoSign!''}" style="width: 336px"/>
                                </div>
                            </div>
                        </div>

                        <#-- 二维码生成地址 -->
                        <div id="qrcode_id" hidden>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">毕业证分享二维码 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input id="qrurl" name="qrurl" class="form-control js-postData" type="text" value="${qrUrl!''}"  style="width: 336px" disabled/>
                                    <input id="create_id" type="button" value="生成二维码" class="btn/">这里只要设置毕业证跳转信息，一定会产生二维码信息，如果更改跳转地址一定要重新的创建二维码信息
                                </div>
                            </div>
                        </div>

                        <#-- 跳转地址 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转地址 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="jumpUrl" name="jumpUrl" class="form-control js-postData" type="text" value="${content.jumpUrl!''}" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 目标课程ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">目标课程ID </label>
                            <div class="controls">
                                <input id="targetSkuId" name="targetSkuId" class="form-control js-postData" type="text" value="${content.targetSkuId!''}" style="width: 336px" onblur="checkName('#targetSkuId')"/>
                            </div>
                        </div>

                        <#-- 备注说明 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${createUser!''}" style="width: 336px" readonly/>
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

        var value = $("#type").find("option:selected").val();
        if (value === '1') {
            $("#train_id").show();
        }
        if (value === '2') {
            $("#qrcode_id").show();
        }

        $("#type").change(function () {
            var value = $("#type").find("option:selected").val();
            if (value === '1') {
                $("#train_id").show();
            } else {
                $("#train_id").hide();
            }
            if (value === '2') {
                $("#qrcode_id").show();
            } else {
                $("#qrcode_id").hide();
            }
        });

        $('#appearDate').datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $('#disappearDate').datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        //验证表单
        var validateForm = function () {
            var msg = "";
            if($('#skuId').val() === ''){
                msg += "课程ID为空！\n";
            }
            if($('#grade').val() === ''){
                msg += "年级信息为空！\n";
            }
            if($('#type').val() === '') {
                msg += "跳转类型未选择！\n";
            }
            if($('#type').val() === '1'){
                if($('#buttonSign').val() === ''){
                    msg += "按钮-报名为空！\n";
                }
                if($('#buttonNoSign').val() === ''){
                    msg += "按钮-未报名为空！\n";
                }
                if($('#iconTextSign').val() === ''){
                    msg += "图标文案-报名为空！\n";
                }
                if($('#iconTextNoSign').val() === ''){
                    msg += "图标文案-未报名为空！\n";
                }
                if($('#disappearDate').val() === ''){
                    msg += "直通车图标消失时间 为空！\n";
                }
            }
            if($('#type').val() === '2'){
                if($('#qrurl').val() === ''){
                    msg += "请创建一个二维码！\n";
                }
            }
            if($('#jumpUrl').val() === ''){
                msg += "跳转地址为空！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        };

        //保存提交
        $(document).on("click",'#save_ad_btn',function () {
            if(validateForm()){
                var flag = true;
                var skuId = $("#skuId").val();
                var grade = $("#grade").val();
                var type = $("#type").val();
                var skipId_hid = $("#skipId_hid").val();
                $.ajaxSettings.async = false;
                if (skipId_hid === "") {
                    if (type && skuId) {
                        $.get("check_id.vpage", {skuId: skuId,grade: grade, type: type}, function (data) {
                            if (!data.success) {
                                alert("此跳转信息已经存在，请更改课程ID,年级或跳转类型后重新尝试");
                                $("#skuId").val('');
                                $("#type").val('');
                                $("#grade").val('');
                                flag = false;
                            }
                        });
                    }
                    if (!flag) {
                        return;
                    }
                }
                $.ajaxSettings.async = true;
                var post = {};
                $(".js-postData").each(function(i,item){
                    post[item.name] = $(item).val();
                });
                $.post('save.vpage',post,function (res) {
                    if(res.success){
                        alert("保存成功");
                        location.href= 'index.vpage';
                    }else{
                        alert("保存失败");
                    }
                });
            }
        });

        $(document).on("click",'#create_id',function () {
            var type = $("#type").val();
            if (type === '' || type != '2') {
                return;
            }
            var skuId = $("#skuId").val();
            var jumpUrl = $("#jumpUrl").val();
            if (skuId === undefined || jumpUrl === undefined || skuId === '' || jumpUrl === '') {
                alert("请先填写跳转地址和课程ID");
                return;
            }
            $.ajax({
                type: "POST",
                url: "create_qrcode.vpage",
                data: {
                    skuId: skuId,
                    jumpUrl: jumpUrl
                },
                success: function (data) {
                     $("#qrurl").val(data.newUrl);
                }
            });

        });
    });

    function checkName(id) {
        var skuId = $(id).val();
        if (skuId) {
            $.get("/opmanager/studytogether/common/sku_name.vpage", {skuId: skuId}, function (data) {
                if (!data.success) {
                    alert(skuId + "对应的SKU不存在");
                    $(id).val('');
                }
            });
        }
    }
</script>
</@layout_default.page>

