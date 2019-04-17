<#import "../../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
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
            <#if template?? && template.id?has_content>编辑<#else >新增</#if>课程内容模板
        <#else >
            课程内容模板
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <legend class="field-title">基础信息</legend>
            <el-form label-position="right" label-width="140px" :model="baseForm" size="mini" :rules="rules"
                     ref="baseForm">
                <el-form-item label="模板名称：" prop="name">
                    <el-input :disabled="!edit" style="width: 240px;" v-model="baseForm.name" maxlength="50" placeholder="50字以内"></el-input>
                </el-form-item>
                <el-form-item label="模板皮肤类型：" prop="template_type">
                    <el-select :disabled="!edit" style="width: 240px;" v-model="baseForm.template_type" placeholder="请选择">
                        <el-option label="国学文本类型" value='SinologyText'></el-option>
                        <el-option label="英语绘本ID类型" value='PicBookId'></el-option>
                        <el-option label="书本音频类型" value='BookAudio'></el-option>
                        <el-option label="图文阅读统计类型" value='ImgTextReading'></el-option>
                        <el-option label="书本图文类型" value='BookImgText'></el-option>
                    </el-select>
                    <el-button type="primary" size="mini" @click="jumpPreviewPage(baseForm.template_type)">类型展示预览</el-button>
                </el-form-item>
                <el-form-item label="创建人：">
                    <span>{{baseForm.createUser}}</span>
                </el-form-item>
                <legend class="field-title">课节目标</legend>
                <el-form v-if="baseForm.template_type=='SinologyText'" label-position="right" label-width="140px"
                         :model="courseForm_SinologyText" :rules="rules" ref="courseForm_SinologyText"
                         size="mini">
                    <el-form-item label="标题：" prop="title">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="courseForm_SinologyText.title" maxlength="8" placeholder="最多输入8字"></el-input>
                    </el-form-item>
                    <el-form-item label="作者：" prop="author">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="courseForm_SinologyText.author"></el-input>
                    </el-form-item>
                    <el-form-item label="目标描述：">
                        <el-input :disabled="!edit" type="textarea" resize="both" style="width: 240px;" v-model="courseForm_SinologyText.target" maxlength="60" placeholder="最多输入60字"></el-input>
                    </el-form-item>
                    <el-form-item label="上传音频：">
                        <el-input style="width: 240px;" v-model="courseForm_SinologyText.audioUrl"
                                  :disabled="true"></el-input>
                        <vue-upload accept="audio/*" :model="['courseForm_SinologyText', 'audioUrl']"
                                    @result="uploadResult"></vue-upload>
                        <el-button type="primary" size="mini" @click="preview(courseForm_SinologyText.audioUrl)">预览
                        </el-button>
                    </el-form-item>
                    <el-form-item label="音频时长：">
                        <el-input :disabled="!edit" style="width: 80px;" v-model="courseForm_SinologyText.audioTime"></el-input>
                        s
                    </el-form-item>
                    <el-form-item label="文本内容：">
                        <vue-add :result.sync="courseForm_SinologyText.textContent"></vue-add>
                    </el-form-item>
                </el-form>
                <el-form v-if="baseForm.template_type=='PicBookId'" label-position="right" label-width="140px"
                         :model="courseForm_PicBookId" :rules="rules" ref="courseForm_PicBookId"
                         size="mini">
                    <el-form-item label="标题：" prop="title">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="courseForm_PicBookId.title" maxlength="8" placeholder="最多输入8字"></el-input>
                    </el-form-item>
                    <el-form-item label="目标描述：" prop="goalDetail">
                        <el-input :disabled="!edit" type="textarea" resize="both" style="width: 240px;" v-model="courseForm_PicBookId.goalDetail" maxlength="60" placeholder="最多输入60字"></el-input>
                    </el-form-item>
                    <el-form-item label="绘本ID：" prop="pictureBookId">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="courseForm_PicBookId.pictureBookId"></el-input>
                    </el-form-item>
                </el-form>
                <el-form v-if="baseForm.template_type=='BookAudio'" label-position="right" label-width="140px"
                         :model="courseForm_BookAudio" :rules="rules" ref="courseForm_BookAudio"
                         size="mini">
                    <el-form-item label="标题：" prop="title">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="courseForm_BookAudio.title" maxlength="8" placeholder="最多输入8字"></el-input>
                    </el-form-item>
                    <el-form-item label="目标描述：" prop="goalDetail">
                        <el-input :disabled="!edit" type="textarea" resize="both" style="width: 240px;" v-model="courseForm_BookAudio.goalDetail" maxlength="60" placeholder="最多输入60字"></el-input>
                    </el-form-item>
                    <el-form-item label="上传封面：" prop="coverImgUrl">
                        <el-input style="width: 240px;" v-model="courseForm_BookAudio.coverImgUrl"
                                  :disabled="true"></el-input>
                        <vue-upload accept="image/*" :model="['courseForm_BookAudio', 'coverImgUrl']"
                                    @result="uploadResult"></vue-upload>
                        <el-button type="primary" size="mini" @click="preview(BookAudio.coverImgUrl)">预览</el-button>
                    </el-form-item>
                    <el-form-item label="上传图片+音频：" required>
                        <vue-upload accept="audio/*|image/*" :limit="+2" type="1"
                                    :model="['courseForm_BookAudio', 'sentenceList']"
                                    @result="uploadResult"></vue-upload>
                        <span style="font-size: 12px;color: #ff0000;">请同时选择图片和音频，并保证命名对应一致</span>
                        <el-table
                                v-if="courseForm_BookAudio.sentenceList.length > 0"
                                :data="courseForm_BookAudio.sentenceList"
                                border
                                stripe
                                style="width: 100%">
                            <el-table-column
                                    label="图片列表">
                                <template scope="scope">
                                    <el-button @click="preview(scope.row.imgUrl)" type="text" size="small">预览
                                    </el-button>
                                </template>
                            </el-table-column>
                            <el-table-column
                                    label="音频列表">
                                <template scope="scope">
                                    <el-button @click="preview(scope.row.audioUrl)" type="text" size="small">预览
                                    </el-button>
                                </template>
                            </el-table-column>
                            <el-table-column
                                    prop="audioSeconds"
                                    width="130"
                                    label="音频时长">
                                <template scope="scope">
                                    <el-input :disabled="!edit" style="width:80px;display: inline-block;" v-model="scope.row.audioSeconds"></el-input> s
                                </template>
                            </el-table-column>
                            <el-table-column
                                    prop="sentence"
                                    label="文本内容">
                                <template scope="scope">
                                    <el-input :disabled="!edit" v-model="scope.row.sentence"></el-input>
                                </template>
                            </el-table-column>
                            <el-table-column
                                    prop="rank"
                                    label="展示顺序">
                                <template scope="scope">
                                    <el-input :disabled="!edit" v-model="scope.row.rank" placeholder="整数填写"></el-input>
                                </template>
                            </el-table-column>
                            <el-table-column
                                    label="操作">
                                <template scope="scope">
                                    <el-button @click="handelRemove(scope.$index, scope.row)" type="text" size="small">
                                        删除
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form-item>

                </el-form>
                <el-form v-if="baseForm.template_type=='ImgTextReading'" label-position="right" label-width="140px"
                         :model="courseForm_ImgTextReading" :rules="rules" ref="courseForm_ImgTextReading"
                         size="mini">
                    <el-form-item label="字数：">
                        <el-input :disabled="!edit" style="width: 80px;" v-model="courseForm_ImgTextReading.wordCount"></el-input>
                        个
                    </el-form-item>
                    <el-form-item label="推荐阅读时长：">
                        <el-input :disabled="!edit" style="width: 80px;" v-model="courseForm_ImgTextReading.generalTimeString"></el-input>
                        min
                    </el-form-item>
                    <el-form-item label="知识点数量：">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="courseForm_ImgTextReading.knowledgeCount" placeholder="正整数"></el-input>
                        个
                    </el-form-item>
                    <el-form-item label="上传封面：" prop="coverImgUrl">
                        <el-input  style="width: 240px;" v-model="courseForm_ImgTextReading.coverImgUrl"
                                  :disabled="true"></el-input>
                        <vue-upload accept="image/*" :model="['courseForm_ImgTextReading', 'coverImgUrl']"
                                    @result="uploadResult"></vue-upload>
                        <el-button type="primary" size="mini" @click="preview(courseForm_ImgTextReading.coverImgUrl)">预览
                        </el-button>
                    </el-form-item>
                    <el-form-item label="课节导语：" prop="introduction">
                        <el-input :disabled="!edit" type="textarea" resize="both" style="width: 240px;" v-model="courseForm_ImgTextReading.introduction"></el-input>
                    </el-form-item>
                </el-form>
                <el-form v-if="baseForm.template_type=='BookImgText'" label-position="right" label-width="140px"
                         :model="courseForm_BookImgText" :rules="rules" ref="courseForm_BookImgText"
                         size="mini">
                    <el-form-item label="标题：" prop="title" maxlength="8">
                        <el-input :disabled="!edit" style="width: 240px;" v-model="courseForm_BookImgText.title" maxlength="8" placeholder="最多输入8字"></el-input>
                    </el-form-item>
                    <el-form-item label="目标描述：" prop="goalDetail">
                        <el-input :disabled="!edit" type="textarea" resize="both" style="width: 240px;" v-model="courseForm_BookImgText.goalDetail" maxlength="60" placeholder="最多输入60字"></el-input>
                    </el-form-item>
                    <el-form-item label="上传封面：" prop="coverImgUrl">
                        <el-input style="width: 240px;" v-model="courseForm_BookImgText.coverImgUrl"
                                  :disabled="true"></el-input>
                        <vue-upload accept="image/*" :model="['courseForm_BookImgText', 'coverImgUrl']"
                                    @result="uploadResult"></vue-upload>
                        <el-button type="primary" size="mini" @click="preview(courseForm_BookImgText.coverImgUrl)">预览
                        </el-button>
                    </el-form-item>
                </el-form>
                <legend class="field-title">关联学习环节</legend>
                <el-form-item label="关联学习环节ID：">
                    <el-select :disabled="!edit" v-model="linkId" filterable :filter-method="getLinkId" placeholder="请选择"
                               @change="linkIdChange">
                        <el-option
                                v-for="item in linkIdList"
                                :key="item.id"
                                :label="item.name"
                                :value="item.id">
                        </el-option>
                    </el-select>
                    <el-button type="primary" size="mini" @click="linkInfo">新建学习环节</el-button>
                </el-form-item>
                <el-form-item v-for="(item, index) in baseForm.link_ids" :label="'学习环节' + (index + 1)" :required="index==0?'link_ids':false">
                    <el-input :disabled="!edit" style="width: 240px;" v-model="baseForm.link_ids[index]"></el-input>
                    <el-button v-if="index===0" type="primary" size="mini" @click="add()">添加</el-button>
                    <el-button v-else type="primary" size="mini" @click="remove(index)">删除</el-button>
                    <el-button type="primary" size="mini" @click="linkInfo(baseForm.link_ids[index])">环节编辑</el-button>
                    <span style="padding: 0 20px;" v-if="link_info[baseForm.link_ids[index]]">类型：{{ link_info[baseForm.link_ids[index]].type | filterLinkInfo }}</span>
                </el-form-item>
                <el-form-item style="margin-top: 80px;">
                    <el-button type="info" @click="back">返回</el-button>
                    <el-button style="margin-left: 60px;" type="primary" @click="submit" v-if="edit">确定</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>

</div>
<script type="text/javascript">
    new Vue({
        el: '#app',
        data() {
            return {
                baseForm: {
                    name: '',
                    template_id: '',
                    template_type: '',
                    createUser: '',
                    link_ids: ['']
                },
                link_info: {}, // link_id类别名
                courseForm_SinologyText: {
                    title: '',
                    author: '',
                    target: '',
                    audioUrl: '',
                    audioTime: '',
                    textContent: ['']
                },
                courseForm_PicBookId: {
                    title: '',
                    goalDetail: '',
                    pictureBookId: ''
                },
                courseForm_BookAudio: {
                    title: '',
                    goalDetail: '',
                    content: '',
                    pictureBookId: '',
                    coverImgUrl: '',
                    sentenceList: [],

                },
                courseForm_ImgTextReading: {
                    wordCount: '',
                    generalTimeString: '',
                    knowledgeCount: '',
                    coverImgUrl: '',
                    introduction: ''
                },
                courseForm_BookImgText: {
                    title: '',
                    goalDetail: '',
                    coverImgUrl: ''
                },
                // 可搜索数组
                linkId: '',
                linkIdList: [],
                edit: 0,
                cdn_host: '',
                rules: {
                    name: [{required: true, message: '请输入模板名称', trigger: 'blur'}],
                    template_type: [{required: true, message: '请选择皮肤类型', trigger: 'change'}],
                    title: [{required: true, message: '请输入标题', trigger: 'blur'}, { min: 1, max: 8, message: '字数限制8个以内', trigger: 'blur' }],
                    author: [{required: true, message: '请输入作者', trigger: 'blur'}],
                    target: [{required: true, message: '请输入目标描述', trigger: 'blur'}],
                    goalDetail: [{required: true, message: '请输入目标描述', trigger: 'blur'}],
                    pictureBookId: [{required: true, message: '请输入绘本ID', trigger: 'blur'}],
                    coverImgUrl: [{required: true, message: '请上传封面', trigger: 'change'}],
                    introduction: [{required: true, message: '请输入课节导语', trigger: 'change'}],
                    link_ids: [{type: 'array', min: 1, required: true, fields:{0: {message: '请至少添加一个学习环节', required: true, trigger: "change"}}}],
                }
            }
        },
        computed: {},
        watch: {
            'baseForm.link_ids': {
                handler(val) {
                    console.log(val)
                    this.getLinkInfo()
                },
                deep: true,
            }
        },
        filters: {
            filterLinkInfo: function(val) {
                let obj = {
                    DIRECT_AUDIO:'直接上传音频',
                    DIRECT_VIDEO:'直接上传视频',
                    INDIRECT_VIDEO:'内容库轻交互音视频',
                    SINGLE_WITHOUT_RESOLUTION:'单选无解析',
                    SINGLE_HAS_RESOLUTION:'单选有解析',
                    ONE_QUESTION_MORE_ASK:'一题多问',
                    error: '错误数据，请检查id是否存在！'
                }
                return val ? obj[val] : ''
            }
        },
        mounted() {
            this.$nextTick(() => {
                this.getData();
                this.edit = Number(getQuery().edit)
            });
        },
        methods: {
            uploadResult(data, model) {
                let result = this;
                for (let index = 0; index < model.length; index++) {
                    console.log(2, index, model[index]);
                    if (index == model.length - 1) {
                        console.log(3, model[index - 1], model[index]);
                        if (model[index - 1] == 'courseForm_BookAudio' && model[index] == 'sentenceList') {
                            let arrObj = {
                                audioSeconds: '',
                                sentence: '',
                                rank: ''
                            };
                            console.log(data)
                            for (let i in data) {
                                console.log(data[i], data[i].file.type.includes('image'))
                                arrObj[data[i].file.type.includes('image') ? 'imgUrl' : 'audioUrl'] = data[i].fileName;
                            }
                            this.courseForm_BookAudio.sentenceList.push(arrObj)
                            console.log(this.courseForm_BookAudio.sentenceList)
                        } else {
                            result[model[index]] = data.fileName;
                        }
                    }
                    result = result[model[index]];
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
                this.$refs['baseForm'].validate(valid => {
                    if (valid && this.baseForm.link_ids[0] !== '') {
                        this.$refs['courseForm_' + this.baseForm.template_type].validate(val => {
                            if(this.baseForm.template_type == "BookAudio") {
                                if(!this.courseForm_BookAudio.sentenceList.length) {
                                    this.$message.error('请确认图片+音频表格数据')
                                    return false
                                }
                                let item;
                                for (let i in this.courseForm_BookAudio.sentenceList) {
                                    item = this.courseForm_BookAudio.sentenceList[i];
                                    for (let j in item) {
                                        if(item[j] == "") {
                                            this.$message.error('请确认图片+音频表格数据')
                                            return false
                                        }
                                    }
                                }
                            }
                            if(val) {
                                let obj = {};
                                if (this.baseForm.template_type) {
                                    obj = this['courseForm_' + this.baseForm.template_type]
                                }
                                // link_ids 去空，重复判断
                                let link_ids = this.baseForm.link_ids.filter(function (s) {
                                    return s && $.trim(s); // 注：IE9(不包含IE9)以下的版本没有trim()方法
                                });
                                let hash = {};
                                for (let i = 0; i < link_ids.length; i++ ) {
                                    if (hash[link_ids[i]] ) {
                                        this.$alert('请不要关联相同的学习环节！');
                                        return
                                    }
                                    hash[link_ids[i]] = true;
                                }
                                //大佬们封装的很好，但是感觉直接赋值更直接一些～
                                // obj = Object.assign({}, obj, this.baseForm, this.baseForm);
                                let formData = new FormData();
                                // makeFormData(obj, formData);
                                formData.append("template_id", this.baseForm.template_id);
                                formData.append("name", this.baseForm.name);
                                formData.append("template_type", this.baseForm.template_type);

                                formData.append("link_ids", JSON.stringify(link_ids));
                                formData.append("template", JSON.stringify(obj));
                                $.ajax({
                                    url: '/opmanager/studyTogether/newTemplate/save_newTemplate.vpage',
                                    data: formData,
                                    type: "POST",
                                    processData: false,
                                    contentType: false,
                                    success: (data) => {
                                        if (data.success) {
                                            this.$alert('保存成功', {
                                                callback: function () {
                                                    window.location.href = '/opmanager/studyTogether/newTemplate/newTemplate_list_page.vpage';
                                                }
                                            });
                                        } else {
                                            this.$alert("保存失败，原因：" + data.info);
                                        }
                                        // 回调处理
                                    }
                                })
                            } else {
                                this.$message.error('请确认表单信息是否符合规则！')
                                return false
                            }
                        })

                    } else {
                        this.$message.error('请确认表单信息是否符合规则！')
                        return false
                    }
                })
            },
            back() {
                window.location.href = '/opmanager/studyTogether/newTemplate/newTemplate_list_page.vpage';
            }
            ,
            getData() {
                let form = {};
                if (getQuery().templateId) {
                    form.template_id = getQuery().templateId;
                }
                $.ajax({
                    url: '/opmanager/studyTogether/newTemplate/get_newTemplate_info.vpage',
                    data: form,
                    type: "GET",
                    success: (data) => {
                        if (typeof getQuery().templateId == 'undefined') {
                            this.baseForm.createUser = data.admin_user;
                        } else {
                            this.baseForm.createUser = data.template.createUser;
                            this.baseForm.name = data.template.name;
                            this.baseForm.template_id = data.template.id;
                            this.baseForm.template_type = data.template.templateType;
                            this.baseForm.link_ids = data.template.linkIds;
                        }
                        this.cdn_host = data.cdn_host;
                        switch (this.baseForm.template_type) {
                            case 'SinologyText':
                                this.courseForm_SinologyText = data.template.templateInfo.sinologyTextTemplate;
                                break;
                            case 'PicBookId':
                                this.courseForm_PicBookId = data.template.templateInfo.picBookIdTemplate;
                                break;
                            case 'BookAudio':
                                this.courseForm_BookAudio = data.template.templateInfo.bookAudioTemplate;
                                break;
                            case 'ImgTextReading':
                                this.courseForm_ImgTextReading = data.template.templateInfo.imgTextReadingTemplate;
                                break;
                            case 'BookImgText':
                                this.courseForm_BookImgText = data.template.templateInfo.bookImgTextTemplate;
                                break;
                            default:
                                break;
                        }
                    }
                })
            },
            getLinkId(val) {
                $.ajax({
                    url: '/opmanager/studyTogether/link/get_link_id_by_name.vpage',
                    data: {name: val},
                    type: "GET",
                    success: (data) => {
                        this.linkIdList = data.return_list;
                        //TODO:检查是否添加过
                    }
                })
            },

            // 添加删除学习环节
            add() {
                this.baseForm.link_ids.push('');
            },
            remove(index) {
                this.baseForm.link_ids.splice(index, 1);
            },
            linkInfo(linkId){
                let linkUrl = '/opmanager/studyTogether/link/info.vpage?edit=1'
                if(linkId) {
                    linkUrl += ('&linkId=' + linkId);
                }
                window.open(linkUrl);
            },
            // 书本表格里的删除
            handelRemove(index) {
                this.courseForm_BookAudio.sentenceList.splice(index, 1);
            },
            // 关联学习环节问题
            linkIdChange(val) {
                this.linkId = '';
                if (this.baseForm.link_ids.includes(val)) {
                    this.$message.error('该环节已存在，请勿重复选取！')
                    return
                }
                this.baseForm.link_ids[this.baseForm.link_ids.length - 1] = val;
                this.baseForm.link_ids.push('');
            },
            getLinkInfo() {
                let self = this;
                let fn = function (id, obj) {
                    $.ajax({
                        url: '/opmanager/studyTogether/link/get_link_by_id.vpage',
                        data: {id: id},
                        type: "GET",
                        success: (data) => {
                            if (data.success) {
                                obj[id] = data.linkInfo;
                            } else {
                                obj[id] = {
                                    type: 'error'
                                }
                            }
                            self.$forceUpdate();
                        }
                    })
                };
                let arr = this.baseForm.link_ids;
                for (let i = 0; i < arr.length; i++) {
                    if(arr[i] && $.trim(arr[i] && !this.link_info[arr[i]])) {
                        fn(arr[i], this.link_info);
                    }
                }
            },
            jumpPreviewPage(type){
                var url = '';
                switch (type) {
                    case 'SinologyText':
                        url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/04/04/20190404154040063652.png';
                        break;
                    case 'ImgTextReading':
                        url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/04/04/20190404154214878094.png';
                        break;
                    case 'PicBookId':
                        url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/04/04/20190404154110942109.png';
                        break;
                    case 'BookAudio':
                        url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/04/04/20190404154135334968.png';
                        break;
                    case 'BookImgText':
                        url = 'https://oss-image.17zuoye.com/study_course_map_face/2019/04/04/20190404154241305829.png';
                        break;
                    default:
                        return;
                }
                window.open(url);
            }
        }
    });
</script>
</@layout_default.page>