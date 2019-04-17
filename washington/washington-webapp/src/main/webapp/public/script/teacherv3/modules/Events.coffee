define (require, exports)->

    class Events
        @on: (eventName, callback)->
            $("body").on eventName, callback
            return
        @emit: (eventName, params)->
            $("body").trigger eventName, params
            return

    return Events