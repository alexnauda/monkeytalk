package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexTextArea extends FlexScrollBase { 
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