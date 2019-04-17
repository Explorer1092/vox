/*错题解析*/
define(['jquery', 'knockout', 'examCore_new'], function ($, ko) {
    var ids = [], examViewBox = $('#examViewBox');
    var wrongList = examViewBox.data('wrong_list');
    examViewBox.html('<div style="padding:50px 0; text-align:center">数据加载中...</div>');

    //兼容
    if (wrongList.length > 0 && wrongList[0].qid) {
        for (var i = 0; i < wrongList.length; i++) {
            ids.push(wrongList[i].qid);
        }
    } else {
        ids = wrongList.split(',');
    }

    if (ids.length == 0) {
        examViewBox.html('暂不支持预览');
        return false;
    }
    // FIX manually set the global ko property
    window.ko = ko;

    //初始化
    vox.exam.create(function (data) {
        if (data.success) {
            var node = document.getElementById('examViewBox');
            var obj = vox.exam.render(node, 'parent_history', {
                ids: ids,
                getQuestionByIdsUrl: 'parent/homework/loadquestion.vpage',
                getCompleteUrl: completeUrl,
                app: 'weixin',
                env: examEnv

            });

            //加载解析
            var parseNode = document.getElementById('parseBox');
            var parseObj = vox.exam.render(parseNode, 'parent_preview', {
                ids: ids,
                getQuestionByIdsUrl: 'parent/homework/loadquestion.vpage',
                app: 'weixin',
                showExplain: true,
                env: examEnv
            });

            $(document).on('click', '#previous', function () {
                obj.previous();
                parseObj.previous();
            });

            $(document).on('click', '#next', function () {
                obj.next();
                parseObj.next();
            });
        } else {
            examViewBox.html('暂不支持预览');
        }
    }, false, {env: examEnv});

});