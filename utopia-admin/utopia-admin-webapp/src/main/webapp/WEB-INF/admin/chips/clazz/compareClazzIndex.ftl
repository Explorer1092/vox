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
</style>
<div id="userContainer" class="span9">
    <legend>数据监控列表页
        <button type="button" id="create" class="btn btn-primary pull-right" @click="create">添加</button>
    </legend>
    <div class="row">
        <div class="span12 well">
            <table class="table table-condensed  table-hover table-striped table-bordered">
                <thead>
                    <tr>
                        <td style="width: 20%">标题</td>
                        <td style="width: 50%">对比班级</td>
                        <td style="width: 20%">基线班级</td>
                        <td style="width: 10%">操作</td>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="clazzCompareList.length <= 0">
                        <td colspan="4" align="center">暂无数据</td>
                    </tr>
                    <tr v-else v-for="(item, index) in clazzCompareList">
                       <td>{{ item.title }}</td>
                       <td>{{ item.compareClazzNames }}</td>
                       <td>{{ item.basicClazzName }}</td>
                       <td>
                           <label style="position: relative;" class="btn btn-primary">
                               <span v-on:click="edit(item.id)">编辑</span>
                           </label>
                           <label style="position: relative;" class="btn btn-primary">
                               <span v-on:click="entry(item.id)">进入</span>
                           </label>
                           <label style="position: relative;" class="btn btn-primary">
                               <span v-on:click="del(item.id)">删除</span>
                           </label>
                       </td>
                    </tr>
                    <tr v-if="clazzCompareList.length > 0">
                        <td colspan="3">
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
            clazzCompareList:[],
            currentPage: 1,
            totalPages: 0,
        },
        methods: {
            // 筛选
            // 用户查询按钮点击
            // 上一页
            prevPage: function () {
                this.currentPage = this.currentPage - 1;
                var _this = this;
                console.log("currentPage",_this.currentPage)
                $.get('${requestContext.webAppContextPath}/chips/clazz/monitor/compareClazzList.vpage', {
                    pageNum:_this.currentPage
                }, function (res) {
                    _this.clazzCompareList = res.clazzCompareList
                    _this.totalPages = res['totalPage'];
                });
            },
            // 下一页
            nextPage: function () {
                this.currentPage = this.currentPage + 1;
                var _this = this;
                console.log("currentPage",_this.currentPage)
                $.get('${requestContext.webAppContextPath}/chips/clazz/monitor/compareClazzList.vpage', {
                    pageNum:_this.currentPage
                }, function (res) {
                    _this.clazzCompareList = res.clazzCompareList
                    _this.totalPages = res['totalPage'];
                });
            },
            create:function () {
                window.location.href = "/chips/clazz/monitor/compareClazzEdit.vpage"
            },
            edit:function (id) {
                console.log("id",id)
                window.location.href = "/chips/clazz/monitor/compareClazzEdit.vpage?id=" + id;
            },
            entry:function (id) {
                console.log("id",id)
                window.location.href = "/chips/clazz/monitor/compareClazzDetailIndex.vpage?id=" + id;
            },
            del:function (id) {
                console.log("delete-id", id);
                var msg = confirm("确定删除？")
                if(msg == true) {
                    $.get('${requestContext.webAppContextPath}/chips/clazz/monitor/compareClazzListDel.vpage', {
                        "id":id
                    }, function (res) {
                        if(res.success){
                            window.location.reload();
                        } else {
                            alert(res.info)
                        }
                    });
                } else {

                }
            }
        },
        created: function () {
            var _this = this;
            $.get('${requestContext.webAppContextPath}/chips/clazz/monitor/compareClazzList.vpage', {
                pageNum:1
            }, function (res) {
               _this.clazzCompareList = res.clazzCompareList
               _this.totalPages = res['totalPage'];
            });
        }
    });

</script>
</@layout_default.page>
