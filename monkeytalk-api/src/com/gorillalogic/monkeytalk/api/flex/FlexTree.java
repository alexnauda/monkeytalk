package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexTree extends FlexListBase { 
/**
* Close
* @param itemRenderer
*        String itemRenderer
* @param triggerEvent
*        String triggerEvent
*/
        public void close(String itemRenderer, String triggerEvent);
/**
* DragDrop
* @param action
*        String action
* @param dropParent
*        String dropParent
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
*/
        public void dragdrop(String action, String dropParent, String draggedItem, String keyModifier);
/**
* Open
* @param itemRenderer
*        String itemRenderer
* @param triggerEvent
*        String triggerEvent
*/
        public void open(String itemRenderer, String triggerEvent);
    }