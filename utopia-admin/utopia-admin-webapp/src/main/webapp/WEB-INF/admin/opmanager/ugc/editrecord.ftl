<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend>添加编辑UGC活动</legend>
    <div class="row-fluid">
        <div class="span12">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">范围类型：</label>
                        <div class="controls">
                            <#if types??>
                                <select id="codeType" name="codeType">
                                    <#list types as t >
                                        <option value="${t.name()!}" <#if record?? && (record.codeType == t.name())> selected </#if> >${t.name()!}</option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">用户角色：</label>
                        <div class="controls tagIdsContent">
                            <#if userTypes?? >
                                <select id="userType" name="userType">
                                    <#list userTypes as t >
                                        <option value="${t.name()!}" <#if record?? && (record.userType == t.name())> selected </#if> >${t.description!}</option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">是否校园大使专享：</label>
                        <div class="controls">
                            <input type="checkbox" id="ambassadorOnly" name="ambassadorOnly" <#if record?? && record.ambassadorOnly>checked="checked"</#if>  />
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="productName">活动名称：</label>
                        <div class="controls">
                            <input type="text" <#if record??>value="${record.name!''}"</#if> name="recordName"  id="recordName" class="input">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">活动开始时间</label>
                        <div class="controls">
                            <input type="text" id="startDate" name="startDate" <#if record??> value="${record.startDate?string("yyyy-MM-dd HH:mm:ss")!''}"</#if> />
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">活动结束时间</label>
                        <div class="controls">
                            <input type="text" id="endDate" name="endDate" <#if record??> value="${record.endDate?string("yyyy-MM-dd HH:mm:ss")!''}"</#if> />
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">发布线上：</label>
                        <div class="controls">
                            <input type="checkbox" id="published" name="published" <#if record?? && record.published>checked="checked"</#if>  />
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">是否删除：</label>
                        <div class="controls">
                            <input type="checkbox" id="disabled" name="disabled" <#if record?? && record.disabled>checked="checked"</#if>  />
                        </div>
                    </div>
                </fieldset>
            <div class="modal-footer">
                <button id="saveRuleBtn" class="btn btn-primary">确 定</button>
            </div>

        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("#startDate").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $("#endDate").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });


        $("#saveRuleBtn").on("click", function () {
            var recordMapper = {
                codeType: $("#codeType").val(),
                userType: $("#userType").val(),
                recordName: $("#recordName").val(),
                startDate: $("#startDate").val(),
                endDate: $("#endDate").val(),
                ambassadorOnly: $("#ambassadorOnly").prop("checked"),
                published: $("#published").prop("checked"),
                disabled: $("#disabled").prop("checked")
                <#if record??>, recordId: '${(record.id)!''}'</#if>
            };
            if (recordMapper.recordName == undefined || recordMapper.recordName.trim() == '') {
                alert("请输入活动名称");
                return false;
            }
            if (recordMapper.startDate == undefined || recordMapper.startDate.trim() == '') {
                alert("请选择开始时间");
                return false;
            }
            if (recordMapper.endDate == undefined || recordMapper.endDate.trim() == '') {
                alert("请选择结束时间");
                return false;
            }
            $.ajax({
                type: "post",
                url: "saverecord.vpage",
                data: recordMapper,
                success: function (data) {
                    if (data.success) {
                        window.location.href='index.vpage';
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>