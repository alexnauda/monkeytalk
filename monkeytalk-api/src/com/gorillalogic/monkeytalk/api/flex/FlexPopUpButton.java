package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexPopUpButton extends FlexButton { 
/**
* Close
* @param triggerEvent
*        String triggerEvent
*/
        public void close(String triggerEvent);
/**
* Open
* @param triggerEvent
*        String triggerEvent
*/
        public void open(String triggerEvent);
    }