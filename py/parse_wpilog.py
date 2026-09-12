#!/usr/bin/env python3
"""Parse WPILOG file to extract robot rotation data."""

import struct
import sys
import json
import math


def read_varint(data, offset):
    result = 0
    shift = 0
    while True:
        if offset >= len(data):
            raise ValueError("Unexpected end of data")
        byte = data[offset]
        offset += 1
        result |= (byte & 0x7F) << shift
        if (byte & 0x80) == 0:
            break
        shift += 7
    return result, offset


def parse_wpilog(path):
    with open(path, 'rb') as f:
        data = f.read()

    magic = data[0:6]
    if magic != b'WPILOG':
        raise ValueError(f"Invalid magic: {magic}")

    version = struct.unpack_from('<H', data, 6)[0]
    extra_len = struct.unpack_from('<I', data, 8)[0]
    extra = data[12:12 + extra_len].decode('utf-8', errors='replace')
    print(f"Version: {version}, Extra: {extra!r}")

    offset = 12 + extra_len
    entries = {}
    all_data = {}
    record_count = 0

    while offset < len(data):
        try:
            rec_start = offset
            payload_size, offset = read_varint(data, offset)
            if payload_size == 0 or offset + payload_size > len(data):
                break
            payload_end = offset + payload_size
            type_byte = data[offset]
            offset += 1

            if type_byte == 0:
                entry_id, offset = read_varint(data, offset)
                timestamp = struct.unpack_from('<Q', data, offset)[0]
                offset += 8
                if entry_id in entries:
                    etype = entries[entry_id]['type']
                    val = None
                    if etype == 2:  # double
                        val = struct.unpack_from('<d', data, offset)[0]
                        offset += 8
                    elif etype == 1:  # float
                        val = struct.unpack_from('<f', data, offset)[0]
                        offset += 4
                    elif etype == 5:  # double[]
                        n = struct.unpack_from('<I', data, offset)[0]
                        offset += 4
                        val = [struct.unpack_from('<d', data, offset + i*8)[0] for i in range(n)]
                        offset += n * 8
                    elif etype == 4:  # float[]
                        n = struct.unpack_from('<I', data, offset)[0]
                        offset += 4
                        val = [struct.unpack_from('<f', data, offset + i*4)[0] for i in range(n)]
                        offset += n * 4
                    elif etype == 0:  # boolean
                        val = data[offset] != 0
                        offset += 1
                    elif etype == 3:  # string
                        slen, offset = read_varint(data, offset)
                        val = data[offset:offset+slen].decode('utf-8', errors='replace')
                        offset += slen
                    else:
                        offset = payload_end
                    if val is not None:
                        if entry_id not in all_data:
                            all_data[entry_id] = []
                        all_data[entry_id].append((timestamp, val))
                else:
                    offset = payload_end

            elif type_byte == 1:
                eid, offset = read_varint(data, offset)
                nlen, offset = read_varint(data, offset)
                name = data[offset:offset+nlen].decode('utf-8', errors='replace')
                offset += nlen
                tlen, offset = read_varint(data, offset)
                type_str = data[offset:offset+tlen].decode('utf-8', errors='replace')
                offset += tlen
                plen, offset = read_varint(data, offset)
                props = data[offset:offset+plen].decode('utf-8', errors='replace')
                offset += plen
                entries[eid] = {'name': name, 'type_str': type_str, 'type': type_code(type_str), 'props': props}

            else:
                offset = payload_end

            record_count += 1
        except Exception as e:
            print(f"Error at {rec_start}: {e}")
            break

    print(f"Records: {record_count}, Entries: {len(entries)}")

    # Print entries
    print("\n=== Entries ===")
    for eid, e in sorted(entries.items()):
        cnt = len(all_data.get(eid, []))
        print(f"  [{eid}] {e['name']} ({e['type_str']}) - {cnt} samples")

    # Find rotation entries
    rot_kw = ['rotation', 'yaw', 'heading', 'angle', 'pose', 'orientation', 'gyro']
    print("\n=== Rotation Entries ===")
    rot_ids = []
    for eid, e in entries.items():
        if any(k in e['name'].lower() for k in rot_kw):
            rot_ids.append(eid)
            samples = all_data.get(eid, [])
            print(f"  [{eid}] {e['name']} ({e['type_str']}) - {len(samples)} samples")
            if samples:
                vals = [v for _, v in samples]
                if e['type'] in (1, 2):  # scalar
                    avg = sum(vals) / len(vals)
                    print(f"    Avg: {avg}, Min: {min(vals)}, Max: {max(vals)}")
                elif e['type'] in (4, 5):  # array
                    n = len(vals[0])
                    for i in range(n):
                        col = [v[i] for v in vals]
                        avg = sum(col) / len(col)
                        print(f"    [{i}] Avg: {avg}, Min: {min(col)}, Max: {max(col)}")

    return entries, all_data


def type_code(s):
    return {'boolean': 0, 'int64': 1, 'float': 1, 'double': 2,
            'string': 3, 'boolean[]': 5, 'int64[]': 6,
            'float[]': 4, 'double[]': 5, 'string[]': 7, 'raw': 8}.get(s, -1)


if __name__ == '__main__':
    path = sys.argv[1] if len(sys.argv) > 1 else r'C:\Users\orenm\code\miscar\2026-robot-hamseason\robot-2026-hamseason\logs\akit_26-09-06_16-23-25_wheelTesting2.wpilog'
    parse_wpilog(path)
