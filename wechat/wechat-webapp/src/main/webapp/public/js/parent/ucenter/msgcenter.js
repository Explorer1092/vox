/*
 * 消息中心
 */
define(["jquery"], function ($) {
    $(document).on("click", ".js-msgClick", function () {
        var urlStr, self = this;
        var urlBase = $(self).attr("data-href");
        if (urlBase.indexOf('?') != -1) {
            urlStr = urlBase + "&_from=msgcenter";
        } else {
            urlStr = urlBase + "?_from=msgcenter";
        }
        setTimeout(function () {
            location.href = urlStr;
        }, 200);
    });
});