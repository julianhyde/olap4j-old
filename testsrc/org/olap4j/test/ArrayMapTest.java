/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2007-2008 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package org.olap4j.test;

import junit.framework.TestCase;
import org.olap4j.impl.ArrayMap;

import java.util.*;

/**
 * Unit test for {@link org.olap4j.impl.ArrayMap}.
 *
 * @author jhyde
 * @version $Id$
 * @since Dec 9, 2007
 */
public class ArrayMapTest extends TestCase {
    public void testArrayMap() {
        final ArrayMap<String, Integer> map =
            new ArrayMap<String, Integer>();

        // clear empty map
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());

        map.put("Paul", 4);
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        assertTrue(map.containsKey("Paul"));
        assertFalse(map.containsKey("Keith"));

        Integer value = map.put("Paul", 5);
        assertEquals(1, map.size());
        assertEquals(4, value.intValue());
        assertEquals(5, map.get("Paul").intValue());

        // null values are allowed
        map.put("Paul", null);
        assertNull(map.get("Paul"));
        assertTrue(map.containsKey("Paul"));

        // null keys are allowed
        map.put(null, -99);
        assertEquals(-99, map.get(null).intValue());
        assertTrue(map.containsKey(null));
        assertEquals(-99, map.remove(null).intValue());

        final Map<String, Integer> beatles =
            new ArrayMap<String, Integer>();
        beatles.put("John", 4);
        beatles.put("Paul", 4);
        beatles.put("George", 6);
        beatles.put("Ringo", 5);
        map.putAll(beatles);
        assertEquals(4, map.size());
        assertEquals(
            new HashSet<String>(
                Arrays.asList("John", "Paul", "George", "Ringo")),
            map.keySet());
        assertEquals(Arrays.asList(4, 4, 6, 5), map.values());

        String keys = "";
        int valueTotal = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            valueTotal += entry.getValue();
            keys += entry.getKey();
        }
        assertEquals(19, valueTotal);
        assertEquals("PaulJohnGeorgeRingo", keys);

        value = map.remove("Mick");
        assertNull(value);
        assertEquals(4, map.size());

        // corner case: remove last value
        value = map.remove("Ringo");
        assertEquals(5, value.intValue());
        assertEquals(3, map.size());

        // corner case: remove first value
        value = map.remove("Paul");
        assertEquals(4, value.intValue());
        assertEquals(2, map.size());

        // add key back in and it becomes last value
        map.put("Paul", 27);
        assertEquals(Arrays.asList(4, 6, 27), map.values());

        // remove an interior value
        map.remove("George");
        assertEquals(2, map.size());
    }

    /**
     * Oops, forgot that I had written the first test and wrote another. Mostly
     * overlap with {@link #testArrayMap()}, but can't hurt.
     */
    public void testArrayMap2() {
        final ArrayMap<String, Integer> map = new ArrayMap<String, Integer>();
        final HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        assertEquals(0, map.size());
        assertEquals(map, hashMap);
        assertEquals(map.hashCode(), hashMap.hashCode());

        // put
        map.put("foo", 0);
        assertEquals(1, map.size());
        assertEquals(1, map.keySet().size());
        assertEquals(1, map.values().size());

        // equivalence to hashmap
        hashMap.put("foo", 0);
        assertEquals(map, hashMap);
        assertEquals(hashMap, map);
        assertEquals(map.hashCode(), hashMap.hashCode());

        // containsKey, get
        assertTrue(map.containsKey("foo"));
        assertFalse(map.containsKey("bar"));
        assertEquals(Integer.valueOf(0), map.get("foo"));
        assertNull(map.get("baz"));

        // putall
        final Map<String, Integer> hashMap2 = new HashMap<String, Integer>();
        hashMap2.put("bar", 1);
        hashMap2.put("foo", 2);
        hashMap2.put("baz", 0);
        map.putAll(hashMap2);
        hashMap.putAll(hashMap2);
        assertEquals(3, map.size());
        assertEquals(map, hashMap);
        assertEquals(hashMap, map);
        assertEquals(map.hashCode(), hashMap.hashCode());
        assertEquals(map.keySet(), hashMap.keySet());
        // values collections have same contents, not necessarily in same order
        assertEquals(
            new HashSet<Integer>(map.values()),
            new HashSet<Integer>(hashMap.values()));

        // replace existing key
        map.put("foo", -5);
        hashMap.put("foo", -5);
        assertEquals(3, map.size());
        assertEquals(Integer.valueOf(-5), map.get("foo"));
        assertEquals(map, hashMap);
        assertEquals(hashMap, map);

        // null key
        assertFalse(map.containsKey(null));
        map.put(null, 75);
        assertEquals(Integer.valueOf(75), map.get(null));
        assertTrue(map.containsKey(null));

        // null value
        map.put("zzzz", null);
        assertTrue(map.containsKey("zzzz"));
        assertNull(map.get("zzzz"));

        // compare to hashmap
        hashMap.put(null, 75);
        hashMap.put("zzzz", null);
        assertEquals(map, hashMap);
        assertEquals(hashMap, map);

        // isEmpty, clear
        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        // putAll to populate empty map (uses different code path than putAll
        // on non-empty map)
        final ArrayMap<String, Integer> map2 =
            new ArrayMap<String, Integer>();
        map2.putAll(hashMap);
        assertEquals(map2, hashMap);

        // copy constructor
        final ArrayMap<String, Integer> map3 =
            new ArrayMap<String, Integer>(hashMap);
        assertEquals(map3, hashMap);
    }
}

// End ArrayMapTest.java
