package top.sacz.xphelper.base;

import top.sacz.xphelper.dexkit.cache.DexKitCache;

public abstract class BaseDexQuery {
    public final boolean existCache() {
        return DexKitCache.exist(toString());
    }
}
