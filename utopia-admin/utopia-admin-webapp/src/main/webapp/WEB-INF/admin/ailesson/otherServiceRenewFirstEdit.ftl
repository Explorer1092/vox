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
        <h3>{{ name }} 续费提醒首次模板编辑</h3>
        <div class="well">
            <div class="control-group">
                <label class="control-label">定级报告介绍：</label>
                <div class="controls">
                    <textarea  v-model="pojo.openingRemarks" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">等级介绍：</label>
                <div class="controls">
                    <textarea  v-model="pojo.levelIntroduction" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">孩子成绩解读：</label>
                <div class="controls">
                    <textarea  v-model="pojo.scoreRemark" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">孩子本期成绩介绍：</label>
                <div class="controls">
                    <textarea  v-model="pojo.scoreIntroduction" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <template v-for="(item, i) in pojo.weekPointList">
                <div class="control-group">
                    <label class="control-label">薄弱点：</label>
                    <div class="controls">
                        <span  style="width: 600px;height:100px;">{{item.weekPointDesc}}</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">薄弱点解读：</label>
                    <div class="controls">
                        <textarea  v-model="item.remark" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">薄弱点提升：</label>
                    <div class="controls">
                        <textarea  v-model="item.promote" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">推课：</label>
                    <div class="controls">
                        <textarea  v-model="item.pushLesson" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}"  style="width: 600px;height:100px;"></textarea>
                    </div>
                </div>
            </template>
            <div class="control-group">
                <label class="control-label">定级样例视频：</label>
                <div class="controls">
                    <#--<textarea  v-model="pojo.levelVideo"  style="width: 600px;height:100px;"></textarea>-->
                    <div>
                        <#--<video v-if="pojo.levelVideo" v-bind:src="pojo.levelVideo" style="width: 300px;"></video>-->
                        <video v-if="pojo.levelVideo" v-bind:src="pojo.levelVideo" preload="auto" autoplay="autoplay" loop="loop" muted="muted" webkit-playsinline="true" playsinline="true" x5-playsinline="true" x5-video-player-type="h5" x5-video-player-fullscreen="false" x5-video-orientation="portraint"></video>

                    </div>
                    <div>
                        <label class="btn btn-primary">
                            <span v-if="pojo.levelVideo">修改视频</span><span v-else>上传视频</span>
                            <input type="file" accept=""  v-bind:id="'aliyunInput_0'" @change="upload(0)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="pojo.levelVideo"  class="btn btn-primary" @click="deleteAudio(0)">删除</span>
                        <span v-bind:id="'pptMsg_0'"></span>
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
            pojo:'',
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
            cancel:function () {
                var method = getUrlParam("method")
                console.log("cancel method: " + method)
                var hostName = window.location.host;
                console.log("http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&id=" + getUrlParam('id') + "&serviceType=" + getUrlParam('serviceType'));
                window.location.href = "http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&id=" + getUrlParam('id') + "&serviceType=" + getUrlParam('serviceType');
            },
            deleteAudio:function (index) {
                var _this = this;
                _this.pojo.levelVideo = "";
                $("#pptMsg_" + index).html("");
                $("#aliyunInput_" + index).val("");
            }
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
                renewType:getUrlParam("renewType"),
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
                    vm.pojo.levelVideo = "https://" + signResult.videoHost + ossPath;
                    console.log(vm.pojo.levelVideo)
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