/* global define : true, $:true */
/**
 *  @date 2015/9/17
 *  @auto liluwei
 *  @description 该模块主要负责送花逻辑
 */

define(['ajax', 'jqPopup'], function( ioPromise){

    'use strict';

    $(function(){

        var win = window;

        // 点击送花逻辑
        $.iosOnClick(
            '.doSendflower',
            function(){

                var $self = $(this);

                if(+$self.data("send_flower_not_finised")){
                    return $.alert("完成作业，才能送花");
                }

                var sendUrl = '/parentMobile/flower/sendflower.vpage',
                    sendData = {};

                $.each(
                    $self.data(),
                    function(key, value){

                        key = key.match(/send_flower_(\w+)/);

                        if(key){
                            sendData[key[1]] = value;
                        }
                    }
                );

                ioPromise( sendUrl, sendData, 'POST', {seq : true})
                .done(function(sendCallbackData){
                    if(sendCallbackData.success === false){
                        console.error('送花失败 %j ', sendCallbackData);
                        return ;
                    }

                    $self.removeClass('.doSendflower').text('已成功送花');
                    win.location.reload(true);
                });

            }
        );

    });

});
