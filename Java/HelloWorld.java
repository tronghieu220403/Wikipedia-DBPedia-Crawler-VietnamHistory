package Java;

import java.io.File;
import java.lang.management.ManagementFactory;

class HelloWorld {
    public static void main(String[] args) {
        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
        System.out.println("Maximum memory (GB): " + (double)(memorySize/(double)(1024*1024*1024)));
        long diskSize = new File("/").getTotalSpace();
        System.out.println("Disk memory (GB): " + (double)(diskSize/(double)(1024*1024*1024)));
        long freeMemSize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize();
        System.out.println("Free memory (GB): " + (double)((freeMemSize/(double)(1024*1024*1024))));
        String name = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getName();
        System.out.println("Name: " + name);
        String version = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getVersion();
        System.out.println("Version: " + version);
        String arch = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getArch();
        System.out.println("Arch: " + arch);
    }
}
