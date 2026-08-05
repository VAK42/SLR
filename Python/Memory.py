import sys
import copy

# Variables: Labels Bound To Objs In Heap
# id() → Retrieve Memory Address
# is → Compare Memory Address
# == → Compare Value
a = [1, 2]
b = a
b.append(4)
print(id(a) == id(b), a is b) # True True

lA, lB = [1, 2, 3], [1, 2, 3]
print(lA == lB, lA is lB)     # True False

# Small Integers (-5 To 256) & Identifier Strings: Pre-allocated/Cached In RAM
sA, sB = 99 + 1, 99 + 1
lA, lB = 999 + 1, 999 + 1
print(sA is sB, lA is lB)         # True False
strA, strB = 'K42', 'K42'
strX, strY = 'VAK ' + '42', 'VAK 42'
print(strA is strB, strX is strY) # True False

# Shallow Copy: Clone Outer Layer Only → Share Inner Nested Objs
# Deep Copy: Clone All Layers → Create Independent Copies Of Inner Objs
original = [1, [2, 0], 4]
shallow = original.copy()
deep = copy.deepcopy(original)
original[0] = 99
original[1].append(99)
print(original, shallow, deep)

# Reference Counting: Track Total Pointers To Obj (+1 Temporary Reference Created By getrefcount Itself)
refList = [1, 2, 4]
print(sys.getrefcount(refList)) # 2
alias = refList
print(sys.getrefcount(refList)) # 3
container = [refList, refList]
print(sys.getrefcount(refList)) # 5
del alias
del container
print(sys.getrefcount(refList)) # 2