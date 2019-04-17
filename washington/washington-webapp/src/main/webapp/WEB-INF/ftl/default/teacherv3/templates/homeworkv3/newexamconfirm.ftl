<script id="t:confirm" type="text/html">
    <div id="saveMathDialog" class="h-homework-dialog03 h-homework-dialog" style="width: 100%;">
        <div class="inner">
            <div class="list">
                <div class="name">布置班级：</div>
                <span data-bind="text:$root.groupNames()"></span>
            </div>
            <div class="list">
                <div class="name">答题时长：</div>
                <span data-bind="text:$root.durationMinutes()+'分钟'"></span>
            </div>
            <div class="list">测试开始时间：
                <label style="cursor: pointer;padding-right: 0px;">
                    <input type="text" id="startDateInput" data-bind="value:startDate" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.startHourSelect,value:startFocusHour"></select>时
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.startMinSelect,value:startFocusMin"></select>分
                </label>
            </div>
            <div class="list">测试截止时间：
                <label style="cursor: pointer;padding-right: 0px;">
                    <input type="text" id="endDateInput" data-bind="value:endDate" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.endHourSelect,value:endFocusHour"></select>时
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.endMinSelect,value:endFocusMin"></select>分
                </label>
            </div>
            <div style="line-height:24px;font-size:14px;color:#7e7e7e">
                <p style="color:#4e5656">温馨提示：</p>
                <p>1.学生在考试开启时才能作答</p>
                <p>2.学生需要在考试关闭前完成考试，否则考试无效</p>
            </div>
            <div class="btn-box"><a href="javascript:void(0)" data-bind="click:saveNewExam" class="w-btn w-btn-well" style="font-size: 18px; width: 118px; padding: 9px 0;">发布测试</a></div>
        </div>
    </div>
</script>

<script id="t:confirmSuccess" type="text/html">
    <div class="w-noContent" style="padding: 0;">
        <i class="successTips-icon"></i>
        <p class="tipsTxt">已成功布置专项测试<br>您可以在"测试报告"中查看已布置的试卷</p>
    </div>
</script>