/* global $ : true */
/**
 *  创建短链接
 *  @auth : luwei.li
 *  @date 2015/12/23
 */

define(['ajax'], function(ioPromise){
    var CREATE_SHORT_HREF = "/v1/parent/creatparentshorturl.vpage";

    var createShortHref = function(url){
        var dfd = $.Deferred();

        ioPromise(
            CREATE_SHORT_HREF,
            {
                url : url
            }
        )
        .done(function(res){
            if(res.success === false){
                return dfd.reject(res.message);
            }

            dfd.resolve(res.shortUrl);
        })
        .fail(dfd.reject);

        return dfd;
    };

    return createShortHref;
});

