<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=12>
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
        <li>
            <button class="btn btn-primary" @click="addLottery">新增奖项</button>
        </li>
    </ul>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered so_checkboxs" so_checkboxs_values="">
            <thead>
            <tr>
                <th>奖品序号</th>
                <th>商品ID</th>
                <th>商品名称</th>
                <th>奖品档次</th>
                <th>初始数量</th>
                <th>剩余数量</th>
                <#--<th>奖品内容</th>-->
                <th>操作</th>
            </tr>
            </thead>
            <tbody style="display: none" v-show="powerPrizeList.length">
            <tr v-for="powerPrize in powerPrizeList">
                <td>{{powerPrize.id}}</td>
                <td>{{powerPrize.productId}}</td>
                <td>{{powerPrize.name}}</td>
                <td>{{powerPrize.level}}</td>
                <td>{{powerPrize.initStock}}</td>
                <td>{{powerPrize.stock}}</td>
                <td>{{powerPrize.awardRate}}</td>
                <#--<td>{{lotterie.awardContent}}</td>-->
                <td>
                    <button class="btn btn-primary" @click="editPowerPrize(powerPrize)">编辑</button>
                    <button class="btn btn-primary" @click="deletePowerPrize(powerPrize)">删除</button>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div id="edit-activity-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">{{isAddActivtiyDialog ? '新增奖项' : '编辑奖项'}}</h4>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <ul class="inline index-edit-ul">
                <li>
                    <span>奖品ID：</span>
                    <input
                            type="number"
                            placeholder="请输入奖品ID"
                            :readonly="!isAddActivtiyDialog"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.productId"
                            v-model="operateLotteriesInfo.productId">
                </li>
                <li>
                    <span>奖品名称：</span>
                    <input
                            type="text"
                            class="datetimepicker1"
                            placeholder="请输入奖品名称 例如：四旋翼飞行器"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.name"
                            v-model="operateLotteriesInfo.name">
                </li>
                <li>
                    <span>奖品档次：</span>
                    <select name="" id="" v-model="operateLotteriesInfo.level">
                        <option value="0">请选择奖品档次</option>
                        <option v-for="awardLevel in awardLevelList">{{awardLevel}}</option>
                    </select>
                </li>
                <li>
                    <span>初始数量：</span>
                    <input
                            type="number"
                            placeholder="请输入初始数量"
                            :readonly="!isAddActivtiyDialog"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.initStock"
                            v-model="operateLotteriesInfo.initStock">
                </li>
                <li>
                    <span>剩余数量：</span>
                    <input
                            type="number"
                            placeholder="请输入剩余数量"
                            :value="isAddActivtiyDialog ? '' : operateLotteriesInfo.stock"
                            v-model="operateLotteriesInfo.stock">
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
                powerPrizeList:${powerPrizeListJSON!},
                operateLotteriesInfo: {},
                awardLevelList: [1, 2, 3],
            },
            methods: {
                editPowerPrize: function (powerPrize) {
                    vm.isAddActivtiyDialog = false;

                    $('#edit-activity-dialog').modal('show');
                    vm.operateLotteriesInfo = powerPrize;
                },
                sureSave: function () {
                    $('#edit-activity-dialog').modal('hide');
                    if (!vm.operateLotteriesInfo.level) {
                        alert("奖品档次不能为空！");
                        return ;
                    }
                    var item = this.operateLotteriesInfo;
                    console.log(item);
                    $.ajax({
                        'url': 'savePrize.vpage',
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
                deletePowerPrize: function (powerPrize) {
                    $('#edit-activity-dialog').modal('hide');
                    vm.operateLotteriesInfo = powerPrize;
                    var item = this.operateLotteriesInfo.id;
                    console.log(item);
                    $.ajax({
                        'url': 'deletePrize.vpage',
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
                    vm.operateLotteriesInfo.level = 1;
                    vm.isAddActivtiyDialog = true;
                    $('#edit-activity-dialog').modal('show');
                    vm.operateLotteriesInfo = {level: 0};
                },
            }
        });
    </script>
</div>
</@layout_default.page>