<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑课节
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存课节"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="clazzFestivalForm" name="detail_form" enctype="multipart/form-data" action="save.vpage" method="post">
                    <input id="clazzFestivalId_hid" name="clazzFestivalId" value="${clazzFestivalId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <#-- 课节ID-->
                        <#if clazzFestivalId != 0>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课节ID</label>
                            <div class="controls">
                                <input type="text" id="clazzFestivalId" name="clazzFestivalId" class="form-control" value="${clazzFestivalId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                        <#-- 章节ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">章节ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="chapterId" name="chapterId" class="form-control js-postData" type="text" value="<#if content??>${content.chapterId!''}</#if>" style="width: 336px;" maxlength="30"/>
                                <span id="chapterName"></span>
                            </div>
                        </div>

                        <#-- SKU_ID 雪瑞确认 skuId 应该是不允许编辑的，只做展示用。-->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SKU_ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="skuId" name="skuId" class="form-control js-postData" type="text" value="<#if content??>${content.skuId!''}</#if>" style="width: 336px;" disabled/>
                                <span style="color: red">课节配置完成后，SKU_ID如果为空，不要惊慌，五分钟后自动加载</span>
                            </div>
                        </div>

                        <#-- 课节类型 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课节类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择课节类型--</option>
                                <#if types??>
                                    <#list types as type>
                                        <option <#if content?? && content.type??><#if content.type == type> selected="selected"</#if></#if> value = ${type!}>
                                            <#if type?? && type == 1>
                                                普通
                                            <#elseif type?? && type == 2>
                                                复习
                                            </#if>
                                        </option>
                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>

                        <#-- 课节名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课题名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="name" name="name" class="form-control js-postData" type="text" value="<#if content??>${content.name!''}</#if>" style="width: 336px;" maxlength="30"/>
                            </div>
                        </div>

                        <#-- 展示顺序 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">展示顺序 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="seq" name="seq" class="form-control js-postData" type="number" value="<#if content??>${content.seq!''}</#if>" style="width: 336px;" maxlength="30" placeholder="整数填写"/>
                            </div>
                        </div>

                        <#-- 开始时间 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开课日期 </label>
                            <div class="controls">
                                <input type="text" id="openDate" name="openDate" class="form-control js-postData" value="<#if content??>${content.openDate!''}</#if>" onclick="WdatePicker({dateFmt: 'yyyy-MM-dd HH:mm:ss'});" autocomplete="OFF" style="width: 336px;" />
                            </div>
                        </div>

                        <#-- 签到积分奖励 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">签到积分奖励 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="signScore" name="signScore" class="form-control js-postData" value="<#if content??>${content.signScore!''}</#if>" style="width: 336px" maxlength="20"  placeholder="正整数填写"/>分
                            </div>
                        </div>

                        <#-- 签到额外积分奖励 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">签到额外积分奖励 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="signExtraScore" name="signExtraScore" class="form-control js-postData" value="<#if content??>${content.signExtraScore!''}</#if>" style="width: 336px" maxlength="20" placeholder="自然数填写"/>分
                            </div>
                        </div>

                        <#-- 内容模板ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">内容模板ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="templateId" name="templateId" class="form-control js-postData" type="text" value="<#if content??>${content.templateId!''}</#if>" style="width: 336px;" maxlength="30"/>
                                <span id="templateName"></span>
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
                                                <#if lels?? && lels == 10>单元测试
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
                            <label class="col-sm-2 control-label">备注说明 </label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px;"/>
                            </div>
                        </div>

                        <#-- 创建者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 </label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${createUser!''}" readonly style="width: 336px;"/>
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

        //章节ID检测
        $("#chapterId").blur(function () {
            var chapterId = $("#chapterId").val();
            if (chapterId) {
                $.get("/opmanager/studytogether/common/chapter_name.vpage", {chapterId: chapterId}, function (data) {
                    if (data.success) {
                        $("#chapterName").html(data.chapterName);
                    } else {
                        alert(chapterId + "对应的章节不存在");
                        $("#chapterId").val("");
                        $("#chapterName").html("");
                        return;
                    }
                });
            } else {
                $("#chapterId").val("");
                $("#chapterName").html("");
                return;
            }
        });

        //模板ID检测
        $("#templateId").blur(function () {
            var templateId = $("#templateId").val();
            var chapterId = $("#chapterId").val();
            if (chapterId === '') {
                alert("请先填写章节ID");
                return;
            }
            if (templateId && chapterId) {
                $.get("/opmanager/studytogether/common/template_name.vpage", {templateId: templateId, chapterId: chapterId}, function (data) {
                    if (data.success) {
                        $("#templateName").html(data.templateName);
                    } else {
                        alert(templateId + "对应的模板不存在");
                        $("#templateId").val("");
                        $("#templateName").html("");
                        return;
                    }
                });
            } else {
                $("#templateId").val("");
                $("#templateName").html("");
                return;
            }
        });

        $("#seq").blur(function () {
            var chapterId = $("#chapterId").val();
            if(chapterId === '') {
                alert("请先填写章节ID");
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
                        chapterId: chapterId,
                        seq: seq
                    },
                    function (data) {
                        if (!data.success) {
                            alert("对应的章节中已经存在" + seq + ",请查证后重新填写");
                            $("#seq").val('');
                            return;
                        }
                    });
        });

        //验证表单
        var num_reg = /^[0-9]*$/;
        var validateForm = function () {
            var msg = "";
            if($('#chapterId').val() === ''){
                msg += "章节ID不能为空！\n";
            }
            if($('#type').val() === ''){
                msg += "请选择课节类型！\n";
            }
            if($('#name').val() === ''){
                msg += "请填写课节名称！\n";
            }
            if($('#seq').val() === '' || $('#seq').val() <=0 || !$('#seq').val().match(num_reg)) {
                msg += "展示顺序非正整数，重新填写！\n";
            }
            var signScore = $('#signScore').val();
            if(signScore === '' || !signScore.match(num_reg)){
                msg += "签到积分奖励非自然数，重新填写！\n";
            }
            if($('#signExtraScore').val() === '' || !$('#signExtraScore').val().match(num_reg)){
                msg += "签到额外积分奖励非自然数，重新填写！\n";
            }
            if($('#templateId').val() === ''){
                msg += "请填写内容模板ID \n";
            }
            // if($('#envLevel').val() === ''){
            //     msg += "请选择配置环境！\n";
            // }
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

