define(["$17", "knockout", "komapping", 'logger'], function ($17, ko, komapping, logger) {
    "use strict";
    function ScoreRule(){
        var self = this;
        self.homeworkType = ko.observable($17.getQuery("homeworkType"));
    }
    ko.applyBindings(new ScoreRule());
});