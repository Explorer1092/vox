/**
 *  使用jplayer插件扩展的业务音频播放组件
 */
$(function(){
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
            url && $jPlayerContaner.jPlayer("setMedia", {
                mp3: url
            }).jPlayer("play");
        }
        return playIndex;
    }
    function stopAudio(){
        $jPlayerContaner && $jPlayerContaner.jPlayer("clearMedia");
    }

    $17.audioPlayer = $17.audioPlayer || {};
    $17.extend($17.audioPlayer, {
        playAudio : playAudio,
        stopAudio : stopAudio,
        stopAll   : function(){
            $jPlayerContaner && $jPlayerContaner.jPlayer("destroy");
        }
    });
});