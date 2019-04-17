(function($){
//调起应用
    var jxt_default_opts = {
        student: {
            download_url: 'https://wx.17zuoye.com/download/17studentapp',
            client_url: 'a17zuoye://platform.open.api:/student/primary/main/',
            ios_client_url: 'a17zuoye://student_main',
            sj_download: 'http://a.app.qq.com/o/simple.jsp?pkgname=com.A17zuoye.mobile.homework',
            yq_type: 'tab',
            ad_type: 'news_detail',
            yq_url_pre: 'j/jzt'
        },
        parent: {
            download_url: 'https://wx.17zuoye.com/download/17parentapp',
            client_url: 'a17parent://platform.open.api:/parent_main',
            ios_client_url: 'a17parent://parent_main',
            sj_download: 'http://a.app.qq.com/o/simple.jsp?pkgname=com.yiqizuoye.jzt',
            yq_type: 'tab',
            ad_type: 'news_detail',
            yq_url_pre: 'j/jzt'
        }
    };

    var ua = navigator.userAgent.toLowerCase(),
        from_wechat_or_qq_ua_reg = /(micromessenger|qq)\//.test(ua),
        get_webview_url = function get_webview_url(url) {
            return encodeURIComponent(url === 'location' ? location.href : url);
        },
        get_android_client_url = function get_android_client_url(dataset) {
            return dataset.ad_val ? dataset.client_url + '?from=h5&type=' + dataset.ad_type + '&url=' + get_webview_url(dataset.ad_val) : dataset.client_url;
        },
        get_ios_client_url = function get_ios_client_url(dataset) {
            return dataset.ios_client_url + '?yq_from=h5&yq_type=' + dataset.yq_type + '&yq_val=' + get_webview_url(dataset.yq_val);
        },
        ios_universal_links = function ios_universal_links(dataset) {
            return 'https://wechat.17zuoye.com/' + dataset.yq_url_pre + '?yq_from=h5&yq_type=' + dataset.yq_type + '&yq_val=' + get_webview_url(dataset.yq_val);
        };

    /**
     * @param dataset {Object}
     *	@param dataset.client_url  {String} 客户端通用协议 eg:a17parent://platform.open.api:/parent_main   为了在非微信qq环境下 我们能不依赖应用宝
     *	@param dataset.download_url {String} 通用下载地址 页面 eg:https://wx.17zuoye.com/download/17parentapp?cid=100323
     *	@param dataset.yq_type {String} ios 通用协议 type
     *	@param dataset.yq_val  {String} ios 通用协议 url
     *	@param dataset.ad_type {String} 安卓应用宝通用协议type
     *	@param dataset.ad_val  {String} 安卓应用宝通用协议链接
     */
    var do_open_app = function do_open_app(items) {
        if(isBlank(items.type)){
            console.info('type=' + items.type);
            return false;
        }

        var dataset = jxt_default_opts[items.type];

        //安卓和ios的接口还不一致，真坑
        $.extend(dataset, items);
        // dataset.ad_type = items.ad_type || dataset.ad_type;
        // dataset.ad_val = items.ad_val || dataset.ad_val;

        // dataset.yq_type = items.yq_type || dataset.yq_type;
        // dataset.yq_val = items.yq_val || dataset.yq_val;

        if(!isBlank(items.download_url)){
            dataset.download_url = items.download_url;
        }

        if (ua.search('android') > -1) {
            dataset.client_url = get_android_client_url(dataset);

            if (from_wechat_or_qq_ua_reg) {
                var android_scheme = dataset.ad_val ? '&android_scheme=' + encodeURIComponent(dataset.client_url) : '';

                dataset.client_url = '' + dataset.sj_download + android_scheme;
            }
        } else {
            var ios_version = /ip(ad|hone|od)/.test(ua) && ua.match(/os (\d+)_(\d+)/);

            if (ios_version) {
                if (from_wechat_or_qq_ua_reg) {
                    dataset.client_url = +ios_version[1] > 8 ? ios_universal_links(dataset) : dataset.sj_download;
                } else {
                    dataset.client_url = get_ios_client_url(dataset);
                }
            }
        }

        setTimeout(function () {
            location.href = dataset.download_url;
        }, 1800);

        location.href = dataset.client_url;
    };

    function isBlank(str){
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    $(document).on('click', '.do_open_client', function () {
        do_open_app($(this).data());
    });
}($));