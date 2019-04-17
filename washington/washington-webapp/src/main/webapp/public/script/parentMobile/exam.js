/* global define : true, PM : true, $:true, vox : true, ko : true */
/**
 *  @date 2015/11/20
 *  @auto liluwei  特别鸣谢 hailong.yang的辅助
 *  @description 该模块主要负责根据作业id 来展示问题
 *  TODO  该模块目前只负责 查看
 *  TODO  将来可以将examCore 抽出成一个模块
 */
define([], function(){

    'use strict';

    var doPagingAction = {
    };

    window.onunload =  function(){
        // 因为distory 实时修改，所以这里没有携程 纯函数  window.onunload = doPagingAction.destory;
        doPagingAction.distory();
    };

    (function(){
        var callRenderer = function(method){
            (
                $.isFunction(doPagingAction[method]) ? doPagingAction[method] : $.noop
            )();
            doPagingAction.checkHasOtherQuestion();
        };

        $.extend(
            doPagingAction,
            {
                _next :function(){
                    callRenderer("next");
                },
                _previous : function(){
                    callRenderer("previous");
                }
            }
        );

        $(document)
            .on("click", ".doGetPreviousQuestion", doPagingAction._previous)
            .on("click", ".doGetNextQuestion", doPagingAction._next);

    })();

    var render = function(dom){
        var $dom = $(dom);

        if($dom.length === 0){
            vox.log( "找不到填充" );
            return ;
        }

        dom = $dom[0];
		var dataInfo = $dom.data(),
			ids = (dataInfo.eids || '').split(","),
            completeUrl = dataInfo.complete_url || '';

		var create_render = function(dom, type, opts){
            return vox.exam.render(dom, type, $.extend({}, {
				getCompleteUrl : completeUrl,
                ids : ids
            }, opts));
		};

		debugger;
        vox.exam.create(function(res){
            if(!res.success){
                return ;
            }

			var my_answer_render = create_render(dom, "parent_history"),
				right_answer_render = create_render($('.doRenderRightAnswer')[0], "parent_preview", {
					showExplain: true
				});

            var my_answer_render_paper = my_answer_render.paper,
				right_answer_render_paper = right_answer_render.paper,
				common_action = function(action){
					try {
						my_answer_render_paper[action]();
						right_answer_render_paper[action]();
					} catch (e) {
					}

                    document.body.scrollTop = 0;
				};

            $.extend(
                doPagingAction,
                {
                    hasPrevious : function(){
                        return my_answer_render_paper.currentIndex() > 0;
                    },
                    hasNext : function(){
                        return my_answer_render_paper.currentIndex() < my_answer_render_paper.children.length - 1;
                    },
                    checkHasOtherQuestion : function(){
                        $(".doGetNextQuestion")[doPagingAction.hasNext()?"show": "hide"]();
                        $(".doGetPreviousQuestion")[doPagingAction.hasPrevious()?"show": "hide"]();
                    },
                    previous : common_action.bind(null, "previous"),
                    next : common_action.bind(null, "next"),
                    distory : common_action.bind(null, 'dispose')
                }
            );

        }, false, {
            domain : '',
            env : vox.exam.env,
            clientType: "mobile",
            clientName: "17ParentApp",
            audioPlayerType: "external"
        })

    };

    return render;


});

