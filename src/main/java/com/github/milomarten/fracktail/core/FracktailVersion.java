package com.github.milomarten.fracktail.core;

public class FracktailVersion {
    public static String getVersion() {
        var pkg = FracktailVersion.class.getPackage();
        return pkg == null ? "" : pkg.getImplementationVersion();
    }
}
