/* global define : true, $:true, PM : true */

/**
 *  @date 2016-06-21 10:35:51
 *  @auto liluwei
 *  @description 该模块主要负责选择孩子模块
 */

"use strict";

define(['ajax'], function(promise){

	var noop = $.noop;

	return function(append_dom_selector, select_fn, done_fn){
		promise(
			"/parentMobile/ucenter/getKids.vpage"
		)
		.done(function(res){

			var selectClassName = "select";

			var $ul = $('<ul>', {
                    // 预防iosClick
                    html : res.users.reduce(function(prev, current){
                        // jscs:disable maximumLineLength
                        return prev += '<li data-sid="'+ current.student_id +'">' +
                            '<img src="'+ (current.img_url || PM.default_user_image) +'">' +
                            '<i class="icon"></i><p class="name textOverflow">'+ current.real_name +'</p>' +
                            '</li>';
                        // jscs:enable maximumLineLength
                    }, '')
                })
                .on('click', 'li', function(){
                    var $self = $(this);

                    if($self.hasClass(selectClassName)){
                        return ;
                    }

                    var sid = $self.closest('ul')
                        .find('.' + selectClassName).removeClass(selectClassName)
                        .end()
                        .end()
                        .addClass(selectClassName)
                        .data('sid');

                    (select_fn || noop).apply(this, [sid].concat($.makeArray(arguments)));

                })
                .appendTo(
                    $('<div class="mc-childList">').insertBefore(append_dom_selector)
                );

			(done_fn || noop)($ul, res);

		});
	};

});
