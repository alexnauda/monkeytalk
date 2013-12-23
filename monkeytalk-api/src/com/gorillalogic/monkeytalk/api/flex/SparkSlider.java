package com.gorillalogic.monkeytalk.api.flex;
    public interface SparkSlider extends SparkRange { 
/**
* Change
* @param value
*        Number value
*/
        public void change(Number value);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        int keyModifier
*/
        public void type(String keyCode, int keyModifier);
    }