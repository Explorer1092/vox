<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑模板" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js" xmlns="http://www.w3.org/1999/html"></script>
<script src="https://cdn.bootcss.com/lodash.js/4.17.11/lodash.core.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/recorder/record.js"></script>
<style>
    [v-cloak] {
        display: none;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3>{{ name }} 主动服务模板添加/编辑</h3>
        <div class="well">
            <div class="control-group">
                <label class="control-label">大礼包介绍：</label>
                <div class="controls">
                    <textarea  v-model="pojo.giftPackIntroduce" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">大礼包介绍图片：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.giftPackImage" v-bind:src="pojo.giftPackImage" style="width: 300px;">
                    </div>
                    <div>
                        <label class="btn btn-primary">
                            <span v-if="pojo.giftPackImage">修改图片</span><span v-else>上传图片</span>
                            <input type="file" accept=""  v-bind:id="'aliyunInput_gift'" @change="upload('gift')" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="pojo.giftPackImage"  class="btn btn-primary" @click="deleteAudio('gift')">删除</span>
                        <span id="pptMsg_gift"></span>
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">优惠券介绍：</label>
                <div class="controls">
                    <textarea  v-model="pojo.couponIntroduce" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">优惠券介绍图片：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.couponImage" v-bind:src="pojo.couponImage" style="width: 300px;">
                    </div>
                    <div>
                        <label class="btn btn-primary">
                            <span v-if="pojo.couponImage">修改图片</span><span v-else>上传图片</span>
                            <input type="file" accept=""  v-bind:id="'aliyunInput_coupon'" @change="upload('coupon')" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="pojo.couponImage"  class="btn btn-primary" @click="deleteAudio('coupon')">删除</span>
                        <span id="pptMsg_coupon"></span>
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">拼团介绍：</label>
                <div class="controls">
                    <textarea  v-model="pojo.groupIntroduce" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">拼团介绍图片：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.groupImage" v-bind:src="pojo.groupImage" style="width: 300px;">
                    </div>
                    <div>
                        <label class="btn btn-primary">
                            <span v-if="pojo.groupImage">修改图片</span><span v-else>上传图片</span>
                            <input type="file" accept=""  v-bind:id="'aliyunInput_group'" @change="upload('group')" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="pojo.groupImage"  class="btn btn-primary" @click="deleteAudio('group')">删除</span>
                        <span id="pptMsg_group"></span>
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">紧迫性介绍：</label>
                <div class="controls">
                    <textarea  v-model="pojo.urgencyIntroduct" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">购买链接：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.buyLink" v-bind:src="pojo.buyLink" style="width: 300px;">
                    </div>
                    <div>
                        <label class="btn btn-primary">
                            <span v-if="pojo.buyLink">修改图片</span><span v-else>上传图片</span>
                            <input type="file" accept=""  v-bind:id="'aliyunInput_buyLink'" @change="upload('buyLink')" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="pojo.buyLink"  class="btn btn-primary" @click="deleteAudio('buyLink')">删除</span>
                        <span id="pptMsg_buyLink"></span>
                    </div>
                </div>
            </div>
            <div style="margin-left: 600px">
                <span class="btn btn-success btn-large" @click="save">保存</span>
                <span class="btn btn-info btn-large" @click="cancel">返回</span>
            </div>
        </div>
    </div>
</div>

<div class="layer loading_layer" id="loading_layer"></div>
<div class="loading" id="loading"></div>
<script type="text/javascript">
    var vm = new Vue({
        el: '#box',
        data: {
            pojo:"",
            id:"",
            name:"",
            serviceType:"",
        },
        methods: {
            save: function () {
                var _this = this;
                console.log(_this.pojo)
                console.log("json: " + JSON.stringify(_this.pojo));
                $.post('${requestContext.webAppContextPath}/chips/ai/active/service/otherServiceTypeSave.vpage', {
                    json: JSON.stringify(_this.pojo),
                    id: _this.id,
                    name :_this.name,
                    serviceType : _this.serviceType,
                }, function (res) {
                    if (res.success) {
                        alert("保存成功");
                    } else {
                        alert(res.info);
                    }
                });
            },
            deleteAudio:function (index) {
                var _this = this;
                if(index == "gift"){
                    _this.pojo.giftPackImage = ""
                }
                if(index == "coupon"){
                    _this.pojo.couponImage = ""
                }
                if(index == "group"){
                    _this.pojo.groupImage = ""
                }
                if(index == "buyLink"){
                    _this.pojo.buyLink = ""
                }
                $("#pptMsg_" + index).html("");
                $("#aliyunInput_" + index).val("");
            },
            cancel:function () {
                var method = getUrlParam("method")
                console.log("cancel method: " + method)
                var hostName = window.location.host;
                console.log("http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&id=" + getUrlParam('id') + "&serviceType=" + getUrlParam('serviceType'));
                window.location.href = "http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&id=" + getUrlParam('id') + "&serviceType=" + getUrlParam('serviceType');
            },

        },
        created: function () {
            var _this = this;
            var name = getUrlParam("name");
            _this.name = name;
            console.log(name)
            _this.serviceType = getUrlParam("serviceType");
            var id = getUrlParam("id");
            _this.id = id;
            $.get('${requestContext.webAppContextPath}/chips/ai/active/service/queryOtherServiceType.vpage', {
                id: id,
                serviceType: getUrlParam("serviceType"),
                name:getUrlParam("name"),
            }, function (res) {
                if(res.success) {
                    _this.pojo = res.pojo;
                    _this.name = name;
                }
            });
        }
    });

    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]); return null;
    }


    function upload(ind) {
        $("#pptMsg_" + ind).html("上传中...");
        var file =  document.getElementById("aliyunInput_" + ind).files[0];
        console.log(file)
        var fileOriginName = file.name;
        var index = fileOriginName.lastIndexOf(".");
        var ext = fileOriginName.substring(index + 1, fileOriginName.length);
        $.ajax({
            url: "/chips/ai/todaylesson/getSignature.vpage",
            data: {
                ext: ext
            },
            type:"get",
            async: false,
            success:function (data) {
                var signResult = data.data;
                var store  = new OSS({
                    accessKeyId: signResult.accessid,
                    accessKeySecret: signResult.accessKeySecret,
                    endpoint: signResult.endpoint,
                    bucket: signResult.bucket
                });

                var ossPath = signResult.dir + signResult.filename + "." + ext;
                console.log("path:" + ossPath + ";file:" + file)
                store.multipartUpload(ossPath, file).then(function (result) {
                    console.log("https://" + signResult.videoHost + ossPath)
                    var imageUrl = "https://" + signResult.videoHost + ossPath;
                    if(ind == "gift"){
                        vm.pojo.giftPackImage = imageUrl;
                    }
                    if(ind == "coupon"){
                        vm.pojo.couponImage = imageUrl;
                    }
                    if(ind == "group"){
                        vm.pojo.groupImage = imageUrl;
                    }
                    if(ind == "buyLink"){
                        vm.pojo.buyLink = imageUrl;
                    }
                    $("#pptMsg_" + ind).html("上传完成,保存后生效");
                }).catch(function (err) {
                    $("#pptMsg_" + ind).html("上传失败,请重新选择文件");
                    console.log(err);
                });
            }
        });
    }

</script>

</@layout_default.page>