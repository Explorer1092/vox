<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑周奖励" page_num=9 >

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
        添加/编辑奖励
        <a type="button" id="btn_cancel" href="wrindex.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存奖励"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="rewardForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="rewardId" name="rewardId" value="${rewardId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <#-- 奖励ID-->
                        <#if rewardId != 0>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励ID</label>
                            <div class="controls">
                                <input type="text" id="rewardId" name="rewardId" class="form-control" value="${rewardId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 奖励名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control js-postData" value="${content.name!''}" style="width: 336px" maxlength="8"/>
                            </div>
                        </div>

                        <#-- 奖励图标 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励图标 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="iconUrl" readonly name="iconUrl" class="form-control js-postData input" value="${content.iconUrl!''}" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="jpg#png#jpeg">
                                <a id="iconUrl" class="btn btn-success preview"   data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.iconUrl!''}"</#if>">预览</a>
                            </div>
                        </div>

                        <#-- 奖励类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 350px;" class="js-postData">
                                <option value="">--请选择奖励类型--</option>
                                <#if types??>
                                    <#list types as type>
                                        <option <#if content?? && content.type??><#if content.type == type> selected="selected"</#if></#if> value = ${type!}>
                                            <#if type?? && type == 2>视频<#elseif type?? && type == 3>学习币<#elseif type?? && type == 4>电子书<#elseif type?? && type == 5>周报告<#elseif type?? && type == 6>家长奖励</#if>
                                        </option>
                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 奖励内容：1图书 2视频 3学习币 4电子书 5周报告 6家长奖励-->
                        <div id="ebook_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">电子书ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="ebookId" name="ebookId" class="form-control js-postData" value="<#if obj??>${obj.ebookId!''}</#if>" style="width: 336px"/>
                                <span id="ebookName"></span>
                            </div>
                        </div>
                        <div id="video_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">上传视频 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="videoUrl" readonly name="videoUrl" class="form-control js-postData input" value="<#if obj??>${obj.videoUrl!''}</#if>" style="width: 336px"/>
                                <input class="upload_file" type="file" data-suffix="mp4">
                                <a class="btn btn-success preview" data-href="<#if obj?? && cdn_host??>${cdn_host!''}${obj.videoUrl!''}</#if>">预览</a>
                            </div>

                        </div>
                        <div id="coin_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">学习币类型ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="coinType" name="coinType" class="form-control js-postData" value="<#if obj??>${obj.coinType!''}</#if>" style="width: 336px"/>
                                <span id="coinName"></span>
                            </div>
                        </div>
                        <div id="report_id" hidden>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">宝箱文物顺序 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input type="number" id="seq" name="seq" placeholder="顺序必须是数字且大于等于1" class="form-control js-postData" value="<#if obj??>${obj.seq!''}</#if>" style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">报告标题 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input type="text" id="title" name="title" class="form-control js-postData" value="<#if obj??>${obj.title!''}</#if>" style="width: 336px"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">报告描述 <span style="color: red">*</span></label>
                                <div class="controls">
                                    <input type="text" id="desc" name="desc" class="form-control js-postData" value="<#if obj??>${obj.desc!''}</#if>" style="width: 336px"/>
                                </div>
                            </div>
                        </div>
                        <div id="reward_id" class="control-group" hidden>
                            <label class="col-sm-2 control-label">家长奖励类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="rewardType" name="rewardType" class="form-control js-postData" value="<#if obj??>${obj.type!''}</#if>" style="width: 336px"/>
                                <span id="rewardName"></span>
                            </div>
                        </div>
                        <#-- 备注说明 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明</label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者</label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${createUser!''}" style="width: 336px;" readonly/>
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
        if (value === '2') {
            $("#ebook_id").hide();
            $("#video_id").show();
            $("#coin_id").hide();
            $("#report_id").hide();
            $("#reward_id").hide();
        } else if (value === '3') {
            $("#ebook_id").hide();
            $("#video_id").hide();
            $("#coin_id").show();
            $("#report_id").hide();
            $("#reward_id").hide();
        } else if (value === '4') {
            $("#ebook_id").show();
            $("#video_id").hide();
            $("#coin_id").hide();
            $("#report_id").hide();
            $("#reward_id").hide();
        } else if (value === '5') {
            $("#ebook_id").hide();
            $("#video_id").hide();
            $("#coin_id").hide();
            $("#reward_id").hide();
            $("#report_id").show();
        } else if (value === '6') {
            $("#ebook_id").hide();
            $("#video_id").hide();
            $("#coin_id").hide();
            $("#report_id").hide();
            $("#reward_id").show();
        }

        //奖励内容切换
        $("#type").change(function () {
            var value = $("#type").find("option:selected").val();
            if (value === '2') {
                $("#ebook_id").hide();
                $("#video_id").show();
                $("#coin_id").hide();
                $("#report_id").hide();
                $("#reward_id").hide();
            } else if (value === '3') {
                $("#ebook_id").hide();
                $("#video_id").hide();
                $("#coin_id").show();
                $("#report_id").hide();
                $("#reward_id").hide();
            } else if (value === '4') {
                $("#ebook_id").show();
                $("#video_id").hide();
                $("#coin_id").hide();
                $("#report_id").hide();
                $("#reward_id").hide();
            } else if (value === '5') {
                $("#ebook_id").hide();
                $("#video_id").hide();
                $("#coin_id").hide();
                $("#reward_id").hide();
                $("#report_id").show();
            } else if (value === '6') {
                $("#ebook_id").hide();
                $("#video_id").hide();
                $("#coin_id").hide();
                $("#report_id").hide();
                $("#reward_id").show();
            } else {
                alert("类型错误");
            }
        });

        //上传视频
        $(".upload_file").change(function () {
            showLoadingFn();
            var $this = $(this);
            var suffix = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                var acceptSuffix = new String($this.attr("data-suffix")).split("#");
                if (acceptSuffix.indexOf(suffix) === -1) {
                    alert("仅支持以下文件格式" + acceptSuffix);
                    hideLoadingFn();
                    return;
                }
                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                $.ajax({
                    url: '/opmanager/studytogether/common/upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        hideLoadingFn();
                        if (data.success) {
                            $($this.closest('.controls').find("input.input")).attr("value", data.fileName);
                            $($this.closest('.controls').find("a.btn-success")).attr("data-href", data.fileUrl);
                        } else {
                            hideLoadingFn();
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        //文件预览
        $(document).on("click", "a.preview", function () {
            var link = $(this).attr("data-href");
            if (!link) {
                alert("文件上传中，请稍后预览");
                return;
            }
            window.open(link);
        });

        //学习币检测
        $("#coinType").blur(function () {
            var coinType = $("#coinType").val();
            if (coinType) {
                $.get("/opmanager/studytogether/common/coin.vpage", {coinType: coinType}, function (data) {
                    if (data.success) {
                        $("#coinName").html(data.coinName);
                    } else {
                        alert(coinType + "对应的学习币类型不存在");
                        $("#coinType").val("");
                        $("#coinName").html("");
                        return;
                    }
                });
            } else {
                $("#coinType").val("");
                $("#coinName").html("");
                return;
            }
        });

        //电子书检测
        $("#ebookId").blur(function () {
            var ebookId = $("#ebookId").val();
            if (ebookId) {
                $.get("/opmanager/studytogether/common/ebook.vpage", {ebookId: ebookId}, function (data) {
                    if (data.success) {
                        $("#ebookName").html(data.ebookName);
                    } else {
                        alert(ebookId + "对应的电子书不存在");
                        $("#ebookId").val("");
                        $("#ebookName").html("");
                        return;
                    }
                });
            } else {
                $("#ebookId").val("");
                $("#ebookName").html("");
                return;
            }
        });

        //家长奖励检测
        $("#rewardType").blur(function () {
            var rewardType = $("#rewardType").val();
            if (rewardType) {
                $.get("/opmanager/studytogether/common/parent_reward.vpage", {rewardType: rewardType}, function (data) {
                    if (data.success) {
                        $("#rewardName").html(data.rewardName);
                    } else {
                        alert(rewardType + "对应的家长奖励类型不存在");
                        $("#rewardType").val("");
                        $("#rewardName").html("");
                        return;
                    }
                });
            } else {
                $("#rewardType").val("");
                $("#rewardName").html("");
                return;
            }
        });

        //验证表单
        var num_reg = /^[0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#name').val() === ''){
                msg += "奖励名称为空！\n";
            }
            if($('#iconUrl').val() === ''){
                msg += "请上传奖励图标！\n";
            }
            if($('#type').val() === ''){
                msg += "请选择奖励类型！\n";
            }
            if($('#type').val() === '2' && $('#videoUrl').val() === ''){
                msg += "视频地址为空！\n";
            }
            if($('#type').val() === '3' && $('#coinType').val() === ''){
                msg += "学习币类型为空！\n";
            }
            if($('#type').val() === '4' && $('#ebookId').val() === ''){
                msg += "电子书ID为空！\n";
            }
            if($('#type').val() === '5' && ($('#seq').val() === '' || $('#seq').val() <=0 || !$('#seq').val().match(num_reg))){
                msg += "宝箱文物顺序为空或格式不正确！\n";
            }
            if($('#type').val() === '5' && $('#title').val() === ''){
                msg += "报告标题！\n";
            }
            if($('#type').val() === '5' && $('#desc').val() === ''){
                msg += "报告描述！\n";
            }
            if($('#type').val() === '6' && $('#rewardType').val() === ''){
                msg += "家长奖励类型为空！\n";
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
                var post = {};
                $(".js-postData").each(function(i,item){
                    post[item.name] = $(item).val();
                });
                $.post('wrsave.vpage',post,function (res) {
                    if(res.success){
                        alert("保存成功");
                        location.href= 'wrindex.vpage';
                    }else{
                        alert("保存失败");
                    }
                });
            }
        });

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

