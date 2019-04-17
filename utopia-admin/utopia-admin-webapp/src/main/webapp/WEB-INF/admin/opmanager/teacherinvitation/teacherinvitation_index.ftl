<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<#--<script src="https://cdn.bootcss.com/vue/2.4.2/vue.js"></script>-->

<style>
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
<div id="page-index" class="span9" v-cloak>
    <legend>
        <strong>教师邀请活动奖励</strong>
    </legend>
    <ul class="inline">
        <li>
            <button class="btn btn-primary" @click="addConfig">新增地区配置</button>
        </li>
    </ul>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>省份</th>
                        <th>城市</th>
                        <th>语文奖励金额</th>
                        <th>数学奖励金额</th>
                        <th>英语奖励金额</th>
                        <th>中学数学奖励金额</th>
                        <th>中学英语奖励金额</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody style="display: none" v-show="configList.length">
                    <tr v-for="config in configList">
                        <td>{{config.provinceName}}</td>
                        <td>{{config.cityName}}</td>
                        <td>{{config.chinese}}</td>
                        <td>{{config.math}}</td>
                        <td>{{config.english}}</td>
                        <td>{{config.middleMath}}</td>
                        <td>{{config.middleEnglish}}</td>
                        <td>
                            <button class="btn btn-primary" @click="editConfig(config)">编辑</button>
                            <button class="btn btn-danger" @click="deleteConfig(config)">删除</button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

<#--新增、编辑弹窗-->
    <div id="edit-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">{{isAddDialog ? '新增地区奖励' : '编辑地区奖励'}}</h4>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <ul class="inline index-edit-ul">
                <li>
                    <span>省：</span>
                    <td>
                        <select id="province" v-model.lazy="operateItemInfo.provinceCode" :disabled="!isAddDialog">
                            <option v-for="province in provinces" v-bind:value="province.code">
                                {{province.name}}
                            </option>
                        </select>
                    </td>
                </li>
                <li>
                    <span>市：</span>
                    <td>
                        <select id="city" v-model.lazy="operateItemInfo.cityCode" :disabled="!isAddDialog">
                            <option v-for="city in citys" v-bind:value="city.code">
                                {{city.name}}
                            </option>
                        </select>
                    </td>
                </li>
                <li>
                    <span>语文奖励：</span>
                    <input
                            type="number"
                            placeholder="请输入语文奖励金额"
                            :value="isAddDialog ? '' : operateItemInfo.chinese"
                            v-model="operateItemInfo.chinese">
                </li>
                <li>
                    <span>数学奖励：</span>
                    <input
                            type="number"
                            placeholder="请输入数学奖励金额"
                            :value="isAddDialog ? '' : operateItemInfo.math"
                            v-model="operateItemInfo.math">
                </li>
                <li>
                    <span>英语奖励：</span>
                    <input
                            type="number"
                            placeholder="请输入英语奖励金额"
                            :value="isAddDialog ? '' : operateItemInfo.english"
                            v-model="operateItemInfo.english">
                </li>
                <li>
                    <span>中学数学奖励：</span>
                    <input
                            type="number"
                            placeholder="请输入中学数学奖励金额"
                            :value="isAddDialog ? '' : operateItemInfo.middleMath"
                            v-model="operateItemInfo.middleMath">
                </li>
                <li>
                    <span>中学英语奖励：</span>
                    <input
                            type="number"
                            placeholder="请输入中学英语奖励金额"
                            :value="isAddDialog ? '' : operateItemInfo.middleEnglish"
                            v-model="operateItemInfo.middleEnglish">
                </li>
            </ul>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
            <button id="reject_btn" type="button" class="btn btn-primary" @click="sureSaveAward">保 存</button>
        </div>
    </div>

<#--删除配置弹窗-->
    <div id="delete-dialog" class="modal fade hide">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">删除配置</h4>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible; width: auto">
            <p>您确定要删除该配置吗？</p>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">取 消</button>
            <button id="reject_btn" type="button" class="btn btn-primary" @click="sureDelete">确 定</button>
        </div>
    </div>
</div>
<script>
    <#--使用vue渲染列表数据-->
    var vm = new Vue({
        el: '#page-index',
        data: {
            configList: [],
            provinces: [],
            citys: [],
            isAddDialog: true,
            operateItemInfo: {}
        },
        mounted: function () {
            var vm = this;
            vm.initProvinces();
            vm.loadConfig();
        },
        methods: {
            initProvinces: function () {
                var vm = this;
                $.ajax({
                    type: 'get',
                    url: 'getregion.vpage',
                    success: function (data) {
                        vm.provinces = data.region;
                        vm.loadCitys();
                    }
                });
            },
            loadCitys: function () {
                var vm = this;
                $.ajax({
                    type: 'get',
                    url: 'getregion.vpage?pcode=' + vm.operateItemInfo.provinceCode,
                    success: function (data) {
                        vm.citys = data.region;
                    }
                });
            },
            loadConfig: function () {
                var vm = this;
                $.ajax({
                    type: 'get',
                    url: 'query.vpage',
                    success: function (data) {
                        vm.configList = data.data;
                    }
                });
            },
            editConfig: function (config) {
                var vm = this;

                vm.isAddDialog = false;
                $('#edit-dialog').modal('show');
                vm.operateItemInfo = config;
                vm.loadCitys();
            },
            deleteConfig: function (config) {
                $('#delete-dialog').modal('show');
                vm.operateItemInfo = config;
            },
            sureSaveAward: function () {
                var vm = this;

                $('#edit-dialog').modal('hide');
                var provinceName = $("#province").find("option:selected").text().trim();
                var cityName = $("#city").find("option:selected").text().trim();

                vm.operateItemInfo.provinceName = provinceName;
                vm.operateItemInfo.cityName = cityName;

                if (vm.operateItemInfo.cityCode == null || vm.operateItemInfo.provinceCode == null) {
                    alert("未选择城市");
                    return false;
                }
                if (vm.operateItemInfo.chinese == null || vm.operateItemInfo.math == null || vm.operateItemInfo.english == null
                    || vm.operateItemInfo.middleMath == null || vm.operateItemInfo.middleEnglish == null ) {
                    alert("奖励金额不能为空");
                    return false;
                }

                $.ajax({
                    type: 'post',
                    url: 'upsert.vpage',
                    contentType: 'application/json;charset=UTF-8',
                    data: JSON.stringify(vm.operateItemInfo),
                    success: function (data) {
                        if (data.success) {
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }
                    }
                });
            },
            sureDelete: function () {
                $.ajax({
                    type: 'post',
                    url: 'delete.vpage?id=' + vm.operateItemInfo.id,
                    success: function (data) {
                        if (data.success) {
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }
                    }
                });
            },
            addConfig: function () {
                vm.isAddDialog = true;
                $('#edit-dialog').modal('show');
                vm.operateItemInfo = {};
            }
        }
    });

    $(function () {
        $("#province").change(function () {
            vm.operateItemInfo.provinceCode = this.value;
            vm.loadCitys();
        });
    });
</script>
</@layout_default.page>