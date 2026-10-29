"""Executa os testes: compila cada tests/*.input para Marie, simula o assembly e compara
com o arquivo .expected correspondente.

Uso (na pasta do projeto, depois de `mvn clean package`):
    py tools/run_tests.py
"""
import glob
import os
import shutil
import subprocess
import sys

sys.path.insert(0, os.path.dirname(__file__))
from marie_sim import run

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAR = os.path.join(ROOT, 'target', 'jacamarie-1.0-jar-with-dependencies.jar')
JAVA = shutil.which('java') or 'java'


def compile_jaca(path):
    cmd = [JAVA, '-Dfile.encoding=UTF-8', '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
           '-jar', JAR, path]
    done = subprocess.run(cmd, capture_output=True, encoding='utf-8')
    return done.returncode, done.stdout, done.stderr


def parse_expected(path):
    fields = {}
    with open(path, encoding='utf-8') as f:
        for line in f:
            if ':' in line:
                key, value = line.split(':', 1)
                fields[key.strip()] = value.strip()
    return fields


def main():
    if not os.path.exists(JAR):
        print('Compile antes: mvn clean package')
        return 1
    failures = 0
    for source in sorted(glob.glob(os.path.join(ROOT, 'tests', '*.input'))):
        name = os.path.splitext(os.path.basename(source))[0]
        expected = parse_expected(source[:-len('.input')] + '.expected')
        code, asm, err = compile_jaca(source)
        if 'error' in expected:
            ok = code != 0 and expected['error'] in err
            detail = err.strip().splitlines()[0] if err.strip() else '(sem mensagem)'
        else:
            if code != 0:
                ok, detail = False, 'falha ao compilar: ' + err.strip()
            else:
                inputs = expected.get('inputs', '').split()
                try:
                    out, steps = run(asm, inputs)
                    got = ' '.join(str(v) for v in out)
                    ok = got == expected.get('output', '')
                    detail = 'saida: %s (%d passos)' % (got or '(vazia)', steps)
                    if not ok:
                        detail += ' | esperado: ' + expected.get('output', '')
                except Exception as e:
                    ok, detail = False, 'erro na simulacao: %s' % e
        print('%-5s %-22s %s' % ('OK' if ok else 'FALHA', name, detail))
        failures += 0 if ok else 1
    print('\n%d teste(s) com falha' % failures if failures else '\nTodos os testes passaram')
    return 1 if failures else 0


if __name__ == '__main__':
    sys.exit(main())