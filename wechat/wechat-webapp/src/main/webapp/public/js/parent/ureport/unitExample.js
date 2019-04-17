define(['knockout', '$17', 'jquery', 'examCore_new', 'menu'], function(ko, $17, $) {
  var UnitExample, callback, example;
  window.ko = ko;
  window.download = function() {
    $17.loadingStart();
    $17.tongji('parent-单元报告', '点击下载按钮');
    setTimeout(function() {
      location.href = 'http://wx.17zuoye.com/download/17parentapp?cid=102005';
    }, 200);
  };
  UnitExample = (function() {
    function UnitExample(options) {
      this.title = options.title;
      this.total = "/" + options.total;
      this.current = options.curIndex;
      this.countInfo = ko.computed((function(_this) {
        return function() {
          return _this.current + '<b class="number">' + _this.total + '</b>';
        };
      })(this));
    }

    return UnitExample;

  })();
  example = new UnitExample({
    total: $17.getQuery('total'),
    curIndex: $17.getQuery('curIndex'),
    title: decodeURIComponent($17.getQuery('title'))
  });
  callback = (function(_this) {
    return function(data) {
      var q, qa;
      q = $("#questionContent");
      qa = $("#questionContentAnalysis");
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
        return q.html('暂不支持预览');
      }
    };
  })(this);
  vox.exam.create(callback);
  $('#loading').hide();
  $('#reportExample').show();
  return ko.applyBindings(example, $("#example")[0]);
});
