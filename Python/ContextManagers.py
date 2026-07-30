import time
import threading
from contextlib import contextmanager, suppress, ExitStack

# Context Manager Protocol (Java AutoCloseable)
# __enter__: Run On Entering With Block -> Return Value Binds To 'as'
# __exit__: Receive *args -> Return True Suppresses Exception
class ManagedConn:
  def __init__(self, host):
    self.host = host
  def __enter__(self):
    return f'Conn:{self.host}'
  def __exit__(self, excType, excVal, tb): # *args
    if excType is ValueError:              # args[0]
      return True # Suppress Exception
    return False  # Propagate Exception
with ManagedConn('db.com') as conn:
  print(conn)     # Conn:db.com
with ManagedConn('cache.com') as conn:
  raise ValueError('Invalid!') # Suppressed -> Code Continues

# Context Manager
@contextmanager
def timer(label):
  # __enter__
  start = time.perf_counter()
  try:
    yield # 'as'
  # __exit__
  finally:
    print(label, time.perf_counter() - start)
with timer('OK'):
  data = sorted(range(10000), reverse=True)

# contextlib.suppress & ExitStack
# suppress -> Ignore Specified Exceptions
with suppress(FileNotFoundError, KeyError):
  _ = {'a': 1}['z'] # Suppressed
# with: Fixed Resources
# ExitStack: Dynamic Resources In Loops (Auto Close All On Exit)
with ExitStack() as stack:
  files = [stack.enter_context(open(f)) for f in ['V.py', 'K.py']]
  print(len(files))
# Enter 'with ExitStack()': Create Internal Cleanup Stack
# Loop & Call 'stack.enter_context()': Open Files & Push __exit__ To Stack
# Execute -> print()
# Exit -> Pop & Call __exit__ On All Opened Files (Auto Close LIFO)

# Custom ManagedLock: Thread Safety With Timeout
class ManagedLock:
  def __init__(self, timeout=5.0):
    self._lock = threading.Lock()
    self.timeout = timeout
  def __enter__(self): # acquire(timeout): Prevent Infinite Wait -> Raise TimeoutError If Unavailable
    if not self._lock.acquire(timeout=self.timeout):
      raise TimeoutError('Lock Timeout') # Abort Infinite Freeze
    return self
  def __exit__(self, excType, excVal, tb): # release(): Release Lock Even If Block Crashes -> Prevent Deadlock
    self._lock.release() # Guarantee Unlock On Exit
    return False
state = {'count': 0}
lock = ManagedLock()
def safeInc():
  with lock:
    state['count'] += 1
threads = [threading.Thread(target=safeInc) for _ in range(50)]
for t in threads: t.start()
for t in threads: t.join()
print(state['count']) # 50

# DB Transaction Pattern (@contextmanager Commit/Rollback)
@contextmanager
def dbTransaction(connStr):
  conn = {'changes': []}
  try:
    yield conn
    print(len(conn['changes'])) # Commit
  except Exception as err:
    print(err) # Rollback
    raise
with dbTransaction('postgres://localhost') as c:
  c['changes'].append('INSERT') # 1 (Commit)
try:
  with dbTransaction('postgres://localhost') as c:
    c['changes'].append('DELETE')
    raise RuntimeError('Rule Failed')
except RuntimeError as e:
  print(e) # Rule Failed