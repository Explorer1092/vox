define(["$17", "knockout", "logger", "jp", "komapping", "weuijs"], function ($17, ko, logger) {
    "use strict";
    function offlineLog(logJson) {
        logJson = $.extend(true,{
            app    : "teacher",
            module : "m_2M8WOHc5"
        },logJson);
        logger.log(logJson);
    }

    function OfflineDetail(ohids){
        var self = this;
        self.ohids = ohids;
        self.subject = ko.observable("");
        self.subjectName = ko.observable("");
        self.endDateTime = ko.observable("");
        self.newHomeworkContents = ko.observable(null);
        self.offlineHomeworkContents = ko.observable(null);
        self.focusTab = ko.observable("SIGN");
        self.showStudents = ko.observableArray([]);
        self.signedStudents = [];
        self.unsignedStudents = [];
        self.signedCount = ko.observable(0);
        self.unSignedCount = ko.observable(0);
        self.webLoading = ko.observable(true);
        self.audioList = [];
        self.playIndex = 0;
    }
    OfflineDetail.prototype = {
        constructor : OfflineDetail,
        toolsPreview : function(type,webSiteUrl){
            setTimeout(function(){
                location.href = (webSiteUrl ? webSiteUrl : "//www.17zuoye.com/") + "/view/mobile/parent/learning_tool/teach_list?clazz_level=3&self_study_type=" + type;
            },200);
            offlineLog({
                op : (type == 'WALKMAN_ENGLISH' ? "o_qwgHlIDr" : (type == 'PICLISTEN_ENGLISH' ? "o_l4sAlaFw" : ''))
            });
        },
        changeTab   : function(tab,clickWay){
            var self = this;
            if(tab === self.focusTab()){
                return false;
            }else if(tab === "UNSIGN"){
                self.stopAudioFn();
            }
            var students;
            switch (tab){
                case "SIGN":
                    students = self.signedStudents;
                    break;
                case "UNSIGN":
                    students = self.unsignedStudents;
                    break;
                default:
                    students = [];
            }
            self.focusTab(tab);
            self.showStudents(students);
            clickWay && offlineLog({
                op :  "o_qBbwvPHx"
            });
        },
        stopAudioFn   : function(){
            var self = this;
            try{
                my_jPlayer.jPlayer("destroy");
            }catch (e){
                offlineLog({
                    op : "stopAudio_error",
                    etc:{
                        s0 : e.message
                    }
                });
            }
        },
        playAudio   : function(element,self){
            var that = this; //this -> user object
            var $element = $(element),wt = window.external;
            if($element.hasClass("active")){
                self.stopAudioFn();
                setTimeout(function(){
                    $element.removeClass("active");
                },100);
            }else{
                offlineLog({
                    op     : "o_IyN9PCrW"
                });
                $(".voicePlayer").removeClass("active");
                self.audioList = that.voiceUrl.split("|") || []; //防止以后多条音频
                self.playIndex = 0;
                if(self.audioList.length == 0){
                    //听读模式
                    wt && typeof wt.showToast === 'function' && wt.showToast("该学生没有音频");
                    self.stopAudioFn();
                    return false;
                }
                $element.addClass("active");

                my_jPlayer.jPlayer({
                    ready: function () {
                    },
                    ended: function () {
                        self.playIndex += 1;
                        self.playNextAudio(self.playIndex);
                    },
                    error: function (event) {
                        self.playIndex += 1;
                        self.playNextAudio(self.playIndex);

                        offlineLog({
                            app: "teacher",
                            op: 'video_play_error',
                            s0: event.jPlayer.error.context || '',
                            s1: event.jPlayer.error.message || '',
                            s2: event.jPlayer.error.hint || ''
                        });
                    }
                });

                self.playNextAudio(self.playIndex);
            }
        },
        playNextAudio : function(pIndex){
            var self = this;
            if(pIndex >= self.audioList.length){
                self.playIndex = 0;
                $(".voicePlayer").removeClass("active");
                return false;
            }
            my_jPlayer.jPlayer("setMedia", {
                mp3: self.audioList[pIndex]
            }).jPlayer("play");
        },
        nextStuAudio  : function(url, state, currentTime, duration){
            var self = this;
            if(state === "ended" || state === "error"){
                self.playIndex = self.playIndex + 1;
                self.playNextAudio(self.playIndex);
            }
        },
        setReponseInfo : function(info){
            var self = this;
            $.toast(info,"text");
        },
        init        : function(){
            var self = this;
            $.showLoading();
            $.get("/teacher/offline/homework/detail.vpage",{
                ohids : self.ohids
            },function(data){
                var result = (typeof data === "string") ? $.parseJSON(data) : data;
                $.hideLoading();
                if(result.success){
                    self.subject(result.subject);
                    self.subjectName(result.subjectName);
                    self.endDateTime(result.endTime);
                    self.newHomeworkContents(result.newHomeworkContents || null);
                    self.offlineHomeworkContents(result.offlineHomeworkContents || []);
                    self.signedStudents = result.signedStudents || [];
                    self.signedCount(self.signedStudents.length);
                    self.unsignedStudents = result.unsignedStudents || [];
                    self.unSignedCount(self.unsignedStudents.length);
                    self.focusTab("SIGN");
                    self.showStudents(self.signedStudents);
                    self.webLoading(false);
                }else{
                    self.setReponseInfo(result.info);
                }
            });
            offlineLog({
                op : "o_gnJVNoL5"
            });
        }
    };
    var my_jPlayer = $("#jplayerId"),
        offlineDetail = new OfflineDetail($17.getQuery("ohids")),
        nodeList = document.getElementsByClassName("offlineHomeworkDetail");
    offlineDetail.init();
    for(var t = 0,tLen = nodeList.length; t < tLen; t++){
        ko.applyBindings(offlineDetail,nodeList[t]);
    }

});