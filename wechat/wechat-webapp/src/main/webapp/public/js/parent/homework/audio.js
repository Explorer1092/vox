define(['jquery','logger'], function ($,logger) {

    var globalAudio = new window.Audio();

    //音频结束
    globalAudio.onended = function () {
        var queue = globalAudio.queue,
            currentPlayIndex = globalAudio.currentPlayIndex;
        if ($.isArray(queue) && currentPlayIndex < queue.length - 1) {
            globalAudio.currentPlayIndex++;
            globalAudio.src = queue[globalAudio.currentPlayIndex];

            playAudio();
        } else {
            stopAudio();
        }

    };

    //音频加载失败处理
    globalAudio.onerror = function(){
        globalAudio.currentPlayIndex++;
        globalAudio.src = queue[globalAudio.currentPlayIndex];
    };

    //当媒介长度改变时
    globalAudio.ondurationchange = function(){

    };

    //当发生故障并且文件突然不可用时
    globalAudio.onemptied = function(){
        //alert("当发生故障并且文件突然不可用时");
    };

    //在浏览器不论何种原因未能取回媒介数据时
    globalAudio.onstalled = function(){
        //console.info("在浏览器不论何种原因未能取回媒介数据时");
    };

    //当文件就绪可以开始播放时运行的脚本（缓冲已足够开始时）
    globalAudio.oncanplay = function(){
        //console.info("当文件就绪可以开始播放时运行的脚本（缓冲已足够开始时）");

    };

    var playAudio = function (pladyDom) {
            if (pladyDom) {
            var $playDom = $(pladyDom),
                src = $playDom.data("audio_src"),
                srcArray = src.split('|'),
                firstSrc = src;

            if (srcArray.length > 1) {
                globalAudio.queue = srcArray;
                globalAudio.currentPlayIndex = 0;
                firstSrc = srcArray[0];
            }

            globalAudio.src = firstSrc;
            globalAudio.currentDom = $(pladyDom)[0];
        }

        globalAudio.play();

        var playDom = globalAudio.currentDom;

        $(playDom).addClass("icon-n-stop");
    },


    pauseAudio = function () {
        globalAudio.pause();

        var playDom = globalAudio.currentDom;
        $(playDom).removeClass("icon-n-stop");
    },
    stopAudio = function () {

        var playDom = globalAudio.currentDom;
        $(playDom).removeClass("icon-n-stop");

        globalAudio.queue = globalAudio.currentDom = globalAudio.currentPlayIndex = null;

    };


    $('.playAudioBtn').on('click', function () {
        var self = this;

        if (globalAudio.currentDom === self) {
            globalAudio.paused ? playAudio() : pauseAudio();
            return;
        }

        stopAudio();

        playAudio(self);
        logger.log({
            "module":"homework",
            "op":"homework_click_listen_audio"
        });
    });
});

