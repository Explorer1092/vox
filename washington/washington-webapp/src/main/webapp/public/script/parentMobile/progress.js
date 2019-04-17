/* global jQuery : true*/

/**
 * @description : 这个主要用于作业进度 目前仅仅支持 圆形的进度条 设计
 * @author : luwei.li
 * Created by Administrator on 2015/12/28.
 */
(function($){

	var defaultOpts = {
		radius      : 72,    // 圆环半径
		lineWidth   : 10,  // 圆环边的宽度
		strokeStyle : '#e5e9ee', //边的颜色
		lineCap     : 'round',
		x : 77,
		y : 77,
		startAngle : 0,
		counterclockwise : false  //False = 顺时针，true = 逆时针。默认false
	};

	var WIN = window,
		PI = WIN.Math.PI,
        CHARTSTARTANGLE = -1/2 * PI;  //以12点为起点

	var drawArc = function(ctx, opts){
			ctx.beginPath();
			ctx.arc(opts.x, opts.y, opts.radius, opts.startAngle, opts.endAngle, opts.counterclockwise );
			ctx.lineWidth = opts.lineWidth;
			ctx.strokeStyle = opts.strokeStyle;
			ctx.lineCap = opts.lineCap;
			ctx.stroke();
			ctx.closePath();
		},
		parentRadianByPercent = function(percent, all){
			return percent === 0 ? 0 : 2 * PI / ( (all || 100) / percent);
		};

	$.fn.progress = function(opts){

		return this.each(function(){
			var $self = $(this);

			if(
				$self.length === 0
				||
				$self[0].tagName.toUpperCase() !== "CANVAS"
			){
				return ;
			}

			var _opts = opts || $self.data() || {};

			var canvas = $self[0],
				ctx = canvas.getContext('2d'),
				baseOpts = $.extend({}, defaultOpts, _opts);

			canvas.width = $self.width();
			canvas.height = $self.height();

			// draw 大圆
			var outerRingOpts = $.extend({}, baseOpts, {
				endAngle : PI * 2,
				strokeStyle : baseOpts.outer_stroke_style
			});
			drawArc(ctx, outerRingOpts);

			// draw 小圆
			var innerRingOpts = $.extend({}, baseOpts, {
				startAngle : CHARTSTARTANGLE,
				endAngle :  parentRadianByPercent(baseOpts.percent, baseOpts.all) + CHARTSTARTANGLE,
				strokeStyle : baseOpts.inner_stroke_style || "#0fcfe4"
			});
			drawArc(ctx, innerRingOpts);

		});

	};

})(jQuery);
