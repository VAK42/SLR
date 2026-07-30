from fractions import Fraction
from decimal import Decimal

# Built-in Data Types
a: int = 10 ** 100           # Arbitrary Precision
b: float = 3.14              # IEEE 754 64-bit Float
c: bool = True               # int Subclass (True == 1)
d: str = 'Python'            # Immutable Unicode Text
e: bytes = d.encode('utf-8') # Immutable Byte Sequence
f: tuple = (1, 2, 3)         # Immutable Ordered Sequence
g: list = [1, 2, 3]          # Mutable Ordered Sequence
h: set = {1, 2, 3}           # Mutable Unique Elements
i: dict = {'name': 'VAK'}    # Key-Value Mapping

# Float - Decimal - Fraction
print(0.1 + 0.2) # 0.30000000000000004
print(Decimal('0.1') + Decimal('0.2')) # 0.3
print(Fraction(1, 3) + Fraction(1, 6)) # 1/2

# Immutable
i = 'VAK'
prev = id(i)
i += ' 42'
print(prev != id(i)) # True

# Mutable
m = [1, 2]
prev = id(m)
m.append(3)
print(prev != id(m)) # False

# Key (Dict) | Element (Set) -> Hashable Required
# Hashable: Immutable (int, float, str, bytes, tuple) - tuple (Hashable Elements)
# Unhashable: Mutable (list, dict, set, tuple) - tuple (Unhashable Elements)
u = (4, 2)
v = {u: 'VAK'}
print(v[u]) #VAK

# Common Trap
def appendBad(item, lst=[]):
  lst.append(item)
  return lst
def appendSafe(item, lst=None):
  if lst is None:
    lst = []
  lst.append(item)
  return lst
print(appendBad('A'))  # ['A']
print(appendBad('B'))  # ['A', 'B']
print(appendSafe('A')) # ['A']
print(appendSafe('B')) # ['B']