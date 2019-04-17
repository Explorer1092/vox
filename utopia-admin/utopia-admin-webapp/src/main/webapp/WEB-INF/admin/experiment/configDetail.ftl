<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='教材详细配置' page_num=25>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        教学诊断实验&nbsp;&nbsp;
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" @click="saveData">
            <i class="icon-pencil icon-white"></i> 保 存
        </a>
    </legend>
    <div class="container-fluid">
        <label class="pull-left lead">{{experiment.id}} : {{experiment.name}}</label>
    </div>

    <div class="container-fluid">
        <div class="well well-large well-success">
            <div class="row-fluid">
                <div class="span12">
                    <label>投放配置</label>
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label">投放年级</label>
                            <div class="controls">
                                <input type="text" v-model="experiment.grades">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">投放地区</label>
                            <div class="controls">
                                <input type="text" v-model="experiment.regions">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">流量标记</label>
                            <div class="controls">
                                <input type="text" v-model="experiment.labels">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="container-fluid">
        <div class="well well-large well-success">
            <div class="row-fluid">
                <div class="span12">
                    <label>实验配置</label>
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label">进入实验提示：</label>
                            <div class="controls">
                                <input type="text" v-model="experiment.preDescription" class="pull-left">
                                <label class="pull-left">&nbsp;&nbsp;(仅普通实验可用)</label>
                            </div>

                        </div>
                        <div class="control-group">
                            <label class="control-label">实验奖励：</label>
                            <div class="controls">
                                <input type="text" v-model="experiment.bonus">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">前测题id：</label>
                            <div class="controls">
                                <input type="text" v-model="experiment.previewQuestion" v-if="experiment.status === 'ONLINE' || experiment.status === 'OFFLINE'" disabled="disabled" readonly>
                                <input type="text" v-model="experiment.previewQuestion" v-if="experiment.status != 'ONLINE' && experiment.status != 'OFFLINE'" >
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">后测题id：</label>
                            <div class="controls">
                                <input type="text" v-model="experiment.postQuestion" v-if="experiment.status === 'ONLINE' || experiment.status === 'OFFLINE'" disabled="disabled" readonly>
                                <input type="text" v-model="experiment.postQuestion" v-if="experiment.status != 'ONLINE' && experiment.status != 'OFFLINE'">
                            </div>
                        </div>

                        <div class="well" v-for="(item,index) in experiment.configList">
                            <p>错因{{ index + 1 }}</p>
                            <div class="control-group">
                                <label class="control-label">错误答案</label>
                                <div class="controls">
                                    <input type="text" v-model="experiment.configList[index].answers" placeholder="(多个答案用,隔开)"  class="pull-left">
                                    <p  class="pull-left" style="font-size: 14px">&nbsp;&nbsp;(多个答案用","隔开)</p>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="inputPassword">课程id</label>
                                <div class="controls">
                                    <input type="text"  v-model="experiment.configList[index].courseIds" placeholder="(多个课程用,隔开)"  class="pull-left">
                                    <p class="pull-left" style="font-size: 14px">&nbsp;&nbsp;(多个课程用","隔开)</p>
                                </div>
                            </div>
                            <button @click="addError" class="btn btn-primary">添加</button>
                            <button @click="delError(index)" class="btn btn-danger">删除</button>
                        </div>
                        <div class="control-group">
                                <ur style="color: red;list-style-type:none">
                                    <li>
                                        1.题型为选择时，答案请填写0,1,2,3 对应的选项为A,B,C,D；判断题类似；
                                    </li>
                                    <li>
                                        2.答案为@，代表错误且未命中其他设置的答案。
                                    </li>
                                </ur>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>


    <div class="container-fluid">
        <div class="well well-large well-success">
            <div class="row-fluid">
                <div class="span12">
                    <label>历史日志：</label>
                    <p v-for="r in logList">
                        {{r.createTime}} -- {{r.operation}}
                    </p>
                </div>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript">
    (function () {
        var vm = new Vue({
            el: '#main_container',
            data: {
                errorItem:{
                    answers:'',
                    courseIds:''
                },
                experiment: {
                    id: '',
                    name: '',
                    grades: '',
                    status: '',
                    statusName: '',
                    regions: '',
                    labels: '',
                    preDescription: '',
                    bonus: 0,
                    previewQuestion: '',
                    postQuestion: null,
                    configList: [{
                        answers:'',
                        courseIds:''
                    }]
                },
                logList:[
                    {
                        operation:'',
                        createTime:''
                    }
                ]
            },
            methods: {
                extend:function(obj){
                    var _this = this;
                    var target = obj;
                    var tmpObj = {};
                    for(key in target){
                        if(typeof target[key] === 'object'){
                            _this.extend(target[key])
                        }else{
                            tmpObj[key] =  target[key]
                        }
                    }
                    return tmpObj;
                },
                addError:function(){
                    var _this = this;
                    _this.experiment.configList.push(_this.extend(_this.errorItem));
                },
                delError:function(index){
                    var _this = this;
                    if( _this.experiment.configList.length <=1){
                        alert("至少保留一个")
                    }else{
                        _this.experiment.configList.splice(index,1)
                    }

                },
                saveData:function () {
                    var _this = this;
                    console.info(JSON.stringify(_this.experiment));
                    $.post("${requestContext.webAppContextPath}/crm/experiment/config/update.vpage", {data:JSON.stringify(_this.experiment)}, function (result) {
                        if (result.success) {
                            alert("保存成功");
                        } else {
                            alert(result.info);
                        }
                    });
                }
            },
            created: function () {
                var _this = this;
                console.info("created");
                var id = '${id!}';
                console.info("id");
                $.get("${requestContext.webAppContextPath}/crm/experiment/config/detail/data.vpage", {id:id}, function (result) {
                    if (result.success) {
                        if (result.experiment != null) {
                            _this.experiment = result.experiment;
                            if (_this.experiment.configList == null || _this.experiment.configList.length == 0) {
                                _this.experiment.configList = [_this.extend(_this.errorItem)];
                            }
                        }
                        if (result.logList != null) {
                            _this.logList = result.logList;
                        }
                        console.log(result);
                    } else {
                        alert(result.info);
                    }
                });
            }
        });
    }());
</script>
</@layout_default.page>