<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='图文素材管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<style>
    [v-cloak]{display: none}
</style>


<div id="main_container" class="span9" v-cloak>
    <legend>
        视频黑名单<button type="button" id="create" class="btn btn-primary pull-right" @click="create">新增</button>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>用户ID</td>
                        <td>用户名</td>
                        <td>更新时间</td>
                        <td>操作</td>
                    </tr>
                    <tr v-for="(item, index) in blackList" v-bind:key="index">
                        <td>{{ item.id }}</td>
                        <td>{{ item.userName }}</td>
                        <td>{{ moment(new Date(item.updateDate)).format("YYYY-MM-DD HH:mm:ss") }}</td>
                        <td>
                            <span class="btn btn-success" @click="del(item.id)">删除</span>
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
            model: false,
            userId:'',
            blackList:[]
        },
        methods:{
            del:function (id) {
                console.log("edit.id", id)
                $.post('/chips/video/black/delete.vpage', {
                    userId: id,
                }, function(res){
                    if(res.success){
                        alert("删除成功");
                        location.href = '/chips/video/black/list.vpage';
                    } else {
                        alert(res.info)
                    }
                });
            },
            create:function () {
                window.location.href = "/chips/video/black/editIndex.vpage"
            },
        },
        created: function () {
            var _this = this;
            $.get('${requestContext.webAppContextPath}/chips/video/black/listData.vpage', {

            }, function (res) {
                if(res.success) {
                    _this.blackList = res.blackList;
                }
            });
        }
    });
</script>

</@layout_default.page>