
function firstCategories() {
    $(".firstCategory").empty();
    $(".firstCategory").append("<option value='' key='-1'>请选择</option>");
    if (CATEGORIES != null) {
        for (var i in CATEGORIES) {
            var dataItem = CATEGORIES[i];
            $(".firstCategory").append("<option value='" + dataItem.id + "' key='" + i + "'>" + dataItem.name + "</option>");
        }

        var category = $(".firstCategory").attr("category");
        if (!blankString(category)) {
            $(".firstCategory").val(category);
        }
    }
    secondCategories();
}
function secondCategories() {
    $(".secondCategory").empty();
    $(".secondCategory").append("<option value=''>请选择</option>");

    var firstIndex = $(".firstCategory option:selected").attr("key");
    if (CATEGORIES != null && !blankString(firstIndex) && CATEGORIES[firstIndex] != null && CATEGORIES[firstIndex].childList != null) {
        var seconds = CATEGORIES[firstIndex].childList;
        for (var i in seconds) {
            var dataItem = seconds[i];
            $(".secondCategory").append("<option value='" + dataItem.id + "' key='" + i + "'>" + dataItem.name + "</option>");
        }

        var firstDefault = $(".firstCategory").attr("category");
        var firstValue = $(".firstCategory").val();
        var category = $(".secondCategory").attr("category");
        if (!blankString(firstDefault) && firstValue == firstDefault && !blankString(category)) {
            $(".secondCategory").val(category);
        }
    }
    thirdCategories();
}
function thirdCategories() {
    $(".thirdCategory").empty();
    $(".thirdCategory").append("<option value=''>请选择</option>");

    var firstIndex = $(".firstCategory option:selected").attr("key");
    var secondIndex = $(".secondCategory option:selected").attr("key");
    if (CATEGORIES != null && !blankString(firstIndex) && !blankString(secondIndex) && CATEGORIES[firstIndex].childList[secondIndex] != null && CATEGORIES[firstIndex].childList[secondIndex].childList != null) {
        var thirds = CATEGORIES[firstIndex].childList[secondIndex].childList;
        for (var i in thirds) {
            var dataItem = thirds[i];
            $(".thirdCategory").append("<option value='" + dataItem.id + "' key='" + i + "'>" + dataItem.name + "</option>");
        }

        var category = $(".thirdCategory").attr("category");
        if (!blankString(category)) {
            $(".thirdCategory").val(category);
        }
    }

}