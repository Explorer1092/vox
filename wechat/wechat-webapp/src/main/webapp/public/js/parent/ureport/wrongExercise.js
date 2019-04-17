define(['knockout', '$17', 'jquery', 'examCore_new', 'menu'], function(ko, $17, $) {
  var callback, fillData, params, rightFlag, wrongFlag;
  window.ko = ko;
  window.download = function() {
    $17.loadingStart();
    $17.tongji('parent-单元报告', '点击下载按钮');
    setTimeout(function() {
      location.href = 'http://wx.17zuoye.com/download/17parentapp?cid=102005';
    }, 200);
  };
  params = {
    total: $17.getQuery('total'),
    curIndex: $17.getQuery('curIndex'),
    title: decodeURIComponent($17.getQuery('title')),
    rate: $17.getQuery('rate'),
    sr: decodeURIComponent($17.getQuery('sr'))
  };
  rightFlag = $('#rightFlag');
  wrongFlag = $('#wrongFlag');
  fillData = function(data) {
    if (params.sr === "正确") {
      rightFlag.show();
    } else {
      wrongFlag.show();
    }
    $('.subHead').html(data.curIndex + '<b class="number">/' + data.total + '</b>');
    return $('#accuracy').html(data.rate + '%');
  };
  $('#loading').hide();
  $('#reportWrong').show();
  fillData(params);
  callback = function(data) {
    var q, qa;
    q = $('#wrongQuestion');
    qa = $('#wrongQuestionAnalysis');
    if (data.success) {
      vox.exam.render(q[0], 'parent_preview', {
        ids: [$17.getQuery('eid')],
        getQuestionByIdsUrl: 'parent/homework/loadquestion.vpage',
        app: 'exam_parent_preview'
      });
      return vox.exam.render(qa[0], 'parent_preview', {
        ids: [$17.getQuery('eid')],
        getQuestionByIdsUrl: 'parent/homework/loadquestion.vpage',
        app: 'exam_parent_preview',
        showExplain: true
      });
    } else {
      q.html('暂不支持预览');
      return qa.html('暂不支持预览');
    }
  };
  return vox.exam.create(callback);
});
