<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='图文素材管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>

<style>
    [v-cloak]{display: none}
</style>


<div id="main_container" class="span9" v-cloak>
    <legend>
        图文素材管理<button type="button" id="create" class="btn btn-primary pull-right" @click="create">添加</button>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>文章ID</td>
                        <td>文章标题</td>
                        <td>查看次数</td>
                        <td>更新时间</td>
                        <td>操作</td>
                    </tr>
                    <tr v-for="(item, index) in articleList" v-bind:key="index">
                        <td>{{ item.id }}</td>
                        <td>{{ item.title }}</td>
                        <td>{{ item.num }}</td>
                        <td>{{ item.updateTime }}</td>
                        <td>
                            <span class="btn btn-success" @click="edit(item.id)">编辑</span>
                            <span class="btn btn-success" @click="preview(item.id)">预览</span>
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
</div>

<script type="text/javascript">
    var vm = new Vue({
        el:"#main_container",
        data:{
            model: false,
            userId:'',
            articleList:[]
        },
        methods:{
            edit:function (id) {
                console.log("edit.id", id)
                window.location.href = "/chips/studyInfo/editIndex.vpage?id=" + id;
            },
            preview:function (id) {
                    console.log("preview-id: " + id)
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
                    var url = "http://" + hostName + "/chipsv2/center/study_information.vpage?articleId=" + id;
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
            create:function () {
                window.location.href = "/chips/studyInfo/editIndex.vpage"
            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
        },
        created: function () {
            var _this = this;
            $.get('${requestContext.webAppContextPath}/chips/studyInfo/listData.vpage', {

            }, function (res) {
                if(res.success) {
                    _this.articleList = res.articleList;
                }
            });
        }
    });
</script>

</@layout_default.page>