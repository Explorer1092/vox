/**
 * @author xinqiang.wang
 * @description "录音推荐"
 * @createDate 2016/8/15
 */

define(["$17", "knockout", "komapping", "logger", "jbox", 'jp'], function ($17, ko, komapping, logger, jbox) {
    var VoiceRecommendModel = function () {

        var self = this;
        self.homeworkId = $17.getQuery("homeworkId");
        self.clazzName = ko.observable('');
        self.voiceExpired = ko.observable(false);
        self.categoryVoiceList = ko.observableArray([]);
        self.selectedCategoryVoiceList = ko.observableArray([]);
        self.showStep1 = ko.observable(true);
        self.showStep2 = ko.observable(false);

        self.selectedStudentsCount = ko.observable(0);
        self.recommendComment = ko.observable(); //评语内容

        /*获取列表*/
        self.getVoiceList = function () {
            $.post("/teacher/homework/report/voicelist.vpage", {homeworkId: self.homeworkId}, function (data) {
                if (data.success) {
                    self.clazzName(data.clazzName);
                    self.voiceExpired(data.voiceExpired);

                    for (var i in data.categoryVoiceMap) {
                        var id = i.split('|')[0];
                        var name = i.split('|')[1];

                        for (var j = 0; j < data.categoryVoiceMap[i].length; j++) {
                            data.categoryVoiceMap[i][j].checked = false;
                            data.categoryVoiceMap[i][j].isPlay = false;
                        }
                        var categoryMap = {
                            id: id,
                            name: name,
                            checked: false,
                            count: 0,
                            studentList: data.categoryVoiceMap[i]

                        };
                        self.categoryVoiceList.push(ko.mapping.fromJS(categoryMap));
                    }

                    //set first selected
                    ko.utils.arrayForEach(self.categoryVoiceList(), function (_category, index) {
                        if (index == 0) {
                            _category.checked(true);
                            self.categoryBtn(_category);
                        }
                    });
                }
            });
        };

        /*初始化*/
        self.getVoiceList();

        /*选择分类*/
        self.categoryBtn = function (that) {
            ko.utils.arrayForEach(self.categoryVoiceList(), function (_category) {
                _category.checked(false);
            });
            that.checked(true);
        };

        /*是否推荐*/
        self.isRecommendBtn = function (parent) {
            var that = this;
            that.checked(!that.checked());
            var count = 0;
            ko.utils.arrayForEach(parent.studentList(), function (_stu) {
                if (_stu.checked()) {
                    count += 1;
                }
            });
            if (count > 5) {
                that.checked(false);
                $17.jqmHintBox("最多可选5个");
                return false;
            }

            self.selectedStudentsCount(count);
            parent.count(count);
        };

        self.gotoStep2Btn = function () {
            self.showStep1(false);
            self.showStep2(true);
        };

        /*去推荐*/
        self.saveVoiceCommendBnt = function () {
            var recommendVoiceList = [];
            ko.utils.arrayForEach(self.categoryVoiceList(),function(_cate){
                ko.utils.arrayForEach(_cate.studentList(),function(_stu){
                    if(_stu.checked()){
                        recommendVoiceList.push({
                            studentId : _stu.studentId(),
                            studentName: _stu.studentName(),
                            categoryName : _cate.name(),
                            voiceList: _stu.voiceList()
                        });
                    }
                });
            });

            $.post("/teacher/homework/report/voicerecommend.vpage", {
                homeworkId: self.homeworkId || null,
                recommendComment: $17.isBlank(self.recommendComment()) ? "这些同学读得很不错！" : self.recommendComment(),
                recommendVoiceList: JSON.stringify(recommendVoiceList)
            }, function (data) {
                if (data.success) {
                    $17.alert("推荐成功",function(){
                        location.href = '/teacher/homework/report/detail.vpage?homeworkId='+self.homeworkId;
                    });
                }else{
                    $17.alert(data.info);
                }
            });

        };

        /*音频播放*/
        var my_jPlayer = $("#jplayerId");
        var isPlaying = false;
        var playErrorCount = 0;
        self.setStopAll = function () {
            ko.utils.arrayForEach(self.categoryVoiceList(), function (_list) {
                ko.utils.arrayForEach(_list.studentList(), function (_detail) {
                    _detail.isPlay(false);
                });
            });
            my_jPlayer.jPlayer("destroy");
        };

        self.voicePlay = function (urls, urlIndex) {
            playErrorCount = 0;
            my_jPlayer.jPlayer({
                ready: function () {
                },
                playing : function(event){
                    isPlaying = true;
                },
                timeupdate : function(event){
                    //fix 音频文件头中没有content-Length属性，造成不调用ended结束方法
                    if(isPlaying && event.jPlayer.status.duration == 0 && event.jPlayer.status.currentTime == 0){
                        isPlaying = false;
                        urlIndex += 1;
                        if (urls.length == urlIndex) {
                            self.setStopAll();
                            return false;
                        }
                        my_jPlayer.jPlayer("setMedia", {
                            mp3: urls[urlIndex]
                        }).jPlayer("play");
                    }
                },
                ended: function () {
                    if(isPlaying){
                        isPlaying = false;
                        urlIndex += 1;
                        if (urls.length == urlIndex) {
                            self.setStopAll();
                            return false;
                        }
                        my_jPlayer.jPlayer("setMedia", {
                            mp3: urls[urlIndex]
                        }).jPlayer("play");
                    }
                },
                error: function (event) {
                    isPlaying = false;
                    urlIndex += 1;
                    playErrorCount += 1;
                    if (urls.length == urlIndex) {
                        var startStr = playErrorCount < urls.length ? "部分音频" : "";
                        playErrorCount > 0 && $17.jqmHintBox(startStr + "播放失败");
                        return false;
                    }
                    my_jPlayer.jPlayer("setMedia", {
                        mp3: urls[urlIndex]
                    }).jPlayer("play");

                    logger.log({
                        app: "teacher",
                        module: 'WECHAT_Newhomework_voice_recommend',
                        op: 'video_play_error',
                        s0: event.jPlayer.error.context || '',
                        s1: event.jPlayer.error.message || '',
                        s2: event.jPlayer.error.hint || ''
                    });

                }
            });

            my_jPlayer.jPlayer("setMedia", {
                mp3: urls[urlIndex]
            }).jPlayer("play");
        };

        self.voiceStop = function () {
            my_jPlayer.jPlayer("stop");
        };


        self.voicePlayOrStopBtn = function (parent) {
            var that = this;
            if (self.voiceExpired()) {
                $17.jqmHintBox("录音已过期");
                return false;
            }
            var voiceList = that.voiceList();
            my_jPlayer.jPlayer("destroy");
            if (that.isPlay()) {
                that.isPlay(false);
                self.voiceStop();
                return false;
            }

            ko.utils.arrayForEach(self.categoryVoiceList(),function(_cate){
                ko.utils.arrayForEach(_cate.studentList(),function(_stu){
                    _stu.isPlay(false);
                });
            });
            that.isPlay(true);
            self.voicePlay(voiceList, 0);
        };
    };

    ko.applyBindings(new VoiceRecommendModel());
});

