<script id="t:confirm" type="text/html">
    <div id="saveMathDialog" class="h-homework-dialog03 h-homework-dialog" style="width: 100%;">
        <div class="inner">
            <div class="list">布置时间：<span data-bind="text:startDate"></span></div>
            <div class="list">
                <div class="name">布置班级：</div>
                <div class="info grade">
                    <!--ko if:clazzNames() != null && clazzNames().length > 0-->
                        <!--ko foreach:{data:clazzNames,as:'group'}-->
                        <span><!--ko text:group.groupName--><!--/ko-->(预计<!--ko text:Math.ceil(group.seconds/60)--><!--/ko-->分钟)</span>
                        <!--/ko-->
                    <!--/ko-->
                    <!--ko ifnot:clazzNames() != null && clazzNames().length > 0-->
                        <span>班级未显示</span>
                    <!--/ko-->
                </div>
            </div>
            <div class="list">
                <span class="tips-grey" style="padding-left:25px;">(学生实际完成用时受设备和网络影响，且每班题目可能有所不同，预计时长仅供参考)</span>
            </div>
            <div class="list"><div class="name">布置内容：</div>
                <div class="info">
                    <!--ko foreach:{data : tabDetails,as:'detail'}-->
                    <span class="tj"><strong data-bind="text:detail.tabName"></strong>
                        （共<strong data-bind="text:detail.assignCnt">0</strong><!--ko text:$root.getUnitOfMeasure(detail.tabType)--><!--/ko-->）
                    </span>
                    <!--/ko-->
                </div>
            </div>
            <div class="list">截止时间：
                <label style="cursor: pointer;" data-bind="click:changeEndDate.bind($data,0,'zero'),css:{'w-radio-current':endLabel() == 'zero'}"><span class="w-radio"></span> <span class="w-icon-md">今天内</span></label>
                <label style="cursor: pointer;" data-bind="click:changeEndDate.bind($data,1,'one'),css:{'w-radio-current':endLabel() == 'one'}"><span class="w-radio"></span> <span class="w-icon-md">明天内</span></label>
                <label style="cursor: pointer;" data-bind="click:changeEndDate.bind($data,2,'two'),css:{'w-radio-current':endLabel() == 'two'}"><span class="w-radio"></span> <span class="w-icon-md">三天内</span></label>
                <label style="cursor: pointer;padding-right: 0px;" data-bind="click:changeEndDate.bind($data,-1,'custom'),css:{'w-radio-current':endLabel() == 'custom'}">
                    <span class="w-radio"></span> <span class="w-icon-md">自定义</span>
                    <input type="text" id="endDateInput" data-bind="value:endDateInput" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.hourSelect,value:focusHour"></select>时
                    <select class="w-int" style="width: 60px;" data-bind="options:$root.minSelect,value:focusMin"></select>分
                </label>
            </div>
            <div class="list tips-grey">提交作业截止时间为<span data-bind="text:displayEndDate"></span></div>
            <p style="padding: 10px 0;" data-bind="if:$root.showBean,visible:$root.showBean">
                给每班奖励学豆：<span class="operation" data-bind="css:{'operation-disabled': beanCount() <= 0},click:$root.minusBean.bind($data,$element)">-</span><input title="" type="text" data-bind="textInput: beanCount" class="beansCount" /><span class="operation" data-bind="css:{'operation-disabled': beanCount() >= maxBeanCount() },click:$root.plusBean.bind($data,$element)">+</span><span class="w-icon w-icon-39"></span>
                <span style="color: red; display: none;" data-bind="visible:beanCount() > maxBeanCount()">当前设置的奖励学豆数已超过上限<!--ko text:maxBeanCount()--><!--/ko-->个，请降低</span>
            </p>
            <p id="confirm_message">
                作业注意事项:
            <div class="t-homework-mis" style="padding: 5px 0 0 0;">
                <textarea id="v-leave-message" class="w-int" maxlength="100" data-bind="textInput:comment.view" style="height: 70px; line-height: 22px;"></textarea>
                <div class="m-info" style="right: 5px; bottom: 0;">还可以输入<strong id="v-leave-message-num" data-bind="text:(100 - comment().length)">100</strong>个字</div>
            </div>
            </p>
            <div class="btn-box"><a href="javascript:void(0)" data-bind="css:{'w-btn-disabled' : beanCount() > maxBeanCount()},click:saveHomework.bind($data,$element)" class="w-btn w-btn-well" style="font-size: 18px; width: 118px; padding: 9px 0;">确认布置</a></div>
        </div>
    </div>
</script>
<#include "../downteacherapptip.ftl">