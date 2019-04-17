<div data-bind="visible : $data.selectedHomeworkType() == 'VOICE_OBJECTIVE' ">
    <!-- ko foreach: {data : $data.voiceObjectiveDetail(), as : '_vo'} -->
    <div class="mhw-base mar-b20" >
        <#--<div class="mb-hd clearfix" data-bind="visible: _vo.assignTimes() > 0" style="display: none;">
            <div class="fl txt-grey">
                <span>共被使用<span data-bind="text: _vo.assignTimes()" style="padding: 0"></span>次</span>
            </div>
        </div>-->
        <div class="mb-mn pad-30">
            <div data-bind="attr:{id : 'voiceObjectiveImg' + $index()}"></div>
            <div data-bind="text:$root.loadVoiceObjectiveImg(_vo.questionId(),$index())"></div>
            <div class="mhw-selectBtns mar-t14">
                <!--ko ifnot: _vo.checked()-->
                <a data-bind="click: $root.addVoiceObjective" href="javascript:void(0)" class="btn w-btn w-btn-s">
                    <strong>+</strong>选入
                </a>
                <!--/ko-->

                <!--ko if: _vo.checked()-->
                <a data-bind="click: $root.removeVoiceObjective" class="btn w-btn w-btn-s w-btn-lightBlue" href="javascript:void(0)">
                    <strong>-</strong>移除
                </a>
                <!--/ko-->
            </div>
        </div>
    </div>
    <!--/ko-->
</div>


