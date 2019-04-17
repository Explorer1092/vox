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
                  <label class="control-label" for="input01">任务标签</label>
                  <div class="controls">
                    <input type="text" placeholder="请输入任务标签" class="input-xlarge" v-model="template.lable">
                    <p class="help-block"></p>
                  </div>
            </div>
            <div class="control-group">
                  <label class="control-label" for="input01">描述信息</label>
                  <div class="controls">
                    <input type="text" placeholder="请输入任务标签" class="input-xlarge" v-model="template.desc">
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
                  <label class="control-label">年级</label>
                  <div class="controls">
                      <select class="input-xlarge" v-model="template.grade">
                         <option v-for="item in grades" :value="item.key">{{item.value}}</option>
                      </select>
                  </div>
            </div>
            <div class="control-group">
                  <label class="control-label">学期</label>
                  <div class="controls">
                      <select class="input-xlarge" v-model="template.term">
                         <option v-for="item in terms" :value="item.key">{{item.value}}</option>
                      </select>
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
                  <label class="control-label">题型</label>
                  <div class="controls">
                      <select class="input-xlarge" v-model="template.act">
                         <option v-for="item in acts" :value="item.key">{{item.value}}</option>
                      </select>
                  </div>
            </div>
            <div class="control-group">
                  <label class="control-label">模板状态</label>
                  <div class="controls">
                      <select class="input-xlarge" v-model="template.status">
                         <option v-for="item in status" :value="item.key">{{item.value}}</option>
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
                  <label class="control-label" for="input01">任务奖励倍数</label>
                  <div class="controls">
                    <input type="number" placeholder="" class="input-xlarge" v-model="template.multiple">
                    <p class="help-block"></p>
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
            grades:JSON.parse('${json_encode(grades)}'),
            terms:JSON.parse('${json_encode(terms)}'),
            subjects:JSON.parse('${json_encode(subjects)}'),
            status:JSON.parse('${json_encode(status)}'),
            acts:JSON.parse('${json_encode(acts)}'),
            trule:'{}',
            tattachment:'{}',
            template:{
                title:'',
                subtitle:'',
                lable:'',
                desc:'',
                env:'',
                grade:'FIRST_GRADE',
                term:'LAST_TERM',
                subject:'ENGLISH',
                act:'',
                status:'',
                rank:1,
                multiple:1,
                icon:'',
                rule:'',
                attachment:''
            }
        },
        methods: {
            momentDatetime(date) {
                return moment(new Date(date)).format("YYYY-MM-DD HH:mm:ss");
            },
            initData () {
                let self = this;
                $.post('loadassignmenttemplatebytemplateid.vpage',{templateId:id},function(res){
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
                    }
                })
            },
            submit() {
                let self = this;
                self.template.rule = JSON.parse(self.trule);
                self.template.attachment = JSON.parse(self.tattachment);
                $.post("upsertassignmenttemplate.vpage", {"template":JSON.stringify(self.template)}, function (res) {
                    if (res.success) {
                        alert("操作成功");
                        if(self.template.grade!=''&&self.template.subject!=''&&self.template.term!='') {
                            location.href = 'index.vpage#1'+self.template.grade+'#2'+self.template.subject+'#3'+self.template.term;
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
        }
    });

</script>
</@layout_default.page>