package com.gorillalogic.monkeytalk.api.flex;
    public interface FlexDisplayObject { 
/**
* Click
* @param keyModifier
*        String keyModifier
*/
        public void click(String keyModifier);
/**
* MouseMove
* @param localX
*        int localX
* @param localY
*        int localY
* @param keyModifier
*        String keyModifier
*/
        public void mousemove(int localX, int localY, String keyModifier);
    }