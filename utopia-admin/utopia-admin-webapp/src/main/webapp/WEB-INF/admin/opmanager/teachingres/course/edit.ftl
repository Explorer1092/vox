<#import "../../../layout_default.ftl" as layout_default />
<#import "../../../mizar/pager.ftl" as pager />
<@layout_default.page page_title="教学资源--同步课件--编辑" page_num=9>
<link rel="stylesheet" href="https://cdn.bootcss.com/element-ui/2.6.1/theme-chalk/index.css">
<link rel="stylesheet" href="${requestContext.webAppContextPath}/public/css/opmanager/teachingres/course/edit.css">

<div id="courseContainer" class="span9" v-cloak>
    <div class="titleBox">
        <div class="title">
            <h3>添加/编辑课件</h3>
            <a href="javascript:void(0);" class="btn btn-primary" v-on:click="saveCourse()">
                <i class="icon-ok icon-white"></i> 保 存
            </a>
            <a href="/opmanager/teacher_resource/course/index.vpage" class="btn btn-primary">
                <i class="icon-backward icon-white"></i> 返 回
            </a>
        </div>
    </div>
    <h4 class="blockTitle">附件信息</h4>
    <div class="uploadBox">
        <div class="itemBox" v-for="(file, index) in (detailInfo.fileList || [])">
            <div class="fileBox">
                <#--word、ppt、zip、pdf、audio、video、file-->
                <div class="fileType" v-bind:class="getFileType(file.fileUrl)"></div>
                <div class="fileName">{{file.fileName}}</div>
            </div>
            <div class="btnBox">
                <div v-on:click="deleteFile(file, index)">删除</div>
                <div class="edit" v-on:click="editFile(file, index)">编辑</div>
            </div>
        </div>
        <#--新增-->
        <div class="itemBox uploadTypeBox">
            <div class="fileBox" v-bind:class="{'star': !(detailInfo.fileList || []).length}">上传资源</div>
            <div class="btnBox">
                <div v-on:click="uploadFile()">上传文件</div>
            </div>
        </div>
    </div>

    <h4 class="blockTitle">基本信息</h4>
    <div class="baseInfoBox">
        <div class="baseItemBox baseItemBox1">
            <label class="star" for="">资源封面设置：</label>
        </div>
        <div class="baseItemBox">
            <label for="">&nbsp;</label>
            <div class="baseItemContent">
                <div class="baseUploadBox">
                    <div class="imageBox" v-bind:class="{'uploaded': detailInfo.image}" v-on:click="uploadImage('image')">
                        <img v-bind:src="compressImg(detailInfo.image)" alt=""  v-if="detailInfo.image">
                    </div>
                    <h5 class="star">题图</h5>
                    <p>格式：jpeg/png，250*184</p>
                </div>
                <div class="baseUploadBox">
                    <div class="imageBox" v-bind:class="{'uploaded': detailInfo.appImage}" v-on:click="uploadImage('appImage')">
                        <img v-bind:src="compressImg(detailInfo.appImage)" alt="" v-if="detailInfo.appImage">
                    </div>
                    <h5 v-bind:class="{'star': detailInfo.featuring}">老师APP首页</h5>
                    <p>格式：jpeg/png，300*165</p>
                </div>
                <div class="baseUploadBox">
                    <div class="imageBox" v-bind:class="{'uploaded': detailInfo.headImage}" v-on:click="uploadImage('headImage')">
                        <img v-bind:src="compressImg(detailInfo.headImage)" alt="" v-if="detailInfo.headImage">
                    </div>
                    <h5>详情页封面图（无则使用默认）</h5>
                    <p>格式：jpeg/png，750*280</p></div>

                <div id="imageSelect" style="display: none;">选择文件</div>
                <div id="imageUpload" style="display: none;">开始上传</div>
            </div>
        </div>
        <div class="baseItemBox baseItemBox2">
            <label for="" class="star">选择学科：</label>
            <div class="baseItemContent">
                <span v-bind:class="{'active': choiceSubjectEnglishName === subject.subjectEnglishName}"
                      v-on:click="choiceSubject(subject)"
                      v-for="subject in subjectList">{{subject.subjectName}}</span>
            </div>
        </div>
        <div class="baseItemBox baseItemBox3">
            <label for="" class="star">资源属性：</label>
            <div class="baseItemContent">
                <select name="" id="" v-model="choiceClazzLevelId" v-on:change="choiceClazzLevel()">
                    <option v-bind:value="clazzLevel.clazzLevelId"
                            v-for="clazzLevel in clazzLevelList">{{clazzLevel.clazzLevelName}}</option>
                </select>
                <select name="" id="" v-model="choiceTermId" v-on:change="choiceTerm()">
                    <option v-bind:value="term.termId"
                            v-for="term in termList">{{term.termName}}</option>
                </select>
                <select name="" id="" v-model="choiceBookId" v-on:change="choiceBook()">
                    <option v-bind:value="book.id"
                            v-for="book in bookList">{{book.name}}</option>
                </select>
                <select name="" id="" v-model="choiceUnitId" v-on:change="choiceUnit()">
                    <option v-bind:value="unit.unitId"
                            v-for="unit in unitList">{{unit.unitName}}</option>
                </select>
                <select name="" id="" v-model="choiceLessonId">
                    <option v-bind:value="lesson.lessonId"
                            v-for="lesson in lessonList">{{lesson.lessonRealName}}</option>
                </select>
            </div>
        </div>

        <div class="baseItemBox">
            <label for="titleInput" class="star">资源标题：</label>
            <textarea name="" maxlength="40" v-model="detailInfo.title" id="titleInput"></textarea>
            <span class="tip">注：最多输入40个字符</span>
        </div>
        <div class="baseItemBox">
            <label for="subHeadInput">分享副标题：</label>
            <textarea name="" maxlength="40" v-model="detailInfo.subHead" id="subHeadInput"></textarea>
            <span class="tip">注：最多输入40个字符，无则默认使用：“点击查看资源详情”</span>

        </div>
        <div class="baseItemBox">
            <label for="" class="star">资源简介：</label>
            <div class="editor">
                <!--此处为文本编辑器-->
                <script id="editor" type="text/plain"></script>
            </div>
            <!--此处为文本编辑器-->
        </div>
    </div>

    <h4 class="blockTitle">任务设置</h4>
    <div class="taskInfoBox">
        <div class="taskItemBox">
            <label for="receiveLimitCheckbox">是否认证可领：</label>
            <input type="checkbox" v-model="detailInfo.receiveLimit" id="receiveLimitCheckbox">
        </div>
        <div class="taskItemBox">
            <label for="" class="star">作业类型：</label>
            <select name="" id="workTypeSelect" v-model="detailInfo.workType">
                <#list workTypeList as workType>
                <option value="${workType.value!''}">${workType.name!''}</option>
                </#list>
            </select>
        </div>
        <div class="taskItemBox">
            <label for="" class="star">任务类型：</label>
            <select name="" id="taskSelect" v-model="detailInfo.task" v-bind:disabled="detailInfo.id">
                <#list taskList as task>
                    <option value="${task.value!''}">${task.name!''}</option>
                </#list>
            </select>
            <span class="tip">注：非免费的任务类型，必须要设置有效期</span>
        </div>
        <div class="taskItemBox">
            <label for="validityPeriodInput" v-bind:class="{'star': detailInfo.task !== 'FREE'}">有效期：</label>
            <input type="number" v-model="detailInfo.validityPeriod" id="validityPeriodInput" v-bind:disabled="detailInfo.id">
            <span class="tip">（天）</span>
        </div>
        <div class="taskItemBox">
            <label for="coursewareLabelInput" class="star">标签：</label>
            <input type="text" v-model="labelInputText" id="coursewareLabelInput" placeholder="热门/限时">
            <span class="tip">注：多个标签使用 / （半角符号）分割，如：热门/限时/节日，最多可设置3个标签，每个标签最多四个字</span>
        </div>
        <div class="taskItemBox">
            <label for="" class="star">课件来源：</label>
            <select name="" id="" v-model="detailInfo.source">
                <option v-bind:value="courseSource.sourceId"
                        v-for="courseSource in courseSourceList">{{courseSource.sourceName}}</option>
            </select>
        </div>
        <div class="taskItemBox">
            <label for="" class="star">状态：</label>
            <select name="" id="" v-model="detailInfo.online">
                <option v-bind:value="onlineState.state"
                        v-for="onlineState in onlineStateList">{{onlineState.stateName}}</option>
            </select>
        </div>
        <div class="taskItemBox">
            <label for="displayOrderInput">置顶排序：</label>
            <input type="number" v-model="detailInfo.displayOrder" id="displayOrderInput">
        </div>
        <div class="taskItemBox">
            <label for="featureCheckbox">首页是否展示：</label>
            <input type="checkbox" v-model="detailInfo.featuring" id="featureCheckbox">
        </div>
    </div>

    <br><br>
    <a href="javascript:void(0);" class="btn btn-primary" v-on:click="saveCourse()">
        <i class="icon-ok icon-white"></i> 保 存
    </a>
    <template  v-if="detailInfo.id">
        <a href="javascript:void(0);" class="btn btn-primary" v-on:click="previewCourse()">
            <i class="icon-zoom-in icon-white"></i> 预 览
        </a>
        <p class="previewTip">提示：只支持预览已经保存的内容哦，编辑状态的内容无法预览。</p>
    </template>

    <#-- 添加资源弹窗 -->
    <div id="addResourceDialog" class="modal fade hide" aria-hidden="true">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" v-on:click="cancelUploadFile()">×</button>
                <h3 class="modal-title">{{showEditFileIndex === -1 ? '添加' : '编辑'}}资源</h3>
            </div>
            <div class="modal-body">
                <div class="addBox">
                    <div class="addItem">
                        <label for="">资源标题：</label>
                        <input type="text" maxlength="40" placeholder="请输入资源标题" v-model="editingFileObj.fileName">
                    </div>
                    <div class="addItem" style="display: none">
                        <label for="">资源类型：</label>
                        <div class="labelBox">
                            <span>课件</span>
                            <span>教案</span>
                            <span>习题</span>
                            <span>素材</span>
                            <span>绘本</span>
                        </div>
                    </div>
                    <div class="addItem">
                        <label for="">资源上传：</label>
                        <a href="javascript:void(0);" class="btn btn-primary" v-on:click="uploadOtherFile()">
                            <!--阿里云触发dom-->
                            <div id="fileSelect" style="display: none;">选择文件</div>
                            <div id="fileUpload" style="display: none;">开始上传</div>
                            <!--上传按钮-->
                            <i class="icon-folder-open icon-white"></i>
                            <template v-if="uploadFileObj.uploadState === 'init' || uploadFileObj.uploadState === 'start'">
                                {{editingFileObj.fileUrl ? '重新上传' : '上传'}}
                            </template>
                            <template v-else-if="uploadFileObj.uploadState === 'progress'">
                                上传中{{uploadFileObj.uploadProgress}}
                            </template>
                            <template v-else-if="uploadFileObj.uploadState === 'end'">
                                上传成功，点击重新上传
                            </template>
                        </a>
                        <span class="tip"></span>
                    </div>
                    <div class="addItem tipBox">
                        温馨提示：<br>1. 支持上传 png/jpg/jpeg、doc/docx、ppt/pptx、pdf、zip/rar、mp3、mp4. <br>2. 选择完之后点击确定关闭弹窗，点击页面上的保存按钮才会生效哦~
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn" v-on:click="cancelUploadFile()">取 消</button>
                <button class="btn btn-primary" v-on:click="sureUplodFile()">确 定</button>
            </div>
        </div>
    </div>
</div>
<script>
    var mainSiteBaseUrl = "${(ProductConfig.getMainSiteBaseUrl())!''}";
    // var mainSiteBaseUrl = "https://www.test.17zuoye.net";
</script>

<#--<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>-->
<script src="${requestContext.webAppContextPath}/public/js/vue.debug.js"></script>
<#-- element-ui 因为包含字体文件，所以未使用本地资源-->
<script src="https://cdn.bootcss.com/element-ui/2.6.1/index.js"></script>
<#--富文本编辑器-->
<script src="${requestContext.webAppContextPath}/public/plugin/wxeditor/js/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/plugin/wxeditor/js/ueditor.all.js"></script>
<#--直传阿里云-->
<script src="${requestContext.webAppContextPath}/public/js/plupload-1.2.1/plupload.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/opmanager/teachingres/course/ossupload.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/opmanager/teachingres/course/edit.js"></script>
</@layout_default.page>