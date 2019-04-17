(function($17,ko) {
    "use strict";

    ko.components.register('word-recognition-and-reading',{
        viewModel : function(params){
            var question = ko.mapping.toJS(params.question);
            var subContents = question.content.subContents;
            var subContent = $.isArray(subContents) ? subContents[0] : {};
            var playingAudioCb = params.playAudio;
            this.playing = params.playing || ko.observable(false);
            this.extras = subContent.extras || {};
            this.clientRole = params.clientRole || "teacher";  //clientRole取值[teacher | student ]
            this.question = question;  //http://knockoutjs.com/documentation/component-custom-elements.html
            this.playingAudio = function(){
                typeof playingAudioCb === "function" && playingAudioCb({
                    id          : this.question.id,
                    audioUrl    : this.extras.chineseWordAudioUrl
                });
            }.bind(this);
        },
        template : template("T:WORD_RECOGNITION_AND_READING_QUESTION",{})
    });


}($17,ko));