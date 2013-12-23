package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexButton extends FlexObject { 
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }