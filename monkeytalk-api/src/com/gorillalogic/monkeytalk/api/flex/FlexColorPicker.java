package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexColorPicker extends FlexComboBase { 
/**
* Change
* @param color
*        String color
*/
        public void change(String color);
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