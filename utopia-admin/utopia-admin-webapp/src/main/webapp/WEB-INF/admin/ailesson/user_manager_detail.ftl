<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条英语用户管理 - 详情' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<style>
    .middle {
        display: block;
        text-align: center;
        padding-bottom: 5px;
        margin-bottom: 10px;
        border-bottom: solid 2px #ccc;
    }
    .userLabel {
        font-weight: 600;
    }
    .tb-cell {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 100%;
    }
</style>
<div id="userContainer" class="span9">
    <div class="row well">
        <div class="span12">
            <span class="middle">用户详情</span>
        </div>
        <div v-if="!userInfo" class="span12"> - </div>
        <div v-else class="span12">
            <div class="row">
                <div class="span3"><span class="userLabel">用户id：</span>{{ userInfo.id }}</div>
                <div class="span3"><span class="userLabel">姓名：</span>{{ userInfo.name }}</div>
                <div class="span3"><span class="userLabel">微信：</span>{{ userInfo.wxCode }}</div>
                <div class="span3"><span class="userLabel">电话：</span>{{ userInfo.phone }}</div>
            </div>
            <div class="row">
                <div class="span3"><span class="userLabel">省份：</span>{{ userInfo.province }}</div>
                <div class="span3"><span class="userLabel">学习年限：</span>{{ userInfo.studyDuration }}</div>
                <div class="span3"><span class="userLabel">是否竞品：</span>{{ userInfo.buyCompetitor ? '是' : '否'}}</div>
                <div class="span3"><span class="userLabel">定级：</span>{{ userInfo.level }}</div>
            </div>
            <div class="row">
                <div class="span3"><span class="userLabel">家长通总消费：</span>{{ Math.round(userInfo.jztConsume * 1000) / 1000 }}</div>
                <div class="span3"><span class="userLabel">薯条总消费：</span>{{ Math.round(userInfo.chipsConsume * 1000) / 1000 }}</div>
                <div class="span3"><span class="userLabel">最后活跃时间：</span>{{ userInfo.lastActive }}</div>
                <div class="span3"><span class="userLabel">购课次数：</span>{{ userInfo.buyTimes }}</div>
            </div>
            <div class="row">
                <div class="span3"><span class="userLabel">推荐成功次数：</span>{{ userInfo.successfulRecommendTimes }}</div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="span12 well">
            <ul class="nav nav-tabs">
                <li class="active">
                    <a href="#scoreListContainer" data-toggle="tab" @click="tabClick('scoreListContainer')">用户成绩</a>
                </li>
                <li>
                    <a href="#operationListContainer" data-toggle="tab" @click="tabClick('operationListContainer')">运营信息</a>
                </li>
                <li>
                    <a href="#mailInfoContainer" data-toggle="tab" @click="tabClick('mailInfoContainer')">邮寄信息</a>
                </li>
            </ul>

            <div class="tab-content">
                <div class="tab-pane active" id="scoreListContainer">
                    <div style="padding: 5px">
                        <span>产品：</span>
                        <select id="currentProduct" @change="loadScoreByUserId" v-model="currentProduct">
                            <option v-for="(item, index) in products" :key="item.id" :value="item.id">{{ item.name }}</option>
                        </select>
                    </div>

                    <div v-if="userInfoWithScoreList.length <= 0">
                        <span>暂无数据</span>
                    </div>

                    <div v-else v-for="(scoreInfo, index) in userInfoWithScoreList">
                        <div style="widows: 100%;margin: 5px 0 10px 0;">
                            <div style="width: 40%; float: left;">课程名称：<span style="font-weight: 600;">{{scoreInfo.productItemName}}</span></div>
                            <div style="width: 25%; float: left;">完成率：<span style="font-weight: 600;">{{scoreInfo.finishedNum}}/{{scoreInfo.totalNum}} | 百分比={{Math.round(scoreInfo.finishRate * 100000) / 1000}}%</span></div>
                            <div style="clear: both"></div>
                        </div>
                        <table class="table table-striped table-bordered" style="max-width: 1080px;">
                            <thead>
                                <tr>
                                    <td><div class="tb-cell">课程序号</div></td>
                                    <td><div class="tb-cell">课程名称</div></td>
                                    <td><div class="tb-cell">日期</div></td>
                                    <td><div class="tb-cell">成绩</div></td>
                                    <td><div class="tb-cell">运营日志</div></td>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(score, scoreIndex) in scoreInfo.scoreLis">
                                    <td><div class="tb-cell">第 {{score.rank + 1}} 课</div></td>
                                    <td><div class="tb-cell">{{score.name}}</div></td>
                                    <td><div class="tb-cell">{{ moment(new Date(score.openDate)).format("YYYY-MM-DD HH:mm:ss") }}</div></td>
                                    <td><div class="tb-cell">{{ score.score == -1 ? "未完成" : score.score }}</div></td>
                                    <td><div class="tb-cell">
                                        <input :id="score.unitId + '-input'" type="text" :value="score.operationLog" disabled="disabled" style="max-width: 250px">
                                        <button :id="score.unitId + '-edit'" class="btn" type="button" @click="editOperationLog(score.unitId)">编辑</button>
                                        <button :id="score.unitId + '-save'"  style="display: none;" class="btn" type="button" @click="saveOperationLog(score.unitId, index, scoreIndex)">保存</button>
                                        <button :id="score.unitId + '-cancel'" style="display: none;" class="btn" type="button" @click="cancelOperationLog(score.unitId, score.operationLog)">取消</button>
                                    </div></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="tab-pane" id="operationListContainer">
                    <table class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <td v-for="(column, index) in operationColumns">{{column}}</td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-if="operationList.length <= 0">
                            <td colspan="5" align="center">暂无数据</td>
                        </tr>
                        <tr v-else v-for="(user, index) in operationList">
                            <td>{{ index + 1 }}</td>
                            <td>{{user.name}}</td>
                            <td>{{user.id}}</td>
                            <td>{{user.className}}</td>
                            <td>{{user.productName}}</td>
                            <td>{{ moment(new Date(user.registerDate)).format("YYYY-MM-DD HH:mm:ss") }}</td>
                            <td>{{user.inGroup ? '是' : '否'}}</td>
                            <td>{{user.orderRef}}</td>
                            <td>{{user.questionnaires ? '是' : '否'}}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <div class="tab-pane" id="mailInfoContainer">
                    <div class="row">
                        <div class="offset1 span1">邮寄地址:</div>
                        <div v-if="editInfo.addr.showInput" class="span3"><input type="text" v-model="editInfo.addr.value"></div>
                        <div v-else class="span3">{{mailInfo.recipientAddr || '暂缺'}}</div>
                        <div v-if="editInfo.addr.showInput" class="span2">
                            <button class="btn" type="button" @click="saveMailInfo('addr')">保存</button>
                            <button class="btn" type="button" @click="cancelMailInfo('addr')">取消</button>
                        </div>
                        <div v-else class="span1"><button class="btn" type="button" @click="modifyMailInfo('addr')">编辑</button></div>
                    </div>
                    <div class="row" style="margin-bottom: 10px; margin-top: 10px">
                        <div class="offset1 span1">收件人:</div>
                        <div v-if="editInfo.name.showInput" class="span3"><input type="text" v-model="editInfo.name.value"></div>
                        <div v-else class="span3">{{mailInfo.recipientName || '暂缺'}}</div>
                        <div v-if="editInfo.name.showInput" class="span2">
                            <button class="btn" type="button" @click="saveMailInfo('name')">保存</button>
                            <button class="btn" type="button" @click="cancelMailInfo('name')">取消</button>
                        </div>
                        <div v-else class="span1"><button class="btn" type="button" @click="modifyMailInfo('name')">编辑</button></div>
                    </div>
                    <div class="row">
                        <div class="offset1 span1">电话:</div>
                        <div v-if="editInfo.tel.showInput" class="span3"><input type="text" v-model="editInfo.tel.value"></div>
                        <div v-else class="span3">{{mailInfo.recipientTel || '暂缺'}}</div>
                        <div v-if="editInfo.tel.showInput" class="span2">
                            <button class="btn" type="button" @click="saveMailInfo('tel')">保存</button>
                            <button class="btn" type="button" @click="cancelMailInfo('tel')">取消</button>
                        </div>
                        <div v-else class="span1"><button class="btn" type="button" @click="modifyMailInfo('tel')">编辑</button></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    var vm = new Vue({
        el: '#userContainer',
        data: {
            userInfo: null,

            userInfoWithScoreList: [],
            operationList:[],
            mailInfo: {},

            activeTab: 'scoreListContainer',
            operationColumns: ['序号', '姓名', '用户id', '班名', '产品', '报名日期', '是否进群', '订单来源', '是否问卷'],

            // 用户购买的全部产品
            products: [],
            // 当前选中的产品，默认选中第一个
            currentProduct: '',

            editInfo: {
                addr: {
                    showInput: false,
                    value: ''
                },
                name: {
                    showInput: false,
                    value: ''
                },
                tel: {
                    showInput: false,
                    value: ''
                }
            }
        },
        methods: {
            tabClick: function (tabName) {
                if (this.activeTab === tabName) {
                    return;
                }

                this.activeTab = tabName;

                if (this.activeTab === 'scoreListContainer') {
                    // 加载成绩列表
                    this.loadScoreByUserId();
                } else if (this.activeTab === 'operationListContainer') {
                    // 加载运营列表
                    this.loadOperationByUserId();
                } else {
                    this.loadMailInfoByUserId();
                }

            },
            // 加载用户的成绩列表
            loadScoreByUserId: function () {
                if (!this.userInfo || !this.userInfo.id) {
                    return;
                }
                var self = this;
                $.get("/chips/user/ai/userScoreList.vpage", {
                    userId: self.userInfo.id,
                    productId: self.currentProduct,
                }, function (res) {
                    var userInfoWithScoreList = res['scoreList'];
                    self.userInfoWithScoreList = userInfoWithScoreList || [];
                });

            },
            editOperationLog: function (unitId) {
                $('#' + unitId + '-input').removeAttr("disabled");
                $('#' + unitId + '-save').css("display", "inline");
                $('#' + unitId + '-cancel').css("display", "inline");
                $('#' + unitId + '-edit').css("display", "none");
            },
            saveOperationLog: function (unitId, index, scoreIndex) {
                var operationLog = $('#' + unitId + '-input').val();

                var self = this;

                $.post("/chips/user/ai/unitResultOperationLogEdit.vpage", {
                    userId: '${userId}',
                    unitId: unitId,
                    operationLog: operationLog,
                }, function (res) {
                    if (res.success === true) {
                        self.userInfoWithScoreList[index].scoreLis[scoreIndex].operationLog = operationLog;
                        $('#' + unitId + '-input').attr("disabled", "disabled");
                        $('#' + unitId + '-save').css("display", "none");
                        $('#' + unitId + '-cancel').css("display", "none");
                        $('#' + unitId + '-edit').css("display", "inline");

                        window.alert("保存成功");
                    } else {
                        window.alert("保存失败");
                    }

                });
            },
            cancelOperationLog: function (unitId, oldOperationLog) {
                $('#' + unitId + '-input').val(oldOperationLog);
                $('#' + unitId + '-input').attr("disabled", "disabled");
                $('#' + unitId + '-save').css("display", "none");
                $('#' + unitId + '-cancel').css("display", "none");
                $('#' + unitId + '-edit').css("display", "inline");
            },
            // 加载用户的运营信息
            loadOperationByUserId: function () {
                if (!this.userInfo || !this.userInfo.id) {
                    return;
                }
                var self = this;
                $.get("/chips/user/ai/userOperationInfo.vpage", {
                    userId: self.userInfo.id,
                }, function (res) {
                    var operationList = res['operationList'];
                    self.operationList = operationList || [];
                });
            },
            // 加载用户邮寄信息
            loadMailInfoByUserId: function () {
                if (!this.userInfo || !this.userInfo.id) {
                    return;
                }
                var self = this;
                $.get("/chips/user/ai/userMailInfo.vpage", {
                    userId: self.userInfo.id,
                }, function (res) {
                    var mailInfo = res['mailInfo'];
                    self.mailInfo = mailInfo || {};
                });
            },
            // 编辑寄件信息
            modifyMailInfo: function (editType) {
                var value = '';
                if (editType === 'addr') {
                    value = this.mailInfo.recipientAddr;
                }  else if (editType === 'name') {
                    value = this.mailInfo.recipientName;
                } else {
                    value = this.mailInfo.recipientTel;
                }
                this.$set(this.editInfo, editType, {
                    showInput: true,
                    value: value
                });
            },
            saveMailInfo: function(editType) {
                var self = this;
                $.post("/chips/user/ai/userMailInfoSave.vpage", {
                    userId: '${userId}',
                    editType: editType,
                    value: self.editInfo[editType].value
                }, function (res) {
                    if (res.success === true) {
                        if (editType === 'addr') {
                            self.mailInfo.recipientAddr = self.editInfo[editType].value;
                        }  else if (editType === 'name') {
                            self.mailInfo.recipientName = self.editInfo[editType].value;
                        } else {
                            self.mailInfo.recipientTel = self.editInfo[editType].value;
                        }
                        self.$set(self.editInfo, editType, {
                            showInput: false,
                            value: ''
                        });
                    } else {
                        window.alert('用户邮寄信息保存失败！');
                    }

                });
            },
            cancelMailInfo: function (editType) {
                this.$set(this.editInfo, editType, {
                    showInput: false,
                    value: ''
                });
            }
        },
        created: function () {
            var self = this;
            // 加载用户详细信息
            $.get("/chips/user/ai/userInfoDetail.vpage", {
                userId: '${userId}',
            }, function (res) {
                var userInfo = res['userInfo'];
                    self.userInfo = userInfo || {};
                self.products = userInfo['userBoughtProducts'] || [];
                self.currentProduct = self.products.length > 0 ? self.products[0].id : '';
                 if (userInfo.id) {
                     // 加载用户成绩
                     self.loadScoreByUserId();
                 }
            });
        }
    });
</script>
</@layout_default.page>