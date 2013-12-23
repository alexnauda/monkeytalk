package com.gorillalogic.monkeytalk.api.flex;
    public interface SparkRichEditableText extends FlexObject { 
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
*        int direction
* @param detail
*        String detail
*/
        public void scroll(int position, int direction, String detail);
/**
* SelectText
* @param beginIndex
*        int beginIndex
* @param endIndex
*        int endIndex
*/
        public void selecttext(int beginIndex, int endIndex);
/**
* ChangeFocus
* @param shiftKey
*        Boolean shiftKey
* @param keyCode
*        String keyCode
*/
        public void changefocus(Boolean shiftKey, String keyCode);
/**
* MouseScroll
* @param delta
*        int delta
*/
        public void mousescroll(int delta);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        int keyModifier
*/
        public void type(String keyCode, int keyModifier);
    }