/*
 * Copyright 2019-2022 The OSHI Project Contributors
 * SPDX-License-Identifier: MIT
 */
package oshi.util.platform.windows;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.PdhUtil;
import com.sun.jna.platform.win32.PdhUtil.PdhException;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.PerfDataUtil.PerfCounter;

/**
 * Enables queries of Performance Counters using wild cards to filter instances
 */
@ThreadSafe
public final class PerfCounterQuery {

    private static final Logger LOG = LoggerFactory.getLogger(PerfCounterQuery.class);

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    // Use a thread safe set to cache failed pdh queries
    private static final Set<String> FAILED_QUERY_CACHE = ConcurrentHashMap.newKeySet();

    // A map to cache localization strings
    private static final ConcurrentHashMap<String, String> LOCALIZE_CACHE = new ConcurrentHashMap<>();

    /*
     * Multiple classes use these constants
     */
    public static final String TOTAL_INSTANCE = "_Total";
    public static final String TOTAL_OR_IDLE_INSTANCES = "_Total|Idle";
    public static final String TOTAL_INSTANCES = "*_Total";
    public static final String NOT_TOTAL_INSTANCE = "^" + TOTAL_INSTANCE;
    public static final String NOT_TOTAL_INSTANCES = "^" + TOTAL_INSTANCES;

    private PerfCounterQuery() {
    }

    /**
     * Query the a Performance Counter using PDH, with WMI backup on failure, for values corresponding to the property
     * enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements
     *                     {@link oshi.util.platform.windows.PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will be refreshed at the same
     *                     time
     * @param perfWmiClass The WMI PerfData_RawData_* class corresponding to the PDH object
     * @return An {@link EnumMap} of the values indexed by {@code propertyEnum} on success, or an empty map if both PDH
     *         and WMI queries failed.
     */
    public static <T extends Enum<T>> Map<T, Long> queryValues(Class<T> propertyEnum, String perfObject,
            String perfWmiClass) {
        if (!FAILED_QUERY_CACHE.contains(perfObject)) {
            Map<T, Long> valueMap = queryValuesFromPDH(propertyEnum, perfObject);
            if (!valueMap.isEmpty()) {
                return valueMap;
            }
            // If we are here, query failed
            LOG.warn("Disabling further attempts to query {}.", perfObject);
            FAILED_QUERY_CACHE.add(perfObject);
        }
        return queryValuesFromWMI(propertyEnum, perfWmiClass);
    }

    /**
     * Query the a Performance Counter using PDH for values corresponding to the property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements
     *                     {@link oshi.util.platform.windows.PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will be refreshed at the same
     *                     time
     * @return An {@link EnumMap} of the values indexed by {@code propertyEnum} on success, or an empty map if the PDH
     *         query failed.
     */
    public static <T extends Enum<T>> Map<T, Long> queryValuesFromPDH(Class<T> propertyEnum, String perfObject) {
        T[] props = propertyEnum.getEnumConstants();
        // If pre-Vista, localize the perfObject
        String perfObjectLocalized = PerfCounterQuery.localizeIfNeeded(perfObject, false);
        EnumMap<T, PerfCounter> counterMap = new EnumMap<>(propertyEnum);
        EnumMap<T, Long> valueMap = new EnumMap<>(propertyEnum);
        try (PerfCounterQueryHandler pdhQueryHandler = new PerfCounterQueryHandler()) {
            // Set up the query and counter handles
            for (T prop : props) {
                PerfCounter counter = PerfDataUtil.createCounter(perfObjectLocalized,
                        ((PdhCounterProperty) prop).getInstance(), ((PdhCounterProperty) prop).getCounter());
                counterMap.put(prop, counter);
                if (!pdhQueryHandler.addCounterToQuery(counter)) {
                    return valueMap;
                }
            }
            // And then query. Zero timestamp means update failed
            if (0 < pdhQueryHandler.updateQuery()) {
                for (T prop : props) {
                    valueMap.put(prop, pdhQueryHandler.queryCounter(counterMap.get(prop)));
                }
            }
        }
        return valueMap;
    }

    /**
     * Query the a Performance Counter using WMI for values corresponding to the property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements
     *                     {@link oshi.util.platform.windows.PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param wmiClass     The WMI PerfData_RawData_* class corresponding to the PDH object
     * @return An {@link EnumMap} of the values indexed by {@code propertyEnum} if successful, an empty map if the WMI
     *         query failed.
     */
    public static <T extends Enum<T>> Map<T, Long> queryValuesFromWMI(Class<T> propertyEnum, String wmiClass) {
        WmiQuery<T> query = new WmiQuery<>(wmiClass, propertyEnum);
        WmiResult<T> result = Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(query);
        EnumMap<T, Long> valueMap = new EnumMap<>(propertyEnum);
        if (result.getResultCount() > 0) {
            for (T prop : propertyEnum.getEnumConstants()) {
                switch (result.getCIMType(prop)) {
                case Wbemcli.CIM_UINT16:
                    valueMap.put(prop, (long) WmiUtil.getUint16(result, prop, 0));
                    break;
                case Wbemcli.CIM_UINT32:
                    valueMap.put(prop, WmiUtil.getUint32asLong(result, prop, 0));
                    break;
                case Wbemcli.CIM_UINT64:
                    valueMap.put(prop, WmiUtil.getUint64(result, prop, 0));
                    break;
                case Wbemcli.CIM_DATETIME:
                    valueMap.put(prop, WmiUtil.getDateTime(result, prop, 0).toInstant().toEpochMilli());
                    break;
                default:
                    throw new ClassCastException("Unimplemented CIM Type Mapping.");
                }
            }
        }
        return valueMap;
    }

    /**
     * Localize a PerfCounter string. English counter names should normally be in
     * {@code HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows
     * NT\CurrentVersion\Perflib\009\Counter}, but language manipulations may delete the {@code 009} index. In this case
     * we can assume English must be the language and continue. We may still fail to match the name if the assumption is
     * wrong but it's better than nothing.
     *
     * @param perfObject A String to localize
     * @param force      If true, always localize
     * @return The localized string if localization successful, or the original string otherwise.
     */
    public static String localizeIfNeeded(String perfObject, boolean force) {
        return !force && IS_VISTA_OR_GREATER ? perfObject
                : LOCALIZE_CACHE.computeIfAbsent(perfObject, PerfCounterQuery::localizeUsingPerfIndex);
    }

    private static String localizeUsingPerfIndex(String perfObject) {
        String localized = perfObject;
        try {
            localized = PdhUtil.PdhLookupPerfNameByIndex(null, PdhUtil.PdhLookupPerfIndexByEnglishName(perfObject));
        } catch (Win32Exception e) {
            LOG.warn(
                    "Unable to locate English counter names in registry Perflib 009. Assuming English counters. Error {}. {}",
                    String.format("0x%x", e.getHR().intValue()),
                    "See https://support.microsoft.com/en-us/help/300956/how-to-manually-rebuild-performance-counter-library-values");
        } catch (PdhException e) {
            LOG.warn("Unable to localize {} performance counter.  Error {}.", perfObject,
                    String.format("0x%x", e.getErrorCode()));
        }
        if (localized.isEmpty()) {
            return perfObject;
        }
        LOG.debug("Localized {} to {}", perfObject, localized);
        return localized;
    }

    /**
     * Contract for Counter Property Enums
     */
    public interface PdhCounterProperty {
        /**
         * @return Returns the instance.
         */
        String getInstance();

        /**
         * @return Returns the counter.
         */
        String getCounter();
    }
}
