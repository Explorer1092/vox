<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    [v-cloak] { display: none }
    .config-edit-ul {
        text-align: center;
    }
    .config-edit-ul > li{
        display: block !important;
        margin-left: -60px;
    }
    .config-edit-ul li>span{
        display: inline-block;
        width: 140px;
        line-height:30px;
        text-align: right;
        vertical-align: top;
    }
    .check-box{
        display: inline-block;
        width: 220px;
        text-align: left;
        vertical-align: bottom;
    }
    .check-box label{
        font-size: 16px;
        display: inline-block;
        margin-right: 20px;
    }
</style>
<div id="award-config" class="span9" v-cloak>
    <legend>
        <strong>奖品配置</strong>
    </legend>
    <ul class="inline">
        <li>
            <a class="btn btn-primary" href="/opmanager/lottery/compaign/index.vpage">返回抽奖活动列表</a>
            <button class="btn btn-primary" @click="addAward">新增奖品配置</button>
            <span style="color: #f00;">友情提示：各项中奖率之和要等于1000000，否则后果自负</span>
        </li>
    </ul>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                        <tr>
                            <th>奖品名称</th>
                            <th>奖品级别</th>
                            <th>中奖概率</th>
                            <th>是否大奖</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody style="display: none" v-show="awardList.length">
                        <tr v-for="award in awardList">
                            <td>{{award.awardName}}</td>
                            <td>{{award.awardLevel}}</td>
                            <td>{{award.lotteryProbility}}</td>
                            <td>{{award.isBigAward ? '是' : '否'}}</td>
                            <td>
                                <button class="btn btn-primary" @click="editAward(award)">编辑</button>
                                <button class="btn btn-danger" @click="deleteAward(award)">删除</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <#--新增、编辑奖品弹窗-->
    <div id="edit-award-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">{{isAddAwardDialog ? '新增奖品配置' : '编辑奖品配置'}}</h4>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <ul class="inline config-edit-ul">
                <li>
                    <span>奖品名称：</span>
                    <input
                        type="text"
                        placeholder="请输入奖品名称"
                        :value="isAddAwardDialog ? '' : operateAwardInfo.awardName"
                        v-model="operateAwardInfo.awardName">
                </li>
                <li>
                    <span>奖品级别：</span>
                    <input
                        type="text"
                        placeholder="请输入奖品级别"
                        :value="isAddAwardDialog ? '' : operateAwardInfo.awardLevel"
                        v-model="operateAwardInfo.awardLevel">
                </li>
                <li>
                    <span>奖品内容：</span>
                    <input
                        type="text"
                        placeholder="请输入奖品内容">
                </li>
                <li>
                    <span>中奖概率：</span>
                    <input
                        type="text"
                        placeholder="请输入中奖概率"
                        :value="isAddAwardDialog ? '' : operateAwardInfo.lotteryProbility"
                        v-model="operateAwardInfo.lotteryProbility">
                </li>
                <li>
                    <span>有无大奖：</span>
                    <div class="check-box">
                        <label for="has-award">
                            <span>是</span>
                            <input
                                type="radio"
                                name="hasaward"
                                id="has-award"
                                :value="true"
                                v-model="operateAwardInfo.isBigAward">
                        </label>
                        <label for="hasnot-award">
                            <span>否</span>
                            <input
                                type="radio"
                                name="hasaward"
                                id="hasnot-award"
                                :value="false"
                                v-model="operateAwardInfo.isBigAward">
                        </label>
                    </div>
                </li>
            </ul>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
            <button id="reject_btn" type="button" class="btn btn-primary" @click="sureSaveAward">保 存</button>
        </div>
    </div>

    <#--删除奖品弹窗-->
    <div id="delete-award-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">删除奖品</h4>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <p>您确定要删除该奖品吗？</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
            <button id="reject_btn" type="button" class="btn btn-primary" @click="sureDeleteAward">确 定</button>
        </div>
    </div>
</div>
<script>
    <#--使用vue渲染列表数据-->
    var vm = new Vue({
        el: '#award-config',
        data: {
            awardList: [
                {
                    awardId: 10001,
                    awardName: 'iPhone X 1台',
                    awardLevel: 1,
                    lotteryProbility: 1,
                    isBigAward: true
                },
                {
                    awardId: 10002,
                    awardName: 'Kindle 1台',
                    awardLevel: 2,
                    lotteryProbility: 5,
                    isBigAward: false
                },
                {
                    awardId: 10003,
                    awardName: '蓝牙耳机1个',
                    awardLevel: 3,
                    lotteryProbility: 30,
                    isBigAward: true
                }
            ],
            operateAwardInfo: {},
            isAddAwardDialog: true
        },
        mounted: function () {
        },
        methods: {
            editAward: function (award) {
                vm.isAddAwardDialog = false;
                $('#edit-award-dialog').modal('show');
                vm.operateAwardInfo = award;
            },
            deleteAward: function (award) {
                $('#delete-award-dialog').modal('show');
                vm.operateAwardInfo = award;
            },
            sureSaveAward: function () {
                $('#edit-award-dialog').modal('hide');
            },
            sureDeleteAward: function () {
                $('#delete-award-dialog').modal('hide');
            },
            addAward: function () {
                vm.isAddAwardDialog = true;
                $('#edit-award-dialog').modal('show');
                vm.operateAwardInfo = {};
            }
        }
    });
</script>
</@layout_default.page>