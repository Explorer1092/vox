<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    span {
        font: "arial";
    }

    [v-cloak] {
        display: none
    }

    .index-edit-ul {
        text-align: center;
    }

    .index-edit-ul > li {
        display: block !important;
        margin-left: -60px;
    }

    .index-edit-ul li > span {
        display: inline-block;
        width: 140px;
        line-height: 30px;
        text-align: right;
        vertical-align: top;
    }
</style>
<div id="main_container" class="span9">
    <legend>
        <strong>活动奖品编辑</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="/site/lottery/ratelist.vpage">中奖率调整</a>
    </legend>
    <ul class="inline">
        <li>
            活动：<select name="campaignId" id="campaignId">
        <#--<option value="7" <#if campaignId == 7>selected="selected"</#if>>老师转盘抽奖</option>-->
            <option value="57" <#if campaignId == 57>selected="selected"</#if>>开学大礼包抽奖</option>
        <#--<option value="46" <#if campaignId == 46>selected="selected"</#if>>中学老师抽奖</option>-->
            <option value="49" <#if campaignId == 49>selected="selected"</#if>>APP大爆料宝箱抽奖</option>
            <option value="51" <#if campaignId == 51>selected="selected"</#if>>17奖学金金奖池</option>
            <option value="52" <#if campaignId == 52>selected="selected"</#if>>17奖学金银奖池</option>
            <option value="53" <#if campaignId == 53>selected="selected"</#if>>17奖学金铜奖池</option>
            <option value="54" <#if campaignId == 54>selected="selected"</#if>>六一点读机抽奖</option>
            <option value="56" <#if campaignId == 56>selected="selected"</#if>>点读机打包购买抽奖</option>
            <option value="58" <#if campaignId == 56>selected="selected"</#if>>阿分题英语21天学习活动抽奖</option>
            <option value="59" <#if campaignId == 59>selected="selected"</#if>>直播双十二9.9抽奖活动</option>
            <option value="60" <#if campaignId == 60>selected="selected"</#if>>2017寒假作业抽奖</option>
            <option value="61" <#if campaignId == 61>selected="selected"</#if>>阿分题数学寒假练题大赛活动抽奖</option>
            <option value="62" <#if campaignId == 62>selected="selected"</#if>>21天全能学霸养成计划活动抽奖</option>
            <option value="63" <#if campaignId == 63>selected="selected"</#if>>学生APP教学用品中心抽奖</option>
            <option value="64" <#if campaignId == 64>selected="selected"</#if>>初中英语老师布置作业抽奖</option>
            <option value="65" <#if campaignId == 65>selected="selected"</#if>>2018暑假作业抽奖</option>
        </select>
        </li>
        <li>
            <button class="btn btn-primary" @click="queryLottery">查询</button>
            <button class="btn btn-primary" @click="addLottery">新增奖项</button>
        </li>
    </ul>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered so_checkboxs" so_checkboxs_values="">
            <thead>
            <tr>
                <th>ID</th>
                <th>奖品序号</th>
                <th>活动ID</th>
                <th>奖项</th>
                <th>奖品</th>
                <th>中奖率</th>
                <th>总中奖数量</th>
                <th>剩余中奖数量</th>
                <#--<th>奖品内容</th>-->
                <th>操作</th>
            </tr>
            </thead>
            <tbody style="display: none" v-show="lotteries.length">
            <tr v-for="lotterie in lotteries">
                <td>{{lotterie.id}}</td>
                <td>{{lotterie.awardId}}</td>
                <td>{{lotterie.campaignId}}</td>
                <td>{{lotterie.awardLevelName}}</td>
                <td>{{lotterie.awardName}}</td>
                <td>{{lotterie.awardRate}}</td>
                <td>{{lotterie.totalAwardLimit}}</td>
                <td>{{lotterie.remainAwardNum}}</td>
                <#--<td>{{lotterie.awardContent}}</td>-->
                <td>
                    <button class="btn btn-primary" @click="editLottery(lotterie)">编辑</button>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div id="edit-activity-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">{{isAddActivtiyDialog ? '新增奖项' : '编辑奖项'}}</h4>
            <small style="color: red" v-if="!isAddActivtiyDialog && campaignId==63">
                友情提示：不要修改奖品内容中的 awardId, 会导致保存失败
            </small>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <ul class="inline index-edit-ul">
                <li>
                    <span>活动：</span>
                    <input type="text" readonly="readonly" v-model="campaignTypeName">
                </li>
                <li>
                    <span>奖品序号：</span>
                    <input
                            type="text"
                            placeholder="请输入奖品序号 不可重复噢"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.awardId"
                            v-model="operateLotteriesInfo.awardId">
                </li>
                <li>
                    <span>奖项：</span>
                    <input
                            type="text"
                            placeholder="请输入奖项 例如：文具用品"
                            :readonly="!isAddActivtiyDialog"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.awardLevelName"
                            v-model="operateLotteriesInfo.awardLevelName">
                </li>
                <li>
                    <span>奖品：</span>
                    <input
                            type="text"
                            class="datetimepicker1"
                            placeholder="请输入奖品 例如：四旋翼飞行器"
                            :readonly="!isAddActivtiyDialog"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.awardName"
                            v-model="operateLotteriesInfo.awardName">
                </li>
                <li>
                    <span>中奖率：</span>
                    <input
                            type="number"
                            placeholder="请输入中奖率"
                            :readonly="!isAddActivtiyDialog"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.awardRate"
                            v-model="operateLotteriesInfo.awardRate">
                </li>
                <li>
                    <span>总中奖数量：</span>
                    <input
                            type="number"
                            placeholder="请输入总中奖数量 可为空"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.totalAwardLimit"
                            v-model="operateLotteriesInfo.totalAwardLimit">
                </li>
                <li>
                    <span>剩余中奖数量：</span>
                    <input
                            type="number"
                            placeholder="请输入剩余中奖数量 可为空"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.remainAwardNum"
                            v-model="operateLotteriesInfo.remainAwardNum">
                </li>
                <li>
                    <span>奖品内容：</span>
                    <textarea
                            type="text"
                            rows="5"
                            placeholder="请输入 json 串 可为空"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.awardContent"
                            v-model="operateLotteriesInfo.awardContent"></textarea>
                </li>
            </ul>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
            <button id="reject_btn" type="button" class="btn btn-primary" @click="sureSave">保 存</button>
        </div>
    </div>

    <script>
        var vm = new Vue({
            el: '#main_container',
            data: {
                isAddActivtiyDialog: true,
                lotteries:${lotteriesJSON!},
                campaignId:${campaignId!},
                campaignTypeName: '${campaignTypeName!}',
                operateLotteriesInfo: {}
            },
            methods: {
                editLottery: function (lottery) {
                    vm.isAddActivtiyDialog = false;
                    $('#edit-activity-dialog').modal('show');
                    vm.operateLotteriesInfo = lottery;
                },
                sureSave: function () {
                    $('#edit-activity-dialog').modal('hide');
                    this.operateLotteriesInfo.campaignId = this.campaignId;
                    var item = this.operateLotteriesInfo;
                    console.log(item);
                    $.ajax({
                        'url': 'save.vpage',
                        'type': 'post',
                        'contentType': 'application/json; charset=utf-8',
                        'data': JSON.stringify(item),
                        'success': function (data) {
                            if (!data.success) {
                                alert(data.info);
                            }
                            window.location.reload();
                        }
                    });
                },
                addLottery: function () {
                    vm.isAddActivtiyDialog = true;
                    $('#edit-activity-dialog').modal('show');
                    vm.operateLotteriesInfo = {};
                },
                queryLottery: function () {
                    var id = $('#campaignId').val();
                    window.location.href = 'ratelist.vpage?id=' + id;
                }
            }
        });
    </script>
</div>
</@layout_default.page>