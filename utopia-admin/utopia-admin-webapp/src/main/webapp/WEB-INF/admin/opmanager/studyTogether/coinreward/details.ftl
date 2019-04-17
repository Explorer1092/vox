<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑连续奖励
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="cscrId" name="cscrId" value="${cscrId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">

                        <#-- 连续奖励ID-->
                        <#if cscrId?has_content>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">连续奖励ID</label>
                            <div class="controls">
                                <input type="text" id="cscrId" name="cscrId" class="form-control" value="${cscrId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 奖励周数 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励周数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="weekCount" name="weekCount" placeholder="整数填写" <#if content?? && content.weekCount??>disabled</#if> class="form-control js-postData" value="${content.weekCount!''}" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 奖励天数 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励天数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="dayCount" name="dayCount" placeholder="整数填写" <#if content?? && content.dayCount??>disabled</#if> class="form-control js-postData" value="${content.dayCount!''}" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 学习币类型ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学习币类型ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="coinTypeId" name="coinTypeId" class="form-control js-postData" value="${content.coinTypeId!''}" style="width: 336px;"/>
                                <span id="coinName"></span>
                            </div>
                        </div>

                        <#-- 家长奖励类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">家长奖励类型 </label>
                            <div class="controls">
                                <input type="text" id="rewardType" name="rewardType" class="form-control js-postData" value="${content.rewardType!''}" style="width: 336px;"/>
                                <span id="rewardName"></span>
                            </div>
                        </div>

                        <#-- 奖励描述 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励描述 </label>
                            <div class="controls">
                                <input type="text" id="desc" name="desc" class="form-control js-postData" value="${content.desc!''}" style="width: 336px;"/>
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

<script type="text/javascript">
    $(function () {

        $("#coinTypeId").blur(function () {
            var coinType = $("#coinTypeId").val();
            if (coinType) {
                $.get("/opmanager/studytogether/common/coin.vpage", {coinType: coinType}, function (data) {
                    if (data.success) {
                        $("#coinName").html(data.coinName);
                    } else {
                        alert(coinType + "对应的学习币类型不存在");
                        $("#coinTypeId").val("");
                        $("#coinName").html("");
                        return;
                    }
                });
            } else {
                $("#coinTypeId").val("");
                $("#coinName").html("");
                return;
            }
        });

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

        var num_reg = /^[1-9][0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#weekCount').val() == '' || !$('#weekCount').val().match(num_reg)){
                msg += "奖励周数为空或不是数字！\n";
            }
            if($('#dayCount').val() == '' || !$('#dayCount').val().match(num_reg)){
                msg += "奖励天数为空或不是数字！\n";
            }
            if($('#coinTypeId').val() == ''){
                msg += "学习币类型ID为空！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        };

        $(document).on("click",'#save_ad_btn',function () {
            if(validateForm()){
                /*var flag = true;
                var weekCount = $("#weekCount").val();
                var dayCount = $("#dayCount").val();
                $.ajaxSettings.async = false;
                if (weekCount && dayCount) {
                    $.get("check_id.vpage", {weekCount: weekCount, dayCount: dayCount}, function (data) {
                        if (!data.success) {
                            alert("此奖励已经存在，请更改周数或者天数后重新尝试");
                            $("#weekCount").val('');
                            $("#dayCount").val('');
                            flag = false;
                        }
                    });
                }
                if (!flag) {
                    return;
                }
                $.ajaxSettings.async = true;*/
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

    });
</script>
</@layout_default.page>

