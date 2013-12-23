package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexScrollBar extends FlexObject { 
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
    }