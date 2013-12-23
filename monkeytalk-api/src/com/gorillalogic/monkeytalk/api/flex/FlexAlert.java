package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexAlert extends FlexPanel { 
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }