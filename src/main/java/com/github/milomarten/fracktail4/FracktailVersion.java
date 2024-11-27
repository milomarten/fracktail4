package com.github.milomarten.fracktail4;

public class FracktailVersion {
    public static String getVersion() {
        var pkg = FracktailVersion.class.getPackage();
        return pkg == null ? "" : pkg.getImplementationVersion();
    }
}
