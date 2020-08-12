package cn.zenliu.java.translator;

import org.ahocorasick.trie.Emit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class InternalSelector {
    InternalSelector(){}
    public static Comparator<Emit> EMIT_COMPARATOR = (a, b) -> {
        if (a.getStart() == b.getStart() && a.size() < b.size()) {
            return 1;
        } else if (a.getStart() == b.getStart() && a.size() == b.size()) {
            return 0;
        } else if (a.getStart() == b.getStart()) {
            return -1;
        } else return Integer.compare(a.getStart(), b.getStart());
    };

   public static SegmentationSelector FORWARD_LONGEST_SELECTOR = emits -> {
       final ArrayList<Emit> list = new ArrayList<>(emits);
       list.sort(EMIT_COMPARATOR);
        final AtomicInteger endValueToRemove = new AtomicInteger(-1);
        return list.stream().filter(e -> {
            if (e.getStart() > endValueToRemove.get() && e.getEnd() > endValueToRemove.get()) {
                endValueToRemove.set(e.getEnd());
                return false;
            } else return true;
        }).collect(Collectors.toList());
    };
}
