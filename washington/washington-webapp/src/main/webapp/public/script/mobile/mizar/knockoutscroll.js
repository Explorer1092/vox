/**
 * @author xinqiang.wang
 * @description "ko 滚动加载数据"
 * @createDate 2016/9/8
 * 使用方法：demo
 * <div data-bind="foreach: list">
 *     <div>
 *         <span data-bind="text: $index()"></span>
 *      </div>
 *</div>
 *<div data-bind="scroll: list().length < 10, scrollOptions: { loadFunc: yourFunc, offset: 10 }">loading</div>
 */

(function (factory) {
    // Module systems magic dance.
    if (typeof require === "function" && typeof exports === "object" && typeof module === "object") {
        // CommonJS or Node: hard-coded dependency on "knockout"
        factory(require("knockout"), exports);
    } else if (typeof define === "function" && define["amd"]) {
        // AMD anonymous module with hard-coded dependency on "knockout"
        define(["knockout", "exports"], factory);
    } else {
        // <script> tag: use the global `ko` object, attaching a `mapping` property
        factory(ko, ko.mapping = {});
    }
}(function (ko) {
    ko.bindingHandlers.scroll = {
        updating: true,
        init: function(element, valueAccessor, allBindingsAccessor) {
            var self = this;
            self.updating = true;
            ko.utils.domNodeDisposal.addDisposeCallback(element, function() {
                $(window).off("scroll.ko.scrollHandler");
                self.updating = false
            });
        },
        update: function(element, valueAccessor, allBindingsAccessor){
            var props = allBindingsAccessor().scrollOptions;
            var offset = props.offset ? props.offset : "0";
            var loadFunc = props.loadFunc;
            var load = ko.utils.unwrapObservable(valueAccessor());
            var self = this;

            if(load){
                element.style.display = "";
                $(window).on("scroll.ko.scrollHandler", function(){
                    if(($(document).height() - offset <= $(window).height() + $(window).scrollTop())){
                        if(self.updating){
                            loadFunc();
                            self.updating = false;
                        }
                    }
                    else{
                        self.updating = true;
                    }
                });
            }
            else{
                element.style.display = "none";
                $(window).off("scroll.ko.scrollHandler");
                self.updating = false
            }
        }
    };

}));



