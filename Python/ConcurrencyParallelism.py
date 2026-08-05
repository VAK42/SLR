# Multi-Threading & Thread Safety (GIL, Lock, Semaphore, Thread-Local Storage)
import threading, time, asyncio
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor, as_completed
lock = threading.Lock()
sem = threading.Semaphore(2)
tls = threading.local()
cnt = 0
def incSafe():
  global cnt
  with lock: cnt += 1 # Guarantee Atomic Operation
threads = [threading.Thread(target=incSafe) for _ in range(40)]
for t in threads: t.start()
for t in threads: t.join()
print(cnt) # 40
def worker(idx):
  with sem: # Max 2 Concurrent Threads
    tls.val = idx * 10 # Thread-Local Storage: Each Thread Has Own Variable
    time.sleep(0.01)
    print(idx, tls.val)
workers = [threading.Thread(target=worker, args=(i,)) for i in range(2)]
for w in workers: w.start()
for w in workers: w.join()

# Multi-Processing → Bypass GIL For CPU-Bound Workloads
def cpuTask(n):
  return sum(i ** 2 for i in range(n))
if __name__ == '__main__':
  with ProcessPoolExecutor(max_workers=2) as pool:
    print(list(pool.map(cpuTask, [1000, 2000]))) # [332833500, 2666467000]

# Asyncio - Single-Thread Event Loop
class AsyncRes:
  async def __aenter__(self): return self # Async Context Manager
  async def __aexit__(self, *args): return False
class AsyncCounter:
  def __init__(self, maxVal): self.max = maxVal; self.curr = 0
  def __aiter__(self): return self # Async Iterator
  async def __anext__(self):
    if self.curr >= self.max: raise StopAsyncIteration
    self.curr += 1
    await asyncio.sleep(0.01)
    return self.curr
async def fetchPage(url, delay):
  await asyncio.sleep(delay) # Yield Control To Event Loop
  return f'Content:{url}'
async def mainAsync():
  async with AsyncRes():
    res = await asyncio.gather(fetchPage('a', 0.1), fetchPage('b', 0.05)) # Runs Concurrently
    print(res) # ['Content:a', 'Content:b']
  async for val in AsyncCounter(2):
    print(val) # 1 2
  q = asyncio.Queue()
  await q.put('Item')
  print(await q.get()) # Item (Async Queue Producer-Consumer)
  task = asyncio.create_task(fetchPage('c', 0.01)) # Schedule Background Task
  print(await task) # Content:c
asyncio.run(mainAsync())

# concurrent.futures (ThreadPoolExecutor & ProcessPoolExecutor Unified Interface)
with ThreadPoolExecutor(max_workers=2) as ex:
  futs = [ex.submit(time.sleep, 0.01) for _ in range(2)]
  print([f.done() for f in as_completed(futs)]) # [True, True] (as_completed: Yield Futures As They Finish)