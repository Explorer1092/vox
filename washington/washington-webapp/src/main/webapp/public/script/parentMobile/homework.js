/* global define : true, PM : true, $:true */
/**
 *  @date 2015/9/16
 *  @auto liluwei
 *  @description 该模块主要负责作业模块 送花逻辑 等等
 */

define(["audio" ], function(){
    var voxSpread = window.YQ && window.YQ.voxSpread;

    if(voxSpread){
        voxSpread({
            boxId : $("#banner-220202"),
            keyId : 220202
        });
    }
});
