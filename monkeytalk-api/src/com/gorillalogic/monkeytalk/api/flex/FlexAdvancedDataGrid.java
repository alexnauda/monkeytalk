package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexAdvancedDataGrid extends FlexListBase { 
/**
* ChangeFocus
* @param shiftKey
*        Boolean shiftKey
* @param keyCode
*        String keyCode
*/
        public void changefocus(Boolean shiftKey, String keyCode);
/**
* Click
* @param keyModifier
*        String keyModifier
*/
        public void click(String keyModifier);
/**
* Close
* @param itemRenderer
*        String itemRenderer
* @param triggerEvent
*        String triggerEvent
*/
        public void close(String itemRenderer, String triggerEvent);
/**
* ColumnGroupedADGHeaderShift
* @param movingColumnIndex
*        Integer movingColumnIndex
* @param newColumnIndex
*        Integer newColumnIndex
* @param oldColumnIndex
*        Integer oldColumnIndex
* @param triggerEvent
*        String triggerEvent
*/
        public void columngroupedadgheadershift(Integer movingColumnIndex, Integer newColumnIndex, Integer oldColumnIndex, String triggerEvent);
/**
* ColumnStretch
* @param columnIndex
*        Integer columnIndex
* @param localX
*        VT_R8 localX
*/
        public void columnstretch(Integer columnIndex, String localX);
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
* Edit
* @param itemRenderer
*        String itemRenderer
* @param rowIndex
*        Integer rowIndex
* @param columnIndex
*        Integer columnIndex
*/
        public void edit(String itemRenderer, Integer rowIndex, Integer columnIndex);
/**
* GetAutomationChildAt
* @param childIndex
*        Integer childIndex
*/
        public void getautomationchildat(Integer childIndex);
/**
* GetCellData
* @param RowIndex
*        Integer RowIndex
* @param ColumnIndex
*        Integer ColumnIndex
* @param RestoreOriginalPosition
*        Boolean RestoreOriginalPosition
*/
        public void getcelldata(Integer RowIndex, Integer ColumnIndex, Boolean RestoreOriginalPosition);
/**
* GetGroupedItemChildrenCount
* @param RowIndex
*        Integer RowIndex
* @param RestoreOriginalPosition
*        Boolean RestoreOriginalPosition
*/
        public void getgroupeditemchildrencount(Integer RowIndex, Boolean RestoreOriginalPosition);
/**
* GetItemsCount
*/
        public void getitemscount();
/**
* GetRowData
* @param RowIndex
*        Integer RowIndex
* @param RestoreOriginalPosition
*        Boolean RestoreOriginalPosition
*/
        public void getrowdata(Integer RowIndex, Boolean RestoreOriginalPosition);
/**
* HeaderClick
* @param columnIndex
*        Integer columnIndex
* @param dataField
*        String dataField
* @param keyModifier
*        String keyModifier
* @param headerPart
*        String headerPart
* @param triggerEvent
*        String triggerEvent
*/
        public void headerclick(Integer columnIndex, String dataField, String keyModifier, String headerPart, String triggerEvent);
/**
* HeaderShift
* @param newIndex
*        Integer newIndex
* @param oldIndex
*        Integer oldIndex
* @param triggerEvent
*        String triggerEvent
*/
        public void headershift(Integer newIndex, Integer oldIndex, String triggerEvent);
/**
* IsGroupeditem
* @param RowIndex
*        Integer RowIndex
* @param RestoreOriginalPosition
*        Boolean RestoreOriginalPosition
*/
        public void isgroupeditem(Integer RowIndex, Boolean RestoreOriginalPosition);
/**
* MouseMove
* @param localX
*        Integer localX
* @param localY
*        Integer localY
* @param keyModifier
*        String keyModifier
*/
        public void mousemove(Integer localX, Integer localY, String keyModifier);
/**
* MouseScroll
* @param delta
*        Integer delta
*/
        public void mousescroll(Integer delta);
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
* Open
* @param itemRenderer
*        String itemRenderer
* @param triggerEvent
*        String triggerEvent
*/
        public void open(String itemRenderer, String triggerEvent);
/**
* Scroll
* @param position
*        Integer position
* @param direction
*        String direction
* @param detail
*        String detail
*/
        public void scroll(Integer position, String direction, String detail);
/**
* SelectIndex
* @param itemIndex
*        Integer itemIndex
* @param triggerEvent
*        String triggerEvent
* @param keyModifier
*        String keyModifier
*/
        public void selectindex(Integer itemIndex, String triggerEvent, String keyModifier);
/**
* SetFocus
*/
        public void setfocus();
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }