<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="AI对话添加" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">
<style>
    body{
        line-height:20px !important;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    }
    .navbar{
        min-height:41px;
        height: 41px !important;
        border-width:0;
        -webkit-box-sizing: border-box;
        -moz-box-sizing: border-box;
        box-sizing: border-box;
    }
    .navbar .navbar-inner{
        min-height:41px;
        height: 41px !important;
        border-width:0;
        -webkit-box-sizing: border-box;
        -moz-box-sizing: border-box;
        box-sizing: border-box;
    }
    .collapse{
        display: block;
    }
    legend{
        padding-bottom: 20px;
    }

    .form-control {
        height: 34px !important;
        margin-bottom: 0 !important;
    }

    .first-form label {
        width: 100px;
        text-align: right;
        padding-right: 15px;
    }

    .feedback-form label {
        width: 120px;
        text-align: right;
        padding-right: 15px;
    }

    .form-title-input {
        width: 196px !important;
        display: inline-block !important;
    }

    input {
        outline: none !important;
    }

    input::placeholder {
        color: #999999;
    }

    .bs-callout {
        padding: 0 20px;
        margin: 20px 0;
        border: 1px solid #eee;
        border-left: 5px solid #1b809e;
        border-radius: 3px;
    }

    .bs-callout-danger {
        border-left-color: #ce4844;
    }

    .page-header {
        margin-top: 25px !important;
    }

    .form-inline .form-group {
        margin-bottom: 10px !important;
    }
    .data-modal{
        position: fixed;
        top: 0;
        bottom: 0;
        right: 0;
        left: 0;
        background: rgba(0,0,0,0.5);
        z-index: 9999;
    }
    .data-modal .data-content{
        background: #fff;
        height: 80%;
        position: absolute;
        top: 10%;
        bottom: 5%;
        left: 30%;
        right: 30%;
        border-radius: 10px;
        overflow: auto;
    }
    .data-modal .data-content p{
        text-align: center;
        line-height: 40px;
        font-size: 20px;
    }
    .data-modal .data-content .data-title{
        height: 50px;
        border-bottom: 1px solid #DDDDDD;
        text-align: center;
        position: relative;
    }
    .data-modal .data-content .data-title h3{
        margin: 0;
        padding: 0;
        line-height: 50px;
    }
    .data-modal .data-content .data-title span{
        font-size: 30px;
        line-height: 50px;
        width: 50px;
        height: 50px;
        position: absolute;
        top: 0;
        right: 0;
        cursor: pointer;
    }
    .base_information label{
        width: 150px;
        text-align: right;
        padding-right: 15px;
    }
    .base_information input{
        width: 300px !important;
    }

    .jsgf-modal{
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        background: rgba(0,0,0,0.5);
        overflow-y: scroll;
        display: none;
        z-index:9999;
    }
    .jsgf-content{
        width: 900px;
        background: #fff;
        position: absolute;
        top: 80px;
        left: 50%;
        margin-bottom: 40px;
        transform: translateX(-50%);
        border-radius: 5px;
        padding:20px;
    }
    .jsgf-content textarea{
        width: 500px;
    }
    .jsgf-content .jsgf-item{
        margin:10px 0;
    }
    .jsgf-content .jsgf-close{
        font-size: 30px;
        padding: 15px;
        position: absolute;
        top: 0;
        right: 0;
        cursor: pointer;
    }
</style>
<div id="AppModel"></div>


<script type="text/x-template" id="app-template">
    <div id="main_container" class="span9">

        <!--基础内容-->
        <legend><#if id?? && id != ''>编辑<#else>添加</#if> - 情景对话<#if id?? && id != ''>Edit<#else>Add</#if> - Conversation Lesson</legend>
        <div class="form-inline base_information">
            <div class="form-group">
                <label>题目编号LessonID</label> <input type="text" class="form-control" value="" placeholder="题目编号LessonID" v-model="id" <#if id?? && id != ''>disabled="disabled"</#if>/>
            </div>
            <div class="form-group">
                <label>标题Title</label> <input type="text" class="form-control" value="" placeholder="标题" v-model="title" />
            </div>
        </div>

        <div class="bs-callout bs-callout-danger">
            <div class="first-form">
                <h3 class="page-header">开场Opening<#if id?? && id != ''> - 编辑<#else> - 添加</#if> <span class="btn btn-success fold_btn">折叠/展开show/open</span> </h3>
                <div class="form-group fold_box">
                    <div class="form-inline">
                        <div v-for='(item,key) in begin' class="form-group" v-if="key != 'status' && key != 'level'">
                            <label>{{getPlaceholder(key).name}}</label>
                            <input type="text" class="form-control" :placeholder="getPlaceholder(key).placeholder" v-model="begin[key]">
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 添加项目 -->
        <my-component></my-component>

        <#--<input type="button" class="btn btn-lg btn-info" @click="topicAdd" value="添加Question"/>-->
        <#--<input type="button" class="btn btn-lg btn-danger" @click="topicRemove" v-if="topic.length > 1"-->
               <#--value="删除Question"/>-->

        <!--结尾内容-->
        <div class="bs-callout bs-callout-danger">
            <div class="first-form">
                <h3 class="page-header">结尾内容Ending <span class="btn btn-success fold_btn">折叠/展开show/open</span> </h3>
                <div class="form-group fold_box">
                    <div class="form-inline">
                        <div v-for='(item,key) in end' class="form-group" v-if="key != 'status' && key != 'level' && key != 'tip'">
                            <label>{{getPlaceholder(key).name}}</label> <input type="text" class="form-control" value=""
                                                          :placeholder="getPlaceholder(key).placeholder" v-model="end[key]">
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="form-group" style="text-align: center;">
            <input type="button" class="btn btn-info btn-lg" value="提交对话Upload" @click="getResults"
                   style="width: 200px;"/>
        </div>

        <span class="btn btn-info btn" @click="getResults" style="position: fixed;bottom: 100px;right: 20px;">提交对话Upload</span>
        <span class="btn btn-success hidden_btn" style="position: fixed;bottom: 60px;right: 20px;">折叠全部Fold all</span>
        <span class="btn btn-info go_home" style="position: fixed;bottom: 20px;right: 20px;">回到顶部Go to the top</span>
    </div>
</script>

<#-- 对话开始 -->
<script type="text/x-template" id="feedback-template">
    <div class="feedback-form">
        <div class="bs-callout" v-for="(topicItem, index) in topic">
            <h3 class="page-header">Question -{{index + 1}} 对话开始Conversation</h3>

            <!--对话开始-->
            <div class="form-group">
                <div>
                    <div class="col-sm-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">AI台词Teacher's line</div>
                            <div class="panel-body">
                                <template v-for='(begin, key) in topicItem.begin' v-if="key != 'status'">
                                    <template v-if='key === "translation"'>
                                        <div class="form-group form-inline">
                                            <label>{{$root.getPlaceholder(key).name}}</label>
                                            <input type="text" class="form-control" style="width: 60%;" :placeholder="$root.getPlaceholder(key).placeholder"
                                                   v-model="topicItem.begin[key]"/>
                                            <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                                        </div>
                                    </template>
                                </template>
                                <div class="fold_box">
                                    <template v-for='(begin, key) in topicItem.begin' v-if="key != 'status'">
                                        <template v-if='key !== "translation"'>
                                            <div class="form-group form-inline">
                                                <label>{{$root.getPlaceholder(key).name}}</label>
                                                <input type="text" class="form-control" style="width: 60%;" :placeholder="$root.getPlaceholder(key).placeholder"
                                                       v-model="topicItem.begin[key]"/>
                                            </div>
                                        </template>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">帮助卡片录入Help Card</div>
                            <div class="panel-body">
                                <template v-for='(begin, key) in topicItem.help' v-if="key === 'helpEn'">
                                    <div class="form-group form-inline">
                                        <label>{{key}}</label>
                                        <input type="text" class="form-control" style="width: 60%;" placeholder="添加文本，没有则不填"
                                               v-model="topicItem.help[key]"/>
                                        <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                                    </div>
                                </template>
                                <div class="fold_box">
                                    <template v-for='(begin, key) in topicItem.help' v-if="key !== 'helpEn'">
                                        <div class="form-group form-inline">
                                            <label>{{key}}</label>
                                            <input type="text" class="form-control" style="width: 60%;" placeholder="添加文本，没有则不填"
                                                   v-model="topicItem.help[key]"/>
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">知识点总结Knowledge points</div>
                            <div class="panel-body">
                                <template v-for='(sentence,key) in topicItem.knowledge' v-if="key === 'explain'">
                                    <div class="form-group form-inline">
                                        <template v-if="key !== 'sentences'">
                                            <label>{{ key }}</label>
                                            <input type="text" class="form-control"  style="width: 400px" placeholder="添加文本，没有则不填" v-model="topicItem.knowledge[key]">
                                            <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                                        </template>
                                    </div>
                                </template>
                                <div class="fold_box">
                                    <template v-for='(sentence,key) in topicItem.knowledge' v-if="key !== 'explain'">
                                        <div class="form-group form-inline">
                                            <template v-if="key !== 'sentences'">
                                                <label>{{ key }}</label>
                                                <input type="text" class="form-control"  style="width: 400px" placeholder="添加文本，没有则不填" v-model="topicItem.knowledge[key]">
                                            </template>
                                            <template v-else>
                                                <template v-for="(item,sentenceIndex) in topicItem.knowledge.sentences" :key="sentenceIndex">
                                                    <div class="form-group">
                                                        <label>sentence</label>
                                                        <input type="text" class="form-control" v-model="topicItem.knowledge.sentences[sentenceIndex].sentence" style="width: 400px" placeholder="添加文本，没有则不填">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>sentenceAudio</label>
                                                        <input type="text" class="form-control" v-model="topicItem.knowledge.sentences[sentenceIndex].sentenceAudio" style="width: 400px" placeholder="添加Add文本，没有则不填">
                                                    </div>
                                                    <button class="btn btn-success" @click="addsentence(index,sentenceIndex)">添加Add</button>
                                                    <button class="btn btn-danger" @click="delsentence(index,sentenceIndex)">删除Delete</button>
                                                </template>
                                            </template>
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

            <!--用户回答-->
            <div class="col-sm-12">
                <div class="panel panel-default">
                    <div class="panel-heading">用户回答Users'answer</div>
                    <div class="panel-body">
                        <div v-for='(item, key) in topicItem.contents' class="form-group">
                            <div class="form-group">
                                <label><h4>用户回答Users'answer - {{key + 1}}</h4></label>
                                <textarea type="text" class="form-control form-title-input" readonly style="width: 400px!important;height: 74px !important;"  placeholder="jsgf录入，不需要输入，点击编辑生成" v-model="item.pattern" :index="item.pattern"></textarea>
                                <div class="form-inline" style="display: inline-block;">
                                    <label>level</label>
                                    <select class="form-control" v-model="item.feedback[0]['level']">
                                        <option disabled value="请选择">请选择</option>
                                        <option value="A+">A+</option>
                                        <option value="A">A</option>
                                        <option value="B">B</option>
                                        <option value="C">C</option>
                                        <option value="D">D</option>
                                        <option value="E">E</option>
                                        <option value="F">F</option>
                                        <option value="F1">F1</option>
                                        <option value="F2">F2</option>
                                        <option value="F3">F3</option>
                                        <option value="F4">F4</option>
                                        <option value="F5">F5</option>
                                    </select>
                                </div>

                                <#--<input type="button" value="+ 关闭反馈" class="btn" @click="showHide($event, '#contents_'+ index +'_' + key)"/>-->
                                <span class="btn btn-warning edit_jsgf">编辑Edit</span>
                                <div class="jsgf-modal">
                                    <div class="jsgf-content">
                                        <div class="">
                                            <h3 style="margin-top: 0;">编辑Edit <span class="jsgf-close">&times;</span></h3>
                                        </div>
                                        <hr style="clear: both;"/>
                                        <div class="jsgf-item">
                                            <span class="btn" style="width: 70px;">jsgf</span>
                                            <textarea type="text" placeholder="" v-model="item.pattern"></textarea>
                                        </div>
                                        <hr style="clear: both;"/>
                                        <div class="jsgf-item" v-for="(jsgfItem,jsgfIndex) in item.jsgfText.varText">
                                            <span class="btn" style="width: 70px;">变量</span>
                                            <textarea type="text" v-model="item.jsgfText.varText[jsgfIndex].key" placeholder="加入变量，格式<a>=apple,pear,orange不同变量间用|分隔"></textarea>
                                            <span @click="addJsgf('varText',index ,key)" class="btn btn-success">添加Add</span>
                                            <span @click="delJsgf('varText',index ,key,jsgfIndex)" class="btn btn-danger">删除Delete</span>
                                        </div>
                                        <hr style="clear: both;"/>

                                        <div class="jsgf-item" v-for="(jsgfItem,jsgfIndex) in item.jsgfText.styleText">
                                            <span class="btn" style="width: 70px;">句式</span>
                                            <textarea type="text" v-model="item.jsgfText.styleText[jsgfIndex].key" placeholder="加入句式，格式<a><b>，不同句式间用|分隔"></textarea>
                                            <span @click="addJsgf('styleText',index ,key)" class="btn btn-success">添加Add</span>
                                            <span @click="delJsgf('styleText',index ,key,jsgfIndex)" class="btn btn-danger">删除Delete</span>
                                        </div>
                                        <hr style="clear: both;"/>

                                        <div class="jsgf-item" v-for="(jsgfItem,jsgfIndex) in item.jsgfText.keywordText">
                                            <span class="btn" style="width: 70px;">关键字</span>
                                            <textarea type="text" v-model="item.jsgfText.keywordText[jsgfIndex].key" placeholder="一个关键词小于两个word，不同关键词用|分隔"></textarea>
                                            <span @click="addJsgf('keywordText',index ,key)" class="btn btn-success">添加Add</span>
                                            <span @click="delJsgf('keywordText',index ,key,jsgfIndex)" class="btn btn-danger">删除Delete</span>
                                        </div>
                                        <hr style="clear: both;"/>
                                        <div class="html_box"></div>
                                        <div class="text-center">
                                            <span class="btn btn-primary" @click="get_jsgf(index ,key)">提交</span>
                                        </div>
                                    </div>
                                </div>
                                <span class="btn btn-primary" @click="check(item.pattern)">检测Test</span>
                                <span class="btn btn-info" @click="openModal(item.pattern)">展开数据Open students' dialogue</span>
                                <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                                <span @click='remove(index,key)' class="btn btn-danger">删除Delete</span>
                            </div>
                            <div class="fold_box">
                                <div :id="'contents_'+ index +'_' + key">
                                    <div v-for='(feedItem, feedIndex) in item.feedback' class="form-group well">
                                        <div class="panel-body">
                                            <div v-for='(wordItem, wordKey) in feedItem' v-if="wordKey != 'status'" class="form-group">
                                                <!--select-->
                                                <div class="form-group form-inline" v-if="wordKey == 'level'">
                                                    <label>{{$root.getPlaceholder(wordKey).name}}</label>
                                                    <select class="form-control" v-model="feedItem[wordKey]">
                                                        <option disabled value="请选择">请选择</option>
                                                        <option value="A+">A+</option>
                                                        <option value="A">A</option>
                                                        <option value="B">B</option>
                                                        <option value="C">C</option>
                                                        <option value="D">D</option>
                                                        <option value="E">E</option>
                                                        <option value="F">F</option>
                                                        <option value="F1">F1</option>
                                                        <option value="F2">F2</option>
                                                        <option value="F3">F3</option>
                                                        <option value="F4">F4</option>
                                                        <option value="F5">F5</option>
                                                    </select>
                                                </div>

                                                <!--input-->
                                                <div class="form-group form-inline" v-if="wordKey !== 'level'">
                                                    <label>{{$root.getPlaceholder(wordKey).name}}</label>
                                                    <input type="text" class="form-control" style="width: 500px;" :placeholder="$root.getPlaceholder(wordKey).placeholder" v-model="feedItem[wordKey]"
                                                           :index="feedItem[wordKey]"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="form-group form-inline">
                                    <label>&nbsp;</label>
                                    <span class="btn btn-primary" @click="feedbackPlus(index, key)">+ 添加下一轮反馈Add another feedback</span>
                                    <span class="btn btn-primary" @click='feedbackPlus(index, key, "true")'>+ 复制添加Copy and add</span>
                                    <span class="btn btn-danger" @click='feedbackRemove(index, key)' v-if="item.feedback.length > 1">- 删除Delete</span>
                                    <#--<input type="button" @click="feedbackPlus(index, key)" value="+ 添加下一轮反馈" class="btn btn-primary"/>-->
                                    <#--<input type="button" @click='feedbackPlus(index, key, "true")' value="+ 复制添加" class="btn"/>-->
                                    <#--<input type="button" @click='feedbackRemove(index, key)' value="- 删除" class="btn btn-danger" v-if="item.feedback.length > 1"/>-->
                                </div>
                            </div>
                            <hr>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-group form-inline">
                <label>&nbsp;</label>
                <span type="button" @click='plus(index)' class="btn btn-info">+ 添加学生回答Add levels</span>
                <span type="button" @click='plus(index, "true")' class="btn btn-info">+ 复制添加</span>

                <#--<input type="button" @click='plus(index)' value="+ 添加学生回答" class="btn btn-info"/>-->
                <#--<input type="button" @click='plus(index, "true")' value="+ 复制添加" class="btn"/>-->
                <#--<input type="button" @click='remove(index)' value="- 删除" class="btn btn-danger" v-if="topicItem.contents.length > 1"/>-->
            </div>

            <div class="form-group">
                <span class="btn btn-lg btn-info" @click="topicAdd">添加Question Add question</span>
                <span class="btn btn-lg btn-danger" @click="topicRemove(index)">删除Question Delete question</span>

                <#--<input type="button" class="btn btn-lg btn-info" @click="topicAdd" value="添加Question"/>-->
                <#--<input type="button" class="btn btn-lg btn-danger" @click="topicRemove(index)" value="删除Question"/>-->
            </div>


        </div>

        <div class="data-modal" v-show="modalShow">
            <div class="data-content">
                <div class="data-title">
                    <h3>展开的数据</h3>
                    <span @click="closeModal">&times</span>
                </div>
                <p v-for="(item,index) in modaldata" :key="index">{{ item }}</p>
            </div>
        </div>

    </div>
</script>



<script type="text/javascript">
    (function () {
        function topicMapData() {
            this.begin = {
                translation: '',
                cnTranslation: '',
                video: '',
                firstFrame: '',
                roleImage: '',
                tip: '',
                picture: '',
                feedback: '',
                audio: '',
                popTip: '',
                popTipAudio: '',
            };
            this.help = {
                "helpEn": "",
                "helpCn": "",
                "helpAudio": "",
                "helpTitle": "",
                "helpStyle": "",
            };
            this.knowledge = {
                explain: "",
                explainAudio: "",
                sentences:[
                    {
                        sentence: "",
                        sentenceAudio: "",
                    }
                ]
            };
            this.contents = [];
        }

        var vm = new Vue({
            template: '#app-template',
            el: '#AppModel',
            data: {
                topicCount: -1,
                id: "",
                title: "",
                begin: {
                    translation: "",
                    cnTranslation: "",
                    tip: "",
                    picture: "",
                    video: "",
                    roleImage: "",
                    audio: ""
                },
                topic: [],
                end: {
                    translation: "",
                    cnTranslation: "",
                    video: "",
                    roleImage: "",
                    feedback: "",
                    picture: "",
                    audio: ""
                }
            },
            methods: {
                getResults: function () {
                    if(!vm.id || vm.id == ''){
                        alert("题目编号不能为空。");
                        return;
                    }
                    if(!vm.title || vm.title == ''){
                        alert("标题不能为空。");
                        return;
                    }

                    $.post("${requestContext.webAppContextPath}/chips/ailesson/dialogue/save.vpage", {
                        data: JSON.stringify({
                            id: vm.id,
                            title: vm.title,
                            begin: vm.begin,
                            end: vm.end,
                            topic: vm.topic
                        })
                    }, function (data) {
                        if (data.success) {
                            alert("添加成功");
                        } else {
                            alert(data.info);
                            console.info(data);
                        }
                    });
                    console.info(vm._data);
                },
                getPlaceholder: function (key) {
                    var _textWord = {
                        "id": {name: '题目编号', placeholder: "必填"},
                        "picture": {name: 'Pic', placeholder: "https://xxx.jpg,https://aaaa.jpg"},
                        "feedback": {name: 'Feedback', placeholder: "添加文本，没有则不填"},
                        "level": {name: 'Level', placeholder: "请选择"},
                        "tip": {name: 'Tip', placeholder: "添加文本，没有则不填"},
                        "video": {name: 'Video', placeholder: "添加文本，没有则不填"},
                        "firstFrame": {name: 'FirstFrame', placeholder: "首帧图"},
                        "translation": {name: 'Text_en', placeholder: "添加文本，没有则不填"},
                        "audio": {name: 'Audio', placeholder: "添加文本，没有则不填"},
                        "cnTranslation": {name: 'Text_cn', placeholder: "添加文本，没有则不填"}
                    };

                    if (_textWord[key]) {
                        return _textWord[key];
                    } else {
                        return {name: key, placeholder: "添加文本，没有则不填"};
                    }
                },
                contentModel: function () {
                    return {
                        pattern: "",
                        jsgfText:{
                            styleText:[
                                {"key":''}
                            ],
                            keywordText:[
                                {"key":''}
                            ],
                            varText:[
                                {"key":''}
                            ],
                        },
                        feedback: [{
                            level: '',
                            translation: '',
                            cnTranslation: '',
                            video: '',
                            firstFrame:'',
                            roleImage:"",
                            feedback: '',
                            tip: '',
                            picture: '',
                            audio: '',
                            warning: '',
                            warningAudio: '',
                        }]
                    }
                },
                topicAdd: function () {
                    var _topicMapData = new topicMapData();

                    this.topicCount += 1;
                    this.topic.push(_topicMapData);

                    var contents = this.topic[this.topicCount].contents;
                    contents.push(this.contentModel());
                },
                topicRemove: function () {

                    if (this.topicCount <= 0) {
                        return;
                    }

                    console.log(this.topic);

                    this.topic.pop();

                    // this.topic.splice(this.topicCount, 1);

                    this.topicCount -= 1;
                },

            },
            created: function () {
                //初始化添加Topic
                this.topicAdd();

                var _self = this;
                Vue.component('my-component', {
                    template: '#feedback-template',
                    data: function () {
                        return {
                            topic: _self.topic,
                            modalShow:false,
                            modaldata:[],
                        };
                    },
                    created: function () {
                        var lessonId = '${id!}';
                        if(lessonId && lessonId!=''){
                            var _modelSelf = this;

                            $.get("${requestContext.webAppContextPath}/chips/ailesson/dialogue/detail.vpage", {id: lessonId}, function (result) {
                                if(result.success){
                                    _self.id = lessonId;
                                    _self.begin = result.data.begin;
                                    _self.end = result.data.end;
                                    _self.title = result.data.title;

                                    console.log(result.data.topic);
                                    _self.topicCount = result.data.topic.length;

                                    for(var i = 0; i < result.data.topic.length;i++){
                                        for(var j = 0; j < result.data.topic[i].contents.length;j++){
                                            if(result.data.topic[i].contents[j].jsgfText){
                                                result.data.topic[i].contents[j].jsgfText = JSON.parse(result.data.topic[i].contents[j].jsgfText)
                                            }else{
                                                var tmpObj = {
                                                    styleText:[
                                                        {"key":''}
                                                    ],
                                                    keywordText:[
                                                        {"key":''}
                                                    ],
                                                    varText:[
                                                        {"key":''}
                                                    ],
                                                };
                                                result.data.topic[i].contents[j].jsgfText = JSON.parse(JSON.stringify(tmpObj));
                                            }

                                        }
                                    }

                                    _self.topic = _modelSelf.topic = result.data.topic;

                                    console.log(result.data.topic)
                                }
                            });
                        }
                    },
                    methods: {
                        topicAdd:function () {
                            var _topicMapData = new topicMapData();

                            _self.topicCount += 1;
                            _self.topic.push(_topicMapData);

                            var contents = _self.topic[_self.topicCount].contents;
                            contents.push(_self.contentModel());
                        },
                        topicRemove:function (index) {
                            if (this.topic.length <= 1) {
                                alert("最少保留一个")
                                return;
                            }
                            console.log(this.topic);
                            this.topic.splice(index,1);

                            // this.topic.splice(this.topicCount, 1);

                            this.topicCount -= 1;
                        },
                        plus: function (index, isClone) {
                            var _newData = _self.contentModel();
                            var _cts = this.topic[index].contents;
                            if(isClone){
                                _newData = $.extend(true, {}, _cts[_cts.length - 1]);
                            }
                            _cts.push(_newData);
                        },
                        remove: function (index,key) {
                            var _contents = this.topic[index].contents;
                            _contents.splice(key, 1);
                        },
                        feedbackPlus: function (to_index, ct_key, isClone) {
                            var _newData = {
                                level: '',
                                translation: '',
                                cnTranslation: '',
                                video: '',
                                feedback: '',
                                tip: '',
                                picture: '',
                                audio: '',
                                status: '',
                                warning: '',
                                warningAudio: '',
                            };

                            var _fbk = this.topic[to_index].contents[ct_key].feedback;

                            if(isClone){
                                _newData = $.extend(true, _newData, (_fbk[_fbk.length - 1]) || {});
                            }

                            _fbk.push(_newData);
                        },
                        feedbackRemove: function (to_index, ct_key) {
                            var _contents = this.topic[to_index].contents;
                            _contents[ct_key].feedback.splice(_contents[ct_key].feedback.length - 1, 1);
                        },
                        showHide: function (e, _id) {
                            if ($(_id).is(':visible')) {
                                $(_id).hide();
                                $(e.target).val("- 打开反馈").addClass("btn-success");
                            } else {
                                $(_id).show();
                                $(e.target).val("+ 关闭反馈").removeClass("btn-success");
                            }

                        },
                        closeModal:function(){
                            var _this = this;
                            _this.modalShow = false;
                        },
                        openModal:function(txt){
                            var _this = this;

                            $.post("${requestContext.webAppContextPath}/chips/ailesson/jsgf/expand.vpage", {data: txt}, function (result) {
                                if(result.success){
                                    _this.modalShow = true;
                                    _this.modaldata = result.data;
                                }else{
                                    alert(result.info)
                                }
                            })
                        },
                        check:function(txt){
                            var _this = this;
                            $.post("${requestContext.webAppContextPath}/chips/ailesson/jsgf/check.vpage", {data: txt}, function (result) {
                                if(result.success){
                                    alert("格式正确")
                                }else{
                                    alert(result.info)
                                }
                            });
                        },
                        addsentence:function(topicIndex,sentenceIndex){
                            var _newSentenceDataInit = {
                                sentence: "",
                                sentenceAudio: "",
                            };
                            var _sentences = this.topic[topicIndex].knowledge.sentences;
                            _newSentenceData = this._cloneObject(_newSentenceDataInit,true);
                            _sentences.push(_newSentenceData);
                        },
                        delsentence:function(topicIndex,sentenceIndex){
                            var _sentences = this.topic[topicIndex].knowledge.sentences;
                            if(_sentences.length > 1){
                                _sentences.splice(sentenceIndex,1);
                            }else{
                                alert("最少保留一个");
                            }

                        },
                        _cloneObject:function(target, deep) {
                            if (deep === void 0) { deep = false; }
                            if (target == null)
                                return null;
                            var newObject;
                            if(Array.isArray(target)){
                                newObject = [] ;
                            }else{
                                newObject = {}
                            }

                            for (var key in target) {
                                var value = target[key];
                                if (deep && typeof value == "object") {
                                    // 如果是深表复制，则需要递归复制子对象
                                    value = this._cloneObject(value, true);
                                }
                                newObject[key] = value;
                            }
                            return newObject;
                        },
                        addJsgf:function(type,index ,feedIndex){
                            var obj = {key:''};
                            this.topic[index].contents[feedIndex].jsgfText[type].push(JSON.parse(JSON.stringify(obj)))
                        },
                        delJsgf:function(type,index ,feedIndex,jsgfIndex){
                            if(this.topic[index].contents[feedIndex].jsgfText[type].length > 1){
                                this.topic[index].contents[feedIndex].jsgfText[type].splice(jsgfIndex,1)
                            }else{
                                alert("英雄，至少要留一个")
                            }
                        },
                        get_jsgf:function(index ,key){
                            var _this = this;
                            var varTextArr = this.topic[index].contents[key].jsgfText.varText.map(function(item,index){
                                return item.key
                            });

                            var styleTextArr = this.topic[index].contents[key].jsgfText.styleText.map(function(item,index){
                                return item.key
                            });

                            var keywordTextArr = this.topic[index].contents[key].jsgfText.keywordText.map(function(item,index){
                                return item.key
                            });

                            var data = {
                                varText: varTextArr,
                                styleText:styleTextArr,
                                keywordText:keywordTextArr
                            };

                            $.post("${requestContext.webAppContextPath}/chips/ailesson/jsgf/create.vpage", {
                                data:JSON.stringify(data)
                            }, function (result) {
                                console.log(result)
                                if(result.success){
                                    $(".html_box").html(result.result);
//                                    alert("success");
                                }else{
                                    alert(result.info)
                                }

                            });
                        }
                    }
                });
            }
        });


        $("#main_container").on("click",'.fold_btn',function(){
            $(this).parent().next().slideToggle()
        });


        $("#main_container").on("click",".edit_jsgf",function(){
            $(this).next().fadeToggle()
            $(".html_box").html('')
        })

        $("#main_container").on("click",".jsgf-close",function(){
            $(this).closest(".jsgf-modal").fadeToggle()
        });

        $(".hidden_btn").on("click", function(){
            $(".fold_box").slideUp()
        });

        $('.hidden_btn').trigger("click")

        $(".go_home").click(function(){
            $("body,html").animate({
                scrollTop:0
            });
        })



//        $(".fold_btn").click(function(){
//            $(this).parent().next().slideToggle()
//        })



    }());
</script>

</@layout_default.page>