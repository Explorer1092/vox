define (require, exports)->

    class CheckBoxs
        @changeAllStatus: (charter, clause, court, sentence)->
            charter[clause] !charter[clause]

            for judge in court
                judge.isChecked charter[clause]()

            sentence()

            callBack = sentence or @sentence or ()->

            return

    return CheckBoxs