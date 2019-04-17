/**
 * @author: pengmin.chen
 * @description: "同步课件后台添加编辑页"
 * @createdDate: 2019/3/23
 * @lastModifyDate: 2019/3/23
 */

var subjectList = [
        {
            subjectName: '语文',
            subjectEnglishName: 'CHINESE'
        },
        {
            subjectName: '数学',
            subjectEnglishName: 'MATH'
        },
        {
            subjectName: '英语',
            subjectEnglishName: 'ENGLISH'
        }
    ],
    clazzLevelList = [
        {
            clazzLevelName: '一年级',
            clazzLevelId: 1
        },
        {
            clazzLevelName: '二年级',
            clazzLevelId: 2
        },
        {
            clazzLevelName: '三年级',
            clazzLevelId: 3
        },
        {
            clazzLevelName: '四年级',
            clazzLevelId: 4
        },
        {
            clazzLevelName: '五年级',
            clazzLevelId: 5
        },
        {
            clazzLevelName: '六年级',
            clazzLevelId: 6
        }
    ],
    termList = [
        {
            termId: 1,
            termName: '上册'
        },
        {
            termId: 2,
            termName: '下册'
        }
    ],
    courseSourceList = [
        {
            sourceId: 0,
            sourceName: '课件大赛'
        },
        {
            sourceId: 1,
            sourceName: '一起作业'
        }
    ],
    onlineStateList = [
        {
            state: true,
            stateName: '上线'
        },
        {
            state: false,
            stateName: '下线'
        }
    ];

new Vue({
    el: '#courseContainer',
    data: {
        subjectList: subjectList, // 学科列表
        clazzLevelList: clazzLevelList, // 年级列表
        termList: termList, // 上下册列表
        bookList: [], // 教材
        unitList: [], // 单元
        lessonList: [], // 教程
        courseSourceList: courseSourceList, // 课件来源
        onlineStateList: onlineStateList, // 上线状态
        labelInputText: '', // 标签

        choiceSubjectEnglishName: subjectList[0].subjectEnglishName, // 选择的学科
        choiceClazzLevelId: clazzLevelList[0].clazzLevelId, // 选择的年级id
        choiceTermId: termList[0].termId, // 选择的上下册id
        choiceBookId: '', // 选择的教材
        choiceUnitId: '', // 选择的单元
        choiceLessonId: '', // 选择的教程
        showEditFileIndex: -1, // -1表示新增、其他书表示编辑的索引
        editingFileObj: {}, // 编辑文件中的信息（可能随时取消，重置到editedFileObj）
        editedFileObj: {}, // 编辑文件后的信息，最终需要更新到detailInfo.fileList中用于存储数据
        uploadFileObj: {
            uploadState: 'init',
            uploadProgress: '0%'
        }, // 上传文件是的信息
        uploadImageType: '', // 上传图片的类型（题图、老师APP首页、详情页封面图）

        detailInfo: {}, // 除了学科、教材这些联动的选择，其他字段均存储在detailInfo中
        ueEditor: null, // 富文本编辑器
    },
    methods: {
        // 选择学科
        choiceSubject: function(subject){
            var vm = this;
            vm.choiceSubjectEnglishName = subject.subjectEnglishName;
            vm.requestBookInfo();
        },
        // 请求教材信息（学科 + 年级 + 上下册 load）
        requestBookInfo: function(detailInfo) {
            var vm = this;
            $.ajax({
                url: '/opmanager/teacher_resource/book_list.vpage',
                type: 'GET',
                data: {
                    subject: vm.choiceSubjectEnglishName,
                    clazzLevel: vm.choiceClazzLevelId,
                    term: vm.choiceTermId
                },
                success: function (res) {
                    if (!res.success) {
                        vm.showTip(res.info, 'error');
                        return;
                    }
                    vm.bookList = res.books;

                    if (detailInfo && detailInfo.id) { // 已有信息回显
                        vm.choiceBookId = detailInfo.bookId;

                        for (var i = 0; i < vm.bookList.length; i++) {
                            if (vm.bookList[i].id === detailInfo.bookId) {
                                vm.unitList = vm.bookList[i].unitList;
                                break;
                            }
                        }
                        vm.choiceUnitId = detailInfo.unitId;
                    } else {
                        vm.choiceBookId = vm.bookList[0].id;
                        vm.unitList = vm.bookList[0].unitList;
                        vm.choiceUnitId = vm.unitList[0].unitId;
                    }
                    vm.requestLessonInfo(detailInfo);
                }
            });
        },
        // 选择年级
        choiceClazzLevel: function() {
            var vm = this;
            vm.requestBookInfo();
        },
        // 选择上下册
        choiceTerm: function() {
            var vm = this;
            vm.requestBookInfo();
        },
        // 选择book
        choiceBook: function() {
            var vm = this;
            for (var i = 0; i < vm.bookList.length; i++) {
                if (vm.bookList[i].id === vm.choiceBookId) {
                    vm.unitList = vm.bookList[i].unitList;
                    vm.choiceUnitId = vm.unitList[0].unitId;
                    vm.requestLessonInfo(); // 请求lesson
                    break;
                }
            }
        },
        // 选择单元
        choiceUnit: function() {
            var vm = this;
            vm.requestLessonInfo(); // 请求lesson
        },
        // 请求课程信息（unit load）
        requestLessonInfo: function(detailInfo) {
            var vm = this;
            $.ajax({
                url: '/opmanager/teacher_resource/lessions.vpage',
                type: 'GET',
                data: {
                    unitId: vm.choiceUnitId
                },
                success: function (res) {
                    if (!res.success) {
                        vm.showTip(res.info, 'error');
                        return;
                    }
                    vm.lessonList = res.data;
                    if (detailInfo && detailInfo.id) {
                        vm.choiceLessonId = detailInfo.lessonId;
                    } else {
                        vm.choiceLessonId = vm.lessonList[0].lessonId;
                    }
                }
            });
        },
        // 请求课件详情
        requestCourseDetail: function(courseId) {
            var vm = this;
            $.ajax({
                url: '/opmanager/teacher_resource/detail.vpage',
                type: 'GET',
                data: {
                    id: courseId
                },
                success: function (res) {
                    if (!res.success) {
                        vm.showTip(res.info, 'error');
                        return;
                    }
                    vm.detailInfo = res.data;
                    vm.labelInputText = res.data.label.join('/'); // 标签（拼成字符串展示）
                    vm.backShowState(vm.detailInfo);
                    vm.initEditor(vm.detailInfo.desc);
                }
            });
        },
        // 状态回显
        backShowState: function (detailInfo){
            var vm = this;
            vm.choiceSubjectEnglishName = detailInfo.subject;
            vm.choiceClazzLevelId = detailInfo.clazzLevel;
            vm.choiceTermId = detailInfo.termType;
            vm.requestBookInfo(detailInfo); // 请求book
        },
        // 预览
        previewCourse: function() {
            var vm = this;
            var url = mainSiteBaseUrl + "/view/mobile/teacher/primary/teaching_assistant/detail_course.vpage?resourceId=" + vm.detailInfo.id;
            window.open(url, '_top ', 'width=375,height=667');
        },
        // 保存课件
        saveCourse: function() {
            var vm = this;
            if (!vm.querySaveParam()) return;
            // 校验通过开始上传
            var obj = $.extend(vm.detailInfo, {
                subject: vm.choiceSubjectEnglishName,
                clazzLevel: vm.choiceClazzLevelId,
                termType: vm.choiceTermId,
                bookId: vm.choiceBookId,
                unitId: vm.choiceUnitId,
                lessonId: vm.choiceLessonId,
                desc: vm.ueEditor.getContent(),
                label: vm.labelInputText.split('/') // 解析完再上传
            });
            $.ajax({
                url: '/opmanager/teacher_resource/upsert.vpage',
                type: 'POST',
                data: JSON.stringify(obj),
                dataType: 'JSON',
                contentType: 'application/json',
                success: function(res) {
                    if (!res.success) {
                        vm.showTip(res.info, 'error');
                        return;
                    }
                    vm.detailInfo = res.data;
                    vm.$confirm('保存成功', '提示', {
                        cancelButtonText: getQuery('courseId') ? '继续编辑' : '查看详情',
                        confirmButtonText: '返回首页',
                        callback: function (action){
                            if (action === 'confirm') {
                                window.location.href = '/opmanager/teacher_resource/course/index.vpage';
                            } else if (action === 'cancel' && !getQuery('courseId')) { // 点击查看详情且为新增情况
                                window.location.href = '/opmanager/teacher_resource/course/edit.vpage?courseId=' + vm.detailInfo.id;
                            }
                        }
                    });
                }
            });
        },
        // 保存时判断字段是否都填写了
        querySaveParam:function () {
            var vm = this;
            // 校验上传的附件
            if (!(vm.detailInfo.fileList || []).length) {
                vm.showTip('您还未上传附件哦~<br><br><strong>温馨提示：所有带 <i>*</i> 的都是必填的哦~</strong>', 'error', true)
                return false;
            }

            // 校验图片
            if (!vm.detailInfo.image) {
                vm.showTip('您还未上传题图哦~<br><br><strong>温馨提示：所有带 <i>*</i> 的都是必填的哦~</strong>', 'error', true)
                return false;
            }
            if (vm.detailInfo.featuring && !vm.detailInfo.appImage) {
                vm.showTip('您设置了首页展示，必须要上传老师APP首页图片哦~<br><br><strong>温馨提示：所有带 <i>*</i> 的都是必填的哦~</strong>', 'error', true)
                return false;
            }

            // 基本信息
            if (!vm.detailInfo.title) {
                vm.showTip('您还未填写资源标题哦~<br><br><strong>温馨提示：所有带 <i>*</i> 的都是必填的哦~</strong>', 'error', true)
                return false;
            }
            if (!vm.ueEditor.getContent()) {
                vm.showTip('您还未填写资源简介哦~<br><br><strong>温馨提示：所有带 <i>*</i> 的都是必填的哦~</strong>', 'error', true)
                return false;
            }

            // 任务设置（非免费是必须设置有限期）
            if (vm.detailInfo.task !== 'FREE' && !vm.detailInfo.validityPeriod) {
                vm.showTip('您选择了非免费的任务类型，必须要设置有效期哦~<br><br><strong>温馨提示：所有带 <i>*</i> 的都是必填的哦~</strong>', 'error', true)
                return false;
            }
            if (!vm.labelInputText) {
                vm.showTip('您还未设置标签哦~<br><br><strong>温馨提示：所有带 <i>*</i> 的都是必填的哦~</strong>', 'error', true)
                return false;
            }
            if (vm.labelInputText && vm.labelInputText.split('/').length > 3) {
                vm.showTip('标签最多设置三个哦~', 'error', true)
                return false;
            }
            var hasLabelTooLong = vm.labelInputText.split('/').some(function(item) {
                return item.length > 4;
            });
            if (hasLabelTooLong) {
                vm.showTip('每个标签最多四个字哦~', 'error', true);
                return false;
            }
            return true;
        },
        // 上传文件
        uploadFile: function() {
            var vm = this;
            vm.showEditFileIndex = -1;
            vm.uploadFileObj = {
                uploadState: 'init',
                uploadProgress: '0%'
            }; // 重置
            vm.editingFileObj = {}; // 重置
            vm.editedFileObj = {}; // 重置
            $('#addResourceDialog').modal("show");
        },
        // 编辑文件
        editFile: function(file, index) {
            var vm = this;
            vm.uploadFileObj.uploadState = 'init'; // 重置
            vm.showEditFileIndex = index;
            vm.editingFileObj = JSON.parse(JSON.stringify(file)); // 不希望操作时自动更新editedFileObj
            vm.editedFileObj = JSON.parse(JSON.stringify(file));
            $('#addResourceDialog').modal("show");
        },
        // 删除文件
        deleteFile: function (file, index) {
            var vm = this;
            vm.$confirm("您确定要删除该附件吗?", '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(function () {
                vm.detailInfo.fileList.splice(index, 1);
            });
        },
        // 弹窗上传其他文件
        uploadOtherFile: function() {
            var vm = this;
            if (vm.uploadFileObj.uploadState === 'start' || vm.uploadFileObj.uploadState === 'progress') return;
            $('#fileSelect').click();
        },
        // 上传附件弹窗--取消
        cancelUploadFile: function () {
            var vm = this;
            vm.editingFileObj = Object.create(vm.editedFileObj); // 将编辑中的重置回已编辑的数据
            $('#addResourceDialog').modal("hide");
        },
        // 上传附件弹窗--确定（此时只是存储字段，并未真正存储）
        sureUplodFile: function () {
            var vm = this;
            if (!vm.editingFileObj.fileName && !vm.editingFileObj.fileUrl) {
                vm.showTip('请填写资源标题和上传资源附件哦~');
                return;
            }
            if (!vm.editingFileObj.fileName) {
                vm.showTip('请填写资源标题哦~');
                return;
            }
            if (!vm.editingFileObj.fileUrl) {
                vm.showTip('请上传资源附件哦~');
                return;
            }
            vm.editedFileObj = vm.editingFileObj; // 将编辑中的数据更新到已编辑的数据中
            $('#addResourceDialog').modal("hide");

            // 新增或者更新原文件
            if (vm.showEditFileIndex === -1) { // 新增
                if (!(vm.detailInfo.fileList || []).length) { // 第一次新增
                    vm.$set(vm.detailInfo, 'fileList', [vm.editedFileObj]);
                } else {
                    vm.detailInfo.fileList.push(vm.editedFileObj);
                }
            } else { // 编辑
                vm.detailInfo.fileList.splice(vm.showEditFileIndex, 1, vm.editedFileObj); // 这样能触发视图更新
            }
        },
        // 上传图片
        uploadImage: function (type) {
            var vm = this;
            vm.uploadImageType = type;
            $('#imageSelect').click();
        },
        // 根据文件名返回文件类型设置icon
        getFileType: function (name) {
            var suffixName = name.substr(name.lastIndexOf('.') + 1).toLowerCase(); // 后缀名
            var fileType = '';
            if (['jpg', 'jpeg', 'png'].indexOf(suffixName) > -1) fileType = 'pic';
            else if (['doc', 'docx'].indexOf(suffixName) > -1) fileType = 'word';
            else if (['ppt', 'pptx'].indexOf(suffixName) > -1) fileType = 'ppt';
            else if (['pdf'].indexOf(suffixName) > -1) fileType = 'pdf';
            else if (['zip', 'rar'].indexOf(suffixName) > -1) fileType = 'zip';
            else if (['mp3'].indexOf(suffixName) > -1) fileType = 'audio';
            else if (['mp4'].indexOf(suffixName) > -1) fileType = 'video';
            else fileType = 'file';
            return fileType;
        },
        // 缩略图压缩
        compressImg(link) {
            if (link && (link.indexOf('oss-image.17zuoye.com') > -1 || link.indexOf('v.17xueba.com') > -1)) {
                return link + '?x-oss-process=image/resize,w_200/quality,Q_70';
            }
            return link;
        },
        // 显示提示
        showTip: function (info, type, useHtml) {
            var vm = this,
                opt = {
                    dangerouslyUseHTMLString: useHtml,
                    showClose: true,
                    message: info
                };
            if (type) {
                opt = $.extend(opt, {
                    type: type
                });
            }
            vm.$message(opt);
        },
        initEditor: function (desc) {
            var vm = this;
            vm.ueEditor = UE.getEditor("editor", {
                serverUrl: "/advisory/ueditorcontroller.vpage",
                topOffset: 0,
                zIndex: 1040,
                autoHeightEnabled: false,
                initialFrameHeight: 650,
                autoWidth: true,
                initialFrameWidth: $('.baseItemBox').width() * 0.7,
                toolbars: [[
                    'fullscreen', 'source', '|', 'undo', 'redo', '|',
                    'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                    'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                    'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                    'directionalityltr', 'directionalityrtl', 'indent', '|',
                    'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                    'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                    'simpleupload', 'pagebreak', '|',
                    'horizontal', 'date', 'time', 'preview'
                ]]
            });

            vm.ueEditor.ready(function () {
                $(".itembox").on("click", function (a) {
                    vm.ueEditor.execCommand("insertHtml", "<div>" + $(this).html() + "</div><br />")
                });

                if (desc) {
                    vm.ueEditor.setContent(desc.replace('\n','').replace("'" , "\\'"));
                }
            });

            var b = ["borderTopColor", "borderRightColor", "borderBottomColor", "borderLeftColor"], d = [];
            $.each(b, function (a) {
                d.push(".itembox .wxqq-" + b[a])
            });
        }
    },
    mounted: function () {
        var vm = this;
        if (getQuery('courseId')) {
            vm.requestCourseDetail(getQuery('courseId'));
        } else {
            vm.initEditor();
            vm.requestBookInfo();
            // 新增时默认给几个必须项设置默认值
            vm.detailInfo = {
                source: 1, // 课件来源
                online: false, // 状态
                receiveLimit: false, // 是否认证可领
                featuring: false, // 首页展示
                workType: $('#workTypeSelect option').eq(0).attr('value'), // 作业类型默认选中第一个
                task: $('#taskSelect option').eq(0).attr('value') // 任务类型默认选中第一个
            }
        }
        OSSFileUploader(vm);
        OSSImageUploader(vm);
    }
});

// 获取链接参数
function getQuery (item){
    var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
    return svalue ? decodeURIComponent(svalue[1]) : '';
}