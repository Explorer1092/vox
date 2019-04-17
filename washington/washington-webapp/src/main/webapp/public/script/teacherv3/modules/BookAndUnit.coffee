define (require, exports)->
    Events = require "Events"

    class ViewModel extends Events
        constructor: ->
        bookInfo: ko.observable {}
        focusUnit: ko.observable 0
        _focusIndex: 0
        changeBook: ->
            console.info "这里要些还课本逻辑"
            return
        changeUnit: (index, self)->
            self.focusUnit @unitId()
            self._focusIndex = index

            ViewModel.emit "ko.event.changeUnit", [@unitId]
            return
        hasContent: (index, self)->
            ab = self.bookInfo().unitList()[index].abacus
            return ab.englishBasic() + ab.mathBasic() + ab.special() + ab.reading() + ab.exam()
        init: (bookInfo)->
            for unit, index in bookInfo.unitList or []
                unit.abacus =
                    englishBasic: 0
                    englishBasicTime: 0
                    mathBasic: 0
                    mathBasicTime: 0
                    special: 0
                    specialTime: 0
                    reading: 0
                    readingTime: 0
                    exam: 0
                    examTime: 0

                unit.isOpen = no

                if unit.defaultUnit
                    @_focusIndex = index
                    @focusUnit unit.unitId
                    ViewModel.emit "ko.event.changeUnit", [unit.unitId]


            @bookInfo ko.mapping.fromJS bookInfo

            return
        bookFilter: (base, clazzIds)->
            updateTimes = []
            bookInfos = []

            for clazz in base
                if _.indexOf(clazzIds, clazz.id) >= 0
                    updateTimes.push clazz.updateTime
                    bookInfos.push clazz.bookJson

            return {} if updateTimes.length == 0

            maxTime = Math.max.apply null, updateTimes

            return JSON.parse bookInfos[_.indexOf updateTimes, maxTime]

    return ViewModel

#viewModel =
#    bookInfo: ko.observable {}
#    focusUnit: ko.observable 0
#    _focusIndex: 0
#
#self = viewModel
#
#viewModel.changeInfoStaus = ->
#    @isOpen not @isOpen()
#    return
#
#viewModel.hasContent = (index)->
#    ab = self.bookInfo().unitList()[index].abacus
#    ab.englishBasic() + ab.mathBasic() + ab.special() + ab.reading() + ab.exam()
#
#viewModel.countPlus = (type, diff, diffTime, _index)->
#    focusIndex = _index or self._focusIndex
#    self.bookInfo().unitList()[focusIndex].abacus[type] self.bookInfo().unitList()[focusIndex].abacus[type]() + diff
#    self.bookInfo().unitList()[focusIndex].abacus["#{type}Time"] self.bookInfo().unitList()[focusIndex].abacus["#{type}Time"]() + diffTime
#    return
#
#viewModel.countMinus = (type, diff, diffTime, _index)->
#    focusIndex = _index or self._focusIndex
#    self.bookInfo().unitList()[focusIndex].abacus[type] self.bookInfo().unitList()[focusIndex].abacus[type]() - diff
#    self.bookInfo().unitList()[focusIndex].abacus["#{type}Time"] self.bookInfo().unitList()[focusIndex].abacus["#{type}Time"]() - diffTime
#    return
#
#viewModel.countReset = ->
#    self.bookInfo().unitList()[self._focusIndex].abacus.englishBasic 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.englishBasicTime 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.mathBasic 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.mathBasicTime 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.special 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.specialTime 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.reading 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.readingTime 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.exam 0
#    self.bookInfo().unitList()[self._focusIndex].abacus.examTime 0
#    return
#
