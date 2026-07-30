import time
import sys
from collections import Counter, defaultdict, deque, namedtuple

# List Performance & Operations
# append() -> O(1) Amortized | insert(0) -> O(N) Shifting Elements | Slicing -> O(K)
lst = []
sAppend = time.perf_counter()
for i in range(100_000): lst.append(i)
appendTime = time.perf_counter() - sAppend
sInsert = time.perf_counter()
for i in range(1000): lst.insert(0, i)
insertTime = time.perf_counter() - sInsert
print(appendTime, insertTime)
sArr = lst[10:20]
print(len(sArr)) # 10
nums = [8, 1, 4, 1, 5, 9, 2, 6]
print(sorted(nums), list(reversed(nums)), min(nums), max(nums)) # [1, 1, 2, 4, 5, 6, 8, 9] [6, 2, 9, 5, 1, 4, 1, 8] 1 9

# Tuple: Save Memory Compared To List (Fixed Size Array)
tup = (10, 20, 30, 40)
lSame = [10, 20, 30, 40]
print(sys.getsizeof(tup), sys.getsizeof(lSame)) # Tuple Size < List Size
head, *middle, tail = (1, 2, 0, 4, 5)
print(head, middle, tail) # 1 [2, 0, 4] 5
pt = 10, 20
x, y = pt
print(pt, x, y) # (10, 20) 10 20

# Dict & Set
dct= {'a': 1, 'b': 2, 'c': 4}
dct['a'] = 99
print(dct['a'], 'b' in dct, dct.get('d', 'OK')) # 99 True OK
# .get(key, value): Nếu Ko Có key -> Trả value
dA, dB = {'x': 1}, {'y': 2}
merged = {**dA, **dB}
print(merged) # {'x': 1, 'y': 2}
sA, sB = {1, 2, 0, 4}, {0, 4, 5, 6}
# Union (|) + Intersection (&) + Difference (-) + Symmetric Difference (^)
print(sA | sB, sA & sB, sA - sB, sA ^ sB) # {0, 1, 2, 4, 5, 6} {0, 4} {1, 2} {1, 2, 5, 6}
unique = set([1, 1, 2, 2, 0, 0, 4])
print(unique) # {0, 1, 2, 4}
frozen = frozenset([1, 2, 0]) # Immutable Set
dctFrozen = {frozen: 'FrozenValue'}
print(dctFrozen[frozen]) # FrozenValue

# Collections Module
# Counter: Frequency Count Map
words = ['a', 'b', 'a', 'c', 'b', 'a']
counter = Counter(words)
print(counter, counter.most_common(2)) # Counter({'a': 3, 'b': 2, 'c': 1}) [('a', 3), ('b', 2)]
counterB = Counter(['a', 'd'])
print(counter + counterB)              # Counter({'a': 4, 'b': 2, 'c': 1, 'd': 1})

# defaultdict: Auto Initialize Default Values For Missing Keys -> Prevent KeyError
graph = defaultdict(list)
graph['nodeA'].append('nodeB')
print(graph['nodeA'], graph['nodeZ']) # ['nodeB'] []
wordCount = defaultdict(int)
for w in words: wordCount[w] += 1
print(dict(wordCount)) # {'a': 3, 'b': 2, 'c': 1}

# deque: Queue FIFO & Stack LIFO + maxlen Auto Eviction
q = deque(maxlen=3)
for i in range(1, 5): q.append(i)
print(q)              # deque([2, 3, 4], maxlen=3)
stack = deque()
stack.append('a'); stack.append('b')
print(stack.pop())    # b
fifo = deque()
fifo.append('a'); fifo.append('b')
print(fifo.popleft()) # a

# namedtuple: Lightweight Tuple With Named Attributes
PT = namedtuple('PT', ['x', 'y'])
p = PT(10, 20)
print(p, p.x, p.y, p[0]) # PT(x=10, y=20) 10 20 10
Emp = namedtuple('Emp', ['name', 'dept', 'salary'])
Emp.__new__.__defaults__ = ('IT', 50000)
emp = Emp('Alice')
print(emp) # Emp(name='Alice', dept='IT', salary=50000)