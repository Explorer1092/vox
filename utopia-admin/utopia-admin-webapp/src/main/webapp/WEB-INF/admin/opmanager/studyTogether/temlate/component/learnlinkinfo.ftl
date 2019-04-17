<#import "../../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/utils.js"></script>
<style>
    input[type='text'] {
        -webkit-appearance: none;
        background-color: #FFF;
        border-radius: 4px;
        border: 1px solid #DCDFE6;
        box-sizing: border-box;
        color: #606266;
        display: inline-block;
        font-size: inherit;
        height: 30px;
        line-height: 40px;
        outline: 0;
        padding: 0 15px;
        -webkit-transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
        transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
        margin: 0;
        width: 100%;
    }
</style>
<div class="span9" id="app">
    <legend>
        <#if edit?? && edit == true>
            <#if template?? && template.id?has_content>编辑<#else >新增</#if>学习环节
        <#else >
            学习环节
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <legend class="field-title">基础信息</legend>
            <el-form label-position="right" label-width="170px" size="mini" :model="linkForm" :rules="rules"
                     ref="linkForm">
                <el-form-item label="学习环节名称：" prop="name">
                    <el-input :disabled="!edit" style="width: 240px;" v-model="linkForm.name" maxlength="50" placeholder="50字以内"></el-input>
                </el-form-item>
                <el-form-item label="环节ID：">
                    <el-input :disabled="!edit" style="width: 240px;" v-model="linkForm.linkId" disabled="true"></el-input>
                </el-form-item>
                <el-form-item label="创建人：" v-model="linkForm.createUser">
                    <el-span>{{linkForm.createUser}}</el-span>
                </el-form-item>
                <legend class="field-title">学习环节</legend>
                <el-form-item label="新增环节分类及环节：" required>
                    <el-form-item prop="linkClass" style="display: inline-block;">
                        <el-select :disabled="!edit" v-model="linkForm.linkClass" placeholder="选择环节分类" @change="linkForm.type=''">
                            <el-option
                                    v-for="item in linkOptions"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item prop="type" style="display: inline-block;">
                        <el-select :disabled="!edit" v-model="linkForm.type" placeholder="选择学习环节">
                            <el-option
                                    v-for="item in linkCompute"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-form-item>
                <div style="margin: 0 0 30px 50px;font-weight: bold;">环节内容</div>
                <el-form-item label="环节标题：" prop="title">
                    <el-input :disabled="!edit" style="width: 240px;" v-model="linkForm.title" maxlength="6"></el-input>
                </el-form-item>
                <el-form-item label="环节图片：" prop="img">
                    <el-input style="width: 240px;" v-model="linkForm.img" :disabled="true"></el-input>
                    <vue-upload accept="image/jpg" :model="['linkForm', 'img']" @result="uploadResult"></vue-upload>
                    <el-button type="primary" size="mini" @click="preview(linkForm.img)">预览</el-button>
                </el-form-item>
                <el-form label-position="right" label-width="170px" size="mini" :model="link_1_DIRECT_VIDEO"
                         :rules="rules" ref="link_1_DIRECT_VIDEO"
                         v-show="linkForm.linkClass==1 && linkForm.type=='DIRECT_VIDEO'">
                    <el-form-item label="上传视频：" prop="videoUrl" required>
                        <el-input style="width: 240px;" v-model="link_1_DIRECT_VIDEO.videoUrl"
                                  :disabled="true"></el-input>
                        <vue-upload accept="video/mp4" :model="['link_1_DIRECT_VIDEO', 'videoUrl']"
                                    @result="uploadResult"></vue-upload>
                        <el-button type="primary" size="mini" @click="preview(link_1_DIRECT_VIDEO.videoUrl)">预览
                        </el-button>
                    </el-form-item>
                    <el-form-item label="视频时长：" prop="duration">
                        <el-input :disabled="!edit" style="width: 80px;" v-model="link_1_DIRECT_VIDEO.duration"></el-input>
                        s
                    </el-form-item>
                </el-form>
                <el-form label-position="right" label-width="170px" size="mini" :model="link_1_INDIRECT_VIDEO"
                         :rules="rules" ref="link_1_INDIRECT_VIDEO"
                         v-show="linkForm.linkClass==1 && linkForm.type=='INDIRECT_VIDEO'">
                    <el-form-item label="内容库音视频课程ID：" prop="videoId">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="link_1_INDIRECT_VIDEO.videoId"></el-input>
                    </el-form-item>
                </el-form>
                <el-form label-position="right" label-width="170px" size="mini" :model="link_2_DIRECT_AUDIO"
                         :rules="rules" ref="link_2_DIRECT_AUDIO"
                         v-show="linkForm.linkClass==2 && linkForm.type=='DIRECT_AUDIO'">
                    <el-form-item label="子标题：" prop="subTitle">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="link_2_DIRECT_AUDIO.subTitle"></el-input>
                    </el-form-item>
                    <el-form-item label="作者：">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="link_2_DIRECT_AUDIO.author"></el-input>
                    </el-form-item>
                    <el-form-item label="背景图片：" prop="background">
                        <el-input style="width: 240px;" v-model="link_2_DIRECT_AUDIO.background"
                                  :disabled="true"></el-input>
                        <vue-upload accept="image/*" :model="['link_2_DIRECT_AUDIO', 'background']"
                                    @result="uploadResult"></vue-upload>
                        <el-button type="primary" size="mini" @click="preview(link_2_DIRECT_AUDIO.background)">预览
                        </el-button>
                    </el-form-item>
                    <el-form-item label="上传音频：" prop="audioUrl" required>
                        <el-input style="width: 240px;" v-model="link_2_DIRECT_AUDIO.audioUrl"
                                  :disabled="true"></el-input>
                        <vue-upload accept="audio/*" :model="['link_2_DIRECT_AUDIO', 'audioUrl']"
                                    @result="uploadResult"></vue-upload>
                        <el-button type="primary" size="mini" @click="preview(link_2_DIRECT_AUDIO.audioUrl)">预览
                        </el-button>
                    </el-form-item>
                    <el-form-item label="音频时长：" prop="duration">
                        <el-input style="width: 80px;" v-model="link_2_DIRECT_AUDIO.duration"></el-input>
                        s
                    </el-form-item>
                    <el-form-item label="文本内容类型">
                        <label for="title" class="content_type">
                            <el-radio-group v-model="link_2_DIRECT_AUDIO.contentType">
                                <el-radio :disabled="!edit" label="lrcContent">lrc文本</el-radio>
                                <el-radio :disabled="!edit" label="content">富文本</el-radio>
                            </el-radio-group>
                        </label>
                    </el-form-item>
                    <el-form-item label="文本内容：" v-show="link_2_DIRECT_AUDIO.contentType=='content'">
                        <label for="title" class="text_content">
                            <!-- 加载编辑器的容器 -->
                            <script id="content_link" type="text/plain"></script>
                        </label>
                    </el-form-item>
                    <el-form-item label="lrc文本内容：" v-show="link_2_DIRECT_AUDIO.contentType=='lrcContent'">
                        <label for="title" class="lrc_content">
                            <el-input :disabled="!edit" type="textarea" resize="both" rows="8" style="width: 240px;" v-model="link_2_DIRECT_AUDIO.lrcContent" placeholder="时间写在文段前面，拼音紧跟文字之后，且前后用#分隔,eg：
                                [00:01:34]太尉觉得奇怪，找来住持真人一问，
                                他战#zhàn#战#zhàn#兢#jīng#兢#jīng#地回答">
                            </el-input>
                        </label>
                    </el-form-item>
                </el-form>
                <el-form v-if="linkForm.linkClass==3" label-position="right" label-width="170px" size="mini"
                         :model="question" :rules="rules" ref="question">
                    <el-form-item label="环节是否计入打分：" required>
                        <el-radio-group v-model="question.needScore">
                            <el-radio :disabled="!edit" :label="1">是</el-radio>
                            <el-radio :disabled="!edit" :label="0">否</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="学习成果是否展示：" required>
                        <el-radio-group v-model="question.needResult">
                            <el-radio :disabled="!edit" :label="1">是</el-radio>
                            <el-radio :disabled="!edit" :label="0">否</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="题目ID：" prop="question">
                        <el-input :disabled="!edit" type="textarea" resize="both" style="width: 240px;" v-model="question.questionId"></el-input>
                    </el-form-item>
                </el-form>

                <el-form-item style="margin-top: 80px;">
                    <el-button type="info" @click="back">返回</el-button>
                    <el-button v-if="edit" style="margin-left: 60px;" type="primary" @click="submit">确定</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>

</div>
<script type="text/javascript">
    let ue_first;
    new Vue({
        el: '#app',
        data() {
            return {
                linkForm: {
                    name: '',
                    linkId: '',
                    linkClass: '',
                    type: '',
                    // content: '',
                    title: '',
                    img: '',
                    createUser: '',
                },
                rules: {
                    name: [{required: true, message: '请输入环节名称', trigger: 'blur'}],
                    linkClass: [{required: true, message: '请选择环节分类', trigger: 'change'}],
                    type: [{required: true, message: '请选择环节', trigger: 'change'}],
                    title: [{required: true, message: '请输入环节标题', trigger: 'blur'}, { min: 1, max: 6, message: '字数限制6个以内', trigger: 'blur' }],
                    img: [{required: true, message: '请上传图片', trigger: 'change'}],
                    background: [{required: true, message: '请上传图片', trigger: 'change'}],
                    videoUrl: [{required: true, message: '请上传视频', trigger: 'change'}],
                    audioUrl: [{required: true, message: '请上传音频', trigger: 'change'}],
                    duration: [{required: true, message: '请输入时长', trigger: 'blur'}],
                    videoId: [{required: true, message: '请输入ID', trigger: 'blur'}],
                    questionId: [{required: true, message: '请输入题目ID', trigger: 'blur'}],
                    subTitle: [{required: true, message: '请输入子标题', trigger: 'blur'}]
                },
                linkOptions: [
                    {
                        value: 1,
                        label: '视频分类',
                        option: [{label: '直接上传视频', value: 'DIRECT_VIDEO'}, {
                            label: '内容库轻交互音视频',
                            value: 'INDIRECT_VIDEO'
                        }]
                    },
                    {value: 2, label: '音频分类', option: [{label: '直接上传音频', value: 'DIRECT_AUDIO'}]},
                    {
                        value: 3,
                        label: '选择练习',
                        option: [{label: '单选无解析', value: 'SINGLE_WITHOUT_RESOLUTION'}, {
                            label: '单选有解析',
                            value: 'SINGLE_HAS_RESOLUTION'
                        }, {label: '一题多问', value: 'ONE_QUESTION_MORE_ASK'}]
                    }
                ],
                link_1_DIRECT_VIDEO: {
                    videoUrl: '',
                    duration: ''
                },
                link_1_INDIRECT_VIDEO: {
                    videoId: ''
                },
                link_2_DIRECT_AUDIO: {
                    subTitle: '',
                    author: '',
                    background: '',
                    audioUrl: '',
                    duration: '',
                    content: '',
                    lrcContent: '',
                    contentType: ''
                },
                question: {
                    needScore: 1,
                    needResult: 1,
                    questionId: ''
                },
                edit: 0,
                cdn_host: ''

            }
        },
        computed: {
            linkCompute: function () {
                console.log(this.linkForm.linkClass)
                if (this.linkForm.linkClass) {
                    return this.linkOptions[this.linkForm.linkClass - 1].option;
                }
                return [];
            }
        },
        mounted() {
            this.UEInit();
            this.edit = Number(getQuery().edit)
            this.getData();
        },
        methods: {
            UEInit() {
                ue_first = UE.getEditor('content_link', {
                    serverUrl: "../ueditorcontroller.vpage",
                    zIndex: 1040,
                    fontsize: [12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
                    toolbars: [[
                        'fullscreen', 'source', '|', 'undo', 'redo', '|',
                        'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                        'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                        'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                        'directionalityltr', 'directionalityrtl', 'indent', '|',
                        'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                        'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                        'simpleupload', 'pagebreak', '|',
                        'horizontal', 'date', 'time', 'spechars', '|', 'preview', 'searchreplace'
                    ]]
                });
                if (this.link_2_DIRECT_AUDIO.content !== '') {
                    ue_first.ready(() => {
                        ue_first.setContent(this.link_2_DIRECT_AUDIO.content.replace(/\n/g, '<p><br/></p>'));
                    });
                }
            },
            uploadResult(data, model) {
                if (data.success) {
                    let result = this;
                    for (let index = 0; index < model.length; index++) {
                        if (index == model.length - 1) {
                            result[model[index]] = data.fileName;
                            break;
                        }
                        result = result[model[index]];

                    }
                }
            },
            preview(url) {
                if (url != '') {
                    url = this.cdn_host + url;
                    window.open(url);
                }
            },
            submit() {
                // 提交表单
                this.$refs['linkForm'].validate((valid) => {
                    if (valid) {
                        this.$refs[this.linkForm.linkClass != '3' ? 'link_' + this.linkForm.linkClass + '_' + this.linkForm.type : 'question'].validate((val) => {
                            if (val) {
                                let obj = {};
                                if (this.linkForm.linkClass !== '' && this.linkForm.type !== '') {
                                    obj = this.linkForm.linkClass != '3' ? this['link_' + this.linkForm.linkClass + '_' + this.linkForm.type] : this.question
                                }
                                if (this.linkForm.linkClass == 2 && this.linkForm.type == 'DIRECT_AUDIO') {
                                    obj.content = ue_first.getContent();
                                }
                                let form = Object.assign({}, obj, this.linkForm);
                                console.log(form);
                                let formData = new FormData();
                                makeFormData(form, formData)
                                //保存，应该是这么写吧。。。
                                $.ajax({
                                    url: '/opmanager/studyTogether/link/save.vpage',
                                    data: formData,
                                    type: "POST",
                                    processData: false,
                                    contentType: false,
                                    success: (data) => {
                                        console.log(data)
                                        // 回调处理
                                        if (data.success) {
                                            this.$alert('保存成功', {
                                                callback: function () {
                                                    window.location.href = '/opmanager/studyTogether/link/index.vpage';
                                                }
                                            });
                                        } else {
                                            this.$alert("保存失败，原因：" + data.info);
                                        }
                                    }
                                })
                            } else {
                                this.$message.error('请检查表单信息是否符合规则！')
                            }
                        })
                    } else {
                        this.$message.error('请检查表单信息是否符合规则！')
                        return false;
                    }
                });

            },
            back() {
                window.location.href = '/opmanager/studyTogether/link/index.vpage';
            },
            getData() {
                console.log(getQuery().linkId);
                $.ajax({
                    url: '/opmanager/studyTogether/link/info_data.vpage?linkId=' + getQuery().linkId,
                    type: "GET",
                    processData: false,
                    contentType: false,
                    success: (data) => {
                        console.log(data.content);
                        // 回调处理
                        // this.linkForm = data;

                        this.linkForm = {
                            name: data.content.name,
                            linkId: data.content.id,
                            linkClass: 1,
                            type: data.content.type,
                            // content: 'huanjie',
                            title: '',
                            img: '',
                            createUser: data.content.createUser
                        };
                        this.cdn_host = data.cdn_host;
                        // 'linkClass' 'type' 逻辑
                        switch (data.content.type) {
                            case 'DIRECT_VIDEO':
                                this.linkForm.linkClass = 1;
                                this.linkForm.img = data.content.content.directVideoContent.pic;
                                this.linkForm.title = data.content.content.directVideoContent.title;
                                this.link_1_DIRECT_VIDEO.videoUrl = data.content.content.directVideoContent.videoUrl;
                                this.link_1_DIRECT_VIDEO.duration = data.content.content.directVideoContent.duration;
                                return;
                            case 'INDIRECT_VIDEO':
                                this.linkForm.linkClass = 1;
                                this.linkForm.img = data.content.content.indirectVideoContent.pic;
                                this.linkForm.title = data.content.content.indirectVideoContent.title;
                                this.link_1_INDIRECT_VIDEO.videoId = data.content.content.indirectVideoContent.videoId;
                                return;
                            case 'DIRECT_AUDIO':
                                this.linkForm.linkClass = 2;
                                this.linkForm.img = data.content.content.directAudioContent.pic;
                                this.linkForm.title = data.content.content.directAudioContent.title;
                                this.link_2_DIRECT_AUDIO.subTitle = data.content.content.directAudioContent.subTitle;
                                this.link_2_DIRECT_AUDIO.author = data.content.content.directAudioContent.author;
                                this.link_2_DIRECT_AUDIO.background = data.content.content.directAudioContent.image;
                                this.link_2_DIRECT_AUDIO.audioUrl = data.content.content.directAudioContent.audioUrl;
                                this.link_2_DIRECT_AUDIO.duration = data.content.content.directAudioContent.duration;
                                this.link_2_DIRECT_AUDIO.content = data.content.content.directAudioContent.content;
                                this.link_2_DIRECT_AUDIO.lrcContent = data.content.content.directAudioContent.lrcContent;
                                if (this.link_2_DIRECT_AUDIO.content !== '') {
                                    ue_first.ready(() => {
                                        ue_first.setContent(this.link_2_DIRECT_AUDIO.content.replace(/\n/g, '<p><br/></p>'));
                                    });
                                    this.link_2_DIRECT_AUDIO.contentType = 'content';
                                }
                                if (this.link_2_DIRECT_AUDIO.lrcContent !== null && this.link_2_DIRECT_AUDIO.lrcContent !== '') {
                                    this.link_2_DIRECT_AUDIO.contentType = 'lrcContent';
                                }
                                return;
                            case 'ONE_QUESTION_MORE_ASK':
                            case 'SINGLE_HAS_RESOLUTION':
                            case 'SINGLE_WITHOUT_RESOLUTION':
                                this.linkForm.linkClass = 3;
                                this.linkForm.img = data.content.content.chooseContent.pic;
                                this.linkForm.title = data.content.content.chooseContent.title;
                                let questionIds1 = data.content.content.chooseContent.questionId;
                                let idStr1 = questionIds1.join("#");
                                this.question.questionId = idStr1;
                                this.question.needResult = Number(data.content.content.chooseContent.needResult || 0);
                                this.question.needScore = Number(data.content.content.chooseContent.needScore || 0);
                                return;
                            default:
                                return;
                        }
                    }
                })
            }
        }
    })
</script>
</@layout_default.page>