<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='增值Task' page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    span.label {
        margin-left: 5px;
    }
</style>
<span class="span9" id="main_container_updata">
    <div class="form-horizontal">
    <fieldset>
        <div id="legend" class="">
              <legend class="">新建或更新任务模版</legend>
        </div>
        <div class="control-group">
              <label class="control-label" for="input01">任务标题</label>
              <div class="controls">
                <input type="text" placeholder="请输入任务标题" class="input-xlarge" v-model="template.title">
                <p class="help-block"></p>
              </div>
        </div>
        <div class="control-group">
              <label class="control-label" for="input01">任务副标题</label>
              <div class="controls">
                <input type="text" placeholder="请输入任务副标题" class="input-xlarge" v-model="template.subtitle">
                <p class="help-block"></p>
              </div>
         </div>
         <div class="control-group">
              <label class="control-label" for="input01">描述</label>
              <div class="controls">
                <input type="text" placeholder="请输入描述" class="input-xlarge" v-model="template.desc">
                <p class="help-block"></p>
              </div>
         </div>
         <div class="control-group">
              <label class="control-label" for="input01">任务标签</label>
              <div class="controls">
                <input type="text" placeholder="请输入任务标签" class="input-xlarge" v-model="template.lable">
                <p class="help-block"></p>
              </div>
         </div>
        <div class="control-group">
              <label class="control-label">任务来源</label>
              <div class="controls">
                  <select class="input-xlarge"  v-model="template.source">
                     <option v-for="item in source" :value="item.key">{{item.value}}</option>
                  </select>
              </div>
        </div>
        <div class="control-group">
              <label class="control-label" for="input01">任务图标</label>
              <div class="controls">
                <input type="text" placeholder="任务图标" class="input-xlarge" v-model="template.icon">
                <p class="help-block"></p>
              </div>
         </div>
        <div class="control-group">
              <label class="control-label" for="input01">任务奖励倍数</label>
              <div class="controls">
                <input type="number" placeholder="" class="input-xlarge" v-model="template.multiple">
                <p class="help-block"></p>
              </div>
         </div>
        <div class="control-group">
              <label class="control-label">任务有效环境</label>
              <div class="controls">
                  <select class="input-xlarge" v-model="template.env">
                     <option v-for="item in modes" :value="item.key">{{item.value}}</option>
                  </select>
              </div>
        </div>
        <div class="control-group">
            <label class="control-label">周期</label>
            <div class="controls">
                <select class="input-xlarge" v-model="template.cycleType" @change="selectChange">
                    <option v-for="item in types" :value="item.key">{{item.value}}</option>
                </select>
                <div v-show="template.cycleType=='day'">
                    <input type="text" id="dayStartTimePicker" :value="startTime" @change="change01('dayStartTimePicker')">
                    <span class="label label-info" @click="changeStartTime('00:00:00')">00:00:00</span>
                    <span class="label label-info" @click="changeStartTime('12:00:00')">12:00:00</span>
                    <span class="label label-info" @click="changeStartTime('16:00:00')">16:00:00</span>
                    <br>
                    <input type="text" id="dayEndTimePicker" :value="endTime" @change="change02('dayEndTimePicker')">
                    <span class="label label-info" @click="changeEndTime('20:59:59')">20:59:59</span>
                    <span class="label label-info" @click="changeEndTime('22:59:59')">22:59:59</span>
                    <span class="label label-info" @click="changeEndTime('23:59:59')">23:59:59</span>
                </div>
                <div v-show="template.cycleType=='week'">
                    <select v-model="weekStart" >
                        <option v-for="item in weekDays" :value="item.value">{{item.text}}</option>
                    </select>
                    <input id="weekStartTimePicker" type="text" :value="startTime" @change="change01('weekStartTimePicker')">
                    <span class="label label-info" @click="changeStartTime('00:00:00')">00:00:00</span>
                    <span class="label label-info" @click="changeStartTime('12:00:00')">12:00:00</span>
                    <span class="label label-info" @click="changeStartTime('16:00:00')">16:00:00</span>
                    <br>
                    <select v-model="weekEnd" >
                        <option v-for="item in weekDays" :value="item.value">{{item.text}}</option>
                    </select>
                    <input id="weekEndTimePicker" type="text" :value="endTime" @change="change02('weekEndTimePicker')">
                    <span class="label label-info" @click="changeEndTime('20:59:59')">20:59:59</span>
                    <span class="label label-info" @click="changeEndTime('22:59:59')">22:59:59</span>
                    <span class="label label-info" @click="changeEndTime('23:59:59')">23:59:59</span>
                </div>
                <div v-show="template.cycleType=='month'">
                    <select  v-model="monthStart">
                        <option v-for="item in months" :value="item">{{item}}</option>
                    </select>
                    <input id="monthStartTimePicker" type="text" :value="startTime" @change="change01('monthStartTimePicker')">
                    <span class="label label-info" @click="changeStartTime('00:00:00')">00:00:00</span>
                    <span class="label label-info" @click="changeStartTime('12:00:00')">12:00:00</span>
                    <span class="label label-info" @click="changeStartTime('16:00:00')">16:00:00</span>
                    <br>
                    <select  v-model="monthEnd">
                        <option v-for="item in months" :value="item">{{item}}</option>
                    </select>
                    <input id="monthEndTimePicker" type="text" :value="endTime" @change="change02('monthEndTimePicker')">
                    <span class="label label-info" @click="changeEndTime('20:59:59')">20:59:59</span>
                    <span class="label label-info" @click="changeEndTime('22:59:59')">22:59:59</span>
                    <span class="label label-info" @click="changeEndTime('23:59:59')">23:59:59</span>
                </div>
                <div v-show="template.cycleType=='fixed'">
                    <input  id="fixedStartTimePicker" type="text" :value="startTime" @change="change01('fixedStartTimePicker')">
                    <span class="label label-info" @click="changeStartTime('00:00:00')">00:00:00</span>
                    <span class="label label-info" @click="changeStartTime('12:00:00')">12:00:00</span>
                    <span class="label label-info" @click="changeStartTime('16:00:00')">16:00:00</span>
                <br>
                    <input  id="fixedEndTimePicker" type="text" :value="endTime" @change="change02('fixedEndTimePicker')">
                    <span class="label label-info" @click="changeEndTime('20:59:59')">20:59:59</span>
                    <span class="label label-info" @click="changeEndTime('22:59:59')">22:59:59</span>
                    <span class="label label-info" @click="changeEndTime('23:59:59')">23:59:59</span>
                </div>
            </div>
        </div>
        <div class="control-group">
              <label class="control-label">模板状态</label>
              <div class="controls">
                  <select class="input-xlarge" v-model="template.status">
                     <option v-for="item in statuses" :value="item.key">{{item.value}}</option>
                  </select>
              </div>
        </div>
        <div class="control-group">
            <label class="control-label">模板的生效时间</label>
            <div class="controls">
                <input  id="formStartTimePicker" @change="changeFrom" type="text" :value="template.from">
                <br>
                <input  id="toEndTimePicker" @change="changeTo" type="text" :value="template.to">
            </div>
        </div>
        <div class="control-group">
              <label class="control-label">学科</label>
              <div class="controls">
                  <select class="input-xlarge" v-model="template.subject">
                     <option v-for="item in subjects" :value="item.key">{{item.value}}</option>
                  </select>
              </div>
        </div>
        <div class="control-group">
              <label class="control-label" for="input01">排序</label>
              <div class="controls">
                <input type="number" placeholder="" class="input-xlarge" v-model="template.rank">
                <p class="help-block"></p>
              </div>
        </div>
        <div class="control-group">
              <label class="control-label">规则</label>
              <div class="controls">
                <div class="textarea">
                      <textarea  v-model="trule" style="width:430px;height:100px"> </textarea>
                </div>
              </div>
        </div>
        <div class="control-group">
              <label class="control-label">特例备注</label>
              <div class="controls">
                <div class="textarea">
                      <textarea  v-model="tattachment" style="width:430px;height:100px"> </textarea>
                </div>
              </div>
        </div>
        <div class="control-group">
            <div class="controls">
                  <div class="btn btn-info" @click="submit">提交</div>
            </div>
        </div>
    </fieldset>
  </div>
</span>

<script>
    function getQuery(key) {
        let reg = new RegExp("(^|&)" + key + "=([^&]*)(&|$)");
        let res = window.location.search.substr(1).match(reg);
        return res != null ? decodeURIComponent(res[2]) : null;
    }
    let id = getQuery("id") || "";

    let vm = new Vue({
        el: '#main_container_updata',
        data: {
            modes:JSON.parse('${json_encode(modes)}'),
            statuses:JSON.parse('${json_encode(statuses)}'),
            types:JSON.parse('${json_encode(types)}'),
            subjects:JSON.parse('${json_encode(subjects)}'),
            source:JSON.parse('${json_encode(sources)}'),
            weekStart:'2',
            weekEnd:'2',
            monthStart:'0',
            monthEnd:'0',
            endTime:'',
            startTime:'',
            trule:'{}',
            tattachment:'{}',
            template:{
                title:'',
                subtitle:'',
                lable:'',
                desc:'',
                env:'测试环境',
                from:'',
                to:'',
                status:'',
                subject:'',
                rank:1,
                source:'learning',
                multiple:1,
                icon:'',
                rule:'{}',
                attachment:'{}',
                cycleType:'day',
                startTime:'',
                endTime:'',
            },
            weekDays: [
                {text: '周一', value: 2},
                {text: '周二', value: 3},
                {text: '周三', value: 4},
                {text: '周四', value: 5},
                {text: '周五', value: 6},
                {text: '周六', value: 7},
                {text: '周日', value: 1}
            ],
            months:[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 0]
        },
        methods: {
            change01(id) {
                let self = this;
                if ($('#'+id).val()!='') {
                    self.startTime = $('#'+id).val();
                }
            },
            change02(id) {
                let self = this;
                if ($('#'+id).val()!='') {
                    self.endTime = $('#'+id).val();
                }
            },
            changeFrom(){
                let self = this;
                if ($('#formStartTimePicker').val()!='') {
                    self.template.from = $('#formStartTimePicker').val();
                }
            },
            changeTo(){
                let self = this;
                if ($('#toEndTimePicker').val()!='') {
                    self.template.to = $('#toEndTimePicker').val();
                }
            },
            changeStartTime(time){
               let self = this;
               let timer = self.startTime;
               if(timer==undefined) {
                  return;
               }else if(timer.length >= 10) {
                   timer = timer.substring(0, 10);
               }else {
                   timer = "${.now?string("yyyy-MM-dd")}";
               }
               if (self.template.cycleType =='fixed') {
                   self.startTime = timer+' '+ time;
               }else {
                   self.startTime = time;
               }
            },
            changeEndTime (time) {
                let self = this;
                let timer = self.endTime;
                if(timer==undefined) {
                    return;
                }else if(timer.length >= 10) {
                    timer = timer.substring(0, 10);
                }else {
                    timer = "${.now?string("yyyy-MM-dd")}";
                }
                if (self.template.cycleType =='fixed') {
                    self.endTime = timer+' '+ time;
                }else {
                    self.endTime = time;
                }
            },
            changeFromTime(time) {
                let self = this;
                let timer = self.template.from;
                if(timer==undefined) {
                    return;
                }else if(timer.length >= 10) {
                    timer = timer.substring(0, 10);
                }else {
                    timer = "${.now?string("yyyy-MM-dd")}";
                }
                self.template.from = timer+' '+ time;
            },
            changeToTime(time) {
                let self = this;
                let timer = self.template.to;
                if(timer==undefined) {
                    return;
                }else if(timer.length >= 10) {
                    timer = timer.substring(0, 10);
                }else {
                    timer = "${.now?string("yyyy-MM-dd")}";
                }
                self.template.to = timer+' '+ time;
            },
            selectChange(){
                this.startTime = "";
                this.endTime = "";
            },
            momentDatetime(date) {
                return moment(new Date(date)).format("YYYY-MM-DD HH:mm:ss");
            },
            initData () {
                let self = this;
                $.post('loadtasktemplatebytemplateid.vpage',{templateId:id},function(res){
                    if(res.success){
                        self.template = res.template;
                        let obj={};
                        if(res.template.rule.attributes!=undefined){
                            obj=res.template.rule.attributes;
                            obj.eventType=res.template.rule.eventType;
                            obj.totalTimes=res.template.rule.totalTimes;
                        }else{
                            obj=res.template.rule;
                        }
                        self.template.rule = obj;
                        self.trule = JSON.stringify(self.template.rule);
                        self.tattachment = JSON.stringify(self.template.attachment);
                        self.startTime = res.template.startTime;
                        self.endTime = res.template.endTime;
                        self.template.from = self.momentDatetime(self.template.from);
                        self.template.to = self.momentDatetime(self.template.to);
                        if (res.template.endTime) {
                            if(res.template.cycleType=="week"||res.template.cycleType=="month"){
                                var _index=res.template.endTime.indexOf(":");
                                var prefix=res.template.endTime.slice(0,_index);
                                var suffix=res.template.endTime.slice(_index+1,res.template.endTime.length);
                                self.weekEnd=prefix;
                                self.monthEnd=prefix;
                                self.endTime=suffix;
                            }
                        }
                        if (res.template.startTime) {
                            if(res.template.cycleType=="week"||res.template.cycleType=="month"){
                                var _index=res.template.startTime.indexOf(":");
                                var prefix=res.template.startTime.slice(0,_index);
                                var suffix=res.template.startTime.slice(_index+1,res.template.startTime.length);
                                self.weekStart=prefix;
                                self.monthStart=prefix;
                                self.startTime=suffix;
                            }
                        }
                    }
                })
            },
            submit() {
                let self = this;
                if(self.template.cycleType == "week") {
                    self.template.startTime =  self.weekStart+ ":" +self.startTime;
                    self.template.endTime = self.weekEnd+ ":" +self.endTime;
                }
                else if(self.template.cycleType=="month") {
                    self.template.startTime =  self.monthStart+ ":" +self.startTime;
                    self.template.endTime = self.monthEnd+ ":" +self.endTime;
                }else {
                    self.template.startTime =  self.startTime;
                    self.template.endTime = self.endTime;
                }
                self.template.rule = JSON.parse(self.trule);
                self.template.attachment = JSON.parse(self.tattachment);
                $.post("upserttasktemplate.vpage", {"template":JSON.stringify(self.template)}, function (res) {
                    if (res.success) {
                        alert("操作成功");
                        if (self.template.source!='') {
                            location.href = 'index.vpage#'+self.template.source;
                        }else {
                            location.href = 'index.vpage';
                        }
                    } else {
                        alert(res.info);
                    }
                })
            }
        },
        created() {
            if (id!='') {
                this.initData()
            }
            this.template.from ="${.now?string("yyyy-MM-dd")}" +' '+ "00:00:00";
            this.template.to = "2029-12-31 23:59:59";
        },
        mounted() {
            $('#dayStartTimePicker').datetimepicker({
                format: 'hh:ii:ss',
                startView: 1,
                pickDate: false
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'startTime',v);
            });
            $('#dayEndTimePicker').datetimepicker({
                format: 'hh:ii:ss',
                startView: 1,
                pickDate: false
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'endTime',v);
            });

            $('#weekStartTimePicker').datetimepicker({
                format: 'hh:ii:ss',
                startView: 1,
                pickDate: false
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'startTime',v);
            });

            $('#weekEndTimePicker').datetimepicker({
                format: 'hh:ii:ss',
                startView: 1,
                pickDate: false
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'endTime',v);
            });

            $('#monthStartTimePicker').datetimepicker({
                format: 'hh:ii:ss',
                startView: 1,
                pickDate: false
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'startTime',v);
            });

            $('#monthEndTimePicker').datetimepicker({
                format: 'hh:ii:ss',
                startView: 1,
                pickDate: false
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'endTime',v);
            });

            $('#fixedStartTimePicker').datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss'
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'startTime',v);
            });

            $('#fixedEndTimePicker').datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss'
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm,'endTime',v);
            });

            $('#formStartTimePicker').datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss'
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm.template,'from',v);
            });

            $('#toEndTimePicker').datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss'
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm.template,'to',v);
            });
        }
    });

</script>
</@layout_default.page>