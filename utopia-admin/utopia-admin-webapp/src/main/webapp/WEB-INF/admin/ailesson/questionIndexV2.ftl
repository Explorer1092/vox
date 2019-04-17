<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='用户答题结果查询V2' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style type="text/css">
    [v-cloak]{display: none;}
    .main_content{min-height: 500px;border: 1px solid #ddd;border-radius: 10px;padding: 20px;margin-top: 15px;background: #fff;}
    audio{width:150px;}

    table {
        text-align: center;
        border: 1px #cecece solid;
        border-radius: 8px;
        width: 100%;
        table-layout:fixed;
        word-break:break-all;
        border-collapse: separate;
    }
    table thead {
        background-color: #dadada;
    }
    table tr td, table tr th {
        border-right: 1px rgb(206, 206, 206) solid;
        border-bottom:1px rgb(206, 206, 206) solid;
    }
    table tr td:last-child, table tr th:last-child {
        /*border-right: none;*/
    }
    table tr:last-child td {
        border-bottom: none; 
    }
</style>


<div class="main span10" id="search_result" v-cloak>
    <h3 style="padding: 0;margin: 0;">用户答题结果查询V2</h3>
    <hr style="margin-top: 5px;">

    <div class="form-inline" style="padding-bottom: 15px;">
        <span>用户ID：</span>
        <input type="text" v-model="user_id" placeholder="用户ID">
        <span class="btn btn-success" @click="get_books">查询</span>
    </div>

    <span class="form-inline">
        <span>教材：</span>
        <select v-model="book_id" @change="get_units">
            <option value="">请选择</option>
            <option v-bind:value="item.bookId" v-for="item in books">{{ item.bookName }}</option>
        </select>
    </span>

    <span class="form-inline">
        <span>类型：</span>
        <select v-model="unit_type" @change="get_units">
            <option value="">请选择</option>
            <option value="topic_learning">话题学习</option>
            <option value="special_consolidation">专项巩固</option>
            <option value="dialogue_practice">对话实战</option>
            <option value="essential_to_pass">过考必备</option>
            <option value="mock_test">模拟考</option>
            <option value="short_lesson">短期旅行口语</option>
            <option value="unknown">未定义</option>
        </select>
    </span>

    <span class="form-inline">
        <span>单元：</span>
        <select v-model="unit_index" >
            <option value="-1">请选择</option>
            <option v-bind:value="index" v-for="(item,index) in units">{{ item.unitName }}</option>
        </select>
    </span>

    <#--<span class="form-inline">-->
        <#--<span>课程：</span>-->
        <#--<select class="span2" v-model="lesson_id">-->
            <#--<option value="">请选择</option>-->
            <#--<option v-bind:value="item.lessonId" v-for="item in lessons">{{ item.lessonName }} - {{ item.lessonScore }}</option>-->
        <#--</select>-->
    <#--</span>-->

    <span class="btn btn-success" @click="get_more_message">确定</span>

    <div class="main_content">
        <span>用户名：{{ username }}</span>
        <#--<span style="margin-left: 30px;">产品：</span>-->
        <#--<span style="margin-left: 30px;">课程：</span>-->
        <hr>
        <h4>答题详情</h4>
        <div>
            <table cellpadding="10">
                <thead>
                    <tr>
                        <th style="width: 100px;">序号</th>
                        <th>题目</th>
                        <th style="width: 40px;">回答次数</th>
                        <th>用户回答</th>
                        <th style="width: 150px;">作答时间</th>
                        <th>得分</th>
                        <th>参考答案</th>
                        <th style="width: 8%;">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <template v-for="(row, i) in tableData">
                        <#-- 不支持的类型 -->
                        <#-- <tr v-if="!support_schemas[row.schemaName]">
                            <td colspan="6" style="color: red; padding: 10px;">这是一个不支持的题目类型：{{ row.schemaName }}</td>
                        </tr> -->
                        <#-- 模拟考 问答 -->
                        <tr v-if="row.schemaName === 'mock_qa'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div>
                                    <div v-html="row.question.jsonData.question_content"></div>
                                    <a :href="row.question.jsonData.mock_qa_video" target="_blank">[提问视频]</a>
                                </div>
                                <br/>
                                <a :href="row.question.jsonData.feedback_video" target="_blank">[等待视频]</a>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <audio controls v-bind:src="item.userAudio">您的浏览器不支持 audio 标签。</audio>
                                <#-- <div>{{ item.userText }}</div> -->
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                                <text-adapter :voice-engine-json="item.voiceEngineJson"></text-adapter>
                            </td>
                            <td>
                                <div>
                                    <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                        {{ item.level || '--' }}
                                    </span>
                                </div>
                                <div>
                                    {{ item.sample.toLowerCase() }}
                                </div>
                            </td>
                            <td  style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.question_content,row.lessonId)">主动服务</span>
                                <span  v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.question_content,row.lessonId)">已服务</span>
                            </td>
                        </tr>


                        <#-- 模拟考 选择 -->
                        <tr v-if="row.schemaName === 'mock_choice'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <a :href="row.question.jsonData.mock_choice_video" target="_blank">mock_choice_video</a>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <a target="_blank" v-bind:href="item.userAnswer"><img style="height: 100px" v-bind:src="item.userAnswer"/></a>
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                            </td>
                            <td>
                                <template v-for="item in row.question.jsonData.options">
                                    <template v-if="item.is_correct">
                                        <a target="_blank" v-bind:href="item.option"><img style="height: 100px" v-bind:src="item.option"/></a>
                                    </template>
                                </template>
                            </td>
                            <td  style="vertical-align: top;">
                                <span  v-if="row.activeServiceFlag" class="btn btn-success "  @click="activeService(resultUserId,row.id,item.id,row.aids,'',row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,'',row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 话题学习 情景导入 选择题 -->
                        <tr v-if="row.schemaName === 'choice_lead_in'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.check_quetions[0].content"></div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <div>{{ item.userAnswer }}</div>
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                            </td>
                            <td>
                                <template v-for="item in row.question.jsonData.check_quetions[0].options">
                                    <template v-if="item.is_correct">
                                        <p>{{ item.option }}</p>
                                    </template>
                                </template>
                            </td>
                            <td  style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.check_quetions[0].content,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.check_quetions[0].content,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 话题学习 热身训练 单词跟读 -->
                        <tr v-if="row.schemaName === 'word_repeat'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                 <div>
                                     {{ row.question.jsonData.word }}
                                 </div>
                                 <div>
                                     {{ row.question.jsonData.word_cn }}
                                 </div>
                                 <div>
                                    <a target="_blank" v-bind:href="row.question.jsonData.word_image">
                                        <img style="height: 100px" v-bind:src="row.question.jsonData.word_image"/>
                                    </a>
                                 </div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <audio controls v-bind:src="item.userAudio">{{ item.userAudio }}</audio>
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                                <text-adapter :voice-engine-json="item.voiceEngineJson"></text-adapter>
                            </td>
                            <td>
                                <audio controls v-bind:src="row.question.jsonData.word_audio">{{ row.question.jsonData.word_audio }}</audio>    
                            </td>
                            <td style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.word,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.word,row.lessonId)">已服务</span>
                            </td>
                        </tr>
                        
                        <#-- 话题学习 热身训练 句子跟读 -->
                        <tr v-if="row.schemaName === 'sentence_repeat'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.sentence"></div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <audio controls v-bind:src="item.userAudio">{{ item.userAudio }}</audio>
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                                <text-adapter :voice-engine-json="item.voiceEngineJson"></text-adapter>
                            </td>
                            <td>
                                <audio controls v-bind:src="row.question.jsonData.sentence_audio">{{ row.question.jsonData.sentence_audio }}</audio>    
                            </td>
                            <td style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.sentence,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.sentence,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 专项巩固 词汇拓展 听单词选图 -->
                        <tr v-if="row.schemaName === 'choice_word2pic'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.content"></div>
                                <div>
                                    {{ row.question.jsonData.content_cn }}
                                </div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <a target="_blank" v-bind:href="item.userAnswer"><img style="height: 100px" v-bind:src="item.userAnswer"/></a>
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                            </td>
                            <td>
                                <template v-for="item in row.question.jsonData.options">
                                    <template v-if="item.is_correct">
                                        <a target="_blank" v-bind:href="item.option"><img style="height: 100px" v-bind:src="item.option"/></a>
                                    </template>
                                </template>
                            </td>
                            <td  style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 专项巩固 词汇拓展 听句子选图 -->
                        <tr v-if="row.schemaName === 'choice_sentence2pic'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.content"></div>
                                <div>
                                     {{ row.question.jsonData.content_cn }}
                                </div>
                                <div>
                                     <audio controls v-bind:src="row.question.jsonData.content_audio">{{ row.question.jsonData.content_audio }}</audio>
                                </div>   
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <a target="_blank" v-bind:href="item.userAnswer"><img style="height: 100px" v-bind:src="item.userAnswer"/></a>
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                            </td>
                            <td>
                                <template v-for="item in row.question.jsonData.options">
                                    <template v-if="item.is_correct">
                                        <a target="_blank" v-bind:href="item.option"><img style="height: 100px" v-bind:src="item.option"/></a>
                                    </template>
                                </template>
                            </td>
                            <td style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 专项巩固 词汇拓展 听单词选翻译 -->
                        <tr v-if="row.schemaName === 'choice_word2trans'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.content"></div>
                                <div>
                                     {{ row.question.jsonData.content_cn }}
                                </div>
                                <div>
                                     <audio controls v-bind:src="row.question.jsonData.content_audio">{{ row.question.jsonData.content_audio }}</audio>
                                </div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                {{ item.userAnswer }}
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                            </td>
                            <td>
                                <template v-for="item in row.question.jsonData.options">
                                    <template v-if="item.is_correct">
                                        {{ item.option }}
                                    </template>
                                </template>
                            </td>
                            <td style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 专项巩固 词汇拓展 句子选音频 -->
                        <tr v-if="row.schemaName === 'choice_sentence2audio'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.content"></div>
                                <div>
                                     <audio controls v-bind:src="row.question.jsonData.content_audio">{{ row.question.jsonData.content_audio }}</audio>
                                </div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                {{ item.userAnswer }}
                                <#--<audio controls v-bind:src="item.userAnswer || item.userAudio">{{ item.userAnswer }}</audio>-->
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                            </td>
                            <td>
                                <template v-for="item in row.question.jsonData.options">
                                    <template v-if="item.is_correct">
                                        {{ item.option_en }}
                                        <#--<audio style="height: 50px;" controls v-bind:src="item.option">{{ item.option }}</audio>-->
                                    </template>
                                </template>
                            </td>
                            <td style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 专项巩固 词汇拓展 句子问答 -->
                        <tr v-if="row.schemaName === 'qa_sentence'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.content"></div>
                                <div>
                                     <audio controls v-bind:src="row.question.jsonData.audio">{{ row.question.jsonData.content_audio }}</audio>
                                </div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <div>
                                    <audio style="height: 50px;" controls v-bind:src="item.userAnswer || item.userAudio">{{ item.userAnswer }}</audio>
                                </div>
                               <#--  <div>
                                    {{ item.userText }}
                                </div> -->
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                                <text-adapter :voice-engine-json="item.voiceEngineJson"></text-adapter>
                            </td>
                            <td>
                                <div>
                                    <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                        {{ item.level || '--' }}
                                    </span>
                                </div>
                                <div>
                                    {{ item.sample.toLowerCase() }}
                                </div>
                            </td>
                            <td style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 对话实战 任务对话 -->
                        <tr v-if="row.schemaName === 'task_conversation'  || row.schemaName === 'task_topic'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-if="row.question.jsonData.subjectId !== null" >
                                    {{ row.question.jsonData.translation || '/' }}
                                </div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <div>
                                    <audio style="height: 50px;" controls v-bind:src="item.userAnswer || item.userAudio">{{ item.userAnswer }}</audio>
                                </div>
                                <#-- <div>
                                    {{ item.userText }}
                                </div> -->
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                                <text-adapter :voice-engine-json="item.voiceEngineJson"></text-adapter>
                            </td>
                            <td>
                                <div>
                                    <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                        {{ item.level || '--' }}
                                    </span>
                                </div>
                                <div>
                                    {{ item.sample.toLowerCase() }}
                                </div>
                            </td>
                            <td style="vertical-align: top;">
                                <template v-if="row.question.jsonData.subjectId !== null">
                                    <span  v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.translation,row.lessonId)">主动服务</span>
                                    <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.translation,row.lessonId)">已服务</span>
                                </template>
                                <template v-else>
                                    <span  v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,'',row.lessonId)">主动服务</span>
                                    <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,'',row.lessonId)">已服务</span>
                                </template>
                            </td>
                        </tr>

                        <#-- 对话实战 文化题单选 -->
                        <tr v-if="row.schemaName === 'choice_cultural'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">
                                <div v-html="row.question.jsonData.content"></div>
                                <div>
                                     <audio controls v-bind:src="row.question.jsonData.content_audio">{{ row.question.jsonData.content_audio }}</audio>
                                </div>
                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                {{ item.userAnswer }}
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                            </td>
                            <td>
                                <template v-for="item in row.question.jsonData.options">
                                    <template v-if="item.is_correct">
                                        {{ item.option }}
                                    </template>
                                </template>
                            </td>
                            <td style="vertical-align: top;">
                                <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">主动服务</span>
                                <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.content,row.lessonId)">已服务</span>
                            </td>
                        </tr>

                        <#-- 视频对话 -->
                        <tr v-if="row.schemaName === 'video_dialogue' || row.schemaName === 'video_conversation'" v-for="(item, rowIndex) in row.answer">
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">{{ row.customName || '/' }}</td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length" style="vertical-align: top;">

                                <div v-if="row.question.jsonData.subjectId !== null" >
                                    {{ row.question.jsonData.translation || '/' }}
                                </div>

                            </td>
                            <td v-if="rowIndex === 0" :rowspan="row.answer.length">{{ row.answerCount }}</td>
                            <td>
                                <audio controls v-bind:src="item.userAudio">您的浏览器不支持 audio 标签。</audio>
                                <#-- <div>{{ item.userText }}</div> -->
                            </td>
                            <td>
                                {{ item.createDate | format_time }}
                            </td>
                            <td>
                                <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                    {{ item.score || item.originScore }}
                                </span>
                                <text-adapter :voice-engine-json="item.voiceEngineJson"></text-adapter>
                            </td>
                            <td>
                                <div>
                                    <span style="background-color: #3c3c3c;display: inline-block;padding: 2px 5px;color: white;border-radius: 10px;">
                                        {{ item.level || '--' }}
                                    </span>
                                </div>
                                <div>{{ item.sample.toLowerCase() }}</div>
                            </td>
                            <td style="vertical-align: top;">
                                <template v-if="row.question.jsonData.subjectId !== null" >
                                    <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.translation,row.lessonId)">主动服务</span>
                                    <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,row.question.jsonData.translation,row.lessonId)">已服务</span>
                                </template>
                                <template v-else>
                                    <span v-if="row.activeServiceFlag" class="btn btn-success " @click="activeService(resultUserId,row.id,item.id,row.aids,'',row.lessonId)">主动服务</span>
                                    <span v-else class="btn btn-info " @click="activeService(resultUserId,row.id,item.id,row.aids,'',row.lessonId)">已服务</span>
                                </template>

                            </td>
                        </tr>
                    </template>
                    <tr v-if="tableData.length === 0">
                        <td colspan="6" style="text-align: center;padding: 100px;color: gray;font-size: 20px;">无数据！</td>
                    </tr>
                </tbody>
            </table>
            <div style="height: 50px;"></div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var vm = new Vue({
        el:"#search_result",
        data:{
            msg:"hello",
            username:'',
            user_id:'',
            book_id:'',
            lesson_id:'',
            unit_index:-1,
            unit_type:'',
            books:[],
            units:[],
//            lessons:[],
            list:[],
            unit_id:"",//记录回填的uint_id
            support_schemas: {
                mock_qa: '模拟考问答',
                mock_choice: '模拟考选择',
                choice_lead_in: '情境导入选择',
                word_repeat: '单词跟读',
                sentence_repeat: '句子跟读',
                video_conversation: '视频对话',
                choice_word2pic: '听单词选图',
                choice_sentence2pic: '听句子选图',
                choice_word2trans: '听单词选翻译',
                choice_sentence2audio: '句子选音频',
                qa_sentence: '句子问答',
                task_conversation: '任务对话',
                choice_cultural: '文化题单选',

                video_dialogue: '视频对话',
                task_topic: '任务话题'

                // 下面是不支持的
                // choice_cultural2pic: '文化题选图',
                // warm_up_vocabs: '热身训练单词封面',
                // chip_english_tips: '小贴士',
                // knowledge_expand_review: '知识拓展回顾',
                // video_lesson_to_pass: '过考必备',
                // result_page_to_pass: '过考必备结果'
            },
            resultUserId:"",
            resultBookId:"",
            resultUnitId:"",
            resultLessonId:"",

        },
        computed: {
            tableData: function() {
                var thiz = this;
                var tableData = [];
                console.log(this.list);
                this.list.forEach(function(e){
                    if(!thiz.support_schemas[e.question.schemaName]) {
                        return;
                    }
                    e.question.jsonData = JSON.parse(e.question.jsonData || '{}');
                    tableData.push({
                        id: e.question.id,
                        schemaName: e.question.schemaName,
                        customName: e.question.customName,
                        question: e.question,
                        answer: e.answer,
                        activeServiceFlag:e.activeServiceFlag,
                        aids:e.aids,
                        lessonId:e.lessonId,
                        answerCount:e.answerCount,
                    });
                }); 
                return tableData;
            }
        },
        methods: {
            get_books:function(){
                var _this = this;
//                _this.user_id = 268842;
                var userId = _this.user_id;
                if(!userId) {
                    alert('请输入用户ID!');
                    return;
                }
                $.get("/chips/user/question/books.vpage",{
                    userId: userId
                }).then(function(res){
                    if(res.success){
                        if(res.username === '该用户不存在') {
                            alert('该用户不存在!');
                            return;
                        }
                        if(res.list.length === 0) {
                            alert('该用户账号下产品为空!');
                            return;
                        }
                        _this.books = res.list;
                        _this.username = res.username;
                        _this.units = [];
//                        _this.lessons = [];
                        _this.unit_type = '';
                        _this.unit_index = -1;
                    }else{
                        alert(res.info)
                    }
                });
            },
            get_units: function(flag) {
                var _this = this;
                if(!this.user_id || !this.book_id) {
                    return;
                }
                $.get("/chips/user/question/lessons.vpage",{
                    userId:_this.user_id,
                    bookId:_this.book_id,
                    unitType:_this.unit_type,
                    unitId:_this.unit_id
                }).then(function(res){
                    if(res.success) {
                        _this.units = res.list;
                        _this.unit_index = res.unitIndex;
//                        _this.lessons = res.unitIndex === -1 ? [] : _this.units[_this.unit_index].lessons;
                        if(flag === true){
                            console.log("call get_more_message")
                            _this.get_more_message()
                        }
                    }else {
                        console.warn('/chips/user/question/lessons.vpage  res.info:', res.info);
                    }
                });
            },
//            get_lessons: function() {
//                var _this = this;
//                if(_this.unit_index > -1){
//                    _this.lessons = _this.units[_this.unit_index].lessons;
//                }
//            },
            get_more_message: function() {
                var _this = this;
                if(!this.user_id) {
                    alert('请填写用户ID!');
                    return;
                }
                console.log("_this.units[_this.unit_index]")
                console.log(_this.unit_index)
                console.log(_this.units)
                console.log(_this.units[_this.unit_index])
                $.post("/chips/user/question/userAnswerV2.vpage",{
                    userId:_this.user_id,
                    bookId:_this.book_id,
                    unitId:_this.units[_this.unit_index].unitId
                }).then(function(res){
                    if(res.success) {
                        console.log(res)
                        _this.list = res.list;
                        _this.resultUserId = res.userId;
                        _this.resultBookId = _this.book_id;
                        _this.resultLessonId = _this.lesson_id;
                        if(_this.unit_index === -1){//回填的userId
                            _this.resultUnitId = _this.unit_id;
                        } else { //下拉框change后的
                            _this.resultUnitId = _this.units[_this.unit_index].unitId;
                        }
                    }
                })
            },
            activeService:function (userId, qid, aid, aids, questionName,lessonId) {
                console.log("lessonId: " + lessonId)
                _this = this;
                $.get("/chips/ai/active/service/haveQuestionTemplate.vpage",{
                    qid:qid,
                }).then(function(res){
                    console.log("/chips/ai/active/service/userTemplateIndex.vpage?userId=" + userId + "&qid=" + qid
                            + "&bookId=" + _this.resultBookId + "&unitId=" + _this.resultUnitId + "&lessonId=" + lessonId
                            + "&aid=" + aid + "&aids=" + aids + "&unitType=" + _this.unit_type + "&questionName=" + questionName)
                    if(res.success) {
                        window.location.href = "/chips/ai/active/service/userTemplateIndex.vpage?userId=" + userId + "&qid=" + qid
                                + "&bookId=" + _this.resultBookId + "&unitId=" + _this.resultUnitId + "&lessonId=" + lessonId
                                + "&aid=" + aid + "&aids=" + aids + "&unitType=" + _this.unit_type + "&questionName=" + questionName;
                    }else {
                        alert( res.info);
                    }
                });
            }
        },
        created: function() {
            var _this = this;
            var userId = getUrlParam("userId");
            var bookId = getUrlParam("bookId");
            var unitId = getUrlParam("unitId");
            var unitType = getUrlParam("unitType");
            console.log(userId)
            console.log(bookId)
            console.log(unitId)
            console.log(unitType)
            if(userId){
                $.get("/chips/user/question/books.vpage",{
                    userId:userId
                }).then(function(res){
                    if(res.success){
                        _this.books = res.list;
                        _this.username = res.username;
                        _this.units = [];
//                        _this.lessons = [];
                        _this.unit_type = unitType;
//                        _this.unit_index = -1;
                        _this.user_id= userId;
                        _this.book_id = bookId;
                        _this.unit_id = unitId;
                        _this.get_units(true);
                        console.log(_this)
                        console.log(_this.unit_id)
//                        if(_this.unit_id) {
//                            _this.get_more_message();
//                        }
//                        _this.unit_index = 2;
//                        _this.list = []
                    }else{
                        alert(res.info)
                    }
                });
            }
        }
    });

    Vue.filter('format_time', function(timestamp) {
        function numberAdapter(n) {
            if(n < 10) {
                return '0' + n;
            }
            return n;
        }
        var date = new Date(timestamp);
        var Y = date.getFullYear() + '-';
        var M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
        var D = numberAdapter(date.getDate()) + ' ';
        var h = numberAdapter(date.getHours()) + ':';
        var m = numberAdapter(date.getMinutes()) + ':';
        var s = numberAdapter(date.getSeconds());
        return Y+M+D+h+m+s;
    });

    Vue.component('text-adapter', {
        props: ['voiceEngineJson'],
        computed: {
            voiceEngineObj: function() { return JSON.parse(this.voiceEngineJson || '{}') }
        },
        methods: {
            getColor: function(score) {
                if(0 < score && score <= 3) {
                    return 'red';
                }else if(3 < score && score <= 7) {
                    return 'black';
                }else if(7 < score && score <= 10) {
                    return '#38d200';
                }
            }
        },
        template: `<div>
            <div v-for="item in voiceEngineObj.lines">
                <span v-for="sub_item in item.words" v-bind:style="{color: getColor(sub_item.score)}">{{ sub_item.text.toLowerCase() }}</span>
            </div>
        </div>`
    });

    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }
</script>

</@layout_default.page>