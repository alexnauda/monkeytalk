package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexSlider extends FlexObject { 
/**
* Change
* @param value
*        Number value
* @param thumbIndex
*        int thumbIndex
* @param clickTarget
*        String clickTarget
* @param triggerEvent
*        String triggerEvent
* @param keyCode
*        String keyCode
*/
        public void change(Number value, int thumbIndex, String clickTarget, String triggerEvent, String keyCode);
    }