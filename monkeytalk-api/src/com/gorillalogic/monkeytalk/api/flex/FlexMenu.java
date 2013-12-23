package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexMenu extends FlexObject { 
/**
* Hide
*/
        public void hide();
/**
* Select
* @param itemRenderer
*        String itemRenderer
*/
        public void select(String itemRenderer);
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