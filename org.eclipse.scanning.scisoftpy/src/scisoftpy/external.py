###
# Copyright 2013 Diamond Light Source Ltd.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###

'''External python caller
'''

import os
if os.name == 'java':
    _isjava = True
    from jython.jycore import ndarray, ndgeneric, scalarToPython #@UnusedImport
else:
    _isjava = False
    from python.pycore import ndarray, ndgeneric, scalarToPython #@Reimport

_env = os.environ


'''
This is a way to create an external python process, execute a function,
with argument passing and output return, plus exception handling

. pickle arguments
. create python process with python path
.   setup import in process
.   unpickle arguments
.   call function
.   handle exceptions
.   pickle output
. unpickle output
. exception raising
'''

from os import path as _path

from cPickle import dump as _psave, load as _pload

def save_args(arg, dir=None): #@ReservedAssignment
    '''Save arguments as files in a temporary directory
    Use pickle for most objects but use NumPy's npy format for arrays
    arg -- sequence of arguments
    
    Return name of directory used
    '''
    import tempfile as _tmp
    d = _tmp.mkdtemp(prefix='ef-args', dir=dir)
    _n, tree = _pickle(d, arg, 0) # pickle non-sequences
    try: # now do argument structure
        f = open(_path.join(d, "tree.pkl"), 'w')
        _psave(tree, f)
    except:
        raise
    else:
        f.close()

    return d

from scisoftpy.io import save as _asave, load as _aload

def _pickle(p, arg, n):
    '''Create structure of all data recursively and replace objects with name.
    It also saves non-sequences.

    Returns tuple of next number and either list, dictionary or name
    '''
    if isinstance(arg, dict):
        d = dict()
        for k in arg:
            n, d[k] = _pickle(p, arg[k], n)
        return n, d
    elif isinstance(arg, (list, tuple)):
        l = list()
        for a in arg:
            n, o = _pickle(p, a, n)
            l.append(o)
        if isinstance(arg, tuple):
            return n, tuple(l)
        return n, l
    elif isinstance(arg, ndarray):
        name = "p%03d.npy" % n
        _asave(_path.join(p, name), arg)
        return n+1, name
    else:
        name = "p%03d.pkl" % n
        if isinstance(arg, ndgeneric):
            arg = scalarToPython(arg)
        try:
            f = open(_path.join(p, name), 'w')
            _psave(arg, f)
        except:
            raise
        else:
            f.close()
        return n+1, name

def load_args(d):
    '''Load arguments from files in a temporary directory
    returns a tuple of arguments 
    '''
    files = os.listdir(d)
    fdict = dict()
    for fa in files:
        fn = _path.join(d, fa)
        if fn.endswith('.pkl'):
            try:
                f = open(fn, 'r')
                fdict[fa] = _pload(f)
            except :
                print
            finally:
                f.close()
        else:
            fdict[fa] = _aload(fn)[0]
            
    return _recreate_args(fdict['tree.pkl'], fdict)

def _recreate_args(arg, fdict):
    '''Unserialize tree from references to dictionary
    '''
    if isinstance(arg, dict):
        d = dict()
        for k in arg:
            d[k] = _recreate_args(arg[k], fdict)
        return d
    elif isinstance(arg, (list, tuple)):
        l = list()
        for a in arg:
            o = _recreate_args(a, fdict)
            l.append(o)
        if isinstance(arg, tuple):
            return tuple(l)
        return l
    else:
        return fdict[arg]

import sys

def wrapper(func):
    '''Decorator to run a function
    '''
    def run_func(*args, **kwargs):
        try:
            ret = func(*args, **kwargs)
        except Exception:
            import traceback
            ex_type, ex_value, tb = sys.exc_info()
            error = ex_type, ex_value, traceback.extract_tb(tb)[1:]
            ret = None
        else:
            error = None
        
        return (ret, error)

    return run_func

def pyenv(exe=None, path=None, ldpath=None):
    '''Get python environment
    exe -- python executable
    path -- list of paths
    ldpath -- list of dynamic library paths
    return tuple containing python executable string, python path as list 
    '''

#    print 'ScisoftPy package is in', pkg
    if _isjava:
        pyexe, pypath, pyldpath = _cached_pyenv

        if exe:
            pyexe = exe
    
        if path:
#            pypath = [ p for p in sys.path if not p.endswith('jar') ]
            pypath = list(path)
    
        if ldpath:
            pyldpath = list(ldpath)

    else:
        if exe is None:
            pyexe = sys.executable
        else:
            pyexe = exe
    
        if path is None:
            pypath = [ p for p in sys.path if not p.endswith('jar') ]
        else:
            pypath = list(path)
    
        if ldpath:
            pyldpath = ldpath
        else:
            pyldpath = None

    # add current package
    h, _t = _path.split(__file__)
    if '__pyclasspath__' in h:
        _h, t = _path.split(h)
        cp = [ p for p in _env['CLASSPATH'].split(os.pathsep) if not p.endswith('jar') ]
        for p in cp:
            f = _path.join(p, t)
            if _path.exists(f):
                pkg = p
                break
        else:
            raise RuntimeError, "Cannot find ScisoftPy in PYTHONPATH"
    else:
        pkg, _t = _path.split(h)
    pypath.insert(0, pkg)

    return pyexe, pypath, pyldpath


_dls_modules = dict() # cache for modules

def get_dls_module(module='numpy', module_init='/etc/profile.d/modules.sh'):
    if module in _dls_modules:
        return _dls_modules[module]

    if sys.platform == 'win32':
        raise ValueError, "Cannot use dls_module argument on Windows"
    elif sys.platform == 'darwin':
        raise ValueError, "Cannot use dls_module argument on Mac OS X"
    elif not sys.platform.startswith('linux') and not sys.platform.startswith('java'):
        print "Warning dls_module argument may not work"

    env = dict(_env)
    env.pop('PYTHONPATH', None)
    import subprocess as sub
    p = sub.Popen(['bash', '-l'], shell=False, env=env, stdin=sub.PIPE, stdout=sub.PIPE, stderr=sub.PIPE)
    p.stdin.write('source %s\n' % module_init)
    p.stdin.write('module load %s\n' % module)
    p.stdin.write('pyexe=$(which python)\n')
    p.stdin.write('echo "EXEC:$pyexe"\n')
    p.stdin.write('echo "PATH:$PYTHONPATH"\n')
    p.stdin.write('echo "LDPATH:$LD_LIBRARY_PATH"\n')
    p.stdin.close()
    exe, path, ldpath = parse_for_env(p.stdout)
    if exe is None:
        raise RuntimeError, "Problem with running external process: %s" % p.stderr.read()
    _dls_modules[module] = exe, path, ldpath
    return exe, path, ldpath

def get_python():
    env = dict(_env)
    env.pop('PYTHONPATH', None)
    import subprocess as sub
    p = sub.Popen('python', shell=False, env=env, stdin=sub.PIPE, stdout=sub.PIPE, stderr=sub.PIPE)
    p.stdin.write('import sys\n')
    p.stdin.write('print "EXEC|%s" % sys.executable\n')
    p.stdin.write('print "PATH|%s" % "|".join(sys.path)\n')
    p.stdin.write('import os\n')
    p.stdin.write('if sys.platform == "win32":\n')
    p.stdin.write('    key = "PATH"\n')
    p.stdin.write('elif sys.platform == "darwin":\n')
    p.stdin.write('    key = "DYLD_LIBRARY_PATH"\n')
    p.stdin.write('else:\n')
    p.stdin.write('    key = "LD_LIBRARY_PATH"\n')
    p.stdin.write('lp = os.environ[key].split(os.pathsep)\n')
    p.stdin.write('print "LDPATH|%s" % "|".join(lp)\n')
    p.stdin.close()
    exe, path, ldpath = parse_for_env(p.stdout, sep='|')
    if exe is None:
        raise RuntimeError, "Problem with running external process: %s" % p.stderr.read()
    return exe, path, ldpath

def parse_for_env(stream, sep=':'):
    exe = None
    path = None
    ldpath = None
    while True:
        l =  stream.readline()
        if not l:
            break
        l = l.strip()
        if l:
            r = l.split(sep)
            if r[0] == 'EXEC':
                exe = r[1]
            elif r[0] == 'PATH':
                path = [a for a in r[1:] if a]
            elif r[0] == 'LDPATH':
                ldpath = [a for a in r[1:] if a]

    return exe, path, ldpath

if _isjava:
    _cached_pyenv = get_python()

def find_module_path(path, module):
    modulefile = module +".py"
    for p in path:
        p = _path.abspath(p)
        if _path.exists(_path.join(p, module)):
            return p
        if _path.exists(_path.join(p, modulefile)):
            return p
    return None

if _isjava:
    # need Java class as the Python code below does not work in Jython!!!
    from org.eclipse.scanning.scisoftpy.python import PythonSubProcess
else:
    from Queue import Queue, Empty
    from threading import Thread
    from subprocess import Popen, PIPE
    cmds='''import sys
while True:
  print 'READY'
  sys.stdout.flush()
  l = sys.stdin.readline()
  if not l:
    break
  exec l
'''
    class StreamHandler(object):
        def __init__(self, stream):
            self.stream = stream
            self.alive = True
            self.out = Queue()
            def add():
                while self.alive:
                    line = self.stream.readline()
                    if line:
                        self.out.put(line)
                    else:
                        break
            self.thd = Thread(target=add)
            self.thd.daemon = True
            self.thd.start()

        def readline(self, timeout=None):
            try:
                return self.out.get(block=True, timeout=timeout)
            except Empty:
                return None

        def kill(self):
            self.alive = False

        def clear(self):
            while self.out.qsize() > 0:
                self.out.get_nowait()

    class PythonSubProcess(object):
        READY = 'READY\n'
        TIMEOUT = 0.005
        def __init__(self, exe="python", env=None):
            self.proc = Popen([exe, '-c', cmds], bufsize=1, env=env, stdin=PIPE, stdout=PIPE, stderr=PIPE)
            self.out = StreamHandler(self.proc.stdout)
            self.err = StreamHandler(self.proc.stderr)
            self.stdin = self.proc.stdin
            from time import sleep
            sleep(0.5)

            l = self.out.readline()
            if l != self.READY:
                if l is None:
                    l = "None"
                el = self.err.readline(self.TIMEOUT)
                if el is None:
                    el = "None"
                raise OSError, "Problem with python subprocess not being ready: " + l + "; " + el

        def communicate(self, text):
            self._send(text)

            results = []
            while True:
                l = self.out.readline()
                if l == self.READY:
                    break
                results.append(l)
            lines = ["".join(results)]
            results = []
            while True:
                l = self.err.readline(self.TIMEOUT)
                if not l:
                    break
                results.append(l)
            if len(results) > 0:
                lines.append("".join(results))
            else:
                lines.append(None)
            return lines

        def _send(self, text):
            self.out.clear()
            self.err.clear()
            self.stdin.write(text)
            self.stdin.flush()

        def stop(self):
            self.stdin.close()

#PYDEV_SRC="/scratch/eclipse441_64/plugins/org.python.pydev_3.9.2.201502050007/pysrc"

class ExternalFunction(object):
    '''Emulates a function object with an attached python process
    '''
    def __init__(self, exe, env, module, function, keep):
        self.exe = exe
        self.env = env
        self.mod = module
#        modules = [(k, v.__name__) for k,v in globals().items() if isinstance(v, type(sys)) and not k.startswith('__')]
#        print func.__name__
#        pprint(modules)
        self.func = function
        self.keep = keep
        self.thd = None
        self.proc = None
        if self.keep:
            self._mk_process()

    def _mk_process(self):
        self.proc = PythonSubProcess(self.exe, self.env)
#         self.proc.stdin.write('import sys\n')
#         self.proc.stdin.write('sys.path.append("%s")\n' % PYDEV_SRC)
        _out, err = self.proc.communicate('from scisoftpy import external as _fwext\n')
        if err:
            raise RuntimeError, "Problem with import: %s" % err
        _out, err = self.proc.communicate('from %s import %s\n' % (self.mod, self.func))
        if err:
            raise RuntimeError, "Problem with import: %s" % err
        _out, err = self.proc.communicate('_fwwrapped = _fwext.wrapper(%s)\n' % self.func)
        if err:
            raise RuntimeError, "Problem with wrapping: %s" % err

    def stop(self):
        '''Stop process
        '''
        if self.proc:
            self.proc.stop()
            self.proc = None

    def __del__(self):
        self.stop()

    def __call__(self, *arg, **kwarg):
        import shutil
        argsdir = save_args((arg, kwarg))
        try:
            if not self.keep or not self.proc:
                self._mk_process()
            out, err = self.proc.communicate('_fwiarg, _fwikwarg = _fwext.load_args(\"%s\")\n' % argsdir)
#             print >> sys.stderr, "1Out:", out
#             print >> sys.stderr, "1Err:", err
            if err:
                raise RuntimeError, "Problem with running external process: %s" % err

            out, err = self.proc.communicate('print "FWOUT:|%s|" % _fwext.save_args(_fwwrapped(*_fwiarg, **_fwikwarg))\n')
#             print >> sys.stderr, "2Out:", out
#             print >> sys.stderr, "2Err:", err

            if out:
                for l in out.splitlines():
#                     print >> sys.stderr, "3Out:", l
                    if not l:
                        continue
                    l = l.strip()
                    if l.startswith('FWOUT'):
                        r = l.split('|')
                        if len(r) > 1:
                            d = r[1]
                            try:
                                ret, err = load_args(d)
                                if err:
                                    import traceback
                                    print >> sys.stderr, '\n'.join(traceback.format_list(err[2]))
                                    raise err[1]
                                return ret
                            finally:
                                shutil.rmtree(d)
                    else:
                        print l
            if err:
                raise RuntimeError, "Problem with saving results: %s" % err
        finally:
            shutil.rmtree(argsdir)
            if not self.keep:
                self.stop()

def create_function(function, module=None, exe=None, path=None, extra_path=None, dls_module=None, keep=True):
    '''Create a function that will run in an external python

    function -- function or its name, if the former then module is not needed
    module -- name of module
    exe -- path of Python executable
    path -- list of Python paths
    extra_path -- list of extra Python (prepended) paths for local packages
    dls_module -- if True, use 'numpy', else if string use as module name to obtain python parameters
    keep -- if True, keep process alive

    returns a function object

    For example, you have a module called blah with a function foo then
    >>> ext_foo = create_function("foo", "blah", dls_module=True)
    >>> ext_foo(1.2, 3.4, k=True)
    
    If blah is in your current python path then,
    >>> from blah import foo
    >>> ext_foo = create_function(foo, dls_module=True)
    
    The dls_module argument allows a python configuration from DLS's module system
    to be used to set up the external python process. 
    '''

    if not isinstance(function, str):
        fn = function
        function = fn.__name__
        if fn.__module__ == '__main__':
            raise RuntimeError, "Cannot create function as it needs to be in a module of its own"
        if module is None:
            module = fn.__module__

    ldpath = None
    if dls_module:
        if isinstance(dls_module, str):
            exe, path, ldpath = get_dls_module(dls_module)
        else:
            exe, path, ldpath = get_dls_module()
    exe, path, ldpath = pyenv(exe, path, ldpath)

    p = find_module_path(path, module)
    if p is None:
        p = find_module_path(sys.path, module)
    if p and p not in path:
        path.insert(0, p)
    if extra_path:
        if p is None:
            p = find_module_path(extra_path, module)
        path = extra_path + path
    if p is None:
        raise ValueError, "Cannot find module in path: try specifying it in extra_path"
    env = dict(_env)
    env['PYTHONPATH'] = os.pathsep.join(path)
    if ldpath:
        if sys.platform == 'win32':
            key = 'PATH'
        elif sys.platform == 'darwin':
            key = 'DYLD_LIBRARY_PATH'
        else:
            key = 'LD_LIBRARY_PATH'
        env[key] = os.pathsep.join(ldpath)

    return ExternalFunction(exe, env, module, function, keep)
