package com.potflesh.wenda.async;

import java.util.List;

/**
 * Created by bazinga on 2017/4/17.
 */
public interface EventHandler {

    void doHandle(EventModel model);

    // 一个 handler 可能关心好几个 event 事件
    List<EventType> getSupportEventTypes();
}
