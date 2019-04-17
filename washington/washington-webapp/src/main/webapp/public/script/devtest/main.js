define(function(require, exports, module){
    var fn = require("fn");
    var fn1 = require("fn1");
    var _ = require("underscore");

    fn();

    _.each([1, 2, 3], function(v){
        console.info(v);
    });

    console.info(fn1);

    fn1.test1();

    fn1.test2();
});