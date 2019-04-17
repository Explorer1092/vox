// Generated by CoffeeScript 1.9.2
(function() {
  var extend = function(child, parent) { for (var key in parent) { if (hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    hasProp = {}.hasOwnProperty;

  define(function(require, exports) {
    var Events, ViewModel;
    Events = require("Events");
    ViewModel = (function(superClass) {
      extend(ViewModel, superClass);

      function ViewModel() {}

      ViewModel.prototype.bookInfo = ko.observable({});

      ViewModel.prototype.focusUnit = ko.observable(0);

      ViewModel.prototype._focusIndex = 0;

      ViewModel.prototype.changeBook = function() {
        console.info("这里要些还课本逻辑");
      };

      ViewModel.prototype.changeUnit = function(index, self) {
        self.focusUnit(this.unitId());
        self._focusIndex = index;
        ViewModel.emit("ko.event.changeUnit", [this.unitId]);
      };

      ViewModel.prototype.hasContent = function(index, self) {
        var ab;
        ab = self.bookInfo().unitList()[index].abacus;
        return ab.englishBasic() + ab.mathBasic() + ab.special() + ab.reading() + ab.exam();
      };

      ViewModel.prototype.init = function(bookInfo) {
        var i, index, len, ref, unit;
        ref = bookInfo.unitList || [];
        for (index = i = 0, len = ref.length; i < len; index = ++i) {
          unit = ref[index];
          unit.abacus = {
            englishBasic: 0,
            englishBasicTime: 0,
            mathBasic: 0,
            mathBasicTime: 0,
            special: 0,
            specialTime: 0,
            reading: 0,
            readingTime: 0,
            exam: 0,
            examTime: 0
          };
          unit.isOpen = false;
          if (unit.defaultUnit) {
            this._focusIndex = index;
            this.focusUnit(unit.unitId);
            ViewModel.emit("ko.event.changeUnit", [unit.unitId]);
          }
        }
        this.bookInfo(ko.mapping.fromJS(bookInfo));
      };

      ViewModel.prototype.bookFilter = function(base, clazzIds) {
        var bookInfos, clazz, i, len, maxTime, updateTimes;
        updateTimes = [];
        bookInfos = [];
        for (i = 0, len = base.length; i < len; i++) {
          clazz = base[i];
          if (_.indexOf(clazzIds, clazz.id) >= 0) {
            updateTimes.push(clazz.updateTime);
            bookInfos.push(clazz.bookJson);
          }
        }
        if (updateTimes.length === 0) {
          return {};
        }
        maxTime = Math.max.apply(null, updateTimes);
        return JSON.parse(bookInfos[_.indexOf(updateTimes, maxTime)]);
      };

      return ViewModel;

    })(Events);
    return ViewModel;
  });

}).call(this);

//# sourceMappingURL=BookAndUnit.js.map
