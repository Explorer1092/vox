define(['knockout', '$17', 'examCore_new'], function(ko, $17) {
  var Prepare, prepare;
  Prepare = (function() {
    function Prepare() {

      /*预习安排 */
      this.title = '';
      this.prepareTotal = 0;
      this.prepares = [];
      this.showPrepares = ko.observableArray([]);
      this.hasMorePrepare = ko.observable(false);
    }

    Prepare.prototype.order = function(index) {
      return index + 1;
    };

    Prepare.prototype.setPrepares = function(datas) {
      this.prepareTotal = datas.length;
      this.prepares = datas;
      return this.showMorePrepares(3);
    };

    Prepare.prototype.showMorePrepares = function(count) {
      var i, item, len, temp;
      if (!isNaN(count)) {
        temp = this.prepares.splice(0, count);
      } else {
        temp = this.prepares.splice(0);
      }
      for (i = 0, len = temp.length; i < len; i++) {
        item = temp[i];
        this.showPrepares.push(item);
      }
      return this.hasMorePrepare(this.prepares.length > 0);
    };


    /*显示预习安排例题 */

    Prepare.prototype.showNextUnitQuestion = function(data, curIndex) {
      return (function(_this) {
        return function() {
          $17.loadingStart();
          $17.tongji('parent-单元报告', '点击预习安排例题');
          setTimeout(function() {
            location.href = '/parent/homework/report/example.vpage?eid=' + data.eid + '&total=' + _this.prepareTotal + '&curIndex=' + curIndex + '&title=' + encodeURIComponent(_this.title);
          }, 200);
        };
      })(this);
    };

    return Prepare;

  })();
  prepare = new Prepare;
  return prepare;
});
