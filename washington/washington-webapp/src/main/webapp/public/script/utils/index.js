/**
 *   http://edu.hivoice.cn:9088/WebAudio-1.0-SNAPSHOT/audio/play/dca2d268-2e1d-4b5c-89a0-661e8df6d64b/1536841337734441224/sh1
 *   转变为
 *   http://edush.hivoice.cn:9088/WebAudio-1.0-SNAPSHOT/audio/play/dca2d268-2e1d-4b5c-89a0-661e8df6d64b/1536841337734441224/sh1
 */
$(function(){
    $17.utils = $17.utils || {};
    var urlPostfixs = ["sh","gz","bj"];
    $17.extend($17.utils, {
        hardCodeUrl: function(url){
            if(!url){
                return url;
            }
            var lastIndex = url.lastIndexOf("/");
            if(lastIndex === -1) return url;

            var postfixChar = url.substr(lastIndex + 1,2);
            var postfixCharIndex = urlPostfixs.indexOf(postfixChar);
            if(postfixCharIndex === -1) return url;
            //找到 //edu.的位置
            var eduKeyWord = "//edu.";
            var eduIndex = url.indexOf(eduKeyWord);
            if(eduIndex === -1) return url;

            var firstHalf = url.substring(0,eduIndex);
            var lastHalf = url.substring(eduIndex + eduKeyWord.length);
            return firstHalf + "//edu" + postfixChar + "." + lastHalf;
        }
    });
});