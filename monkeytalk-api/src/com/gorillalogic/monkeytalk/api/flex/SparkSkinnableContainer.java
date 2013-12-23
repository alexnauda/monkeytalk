package com.gorillalogic.monkeytalk.api.flex;
    public interface SparkSkinnableContainer extends SparkSkinnableContainerBase { 
/**
* DragComplete
* @param keyModifier
*        int keyModifier
*/
        public void dragcomplete(int keyModifier);
/**
* DragDrop
* @param action
*        String action
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        int keyModifier
*/
        public void dragdrop(String action, String draggedItem, int keyModifier);
/**
* DragStart
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        int keyModifier
*/
        public void dragstart(String draggedItem, int keyModifier);
/**
* MouseScroll
* @param delta
*        int delta
*/
        public void mousescroll(int delta);
    }