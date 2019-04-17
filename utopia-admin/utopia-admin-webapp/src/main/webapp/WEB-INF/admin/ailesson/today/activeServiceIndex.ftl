<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='主动服务模板' page_num=26>
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
        主动服务模板管理
    </legend>

    <div class="row-fluid">
        <div class="span12" >
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form form-inline form-horizontal" action="">
                    <div class="form-group">
                        <label for="" class="mylabel">题目ID：</label>
                        <input v-model="qid" >
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="filter" @click="queryByQid" class="btn btn-success">查询</button>
                    </div>
                </form>
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
                        <label for="" class="mylabel">单元：</label>
                        <#--<input name="unitId" value="${unitId!}">-->
                        <select v-model="unitId" @change="getLessons">
                            <option value="">请选择</option>
                            <#--<option v-bind:value="item.id" v-for="item in unitList">{{ item.jsonData.name }}</option>-->
                            <option v-bind:value="item.id" v-for="item in unitList">{{ item.customName }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="" class="mylabel">课程：</label>
                    <#--<input name="unitId" value="${unitId!}">-->
                        <select v-model="lessonId">
                            <option value="">请选择</option>
                        <#--<option v-bind:value="item.id" v-for="item in unitList">{{ item.jsonData.name }}</option>-->
                            <option v-bind:value="item.id" v-for="item in lessonList">{{ item.customName }}</option>
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
                        <td>题目ID</td>
                        <td>题目</td>
                        <td>主动服务模板</td>
                        <td>编辑时间</td>
                        <td>操作</td>
                    </tr>
                    <tr v-for="(item,index) in result">
                        <td>{{ item.id }}</td>
                        <td>{{ item.name }}</td>
                        <td v-if="item.flag">已添加</td>
                        <td v-else>未添加</td>
                        <td>{{ item.updateTime }}</td>
                        <#--<td>-->
                            <#--<a v-bind:href="'/chips/ai/todaylesson/add.vpage?id=' + item.id + '&type=' + item.type" name="edit" v-bind:data-id="item.id">编辑</a>-->
                            <#--<a href="javascript:void(0);" @click="remove(item.id, item.type, index)">删除</a>-->
                        <#--</td>-->
                        <td v-if="item.flag">
                            <#--<a v-bind:href="'/chips/ai/todaylesson/activeServiceAdd.vpage?qid=' + item.id " name="edit" v-bind:data-id="item.id">编辑模板</a>-->
                            <button type="button"  @click="add(item.id)" class="btn btn-success">编辑模板</button>
                            <button type="button"  @click="preview(item.id)" class="btn btn-success">预览</button>
                            <#--<a v-bind:href="'/chips/ai/todaylesson/activeServiceAdd.vpage?qid=' + item.id " name="edit" v-bind:data-id="item.id">预览</a>-->
                            <#--<a v-bind:href="'/chips/ai/todaylesson/activeServiceAdd.vpage?id=' + item.id + '&type=' + item.type" name="edit" v-bind:data-id="item.id">编辑</a>-->
                            <#--<button type="button" id="preview" @click="preview" class="btn btn-success">预览</button>-->
                        </td>
                        <td v-else>
                            <#--<a v-bind:href="'/chips/ai/todaylesson/activeServiceAdd.vpage?qid=' + item.id " name="edit" v-bind:data-id="item.id">添加模板</a>-->
                            <button type="button"  @click="add(item.id)" class="btn btn-success">添加模板</button>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <div id="myModal" class="modal" :class="{hide:!model,fade:!model}" tabindex="-1" role="dialog"
         aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" @click="closeModel">×</button>
            <h3 id="myModalLabel">用手机扫描二维码预览</h3>
        </div>
        <div class="modal-body">
            <div id="sharecode_box" style="text-align: center"></div>
        </div>
    </div>

    <div :class="{in:model,'modal-backdrop':model,fade:model}"></div>

</div>
<script type="text/javascript">
    var vm = new Vue({
        el:"#main_container",
        model: false,
        data:{
            clean:true,
            model: false,
            bookList:[],
            unitList:[],
            unitTypeList:[],
            lessonList:[],
            bookId:"",
            unitId:"",
            unitType:"",
            result:[],
            lessonId:"",
            qid:"",
            method:"",
            resultBookId:"",
            resultUnitId:"",
            resultUnitType:"",
            resultLessonId:"",

        },
        methods:{
            getUnitTypes:function(){
                console.log("getUnitTypes method called")
                var _this = this;
                if(_this.bookId){
                    $.get("/chips/ai/todaylesson/getUnitTypes.vpage",{
                        bookId:_this.bookId
                    }).then(function(res){
                        if(res.success){
                            _this.unitTypeList = res.data;
                            if(_this.clean){
                                console.log("getUnitypes clean")
                                _this.unitType = "";
                                _this.unitList = [];
                                _this.lessonList = [];
                                _this.unitId = "";
                                _this.lessonId = "";
                            }
                        }else{
                            alert(res.info)
                        }
                    });
                } else {
                    _this.unitTypeList = [];
                    _this.unitList = [];
                    _this.unitId = "";
                    _this.unitType = "";
                    _this.lessonId = "";
                    _this.lessonList = [];
                }
            },
            getUnits:function(){
                var _this = this;
                console.log(_this.unitType)
                console.log("getUnits method called")
                if(_this.unitType){
                    $.get("/chips/ai/todaylesson/getUnits.vpage",{
//                        method:"getUnits",
                        bookId:_this.bookId,
                        unitType:_this.unitType,
                    }).then(function(res){
                        if(res.success){
                            _this.unitList = res.data;
                            if(_this.clean) {
                                _this.unitId = "";
                                _this.lessonList = [];
                                _this.lessonId = "";
                            }
                        } else {
                            alert(res.info)
                        }
                    });
                } else {
                    _this.unitList = [];
                    _this.unitId = "";
                    _this.lessonList = [];
                    _this.lessonId = "";
                }
            },
            getLessons:function () {
                console.log("getLessons method called")
                var _this = this;
                if(_this.unitId){
                    $.get("/chips/ai/todaylesson/getLessons.vpage",{
                        bookId:_this.bookId,
                        unitId:_this.unitId,
                    }).then(function(res){
                        if(res.success){
                            _this.lessonList = res.data;
                            if(_this.clean) {
                                _this.lessonId = "";
                            } else {
                                _this.clean = true;
                            }
                        } else {
                            alert(res.info);
                        }

                    });
                } else {
                    _this.lessonList = [];
                    _this.lessonId = "";
                }
            },
            queryByQid:function() {
                var _this = this;
                console.log(_this.qid);
                $.get("/chips/ai/todaylesson/getQuestionResult.vpage",{
                    qid:_this.qid,
                    bookId:_this.bookId,
                    unitType:_this.unitType,
                    unitId:_this.unitId,
                    lessonId:_this.lessonId,
                }).then(function(res){
                    if(res.success){
                        _this.result = res.data
                        _this.resultBookId = _this.bookId;
                        _this.resultUnitType = _this.unitType;
                        _this.resultUnitId = _this.unitId;
                        _this.resultLessonId = _this.lessonId;
                        console.log(_this)
                    }else {
                        alert(res.info)
                    }
                });
                _this.method = "byQid";
            },
            query:function() {
                var _this = this;
                $.get("/chips/ai/todaylesson/getQuestionResult.vpage",{
                    lessonId:_this.lessonId,
                    bookId:_this.bookId,
                    unitType:_this.unitType,
                    unitId:_this.unitId,
                }).then(function(res){
                    if(res.success){
                        _this.result = res.data
                        _this.resultBookId = _this.bookId;
                        _this.resultUnitType = _this.unitType;
                        _this.resultUnitId = _this.unitId;
                        _this.resultLessonId = _this.lessonId;
                    } else {
                        alert(res.info);
                    }
                });
                _this.method = "byLesson";
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
            },
            add:function (qid) {
                console.log("qid:");
                console.log(qid);
                _this= this;
                console.log( "/chips/ai/todaylesson/activeServiceAdd.vpage?qid=" + qid + "&method=" + _this.method + "&bookId=" + _this.resultBookId
                        + "&unitType=" + _this.resultUnitType + "&unitId=" + _this.resultUnitId + "&lessonId=" + _this.resultLessonId)
//                console.log("/chips/ai/todaylesson/activeServiceAdd.vpage?qid=" + qid + "&method=" + _this.method + "&bookId=" + _this.bookId
//                        + "&unitType=" + _this.unitType + "&unitId=" + _this.unitId + "&lessonId=" + _this.lessonId)
                window.location.href = "/chips/ai/todaylesson/activeServiceAdd.vpage?qid=" + qid + "&method=" + _this.method + "&bookId=" + _this.bookId
                        + "&unitType=" + _this.unitType + "&unitId=" + _this.unitId + "&lessonId=" + _this.lessonId;
            },
            preview: function (qid) {
                var _this = this;
                var hostName = window.location.host;
                var _map = {
                    'admin.test.17zuoye.net': 'wechat.test.17zuoye.net',
                    'admin.17zuoye.net': 'wechat.17zuoye.com',
                    'admin.dc.17zuoye.net': 'wechat.17zuoye.com',
                    'admin.staging.17zuoye.net': 'wechat.staging.17zuoye.net',
                }

                hostName = _map[hostName] ? _map[hostName] : hostName;

                if (hostName.indexOf('8085') > -1) {
                    hostName = hostName.replace(/8085/g, "8180")
                }
//                var url = "http://" + hostName + "/chips/center/activeServicePreview.vpage?qid=" + qid + "&bookId=" + _this.bookId + "&unitId=" + _this.unitId  ;
                var url = "http://" + hostName + "/chips/center/activeServicePreview.vpage?qid=" + qid + "&bookId=" + _this.resultBookId + "&unitId=" + _this.resultUnitId  ;
                console.log(url)
                var codeImgSrc = "https://www.17zuoye.com/qrcode?m=" + encodeURIComponent(url);

                var imgObj = new Image();
                imgObj.src = codeImgSrc;
                imgObj.style.width = "200px";
                imgObj.style.height = "200px";
                $("#sharecode_box").html('')
                $("#sharecode_box").append(imgObj);
                _this.model = true;
            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
        },
        created:function(){
            var _this = this;
            $.get("/chips/ai/todaylesson/getBooks.vpage",{
            }).then(function(res){
                if(res.success){
                    _this.bookList = res.data;
                }else{
                    alert(res.info)
                }
            });
            var method = "${method!}";
            if(method === "byQid"){
                var qid = "${qid!}"
                _this.qid = qid;
                _this.queryByQid();
            }
            if(method === "byLesson"){
                _this.clean = false;
                var bookId = "${bookId!}"
                var unitType = "${unitType!}"
                var unitId = "${unitId!}"
                var lessonId = "${lessonId!}"
                _this.bookId = bookId;
                _this.unitType = unitType;
                _this.unitId = unitId;
                _this.lessonId = lessonId;
                _this.getUnitTypes();
                _this.getUnits();
                _this.getLessons();
                _this.query();
            }
        },
    });
</script>
</@layout_default.page>