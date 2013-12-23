package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexAccordion extends FlexContainer { 
/**
* Change
* @param relatedObject
*        String relatedObject
*/
        public void change(String relatedObject);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }