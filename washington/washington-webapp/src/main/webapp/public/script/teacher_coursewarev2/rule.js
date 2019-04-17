/**
 * @author: pengmin.chen
 * @description: "课件大赛-规则"
 * @createdDate: 2018/10/10
 * @lastModifyDate: 2018/10/10
 */

define(['jquery', 'YQ', 'voxLogs'], function ($, YQ) {
    doTrack('o_Q5GbxGsxK4');

    // 打点方法
    function doTrack () {
        var track_obj = {
            database: 'web_teacher_logs',
            module: 'm_f1Bw7hDbxx'
        };
        for (var i = 0; i < arguments.length; i++) {
            if (i === 0) {
                track_obj['op'] = arguments[i];
            } else {
                track_obj['s' + (i - 1)] = arguments[i];
            }
        }
        YQ.voxLogs(track_obj);
    }
});