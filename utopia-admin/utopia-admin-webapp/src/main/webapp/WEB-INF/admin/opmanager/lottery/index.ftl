<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    [v-cloak] { display: none }
    .index-edit-ul {
        text-align: center;
    }
    .index-edit-ul > li{
        display: block !important;
        margin-left: -60px;
    }
    .index-edit-ul li>span{
        display: inline-block;
        width: 140px;
        line-height:30px;
        text-align: right;
        vertical-align: top;
    }
</style>
<div id="award-index" class="span9" v-cloak>
    <legend>
        <strong>抽奖活动管理</strong>
    </legend>
    <ul class="inline">
        <li>
            <button class="btn btn-primary" @click="addAcitvity">新增抽奖活动</button>
        </li>
    </ul>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                        <tr>
                            <th>活动名称</th>
                            <th>开始时间</th>
                            <th>结束时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody style="display: none" v-show="activityList.length">
                        <tr v-for="activity in activityList">
                            <td>{{activity.activityName}}</td>
                            <td>{{activity.activityStartTime}}</td>
                            <td>{{activity.activityEndTime}}</td>
                            <td>
                                <button class="btn btn-primary" @click="editActivity(activity)">编辑</button>
                                <button class="btn btn-danger" @click="deleteActivity(activity)">删除</button>
                                <a class="btn btn-info" :href="'/opmanager/lottery/compaign/config.vpage?activityId=' + activity.activityId">奖品配置</a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <#--新增、编辑活动弹窗-->
    <div id="edit-activity-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">{{isAddActivtiyDialog ? '新增抽奖活动' : '编辑抽奖活动'}}</h4>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <ul class="inline index-edit-ul">
                <li>
                    <span>活动名称：</span>
                    <input
                        type="text"
                        placeholder="请输入活动名称"
                        :value="isAddActivtiyDialog ? '' : operateActivityInfo.activityName"
                        v-model="operateActivityInfo.activityName">
                </li>
                <li>
                    <span>开始时间：</span>
                    <input
                        type="text"
                        class="datetimepicker1"
                        placeholder="请选择开始时间"
                        :value="isAddActivtiyDialog ? '' : operateActivityInfo.activityStartTime"
                        v-model="operateActivityInfo.activityStartTime">
                </li>
                <li>
                    <span>结束时间：</span>
                    <input
                        type="text"
                        class="datetimepicker2"
                        placeholder="请选择开始时间"
                        :value="isAddActivtiyDialog ? '' : operateActivityInfo.activityEndTime"
                        v-model="operateActivityInfo.activityEndTime">
                </li>
                <li>
                    <span>大奖规则：</span>
                    <select name="" id="" v-model="operateActivityInfo.ruleType">
                        <option :value="bigAwardRule.ruleType" v-for="bigAwardRule in bigAwardRuleList">{{bigAwardRule.ruleText}}</option>
                    </select>
                </li>
            </ul>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
            <button id="reject_btn" type="button" class="btn btn-primary" @click="sureSaveAward">保 存</button>
        </div>
    </div>

    <#--删除活动弹窗-->
    <div id="delete-activity-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">删除抽奖活动</h4>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <p>您确定要删除该活动吗？</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
            <button id="reject_btn" type="button" class="btn btn-primary" @click="sureDeleteAward">确 定</button>
        </div>
    </div>

    <script>
        <#--使用vue渲染列表数据-->
        var vm = new Vue({
            el: '#award-index',
            data: {
                activityList: [
                    {
                        activityId: 101,
                        activityName: '2018春季开学布置作业抽奖',
                        activityStartTime: '2018-02-26',
                        activityEndTime: '2019-03-25',
                        ruleType: 0
                    },
                    {
                        activityId: 102,
                        activityName: '2018春季邀请老师抽奖',
                        activityStartTime: '2018-02-29',
                        activityEndTime: '2019-04-15',
                        ruleType: 1
                    },
                    {
                        activityId: 103,
                        activityName: '2017冬季布置作业抽奖',
                        activityStartTime: '2017-12-26',
                        activityEndTime: '2018-01-25',
                        ruleType: 2
                    }
                ],
                isAddActivtiyDialog: true,
                operateActivityInfo: {}, // 操作的活动信息，get
                bigAwardRuleList: [
                    {
                        ruleText: '可重复中大奖',
                        ruleType: 0
                    },
                    {
                        ruleText: '不允许同一个用户中多个大奖',
                        ruleType: 1
                    },
                    {
                        ruleText: '不允许同校用户中多个大奖',
                        ruleType: 2
                    },
                    {
                        ruleText: '不允许同一个地区用户中多个大奖',
                        ruleType: 3
                    }
                ]
            },
            mounted: function () {
                $('.datetimepicker1, .datetimepicker2').datetimepicker(
                    {
                        format: 'yyyy-mm-dd',
                        autoclose: true,
                        todayBtn: true,
                        minView: 2
                    }
                ).on('changeDate', function(ev){
                    // 使用vm.$set同步
                    if ($(this).hasClass('datetimepicker1')) {
                        vm.$set(vm.operateActivityInfo, 'activityStartTime', $(this).val());
                    } else if ($(this).hasClass('datetimepicker2')) {
                        vm.$set(vm.operateActivityInfo, 'activityEndTime', $(this).val());
                    }
                });
            },
            methods: {
                editActivity: function (activity) {
                    vm.isAddActivtiyDialog = false;
                    $('#edit-activity-dialog').modal('show');
                    vm.operateActivityInfo = activity;
                },
                deleteActivity: function (activity) {
                    $('#delete-activity-dialog').modal('show');
                    vm.operateActivityInfo = activity;
                },
                sureSaveAward: function () {
                    $('#edit-activity-dialog').modal('hide');
                    console.log('this.operateActivityInfo', this.operateActivityInfo)
                },
                sureDeleteAward: function () {
                    $('#delete-activity-dialog').modal('hide');
                },
                addAcitvity: function () {
                    vm.isAddActivtiyDialog = true;
                    $('#edit-activity-dialog').modal('show');
                    vm.operateActivityInfo = {};
                }
            }
        });


    </script>
</div>
</@layout_default.page>