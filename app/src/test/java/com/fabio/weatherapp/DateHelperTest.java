package com.fabio.weatherapp;

import org.junit.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.util.Map;

public class DateHelperTest {

    @Test
    public void testGetTodayDate() {
        Map<String,Integer> result = DateHelper.getTodayDate();
        assertEquals("Array has not 3 elements", 3, result.size());
        assertTrue(result.containsKey("day"));
        assertTrue(result.containsKey("month"));
        assertTrue(result.containsKey("year"));
        Integer day = result.get("day");
        Integer month = result.get("month");
        Integer year = result.get("year");
        assertTrue((day>=1 && day<=31));
        assertTrue((month>=1 && month<=12));
        assertTrue(year>=2020);
    }
}
