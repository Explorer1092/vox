/**
 * @author: pengmin.chen
 * @description: "课件大赛-首页"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'knockout', 'YQ', 'voxLogs'], function ($, ko, YQ) {
    var inndexModal = function () {
        var self = this;
        doTrack('o_Php0oMq3SD', YQ.getQuery('referrer'));
        $.extend(self, {
            professorTypeIndex: ko.observable(0), // 专家评审类型tab(英语、语文、数学、信息化)
            professorTypeList: ko.observableArray(['数学专家团队', '语文专家团队', '英语专家团队', '信息化技术教育专家团队']),
            professorList: ko.observableArray([]), // 专家列表
            isShowTopThree: ko.observable(false), // 是否展示榜单
            topThreeTimeRange: ko.observable(''), // 榜单的时间范围
            topThreeCourseList: ko.observable([
                {
                    englishName: 'CHINESE',
                    id: 101,
                    topthreeList: ko.observableArray([])
                },
                {
                    englishName: 'Math',
                    id: 102,
                    topthreeList: ko.observableArray([])
                },
                {
                    englishName: 'ENGLISH',
                    id: 103,
                    topthreeList: ko.observableArray([])
                }
            ]), // 前三名列表

            // 切换首页-专家团队
            switchProfessorType: function (index) {
                doTrack('o_KU93z6DuCT', userInfo.subject, self.professorTypeList()[index]);
                self.professorTypeIndex(index);
                setprofessorList(index);
            },
            // 查看更多
            seeMoreCourse: function () {
                window.location.href = '/courseware/contest/course.vpage';
            },
            // 跳转资源详情
            toDetailPage: function (data) {
                window.location.href = '/courseware/contest/detail.vpage?courseId=' + data.courseId;
            }
        });

        // 设置专家list
        function setprofessorList (index) {
            var allProfessorList = [
                [ // 数学
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_math2.png',
                        name: '梁秋莲',
                        identity: '全国小学数学专业委员会副理事长',
                        description: '原河南省基础教育教学研究室教研员，小学室副主任，特级教师，河南省优秀教师。任中国教育学会小学数学教学专业委员会副理事长，任教育部课程教材研究所兼职研究员，且任人教社教材培训团专家，另担任人教版义务教育数学课程标准实验教材编委，编写和修订教科书和教师教学用书。'
                    },
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_math3.png',
                        name: '姚剑强',
                        identity: '上海市教委教研室小学数学教研员',
                        description: '上海市教委教研室小学数学教研员，中学高级教师，上海市数学特级教师。兼任中国教育学会小学数学教学专业委员会第八届理事会常务理事、中国教育学会数学教育研究发展中心全国数学建模工作委员会副会长、上海市教育学会中小学数学教学专业委员会副秘书长、上海市教师学研究会数学教师专业委员会秘书长、上海师范大学教育学院兼职教授等。长期从事小学数学课程、教学研究和实践工作，具有一定的研究能力和丰富的实践经验。'
                    },
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_math1.png',
                        name: '宋显庆',
                        identity: '江西省教育厅教学教材研究室小学数学教研员',
                        description: "江西省教育厅教学教材研究室小学数学教研员，江西省学科带头人，中学高级教师，骨干教师，江西省教育学会小学数学教学专业委员会常务副主任委员。"
                    }
                ],
                [ // 语文
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_chinese3.png',
                        name: '李亮',
                        identity: '江苏省中小学教研室小学语文教研员',
                        description: '江苏省中小学教研室小学语文教研员， 江苏省教育学会小学语文专业委员会理事长， 苏教版小学语文教材副主编 ，中学高级教师。'
                    },
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_chinese1.png',
                        name: '任海林',
                        identity: '山西省太原市教研科研中心中学语文教研员',
                        description: '山西省太原市教研科研中心语文教研员，高级教师，山西省名教师工程培养对象，山西省学科带头人，山西省教学能手，太原市名教师，太原市优秀青年人才。曾获全国第五届省会城市直辖市中青年教师课堂教学大赛一等奖等荣誉，多次在西安、南京等地执教观摩课。多篇论文、教学设计、课堂实录等收录中文核心期刊。'
                    },
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_chinese2.png',
                        name: '武咏梅',
                        identity: '河南省电教馆研究部主任',
                        description: '河南省电教馆研究部主任，中学高级教师，河南省学术技术带头人，郑州市五一劳动奖章获得者。主要从事教育信息化环境下中小学信息技术与教学融合的应用研究。多次担任河南省教育厅教育信息化大奖赛、教学技能大赛、信息技术与学科融合优质课大赛评委，多次担任全国“一师一优课、一课一名师”活动评委，多次担任中央电化教育馆教育信息化大奖赛专家评委。'
                    }
                ],
                [ // 英语
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_english1_v2.png',
                        name: '何锋',
                        identity: '江苏省中小学教学研究室副书记 英语教研员',
                        description: '江苏省中小学教学研究室副书记，英语教研员，中学正高级教师，国家课标教材（译林版）中小学英语系列教材主编。兼任中国教育学会外语专业委员会副理事长、国家基础教育实验中心外语教育研究中心副主任、江苏省教育学会外语教学专业委员会副理事长、江苏省基础教育课程改革中学英语组组长。扬州大学硕士研究生导师、南京晓庄学院客座教授。'
                    },
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_english3.png',
                        name: '温爱英',
                        identity: '江西省教育厅教学教材研究室英语教研员',
                        description: '江西省教育厅教学教材研究室英语教研员，特级教师，英语学科带头人。国家新课程远程研修专家组成员，教育部中考评价英语学科组专家成员，人民教育出版社特聘教材培训专家，江西师范大学兼职硕士生导师，中国教育学会外语教学专业委员会理事，国家基础教育实验中心外语教育研究中心理事。长期从事基础教育英语教学与测试研究，先后主持完成4个国家级重点课题、2个省级课题；在全国中文核心期刊发表论文30多篇，主编著作4部，参与2部著作的编写；教学成果“中考英语测试与教学相互促进模式的研究与实践”获国家级教学成果奖。'
                    },
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_english2.png',
                        name: '刘珉',
                        identity: '山东省枣庄市教学研究室小学英语教研员',
                        description: '山东省枣庄市教学研究室 小学英语教研员，山东省远程研修核心成员、中学高级教师，山东省教科院兼职研究员和小学英语教研员、聊城大学及枣庄学院客座教授。'
                    }
                ],
                [ // 信息化
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_tech1.png',
                        name: '马涛',
                        identity: '北京市海淀区教育网络与数据中心主任',
                        description: '北京市海淀区教育网络与数据中心主任，北京市信息技术学科教学带头人。教育部基础教育教材审定“普通高中信息技术教育教材审查”专家，中国教育技术协会信息技术教育专业委员会常务理事，北京市教育科学研究院基础教育教学指导委员会——信息技术学科兼职教研员，海淀区中学教师系列高级专业技术职务任职资格评审委员会副主任委员，中央电化教育馆全国电子白板展示课评委、全国中小学电脑制作活动评委，国培计划中西部骨干教师培训主讲教师，中央电化教育馆全国中小学创新课堂教学实践活动模范教研员。'
                    },
                    {
                        avatar: cdnHeader + '/public/skin/teacher_coursewarev2/images/professor_tech2.png',
                        name: '李玉顺',
                        identity: '中国教育发展战略协会信息化专业委员会专家',
                        description: '北京师范大学教育学部副教授，数字学习与教育公共服务教育部工程研究中心 副主任，中国教育发展战略协会信息化专业委员会专家，北京教育学会新媒体教育技术分会会长，北京市中小学数字校园建设工作核心专家，北京数字学校发展战略研究核心专家。'
                    }
                ]
            ];
            self.professorList(allProfessorList[index]);
        }

        // 判断前三名是否展示
        function judgeTopThreeShow() {
            var nowDate = new Date().getTime();
            // nowDate = new Date('11/21/2018 00:00:01').getTime();
            var stepOneShowDate = new Date('11/21/2018 00:00:00').getTime();
            var stepTwoShowDate = new Date('12/21/2018 00:00:00').getTime();
            if (nowDate >= stepOneShowDate) { // 11/21/2018 开始展示榜单
                self.isShowTopThree(true);
                self.topThreeTimeRange('2018.09.25~2018.11.20');
            }
            if (nowDate >= stepTwoShowDate) {
                self.isShowTopThree(true);
                self.topThreeTimeRange('2018.09.25~2018.12.20');
            }
        }

        // 请求前三名的课件
        function requestTopThreeCourseList() {
            var topThreeCourseList = self.topThreeCourseList();
            for (var i = 0; i < topThreeCourseList.length; i++) {
                (function (i) {
                    $.ajax({
                        url: '/courseware/contest/allCourses.vpage',
                        type: 'GET',
                        data: {
                            clazzLevel: 0,
                            subject: topThreeCourseList[i].id,
                            pageNum: 0,
                            pageSize: 3,
                            orderMode: 1, // 排序方式，1为按评分排序，2为按时间排序
                            topThree: true // 请求前三名
                        },
                        success: function (res) {
                            if (res.success) {
                                topThreeCourseList[i].topthreeList(res.data);
                            } else {
                                alertTip(res.info || '请求失败，稍后重试！');
                            }
                        }
                    });
                })(i);
            }
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

        // 设置初始激活tab
        setprofessorList(0);
        requestTopThreeCourseList();
        judgeTopThreeShow();
    };

    ko.applyBindings(new inndexModal(), document.getElementById('indexContent'));
});