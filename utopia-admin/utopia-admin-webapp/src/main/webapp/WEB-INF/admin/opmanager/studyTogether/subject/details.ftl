<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑主题
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="subjectId" name="subjectId" value="${subjectId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">

                        <#-- 主题ID -->
                        <#if subjectId != 0>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">主题ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="subjectId" name="subjectId" class="form-control" value="${subjectId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 系列ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="seriesId" name="seriesId" class="form-control js-postData" value="${content.seriesId!''}" style="width: 336px"/>
                                <span id="seriesName"></span>
                            </div>
                        </div>

                        <#-- 主题名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">主题名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control js-postData" value="${content.name!''}" style="width: 336px"/>
                                <span id="spuName"></span>
                            </div>
                        </div>

                        <#-- 主题顺序 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">主题顺序 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="seq" name="seq" placeholder="整数填写" class="form-control js-postData" value="${content.seq!''}" style="width: 336px"/>
                            </div>
                        </div>

                        <#-- 配置环境 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择配置环境--</option>
                                <#if levels??>
                                    <#list levels as lels>
                                        <option <#if content?? && content.envLevel??><#if content.envLevel == lels> selected="selected"</#if></#if> value = ${lels!}>
                                            <#if lels?? && lels == 10>单元测试环境
                                            <#elseif lels?? && lels == 20>开发环境
                                            <#elseif lels?? && lels == 30>测试环境
                                            <#elseif lels?? && lels == 40>预发布环境
                                            <#elseif lels?? && lels == 50>生产环境
                                            </#if>
                                        </option>
                                    </#list>
                                </#if>
                                </select>
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

         $("#seriesId").blur(function () {
             var seriesId = $("#seriesId").val();
             if (seriesId) {
                 $.get("/opmanager/studytogether/common/series_name.vpage", {seriesId: seriesId}, function (data) {
                     if (data.success) {
                         $("#seriesName").html(data.seriesName);
                     } else {
                         alert(seriesId + "对应的系列不存在");
                         $("#seriesId").val("");
                         $("#seriesName").html("");
                         return;
                     }
                 });
             } else {
                 $("#seriesId").val("");
                 $("#seriesName").html("");
                 return;
             }
         });

         $("#subjectId").blur(function () {
             var subjectId = $("#subjectId").val();
             if (subjectId) {
                 $.post("checkId.vpage", {subjectId: subjectId}, function (data) {
                     if (!data.success) {
                         alert(subjectId + "对应的主题已经存在");
                         $("#subjectId").val("");
                         return;
                     }
                 });
             }
         });

        var num_reg = /^[0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#seriesId').val() == ''){
                msg += "SPU系列ID为空！\n";
            }
            if($('#name').val() == ''){
                msg += "主题名称为空！\n";
            }
            if($('#seq').val() == '' || !$('#seq').val().match(num_reg)){
                msg += "主题顺序为空或不是数字！\n";
            }
            if($('#envLevel').val() == ''){
                msg += "配置环境为空！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        };

        $(document).on("click",'#save_ad_btn',function () {
            if(validateForm()){
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

        $("#seq").blur(function () {
            var seriesId = $("#seriesId").val();
            if(seriesId === '') {
                alert("请先填写系列ID");
                $("#seq").val('');
                return;
            }
            var seq = $("#seq").val();
            if (!seq || seq <= 0) {
                alert("顺序需要大于0");
                $("#seq").val('');
                return;
            }
            $.post(
                "check_seq.vpage",
                {
                    seriesId: seriesId,
                    seq: seq
                },
                function (data) {
                if (!data.success) {
                    alert("对应的系列中已经存在" + seq + ",请查证后重新填写");
                    return;
                }
            });
        });
    });
</script>
</@layout_default.page>

