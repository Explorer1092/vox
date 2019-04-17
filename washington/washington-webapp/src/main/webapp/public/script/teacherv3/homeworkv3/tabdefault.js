(function($17,ko) {
    "use strict";

    var tabDefault = function(){
        var self = this;
        self.tabType       = ko.observable("");
    };
    tabDefault.prototype = {
        constructor       : tabDefault,
        run               : function(){},
        initialise        : function(option){
            var self = this;
            self.tabType = option.tabType || "";
        },
        clearAll          : function(){}
    };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getDefault: function(){
            return new tabDefault();
        }
    });
}($17,ko));