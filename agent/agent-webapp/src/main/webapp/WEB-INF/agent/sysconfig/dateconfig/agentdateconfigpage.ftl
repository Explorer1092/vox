<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='市经理调整学校' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 市经理调整学校</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="add_dict" class="btn btn-success" href="javascript:void(0)">
                    <i class="icon-plus icon-white"></i>
                    编辑
                </a>
            </div>
        </div>
        <div class="box-content">
            <#if cityManagerConfig??>
                开放日期：每月 ${cityManagerConfig.startDay!0}-${cityManagerConfig.endDay!0}日
            </#if>
        </div>
    </div>
</div>

<div id="configDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">开放日期</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <div class="controls">
                            每月：
                            <input type="text" style="width: 25px;" id="startDate" maxlength="2"
                                   value="<#if cityManagerConfig??>${cityManagerConfig.startDay!0}</#if>"/>
                            -
                            <input type="text" style="width: 25px;" id="endDate" maxlength="2"
                                   value="<#if cityManagerConfig??>${cityManagerConfig.endDay!0}</#if>"/>
                            日
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="update_btn" type="button" class="btn btn-primary">保存</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $("#add_dict").live("click", function () {
        $("#configDialog").modal('show');
    });
    $("#update_btn").live("click", function () {
        var startTime = $("#startDate").val();
        var endTime = $("#endDate").val();
        if (!validNumber(startTime)) {
            alert("开始日期必须为数字");
            return;
        }
        if (!validNumber(startTime)) {
            alert("结束日期必须为数字");
            return;
        }
        if (startTime > 31) {
            alert("开始日期不能大于31日");
            return;
        }
        if (endTime > 31) {
            alert("结束日期不能大于31日");
            return;
        }
        if (parseInt(startTime) > parseInt(endTime)) {
            alert("开始日期不能大于结束日期");
            return;
        }
        var data = {
            start: startTime,
            end: endTime
        };
        $.post("updatecitymanagerconfigschool.vpage", data, function (data) {
            if (data.success) {
                alert("更新成功！")
                window.location.reload();
            } else {
                alert(data.info);
                $("#configDialog").modal('hide');
            }
        })
    })
</script>
</@layout_default.page>