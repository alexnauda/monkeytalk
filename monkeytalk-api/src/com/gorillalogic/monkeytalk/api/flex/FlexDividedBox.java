package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexDividedBox extends FlexBox { 
/**
* Dragged
* @param dividerIndex
*        int dividerIndex
* @param delta
*        Number delta
*/
        public void dragged(int dividerIndex, Number delta);
/**
* Pressed
* @param dividerIndex
*        int dividerIndex
* @param delta
*        Number delta
*/
        public void pressed(int dividerIndex, Number delta);
/**
* Released
* @param dividerIndex
*        int dividerIndex
* @param delta
*        Number delta
*/
        public void released(int dividerIndex, Number delta);
    }