package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexNumericStepper extends FlexObject { 
/**
* Change
* @param value
*        Number value
*/
        public void change(Number value);
/**
* Input
* @param text
*        String text
*/
        public void input(String text);
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