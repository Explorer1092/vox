function VoxExternalPluginExists() {
    if(!window.external){
        return false;
    }else{
        //typeof window.external.getVoxExternalPluginVersion 无法获取预期结果。IE返回的是 unknown
        try{
            return window.external.getVoxExternalPluginVersion() != '';
        }
        catch(e) {
            return false;
        }
    }
}

function VoxExternalPluginChivox(gameObject, externalObject) {

    this.gameObject = gameObject;
    this.externalObject = externalObject;

    this.init = function(appId, secretKey, latencyDetectorUrl) {
        this.externalObject.chivoxInit(appId, secretKey, latencyDetectorUrl);
    };

    this.startRecord = function(recordLength, serverParam) {
        this.externalObject.chivoxStartRecord(recordLength, $.toJSON(serverParam));
    };
    this.onRecordIdGot = function(recordId) {
        this.gameObject.chivoxOnRecordIdGot(recordId);
    };
    this.onRecordStarted = function(recordId) {
        this.gameObject.chivoxOnRecordStarted();
    };
    this.onRecordStopped = function(recordId, recordLength) {
        this.gameObject.chivoxOnRecordStopped(recordLength);
    };
    this.onResult = function(recordId) {
        var gameObject = this.gameObject;
        var externalObject = this.externalObject;
        setTimeout(function() {
            var result = externalObject.chivoxGetRecordResult(recordId);
            gameObject.chivoxOnResult(result);
        }, 100);
    };
    this.onCoreRequestExceptionTimeout = function(recordId) {
        this.gameObject.chivoxOnCoreRequestExceptionTimeout();
    };

    this.startReplay = function(param) {
        this.externalObject.chivoxStartReplay(param.recordId);
    };

    this.onReplayStopped = function(recordId) {
        this.gameObject.chivoxOnReplayStopped();
    };


    return this;
}
