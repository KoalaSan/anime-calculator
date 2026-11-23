#!/usr/bin/env python3
"""Genera assets/banner.png sin librerías externas (PNG RGB escrito a mano)."""
import math
import os
import random
import struct
import zlib

W, H = 1200, 630
OUT = os.path.join(os.path.dirname(os.path.dirname(__file__)), "assets", "banner.png")

FONT = {
    "A": ["01110", "10001", "10001", "11111", "10001", "10001", "10001"],
    "N": ["10001", "11001", "10101", "10011", "10001", "10001", "10001"],
    "I": ["11111", "00100", "00100", "00100", "00100", "00100", "11111"],
    "M": ["10001", "11011", "10101", "10101", "10001", "10001", "10001"],
    "E": ["11111", "10000", "10000", "11110", "10000", "10000", "11111"],
    "C": ["01111", "10000", "10000", "10000", "10000", "10000", "01111"],
    "L": ["10000", "10000", "10000", "10000", "10000", "10000", "11111"],
    "J": ["00111", "00010", "00010", "00010", "10010", "10010", "01100"],
    "V": ["10001", "10001", "10001", "10001", "10001", "01010", "00100"],
    "D": ["11110", "10001", "10001", "10001", "10001", "10001", "11110"],
    "R": ["11110", "10001", "10001", "11110", "10100", "10010", "10001"],
    "O": ["01110", "10001", "10001", "10001", "10001", "10001", "01110"],
    "T": ["11111", "00100", "00100", "00100", "00100", "00100", "00100"],
    "1": ["00100", "01100", "00100", "00100", "00100", "00100", "01110"],
    "2": ["01110", "10001", "00001", "00010", "00100", "01000", "11111"],
    ".": ["00000", "00000", "00000", "00000", "00000", "01100", "01100"],
    " ": ["00000"] * 7,
}

pixels = bytearray(W * H * 3)


def put(x, y, color, alpha=1.0):
    if not (0 <= x < W and 0 <= y < H) or alpha <= 0:
        return
    i = (y * W + x) * 3
    for k in range(3):
        pixels[i + k] = int(pixels[i + k] * (1 - alpha) + color[k] * alpha)


def lerp(a, b, t):
    return tuple(int(a[k] + (b[k] - a[k]) * t) for k in range(3))


def gradient():
    stops = [
        (0.00, (13, 9, 32)),
        (0.42, (52, 25, 93)),
        (0.74, (126, 49, 131)),
        (1.00, (255, 132, 178)),
    ]
    for y in range(H):
        for x in range(W):
            t = 0.42 * (x / W) + 0.58 * (y / H)
            for i in range(len(stops) - 1):
                p0, c0 = stops[i]
                p1, c1 = stops[i + 1]
                if p0 <= t <= p1:
                    put(x, y, lerp(c0, c1, (t - p0) / (p1 - p0)))
                    break


def circle(cx, cy, r, color, alpha=1.0):
    for y in range(max(0, int(cy - r)), min(H, int(cy + r) + 1)):
        for x in range(max(0, int(cx - r)), min(W, int(cx + r) + 1)):
            d = math.hypot(x - cx, y - cy)
            if d <= r:
                put(x, y, color, alpha * min(1.0, (r - d) / 2 + 0.35))


def star(cx, cy, r, color, alpha=0.9, thickness=3):
    for t in range(-r, r + 1):
        for d in range(-thickness, thickness + 1):
            put(cx + t, cy + d, color, alpha * (1 - abs(d) / (thickness + 1)))
            put(cx + d, cy + t, color, alpha * (1 - abs(d) / (thickness + 1)))


def text(message, x, y, scale, color, glow=True):
    cursor = x
    for ch in message.upper():
        glyph = FONT.get(ch, FONT[" "])
        for row, bits in enumerate(glyph):
            for col, bit in enumerate(bits):
                if bit != "1":
                    continue
                gx = cursor + col * scale
                gy = y + row * scale
                if glow:
                    for oy in (-3, 0, 3):
                        for ox in (-3, 0, 3):
                            put(gx + ox, gy + oy, (255, 111, 169), 0.16)
                for dy in range(scale):
                    for dx in range(scale):
                        put(gx + dx, gy + dy, color)
        cursor += 6 * scale


def mascot():
    cx, cy = 955, 300
    circle(cx, cy, 168, (255, 111, 169), 0.13)
    circle(cx, cy + 10, 152, (181, 123, 255))  # pelo
    circle(cx, cy + 32, 110, (255, 227, 211))  # cara
    # flequillo
    for y in range(cy - 95, cy - 20):
        for x in range(cx - 110, cx + 110):
            if ((x - cx) / 112) ** 2 + ((y - (cy - 70)) / 62) ** 2 <= 1:
                put(x, y, (255, 154, 200))
    circle(cx - 42, cy + 28, 21, (59, 34, 84))
    circle(cx + 42, cy + 28, 21, (59, 34, 84))
    circle(cx - 36, cy + 21, 7, (255, 255, 255))
    circle(cx + 48, cy + 21, 7, (255, 255, 255))
    circle(cx - 68, cy + 58, 20, (255, 127, 168), 0.5)
    circle(cx + 68, cy + 58, 20, (255, 127, 168), 0.5)
    for x in range(cx - 26, cx + 27):
        y = int(cy + 78 - 0.028 * (x - cx) ** 2)
        for d in (-1, 0, 1):
            put(x, y + d, (179, 68, 110))
    circle(cx + 98, cy - 82, 22, (255, 225, 240))
    circle(cx + 98, cy - 82, 8, (255, 180, 63))


def write_png(path):
    raw = b"".join(b"\x00" + bytes(pixels[y * W * 3:(y + 1) * W * 3]) for y in range(H))

    def chunk(tag, data):
        return (struct.pack(">I", len(data)) + tag + data
                + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 2, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


gradient()
random.seed(7)
for _ in range(46):
    circle(random.randint(0, W), random.randint(0, H), random.randint(3, 22),
           (255, 220, 240), random.uniform(0.05, 0.14))
for _ in range(14):
    star(random.randint(40, W - 40), random.randint(30, H - 30),
         random.randint(6, 16), (255, 236, 190), 0.7, 2)
mascot()
text("ANIME CALC", 82, 215, 9, (255, 255, 255))
text("JAVA  ANDROID", 86, 368, 4, (255, 214, 233))
os.makedirs(os.path.dirname(OUT), exist_ok=True)
write_png(OUT)
print("Generado", OUT, os.path.getsize(OUT), "bytes")
