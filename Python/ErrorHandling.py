import traceback
from contextlib import suppress

def fileReader(path):
  file = None
  try:
    file = open(path, "r")
    content = file.read()
    lines = content.split("\n")
    return lines
  except FileNotFoundError:
    print(f'File Not Found: {path}')
    return []
  except PermissionError:
    print(f'Permission Denied: {path}')
    return []
  except Exception as err:
    print(f'Unexpected Error: {err}')
    raise
  else:
    print('Successfully!')
  finally:
    if file:
      file.close()
    print('Done!')
result = fileReader('VAK.txt')
print(result)

# Exception Chaining
class DatabaseError(Exception):
  pass
class ConnectionError(DatabaseError):
  pass
class QueryError(DatabaseError):
  pass
def connectToDb(host):
  raise ConnectionRefusedError(f"Unable To Connect To:{host}")
def runQuery(host, sql):
  try:
    connectToDb(host)
  except ConnectionRefusedError as err:
    raise ConnectionError("Database Unavailable!") from err
def fetchUsers(host):
  try:
    runQuery(host, "SELECT * FROM users")
  except ConnectionError as err:
    print(f'Connection Error: {err}')
    print(f'Original Cause: {err.__cause__}')
fetchUsers("db.backend.com")

# Custom Exception Hierarchy:
class AppError(Exception):
  def __init__(self, message, code=None):
    super().__init__(message)
    self.code = code
class ValidationError(AppError):
  pass
class AuthError(AppError):
  pass
class NotFoundError(AppError):
  pass
def getUser(userId):
  if not isinstance(userId, int):
    raise ValidationError('User ID Must Be Integer!', code=400)
  if userId == 0:
    raise AuthError('Anonymous User Not Allowed!', code=401)
  if userId > 1000:
    raise NotFoundError(f'User Not Found: {userId}', code=404)
  return f'User: {userId}'
for uid in ['VAK', 0, 9999, 42]:
  try:
    print(f'Retrieve User: {getUser(uid)}')
  except ValidationError as err:
    print(f'ValidationError Code: {err.code} - {err}')
  except AuthError as err:
    print(f'AuthError Code: {err.code} - {err}')
  except NotFoundError as err:
    print(f'NotFoundError Code: {err.code} - {err}')
for uid in ['VAK', 0, 9999, 42]:
  try:
    print(f'Retrieve User: {getUser(uid)}')
  except AppError as err:
    print(f'AppError Code: {err.code} - {err}')

# contextlib.suppress
with suppress(FileNotFoundError):
  open("VAK.txt")
# Prevent Original Exception From Being Swallowed By Cleanup Exception
def riskyCleanup():
  raise RuntimeError("Cleanup Failed!")
def doWork():
  try:
    raise ValueError("Work Failed")
  finally:
    try:
      riskyCleanup()
    except RuntimeError as cleanupErr:
      print(cleanupErr) # Finally Exception Resolved -> Allow Try Exception To Continue
try:
  doWork()
except ValueError as e:
  print(e) # Work Failed

# Traceback Module: Trace Thầm Lặng Mà Ko Làm Dừng App
def deepFunction():
  return 1 / 0
def middleFunction():
  return deepFunction()
def topFunction():
  try:
    middleFunction()
  except ZeroDivisionError:
    traceback.print_exc() # -> e.printStackTrace()