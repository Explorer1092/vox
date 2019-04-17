<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑模板" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js" xmlns="http://www.w3.org/1999/html"></script>
<script src="https://cdn.bootcss.com/lodash.js/4.17.11/lodash.core.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/recorder/record.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>-->
<#--<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">-->
<style>
    [v-cloak] {
        display: none;
    }

    .wd600 {
        width: 600px !important;
    }
    .disable {
        background: #555 !important;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3>{{ dataPrimary.name }}主动服务模板添加/编辑</h3>
        <div class="well">
            <template v-for="(item, i) in itemList">
                <div v-if="item.type === 'text'" class="control-group">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div v-if="item.type === 'url'" class="control-group">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary" v-bind:class="{disable: !microphoneEnable}">
                            <span v-if="microphoneEnable" v-on:click="onRecord(item.index)" >{{ currentRecording !== item.index ? '开始录音' : '结束录音' }}</span>
                            <span v-if="!microphoneEnable">无法录音</span>
                        </label>
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-if="item.value">修改音频</span><span v-else>上传音频</span>
                                <input type="file" accept="audio/*"  v-bind:id="'aliyunInput_' + item.index" @change="upload(item.index)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="currentRecording === item.index">正在录音...</span>
                        <div v-bind:id="'record-audio-' + item.index"></div>
                        <span v-if="item.value"  class="btn btn-primary" @click="deleteAudio(item.index)">删除</span>
                        <audio v-if="item.value" class="pull-right" v-bind:src="item.value" controls target="_blank" style="padding-right: 550px;"></audio>
                        <span v-bind:id="'pptMsg_' + item.index"></span>
                    </div>
                </div>
            </template>
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
    var recorder = null;
    var vm = new Vue({
        el: '#box',
        data: {
            itemList:[],
            dataPrimary:{
                qid:'',
                name:"",
            },
            currentRecording: null,
            audios: {},
            microphoneEnable: true
        },
        methods: {
            onRecord: function(index) {
                var thiz = this;
                if(!recorder) {
                     recorder = new _Recorder({initCallback: function(){
                        thiz.currentRecording = index;
                        document.getElementById('record-audio-' + index).innerHTML = '';
                        recorder.start();
                    } ,funCancel: function(msg) {
                        thiz.microphoneEnable = false;
                        alert(msg);
                    }});
                    return;
                }
                try{
                    if(this.currentRecording !== null) {
                        this.currentRecording = null;
                        recorder.stop();
                        recorder.getMp3Blob(function(blob) {
                            thiz.audios[index] = blob;
                            thiz.uploadAudioRecord(index)
                        });
                    }else {
                        this.currentRecording = index;
                        document.getElementById('record-audio-' + index).innerHTML = '';
                        recorder.start();
                    }
                }catch(err) {
                    alert('录音失败，建议使用Firefox浏览器，如已经是Firefox，请联系技术人员。')
                }

            },
            uploadAudioRecord: function(index) {
                var thiz = this;
                $.ajax({
                    url: "/chips/ai/todaylesson/getSignature.vpage",
                    data: {
                        ext: 'mp3'
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

                        var ossPath = signResult.dir + signResult.filename + ".mp3";
                        console.log("path:" + ossPath + ";file:" + thiz.audios[index])
                        store.multipartUpload(ossPath, thiz.audios[index]).then(function (result) {
                            console.log("https://" + signResult.videoHost + ossPath)
                            vm.itemList[index].value = "https://" + signResult.videoHost + ossPath;
                            console.log(vm.itemList[index].value)
                            $("#pptMsg_" + index).html("上传完成,保存后生效");
                        }).catch(function (err) {
                            $("#pptMsg_" + index).html("上传失败,请重新选择文件");
                            console.log(err);
                        });
                    }
                });
            },
            uploadAudio: function(index) {
                var thiz = this;
                $.ajax({
                    url: "/chips/ai/todaylesson/getSignature.vpage",
                    data: {
                        ext: 'mp3'
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

                        var ossPath = signResult.dir + signResult.filename + ".mp3";
                        console.log("path:" + ossPath + ";file:" + thiz.audios[index])
                        store.multipartUpload(ossPath, thiz.audios[index]).then(function (result) {
                            console.log("https://" + signResult.videoHost + ossPath)
                            vm.itemList[index].value = "https://" + signResult.videoHost + ossPath;
                            console.log(vm.itemList[index].value)
                            $("#pptMsg_" + index).html("上传完成,保存后生效");
                        }).catch(function (err) {
                            $("#pptMsg_" + index).html("上传失败,请重新选择文件");
                            console.log(err);
                        });
                    }
                });
            },
            save: function () {
                var _this = this;
                $.post('${requestContext.webAppContextPath}/chips/ai/todaylesson/activeServiceSave.vpage', {
                    dataJson: JSON.stringify(_this.itemList),
                    qid:_this.dataPrimary.qid
                }, function (res) {
                    if (res.success) {
                        _this.previewStatus = true;
                        alert("保存成功");
                    } else {
                        alert(res.info);
                    }
                });
            },
            cancel:function () {
                var method = "${method!}"
                console.log("cancel method: " + method)
                var hostName = window.location.host;
                console.log("http://" + hostName +"/chips/ai/todaylesson/activeServiceIndex.vpage?method=" + method
                        + "&qid=${qid!}&bookId=${bookId!}&unitType=${unitType!}&unitId=${unitId!}&lessonId=${lessonId!}");
                    window.location.href = "http://" + hostName +"/chips/ai/todaylesson/activeServiceIndex.vpage?method=" + method
                            + "&qid=${qid!}&bookId=${bookId!}&unitType=${unitType!}&unitId=${unitId!}&lessonId=${lessonId!}";
            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
            deleteAudio:function (index) {
                var _this = this;
                _this.itemList[index].value = "";
                $("#pptMsg_" + index).html("");
                $("#aliyunInput_" + index).val("");
            }
        },
        created: function () {
            var _this = this;
            var qid = "${qid!}";
            console.log("qid")
            console.log(qid)
            $.get('${requestContext.webAppContextPath}/chips/ai/todaylesson/activieServiceQuery.vpage', {
                qid: qid
            }, function (res) {
                if(res.success) {
                    _this.itemList = res.itemList;
                    _this.dataPrimary.name = res.name;
                    _this.dataPrimary.qid = res.qid;
                    console.log(_this.itemList)
                    console.log(_this.dataPrimary)
                }
            });
        }
    });

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
                    vm.itemList[ind].value = "https://" + signResult.videoHost + ossPath;
                    console.log(vm.itemList[ind].value)
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