<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="编辑用户模板" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
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
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <#--<h3 class="h3 text-center"><#if     id?? && id != ''>编辑<#else>添加</#if></h3>-->
        <h3>{{ dataPrimary.name }}主动服务模板编辑</h3>
        <div class="well">
                <div v-if="userAnswer != ''" class="control-group">
                    <label class="control-label">{{ userAnswer.name }}：</label>
                    <div v-if="userAnswer.type === 'video'">
                        <#--<video  class="pull-left" v-bind:src="userAnswer.value"  target="_blank" style="margin-left: 360px; height: 400px"></video>-->
                        <video  class="pull-left"  preload="auto" autoplay="autoplay" loop="loop" muted="muted"  v-bind:src="userAnswer.value"
                                style="margin-left: 360px; height: 400px"></video>
                    </div>
                    <div v-else  class="controls">
                        <audio  class="pull-left" v-bind:src="userAnswer.value" controls target="_blank" style="margin-left:330px;"></audio>
                    </div>

                </div>
            <template v-for="(item, i) in itemList">


                <div v-if="item.type === 'text'" class="control-group">
                    <input type="checkbox" v-model="item.checkBox">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div v-if="item.type === 'url'" class="control-group">
                    <input type="checkbox" v-model="item.checkBox">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-on:click="onRecord(item.index,i)" >{{ currentRecording !== item.index ? '开始录音' : '结束录音' }}</span>
                        </label>
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-if="item.value">修改音频</span><span v-else>上传音频</span>
                            <input type="file" accept="audio/*"  v-bind:id="'aliyunInput_' + item.index" @change="upload(item.index,i)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="currentRecording === item.index">正在录音...</span>
                        <div v-bind:id="'record-audio-' + item.index"></div>
                        <span v-if="item.value"  class="btn btn-primary" @click="deleteAudio(item.index,i)">删除</span>

                        <audio v-if="item.value" class="pull-right" v-bind:src="item.value" controls target="_blank" style="padding-right: 550px;"></audio>
                        <span v-bind:id="'pptMsg_' + item.index"></span>
                    </div>
                </div>
            </template>
            <div style="margin-left: 500px">
                <#--<span class="btn btn-success btn-large" @click="save">保存</span>-->
                <span class="btn btn-success btn-large" @click="preview">预览</span>
                <span class="btn btn-info btn-large" @click="cancel">返回</span>
                <span class="btn btn-info btn-large" @click="recover">恢复默认模板</span>
            </div>
        </div>

        <hr>
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
    var recorder = null;
    var vm = new Vue({
        el: '#box',
        data: {
            model: false,
            itemList:[],
            userAnswer:"",
//            model: false,
            dataPrimary:{
                qid:'',
                userId:"",
            },
            currentRecording: null,
            audios: {}
        },

        methods: {
            save: function () {
                var _this = this;
                qid = _this.dataPrimary.qid,
                userId = _this.dataPrimary.userId,
                bookId = "${bookId!}";
                unitId = "${unitId!}";
                console.log(JSON.stringify(_this.itemList))
                $.post('${requestContext.webAppContextPath}/chips/ai/todaylesson/userTemplateSave.vpage', {
                    dataJson: JSON.stringify(_this.itemList),
                    qid:qid,
                    userId:userId,
                    bookId:bookId,
                    unitId:unitId,
                }, function (res) {
                    if (res.success) {
                        alert("保存成功!");
                    } else {
                        alert(res.info);
                    }
                });
            },
            preview:function () {
                var _this = this;
                qid = _this.dataPrimary.qid,
                userId = _this.dataPrimary.userId,
                bookId = "${bookId!}";
                unitId = "${unitId!}";
                console.log(JSON.stringify(_this.itemList))
                $.post('${requestContext.webAppContextPath}/chips/ai/todaylesson/userTemplateSave.vpage', {
                    dataJson: JSON.stringify(_this.itemList),
                    qid:qid,
                    userId:userId,
                    bookId:bookId,
                    unitId:unitId,
                    aid:"${aid! ''}",
                    lessonId : "${lessonId! ''}"
                }, function (res) {
                    if (res.success) {
                        var url = res.url;
                        console.log(url)
                        var codeImgSrc = "https://www.17zuoye.com/qrcode?m=" + encodeURIComponent(url);

                        var imgObj = new Image();
                        imgObj.src = codeImgSrc;
                        imgObj.style.width = "200px";
                        imgObj.style.height = "200px";
                        $("#sharecode_box").html('');
                        $("#sharecode_box").append(imgObj);
                        _this.model = true;
                    } else {
                        alert(res.info);
                    }
                });

            },
            cancel:function () {
                _this = this;
                var method = "${bookId!}"
                console.log("cancel method: " + method)
                var hostName = window.location.host;

                console.log( "http://" + hostName + "/chips/user/question/index.vpage?userId=" + _this.dataPrimary.userId + "&bookId=${bookId!}&unitId=${unitId!}&lessonId=${lessonId!}");
                window.location.href =  "http://" + hostName + "/chips/user/question/index.vpage?userId=" + _this.dataPrimary.userId + "&bookId=${bookId!}&unitId=${unitId!}&lessonId=${lessonId!}";
            },
            recover:function () {
                var _this = this;
                var qid = "${qid!}";
                var userId = "${userId!}";
                var unitId = "${unitId!}";
                var lessonId = "${lessonId!}";
                var aid = "${aid!}";
                $.get('${requestContext.webAppContextPath}/chips/ai/todaylesson/userTemplateDelete.vpage', {
                    qid: qid,
                    userId:userId,
                }, function (re) {
                    console.log(111)
                    if(re.success){
                        $.get('${requestContext.webAppContextPath}/chips/ai/todaylesson/userTemplateQuery.vpage', {
                            qid: qid,
                            userId:userId,
                            unitId:unitId,
                            lessonId:lessonId,
                            aid:aid
                        }, function (res) {
                            if(res.success) {
                                _this.itemList = res.itemList;
                                _this.dataPrimary.name = res.name;
                                _this.dataPrimary.qid = qid;
                                _this.dataPrimary.userId = userId;

                                console.log(_this.dataJson)
                                console.log(_this.dataCheckBox)
                            }
                        });
                    }
                });
            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
            deleteAudio:function (index,ind) {
                var _this = this;
                _this.itemList[ind].value = "";
                $("#pptMsg_" + index).html("");
                $("#aliyunInput_" + index).val("");
            },
            onRecord: function(index, ind) {
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
                            thiz.audios[ind] = blob;
                            thiz.uploadAudioRecord(index,ind)
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
            uploadAudioRecord: function(index,ind) {
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
                        console.log("path:" + ossPath + ";file:" + thiz.audios[ind])
                        store.multipartUpload(ossPath, thiz.audios[ind]).then(function (result) {
                            console.log("https://" + signResult.videoHost + ossPath)
                            vm.itemList[ind].value = "https://" + signResult.videoHost + ossPath;
                            console.log(vm.itemList[ind].value)
                            $("#pptMsg_" + index).html("上传完成,保存后生效");
                        }).catch(function (err) {
                            $("#pptMsg_" + index).html("上传失败,请重新选择文件");
                            console.log(err);
                        });
                    }
                });
            },
        },
        created: function () {
            var _this = this;
            var qid = "${qid!}";
            var userId = "${userId!}";
            var unitId = "${unitId!}";
            var lessonId = "${lessonId!}";
            var aid = "${aid!}";
            console.log("qid")
            console.log(qid)
            console.log(userId)
            $.get('${requestContext.webAppContextPath}/chips/ai/todaylesson/userTemplateQuery.vpage', {
                qid: qid,
                userId:userId,
                unitId:unitId,
                lessonId:lessonId,
                aid:aid
            }, function (res) {
                if(res.success) {
                    _this.itemList = res.itemList;
                    _this.dataPrimary.name = res.name;
                    _this.dataPrimary.qid = qid;
                    _this.dataPrimary.userId = userId;
                    if(res.userAnswer != null){
                        _this.userAnswer = res.userAnswer;
                    }

                    console.log(_this.itemList)
                    console.log(_this.dataPrimary)
                }
            });
        }
    });

    function upload(ind,i) {
        console.log("method upload is called")
        console.log(ind)
        console.log(vm.itemList);
        $("#pptMsg_" + ind).html("上传中...");
//        var file = e.target.files[0];
        var file =  document.getElementById("aliyunInput_" + ind).files[0];
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
                var store  = new OSS({
                    accessKeyId: signResult.accessid,
                    accessKeySecret: signResult.accessKeySecret,
                    endpoint: signResult.endpoint,
                    bucket: signResult.bucket
                });

                var ossPath = signResult.dir + signResult.filename + "." + ext;
                store.multipartUpload(ossPath, file).then(function () {
                    console.log("https://" + signResult.videoHost + ossPath)
                    vm.itemList[i].value = "https://" + signResult.videoHost + ossPath;
                    console.log(vm.itemList[i].value)
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