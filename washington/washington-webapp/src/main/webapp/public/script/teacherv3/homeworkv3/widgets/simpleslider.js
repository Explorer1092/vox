/**
 * @fileoverview
 * @author cm 2016.6.30
 * @Depend jQuery
 * @demo
 *  var S = new SimpleSlider({
        slideName    : "slider",
        clickLeftId  : "#swipingLeft",
        clickRightId : "#swipingRight",
        slideItem    : ".slideItem",
        slideCount   : 3,
        totalCount   : 0,
        clickSlideItemFun : function(){
            //todo
        }
 *  });
 *
 *  @Return simpleSliderFun
 *
 **/

var SimpleSlider = (function(){
    var sliders = {};

    var _bind = function(name){

        $(sliders[name].clickRightId).on("click",function(){
            var slideName = $(this).parents(".sliderHolder").attr("name") || "slider";
            var totalCount = sliders[slideName].totalCount,moving=0;

            $(sliders[slideName].clickLeftId).show();
            if(totalCount < sliders[slideName].slideCount || totalCount <= sliders[slideName].currentPosition+sliders[slideName].slideCount){
                //console.log("end");
                return;
            }else{
                if(totalCount > sliders[slideName].currentPosition+sliders[slideName].slideCount){
                    sliders[slideName].currentPosition += sliders[slideName].slideCount;
                }else{
                    sliders[slideName].currentPosition = totalCount;

                }
                moving = sliders[slideName].itemWidth * sliders[slideName].currentPosition;
                $(sliders[slideName].slideContainer + " ul").css("left","-" + moving + "px");
            }
            if(totalCount <= sliders[slideName].currentPosition+sliders[slideName].slideCount){
                $(sliders[slideName].clickRightId).hide();
            }

            $($(sliders[slideName].slideItem)[sliders[slideName].currentPosition]).trigger("click");
        });

        $(sliders[name].clickLeftId).on("click",function(){
            var slideName = $(this).parents(".sliderHolder").attr("name") || "slider";
            var moving=0;

            $(sliders[slideName].clickRightId).show();
            if(sliders[slideName].currentPosition==0){
                //console.log("begin");
                return;
            }else{
                if(sliders[slideName].currentPosition-sliders[slideName].slideCount >= 0){
                    sliders[slideName].currentPosition -= sliders[slideName].slideCount;
                }else{
                    sliders[slideName].currentPosition = 0;
                }
                moving = sliders[slideName].itemWidth * sliders[slideName].currentPosition;;
                $(sliders[slideName].slideContainer + " ul").css("left","-" + moving + "px");
            }
            if(sliders[slideName].currentPosition == 0){
                $(sliders[slideName].clickLeftId).hide();
            }

            $($(sliders[slideName].slideItem)[sliders[slideName].currentPosition]).trigger("click");
        });

        //todo click之后通过callback去绑定点击事件
        $(sliders[name].slideItem).on("click",sliders[name].clickSlideItemFun);
    };

    var simpleSliderFun = function(){
        var defaults = {
            slideName      : "slider",
            clickLeftId    : "#swipingLeft",
            clickRightId   : "#swipingRight",
            slideContainer : "#slideContainer",
            slideItem      : ".slideItem",
            itemWidth      : "206",
            currentPosition: 0,
            slideCount     : 1,
            totalCount      : 0,
            clickSlideItemFun : function(){
                //todo sth
            }
        };
        var options = arguments[0];
        var slideName = options.slideName || defaults.slideName;

        if(sliders.hasOwnProperty(slideName)){
            this.destory(slideName);
        }
        sliders[slideName] = $.extend(true,defaults, options);
        this.init(slideName);
    }

    simpleSliderFun.prototype = {
        constructor : simpleSliderFun,

        init : function(name) {

            //todo css样式及属性添加，规范slider结构

            _bind(name);
        },

        destory : function(name){

            $(sliders[name].clickLeftId).hide().unbind("click");
            $(sliders[name].clickRightId).show().unbind("click");
            //$(sliders[name].slideItem).unbind("click");
        }
    };
    return simpleSliderFun;

})();