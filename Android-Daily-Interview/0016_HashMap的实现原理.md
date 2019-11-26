# 什么是HashMap？

HashMap是一个用于存储Key-Value的集合，每个键值对也叫做Entry。这些**键值对分散存储在一个数组中**，这个数组就是HashMap的主干。

HashMap数组每个元素的初始值都是Null：

![](./pics/Java_HashMap_%E5%88%9D%E5%A7%8B%E5%80%BC.png)

我们最常使用的 2 个方法：public V get(Object key) 和 public V put(K key, V value)。

# put()的原理

比如调用hashMap.put("apple",0)，也就是在HashMap中插入键值对apple-0，实际执行步骤如下：

```
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
```

如上所述，使用key值调用hash()方法，计算出对应的hash值。

> Java源代码中如何计算Hash值的？

```
/**
 * Computes key.hashCode() and spreads (XORs) higher bits of hash
 * to lower.  Because the table uses power-of-two masking, sets of
 * hashes that vary only in bits above the current mask will
 * always collide. (Among known examples are sets of Float keys
 * holding consecutive whole numbers in small tables.)  So we
 * apply a transform that spreads the impact of higher bits
 * downward. There is a tradeoff between speed, utility, and
 * quality of bit-spreading. Because many common sets of hashes
 * are already reasonably distributed (so don't benefit from
 * spreading), and because we use trees to handle large sets of
 * collisions in bins, we just XOR some shifted bits in the
 * cheapest possible way to reduce systematic lossage, as well as
 * to incorporate impact of the highest bits that would otherwise
 * never be used in index calculations because of table bounds.
 */
static final int hash(Object key) {
	int h;
	return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

假定最后计算出的index是2，那么结果如下：

![](./pics/Java_HashMap_%E8%AE%A1%E7%AE%97index%E5%80%BC.png)

但是，因为HashMap的长度是有限的，当插入的Entry越来越多时，再完美的Hash函数也难免会出现index冲突的情况。比如下面这样：

![](./pics/Java_HashMap_%E8%AE%A1%E7%AE%97index%E9%87%8D%E5%A4%8D.png)

这时候该怎么办呢？我们可以利用**链表**来解决。HashMap数组的每一个元素不止是一个Entry对象，也是一个链表的头节点。每一个Entry对象通过Next指针指向它的下一个Entry节点。当新来的Entry映射到冲突的数组位置时，只需要插入到对应的链表即可：

![](./pics/Java_HashMap_%E8%8A%82%E7%82%B9%E9%87%8D%E5%A4%8D%E6%97%B6%E4%BD%BF%E7%94%A8%E9%93%BE%E8%A1%A8.png)

需要注意的是，新来的Entry节点插入链表时，使用的是“头插法”。

# get()的原理

使用Get方法根据Key来查找Value的时候，发生了什么呢？首先会把输入的Key做一次Hash映射，得到对应的index：

```
index =  Hash("apple");
```

由于刚才所说的Hash冲突，同一个位置有可能匹配到多个Entry，这时候就需要顺着对应链表的头节点，一个一个向下来查找。假设我们要查找的Key是“apple”：

![](./pics/Java_HashMap_%E6%9F%A5%E6%89%BE%E8%8A%82%E7%82%B9.png)

第一步，我们查看的是头节点Entry6，Entry6的Key是banana，显然不是我们要找的结果。

第二步，我们查看的是Next节点Entry1，Entry1的Key是apple，正是我们要找的结果。

之所以把Entry6放在头节点，是因为HashMap的发明者认为，**后插入的Entry被查找的可能性更大**。

# 疑问

## 为什么初始长度是16？

在Java源代码中有如下代码内容：

```
/**
 * The default initial capacity - MUST be a power of two.
 */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
```

## HashMap 实体类分析

HashMap也是我们使用非常多的Collection，它是**基于哈希表的Map接口**的实现，以**key-value**的形式存在。在HashMap中，key-value总是会当做一个整体来处理，系统会根据hash算法来计算key-value的存储位置，我们总是可以通过key快速地存、取value。

* **数据结构及存储**相关的知识点

  数组存储区间是连续的，占用内存严重，故空间复杂度很大。但数组的二分查找时间复杂度小，为O(1)；寻址容易，插入和删除困难；
  
  链表存储区间离散，占用内存比较宽松，故空间复杂度很小，但时间复杂度很大，达O（N）。寻址困难，插入和删除容易。
  
  那么我们能不能综合两者的特性，做出一种寻址容易，插入删除也容易的数据结构？答案是肯定的，这就是我们要提起的哈希表。哈希表（(Hash table）既满足了数据的查找方便，同时不占用太多的内容空间，使用也十分方便。

* HashMap的数据存储实现

    哈希表有多种不同的实现方法，以下是最常用的一种方法—— 拉链法，我们可以理解为“链表的数组” ：
    
    ![image](D:\NotePics\AndroidStudio\DataStructure\HashMap_data.png)
    
    **哈希表是由数组+链表组成的**。在一个长度为16的数组中，**每个元素存储的是一个链表的头结点**。那么这些元素是按照什么样的规则存储到数组中呢？一般情况是通过hash(key)%len获得，也就是元素的key的哈希值对数组长度取模得到。比如上述哈希表中，12%16=12,28%16=12,108%16=12,140%16=12。所以12、28、108以及140都存储在数组下标为12的位置。

    但是实际上Android中的HashMap初始化长度为4，而不是16。
    
    HashMap其实也是一个线性的数组实现的,所以可以理解为其存储数据的容器就是一个线性数组。
* 
---
疑问和参考：

1. modCount字段有什么作用？
2. 

[HashMap的实现与优化](http://www.importnew.com/21294.html)
[Android Hashmap源码解析](http://blog.csdn.net/abcdef314159/article/details/51165630)
---

### HashMap 源代码结构

HashMap的定义：

```
public class HashMap<K, V> extends AbstractMap<K, V> implements Cloneable, Serializable {
```

可见：HashMap继承自AbstractMap（抽象类），而AbstractMap类提供Map接口（Map接口定义了键映射到值的规则）的骨干实现，以最大限度地减少实现此接口所需的工作。

HashMap 实体类的结构示意图：

![image](D:\NotePics\AndroidStudio\DataStructure\HashMap_structure.png)

HashMap的继承体系：

![image](D:\NotePics\AndroidStudio\DataStructure\HashMap_class_diagram.png)

### HashMapEntry

```
    static class HashMapEntry<K, V> implements Entry<K, V> {
        final K key;
        V value;
        final int hash;
        HashMapEntry<K, V> next;
```
HashMapEntry是HashMap中定义的静态内部类，实现Map.Entry接口，专用于HashMap数据结构（Map.Entry实际上就是mapping）。

HashMapEntry类中定义了4个字段值：

    final K key;
    V value;
    final int hash;
    HashMapEntry<K, V> next;

定义的key、value值都是为了覆写Map.Entry中的方法，并在方法中返回对应的值：key和value。其中next指向的是下一个HashMapEntry条目。

### HashIterator

HashIterator类似于HashMapEntry的迭代器，是HashMap中定义的抽象类。

```
    private abstract class HashIterator {
        int nextIndex;
        HashMapEntry<K, V> nextEntry = entryForNullKey;
        HashMapEntry<K, V> lastEntryReturned;
        int expectedModCount = modCount;
        ...
```

HashIterator是抽象类，但是其中定义了Iterator接口相关的2个方法：remove()和hasNext()。

### KeyIterator/ValueIterator/EntryIterator

KeyIterator/ValueIterator/EntryIterator类似于HashMap中特定的迭代器，KeyIterator为键值的迭代器，ValueIterator为键对应的值的迭代器，EntryIterator为条目的迭代器。

从类和接口的维度来看，KeyIterator/ValueIterator/EntryIterator都是继承自HashIterator，并实现Iterator接口。也就必须实现Iterator中的方法。

```
    private final class KeyIterator extends HashIterator
            implements Iterator<K> {
        public K next() { return nextEntry().key; }
    }

    private final class ValueIterator extends HashIterator
            implements Iterator<V> {
        public V next() { return nextEntry().value; }
    }

    private final class EntryIterator extends HashIterator
            implements Iterator<Entry<K, V>> {
        public Entry<K, V> next() { return nextEntry(); }
    }
```

KeyIterator对应的实现next()：

```
public K next() { return nextEntry().key; }
```

另外两个类和KeyIterator类似。

### KeySet/EntrySet/Values

KeySet/EntrySet/Values是HashMap中定义的私有实体类，表示的是Key/Entry/Values的集合；也就是将HashMap中的键、值以及mapping的值取出，组成的集合。

KeySet/EntrySet都是继承自AbstractSet；Values继承自AbstractCollection。

```
private final class KeySet extends AbstractSet<K> {
    public Iterator<K> iterator() {
        return newKeyIterator();
    }
    public int size() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public boolean contains(Object o) {
        return containsKey(o);
    }
    public boolean remove(Object o) {
        int oldSize = size;
        HashMap.this.remove(o);
        return size != oldSize;
    }
    public void clear() {
        HashMap.this.clear();
    }
}
```
EntrySet和Values的集合定义类似。

### HashMap 源代码分析

* HashMap中定义的字段内容值如下：

```
    /**
     * Min capacity (other than zero) for a HashMap. Must be a power of two
     * greater than 1 (and less than 1 << 30). 初始化容量为4
     */
    private static final int MINIMUM_CAPACITY = 4;

    /**
     * Max capacity for a HashMap. Must be a power of two >= MINIMUM_CAPACITY. 最大容量为1GB--> XXX字节
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * An empty table shared by all zero-capacity maps (typically from default
     * constructor). It is never written to, and replaced on first put. Its size
     * is set to half the minimum, so that the first resize will create a
     * minimum-sized table. 空Entry[]条目数组，容量为2
     */
    private static final Entry[] EMPTY_TABLE
            = new HashMapEntry[MINIMUM_CAPACITY >>> 1];

    /**
     * The default load factor. Note that this implementation ignores the
     * load factor, but cannot do away with it entirely because it's
     * mentioned in the API.
     *
     * <p>Note that this constant has no impact on the behavior of the program,
     * but it is emitted as part of the serialized form. The load factor of
     * .75 is hardwired into the program, which uses cheap shifts in place of
     * expensive division. 默认容量因子为0.75f
     */
    static final float DEFAULT_LOAD_FACTOR = .75F;

    /**
     * The hash table. If this hash map contains a mapping for null, it is
     * not represented this hash table. 哈希表，HashMapEntry<K,V>[]
     */
    transient HashMapEntry<K, V>[] table;

    /**
     * The entry representing the null key, or null if there's no such mapping. 为null或者不存在的mapping，准备的HashMapEntry实例
     */
    transient HashMapEntry<K, V> entryForNullKey;

    /**
     * The number of mappings in this hash map. HashMap的容量大小
     */
    transient int size;

    /**
     * Incremented by "structural modifications" to allow (best effort)
     * detection of concurrent modification. HashMap中key-value中value被改变的次数
     */
    transient int modCount;

    /**
     * The table is rehashed when its size exceeds this threshold.
     * The value of this field is generally .75 * capacity, except when
     * the capacity is zero, as described in the EMPTY_TABLE declaration
     * above. 容量阈值，超过这个阈值则根据默认容量因子增加HashMap容量
     */
    private transient int threshold;

    // Views - lazily initialized
    private transient Set<K> keySet; key集合
    private transient Set<Entry<K, V>> entrySet; Entry集合 条目集合
    private transient Collection<V> values; value的集合
```

* HashMap中定义的public方法如下：

    * get(Object key)，获取到key对应的value值。

            /**
             * Returns the value to which the specified key is mapped,
             * or {@code null} if this map contains no mapping for the key.
             *
             * <p>More formally, if this map contains a mapping from a key
             * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
             * key.equals(k))}, then this method returns {@code v}; otherwise
             * it returns {@code null}.  (There can be at most one such mapping.)
             *
             * <p>A return value of {@code null} does not <i>necessarily</i>
             * indicate that the map contains no mapping for the key; it's also
             * possible that the map explicitly maps the key to {@code null}.
             * The {@link #containsKey containsKey} operation may be used to
             * distinguish these two cases.
             *
             * @see #put(Object, Object)
             */
            public V get(Object key) {
                if (key == null)
                    return getForNullKey();
                Entry<K,V> entry = getEntry(key);
            
                return null == entry ? null : entry.getValue();
            }
            
            /**
             * Returns the entry associated with the specified key in the
             * HashMap.  Returns null if the HashMap contains no mapping
             * for the key.
             */
            final Entry<K,V> getEntry(Object key) {
                if (size == 0) {
                    return null;
                }
            
                int hash = (key == null) ? 0 : sun.misc.Hashing.singleWordWangJenkinsHash(key);
                for (HashMapEntry<K,V> e = table[indexFor(hash, table.length)];
                     e != null;
                     e = e.next) {
                    Object k;
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                }
                return null;
            }

    * put(K key, V value)，将key-value放入HashTable的指定位置。

            /**
             * Associates the specified value with the specified key in this map.
             * If the map previously contained a mapping for the key, the old
             * value is replaced.
             *
             * @param key key with which the specified value is to be associated
             * @param value value to be associated with the specified key
             * @return the previous value associated with <tt>key</tt>, or
             *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
             *         (A <tt>null</tt> return can also indicate that the map
             *         previously associated <tt>null</tt> with <tt>key</tt>.)
             */
            public V put(K key, V value) {
                if (table == EMPTY_TABLE) {
                    inflateTable(threshold);
                }
                if (key == null)
                    return putForNullKey(value);
                int hash = sun.misc.Hashing.singleWordWangJenkinsHash(key);
                int i = indexFor(hash, table.length);
                for (HashMapEntry<K,V> e = table[i]; e != null; e = e.next) {
                    Object k;
                    if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                        V oldValue = e.value;
                        e.value = value;
                        e.recordAccess(this);
                        return oldValue;
                    }
                }
            
                modCount++;
                addEntry(hash, key, value, i);
                return null;
            }
            
            /**
             * Adds a new entry with the specified key, value and hash code to
             * the specified bucket.  It is the responsibility of this
             * method to resize the table if appropriate.
             *
             * Subclass overrides this to alter the behavior of put method.
             */
            void addEntry(int hash, K key, V value, int bucketIndex) {
                if ((size >= threshold) && (null != table[bucketIndex])) {
                    resize(2 * table.length);
                    hash = (null != key) ? sun.misc.Hashing.singleWordWangJenkinsHash(key) : 0;
                    bucketIndex = indexFor(hash, table.length);
                }
            
                createEntry(hash, key, value, bucketIndex);
            }
            
            /**
             * Like addEntry except that this version is used when creating entries
             * as part of Map construction or "pseudo-construction" (cloning,
             * deserialization).  This version needn't worry about resizing the table.
             *
             * Subclass overrides this to alter the behavior of HashMap(Map),
             * clone, and readObject.
             */
            void createEntry(int hash, K key, V value, int bucketIndex) {
                HashMapEntry<K,V> e = table[bucketIndex];
                table[bucketIndex] = new HashMapEntry<>(hash, key, value, e);
                size++;
            }

        与此相关的是：new HashMapEntry<>(hash, key, value, e)构造方法：

            /**
             * Creates new entry.
             */
            HashMapEntry(int h, K k, V v, HashMapEntry<K,V> n) {
                value = v;
                next = n;
                key = k;
                hash = h;
            }
        
        参数n传入后，会赋值给next，也就是：新创建的HashMapEntry将位于节点的首位置。

### Java中HashMap的实现

* 对比Android和Java中相关方法的实现

```
    /**
     * Implements Map.put and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict if false, the table is in creation mode.
     * @return previous value, or null if none
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```

大致可得出的结论是：Java项目中的HashMap实现和Android项目中的HashMap实现是不一样的。可能的原因是：Android会照顾到设备的CPU、内存和耗电量等因素。

总结：

- HashMap可以接受null键和null值，而HashTable则不能，HashMap是非synchronized的；存储的是键值对。

- HashMap是基于hashing原理,使用put(key,value)存储对象到HashMap中，使用get(key)从HashMap中获取对象，当我们给put方法传递键和值时，我们先对键调用hashCode()方法，返回的hashCode用于找到bucket位置来存储键对象和值对象，作为Map.Entry.

- 如果两个对象hashCode相同：

  存储时：他们会找到相同的bucket位置，发生碰撞，因为HashMap使用链表存储对象（每个Map.Entry都有一个next指针），这个Entry会存储在链表中。

  获取时:会用hashCode找到bucket位置，然后调用key.equals()方法找到链表中正确的节点.最终找到要找的值对象.

  减少碰撞：使用final修饰的对象、或不可变的对象作为键，使用(Integer、String)（是不可变、final的,而且已经重写了equals和hashCode方法）这样的wrapper类作为键是非常好的，（我们可以使用自定义的对象作为键吗？答：当然可以，只要它遵守了equals和hashCode方法定义规则，并且当对象插入到Map中之后将不会再改变。）

- HashMap负载因子默认是0.75，可设置，当map填满了75%的bucket时候，将会创建原来HashMap大小两倍的bucket数组，来重新调整map的大小，并将原来的对象放入新的bucket数组中,这个过程叫做rehashing，因为它调用hash方法找到新的bucket位置。

- 重新调整map大小可能会发生竞争问题：如果两个线程都发现HashMap需要调整大小了，它们都会尝试进行调整，在调整中，存储在链表中的元素的次序会反过来，因为移动bucket位置的时候，HashMap并不会将元素放在链表的尾部，而是放在头部，这是为了避免尾部遍历，如果条件竞争发生了，就死循环了。