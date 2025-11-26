# The Complete Python Mastery Guide

An exhaustive compilation of every advanced Python skill, standard library module, and professional idiom discussed.

---

## 1. Collections & Iteration

### `collections` Module
*   **`Counter`**: Counts hashable objects (`most_common`, arithmetic operations).
*   **`defaultdict`**: Dictionary with auto-initialized values (no `KeyError`).
*   **`namedtuple`**: Lightweight, immutable object classes (memory efficient).
*   **`deque`**: Double-ended queue, O(1) appends/pops from both ends.
*   **`ChainMap`**: Logically merges multiple dictionaries for lookup (e.g., config + defaults).
*   **`UserDict` / `UserList`**: Proper base classes for custom dictionaries/lists (avoid inheriting `dict`/`list`).

### `itertools` Module
*   **`cycle`**: Infinite loop over an iterable.
*   **`combinations`**: Subsequences of length `r` (no replacement, order doesn't matter).
*   **`permutations`**: Subsequences where order matters.
*   **`combinations_with_replacement`**: Allow elements to be chosen more than once.
*   **`starmap`**: Apply function to argument tuples (unpacking).
*   **`accumulate`**: Return running totals or results of a binary function.
*   **`takewhile`**: Return elements as long as the predicate is true.
*   **`filterfalse`**: Keep elements where the predicate is `False`.
*   **`groupby`**: Group consecutive elements by a key (requires sorted input).
*   **`chain`**: Treat multiple iterables as a single stream.

### Other Iteration Tools
*   **`next(iterator, default)`**: Retrieve next item, with optional default to avoid `StopIteration`.
*   **`more-itertools`**: Community standard for extra tools (`chunked`, `windowed`).
*   **`enumerate`**: Get index and value.
*   **`zip`**: Combine iterables.

---

## 2. Functional Programming & Operators

### `functools` Module
*   **`lru_cache`**: Decorator for instant memoization (caching results).
*   **`partial`**: Pre-fill function arguments to create new functions.
*   **`reduce`**: Collapse an iterable to a single value.
*   **`singledispatch`**: Function overloading based on the type of the first argument.
*   **`wraps`**: Decorator to preserve metadata (`__name__`, `__doc__`) of the wrapped function.

### `operator` Module
*   **`itemgetter`**: Efficiently fetch items by index/key (great for `sorted` keys).
*   **`attrgetter`**: Efficiently fetch attributes.
*   **`methodcaller`**: Call a method on an object.
*   **Math Operators**: `add`, `mul`, etc. (faster than lambdas).

---

## 3. Data Structures & Algorithms

### Built-in Structures
*   **`dataclasses`**: Boilerplate-free classes (`@dataclass`).
*   **`heapq`**: Min-heap priority queue (`heappush`, `heappop`, `nlargest`).
*   **`bisect`**: Binary search and sorted insertions (`insort`, `bisect_left`).
*   **`array`**: Memory-efficient arrays for numeric data (C-style).
*   **`weakref`**: References that don't prevent garbage collection.
*   **`weakref.WeakValueDictionary`**: Cache that auto-cleans when objects are unused.
*   **`graphlib.TopologicalSorter`**: Resolve dependencies (Python 3.9+).
*   **`zoneinfo`**: Standard timezone support (Python 3.9+).

### Custom Structures
*   **Min Stack**: Stack that supports `get_min()` in O(1).
*   **`types.SimpleNamespace`**: Quick object with dot-access attributes.
*   **`types.MappingProxyType`**: Read-only view of a dictionary.
*   **`enum`**:
    *   **`Enum`**: Type-safe enumerations.
    *   **`Flag`**: Bitwise enums (combine with `|`).
    *   **`auto`**: Auto-assign values.

---

## 4. System, Files & Networking

### File System
*   **`pathlib`**: Object-oriented filesystem paths (replaces `os.path`).
*   **`shutil`**: High-level file operations (copy tree, archive, move, `which`).
*   **`tempfile`**: Secure creation/cleanup of temporary files (`TemporaryFile`, `TemporaryDirectory`).
*   **`glob`**: File pattern matching (`*.py`).
*   **`fnmatch`**: Unix-style filename pattern matching for strings.
*   **`fileinput`**: Process lines from multiple input files or stdin (pipeline style).
*   **`mmap`**: Memory-map large files to read/search without loading into RAM.
*   **`os.scandir`**: Faster directory traversal than `os.listdir`.
*   **`os.walk`**: Recursive directory tree generator.

### Networking & Web
*   **`ipaddress`**: Parsing and logic for IPv4/IPv6 addresses/subnets.
*   **`socket`**: Low-level networking interface.
*   **`http.server`**: Instant HTTP server (`python -m http.server`).
*   **`urllib.parse`**: URL manipulation (`urlparse`, `parse_qs`, `urlencode`).
*   **`webbrowser`**: Open URLs in the user's default browser.
*   **`smtplib`**: Sending emails via SMTP.
*   **`uuid`**:
    *   **`uuid4`**: Random UUIDs.
    *   **`uuid5`**: Deterministic UUIDs (namespace based).

### Serialization & Formats
*   **`json`**: Custom encoders via `JSONEncoder` subclassing.
*   **`csv`**: robust CSV parsing (`DictReader`, handling quotes).
*   **`sqlite3`**: Built-in serverless SQL database.
*   **`pickle`**: Python object serialization (unsafe for untrusted data).
*   **`shelve`**: Persistent dictionary backed by a file.
*   **`xml.etree.ElementTree`**: Standard XML parsing.
*   **`base64`**: Binary-to-text encoding.

---

## 5. Concurrency & Parallelism

### `threading`
*   **`Event`**: Signal between threads (wait/set).
*   **`Barrier`**: Wait for N threads to reach a point.
*   **`Lock` / `RLock`**: Mutexes.

### `multiprocessing`
*   **`shared_memory`**: Zero-copy data sharing between processes (Python 3.8+).

### `concurrent.futures`
*   **`ThreadPoolExecutor`**: High-level threading.
*   **`ProcessPoolExecutor`**: High-level multiprocessing.

### `asyncio`
*   **`TaskGroup`**: Structured concurrency (Python 3.11+).
*   **`to_thread`**: Run blocking IO in a separate thread (Python 3.9+).

### Other
*   **`queue`**: Thread-safe queues (`Queue`, `LifoQueue`, `PriorityQueue`).
*   **`contextvars`**: Async-safe global state (storage local to a Task).
*   **`sched`**: In-process event scheduler.

---

## 6. Debugging, Profiling & Quality

### Debugging
*   **`breakpoint()`**: Drops into the debugger (PDB).
*   **F-String Debug**: `f"{var=}"` prints `var=value`.
*   **`traceback`**: Print full stack traces (`print_exc`).
*   **`dis`**: Disassemble Python code to bytecode.
*   **`inspect`**: Introspect live objects, stack frames, and source code.
*   **`__repr__` vs `__str__`**: `__repr__` for devs (unambiguous), `__str__` for users.

### Profiling & Optimization
*   **`timeit`**: Benchmark small code snippets accurately.
*   **`cProfile`**: Profile entire applications to find bottlenecks.
*   **`sys.getsizeof`**: Check memory usage of an object.
*   **`__debug__`**: Constant that is `True` unless running with `-O`.

### Testing
*   **`unittest.mock`**: Create fake objects (`MagicMock`) and assert calls.
*   **`doctest`**: Write and run tests inside docstrings.

---

## 7. Security & Cryptography

*   **`secrets`**: Cryptographically secure random numbers (passwords, tokens).
*   **`hashlib`**: Hashing algorithms (SHA256, MD5).
*   **`hmac`**: Keyed-hashing for message signing/verification.
*   **`getpass`**: Secure password input in CLI (hidden typing).
*   **`html`**: Escape user input (`html.escape`) to prevent XSS.
*   **`ast.literal_eval`**: Safely evaluate strings containing Python literals (safer than `eval`).

---

## 8. Advanced Language Idioms & Features

### Syntax & Operators
*   **Walrus Operator `:=`**: Assignment inside expressions (Python 3.8+).
*   **Dict Union `|`**: Merge dictionaries cleanly (Python 3.9+).
*   **Extended Unpacking**: `first, *rest = data`.
*   **Transpose**: `zip(*matrix)`.
*   **`else` in Loops**: Runs if loop completes without `break`.
*   **`yield from`**: Delegate to another generator.
*   **`Ellipsis` (`...`)**: Placeholder or slicing syntax.

### Class Magic
*   **`__slots__`**: Memory optimization (disable `__dict__`).
*   **`__call__`**: Make instances callable like functions.
*   **`__missing__`**: Handle missing keys in dict subclasses.
*   **`__getattr__`**: Handle missing attribute access dynamically.
*   **`__new__`**: Object creation (Singleton pattern).
*   **`__init_subclass__`**: Hook for plugin registration/validation.
*   **Descriptors**: Customize attribute access (`__get__`, `__set__`).
*   **`abc`**: Abstract Base Classes (`@abstractmethod`).

### Typing
*   **`typing.Protocol`**: Structural typing (Duck typing).
*   **`typing.Annotated`**: Attach metadata to types.
*   **`typing.Generic`**: Create custom generic classes.

---

## 9. Utilities & "Deep Cuts"

*   **`subprocess`**: Run external commands securely.
*   **`argparse`**: Build professional CLIs.
*   **`logging`**: Flexible application logging.
*   **`contextlib`**:
    *   `suppress`: Ignore exceptions.
    *   `redirect_stdout`: Capture print output.
    *   `@contextmanager`: Easy context managers.
*   **`decimal`**: Exact financial arithmetic.
*   **`fractions`**: Exact rational arithmetic.
*   **`statistics`**: Mean, median, mode, stdev.
*   **`math.isclose`**: Correctly compare floating point numbers.
*   **`colorsys`**: Convert between RGB, HSV, HLS.
*   **`textwrap`**: Format/indent text for CLI output.
*   **`difflib`**: Compare sequences (`get_close_matches`, `HtmlDiff`).
*   **`reprlib`**: Safe `repr` for huge objects (auto-truncation).
*   **`pprint`**: Pretty print complex data structures.
*   **`shlex`**: Parse shell-like strings (handling quotes).
*   **`atexit`**: Register cleanup functions to run on termination.
*   **`signal`**: Handle signals like Ctrl+C (`SIGINT`).
*   **`sys.setrecursionlimit`**: Increase stack depth for deep recursion.
*   **`sys.excepthook`**: Global handler for uncaught exceptions.
*   **`gc`**: Interface to the Garbage Collector.
*   **`runpy`**: Execute modules dynamically.
*   **`importlib.metadata`**: Get installed package versions/info.
*   **`importlib.resources`**: Read data files inside packages.
*   **`zipapp`**: Bundle projects into single executable files.
*   **`ctypes`**: Call C shared libraries directly.
*   **`cmd`**: Framework for building interactive shells.
*   **`rlcompleter`**: Tab completion for interactive shells.
*   **`warnings`**: Issue deprecation warnings.
*   **`time.monotonic`**: Robust timing (unaffected by system clock changes).
*   **`lzma` / `gzip` / `zipfile` / `tarfile`**: Compression and archiving.
*   **`slice` objects**: Reusable slice definitions.
*   **`NotImplemented`**: Signal for operator overloading fallback.
*   **`__all__`**: Control public API exports.
