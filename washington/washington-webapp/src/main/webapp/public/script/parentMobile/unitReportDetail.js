/**
 /* global define : true, PM : true, $:true, vox : true */
/**
 *  @date 2015/11/20
 *  @auto liluwei
 *  @description 该模块主要负责作业模块 送花逻辑 等等
 */
define(['exam', 'redo'], function(render){

    'use strict';

    $(function(){
        // 渲染高频错题
        $(".doRenderQuestionByIds").each(function(index, dom){
            render(dom);
        });
        //是否带http
        var build_url = function(url){
            if(url.indexOf('://')>-1){
                return url;
            }

            return [location.protocol, '', location.host, url].join('/');
        };

        $.iosOnClick(
            ".do-goto-game",
            function() {
                var $self = $(this),
                dataset = $self.data();

                PM.doExternal("openFairylandPage",JSON.stringify({
                    name: "fairyland_app:" + dataset.appkey,
                    url: build_url(dataset.launchurl),
                    useNewCore: dataset.browser,
                    orientation: dataset.orientation,
                    initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                }));
            }
        );


    });

});

