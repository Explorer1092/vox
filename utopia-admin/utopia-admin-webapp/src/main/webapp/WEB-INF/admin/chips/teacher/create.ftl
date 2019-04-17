<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='新建薯条英语教师' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        新建\编辑&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div id="box" class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" action="save.vpage" method="post">
                <#--<input id="productId" name="id" value="${id!}" type="hidden" />-->
                    <div class="form-horizontal">
                        <input type="hidden" v-model="teacher.id">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">姓名：</span></label>
                            <div class="controls">
                                <input type="text" v-model="teacher.name" class="form-control input_txt"/>
                            </div>
                        </div>
                        <#--<div class="control-group">-->
                            <#--<label class="col-sm-2 control-label">微信号：</span></label>-->
                            <#--<div class="controls">-->
                                <#--<input type="text" v-model="teacher.wxCode" class="form-control input_txt"/>-->
                            <#--</div>-->
                        <#--</div>-->
                        <#--<div class="control-group">-->
                            <#--<label class="col-sm-2 control-label">二维码：</span></label>-->
                            <#--<div class="controls">-->
                                <#--<input type="text" v-model="teacher.qrImage" class="form-control input_txt"/>-->
                            <#--</div>-->
                        <#--</div>-->


                        <div class="control-group">
                            <label class="col-sm-2 control-label">头像：</label>
                            <div class="controls">
                                <label style="position: relative;" class="btn btn-primary">
                                    <span v-if="teacher.headPortrait"><image v-bind:src="teacher.headPortrait" style="width: 200px;height: 200px"></image></span><span v-else>上传头像</span>
                                    <input type="file" accept=""  id="aliyunInput" @change="upload(event)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>

                                </label>
                                <span id="pptMsg"></span>
                            </div>
                        </div>
                    </div>
                    <div style="margin-left: 500px">
                        <span class="btn btn-success btn-large" @click="save">保存</span>
                        <span class="btn btn-info btn-large" @click="cancel">返回</span>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    var vm = new Vue({
        el: '#box',
        data: {
            teacher:{
                id:'',
                name:"",
                wxCode:"",
                qrImage:"",
                headPortrait:"",
            },
        },

        methods: {
            save: function () {
                var _this = this;
                console.log(_this.teacher)
                $.post('${requestContext.webAppContextPath}/chips/ai/teacher/save.vpage', {
                    id:_this.teacher.id,
                    name:_this.teacher.name,
                    wxCode:_this.teacher.wxCode,
                    qrImage:_this.teacher.qrImage,
                    headPortrait:_this.teacher.headPortrait,
                }, function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.history.back();
                        window.location.reload();
                    } else {
                        alert("保存失败:" + res.info);
                    }
                });
            },
            cancel:function () {
                window.history.back();
            }
        },
        created: function () {
            var _this = this;
            $.get('${requestContext.webAppContextPath}/chips/ai/teacher/query.vpage', {
                id: "${id!}",
            }, function (res) {
                if(res.success) {

                    _this.teacher.id = res.teacher.id;
                    _this.teacher.name = res.teacher.name;
                    _this.teacher.wxCode = res.teacher.wxCode;
                    _this.teacher.qrImage = res.teacher.qrImage;
                    _this.teacher.headPortrait = res.teacher.headPortrait;
                    console.log(res)
                    console.log(_this.teacher)

                }
            });
        }
    });

    function upload(e) {
        console.log("method upload is called")
        console.log(vm.itemList);
        $("#pptMsg").html("上传中...");
        var file = e.target.files[0];
        var fileOriginName = file.name;
        var index = fileOriginName.lastIndexOf(".");
        var ext = fileOriginName.substring(index + 1, fileOriginName.length);
        console.log("ssssss")
        console.log(file)
        $.ajax({
            url: "/chips/ai/todaylesson/getSignature.vpage",
            data: {
                ext: ext
            },
            type:"get",
            async: false,
            success:function (data) {
                var signResult = data.data;
                let store  = new OSS({
                    accessKeyId: signResult.accessid,
                    accessKeySecret: signResult.accessKeySecret,
                    endpoint: signResult.endpoint,
                    bucket: signResult.bucket
                });

                var ossPath = signResult.dir + signResult.filename + "." + ext;
                store.multipartUpload(ossPath, file).then(function () {
                    console.log("https://" + signResult.videoHost + ossPath)
                    vm.teacher.headPortrait = "https://" + signResult.videoHost + ossPath;
                    console.log( vm.teacher.headPortrait)
                    $("#pptMsg").html("");
//                    $("#pptMsg").html("上传完成,保存后生效");
                }).catch(function (err) {
                    $("#pptMsg").html("上传失败,请重新选择文件");
                    console.log(err);
                });
            }
        });
    }
</script>
</@layout_default.page>