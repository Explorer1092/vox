<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='视频黑名单' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<div id="main_container" class="span9">
    <legend>
        添加黑名单
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="control-group">
                    <label class="control-label">用户Id：</label>
                    <div class="controls">
                        <input v-model="userId">
                    </div>
                </div>
            </div>
            <span class="btn btn-success" @click="save()">添加</span>
            <span class="btn btn-cancel" @click="window.open('/chips/video/black/list.vpage');">取消</span>
        </div>
    </div>
    <#-- <input type="file" id="ck_image_upload"/> -->
</div>

<script type="text/javascript">
    var vm = new Vue({
        el: "#main_container",
        data: {
            userId: '',
        },
        methods: {
            save: function () {
                var _this = this;
                $.post('/chips/video/black/add.vpage', {
                    userId: _this.userId,
                }, function(res){
                    if(res.success){
                        alert("保存成功");
                        location.href = '/chips/video/black/list.vpage';
                    } else {
                        alert(res.info)
                    }
                });
            },
            getParams: function(name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return decodeURI(r[2]); return null;
            },
        },
        created: function () {
        },
    });

    
</script>

</@layout_default.page>