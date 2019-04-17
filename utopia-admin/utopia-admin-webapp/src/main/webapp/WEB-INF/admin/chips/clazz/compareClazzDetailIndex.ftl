<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='主动服务' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/vue-datepicker/vue-datepicker.js"></script>
<style>
    .control-label{
        width:80px !important;
    }
    .form-horizontal .controls {
        margin-left: 100px !important;
    }
    .box{
        display: inline-block;

    }
    .not-finished-status{
        color: red;
    }
    .vdp-datepicker input {
        height: 30px;
    }
    .remind-div{
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        opacity: 0.9;
        background: #c3c3c3;
    }
    .remind-div-container {
        position: absolute;
        width: 200px;
        top: calc(40%);
        left: calc(50% - 100px);
        background-color: #fff;
        padding: 10px;
    }
    .remind-div-title {
        font-size: 18px;
        font-weight: 600;
        border-bottom: solid 1px #CCC;
        padding-bottom: 10px;
        height: 18px;
        line-height: 18px;
        margin-bottom: 10px;
    }
    .remind-div-close-btn {
        float: right;
        cursor: pointer;
        background: #f2f2f2;
        padding: 5px;
        margin-top: -5px;
    }
    .remind-div-body{
        margin-bottom: 10px;
        text-indent: 30px;
        font-size: 15px;
        color: #000;
        padding-bottom: 5px;: ;
        margin-bottom: 5px;
        border-bottom: solid 1px #CCC;
    }
    .remind-div-foot button {
        float: right;
    }
    .form-group {
        margin: 5px 0;
        display: inline-block;
    }

    .form-group .mylabel {
        width: 150px;
        text-align: right;
    }

    .table-responsive {
        min-height: .01%;
        overflow-x: auto
    }

    @media screen and (max-width: 767px) {
        .table-responsive {
            width: 100%;
            margin-bottom: 15px;
            overflow-y: hidden;
            -ms-overflow-style: -ms-autohiding-scrollbar;
            border: 1px solid #ddd
        }

        .table-responsive > .table {
            margin-bottom: 0
        }

        .table-responsive > .table > tbody > tr > td, .table-responsive > .table > tbody > tr > th, .table-responsive > .table > tfoot > tr > td, .table-responsive > .table > tfoot > tr > th, .table-responsive > .table > thead > tr > td, .table-responsive > .table > thead > tr > th {
            white-space: nowrap
        }

        .table-responsive > .table-bordered {
            border: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:first-child, .table-responsive > .table-bordered > tbody > tr > th:first-child, .table-responsive > .table-bordered > tfoot > tr > td:first-child, .table-responsive > .table-bordered > tfoot > tr > th:first-child, .table-responsive > .table-bordered > thead > tr > td:first-child, .table-responsive > .table-bordered > thead > tr > th:first-child {
            border-left: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:last-child, .table-responsive > .table-bordered > tbody > tr > th:last-child, .table-responsive > .table-bordered > tfoot > tr > td:last-child, .table-responsive > .table-bordered > tfoot > tr > th:last-child, .table-responsive > .table-bordered > thead > tr > td:last-child, .table-responsive > .table-bordered > thead > tr > th:last-child {
            border-right: 0
        }

        .table-responsive > .table-bordered > tbody > tr:last-child > td, .table-responsive > .table-bordered > tbody > tr:last-child > th, .table-responsive > .table-bordered > tfoot > tr:last-child > td, .table-responsive > .table-bordered > tfoot > tr:last-child > th {
            border-bottom: 0
        }
    }

    .table_box {
        max-height: 700px;
    }

    .table_box table tr td {
        white-space: nowrap;
    }

    .table_box table tbody tr td {
        white-space: nowrap;
    }
</style>
<div id="userContainer" class="span9">
    <div class="row">
        <div class="span12 well form form-inline form-horizontal">
            <form id="frm" class="form form-inline form-horizontal" action="/chips/chips/clazz/summary/list.vpage">
                <div class="form-group">
                    <label for="" class="mylabel">{{ title }}</label>
                </div>
                <div class="form-group">
                    <label for="" class="mylabel">数据类型：</label>
                    <select v-model="dataType" class="multiple district_select">
                        <option value="2" selected>当日数据</option>
                        <option value="1" >最新数据</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="" class="mylabel">单元：</label>
                    <select v-model="dayIndex" class="multiple district_select">
                        <option v-for="(item, index) in dayIndexList" :key="item" :value="item">DAY{{ item }}</option>
                    </select>
                </div>
                <div class="form-group" style="text-align: center;width: 150px;">
                    <label style="position: relative;" class="btn btn-primary">
                        <span v-on:click="filter">查看</span>
                    </label>
                </div>
            </form>
        </div>
        <div class="well">

            <table class="table table-condensed  table-hover table-bordered">
                <tr>
                    学习人数:{{clazzComparePojo.totalUserCount}}
                </tr>
            </table>
            <table class="table table-condensed  table-hover table-bordered">
                <tr>
                    <td colspan="3"> 完课人数:{{clazzComparePojo.totalCompleteCount}}</td>
                    <td colspan="3"> 完课率:{{clazzComparePojo.totalCompleteRate}}</td>
                </tr>
                <tr>
                    <td width="10%"> 班级</td>
                    <td width="10%"> 学习人数</td>
                    <td width="10%"> 完课人数</td>
                    <td width="10%"> 完课率</td>
                    <td width="10%"> 催补课</td>
                    <td width="10%"> </td>
                </tr>
                <template  v-for="(item, index) in clazzComparePojo.firstList" >
                    <tr v-if="item.clazzId == basicClazzId" style="background-color: #00b050">
                        <td>{{ item.clazzName }}</td>
                        <td>{{ item.userCount }}</td>
                        <td>{{ item.completeCount }}</td>
                        <td>{{ item.completeRateStr }}</td>
                        <td>{{ item.remindCount }}</td>
                        <td>
                            <label style="position: relative;" class="btn btn-primary">
                                <span v-on:click="entry(item.clazzId, item.productId)">进入班级</span>
                            </label>
                        </td>
                    </tr>
                    <tr v-else>
                        <td>{{ item.clazzName }}</td>
                        <td>{{ item.userCount }}</td>
                        <td>{{ item.completeCount }}</td>
                        <td>{{ item.completeRateStr }}</td>
                        <td>{{ item.remindCount }}</td>
                        <td>
                            <label style="position: relative;" class="btn btn-primary">
                                <span v-on:click="entry(item.clazzId, item.productId)">进入班级</span>
                            </label>
                        </td>
                    </tr>
                </template>
            </table>
            <table class="table table-condensed  table-hover table-bordered">
                <tr>
                    <td colspan="2"> 完课点评人数:{{clazzComparePojo.totalRemarkCount}}</td>
                    <td colspan="3"> 完课点评率:{{clazzComparePojo.totalRemarkRate}}</td>
                </tr>
                <tr>
                    <td width="10%"> 班级</td>
                    <td width="10%"> 完课人数</td>
                    <td width="10%"> 完课点评人数</td>
                    <td width="10%"> 完课点评率</td>
                    <td width="10%"> </td>
                </tr>
                <template v-for="(item, index) in clazzComparePojo.secondList">
                    <tr v-if="item.clazzId == basicClazzId" style="background-color: #00b050">
                        <td>{{ item.clazzName }}</td>
                        <td>{{ item.completeCount }}</td>
                        <td>{{ item.remarkCount }}</td>
                        <td>{{ item.remarkRateStr }}</td>
                        <td>
                            <label style="position: relative;" class="btn btn-primary">
                                <span v-on:click="entry(item.clazzId, item.productId)">进入班级</span>
                            </label>
                        </td>
                    </tr>
                    <tr v-else>
                        <td>{{ item.clazzName }}</td>
                        <td>{{ item.completeCount }}</td>
                        <td>{{ item.remarkCount }}</td>
                        <td>{{ item.remarkRateStr }}</td>
                        <td>
                            <label style="position: relative;" class="btn btn-primary">
                                <span v-on:click="entry(item.clazzId, item.productId)">进入班级</span>
                            </label>
                        </td>
                    </tr>
                </template>
            </table>
            <table class="table table-condensed  table-hover table-bordered">
                <tr>
                    <td colspan="3"> 续费人数:{{clazzComparePojo.totalPaidCount}}</td>
                    <td colspan="3"> 续费率:{{clazzComparePojo.totalPaidRate}}</td>
                </tr>
                <tr>
                    <td width="10%"> 班级</td>
                    <td width="10%"> 定级人数</td>
                    <td width="10%"> 续费人数</td>
                    <td width="10%"> 续费率</td>
                    <td width="10%"> 续费提醒</td>
                    <td width="10%"> </td>
                </tr>
                <template v-for="(item, index) in clazzComparePojo.thridList">
                    <tr v-if="item.clazzId == basicClazzId" style="background-color: #00b050">
                        <td>{{ item.clazzName }}</td>
                        <td>{{ item.gradeCount }}</td>
                        <td>{{ item.paidCount }}</td>
                        <td>{{ item.paidRateStr }}</td>
                        <td>{{ item.paidRemindCount }}</td>
                        <td>
                            <label style="position: relative;" class="btn btn-primary">
                                <span v-on:click="entry(item.clazzId, item.productId)">进入班级</span>
                            </label>
                        </td>
                    </tr>
                    <tr v-else>
                        <td>{{ item.clazzName }}</td>
                        <td>{{ item.gradeCount }}</td>
                        <td>{{ item.paidCount }}</td>
                        <td>{{ item.paidRateStr }}</td>
                        <td>{{ item.paidRemindCount }}</td>
                        <td>
                            <label style="position: relative;" class="btn btn-primary">
                                <span v-on:click="entry(item.clazzId, item.productId)">进入班级</span>
                            </label>
                        </td>
                    </tr>
                </template>
            </table>
        </div>
    </div>
</div>
<script type="text/javascript">
    var vm = new Vue({
        el: '#userContainer',
        data: {
            dayIndexList:[],
            title:"",
            id:"${id!}",
            dataType:"2",
            dayIndex:1,
            clazzComparePojo:"",
            basicClazzId:""
        },
        methods: {
            filter:function (id) {
                var _this = this;
                console.log("id",_this.id)
                console.log("dataType",_this.dataType)
                console.log("dayIndex",_this.dayIndex)
                $.ajax({
                    url: "/chips/clazz/monitor/compareClazzDetailQuery.vpage",
                    type: "GET",
                    data: {
                        "id": _this.id,
                        "dataType": _this.dataType,
                        "dayIndex": _this.dayIndex,
                    },
                    success: function (res) {
                        if (res.success) {
                            _this.clazzComparePojo = res.clazzComparePojo;
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            },
            entry:function (clazzId, productId) {
                console.log("clazzId", clazzId)
                console.log("productId", productId)
                window.open("/chips/chips/clazz/manager/basicInfo.vpage?clazzId=" + clazzId + "&productId=" + productId)
            }
        },
        created: function () {
            var _this = this;
            console.log("id", "${id!}");
            $.get('${requestContext.webAppContextPath}/chips/clazz/monitor/compareClazzDetailHead.vpage', {
                id: "${id!}"
            }, function (res) {
               _this.dayIndexList = res.dayIndexList
               _this.title = res['title'];
               _this.basicClazzId = res['basicClazzId'];
            });
        }
    });

</script>
</@layout_default.page>
