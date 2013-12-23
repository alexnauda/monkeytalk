package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexList extends FlexListBase { 
/**
* Edit
* @param itemRenderer
*        String itemRenderer
* @param rowIndex
*        int rowIndex
* @param columnIndex
*        int columnIndex
*/
        public void edit(String itemRenderer, int rowIndex, int columnIndex);
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
    }