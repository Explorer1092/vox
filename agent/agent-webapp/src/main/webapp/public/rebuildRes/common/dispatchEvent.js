//事件分发，依赖jquery
(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery'], factory);
    } else if (typeof exports === 'object') {
        // CommonJS
        module.exports = factory(require('jquery'));
    } else {
        // root or window
        root.getVerifyCodeModal = factory(root.jQuery);
    }
}(this, function ($) {
    function dispatchEvent (options){
        var _this = this;
        _this.templateData = options;

        var defaultOptions = {
            /*base 基础事件，包含选择器,事件类型,回调
            base:[
                {
                    selector:"#eventBtn",
                    eventType:"click",
                    callBack:function(){
                        console.log('I am clicked');
                    }
                }
            ]*/
        };

        _this.options = $.extend(defaultOptions, options);

        var baseEventArray = _this.options.base;
        $.each(baseEventArray,function(i,item){
            if(item.selector){
                $(document).on(item.eventType,item.selector,item.callBack);
            }
        });
    }

    return dispatchEvent;
}));
