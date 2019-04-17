define(function(require, exports, module){
    var $ = require("jquery");

    console.info($);

    $(".v-change-book").on("click", function(){
        console.info("jq 被处罚");
    });

    exports.test1 = function(){
        console.info("test1");
    };

    exports.test2 = function(){
        console.info("test2");
    };
});