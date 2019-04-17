<#import "../../module.ftl" as module>
<@module.learningCenter menuName='history'>
    <@sugar.capsule js=["datepicker"] css=["homeworkhistory.report","plugin.datepicker"] />
<div class="h-historyWork w-fl-right h-historyBox">
    <div class="h-title-2">
        <span class="left-text"></span>
    </div>
    <div style="margin-left: 30px;margin-top: 30px">
        <div>
            <div class="hWork-btns" style="width:100%;text-align: left">
                <p style="padding-bottom: 4px;">
                    <span style="display:inline-block;width: 74px">选择日期：</span>
                    <input style="width: 140px;" readonly="readonly" type="text" id="beginDateInput" class="date_icon" />
                    <a href="javascript:void(0);" class="hw-btn hw-btn-green searchBtn" style="line-height:1;width:80px;margin-left:10px">查询</a>
                </p>
            </div>
        </div>
        <p style="margin-left: 74px;margin-bottom: 20px;color: #999;">你将查询从选择日期开始的30天内作业记录</p>
    </div>
    <div class="J_mainContent" style="margin-top:20px"></div>
</div>
<div class="message_page_list" id="sharingPage"></div>
<script type="text/javascript">
    var $homeworkReportList = {
        env : <@ftlmacro.getCurrentProductDevelopment />
    }

    var constantObj = {
        defaultStartDate  : "${startDate!}"
    };
</script>
    <@sugar.capsule js=["studentreport.earlylist"] />
</@module.learningCenter>
