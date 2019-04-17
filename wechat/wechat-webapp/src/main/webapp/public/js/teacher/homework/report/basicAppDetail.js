/**
 * @author xinqiang.wang
 * @description "基础练习详情"
 * @createDate 2016/7/22
 */

define(["$17", "knockout", "komapping", "logger", "jbox", 'jp'], function ($17, ko, komapping, logger, jbox, jp) {
    var categoryId = 0;
    var BasicAppHomeworkDetailModel = function () {
        var self = this;
        self.basicAppDetail = ko.observableArray([]);
        self.homeworkId = $17.getQuery("homeworkId");
        self.categoryId = $17.getQuery("categoryId");
        self.lessonId = $17.getQuery("lessonId");
        self.showStudentsListBox = ko.observable(false);
        self.studentsList = ko.observableArray([]);
        self._getBasicAppDetail = function () {
            $17.weuiLoadingShow();
            $.post('/teacher/homework/report/detailsbaseapp.vpage', {
                homeworkId: self.homeworkId,
                categoryId: self.categoryId,
                lessonId: self.lessonId
            }, function (data) {
                if (data.success) {
                    self.basicAppDetail(data);
                    categoryId = data.categoryId || 0;
                } else {

                }
                $17.weuiLoadingHide();
            }).fail(function () {
                $17.weuiLoadingHide();
            });
        }();

        /*计算百分比*/
        self.errorOrRightPercent = function (count, totalCount) {
            var percent = 0;
            if (totalCount > 0) {
                percent = Math.floor(count / totalCount * 100)
            }
            return '(' + percent + '%)';
        };

        self.showStudentsBtn = function (er) {
            var that = this;
            if (er == 'error') {
                self.studentsList(that.answerErrorInfo);
            } else {
                self.studentsList(that.answerRightInfo);
            }
            if (self.studentsList().length > 0) {
                self.showStudentsListBox(true);
            }
        };
    };

    /*音频播放*/
    var my_jPlayer = $("#jplayerId");
    var isPlaying = false;
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
                $(".playAudioBtn").removeClass('stop');
            }
        },
        ended: function () {
            if(isPlaying){
                isPlaying = false;
                $(".playAudioBtn").removeClass('stop');
            }
        },
        error: function (event) {
            isPlaying = false;
            $(".playAudioBtn").removeClass('stop');
            logger.log({
                app: "teacher",
                module: 'WECHAT_Newhomework_passport_' + $17.getQuery("subject") || LoggerProxy.subject,
                op: 'video_play_error',
                s0: event.jPlayer.error.context || '',
                s1: event.jPlayer.error.message || '',
                s2: event.jPlayer.error.hint || ''
            });
        }
    });

    //音频播放
    $(document).on('click', '.playAudioBtn', function () {
        var $this = $(this);
        var mp3Url = $this.data("audio_src");
        if($17.isBlank(mp3Url)){
            $17.jqmHintBox("暂无音频");
            return false;
        }

        $(".playAudioBtn").removeClass('stop');
        $this.toggleClass("stop");
        if ($this.hasClass("stop")) {
            my_jPlayer.jPlayer("setMedia", {
                mp3: mp3Url
            }).jPlayer("play");
        } else {
            my_jPlayer.jPlayer("stop");
        }
        logger.log({
            app: "teacher",
            module: 'm_pGqNIEG2',
            s0: $17.getQuery("subject") || LoggerProxy.subject,
            op: 'BasicPractice_type_details_repeat_play_click',
            s2: $17.getQuery("homeworkId"),
            s3: "BASIC_APP",
            s4: categoryId
        });
    });

    ko.applyBindings(new BasicAppHomeworkDetailModel());
});
