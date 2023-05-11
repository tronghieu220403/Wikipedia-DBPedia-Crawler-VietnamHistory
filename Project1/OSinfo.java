package Project1;

import java.io.File;
import java.lang.management.ManagementFactory;

class OperatingSystemInfo {
    private String name;
    private String version;
    private String arch;
    private long memorySize;
    private long diskSize;

    private OperatingSystemInfo()
    {
        this.name = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getName();
        this.version = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getVersion();
        this.arch = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getArch();
        this.memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
        this.diskSize = new File("C:/").getTotalSpace();
    }

    public String getName() {
        return this.name;
    }

    public String getVersion(){
        return this.version;
    }

    public String getArch() {
        return this.arch;
    }

    public long getMemorySize()   {   
        return this.memorySize; 
    }

    public long getDiskSize() {
        return this.diskSize;
    }

    public static void main(String[] args) {
        OperatingSystemInfo mySystemInfo = new OperatingSystemInfo();
        System.out.println((double)(mySystemInfo.diskSize/(double)(1024*1024*1024)));
        long freeMemSize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize();
        System.out.println("Free memory (GB): " + (double)((freeMemSize/(double)(1024*1024*1024))));
    
        System.out.println(System.getenv("PROCESSOR_IDENTIFIER"));
        System.out.println(System.getenv("PROCESSOR_ARCHITECTURE"));
        System.out.println(System.getenv("PROCESSOR_ARCHITEW6432"));
        System.out.println(System.getenv("NUMBER_OF_PROCESSORS"));


    }
}
