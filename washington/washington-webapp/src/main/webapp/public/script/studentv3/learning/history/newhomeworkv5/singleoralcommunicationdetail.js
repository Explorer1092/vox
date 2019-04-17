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
            $.get("/student/learning/report/personaloralcommunicationdetail.vpage",{
                homeworkId : self.homeworkId,
                stoneId : self.stoneId
            }).done(function(res){
                if(res.success){
                    self.content(res.content || null);
                    self.loading(false);
                }else{
                    self.content(null);
                    self.loading(true);
                }
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
        }
    };


    var viewModel = new OralItem({
        homeworkId : $17.getQuery("hid"),
        stoneId : $17.getQuery("stoneId")
    });

    ko.applyBindings(viewModel,document.getElementById("tabContentHolder"));

});