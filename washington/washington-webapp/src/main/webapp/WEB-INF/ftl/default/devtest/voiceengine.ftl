<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <@sugar.capsule js=["jquery", "VoxExternalPlugin"] />
</head>
<body>

<div id="log" style="width:100%; height: 200px; border:1px solid black; overflow: scroll;">
</div>

<script>
    function pad_0n(n) {
        if(n < 10) return '0' + n;
        return n;
    }
    function now() {
        var date = new Date();
        var d = date.getDate();
        var m = date.getMonth() + 1; //Months are zero based
        var y = date.getFullYear();
        var hour = date.getHours();
        var min = date.getMinutes();
        var sec = date.getSeconds();
        return y + "-" + pad_0n(m) + "-" + pad_0n(d) + ' '
                + pad_0n(hour) + ':' + pad_0n(min) + ':' + pad_0n(sec);
    }
    function log(s) {
        var $log = $('#log');
        $('<div>').text(now() + ' ' + s).appendTo($log);
        $log.scrollTop($log[0].scrollHeight);
    }
    $(function(){
        log('init ok');

        if( ! VoxExternalPluginExists() )
            log('WARNING! NO VoxExternalPlugin');
        else
            log('VoxExternalPluginVersion: ' + window.external.getVoxExternalPluginVersion());
    });



    // 这个对象是模拟一个flash对象，提供了必要的回调接口
    var fakeFlashObject = {

        chivoxOnRecordIdGot : function(recordId) {
            this.lastRecordId = recordId;
            log('chivoxOnRecordIdGot ' + recordId);
        },

        chivoxOnRecordStarted : function() {
            log('chivoxOnRecordStarted ');
        },

        chivoxOnRecordStopped : function(recordLength) {
            log('chivoxOnRecordStopped ' + recordLength);
        },

        //注意！这里的result是一个json字符串，需要json解码之后才能变成对象
        chivoxOnResult : function(result) {
            log('chivoxOnResult :' + result);
        },

        chivoxOnCoreRequestExceptionTimeout : function() {
            log('chivoxOnCoreRequestExceptionTimeout');
        },

        chivoxOnReplayStopped : function() {
            log('chivoxOnReplayStopped');
        },



        // FOR DEBUG & TEST PURPOSE ONLY!!!!!!!!!!
        debugInit : function () {
            log('init');
            window.voxVoiceEngine.init('139640305600024f', 'e50f6116f37e0624a46801c0e25624ad', 'http://10.0.1.130/v2.0/latencydetect');
        },

        debugRecord : function () {
            log('record: hello');
            var param = {
                rank: 5,
                coreType: 'en.sent.score',
                res: 'eng.snt.g4',
                userId: 'test_user',
                applicationId: '139640305600024f',
                refText: 'hello',
                sentenceId: 'my_sentence_id',
                playDing: false
            };
            window.voxVoiceEngine.startRecord(2000, param);
        },

        debugReplay : function() {
            log('debugReplay: ' + this.lastRecordId);
            var param = {
                recordId: this.lastRecordId
            };
            window.voxVoiceEngine.startReplay(param);
        },

        __dummy: null
    };

    window.voxVoiceEngine = new VoxExternalPluginChivox(fakeFlashObject, window.external);

</script>

<div>
    <button onclick="fakeFlashObject.debugInit()">Init</button>
    <button onclick="fakeFlashObject.debugRecord()">Record</button>
    <button onclick="fakeFlashObject.debugReplay()">Replay</button>
</div>
</body>
