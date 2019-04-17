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
        <h3>{{ name }}主动服务模板添加/编辑</h3>
        <div class="well">
            <template v-for="(item, i) in pojo.topItemList">
                <div v-if="item.type === 'text'" class="control-group">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div v-if="item.type === 'audio'" class="control-group">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary" v-bind:class="{disable: !microphoneEnable}">
                            <span v-if="microphoneEnable" v-on:click="onRecord('top_',i,'')" >{{ currentRecording != ('top_' + i) ? '开始录音' : '结束录音' }}</span>
                            <span v-if="!microphoneEnable">无法录音</span>
                        </label>
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-if="item.value">修改音频</span><span v-else>上传音频</span>
                            <input type="file" accept="audio/*"  v-bind:id="'top_aliyunInput_' + i" @change="upload('top_',i,'')" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="currentRecording === 'top_' + i">正在录音...</span>
                        <div v-bind:id="'top_record-audio-' + i"></div>
                        <span v-if="item.value"  class="btn btn-primary" @click="deleteAudio('top_',i,'')">删除</span>
                        <audio v-if="item.value" class="pull-right" v-bind:src="item.value" controls target="_blank" style="padding-right: 550px;"></audio>
                        <span v-bind:id="'top_pptMsg_' + i"></span>
                    </div>
                </div>
            </template>
        </div>
        <div class="well">
            <template v-for="(wp, j) in pojo.weekPointList">
                <div >{{ wp.weekPointDesc }}：</div>
                <div class="well">
                <template v-for="(item, i) in wp.wpItemList">
                    <div v-if="item.type === 'text'" class="control-group">
                        <label class="control-label">{{ item.name }}：</label>
                        <div class="controls">
                            <textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>
                        </div>
                    </div>
                    <div v-if="item.type === 'audio'" class="control-group">
                        <label class="control-label">{{ item.name }}：</label>
                        <div class="controls">
                            <label style="position: relative;" class="btn btn-primary" v-bind:class="{disable: !microphoneEnable}">
                                <span v-if="microphoneEnable" v-on:click="onRecord('wp_',i, j)" >{{ currentRecording != (j + 'wp_' + i) ? '开始录音' : '结束录音' }}</span>
                                <span v-if="!microphoneEnable">无法录音</span>
                            </label>
                            <label style="position: relative;" class="btn btn-primary">
                                <span v-if="item.value">修改音频</span><span v-else>上传音频</span>
                                <input type="file" accept="audio/*"  v-bind:id="j + 'wp_aliyunInput_' + i" @change="upload('wp_',i, j)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                            </label>
                            <span v-if="currentRecording === j + 'wp_' + i">正在录音...</span>
                            <div v-bind:id="j + 'wp_record-audio-' + i"></div>
                            <span v-if="item.value"  class="btn btn-primary" @click="deleteAudio('wp_',i, j)">删除</span>
                            <audio v-if="item.value" class="pull-right" v-bind:src="item.value" controls target="_blank" style="padding-right: 550px;"></audio>
                            <span v-bind:id="j + 'wp_pptMsg_' + i"></span>
                        </div>
                    </div>
                </template>
                </div>
            </template>
        </div>
        <div class="well">
            <template v-for="(item, i) in pojo.bottomItemList">
                <div v-if="item.type === 'text'" class="control-group">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div v-if="item.type === 'audio'" class="control-group">
                    <label class="control-label">{{ item.name }}：</label>
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary" v-bind:class="{disable: !microphoneEnable}">
                            <span v-if="microphoneEnable" v-on:click="onRecord('bottom_',i,'')" >{{ currentRecording != ('bottom_' + i) ? '开始录音' : '结束录音' }}</span>
                            <span v-if="!microphoneEnable">无法录音</span>
                        </label>
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-if="item.value">修改音频</span><span v-else>上传音频</span>
                            <input type="file" accept="audio/*"  v-bind:id="'bottom_aliyunInput_' + i" @change="upload('bottom_',i,'')" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="currentRecording === 'bottom_' + i">正在录音...</span>
                        <div v-bind:id="'bottom_record-audio-' + i"></div>
                        <span v-if="item.value"  class="btn btn-primary" @click="deleteAudio('bottom_',i,'')">删除</span>
                        <audio v-if="item.value" class="pull-right" v-bind:src="item.value" controls target="_blank" style="padding-right: 550px;"></audio>
                        <span v-bind:id="'bottom_pptMsg_' + i"></span>
                    </div>
                </div>
            </template>
        </div>
        <div style="margin-left: 600px">
            <span class="btn btn-success btn-large" @click="save">保存</span>
            <span class="btn btn-info btn-large" @click="cancel">返回</span>
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
            id:'',
            name:"",
            serviceType:"",
            currentRecording: null,
            audios: {},
            knowledgeList:[],
            summary:"",
            defaultSummary:"",
            microphoneEnable: true,
            pojo:[]
        },
        methods: {
            onRecord: function(prefix,index, j) {
                console.log("prefix:" + prefix + "; index : " + index + "; j:" + j)
                var thiz = this;
                if(!recorder) {
                    recorder = new _Recorder({initCallback: function(){
                        thiz.currentRecording = j + prefix + index;
                        document.getElementById(j + prefix + 'record-audio-' + index).innerHTML = '';
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
                            thiz.audios[j + prefix + index] = blob;
                            thiz.uploadAudioRecord(prefix,index, j)
                        });
                    }else {
                        this.currentRecording = j + prefix + index;
                        document.getElementById(j + prefix + 'record-audio-' + index).innerHTML = '';
                        recorder.start();
                    }
                }catch(err) {
                    alert('录音失败，建议使用Firefox浏览器，如已经是Firefox，请联系技术人员。' + prefix + ";" + err)
                }
            },
            uploadAudioRecord: function(prefix,index, j) {
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
                        console.log("thiz.audios", thiz.audios)
                        console.log("thiz.j + prefix + index",j + prefix + index)
                        var ossPath = signResult.dir + signResult.filename + ".mp3";
                        console.log("path:" + ossPath + ";file:" + thiz.audios[j + prefix + index])
                        store.multipartUpload(ossPath, thiz.audios[j + prefix + index]).then(function (result) {
                            console.log("https://" + signResult.videoHost + ossPath)
                            if(prefix == 'top_'){
                                vm.pojo.topItemList[index].value = "https://" + signResult.videoHost + ossPath;
                            } else if(prefix == 'bottom_'){
                                vm.pojo.bottomItemList[index].value = "https://" + signResult.videoHost + ossPath;
                            } else {
                                vm.pojo.weekPointList[j].wpItemList[index].value  = "https://" + signResult.videoHost + ossPath;
                            }
//                            console.log(vm.knowledgeList[index].value)
                            $("#" + j + prefix + "pptMsg_" + index).html("上传完成,保存后生效");
                        }).catch(function (err) {
                            $("#" + j + prefix + "pptMsg_" + index).html("上传失败,请重新选择文件");
                            console.log(err);
                        });
                    }
                });
            },
//            uploadAudio: function(prefix, index) {
//                var thiz = this;
//                $.ajax({
//                    url: "/chips/ai/todaylesson/getSignature.vpage",
//                    data: {
//                        ext: 'mp3'
//                    },
//                    type:"get",
//                    async: false,
//                    success:function (data) {
//                        var signResult = data.data;
//                        var store  = new OSS({
//                            accessKeyId: signResult.accessid,
//                            accessKeySecret: signResult.accessKeySecret,
//                            endpoint: signResult.endpoint,
//                            bucket: signResult.bucket
//                        });
//
//                        var ossPath = signResult.dir + signResult.filename + ".mp3";
//                        console.log("path:" + ossPath + ";file:" + thiz.audios[prefix + index])
//                        store.multipartUpload(ossPath, thiz.audios[prefix + index]).then(function (result) {
//                            console.log("https://" + signResult.videoHost + ossPath)
//                            if(prefix == 'top_'){
//                                vm.pojo.topItemList[index].audio = "https://" + signResult.videoHost + ossPath;
//                            } else if(prefix == 'bottom_'){
//                                vm.pojo.bottomItemList[index].audio = "https://" + signResult.videoHost + ossPath;
//                            } else {
//                                vm.knowledgeList[index].value = "https://" + signResult.videoHost + ossPath;
//                            }
////                            console.log(vm.knowledgeList[index].value)
//                            $("#" + prefix + "pptMsg_" + index).html("上传完成,保存后生效");
//                        }).catch(function (err) {
//                            $("#" + prefix + "pptMsg_" + index).html("上传失败,请重新选择文件");
//                            console.log(err);
//                        });
//                    }
//                });
//            },
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
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
            deleteAudio:function (prefix,index, j) {
                var _this = this;
//                _this.knowledgeList[index].value = "";
                if(prefix == 'top_') {
                    _this.pojo.topItemList[index].value = "";
                    $("#" + prefix + "pptMsg_" + index).html("");
                    $("#" + prefix + "aliyunInput_" + index).val("");
                } else if(prefix == 'bottom_'){
                    _this.pojo.bottomItemList[index].value = "";
                    $("#" + prefix + "pptMsg_" + index).html("");
                    $("#" + prefix + "aliyunInput_" + index).val("");
                } else {
                    _this.pojo.weekPointList[j].wpItemList[index].value = "";
                    $("#" + j + prefix + "pptMsg_" + index).html("");
                    $("#" + j + prefix + "aliyunInput_" + index).val("");
                }

            },
            addPronKeyword:function () {
                var _this = this;
                _this.pronList.push({keyword:"",comment:"",audio:""})
                console.log(_this.pronList)
            },
            addGramLevel:function () {
                var _this = this;
                _this.gramList.push({level:"",comment:"",audio:""})
                console.log(_this.gramList)
            },
            delPronKeyword:function (ind) {
                var _this = this;
                console.log(ind)
                _this.pronList.splice(ind, 1);
                console.log(_this.pronList)
            },
            delGramLevel:function (ind) {
                var _this = this;
                console.log(ind)
                _this.gramList.splice(ind, 1);
                console.log(_this.gramList)
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
                renewType:getUrlParam("renewType"),
            }, function (res) {
                if(res.success) {
//                    _this.knowledgeList = res.itemList;
                    _this.pojo = res.pojo;
                    console.log(_this.pojo.topItemList)
                }
            });
        }
    });

    function upload(prefix, ind,j) {
        console.log("upload-prefix", prefix)
        console.log("upload-ind", ind)
        console.log("upload-j", j)
        $("#" +j + prefix + "pptMsg_" + ind).html("上传中...");
        var file =  document.getElementById(j + prefix + "aliyunInput_" + ind).files[0];
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
                    console.log("vm.pojo.topItemList",vm.pojo.topItemList)
//                    vm.knowledgeList[ind].value = "https://" + signResult.videoHost + ossPath;
                    if(prefix == 'top_'){
                        vm.pojo.topItemList[ind].value = "https://" + signResult.videoHost + ossPath;
                    } else if(prefix == 'bottom_'){
                        vm.pojo.bottomItemList[ind].value = "https://" + signResult.videoHost + ossPath;
                    } else {
                        vm.pojo.weekPointList[j].wpItemList[ind].value = "https://" + signResult.videoHost + ossPath;
                    }
//                    console.log(vm.knowledgeList[ind].value)
                    $("#" +j + prefix + "pptMsg_" + ind).html("上传完成,保存后生效");
                }).catch(function (err) {
                    $("#" +j + prefix + "pptMsg_" + ind).html("上传失败,请重新选择文件");
                    console.log(err);
                });
            }
        });
    }
    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

</script>

</@layout_default.page>