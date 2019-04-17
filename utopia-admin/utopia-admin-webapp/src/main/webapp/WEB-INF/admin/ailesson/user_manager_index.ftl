<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条英语用户管理首页' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
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
</style>
<div id="userContainer" class="span9">
    <div class="row">
        <div class="span12 well">
            <form class="form-horizontal">
                <div class="row">
                    <div class="box">
                        <div class="control-group">
                            <label class="control-label" for="userId">用户id</label>
                            <div class="controls">
                                <input type="text" id="userId" v-model="userId" placeholder="用户id">
                            </div>
                        </div>
                    </div>
                    <div class="box">
                        <div class="control-group">
                            <label class="control-label" for="className">班名</label>
                            <div class="controls">
                                <select id="className" v-model="classId">
                                    <option v-for="(item, index) in classList" :key="item.id" :value="item.id">{{ item.name }}</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="box">
                        <div class="control-group">
                            <label class="control-label" for="productName">产品</label>
                            <div class="controls">
                                <template v-for="(e, i) in productTypeList">
                                    <#--<input v-if="e.selected">-->
                                    <input v-if="e.selected" @change="getProductList" class="productCheckBox" name="productType" type="checkbox" checked v-bind:value="e.value">{{e.desc}}
                                    <input v-else @change="getProductList" class="productCheckBox" name="productType" type="checkbox" v-bind:value="e.value">{{e.desc}}
                                </template>
                                <select id="productName" @change="loadClassByProductId" v-model="orderProductId">
                                    <option v-for="(item, index) in orderProductList" :key="item.id" :value="item.id">{{ item.name }}</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="box">
                        <div class="control-group">
                            <label class="control-label">消费范围</label>
                            <div class="controls">
                                <input type="number" id="minCost" class="input-small" v-model.number="minCost" placeholder="最低消费">
                                <span> &lt; 家长消费 &lt; </span>
                                <input type="number" id="maxCost" class="input-small" v-model.number="maxCost" placeholder="最高消费">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="span6">
                        <div class="control-group">
                            <div class="controls">
                                <button type="button" @click="formSubmit" class="btn btn-success">筛选</button>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <div class="row">
        <div class="span12 well">
            <table class="table table-striped">
                <caption>用户列表</caption>
                <thead>
                    <tr>
                        <td v-for="(column, index) in userColumns">{{column}}</td>
                        <td>操作</td>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="userList.length <= 0">
                        <td colspan="5" align="center">暂无数据</td>
                    </tr>
                    <tr v-else v-for="(user, index) in userList">
                        <td>{{user.name}}</td>
                        <td>{{user.id}}</td>
                        <td>{{user.className}}</td>
                        <td>{{user.buyTimes}}</td>
                        <td>{{Math.round(user.jztConsume * 1000) / 1000}}</td>
                        <td><button class="btn" type="button" @click="userBtnClick(user.id)">查询</button></td>
                    </tr>
                    <tr v-if="userList.length > 0">
                        <td colspan="6">
                            <div style="width: 300px; margin: 0 auto;">
                                <button v-if="currentPage > 1" class="btn" type="button" @click="prevPage()">上一页</button>
                                <button v-if="currentPage < totalPages" class="btn" type="button" @click="nextPage()">下一页</button>
                                <span>当前第 {{currentPage}} 页 | 共 {{totalPages}} 页</span>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
<script type="text/javascript">
    var vm = new Vue({
        el: '#userContainer',
        data: {
            userId: '',
            classId: '',
            orderProductId: '',
            minCost: '',
            maxCost: '',

            classList: [],
            orderProductList:[],
            userList: [],
            currentPage: 1,
            totalPages: 0,

            userColumns: ['姓名', '用户id', '所属班级', '购课次数', '家长通消费'],
            productTypeList:[],
        },
        methods: {
            // 根据产品找到班级列表
            loadClassByProductId: function () {
                var self = this;
                $.get("/chips/user/ai/class/obtainByProductItemId.vpage", {
                    orderProduceId: self.orderProductId,
                }, function (res) {
                    var classList = res['chipsEnglishClassList'];
                    self.classList = classList || [];
                    self.classList = [{id:'-1', name:' --- '}].concat(self.classList);
                    // 默认选中第一个产品，并加载该产品下的班级
                    //if (classList && classList.length > 0) {
                    //    self.classId = classList[0].id;
                    //}
                    self.classId = '-1';
                });
            },
            // 表单提交筛选
            formSubmit: function () {
                if (this.minCost !== '' && this.maxCost !== '' && this.minCost > this.maxCost) {
                    alert('最低消费不能大于最高消费！');
                    return;
                }

                //if (this.classId == null || this.classId === '') {
                //    alert('班级不能为空！');
                //    return;
                //}

                var self = this;
                // 筛选用户
                $.get("/chips/user/ai/filterUser.vpage", {
                    userId: self.userId,
                    classId: self.classId,
                    productId: self.orderProductId,
                    minCost: self.minCost,
                    maxCost: self.maxCost,
                    pageNumber: self.currentPage,
                }, function (res) {
                    var userList = res['userList'];
                    self.userList = userList || [];
                    self.currentPage = res['currentPage'];
                    self.totalPages = res['totalPages'];
                });
            },
            // 用户查询按钮点击
            userBtnClick: function(userId) {
                window.open("${requestContext.webAppContextPath}/chips/user/ai/detail.vpage?userId=" + userId);
            },
            // 上一页
            prevPage: function () {
                this.currentPage = this.currentPage - 1;
                this.formSubmit();
            },
            // 下一页
            nextPage: function () {
                this.currentPage = this.currentPage + 1;
                this.formSubmit();
            },
            getProductList:function () {
                var productTypeArr = [];
                var ind = 0;
                $(".productCheckBox").each(function () {
                    if($(this).is(':checked')){
                        productTypeArr[ind] = $(this).val();
                        ind = ind + 1;
                    }
                    console.log($(this).is(':checked') + "-" + $(this).val())
                })
                var self = this;
                console.log("productType:",productTypeArr.join(","));
                $.ajax({
                    url: "/chips/user/ai/allChipEnglishProduct.vpage",
                    type: "GET",
                    data: {
                        "productType": productTypeArr.join(",")
                    },
                    success: function (res) {
                        if (res.success) {
                            var orderProductList = res['orderProductList'];
                            var productTypeList = res['productTypeList'];
                            self.productTypeList = productTypeList || [];
                            self.orderProductList = orderProductList || [];
                            // 默认选中第一个产品，并加载该产品下的班级
                            if (orderProductList && orderProductList.length > 0) {
                                self.orderProductId = orderProductList[0].id;
                            }
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            }
        },
        created: function () {
            var self = this;
            // 获取薯条英语所有的产品
            $.get("/chips/user/ai/allChipEnglishProduct.vpage", function (res) {
                var orderProductList = res['orderProductList'];
                var productTypeList = res['productTypeList'];
                self.productTypeList = productTypeList || [];
                self.orderProductList = orderProductList || [];
                // 默认选中第一个产品，并加载该产品下的班级
                if (orderProductList && orderProductList.length > 0) {
                    self.orderProductId = orderProductList[0].id;
                }
            });
        }
    });
</script>
</@layout_default.page>