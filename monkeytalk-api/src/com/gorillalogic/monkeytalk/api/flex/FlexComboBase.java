package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexComboBase extends FlexObject { 
/**
* Close
* @param triggerEvent
*        String triggerEvent
*/
        public void close(String triggerEvent);
/**
* Input
* @param text
*        String text
*/
        public void input(String text);
/**
* Open
* @param triggerEvent
*        String triggerEvent
*/
        public void open(String triggerEvent);
/**
* Scroll
* @param position
*        int position
* @param direction
*        String direction
* @param detail
*        String detail
*/
        public void scroll(int position, String direction, String detail);
/**
* SelectText
* @param beginIndex
*        int beginIndex
* @param endIndex
*        int endIndex
*/
        public void selecttext(int beginIndex, int endIndex);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }