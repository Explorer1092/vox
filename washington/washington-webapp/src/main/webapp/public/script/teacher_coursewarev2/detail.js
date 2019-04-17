/**
 * @author: pengmin.chen
 * @description: "课件大赛-作品详情"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'qrcode', 'voxLogs'], function ($, ko, YQ) {
    // 星级对应文案
    var allEvaluationWordList = ['失误较多，潜力巨大', '略有失误，有待优化', '亮点欠佳，仍可提升', '瑕不掩瑜，上层之作', '非常完美，出类拔萃'];
    // 五颗星标签
    var fiveStarTipList = ['充分体现信息化教学能力', '符合新课标与核心素养', '教学理念与时俱进', '教学设计以学生为中心', '有效培养学生信息化技能', '重视学生实践活动', '教案规范且结构完整', '资源整合能力强', '教学内容与课件高度结合', '课件颜值高且运行可靠'];
    // 非五颗星好标签
    var otherStarGoodTipList = ['充分体现信息化教学能力', '符合新课标与核心素养', '教学理念与时俱进', '教学设计以学生为中心', '有效培养学生信息化技能', '重视学生实践活动', '教案规范且结构完整', '资源整合能力强', '教学内容与课件高度结合', '课件颜值高且运行可靠'];
    // 非五颗星差标签
    var otherStarBadTipList = ['信息化教学能力体现不足', '与新课标与核心素养略有不符', '教学理念稍显陈旧', '以老师为中心，师生互动过少', '未能培养学生信息化技能', '忽视学生实践活动', '教案不够规范，内容有缺失', '资源整合能力较弱', '教学内容与课件结合不足', '课件颜值不高，运行不畅'];
    var gradeList = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级'];
    var termList = ['上', '下'];
    var subjectMap  = {
        'CHINESE': '语文',
        'MATH': '数学',
        'ENGLISH': '英语'
    };

    var mobileCourseUrl = window.location.protocol + '//' + window.location.host + '/view/mobile/teacher/activity2018/coursewarematch/detail?courseId=' + YQ.getQuery('courseId') + '&referrer=pc_share';
    var detailModal = function () {
        var self = this;
        $.extend(self, {
            courseDetailInfo: ko.observable({}), // 课件详情
            ownHasEvaluate: ko.observable(false), // 是否评价过标志
            ownEvaluationInfo: ko.observable({}), // 个人对课件的评价信息
            couldEvaluateInfo: ko.observable({}), // 是否可以评价的信息
            courseEvaluationInfo: ko.observable({}), // 课件的评价信息
            allEvaluationInfo: ko.observable({}), // 全部评价弹窗信息

            // 控制弹窗展示
            isShowCommonAlert: ko.observable(false), // 是否展示通用弹窗（下载成功、评价成功、不满条件等）
            isShowAllEvaluationAlert: ko.observable(false), // 是否展示全部评价弹窗
            isShowEvaluateAlert: ko.observable(false), // 是否展示评价弹窗

            // 通用弹窗
            commonAlertOpt: ko.observable({}), // 通用弹窗配置

            // 评价弹窗
            evaluationAlertType: ko.observable(0), // 评价弹窗类型（0为直接评价，1位下载时的评价）
            evaluationAlertTitle: ko.observable('评价当前作品'), // 评价弹窗的title文案（点击下载如果未评价则调用评价弹窗，title不同）
            evaluationStar: ko.observable(0), // 评价弹窗上评价的星级
            evaluationWord: ko.observable(''), // 评价弹窗上评价文案
            fiveStarEvaluationTipList: ko.observableArray([]), // 评价弹窗上评价标签列表
            otherStarGoodEvaluationTipList: ko.observableArray([]), // 评价弹窗上5颗星评价标签列表
            otherStarBadEvaluationTipList: ko.observableArray([]), // 评价弹窗上小于5颗星评价标签列表

            choiceEvaluationTips: ko.observableArray([]), // 评价弹窗上最终选择的标签
            evaluateErrorTip: ko.observable(''), // 评价弹窗上至多5个提示
            evaluateBtnActive: ko.observable(false), // 评价弹窗按钮是否置灰

            // 全部评价
            allEvaluationList: ko.observableArray([]), // 全部评价

            // 查看所有评价
            seeAllEvaluation: function (type) {
                doTrack('o_dlEZWWqlEG', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
                if (type === 'own') { // 个人全部评价
                    self.allEvaluationList(self.ownEvaluationInfo()._evaluationList);
                } else { // 当前课件全部评价
                    self.allEvaluationList(self.courseEvaluationInfo().labelInfo);
                }
                self.isShowAllEvaluationAlert(true);
            },

            // 立即评价
            evaluateCourse: function () {
                doTrack('o_eNHMCuDpzG', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
                reqeustCouldEvaluate(0);
            },
            // 关闭评价弹窗
            closeEvaluateAlert: function () {
                self.resetEvaluateContent();
                self.isShowEvaluateAlert(false);
            },
            // 复位评价信息
            resetEvaluateContent: function () {
                self.evaluationStar(0); // 清空已评星级
                self.evaluationWord(''); // 清空已评文案
                self.fiveStarEvaluationTipList([]); // 清空5星可评标签列表
                self.otherStarGoodEvaluationTipList([]); // 清空非5星好评列表
                self.otherStarBadEvaluationTipList([]); // 清空非5星差评列表
                self.choiceEvaluationTips([]); // 清空已评标签
            },
            // 点击星级
            choiceStar: function (index) {
                doTrack('o_vzNwDEtNKH', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), index + 1);
                self.resetEvaluateContent();
                self.evaluationStar(index + 1); // 弹窗星级
                self.evaluationWord(allEvaluationWordList[index]); // 弹窗星级文案
                self.choiceEvaluationTips([]); // 清空已选标签

                if (self.evaluationStar() === 5) {
                    self.showFiveStarTipContent();
                } else {
                    self.showOtherStarTipContent();
                }
                self.checkBtnState();
            },

            // 显示五星标签内容
            showFiveStarTipContent: function () {
                var currentEvaluationTipList = [];
                for (var i = 0; i < fiveStarTipList.length; i++) {
                    currentEvaluationTipList.push({
                        isChoice: false,
                        tipText: fiveStarTipList[i]
                    });
                }
                self.fiveStarEvaluationTipList(currentEvaluationTipList);
            },
            // 显示非五星标签内容
            showOtherStarTipContent: function () {
                // 非五星好评
                var currentEvaluationTipListLeft = [];
                for (var m = 0; m < otherStarGoodTipList.length; m++) {
                    currentEvaluationTipListLeft.push({
                        isChoice: false,
                        tipText: otherStarGoodTipList[m]
                    });
                }
                self.otherStarGoodEvaluationTipList(currentEvaluationTipListLeft);

                // 非五星差评
                var currentEvaluationTipListRight = [];
                for (var n = 0; n < otherStarBadTipList.length; n++) {
                    currentEvaluationTipListRight.push({
                        isChoice: false,
                        tipText: otherStarBadTipList[n]
                    });
                }
                self.otherStarBadEvaluationTipList(currentEvaluationTipListRight);
            },
            // 选择5星评价标签
            choiceFiveEvaluationTip: function (data) {
                if (!data.isChoice && self.choiceEvaluationTips().length === 5) { // 已经有5个继续添加
                    self.evaluateErrorTip('最多只能选择5个标签哦~');
                    return;
                }
                self.evaluateErrorTip('');
                doTrack('o_bzvumZ0O6b', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));

                var tmp = clone(data);
                tmp.isChoice = !tmp.isChoice;
                self.fiveStarEvaluationTipList.replace(data, tmp);

                var choiceEvaluationTips = self.choiceEvaluationTips();
                if (tmp.isChoice) {
                    choiceEvaluationTips.push(data.tipText); // 增
                } else {
                    choiceEvaluationTips.splice(choiceEvaluationTips.indexOf(data), 1); // 删
                }

                self.checkBtnState();
            },
            // 选择非5星评价标签
            choiceOtherEvaluationTip: function (index, type, data) {
                var choiceEvaluationTips = self.choiceEvaluationTips();
                if (type ==='good') {
                    var currentEvaluationTips = self.otherStarGoodEvaluationTipList();
                    var oppositeEvaluationTips = self.otherStarBadEvaluationTipList();

                    var currentEvaluationTipObj = self.otherStarGoodEvaluationTipList; // replace 对象时使用
                    var oppositeEvaluationTipObj = self.otherStarBadEvaluationTipList;
                } else {
                    var currentEvaluationTips = self.otherStarBadEvaluationTipList();
                    var oppositeEvaluationTips = self.otherStarGoodEvaluationTipList();

                    var currentEvaluationTipObj = self.otherStarBadEvaluationTipList;
                    var oppositeEvaluationTipObj = self.otherStarGoodEvaluationTipList;
                }

                if (!data.isChoice && choiceEvaluationTips.length === 5) { // 已经有5个继续添加
                    self.evaluateErrorTip('最多只能选择5个标签哦~');
                    return;
                }
                self.evaluateErrorTip('');
                doTrack('o_bzvumZ0O6b', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));

                // 选中当前的
                var tmp1 = clone(data);
                tmp1.isChoice = !tmp1.isChoice;
                currentEvaluationTipObj.replace(data, tmp1);
                if (tmp1.isChoice) {
                    choiceEvaluationTips.push(data.tipText); // 增
                } else {
                    choiceEvaluationTips.splice(choiceEvaluationTips.indexOf(data), 1); // 删
                }

                // 判断对立面的
                if (tmp1.isChoice && oppositeEvaluationTips[index].isChoice) { // 当前的状态为true 且 对立面相同位置已存在选中
                    var tmp2 = clone(oppositeEvaluationTips[index]);
                    tmp2.isChoice = !tmp2.isChoice;
                    oppositeEvaluationTipObj.replace(oppositeEvaluationTips[index], tmp2);
                    choiceEvaluationTips.splice(choiceEvaluationTips.indexOf(oppositeEvaluationTips[index].tipText), 1); // 删
                }

                self.checkBtnState();
            },
            // check 按钮状态
            checkBtnState: function () {
                // 按钮激活状态：0~4颗星 必选选择标签，5颗星可以不选标签
                self.evaluateBtnActive((self.evaluationStar() > 0 && self.evaluationStar() < 5 && self.choiceEvaluationTips().length > 0) || (self.evaluationStar() === 5));
            },
            // 提交评价
            commitEvaluate: function () {
                if (!self.evaluateBtnActive()) return;
                doTrack('o_jMGikdyJzT', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
                requestCommitEvaluate();
            },
            // 下载资源
            downloadCourse: function () {
                doTrack('o_oFuqmtiH9n', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
                requestCouldDownload();
            },
            // 预览资源
            previewCourse: function (data) {
                doTrack('o_gKAJhBuSl2', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));

                if (['zip', 'rar'].indexOf(data.type) > -1) {
                    alertTip('压缩包暂时不支持预览哦~');
                    return ;
                }

                if (['png', 'jpg', 'jpeg'].indexOf(data.type.toLowerCase()) > -1) {
                    var imagePreviewHtml = "<img src=" + data.src + " />";
                    $("#JS-previewBox").html(imagePreviewHtml);
                } else {
                    var previewUrl = 'https://ow365.cn/?' + previewFrontUrl(data.src) + '&furl=' + window.encodeURIComponent(data.src.replace('http://', 'https://'));
                    var filePreviewIframe = "<iframe src='" + previewUrl + "'></iframe>";
                    $("#JS-previewBox").html(filePreviewIframe);
                }
            },
            // 分享
            shareCourse: function () {
                window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
            },
            // 拉票
            canvassCourse: function () {
                if (self.courseDetailInfo().leftTime <= 0) {
                    alertTip('拉票已结束');
                    return;
                }
                window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
            },
            // 投票
            voteCourse: function () {
                if (self.courseDetailInfo().leftTime <= 0) {
                    alertTip('投票已结束');
                    return;
                }
                if (!self.courseDetailInfo().surplus) {
                    return;
                }
                if (!self.courseDetailInfo().totalSurplus) { // 总次数为0
                    if (self.courseDetailInfo().authed) {
                        commonAlertVoteFailAuth();
                    } else {
                        commonAlertVoteFailNotAuth();
                    }
                    return;
                }
                requestVote();
            }
        });

        // 生成二维码
        function getCourseShareQrcode() {
            $('#coureQrcode').qrcode({
                render: "canvas",
                width: 120, // 设置宽度
                height: 120, // 设置高度
                typeNumber: -1, // 计算模式
                correctLevel: 2, // 纠错等级
                text: mobileCourseUrl // 链接
            });
        }

        // 请求课件详情
        function reqeustDetailInfo() {
            $.ajax({
                url: '/courseware/contest/myworks/detail.vpage',
                type: 'GET',
                data: {
                    id: YQ.getQuery('courseId'),
                    preview: true
                },
                success: function (res) {
                    if (res.success) {
                        doTrack('o_2XFSAKwbig', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
                        // 处理previewList中文件名
                        var previewList = [];
                        for(var i = 0; i < res.previewList.length; i++) {
                            // 根据type类型选择fileType，控制前端图标展示
                            var fileType = 'pic';
                            if (['zip', 'rar'].indexOf(res.previewList[i].type.toLowerCase()) > -1) fileType = 'zip';
                            else if (['ppt', 'pptx'].indexOf(res.previewList[i].type.toLowerCase()) > -1) fileType = 'ppt';
                            else if (['doc', 'docx'].indexOf(res.previewList[i].type.toLowerCase()) > -1) fileType = 'word';

                            previewList.push({
                                name: res.previewList[i].name,
                                filterName: filterFileName(res.previewList[i].name),
                                src: res.previewList[i].src,
                                type: res.previewList[i].type,
                                _fileType: fileType
                            });
                        }
                        for (var j = 0; j < res.pictureUrlList.length; j++) {
                            previewList.push({
                                name: res.pictureUrlList[j].name,
                                filterName: filterFileName(res.pictureUrlList[j].name),
                                src: res.pictureUrlList[j].url,
                                type: 'png',
                                _fileType: 'pic'
                            });
                        }
                        var data = $.extend(res, {
                            _formatDate: res.date.substring(0, 10),
                            _descriptionInfo: '小学' + subjectMap[res.subject] + '（' + res.bookName + '）' + gradeList[res.clazzLevel - 1] + termList[res.term - 1] + ' ' + res.unitName + ' ' + res.lessonRealName,
                            _previewList: previewList
                        });
                        self.courseDetailInfo(data);

                        self.previewCourse(self.courseDetailInfo()._previewList[0]); // 默认预览第一个文件
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            })
        }

        // 过滤文件名（控制显示中间15个字加省略号）
        function filterFileName(fileName) {
            var noSuffixFileName = fileName.substring(0, fileName.lastIndexOf('.')); // 文件名（不带后缀）
            var suffix = fileName.substring(fileName.lastIndexOf('.')); // 后缀

            if (noSuffixFileName.length <= 6) return fileName;
            else return (noSuffixFileName.substring(0, 6) + '...' + suffix);
        }

        // 365预览前前缀
        function previewFrontUrl(src) {
            var otherParam = '';
            if (src.indexOf('v.17xueba.com') > -1) {
                otherParam = 'i=16939&ssl=1&n=5';
            } else {
                otherParam = 'i=16940&ssl=1&n=5';
            }
            return otherParam;
        }

        // 查询自己对课件的评价信息
        function requestOwnEvaluationInfo() {
            $.ajax({
                url: '/courseware/evaluation/personageEvaluations.vpage',
                type: 'GET',
                data: {
                    coursewareId: YQ.getQuery('courseId')
                },
                success: function (res) {
                    if (res.success) {
                        self.ownHasEvaluate(res.everEvaluation); // 是否评价过的标志
                        if (self.ownHasEvaluate()) { // 存在评价
                            addOwnEvalationParams(res.data)
                        }
                    } else if (res.errorCode !== '-200') { // 未登录不提示
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 给个人评价信息添加字段
        function addOwnEvalationParams(data) {
            var evaluationList = [];
            for (var i = 0; i < data.commentList.length; i++) {
                evaluationList.push({
                    labelName: data.commentList[i],
                    labelNum: 0
                });
            }
            // 添加一个evaluationList字段（与全部评价保持一致）
            var newData = $.extend(data, {
                _evaluationList: evaluationList
            });
            self.ownEvaluationInfo(newData);
        }

        // 查询该课件全部评价信息
        function requestAllEvaluationInfo() {
            $.ajax({
                url: '/courseware/evaluation/evaluations.vpage',
                type: 'GET',
                data: {
                    coursewareId: YQ.getQuery('courseId')
                },
                success: function (res) {
                    if (res.success) {
                        self.courseEvaluationInfo(res);
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 查询是否可以评价该课件(type表示弹窗来源，0表示来源于直接评价，1表示来源于下载)
        function reqeustCouldEvaluate(type) {
            $.ajax({
                url: '/courseware/evaluation/couldEvaluate.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.success) {
                        self.couldEvaluateInfo(res);
                        if (self.couldEvaluateInfo().couldEvaluate) { // 可以评价
                            if (!type) { // 来源于直接评价
                                self.evaluationAlertType(0);
                                self.evaluationAlertTitle('评价当前作品');
                                self.isShowEvaluateAlert(true);
                                doTrack('o_2j8Wf2N4D4', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '我的评价');
                            } else { // 来源于下载
                                self.evaluationAlertType(1);
                                self.evaluationAlertTitle('请先评价当前作品再下载');
                                self.isShowEvaluateAlert(true);
                                doTrack('o_2j8Wf2N4D4', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '下载资源');
                            }
                        } else { // 评价次数已用完
                            commonAlertEvaluateEmptyTime();
                        }
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 请求提交评价
        function requestCommitEvaluate() {
            var obj = {
                star: self.evaluationStar(),
                keyWord: self.evaluationWord(),
                labelList: self.choiceEvaluationTips(),
                coursewareId: YQ.getQuery('courseId')
            };
            $.ajax({
                url: '/courseware/evaluation/createEvaluations.vpage',
                type: 'POST',
                data: JSON.stringify(obj),
                dataType: 'JSON',
                contentType: 'application/json',
                success: function (res) {
                    if (res.success) {
                        if (!self.evaluationAlertType()) { // 直接评价时，弹窗提示评价成功
                            if (self.couldEvaluateInfo().isAuthentication) {
                                commonAlertEvaluateIsAuthSuccess();
                            } else {
                                commonAlertEvaluateNotAuthSuccess();
                            }
                        } else { // 下载评价时，直接开始下载，并弹窗提示下载成功
                            commonAlertDownloadSuccess()
                        }

                        // 更新用户评价信息
                        self.ownHasEvaluate(true); // 是否评价过的标志
                        addOwnEvalationParams({
                            coursewareId: YQ.getQuery('courseId'),
                            keyWord: obj.keyWord,
                            star: obj.star,
                            commentList: obj.labelList
                        });
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                },
                complete: function () {
                    self.closeEvaluateAlert(false);
                }
            });
        }

        // 请求查看是否可以下载（次数是否超限）
        function requestCouldDownload() {
            commonAlertDownloadSuccess(); // 开发下载

            // 原先逻辑：先评价再下载
            // $.ajax({
            //     url: '/courseware/share/couldDownload.vpage',
            //     type: 'GET',
            //     data: {
            //         coursewareId: YQ.getQuery('courseId')
            //     },
            //     success: function (res) {
            //         if (res.success) {
            //             // 先判断是否有下载次数，在判断是否可以评价，都满足后才可下载
            //             if (!res.couldDownload) { // 无下载次数
            //                 commonAlertDownloadEmptyTime();
            //                 return;
            //             }
            //             if (!self.ownHasEvaluate()) { // 未评价
            //                 reqeustCouldEvaluate(1);
            //                 return;
            //             }
            //             commonAlertDownloadSuccess();
            //         } else {
            //             alertTip(res.info || '请求失败，稍后重试！');
            //         }
            //     },
            //     error: function () {
            //         alertTip('请求失败，稍后重试！');
            //     }
            // });
        }

        // 更新下载次数
        function updateDownloadTime() {
            $.ajax({
                url: '/courseware/share/updateDownloadInfo.vpage',
                type: 'GET',
                data: {
                    coursewareId: YQ.getQuery('courseId'),
                    createTeacherId: self.courseDetailInfo().teacherId
                },
                success: function (res) {
                    if (!res.success) {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                },
                error: function () {
                    alertTip('请求失败，稍后重试！');
                }
            });
        }

        // 投票
        function requestVote() {
            $.ajax({
                url: '/courseware/canvass/vote.vpage',
                type: 'GET',
                data: {
                    courseId: YQ.getQuery('courseId')
                },
                success: function (res) {
                    if (res.success) {
                        var tmp = self.courseDetailInfo();
                        tmp.totalCanvassNum = res.totalSurplus; // 剩余总投票次数
                        tmp.surplus = res.surplus; // 该课件的剩余投票次数
                        tmp.canvassNum = res.canvassNum; // 该课件的投票次数
                        self.courseDetailInfo(tmp);
                        commonAlertVoteSuccess();
                    } else {
                        alertTip(res.info || '请求失败，稍后重试！');
                    }
                }
            });
        }

        // 成功评价-已认证
        function commonAlertEvaluateIsAuthSuccess() {
            showCommonAlert({
                state: 'success', // 状态：success、error
                title1: '您已点评成功！', // 大标题
                title2: '',  // 小标题
                content: '赶快把它分享给身边老师，一起学习品鉴，受益更多孩子吧！<br><br>还有机会上榜“点评达人”，收获丰厚礼物哦！<br><br>分享步骤：点击“去APP分享”按钮---扫描二维码---点击页面底部“用App打开再分享”按钮---分享', // html形式
                left_btn_text: '取消',
                right_btn_text: '去分享',
                left_btn_cb: function () {
                    self.isShowCommonAlert(false);
                },
                right_btn_cb: function () {
                    doTrack('o_wrEBRNnMlH', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                }
            });
        }

        // 成功评价-未认证
        function commonAlertEvaluateNotAuthSuccess() {
            var contentHtml = "<p>尊敬的老师，为喜爱的作品助力，认证后评分占70%权重！还有机会上榜“点评达人”，收获丰厚礼物哦！</p><br><ul style=\"padding-left:50px;\">" +
                "<li style=\"list-style-type:disc;margin-bottom:10px;\">认证条件一: 设置姓名并绑定手机</li>" +
                "<li style=\"list-style-type:disc;margin-bottom:10px;\">认证条件二: 至少8名学生完成3次练习</li>" +
                "<li style=\"list-style-type:disc;\">认证条件三: 至少3名学生绑定手机</li>" +
                "</ul>";
            showCommonAlert({
                state: 'success', // 状态：success、error
                title1: '您已点评成功！', // 大标题
                title2: '',  // 小标题
                content: contentHtml, // html形式
                left_btn_text: '取消',
                right_btn_text: '去认证',
                left_btn_cb: function () {
                    self.isShowCommonAlert(false);
                },
                right_btn_cb: function () {
                    doTrack('o_mddpRJ68tD', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
                    window.open('/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage', '_blank'); // 认证地址
                }
            });
            doTrack('o_UY2CwD9Lo9', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
        }

        // 评价次数已用光
        function commonAlertEvaluateEmptyTime () {
            var contentHtml = "<p>抱歉老师，您今日的评价次数已经使用完毕，认证后解锁无限制评价特权！还有机会上榜“点评达人”，收获丰厚礼物哦！</p><br><ul style=\"padding-left:50px;\">" +
                "<li style=\"list-style-type:disc;margin-bottom:10px;\">认证条件一: 设置姓名并绑定手机</li>" +
                "<li style=\"list-style-type:disc;margin-bottom:10px;\">认证条件二: 至少8名学生完成3次练习</li>" +
                "<li style=\"list-style-type:disc;\">认证条件三: 至少3名学生绑定手机</li>" +
                "</ul>";
            showCommonAlert({
                state: 'error', // 状态：success、error
                title1: '评价次数已用完！', // 大标题
                title2: '',  // 小标题
                content: contentHtml, // html形式
                left_btn_text: '取消',
                right_btn_text: '去认证',
                left_btn_cb: function () {
                    self.isShowCommonAlert(false);
                },
                right_btn_cb: function () {
                    doTrack('o_SGXEHVZVDv', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId')); // 去分享
                    window.open('/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage', '_blank'); // 认证地址
                }
            });
            doTrack('o_UY2CwD9Lo9', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'));
        }

        // 成功下载
        function commonAlertDownloadSuccess() {
            var downloadIframe = "<iframe style='display:none;' src='" + self.courseDetailInfo().zipFileUrl + "'/>";
            $("body").append(downloadIframe);

            var contentHtml = '赶快把它分享给身边老师，一起学习品鉴，受益更多孩子吧！<br><br>还有机会上榜“点评达人”，收获丰厚礼物哦！<br><br>分享步骤：点击“去APP分享”按钮---扫描二维码---点击页面底部“用App打开再分享”按钮---分享';
            showCommonAlert({
                state: 'success', // 状态：success、error
                title1: '您已下载成功！', // 大标题
                title2: '',  // 小标题
                content: contentHtml, // html形式
                left_btn_text: '取消',
                right_btn_text: '去分享',
                left_btn_cb: function () {
                    self.isShowCommonAlert(false);
                },
                right_btn_cb: function () {
                    doTrack('o_TsUMgDEMTj', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId')); // 去分享
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                }
            });
            doTrack('o_gZiM3S8Znn', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId')); // 去分享

            updateDownloadTime(); // 更新下载次数
        }

        // 下载次数已用光
        function commonAlertDownloadEmptyTime() {
            showCommonAlert({
                state: 'error', // 状态：success、error
                title1: '下载特权已用光！', // 大标题
                title2: '',  // 小标题
                content: '抱歉老师，您的下载特权已经使用完毕，分享作品让更多老师、学生受益！<br><br>还有机会上榜“点评达人”，收获丰厚礼物哦！分享一次，可解锁5次下载权！<br><br>分享步骤：点击“去APP分享”按钮---扫描二维码---点击页面底部“用App打开再分享”按钮---分享', // html形式
                left_btn_text: '取消',
                right_btn_text: '去分享',
                left_btn_cb: function () {
                    self.isShowCommonAlert(false);
                },
                right_btn_cb: function () {
                    doTrack('o_ZHfZ6vT9QS', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId')); // 去分享
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                }
            });
            doTrack('o_RpXjVjnQ0d', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId')); // 去分享
        }

        // 投票成功
        function commonAlertVoteSuccess() {
            doTrack('o_E9xeSh0Wwu', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '投票汇聚页');
            showCommonAlert({
                state: 'vote-success', // 状态
                title1: '投票成功', // 大标题
                title2: '',  // 小标题
                content: '您已成功投票！点击“为TA拉票”，让更多老师为您喜爱的作品投票！', // html形式
                left_btn_text: '为TA拉票',
                right_btn_text: '为其他作品投票',
                left_btn_cb: function () {
                    doTrack('o_SNLcLSyDIp', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '作品详情页');
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                },
                right_btn_cb: function () {
                    doTrack('o_Gxk2guy1EL', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '作品详情页');
                    self.isShowCommonAlert(false);
                    window.location.href = '/courseware/contest/vote.vpage';
                }
            });
        }

        // 投票失败-已认证
        function commonAlertVoteFailAuth() {
            doTrack('o_c0eKJ0mPOS', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '作品详情页');
            showCommonAlert({
                state: 'vote-share', // 状态
                title1: '投票失败', // 大标题
                title2: '',  // 小标题
                content: '您今天的10次投票机会已用尽了哦～<br>点击“为Ta拉票“，让更多老师为您喜爱的作品投票吧！', // html形式
                left_btn_text: '为TA拉票',
                right_btn_text: '查看投票结果',
                left_btn_cb: function () {
                    doTrack('o_SNLcLSyDIp', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '作品详情页');
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                },
                right_btn_cb: function () {
                    doTrack('o_mSulHYlkov', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '投票汇聚页');
                    self.isShowCommonAlert(false);
                    window.location.href = '/courseware/contest/vote.vpage';
                }
            });
        }

        // 投票失败-未认证
        function commonAlertVoteFailNotAuth() {
            doTrack('o_c0eKJ0mPOS', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '作品详情页');
            showCommonAlert({
                state: 'vote-fail', // 状态
                title1: '投票失败', // 大标题
                title2: '',  // 小标题
                content: '您今天的5次投票机会已用尽了哦～<br>点击“去认证”，解锁每天10次投票机会！', // html形式
                left_btn_text: '为TA拉票',
                right_btn_text: '去认证',
                left_btn_cb: function () {
                    doTrack('o_SNLcLSyDIp', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '作品详情页');
                    window.open('/project/share/index.vpage?wxtip=true&link=' + window.encodeURIComponent(mobileCourseUrl)); // 跳转通用二维码分享页
                },
                right_btn_cb: function () {
                    doTrack('o_EyNJ7kmyZA', subjectMap[self.courseDetailInfo().subject], YQ.getQuery('courseId'), '投票汇聚页');
                    window.open('/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage', '_blank'); // 认证地址
                }
            });
        }

        // 显用通用弹窗（不满足条件、次数超限、评价成功、下载成功等）
        function showCommonAlert(option) {
            // 弹窗配置
            var default_opt = {
                state: 'success', // 状态：success、error
                title1: '', // 大标题
                title2: '',  // 小标题
                content: '内容', // html形式
                left_btn_text: '取消',
                right_btn_text: '确定',
                left_btn_cb: function () {
                    self.isShowCommonAlert(false);
                },
                right_btn_cb: function () {
                    self.isShowCommonAlert(false);
                }
            };
            var opt = $.extend(default_opt, option);

            self.isShowCommonAlert(true);
            self.commonAlertOpt(opt);
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

        // 克隆对象
        function clone (Obj) {
            var buf;
            if (Obj instanceof Array) {
                var i = Obj.length;
                while (i--) {
                    buf[i] = clone(Obj[i]);
                }
                return buf;
            } else if (Obj instanceof Object) {
                buf = {};
                for (var k in Obj) {
                    buf[k] = clone(Obj[k]);
                }
                return buf;
            } else {
                return Obj;
            }
        }

        reqeustDetailInfo(); // 查询课件详情
        requestOwnEvaluationInfo(); // 查询自己对课件的评价信息
        requestAllEvaluationInfo(); // 查询该课件所有的评价信息
        getCourseShareQrcode(); // 获取课件分享二维码
    };

    ko.applyBindings(new detailModal(), document.getElementById('detailContent'));
});