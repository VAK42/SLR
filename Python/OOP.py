from typing import Protocol, runtime_checkable
from abc import ABC, abstractmethod
import math

# Encapsulation
# private -> __var | protected -> _var | public -> var | Getter -> @property | Setter -> @var.setter
# Name Mangling: Prevents Subclass From Overriding Parent Private Attributes During Inheritance
class BankAccount:
  def __init__(self, owner, balance):
    self.owner = owner       # public String owner
    self.__balance = balance # private double balance
  @property
  def balance(self):         # public double getBalance()
    return self.__balance
  @balance.setter
  def balance(self, val):    # public void setBalance(double val)
    if val >= 0: self.__balance = val
  def deposit(self, amount): # public void deposit(double amount)
    if amount > 0:
      self.__balance += amount
acc = BankAccount('VAK', 1000)
acc.deposit(500)
acc.balance = 2000                 # acc.setBalance(2000)
print(acc.balance)                 # acc.getBalance()
print(acc._BankAccount__balance)   # Private Access -> Java: Reflection

# Inheritance & Polymorphism
# class Child extends Parent -> class Child(Parent)
# super() -> super().__init__()
class Shape:
  def __init__(self, color): self.color = color
  def area(self): raise NotImplementedError
class Circle(Shape):        # class Circle extends Shape
  def __init__(self, color, r):
    super().__init__(color) # super(color);
    self.r = r
  def area(self): return math.pi * self.r ** 2
class Dog:
  def speak(self): return 'Gow Gow'
class Cat:
  def speak(self): return 'Meow Meow'
# Java: Interface Animal {void speak()} -> Implements Animal
# Python: Duck Typing
for animal in [Dog(), Cat()]:
  print(animal.speak())

# Static
class Database:
  count = 0              # public static int count = 0;
  def __init__(self, host):
    self.host = host
    Database.count += 1
  @classmethod           # Static Factory Method
  def fromUrl(cls, url):
    return cls(url.split('://')[1])
  @staticmethod          # public static boolean validatePort(int port)
  def validatePort(port):
    return 1 <= port <= 65535
db = Database.fromUrl('db://localhost')
print(f'{db.host} - {Database.count} - {Database.validatePort(5432)}')

# Multiple Inheritance & MRO (C3 Linearization)
# Java: Multiple Inheritance Not Supported
# Python: Multiple Inheritance Supported Through MRO Algorithm (C3 Linearization) & super() Cooperative
class Logger:
  def log(self): return 'Logger' + super().log()
class Serializer:
  def log(self): return 'Serializer' + super().log()
class BaseService:
  def log(self): return 'Base'
class Service(Logger, Serializer, BaseService): pass
print([c.__name__ for c in Service.__mro__])
print(Service().log())

# Dunder Methods
class Vector:
  def __init__(self, x, y): self.x, self.y = x, y
  def __repr__(self): return f'Vector({self.x}, {self.y})'        # public String toString()
  def __add__(self, o): return Vector(self.x + o.x, self.y + o.y) # vec1.add(vec2)
  def __mul__(self, s): return Vector(self.x * s, self.y * s)
  def __len__(self): return 2                                     # list.size()
  def __eq__(self, o): return self.x == o.x and self.y == o.y     # public boolean equals(Object o)
  def __call__(self, fn): return fn(self.x, self.y)               # Function.apply(x, y)
  def __abs__(self): return math.hypot(self.x, self.y)
v1, v2 = Vector(1, 2), Vector(3, 4)
print(f'Add: {v1 + v2} | Mul: {v1 * 3} | Abs: {abs(v2):.2f} | Call: {v1(lambda x, y: x + y)}')

# Descriptors Protocol (__get__, __set__, __set_name__)
# -> Lombok / Field Validation Annotations
class Validator:
  def __set_name__(self, owner, name): self.name = name
  def __get__(self, inst, owner): return inst.__dict__.get(self.name) if inst else self
  def __set__(self, inst, val):
    if val < 0: raise ValueError(f'{self.name} Pos')
    inst.__dict__[self.name] = val
class Product:
  price = Validator() # Auto Intercept Getter/Setter Của price
  def __init__(self, price): self.price = price
p = Product(100)
print(p.price)

# Metaclasses: Intercepts & Customizes Class Creation
# -> Reflection / Annotation Processor / Class Factory
class AutoRegister(type):
  registry = {}
  def __new__(mcs, name, bases, ns):
    cls = super().__new__(mcs, name, bases, ns)
    if bases: mcs.registry[name] = cls
    return cls
class Plugin(metaclass=AutoRegister): pass
class PluginA(Plugin): pass
print(list(AutoRegister.registry.keys()))

# ABC vs Protocol vs Abstract Class With Shared Logic
# ABC -> public abstract class | Protocol -> public interface
class Repository(ABC):      # public abstract class Repository
  def __init__(self): self._cache = {}
  @abstractmethod           # abstract void _fetchRaw(String key);
  def _fetchRaw(self, key): pass
  def find(self, key):      # Template Method Pattern (logic dùng chung)
    if key not in self._cache: self._cache[key] = self._fetchRaw(key)
    return self._cache[key]
class MemRepo(Repository):  # public class MemRepo extends Repository
  def _fetchRaw(self, key): return f"Data({key})"
@runtime_checkable
class Renderable(Protocol): # public interface Renderable { String render(); }
  def render(self) -> str: ...
class UIWidget:             # public class UIWidget implements Renderable -> Structural Typing
  def render(self): return 'WidgetUI'
repo = MemRepo()