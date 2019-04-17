define (require, exports)->
    Events = require "Events"

    class ViewModel extends Events
        constructor: ->
        checkStat: (self)->
            count = 0
            for clazz in self.data()[self.focusLevel() - 1]
                count++ if clazz.isChecked()

            return count is self.data()[self.focusLevel() - 1].length
        focusLevel: ko.observable 0
        groupSupported: ko.observable no
        data: ko.observableArray []
        groupData: ko.observableArray []
        showType: [
            ko.observable "clazz"
            ko.observable "clazz"
            ko.observable "clazz"
            ko.observable "clazz"
            ko.observable "clazz"
            ko.observable "clazz"
        ]
        selectAll: [
            ko.observable yes
            ko.observable yes
            ko.observable yes
            ko.observable yes
            ko.observable yes
            ko.observable yes
        ]
        groupSelectAll: [
            ko.observable no
            ko.observable no
            ko.observable no
            ko.observable no
            ko.observable no
            ko.observable no
        ]
        _getClazzIds: ->
            clazzIds = []
            for clazz in @data()[@focusLevel() - 1]
                clazzIds.push clazz.id() if clazz.isChecked()

            return clazzIds
        changeShowType: (type, self)->
            self.showType[self.focusLevel() - 1] type
            return
        changeAllStatus: (self)->
            self.selectAll[self.focusLevel() - 1] !self.selectAll[self.focusLevel() - 1]()

            for clazz in @
                clazz.isChecked self.selectAll[self.focusLevel() - 1]()

            ViewModel.emit "ko.event.changeClazz", [self.focusLevel(), self._getClazzIds()]

            return
        changeStatus: (self)->
            @isChecked !@isChecked()

            self.selectAll[self.focusLevel() - 1] self.checkStat(self)

            ViewModel.emit "ko.event.changeClazz", [self.focusLevel(), self._getClazzIds()]

            return
        changeLevel: (newLevel, self)->
            self.focusLevel newLevel
            ViewModel.emit "ko.event.changeClazz", [self.focusLevel(), self._getClazzIds()]

            return
        setGroupSupported: (flag)->
            @groupSupported flag

            return
        init: (levelInfo)->
            # 处理未分组数据
            for clazzs in levelInfo
                for clazz in clazzs
                    clazz.isChecked = yes

            # 生成分组数据
            newLevel = [];
            for clazzs in levelInfo
                groups = []
                for clazz in clazzs
                    groups = groups.concat clazz.curTeacherArrangeableGroups if clazz.curTeacherArrangeableGroups.length > 0

                newLevel.push groups
            for clazzs in newLevel
                for clazz in clazzs
                    clazz.isChecked = no

            @data ko.mapping.fromJS(levelInfo)()
            @groupData ko.mapping.fromJS(newLevel)()

            return

    return ViewModel