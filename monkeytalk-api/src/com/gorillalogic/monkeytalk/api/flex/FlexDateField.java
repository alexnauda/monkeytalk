package com.gorillalogic.monkeytalk.api.flex;

import java.util.Date;
    public interface FlexDateField extends FlexComboBase { 
/**
* Change
* @param newDate
*        Date newDate
*/
        public void change(Date newDate);
/**
* Scroll
* @param detail
*        String detail
*/
        public void scroll(String detail);
/**
* Type
* @param keyCode
*        String keyCode
* @param keyModifier
*        String keyModifier
*/
        public void type(String keyCode, String keyModifier);
    }