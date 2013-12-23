package com.gorillalogic.monkeytalk.api.flex;
    public interface SparkGroup extends SparkGroupBase { 
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
* DragEnterWithPos
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
* @param localX
*        int localX
* @param localY
*        int localY
* @param action
*        String action
*/
        public void dragenterwithpos(String draggedItem, String keyModifier, int localX, int localY, String action);
/**
* DragStartWithPos
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
* @param localX
*        int localX
* @param localY
*        int localY
* @param action
*        String action
*/
        public void dragstartwithpos(String draggedItem, String keyModifier, int localX, int localY, String action);
/**
* DragDropWithPos
* @param action
*        String action
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
* @param localX
*        int localX
* @param localY
*        int localY
* @param action
*        String action
*/
        public void dragdropwithpos(String action, String draggedItem, String keyModifier, int localX, int localY);
/**
* DragStart
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
*/
        public void dragstart(String draggedItem, String keyModifier);
/**
* DragEnter
* @param draggedItem
*        String draggedItem
* @param keyModifier
*        String keyModifier
*/
        public void dragenter(String draggedItem, String keyModifier);
    }