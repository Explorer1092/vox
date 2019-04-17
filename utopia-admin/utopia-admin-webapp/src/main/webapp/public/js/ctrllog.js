/**
 * Created by xinqiang.wang on 2017/2/20.
 *
 */
(function () {
    var __getSelectedContents = function () {
        if (window.getSelection) { //chrome,firefox,opera
            //noinspection JSDuplicatedDeclaration
            var range = window.getSelection().getRangeAt(0);
            //noinspection JSDuplicatedDeclaration
            var container = document.createElement('div');
            container.appendChild(range.cloneContents());
            return container.innerText;

        } else if (document.getSelection) { //other
            //noinspection JSDuplicatedDeclaration
            var range = document.getSelection().getRangeAt(0);
            //noinspection JSDuplicatedDeclaration
            var container = document.createElement('div');
            container.appendChild(range.cloneContents());
            return container.innerText;

        } else if (document.selection) { //IE// lao8.org
            return document.selection.createRange().htmlText;
        }
    };


    var __admin_save_ctrl = function (content) {
        var toJSON = typeof JSON === 'object' && JSON.stringify ? JSON.stringify : $.toJSON;
        var msg = {
            module: "admin_ctrl_log",
            userName: _currentUserName_ || $('#admin_name_layout_page').text(),
            content: $.trim(content),
            time: new Date().getTime(),
            url: encodeURIComponent(location.href),
            pathname: encodeURIComponent(location.pathname)

        };
        if(content != ''){
            var url = '//log.17zuoye.cn/log?_c=vox_logs:admin_user_access_log&_l=3&_log=' + encodeURIComponent(toJSON(msg)) + '&_t=' + new Date().getTime();
            $('<img />').attr('src', url).css('display', 'none').appendTo($('body'));
        }
    };

    var copyingTime = 0;
    $(window).bind('keydown', function (event) {
        if (event.ctrlKey || event.metaKey) {
            switch (String.fromCharCode(event.which).toLowerCase()) {
                case 'c':
                    if (copyingTime == 0) {
                        copyingTime = new Date().getTime();
                        __admin_save_ctrl(__getSelectedContents());
                    }
                    var time = new Date().getTime();
                    if (time - copyingTime > 1000) {
                        __admin_save_ctrl(__getSelectedContents());
                        copyingTime = new Date().getTime();
                    }
                    break;
            }
        }
    });
})();
