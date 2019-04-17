<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='轻运营班主任后台' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    .btn{
        margin-bottom: 10px;
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
<div id="main_container" class="span9">
    <legend>
        轻运营班主任后台
    </legend>
    <div >
        <a class="btn btn-primary" href="/chips/ai/todaylesson/add.vpage">添加短期课</a>
        <a class="btn btn-primary" href="/chips/ai/todaylesson/add.vpage?type=official">添加长期课</a>
    </div>

    <div class="row-fluid">
        <div class="span12" >
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form form-inline form-horizontal" action="">
                    <div class="form-group">
                        <label for="" class="mylabel">教材：</label>
                        <select  v-model="bookId" @change="getUnitTypes">
                            <option value="">请选择</option>
                            <option v-bind:value="item.value" v-for="item in bookList">{{ item.desc }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="" class="mylabel">类型：</label>
                        <select v-model="unitType" @change="getUnits">
                            <option value="">请选择</option>
                            <option v-bind:value="item.value" v-for="item in unitTypeList">{{ item.desc }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="" class="mylabel">Unit：</label>
                        <#--<input name="unitId" value="${unitId!}">-->
                        <select v-model="unitId">
                            <option value="">请选择</option>
                            <#--<option v-bind:value="item.id" v-for="item in unitList">{{ item.jsonData.name }}</option>-->
                            <option v-bind:value="item.id" v-for="item in unitList">{{ item.customName }}</option>
                        </select>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="filter" @click="query" class="btn btn-success">查询</button>
                    </div>
                </form>

            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>课程</td>
                        <td>unitId</td>
                        <td>标题</td>
                        <td>更新日期</td>
                        <td>操作</td>
                    </tr>
                    <tr v-for="(item,index) in result">
                        <td>{{ item.bookName }}</td>
                        <td>{{item.unitId}}</td>
                        <td>{{item.title}}</td>
                        <td>{{item.updateDate}}</td>
                        <td>
                            <a v-bind:href="'/chips/ai/todaylesson/add.vpage?id=' + item.id + '&type=' + item.type" name="edit" v-bind:data-id="item.id">编辑</a>
                            <a href="javascript:void(0);" @click="remove(item.id, item.type, index)">删除</a>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>



</div>
<script type="text/javascript">
    var vm = new Vue({
        el:"#main_container",
        data:{
            bookList:[],
            unitList:[],
            unitTypeList:[],
            bookId:"",
            unitId:"",
            unitType:"",
            result:[]
        },
        methods:{
            getUnitTypes:function(){
                var _this = this;
                if(_this.bookId){
                    $.get("/chips/ai/todaylesson/getUnitTypes.vpage",{
//                        method:"getUnitTypes",
                        bookId:_this.bookId
                    }).then(function(res){
                        if(res.success){
                            _this.unitTypeList = res.data;
                            if(!_this.unitTypeList || _this.unitTypeList.length == 0){
                                _this.unitList = [];
                                _this.unitId = "";
                            }
                            _this.unitType = "";
                        }else{
                            alert(res.info)
                        }
                    });
                } else {
                    _this.unitTypeList = [];
                    _this.unitList = [];
                    _this.unitId = "";
                    _this.unitType = "";
                }
            },
            getUnits:function(){
                var _this = this;
                if(_this.unitType){
                    $.get("/chips/ai/todaylesson/getUnits.vpage",{
//                        method:"getUnits",
                        bookId:_this.bookId,
                        unitType:_this.unitType,
                    }).then(function(res){
                        _this.unitList = res.data;
                    });
                } else {
                    _this.unitList = [];
                    _this.unitId = "";
                }
            },
            query:function() {
                var _this = this;
                $.get("/chips/ai/todaylesson/getResults.vpage",{
                    bookId:_this.bookId,
                    unitType:_this.unitType,
                    unitId:_this.unitId,
                }).then(function(res){
                    _this.result = res.data
                });
            },
            remove:function (id,type,index) {
                if (!confirm("确定删除数据吗？")) {
                    return;
                }
                var _this = this;
                $.post('/chips/ai/todaylesson/delete.vpage', {
                    id: id,
                    type:type
                }).then(function(res) {
                    if (res.success) {
                        _this.result.splice(index,1)
                    } else {
                        alert(res.info);
                    }
                });
            }
        },
        created:function(){
            var _this = this;

                $.get("/chips/ai/todaylesson/getBooks.vpage",{
//                    method:"getBooks",
                }).then(function(res){
                    if(res.success){
                        _this.bookList = res.data;
                    }else{
                        alert(res.info)
                    }
                });
//            _this.$options.methods.query();
            $.get("/chips/ai/todaylesson/getResults.vpage",{
//                method:"getResults",
                bookId:_this.bookId,
                unitType:_this.unitType,
                unitId:_this.unitId,
            }).then(function(res){
                _this.result = res.data
            });
            }
    });
</script>
</@layout_default.page>