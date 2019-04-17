define(['knockout', '$17', 'jquery', 'examCore_new'], function(ko, $17, $) {
  var Wrong, moreWrong, wqDom, wrong;
  window.ko = ko;
  window.WApp = {};
  WApp.toWrongQuestion = function(index, eid) {
    $17.loadingStart();
    $17.tongji('parent-单元报告', '点击高频错题例题');
    setTimeout((function(_this) {
      return function() {
        location.href = '/parent/homework/report/wrongExercise.vpage?eid=' + wrong.curQuestion.eid + '&total=' + wrong.wrongsTotal + '&curIndex=' + wrong.curQuestion.index + '&title=' + encodeURIComponent(wrong.title) + '&sr=' + encodeURIComponent(wrong.curQuestion.sr) + '&rate=' + wrong.curQuestion.rate;
      };
    })(this), 200);
  };
  moreWrong = $('#moreWrong');
  moreWrong.on('click', function() {
    return wrong.showMoreWrongs();
  });
  wqDom = $('#wrongQuestions');
  Wrong = (function() {
    function Wrong() {

      /*本班情况 */
      this.title = '';
      this.wrongsTotal = 0;
      this.wrongs = [];
      this.showWrongs = [];
      this.hasMoreExercises = ko.observable(false);
      this.curQuestion = null;
    }

    Wrong.prototype.order = function(index) {
      return index + 1;
    };

    Wrong.prototype.setWrongs = function(datas) {
      var callback, d, i, index, len;
      this.wrongsTotal = datas.length;
      if (this.wrongsTotal === 0) {
        $("#wrong").hide();
      }
      this.wrongs = datas;
      for (index = i = 0, len = datas.length; i < len; index = ++i) {
        d = datas[index];
        d.index = index + 1;
      }
      callback = (function(_this) {
        return function(data) {
          if (data.success) {
            return _this.showMoreWrongs();
          }
        };
      })(this);
      return vox.exam.create(callback);
    };

    Wrong.prototype.showMoreWrongs = function(count) {
      var i, item, j, len, len1, q, results, temp;
      if (!isNaN(count)) {
        temp = this.wrongs.splice(0, count);
      } else {
        temp = this.wrongs.splice(0);
      }
      for (i = 0, len = temp.length; i < len; i++) {
        item = temp[i];
        this.showWrongs.push(item);
      }
      this.hasMoreExercises(this.wrongs.length > 0);
      results = [];
      for (j = 0, len1 = temp.length; j < len1; j++) {
        q = temp[j];
        results.push(this.appendQuestion(q));
      }
      return results;
    };


    /* 显示高频错题 */

    Wrong.prototype.showWrongQuestion = function(data, curIndex) {
      return (function(_this) {
        return function() {
          $17.loadingStart();
          $17.tongji('parent-单元报告', '点击高频错题例题');
          setTimeout(function() {
            location.href = '/parent/homework/report/wrongExercise.vpage?eid=' + data.eid + '&total=' + _this.wrongsTotal + '&curIndex=' + curIndex + '&title=' + _this.title;
          }, 200);
        };
      })(this);
    };

    Wrong.prototype.appendQuestion = function(question) {
      var hf, t;
      hf = 'javascript:WApp.toWrongQuestion()';
      this.curQuestion = question;
      t = $('<div class="column"><h2><a href=' + hf + ' class="btn-view">查看</a><span>第' + question.index + '题</span></h2></div><div class="content"></div>');
      wqDom.append(t);
      vox.exam.render(t[1], 'parent_preview', {
        ids: [question.eid],
        getQuestionByIdsUrl: 'parent/homework/loadquestion.vpage',
        app: 'exam_parent_preview'
      });
      if (this.wrongs.length > 0) {
        return moreWrong.show();
      } else {
        return moreWrong.hide();
      }
    };

    return Wrong;

  })();
  wrong = new Wrong;
  window.wrong = wrong;
  return wrong;
});
