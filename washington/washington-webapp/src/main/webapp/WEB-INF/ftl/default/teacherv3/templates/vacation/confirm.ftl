<script id="t:confirm" type="text/html">
    <div id="saveMathDialog" class="h-homework-dialog03 h-homework-dialog" style="width: 100%;">
        <div class="inner">
            <div class="list">
                <div class="name">布置班级：</div>
                <div class="info grade" style="width: 430px;">
                    <!--ko if:clazzNames() != null && clazzNames().length > 0-->
                    <!--ko foreach:clazzNames-->
                    <span data-bind="text:$data"></span>
                    <!--/ko-->
                    <!--/ko-->
                    <!--ko ifnot:clazzNames() != null && clazzNames().length > 0-->
                    <span>班级未显示</span>
                    <!--/ko-->
                </div>
            </div>
            <div class="list">
                <div class="name" style="float:left;">计划天数：</div>
                <div class="pack">
                    <ul>
                        <!--ko foreach:{data:planDays(),as:'planDay'}-->
                        <li class="job-pack" data-bind="css:{'job-pack-active':$root.focusPlanDay() == planDay.totalDay},click:$root.planDayClick.bind($data,$root)" style="cursor:pointer;">
                            <span class="p-checkbox"></span>
                            <span><!--ko text:planDay.totalDay--><!--/ko-->天作业包</span>
                        </li>
                        <!--/ko-->
                    </ul>
                </div>
            </div>
            <div class="list">开始时间：
                <label style="cursor: pointer;padding-right: 0px;">
                    <input type="text" id="startDateInput" data-bind="value:startDate" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.startHourSelect,value:startFocusHour"></select>时
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.startMinSelect,value:startFocusMin"></select>分
                </label>
            </div>
            <div class="list">截止时间：
                <label style="cursor: pointer;padding-right: 0px;">
                    <input type="text" id="endDateInput" data-bind="value:endDate" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.endHourSelect,value:endFocusHour"></select>时
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.endMinSelect,value:endFocusMin"></select>分
                </label>
            </div>
            <div class="list tips-grey" style="color: #FF6802;">* 请老师根据开学时间要求学生按时提交作业</div>
            <div class="btn-box">
                <a href="javascript:void(0)" data-bind="click:saveHomework.bind($data,$element)" class="w-btn w-btn-well" style="font-size: 18px; width: 118px; padding: 9px 0;">确认布置</a>
            </div>
        </div>
    </div>
</script>
<#include "../downteacherapptip.ftl">