<script id="T:CONFIRM" type="text/html">
    <div id="saveMathDialog" class="h-homework-dialog03 h-homework-dialog" style="width: 100%;">
        <div class="inner">
            <div class="list">
                <div class="name">布置班级：</div>
                <div class="info grade">
                    <template v-if="clazzNames.length > 0">
                        <span v-for="(groupName,index) in clazzNames" v-text="groupName"></span>
                    </template>
                    <template v-if="clazzNames.length == 0">
                        <span>班级未显示</span>
                    </template>
                </div>
            </div>
            <div class="list"><div class="name" v-text="paperType + '：'"></div>
                <div class="info">
                    <span class="tj"><strong v-text="paperInfo.paperName"></strong></span>
                </div>
            </div>
            <div class="list">
                <div class="tips-grey" style="padding-left:70px;">共<span v-text="paperInfo.questionCount">0</span>题&nbsp;&nbsp;预计<span v-text="paperInfo.minutes">0</span>分钟 &nbsp;&nbsp;作答限时<span v-text="paperInfo.examTime">0</span>分钟</div>
            </div>

            <div class="list">开始时间：
                <label style="cursor: pointer;padding-right: 0;">
                    <input type="text" id="startDateInput" v-model="startDateInput" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" v-model="startHour">
                        <option v-for="(hour,index) in startHourSelect" v-text="hour"></option>
                    </select>时
                    <select class="w-int" style="width: 60px;" v-model="startMin">
                        <option v-for="(min,index) in startMinSelect" v-text="min"></option>
                    </select>分
                </label>
            </div>
            <div class="list" style="display: none;">
                <div class="tips-grey" style="padding-left:70px;">(学生在开始时间前30分钟可以看到单元检测信息)</div>
            </div>
            <div class="list">截止时间：
                <label style="cursor: pointer;padding-right: 0;">
                    <input type="text" id="endDateInput" v-model="endDateInput" placeholder="自定义时间" readonly="readonly" class="c-ipt">
                </label>
                <label>
                    <select class="w-int" style="width: 60px;" v-model="endHour">
                        <option v-for="(hour,index) in endHourSelect" v-text="hour"></option>
                    </select>时
                    <select class="w-int" style="width: 60px;" v-model="endMin">
                        <option v-for="(min,index) in endMinSelect" v-text="min"></option>
                    </select>分
                </label>
            </div>
            <div class="list" style="display: none;">
                <div class="tips-grey" style="padding-left:70px;">(学生统一在该时间点可以查看作答报告)</div>
            </div>
            <div class="btn-box"><a href="javascript:void(0)" v-bind:class="{'w-btn-disabled' : false}" v-on:click="saveHomework" class="w-btn w-btn-well" style="font-size: 18px; width: 118px; padding: 9px 0;">确认布置</a></div>
        </div>
    </div>
</script>