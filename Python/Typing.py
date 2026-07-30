from typing import List, Dict, Tuple, Optional, Union, Any, Callable
# Dynamic Typing: Stack (Reference) -> Heap (Type + Value)
x = 42
print(type(x).__name__)
x = 'VAK'
print(type(x).__name__)

# Strong Typing
try:
  result = 'VAK ' + 42
except TypeError as e:
  print(e)
valid = 'VAK ' + str(42)
print(valid)

# Duck Typing
class Duck:
  def quack(self): return 'Quack Quack!'
  def fly(self): return 'Flap Flap!'
class Person:
  def quack(self): return 'Duck Duck!'
  def fly(self): return 'Fly Fly!'
class Car:
  def drive(self): return 'Vroom Vroom!'
def duck(thing):
  print(thing.quack())
  print(thing.fly())
duck(Duck())
duck(Person())
try:
  duck(Car())
except AttributeError as e:
  print(e)

# Type Hints
def greet(name: str, times: int = 1) -> str:
  return name * times
# Optional[str] ≡ Union[str, None] ≡ str | None
def findUser(userId: int) -> Optional[str]:
  if userId == 1:
    return 'VAK'
  return None
def processItems(items: List[Union[int, str]]) -> List[str]:
  return [str(item) for item in items]
def validate(val: int | str | None) -> str:
  return str(val) if val is not None else 'None'

# Runtime Type Checks
values = [42, 'VAK', 3.14, True, [1, 2], None]
for val in values:
  print(f'{repr(val):6} | Type == int? : {str(type(val) == int):5} | Instance Of int? : {isinstance(val, int)}')