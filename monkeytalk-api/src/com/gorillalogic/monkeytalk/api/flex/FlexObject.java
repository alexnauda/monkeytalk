package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexObject extends FlexDisplayObject { 
/**
* ChangeFocus
* @param shiftKey
*        Boolean shiftKey
* @param keyCode
*        String keyCode
*/
        public void changefocus(Boolean shiftKey, String keyCode);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }