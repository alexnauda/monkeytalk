package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexMenuBar extends FlexObject { 
/**
* Hide
*/
        public void hide();
/**
* Show
* @param itemRenderer
*        String itemRenderer
*/
        public void show(String itemRenderer);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }