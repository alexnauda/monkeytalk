package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexListBase extends FlexScrollBase { 
/**
* Deselect
* @param itemRenderer
*        String itemRenderer
* @param triggerEvent
*        String triggerEvent
* @param keyModifier
*        String keyModifier
*/
        public void deselect(String itemRenderer, String triggerEvent, String keyModifier);
/**
* DoubleClick
* @param itemRenderer
*        String itemRenderer
*/
        public void doubleclick(String itemRenderer);
/**
* DragCancel
* @param keyModifier
*        String keyModifier
*/
        public void dragcancel(String keyModifier);
/**
* DragDrop
* @param action
*        String action
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
*/
        public void dragdrop(String action, String draggedItem, String keyModifier);
/**
* DragStart
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
*/
        public void dragstart(String draggedItem, String keyModifier);
/**
* MouseScroll
* @param delta
*        int delta
*/
        public void mousescroll(int delta);
/**
* MultiSelect
* @param itemRenderer
*        String itemRenderer
* @param triggerEvent
*        String triggerEvent
* @param keyModifier
*        String keyModifier
*/
        public void multiselect(String itemRenderer, String triggerEvent, String keyModifier);
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
* SelectIndex
* @param itemIndex
*        int itemIndex
* @param triggerEvent
*        String triggerEvent
* @param keyModifier
*        String keyModifier
*/
        public void selectindex(int itemIndex, String triggerEvent, String keyModifier);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }