import functools
import operator
import time
from functools import reduce

# Parameter Modes
# / -> Must Pass By Position + No Keyword Name
# * -> Must Pass By Keyword Name + Can Reorder
# Between / And *: Free To Use Keyword Name Or Not + Free To Reorder
# *args -> Collect Extra Positional Args Into Tuple
# **kwargs -> Collect Extra Keyword Args Into Dict
def registrar(userId, /, name, age, *, role, dept='Engineering'):
  return userId, name, age, role, dept
print(registrar(1004, 42, 'VAK', role='Senior'))
print(registrar(1002, name='Bob', age=25, role='Junior', dept='Design'))
def logger(*messages, **metadata):
  return messages, metadata
print(logger('OK', 'LoadConfig', env='Prod', version='4.0'))

# Scope LEGB Rule: Local -> Enclosing -> Global -> Built-in
# nonlocal: Mod Enclosing Scope Var
# global: Mod Module-Level Var
counter = 0
def outer():
  outerVal = 'Outer'
  def inner():
    nonlocal outerVal
    global counter
    outerVal = 'Inner'
    counter += 1
  inner()
  return outerVal
print(outer()) # Inner
print(counter) # 1

# Closures
def makeAdder(amount):
  def adder(val): return val + amount
  return adder
addFive, addTen = makeAdder(5), makeAdder(10)
print(addFive(4), addTen(4)) # 9 14
print(addFive.__closure__[0].cell_contents) # 5
# Late Binding: Loop Finished Before Call -> All Lambdas Retrieve Final i
# -> Solution: Store Value To Unique Arg Per Function
funcsBad = [lambda x: x + i for i in range(3)]
print(funcsBad[0](10), funcsBad[1](10)) # 12 12
funcsGood = [lambda x, capture=i: x + capture for i in range(3)]
print(funcsGood[0](10), funcsGood[1](10), funcsGood[2](10)) # 10 11 12

# Decorators: Take A Function & Return A Wrapper Adding Pre/Post Logic Without Modifying Original Code
# Parameterized Decorators (@functools.wraps): Preserve Original Function Metadata (__name__, Docstrings)
def timer(func):
  @functools.wraps(func)
  def wrapper(*args, **kwargs):
    start = time.perf_counter()
    res = func(*args, **kwargs)
    print(time.perf_counter() - start)
    return res
  return wrapper
def retry(maxAttempts):
  def decorator(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
      for attempt in range(maxAttempts):
        try:
          return func(*args, **kwargs)
        except Exception as e:
          if attempt == maxAttempts - 1: raise
    return wrapper
  return decorator
@timer
@retry(3)
def fetch(url): return f'From:{url}'
print(fetch('https://backend.com'))
print(fetch.__name__)
# timer -> retry -> fetch -> retry -> timer

# Functional Tools (map, filter, reduce, partial, itemgetter, Comprehensions)
nums = [1, 2, 3, 4, 5]
squared = list(map(lambda n: n ** 2, nums))
evens = list(filter(lambda n: n % 2 == 0, nums))
total = reduce(lambda acc, n: acc + n, nums)
print(squared, evens, total)
squaredComp = [n ** 2 for n in nums]
wordLengths = {w: len(w) for w in ['alpha', 'beta', 'gamma']}
uniqueSquares = {n ** 2 for n in nums}
print(squaredComp, wordLengths, uniqueSquares)
def power(base, exp): return base ** exp
square = functools.partial(power, exp=2)
cube = functools.partial(power, exp=3)
print(square(5), cube(3))
data = [('VAK', 42), ('V', 2), ('K', 4)]
byAge = sorted(data, key=operator.itemgetter(1))
print(byAge)