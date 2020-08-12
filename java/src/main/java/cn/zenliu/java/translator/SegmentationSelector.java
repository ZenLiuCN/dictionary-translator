package cn.zenliu.java.translator;

import org.ahocorasick.trie.Emit;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface SegmentationSelector {
    List<Emit> select(Collection<Emit> emits);
}
