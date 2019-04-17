<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条英语课程管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>

<style>
    [v-cloak]{display: none}
</style>


<div id="main_container" class="span9" v-cloak>
    <legend>
        用户课程查询首页
    </legend>

    <div class="row-fluid">
        <div class="span12" >
            <div class="well" style="font-size: 12px;">
                <div class="form-inline">
                    <input v-model="userId" class="form-control" type="text" placeholder="请输入用户ID">
                    <button class="btn btn-success" @click="submit">确定</button>
                </div>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>教材名称</td>
                        <td>产品名称</td>
                        <td>状态</td>
                        <td>开始时间</td>
                        <td>结束时间</td>
                        <td>操作</td>
                    </tr>
                    <tr v-for="(item, index) in data_list" v-bind:key="index">
                        <td>{{ item.bookName }}</td>
                        <td>{{ item.productName }}</td>
                        <td>{{ item.status }}</td>
                        <td>{{ item.serviceBeginDate }}</td>
                        <td>{{ item.serviceEndDate }}</td>
                        <td>
                            <button class="btn btn-primary">操作</button>
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
            userId:'',
            data_list:[]
        },
        methods:{
            submit:function(){
                var _this = this;
                if(_this.userId){
                    $.post('/chips/course/list.vpage',{
                        userId:_this.userId
                    }).then(function(res){
                        _this.data_list = res.data;
                    })
                }else{
                    alert("请输入id")
                }
            }
        }
    });
</script>

</@layout_default.page>