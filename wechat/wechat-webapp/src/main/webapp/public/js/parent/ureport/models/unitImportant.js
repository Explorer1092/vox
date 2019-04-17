define(['knockout', '$17', 'examCore_new'], function(ko, $17) {

  /*单元重点 */
  var Important, important;
  Important = (function() {
    function Important() {
      this.title = '';
      this.importantTotal = 0;
      this.importants = [];
      this.showImportants = ko.observableArray([]);
      this.hasMoreImportant = ko.observable(false);
    }

    Important.prototype.order = function(index) {
      return index + 1;
    };

    Important.prototype.setImportants = function(datas) {
      this.importantTotal = datas.length;
      this.importants = datas;
      return this.showMoreImportants(3);
    };

    Important.prototype.showMoreImportants = function(count) {
      var i, item, len, temp;
      if (!isNaN(count)) {
        temp = this.importants.splice(0, 3);
      } else {
        temp = this.importants.splice(0);
      }
      for (i = 0, len = temp.length; i < len; i++) {
        item = temp[i];
        this.showImportants.push(item);
      }
      return this.hasMoreImportant(this.importants.length > 0);
    };


    /*显示单元重点例题 */

    Important.prototype.showCurUnitQuestion = function(data, curIndex) {
      return (function(_this) {
        return function() {
          $17.loadingStart();
          $17.tongji('parent-单元报告', '点击单元重点例题');
          setTimeout(function() {
            location.href = '/parent/homework/report/example.vpage?eid=' + data.eid + '&total=' + _this.importantTotal + '&curIndex=' + curIndex + '&title=' + encodeURIComponent(_this.title);
          }, 200);
        };
      })(this);
    };

    return Important;

  })();
  important = new Important;
  return important;
});
