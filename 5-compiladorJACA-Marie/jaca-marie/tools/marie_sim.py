import sys, re

def s16(v):
    v &= 0xFFFF
    return v - 0x10000 if v & 0x8000 else v

def assemble(text):
    lines = []
    for raw in text.splitlines():
        line = raw.split('/')[0].strip()
        if line:
            lines.append(line)
    labels, items = {}, []
    for line in lines:
        label = None
        if ',' in line:
            label, line = [p.strip() for p in line.split(',', 1)]
        parts = line.split()
        op = parts[0].upper()
        arg = parts[1] if len(parts) > 1 else None
        if label:
            if label in labels:
                raise Exception('rotulo duplicado: ' + label)
            labels[label] = len(items)
        items.append((op, arg))
    mem = []
    for op, arg in items:
        if op == 'DEC':
            mem.append(('DATA', s16(int(arg))))
        elif op == 'HEX':
            mem.append(('DATA', s16(int(arg, 16))))
        else:
            mem.append((op, arg))
    return mem, labels

def run(text, inputs, limit=2000000):
    mem, labels = assemble(text)
    def addr(a):
        if a not in labels:
            raise Exception('rotulo indefinido: ' + a)
        return labels[a]
    def val(a):
        kind, v = mem[a]
        if kind != 'DATA':
            raise Exception('leitura de instrucao como dado em %d' % a)
        return v
    ac, pc, out, inp, steps = 0, 0, [], list(inputs), 0
    while True:
        steps += 1
        if steps > limit:
            raise Exception('limite de passos')
        op, arg = mem[pc]
        if op == 'DATA':
            raise Exception('executando dado em %d' % pc)
        pc += 1
        if op == 'LOAD': ac = val(addr(arg))
        elif op == 'STORE': mem[addr(arg)] = ('DATA', ac)
        elif op == 'ADD': ac = s16(ac + val(addr(arg)))
        elif op == 'SUBT': ac = s16(ac - val(addr(arg)))
        elif op == 'CLEAR': ac = 0
        elif op == 'INPUT': ac = s16(int(inp.pop(0)))
        elif op == 'OUTPUT': out.append(ac)
        elif op == 'JUMP': pc = addr(arg)
        elif op == 'JNS':
            a = addr(arg); mem[a] = ('DATA', pc); pc = a + 1
        elif op == 'JUMPI': pc = val(addr(arg))
        elif op == 'SKIPCOND':
            c = int(arg, 16)
            if (c == 0x000 and ac < 0) or (c == 0x400 and ac == 0) or (c == 0x800 and ac > 0): pc += 1
        elif op == 'HALT': return out, steps
        else: raise Exception('instrucao desconhecida: ' + op)

if __name__ == '__main__':
    asm = open(sys.argv[1], encoding='utf-8').read()
    ins = sys.argv[2:]
    out, steps = run(asm, ins)
    print(' '.join(str(x) for x in out) if out else '(sem saida)')