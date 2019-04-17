$(function(){
    var selfPlayer = (function(){
        var $jPlayerContaner;
        function initJplayerElement(){
            $jPlayerContaner = $("#jquery_jplayer_1");
            if($jPlayerContaner.length === 0){
                $jPlayerContaner = $("<div></div>").attr("id","jquery_jplayer_1");
                $jPlayerContaner.appendTo("body");
            }
        }
        function playAudio(audioList,callback){
            if(!audioList){
                $17.alert('音频数据为空');
                return false;
            }
            if(typeof audioList === "string") audioList = audioList.split(",");
            if(!$.isArray(audioList) || audioList.length === 0){
                $17.alert('音频数据为空');
                return false;
            }
            initJplayerElement();
            var playIndex = 0;
            $jPlayerContaner.jPlayer("destroy");
            setTimeout(function(){
                $jPlayerContaner.jPlayer({
                    ready: function (event) {
                        playNextAudio(playIndex,audioList,callback);
                    },
                    error : function(event){
                        playIndex++;
                        playIndex = playNextAudio(playIndex,audioList,callback);
                    },
                    ended : function(event){
                        playIndex++;
                        playIndex = playNextAudio(playIndex,audioList,callback);
                    },
                    volume: 0.8,
                    solution: "html,flash",
                    swfPath: "/public/plugin/jPlayer",
                    supplied: "mp3"
                });
            },200);
        }
        function playNextAudio(playIndex,audioArr,callback){
            if(playIndex >= audioArr.length){
                $jPlayerContaner.jPlayer("destroy");
                $.isFunction(callback) && callback();
            }else{
                var url = audioArr[playIndex];
                url = $17.utils.hardCodeUrl(url);
                url && $jPlayerContaner.jPlayer("setMedia", {
                    mp3: url
                }).jPlayer("play");
            }
            return playIndex;
        }
        function stopAudio(){
            $jPlayerContaner && $jPlayerContaner.jPlayer("clearMedia");
        }
        return {
            playAudio : playAudio,
            stopAudio : stopAudio,
            stopAll   : function(){
                $jPlayerContaner && $jPlayerContaner.jPlayer("destroy");
            }
        };
    }());

    function OralItem(config){
        var self = this;
        self.homeworkId = config.homeworkId || "";
        self.studentId = config.studentId || "";
        self.stoneId = config.stoneId || "";
        self.loading = ko.observable(true);
        self.content = ko.observable(null);
        self.playingStoneId = ko.observable(null);
        self.init();
    }
    OralItem.prototype = {
        constructor : OralItem,
        init : function(){
            var self = this;

            self.loading(true);
            $.get("/teacher/new/homework/report/personaloralcommunicationdetail.vpage",{
                homeworkId : self.homeworkId,
                stoneId : self.stoneId,
                studentId : self.studentId
            }).done(function(res){
                if(res.success){
                    self.content(res.content || null);
                    self.loading(false);
                }else{
                    self.content(null);
                    self.loading(true);
                }
                $17.voxLog({
                    module : "m_Odd245xH",
                    op     : "report_OC_studentdetail_load",
                    s0     : self.homeworkId,
                    s1     : self.stoneId,
                    s2     : self.studentId
                });
            });
        },
        playAudio : function(self){
            var record = this;
            var playingStoneIdFn = self.playingStoneId;
            if(playingStoneIdFn() !== record.uuid){
                playingStoneIdFn(record.uuid);
                selfPlayer.playAudio(record.studentAudio,function(){
                    playingStoneIdFn(null);
                });
            }else{
                selfPlayer.stopAudio();
                playingStoneIdFn(null);
            }

            $17.voxLog({
                module : "m_Odd245xH",
                op  : "report_OC_studentdetail_record_click",
                s0     : self.homeworkId,
                s1     : self.stoneId,
                s2     : self.studentId,
                s3     : record.score
            });
        }
    };


    var viewModel = new OralItem({
        homeworkId : $17.getQuery("hid"),
        studentId : $17.getQuery("studentId"),
        stoneId : $17.getQuery("stoneId")
    });

    ko.applyBindings(viewModel,document.getElementById("tabContentHolder"));

});