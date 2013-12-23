package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexComboBox extends FlexComboBase { 
/**
* Input
* @param text
*        String text
*/
        public void input(String text);
/**
* Select
* @param itemRenderer
*        String itemRenderer
* @param triggerEvent
*        String triggerEvent
* @param keyModifier
*        String keyModifier
*/
        public void select(String itemRenderer, String triggerEvent, String keyModifier);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }