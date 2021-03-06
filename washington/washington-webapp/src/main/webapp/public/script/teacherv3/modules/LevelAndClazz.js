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

      ViewModel.prototype.checkStat = function(self) {
        var clazz, count, i, len, ref;
        count = 0;
        ref = self.data()[self.focusLevel() - 1];
        for (i = 0, len = ref.length; i < len; i++) {
          clazz = ref[i];
          if (clazz.isChecked()) {
            count++;
          }
        }
        return count === self.data()[self.focusLevel() - 1].length;
      };

      ViewModel.prototype.focusLevel = ko.observable(0);

      ViewModel.prototype.groupSupported = ko.observable(false);

      ViewModel.prototype.data = ko.observableArray([]);

      ViewModel.prototype.groupData = ko.observableArray([]);

      ViewModel.prototype.showType = [ko.observable("clazz"), ko.observable("clazz"), ko.observable("clazz"), ko.observable("clazz"), ko.observable("clazz"), ko.observable("clazz")];

      ViewModel.prototype.selectAll = [ko.observable(true), ko.observable(true), ko.observable(true), ko.observable(true), ko.observable(true), ko.observable(true)];

      ViewModel.prototype.groupSelectAll = [ko.observable(false), ko.observable(false), ko.observable(false), ko.observable(false), ko.observable(false), ko.observable(false)];

      ViewModel.prototype._getClazzIds = function() {
        var clazz, clazzIds, i, len, ref;
        clazzIds = [];
        ref = this.data()[this.focusLevel() - 1];
        for (i = 0, len = ref.length; i < len; i++) {
          clazz = ref[i];
          if (clazz.isChecked()) {
            clazzIds.push(clazz.id());
          }
        }
        return clazzIds;
      };

      ViewModel.prototype.changeShowType = function(type, self) {
        self.showType[self.focusLevel() - 1](type);
      };

      ViewModel.prototype.changeAllStatus = function(self) {
        var clazz, i, len;
        self.selectAll[self.focusLevel() - 1](!self.selectAll[self.focusLevel() - 1]());
        for (i = 0, len = this.length; i < len; i++) {
          clazz = this[i];
          clazz.isChecked(self.selectAll[self.focusLevel() - 1]());
        }
        ViewModel.emit("ko.event.changeClazz", [self.focusLevel(), self._getClazzIds()]);
      };

      ViewModel.prototype.changeStatus = function(self) {
        this.isChecked(!this.isChecked());
        self.selectAll[self.focusLevel() - 1](self.checkStat(self));
        ViewModel.emit("ko.event.changeClazz", [self.focusLevel(), self._getClazzIds()]);
      };

      ViewModel.prototype.changeLevel = function(newLevel, self) {
        self.focusLevel(newLevel);
        ViewModel.emit("ko.event.changeClazz", [self.focusLevel(), self._getClazzIds()]);
      };

      ViewModel.prototype.setGroupSupported = function(flag) {
        this.groupSupported(flag);
      };

      ViewModel.prototype.init = function(levelInfo) {
        var clazz, clazzs, groups, i, j, k, l, len, len1, len2, len3, len4, len5, m, n, newLevel;
        for (i = 0, len = levelInfo.length; i < len; i++) {
          clazzs = levelInfo[i];
          for (j = 0, len1 = clazzs.length; j < len1; j++) {
            clazz = clazzs[j];
            clazz.isChecked = true;
          }
        }
        newLevel = [];
        for (k = 0, len2 = levelInfo.length; k < len2; k++) {
          clazzs = levelInfo[k];
          groups = [];
          for (l = 0, len3 = clazzs.length; l < len3; l++) {
            clazz = clazzs[l];
            if (clazz.curTeacherArrangeableGroups.length > 0) {
              groups = groups.concat(clazz.curTeacherArrangeableGroups);
            }
          }
          newLevel.push(groups);
        }
        for (m = 0, len4 = newLevel.length; m < len4; m++) {
          clazzs = newLevel[m];
          for (n = 0, len5 = clazzs.length; n < len5; n++) {
            clazz = clazzs[n];
            clazz.isChecked = false;
          }
        }
        this.data(ko.mapping.fromJS(levelInfo)());
        this.groupData(ko.mapping.fromJS(newLevel)());
      };

      return ViewModel;

    })(Events);
    return ViewModel;
  });

}).call(this);

//# sourceMappingURL=LevelAndClazz.js.map
