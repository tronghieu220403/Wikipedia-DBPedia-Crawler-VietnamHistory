/*
 * Copyright 2016-2022 The OSHI Project Contributors
 * SPDX-License-Identifier: MIT
 */
package oshi.hardware.platform.mac;

import static oshi.util.Memoizer.defaultExpiration;
import static oshi.util.Memoizer.memoize;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.hardware.common.AbstractGlobalMemory;
import oshi.jna.ByRef.CloseableIntByReference;
import oshi.jna.ByRef.CloseableLongByReference;
import oshi.jna.Struct.CloseableVMStatistics;
import oshi.util.Constants;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.SysctlUtil;

/**
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 */
@ThreadSafe
final class MacGlobalMemory extends AbstractGlobalMemory {

    private static final Logger LOG = LoggerFactory.getLogger(MacGlobalMemory.class);

    private final Supplier<Long> available = memoize(this::queryVmStats, defaultExpiration());

    private final Supplier<Long> total = memoize(MacGlobalMemory::queryPhysMem);

    private final Supplier<Long> pageSize = memoize(MacGlobalMemory::queryPageSize);

    private final Supplier<VirtualMemory> vm = memoize(this::createVirtualMemory);

    @Override
    public long getAvailable() {
        return available.get();
    }

    @Override
    public long getTotal() {
        return total.get();
    }

    @Override
    public long getPageSize() {
        return pageSize.get();
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        List<PhysicalMemory> pmList = new ArrayList<>();
        List<String> sp = ExecutingCommand.runNative("system_profiler SPMemoryDataType");
        int bank = 0;
        String bankLabel = Constants.UNKNOWN;
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = Constants.UNKNOWN;
        String memoryType = Constants.UNKNOWN;
        for (String line : sp) {
            if (line.trim().startsWith("BANK")) {
                // Save previous bank
                if (bank++ > 0) {
                    pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType));
                }
                bankLabel = line.trim();
                int colon = bankLabel.lastIndexOf(':');
                if (colon > 0) {
                    bankLabel = bankLabel.substring(0, colon - 1);
                }
            } else if (bank > 0) {
                String[] split = line.trim().split(":");
                if (split.length == 2) {
                    switch (split[0]) {
                    case "Size":
                        capacity = ParseUtil.parseDecimalMemorySizeToBinary(split[1].trim());
                        break;
                    case "Type":
                        memoryType = split[1].trim();
                        break;
                    case "Speed":
                        speed = ParseUtil.parseHertz(split[1]);
                        break;
                    case "Manufacturer":
                        manufacturer = split[1].trim();
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType));

        return pmList;
    }

    private long queryVmStats() {
        try (CloseableVMStatistics vmStats = new CloseableVMStatistics();
                CloseableIntByReference size = new CloseableIntByReference(vmStats.size() / SystemB.INT_SIZE)) {
            if (0 != SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), SystemB.HOST_VM_INFO, vmStats,
                    size)) {
                LOG.error("Failed to get host VM info. Error code: {}", Native.getLastError());
                return 0L;
            }
            return (vmStats.free_count + vmStats.inactive_count) * getPageSize();
        }
    }

    private static long queryPhysMem() {
        return SysctlUtil.sysctl("hw.memsize", 0L);
    }

    private static long queryPageSize() {
        try (CloseableLongByReference pPageSize = new CloseableLongByReference()) {
            if (0 == SystemB.INSTANCE.host_page_size(SystemB.INSTANCE.mach_host_self(), pPageSize)) {
                return pPageSize.getValue();
            }
        }
        LOG.error("Failed to get host page size. Error code: {}", Native.getLastError());
        return 4098L;
    }

    private VirtualMemory createVirtualMemory() {
        return new MacVirtualMemory(this);
    }
}
