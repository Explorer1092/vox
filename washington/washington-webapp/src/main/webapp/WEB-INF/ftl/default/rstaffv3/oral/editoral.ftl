<!DOCTYPE html>
<html>
<head>
<#include "../../nuwa/meta.ftl" />
<title>一起作业，一起作业网，一起作业学生</title>
<@sugar.capsule js=["jquery", "core", "alert", "template", "ZeroClipboard", "datepicker"] css=["plugin.alert", "rstaff.main", "plugin.datepicker"] />
    <style type="text/css">
        div.jqi .jqimessage{
            padding: 10px;
            line-height: 20px;
            color: #444444;
            font-size: 13px;
        }
    </style>
<@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div class="inviteBox" style="padding-top: 40px;text-align: center;" xmlns="http://www.w3.org/1999/html">
    <#if errorMsg?has_content>
        ${errorMsg}
    <#else>
        <div class="spacing_vox_bot">
            <input type="hidden" id="pushId" value="${pushId!}"/>
            <input id="startDate" style="width: 110px;" type="text" placeholder="起始日期" class="int_vox" readonly="readonly"/>
            <label for="startDate"><i class='icon_rstaff icon_rstaff_6'></i></label>
            <input id="endDate" style="width: 110px;" type="text" placeholder="结束日期" class="int_vox" readonly="readonly"/>
            <label for ="endDate"><i class='icon_rstaff icon_rstaff_6'></i></label></i>
            <a id="oralRegionSubmitBtn" href="javascript:void(0);" class="btn_vox btn_vox_warning">
                <i class='icon_rstaff icon_rstaff_20'></i> 提交
            </a>
        </div>
        <div class="clear"></div>
        <div id="oralresult_20150515"></div>
    </#if>

</div>
<script type="text/javascript">
    $(function(){
        /** 开始时间 */
        $("#startDate").datepicker({
            dateFormat: 'yy-mm-dd',
            defaultDate: "",
            numberOfMonths: 1,
            onSelect: function (selectedDate) {
                $('#endDate').datepicker('option', 'minDate', selectedDate);
                $("#startDate").removeClass("alert_vox_error");
            }
        });

        /** 结束时间 */
        $('#endDate').datepicker({
            dateFormat: 'yy-mm-dd',
            defaultDate: "",
            numberOfMonths: 1,
            onSelect:function(){
                $("#endDate").removeClass("alert_vox_error");
            }
        });

        //修改开始结束时间
        $("#oralRegionSubmitBtn").on("click",function(){
            var $this = $(this);
            var pushId = $("#pushId").val();
            if($17.isBlank(pushId)){
                $("#oralresult_20150515").html("选择口语无效，关闭窗口请重新选择");
                return false;
            }
            if($this.isFreezing()){
                return false;
            }
            $this.freezing();
            $.ajax({
                type: "POST",
                url: "/rstaff/oral/adjustoraltime.vpage",
                data: {
                    pushId     : pushId,
                    startDate  : $("#startDate").val(),
                    endDate    : $("#endDate").val()
                },
                success: function (data){
                    $this.thaw();
                    $("#oralresult_20150515").html(data.info);
                    if(data.success){
                        parent.paperOperate.init();
                        parent.$.prompt.close();
                    }
                }
            });
        });


    });
</script>
<@sugar.site_traffic_analyzer_end />
</body>
</html>