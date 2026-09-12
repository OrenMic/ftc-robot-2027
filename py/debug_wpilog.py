#!/usr/bin/env python3
import struct

def read_varint(data, offset):
    result = 0
    shift = 0
    while True:
        if offset >= len(data):
            raise ValueError('Unexpected end of data')
        byte = data[offset]
        offset += 1
        result |= (byte & 0x7F) << shift
        if (byte & 0x80) == 0:
            break
        shift += 7
    return result, offset

with open(r'C:\Users\orenm\code\miscar\2026-robot-hamseason\robot-2026-hamseason\logs\akit_26-09-06_16-23-25_wheelTesting2.wpilog', 'rb') as f:
    data = f.read()

print(f'Total size: {len(data)} bytes')
print(f'Magic: {data[0:6]}')
version = struct.unpack_from('<H', data, 6)[0]
extra_len = struct.unpack_from('<I', data, 8)[0]
extra = data[12:12 + extra_len].decode('utf-8', errors='replace')
print(f'Version: {version}, Extra len: {extra_len}, Extra: {extra!r}')

offset = 12 + extra_len
print(f'Starting records at offset {offset}')
print(f'Next 20 bytes hex: {data[offset:offset+20].hex(" ")}')

# Try reading first record
payload_size, new_offset = read_varint(data, offset)
print(f'First record payload_size: {payload_size}, new offset: {new_offset}')
print(f'Next 40 bytes hex: {data[new_offset:new_offset+40].hex(" ")}')
type_byte = data[new_offset]
print(f'Type byte: {type_byte} (0=data, 1=metadata)')

if type_byte == 1:
    # Metadata record - parse it
    off = new_offset + 1
    eid, off = read_varint(data, off)
    nlen, off = read_varint(data, off)
    name = data[off:off+nlen].decode('utf-8', errors='replace')
    off += nlen
    tlen, off = read_varint(data, off)
    type_str = data[off:off+tlen].decode('utf-8', errors='replace')
    off += tlen
    plen, off = read_varint(data, off)
    props = data[off:off+plen].decode('utf-8', errors='replace')
    off += plen
    print(f'  eid={eid}, name={name!r}, type={type_str!r}, props={props!r}')
    print(f'  metadata consumed to offset: {off} (expected payload end: {new_offset + 1 + payload_size})')
elif type_byte == 0:
    # Data record
    off = new_offset + 1
    eid, off = read_varint(data, off)
    timestamp = struct.unpack_from('<Q', data, off)[0]
    off += 8
    print(f'  eid={eid}, timestamp={timestamp}')
    print(f'  data consumed to offset: {off} (expected payload end: {new_offset + 1 + payload_size})')

# Show more raw hex around the header boundary
print(f'\nHex dump from offset {offset}:')
start = offset
for i in range(0, min(200, len(data) - start), 16):
    chunk = data[start+i:start+i+16]
    hex_str = chunk.hex(' ')
    ascii_str = ''.join(chr(b) if 32 <= b < 127 else '.' for b in chunk)
    print(f'  {start+i:6d}: {hex_str:<48s} {ascii_str}')
