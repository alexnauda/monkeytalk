package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexDataGrid extends FlexListBase { 
/**
* ColumnStretch
* @param columnIndex
*        int columnIndex
* @param localX
*        Number localX
*/
        public void columnstretch(int columnIndex, Number localX);
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
* HeaderClick
* @param columnIndex
*        int columnIndex
*/
        public void headerclick(int columnIndex);
/**
* HeaderShift
* @param newIndex
*        int newIndex
* @param oldIndex
*        int oldIndex
* @param triggerEvent
*        String triggerEvent
*/
        public void headershift(int newIndex, int oldIndex, String triggerEvent);
    }