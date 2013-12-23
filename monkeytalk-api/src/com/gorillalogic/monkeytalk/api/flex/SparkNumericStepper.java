package com.gorillalogic.monkeytalk.api.flex;
    public interface SparkNumericStepper extends SparkSpinner { 
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
*        int keyModifier
*/
        public void type(String keyCode, int keyModifier);
    }