package com.gorillalogic.monkeytalk.api.flex;
    public interface SparkComboBox extends SparkDropDownListBase { 
/**
* Input
* @param text
*        String text
*/
        public void input(String text);
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