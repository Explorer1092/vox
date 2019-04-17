<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="AI任务对话添加" page_num=26>
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

    .first-form label {
        width: 100px !important;
        text-align: right;
        padding-right: 15px;
    }

    .feedback-form label {
        width: 140px;
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
    [v-cloak]{
        display: none;
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
    .label_box label{
        width: 150px;
        text-align: right;
        padding-right: 15px;
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

<div id="AppModel" v-cloak class="span9">
    <!-- 基础内容 -->
    <legend><#if id?? && id != ''>编辑<#else>添加</#if> - 任务对话<#if id?? && id != ''>Edit<#else>Add</#if> - Task</legend>

    <div class="form-inline label_box">
        <div class="form-group">
            <label>题目编号LessonID</label> <input type="text" class="form-control" v-model="result.id" style="width: 300px" :placeholder="getPlaceholder('id').name" <#if id?? && id != ''>disabled="disabled"</#if>/>
        </div>
        <div class="form-group">
            <label>标题Title</label> <input type="text" class="form-control" v-model="result.title" style="width: 300px" placeholder="必填" />
        </div>
        <div class="form-group">
            <label>allNpc</label>
            <select v-model="result.allNpc">
                <option value="true">true</option>
                <option value="false">false</option>
            </select>
        </div>
    </div>

    <!--Topic内容-->
    <div v-for="(item,npc_index) in result.npcs" class="bs-callout bs-callout-success first-form" :key="npc_index">
        <div class="page-header">
            <h3>对话部分-NPC-{{ npc_index+1 }}</h3>
        </div>
        <div class="panel-body">
            <div class="form-group">
                <div class="form-inline">
                    <div class="bs-callout bs-callout-danger">
                        <div class="first-form">
                            <h3 class="page-header">开场 - <#if id?? && id != ''>编辑<#else>添加</#if>Opening<#if id?? && id != ''>Edit<#else>Add</#if> <span class="btn btn-success fold_btn">折叠/展开show/open</span></h3>
                            <div class="form-group fold_box">
                                <div class="form-inline">
                                    <div v-for="(value,key) in item.begin" class="form-group" :key="key">
                                        <label>{{ getPlaceholder(key).name }}</label>
                                        <input type="text" class="form-control" v-model="result.npcs[npc_index].begin[key]" style="width: 300px" :placeholder="getPlaceholder(key).placeholder"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>{{ getPlaceholder('npcName').name }}</label> <input type="text" class="form-control" v-model="result.npcs[npc_index]['npcName']" style="width: 600px;" :placeholder="getPlaceholder('npcName').placeholder">
                        <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                    </div>
                    <div class="fold_box">
                        <div class="form-group">
                            <label>{{ getPlaceholder('status').name }}</label> <input type="text" class="form-control" v-model="result.npcs[npc_index]['status']" style="width: 600px;" placeholder="No or Success">
                        </div>
                        <div class="form-group">
                            <label>{{ getPlaceholder('rightTip').name }}</label> <input type="text" class="form-control" v-model="result.npcs[npc_index]['rightTip']" style="width: 600px;" :placeholder="getPlaceholder('rightTip').placeholder">
                        </div>
                        <div class="form-group">
                            <label>{{ getPlaceholder('backgroundImage').name }}</label> <input type="text" class="form-control" v-model="result.npcs[npc_index]['backgroundImage']" style="width: 600px;" :placeholder="getPlaceholder('backgroundImage').placeholder">
                        </div>
                        <div class="form-group">
                            <label>{{ getPlaceholder('roleImage').name }}</label> <input type="text" class="form-control" v-model="result.npcs[npc_index]['roleImage']" style="width: 600px;" :placeholder="getPlaceholder('roleImage').placeholder">
                        </div>
                    </div>


                    <div v-for="(item,topic_index) in item.topic" class="bs-callout bs-callout-success" :key="topic_index">
                        <div class="page-header">
                            <h3 class="page-title">Topic-{{ topic_index+1 }}</h3>
                        </div>
                        <div class="page-body">
                            <div class="row">
                                <div class="col-md-12">
                                    <template v-for="(item,key) in result.npcs[npc_index].topic[topic_index].begin">
                                        <div class="form-group" v-if="key === 'translation'">
                                            <label>{{ getPlaceholder(key).name }}</label>
                                            <input type="text" class="form-control" v-model="result.npcs[npc_index].topic[topic_index].begin[key]" style="width: 600px" :placeholder="getPlaceholder(key).placeholder">
                                            <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                                        </div>
                                    </template>
                                    <div class="fold_box">
                                        <template v-for="(item,key) in result.npcs[npc_index].topic[topic_index].begin">
                                            <div class="form-group" v-if="key !== 'translation'">
                                                <label>{{ getPlaceholder(key).name }}</label>
                                                <input type="text" class="form-control" v-model="result.npcs[npc_index].topic[topic_index].begin[key]" style="width: 600px" :placeholder="getPlaceholder(key).placeholder">
                                            </div>
                                        </template>
                                    </div>
                                </div>
                                <div class="col-md-12">
                                    <h3 class="title">帮助卡片录入Help Card</h3>
                                    <template v-for="(item,key) in result.npcs[npc_index].topic[topic_index].help">
                                        <div class="form-group" :key="key" v-if="key === 'helpEn'">
                                            <label>{{ getPlaceholder(key).name }}</label>
                                            <input type="text" class="form-control" v-model="result.npcs[npc_index].topic[topic_index].help[key]" style="width: 600px" :placeholder="getPlaceholder(key).placeholder">
                                            <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                                        </div>
                                    </template>
                                    <div class="fold_box">
                                        <div v-for="(item,key) in result.npcs[npc_index].topic[topic_index].help" v-if="key !== 'helpEn'" class="form-group" :key="key">
                                            <label>{{ getPlaceholder(key).name }}</label>
                                            <input type="text" class="form-control" v-model="result.npcs[npc_index].topic[topic_index].help[key]" style="width: 600px" :placeholder="getPlaceholder(key).placeholder">
                                        </div>
                                    </div>

                                </div>
                                <div class="clearfix"></div>
                                <div class="col-md-8">
                                    <h3 class="title">知识点总结Knowledge points <span class="btn btn-success fold_btn">折叠/展开show/open</span></h3>
                                    <div class="fold_box">
                                        <div v-for="(item,key) in result.npcs[npc_index].topic[topic_index].knowledge" class="form-group" :key="key">
                                            <template v-if="key !== 'sentences'">
                                                <label>{{ getPlaceholder(key).name }}</label>
                                                <input type="text" class="form-control" v-model="result.npcs[npc_index].topic[topic_index].knowledge[key]" style="width: 600px" :placeholder="getPlaceholder(key).placeholder">
                                            </template>
                                            <template v-else>
                                                <template v-for="(item,sentenceIndex) in result.npcs[npc_index].topic[topic_index].knowledge.sentences" :key="sentenceIndex">
                                                    <div class="form-group">
                                                        <label>sentence</label>
                                                        <input type="text" class="form-control" v-model="result.npcs[npc_index].topic[topic_index].knowledge.sentences[sentenceIndex].sentence" style="width: 400px" :placeholder="getPlaceholder('sentence').placeholder">
                                                    </div>
                                                    <div class="form-group">
                                                        <label>sentenceAudio</label>
                                                        <input type="text" class="form-control" v-model="result.npcs[npc_index].topic[topic_index].knowledge.sentences[sentenceIndex].sentenceAudio" style="width: 400px" :placeholder="getPlaceholder('sentenceAudio').placeholder">
                                                    </div>
                                                    <button class="btn btn-success" @click="addsentence(npc_index,topic_index,sentenceIndex)">添加Add</button>
                                                    <button class="btn btn-danger" @click="delsentence(npc_index,topic_index,sentenceIndex)">删除Delete</button>
                                                </template>
                                            </template>
                                        </div>
                                    </div>

                                </div>
                            </div>
                            <div v-for="(item,jushi_index) in result.npcs[npc_index].topic[topic_index].contents" :key="jushi_index" class="panel panel-default">
                                <div class="panel-heading">
                                    <div class="panel-title">
                                        <span>反馈句式-{{ jushi_index + 1 }}</span>
                                        <#--<button class="pull-right btn btn-primary btn-xs" @click="showfeedback(npc_index,topic_index,jushi_index)" :id="npc_index+'_' + topic_index + '_' + jushi_index + 'btn'">- 关闭反馈</button>-->
                                    </div>
                                </div>
                                <div class="panel-body" :id="npc_index+'_' + topic_index + '_' + jushi_index">
                                    <div class="form-group" style="width:100%">
                                        <label>句式录入Users'answer</label>
                                        <textarea type="text" readonly v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].pattern" placeholder="句式录入Users'answer"></textarea>
                                        <select class="form-control" v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback[0]['level']">
                                            <option disabled value="">请选择</option>
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
                                            <option value="F3">F4</option>
                                            <option value="F3">F5</option>
                                        </select>
                                        <span @click="check(item.pattern)" class="btn btn-primary">检测Test</span>
                                        <span @click="openModal(item.pattern)" class="btn btn-info">展开Open students' dialogue</span>
                                        <span class="btn btn-success fold_btn">折叠/展开show/open</span>
                                        <#--<span class="btn btn-info" @click="copyjushi(npc_index,topic_index,jushi_index)">复制</span>-->
                                        <#--<span class="btn btn-success" @click="addjushi(npc_index,topic_index)">添加</span>-->
                                        <#--<span class="btn btn-danger" @click="deljushi(npc_index,topic_index,jushi_index)">删除</span>-->
                                        <span class="btn btn-warning edit_jsgf">编辑Edit</span>
                                        <div class="jsgf-modal">
                                            <div class="jsgf-content">
                                                <div class="">
                                                    <h3 style="margin-top: 0;">编辑Edit <span class="jsgf-close">&times;</span></h3>
                                                </div>
                                                <hr style="clear: both;"/>
                                                <div class="jsgf-item">
                                                    <span class="btn" style="width: 70px;">jsgf</span>
                                                    <textarea type="text" placeholder="" v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].pattern"></textarea>
                                                </div>
                                                <hr style="clear: both;"/>
                                                <div class="jsgf-item" v-for="(jsgfItem,jsgfIndex) in item.jsgfText.varText">
                                                    <span class="btn" style="width: 70px;">变量</span>
                                                    <textarea type="text" v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText.varText[jsgfIndex].key" placeholder="加入变量，格式<a>=apple,pear,orange不同变量间用|分隔"></textarea>
                                                    <span @click="addJsgf('varText',npc_index ,topic_index,jushi_index)" class="btn btn-success">添加Add</span>
                                                    <span @click="delJsgf('varText',npc_index ,topic_index,jushi_index,jsgfIndex)" class="btn btn-danger">删除Delete</span>
                                                </div>
                                                <hr style="clear: both;"/>

                                                <div class="jsgf-item" v-for="(jsgfItem,jsgfIndex) in item.jsgfText.styleText">
                                                    <span class="btn" style="width: 70px;">句式</span>
                                                    <textarea type="text" v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText.styleText[jsgfIndex].key" placeholder="加入句式，格式<a><b>，不同句式间用|分隔"></textarea>
                                                    <span @click="addJsgf('styleText',npc_index ,topic_index,jushi_index)" class="btn btn-success">添加Add</span>
                                                    <span @click="delJsgf('styleText',npc_index ,topic_index,jushi_index,jsgfIndex)" class="btn btn-danger">删除Delete</span>
                                                </div>
                                                <hr style="clear: both;"/>

                                                <div class="jsgf-item" v-for="(jsgfItem,jsgfIndex) in item.jsgfText.keywordText">
                                                    <span class="btn" style="width: 70px;">关键字</span>
                                                    <textarea type="text" v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText.keywordText[jsgfIndex].key" placeholder="一个关键词小于两个word，不同关键词用|分隔"></textarea>
                                                    <span @click="addJsgf('keywordText',npc_index ,topic_index,jushi_index)" class="btn btn-success">添加Add</span>
                                                    <span @click="delJsgf('keywordText',npc_index ,topic_index,jushi_index,jsgfIndex)" class="btn btn-danger">删除Delete</span>
                                                </div>
                                                <hr style="clear: both;"/>
                                                <div class="html_box"></div>
                                                <div class="text-center">
                                                    <span class="btn btn-primary" @click="get_jsgf(npc_index ,topic_index,jushi_index)">提交</span>
                                                </div>
                                            </div>
                                        </div>
                                        <hr>
                                    </div>
                                    <div class="fold_box">
                                        <div v-for="(item,feedback_index) in item.feedback" :key="feedback_index" class="panel panel-default">
                                            <div class="panel-body">
                                                <template v-for="(item,key) in item" class="form-group col-md-12" :key="key">
                                                    <div class="form-group col-md-12" v-if="key == 'level'">
                                                        <label>{{ getPlaceholder(key).name }}</label>
                                                        <select class="form-control" v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback[feedback_index][key]">
                                                            <option disabled value="">请选择</option>
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
                                                            <option value="F3">F4</option>
                                                            <option value="F3">F5</option>
                                                        </select>
                                                        <div class="pull-right">
                                                            <button class="btn btn-danger btn-xs" @click="delfeedback(npc_index,topic_index,jushi_index,feedback_index)">删除Delete</button>
                                                            <button class="btn btn-info btn-xs" @click="copyfeedback(npc_index,topic_index,jushi_index,feedback_index)">复制Copy and add</button>
                                                            <button class="btn btn-success btn-xs" @click="addfeedback(npc_index,topic_index,jushi_index)">添加Add</button>
                                                        </div>
                                                    </div>
                                                </template>
                                                <div v-for="(item,key) in item" class="form-group col-md-12" :key="key">
                                                    <template v-if="key != 'level'">
                                                        <label>{{ getPlaceholder(key).name }}</label>
                                                        <input type="text" class="form-control" style="width:500px !important;" v-model="result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback[feedback_index][key]" :placeholder="getPlaceholder(key).placeholder">
                                                    </template>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group" style="margin-left: 10px;">
                                        <span class="btn btn-success btn-sm" @click="addjushi(npc_index,topic_index)">添加Add</span>
                                        <span class="btn btn-info btn-sm" @click="copyjushi(npc_index,topic_index,jushi_index)">复制Copy</span>
                                        <span class="btn btn-danger btn-sm" @click="deljushi(npc_index,topic_index,jushi_index)">删除Delete</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <button class="btn btn-success btn-sm" @click="addtopic(npc_index)">添加Topic</button>
                            <button class="btn btn-info btn-sm" @click="copytopic(npc_index,topic_index)">复制Topic</button>
                            <button class="btn btn-danger btn-sm" @click="deltopic(npc_index,topic_index)">删除Topic</button>
                        </div>
                    </div>

                    <div class="bs-callout bs-callout-danger">
                        <div class="first-form">
                            <h3 class="page-header">结尾内容Ending <span class="btn btn-success fold_btn">折叠/展开show/open</span> </h3>
                            <div class="form-group fold_box">
                                <div class="form-inline">
                                    <div v-for="(value,key) in item.end" class="form-group" :key="key">
                                        <label>{{ getPlaceholder(key).name }}</label>
                                        <input type="text" class="form-control" v-model="result.npcs[npc_index].end[key]" style="width: 300px" :placeholder="getPlaceholder(key).placeholder"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="form-group">
            <button class="btn btn-success" @click="addnpc">添加NPC Add NPC</button>
            <button class="btn btn-info" @click="copynpc(npc_index)">复制NPC Copy NPC</button>
            <button class="btn btn-danger" @click="delnpc(npc_index)">删除NPC Delete NPC</button>
        </div>

    </div>


    <div class="form-group text-center">
        <button class="btn btn-info btn-lg" @click="add">提交对话Upload</button>
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

    <span class="btn btn-info btn" @click="add" style="position: fixed;bottom: 100px;right: 20px;">提交对话Upload</span>
    <span class="btn btn-success hidden_btn" style="position: fixed;bottom: 60px;right: 20px;">折叠全部Fold all</span>
    <span class="btn btn-info go_home" style="position: fixed;bottom: 20px;right: 20px;">回到顶部Go to the top</span>

</div>



<script type="text/javascript">
    (function () {
        var vm = new Vue({
            el: '#AppModel',
            data: {
                topicCount: -1,
                modalShow:false,
                id: "",
                topic:{
                    begin:{
                        translation: '',
                        cnTranslation: '',
                        video: '',
                        tip: '',
                        picture: '',
                        feedback: '',
                        audio: '',
                        popTip: '',
                        popTipAudio: '',
                    },
                    contents:[
                        {
                            pattern:'',
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
                            feedback:[
                                {
                                    level: '',
                                    translation: '',
                                    cnTranslation: '',
                                    video: '',
                                    feedback: '',
                                    tip: '',
                                    picture: '',
                                    audio: '',
                                }
                            ]
                        }
                    ],
                    help:{
                        helpEn: "",
                        helpCn: "",
                        helpAudio: "",
                        helpTitle: "",
                        helpStyle: "",
                    },
                    knowledge:{
                        explain: "",
                        explainAudio: "",
                        sentences:[
                            {
                                sentence: "",
                                sentenceAudio: "",
                            }
                        ]
                    }
                },
                npc:{
                    npcName:"",
                    status:"",
                    rightTip:"",
                    backgroundImage:"",
                    roleImage:"",
                    begin:{
                        translation: "",
                        cnTranslation: "",
                        tip: "",
                        picture: '',
                        video: "",
                        audio: ""
                    },
                    topic:[
                        {
                            begin:{
                                translation: '',
                                cnTranslation: '',
                                video: '',
                                tip: '',
                                picture: '',
                                feedback: '',
                                audio: '',
                            },
                            contents:[
                                {
                                    pattern:'',
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
                                    feedback:[
                                        {
                                            level: '',
                                            translation: '',
                                            cnTranslation: '',
                                            video: '',
                                            feedback: '',
                                            tip: '',
                                            picture: '',
                                            audio: '',
                                        }
                                    ]
                                }
                            ],
                            help:{
                                helpEn: "",
                                helpCn: "",
                                helpAudio: "",
                                helpTitle: "",
                                helpStyle: "",
                            }
                        }
                    ],
                    end:{
                        translation: "",
                        cnTranslation: "",
                        tip: "",
                        picture: '',
                        video: "",
                        audio: ""
                    }
                },
                feedback:{
                    level: '',
                    translation: '',
                    cnTranslation: '',
                    video: '',
                    feedback: '',
                    tip: '',
                    picture: '',
                    audio: '',
                    warning: '',
                    warningAudio: '',
                },
                jushi:{
                    pattern:'',
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
                    feedback:[
                        {
                            level: '',
                            translation: '',
                            cnTranslation: '',
                            video: '',
                            feedback: '',
                            tip: '',
                            picture: '',
                            audio: '',
                        }
                    ]
                },
                sentence:{
                    sentence: "",
                    sentenceAudio: "",
                },
                result:{
                    id:"",
                    title:"",
                    // background:"",
                    // backgroundAudio:"",
                    allNpc:false,
                    // goal:"",
                    // goalAudio:"",
                    npcs:[
                        {
                            npcName:"",
                            status:"",
                            rightTip:"",
                            backgroundImage:"",
                            roleImage:"",
                            begin:{
                                translation: "",
                                cnTranslation: "",
                                tip: "",
                                picture: '',
                                video: "",
                                audio: ""
                            },
                            topic:[
                                {
                                    begin:{
                                        translation: '',
                                        cnTranslation: '',
                                        video: '',
                                        tip: '',
                                        picture: '',
                                        feedback: '',
                                        audio: '',
                                        popTip: '',
                                        popTipAudio: '',
                                    },
                                    contents:[
                                        {
                                            pattern:'',
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
                                            feedback:[
                                                {
                                                    level: '',
                                                    translation: '',
                                                    cnTranslation: '',
                                                    video: '',
                                                    feedback: '',
                                                    tip: '',
                                                    picture: '',
                                                    audio: '',
                                                    warning: '',
                                                    warningAudio: '',
                                                }
                                            ]
                                        }
                                    ],
                                    help:{
                                        helpEn: "",
                                        helpCn: "",
                                        helpAudio: "",
                                        helpTitle: "",
                                        helpStyle: "",
                                    },
                                    knowledge:{
                                        explain: "",
                                        explainAudio: "",
                                        sentences:[
                                            {
                                                sentence: "",
                                                sentenceAudio: "",
                                            }
                                        ]
                                    }

                                }
                            ],
                            end:{
                                translation: "",
                                cnTranslation: "",
                                tip: "",
                                picture: '',
                                video: "",
                                audio: ""
                            },
                        }
                    ],
                },
                modaldata:[]
            },
            methods: {
                addnpc(){
                    var _this = this;
                    var newnpc = _this._cloneObject(_this.npc,true);
                    _this.result.npcs.push(newnpc);
                },
                copynpc(npc_index){
                    var _this = this;
                    var newnpc = _this._cloneObject(_this.result.npcs[npc_index],true);
                    _this.result.npcs.push(newnpc);
                },
                delnpc(npc_index){
                    var _this = this;
                    if(_this.result.npcs.length <= 1){
                        alert("最少保留一个");
                    }else{
                        _this.result.npcs.splice(npc_index,1);
                    }
                },
                addtopic(npc_index){
                    var _this = this;
                    var newtopic = _this._cloneObject(_this.topic,true);
                    _this.result.npcs[npc_index].topic.push(newtopic);
                },
                copytopic(npc_index,topic_index){
                    var _this = this;
                    var newtopic = _this._cloneObject( _this.result.npcs[npc_index].topic[topic_index],true);
                    _this.result.npcs[npc_index].topic.push(newtopic);
                },
                deltopic(npc_index,topic_index){
                    var _this = this;
                    if(_this.result.npcs[npc_index].topic.length <= 1){
                        alert("最少保留一个");
                    }else{
                        _this.result.npcs[npc_index].topic.splice(topic_index,1);
                    }
                },

                addjushi(npc_index,topic_index){
                    var _this = this;
                    var newjushi = _this._cloneObject(_this.jushi,true);
                    _this.result.npcs[npc_index].topic[topic_index].contents.push(newjushi);
                },
                copyjushi(npc_index,topic_index,jushi_index){
                    var _this = this;
                    var newjushi = _this._cloneObject( _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index],true);
                    _this.result.npcs[npc_index].topic[topic_index].contents.push(newjushi);
                },
                deljushi(npc_index,topic_index,jushi_index){
                    var _this = this;
                    if(_this.result.npcs[npc_index].topic[topic_index].contents.length <= 1){
                        alert("最少保留一个");
                    }else{
                        _this.result.npcs[npc_index].topic[topic_index].contents.splice(jushi_index,1);
                    }
                },

                addsentence(npc_index,topic_index){
                    var _this = this;
                    var newsentence = _this._cloneObject(_this.sentence,true);
                    _this.result.npcs[npc_index].topic[topic_index].knowledge.sentences.push(newsentence);
                },
                delsentence(npc_index,topic_index,sentence_index){
                    var _this = this;
                    if(_this.result.npcs[npc_index].topic[topic_index].knowledge.sentences.length <= 1){
                        alert("最少保留一个");
                    }else{
                        _this.result.npcs[npc_index].topic[topic_index].knowledge.sentences.splice(sentence_index,1);
                    }
                },


                addfeedback(npc_index,topic_index,jushi_index){
                    var _this = this;
                    var newtopic = _this._cloneObject(_this.feedback,true);
                    _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback.push(newtopic);
                },
                copyfeedback(npc_index,topic_index,jushi_index,feedback_index){
                    var _this = this;
                    var newtopic = _this._cloneObject( _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback[feedback_index],true);
                    _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback.push(newtopic);
                },
                delfeedback(npc_index,topic_index,jushi_index,feedback_index){
                    var _this = this;
                    if(_this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback.length <= 1){
                        alert("最少保留一个");
                    }else{
                        _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].feedback.splice(feedback_index,1);
                    }
                },
                showfeedback(npc_index,topic_index,jushi_index){

                    var index = "#"+npc_index+"_"+topic_index+"_"+jushi_index;
                    var indexBtn = "#"+npc_index+"_"+topic_index+"_"+jushi_index + "btn";
                    if($(index).is(':hidden')){
                        $(indexBtn).html("- 关闭反馈")
                    }else{
                        $(indexBtn).html("+ 打开反馈")
                    }
                    console.log($(indexBtn).html);
                    $(index).slideToggle();

                },
                add(){
                    var _this = this;
                    if(!_this.result.id || _this.result.id == ''){
                        alert("题目编号不能为空。");
                        return;
                    }
                    if(!_this.result.title || _this.result.title == ''){
                        alert("标题不能为空。");
                        return;
                    }
                    $.post("${requestContext.webAppContextPath}/chips/ailesson/task/save.vpage", {
                        data: JSON.stringify(_this.result)
                    }, function (data) {
                        if (data.success) {
                            alert("添加成功");
                        } else {
                            alert(data.info);
                            console.info(data);
                        }
                    });
                    console.info(_this.result);
                },

                check(txt){
                    var _this = this;
                    $.post("${requestContext.webAppContextPath}/chips/ailesson/jsgf/check.vpage", {data: txt}, function (result) {
                        if(result.success){
                            alert("格式正确")
                        }else{
                            alert(result.info)
                        }
                    });
                },

                addJsgf:function(type,npc_index,topic_index,jushi_index){
                    var obj = {key:''};
                    var _this = this;
                    _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText[type].push(JSON.parse(JSON.stringify(obj)))
                },
                delJsgf:function(type,npc_index,topic_index,jushi_index,jsgfIndex){
                    var _this = this;
                    if(_this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText[type].length > 1){
                        _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText[type].splice(jsgfIndex,1)
                    }else{
                        alert("英雄，至少要留一个")
                    }
                },
                get_jsgf:function(npc_index,topic_index,jushi_index){
                    var _this = this;

                    var varTextArr = _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText.varText.map(function(item,index){
                        return item.key
                    });

                    var styleTextArr = _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText.styleText.map(function(item,index){
                        return item.key
                    });

                    var keywordTextArr = _this.result.npcs[npc_index].topic[topic_index].contents[jushi_index].jsgfText.keywordText.map(function(item,index){
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
                        console.log(result);
                        if(result.success){
                            $(".html_box").html(result.result);
//                                    alert("success");
                        }else{
                            alert(result.info)
                        }

                    });
                },



                closeModal(){
                    var _this = this;
                    _this.modalShow = false;
                },
                openModal(txt){
                    var _this = this;
                    _this.modalShow = true;
                    $.post("${requestContext.webAppContextPath}/chips/ailesson/jsgf/expand.vpage", {data: txt}, function (result) {
                        if(result.success){
                            _this.modaldata = result.data;
                        }
                    })
                },

                getPlaceholder(key) {
                    var _textWord = {
                        "id": {name: '题目编号', placeholder: "必填"},
                        "picture": {name: 'Pic', placeholder: "https://xxx.jpg,https://aaaa.jpg"},
                        "feedback": {name: 'Feedback', placeholder: "添加文本，没有则不填"},
                        "level": {name: 'Level', placeholder: "请选择"},
                        "tip": {name: 'Tip', placeholder: "添加文本，没有则不填"},
                        "video": {name: 'Video', placeholder: "添加文本，没有则不填"},
                        "translation": {name: 'Text_en', placeholder: "添加文本，没有则不填"},
                        "audio": {name: 'Audio', placeholder: "添加文本，没有则不填"},
                        "cnTranslation": {name: 'Text_cn', placeholder: "添加文本，没有则不填"},
                        "npcName": {name: 'npcName', placeholder: "npc姓名"},
                        "rightTip": {name: 'rightDir', placeholder: "如果status不是Success，这个位置填引导语，是success则为空"},
                    };

                    if (_textWord[key]) {
                        return _textWord[key];
                    } else {
                        return {name: key, placeholder: "添加文本，没有则不填"};
                    }
                },
                _cloneObject(target, deep) {
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
            },
            created: function () {
                var _this = this;
                console.info("created");
                var lessonId = '${id!}';
                if(lessonId && lessonId != ''){
                    $.get("${requestContext.webAppContextPath}/chips/ailesson/task/detail.vpage", {id: lessonId}, function (result) {
                        if(result.success){

                            for(var i = 0; i < result.data.npcs.length;i++){
                                console.log(1)
                                for(var j = 0; j < result.data.npcs[i].topic.length;j++){
                                    console.log(2)
                                    for(var k = 0; k < result.data.npcs[i].topic[j].contents.length;k++){
                                        console.log(3)
                                        console.log(result.data.npcs[i].topic[j].contents[k].jsgfText)
                                        if(result.data.npcs[i].topic[j].contents[k].jsgfText){
                                            result.data.npcs[i].topic[j].contents[k].jsgfText = JSON.parse(result.data.npcs[i].topic[j].contents[k].jsgfText)
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
                                            result.data.npcs[i].topic[j].contents[k].jsgfText = JSON.parse(JSON.stringify(tmpObj));
                                        }

                                    }
                                }
                            }


                            _this.result = result.data
                        }
                    });
                }
            }
        });
    }());


    $(function(){
        $("#AppModel").on("click",'.fold_btn',function(){
            $(this).parent().next().slideToggle()
        });

        $("#AppModel").on("click",".edit_jsgf",function(){
            $(this).next().fadeToggle()
            $(".html_box").html('')
        })

        $("#AppModel").on("click",".jsgf-close",function(){
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
    })
</script>

</@layout_default.page>