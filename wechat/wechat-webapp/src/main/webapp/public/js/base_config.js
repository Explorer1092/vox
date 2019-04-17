require.config({
    baseUrl: "/public",
    paths  : {
        "jquery"    : "jquery/dist/jquery.min",
        "ko"        : "knockout/dist/knockout",
        "ko-mapping": "knockout.mapping/knockout.mapping",
        "ko-switch" : "knockout-switch-case/knockout-switch-case.min",
        "ko-amd"    : "knockout-amd-helpers/build/knockout-amd-helpers.min",
        "sammy"     : "sammy/lib/sammy",
        "text"      : "text/text"
    },
    shim   : {
        "jquery": {
            exports: "jquery"
        },
        "sammy" : {
            deps: ["jquery"]
        }
    }
});