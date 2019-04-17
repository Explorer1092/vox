<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='教学诊断实验平台' page_num=25>

<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>
<div id="main_container" class="span9">
    <div class="row-fluid">
        <div class="span12">
            <h3>课程实验平台&nbsp;&nbsp;&nbsp;&nbsp;<span class="btn btn-primary" @click="showGroupModal">添加实验组</span></h3>
            <br>
        </div>
    </div>

    <ul class="nav nav-tabs" id="myTab">
        <li @click="changeTab('COMMON')" :class="{active:tabIndex === 'COMMON'}" style="cursor: pointer"><a>普通实验</a></li>
        <li @click="changeTab('INTERNAL')" :class="{active:tabIndex === 'INTERNAL'}" style="cursor: pointer"><a>作业内部实验</a></li>
        <input type="hidden" id="type">
    </ul>

    <div class="tab-content"
         style="padding: 20px;border: 1px solid #ddd;border-top: none;-webkit-border-radius: 5px;-moz-border-radius: 5px;border-radius: 5px;">
    <#-- 普通实验 -->
        <div class="tab-pane active" id="home">
            <div class="row-fluid" v-for="(r,index) in result">
                <div class="span12">
                    <div style="height: 50px;padding: 10px 0;">
                        <label class="pull-left lead" >实验组 {{index+1}}({{r.id}} : {{r.name}})</label>
                        <button class="btn btn-primary pull-right" @click="showExpModal(r.id)">添加实验</button>
                        <button v-if="r.reported != null && r.reported" class="btn btn-primary pull-right" @click="showExpGoupReport(r.id)">查看报告</button>
                    </div>
                    <table class="table table-bordered">
                        <tr>
                            <td style="width: 18%">编号</td>
                            <td>实验名称</td>
                            <td style="width: 18%">投放地区</td>
                            <td style="width: 8%">投放年级</td>
                            <td style="width: 11.5%">流量标记</td>
                            <td style="width: 5.5%">状态</td>
                            <td style="width: 17%">操作</td>
                        </tr>
                        <tr v-for="i in r.experimentList">
                            <td>{{i.id}}</td>
                            <td>{{i.name}}</td>
                            <td>{{i.regions}}</td>
                            <td>{{i.grades}}</td>
                            <td>{{i.labels}}</td>
                            <td>{{i.statusName}}</td>
                            <td>
                                <button class="btn btn-primary" style="width: 60px" @click="detailExp(i.id)">编辑</button>
                                <button v-if="i.status === 'WAITING' || i.status === 'OFFLINE'" class="btn btn-success" style="width: 60px"  @click="onlineExp(i.id)">上线</button>
                                <button v-if="i.status === 'ONLINE'" class="btn btn-warning" style="width: 60px"  @click="offlineExp(i.id)">下线</button>
                                <button v-if="i.status === 'WAITING'" class="btn btn-danger" style="width: 60px"  @click="deleteExp(i.id)">删除</button>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="modal" id="groupModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" v-if="groupModalShow">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"  @click="closeGroupModal"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel" style="text-align:center;">添加实验组</h4>
                </div>
                <div class="modal-body" style="max-height: 550px !important;">
                    实验组名称：<input name="groupName" id = "groupName" v-model="groupName">
                </div>
                <div class="modal-footer">
                    <button type="button" @click="closeGroupModal">取消</button>
                    <button type="button" @click="saveGroup">保存</button>
                </div>
            </div>
        </div>
    </div>


    <div class="modal" id="extModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" v-if="expModalShow">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"  @click="closeExpModal"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel" style="text-align:center;">添加实验</h4>
                </div>
                <div class="modal-body" style="max-height: 550px !important;">
                    实验名称：<input name="experimentName" id = "experimentName" v-model="experimentName">
                </div>
                <div class="modal-footer">
                    <button type="button" @click="closeExpModal">取消</button>
                    <button type="button" @click="saveExp">保存</button>
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
                groupName:'',
                experimentName:'',
                groupModalShow: false,
                expModalShow:false,
                groupId:'',
                tabIndex: 'COMMON',
                result: [
                    {
                        id: '',
                        name: '',
                        reported: false,
                        experimentList: [
                            {
                                id: '',
                                name: '',
                                regions: '',
                                labels: '',
                                grades: '',
                                status: '',
                                statusName: ''
                            }
                        ]
                    }
                ]
            },
            methods: {
                changeTab: function (tabIndex) {
                    var _this = this;
                    _this.tabIndex = tabIndex;
                    //普通实验
                    $.get("${requestContext.webAppContextPath}/crm/experiment/config/index/data.vpage", {type:tabIndex}, function (result) {
                        if (result.success) {
//                            console.log(result.data)
                            _this.result = result.data;
                        } else {
                            alert(result.info);
                        }
                    });
                },
                showGroupModal:function(id){
                    var _this = this;
                    _this.groupModalShow = true;
                },
                closeGroupModal:function(){
                    var _this = this;
                    _this.groupModalShow = false;
                },
                saveGroup:function () {
                    var _this = this;
                    $.post("${requestContext.webAppContextPath}/crm/experiment/config/group/create.vpage", {name:_this.groupName,type:_this.tabIndex}, function (result) {
                        if (result.success) {
                            alert("保存成功");
                            _this.groupModalShow = false;
                            _this.groupName = '';
                            $.get("${requestContext.webAppContextPath}/crm/experiment/config/index/data.vpage", {type:_this.tabIndex}, function (result) {
                                if (result.success) {
                                    _this.result = result.data
                                } else {
                                    alert(result.info);
                                }
                            });
                        } else {
                            alert(result.info);
                        }
                    });
                },
                showExpGoupReport:function (id) {
                    console.log(id);
                    window.open("${requestContext.webAppContextPath}/crm/experiment/diagnosis/courseAnalysis/list.vpage?group=" + id);
                },
                showExpModal:function(id){
                    var _this = this;
                    _this.groupId = id;
                    _this.expModalShow = true;
                },
                closeExpModal:function(){
                    var _this = this;
                    _this.groupId = '';
                    _this.expModalShow = false;
                },
                saveExp:function () {
                    var _this = this;
                    console.log(_this.experimentName);
                    console.log(_this.groupId);
                    $.post("${requestContext.webAppContextPath}/crm/experiment/config/create.vpage", {name:_this.experimentName, groupId:_this.groupId}, function (result) {
                        if (result.success) {
                            alert("保存成功");
                            _this.groupId = '';
                            _this.experimentName = '';
                            _this.expModalShow = false;
                            $.get("${requestContext.webAppContextPath}/crm/experiment/config/index/data.vpage", {type:_this.tabIndex}, function (result) {
                                if (result.success) {
                                    _this.result = result.data
                                } else {
                                    alert(result.info);
                                }
                            });
                        } else {
                            alert(result.info);
                        }
                    });
                },
                deleteExp:function (id) {
                    var _this = this;
                    $.post("${requestContext.webAppContextPath}/crm/experiment/config/delete.vpage", {id:id}, function (result) {
                        if (result.success) {
                            alert("操作成功");
                            $.get("${requestContext.webAppContextPath}/crm/experiment/config/index/data.vpage", {type:_this.tabIndex}, function (result) {
                                if (result.success) {
                                    _this.result = result.data
                                } else {
                                    alert(result.info);
                                }
                            });
                        } else {
                            alert(result.info);
                        }
                    });
                },
                detailExp:function (id) {
                    var _this = this;
                    console.log(id);
                    window.open("${requestContext.webAppContextPath}/crm/experiment/config/detail.vpage?id=" + id);
                },
                onlineExp:function (id) {
                    var _this = this;
                    $.post("${requestContext.webAppContextPath}/crm/experiment/config/status/change.vpage", {id:id, status:'ONLINE'}, function (result) {
                        if (result.success) {
                            alert("操作成功");
                            $.get("${requestContext.webAppContextPath}/crm/experiment/config/index/data.vpage", {type:_this.tabIndex}, function (result) {
                                if (result.success) {
                                    _this.result = result.data
                                } else {
                                    alert(result.info);
                                }
                            });
                        } else {
                            alert(result.info);
                        }
                    });
                },
                offlineExp:function (id) {
                    var _this = this;
                    $.post("${requestContext.webAppContextPath}/crm/experiment/config/status/change.vpage", {id:id, status:'OFFLINE'}, function (result) {
                        if (result.success) {
                            alert("操作成功");
                            $.get("${requestContext.webAppContextPath}/crm/experiment/config/index/data.vpage", {type:_this.tabIndex}, function (result) {
                                if (result.success) {
                                    _this.result = result.data
                                } else {
                                    alert(result.info);
                                }
                            });
                        } else {
                            alert(result.info);
                        }
                    });
                }
            },
            created: function () {
                var _this = this;
                console.info("created");
                $.get("${requestContext.webAppContextPath}/crm/experiment/config/index/data.vpage", {}, function (result) {
                    if (result.success) {
                        _this.result = result.data
                    } else {
                        alert(result.info);
                    }
                });
            }
        });
    }());
</script>

<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>