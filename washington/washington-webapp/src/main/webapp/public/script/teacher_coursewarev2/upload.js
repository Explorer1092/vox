/**
 * @author: pengmin.chen
 * @description: "课件大赛-上传作品"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'voxLogs'], function ($, ko, YQ) {
    var uploadModal = function () {
        var self = this;
        var gradeList = [
            {
                id: 1,
                name: "一年级"
            },
            {
                id: 2,
                name: "二年级"
            },
            {
                id: 3,
                name: "三年级"
            },
            {
                id: 4,
                name: "四年级"
            },
            {
                id: 5,
                name: "五年级"
            },
            {
                id: 6,
                name: "六年级"
            }
        ];
        var termList = [
            {
                id: 1,
                name: "上学期"
            },
            {
                id: 2,
                name: "下学期"
            }
        ];
        var subjectList = [
            {
                englishName: "CHINESE",
                id: 101,
                name: "语文"
            },
            {
                englishName: "MATH",
                id: 102,
                name: "数学"
            },
            {
                englishName: "ENGLISH",
                id: 103,
                name: "英语"
            }
        ];
        var awardList = [
            {
                id: -1,
                name: "无"
            },
            {
                id: 1,
                name: "国家级"
            },
            {
                id: 2,
                name: "省级"
            },
            {
                id: 3,
                name: "市级"
            },
            {
                id: 4,
                name: "校级"
            },
            {
                id: 5,
                name: "其他"
            }
        ];
        doTrack('o_8mASDNWxoR');
        $.extend(self, {
            isIEEnvironment: ko.observable(false), // 是否是IE环境
            isShowUploadSuccess: ko.observable(false), // 上传成功弹窗
            rejectCourseInfo: ko.observable(''), // 驳回原因
            gradeList: ko.observableArray(gradeList), // 年级列表
            termList: ko.observableArray(termList), // 学期列表
            subjectList: ko.observableArray(subjectList), // 学科列表
            bookList: ko.observableArray([]), // 教材列表
            unitList: ko.observableArray([]), // 单元列表
            lessonList: ko.observableArray([]), // 课程列表
            awardList: ko.observableArray(awardList),

            choiceGradeInfo: ko.observable(gradeList[0]), // 选择的年级
            choiceTermInfo: ko.observable(termList[0]), // 选择的学期
            choiceSubjectInfo: ko.observable({}), // 选择的科目
            choiceBookInfo: ko.observable({}), // 选择的教材
            choiceUnitInfo: ko.observable({}), // 选择的单元
            choiceLessonInfo: ko.observable({}), // 选择的课程
            choiceAwardInfo: ko.observable(awardList[0]), // 选择的奖状

            isShowBookSelect: ko.observable(false), // 是否显示教材下拉
            isShowGradeSelect: ko.observable(false), // 是否显示年级下拉
            isShowTermSelect: ko.observable(false), // 是否显示学期下拉
            isShowUnitSelect: ko.observable(false), // 是否显示单元下拉
            isShowLessonSelect: ko.observable(false), // 是否显示课程下拉
            isShowAwardSelect: ko.observable(false), // 是否显示奖状级别下拉

            // upload main module
            needCanvasCreatePoster: ko.observable(true), // 是否需要canvas动态生成一张封面图（默认封面 + title）
            isShowUploadPoster: ko.observable(false), // 是否显示上传封面图按钮
            isShowUploadAward: ko.observable(true), // 是否显示上传奖状按钮
            isShowUploadWord: ko.observable(true), // 是否显示上传Word按钮
            isShowReUploadWord: ko.observable(false), // 是否显示重新上传word按钮
            isShowUploadCourse: ko.observable(true), // 是否显示上传课件按钮（ppt或zip）
            isShowReUploadCourse: ko.observable(false), // 是否显示重新上传课件按钮（ppt或zip）
            isShowUploadImages: ko.observable(true), // 是否显示上传图片按钮
            isShowUploadAddImages: ko.observable(true), // 是否显示新增上传图片按钮
            inputAwardDesc: ko.observable(''), // 荣誉全称
            awardImageSrc: ko.observable(''), // 奖状信息
            awardImageName: ko.observable(''), // 奖状name
            posterSrc: ko.observable(''), // 封面图地址
            wordName: ko.observable(''), // word名称（前台）
            wordSrc: ko.observable(''), // word地址（后台）
            courseName: ko.observable(''), // 课件名称（前台）
            courseSrc: ko.observable(''), // 课件地址（后台）
            courseType: ko.observable('ppt'), // 课件类型
            imagesSrcList: ko.observableArray([]), // 上传的图片列表
            isShowDeleteImageSure: ko.observable(false), // 是否显示删除图片却弹窗
            isShowDeleteImageIndex: ko.observable(-1), // 删除图片的索引
            inputCourseName: ko.observable(''), // 输入的资源标题
            inputCourseDescription: ko.observable(''), // 输入的资源简介
            inputCourseDescriptionLeft: ko.observable(140), // 剩余可输入的字数
            commitCanActive: ko.observable(false), // 提交按钮是否可点击
            courseStatus: ko.observable(''), // 课件状态
            isUserUploadCover: ko.observable(false), // 封面是否由用户上传

            // 点击教材下拉
            clickSeries: function (data, event) {
                self.isShowBookSelect(!self.isShowBookSelect());

                self.isShowGradeSelect(false);
                self.isShowTermSelect(false);
                self.isShowUnitSelect(false);
                self.isShowLessonSelect(false);
                self.isShowAwardSelect(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击年级下拉
            clickGrade: function (data, event) {
                self.isShowGradeSelect(!self.isShowGradeSelect());

                self.isShowBookSelect(false);
                self.isShowTermSelect(false);
                self.isShowUnitSelect(false);
                self.isShowLessonSelect(false);
                self.isShowAwardSelect(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击学期下拉
            clickTerm: function (data, event) {
                self.isShowTermSelect(!self.isShowTermSelect());

                self.isShowBookSelect(false);
                self.isShowGradeSelect(false);
                self.isShowUnitSelect(false);
                self.isShowLessonSelect(false);
                self.isShowAwardSelect(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击单元下拉
            clickUnit: function (data, event) {
                self.isShowUnitSelect(!self.isShowUnitSelect());

                self.isShowBookSelect(false);
                self.isShowGradeSelect(false);
                self.isShowTermSelect(false);
                self.isShowLessonSelect(false);
                self.isShowAwardSelect(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击课程下拉
            clickLession: function (data, event) {
                self.isShowLessonSelect(!self.isShowLessonSelect());

                self.isShowBookSelect(false);
                self.isShowGradeSelect(false);
                self.isShowTermSelect(false);
                self.isShowUnitSelect(false);
                self.isShowAwardSelect(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },
            // 点击奖状下拉
            clickAward: function (data, event) {
                self.isShowAwardSelect(!self.isShowAwardSelect());

                self.isShowBookSelect(false);
                self.isShowGradeSelect(false);
                self.isShowTermSelect(false);
                self.isShowUnitSelect(false);
                self.isShowLessonSelect(false);

                bindDocumentOneClick();
                event.stopPropagation();
            },

            // 选择年级(不影响任何下拉)
            choiceGrade: function (data) {
                doTrack('o_Q5vfl5Wz4N', userInfo.subject, data.name);
                self.choiceGradeInfo(data);
                requestBookListInfo();
            },
            // 选择学期(不影响任何下拉)
            choiceTerm: function (data) {
                doTrack('o_Ss2pQSKFIk', userInfo.subject, data.name);
                self.choiceTermInfo(data);
                requestBookListInfo();
            },
            // 选择科目(先影响教材下拉，进而影响年级下拉)
            choiceSubject: function (data) {
                doTrack('o_bPtftQLGO8', userInfo.subject, data.name);
                // 点击原科目不触发更新
                if (data.id === self.choiceSubjectInfo().id) return ;

                self.choiceSubjectInfo(data);

                // 对于需要合成canvas封面的，才需要动态设置
                if (self.needCanvasCreatePoster()) setDefautlPoster(self.choiceSubjectInfo().englishName);

                // 重置标题、描述、封面、课件
                // resetCourseInfo();

                requestBookListInfo(); // subject -> book
            },
            // 选择教材(只影响年级下拉)
            choiceBook: function (data) {
                doTrack('o_9jijQnJf10', userInfo.subject, data.id);
                self.choiceBookInfo(data);

                self.unitList(self.choiceBookInfo().unitList); // 取当前book下的unitList
                self.choiceUnitInfo(useUnitIdFilterUnitList(self.unitList()[0].unitId)); //取当前book下的unitList的第一个

                requestLessionListInfo();
            },
            // 选择单元(只影响课程下拉)
            choiceUnit: function (data) {
                doTrack('o_94mFPNbbti', userInfo.subject, data.unitId);
                self.choiceUnitInfo(data);
                requestLessionListInfo();
            },
            // 选择课程(不影响任何下拉)
            choiceLession: function (data) {
                doTrack('o_fkZHTKgA35', userInfo.subject, data.lessonId);
                self.choiceLessonInfo(data);
                self.inputCourseName(self.choiceLessonInfo().lessonRealName);

                // 非用户上传的图片，下拉变化时合成一次封面
                if (!self.isUserUploadCover()) drawCanvasDefaultPoster();
            },
            // 选择奖状级别
            choiceAward: function (data) {
                if (data.id === self.choiceAwardInfo().id) return;
                self.choiceAwardInfo(data);

                self.inputAwardDesc('');
                self.awardImageSrc('');
                self.awardImageName('');
                self.isShowUploadAward(true);
            },
            // 点击上传奖状图片
            choiceAwardImage: function () {
                if (self.choiceAwardInfo().id === self.awardList()[0].id) { // 当前选择为无时，不允许上传奖状照片
                    showPopupTip('请先选择奖项级别哦~');
                    return;
                }
                if (!self.inputAwardDesc()) { // 当前选择为无时，不允许上传奖状照片
                    showPopupTip('请先填写获奖荣誉全称哦~');
                    return;
                }
                $('#JS-awardImage').val('').click();
            },

            // 点击上传封面
            choicePoster: function () {
                $('#JS-poster').val('').click();
            },
            // 点击上传word
            choiceWord: function () {
                $('#wordSelect').click();
            },
            // 点击上传课件
            choiceCourse: function () {
                $('#courseSelect').click();
            },
            // 点击上传图片
            choiceImages: function () {
                $('#JS-images').val('').click();
            },
            // 保存课件
            saveCourse: function () {
                doTrack('o_CIZfLXJ1Np', userInfo.subject, YQ.getQuery('courseId'));
                reqeustSaveCourse(function (res, res2, res3) {
                    // 参数res表示saveContent的回调, 参数res2表示saveAwardImage的回调, 参数res3表示saveBook的回调
                    if (res) {
                        if (!res[0].success) {
                            alertTip(res[0].info);
                            return ;
                        }
                    }
                    if (res2) {
                        // 该参数表示saveAwardImage的回调
                        if (!res2[0].success) {
                            showPopupTip(res2[0].info || '上传出错了，稍后重试'); // 上传模块使用右下角popup提示
                            return;
                        }
                        if (res2[0].data.url) {
                            self.isShowUploadAward(false);
                            self.awardImageSrc(res2[0].data.url);
                            self.awardImageName(res2[0].data.name);
                        }
                    }
                    if (res3) {
                        if (!res3[0].success) {
                            alertTip(res3[0].info);
                            return ;
                        }
                    }
                    alertTip('保存成功！');
                });
            },
            // 提交课件
            commitCourse: function () {
                doTrack('o_936v9S4rWc', userInfo.subject, YQ.getQuery('courseId'));
                if (!self.inputCourseName() || self.isShowUploadPoster() || self.isShowUploadWord() || self.isShowUploadCourse() || self.isShowUploadImages()) return ;
                reqeustSaveCourse(function (res, res2, res3) {
                    // 参数res表示saveContent的回调, 参数res2表示saveAwardImage的回调, 参数res3表示saveBook的回调
                    if (res) {
                        if (!res[0].success) {
                            alertTip(res[0].info);
                            return ;
                        }
                    }
                    if (res2) {
                        if (!res2[0].success) {
                            showPopupTip(res2[0].info || '上传出错了，稍后重试'); // 上传模块使用右下角popup提示
                            return;
                        }
                        if (res2[0].data.url) {
                            self.isShowUploadAward(false);
                            self.awardImageSrc(res2[0].data.url);
                            self.awardImageName(res2[0].data.name);
                        }
                    }
                    if (res3) {
                        if (!res3[0].success) {
                            alertTip(res3[0].info);
                            return ;
                        }
                    }
                    requestCommitCourse(); // 保存接口通过后再commit
                });
            },
            // 跳到个人中心
            seePersonalCenter: function () {
                self.isShowUploadSuccess(false);
                window.location.href = '/courseware/contest/personalcenter.vpage';
            },
            // 继续上传
            continueUpload: function () {
                self.isShowUploadSuccess(false);
            },
            // 删除上传的图片
            deleteUploadImage: function (index, data) {
                self.isShowDeleteImageSure(true);
                self.isShowDeleteImageIndex(index);
            },
            // 确定删除图片
            sureDeleteImage: function () {
                deleteUploadImage();
            },
            // 输入回调
            inputDescriptionKeyUp: function () {
                if (self.inputCourseDescription().length > 140) return;
                self.inputCourseDescriptionLeft(140 - (self.inputCourseDescription().length || 0));
            }
        });

        // 请求课件详情
        function requestCourseDetailInfo() {
            $.ajax({
                url: '/courseware/contest/myworks/detail.vpage',
                type: 'GET',
                data: {
                    id: YQ.getQuery('courseId')
                },
                success: function (res) {
                    if (res.success) {
                        initUploadData(res);
                        self.rejectCourseInfo(res.desc);
                        self.isUserUploadCover(res.isUserUpload);
                    } else if (res.errorCode === '-100' || res.errorCode === '-300') { // 课件不存在 或 老师ID不匹配
                        alertTip(res.info, function () {
                            window.location.href = '/courseware/contest/personalcenter.vpage';
                        });
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 初始化课件详情（回显）
        function initUploadData(detailData) {
            self.inputCourseName(detailData.title); // 标题
            self.inputCourseDescription(detailData.description); // 简介
            if (self.inputCourseDescription()) self.inputCourseDescriptionLeft((140 - self.inputCourseDescription().length) > 0 ? (140 - self.inputCourseDescription().length) : 0); // 简介剩余文字数
            self.courseStatus(detailData.status); // 课件状态

            // 奖状
            for (var i = 0; i < self.awardList().length; i++) {
                if (self.awardList()[i].id === detailData.awardLevelId) self.choiceAwardInfo(self.awardList()[i]);
            }
            if (detailData.awardIntroduction) {
                self.inputAwardDesc(detailData.awardIntroduction);
            }
            if (detailData.awardPreview && detailData.awardPreview.url) {
                self.awardImageSrc(detailData.awardPreview.url);
                self.awardImageName(detailData.awardPreview.name);
                self.isShowUploadAward(false);
            }

            // 封面（存在封面 且 为用户自主上传）
            if (detailData.coverUrl && detailData.isUserUpload) {
                self.needCanvasCreatePoster(false); // 用户增加上传过，不需要canvas生成封面
                self.posterSrc(detailData.coverUrl);
                self.isShowUploadPoster(false);
            }

            // 教案（word）
            if (detailData.wordUrl) {
                self.wordName(filterFileName(detailData.wordName));
                self.wordSrc(detailData.wordUrl);
                self.isShowUploadWord(false);
                self.isShowReUploadWord(true);
                $('#wordProgress').find('.yellow_bar').width('100%'); // 进度条百分比
                $('#wordProgress').find('.value').text('100%'); // 进度条值
            }

            // 课件（ppt/zip）
            if (detailData.coursewareFileUrl) {
                self.courseName(filterFileName(detailData.coursewareFileName));
                self.courseSrc(detailData.coursewareFileUrl);
                if (['.zip', '.rar', '.gz', '.7z'].indexOf(detailData.coursewareFileName.substring(detailData.coursewareFileName.lastIndexOf('.'))) > -1) {
                    self.courseType('zip');
                } else {
                    self.courseType('ppt');
                }
                self.isShowUploadCourse(false);
                self.isShowReUploadCourse(true);
                $('#courseProgress').find('.yellow_bar').width('100%'); // 进度条百分比
                $('#courseProgress').find('.value').text('100%'); // 进度条值
            }

            // 图片（png/jpg）
            if (detailData.pictureUrlList && detailData.pictureUrlList.length) {
                self.imagesSrcList(detailData.pictureUrlList);
                self.isShowUploadImages(false);
            }

            // 年级
            if (detailData.clazzLevel) {
                self.choiceGradeInfo(self.gradeList()[detailData.clazzLevel - 1]);
            }

            // 学期
            if (detailData.term) {
                self.choiceTermInfo(self.termList()[detailData.term - 1]);
            }

            // 下拉回显再各个list的接口回调中完成
            requestOtherSelectInfo(detailData); // 参数决定后续是否回显数据
        }

        // 请求所有的下拉列表
        function requestOtherSelectInfo(detailData) {
            if (detailData.subject) { // 记录中存在subject，跳过获取默认学科步骤
                dealwithSubject(detailData);
            } else { // 不存在则根据老师自身情况选中指定学科（主学科或包班制情况）
                requestDefaultSubject(detailData);
            }
        }

        // 请求老师的主学科或包班情况（即默认展示 哪个老师）
        function requestDefaultSubject(detailData) {
            $.ajax({
                url: '/courseware/contest/multiClazzInfo.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.success) {
                        detailData.subject = res.data.name; // 借用detailData 增加subject即可
                        dealwithSubject(detailData);
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 处理学科，设置当前选择的学科
        function dealwithSubject(detailData) {
            if (detailData && detailData.subject) { // 存在subject，则回显
                self.choiceSubjectInfo(useSubjectFilterSubjectList(detailData.subject));
            } else { // 不存在则取第一个
                self.choiceSubjectInfo(self.subjectList()[0]);
            }

            // 对于需要合成canvas封面的，才需要动态设置
            if (self.needCanvasCreatePoster()) setDefautlPoster(self.choiceSubjectInfo().englishName);
            requestBookListInfo(detailData);
        }

        // 请求 教材-单元 二级列表
        function requestBookListInfo(detailData) {
            $.ajax({
                url: '/courseware/contest/booklist.vpage',
                type: 'GET',
                data: {
                    subject: self.choiceSubjectInfo().englishName, // 学科
                    term: self.choiceTermInfo().id, // 学期
                    clazzLevel: self.choiceGradeInfo().id // 年级
                },
                success: function (res) {
                    if (res.success) {
                        self.bookList(res.books);
                        if (res.books.length) {
                            // 存在bookId则回显
                            if (detailData && detailData.bookId) {
                                self.choiceBookInfo(useBookIdFilterSeriesList(detailData.bookId));
                            } else { // 不存在取第一个
                                self.choiceBookInfo(self.bookList()[0]);
                            }

                            self.unitList(self.choiceBookInfo().unitList);
                            if (self.bookList()[0].unitList.length) {
                                // 存在unitId则回显
                                if (detailData && detailData.unitId) {
                                    self.choiceUnitInfo(useUnitIdFilterUnitList(detailData.unitId));
                                } else {
                                    self.choiceUnitInfo(self.bookList()[0].unitList[0]);
                                }

                                // 请求教程
                                requestLessionListInfo(detailData);
                            } else {
                                self.choiceUnitInfo({});
                            }
                        } else {
                            self.choiceBookInfo({});
                        }
                    } else if (res.errorCode === '-200') {
                        alertTip('请先登录', function () {
                            window.location.href = '/login.vpage';
                        });
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！')
                }
            });
        }

        // 请求课程名称
        function requestLessionListInfo(detailData) {
            $.ajax({
                url: '/courseware/contest/lessions.vpage',
                type: 'GET',
                data: {
                    unitId: self.choiceUnitInfo().unitId // 单元ID
                },
                success: function (res) {
                    if (res.success) {
                        self.lessonList(res.data);
                        if (self.lessonList().length) {
                            if (detailData && detailData.lessonId) { // 存在lessonId，则回显
                                self.choiceLessonInfo(useLessonIdFilterLessonList(detailData.lessonId));
                            } else { // 不存在则取第一个
                                self.choiceLessonInfo(self.lessonList()[0]);
                            }

                            if ((detailData && !detailData.title) || !detailData) { // 存在detailData 且 title为空的时候 或 不存在detailData 是才使用title
                                self.inputCourseName(self.choiceLessonInfo().lessonRealName); // 不存在的情况下默认取lessonRealName，也可修改自行输入
                            }

                            // 生成图片(非用户上传时，合成)
                            if (!self.isUserUploadCover()) drawCanvasDefaultPoster();
                        } else {
                            self.choiceLessonInfo({});
                        }
                    } else if (res.errorCode === '-200') {
                        alertTip('请先登录', function () {
                            window.location.href = '/login.vpage';
                        });
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 设置默认封面
        function setDefautlPoster(subjectEnglishName) {
            switch(subjectEnglishName) {
                case 'ENGLISH':
                    self.posterSrc(cdnHeader + '/public/resource/courseware/images/poster_english_v4.png'); // 可跨域路径
                    break;
                case 'CHINESE':
                    self.posterSrc(cdnHeader + '/public/resource/courseware/images/poster_chinese_v4.png');
                    break;
                case 'MATH':
                    self.posterSrc(cdnHeader + '/public/resource/courseware/images/poster_math_v4.png');
                    break;
                default:
            }
            self.isShowUploadPoster(false);
        }

        // 根据subject englishName 来过滤出对应的年级信息
        function useSubjectFilterSubjectList(englishName) {
            var thatSubjectInfo = null;
            for (var i = 0; i < self.subjectList().length; i++) {
                if (englishName === self.subjectList()[i].englishName) {
                    thatSubjectInfo = self.subjectList()[i];
                    break;
                }
            }
            return thatSubjectInfo;
        }

        // 根据bookId 来过滤出对应的教材信息
        function useBookIdFilterSeriesList(bookId) {
            var thatBookInfo = null;
            for (var j = 0; j < self.bookList().length; j++) {
                if (bookId === self.bookList()[j].id) {
                    thatBookInfo = self.bookList()[j];
                    break;
                }
            }
            return thatBookInfo;
        }

        // 根据UnitId 来过滤出对应单元信息
        function useUnitIdFilterUnitList(unitId) {
            var thatUnitInfo = null;
            for (var m = 0; m < self.unitList().length; m++) {
                if (unitId === self.unitList()[m].unitId) {
                    thatUnitInfo = self.unitList()[m];
                    break;
                }
            }
            return thatUnitInfo;
        }

        // 根据useLessonId 来过滤出对应的课程信息
        function useLessonIdFilterLessonList(lessonId) {
            var thatLessonInfo = null;
            for (var n = 0; n < self.lessonList().length; n++) {
                if (lessonId === self.lessonList()[n].lessonId) {
                    thatLessonInfo = self.lessonList()[n];
                    break;
                }
            }
            return thatLessonInfo;
        }

        // 上传奖状图片
        function requestUploadAwardImage(file) {
            if (!file) { // 非上传图片时校验
                if (self.choiceAwardInfo().id !== self.awardList()[0].id) { // 选择某个级别（不为 无）
                    // 已上传图片 但 未填写全称
                    if (!self.awardImageSrc()) {
                        alertTip('请上传奖项证书或奖杯照片哦~');
                        return;
                    }
                    if (!self.inputAwardDesc()) {
                        alertTip('请填写获奖荣誉全称哦~');
                        return;
                    }
                }
            }

            var awardImageFormData = new FormData();
            awardImageFormData.append('id', YQ.getQuery('courseId'));
            awardImageFormData.append('file', file || '');
            awardImageFormData.append('awardLevelName', self.choiceAwardInfo().name);
            awardImageFormData.append('awardLevelId', self.choiceAwardInfo().id);
            awardImageFormData.append('awardIntroduction', self.inputAwardDesc());
            awardImageFormData.append('awardUrl', (file || self.choiceAwardInfo().id === self.awardList()[0].id) ? '' : self.awardImageSrc());
            awardImageFormData.append('awardName', (file || self.choiceAwardInfo().id ===self.awardList()[0].id) ? '' : self.awardImageName());
            return ($.ajax({
                url: '/courseware/contest/uploadAwards.vpage',
                type: 'POST',
                data: awardImageFormData,
                processData: false,
                contentType: false,
                async: true,
                timeout: 0
            }));
        }

        // 上传封面(type='canvas'表示默认动态生成封面带title， type='default'表示IE环境下的默认封面不带title)
        function requestUploadPoster(file, type) {
            // type = 'canvas' || type = 'default' 表示用户为上传，默认自动生成，这两种情况需要搭配dfd
            if (type === 'canvas' || type === 'default') {
                var isUserUpload = false;
                var dfd = new $.Deferred();
            } else {
                var isUserUpload = true;
            }

            var posterUrl = type !== 'default' ? '' : self.posterSrc(); // 默认封面，IE才传
            var posterName = type !== 'default' ? '' : self.posterSrc().substring(self.posterSrc().lastIndexOf('/') + 1); // 默认封面文件名，IE才传

            var posterFormData = new FormData();
            posterFormData.append('id', YQ.getQuery('courseId'));
            posterFormData.append('file', file);
            posterFormData.append('url', posterUrl);
            posterFormData.append('name', posterName);
            posterFormData.append('isUserUpload', (type === 'canvas' || type === 'default') ? false : true);
            $.ajax({
                url: '/courseware/contest/uploadCover.vpage',
                type: 'POST',
                data: posterFormData,
                processData: false,
                contentType: false,
                async: true,
                timeout: 0,
                success: function (res) {
                    if (res.success) {
                        self.isShowUploadPoster(false);
                        if (!isUserUpload) {
                            dfd.resolve();
                        } else {
                            self.isUserUploadCover(true);
                            self.posterSrc(res.url); // 用户自己上传的图片才立即回显，默认生成的图片，可能需要再次生成，不会回显
                            self.needCanvasCreatePoster(false); // 上传的封面非默认图片，则将flag置为false，不再支持合成图片
                        }
                    } else {
                        if (!isUserUpload) {
                            dfd.resolve(res.info);
                        } else {
                            showPopupTip(res.info || '上传出错了，稍后重试'); // 上传模块使用右下角popup提示
                        }
                    }
                },
                error: function () {
                    showPopupTip('上传出错了，稍后重试'); // 上传模块使用右下角popup提示

                    if (!isUserUpload) {
                        dfd.reject();
                    }
                }
            });

            if (!isUserUpload) {
                return dfd;
            }
        }

        // 将阿里云的链接反向传给后端存储
        function requestUploadOSSUrl(fileSrc, fileName) {
            $.ajax({
                url: '/courseware/contest/updateFileUrl.vpage',
                type: 'POST',
                data: {
                    id: YQ.getQuery('courseId'),
                    fileUrl: fileSrc,
                    name: fileName
                },
                success: function (res) {
                    if (!res.success) {
                        showPopupTip(res.info || '请求失败，稍后重试');
                    }
                },
                error: function () {
                    showPopupTip('请求失败，稍后重试');
                }
            });
        }

        // 上传图片
        function requestUploadImages(files) {
            var imagesFormData = new FormData();
            imagesFormData.append('id', YQ.getQuery('courseId'));
            for (var i = 0; i < files.length; i++) {
                imagesFormData.append('file', files[i]); // 对于多文件，append多次，key一致
            }
            $.ajax({
                url: '/courseware/contest/uploadPictures.vpage',
                type: 'POST',
                data: imagesFormData,
                processData: false,
                contentType: false,
                async: true,
                timeout: 0,
                success: function (res) {
                    if (res.success) {
                        self.isShowUploadImages(false);
                        self.imagesSrcList(res.pictureUrl);
                    } else {
                        showPopupTip(res.info || '上传出错了，稍后重试'); // 上传模块使用右下角popup提示
                    }
                },
                error: function () {
                    showPopupTip('上传出错了，稍后重试'); // 上传模块使用右下角popup提示
                }
            });
        }

        // 删除已经上传的图片
        function deleteUploadImage() {
            var imagesSrcList = self.imagesSrcList();
            var deleteIndex = self.isShowDeleteImageIndex();
            var deleteUrl = self.imagesSrcList()[deleteIndex];
            $.ajax({
                url: '/courseware/contest/deletePicture.vpage',
                type: 'POST',
                data: {
                    id: YQ.getQuery('courseId'), // 课件ID
                    pictureUrl: deleteUrl.url // 图片链接
                },
                success: function(res) {
                    if (res.success) {
                        imagesSrcList.splice(deleteIndex, 1); // 删除
                        self.imagesSrcList(imagesSrcList);
                        if (!self.imagesSrcList().length) {
                            self.isShowUploadImages(true);
                        } else {
                            self.isShowUploadImages(false);
                        }

                        self.isShowDeleteImageSure(false); // 隐藏弹窗
                        self.isShowDeleteImageIndex(-1);
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function() {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 保存课件
        function reqeustSaveCourse(cb) {
            if (self.needCanvasCreatePoster()) { // 需要合成封面图 且 非IE浏览器
                drawCanvasDefaultPoster(cb);
            } else { // 已经上传过封面，则直接走保存接口
                $.when(requestSaveContent(), requestUploadAwardImage(), requestSaveBook()).done(cb);
            }
        }

        // 绘制封面（默认图 + title）
        function drawCanvasDefaultPoster(cb) {
            if (!self.isIEEnvironment()) { // 非IE
                var posterCanvas = document.getElementById('posterCanvas'),
                    posterContext = posterCanvas.getContext('2d'),
                    posterCanvasWidth = posterCanvas.width,
                    posterCanvasHeight = posterCanvas.height,
                    posterDefaultImg = new Image();

                posterDefaultImg.setAttribute('crossOrigin', 'anonymous');
                posterDefaultImg.src = self.posterSrc();
                posterDefaultImg.onload = function () {
                    posterContext.clearRect(0, 0, posterCanvasWidth, posterCanvasHeight); // 清空画布，保证绘制干净
                    posterContext.drawImage(posterDefaultImg, 0, 0, posterCanvasWidth, posterCanvasHeight); // 填充图片

                    posterContext.font = '16px MicrosoftYaHei'; // 填充title
                    posterContext.textAlign = 'center';
                    // posterContext.fillText(self.inputCourseName(), 120, 114); // 如不处理限制文本宽度，直接绘制即可
                    var wordWidth = 0; // 字符宽度
                    var titleStr = self.inputCourseName(); // 操作的title
                    if (posterContext.measureText(titleStr).width < 190) { // title小于最大宽度，直接绘制
                        posterContext.fillText(titleStr, 120, 114);
                    } else { // title大于最大宽度则需要截取绘制
                        for (var i = 0; i < titleStr.length; i++) { // 遍历，根据当前的文本长度是否大于指定宽度，来截取
                            wordWidth += posterContext.measureText(titleStr[i]).width; // 逐渐追加的文本宽度
                            if (wordWidth > 190) { // 指定（190 + '...'）
                                posterContext.fillText(titleStr.substring(0, i) + '...', 120, 114);
                                break;
                            }
                        }
                    }
                    var base64Url = posterCanvas.toDataURL('image/png', 1.0); // toBase64

                    if (cb) { // 存在回调表示在点保存按钮的时候存储封面
                        // $.when(requestUploadPoster(dataURLtoFile(base64Url, 'default_poster.png'), 'canvas'), requestSaveBook(), requestSaveContent()).done(cb);
                        requestUploadPoster(dataURLtoFile(base64Url, 'default_poster.png'), 'canvas');
                        $.when(requestSaveContent(), requestUploadAwardImage(), requestSaveBook()).done(cb);
                    } else {
                        requestUploadPoster(dataURLtoFile(base64Url, 'default_poster.png'), 'canvas');
                        $.when(requestSaveBook()).done(function (res) {
                            if (!res.success) {
                                alertTip(res.info);
                                return;
                            }
                        });
                    }
                }
            } else {
                requestUploadPoster('', 'default');
                $.when(requestSaveContent(), requestUploadAwardImage(), requestSaveBook()).done(cb);
            }
        }

        // 将base64转换为文件
        function dataURLtoFile(dataurl, filename) {
            var arr = dataurl.split(','),
                mime = arr[0].match(/:(.*?);/)[1],
                bstr = atob(arr[1]),
                n = bstr.length,
                u8arr = new Uint8Array(n);
            while(n--){
                u8arr[n] = bstr.charCodeAt(n);
            }
            return new File([u8arr], filename, {type: mime});
        }

        // 保存选择的条件
        function requestSaveBook() {
            return ($.ajax({
                url: '/courseware/contest/myworks/updatebook.vpage',
                type: 'POST',
                data: {
                    id: YQ.getQuery('courseId'), // 课件ID
                    clazzLevel: self.choiceGradeInfo().id, // 年级
                    term: self.choiceTermInfo().id, // 学期
                    subject: self.choiceSubjectInfo().englishName, // 学科
                    bookId: self.choiceBookInfo().id, // book
                    unitId: self.choiceUnitInfo().unitId, // 单元
                    lessonId: self.choiceLessonInfo().lessonId // 课程
                }
            }));
        }

        // 保存输入的标题和简介
        function requestSaveContent() {
            if (!self.inputCourseName()) return ;
            return ($.ajax({
                url: '/courseware/contest/myworks/updatecontent.vpage',
                type: 'POST',
                data: {
                    id: YQ.getQuery('courseId'),
                    title: self.inputCourseName(),
                    description: self.inputCourseDescription(),
                    status: self.courseStatus()
                }
            }));
        }

        // 提交课件
        function requestCommitCourse() {
            if (self.isShowUploadPoster()) {
                showPopupTip('你还未上传封面图哦~', 'warn');
                return ;
            }
            if (self.isShowUploadWord()) {
                showPopupTip('你还未上传教案哦~', 'warn');
                return ;
            }
            if (self.isShowUploadCourse()) {
                showPopupTip('你还未上传课件哦~', 'warn');
                return ;
            }
            if (self.isShowUploadImages()) {
                showPopupTip('你还未上传图片哦~', 'warn');
                return ;
            }
            if (self.imagesSrcList().length < 5) {
                showPopupTip('图片必须要上传5张哦~', 'warn');
                return ;
            }

            $.ajax({
                url: '/courseware/contest/myworks/commit.vpage',
                data: {
                    id: YQ.getQuery('courseId')
                },
                type: 'POST',
                success: function (res) {
                    if (res.success) {
                        self.isShowUploadSuccess(true);
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 过滤文件名（控制显示中间15个字加省略号）
        function filterFileName(fileName) {
            var noSuffixFileName = fileName.substring(0, fileName.lastIndexOf('.')); // 文件名（不带后缀）
            var suffix = fileName.substring(fileName.lastIndexOf('.')); // 后缀

            if (noSuffixFileName.length <= 15) return fileName;
            else return (noSuffixFileName.substring(0, 15) + '...' + suffix);
        }

        // 点击页面收起所有的下拉
        function bindDocumentOneClick() {
            $(window).on('click', function () {
                self.isShowBookSelect(false);
                self.isShowGradeSelect(false);
                self.isShowTermSelect(false);
                self.isShowUnitSelect(false);
                self.isShowLessonSelect(false);
                self.isShowAwardSelect(false);
            });
        }

        // global event
        function bindGlobalEvent() {
            // 监听上传封面
            $(document).on('change', '#JS-awardImage', function () {
                var file = $('#JS-awardImage')[0].files[0];

                if (!file) return ;
                if (['image/jpeg', 'image/jpg','image/png'].indexOf(file.type) === -1) {
                    showPopupTip('你选择的文件格式有误哦~', 'warn');
                    return ;
                }
                if (file.size > 2 * 1024 * 1000) { // 限制上传2M以内的图片
                    showPopupTip('图片大小不能超过2M哦~', 'warn');
                    return ;
                }

                $.when(requestUploadAwardImage(file)).done(function (res) {
                    // 该参数表示saveAwardImage的回调
                    if (!res.success) {
                        showPopupTip(res.info || '上传出错了，稍后重试'); // 上传模块使用右下角popup提示
                        return;
                    }
                    self.isShowUploadAward(false);
                    self.awardImageSrc(res.data.url);
                    self.awardImageName(res.data.name);
                });
            });

            // 监听上传封面
            $(document).on('change', '#JS-poster', function () {
                var file = $('#JS-poster')[0].files[0];

                if (!file) return ;
                if (['image/jpeg', 'image/jpg','image/png'].indexOf(file.type) === -1) {
                    showPopupTip('你选择的文件格式有误哦~', 'warn');
                    return ;
                }
                if (file.size > 2 * 1024 * 1000) { // 限制上传2M以内的图片
                    showPopupTip('图片大小不能超过2M哦~', 'warn');
                    return ;
                }

                requestUploadPoster(file);
            });

            // 监听上传图片
            $(document).on('change', '#JS-images', function () {
                var files = $('#JS-images')[0].files;
                if (!files.length) return ;
                if (!self.imagesSrcList().length && files.length > 5) { // 第一次上传且超过5张
                    showPopupTip('仅支持上传5张图片哦~', 'warn');
                    return ;
                }
                if (self.imagesSrcList().length && files.length > (5 - self.imagesSrcList().length)) {
                    showPopupTip('您最多还能上传' + (5 - self.imagesSrcList().length) + '张图片哦~', 'warn');
                    return ;
                }
                if (files.length > 5) {
                    showPopupTip('仅支持上传5张图片哦~', 'warn');
                    return ;
                }
                // 循环查看每张图片的size和格式
                var hasImageTypeError = false;
                var hasSizeOver = false;
                for (var i = 0; i < files.length; i++) {
                    if (['image/jpeg', 'image/jpg','image/png'].indexOf(files[i].type) === -1) {
                        hasImageTypeError = true;
                    }
                    if (files[i].size > 2 * 1024 * 1000) { // 限制上传100M以内的课件
                        hasSizeOver = true;
                    }

                }
                if (hasImageTypeError) {
                    showPopupTip('你选择的文件格式有误哦~', 'warn');
                    return ;
                }
                if (hasSizeOver) {
                    showPopupTip('每张图片大小不能超过2M哦~', 'warn');
                    return ;
                }

                requestUploadImages(files);
            });
        }

        // 阿里云上传
        function OSSUploader() {
            // 上传word
            var wordUploader = new plupload.Uploader({
                runtimes: 'html5,flash,silverlight,html4',
                browse_button: 'wordSelect', // 选择按钮
                multi_selection: false,
                url : 'http://oss.aliyuncs.com',
                filters: {
                    mime_types : [ // 限制上传的文件后缀，只允许上传doc和docx
                        { title : "Word files", extensions : "doc,docx" },
                    ],
                    max_file_size : '100mb', //最大只能上传100mb的文件
                    prevent_duplicates : false //不允许选取重复文件
                },
                init: {
                    PostInit: function(_this) {
                        document.getElementById('wordUpload').onclick = function() {
                            var upFileName = _this.files[0].name;
                            var index1 = upFileName.lastIndexOf(".");
                            var index2 = upFileName.length;
                            var suffix1 = upFileName.substring(index1 + 1, index2);
                            var _serverUrl = '/courseware/contest/getsignature.vpage?ext=' + suffix1;
                            set_upload_param(wordUploader, '', false, _serverUrl);
                            return false;
                        };
                    },
                    // 文件选择成功
                    FilesAdded: function(up, files) {
                        if (!files.length) return;
                        // 触发开始上传
                        $('#wordUpload').click();
                    },
                    // 开始上传
                    BeforeUpload: function(up, file) {
                        check_object_radio();
                        set_upload_param(up, file.name, true);

                        self.isShowUploadWord(false); // 隐藏重新上传按钮
                        self.isShowReUploadWord(false); // 隐藏选择文件按钮
                        self.wordName(filterFileName(file.name)); // 获取文件名
                    },
                    // 上传中
                    UploadProgress: function(up, file) {
                        $('#wordProgress').find('.yellow_bar').width(file.percent+ '%'); // 进度条百分比
                        $('#wordProgress').find('.value').text(file.percent+ '%'); // 进度条值
                        if (file.percent === 100) {
                            self.isShowReUploadWord(false);
                        }
                    },
                    // 上传结束
                    FileUploaded: function(up, file, info) {
                        if (info.status == 200) {
                            self.isShowReUploadWord(true); // 显示重新上传按钮
                            self.wordSrc(window.location.protocol + '//v.17xueba.com/' + wordUploader.settings.multipart_params.key); // 返回的地址
                            requestUploadOSSUrl(self.wordSrc(), self.wordName()); // 传给后端存储
                        } else {
                            showPopupTip(info.response, 'error');
                        }
                    },
                    // 上传出错
                    Error: function(up, err) {
                        if (err.code == -600) {
                            showPopupTip('课件大小不能超过100M哦~', 'error');
                        } else if (err.code == -601) {
                            showPopupTip('仅支持上传ppt或zip哦~', 'error');
                        } else if (err.code == -602) {
                            showPopupTip('这个文件已经上传过一遍了哦~', 'error');
                        } else {
                            showPopupTip('上传失败', 'error');
                        }
                    }
                }
            });
            // 上传课件
            var courseUploader = new plupload.Uploader({
                runtimes: 'html5,flash,silverlight,html4',
                browse_button: 'courseSelect', // 选择按钮
                multi_selection: false,
                url : 'http://oss.aliyuncs.com',
                filters: {
                    mime_types : [ // 限制上传的文件后缀，只允许上传ppt和zip,rar文件
                        { title : "Ppt files", extensions : "ppt,pptx" },
                        { title : "Zip files", extensions : "zip,rar" }
                    ],
                    max_file_size : '100mb', //最大只能上传100mb的文件
                    prevent_duplicates : false //不允许选取重复文件
                },
                init: {
                    PostInit: function(_this) {
                        document.getElementById('courseUpload').onclick = function() {
                            var upFileName = _this.files[0].name;
                            var index1=upFileName.lastIndexOf(".");
                            var index2=upFileName.length;
                            var suffix1=upFileName.substring(index1+1,index2);
                            var _serverUrl = '/courseware/contest/getsignature.vpage?ext=' + suffix1;
                            set_upload_param(courseUploader, '', false, _serverUrl);
                            return false;
                        };
                    },
                    // 文件选择成功
                    FilesAdded: function(up, files) {
                        if (!files.length) return;
                        // 触发开始上传
                        $('#courseUpload').click();
                    },
                    // 开始上传
                    BeforeUpload: function(up, file) {
                        check_object_radio();
                        set_upload_param(up, file.name, true);

                        self.isShowUploadCourse(false); // 隐藏重新上传按钮
                        self.isShowReUploadCourse(false); // 隐藏选择文件按钮
                        self.courseName(filterFileName(file.name)); // 获取文件名
                        if (['.zip', '.rar', '.gz', '.7z'].indexOf(file.name.substring(file.name.lastIndexOf('.'))) > -1) {
                            self.courseType('zip');
                        } else {
                            self.courseType('ppt');
                        }
                    },
                    // 上传中
                    UploadProgress: function(up, file) {
                        $('#courseProgress').find('.yellow_bar').width(file.percent+ '%'); // 进度条百分比
                        $('#courseProgress').find('.value').text(file.percent+ '%'); // 进度条值
                        if (file.percent === 100) {
                            self.isShowReUploadCourse(false);
                        }
                    },
                    // 上传结束
                    FileUploaded: function(up, file, info) {
                        if (info.status == 200) {
                            self.isShowReUploadCourse(true); // 显示重新上传按钮
                            self.courseSrc(window.location.protocol + '//v.17xueba.com/' + courseUploader.settings.multipart_params.key); // 返回的地址
                            requestUploadOSSUrl(self.courseSrc(), self.courseName()); // 传给后端存储
                        } else {
                            showPopupTip(info.response, 'error');
                        }
                    },
                    // 上传出错
                    Error: function(up, err) {
                        if (err.code == -600) {
                            showPopupTip('课件大小不能超过100M哦~', 'error');
                        } else if (err.code == -601) {
                            showPopupTip('仅支持上传ppt或zip哦~', 'error');
                        } else if (err.code == -602) {
                            showPopupTip('这个文件已经上传过一遍了哦~', 'error');
                        } else {
                            showPopupTip('上传失败', 'error');
                        }
                    }
                }
            });
            wordUploader.init();
            courseUploader.init();
        }

        // 简易通用弹窗
        function alertTip(content, callback) {
            var commonPopupHtml = "<div class=\"coursePopup commonAlert\">" +
                "<div class=\"popupInner popupInner-common\">" +
                    "<div class=\"closeBtn commonAlertClose\"></div>" +
                    "<div class=\"textBox\">" +
                        "<p class=\"shortTxt\">" + content + "</p>" +
                    "</div>" +
                    "<div class=\"otherContent\">" +
                        "<a class=\"surebtn commonSureBtn\" href=\"javascript:void(0)\">确 定</a>" +
                    "</div>" +
                "</div>" +
            "</div>";
            // 不存在则插入dom
            if (!$('.commonAlert').length) {
                $('body').append(commonPopupHtml);
            }
            // 监听按钮点击
            $(document).one('click', '.commonAlertClose',function(){ // 关闭
                $('.commonAlert').remove();
            }).one('click', '.commonSureBtn', function() { // 点击按钮
                if (callback) {
                    callback();
                } else {
                    $('.commonAlert').remove();
                }
            });
        }
        
        function initEnvironment() {
            if ((!!window.ActiveXObject || "ActiveXObject" in window)) {
                self.isIEEnvironment(true);
            } else {
                self.isIEEnvironment(false);
            }
        }

        // 显示右下角小popup
        function showPopupTip (content, type, time) {
            <!-- default: message, error, success, warn, message -->
            var notificationPopupHtml = "<div class=\"notification notificationTip\">" +
                "<div class=\"type-icon icon " + type + "\"></div>" +
                "<div class=\"notification-inner\">" +
                    "<p class=\"title\">提示</p>" +
                    "<p class=\"content popupTipContent\">" + content + "</p>" +
                "</div>" +
                "<div class=\"close-btn closePopupTip closeNotificationTip\"></div>" +
            "</div>";
            // 不存在则插入dom
            if (!$('.notificationTip').length) {
                $('body').append(notificationPopupHtml);
                setTimeout(function() {
                    $('.notificationTip').addClass('show');
                }, 0)
            }
            // 自动关闭
            setTimeout(function () {
                removePopupTip();
            }, time || 3000);
            // 监听手动关闭
            $(document).one('click', '.closeNotificationTip',function(){ // 关闭
                removePopupTip();
            });
        }
        // 关闭popupTip
        function removePopupTip() {
            $('.notificationTip').removeClass('show'); // 新隐藏
            setTimeout(function () {
                $('.notificationTip').remove(); // 再删除
            }, 300);
        }

        // 打点方法
        function doTrack () {
            var track_obj = {
                database: 'web_teacher_logs',
                module: 'm_f1Bw7hDbxx'
            };
            for (var i = 0; i < arguments.length; i++) {
                if (i === 0) {
                    track_obj['op'] = arguments[i];
                } else {
                    track_obj['s' + (i - 1)] = arguments[i];
                }
            }
            YQ.voxLogs(track_obj);
        }

        initEnvironment();
        requestCourseDetailInfo(); // 请求课件详情
        bindGlobalEvent(); // 绑定全局监听函数
        OSSUploader(); // 阿里云上传
    };

    ko.applyBindings(new uploadModal(), document.getElementById('uploadContent'));
});