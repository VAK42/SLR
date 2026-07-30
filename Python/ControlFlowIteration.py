import sys
# Pattern Matching & Loop Else
val = 100
lbl = 'Large' if val > 50 else 'Small' # Ternary Operator
def checkType(obj):
  match obj:
    case int(): return f'I: {obj}'
    case str(): return f'S: {obj}'
    case list(): return f'L: {len(obj)}'
    case None: return 'None'
    case _: return f'Unknown: {type(obj).__name__}'
print(checkType(42), checkType('a'), checkType([1, 2]), checkType(None)) # I: 42 - S: a - L: 2 - None
def findPrime(n):
  for d in range(2, n):
    if n % d == 0: break
  else:
    return True # Runs Only If Loop Completes Naturally Without break
  return False
print(findPrime(7), findPrime(9)) # True False

# Iterator Protocol: Iterator (SingleUse) vs Iterable (Reusable) -> Batch Processing / Streaming
# Iterator: 
# __iter__() -> self
# __next__() -> Value | StopIteration
class Countdown:
  def __init__(self, start):
    self.curr = start
  def __iter__(self):
    return self
  def __next__(self):
    if self.curr <= 0: raise StopIteration
    val = self.curr
    self.curr -= 1
    return val
print(list(Countdown(2))) # [2, 1] (Pointer: Reach 0 & Exhaust)
it = iter([10, 20])
print(next(it), next(it)) # 10 20
# Iterable:
# __iter__() -> Fresh Iterator Each Call + No __next__ Needed
class ReusableRange:
  def __init__(self, stop):
    self.stop = stop
  def __iter__(self):
    return iter(range(self.stop)) # -> New Iterator Every Call
r = ReusableRange(3)
print(list(r), list(r)) # [0, 1, 2] [0, 1, 2] (Reusable Multiple Times)

# Generators
def fibonacci():
  a, b = 0, 1
  while True:
    yield a
    a, b = b, a + b
fibGen = fibonacci()
print([next(fibGen) for _ in range(5)]) # [0, 1, 1, 2, 3]
lComp = [x ** 2 for x in range(1000)]
gExp = (x ** 2 for x in range(1000))
print(sys.getsizeof(lComp) > sys.getsizeof(gExp)) # True (~8856 bytes vs ~200 bytes)
def chain(*iterables):
  for it in iterables:
    yield from it # Delegate To Sub-Generator / Iterable
print(list(chain([1, 2], [3, 4]))) # [1, 2, 3, 4]
def accumulator():
  tot = 0
  while True:
    val = yield tot
    if val is None: break
    tot += val
acc = accumulator()
next(acc) # Prime Generator To 1st yield
print(acc.send(10), acc.send(20)) # 10 30

# Comprehensions (List, Dict, Set, Generator Expression)
nums = [1, 2, 3, 4, 5]
lRes = [n ** 2 for n in nums if n % 2 == 0]
dRes = {n: n ** 2 for n in nums[:3]}
sRes = {n % 3 for n in nums}
gRes = sum(n ** 2 for n in nums)
print(lRes, dRes, sRes, gRes) # [4, 16] {1: 1, 2: 4, 3: 9} {0, 1, 2} 55