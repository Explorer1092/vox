<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='主动服务模板' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<div id="main_container" class="span9">
    <legend>
        开通体验课程
    </legend>

    <div class="row-fluid">
        <div class="span12" >
            <div class="well" style="font-size: 16px; width: 60%; padding-left: 100px">
                <div class="control-group">
                    <label class="control-label">选择开通用户：</label>
                    <div class="controls">
                     <textarea id="targetUser" v-model="phones" rows="20" style="width: 300px"
                               placeholder="一行输入一条数据，如果超过100行建议使用其他策略投放"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">选择开通课程：</label>
                    <div class="controls">
                        <select id="productId" v-model="products" multiple size="10" style="width: 300px">
                            <option value="">---请选择--</option>
                            <option v-for="(item, index) in productList" :key="item.id" :value="item.value">{{ item.desc }}</option>
                        </select>
                        <template  v-for="(e, i) in productTypeList">
                            <input v-if="e.selected" v-on:change="queryProduct" class="productCheckBox" name="productType" type="checkbox" checked v-bind:value="e.value">{{e.desc}}
                            <input v-else v-on:change="queryProduct"  class="productCheckBox" name="productType" type="checkbox" v-bind:value="e.value">{{e.desc}}
                        </template>
                    </div>
                </div>
                <div class="control-group" >
                    <span id="save" v-on:click="save"  class="btn btn-success btn-large" >确定</span>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    var vm = new Vue({
        el:"#main_container",
        model: false,
        data:{
            phones:"",
            products:[],
            productList:[],
            productTypeList:[]

        },
        methods:{
            save:function () {
                var _this = this;
                console.log("products", _this.products);
                console.log("phones", _this.phones);
                console.log("phones", _this.phones.split("\n"));
                console.log("phones", _this.phones.split("\n").join(","));
                $.ajax({
                    url: "/chips/course/openCourseSave.vpage",
                    type: "POST",
                    data: {
                        "phones": _this.phones.split("\n").join(","),
                        "products": _this.products.join(","),

                    },
                    success: function (res) {
                        if (res.success) {
                            var noUserList = res.noUserMobileList;
                            if(noUserList && noUserList.length > 0) {
                                alert("开通成功,有未能开通的手机号:" + noUserList.join(","))
                            } else {
                                alert("开通成功")
                            }
                            window.history.back();
//                            console.log(noUserList)
//                            _this.productList = res.productList;
//                            console.log("res",res);
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            },
            queryProduct:function () {
                var _this = this;
                var productTypeArr = [];
                var ind = 0;
                $(".productCheckBox").each(function () {
                    if($(this).is(':checked')){
                        productTypeArr[ind] = $(this).val();
                        ind = ind + 1;
                    }
                    console.log($(this).is(':checked') + "-" + $(this).val())
                })
                if(productTypeArr.length == 0){
                    productTypeArr = [2,3]
                }
                console.log("productTypeArr",productTypeArr)
                console.log(productTypeArr.join(","))
                $.ajax({
                    url: "/chips/chips/clazz/productList.vpage",
                    type: "GET",
                    data: {
                        "productType": productTypeArr.join(",")
                    },
                    success: function (res) {
                        if (res.success) {
                            console.log(res.productList.length)
                            _this.productList = res.productList;
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            },
        },
        created:function(){
            var _this = this;
            $.get('${requestContext.webAppContextPath}/chips/chips/clazz/changeQuery.vpage', {//用换课里面的接口,获取产品
            }, function (res) {
                if(res.success) {
                    _this.productList = res.toProductList;
                    _this.productTypeList = res.productTypeList;
                }
            });
        },
    });
</script>
</@layout_default.page>