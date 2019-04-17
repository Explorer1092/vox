<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    [v-cloak]{
        display: none;
    }
    .wd600{
        width:600px !important;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <div class="control-group">
            <label class="control-label">标题：</label>
            <div class="controls">
                <input v-model="title" type="text" class="wd600" placeholder="标题">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">对比班级选择：</label>
            <div class="controls">
                {{ compareClazzNames }}
            </div>
        </div>
        <div class="control-group">
            <div style="font-size: 12px;" class="well form form-inline form-horizontal">
                <div class="form-group">
                    <label for="" class="mylabel">状态：</label>
                    <select @change="loadProduct" v-model="productType">
                        <option value="">----请选择----</option>
                        <option value="1">已完结</option>
                        <option value="2">当前</option>
                        <option value="3">未开始</option>
                    </select>
                    <label for="" class="mylabel">产品：</label>
                    <select id="productName" v-model="orderProductId" @change="loadClazz">
                        <option v-for="(item, index) in productList" :key="item.id" :value="item.id">{{ item.name }}</option>
                    </select>
                </div>
                <table class="table table-condensed  table-hover table-striped table-bordered">
                    <tr>
                        <td><input id="sel_1" @change="selectAll" type="checkbox"
                                           value="1"/>全选</td>
                        <td>班名</td>
                    </tr>
                    <tr v-if="clazzList.length <= 0">
                        <td colspan="2" align="center">暂无数据</td>
                    </tr>
                    <tr v-else v-for="(item, index) in clazzList">
                        <td><input type="checkbox"  name="showPlay"  v-bind:value="index"></td>
                        <#--<td v-if="compareClazzIdArr.indexOf(item.id) !== -1">aaaaaa<input type="checkbox"  name="showPlay" checked="compareClazzIdArr.indexOf(item.id) !== -1" v-bind:value="index"></td>-->
                        <#--<td v-else>bbbb<input type="checkbox"  name="showPlay" v-bind:value="index"></td>-->
                        <td>{{ item.name }}</td>
                    </tr>
                </table>
                <div>
                    <label style="position: relative;" class="btn btn-primary">
                        <span v-on:click="add">添加</span>
                    </label>
                </div>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">基线班级选择：</label>
            <div class="controls">
                <select id="basicClazz" v-model="basicClazz">
                    <option v-for="(item, index) in compareClazzArr" :key="item.id" :value="item">{{ item.name }}</option>
                </select>
            </div>
        </div>

        <div class="control-group">
            <div>
                <label style="position: relative;" class="btn btn-primary">
                    <span v-on:click="cancel">取消</span>
                </label>
                <label style="position: relative;" class="btn btn-primary">
                    <span v-on:click="save">确认</span>
                </label>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var vm = new Vue({
        el:'#box',
        data:{
            id:"",
            compareClazzIds:"",
            compareClazzIdArr:[],
            compareClazzArr:[],
            compareClazzNames:"",
            title:"",
            basicClazzId:"",
            basicClazz:"",
            basicClazzName:"",
            productList:[],
            orderProductId:"",
            productType:"",
            clazzList:[]
        },
        methods:{
            save:function(){
                var _this = this;
                $.post('${requestContext.webAppContextPath}/chips/ai/todaylesson/detail.vpage', {
                    data:JSON.stringify(_this.data)
                }, function (res) {
                    if (res.success) {
                        _this.previewStatus = true;
                        alert("保存成功");

                    } else {
                        alert(res.info);
                    }
                });
            },
            loadProduct:function () {
                var _this = this;
                console.log(_this.productType);
                $.ajax({
                    url: "/chips/user/ai/allChipEnglishProduct.vpage",
                    type: "GET",
                    data: {
                        "productType": _this.productType
                    },
                    success: function (res) {
                        if (res.success) {
                            var orderProductList = res['orderProductList'];
                            _this.productList = orderProductList || [];
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            },
            loadClazz:function () {
                var _this = this;
//                $("input[name='showPlay']").each(function (i) {
//                                this.checked = false;       //
//                            });
                $.ajax({
                    url: "/chips/clazz/monitor/loadClazz.vpage",
                    type: "GET",
                    data: {
                        "productId": _this.orderProductId
                    },
                    success: function (res) {
                        if (res.success) {
                            var clazzList = res['clazzList'];
                            _this.clazzList = clazzList || [];
                            $("input[name='showPlay']").each(function (i) {
                                this.checked = false;       //
                            });
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            },
            selectAll:function () {
                var isCheck = $("#sel_1").is(':checked');  //获得全选复选框是否选中
                var userIds = "";
                $("input[name='showPlay']").each(function (i) {
                    this.checked = isCheck;       //循环赋值给每个复选框是否选中
                });
            },
            add:function () {
                var _this = this;
                var compareClazzIds = _this.compareClazzIds;
                var compareClazzNames =  _this.compareClazzNames;
                var compareClazzIdArr =  _this.compareClazzIdArr;
                var compareClazzArr  =  _this.compareClazzArr ;
                $("input[name='showPlay']").each(function (i) {
                    if(this.checked) {
                        console.log("index",this.value)
                        console.log("i",i)
                        console.log("clazz:", _this.clazzList[this.value])
                        if(compareClazzIdArr.indexOf(_this.clazzList[this.value].id) === -1){
                            if(compareClazzIds == ""){
                                compareClazzIds =  _this.clazzList[this.value].id;
                                compareClazzNames = _this.clazzList[this.value].name;
                            } else {
                                compareClazzIds = compareClazzIds + "," + _this.clazzList[this.value].id;
                                compareClazzNames = compareClazzNames  + "," + _this.clazzList[this.value].name;
                            }
                            compareClazzIdArr.push(_this.clazzList[this.value].id)
                            compareClazzArr.push(_this.clazzList[this.value])
                        }
                    }
                });
                _this.compareClazzIds = compareClazzIds;
                _this.compareClazzNames = compareClazzNames;
                _this.compareClazzIdArr = compareClazzIdArr;
                console.log("compareClazzIds", _this.compareClazzIds)
                console.log("compareClazzIdArr", _this.compareClazzIdArr)
                console.log("compareClazzNames", _this.compareClazzNames)
            },
            cancel:function () {
                window.location.href = "/chips/clazz/monitor/compareClazzIndex.vpage"
            },
            save:function () {
                var _this = this;
                $.ajax({
                    url: "/chips/clazz/monitor/compareClazzEditSave.vpage",
                    type: "POST",
                    data: {
                        "title": _this.title,
                        "basicClazzId": _this.basicClazz.id,
                        "basicClazzName": _this.basicClazz.name,
                        "compareClazzIds": _this.compareClazzIds,
                        "compareClazzNames": _this.compareClazzNames,
                        "id": _this.id,
                    },
                    success: function (res) {
                        if (res.success) {
                            window.location.href = "/chips/clazz/monitor/compareClazzIndex.vpage"
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
        created:function(){
            var _this = this;
            var id = "${id!}";
            console.log(id)
            if(id){
                $.post('${requestContext.webAppContextPath}/chips/clazz/monitor/compareClazzQuery.vpage', {
                    id:id
                }, function (res) {
                    console.log(res)
                    var clazzCompare = res['clazzCompare'];
                    _this.id = clazzCompare.id;
                    _this.title = clazzCompare.title;
                    _this.basicClazzId = clazzCompare.basicClazzId;
                    _this.basicClazzName = clazzCompare.basicClazzName;
                    _this.compareClazzIds = clazzCompare.compareClazzIds;
                    _this.compareClazzNames = clazzCompare.compareClazzNames;
                    _this.compareClazzNames = clazzCompare.compareClazzNames;
                    _this.compareClazzIdArr = res['compareClazzIdArr'] ;
                    _this.compareClazzArr = res['compareClazzArr'];
                    _this.basicClazz = res['basicClazz']
                });
            }
        }
    })

</script>

</@layout_default.page>