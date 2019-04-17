/**
 *
 */
(function($){
    $.fn.slick = function(opts){
        var defaults  = {
            prevArrow : '', //左按钮ID
            nextArrow : '', //右按钮ID
            showCardNum : 3,//要显示的卡片数
            cardWidth : 0, //单张卡片的宽度
            totalPage : 1, //总页数
            disableClass: '' //按钮disable
        };
        var settings = $.extend({},defaults,opts);

        var slick = {}, obj = this;
        slick.init = function(){
            settings.prevBtn = settings.prevArrow;
            settings.nextBtn = settings.nextArrow;
            settings.liLength = obj.find('li').length;

            //根据单张卡片的宽度和卡片的总数计算出容器的宽度
            obj.css({width : settings.liLength * settings.cardWidth +'px'});

            //计算总页数
            settings.totalPage = Math.ceil(settings.liLength/settings.showCardNum);

            //判断nextBtn是否可点击
            if(settings.totalPage <= 1){
                $("#"+settings.nextBtn).addClass(settings.disableClass).attr('index',0);
            }else{
                $("#"+settings.nextBtn).attr('index', 1);
            }

            //切换按钮赋值
            $("#"+settings.prevBtn).attr('index',0);

            slick.bindEvents();

        };

        slick.bindEvents = function(){
            $("#"+settings.prevBtn).bind('click',function(){
                var $this = $(this);
                if($this.hasClass(settings.disableClass)){return false}
                var cPage = $this.attr('index')* 1 - 1;
                $this.attr('index',cPage);
                var that = $("#"+settings.nextBtn);
                that.attr('index',that.attr('index')*1-1);
                slick.updateBigTabClass(cPage+1);
            });

            $("#"+settings.nextBtn).bind('click',function(){
                var $this = $(this);
                if($this.hasClass(settings.disableClass)){return false}
                var cPage = $this.attr('index')* 1 + 1;
                $this.attr('index',cPage);
                var that = $("#"+settings.prevBtn);
                that.attr('index',that.attr('index')*1+1);
                slick.updateBigTabClass(cPage);
            });
        };

        slick.updateBigTabClass = function(currentPage){
            var prevArrow = $('#'+settings.prevArrow);
            var nextArrow = $('#'+settings.nextArrow);
            if(settings.totalPage == currentPage){
                nextArrow.addClass(settings.disableClass);
                prevArrow.removeClass(settings.disableClass);
            }else if(currentPage > 1){
                prevArrow.removeClass(settings.disableClass);
                nextArrow.removeClass(settings.disableClass);
            }else if(currentPage - 1 == 0){
                prevArrow.addClass(settings.disableClass);
                nextArrow.removeClass(settings.disableClass);
            }else if(currentPage - 1 < settings.totalPage){
                nextArrow.removeClass(settings.disableClass);
            }
            obj.css({left : -settings.cardWidth*settings.showCardNum*(currentPage-1) +"px"});
        };
        slick.init();
    }
})(jQuery);