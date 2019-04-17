<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='增值-弹窗管理' page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<span class="span9" id="popup_index">
    <h1>弹窗管理</h1>
    <button style="float: right;margin:-30px 0 20px 0;" class="btn btn-info" @click="add">增加</button>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th style="width:10%">ID</th>
            <th style="width:10%">活动ID</th>
            <th style="width:8%">描述</th>
            <th style="width:12%">url</th>
            <th style="width:12%">内容</th>
            <th style="width:5%">类型</th>
            <th style="width:10%">周期</th>
            <th style="width:5%">排序</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th style="width:10%">操作</th>
        </tr>
        </thead>
        <tbody>
            <tr v-for="item in popups">
                <td>{{item.id}}</td>
                <td>{{item.range}}</td>
                <td>{{item.description}}</td>
                <td>{{item.url}}</td>
                <td>{{item.content}}</td>
                <td>
                    <select class="input-small" style="width: 92px"  v-model="item.type" disabled>
                        <option v-for="item in types" :value="item.key">{{item.value}}</option>
                    </select>
                </td>
                <td>
                    <select class="input-small" style="width: 120px" v-model="item.cycle" disabled>
                        <option v-for="item in cycles" :value="item.key">{{item.value}}</option>
                    </select>
                </td>
                <td>{{item.rank}}</td>
                <td>{{item.startDatetime}}</td>
                <td>{{item.endDatetime}}</td>
                <td>
                    <button class="btn btn-info" @click="edit(item.id)">编辑</button>
                    <div class="btn btn-danger" @click="dele(item.id)">删除</div>
                </td>
            </tr>
        </tbody>
    </table>

    <div class="modal fade" id="addChannelModal" tabindex="-1" role="dialog" aria-hidden="true" style="display: none">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel"><span data-bind="text:modalHead()"></span>弹窗</h4>
            </div>
            <div class="modal-body">

                <form class="form-horizontal">
                    <fieldset>

                        <div v-if="popup.id!=null" class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">ID</label>
                            <div class="controls">
                                <input class="input-large" type="text" v-model="popup.id" disabled>
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">活动ID</label>
                            <div class="controls">
                                <select class="input-xlarge" v-model="popup.range">
                                    <option v-for="item in ranges" :value="item.key">{{item.value}}/{{item.key}}</option>
                                </select>
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">描述</label>
                            <div class="controls">
                                <input type="text" placeholder="请输入描述" class="input-xlarge" v-model="popup.description">
                                <p class="help-block"></p>
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">URL</label>
                            <div class="controls">
                                <input type="text" placeholder="请输入URL" class="input-xlarge" v-model="popup.url">
                                <p class="help-block"></p>
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">内容</label>
                            <div class="controls">
                                <input type="text" placeholder="请输入内容" class="input-xlarge" v-model="popup.content">
                                <p class="help-block"></p>
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">类型</label>
                            <div class="controls">
                                <select class="input-small" style="width: 92px" v-model="popup.type">
                                    <option v-for="item in types" :value="item.key">{{item.value}}</option>
                                </select>
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">周期</label>
                            <div class="controls">
                                <select class="input-small" style="width: 120px" v-model="popup.cycle">
                                    <option v-for="item in cycles" :value="item.key">{{item.value}}</option>
                                </select>
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">排序rank</label>
                            <div class="controls">
                                <input type="number" class="input-large" v-model="popup.rank">
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">开始时间</label>
                            <div class="controls">
                                <input id="startDatetime" @change="change01('startDatetime')" autocomplete="off" type="text" class="input-large" v-model="popup.startDatetime">
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">结束时间</label>
                            <div class="controls">
                                <input id="endDatetime" @change="change02('endDatetime')" type="text" autocomplete="off" class="input-large" v-model="popup.endDatetime">
                            </div>
                        </div>

                        <div class="control-group">
                            <!-- Text input-->
                            <label class="control-label" for="input01">拓展extension</label>
                            <div class="controls">
                                <input type="text" class="input-large" autocomplete="off" v-model="popup.extension">
                            </div>
                        </div>

                    </fieldset>
                </form>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-info" data-dismiss="modal" @click="submit">
                    提交
                </button>
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</div>
</span>

<script>
    const vm = new Vue({
        el: '#popup_index',
        data: {
            ranges:JSON.parse('${json_encode(ranges)!"[]"}'),
            types:JSON.parse('${json_encode(types)!"[]"}'),
            cycles:JSON.parse('${json_encode(cycles)!"[]"}'),
            popups:[],
            popup:{
                id:'',
                range:'',
                description:'',
                url:'',
                content:'',
                type:0,
                cycle:0,
                rank:'',
                startDatetime:'',
                endDatetime:'',
                extension:''
            }
        },
        methods: {
            initData () {
                const self = this;
                $.post('load.vpage',{},function(res){
                    if(res.success) {
                        res.popups.forEach(function (value,index) {
                            res.popups[index].startDatetime = self.momentDatetime(res.popups[index].startDatetime);
                            res.popups[index].endDatetime = self.momentDatetime(res.popups[index].endDatetime);
                        })
                        self.popups = res.popups;
                    }
                })
            },
            momentDatetime(date) {
                return moment(new Date(date)).format("YYYY-MM-DD HH:mm:ss");
            },
            change01(id) {
                const self = this;
                if ($('#'+id).val()!='') {
                    self.popup.startDatetime = $('#'+id).val();
                }
            },
            change02(id) {
                const self = this;
                if ($('#'+id).val()!='') {
                    self.popup.endDatetime = $('#'+id).val();
                }
            },
            add () {
                const self = this;
                self.popup = {};
                $("#addChannelModal").modal("show");
            },
            submit(){
                const self = this;
                self.popups.startDatetime = self.momentDatetime(self.popups.startDatetime);
                self.popups.endDatetime = self.momentDatetime(self.popups.endDatetime);
                $.post('upsert.vpage',{"popup":JSON.stringify(self.popup)},function(res) {
                    if (res.success) {
                        alert("操作成功");
                        self.initData();
                    } else {
                        alert(res.info);
                    }
                })
            },
            edit(id) {
                const self = this;
                $.post('loadbyid.vpage',{id:id},function(res){
                    if(res.success) {
                       res.popup.startDatetime = self.momentDatetime(res.popup.startDatetime);
                       res.popup.endDatetime = self.momentDatetime(res.popup.endDatetime);
                       self.popup = res.popup;
                        $("#addChannelModal").modal("show");
                    }
                })
            },
            dele(id) {
                const self = this;
                $.post('delete.vpage',{id:id},function(res){
                    if(res.success) {
                        alert("操作成功");
                        self.initData();
                    }else {
                        alert(res.info);
                    }
                })
            }
        },
        created() {
            this.initData();
        },
        mounted() {
            $('#startDatetime').datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss'
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm.popup,'startDatetime',v);
            });
            $('#endDatetime').datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss'
            }).on('changeDate',function(val){
                let v = $(this).val();
                vm.$set(vm.popup,'endDatetime',v);
            });
        }
    });
</script>


</@layout_default.page>