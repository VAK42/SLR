import weakref
import sys
import gc

# sys.getrefcount(): Total References To Object (+1 Temporary Reference Created By getrefcount Itself)
refList = [1, 2, 3]
print(sys.getrefcount(refList)) # 2
alias = refList
print(sys.getrefcount(refList)) # 3
container = [refList, refList]
print(sys.getrefcount(refList)) # 5
del alias
del container
print(sys.getrefcount(refList)) # 2
# Circular References: Xóa Var Bên Ngoài Nhưng 2 Objs Trong RAM Vẫn Trỏ Lẫn Nhau (Ref Count Always > 0)
class Node:
  def __init__(self, val):
    self.val = val
    self.next = None
    self.prev = None
nodeA, nodeB = Node('A'), Node('B')
nodeA.next, nodeB.next = nodeB, nodeA
del nodeA, nodeB

# Generational Garbage Collector (gc Module): Trace & Collect Cyclic Objs Across Generations (Gen0, Gen1, Gen2)
print(gc.isenabled())
print(gc.get_threshold())
print(gc.get_count())    # True
collected = gc.collect() # Trigger Full Cyclic GC Collection
print(collected)         # 2
gc.collect(0)            # Collect Gen0
gc.collect(1)            # Collect Gen1
gc.disable()
print(gc.isenabled())    # False
gc.enable()
print(gc.isenabled())    # True

# weakref.ref(): Access Object Without Increasing Reference Count
# WeakValueDictionary: Cache Auto-Clears Entries When Strong References Are Deleted
class Resource:
  def __init__(self, resId): self.resId = resId
  def __repr__(self): return f'Resource({self.resId})'
res = Resource(42)
weakRef = weakref.ref(res)
print(weakRef(), weakRef() is res) # Resource(42) True
del res
gc.collect()
print(weakRef()) # None
cache = weakref.WeakValueDictionary()
objA, objB = Resource(1), Resource(2)
cache['A'], cache['B'] = objA, objB
print(len(cache), cache.get('A')) # 2 Resource(1)
del objA
gc.collect()
print(cache.get('A'), cache.get('B')) # None Resource(2)

# Memory Optimization (__slots__): Replace Dynamic Instance __dict__ With Fixed C-Level Attribute Array To Save RAM
class NormalPoint:
  def __init__(self, x, y, z): self.x, self.y, self.z = x, y, z
class SlottedPoint:
  __slots__ = ['x', 'y', 'z']
  def __init__(self, x, y, z): self.x, self.y, self.z = x, y, z
normal, slotted = NormalPoint(1.0, 2.0, 4.0), SlottedPoint(1.0, 2.0, 4.0)
print(sys.getsizeof(normal), sys.getsizeof(slotted))
print(hasattr(normal, '__dict__'), hasattr(slotted, '__dict__')) # True False
normal.extra = 'Allowed'
print(normal.extra)
try:
  slotted.extra = 'Not Allowed'
except AttributeError as e:
  print(type(e))
n = 100000
normals = [NormalPoint(float(i), float(i), float(i)) for i in range(n)]
slotteds = [SlottedPoint(float(i), float(i), float(i)) for i in range(n)]
normalTotal = sum(sys.getsizeof(o) for o in normals)
slottedTotal = sum(sys.getsizeof(o) for o in slotteds)
print(normalTotal, slottedTotal, normalTotal - slottedTotal)