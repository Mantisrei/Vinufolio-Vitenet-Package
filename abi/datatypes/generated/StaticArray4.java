package net.vite.wallet.abi.datatypes.generated;

import net.vite.wallet.abi.datatypes.StaticArray;
import net.vite.wallet.abi.datatypes.Type;

import java.util.List;


public class StaticArray4<T extends Type> extends StaticArray<T> {
    @Deprecated
    public StaticArray4(List<T> values) {
        super(4, values);
    }

    @Deprecated
    @SafeVarargs
    public StaticArray4(T... values) {
        super(4, values);
    }

    public StaticArray4(Class<T> type, List<T> values) {
        super(type, 4, values);
    }

    @SafeVarargs
    public StaticArray4(Class<T> type, T... values) {
        super(type, 4, values);
    }
}
