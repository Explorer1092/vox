package com.voxlearning.utopia.cjlschool.data;


import com.unitever.cif.operation.listener.AgentListener;
import com.unitever.cif.operation.listener.data.ListenedEvent;
import com.unitever.cif.operation.listener.data.ListenedRequest;
import com.unitever.cif.operation.listener.data.ListenedResponse;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.utopia.cjlschool.support.CJLDataProcessor;
import com.voxlearning.utopia.cjlschool.support.CJLDataProcessorFactory;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLEntityType;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CJLDataReceiveListener implements AgentListener {

    private static Logger logger = LoggerFactory.getLogger(CJLDataReceiveListener.class);

    @Setter private CJLDataProcessorFactory dataProcessorFactory;

    @Override
    public void onEventAdd(ListenedEvent msg) {
        CJLDataProcessor processor = getProcessor(msg.getObjectName());
        if (processor == null) {
            return;
        }
        processor.sync(msg.getData());
    }

    @Override
    public void onEventChange(ListenedEvent msg) {
        CJLDataProcessor processor = getProcessor(msg.getObjectName());
        if (processor == null) {
            return;
        }
        processor.modify(msg.getData());
    }

    @Override
    public void onEventDelete(ListenedEvent msg) {
        // we do not care about this
    }

    @Override
    public void onRequestMessage(ListenedRequest msg) {
        // we have no privilege to do
    }

    @Override
    public void onResponseMessage(ListenedResponse msg) {
        CJLDataProcessor processor = getProcessor(msg.getObjectName());
        if (processor == null) {
            return;
        }
        AlpsThreadPool.getInstance().submit(() -> processor.sync(msg.getData()));
    }

    private CJLDataProcessor getProcessor(String objectName) {
        CJLEntityType type = CJLEntityType.parse(objectName);
        if (type == null) {
            logger.warn("Invalid object name found, object={}", objectName);
            return null;
        }

        CJLDataProcessor processor = dataProcessorFactory.getProcessor(type);
        if (processor == null) {
            logger.warn("Can not find processor of this type: type={}", type);
            return null;
        }

        return processor;
    }
}
