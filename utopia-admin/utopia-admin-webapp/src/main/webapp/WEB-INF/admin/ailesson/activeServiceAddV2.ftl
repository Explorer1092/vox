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
            <div class="control-group">
                <label class="control-label">学习情况总结：</label>
                <div class="controls">
                    <textarea v-model="summary" title="{学生姓名}使用{userName},{该题的题目字段内容}使用{question},{本题需主动服务的重点词}使用{keyword}进行替换" style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">保底总结：</label>
                <div class="controls">
                    <textarea v-model="defaultSummary" title="{学生姓名}使用{userName},{该题的题目字段内容}使用{question},{本题需主动服务的重点词}使用{keyword}进行替换" style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
        </div>
        <div class="well">
            <template v-for="(item, i) in pronList" :key="i">
                <div class="control-group">
                    <label class="control-label">关键词：</label>
                    <div class="controls">
                        <textarea v-model="item.keyword"  style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">发音点评：</label>
                    <div class="controls">
                        <textarea v-model="item.comment" style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">发音点评音频：</label>
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary" v-bind:class="{disable: !microphoneEnable}">
                            <span v-if="microphoneEnable" v-on:click="onRecord('pron-',i)" >{{ currentRecording !== 'pron-'+i ? '开始录音' : '结束录音' }}</span>
                            <span v-if="!microphoneEnable">无法录音</span>
                        </label>
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-if="item.audio">修改音频</span><span v-else>上传音频</span>
                            <input type="file" accept="audio/*"  v-bind:id="'pron-aliyunInput_' + i" @change="upload('pron-',i)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="currentRecording == 'pron-'+i">正在录音...</span>
                        <div v-bind:id="'pron-record-audio-' + i"></div>
                        <span v-if="item.audio"  class="btn btn-primary" @click="deleteAudio('pron-',i)">删除</span>
                        <audio v-if="item.audio" class="pull-right" v-bind:src="item.audio" controls target="_blank" style="padding-right: 550px;"></audio>
                        <span v-bind:id="'pron-pptMsg_' + i"></span>
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-on:click="delPronKeyword(i)">删除关键词</span>
                        </label>
                    </div>
                </div>
            </template>
            <div class="control-group">
                <div class="controls">
                    <label style="position: relative;" class="btn btn-primary">
                        <span v-on:click="addPronKeyword()" >添加关键词</span>
                    </label>
                </div>
            </div>
        </div>
        <div class="well">
            <template v-for="(item, i) in gramList" :key="i">
                <div class="control-group">
                    <label class="control-label">Level：</label>
                    <div class="controls">
                        <select v-model="item.level">
                            <option value="">请选择</option>
                            <option value="A">A</option>
                            <option value="B">B</option>
                            <option value="C">C</option>
                            <option value="D">D</option>
                            <option value="E,F,F1,F2,F3,F4">E,F,F1,F2,F3,F4</option>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">语法点讲解：</label>
                    <div class="controls">
                        <textarea v-model="item.comment" style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">语法点讲解音频：</label>
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary" v-bind:class="{disable: !microphoneEnable}">
                            <span v-if="microphoneEnable" v-on:click="onRecord('gram-',i)" >{{ currentRecording !== 'gram-'+i ? '开始录音' : '结束录音' }}</span>
                            <span v-if="!microphoneEnable">无法录音</span>
                        </label>
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-if="item.audio">修改音频</span><span v-else>上传音频</span>
                            <input type="file" accept="audio/*"  v-bind:id="'gram-aliyunInput_' + i" @change="upload('gram-',i)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="currentRecording == 'gram-'+i">正在录音...</span>
                        <div v-bind:id="'gram-record-audio-' + i"></div>
                        <span v-if="item.audio"  class="btn btn-primary" @click="deleteAudio('gram-',i)">删除</span>
                        <audio v-if="item.audio" class="pull-right" v-bind:src="item.audio" controls target="_blank" style="padding-right: 550px;"></audio>
                        <span v-bind:id="'gram-pptMsg_' + i"></span>
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-on:click="delGramLevel(i)" >删除Level</span>
                        </label>
                    </div>
                </div>
            </template>
            <div class="control-group">
                <div class="controls">
                    <label style="position: relative;" class="btn btn-primary">
                        <span v-on:click="addGramLevel()" >添加Level</span>
                    </label>
                </div>
            </div>
        </div>
        <div class="well">
            <template v-for="(item, i) in knowledgeList">
                <div v-if="item.type === 'text'" class="control-group">
                    <label class="control-label">{{ item.key }}：</label>
                    <div class="controls">
                        <textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div v-if="item.type === 'audio'" class="control-group">
                    <label class="control-label">{{ item.key }}：</label>
                    <div class="controls">
                        <label style="position: relative;" class="btn btn-primary" v-bind:class="{disable: !microphoneEnable}">
                            <span v-if="microphoneEnable" v-on:click="onRecord('',i)" >{{ currentRecording != i ? '开始录音' : '结束录音' }}</span>
                            <span v-if="!microphoneEnable">无法录音</span>
                        </label>
                        <label style="position: relative;" class="btn btn-primary">
                            <span v-if="item.value">修改音频</span><span v-else>上传音频</span>
                            <input type="file" accept="audio/*"  v-bind:id="'aliyunInput_' + i" @change="upload('',i)" style="position: absolute;top: 0;left: 0;opacity: 0;width: 100%;"/>
                        </label>
                        <span v-if="currentRecording === i">正在录音...</span>
                        <div v-bind:id="'record-audio-' + i"></div>
                        <span v-if="item.value"  class="btn btn-primary" @click="deleteAudio('',i)">删除</span>
                        <audio v-if="item.value" class="pull-right" v-bind:src="item.value" controls target="_blank" style="padding-right: 550px;"></audio>
                        <span v-bind:id="'pptMsg_' + i"></span>
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
            qid:'',
            name:"",
            currentRecording: null,
            audios: {},
            pronList:[],
            gramList:[],
            knowledgeList:[],
            summary:"",
            defaultSummary:"",
            microphoneEnable: true
        },
        methods: {
            onRecord: function(prefix,index) {
                console.log("prefix:" + prefix + "; index : " + index)
                var thiz = this;
                if(!recorder) {
                    recorder = new _Recorder({initCallback: function(){
                        thiz.currentRecording = prefix + index;
                        document.getElementById(prefix + 'record-audio-' + index).innerHTML = '';
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
                            thiz.audios[prefix + index] = blob;
                            thiz.uploadAudioRecord(prefix,index)
                        });
                    }else {
                        this.currentRecording = prefix + index;
                        document.getElementById(prefix + 'record-audio-' + index).innerHTML = '';
                        recorder.start();
                    }
                }catch(err) {
                    alert('录音失败，建议使用Firefox浏览器，如已经是Firefox，请联系技术人员。' + prefix + ";" + err)
                }
            },
            uploadAudioRecord: function(prefix,index) {
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
                        console.log("path:" + ossPath + ";file:" + thiz.audios[prefix + index])
                        store.multipartUpload(ossPath, thiz.audios[prefix + index]).then(function (result) {
                            console.log("https://" + signResult.videoHost + ossPath)
                            if(prefix == 'pron-'){
                                vm.pronList[index].audio = "https://" + signResult.videoHost + ossPath;
                            } else if(prefix == 'gram-'){
                                vm.gramList[index].audio = "https://" + signResult.videoHost + ossPath;
                            } else {
                                vm.knowledgeList[index].value = "https://" + signResult.videoHost + ossPath;
                            }
//                            console.log(vm.knowledgeList[index].value)
                            $("#" + prefix + "pptMsg_" + index).html("上传完成,保存后生效");
                        }).catch(function (err) {
                            $("#" + prefix + "pptMsg_" + index).html("上传失败,请重新选择文件");
                            console.log(err);
                        });
                    }
                });
            },
            uploadAudio: function(prefix, index) {
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
                        console.log("path:" + ossPath + ";file:" + thiz.audios[prefix + index])
                        store.multipartUpload(ossPath, thiz.audios[prefix + index]).then(function (result) {
                            console.log("https://" + signResult.videoHost + ossPath)
                            if(prefix == 'pron-'){
                                vm.pronList[index].audio = "https://" + signResult.videoHost + ossPath;
                            } else if(prefix == 'gram-'){
                                vm.gramList[index].audio = "https://" + signResult.videoHost + ossPath;
                            } else {
                                vm.knowledgeList[index].value = "https://" + signResult.videoHost + ossPath;
                            }
//                            console.log(vm.knowledgeList[index].value)
                            $("#" + prefix + "pptMsg_" + index).html("上传完成,保存后生效");
                        }).catch(function (err) {
                            $("#" + prefix + "pptMsg_" + index).html("上传失败,请重新选择文件");
                            console.log(err);
                        });
                    }
                });
            },
            save: function () {
                var _this = this;
                $.post('${requestContext.webAppContextPath}/chips/ai/active/service/commonTemplateSave.vpage', {
                    pronJson: JSON.stringify(_this.pronList),
                    gramJson: JSON.stringify(_this.gramList),
                    knowledgeJson: JSON.stringify(_this.knowledgeList),
                    json:"",
                    qid:_this.qid,
                    summary:_this.summary,
                    defaultSummary:_this.defaultSummary
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
                var method = getUrlParam("method")
                console.log("cancel method: " + method)
                var hostName = window.location.host;
                console.log("http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&qid=" + getUrlParam('qid') + "&bookId=" + getUrlParam('bookId') + "&unitType=" + getUrlParam('unitType')
                        + "&unitId=" + getUrlParam('unitId') + "&lessonId=" + getUrlParam('lessonId'));
                window.location.href = "http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&qid=" + getUrlParam('qid') + "&bookId=" + getUrlParam('bookId') + "&unitType=" + getUrlParam('unitType')
                        + "&unitId=" + getUrlParam('unitId') + "&lessonId=" + getUrlParam('lessonId');
            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
            deleteAudio:function (prefix,index) {
                var _this = this;
//                _this.knowledgeList[index].value = "";
                if(prefix == 'pron-'){
                    _this.pronList[index].audio = "";
                } else if(prefix == 'gram-'){
                    _this.gramList[index].audio = "";
                } else {
                    _this.knowledgeList[index].value = "";
                }
                $("#" + prefix + "pptMsg_" + index).html("");
                $("#" + prefix + "aliyunInput_" + index).val("");
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
            var qid = "${qid!}";
            console.log("qid")
            console.log(qid)
            $.get('${requestContext.webAppContextPath}/chips/ai/active/service/commonTemplateDataQuery.vpage', {
//                qid: qid
                qid: getUrlParam("qid"),
            }, function (res) {
                if(res.success) {
                    _this.pronList = res.pronList;
                    _this.gramList = res.gramList;
                    _this.knowledgeList = res.knowledgeList;
                    _this.summary = res.summary;
                    _this.defaultSummary = res.defaultSummary;
                    _this.name = res.name;
                    _this.qid = res.qid;
                    console.log(_this.knowledgeList)
                }
            });
        }
    });

    function upload(prefix, ind) {
        $("#" + prefix + "pptMsg_" + ind).html("上传中...");
        var file =  document.getElementById(prefix + "aliyunInput_" + ind).files[0];
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
//                    vm.knowledgeList[ind].value = "https://" + signResult.videoHost + ossPath;
                    if(prefix == 'pron-'){
                        vm.pronList[ind].audio = "https://" + signResult.videoHost + ossPath;
                    } else if(prefix == 'gram-'){
                        vm.gramList[ind].audio = "https://" + signResult.videoHost + ossPath;
                    } else {
                        vm.knowledgeList[ind].value = "https://" + signResult.videoHost + ossPath;
                    }
//                    console.log(vm.knowledgeList[ind].value)
                    $("#pptMsg_" + ind).html("上传完成,保存后生效");
                }).catch(function (err) {
                    $("#pptMsg_" + ind).html("上传失败,请重新选择文件");
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